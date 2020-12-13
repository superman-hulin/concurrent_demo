package com.thread;

/**
 * @program: su-concurrent
 * @description: 守护线程
 *  默认情况下，java进程需要等待所有线程都运行结束，才会结束。
 *  有一种特殊的线程叫做守护线程，只要其它非守护线程运行结束了，即使守护线程的代码没有执行完，也会强制结束。
 *
 *  守护线程的使用场景
 *      垃圾回收器线程就是一种守护线程
 * @author: Su
 * @create: 2020-09-17 10:31
 **/
public class DaemonThread {
    public static void main(String[] args) throws InterruptedException {
        Thread t1=new Thread(()->{
            while (true){
                if(Thread.currentThread().isInterrupted()){
                    break;
                }
            }
        });
        t1.start();
        Thread.sleep(1000);
        //上诉场景中，主线程结束了，java进程也不会结束，因为还有线程t1在执行

        //在启动之前将t1线程设置为守护线程 ,这样当主线程结束了，则守护线程即使没执行完，也会结束
        t1.start();
        t1.setDaemon(true);
    }
}
