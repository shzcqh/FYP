package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service


public class CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource private BaiduSentimentService baiduSentimentService;
    public void add(Comment comment) {
        System.out.println(">>> [Service.add] 收到新增评论请求: " + comment.getComment());
        comment.setTime(DateUtil.now());
        // —— 新增：情感分析 ——
        SentimentResult r = baiduSentimentService.analyze(comment.getComment());
        comment.setSentiment(r.getSentiment());
        comment.setPositiveProb(r.getPositiveProb());
        comment.setNegativeProb(r.getNegativeProb());
        // —————————————

        commentMapper.insert(comment);
    }

    public void update(Comment comment) {
        commentMapper.updateById(comment);
    }
    public void deleteById(Integer id) {
        commentMapper.deleteById(id);
    }
    public List<Comment> selectAll(Comment comment){
       return commentMapper.selectAll(comment);
    }


    public PageInfo<Comment> selectPage(Comment comment,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Comment> list=commentMapper.selectAll(comment);
        return PageInfo.of(list);
    }



}
