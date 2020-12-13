package com.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * @program: su-concurrent
 * @description: 卖票线程安全
 * @author: Su
 * @create: 2020-09-18 13:52
 **/
public class ThreadSafeExerciseSell {
    //Random为线程安全
    static Random random=new Random();

    //买票的随机数
    public static int randomAmount(){
        return random.nextInt(5)+1;
    }
    public static void main(String[] args) throws InterruptedException {
        //模拟售票对象
        TicketWindow window=new TicketWindow(1000);
        //所有线程的集合
        List<Thread> threadList=new ArrayList<>();
        //卖出的票数统计
        List<Integer> amountList=new Vector<>();
        //通过多线程模拟多人买票
        for(int i=0;i<2000;i++){
            Thread thread=new Thread(()->{
                //window变量是多个线程共享的，则看下该变量是否有线程安全问题，而sell方法中存在多个线程共享变量count，则存在线程安全问题
                int amount=window.sell(randomAmount());
                //amountList是共享变量，add操作存在读写操作，则存在线程安全问题，而Vector就是线程安全版的集合。
                amountList.add(amount);
                //上诉两个的组合不存在线程安全问题，因为它们两个是不同的共享变量，而不是同一个
            });
            //不存在多个线程共享threadList变量，所以不存在线程安全问题
            threadList.add(thread);
            thread.start();
        }

        //需要等子线程都执行结束后，主线程才统计
        for(Thread thread:threadList){
            thread.join();
        }
        /**
         *  怎么验证是线程安全的，即余票+卖出的票=总票数
         */
        //统计卖出的票和余票
        System.out.println("余票：" +window.getCount());
        System.out.println("卖出的票"+amountList.stream().mapToInt(i->i).sum());

    }


}

//售票窗口
class TicketWindow{
    private int count;

    public TicketWindow(int count) {
        this.count = count;
    }

    //获取余票数量
    public int getCount() {
        return count;
    }

    //售票
    public synchronized int sell(int amount){
        if(this.count>=amount){
            this.count-=amount;
            return amount;
        }else {
            return 0;
        }
    }

}
