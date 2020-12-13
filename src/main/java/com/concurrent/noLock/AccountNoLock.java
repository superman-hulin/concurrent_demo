package com.concurrent.noLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: su-concurrent
 * @description: 无锁解决账户并发问题
 *      之前是共享模型之管程（即悲观锁，阻塞），现在是共享模型之无锁（即乐观锁，非阻塞）
 * @author: Su
 * @create: 2020-09-28 20:35
 **/
public class AccountNoLock {
    public static void main(String[] args) {
        Account.demo(new AccountUnsafe(10000));
    }

}
/**
 * 无锁解决
 *     使用AtomicInteger解决，内部并没有用锁来保护共享变量的线程安全。实现原理：
 *        其中的关键是compareAndSet（简称CAS），它必须是原子操作
 *        CAS底层是lock cmpxchg指令（X86架构），在单核CPU和多核CPU下都能够保证cas的原子性
 *        AtomicInteger是将数字的值放在value变量中 private volatile int value; 该变量使用了volatile修饰，CAS是需要依赖volatile的
 *        获取共享变量时，为了保证该变量的可见性，需要使用volatile修饰，它可以用来修饰成员变量和静态成员变量，可以避免线程从自己的工作缓存中查找变量的值
 *        必须到主存中获取它的值，线程操作volatile变量都是直接操作主存。即一个线程对volatile变量的修改，对另一个线程可见
 *        CAS必须借助volatile才能读取到共享变量的最新值来实现【比较并交换】的效果
 *    无锁和synchronized效率比较
 *      无锁情况下，即使重试失败，线程始终在高速运行，没有停歇，而synchronized会让线程在没有获得锁的时候发生上下文切换，进入阻塞
 *      但是当线程数大于cpu核数时，无锁时也会发生上下文切换，那么效率也会降下来
 *    CAS的特点
 *      结合CAS和volatile可以实现无锁并发，适用于线程数少、多核CPU的场景下
 *      CAS是基于乐观锁的思想：最乐观的估计，不怕别的线程来修改共享变量，就算改了也没关系，我吃亏点再重试呗
 *      synchronized是基于悲观锁的思想：最悲观的估计，得防着其它线程来修改共享变量，我上了锁你们都别想改，我改完了解开锁，你们才有机会
 *      CAS体现的是无锁并发、无阻塞并发
 *          因为没有使用synchronized，所以线程不会陷入阻塞，这是效率提升的因素之一
 *          但如果竞争激烈，可以想到重试必然频繁发生，反而效率会受影响
 *
 */
class AccountCas implements Account{
    //共享变量余额就不能用一个普通的整数类来代表，需要使用jdk提供的无锁实现 即原子整数
    private AtomicInteger balance;

    public AccountCas(int balance) {
        this.balance = new AtomicInteger(balance);
    }

    @Override
    public Integer getBalance() {
        return balance.get();
    }

    @Override
    public void withdraw(Integer amount) {
        while (true) {
            //获取余额的最新值
            int prev = balance.get();
            //要修改的余额
            int next = prev - amount;
            /**
             * 需要将prev和next这两个局部变量（存储在线程的工作内存中）同步到主存中
             * 这行代码是真正把next同步到主存中 实现真正的修改
             * 当修改成功 则退出循环
             * 当修改失败 则再次循环尝试修改
             * compareAndSet（原子性的 但并不是通过加锁实现）：比较并设置值
             *      比较：当前线程拿到的prev值和共享变量中的余额进行比较，如果不同，则修改失败 说明已经有其它线程把余额做了修改，那么当前线程获取的最新余额并不是最新。
             *           则再次循环，取最新值
             *      设置：只要比较成功 则将next设置上去
             */
            if (balance.compareAndSet(prev, next)) {
                break;
            }
        }
        //上述while循环可以换成如下代码
       // balance.getAndAdd(-1*amount);
    }
}

/**
 * 由于多线程对共享变量balance进行读写，会有线程安全问题
 */
class AccountUnsafe implements Account {
    private Integer balance;

    public AccountUnsafe(Integer balance) {
        this.balance = balance;
    }

    @Override
    public Integer getBalance() {
            return balance;
    }
    //加锁解决 synchronized (this) {return balance;}}

    @Override
    public void withdraw(Integer amount) {
        balance -= amount;
    }
    //加锁解决 synchronized (this) { balance -= amount;}}
}

interface Account {
    // 获取余额
    Integer getBalance();
    // 取款
    void withdraw(Integer amount);
    /**
     * 方法内会启动 1000 个线程，每个线程做 -10 元 的操作
     * 如果初始余额为 10000 那么正确的结果应当是 0
     */
    static void demo(Account account) {
        List<Thread> ts = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            ts.add(new Thread(() -> {
                account.withdraw(10);
            }));
        }
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println(account.getBalance()
                + " cost: " + (end-start)/1000_000 + " ms");
    }
}
