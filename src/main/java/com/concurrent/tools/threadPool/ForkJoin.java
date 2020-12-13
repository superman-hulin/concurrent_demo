package com.concurrent.tools.threadPool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * @program: su-concurrent
 * @description: Fork/join线程池
 *      概念
 *          Fork/Join 是 JDK 1.7 加入的新的线程池实现，它体现的是一种分治思想，适用于能够进行任务拆分的 cpu 密集型运算
 *          所谓的任务拆分，是将一个大任务拆分为算法上相同的小任务，直至不能拆分可以直接求解。跟递归相关的一些计算，如归并排序、斐波那契数列、都可以用分治思想进行求解
 *          Fork/Join 在分治的基础上加入了多线程，可以把每个任务的分解和合并交给不同的线程来完成，进一步提升了运算效率
 *          Fork/Join 默认会创建与 cpu 核心数大小相同的线程池
 *      使用
 *          提交给 Fork/Join 线程池的任务需要继承 RecursiveTask（有返回值）或 RecursiveAction（没有返回值）
 * @author: Su
 * @create: 2020-10-08 21:39
 **/
public class ForkJoin {
    public static void main(String[] args) {
        ForkJoinPool pool=new ForkJoinPool(); //无参的话，就默认创建与 cpu 核心数大小相同的线程池
        /**
         * 执行流程
         *      假设线程池中只有四个线程，则线程1执行5+{4},一直等待线程2返回结果  线程2执行4+{3},一直等待线程3返回结果
         *      线程3执行3+{2},一直等待线程0返回结果 线程0执行2+{1},并且也执行{1}。 最后结果不断向上返回。
         *      缺点：线程之间执行的任务依赖性太高，没有并行执行
         */
        System.out.println(new MyTask(5));
        pool.submit(new MyTask(5));

        /**
         * 执行流程
         *     线程1执行{1,3}+{4,5},等待线程2和线程3返回结果  线程2执行{1,2}+{3,3}  线程3执行{4,5}  线程0执行{1,2} 并且也执行{3,3}
         *     这种拆分改进后，执行效率更高
         */
        System.out.println(new AddTask1(1,5));

    }


}

/**
 * ForkJoin线程池中只能接收RecursiveTask类型(有返回值)的任务或RecursiveAction（无返回值）类型的任务
 * 求解1-n之间整数的和，采用分治思想
 *     如n取5，则和=new MyTask(5) ,而new MyTask(5)=5+new MyTask(4)  4+new MyTask(3) 3+new MyTask(2) 2+new MyTask(1)
 */

class MyTask extends RecursiveTask<Integer> {
    private int n;

    public MyTask(int n) {
        this.n = n;
    }

    @Override
    protected Integer compute() {
        //分治的终止条件
        if(n==1){
            return 1;
        }
        MyTask t1=new MyTask(n-1);
        //让线程池中的一个线程去执行t1任务
        t1.fork();
        //合并任务结果
        int result=n+t1.join();
        return result;
    }
}

/**
 * 改进线程并行执行的效率
 *      核心在于对任务的划分上
 */
class AddTask1 extends RecursiveTask<Integer> {
    int begin;
    int end;
    public AddTask1(int begin, int end) {
        //求和的起点数
        this.begin = begin;
        //求和的终点数
        this.end = end;
    }
    @Override
    public String toString() {
        return "{" + begin + "," + end + '}';
    }
    @Override
    protected Integer compute() {
        //当只有一个数时直接返回
        if (begin == end) {
            return begin;
        }
        //当只有两个数时直接返回相加 当然也可以继续拆分 不过没意义
        if (end - begin == 1) {
            return end + begin;
        }
        //按中间数拆分为两个求和任务
        int mid = (end + begin) / 2; // 3
        AddTask1 t1 = new AddTask1(begin, mid); // 1,3
        t1.fork();
        AddTask1 t2 = new AddTask1(mid + 1, end); // 4,5
        t2.fork();
        //合并两个求和任务的结果
        int result = t1.join() + t2.join();
        return result;
    }
}
