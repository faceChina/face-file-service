/*
 * (C) 2007-2010 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */
package com.taobao.common.tfs;

import java.io.OutputStream;
import java.util.List;

import com.taobao.common.tfs.packet.FileInfo;

public interface TfsManager {

    /**
     * check if nameserver is at work
     *
     * @return
     */
    boolean isEnable();

    /**
     * set master nameserver ip address
     *
     * @param ipaddr
     * @return
     */
    int setMasterIP(String ipaddr);

    /**
     * set unique store(tair)'s server list and groupname
     *
     * @param groupName
     * @param namespace
     */
    void setUniqueStore(List<String> serverList, String groupName, int namespace);

    /**
     * generate a new tfs file name, suffix is just a mock for compatibility
     * this is a deprecated method, do not set tfsname explicitly, use null instead when needed
     *
     * @param tfsSuffix
     * @return
     */
    String newTfsFileName(String tfsSuffix);

    /**
     * save a local file to tfs
     *
     * @param localFileName
     * @param tfsFileName
     * @param tfsSuffix
     * @return  tfsfilename if save successully, or null if fail
     */
    String saveFile(String localFileName, String tfsFileName, String tfsSuffix);

    /**
     * save a local file to tfs, return simple tfs name(no suffix encode)
     *
     * @param localFileName
     * @param tfsFileName
     * @param tfsSuffix
     * @param simpleName
     * @return  tfsfilename if save successully, or null if fail
     */
    String saveFile(String localFileName, String tfsFileName, String tfsSuffix, boolean simpleName);

    /**
     * save local file(byte[]) to tfs
     *
     * @param tfsFileName
     * @param tfsSuffix
     * @param data   data to save
     * @param offset the start offset of data
     * @param length the size to save (ensure offset+length < data.length)
     * @return tfsfilename if save successully, or null if fail
     */
    String saveFile(String tfsFileName, String tfsSuffix, byte[] data, int offset, int length);

    /**
     * save local file(byte[]) to tfs, return simple tfs name
     *
     * @param tfsFileName
     * @param tfsSuffix
     * @param data   data to save
     * @param offset the start offset of data
     * @param length the size to save (ensure offset+length < data.length)
     * @param simpleName
     * @return tfsfilename if save successully, or null if fail
     */
    String saveFile(String tfsFileName, String tfsSuffix, byte[] data, int offset, int length, boolean simpleName);

    /**
     * save local file(byte[]) to tfs
     *
     * @param data  data to save
     * @param tfsFileName
     * @param tfsSuffix
     * @return      tfsfilename if save successully, or null if fail
     */
    String saveFile(byte[] data, String tfsFileName, String tfsSuffix);

    /**
     * save local file(byte[]) to tfs (weird interface, reserve for compatibility)
     * return simple tfs name
     *
     * @param data  data to save
     * @param tfsFileName
     * @param tfsSuffix
     * @param simpleName
     * @return      tfsfilename if save successully, or null if fail
     */
    String saveFile(byte[] data, String tfsFileName, String tfsSuffix, boolean simpleName);

    /**
     * save a local file to tfs file,
     * based on localfile's length to save as specified type
     * @param localFileName
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    public String saveTfsFile(String localFileName, String tfsFileName, String tfsSuffix);

    /**
     * save a local file to tfs large file anyway
     * @param localFileName
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    public String saveLargeFile(String localFileName, String tfsFileName, String tfsSuffix);

    /**
     * save a local file to tfs large file anyway
     * @param data
     * @param tfsFileName
     * @param tfsSuffix
     * @param key key name
     * @return
     */
    public String saveLargeFile(byte[] data, String tfsFileName, String tfsSuffix, String key);

    /**
     * save a local file to tfs large file anyway
     * @param tfsFileName
     * @param tfsSuffix
     * @param data   data to save
     * @param offset the start offset of data
     * @param length the size to save (ensure offset+length < data.length)
     * @param key key name
     * @return
     */
    public String saveLargeFile(String tfsFileName, String tfsSuffix, byte[] data, int offset, int length, String key);

    /**
     * stat a tfs file
     *
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    FileInfo statFile(String tfsFileName, String tfsSuffix);

    /**
     * delete a tfs file
     *
     * @param tfsFileName
     * @param tfsSuffix
     * @return  true if delete successully, or false if fail
     */
    boolean unlinkFile(String tfsFileName, String tfsSuffix);

    /**
     * hide file
     * @param tfsFileName
     * @param tfsSuffix
     * @param option 1 conceal 0 reveal
     * @return
     */
    boolean hideFile(String fileName, String tfsSuffix, int option);

    /**
     * fetch a tfsfile to local disk
     *
     * @param tfsFileName (tfsFileName + tfsSuffix) = tfsName
     * @param tfsSuffix
     * @param localFileName
     * @return
     */
    boolean fetchFile(String tfsFileName, String tfsSuffix, String localFileName);

    /**
     * fetch a tfsfile to output stream
     * @param tfsFileName
     * @param tfsSuffix
     * @param output
     * @return
     */
    boolean fetchFile(String tfsFileName, String tfsSuffix, OutputStream output);

    /**
     * fetch a tfsfile to output stream
     * @param tfsFileName
     * @param tfsSuffix
     * @param offset offset of tfsfile to fetch (read until end)
     * @param output
     * @return
     */
    boolean fetchFile(String tfsFileName, String tfsSuffix, long offset, OutputStream output);

    /**
     * fetch a tfsfile to output stream
     * @param tfsFileName
     * @param tfsSuffix
     * @param offset offset of tfsfile to fetch
     * @param length
     * @param output
     * @return
     */
    boolean fetchFile(String tfsFileName, String tfsSuffix, long offset, long length, OutputStream output);

    /**
     * save a local file to unique tfs file
     * @param localFileName
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    String saveUniqueFile(String localFileName, String tfsFileName, String tfsSuffix);

    /**
     * save a local file to unique tfs file
     * @param localFileName
     * @param tfsFileName
     * @param tfsSuffix
     * @param simpleName
     * @return
     */
    String saveUniqueFile(String localFileName, String tfsFileName, String tfsSuffix, boolean simpleName);

    /**
     * save a local file(byte []) to unique tfs file
     *
     * @param tfsFileName
     * @param tfsSuffix
     * @param data   data to save
     * @param offset the start offset of data
     * @param length the size to save (ensure offset+length < data.length)
     * @return tfsfilename if save successully, or null if fail
     */
    String saveUniqueFile(String tfsFileName, String tfsSuffix,  byte[] data, int offset, int length);

    /**
     * save a local file(byte []) to unique tfs file
     *
     * @param tfsFileName
     * @param tfsSuffix
     * @param data   data to save
     * @param offset the start offset of data
     * @param length the size to save (ensure offset+length < data.length)
     * @param simpleName
     * @return tfsfilename if save successully, or null if fail
     */
    String saveUniqueFile(String tfsFileName, String tfsSuffix,  byte[] data, int offset, int length, boolean simpleName);

    /**
     * save a local file to unique tfs file
     *
     * @param data
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    String saveUniqueFile(byte[] data, String tfsFileName, String tfsSuffix);

    /**
     * save a local file to unique tfs file
     *
     * @param data
     * @param tfsFileName
     * @param tfsSuffix
     * @param simpleName
     * @return
     */
    String saveUniqueFile(byte[] data, String tfsFileName, String tfsSuffix, boolean simpleName);

    /**
     * unlink an unique tfs file
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    int unlinkUniqueFile(String tfsFileName, String tfsSuffix);


    /**************************************
     *                                    *
     *  tfs large file stream interface   *
     *                                    *
     **************************************/
    /**
     * open a tfs file to write
     * @param tfsFileName
     * @param tfsSuffix
     * @param key
     * @return
     */
    int openWriteFile(String tfsFileName, String tfsSuffix, String key);

    /**
     * open a tfs file to read
     * @param tfsFileName
     * @param tfsSuffix
     * @return
     */
    int openReadFile(String tfsFileName, String tfsSuffix);

    /**
     * read data
     * @param fd
     * @param data
     * @param offset
     * @param length
     * @return
     */
    int readFile(int fd, byte[] data, int offset, int length);

    /**
     * read data
     * @param fd
     * @param fileOffset
     * @param data
     * @param offset
     * @param length
     * @return
     */
    int readFile(int fd, long fileOffset, byte[] data, int offset, int length);

    /**
     * write data
     * @param fd
     * @param data
     * @param offset
     * @param length
     * @return
     */
    int writeFile(int fd, byte[] data, int offset, int length);

    /**
     * close a tfs file and return a tfs file name
     *
     * @param fd
     * @return
     */
    String closeFile(int fd);

    /**
     * destroy
     * @return
     */
    void destroy();
}
