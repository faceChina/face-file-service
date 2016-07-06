package com.zjlp.face.file.dao.impl;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zjlp.face.file.dao.FileOperateFailedRecordDao;
import com.zjlp.face.file.domain.FileOperateFailedRecord;
import com.zjlp.face.file.mapper.FileOperateFailedRecordMapper;
@Repository
public class FileOperateFailedRecordDaoImpl implements
		FileOperateFailedRecordDao {

	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public void add(FileOperateFailedRecord record) {
		sqlSession.getMapper(FileOperateFailedRecordMapper.class).insertSelective(record);
	}

}
