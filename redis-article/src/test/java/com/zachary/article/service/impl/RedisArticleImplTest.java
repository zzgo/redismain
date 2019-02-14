package com.zachary.article.service.impl;

import com.zachary.article.service.RedisArticleService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisArticleImplTest {
    @Resource(name = "redisArticleServiceImpl")
    private RedisArticleService redisArticleService;

    @Test
    public void postArticle() throws Exception {
        String userId = "010"; //用户ID 001
        String title = "The road to west";
        String content = "About body and mental health";
        String link = "www.miguo.com";
        //发布文章，返回文章ID
        String articleId = redisArticleService.postArticle(title, content, link, userId);
        System.out.println("刚发布了一篇文章，文章ID为: " + articleId);
        System.out.println("文章所有属性值内容如下:");
        Map<String, String> articleData = redisArticleService.hgetAll("article:" + articleId);
        for (Map.Entry<String, String> entry : articleData.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();
    }


    @Test
    public void articleVote() throws Exception {
        String userId = "006";
        String articleId = "2";

        System.out.println("开始对文章" + "article:" + articleId + "进行投票啦~~~~~");
        //cang用户给James的文章投票
        redisArticleService.articleVote(userId, articleId);//article:1
        //投票完后，查询当前文章的投票数votes
        String votes = redisArticleService.hget("article:" + articleId, "votes");

        System.out.println("article:" + articleId + "这篇文章的投票数从redis查出来结果为: " + votes);
    }


    @Test
    public void getArticles() throws Exception {
        int page = 1;
        String key = "score:info";
        System.out.println("查询当前的文章列表集合为：");
        List<Map<String, String>> articles = redisArticleService.getArticles(page, key);
        for (Map<String, String> article : articles) {
            System.out.println("  id: " + article.get("id"));
            for (Map.Entry<String, String> entry : article.entrySet()) {
                if (entry.getKey().equals("id")) {
                    continue;
                }
                System.out.println("    " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

}