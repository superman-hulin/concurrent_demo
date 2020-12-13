package com.concurrent;

/**
 * @program: su-concurrent
 * @description: 轻量级锁
 *      应用场景：如果一个对象虽然有多线程访问，但多线程访问的时间是错开的（也就是没有竞争），那么可以使用轻量级锁来优化
 *      轻量级锁对使用者是透明的，即语法仍然是synchronized 也就是说调用synchronized时优先使用轻量级锁方式，如果失败了才使用重量级锁加锁
 *      原理：
 *          obj对象包含对象体（成员变量信息）和对象头（Mask word(存储的hash码和加锁状态位)和Klass Word（对应的类对象））
 *          假设线程A执行到method1中的synchronized时，会产生一个method1的栈桢，该栈桢中产生一个锁记录对象（是jvm层面的），对象中包含对象指针（加锁对象的地址）和
 *          锁记录地址和状态 两部分，然后对象指针指向加锁的对象，同时将加锁对象中的mask word和锁记录对象中的锁记录地址和状态进行交换，交换就是为了表示加锁。
 *          交换之前，加锁对象中mask word的状态位是01表示未加锁状态，而锁记录中的状态位是00，表示轻量级锁。交换如果成功了，则表示加锁成功
 *          交换之后，加锁对象的mask word中就是锁记录的地址（这样就知道是被哪个线程锁住了）和状态位00，而锁记录中就是加锁对象的hash码和状态01
 *          当解锁时。则再恢复回去
 *          上述交换称为cas替换，为原子操作，不会被打断
 *          什么时候cas成功
 *              主要看加锁对象的状态位，如果是01 表示无锁状态，则轻量级锁是可以加成功的，但如果加锁对象的状态位是00，则已经加了轻量级锁，则再加就失败了
 *          什么时候cas失败
 *              1.当加锁对象的状态位不是01时，则表示该对象已经被其它线程锁了，则会失败
 *                即如果是其它线程已经持有了该object的轻量级锁，表明有竞争，进入锁膨胀过程
 *              2.像下面代码那样 线程A首先调用method1中的synchronized进行加锁，然后再调用method2时，又产生一个栈桢，对同一个加锁对象又加一次锁，即
 *               又产生一个新的锁记录对象，同理对象指针指向加锁对象，也会进行cas交换，但是此时加锁对象的状态位是00，则交换失败，但这种失败没关系，因为可以看到加锁对象
 *               中的锁地址就是当前线程中加的，则加锁也会成功，只不过地址那块存放null。增加这种新的锁记录对象就是为了记录加了几次锁
 *               即如果是自己执行了锁重入，那么再添加一条锁记录作为重入的计数
 *
 *          当退出synchronized代码块（解锁时）,如果有取值为null的锁记录，表示有重入，这时重置锁记录，表示重入计数减1
 *          当退出synchronized代码块（解锁时）锁记录的值不为null时，使用cas将Mask word的值恢复给加锁对象的对象头
 *              当成功时，则解锁成功
 *              失败时，说明轻量级锁进行了锁膨胀或已经升级为重量级锁，则进入重量级锁解锁流程
 *
 *     锁膨胀
 *        线程1如果在尝试加轻量级锁的过程中，cas操作无法成功，这时一种情况就是有其它线程（假设线程0）为此对象加上了轻量级锁（有竞争）,这时需要进行锁膨胀，将轻量级锁变为重量级锁
 *        因为此时已经有其它线程加了轻量级锁，那么当前线程就应该进入阻塞状态等待，但是轻量级锁没有阻塞的说法，只有重量级锁才有阻塞，那么这时就应该为obj申请重量级锁
 *     锁膨胀流程
 *         先为加锁对象obj申请一个Monitor锁，让obj中的mask word不再存储原先的锁记录地址，而是指向重量级锁地址，并且状态位变为10，Owner指向线程0的锁记录地址
 *         然后自己进入Monitor的EntryList中进入阻塞状态
 *     此时当线程0退出同步块解锁时，使用cas将Mask word的值恢复给加锁对象的对象头，则失败。这时会进入重量级解锁流程，即按照MOnitor地址找到Monitor对象，设置Owner为null。
 *     唤醒EntryList中的阻塞线程。
 *
 * @author: Su
 * @create: 2020-09-20 21:22
 **/
public class LightWeightLock {
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

}
