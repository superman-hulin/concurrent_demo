package com.concurrent.tools.principle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @program: su-concurrent
 * @description: 应用之缓存与数据库
 *      注意
 *          以下实现体现的是读写锁的应用，保证缓存和数据库的一致性，适合读多写少情景 但有下面的问题没有考虑
 *          适合读多写少，如果写操作比较频繁，以上实现性能低
 *          没有考虑缓存容量
 *          没有考虑缓存过期
 *          只适合单机
 *          并发性还是低，目前只会用一把锁
 *          更新方法太过简单粗暴，清空了所有 key（考虑按类型分区或重新设计 key）
 * 乐观锁实现：用 CAS 去更新
 * @author: Su
 * @create: 2020-10-10 18:39
 **/
public class CachedDao {
}

class GenericDao<T> {
    public T queryOne(Class<T> beanClass, String sql, Object... params) {
            return null;
    }
    public int update(String sql, Object... params) {
        return 1;
    }
}

/**
 * 对普通的dao查询加入缓存机制，即 使用map结构作为缓存，查询时先查询缓存，如果缓存没有再去查数据库
 * 单线程时，下面是没有问题的 但是多线程时，则会有
 * @param <T>
 */
class GenericCachedDao1<T> {
    //问题1： HashMap 作为非线程安全, 则多线程下会有问题  但是就算用了线程安全的map 下面代码依然有线程问题
    HashMap<SqlPair, T> map = new HashMap<>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    //不带缓存机制的dao
    GenericDao genericDao = new GenericDao();

    /**
     * 查询
     * 问题2：假设一开始多个线程都执行该方法，那么都判断缓存中没有 则都执行查询数据库了 那缓存没起到作用
     * @param beanClass
     * @param sql
     * @param params
     * @return
     */
    public T queryOne(Class<T> beanClass, String sql, Object... params) {
        SqlPair key = new SqlPair(sql, params);
        T value = map.get(key);
        //判断缓存中是否有值
        if (value != null) {
            //如果缓存有 则直接返回
            return value;
            }
        //如果没有 则需要去数据库查询
        value = (T)genericDao.queryOne(beanClass, sql, params);
        //将查询结果放入缓存
        map.put(key, value);
        return value;

    }

    /**
     * 问题3：
     *     清空缓存和更新数据库的前后顺序
     *     1. 先清缓存 再更新数据库
     *          线程b先执行清空缓存，此时线程a执行了查询数据库（x=1） 然后将查询结果放入缓存（x=1）
     *          线程b再执行更新数据库（x2）
     *          那么后续所有线程查询将一直是旧值（x=1） 导致缓存和数据库中数据不一致
     *          上述情况很容易发生，因为更新操作会比查询慢一些
     *     2. 先更新数据库 再清缓存
     *          线程b先更新库（x=2），然后线程a查询缓存（x=1）
     *          线程b再清空缓存 线程a再查询的话，会去数据库查询（x=2），并放入缓存
     *          则后续查询都可以得到新值（x=2）
     *     很明显 第二种方式比第一种更好 但是第二种也会出现错误的情况 这时就需要锁来解决
     *
     * @param sql
     * @param params
     * @return
     */
    public int update(String sql, Object... params) {
        /**
         * 采用先更新数据库 再清缓存
         *
         */
        int d=genericDao.update(sql, params);
        map.clear();
        return d;
    }

    // 作为 key 保证其是不可变的
    class SqlPair {
        private String sql;
        private Object[] params;
        public SqlPair(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SqlPair sqlPair = (SqlPair) o;
            return sql.equals(sqlPair.sql) &&
                    Arrays.equals(params, sqlPair.params);
        }
        @Override
        public int hashCode() {
            int result = Objects.hash(sql);
            result = 31 * result + Arrays.hashCode(params);
            return result;
        }
    }
}


/**
 * 使用锁来解决多线程问题  使用读写锁来保证数据正确性和性能（比加普通锁的性能高）
 * @param <T>
 */
class GenericCachedDao2<T> {
    HashMap<SqlPair, T> map = new HashMap<>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    //不带缓存机制的dao
    GenericDao genericDao = new GenericDao();
    public T queryOne(Class<T> beanClass, String sql, Object... params) {
        SqlPair key = new SqlPair(sql, params);
        /**
         * 加读锁, 防止其它线程对缓存更改
         */
        lock.readLock().lock();
        try {
            T value = map.get(key);
            if (value != null) {
                return value;
            }
        } finally {
            lock.readLock().unlock();
        }
        /**
         * 加写锁, 防止其它线程对缓存读取和更改
         * 当一开始缓存为空，则多个线程都执行到该处 只有一个线程获取到写锁 从而进行查询操作并放入缓存 最后释放锁
         * 但是后面的线程拿到锁后 依然又进行了查询 并放入缓存
         * 所以需要通过二次检测来解决该问题
         */
        lock.writeLock().lock();
        try {
            /**
             * 为防止重复查询数据库, 再次验证
             */
            T value = map.get(key);
            if (value == null) {
                // 如果没有, 查询数据库
                value = (T)genericDao.queryOne(beanClass, sql, params);
                map.put(key, value);
            }
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int update(String sql, Object... params) {
        SqlPair key = new SqlPair(sql, params);
        /**
         *   加写锁, 防止其它线程对缓存读取和更改
         *   加锁后 清缓存和更新数据库的顺序就无所谓了
         */
        lock.writeLock().lock();
        try {
            int rows = genericDao.update(sql, params);
            map.clear();
            return rows;
        } finally {
            lock.writeLock().unlock();
        }
    }
    // 作为 key 保证其是不可变的
    class SqlPair {
        private String sql;
        private Object[] params;
        public SqlPair(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SqlPair sqlPair = (SqlPair) o;
            return sql.equals(sqlPair.sql) &&
                    Arrays.equals(params, sqlPair.params);
        }
        @Override
        public int hashCode() {
            int result = Objects.hash(sql);
            result = 31 * result + Arrays.hashCode(params);
            return result;
        }
    }
}
