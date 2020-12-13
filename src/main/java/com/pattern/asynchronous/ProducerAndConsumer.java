package com.pattern.asynchronous;

import java.util.LinkedList;

/**
 * @program: su-concurrent
 * @description: 异步模式之生产者和消费者
 *     要点：
 *        与前面的保护性暂停中的GuardObject不同，不需要产生结果和消费结果的线程一一对应
 *        消费队列可以用来平衡生产和消费的线程资源
 *        生产者仅负责产生结果数据，不关心数据该如何处理，而消费者专心处理结果数据
 *        生产者和消费者是通过消息队列连接，而消息队列是有容量限制的，满时不会再加入数据，空时不会再消耗数据（先进先出）
 *        JDK中各种阻塞队列，采用的就是这种模式
 *     保护性暂停模式之所以称为同步是因为 线程a一旦生产结果，线程b会立刻消费结果
 *     生产者和消费者模式称为异步是因为 生产者生产的消息发到消息队列中，并不意味着马上有消费者消费（队列中可能已经存在有消息，所以需要排队等候消费），会有延迟。
 *
 * @author: Su
 * @create: 2020-09-22 21:38
 **/
public class ProducerAndConsumer {
    public static void main(String[] args) {
        MessageQueue queue=new MessageQueue(2);
        for(int i=0;i<3;i++){
            int id=i;
            new Thread(()->{
                queue.put(new Message(id,"值"+id));
            },"生产者"+i).start();
        }
        new Thread(()->{
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                queue.take();
            }
        },"消费者").start();
    }
}

/**
 * 消息队列类，java线程之间通信（而RabbitMQ等是进程之间通信）
 */
class MessageQueue{
    //消息的队列集合 双向链表，一边放消息 一边取消息
    private LinkedList<Message> list=new LinkedList<>();
    //队列容量
    private int capcity;

    public MessageQueue(int capcity) {
        this.capcity = capcity;
    }

    //获取消息
    public Message take(){
        //检查队列是否为空
        synchronized (list) {
            while (list.isEmpty()) {
                try {
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //从队列头部获取消息并返回
            Message message= list.removeFirst();
            //已经消费了消息，需要唤醒正在wait的生产者线程
            list.notifyAll();
            return message;
        }
    }
    //存入消息
    public void put(Message message){
        synchronized (list){
            //检查队列是否已满
            while (list.size()==capcity){
                try {
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //将消息加入队列尾部
            list.addLast(message);
            //已经加入了消息，需要唤醒正在wait的消费者线程
            list.notifyAll();
        }

    }


}

final class Message{
    private int id;
    private Object value;

    public Message(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Object getObject() {
        return value;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}
