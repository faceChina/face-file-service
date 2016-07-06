package com.zjlp.face.file.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zjlp.face.file.dao.AttachFileScalrDao;
import com.zjlp.face.file.domain.AttachFileScalr;
import com.zjlp.face.file.mapper.AttachFileScalrMapper;

@Repository
public class AttachFileScalrDaoImpl implements AttachFileScalrDao {

	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public void add(AttachFileScalr file) {
		sqlSession.getMapper(AttachFileScalrMapper.class).insert(file);
	}

	@Override
	public void edit(AttachFileScalr file) {
		sqlSession.getMapper(AttachFileScalrMapper.class).updateByPrimaryKeySelective(file);
	}

	@Override
	public AttachFileScalr getById(String fileScalrNo) {
		return sqlSession.getMapper(AttachFileScalrMapper.class).selectByPrimaryKey(fileScalrNo);
	}

	@Override
	public void delete(String fileScalrNo) {
		sqlSession.getMapper(AttachFileScalrMapper.class).deleteByPrimaryKey(fileScalrNo);

	}

	@Override
	public List<AttachFileScalr> findByAttachFileNo(String attachFileNo) {
		return sqlSession.getMapper(AttachFileScalrMapper.class).selectByAttachFileNo(attachFileNo);
	}

}
