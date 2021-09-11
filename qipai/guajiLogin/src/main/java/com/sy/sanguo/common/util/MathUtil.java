package com.sy.sanguo.common.util;

import java.util.*;

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

	public static int draw(Map<Integer, Long> map) {
		if (map != null && !map.isEmpty()) {
			double total_rate = 0.0D;

			double rate;
			for(Iterator var5 = map.values().iterator(); var5.hasNext(); total_rate += (double)rate) {
				rate = (Long)var5.next();
			}

			double random = Math.random();
			int i = 0;
			for(Iterator var7 = map.keySet().iterator(); var7.hasNext(); random -= rate) {
				int val = (Integer)var7.next();
				++i;
				rate = (double)(Long)map.get(val) / total_rate;
				if (random < rate && rate != 0.0D) {
					return val;
				}
			}

			String val = map.keySet().toArray()[0].toString();
			return Integer.parseInt(val);
		} else {
			return 0;
		}
	}

	public static void main(String[] args) {
		Map<Integer, Long> map = new HashMap<>();
		map.put(180,2000L);
		map.put(580,500L);
		map.put(880,400L);
		map.put(1800,250L);
		map.put(8800,100L);
		map.put(18800,50L);
		map.put(38800,0L);
		map.put(88800,0L);
		map.put(0,6700L);
		Map<Integer, Integer> rmap = new LinkedHashMap<>();
		rmap.put(180,0);
		rmap.put(580,0);
		rmap.put(880,0);
		rmap.put(1800,0);
		rmap.put(8800,0);
		rmap.put(18800,0);
		rmap.put(38800,0);
		rmap.put(88800,0);
		rmap.put(0,0);
		long sum = 0;
		int count = 1000;
		for(int i = 1; i <= count; i++){
			Integer p = draw(map);
			if(rmap.containsKey(p)){
				rmap.put(p,rmap.get(p)+1);
			} else {
				System.out.println("结果不在范围："+p);
			}

			sum += p;
		}

		for(Map.Entry<Integer, Integer> set : rmap.entrySet()){
			System.out.println(set.getKey()/100f + "分：" + set.getValue() + "次");
		}
		System.out.println("抽"+count+"一共出分"+sum/100);
	}
}
