package com.pattern.synchronize;

/**
 * @program: su-concurrent
 * @description: 同步模式之保护性暂停
 *   定义：
 *      即Guarded Suspension，用在一个线程等待另一个线程的执行结果
 *      要点：
 *          有一个结果需要从一个线程传递到另一个线程，让他们关联同一个GuardedObject，即产生结果和消费结果的线程一一对应
 *          如果有结果不断从一个线程到另一个线程那么可以使用消息队列
 *          JDK中，join的实现、Future的实现，采用的就是此模式
 *          因为要等待另一方的结果，因此归类到同步模式
 *   优点：
 *      这种设计模式，只需要GuardedObject对象中有值了，生产该值的线程可以继续做其它的事
 *      另外需要等待的结果是在线程中的局部变量，不用全局变量
 *   而join的话有两个缺点
 *      必须要等另一个线程执行结束
 *      结果需要设置为全局变量
 *
 *   join原理
 *      join底层应用的就是同步模式之保护性暂停模式，但是保护性暂停是一个线程等待另一个线程的结果，而join是一个线程等待另一个线程的结束
 *      只需要将while中的条件判断换成判断线程是否结束，而不是结果是否有值
 *
 *   扩展：
 *      如果需要在多个类之间使用GuardedObject对象，作为参数传递不是很方便，因此设计一个用来解耦的中间类，这样不仅能够解耦结果等待者和结果生产者，
 *      还能够同时支持多个任务的管理
 *      这个中间类Futures就好比居民楼一层的信箱（每个信箱有房间编号），结果等待的线程去相应的信箱中取结果
 * @author: Su
 * @create: 2020-09-22 14:50
 **/
public class ProtectivePause {

    public static void main(String[] args) {
        GuardedObject guardedObject=new GuardedObject();
        //线程1 等待线程2 的下载结果
        new Thread(()->{
            //等待结果
            guardedObject.get();
        },"t1").start();

        new Thread(()->{
            //执行下载
            System.out.println("执行下载");
            //将下载结果送入GuardedObject中的成员变量
            guardedObject.complete(new Object());
        },"t2").start();

    }


}

class GuardedObject{
    //结果
    private Object response;
    //永久等待，获取结果
    public Object get(){
        synchronized (this){
            //没有结果时就等待
            while (response==null) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }

    //最多等多久进行获取结果
    public Object get(long timeout){
        synchronized (this){
            //记录开始等待的时间
            long begin=System.currentTimeMillis();
            //经历的时间
            long passedTime=0;
            while (response==null) {
                //经历的时间超过了最大等待时间 就不用等了
                if(passedTime>=timeout){
                    break;
                }
                try {
                    this.wait(timeout-passedTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                passedTime=System.currentTimeMillis()-begin;
            }
            return response;
        }
    }


    public void complete(Object response){
        synchronized (this){
            this.response=response;
            this.notifyAll();
        }
    }
}





