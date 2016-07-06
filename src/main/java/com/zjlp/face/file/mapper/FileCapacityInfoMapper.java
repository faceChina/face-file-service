package com.zjlp.face.file.mapper;

import com.zjlp.face.file.domain.FileCapacityInfo;

public interface FileCapacityInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FileCapacityInfo record);

    int insertSelective(FileCapacityInfo record);

    FileCapacityInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FileCapacityInfo record);

    int updateByPrimaryKey(FileCapacityInfo record);
}