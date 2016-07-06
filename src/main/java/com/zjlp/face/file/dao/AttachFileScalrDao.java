package com.zjlp.face.file.dao;

import java.util.List;

import com.zjlp.face.file.domain.AttachFileScalr;

public interface AttachFileScalrDao {

	void add(AttachFileScalr file);
	
	void edit(AttachFileScalr file);
	
	AttachFileScalr getById(String fileScalrNo);
	
	void delete(String fileScalrNo);
	
	List<AttachFileScalr> findByAttachFileNo(String attachFileNo);
}
