package com.concurrent.tools.principle;

/**
 * @program: su-concurrent
 * @description: ReentrantLock原理
 *     ReentrantLock也是实现了lock接口，并且内部维护了两个同步器（公平sync和非公平sync），首先定义sync实现AQS，然后公平sync和非公平sync再去继承sync
 *     非公平锁实现原理
 *     加锁原理
 *     解锁原理
 *     可重入原理
 *     可打断原理
 *     公平锁实现原理
 *     条件变量实现原理
 *     见源码
 * @author: Su
 * @create: 2020-10-09 19:29
 **/
public class ReentrantLockPrinciple {
    /**
     * 非公平锁实现原理
     *      它默认是非公平锁实现，通过构造方法看出
     *      public ReentrantLock() {
     *          sync = new NonfairSync();
     *          }
     */
}
