package com.sy599.sanguo.cache;

import java.util.LinkedHashMap;
import java.util.Map;


public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	
	private static final long serialVersionUID = -678769635134661827L;
	
	/** 最大数据存储容量 */  
    private static final int  LRU_MAX_CAPACITY = 1024;
    /** 存储数据容量  */  
    private int capacity;
    private static float loadFactor = 0.75f;
    
    /** 
     * 默认构造方法 
     */  
    public LRULinkedHashMap(){
        super();
    }
    
    /**
     * 带参数构造方法
     * @param initialCapacity
     */
    public LRULinkedHashMap(int initialCapacity){
        super(initialCapacity, loadFactor, true);
        capacity = LRU_MAX_CAPACITY;
    }
    
    /**
     * 带参数构造方法
     * @param initialCapacity
     * @param accessOrder
     */
    public LRULinkedHashMap(int initialCapacity,boolean accessOrder){
        super(initialCapacity, loadFactor, accessOrder);
        capacity = LRU_MAX_CAPACITY;
    }
    
    /**
     * 带参数构造方法
     * @param initialCapacity 初始容量
     * @param lruCapacity 最大容量
     */
    public LRULinkedHashMap(int initialCapacity, int lruCapacity){
        super(initialCapacity, loadFactor, true);
        this.capacity = lruCapacity;
    }
    
    /**
     * 带参数构造方法
     * @param initialCapacity 初始容量
     * @param lruCapacity 最大容量
     * @param accessOrder 排序规则
     */
    public LRULinkedHashMap(int initialCapacity, int lruCapacity,boolean accessOrder){
        super(initialCapacity, loadFactor, accessOrder);
        this.capacity = lruCapacity;
    }
    
    /**  
     * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry) 
     */  
    @Override  
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest){
        if(size() > capacity) {
            return true;
        }
        return false;
    }
}  

