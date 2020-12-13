package com.concurrent;

import java.util.concurrent.locks.LockSupport;

/**
 * @program: su-concurrent
 * @description: park和unpark
 *      它们是LockSupport类中的方法
 *          LockSupport.park()表示暂停当前线程
 *          LockSupport.unpark(暂停线程对象)  表示恢复某个线程的运行
 *      注意：unpark可以在park之前调用 也可以在之后调用 都能恢复暂停线程的继续执行
 *
 *      与wait和notify相比
 *          wait notify必须配合Obj的Monitor一起使用，而park unpark不必
 *          park unpark是以线程为单位来 阻塞 和唤醒线程，而notify只能随机唤醒一个等待线程，notifyAll是唤醒所有等待线程 就不那么精确
 *          park unpark可以先unpark 而wait notify不能先notify
 *
 *      原理
 *
 * @author: Su
 * @create: 2020-09-23 09:23
 **/
public class ParkAndUnpark {
    public static void main(String[] args) {
        Thread t1=new Thread(()->{
            System.out.println("开始");
            try {
                /**
                 *加睡眠时间，使t1先park 然后主线程再unpark
                 * 如果该处睡眠2s 主线程睡眠1s 那么主线程先unpark t1再park 但是t1依然会继续执行后续代码 也就是没有park住
                 */
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LockSupport.park(); //当前线程暂停执行，对应的线程状态变为wait
            System.out.println("继续执行");
        },"t1");
        t1.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LockSupport.unpark(t1);//使t1线程继续向下执行
    }

}
