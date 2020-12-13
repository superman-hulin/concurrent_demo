package com.thread;

/**
 * @program: su-concurrent
 * @description: 线程的常用方法
 * @author: Su
 * @create: 2020-09-17 08:28
 **/
public class ThreadMethod {
    public static void main(String[] args) {
        Thread t1=new Thread("t1"){
            @Override
            public void run() {
                System.out.println("running");
            }
        };
        /**如果直接使用线程的run方法，则其实是主线程去执行run方法，并非多线程
         * 只能调用t1的start方法，这样才能是t1线程去执行run方法
         *
         */
        t1.run();
        /**
         * 获取线程的状态信息
         */
        System.out.println(t1.getState());
        /**
         * 使用sleep方法
         */
        Thread t2=new Thread("t2"){
            @Override
            public void run() {
                try {
                    //Thread.sleep在哪个线程中被调用，哪个线程就休眠
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //正在睡眠中的线程有可能被其它线程打断，打断时会抛这个InterruptedException
                    e.printStackTrace();
                }
            }
        };
        //写在主线程下 则主线程休眠
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //打断正在睡眠的t2线程，将其唤醒
        t2.interrupted();

        /**
         * yield方法：
         *  1.调用yield会让当前线程从Running进入Runnable就绪状态，然后让cpu调度执行其它线程
         *  2.具体的实现依赖于操作系统的任务调度器 当只有该线程时，即使yield了，但是cpu依然还是会将时间片分为该线程
         * 相当于主动将cpu时间片让出去给其它线程
         * 和sleep的区别：
         *      1. sleep后 线程进入阻塞状态 cpu不会将时间片分给阻塞状态的线程 而yield后，线程进入就绪状态，等待cpu调度
         *      2. sleep会有时间 而yield没有时间
         *
         * 线程优先级
         *      1.线程优先级会提示调度器优先调度该线程，但它仅仅是一个提示，调度器可以忽略它
         *      2.如果cpu比较忙，那么优先级高的线程会获得更多的时间片，但cpu闲时，优先级几乎没作用
         */
        Runnable runnable1=()->{
            int count=0;
            for(;;){
                System.out.println("--->1"+ count++);
            }
        };
        Runnable runnable2=()->{
            int count=0;
            for(;;){
                //如果不加yield进行干扰，则两个线程打印的count应该比较接近，加了之后，则任务一的count明显比二要加的快
                Thread.yield();
                System.out.println("--->2"+ count++);
            }
        };
        Thread t3=new Thread(runnable1,"t3");
        Thread t4=new Thread(runnable2,"t4");
        //通过设置优先级
        t3.setPriority(Thread.MIN_PRIORITY);
        t4.setPriority(Thread.MAX_PRIORITY);

        /**
         * 使用join方法
         */
        //主线程等待线程t2结束
        try {
            t2.join();//持续等待 直至线程执行结束
            t2.join(1500);//指定最长等待时间，超过了就不等待了
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * interrupt方法详解
         *  1.打断处于阻塞状态的线程：sleep、wait、join  会清空打断状态，即打断后该类线程的打断标记会被记为false( t2.isInterrupted()查看)
         *  2.打断正常运行的线程，不会清空打断状态  该线程被打断后不会停止执行，可以根据该线程的isInterrupted()方法查看是否被别的线程打断，通过判断的方式控制执行
         */
        Thread t5=new Thread(()->{
            while (true){
                //根据打断标记控制执行
                boolean thread=Thread.currentThread().isInterrupted();
                if(thread){
                    break;
                }
            }
        },"t5");
        t5.start();
        //打断t5线程
        t5.interrupt();

    }
}
