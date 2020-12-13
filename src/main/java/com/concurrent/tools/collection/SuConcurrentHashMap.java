package com.concurrent.tools.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @program: su-concurrent
 * @description:
 *      ConcurrentHashMap原理
 *          haspMap的并发死链：在在 JDK 7 才会发生，扩容机制是后链入的节点链入表头 jdk 8后对扩容算法做了调整 不再将元素加入链表头 则不会发生并发死链
 *                           但是仍不意味着能够在多线程环境下能够安全扩容，还会出现其它问题（如扩容丢数据）
 *                           究其死链的原因，是因为在多线程环境下使用了非线程安全的 map 集合
 *                           hashMap初始容量是16 当元素超过容量的3/4会发生扩容 容量扩大2倍
 * @author: Su
 * @create: 2020-10-13 09:45
 **/
public class SuConcurrentHashMap {
}


/**
 * 经典的练习：单词计数
 *   数据：将26个字母每个字母200次存入list 然后打乱list 将字母放入文件 则有26个文件 每个文件200个字母
 *   需求：统计这26个文件中每个字母出现的总次数
 *
 */
class WordCount{
    public static void main(String[] args) {
        demo(
                // 创建 map 集合
                () -> new HashMap<String, Integer>(),
                /**
                 * 由于多个线程共享一个map变量 并且HashMap为线程不安全的 则计数会错误
                 * 那将HashMap改为ConcurrentHashMap 对不对？
                 *      依然不对 因为下面计数操作中get和put是原子安全的 但是组合起来并不是安全的
                 * 解决方案
                 *  1. 对下面计数操作加锁
                 *      synchronized (map){
                 *          Integer counter = map.get(word);
                 *          int newValue = counter == null ? 1 : counter + 1;
                 *          map.put(word, newValue);
                 *      }
                 *     这样可以保证是对的 但是并不好 因为ConcurrentHashMap的好处就是使用的细粒度锁 不是将锁加在整个map上 然后现在相当于退回去了 又加到整个map上了
                 *     则并发度就不高了
                 *  2. 使用ConcurrentHashMap中的computeIfAbsent方法
                 *      demo(
                 *          () -> new ConcurrentHashMap<String, LongAdder>(),
                 *          (map, words) -> {
                 *              for (String word : words) {
                 *              // 如果缺少一个key  则计算生成一个value 然后将key value放入map
                 *              map.computeIfAbsent(word, (key) -> new LongAdder()).increment();
                 *                  }
                 *                 }
                 *          );
                 */
                // 进行计数
                (map, words) -> {
                    for (String word : words) {
                        Integer counter = map.get(word);
                        int newValue = counter == null ? 1 : counter + 1;
                        map.put(word, newValue);
                    }
                }
        );
    }

    /**
     * 模板代码
     * @param supplier 提供集合
     * @param consumer 操作计算
     * @param <V>
     */
    private static <V> void demo(Supplier<Map<String,V>> supplier,
                                 BiConsumer<Map<String,V>,List<String>> consumer) {
        Map<String, V> counterMap = supplier.get();
        List<Thread> ts = new ArrayList<>();
        //循环创建26个线程 每个线程对应一个文件 将统计结果放入共享的map
        for (int i = 1; i <= 26; i++) {
            int idx = i;
            Thread thread = new Thread(() -> {
                //读相应的文件
                List<String> words = readFromFile(idx);
                consumer.accept(counterMap, words);
            });
            ts.add(thread);
        }
        ts.forEach(t->t.start());
        ts.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(counterMap);
    }
    public static List<String> readFromFile(int i) {
        ArrayList<String> words = new ArrayList<>();
        return words;
        }
}
