package com.zachary.redpack.core;

import com.zachary.redpack.basic.Basic;
import com.zachary.redpack.utils.JedisUtil;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @Title:
 * @Author:Zachary
 * @Desc: 用户抢红包
 * @Date:2019/2/14
 **/
public class GetRedPack {
    public void getHongBao() {
        final JedisUtil jedis = new JedisUtil(Basic.IP, Basic.PORT, Basic.AUTH);
        final CountDownLatch count = new CountDownLatch(Basic.threadCount);
        for (int i = 0; i < Basic.threadCount; i++) {
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        //模拟用户ID
                        String userId = UUID.randomUUID().toString();
                        Object object = jedis.eval(Basic.hongBaoLuaScript, 4, Basic.hongBaoPoolKey, Basic.hongBaoDetailListKey, Basic.userIdRecordKey, userId);
                        if (object != null) {
                            System.out.println("用户" + userId + "抢到红包，数量是：" + object);
                        } else {
                            if (jedis.llen(Basic.hongBaoPoolKey) == 0) {
                                break;
                            }
                        }
                    }
                    count.countDown();
                }
            }.start();
        }
        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
