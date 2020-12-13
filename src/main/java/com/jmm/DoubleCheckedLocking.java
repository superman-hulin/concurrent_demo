package com.jmm;

/**
 * @program: su-concurrent
 * @description: double-checked locking问题
 * @author: Su
 * @create: 2020-09-28 10:46
 **/
public class DoubleCheckedLocking {

}
/**
 * 以著名的double-checked locking单例模式为例
 *
 */
final class Singleton{
    private Singleton(){}
    private static Singleton Instance=null;
    public static  Singleton getInstance() {
        synchronized (Singleton.class) {
            //考虑到多线程下对共享变量Instance的读写 则需要加锁实现互斥
            if (Instance == null) {
                Instance = new Singleton();
            }
            return Instance;
        }
    }
    /**
     * 上述实例化是懒汉式(等到用到时才创建)，但是会有性能问题 每次getInstance，都会执行同步代码块。而其实只需要第一次执行同步块进行线程安全保护，后面的无须再进行线程安全保护。能否实现
     * 使用double-checked locking来实现
     *      实现的特点：
     *          懒惰实例化
     *          首次使用 getInstance()才使用synchronized加锁 后续使用时无需加锁
     *          有隐含的问题 但很关键的一点：第一个if使用Instance变量，是在同步块之外 则无法受到保护
     * 问题所在：
     *      synchronized中的代码块可能会发生指令重排，Instance = new Singleton();假设该代码会产生指令重排，先执行Instance赋值 然后再执行构造方法
     *      此时另一线程进入第一个if(Instance==null) ，则会判断不为空，则直接返回还没有执行完构造方法的对象。
     *·解决：
     *    对Instance变量增加volatile关键字 保证不会发生指令重排
     *    private static volatile Singleton Instance=null;
     */
    public static  Singleton getInstance1() {
        //首次访问会同步，而之后的使用没有进入synchronized
        if(Instance==null) {
            synchronized (Singleton.class) {
                //考虑到多线程下对共享变量Instance的读写 则需要加锁实现互斥
                if (Instance == null) {
                    Instance = new Singleton();
                }
            }
        }
        return Instance;
    }



}
