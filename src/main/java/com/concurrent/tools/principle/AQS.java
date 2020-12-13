package com.concurrent.tools.principle;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @program: su-concurrent
 * @description: AQS原理
 *      概述
 *          全称是 AbstractQueuedSynchronizer（队列同步器抽象类），是阻塞式锁和相关的同步器工具的框架
 *      特点：
 *          用 state 属性来表示资源的状态（分独占模式和共享模式），子类需要定义如何维护这个状态，控制如何获取锁和释放锁（比如设置state为1代表已获取到锁，为0代表无锁）
 *          getState - 获取 state 状态
 *          setState - 设置 state 状态
 *          compareAndSetState - cas 机制设置 state 状态
 *          独占模式是只有一个线程能够访问资源，而共享模式可以允许多个线程访问资源
 *          提供了基于 FIFO 的等待队列，类似于 Monitor 的 EntryList
 *          条件变量来实现等待、唤醒机制，支持多个条件变量，类似于 Monitor 的 WaitSet
 * @author: Su
 * @create: 2020-10-09 09:59
 **/
public class  AQS {
    /**
     * 基于该框架，具体实现的话，子类需要实现这些方法(默认抛出 UnsupportedOperationException)
     *      tryAcquire
     *      tryRelease
     *      tryAcquireShared
     *      tryReleaseShared
     *      isHeldExclusively
     * 获取锁
     *      // 如果获取锁失败 由子类自己定义逻辑 如是否加入到阻塞队列
     *      //如果获取成功，可以修改state状态
     *      if (!tryAcquire(arg)) {
     *      // 入队, 可以选择阻塞当前线程 park unpark
     *          }
     * 释放锁
     *      // 如果释放锁成功
     *      if (tryRelease(arg)) {
     *      // 让阻塞线程恢复运行
     *          }
     *
     */

}

/**
 * 实现一个不可重入锁
 *      需要分两步走，第一步是让锁的类实现lock接口  第二步是使用AQS
 */
class TestAQS{
    public static void main(String[] args) {
        MyLock lock=new MyLock();
        new Thread(()->{
            lock.lock();
            try{
                System.out.println("locking...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }finally {
                System.out.println("unlocking...");
                lock.unlock();
            }
        },"t1").start();
        new Thread(()->{
            lock.lock();
            try{
                System.out.println("locking...");
            }finally {
                System.out.println("unlocking...");
                lock.unlock();
            }
        },"t2").start();
    }

}


/**
 * 自定义锁（不可重入锁）
 */
class MyLock implements Lock{
    //需要一个同步器类  锁的功能大部分都是通过该同步器完成的
    //独占锁
    class MySync extends AbstractQueuedSynchronizer{
        /**
         *
         * @param arg 不可重入锁时，不需要使用该参数
         *             如果是可重入锁时，才需要使用该参数做些操作
         * @return
         */
        @Override
        protected boolean tryAcquire(int arg) {
            //获取锁 AQS中state初始值是0 则假设代表未加锁
            if(compareAndSetState(0,1)){
                //加上了锁 并设置owner为当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            //释放锁 由于只有持有锁线程，没有其他线程竞争执行该方法 则不需要cas更改state
            setExclusiveOwnerThread(null);
            setState(0);
            //正是由于state是volatile修饰的 防止了上面代码的指令重排 并且需要把setState(0)放在后面 因为这样才可以保证上面的变量可见性
            return true;
        }

        @Override //是否持有独占锁
        protected boolean isHeldExclusively() {
            return getState()==1;
        }

        public Condition newCondition(){
            //AQS中提供的内部类
            return new ConditionObject();
        }
    }

    private MySync sync=new MySync();

    @Override//加锁 不成功则进入等待队列
    public void lock() {
        //该方法内部会先调用tryAcquire 如果未成功 则加入阻塞队列
        sync.acquire(1);
    }

    @Override //加锁 可打断的
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override //尝试加锁 只尝试一次，直接返回加锁结果 不会死等
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1,unit.toNanos(time));
    }

    @Override  //解锁
    public void unlock() {
        //该方法内部会调用tryRelease 但是还会去唤醒正在阻塞的线程
        sync.release(1);
    }

    @Override //创建条件变量
    public Condition newCondition() {
        return sync.newCondition();
    }
}
