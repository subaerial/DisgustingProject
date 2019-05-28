package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.SendCommentRequest;
import com.mhc.yunxian.dao.model.Comment;

import java.util.List;

public interface CommentService {

    Boolean sendComment(SendCommentRequest sendCommentRequest);

    List<Comment> getComment(String dragonNum);

}
