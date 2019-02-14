package com.zachary.article.service.impl;

import com.zachary.article.basic.Constants;
import com.zachary.article.service.RedisArticleService;
import com.zachary.article.utils.JedisUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Title:
 * @Author:Zachary
 * @Desc:
 * @Date:2019/2/13
 **/
@Service
public class RedisArticleServiceImpl implements RedisArticleService {
    @Resource
    private JedisUtil jedisService;

    /**
     * 发布文章
     *
     * @param title   标题
     * @param content 内容
     * @param link    链接
     * @param userId  用户id
     * @return
     */
    @Override
    public String postArticle(String title, String content, String link, String userId) {
        //模拟生成文章主键
        String articleId = String.valueOf(jedisService.incr("article:"));
        //时间戳
        long now = System.currentTimeMillis() / 1000;
        //生成文章属性
        Map<String, String> articleMap = new HashMap<>();
        articleMap.put("articleId", articleId);
        articleMap.put("title", title);
        articleMap.put("content", content);
        articleMap.put("postTime", String.valueOf(now));
        articleMap.put("link", link);
        articleMap.put("userId", userId);
        articleMap.put("votes", "1");
        //文章的redis的key 格式 为 article:id  -> article:1
        String articleKey = "article:" + articleId;
        //保存文章信息
        jedisService.hmset(articleKey, articleMap);

        //点赞记录表，记录点赞用户有哪些，并且不能重复，无序
        String voted = "voted:" + articleId;
        jedisService.sadd(voted, userId);
        jedisService.expire(voted, Constants.ONE_WEEK_IN_SECONDS);

        //记录文章点赞数，可以排序，评分是时间戳+400（每一个赞）
        jedisService.zadd("score:info", now + Constants.VOTE_SCORE, articleKey);
        //保存时间戳，可以按时间排序的文章列表
        jedisService.zadd("time:", now, articleKey);
        return articleId;
    }

    /**
     * @param key
     * @return
     */
    @Override
    public Map<String, String> hgetAll(String key) {
        return jedisService.hgetAll(key);
    }

    /**
     * 文章点赞
     *
     * @param userId
     * @param articleId
     */
    @Override
    public void articleVote(String userId, String articleId) {
        String articleKey = "article:" + articleId;
        long endtime = System.currentTimeMillis() / 1000 - Constants.ONE_WEEK_IN_SECONDS;

        if (endtime > jedisService.zscore("time:", articleKey)) {
            return;
        }
        //加入点赞表
        if (jedisService.sadd("voted:" + articleId, userId) == 1) {
            jedisService.zincrby("score.info", Constants.VOTE_SCORE, articleKey);
            jedisService.hincrby(articleKey, "votes", 1L);
        }
    }

    /**
     * @param key
     * @param votes
     * @return
     */
    @Override
    public String hget(String key, String votes) {
        return jedisService.hget(key, votes);
    }

    /**
     * 查询文章
     *
     * @param page
     * @param key
     * @return
     */
    @Override
    public List<Map<String, String>> getArticles(int page, String key) {
        int start = (page - 1) * Constants.ARTICLES_PER_PAGE;
        int end = start + Constants.ARTICLES_PER_PAGE - 1;
        Set<String> set = jedisService.zrevrange(key, start, end);
        List<Map<String, String>> maps = new ArrayList<>();
        for (String id : set) {
            Map<String, String> map = hgetAll(id);
            map.put("id", id);
            maps.add(map);
        }

        return maps;
    }
}
