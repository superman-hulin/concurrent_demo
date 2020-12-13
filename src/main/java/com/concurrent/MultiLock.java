package com.concurrent;

/**
 * @program: su-concurrent
 * @description: 多把锁
 *  背景：
 *    之前都是使用synchronized来锁住一个对象实现互斥访问 这样一把锁的话会使并发度较低
 *    多把不相干的锁
 *      假设一间大屋子有两个功能：睡觉、学习互不相关
 *      现在线程1要学习，线程2要睡觉 但如果只用一间屋子（一个对象锁）的话 那么并发度很低
 *      解决方法是准备多个房间（多个对象锁）
 *   将锁的粒度细分
 *      好处，可以增强并发度
 *      坏处 如果一个线程需要同时获得多把锁，就容易发生死锁
 *  线程的活跃性
 *      指线程内的代码本来是有限的，但是由于某种原因线程代码一直没运行完
 *      活跃性分为三种情况：死锁、活锁和饥饿
 *      死锁：
 *          为了增加并发度，将锁变得更细粒度了，会有这样的情况：一个线程需要同时获得多把锁，这时就容易发生死锁
 *          比如 t1线程获得A对象锁，接下来想获取B对象的锁
 *              t2线程获得B对象锁，接下来想获取A对象的锁
 *          典型的情景有哲学家就餐问题
 *      活锁：
 *          活锁出现在两个线程互相改变对方的结束条件，最后谁也无法结束
 *
 *      活锁和死锁的区别：
 *          死锁是由于多个线程互相持有对方所想要的锁，导致谁都无法继续向下运行，所有线程都阻塞了
 *          而活锁的线程没有阻塞，都在不断的运行，但是由于改变了对方的结束条件，最后每个线程都无法结束运行
 *      解决活锁的方法
 *          使产生活锁的这几个线程的执行指令错开
 *
 *      饥饿
 *         一个线程由于优先级太低，始终得不到CPU调度执行，也不能够结束，饥饿的情况不易演示，讲读写锁时会涉及饥饿问题
 *         一个线程饥饿的例子：
 *              死锁问题是由于两个线程没有按顺序获取两个锁，如果都按顺序加锁的话 可以解决死锁，比如t1先获取A 再获取B，t2也需要先获取A 再获取B。
 *              顺序加锁解决哲学家就餐问题：
 *                  哲学家1获取筷子c1，c2
 *                  哲学家2获取筷子c2，c3
 *                  哲学家3获取筷子c3，c4
 *                  哲学家4获取筷子c4，c5
 *                  哲学家5获取筷子c1，c5  //此处如果c5 c1 则就不是顺序加锁了
 *              这样按顺序加锁的话 不会导致死锁，但是会导致哲学家5始终获取不到锁，从而产生饥饿
 * @author: Su
 * @create: 2020-09-23 10:43
 **/
public class MultiLock {
    /**
     * 死锁
     */
    public static void test() {
        Object A = new Object();
        Object B = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (A) {
                System.out.println("lock A");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (B) {

                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (B) {
                System.out.println("lock B");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (A) {

                }
            }
        }, "t2");
        t1.start();
        t2.start();
    }
    /**
     * 活锁
     *  线程t1不断减少 线程t2不断增加
     *  导致互相都无法结束
     */
    static volatile int count=10;
    static final Object lock=new Object();
    public static void test2(){
        new Thread(()->{
            //期望减到0退出循环
            while (count>0){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count--;
            }
        },"t1").start();
        new Thread(()->{
            //期望超过20退出循环
            while (count<20){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
            }
        },"t2").start();
    }

}

/**
 * 多把锁
 */
class BigRoom{
    public void sleep(){
        synchronized (this){ //这样锁住this 使得互不相干的两个功能不能并发
            try {
                System.out.println("睡觉");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void study(){
        synchronized (this){
            try {
                System.out.println("学习");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 准备两个房间 这样多把锁来增加了程序的并发度 只要保证业务是互不相干的
     */
    private final Object studyRoom=new Object();
    private final Object bedRoom=new Object();
    public void sleep1(){
        synchronized (studyRoom){
            try {
                System.out.println("睡觉");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void study1(){
        synchronized (bedRoom){
            try {
                System.out.println("学习");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}
