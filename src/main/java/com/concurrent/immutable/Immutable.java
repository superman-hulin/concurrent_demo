package com.concurrent.immutable;

import java.text.SimpleDateFormat;

/**
 * @program: su-concurrent
 * @description: 共享模型之不可变
 *     不可变类的使用
 *     不可变类设计
 *     无状态类设计
 *          在 web 阶段学习时，设计 Servlet 时为了保证其线程安全，都会有这样的建议，不要为 Servlet 设置成员变量，这
 *          种没有任何成员变量的类是线程安全的
 * @author: Su
 * @create: 2020-10-03 18:11
 **/
public class Immutable {
}

/**
 * 由于SimpleDateFormat类是可变类，同时也没有加线程安全，则下面多线程使用共享对象时出现问题
 * 解决方案：
 *  1.使用synchronized对sdf对象进行加锁  不过会有性能问题
 *  2.使用DateTimeFormatter  jdk8之后引入的类，是不可变类，不会有线程安全问题
 */
class TestDate{
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    System.out.println(sdf.parse("1951-04-21"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
