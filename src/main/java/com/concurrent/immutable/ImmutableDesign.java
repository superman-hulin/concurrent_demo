package com.concurrent.immutable;

/**
 * @program: su-concurrent
 * @description: 不可变类的设计
 *  以String为例，说明不可变设计的要素
 *     1.final的使用
 *       属性用final修饰保证了该属性是只读的，不能修改
 *       类用final修饰保证了该类中的方法不能被覆盖，防止子类无意间破坏不可变性
 *     2.保护性拷贝（通过创建副本对象来避免共享的手段称之为保护性拷贝）
 *       使用char数组构造String对象时，String类中的数组变量虽然是final，但是这只是保证对象引用不可变，而构造函数中的数组参数中的元素依然会被外部修改，
 *       为了依然保证不可变性，使用保护性拷贝（也就是将char数组参数对象拷贝成新的对象）
 *  保护性拷贝可以解决这种共享安全问题，但是同时也会创建太多新的对象，所以不可变类都会关联一个享元模式
 *
 * @author: Su
 * @create: 2020-10-03 18:22
 **/
public class ImmutableDesign {
}
