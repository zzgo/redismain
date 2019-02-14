package com.zachary.article.service;

import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Author:Zachary
 * @Desc:
 * @Date:2019/2/13
 **/
public interface RedisArticleService {
    //发布文章
    String postArticle(String title, String content, String link, String userId);

    //查询文章
    Map<String, String> hgetAll(String key);

    //文章点赞
    void articleVote(String userId, String articleId);

    //查询文章中某一个属性值
    String hget(String key, String votes);

    //获取文章列表，带分页
    List<Map<String, String>> getArticles(int page, String key);
}
