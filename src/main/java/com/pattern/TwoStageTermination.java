package com.pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * @program: su-concurrent
 * @description: 两阶段终止模式
 *     目的：在一个线程T1中如何“优雅”终止线程T2,这里的优雅指的是给T2一个料理后事的机会
 *     错误思路：
 *      1. 使用线程对象的stop()方法停止线程
 *          stop方法会真正杀死线程，如果这是线程锁住了共享资源，那么当它被杀死后就再也没有机会释放锁，其它线程将永远无法获取锁
 *     应用场景：后台监控系统的健康状况（使用后台的一个线程实时监控cpu等状态），一般该线程就是使用while(true)循环进行监控。但是我们应该有一个机制能让它停止监控
 *
 * @author: Su
 * @create: 2020-09-17 09:53
 **/
public class TwoStageTermination {
    private Thread monitor;
    //启动监控线程
    public void start(){
        monitor=new Thread(()->{
            while (true){
              boolean flag=  Thread.currentThread().isInterrupted();
              //判断在运行状态的线程是否被打断
              if(flag){
                  //如果是被打断，则在退出执行之前处理一些后续操作
                  System.out.println("料理后事");
                  break;
              }
              //如果没被打断
              try {
                  //该处sleep的好处就是防止cpu被该线程占有率过高
                    Thread.sleep(1000);//该处被打断时，进入异常处理，此时会清空打断标记，即标记为false
                    //执行监控记录的逻辑
                  System.out.println("执行监控记录"); //该处被打断时，打断标记会为true，则继续进入下一趟循环，然后退出执行
                } catch (InterruptedException e) {
                  //如果该线程在睡眠时被打断，则进入该异常
                    e.printStackTrace();
                    //重新设置打断标记为true
                  Thread.currentThread().interrupt();
                }
            }
        });

        monitor.start();

    }
    //停止监控线程
    public void stop(){
        monitor.interrupt();
    }
}
/**
 * 使用volatile来改进该模式
 */
class TwoStageTerminationImprove{
    private Thread monitor;
    // 标记是否停止循环
    private volatile boolean stop=false;
    //启动监控线程
    public void start(){
        monitor=new Thread(()->{
            while (true){
                //判断是否停止循环
                if(stop){
                    System.out.println("料理后事");
                    break;
                }
                //如果没被打断
                try {
                    //该处sleep的好处就是防止cpu被该线程占有率过高
                    Thread.sleep(1000);
                    System.out.println("执行监控记录");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        monitor.start();

    }
    //停止监控线程
    public void stop(){
        stop=true;
        //如果monitor线程刚好运行到sleep时，则使用打断使线程尽快结束
        monitor.interrupt();
    }
}


