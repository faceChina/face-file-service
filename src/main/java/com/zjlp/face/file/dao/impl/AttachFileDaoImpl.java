package com.zjlp.face.file.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zjlp.face.file.dao.AttachFileDao;
import com.zjlp.face.file.domain.AttachFile;
import com.zjlp.face.file.mapper.AttachFileMapper;
@Repository
public class AttachFileDaoImpl implements AttachFileDao {
	
	@Autowired
	private SqlSession sqlSession;

	@Override
	public void add(AttachFile file) {
		sqlSession.getMapper(AttachFileMapper.class).insert(file);
	}

	@Override
	public void edit(AttachFile file) {
		sqlSession.getMapper(AttachFileMapper.class).updateByPrimaryKeySelective(file);
	}

	@Override
	public AttachFile getById(String fileNo) {
		return sqlSession.getMapper(AttachFileMapper.class).selectByPrimaryKey(fileNo);
	}

	@Override
	public void delete(String fileNo) {
		sqlSession.getMapper(AttachFileMapper.class).deleteByPrimaryKey(fileNo);
	}

	@Override
	public List<AttachFile> findByCodeAndTableId(AttachFile file) {
		return sqlSession.getMapper(AttachFileMapper.class).selectByCodeAndTableId(file);
	}

}
