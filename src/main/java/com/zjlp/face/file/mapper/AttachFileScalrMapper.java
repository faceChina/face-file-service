package com.zjlp.face.file.mapper;

import java.util.List;

import com.zjlp.face.file.domain.AttachFileScalr;

public interface AttachFileScalrMapper {
    int deleteByPrimaryKey(String fileScalrNo);

    int insert(AttachFileScalr record);

    int insertSelective(AttachFileScalr record);

    AttachFileScalr selectByPrimaryKey(String fileScalrNo);

    int updateByPrimaryKeySelective(AttachFileScalr record);

    int updateByPrimaryKey(AttachFileScalr record);
    
    List<AttachFileScalr> selectByAttachFileNo(String attachFileNo);
}