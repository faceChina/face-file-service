package com.zjlp.face.file.dao;

import com.zjlp.face.file.domain.FileCapacityInfo;

public interface FileCapacityInfoDao {

	void add(FileCapacityInfo capacityInfo);
	
	void edit(FileCapacityInfo capacityInfo);
	
	FileCapacityInfo getById(Long id);
	
	void delete(Long id);
}
