package com.concurrent;

import java.util.ArrayList;

/**
 * @program: su-concurrent
 * @description: 线程安全
 * 变量的线程安全分析
 *     成员变量和静态变量是否线程安全
 *       1.如果它们没有共享，则线程安全
 *       2.如果它们被共享了，根据它们的状态是否能够改变，又分两种情况
 *           如果只有读操作，则线程安全
 *           如果有读写操作，则这段代码是临界区，需要考虑线程安全
 *    局部变量是否线程安全
 *      局部变量是线程安全的
 *      但局部变量引用的对象则未必
 *         如果该对象没有逃离方法的作用访问，它是线程安全的
 *         如果该对象逃离方法的作用范围，需要考虑线程安全
 * @author: Su
 * @create: 2020-09-18 09:41
 **/
public class ThreadSafe {
    public static void main(String[] args) {
        ThreadSafe1 threadSafe1=new ThreadSafe1();
        for (int i=0;i<10;i++){
           new Thread(()->{
               threadSafe1.method1(20);
           },"Thread"+(i+1)).start();
        }
    }
}

class ThreadSafe1{
    /**
     * 由于list是私有变量，当多个线程调用该方法时，线程中的栈桢中都会各自创建自己的list变量，不会存在共享问题，所以是线程安全的
     * @param loopNumber
     */
    public final void method1(int loopNumber){
        ArrayList<String> list=new ArrayList<>();
        for (int i=0;i<loopNumber;i++){
            method2(list);
            method3(list);
        }
    }
    private void method2(ArrayList<String> list){
        list.add("1");
    }
    private void method3(ArrayList<String> list){
        list.remove(0);
    }
    /**
     * 如果把method2和method3改为public，则其它线程可以访问到method2和method3，但是依然是线程安全的
     * 因为其它线程调用method2和method3时，传的是其它的list引用，并没有共享list
     */
}

class ThreadSafe2{
    /**
     * 由于list是私有变量，当多个线程调用该方法时，线程中的栈桢中都会各自创建自己的list变量，不会存在共享问题，所以是线程安全的
     * @param loopNumber
     */
    public final void method1(int loopNumber){
        ArrayList<String> list=new ArrayList<>();
        for (int i=0;i<loopNumber;i++){
            method2(list);
            method3(list);
        }
    }
    public void method2(ArrayList<String> list){
        list.add("1");
    }
    public void method3(ArrayList<String> list){
        list.remove(0);
    }
}

/**
 * 局部变量中的线程不安全情况  通过继承上述线程安全类，子类覆盖method2或method3方法（由于该方法是public且没加final）
 *      由于子类方法中新加了线程访问list，则出现了多个线程访问局部变量引用对象，则有线程安全问题
 */
class ThreadSafeSubClass extends ThreadSafe2{
    @Override
    public void method3(ArrayList<String> list) {
        new Thread(()->{
            list.remove(0);
        }).start();
    }

    /**
     * 因此方法的访问修饰符是有意义的，如果改为private，则子类是无法访问到该方法，另外在public方法中加final防止子类修改父类行为
     */
}

/**
 * 常见线程安全类
 *  String Integer StringBuffer Random Vector Hashtable java.util.concurrent包下的类
 *  这里说它们是线程安全的是指，多个线程调用它们同一个实例的某个方法时，是线程安全的。也可以理解为
 *      它们的每个方法是原子的
 *      但注意它们多个方法的组合不是原子的。
 *         比如：Hashtable table=new Hashtable();
 *              if(table.get("key")==null){
 *                  table.put("key",value);
 *              }
 *         table中的get和put方法都是线程安全的 但现在组合一起使用时，一样会有线程安全问题。比如线程一开始get，线程二也get，然后线程二就开始put
 *         此时线程一又put 把之前的值覆盖了。则put两次了，所以有线程安全问题。
 *
 *  不可变类线程安全性
 *     String、Integer等都是不可变类，因为其内部的状态不可以改变，所以它们的方法都是线程安全的。
 */


