package com.concurrent.noLock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.*;

/**
 * @program: su-concurrent
 * @description: 原子类型
 *     原子整数：变量是基本类型
 *     原子引用：变量是引用类型 并且修改时是修改对象（即生成新的对象）
 *     ABA问题：
 *        balance.compareAndSet(prev,next)能成功的前提是prev的值和主存中共享变量的值一致，但是它是不能判断出这个共享变量是否被其它线程修改过，
 *        比如共享变量开始为A，则prev也为A 但在cas之前有线程1将共享变量改为了B，然后线程2又改成了A，然后当前线程执行cas时，发现是一致，则成功。
 *        这种问题在大部分业务场景中没什么影响，但是这有个隐患，主线程无法感知到其他线程对共享变量的修改
 *     使用AtomicStampedReference就可以使主线程感知到其他线程对共享变量的修改，一旦修改则cas失败
 *           只要有其他线程改过了共享变量，那么自己的cas就算失败，这时，仅比较值是不够的，需要再加一个版本号（谁修改了，就把版本号加1）
 *           AtomicStampedReference可以给原子引用加上版本号，追踪原子引用整个的变化过程，我们可以知道引用变量中途被更改了几次
 *     但是有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference 通过布尔标识是否修改过
 *     原子数组：变量是数组（当然也是引用类型），但是修改时并不是修改对象，而是修改对象中的值，即数组中的值，数组对象没变。保护的是数组中的元素
 *              AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray
 *     字段更新器：保护的是某个对象中的属性，能保证多个线程访问该对象的成员变量时该变量的线程安全
 *                AtomicReferenceFieldUpdater、AtomicIntegerFieldUpdater、AtomicLongFieldUpdater
 *     原子累加器：对一个整数做累加操作，jdk8以后新增了两个专门做累加的类，性能比AtomicInteger等原子整数类中的累加高很多
 *          性能提升的原因：就是在有竞争时，设置多个累加单元cell[n],如线程1在cell[1]上累加，线程2在cell[2]上累加....最后将结果汇总
 *          这样多个线程在累加时候操作的不同的cell变量，因此减少了cas重试失败，从而提高性能。
 *          cell的数量和cpu的核数有关
 *     Unsafe对象
 *          该对象提供了非常底层的，操作内存、线程的方法，Unsafe对象不能直接调用，只能通过反射获得
 *          cas、park/unpark 底层调用的都是Unsafe方法
 *
 *
 * @author: Su
 * @create: 2020-09-29 09:45
 **/
public class AtomicType {

}
/**
 * 原子引用类型AtomicReference
 */
class AccountCas2 implements Account2{
    private AtomicReference<BigDecimal> balance;
    public AccountCas2(BigDecimal balance) {
        this.balance = new AtomicReference<>(balance);
    }


    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public void withdraw(BigDecimal amount) {
        while (true){
           BigDecimal prev= balance.get();
           BigDecimal next=prev.subtract(amount);
           if(balance.compareAndSet(prev,next)){
               break;
           }
        }
    }
}

/**
 * 原子引用类型AtomicStampedReference
 */
class AccountCas3{
    static AtomicStampedReference ref=new AtomicStampedReference("A",0);
    public static void main(String[] args) {
        //获取值
        String prev=(String)ref.getReference();
        //获取版本号
        int stamp=ref.getStamp();
        //当前值  修改的新值  当前版本号 新版本号
        ref.compareAndSet(prev,"C",stamp,stamp+1);

    }
}

interface Account2 {
    // 获取余额
    BigDecimal getBalance();
    // 取款
    void withdraw(BigDecimal amount);
}
/**
 * 字段更新器
 */
class Cas4{
    public void test(){
        Student stu=new Student();
        //保护哪个类  哪个属性类型  哪个属性名
        AtomicReferenceFieldUpdater t=AtomicReferenceFieldUpdater.newUpdater(Student.class,String.class,"name");
        /**
         * 修改哪个对象  期待当前属性的值  修改的值
         *     当在此之前有其它线程将name改为了不是null 则下面修改失败 直接返回false
         */
        t.compareAndSet(stu,null,"张三");
    }
}
class Student{
    volatile String name;

}

/**
 * 原子累加器 LongAdder源码解析
 *     LongAdder类有几个关键域
 *     // 累加单元数组, 懒惰初始化
 *      transient volatile Cell[] cells;
 *     // 基础值, 如果没有竞争, 则用 cas 累加这个域
 *      transient volatile long base;
 *    // 在 cells 创建或扩容时, 置为 1, 表示加锁（之前说cas是无锁的实现，该处为什么又表示加锁，见cas锁解析）
 *      transient volatile int cellsBusy;
 *     //transient关键字表示序列化时这些变量不被序列化
 * 使用cellsBusy加锁机制保护cell数组创建和扩容时的线程安全（多线程下可能会有多个线程对cell数组进行扩容）
 *
 */
    /**
     *  cas锁  使用cas的机制实现锁
     *      我们自己不要写这样的代码，会有风险。
     *      jdk底层才有类似这样的代码
     *
     */
class LockCas{
    //该变量表示加锁状态 0：未加锁 1：加锁
    private AtomicInteger state = new AtomicInteger(0);
    //加锁方法
    public void lock() {
        while (true) {
            //从0变为1  也就是尝试将无锁状态改为有锁状态
            //第一个线程执行可以成功，但是第二个线程执行到此处时，当前值是1，与期待值0不一致，则返回false不断循环，也就是无法执行后续代码
            if (state.compareAndSet(0, 1)) {
                break;
            }
        }
    }
    //解锁  该方法不需要cas操作 因为只会是持有锁的线程执行该代码，其它线程执行不了，即单线程。直接将状态设置为0，代表无锁状态
    public void unlock() {
        state.set(0);
    }
}

