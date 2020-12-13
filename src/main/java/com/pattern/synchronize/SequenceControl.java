package com.pattern.synchronize;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: su-concurrent
 * @description: 同步模式之顺序控制： 控制线程的运行次序
 * @author: Su
 * @create: 2020-09-25 09:39
 **/

/**
 * 固定运行顺序
 * 需求1：先打印2再打印1
 */
//使用wait notify的方式解决
public class SequenceControl {
    static final Object lock=new Object();
    //使用标识判断线程2是否打印了2
    static boolean flag=false;

    public static void main(String[] args) {
        Thread t1=new Thread(()->{
            synchronized (lock) {
                while (!flag) {
                    try {
                        //线程2未运行 则进入等待
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("1");
            }
        },"t1");

        Thread t2=new Thread(()->{
            synchronized (lock){
                System.out.println("2");
                flag=true;
                lock.notify();
            }
        },"t2");
        t1.start();
        t2.start();
    }

    /**
     * 使用park unpark方式解决
     */
    public void method1(){
        Thread t1=new Thread(()->{
            LockSupport.park();
            System.out.println("1");
        },"t1");
        Thread t2=new Thread(()->{
            System.out.println("2");
            LockSupport.unpark(t1);
        },"t2");

        t1.start();
        t2.start();
    }

}

/**
 * 交替输出
 * 线程1输出a 5次，线程2输出b 5次，线程3输出c 5次 现在要求输出abcabcabcabcabc怎么实现
 * 分析
 *     输出内容    等待标记   下一个标记
 *     a          1         2
 *     b          2         3
 *     c          3         1
 */
//使用wait notify解决
class Test{
    public static void main(String[] args) {
        SynWaitNotify synWaitNotify=new SynWaitNotify(1,5);
        new Thread(()->{
            synWaitNotify.print("a",1,2);
        },"t1").start();
        new Thread(()->{
            synWaitNotify.print("b",2,3);
        },"t2").start();
        new Thread(()->{
            synWaitNotify.print("c",3,1);
        },"t3").start();
    }
}
class SynWaitNotify{
    //等待标记
    private int flag;
    //循环打印的次数
    private int loopNumber;

    public SynWaitNotify(int flag, int loopNumber) {
        this.flag = flag;
        this.loopNumber = loopNumber;
    }
    //打印
    public void print(String str,int waitFlag,int nextFlag) {
        for (int i = 0; i < loopNumber; i++) {
            synchronized (this) {
                //根据waitFlag反映出是哪个线程调用打印 但是是否轮到该线程打印 需要看waitFlag值是否和flag一致
                while (flag != waitFlag) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print(str);
                //将flag改为下一个线程运行的标记
                flag = nextFlag;
                //唤醒其它线程
                this.notifyAll();
            }
        }
    }
}
//使用await signal解决
class Test2{
    public static void main(String[] args) {
        AwaitSignal awaitSignal=new AwaitSignal(5);
        Condition a=awaitSignal.newCondition();
        Condition b=awaitSignal.newCondition();
        Condition c=awaitSignal.newCondition();
        new Thread(()->{
            awaitSignal.print("a",a,b);
        },"t1").start();
        new Thread(()->{
            awaitSignal.print("b",b,c);
        },"t2").start();
        new Thread(()->{
            awaitSignal.print("c",c,a);
        },"t3").start();
        //三个线程一开始运行都会进入各自休息室等待 则需要一个发起者使线程a先运行
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //需要先获取锁 然后唤醒a线程
        awaitSignal.lock();
        try{
            a.signal();
        }finally {
            awaitSignal.unlock();
        }

    }

}
class AwaitSignal extends ReentrantLock{
    private int loopNumber;

    public AwaitSignal(int loopNumber) {
        this.loopNumber = loopNumber;
    }
    //参数1 打印内容  参数2 进入哪一间休息室 参数3 唤醒下一间休息室
    public void print(String str, Condition current,Condition next){
        for(int i=0;i<loopNumber;i++){
            lock();
            try {
                try {
                    //每个线程都先进入自己的休息室等待
                    current.await();
                    //被唤醒 开始打印
                    System.out.print(str);
                    next.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }finally {
                unlock();
            }
        }
    }
}
//使用park unpark解决
class Test3{
    static Thread t1;
    static Thread t2;
    static Thread t3;
    public static void main(String[] args) {
        ParkUnpark parkUnpark=new ParkUnpark(5);
       t1= new Thread(()->{
            parkUnpark.print("a",t2);
        },"t1");
        t2=new Thread(()->{
            parkUnpark.print("b",t3);
        },"t2");
        t3=new Thread(()->{
            parkUnpark.print("c",t1);
        },"t3");
        t1.start();
        t2.start();
        t3.start();
        //发起者
        LockSupport.unpark(t1);
    }
}
class ParkUnpark{
    private int loopNumber;

    public ParkUnpark(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    /**
     *
     * @param str 打印的内容
     * @param next 需要唤醒的下一个线程
     */
    public void print(String str,Thread next){
        for(int i=0;i<loopNumber;i++){
            //当前线程暂停运行
            LockSupport.park();
            System.out.println(str);
            LockSupport.unpark(next);
        }
    }
}


