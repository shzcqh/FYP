package com.example.batch;
import jakarta.annotation.Resource;

import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import com.example.service.BaiduSentimentService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public class SentimentBackfillRunner implements CommandLineRunner {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private BaiduSentimentService baiduSentimentService;

    @Override
    public void run(String... args) throws Exception {
        // 1) 查询所有未分析的评论
        List<Comment> list = commentMapper.selectAllUnanalyzed();
        System.out.println(">>> 待回填评论数: " + list.size());

        // 2) 逐条调用百度接口并回写
        for (Comment c : list) {
            try {
                BaiduSentimentService.SentimentResult r = baiduSentimentService.analyze(c.getComment());
                commentMapper.updateSentiment(
                        c.getId(),
                        r.getSentiment(),
                        r.getPositiveProb(),
                        r.getNegativeProb()
                );
                System.out.printf("回填成功 id=%d → sentiment=%d%n",
                        c.getId(), r.getSentiment());
            } catch (Exception ex) {
                System.err.printf("回填失败 id=%d: %s%n", c.getId(), ex.getMessage());
            }
            // 如接口限速，可在此处 Thread.sleep(100);
        }

        // 如果你只想跑一次就退出，可以取消下面注释：
         //System.exit(0);
    }
}
