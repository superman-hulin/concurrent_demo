package com.concurrent;

import lombok.extern.slf4j.Slf4j;

/**
 * @program: su-concurrent
 * @description: 使用synchronized实现临界区访问
 *  临界区
 *     一个程序运行多个线程本身是没有问题的
 *     问题出在多个线程访问共享资源
 *          多个线程读共享资源其实也没有问题
 *          在多个线程对共享资源读写操作时发生指令交错，就会出现问题
 *     一段代码块内如果存在对共享资源的多线程读写操作，称这段代码块为临界区
 *
 *  竞态条件
 *      多个线程在临界区内执行，由于代码的执行序列不同而导致结果无法预测，称之为发生了竞态条件
 *
 *  为了避免临界区的竞态条件发生，有多种手段可以达到目的
 *      1.阻塞式的解决方案 synchronized(俗称对象锁)，Lock
 *          每个线程进临界区时需要使用synchronized来获取对象锁，此时其它线程无法获得锁，只能处于阻塞状态，直至线程执行完
 *        释放锁，其余线程才被唤醒进入就绪状态。若有锁的那个线程在执行过程中时间片用完，则该线程等待下一次被cpu调度。在这个等待过程中
 *        其它线程依然进不去临界区
 *      2.非阻塞式的解决方案 原子变量
 *  synchronized实际是用对象锁保证了临界区内代码的原子性，临界区内的代码对外是不可分割的，不会被线程切换所打断
 *
 *  变量的线程安全分析
 *    成员变量和静态变量是否线程安全
 *      1.如果它们没有共享，则线程安全
 *      2.如果它们被共享了，根据它们的状态是否能够改变，又分两种情况
 *          如果只有读操作，则线程安全
 *          如果有读写操作，则这段代码是临界区，需要考虑线程安全
 *    局部变量是否线程安全
 *      局部变量是线程安全的
 *      但局部变量引用的对象则未必
 *          如果该对象没有逃离方法的作用访问，它是线程安全的
 *          如果该对象逃离方法的作用范围，需要考虑线程安全
 * @author: Su
 * @create: 2020-09-17 20:33
 **/
public class SuSynchronized {
    public static void main(String[] args) throws InterruptedException {
        //将对临界区的互斥访问逻辑内置到对象中
        Room room=new Room();
        Thread t1=new Thread(()->{
            for (int i=0;i<500;i++){
                room.increment();
            }
        },"t1");
        Thread t2=new Thread(()->{
            for (int i=0;i<500;i++){
                room.decrement();
            }
        },"t0");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(room.getCount());
    }
    /**
     * synchronized的另一种写法：写在方法上
     *   public synchronized void test(){
     *     }
     *  相当于 public void test(){
     *         synchronized (this){
     *         }
     *     }
     *
     *  public synchronized static void test(){
     *
     *     }
     *     public static void test(){
     *         synchronized (Test.class){
     *
     *         }
     *     }
     */


}

class Room{
    private int count=0;

    public void increment(){
        synchronized (this){
            //临界区
            count++;
        }
    }

    public void decrement(){
        synchronized (this){
            //临界区
            count--;
        }
    }

    public int getCount(){
        //获取值也应该加锁，因为增和减都加锁了，如果获取值不加锁，可能会获取到中间值，而不是最终值,加锁后，相当于必须等增或减的执行操作结束了
        //才去获取值
        synchronized (this){
            return count;
        }
    }

}
