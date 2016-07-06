package com.zjlp.face.file.mapper;

import com.zjlp.face.file.domain.FileOperateFailedRecord;

public interface FileOperateFailedRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FileOperateFailedRecord record);

    int insertSelective(FileOperateFailedRecord record);

    FileOperateFailedRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FileOperateFailedRecord record);

    int updateByPrimaryKey(FileOperateFailedRecord record);
}