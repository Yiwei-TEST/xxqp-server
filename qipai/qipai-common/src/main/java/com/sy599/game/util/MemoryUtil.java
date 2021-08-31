package com.sy599.game.util;

/**
 * @author taohuiliang
 * @date 2013-3-27
 * @version v1.0
 */
public class MemoryUtil {
    
	/**可利用的剩余内存量*/
	public static int getFreeMemoryMB(){
    	Runtime run = Runtime.getRuntime();	 
    	long max = run.maxMemory(); 
    	long total = run.totalMemory();
    	long free = run.freeMemory();
    	 
    	long usable = max - total + free;
    	
    	return (int) (usable/1024/1024);
    }
	
	/**分配的最大内存*/
	public static int getMaxMemoryMB(){
		Runtime run = Runtime.getRuntime();	 
    	long max = run.maxMemory(); 
    	return (int) (max/1024/1024);
	}
	
	/**当前分配的内存*/
	public static int getTotalMemoryMB(){
		Runtime run = Runtime.getRuntime();	 
    	long total = run.totalMemory(); 
    	return (int) (total/1024/1024);
	}
}
