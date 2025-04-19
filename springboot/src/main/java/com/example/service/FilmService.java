package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import com.example.entity.*;
import com.example.mapper.CommentMapper;
import com.example.mapper.FilmMapper;
import com.example.mapper.UserMapper;
import com.example.utils.UserCF;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    @Resource
    private FilmMapper filmMapper;
    @Resource
    private CommentMapper commentMapper;

    /**
     * 新增
     */
    public void add(Film film) {
        filmMapper.insert(film);
    }

    /**
     * 更新
     */
    public void update(Film film) {
        filmMapper.updateById(film);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        filmMapper.deleteById(id);
    }

    /**
     * 查询所有
     */
    public List<Film> selectAll(Film film) {
        return filmMapper.selectAll(film);
    }

    /**
     * 分页查询
     * @param pageNum 当前的页码
     * @param pageSize 每页的个数
     * @return 分页的对象  包含数据和分页参数 total
     */
    public PageInfo<Film> selectPage(Film film, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Film> list = filmMapper.selectAll(film);
        for (Film f : list) {
            this.setScore(f);
        }
        return PageInfo.of(list);
    }

    public Film selectById(Integer id) {
        Film film = filmMapper.selectById(id);
        this.setScore(film);
        return film;
    }

    // 随机推荐电影
    public List<Film> selectRecommend(Integer filmId) {
        List<Film> films = this.selectAll(null);
        // 把当前详情页的电影排除推荐
        films = films.stream().filter(film -> !film.getId().equals(filmId)).collect(Collectors.toList());
        Collections.shuffle(films);  // 打乱电影的排序
        List<Film> filmList = films.stream().limit(3).collect(Collectors.toList());
        for (Film film : filmList) {
            this.setScore(film);
        }
        return filmList;
    }

    private void setScore(Film film) {
        int total = commentMapper.selectTotal(film.getId());
        film.setCommentNum(total);
        if (total == 0) {
            film.setScore(0D);
        } else {
            double sum = commentMapper.selectSum(film.getId());
            double avg = sum / total;
            double roundedScore = Math.round(avg * 10) / 10.0;
            film.setScore(roundedScore);
        }
    }

    // 排行前5的电影数据
    public List<Film> selectRanking() {
        List<Film> filmList = filmMapper.selectAll(null);
        for (Film film : filmList) {
            setScore(film);
        }
        return filmList.stream().sorted((f1, f2) -> f2.getScore().compareTo(f1.getScore())).limit(5).toList();
    }

    @Resource
    private UserMapper userMapper;


    /**
     * 推荐算法
     */
    public List<Film> recommend(Integer userId) {

        // 1. 获取所有的用户信息
        List<User> allUsers = userMapper.selectAll(null);
        // 2. 获取所有的电影信息
        List<Film> allFilms = filmMapper.selectAll(null);
        Comment comment = new Comment();
        comment.setType("长评");
        // 3. 获取所有的长评评论信息
        List<Comment> allLongComment = commentMapper.selectAll(comment);

        comment.setType("短评");
        // 4. 获取所有的长评评论信息
        List<Comment> allShortComment = commentMapper.selectAll(comment);
        // 定义一个存储每个物品和每个用户关系的List
        List<RelateDTO> data = new ArrayList<>();

        // 开始计算每个物品和每个用户之间的关系数据
        for (Film film : allFilms) {
            Integer filmsId = film.getId();
            for (User user : allUsers) {
                Integer usersId = user.getId();
                int index = 1;
                // 1. 判断该用户有没有长评论该电影，评论的权重我们给 2
                Optional<Comment> collectOptional1 =
                        allLongComment.stream().filter(x -> x.getFilmId().equals(filmsId) && x.getUserId().equals(usersId)).findFirst();
                if (collectOptional1.isPresent()) {
                    index += 2;
                }

                // 2. 判断该用户有没有短评论该电影，评论的权重我们给 1
                Optional<Comment> collectOptional2 =
                        allShortComment.stream().filter(x -> x.getFilmId().equals(filmsId) && x.getUserId().equals(usersId)).findFirst();
                if (collectOptional2.isPresent()) {
                    index += 1;
                }
                if (index > 1) {
                    RelateDTO relateDTO = new RelateDTO(usersId, filmsId, index);
                    data.add(relateDTO);
                }
            }
        }

        // 数据准备结束后，就把这些数据一起喂给这个推荐算法
        List<Integer> filmsIds = UserCF.recommend(userId, data);
        // 把物品id转换成物品
        List<Film> result = filmsIds.stream().map(filmsId -> allFilms.stream()
                        .filter(x -> x.getId().equals(filmsId)).findFirst().orElse(null))
                .limit(5).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(result)) {
            // 随机给它推荐10个
            return getRandomPosts(5);
        }
        return result;
    }

    private List<Film> getRandomPosts(int num) {
        List<Film> posts = filmMapper.selectAll(null);
        Collections.shuffle(posts);
        if (posts.size() > num) {
            return posts.subList(0, num);
        }
        return posts;
    }
}
