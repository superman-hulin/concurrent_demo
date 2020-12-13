package com.concurrent.tools;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/**
 * @program: su-concurrent
 * @description: 倒计时锁
 *      概念
 *          用来进行线程同步协作，等待所有线程完成倒计时。
 *         其中构造参数用来初始化等待计数值，await() 用来等待计数归零，countDown() 用来让计数减一
 * @author: Su
 * @create: 2020-10-12 20:43
 **/
public class SuCountdownLatch {
    public static void main(String[] args) throws InterruptedException {
        //创建倒计时锁，并且state为3
        CountDownLatch latch = new CountDownLatch(3);
        new Thread(() -> {
            System.out.println("begin...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程任务完成 对state减1
            latch.countDown();
            System.out.println("end...{}"+latch.getCount());
        }).start();

        new Thread(() -> {
            System.out.println("begin...");
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
            System.out.println("end...{}"+latch.getCount());
        }).start();
        new Thread(() -> {
            System.out.println("begin...");
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程任务完成 对state减1
            latch.countDown();
            System.out.println("end...{}"+latch.getCount());
        }).start();
        System.out.println("waiting...");
        //主线程进入阻塞 需要等到state减为0才继续运行
        latch.await();
        System.out.println("wait end...");
    }
}

/**
 * 主线程等待线程执行结束 为什么不用join来替换
 * 原因：
 *    1.join是偏底层的api 而倒计时锁是高级api
 *    2.一般的场景都是使用线程池中的线程，而不是自己创建 对于固定线程池中的线程 会一直在运行任务 则使用join就不行
 */
class CountdownLatchPool{
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService service = Executors.newFixedThreadPool(4);
        service.submit(() -> {
            System.out.println("begin...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程任务完成 对state减1
            latch.countDown();
            System.out.println("end...{}"+latch.getCount());
        });
        service.submit(() -> {
            System.out.println("begin...");
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程任务完成 对state减1
            latch.countDown();
            System.out.println("end...{}"+latch.getCount());
        });
        service.submit(() -> {
            System.out.println("begin...");
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程任务完成 对state减1
            latch.countDown();
            System.out.println("end...{}"+latch.getCount());
        });
        service.submit(()->{
            try {
                System.out.println("waiting...");
                latch.await();
                System.out.println("wait end...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}

/**
 * 主线程等待多线程准备完毕
 * 模拟游戏加载
 */
class TestCountdownLatch{
    public static void main(String[] args) {
       ExecutorService pool= Executors.newFixedThreadPool(10);
       CountDownLatch countDownLatch=new CountDownLatch(10);
       String[] all=new String[10];
       Random r=new Random();
        for (int j = 0; j <10 ; j++) {
            int k=j;
            pool.submit(()->{
                for (int i = 0; i <=100; i++) {
                    try {
                        Thread.sleep(r.nextInt(50));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    all[k]=i+"%";
                    System.out.print("\r"+Arrays.toString(all));
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pool.shutdown();

    }
}
