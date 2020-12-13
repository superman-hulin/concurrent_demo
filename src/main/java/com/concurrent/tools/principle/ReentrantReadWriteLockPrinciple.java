package com.concurrent.tools.principle;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Thread.sleep;

/**
 * @program: su-concurrent
 * @description: ReentrantReadWriteLock读写锁原理
 *      背景：
 *          如果同时有两个线程都来读数据，由于读操作不涉及修改，则如果还要加锁的话，则会影响性能
 *      当读操作远远高于写操作时，这时候使用 读写锁 让 读-读 可以并发，提高性能  让读-写或写-写是加锁互斥的
 *      主要事项
 *          读锁不支持条件变量
 *          重入时升级不支持：即持有读锁的情况下去获取写锁，会导致获取写锁永久等待
 *              r.lock();
 *              try{
 *                  w.lock(); //不支持
 *              }
 *          重入时降级支持：即持有写锁的情况下去获取读锁
 *      见源码
 * @author: Su
 * @create: 2020-10-10 15:36
 **/
public class ReentrantReadWriteLockPrinciple {
    public static void main(String[] args) {
        DataContainer dataContainer=new DataContainer();
        /**
         * 两个线程都可以同时获取到读锁 不会互斥
         */
        new Thread(()->{
            dataContainer.read();
        },"t1").start();
        new Thread(()->{
            dataContainer.read();
        },"t2").start();

        /**
         * 读写或写写 在获取锁时都会互斥
         */
        new Thread(()->{
            dataContainer.read();
        },"t3").start();
        new Thread(()->{
            dataContainer.write();
        },"t4").start();

    }
}

class DataContainer {
    private Object data;
    private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock r = rw.readLock();
    private ReentrantReadWriteLock.WriteLock w = rw.writeLock();

    public Object read() {
        System.out.println("获取读锁...");
        r.lock();
        try {
            System.out.println("读取");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return data;
        } finally {
            System.out.println("释放读锁...");
            r.unlock();
        }
    }
    public void write() {
        System.out.println("获取写锁...");
        w.lock();
        try {
            System.out.println("写入");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            System.out.println("释放写锁...");
            w.unlock();
        }
    }
}
class CachedData {
    Object data;
    // 是否有效，如果失效，需要重新计算 data
    volatile boolean cacheValid;
    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    void processCachedData() {
        //加读锁
        rwl.readLock().lock();
        //判断缓存数据是否有效
        if (!cacheValid) {
            // 如果无效，则需要写操作了 但是由于不支持升级，则需要获取写锁前必须释放读锁
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            try {
        // 判断是否有其它线程已经获取了写锁、更新了缓存, 避免重复更新
                if (!cacheValid) {
                    //data = ...
                    cacheValid = true;
                }
        // 降级为读锁, 释放写锁, 这样能够让其它线程读取缓存
                rwl.readLock().lock();
            } finally {
                rwl.writeLock().unlock();
            }
        }
        // 如果有效，则自己用完数据, 释放读锁
        try {
            //use(data);
        } finally {
            rwl.readLock().unlock();
        }
    }
}

