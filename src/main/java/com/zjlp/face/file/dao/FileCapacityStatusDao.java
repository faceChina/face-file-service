package com.zjlp.face.file.dao;

import com.zjlp.face.file.domain.FileCapacityStatus;

public interface FileCapacityStatusDao {
	
	void add(FileCapacityStatus capacityInfo);
	
	void edit(FileCapacityStatus capacityInfo);
	
	FileCapacityStatus getById(Long id);
	
	void delete(Long id);
}
