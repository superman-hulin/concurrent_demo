package com.concurrent.tools.threadPool;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * @program: su-concurrent
 * @description: 任务调度线程池
 * @author: Su
 * @create: 2020-10-07 10:27
 **/
public class ScheduledThreadPool {
    /**
     * 延时执行
     *      某个任务有异常时，不会影响其他任务的正常执行
     * @param args
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //JDK Executors 类中提供了众多工厂方法来创建各种用途的线程池
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        // 添加两个任务，希望它们都在 1s 后执行  任务1中睡眠2秒不会影响到任务2的定时执行
        executor.schedule(() -> {
            System.out.println("任务1");
            try { Thread.sleep(2000); } catch (InterruptedException e) { }
        }, 1000, TimeUnit.MILLISECONDS);

        executor.schedule(() -> {
            System.out.println("任务2");
        }, 1000, TimeUnit.MILLISECONDS);

        /**
         * 正确处理线程池异常
         */
        //自己使用try catch捕获 调用者才可以看到异常信息
        executor.schedule(() -> {
            System.out.println("任务3");
            //int i=1/0;如果只这样写，则调用者在控制台看不到异常信息
            try{
                int i=1/0;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }, 1000, TimeUnit.MILLISECONDS);

        //使用future可接收异常
       Future<Boolean> f= executor.submit(() -> {
            System.out.println("任务3");
            int i=1/0;
            return true;
        });
       //如果任务执行没有产生异常，则返回 返回值，如果产生了异常，则将异常返回
       f.get();
    }
    /**
     * 定时执行
     *      每隔多久执行一次
     */
    public static void method(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(()->{
            System.out.println("running");
            //如果任务执行时间大于执行周期的时间，则以任务执行时间为主
        },1,1,TimeUnit.SECONDS);//延时1秒开始执行  执行周期为1秒
    }
    /**
     * 定时执行
     *      重复执行之间间隔一定时间再执行
     */
    public static void method1(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleWithFixedDelay(()->{
            System.out.println("running");
        },1,1,TimeUnit.SECONDS);//延时1秒开始执行  上一次执行结束过1秒后再开始执行下一次
    }
}

/**
 * 如何让每周四18:00:00 定时执行任务
 */
class TestSchedule{
    public static void main(String[] args) {
        // 获得当前时间
        LocalDateTime now = LocalDateTime.now();
        // 获取本周四 18:00:00.000
        LocalDateTime thursday =
                now.with(DayOfWeek.THURSDAY).withHour(18).withMinute(0).withSecond(0).withNano(0);
        // 如果当前时间已经超过 本周四 18:00:00.000， 那么找下周四 18:00:00.000
        if(now.compareTo(thursday) >= 0) {
            thursday = thursday.plusWeeks(1);
        }
        // 计算时间差，即延时执行时间
        long initialDelay = Duration.between(now, thursday).toMillis();
        // 计算间隔时间，即 1 周的毫秒值
        long oneWeek = 7 * 24 * 3600 * 1000;
        ScheduledExecutorService pool=Executors.newScheduledThreadPool(1);
        pool.scheduleAtFixedRate(()->{

        },initialDelay, oneWeek, TimeUnit.MILLISECONDS);
    }

}
