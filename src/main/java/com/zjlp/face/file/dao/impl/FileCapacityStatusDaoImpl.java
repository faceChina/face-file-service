package com.zjlp.face.file.dao.impl;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zjlp.face.file.dao.FileCapacityStatusDao;
import com.zjlp.face.file.domain.FileCapacityStatus;
import com.zjlp.face.file.mapper.FileCapacityStatusMapper;
@Repository
public class FileCapacityStatusDaoImpl implements FileCapacityStatusDao {

	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public void add(FileCapacityStatus capacityInfo) {
		sqlSession.getMapper(FileCapacityStatusMapper.class).insert(capacityInfo);
	}

	@Override
	public void edit(FileCapacityStatus capacityInfo) {
		sqlSession.getMapper(FileCapacityStatusMapper.class).updateByPrimaryKeySelective(capacityInfo);
	}

	@Override
	public FileCapacityStatus getById(Long id) {
		return sqlSession.getMapper(FileCapacityStatusMapper.class).selectByPrimaryKey(id);
	}

	@Override
	public void delete(Long id) {
		sqlSession.getMapper(FileCapacityStatusMapper.class).deleteByPrimaryKey(id);
	}

}
