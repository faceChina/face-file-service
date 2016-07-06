/*
 * (C) 2007-2010 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */
package com.taobao.common.tfs;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.common.tfs.etc.TfsConstant;
import com.taobao.common.tfs.impl.ClientConfig;
import com.taobao.common.tfs.impl.FSName;
import com.taobao.common.tfs.impl.GcWorker;
import com.taobao.common.tfs.impl.TfsFile;
import com.taobao.common.tfs.impl.TfsLargeFile;
import com.taobao.common.tfs.impl.TfsSession;
import com.taobao.common.tfs.impl.TfsSmallFile;
import com.taobao.common.tfs.packet.FileInfo;
import com.taobao.common.tfs.packet.UnlinkFileMessage;
import com.taobao.common.tfs.unique.TairUniqueStore;
import com.taobao.common.tfs.unique.UniqueStore;

public class DefaultTfsManager implements TfsManager {
    private static final Log log = LogFactory.getLog(DefaultTfsManager.class);

    // check file type
    private static final int TFS_INVALID_FILE_TYPE = 1;
    private static final int TFS_SMALL_FILE_TYPE = 2;
    private static final int TFS_LARGE_FILE_TYPE = 3;

    private boolean enable = false;
    private TfsSession tfsSession = new TfsSession();
    private FilePool filePool = new FilePool();
    private boolean runTimer = true;
    private Timer timer = null;

    // for unique store
    private UniqueStore uniqueStore = null;
    private List<String> serverList = null;
    private String groupName = null;
    private int namespace = -1;

    private class FilePool {
        // compatibility to small file's no fd logic.
        private ThreadLocal<TfsSmallFile> fileLocal = new ThreadLocal<TfsSmallFile>();
        private ThreadLocal<TfsLargeFile> largeFileLocal = new ThreadLocal<TfsLargeFile>();
        // large file use fd to support manipulating multifile simultaneously
        // use synchronized method, no atomic need
        private Map<Integer, TfsLargeFile> largeFileMap = new HashMap<Integer, TfsLargeFile>();
        private int globalFd = 0;
        private static final int MAX_FILE_FD = Integer.MAX_VALUE;
        private static final int MAX_OPEN_FD_COUNT = MAX_FILE_FD - 1;

        public TfsSmallFile getFile() {
            if (!enable) {
                doInit();
            }

            TfsSmallFile tfsFile = (TfsSmallFile)fileLocal.get();

            if (tfsFile == null) {
                tfsFile = new TfsSmallFile();
                tfsFile.setSession(tfsSession);
                fileLocal.set(tfsFile);
            }
            return tfsFile;
        }

        public TfsLargeFile getLargeFile() {
            if (!enable) {
                doInit();
            }
            TfsLargeFile tfsFile = (TfsLargeFile)largeFileLocal.get();

            if (tfsFile == null) {
                tfsFile = new TfsLargeFile();
                tfsFile.setSession(tfsSession);
                largeFileLocal.set(tfsFile);
            } else {
                tfsFile.cleanUp();
            }
            return tfsFile;
        }

        public synchronized TfsLargeFile getLargeFile(int fd) throws TfsException {
            TfsLargeFile tfsLargeFile = largeFileMap.get(fd);
            if (tfsLargeFile == null) {
                throw new TfsException("invaild fd: " + fd);
            }
            return tfsLargeFile;
        }

        public synchronized int getLargeFileFd() throws TfsException {
            if (largeFileMap.size() >= MAX_OPEN_FD_COUNT) {
                throw new TfsException("too much file opened.");
            }

            if (globalFd == MAX_FILE_FD) {
                globalFd = 0;
            }

            if (!enable) {
                doInit();
            }

            boolean fdConflict = true;
            int retry = MAX_OPEN_FD_COUNT;
            // fd overlap
            while (retry-- > 0 && (fdConflict = largeFileMap.containsKey(++globalFd))) {
                if (globalFd == MAX_FILE_FD) {
                    globalFd = 0;
                }
            }

            if (fdConflict) {
                throw new TfsException("too much file opened.");
            }

            TfsLargeFile tfsLargeFile = new TfsLargeFile();
            tfsLargeFile.setSession(tfsSession);
            largeFileMap.put(globalFd, tfsLargeFile);

            return globalFd;
        }

        public synchronized void removeFile(int fd) {
            TfsLargeFile tfsFile = largeFileMap.get(fd);
            if (tfsFile != null) {
                tfsFile.cleanUp();
                tfsFile = null;
            }

            largeFileMap.remove(fd);
        }
    }

    /**************************************
     *                                    *
     *     Assorted initialization        *
     *                                    *
     **************************************/

    public DefaultTfsManager() {
    }

    public DefaultTfsManager(boolean runTimer) {
        this.runTimer = runTimer;
    }

    private synchronized int doInit() {
        int ret = TfsConstant.TFS_SUCCESS;

        if (!enable) {
            if (runTimer && timer == null) {
                timer = new Timer();
                timer.schedule((new GcWorker()), 0, ClientConfig.GC_INTERVAL);
                timer.schedule(tfsSession.getCacheMetric(), 0, ClientConfig.CACHEMETRIC_INTERVAL);
            }

            if ((ret = tfsSession.init()) != TfsConstant.TFS_SUCCESS) {
                log.error("init tfs session fail, server: " + getNsip());
            } else {
                enable = true;
            }
        }

        return ret;
    }

    /**
     * lazy init
     *
     */
    public void init() {
    }

    /**
     * destroy and clean
     *
     */
    public void destroy() {
        if (enable && tfsSession != null) {
            tfsSession.destroy();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * set master nameserver ip address
     * for compatibility to original jni interface that
     * only setMaserIp can be done init work
     *
     * @param ipaddr
     * @return
     */
    public synchronized int setMasterIP(String ipaddr) {
        tfsSession.setNameServerIp(ipaddr);

        if (!enable) {
            return doInit();
        }

        int ret = tfsSession.init();
        if (ret != TfsConstant.TFS_SUCCESS) {
            log.error("init tfs session fail, server: " + getNsip());
            // force reinit
            enable = false;
        }

        return ret;
    }

    public String getMasterIP() {
        return tfsSession.getNameServerIp();
    }


    /**
     * check if tfs nameserve is at work
     *
     * @return
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * set TfsSession instance
     *
     * @param tfsSession
     */
    public void setTfsSession(TfsSession tfsSession) {
        this.tfsSession = tfsSession;
    }

    public TfsSession getTfsSession() {
        return tfsSession;
    }

    /**
     * set nameserver ip address
     *
     * @param nsip
     * @return
     */
    public void setNsip(String nsip) {
        tfsSession.setNameServerIp(nsip);
    }

    public String getNsip() {
        return tfsSession.getNameServerIp();
    }

    /**
     * set tfs cluster index (deprecated method)
     * must get cluster index from ns
     *
     * @param tfsClusterIndex
     */
    public void setTfsClusterIndex(char tfsClusterIndex) {
        tfsSession.setTfsClusterIndex(tfsClusterIndex);
    }

    public char getTfsClusterIndex() {
        return tfsSession.getTfsClusterIndex();
    }

    /**
     * TAIR::configserver serverlist
     * @param serverList
     */
    public void setUniqueServerList(List<String> serverList) {
        this.serverList = serverList;
    }

    public List<String> getUniqueServerList() {
        return this.serverList;
    }

    /**
     * TAIR::group name
     *
     * @param groupName
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    /**
     * TAIR:namespace
     *
     * @param namespace
     */
    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }

    public int getNamespace() {
        return this.namespace;
    }

    /**
     * Tair::init() for compatibility to original jni
     *
     * @param serverList
     * @param groupName
     * @param namespace
     */
    public void setUniqueStore(List<String> serverList, String groupName, int namespace) {
        setUniqueServerList(serverList);
        setGroupName(groupName);
        setNamespace(namespace);
    }

    /**
     * max amount of requests(threads) waiting
     *
     * @return
     */
    public void setMaxWaitThread(int maxWaitThread) {
        if (maxWaitThread > 0) {
            tfsSession.setMaxWaitThread(maxWaitThread);
        }
    }

    public int getMaxWaitThread() {
        return tfsSession.getMaxWaitThread();
    }

    /**
     * timeout for one request
     *
     * @param timeout
     */
    public void setTimeout(int timeout) {
        if (timeout > 0) {
            tfsSession.setTimeout(timeout);
        }
    }

    public int getTimeout() {
        return tfsSession.getTimeout();
    }

    /**
     * Various Config.
     * just one global config. so serveral TfsManager's config
     * may work based on config's sequence
     * TODO: refactor TfsManager to be singleton to support multi server
     */

    /**
     * max amount of blockid==>serverlist cache intems
     *
     * @param maxCacheItemCount
     */
    public void setMaxCacheItemCount(int maxCacheItemCount) {
        if (maxCacheItemCount > 0) {
            ClientConfig.CACHEITEM_COUNT = maxCacheItemCount;
        }
    }

    public int getMaxCacheItemCount() {
        return ClientConfig.CACHEITEM_COUNT;
    }

    /**
     * max effecting time of cache
     *
     * @param maxCacheTime
     */
    public void setMaxCacheTime(int maxCacheTime) {
        if (maxCacheTime > 0) {
            ClientConfig.CACHE_TIME = maxCacheTime;
        }
    }

    public int getMaxCacheTime() {
        return ClientConfig.CACHE_TIME;
    }

    /**
     * gc time worker interval
     *
     * @param gcInternal (ms)
     */
    public void setGcInterval(int gcInternal) {
        if (gcInternal > 0) {
            ClientConfig.GC_INTERVAL = gcInternal;
        }
    }

    public int getGcInterval() {
        return ClientConfig.GC_INTERVAL;
    }

    /**
     * cache metric statistics timer worker interval
     *
     * @param cacheMetricInterval
     */
    public void setCacheMetricInterval(int cacheMetricInterval) {
        if (cacheMetricInterval > 0) {
            ClientConfig.CACHEMETRIC_INTERVAL = cacheMetricInterval;
        }
    }

    public int getCacheMetricInterval() {
        return ClientConfig.CACHEMETRIC_INTERVAL;
    }

    /**
     * gc localkey and gcfile expired time
     *
     * @param gcExpiredTime
     */
    public void setGcExpiredTime(int gcExpiredTime) {
        if (gcExpiredTime >= TfsConstant.MIN_GC_EXPIRED_TIME) {
            ClientConfig.GC_EXPIRED_TIME = gcExpiredTime;
        }
    }

    public long getGcExpiredTime() {
        return ClientConfig.GC_EXPIRED_TIME;
    }

    /**
     * segment length
     *
     * @param segmentLength
     */
    public void setSegmentLength(int segmentLength) {
        if (segmentLength > 0 && segmentLength <= TfsConstant.MAX_SEGMENT_LENGTH) {
            ClientConfig.SEGMENT_LENGTH = segmentLength;
        }
    }

    public int getSegmentLength() {
        return ClientConfig.SEGMENT_LENGTH;
    }

    /**
     * batch operate count
     *
     * @param batchCount
     */
    public void setBatchCount(int batchCount) {
        if (batchCount > 0 && batchCount <= TfsConstant.MAX_BATCH_COUNT) {
            ClientConfig.BATCH_COUNT = batchCount;
        }
    }

    public int getBatchCount() {
        return ClientConfig.BATCH_COUNT;
    }
    /************************************************************/

    /**************************************
     *                                    *
     *     Interface implementation       *
     *                                    *
     **************************************/

    // only check first char, serious name check is FSName's duty
    private int checkFileType(String tfsFileName) {
        if (tfsFileName != null && !tfsFileName.isEmpty()) {
            char keyChar = tfsFileName.charAt(0);
            if (keyChar == FSName.TFS_SMALL_KEY_CHAR) {
                return TFS_SMALL_FILE_TYPE;
            }
            if (keyChar == FSName.TFS_LARGE_KEY_CHAR) {
                return TFS_LARGE_FILE_TYPE;
            }
        }
        return TFS_INVALID_FILE_TYPE;
    }

    private int checkFileType(long length) {
        if (length <= 0) {
            return TFS_INVALID_FILE_TYPE;
        }
        if (length < TfsFile.MAX_SMALL_FILE_LENGTH) {
            return TFS_SMALL_FILE_TYPE;
        }
        if (length < TfsFile.MAX_LARGE_FILE_LENGTH) {
            return TFS_LARGE_FILE_TYPE;
        }
        return TFS_INVALID_FILE_TYPE;
    }

    /**
     * get new filename (deprecated method)
     * only for compatibility
     * TODO : just return null
     *
     * @param suffix (dummy)
     */
    public String newTfsFileName(String suffix) {
        TfsFile file = filePool.getFile();

        try {
            return file.createFileName();
        } catch (TfsException e) {
            log.error("new tfs file name fail: ", e);
        }

        return null;
    }

    // reserve saveFile (small) interface
    public String saveFile(String localFileName, String tfsFileName, String tfsSuffix) {
        return saveFile(localFileName, tfsFileName, tfsSuffix, false);
    }

    public String saveFile(String localFileName, String tfsFileName, String tfsSuffix, boolean simpleName) {
        if (localFileName == null) {
            log.error("localfile name is null");
            return null;
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(localFileName);
            log.info(localFileName+"==>"+(null == input));
            return saveFileEx(input, tfsFileName, tfsSuffix, TFS_SMALL_FILE_TYPE, localFileName, simpleName);
        } catch (TfsException e) {
            log.error("save fail: " + localFileName + "=>" + tfsFileName + ","
                      + tfsSuffix, e);
        } catch (FileNotFoundException e) {
            log.error("local file: " + localFileName + " not exist,", e);
        } catch (IOException e) {
            log.error("local file: " + localFileName + " IO failed,", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                log.error("close local file fail ", e);
            }
        }
        return null;
    }

    // new dynamic saveTfsFile interface,
    // based on localFile's length to save as specified type
    public String saveTfsFile(String localFileName, String tfsFileName, String tfsSuffix) {
        if (localFileName == null) {
            log.error("localfile name is null");
            return null;
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(localFileName);
            long length = input.getChannel().size();
            return saveFileEx(input, tfsFileName, tfsSuffix,
                              checkFileType(length), localFileName, false);
        } catch (TfsException e) {
            log.error("save fail: " + localFileName + "=>" + tfsFileName + ","
                      + tfsSuffix, e);
        } catch (FileNotFoundException e) {
            log.error("local file: " + localFileName + " not exist,", e);
        } catch (IOException e) {
            log.error("local file: " + localFileName + " IO failed,", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                log.error("close local file fail ", e);
            }
        }
        return null;
    }

    public boolean fetchFile(String tfsFileName, String tfsSuffix,
                             String localFileName) {
        if (localFileName == null) {
            log.error("fetch file fail: local file null");
            return false;
        }

        try {
            BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(localFileName));
            boolean ret = fetchFile(tfsFileName, tfsSuffix, output);
            output.close();
            return ret;
        } catch (IOException e) {
            log.error("fetch: " + tfsFileName + "," + tfsSuffix + "=>"
                      + localFileName, e);
        }

        return false;
    }

    public boolean fetchFile(String tfsFileName, String tfsSuffix,
                             OutputStream output) {
        return fetchFile(tfsFileName, tfsSuffix, 0, Long.MAX_VALUE, output);
    }

    public boolean fetchFile(String tfsFileName, String tfsSuffix, long offset,
                             OutputStream output) {
        return fetchFile(tfsFileName, tfsSuffix, offset, Long.MAX_VALUE, output);
    }

    public boolean fetchFile(String tfsFileName, String tfsSuffix, long offset,
                             long length, OutputStream output) {
        if (null == output) {
            log.error("fetch file fail : output stream null");
            return false;
        }

        int type;
        if ((type = checkFileType(tfsFileName)) == TFS_LARGE_FILE_TYPE) {
            return fetchLargeFile(tfsFileName, tfsSuffix, offset, length, output);
        }
        if (type == TFS_SMALL_FILE_TYPE) {
            return fetchSmallFile(tfsFileName, tfsSuffix, offset, length, output);
        }
        log.error("file name is invalid: " + tfsFileName + " suffix: " + tfsSuffix);

        return false;
    }

    // default mode is FORCE_STAT mode
    public FileInfo statFile(String tfsFileName, String tfsSuffix) {
        int type;
        if ((type = checkFileType(tfsFileName)) == TFS_LARGE_FILE_TYPE) {
            return statLargeFile(tfsFileName, tfsSuffix);
        }
        if (type == TFS_SMALL_FILE_TYPE) {
            return statSmallFile(tfsFileName, tfsSuffix);
        }
        log.error("file name is invalid: " + tfsFileName + " suffix: " + tfsSuffix);

        return null;
    }

    public boolean unlinkFile(String tfsFileName, String tfsSuffix) {
        int type;
        if ((type = checkFileType(tfsFileName)) == TFS_LARGE_FILE_TYPE) {
            return unlinkLargeFile(tfsFileName, tfsSuffix);
        }
        if (type == TFS_SMALL_FILE_TYPE) {
            return unlinkSmallFile(tfsFileName, tfsSuffix);
        }

        log.error("file name is invalid: " + tfsFileName + " suffix: " + tfsSuffix);
        return false;
    }

    // hide semantic is uniform
    public boolean hideFile(String tfsFileName, String tfsSuffix, int option) {
        TfsFile file = filePool.getFile();

        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.UNLINK_MODE);
            if (option == 0) {
                file.unlink(UnlinkFileMessage.REVEAL);
            } else if (option == 1) {
                file.unlink(UnlinkFileMessage.CONCEAL);
            } else {
                log.error("invalid tfs hide option");
                return false;
            }
            file.close();
            return true;
        } catch (TfsException e) {
            log.error("hide: " + tfsFileName + "," + tfsSuffix, e);
        }

        return false;
    }
    /************************************************************/

    /***************************************
     *    unique interface implementation  *
     ***************************************/
    // sort of unique inter implementation
    private boolean checkUniqueStoreConfig() {
        return (serverList != null && groupName != null && namespace != -1);
    }

    private synchronized void initUniqueStore() {
        if (uniqueStore == null) {
            uniqueStore = new TairUniqueStore();
            ((TairUniqueStore)uniqueStore).setConfigServerList(serverList);
            ((TairUniqueStore)uniqueStore).setGroupName(groupName);
            ((TairUniqueStore)uniqueStore).setNamespace(namespace);
            uniqueStore.init();
        }
    }

    private void checkUniqueStoreInit() {
        if (uniqueStore == null) {
            initUniqueStore();
        }
    }

    public String saveUniqueFile(String localFileName, String tfsFileName,
                                 String tfsSuffix) {
        return saveUniqueFile(localFileName, tfsFileName, tfsSuffix, false);
    }

    public String saveUniqueFile(String localFileName, String tfsFileName,
                                 String tfsSuffix, boolean simpleName) {
        if (localFileName == null) {
            log.error("localfile name is null");
            return null;
        }
        if (!checkUniqueStoreConfig()) {
            log.error("unique server parameter not config error: serverList, groupName, namespace must be configed");
            return null;
        }

        FileInputStream input = null;
        byte[] data = null;
        try {
            input = new FileInputStream(localFileName);
            data = new byte[(int)input.getChannel().size()];

            int ret = input.read(data);

            if (ret != data.length) {
                log.error("read file:" + localFileName + " failed.");
                return null;
            }

            checkUniqueStoreInit();

            TfsFile file = filePool.getFile();
            return file.saveUnique(uniqueStore, tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
        } catch (FileNotFoundException e) {
            log.error("file not found.", e);
        } catch (IOException e) {
            log.error("io error.", e);
        } catch (RuntimeException e) {
            // tair may throw runtimeexception if first init fail.
            // must reinit again
            uniqueStore = null;
            log.warn("saveUniqueFile fail, just saveFile. runtime exception: ", e);
            return saveFile(tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
        } catch (Exception e) {
            log.warn("saveUniqueFile fail, just saveFile.", e);
            return saveFile(tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("close local file fail.");
                }
            }
        }

        return null;
    }

    public String saveUniqueFile(String tfsFileName, String tfsSuffix, byte[] data,
                                 int offset, int length) {
        return saveUniqueFile(tfsFileName, tfsSuffix, data, offset, length, false);
    }

    public String saveUniqueFile(String tfsFileName, String tfsSuffix, byte[] data,
                                 int offset, int length, boolean simpleName) {
        if (null == data || 0 == data.length) {
            log.error("save unique null(empty) byte data fail");
            return null;
        }
        if (!checkUniqueStoreConfig()) {
            log.error("unique server parameter not config error: serverList, groupName, namespace must be configed");
            return null;
        }

        TfsFile file = filePool.getFile();
        try {
            checkUniqueStoreInit();
            return file.saveUnique(uniqueStore, tfsFileName, tfsSuffix, data, offset, length, simpleName);
        } catch (RuntimeException e) {
            // tair may throw runtimeexception if first init fail.
            // must reinit again
            uniqueStore = null;
            log.warn("saveUniqueFile fail, just saveFile. runtime exception: ", e);
            return saveFile(tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
        } catch (Exception e) {
            log.warn("saveUniqueFile fail, just saveFile.", e);
            return saveFile(tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
        }
    }

    public String saveUniqueFile(byte[] data, String tfsFileName, String tfsSuffix) {
        if (null == data || 0 == data.length) {
            log.error("save unique null(empty) byte data fail");
            return null;
        }

        return saveUniqueFile(tfsFileName, tfsSuffix, data, 0, data.length, false);
    }

    public String saveUniqueFile(byte[] data, String tfsFileName, String tfsSuffix, boolean simpleName) {
        if (null == data || 0 == data.length) {
            log.error("save unique null(empty) byte data fail");
            return null;
        }
        return saveUniqueFile(tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
    }

    public int unlinkUniqueFile(String tfsFileName, String tfsSuffix) {
        if (!checkUniqueStoreConfig()) {
            log.error("unique server parameter not config error: serverList, groupName, namespace must be configed");
            return -1;
        }

        TfsFile file = filePool.getFile();
        try {
            checkUniqueStoreInit();
            return file.unlinkUnique(uniqueStore, tfsFileName, tfsSuffix);
        } catch (RuntimeException e) {
            // tair may throw runtimeexception if first init fail.
            // must reinit again
            log.error("unlink fail, runtime exception caught: " + tfsFileName + "," + tfsSuffix, e);
            uniqueStore = null;
        } catch (Exception e) {
            // unlink unique, tair must alive ..
            log.error("unlink fail: " + tfsFileName + "," + tfsSuffix, e);
        }

        return -1;
    }
    /************************************************************/


    /***********************************************
     *  stream way is not uniform, so is specified *
     ***********************************************/
    // small
    public String saveFile(String tfsFileName, String tfsSuffix,
                           byte[] data, int offset, int length) {
        return saveFile(tfsFileName, tfsSuffix, data, offset, length, false);
    }

    public String saveFile(String tfsFileName, String tfsSuffix,
                           byte[] data, int offset, int length, boolean simpleName) {
        if (null == data || 0 == data.length) {
            log.error("save null(empty) byte data fail");
            return null;
        }

        TfsFile file = filePool.getFile();
        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.WRITE_MODE);
            file.write(data, offset, length);
            file.close();

            String retName = file.getFileName(simpleName);
            return (simpleName ? retName + (null == tfsSuffix ? "" : tfsSuffix) :
                    retName);
        } catch (TfsException e) {
            log.error("save file fail: => " + tfsFileName + " , "
                      + tfsSuffix , e);
        }

        return null;
    }

    public String saveFile(byte[] data, String tfsFileName, String tfsSuffix) {
        if (null == data || 0 == data.length) {
            log.error("save null(empty) byte data fail");
            return null;
        }
        return saveFile(tfsFileName, tfsSuffix, data, 0, data.length, false);
    }

    public String saveFile(byte[] data, String tfsFileName, String tfsSuffix, boolean simpleName) {
        if (null == data || 0 == data.length) {
            log.error("save null(empty) byte data fail");
            return null;
        }
        return saveFile(tfsFileName, tfsSuffix, data, 0, data.length, simpleName);
    }

    // for large,
    // oepnReadFile  ==> readFile  ==> closeFile
    // openWriteFile ==> writeFile ==> closeFile
    private int openFile(String tfsFileName, String tfsSuffix, int mode, String key) {
        int fd = TfsConstant.EXIT_INVALIDFD_ERROR;

        TfsLargeFile file = null;
        try {
            fd = filePool.getLargeFileFd();
            file = filePool.getLargeFile(fd);
        } catch (TfsException e) {
            log.error("get fd fail.", e);
            return TfsConstant.EXIT_INVALIDFD_ERROR;
        }

        try {
            file.open(tfsFileName, tfsSuffix, mode, key);
        } catch (TfsException e) {
            log.error("open tfs file fail. ", e);
            filePool.removeFile(fd);
            return TfsConstant.EXIT_INVALIDFD_ERROR;
        }

        return fd;
    }

    public int openWriteFile(String tfsFileName, String tfsSuffix, String key) {
        return openFile(tfsFileName, tfsSuffix, TfsConstant.WRITE_MODE, key);
    }

    public int openReadFile(String tfsFileName, String tfsSuffix) {
        return openFile(tfsFileName, tfsSuffix, TfsConstant.READ_MODE, null);
    }

    public int readFile(int fd, byte[] data, int offset, int length) {
        try {
            TfsLargeFile file = filePool.getLargeFile(fd);
            return file.read(data, offset, length);
        } catch (TfsException e) {
            log.error("read tfs fail fd: " + fd, e);
        }

        return TfsConstant.EXIT_INVALIDFD_ERROR;
    }

    public int readFile(int fd, long fileOffset, byte[] data, int offset, int length) {
        try {
            TfsLargeFile file = filePool.getLargeFile(fd);
            file.seek(fileOffset, TfsFile.SEEK_SET);
            return file.read(data, offset, length);
        } catch (TfsException e) {
            log.error("read tfs fail fd: " + fd, e);
        }

        return TfsConstant.EXIT_INVALIDFD_ERROR;
    }

    public int writeFile(int fd, byte[] data, int offset, int length) {
        try {
            TfsLargeFile file = filePool.getLargeFile(fd);
            return file.write(data, offset, length);
        } catch (TfsException e) {
            log.error("save file fail", e);
        }

        return TfsConstant.EXIT_INVALIDFD_ERROR;
    }

    public String closeFile(int fd) {
        try {
            TfsLargeFile file = filePool.getLargeFile(fd);
            file.close();
            String tfsName = file.getFileName();
            return tfsName;
        } catch (TfsException e) {
            log.error("close file fail", e);
            return null;
        } finally {
            filePool.removeFile(fd);
        }
    }
    /************************************************************/

    /************************************
     * sort of specified implementation *
     ************************************/
    private String saveFileEx(FileInputStream input, String tfsFileName, String tfsSuffix,
                              int type, String key, boolean simpleName)
        throws TfsException, IOException {
        if (input == null) {
        	log.error("args 'input' is null");
            return null;
        }

        TfsFile file;
        if (type == TFS_LARGE_FILE_TYPE) {
            file = filePool.getLargeFile();
            ((TfsLargeFile)file).open(tfsFileName, tfsSuffix, TfsConstant.WRITE_MODE, key);
        } else if (type == TFS_SMALL_FILE_TYPE) {
            file = filePool.getFile();
            log.info(tfsFileName+","+tfsSuffix+","+TfsConstant.WRITE_MODE);
            file.open(tfsFileName, tfsSuffix, TfsConstant.WRITE_MODE);
        } else {
            throw new TfsException("local file is empty or too large. not support now.");
        }

        long length = input.getChannel().size();
        log.info("length:"+length);
        // read from local at most
        int perLength = (int)Math.min(TfsFile.MAX_LARGE_IO_LENGTH, length);
        log.info("perLength:"+perLength);
        byte[] data = new byte[perLength];
        int readLength = 0;

        while (length > 0) {
            readLength = input.read(data, 0, perLength);
            if (readLength > 0) {
                file.write(data, 0, readLength);
                length -= readLength;
            } else {
                break;
            }
        }

        file.close();
        String retName = file.getFileName(simpleName);
        log.info(retName);
        log.info((simpleName ? retName + (null == tfsSuffix ? "" : tfsSuffix) :
                retName));
        return (simpleName ? retName + (null == tfsSuffix ? "" : tfsSuffix) :
                retName);
    }

    private boolean fetchSmallFile(String tfsFileName, String tfsSuffix, long offset,
                                   long length, OutputStream output) {
        TfsFile file = filePool.getFile();

        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.READ_MODE);
            if (offset > 0) {
                file.seek(offset, TfsFile.SEEK_SET);
            }
            if (length == Long.MAX_VALUE) {
                while (!file.isEof()) {
                    byte[] data = file.readV2(TfsFile.MAX_SMALL_IO_LENGTH);
                    output.write(data);
                }
            } else {
                long totalLength = length;
                while (totalLength > 0 && !file.isEof()) {
                    byte[] data = file.readV2(TfsFile.MAX_SMALL_IO_LENGTH);
                    output.write(data);
                    totalLength -= data.length;
                }
            }
            if (offset == 0) {  // only offset from 0, can check whole file crc
                file.checkCrc();
            }
            file.close();
            return true;
        } catch (TfsException e) {
            log.error("fetch to outputStream fail: " + tfsFileName + "," + tfsSuffix, e);
        } catch (IOException e) {
            log.error("fetch to outputStream fail: " + tfsFileName + "," + tfsSuffix, e);
        }

        return false;
    }

    private FileInfo statSmallFile(String tfsFileName, String tfsSuffix) {
        TfsFile file = filePool.getFile();

        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.READ_MODE);
            FileInfo info = file.stat(TfsFile.FORCE_STAT);
            file.close();
            return info;
        } catch (TfsException e) {
            log.error("stat: " + tfsFileName + "," + tfsSuffix, e);
        }

        return null;
    }

    private boolean unlinkSmallFile(String tfsFileName, String tfsSuffix) {
        TfsFile file = filePool.getFile();
        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.UNLINK_MODE);
            file.unlink(UnlinkFileMessage.DELETE);
            file.close();
            return true;
        } catch (TfsException e) {
            log.error("unlink: " + tfsFileName + "," + tfsSuffix, e);
        }

        return false;
    }

    private boolean fetchLargeFile(String tfsFileName, String tfsSuffix, long offset,
                                   long length, OutputStream output) {
        if (null == output) {
            log.error("fetch file fail : output stream null");
            return false;
        }

        TfsFile file = filePool.getLargeFile();

        byte[] data  = new byte[TfsFile.MAX_LARGE_IO_LENGTH];
        int readLength = 0;
        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.READ_MODE);
            if (offset > 0) {
                file.seek(offset, TfsFile.SEEK_SET);
            }
            if (length == Long.MAX_VALUE) {
                while (!file.isEof()) {
                    readLength = file.read(data, 0, TfsFile.MAX_LARGE_IO_LENGTH);
                    output.write(data, 0, readLength);
                }
            } else {            // read specified length
                long totalLength = length;
                while (totalLength > 0 && !file.isEof()) {
                    readLength = file.read(data, 0, TfsFile.MAX_LARGE_IO_LENGTH);
                    output.write(data, 0, readLength);
                    totalLength -= readLength;
                }
            }
            return true;
        } catch (TfsException e) {
            log.error("fetch to outputStream fail: " + tfsFileName + "," + tfsSuffix, e);
        } catch (IOException e) {
            log.error("fetch to outputStream fail: " + tfsFileName + "," + tfsSuffix, e);
        } finally {
            try {
                file.close();
            } catch (TfsException e) {
                log.error("close tfs file fail");
            }
        }

        return false;
    }

    private boolean unlinkLargeFile(String tfsFileName, String tfsSuffix) {
        TfsFile file = filePool.getLargeFile();

        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.UNLINK_MODE);
            file.unlink(UnlinkFileMessage.DELETE);
            file.close();
            return true;
        } catch (TfsException e) {
            log.error("unlink: " + tfsFileName + "," + tfsSuffix, e);
        }
        return false;
    }

    private FileInfo statLargeFile(String tfsFileName, String tfsSuffix) {
        TfsFile file = filePool.getLargeFile();
        FileInfo info;
        try {
            file.open(tfsFileName, tfsSuffix, TfsConstant.STAT_MODE);
            info = file.stat(TfsFile.FORCE_STAT);
            file.close();
            return info;
        } catch (TfsException e) {
            log.error("stat: " + tfsFileName + "," + tfsSuffix, e);
        }

        return null;
    }
    /************************************************************/

    /**********************************
     * sort of special implementation *
     **********************************/
    // consider user want to save large tfs file anyway, public interface
    public String saveLargeFile(String localFileName, String tfsFileName, String tfsSuffix) {
        if (localFileName == null) {
            log.error("localfile name is null");
            return null;
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(localFileName);
            log.info(localFileName+"==>"+(null == input));
            return saveFileEx(input, tfsFileName, tfsSuffix, TFS_LARGE_FILE_TYPE, localFileName, false);
        } catch (TfsException e) {
            log.error("save fail: " + localFileName + "=>" + tfsFileName + ","
                      + tfsSuffix, e);
        } catch (FileNotFoundException e) {
            log.error("local file: " + localFileName + " not exist,", e);
        } catch (IOException e) {
            log.error("local file: " + localFileName + " IO failed,", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                log.error("close local file fail ", e);
            }
        }
        return null;
    }

    public String saveLargeFile(byte[] data, String tfsFileName, String tfsSuffix, String key) {
        if (null == data || 0 == data.length) {
            log.error("save null(empty) byte data fail");
            return null;
        }
        return saveLargeFile(tfsFileName, tfsSuffix, data, 0, data.length, key);
    }

    public String saveLargeFile(String tfsFileName, String tfsSuffix,
                                byte[] data, int offset, int length, String key) {
        if (null == data || 0 == data.length) {
            log.error("save null(empty) byte data fail");
            return null;
        }

        TfsFile file = filePool.getLargeFile();
        try {
            ((TfsLargeFile)file).open(tfsFileName, tfsSuffix, TfsConstant.WRITE_MODE, key);
            file.write(data, offset, length);
            file.close();
            return file.getFileName();
        } catch (TfsException e) {
            log.error("save file fail: => " + tfsFileName + " , "
                      + tfsSuffix , e);
        }

        return null;
    }

    // for gc
    public boolean unlinkFile(int blockId, long fileId, long serverId) {
        TfsSession session = new TfsSession();
        session.setNameServerId(serverId);
        if (session.init() != TfsConstant.TFS_SUCCESS) {
            return false;
        }
        TfsSmallFile file = new TfsSmallFile();
        file.setSession(session);

        try {
            file.open(blockId, fileId, TfsConstant.UNLINK_MODE);
            file.unlink(UnlinkFileMessage.DELETE);
            file.close();
            return true;
        } catch (TfsException e) {
            log.error("unlink: blockId: " + blockId + ", fileId: " + fileId, e);
        }

        return false;
    }
    /************************************************************/
}
