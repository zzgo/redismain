package com.zachary.redpack.main;

import com.zachary.redpack.core.GetRedPack;
import com.zachary.redpack.core.InitRedPack;

/**
 * @Title:
 * @Author:Zachary
 * @Desc:
 * @Date:2019/2/14
 **/
public class App {
    public static void main(String[] args) {
        new InitRedPack().init();
        new GetRedPack().getHongBao();
    }
}
