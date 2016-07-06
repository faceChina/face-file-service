package com.zjlp.face.file.mapper;

import java.util.List;

import com.zjlp.face.file.domain.AttachFile;

public interface AttachFileMapper {
    int deleteByPrimaryKey(String fileNo);

    int insert(AttachFile record);

    int insertSelective(AttachFile record);

    AttachFile selectByPrimaryKey(String fileNo);

    int updateByPrimaryKeySelective(AttachFile record);

    int updateByPrimaryKey(AttachFile record);

	List<AttachFile> selectByCodeAndTableId(AttachFile record);
}