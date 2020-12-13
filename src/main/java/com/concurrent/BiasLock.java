package com.concurrent;

import java.util.Vector;

/**
 * @program: su-concurrent
 * @description: 偏向锁
 *  相关概念：
 *      轻量级锁在没有竞争时，每次重入仍然需要执行cas操作
 *      java6中引入了偏向锁来做进一步优化：只有第一次使用cas将线程ID设置到对象的Mark word头，之后发现这个线程ID是自己的就表示没有竞争，不用重新cas，
 *      以后只要不发生竞争，这个对象就归该线程所有。
 *  加偏向锁的流程：
 *      针对下面代码中的锁重入现象，使用偏向锁来优化
 *      m1调用synchronized (obj)时，用线程Id替换加锁对象中的markword
 *      m2调用时，则不用cas操作了，只需要拿到加锁对象中的markword，检查线程id是否是自己的
 *      m3调用时，同理。
 *
 *  一个对象创建时
 *      如果开启了偏向锁（默认开启）,那么对象创建后，markword值最后3位为101代表开启了，否则001代表未开启。
 *      偏向锁是默认是延迟的，不会在程序启动时立即生效，如果想避免延迟，可以加vm参数来禁用延迟
 *      也可以使用vm参数来禁用偏向锁（适用于竞争场景）
 *      如果没有开启偏向锁，那么对象创建后，markword值的最后3位为001，这时它的hashcode、age都为0，第一次用到hashcode时才会赋值
 *      注意：开启了偏向锁只是代表可以偏向，并不是说创建了就加了偏向锁。还是需要synchronized进行加偏向锁。
 *      当一个对象开启了偏向锁，使用synchronized给该对象加锁时，会优先选择加偏向锁
 *  偏向锁的禁用方式：
 *      1.使用vm参数来禁用偏向锁
 *      2.在加锁对象加锁之前，调用对象的hashCode可以禁用偏向锁。因为偏向锁情形下，并且加锁前调用了该对象的hashCode方法，对象头中存储不下hashCode，
 *      则为该对象加锁时，优先就是用轻量级锁。
 *        而为什么调用对象的hashCode时没有禁用轻量级锁和重量级锁，因为这两种锁的情形下，对象的hashCode由于cas操作被换到了锁记录或者Monitor对象中存储。
 *      3.当有其它线程使用偏向锁对象时，会将偏向锁升级为轻量级锁。
 *      4. 调用wait/notify  因为只有重量级锁才有这两个方法
 *  注意：偏向锁和轻量级锁都是在没有竞争的情形下的，也就是多个线程访问的时间是错开的
 *
 *  批量重偏向
 *      如果想要加锁的对象虽然被多个线程访问，但没有竞争，这时偏向了线程T1的对象仍有机会重新偏向T2，重偏向会重置对象的ThreadID
 *      当撤销偏向锁阈值超过20次后，jvm会这样觉得，我是不是偏向错了呢，于是会在给这些对象加锁时重新偏向至加锁线程（见下面代码分析）
 *
 *  批量撤销
 *      当撤销偏向锁阈值超过40次后，jvm会这样觉得，自己确实偏向错了，根本不应该偏向。于是整个类的所有对象都会变为不可偏向的，新建的对象也是不可偏向的
 *      即：t1线程对40个狗对象加锁，都是加的偏向锁，偏向t1
 *         等t1线程都加完后，t2取出这40个狗对象，再进行加锁操作，则前20次时，都是撤销偏向锁，升级为轻量级锁
 *         但是考虑到撤销也消耗性能，则第20次之后采用批量重偏向进行优化，即不再撤销偏向锁，而是直接将线程id换成t2
 *         等t2线程都加完后，t3线程取出这40个狗对象进行加锁操作，则前20次都是给对象加轻量级锁，后20次都是撤销偏向锁，,升级为轻量级锁,
 *         这样累计做了40次撤销后，jvm就会将该类的所有对象（即使重新创建的）都设置为不可偏向。
 *  锁消除
 *
 *
 * @author: Su
 * @create: 2020-09-21 19:55
 **/
public class BiasLock {
    static final Object obj=new Object();
    public static void method1(){
        synchronized (obj){
            //同步块A
            method2();

        }
    }
    public static void method2(){
        synchronized (obj){
            //同步块B
        }
    }
    public static void method3(){
        synchronized (obj){
            //同步块C
        }
    }

    /**
     * 批量重偏向
      */
    private static void test3(){
        Vector<Dog> list=new Vector<>();
        Thread t1=new Thread(()->{
            for(int i=0;i<30;i++){
                Dog dog=new Dog();
                list.add(dog);
                /**
                 * 由于当前无竞争，则优先加偏向锁，循环30次给30个狗对象都加偏向锁
                 */
                synchronized (dog){
                    //打印该对象
                    //最后三位是101，代表偏向锁 前面的位是线程1的id
                }
            }
            //使用这种方式将线程1和线程2的时间错开，相当于无竞争
            //上面给30个狗对象加偏向锁之后，唤醒线程t2对这30个狗对象重新加锁
            synchronized (list){
                list.notify();
            }
        },"t1");
        t1.start();

        Thread t2=new Thread(()->{
            synchronized (list){
                try {
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for(int i=0;i<30;i++){
                //循环到第20次之后，不会再撤销dog对象的偏向锁了，只是将线程id换成线程t2的，即实现了批量重偏向
                Dog dog=list.get(i);
                //打印对象
                //最后三位是101，代表偏向锁 前面的位是线程1的id
                synchronized (dog){
                    //打印该对象
                    //前19次都是：将dog对象的偏向锁撤销，升级为轻量级锁，即状态位为00
                    //当第20次时，最后三位是101，代表偏向锁，前面的位是线程2的id
                }
                //打印对象
                //前19次都是：对轻量级锁进行解锁。最后三位状态位为000，代表不可偏向
                //当第20次时:对偏向锁进行解锁。最后三位是101，代表偏向锁，前面的位是线程2的id
            }

        },"t2");
        t2.start();
    }
}
class Dog{

}
