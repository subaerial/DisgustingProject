package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.SendCommentRequest;
import com.mhc.yunxian.dao.CommentDao;
import com.mhc.yunxian.dao.model.Comment;
import com.mhc.yunxian.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {



    @Autowired
    CommentDao commentDao;


    @Override
    public List<Comment> getComment(String dragonNum) {
        return commentDao.selectComment(dragonNum);
    }

    @Override
    public Boolean sendComment(SendCommentRequest sendCommentRequest){

        Comment comment = new Comment();

        comment.setComment(sendCommentRequest.getComment());
        comment.setOpenid(sendCommentRequest.getOpenid());
        comment.setDragonNum(sendCommentRequest.getDragon_num());
        comment.setCreateTime(new Date());
        comment.setComment(sendCommentRequest.getComment());

        if(commentDao.insertSelective(comment) < 1){
            return false;
        }

        return true;
    }
}
