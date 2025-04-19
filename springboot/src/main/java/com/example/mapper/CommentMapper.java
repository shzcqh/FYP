package com.example.mapper;

import com.example.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper {
    void insert(Comment comment);

    void updateById(Comment comment);

    void deleteById(Integer id);

    List<Comment> selectAll(Comment comment);

@Select("select count(*) from comment where film_id=#{filmId}")
    int selectTotal(Integer filmId);
@Select("select sum(score) from comment where film_id=#{filmId}")
    double selectSum(Integer filmId);
    /** 查所有还没跑过情感分析的评论 */
    List<Comment> selectAllUnanalyzed();

    /** 回写情感字段 */
    int updateSentiment(
            @Param("id") int id,
            @Param("sentiment") int sentiment,
            @Param("positiveProb") double positiveProb,
            @Param("negativeProb") double negativeProb
    );

}
