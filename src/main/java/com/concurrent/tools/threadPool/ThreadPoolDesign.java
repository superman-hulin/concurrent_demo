package com.concurrent.tools.threadPool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: su-concurrent
 * @description: 自定义线程池
 *  背景：
 *     线程是一种系统资源，每创建一个新线程，就会占用一定内存（分配栈内存），如果高并发时，为每个任务都创建一个线程，那么内存的消耗是非常大的
 *     线程不是创建的越多越好，会产生更多的线程上下文切换
 *     因此使用线程池，线程池就是创建一批线程，让这些线程可以得到充分的利用，这样既可以减少内存的占用，也可以减少线程的数量，避免出现频繁的上下文切换
 *  自定义线程池三个组件
 *      1. 线程池
 *          里面有可以被重用的线程
 *      2. 阻塞队列
 *         体现的是生产者和消费者模式下的平衡两者速度差异，线程池中的线程是任务的消费者
 *         作用：
 *            当生产者线程迟迟没有提交新的线程，那么线程池中的消费者线程必须等待，是在阻塞队列中等待
 *            当任务一下特别多，线程池中的线程来不及消费了，则把这些任务也放在阻塞队列中
 *
 * @author: Su
 * @create: 2020-10-04 11:46
 **/
public class ThreadPoolDesign {
    public static void main(String[] args) {
      SuThreadPool suThreadPool=  new SuThreadPool(2,
              1000,TimeUnit.MILLISECONDS,10,(queue,task)->{
          // 1. 死等
        // queue.put(task);
        // 2) 带超时等待
        // queue.offer(task, 1500, TimeUnit.MILLISECONDS);
        // 3) 让调用者放弃任务执行
        // log.debug("放弃{}", task);
        // 4) 让调用者抛出异常 假设任务3抛异常了,那么后面的任务就不会再执行了，因为主线程抛异常了，后面的就不会执行了
        // throw new RuntimeException("任务执行失败 " + task);
        // 5) 让调用者自己执行任务  让主线程自己执行该任务
          task.run();
      });
        for(int i=0;i<5;i++){
            int j=i;
            suThreadPool.execute(()->{
                System.out.println(j);
            });
        }
    }
}

/**
 * 当主线程一次性生产的任务数大于线程池核心数+任务队列容量，主线程应该采用何种策略的接口 由线程池使用者写具体的策略实现
 * @param <T>
 */
@FunctionalInterface // 拒绝策略
interface RejectPolicy<T> {
    void reject(BlockingQueue<T> queue, T task);
}

class SuThreadPool{
    //任务队列  任务使用Runnable类型
    private BlockingQueue<Runnable> taskQueue;
    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();
    // 线程池中的核心线程数
    private int coreSize;
    // 获取任务时的超时时间 当线程池中的线程去获取任务时，如果超过超时时间，则将该线程停止获取
    private long timeout;
    //时间单位
    private TimeUnit timeUnit;
    private RejectPolicy<Runnable> rejectPolicy;

    public SuThreadPool(int coreSize, long timeout, TimeUnit timeUnit,int queueCapcity,RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockingQueue<>(queueCapcity);
        this.rejectPolicy=rejectPolicy;
    }

    //执行任务 将任务交给线程执行
    public void execute(Runnable task){
        //workers是共享变量，并且不是线程安全的 则加锁保证线程安全 taskQueue是线程安全的
        synchronized (workers) {
            //如果线程集合中的线程数还没有超过coreSize时，则新建worker对象来执行该任务（初始线程集合为空）
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task);
                workers.add(worker);
                worker.start();
            }
            //如果线程集合超过coreSize时，则不能再新建worker对象了，需要将该任务加入任务队列，等待已有空闲线程获取并执行任务
            else {
                //taskQueue.put(task);
                /**
                 * 当任务数大于线程核心数+任务队列容量时，主线程有如下策略
                 *   1) 死等
                 *   2) 带超时等待
                 *   3) 让调用者放弃任务执行
                 *   4) 让调用者抛出异常
                 *   5) 让调用者自己执行任务
                 * 则采用策略模式来实现 不把具体的实现写死在线程池类中，而是提供策略接口，线程池使用者传入策略实现
                 */
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    class Worker extends Thread{
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            /**
             * 执行任务两种情况：
             *  1. 执行新建该worker时传入的任务task
             *  2. 当该worker执行完最开始新建时的任务时，该线程不应该停止，而是去任务队列中获取新任务继续执行
             */
            //while (task!=null||(task=taskQueue.take())!=null){ //taskQueue.take()会无限在队列中等待任务 则该线程不会结束
            while (task!=null||(task=taskQueue.poll(timeout,timeUnit))!=null){ //如果等待队列中的任务超时了，则该线程结束掉。
                try{
                    task.run();
                }catch (Exception e){

                }finally {
                    //任务被执行完了
                    task=null;
                }
            }
            //一旦该worker退出循环了 则代表没有任务需要执行了，则从线程集合中删除该线程
            synchronized (workers){
                workers.remove(this);
            }
        }
    }
}

class BlockingQueue<T>{
    //任务队列使用双向链表  ArrayDeque比LinkedList性能要好
    private Deque<T> queue=new ArrayDeque<>();
    //锁  线程池中有多个线程都要去队列的头部获取任务，则需要互斥。 使用锁去分别保护头部和尾部元素
    private ReentrantLock lock=new ReentrantLock();
    //阻塞队列是有容量的，当阻塞队列为空时，消费者需要进入阻塞，则需要条件变量， 当满时，生产者也需要阻塞，则需要条件变量
    private Condition fullWaitSet=lock.newCondition();
    private Condition emptyWaitSet=lock.newCondition();
    //阻塞队列容量
    private int capcity;

    public BlockingQueue(int capcity) {
        this.capcity = capcity;
    }

    //带超时的阻塞获取
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            // 将 timeout 统一转换为 纳秒
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                try {
                    if (nanos <= 0) {
                        return null;
                    }
                    // 返回值是剩余时间
                    nanos = emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    //阻塞获取
    public T take(){
       lock.lock();
       try{
        while (queue.isEmpty()){
            try {
                emptyWaitSet.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        T t=queue.removeFirst();
        //唤醒添加线程 队列不为满了
        fullWaitSet.signal();
        return t;
       }finally {
           lock.unlock();
       }
    }
    //阻塞添加 如果主线程中生产的任务数大于线程池的核心线程数加上任务队列，则主线程会一直死等任务队列有空位将剩余的任务加入队列
    public void put(T element){
        lock.lock();
        try{
            //循环判断 如果满了进入阻塞 等待被唤醒
            while (queue.size()==capcity){
                try {
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(element);
            //唤醒获取线程 队列不为空了
            emptyWaitSet.signal();
        }finally {
            lock.unlock();
        }
    }
    // 带超时时间阻塞添加
    public boolean offer(T task, long timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (queue.size() == capcity) {
                try {
                    if(nanos <= 0) {
                        return false;
                    }
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }
    //任务数超过时，主线程的拒绝策略
    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            // 判断队列是否满
            if(queue.size() == capcity) {
                rejectPolicy.reject(this, task);
            } else { // 有空闲
                queue.addLast(task);
                emptyWaitSet.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public int size(){
        lock.lock();
        try{
            return queue.size();
        }finally {
            lock.unlock();
        }
    }


}
