package com.example.demo.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器:
 *      用于解决缓存穿透问题。
 * 布隆过滤器通过初始1个值全为0的数组, 再通过 hash (3次hash) 的方法判断元素是否在集合中。   无法删除的数据结构
 * 存储数据: id为1的数据，通过多个hash函数获取hash值，根据hash计算数组对应位置改为1; **查询数据**:使用相同hash函数获取hash值，判断对应位置是否都为1。
 * 存在误判, 如果某个 id 对应的hash值被其他已存在 id 的hash值覆盖, 就会使得本来不存在的数据被判定为存在(一般设置误判率为5%以内)。
 */
@Component
public class BloomFilterUtil {
    private static final long  capacity = 100000L;
    private static final double errorRate = 0.03;
    private BloomFilter<Long> bloomFilter;

    public BloomFilterUtil() {
        this.bloomFilter = BloomFilter.create(Funnels.longFunnel(), capacity, errorRate);
    }

    public void add(String key) {
        bloomFilter.put(Long.parseLong(key));
    }

    public boolean mightContain(String key) {
        return bloomFilter.mightContain(Long.parseLong(key));
    }
}