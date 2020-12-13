package com.jmm;

/**
 * @program: su-concurrent
 * @description: java内存模型
 *      Monitor主要关注的是访问共享变量时，保证临界区代码的原子性
 *      java内存模型是学习共享变量在多线程间的【可见性】问题与多条指令执行时的【有序性】问题
 *      java内存模型
 *          JMM即java Memory Model 它定义了主存、工作内存抽象概念，底层对应着CPU寄存器、缓存、硬件内存和CPU指令优化等
 *          JMM体现在以下几个方面
 *              原子性：保证指令不会受到线程上下文切换的影响
 *              可见性：保证指令不会受cpu缓存的影响
 *              有序性：保证指令不会受cpu指令并行优化影响
 *      volatile原理
 *          volatile的底层实现原理是内存屏障（Memory Barrier）
 *          对volatile变量的写指令后会加入写屏障
 *          对volatile变量的读指令前会加入读屏障
 *          如何保证可见性
 *             写屏障保证在该屏障之前的，对共享变量的改动，都同步到主存当中
 *             public void actor2(I_Result r) {
 *                    num = 2;
 *                    ready = true; ready是volatile赋值带写屏障
 *                    //写屏障  该屏障之前的所有共享变量的改动都同步到主存中 即使没有加volatile的num变量，也会被同步到
 *                   }
 *             读屏障保证在该屏障之后，对共享变量的读取，加载的是主存中最新数据
 *             public void actor1(I_Result r) {
 *                    读屏障
 *                    ready是volatile 读取值带读屏障
 *                    if(ready) {
 *                        r.r1 = num + num;
 *                    } else {
 *                        r.r1 = 1;
 *                            }
 *                    }
 *          如何保证有序性
 *              写屏障会确保指令重排时，不会将写屏障之前的代码排在写屏障之后
 *              读屏障会确保指令重排时，不会将读屏障之后的代码排在读屏障之前
 *     synchronized可以保证原子性、可见性和有序性，但是不能阻止同步代码块中的指令重排，但是synchronized使该代码块成为只会有单线程访问，
 *     所以synchronized中的同步代码块是不会由于有序性而出现问题。
 * @author: Su
 * @create: 2020-09-25 16:44
 **/
public class SuJmm {
    /**
     * 可见性  指一个线程对主内存中的变量值进行了修改 导致另一个线程对该值不可见
     *    问题分析：1.初始状态，t线程刚开始从主内存（所有共享信息存储的地方）读取了run的值到工作内存（每个线程私有的信息）
     *            2.因为t线程要频繁从主内存中读取run的值，JIT编译器会将run的值缓存至自己工作内存中的高速缓存中，减少对主存中run的访问 提高效率
     *            3.1秒后 main线程修改了run的值，并同步至主存，而t是从自己工作内存中的高速缓存中读取这个变量的值，结果永远是旧值
     *    解决方案：
     *         第一种：对该变量加volatile关键字 代表线程就不会从自己工作内存的缓存中读取，而是每次都要到主内存中获取最新值
     *         第二种：使用synchronized来实现可见性
     *         区别：synchronized需要使用重量级锁 而volatile更轻量
     *    volatile（易变关键字）
     *      它可以用来修饰成员变量和静态成员变量，他可以避免线程从自己的工作缓存中查找变量的值，必须到主存中获取它的值，线程操作volatile变量都是直接操作主存
     *    可见性 vs 原子性
     *          下面例子体现的实际就是可见性，它保证的是多个线程之间，一个线程对volatile变量的修改对另一个线程可见，不能保证原子性，仅用在一个写线程 多个读线程的情况
     *          比较一下之前线程安全的例子：两个线程 一个i++，一个i--,volatile只能保证看到最新值 不能解决指令交错
     *          注意：
     *              synchronized语句块既可以保证代码块的原子性，也同时保证代码块内变量的可见性。但缺点是它属性重量级操作 性能相对较低
     *              如果示例的死循环中加入System.out.println()会发现即使不加volatile修饰符，线程t也能正确看到对run变量的修改了，从而实现可见性
     */
    //示例代码
    static boolean run=true;
    public static void main(String[] args) {
        Thread t=new Thread(()->{
            while (run){
                //
            }
        });
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("停止t");
        run=false; //即使主线程修改了该标识 但是线程t不会如预想的停下来
        /**
         * 使用volatile解决
         *   volatile static boolean run=true;
         *  使用synchronized解决
         *      final static Object lock=new Object(); 加锁对象
         *
         *      while(true){
         *       synchronized(lock){
         *          if(!run){
         *          break;
         *          }
         *         }
         *        }
         *
         *        synchronized(lock){
         *          run=false;
         *          }
         */
    }

    /**
     * 有序性
     *      JVM会在不影响正确性的前提下，可以调整语句的执行顺序，如下面代码
     *          static int i;
     *          static int j;
     *          在某个线程内执行如下赋值操作
     *              i=..  j=..
     *          可以看到 先执行i还是先执行j，对最终结果不会产生影响。所以赋值语句真正执行时，既可以是i=.. j=.. ，也可以是j=.. i=..
     *      这种特性称之为指令重排 多线程下指令重排会影响正确性。为什么要有重排指令这项优化 需要从CPU执行指令的原理来理解
     *      CPU将指令还可以分为更小的阶段，分为取指令、指令译码、执行指令、内存访问和数据写回这五个阶段，在不改变程序结果的前提下，这些指令的各个阶段
     *      可以通过重排序和组合来实现指令级并行。指令重排的前提是重排指令不能影响结果，例如
     *          可以重排的例子
     *          int a=10
     *          int b=20
     *          sout(a+b)
     *          不可以重排的例子
     *          int a=10
     *          int b=a-5
     *      示例代码：
     *          int num = 0;
     *          boolean ready = false;
     *          // 线程1 执行此方法
     *          public void actor1(I_Result r) {
     *              if(ready) {
     *                  r.r1 = num + num;
     *              } else {
     *                  r.r1 = 1;
     *                      }
     *              }
     *          // 线程2 执行此方法
     *          public void actor2(I_Result r) {
     *              num = 2;
     *              ready = true;
     *              }
     *         r.r1会有三种结果 1,4,0（0是由于actor2中可能会发生指令重排 即先执行ready = true）
     *     则需要禁止这种指令重排
     *         volatile boolean ready = false; 加在ready上 防止ready = true之前的所有代码指令重排
     *
     */






}
