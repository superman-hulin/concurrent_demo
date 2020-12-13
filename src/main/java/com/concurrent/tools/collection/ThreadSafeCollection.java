package com.concurrent.tools.collection;

/**
 * @program: su-concurrent
 * @description: 线程安全集合类
 *      线程安全集合类可以分为三大类
 *          1. 遗留的安全集合（比较老的，都是直接在方法上加synchronized 并发性能比较低）
 *              Hashtable：线程安全的map实现
 *              Vector：线程安全的list实现
 *          2. 修饰的安全集合（使用Collections装饰的线程安全集合）
 *              Collections.synchronizedCollection
 *              Collections.synchronizedList
 *              Collections.synchronizedMap
 *              Collections.synchronizedSet
 *              Collections.synchronizedNavigableMap
 *              Collections.synchronizedNavigableSet
 *              Collections.synchronizedSortedMap
 *              Collections.synchronizedSortedSet
 *             这些方法的作用就是将原本是线程不安全的集合变成线程安全的
 *             例如
 *              public static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) {
 *                  return new SynchronizedMap<>(m);
 *              //是Collections的私有内部类 实现了map接口 每个方法只是调用了传入的m的方法 然后方法里加了synchronized
 *              //性能上并没有提高什么 但是这种装饰者模式值得学习
 *              }
 *         3. JUC安全集合（推荐使用）
 *            分为三类：
 *              Blocking类
 *                  大部分实现基于锁，并提供用来阻塞的方法
 *              CopyOnWrite类
 *                  CopyOnWrite 之类容器修改开销相对较重
 *              Concurrent类（推荐使用）
 *                  内部很多操作使用 cas 优化，一般可以提供较高吞吐量
 *                  缺点：弱一致性
 *                      遍历时弱一致性，例如，当利用迭代器遍历时，如果容器发生修改，迭代器仍然可以继续进行遍历，这时内容是旧的
 *                      （遍历时如果发生了修改，对于非安全容器来讲，使用 fail-fast 机制也就是让遍历立刻失败，抛出ConcurrentModificationException，不再继续遍历）
 *                      求大小弱一致性，size 操作未必是 100% 准确
 *                      读取弱一致性
 *
 *
 *
 * @author: Su
 * @create: 2020-10-13 09:18
 **/
public class ThreadSafeCollection {

}
