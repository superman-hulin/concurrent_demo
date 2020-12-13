package com.thread;

/**
 * @program: su-concurrent
 * @description: java中线程的六种状态
 * @author: Su
 * @create: 2020-09-17 19:34
 **/
public class ThreadState {
    public static void main(String[] args) {
        /**
         * 处于new状态 （还未start）
         */
        Thread t1=new Thread("t1"){
            @Override
            public void run() {
                System.out.println("running");
            }
        };
        /**
         * 处于runnable状态（java中的runnable状态包含 操作系统中的就绪状态、运行状态和阻塞状态）
         */
        Thread t2=new Thread("t2"){
            @Override
            public void run() {
                while (true){

                }
            }
        };
        t2.start();
        /**
         * 主线程睡眠了0.5秒，则t3线程会提前执行结束进入终止状态
         */
        Thread t3=new Thread(()->{
            System.out.println("running");
        },"t3");
        t3.start();
        /**
         * 处于timed_waiting状态
         */
        Thread t4=new Thread("t4"){
            @Override
            public void run() {
                synchronized (ThreadState.class){
                    try {
                        Thread.sleep(1000000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t4.start();
        /**
         * 处于waiting状态
         */
        Thread t5=new Thread("t5"){
            @Override
            public void run() {
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t5.start();
        /**
         * t4线程中会先执行拿到ThreadState锁，则t6线程拿不到该锁，则处于blocked状态
         */
        Thread t6=new Thread("t6"){
            @Override
            public void run() {
                synchronized (ThreadState.class){
                    try {
                        Thread.sleep(1000000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t6.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
}
