package com.zachary.redpack.core;


import com.alibaba.fastjson.JSONObject;
import com.zachary.redpack.basic.Basic;
import com.zachary.redpack.utils.JedisUtil;

import java.util.concurrent.CountDownLatch;

/**
 * @Title:
 * @Author:Zachary
 * @Desc:初始化红包
 * @Date:2019/2/14
 **/
public class InitRedPack {

    /**
     * 初始化红包池子
     */
    public void init() {
        final JedisUtil jedis = new JedisUtil(Basic.IP, Basic.PORT, Basic.AUTH);
        jedis.flushall();  //清空,线上不要用.....
        final CountDownLatch count = new CountDownLatch(Basic.threadCount);
        for (int i = 0; i < Basic.threadCount; i++) {
            final int page = i;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    int mCount = Basic.hongBaoCount / Basic.threadCount;
                    for (int j = page * mCount; j < (page + 1) * mCount; j++) {
                        JSONObject json = new JSONObject();
                        json.put("id", "rid_" + j);
                        json.put("money", j);
                        jedis.lpush(Basic.hongBaoPoolKey, json.toJSONString());
                    }

                    count.countDown();
                }
            };
            thread.start();
        }
        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
