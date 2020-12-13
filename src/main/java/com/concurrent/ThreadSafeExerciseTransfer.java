package com.concurrent;

import java.util.Random;

/**
 * @program: su-concurrent
 * @description: 线程安全之转账案例
 * @author: Su
 * @create: 2020-09-18 16:59
 **/
public class ThreadSafeExerciseTransfer {
    //Random为线程安全
    static Random random=new Random();

    //随机数
    public static int randomAmount(){
        return random.nextInt(100)+1;
    }
    public static void main(String[] args) throws InterruptedException {
        Account a=new Account(1000);
        Account b=new Account(1000);
        Thread t1=new Thread(()->{
            for (int i=0;i<1000;i++){
                /**该方法中有两个共享变量 a的余额和b的余额
                 * 如果仅在该方法中加synchronized是不行的 因为
                 * synchronized (this){
                 * }
                 * 保护的是this对象的共享变量，而另一个共享变量没有被保护
                 * 则使用synchronized (Account.class){
                 *   }来锁住这两个共享变量  因为它们都属于Account
                 *
                 *   但其实使用这种方式 性能是不太高的 因为到时候会有很多个Account对象，所以只能有两个账户互相转账才能正常运行
                 */

                a.transfer(b,randomAmount());
            }
        },"t1");
        Thread t2=new Thread(()->{
            for (int i=0;i<1000;i++){
                b.transfer(a,randomAmount());
            }
        },"t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        //查看转账2000次后的总金额
        System.out.println(a.getMoney()+b.getMoney());
    }
}

class Account{
    private int money;

    public Account(int money) {
        this.money = money;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void transfer(Account target,int amount){
        if(this.money>=amount){
            this.setMoney(this.getMoney()-amount);
            target.setMoney(target.getMoney()+amount);
        }
    }
}
