package com.pattern.asynchronous;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @program: su-concurrent
 * @description: 异步模式之工作线程
 *     定义:
 *         让有限的工作线程来轮流异步处理无限多的任务。典型实现就是线程池，也体现了经典设计模式中的享元模式（重用对象，而该模式是重用线程对象）
 *     注意，不同任务类型应该使用不同的线程池，这样能够避免饥饿，并能提高效率
 *        例如，如果一个餐馆的工人既要招呼客人（任务类型A），又要到后厨做菜（任务类型B）显然效率不咋地，分成服务员（线程池A）与厨师（线程池B）更为合理
 *     饥饿
 *        固定大小线程池会有饥饿现象
 *          两个工人是同一个线程池中的两个线程
 *          他们要做的事情是：为客人点餐和到后厨做菜，这是两个阶段的工作
 *           客人点餐：必须先点完餐，等菜做好，上菜，在此期间处理点餐的工人必须等待
 *           后厨做菜：没啥说的，做就是了
 *         比如工人A 处理了点餐任务，接下来它要等着 工人B 把菜做好，然后上菜，他俩也配合的蛮好
 *         但现在同时来了两个客人，这个时候工人A 和工人B 都去处理点餐了，这时没人做饭了，饥饿
 *     创建多少线程池合适
 *       过小会导致程序不能充分利用系统资源、容易导致饥饿
 *       过大会导致更多的线程上下文切换，占用更多内存
 *       1.CPU密集型运算
 *          通常采用 cpu 核数 + 1 能够实现最优的 CPU 利用率，+1 是保证当线程由于页缺失故障（操作系统）或其它原因导致暂停时，额外的这个线程就能顶上去，保证 CPU 时钟周期不被浪费
 *       2.IO密集型运算
 *          CPU 不总是处于繁忙状态，例如，当你执行业务计算时，这时候会使用 CPU 资源，但当你执行 I/O 操作时、远程RPC 调用时，包括进行数据库操作时，这时候 CPU 就闲下来了，你可以利用多线程提高它的利用率。
 *          经验公式如下
 *           线程数 = 核数 * 期望 CPU 利用率 * 总时间(CPU计算时间+等待时间) / CPU 计算时间
 *          例如 4 核 CPU 计算时间是 50% ，其它等待时间是 50%，期望 cpu 被 100% 利用，套用公式
 *          4 * 100% * 100% / 50% = 8
 *          例如 4 核 CPU 计算时间是 10% ，其它等待时间是 90%，期望 cpu 被 100% 利用，套用公式
 *          4 * 100% * 100% / 10% = 40
 * @author: Su
 * @create: 2020-10-06 20:45
 **/
public class WorkerThread {
}

class TestDeadLock {
    static final List<String> MENU = Arrays.asList("地三鲜", "宫保鸡丁", "辣子鸡丁", "烤鸡翅");
    static Random RANDOM = new Random();
    static String cooking() {
        return MENU.get(RANDOM.nextInt(MENU.size()));
    }
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> { //开启线程1
            System.out.println("处理点餐...");
            Future<String> f = executorService.submit(() -> { //开启线程2
                System.out.println("做菜.");
                return cooking();
            });
            try {
                System.out.println(("上菜: "+f.get()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
    //以上方式相当于模拟来了一个客人，两个线程一起工作，一个点餐和上菜，一个做菜 不会有问题
    //当来了两个客人时，则两个线程都去点餐了，并且都在等待新的线程来做菜，但是由于线程数不够，则两个线程一直死等，称为饥饿
    public static void m1() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> { //开启线程1
            System.out.println("处理点餐..");
            Future<String> f = executorService.submit(() -> {
                System.out.println("做菜");
                return cooking();
            });
            try {
                System.out.println(("上菜: "+f.get()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> { //开启线程3
            System.out.println("处理点餐");
            Future<String> f = executorService.submit(() -> {
                System.out.println("做菜");
                return cooking();
            });
            try {
                System.out.println(("上菜: "+f.get()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * 解决饥饿方案：
     *     解决方法可以增加线程池的大小，不过不是根本解决方案。
     *     应该是不同任务类型使用不同的线程池
     *     ExecutorService waiterPool = Executors.newFixedThreadPool(1);
     *     ExecutorService cookPool = Executors.newFixedThreadPool(1);
     */
}
