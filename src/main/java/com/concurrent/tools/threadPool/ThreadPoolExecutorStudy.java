package com.concurrent.tools.threadPool;

/**
 * @program: su-concurrent
 * @description: ThreadPoolExecutor学习
 *      线程池状态
 *          使用int的高3位来表示线程池状态，低29位表示线程数量
 *          状态名      高三位  说明
 *          RUNNING     111
 *          SHOTDOWN    000   不会接收新任务，但会处理阻塞队列剩余任务
 *          STOP        001   会中断正在执行的任务，并抛弃阻塞队列任务
 *          TIDYING     010   任务全执行完毕，活动线程为0即将进入终结
 *          TERMINATED  011   终结状态
 *      之所以将线程池状态与线程个数合二为一成一个变量（原子变量ctl）,这样就可以用一次 cas 原子操作进行赋值
 *
 *      工作方式
 *          线程池中刚开始没有线程，当一个任务提交给线程池后，线程池会创建一个新线程来执行任务。
 *          当线程数达到 corePoolSize 并没有线程空闲，这时再加入任务，新加的任务会被加入workQueue 队列排队，直到有空闲的线程。
 *          如果队列选择了有界队列，那么任务超过了队列大小时，会创建 maximumPoolSize - corePoolSize 数目的线程来救急。
 *          如果线程到达 maximumPoolSize 仍然有新任务这时会执行拒绝策略。拒绝策略 jdk 提供了 4 种实现，其它著名框架也提供了实现
 *          当高峰过去后，超过corePoolSize 的救急线程如果一段时间没有任务做，需要结束节省资源，这个时间由keepAliveTime 和 unit 来控制
 * @author: Su
 * @create: 2020-10-07 22:03
 **/
public class ThreadPoolExecutorStudy{
    /**
     * ThreadPoolExecutor的构造方法
     *  public ThreadPoolExecutor(int corePoolSize, //核心线程数
     *      int maximumPoolSize, //最大线程数
     *      long keepAliveTime,  //针对救急线程的生存时间
     *      TimeUnit unit,  //时间单位
     *      BlockingQueue<Runnable> workQueue, //阻塞队列
     *      ThreadFactory threadFactory, //线程工厂（生产线程对象，可以为线程创建时起名字）
     *      RejectedExecutionHandler handler) //拒绝策略
     */


    /**
     * 根据构造方法，JDK Executors 类中提供了众多工厂方法来创建各种用途的线程池
     *     1.固定线程池
     *      public static ExecutorService newFixedThreadPool(int nThreads) {
     *          return new ThreadPoolExecutor(nThreads, nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
     *          }
     *        核心线程数 == 最大线程数（没有救急线程被创建），因此也无需超时时间
     *        阻塞队列是无界的，可以放任意数量的任务
     *       适用于任务量已知，相对耗时的任务
     *    2. 缓冲线程池
     *      public static ExecutorService newCachedThreadPool() {
     *          return new ThreadPoolExecutor(0, Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
     *          }
     *      核心线程数是 0， 最大线程数是 Integer.MAX_VALUE，救急线程的空闲生存时间是 60s，意味着
     *           全部都是救急线程（60s 后可以回收）
     *           救急线程可以无限创建
     *      队列采用了 SynchronousQueue 实现特点是，它没有容量，没有线程来取是放不进去的（一手交钱、一手交货）
     *      整个线程池表现为线程数会根据任务量不断增长，没有上限，当任务执行完毕，空闲 1分钟后释放线程。 适合任务数比较密集，但每个任务执行时间较短的情况
     *    3. 单线程的线程池
     *      public static ExecutorService newSingleThreadExecutor() {
     *          return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()));
     *          }
     *      使用场景：
     *          希望多个任务排队执行。线程数固定为 1，任务数多于 1 时，会放入无界队列排队。任务执行完毕，这唯一的线程也不会被释放。
     *      区别：
     *          自己创建一个单线程串行执行任务，如果任务执行失败而终止那么没有任何补救措施，而线程池还会新建一个新线程，保证池的正常工作
     *          Executors.newSingleThreadExecutor() 线程个数始终为1，不能修改
     *          FinalizableDelegatedExecutorService 应用的是装饰器模式，只对外暴露了 ExecutorService 接口，因此不能调用 ThreadPoolExecutor 中特有的方法
     *          Executors.newFixedThreadPool(1) 初始时为1，以后还可以修改，对外暴露的是 ThreadPoolExecutor 对象，可以强转后调用 setCorePoolSize 等方法进行修改
     */


    /**
     * 提交任务
     *      // 执行任务
     *      void execute(Runnable command);
     *      // 提交任务 task，用返回值 Future 获得任务执行结果
     *      <T> Future<T> submit(Callable<T> task);
     *      // 提交 tasks 中所有任务
     *      <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)throws InterruptedException;
     *      // 提交 tasks 中所有任务，带超时时间
     *      <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
     *      long timeout, TimeUnit unit)throws InterruptedException;
     *      // 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消
     *      <T> T invokeAny(Collection<? extends Callable<T>> tasks)throws InterruptedException, ExecutionException;
     *      // 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消，带超时时间
     *      <T> T invokeAny(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit)throws InterruptedException, ExecutionException, TimeoutException;
     */

}
