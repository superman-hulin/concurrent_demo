package com.concurrent.tools.principle;

import java.util.concurrent.locks.StampedLock;

import static java.lang.Thread.sleep;

/**
 * @program: su-concurrent
 * @description: 另一种读写锁
 *      该类自JDK8加入，是为了进一步优化读性能（之前讲读读操作时是可以并发的，但是由于读操作还是需要使用cas修改状态 则性能还可以进一步优化）
 *      如果想读操作的性能达到极致，则使用该类
 *      它的特点是在使用读锁、写锁时都必须配合【戳】使用
 *      读锁的加解：
 *          long stamp = lock.readLock(); //返回一个戳
 *          lock.unlockRead(stamp);  //使用戳来解锁
 *      写锁的加解：
 *          long stamp = lock.writeLock();
 *          lock.unlockWrite(stamp);
 *      真正提高读性能的是乐观读的方法
 *          乐观读，StampedLock 支持 tryOptimisticRead() 方法（乐观读），读取完毕后需要做一次 戳校验 如果校验通过，表示这期间确实没有写操作，数据可以安全使用，如果校验没通过，需要重新获取读锁，保证数据安全。
 *          long stamp = lock.tryOptimisticRead(); //该方法是没有任何加锁操作
 *          // 验戳  目的是判断获取戳和读取操作之间是否有写操作干扰
 *          if(!lock.validate(stamp)){ //如果验戳返回true 则直接读 否则 需要进行锁升级 升级为读锁
 *              // 锁升级
 *          }
 *      注意：
 *          StampedLock 不支持条件变量
 *          StampedLock 不支持可重入
 *     所以是使用ReentrantReadWriteLock还是StampedLock需要看特定的场景
 * @author: Su
 * @create: 2020-10-12 15:26
 **/
public class StampedLockPrinciple {

    public static void main(String[] args) {
        DataContainerStamped dataContainer = new DataContainerStamped(1);
        new Thread(() -> {
            dataContainer.read(1);
        }, "t1").start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
        new Thread(() -> {
            dataContainer.read(0);
        }, "t2").start();
    }
}

class DataContainerStamped {
    private int data;
    private final StampedLock lock = new StampedLock();
    public DataContainerStamped(int data) {
        this.data = data;
    }
    public int read(int readTime) {
        //乐观读
        long stamp = lock.tryOptimisticRead();
        System.out.println("optimistic read locking...{}"+stamp);
        try {
            Thread.sleep(readTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //判断读的内容是否被其它写线程更改了 如果没有则直接返回读的内容
        if (lock.validate(stamp)) {
            System.out.println("read finish...{}, data:{}"+stamp+data);
            return data;
        }
        // 锁升级 - 读锁  如果有 则加读锁 重新读
        System.out.println("updating to read lock... {}"+stamp);
        try {
            stamp = lock.readLock();
            System.out.println("read lock {}"+stamp);
            try {
                Thread.sleep(readTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("read finish...{}, data:{}"+stamp+data);
            return data;
        } finally {
            System.out.println("read unlock {}"+stamp);
            lock.unlockRead(stamp);
        }
    }

    public void write(int newData) {
        long stamp = lock.writeLock();
        System.out.println("write lock {}"+stamp);
        try {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.data = newData;
        } finally {
            System.out.println("write unlock {}"+stamp);
            lock.unlockWrite(stamp);
        }
    }
}