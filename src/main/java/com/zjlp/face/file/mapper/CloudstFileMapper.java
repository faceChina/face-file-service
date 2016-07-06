package com.zjlp.face.file.mapper;

import java.util.List;

import com.zjlp.face.file.domain.CloudstFile;

public interface CloudstFileMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CloudstFile record);

    int insertSelective(CloudstFile record);

    CloudstFile selectByPrimaryKey(String fileNo);

    int updateByPrimaryKeySelective(CloudstFile record);

    int updateByPrimaryKey(CloudstFile record);

	List<CloudstFile> selectByCodeAndTableId(CloudstFile record);
}