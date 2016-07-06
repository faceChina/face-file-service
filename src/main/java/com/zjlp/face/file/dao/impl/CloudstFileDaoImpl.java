package com.zjlp.face.file.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zjlp.face.file.dao.CloudstFileDao;
import com.zjlp.face.file.domain.CloudstFile;
import com.zjlp.face.file.mapper.CloudstFileMapper;
@Repository
public class CloudstFileDaoImpl implements CloudstFileDao {
	
	@Autowired
	private SqlSession sqlSession;

	@Override
	public void add(CloudstFile file) {
		sqlSession.getMapper(CloudstFileMapper.class).insert(file);
	}

	@Override
	public void edit(CloudstFile file) {
		sqlSession.getMapper(CloudstFileMapper.class).updateByPrimaryKeySelective(file);
	}

	@Override
	public CloudstFile getById(String fileNo) {
		return sqlSession.getMapper(CloudstFileMapper.class).selectByPrimaryKey(fileNo);
	}

	@Override
	public void delete(Long Id) {
		sqlSession.getMapper(CloudstFileMapper.class).deleteByPrimaryKey(Id);
	}

	@Override
	public List<CloudstFile> findByCodeAndTableId(CloudstFile file) {
		return sqlSession.getMapper(CloudstFileMapper.class).selectByCodeAndTableId(file);
	}

}
