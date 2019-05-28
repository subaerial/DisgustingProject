package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.Comment;

import java.util.List;

public interface CommentDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Comment record);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);





    List<Comment> selectComment(String dragonNum);
}