package com.zjlp.face.file.dao;

import java.util.List;

import com.zjlp.face.file.domain.AttachFile;

public interface AttachFileDao {

	void add(AttachFile file);
	
	void edit(AttachFile file);
	
	AttachFile getById(String fileNo);
	
	void delete(String fileNo);
	
	List<AttachFile> findByCodeAndTableId(AttachFile file);
}
