package com.zjlp.face.file.mapper;

import com.zjlp.face.file.domain.FileCapacityStatus;

public interface FileCapacityStatusMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FileCapacityStatus record);

    int insertSelective(FileCapacityStatus record);

    FileCapacityStatus selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FileCapacityStatus record);

    int updateByPrimaryKey(FileCapacityStatus record);
}