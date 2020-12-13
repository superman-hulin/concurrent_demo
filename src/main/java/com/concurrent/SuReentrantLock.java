package com.concurrent;

import lombok.val;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: su-concurrent
 * @description: 可重入锁ReentrantLock
 *  相对于synchronized 它具备如下特点
 *     可中断
 *        synchronized把锁加上之后是不能中断的，无法使用语法和其它线程来破坏这个锁和取消这个锁  但ReentrantLock可以
 *     可以设置超时时间
 *        synchronized的话，线程一旦获取不到锁就会在EntryList中一直等待 但ReentrantLock可以设置超时时间，规定在这个时间内如果
 *        获取不到锁，则放弃等待加锁 先去执行其它的逻辑
 *     可以设置为公平锁
 *        公平锁就是为了防止线程饥饿的现象，让正在排队等待加锁的线程先到先得 而不是随机竞争
 *     支持多个条件变量
 *        条件变量就相当于synchronized中的waitSet（即拿到锁的线程在运行过程中发现条件不满足，则主动释放锁进入waitSet等待该条件）
 *        synchronized中只有一个waitSet 即而ReentrantLock支持多个
 *        这样的好处是将等待相同条件的线程放到同一个waitSet，这样后续的notify线程可以细分唤醒哪类条件线程
 *  相同点：都支持可重入
 *  可重入：
 *      指同一个线程如果首次获得了这把锁，那么因为它是这把锁的拥有者，因此有权利再次获取这把锁
 *      如果是不可重入锁，那么第二次获得锁时，自己也会被锁挡住进不去
 * @author: Su
 * @create: 2020-09-23 20:57
 **/
public class SuReentrantLock {

    /**
     * 可重入 main调用m1方法时，m1方法中开始获取锁，然后执行m2 m2中又进行获取锁也成功 则说明是可重入锁
     */
    //synchronized是关键字级别加锁 而ReentrantLock是对象级加锁
    static ReentrantLock lock=new ReentrantLock();//这本身就是一把锁

//    public static void main(String[] args) {
//        method1();
//    }
    public static void method1(){
        /**
         * lock()就是加锁 如果获得了锁 当前线程就是这把锁的主人 如果获取不到 则进入lock对象中的等待队列进入等待
         * 相当于lock对象取代了之前的Monitor对象
         */
        lock.lock();
        try{
            System.out.println("执行 method1");
            method2();
        }finally {
            lock.unlock(); //解锁
        }
    }

    public static void method2(){
        lock.lock();
        try{
            System.out.println("执行 method2");
            method3();
        }finally {
            lock.unlock();
        }
    }
    public static void method3(){
        lock.lock();
        try{
            System.out.println("执行 method3");
        }finally {
            lock.unlock();
        }
    }

    /**
     * 可打断
     *    指在等待锁的过程中，其它线程可以使用 方法中止该等待
     *    默认的话 synchronized和lock()方法获取锁时是不可打断的 也就是其它线程持有了锁，当前想要获取锁的线程会一直等待下去
     *    但我们希望不一直等待下去，希望这个等待过程可以被中止
     *    意义：使用这种可打断锁，可以避免死锁发生，让某线程不持续等待下去
     */
    public static void main(String[] args) {
        //使用这种方法加锁时，等待加锁的过程是可以被其它线程打断的
        Thread t1 = new Thread(() -> {
            try {
                //如果没有竞争 则该方法会正常获取锁  如果有竞争就进入阻塞队列 可以被其它线程用interrupt方法进行打断
                //当被打断时，就进入InterruptedException的catch
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                //被打断时，执行该处代码
                e.printStackTrace();
                //由于没有获得到锁，该方法应该直接返回 不应该继续执行后续代码
                return;
            }
            try{
                System.out.println("获取到锁了");
            }finally {
                lock.unlock();
            }
        }, "t1");
        //主线程先获取锁
        lock.lock();
        //再启动t1线程 则获取不到锁 进入阻塞队列
        t1.start();
        //打断正在等待锁的线程t1
        t1.interrupt();
    }

    /**
     * 锁超时
     *     可打断的机制是被动结束等待，而锁超时是主动的方式避免死锁的
     *     尝试获取锁，如果一段时间内获取失败 则主动放弃
     *     使用该种获取锁方式可以解决哲学家就餐问题的死锁 并且不会导致有哲学家出现饥饿
     */
    public static void method4(){
        new Thread(()->{
            //尝试获取锁 返回true代表获取成功 false代表获取失败
            //如果获取失败 会立即返回 不会等待
            if(!lock.tryLock()){
                System.out.println("获取不到锁");
                return;
            }
            try {
                //如果获取失败 会等待1分钟再返回结果
                boolean flag=lock.tryLock(1,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //这个也支持可打断
                return;
            }
            try{
                System.out.println("获取到锁");
            }finally {
                lock.unlock();
            }
        }).start();
    }

    /**
     * 公平锁
     *    ReentrantLock默认是不公平的 可以通过构造函数来进行修改
     *    公平锁一般没有必要 会降低并发度
     */
    //改为公平锁
    ReentrantLock lock1=new ReentrantLock(true);

    /**
     * 条件变量
     *   synchronized中也有条件变量，就是那个waitSet休息室，当条件不满足时进入waitSet等待
     *   ReentrantLock的条件变量比synchronized强大之处在于，它是支持多个条件变量的，就好比：
     *      synchronized是那些不满足条件的线程都在一间休息室等消息
     *      而ReentrantLock支持多间休息室，有专门等烟的休息室、等早餐的休息室，唤醒时也是按休息室来唤醒
     */
    ReentrantLock reentrantLock=new ReentrantLock();
    public void method5(){
        //创建一个新的条件变量（休息室）
        Condition condition = reentrantLock.newCondition();
        Condition condition2 = reentrantLock.newCondition();
        //await前需要先获得锁 即进入休息室前需要先获得锁 和synchronized一样，wait前需要先获得锁
        reentrantLock.lock();
        //进入休息室等待
        try {
            //await执行后 会释放锁 进入conditionObject等待
            //await的线程被唤醒（或打断、或超时）去重新竞争lock锁
            //竞争lock锁成功后 从await后继续执行
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //唤醒条件变量condition中的某一个线程
        condition.signal();
        //唤醒该条件变量中的所有线程
        condition.signalAll();
    }

}
