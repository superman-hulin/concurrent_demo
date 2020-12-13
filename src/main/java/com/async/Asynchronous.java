package com.async;

import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @program: su-concurrent
 * @description: 异步 不等待
 *  public static void main(String[] args) {
 *         new Thread(()->读取文件);
 *         System.out.println("as");
 *     }
 *   使用额外线程来读取文件  这样主线程main执行的时候不用等待线程读取完文件 而是直接执行后面打印
 *   多线程可以让方法执行变为异步的
 * 单核cpu和多核cpu中的程序运行效率
 *     单核下 多线程不能实际提高程序运行效率，但是能够在不同的任务之间切换，不同线程轮流使用cpu，不至于一个线程总占用cpu
 *     多核下 可以并行跑多个线程 但能否提高程序运行效率还是要分情况的（有些任务可以进行拆分并行执行 有些则不能）
 * IO操作不占用cpu 只是我们一般使用阻塞型io来拷贝文件 这是相当于线程虽然不用cpu，但需要一直等待io结束，没能充分利用线程 所以才有非阻塞io和异步io
 * @author: Su
 * @create: 2020-09-08 19:27
 **/
public class Asynchronous {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        /**
         *  创建和使用线程 推荐方式二
         */
        //创建线程对象方式一    匿名内部类的方式创建一个Thread子类对象并重写run方法
        Thread t=new Thread(){
            public void run(){
                //要执行的任务

            }
        };
        //指定线程名称
        t.setName("t1");

        //启动线程（注意该方法只是让线程进入就绪状态，不一定立即执行，每个线程对象的该方法只能调用一次）
        t.start();

        //创建线程对象方式二  使用Runnable(对应线程要执行的代码)配置Thread(代表线程) 将线程和任务分开
        Runnable runnable=new Runnable() {
            public void run() {
                //要执行的任务
            }
        };
        //创建线程对象
        Thread tt=new Thread(runnable,"T2");
        //启动线程
        tt.start();
        /**
         *个接口中只有一个抽象方法 就可以使用lambda表达式
         * Runnable runnable=()->{
         *                 //要执行的任务
         *             };
         */

        //创建线程对象方式三  使用FutureTask配合Thread  FutureTask能够接收Callable类型的参数，用来处理有返回结果的情况 泛型为结果的类型
        FutureTask<Integer> t3=new FutureTask<Integer>(()->{
                 return 100;
        });
        //创建线程并开启
        new Thread(t3,"t3").start();
        //主线程阻塞，同步等待task执行完毕的结果
        Integer result=t3.get();

        /**
         * 线程运行的原理
         *   栈与栈桢
         *      JVM中由堆、栈和方法区组成，其中栈内存是给线程用的，每个线程启动后 虚拟机就会为其分配一块栈内存
         *      每个栈由多个栈桢组成 对应着每次方法调用时所占的内存
         *      每个线程只能有一个活动栈桢 对应着当前正在执行的那个方法
         *   例子1(只有一个主线程时)：main方法调用m1方法 m1方法调用m2  执行main函数时，会创建一个主线程并分配栈空间，主线程会为主方法main分配一个栈桢，main调用m1时又会分配一个栈桢，m1调用m2时又分配一个栈桢
         *        m2执行完毕后 对应的栈桢释放 m1执行完毕后也释放。
         *   栈内存是使用完自动释放 堆需要垃圾回收机制
         *
         *   例子2（多线程时）：
         *   public static void main(String[] args){
         *      Thread t1=new Thread(){
         *          public void run(){
         *              method1(20);
         *          }
         *        };
         *       t1.setName("t1");
         *       t1.start();
         *       method1(10);
         *      }
         *   private static void method1(int x){
         *         int y=x+1;
         *         Object m=method2();
         *     }
         *   private static Object method2(){
         *         Object n=new Object();
         *         return n;
         *     }
         *   为主方法分配主线程并且分配栈空间，子线程t1也会被分配栈空间，然后主方法调用method1方法，则分配主线程所在栈的栈桢，与此同时线程t1调用method1，
         *   也会分配子线程所在栈的栈桢，主线程和子线程互不影响。
         *
         *   线程上下文切换
         *      从使用cpu到不使用cpu 就会发生线程上下文切换
         *      产生的原因：
         *          线程的cpu时间片用完
         *          垃圾回收  （暂停当前所有的工作线程，让垃圾回收的线程开始回收垃圾）
         *          有更高优先级的线程需要运行
         *          线程自己调用了sleep、yield、wait等方法
         *       当线程上下文发生切换时，需要由操作系统保存当前线程的状态（比如记忆当前线程执行到哪行代码等，由程序计数器来实现），然后再执行上下文切换。
         *       程序计数器（线程私有的）的作用是记住下一条jvm指令的执行地址（也就是当前线程如果再被执行的时候，该执行哪行代码）
         *       状态包括程序计数器、虚拟机栈中每个栈桢的信息，如局部变量、操作数栈、返回地址等。
         *       线程上下文频繁发生会影响性能
         */





    }

}
