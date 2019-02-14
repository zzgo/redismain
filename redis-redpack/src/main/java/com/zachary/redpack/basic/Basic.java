package com.zachary.redpack.basic;

/**
 * @Title:
 * @Author:Zachary
 * @Desc:
 * @Date:2019/2/14
 **/
public class Basic {
    //基本的redis配置信息
    public static String IP = "192.168.111.128";
    public static int PORT = 6379;
    public static String AUTH = "zhangqi";

    public static int hongBaoCount = 1000;//红包数量

    public static int threadCount = 20;//线程数量

    public static String hongBaoPoolKey = "hongBaoPollKey";//List类型来模拟红包池子

    public static String hongBaoDetailListKey = "hongBaoDetailListKey";//List来记录用户抢红包的详情

    public static String userIdRecordKey = "userIdRecordKey";//Hash类型，记录了那些用户抢了红包，防止重复抢红包


	/*
     * KEYS[1]:hongBaoPoolKey：                   //键hongBaoPool为List类型，模拟红包池子，用来从红包池抢红包
	 * KEYS[2]:hongBaoDetailListKey：//键hongBaoDetailList为List类型，记录所有用户抢红包的详情
	 * KEYS[3]:userIdRecordKey：           //键userIdRecord为Hash类型，记录所有已经抢过红包的用户ID
	 * KEYS[4]:userid ：                              //模拟抢红包的用户ID
	 *
	 *
	 * jedis.eval(  Basic.getHongBaoScript,   4,    Basic.hongBaoPoolKey,  Basic.hongBaoDetailListKey,	Basic.userIdRecordKey,  userid);
	 *                      Lua脚本                                参数个数                  key[1]                     key[2]                       key[3]      key[4]
	*/

    public static String hongBaoLuaScript =
            //使用 redis.call()来执行redis 命令
            //判断userIdRecordKey 里面的用户userIId 是否存在了，存在了的话，就不需要抢，直接返回nil（空）
            "if redis.call('hexists',KEYS[3],KEYS[4]) ~= 0 then \n" +
                    "return nil;\n" +
            "else\n" +
                    //从红包池中取出一个红包
                    "local hongBao = redis.call('rpop',KEYS[1])\n" +
                    "if hongBao then\n" +
                        //local关键字，声明局部变量，x理解成table（表）类型, "cjson是c的一个类，decode解码成{"rid_1":1,"money":9}形式
                        "local x = cjson.decode(hongBao)\n" +
                        "x['userId'] = KEYS[4]\n" +
                        //encode 加密成redis能认识的格式 ， re = {"rid_1":1,"money":9,"userId":001}
                        "local re = cjson.encode(x)\n" +
                        //hset userIdRecordKey userId 为 1 记录用户已经抢过红包了
                        "redis.call('hset',KEYS[3],KEYS[4],'1')\n" +
                        //lpush key value list 从左边添加一条记录记录用户抢红包的详情
                        "redis.call('lpush',KEYS[2],re)\n" +
                        "return re\n" +
                    "end\n" +
            "end\n" +
            "return nil;";



    public static String getHongBaoScript =
            //查询用户是否已抢过红包，如果用户已抢过红包，则直接返回nil 
            "if redis.call('hexists', KEYS[3], KEYS[4]) ~= 0 then\n"   +
                    "return nil\n" +
                    "else\n"  +
                    //从红包池取出一个小红包
                    "local hongBao = redis.call('rpop', KEYS[1]);\n"  +
                    //判断红包池的红包是否为不空
                    "if hongBao then\n"  +
                    "local x = cjson.decode(hongBao);\n"  +
                    //将红包信息与用户ID信息绑定，表示该用户已抢到红包 
                    "x['userId'] = KEYS[4];\n"  +
                    "local re = cjson.encode(x);\n"  +
                    //记录用户已抢过userIdRecordKey  hset userIdRecordKey  userid 1
                    "redis.call('hset', KEYS[3], KEYS[4], '1');\n"  +
                    //将抢红包的结果详情存入hongBaoDetailListKey
                    "redis.call('lpush', KEYS[2], re);\n" +
                    "return re;\n"  +
                    "end\n"  +
                    "end\n"  +
                    "return nil";


}
