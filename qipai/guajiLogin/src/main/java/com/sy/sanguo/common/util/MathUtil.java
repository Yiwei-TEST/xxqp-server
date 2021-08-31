package com.sy.sanguo.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathUtil {
   
	/**
	 * 随机获得某个区间的值（low,high）
	 * @param low
	 * @param high
	 * @return float
	 */
	public static double random(double low,double high){
    	return (double) (low+(high-low)*Math.random());
    }
	
	/**根据指定概率摇奖看是否命中，如果数值>1则百分百命中
	 * @param rate
	 * @return boolean
	 */
	public static boolean shake(double rate){
		return Math.random()<rate;
	}
	
	/**
	 * 随机获得某个区间的值（min,max）
	 * @param min
	 * @param max
	 * @return int
	 */
	public static int mt_rand(int min,int max){
		Random r = new Random();
		return min + r.nextInt(max-min+1);
	}
	
	/**
	 * 随机区间
	 * @param region
	 * @param values
	 * @return int
	 */
	public static int randomRegion(List<Integer> region, List<Integer> values){
		int total = 0;
		for(int i:region){
			total += i;
		}
		int sum = 0;
		int random = MathUtil.mt_rand(1, total);
		int index = 0;
		int i=0;
		for(int data:region){
			sum += data ;
	        if(random <= sum ){
	            index = i;
	            break;
	        }
	        i++;
		}
		return values.get(index);
	}
	
	/**
	 * 随机区间
	 * @param region
	 * @param values
	 * @return int
	 */
	public static int randomRegion(List<Integer> region, int[] values){
		int total = 0;
		for(int i:region){
			total += i;
		}
		int sum = 0;
		int random = MathUtil.mt_rand(1, total);
		int index = 0;
		int i=0;
		for(int data:region){
			sum += data ;
	        if(random <= sum ){
	            index = i;
	            break;
	        }
	        i++;
		}
		return values[index];
	}
	
	/**
	 * 随机区间
	 * @param region
	 * @param values
	 * @return int
	 */
	public static int randomRegion(int[] region, int[] values){
		int total = 0;
		for(int i:region){
			total += i;
		}
		int sum = 0;
		int random = MathUtil.mt_rand(1, total);
		int index = 0;
		int i=0;
		for(int data:region){
			sum += data ;
	        if(random <= sum ){
	            index = i;
	            break;
	        }
	        i++;
		}
		return values[index];
	}
	
	/**从1~n个自然数种随机取m个*/
	public static List<Integer> draw(int n,int m){
		if(n<m){
			return Collections.emptyList() ;
		}
		
		List<Integer> tmp = new ArrayList<Integer>();
		for(int i=1;i<=n;i++){
			tmp.add(i);
		}
		
		Collections.shuffle(tmp);
		
		return tmp.subList(0, m);
	}
}
