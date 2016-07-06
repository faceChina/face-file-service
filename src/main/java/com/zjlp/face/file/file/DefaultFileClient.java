package com.zjlp.face.file.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taobao.common.tfs.TfsManager;
import com.taobao.common.tfs.packet.FileInfo;
import com.zjlp.face.file.file.exception.FileException;

@Service("fileClient")
public class DefaultFileClient implements FileClient {
	
	@Autowired
	private TfsManager tfsManager;  
	
	@Override
	public String put(String localfile, String tfsSuffix) throws FileException {
		return tfsManager.saveFile(localfile, null, tfsSuffix);
	}

	@Override
	public String put(String localFile, String tfsFileName, String tfsSuffix)
			throws FileException {
		return tfsManager.saveFile(localFile, tfsFileName, tfsSuffix);
	}

	@Override
	public String put(byte[] data, String tfsSuffix) throws FileException {
		return tfsManager.saveFile(data, null, tfsSuffix);
	}

	@Override
	public String put(byte[] data, String tfsFileName, String tfsSuffix) {
		return tfsManager.saveFile(data, tfsFileName, tfsSuffix);
	}

	@Override
	public boolean get(String tfsFileName, String tfsSuffix, OutputStream out)
			throws FileException {
		return tfsManager.fetchFile(tfsFileName, tfsSuffix, out);
	}

	@Override
	public String update(String localFile, String tfsFileName, String tfsSuffix)
			throws FileException {
		return tfsManager.saveFile(localFile, tfsFileName, tfsSuffix);
	}

	@Override
	public String update(byte[] data, String tfsFileName, String tfsSuffix)
			throws FileException {
		return tfsManager.saveFile(data, tfsFileName, tfsSuffix);
	}

	@Override
	public boolean get(String tfsFileName, String tfsSuffix, String localFile)
			throws FileException {
		return tfsManager.fetchFile(tfsFileName, tfsSuffix, localFile);
	}

	@Override
	public byte[] getBytes(String tfsFileName, String tfsSuffix)
			throws FileException, IOException {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			boolean result = get(tfsFileName, tfsSuffix, out);
			if (result == false) {
				return null;
			}
			return out.toByteArray();
		} catch (FileException e) {
			throw e;
		}finally{
			if (null != out) {
				out.close();
			}
		}
	}

	@Override
	public boolean delete(String tfsFileName, String tfsSuffix)
			throws FileException {
		return tfsManager.unlinkFile(tfsFileName, tfsSuffix);
	}
	
	@Override
	public FileStatus stat(String tfsFileName) throws FileException {
		return this.stat(tfsFileName,null);
	}

	@Override
	public FileStatus stat(String tfsFileName, String tfsSuffix)
			throws FileException {
		 FileInfo fileInfo = tfsManager.statFile(tfsFileName, tfsSuffix);
		 if(null == fileInfo){
			 return null;
		 }
		 return new FileStatus(fileInfo);
	}

}
