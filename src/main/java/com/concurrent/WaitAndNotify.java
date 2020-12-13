package com.concurrent;

/**
 * @program: su-concurrent
 * @description: wait和notify
 *      背景：为什么需要wait
 *          获取到锁的当前线程由于条件不满足，则不能继续进行计算，但如果一直占用着锁，其它线程就得一直阻塞，效率太低
 *          于是jvm引入wait/notify机制
 *      原理：
 *          Owner线程发现条件不满足，调用wait方法，即可进入WaitSet变为WAITING状态，
 *          BLOCKED和WAITING的线程都处于阻塞状态，不占用CPU时间片
 *          BLOCKED线程会在Owner线程释放锁时唤醒
 *          WAITING线程会在Owner线程调用notify或notifyALL时唤醒，但唤醒后并不意味着立刻获得锁，仍需进入EntryList重新竞争。
 *      API：
 *          obj.wait()让进入object监视器的线程到waitSet等待
 *          obj.notify()在object上正在waitSet等待的线程中挑一个唤醒
 *          obj.notifyAll()让object上正在waitSet等待的线程全部唤醒
 *          它们都是线程之间进行协作的手段，都属于Object对象的方法，必须获得此对象的锁，才能调用这几个方法
 *     wait notify的正确姿势
 *      sleep(long n)和wait(long n)的区别
 *          1)sleep是Thread方法，而wait是Object的方法
 *          2)sleep不需要强制和synchronized配合使用，但wait需要和synchronized一起用
 *          3)sleep在睡眠的同时，不会释放对象锁的。但wait在等待的时候会释放对象锁
 *          4)共同点，都是进入TIMED_WAITING状态
 * @author: Su
 * @create: 2020-09-22 12:06
 **/
public class WaitAndNotify {
    final static Object obj=new Object();
    public static void main(String[] args) {
        new Thread(()->{
            synchronized (obj){
                System.out.println("执行代码");
                try {
                    obj.wait();//让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("其它代码");
            }
        },"t1").start();

        new Thread(()->{
            synchronized (obj){
                System.out.println("执行代码");
                try {
                    obj.wait(15);//让线程在obj上等待15毫秒 如果没有其他线程进行唤醒，则15毫秒后可以自动继续执行后续代码
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("其它代码");
            }
        },"t2").start();

        //主线程2秒后执行
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (obj){
            obj.notify();//唤醒obj上一个线程
            obj.notifyAll();//唤醒obj上所有等待线程
        }
    }
}
