package com.zjlp.face.file.dao;

import java.util.List;

import com.zjlp.face.file.domain.CloudstFile;

public interface CloudstFileDao {

	void add(CloudstFile file);
	
	void edit(CloudstFile file);
	
	CloudstFile getById(String fileNo);
	
	void delete(Long Id);
	
	List<CloudstFile> findByCodeAndTableId(CloudstFile file);
}
