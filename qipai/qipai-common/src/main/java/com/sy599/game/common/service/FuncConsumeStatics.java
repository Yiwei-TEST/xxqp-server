package com.sy599.game.common.service;

import com.alibaba.fastjson.TypeReference;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.staticdata.model.ConsumeRegion;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import org.apache.commons.lang.StringUtils;

import java.util.*;


/**
 * 玩家当天各个功能的使用次数统计
 * @author zhoufan
 * @date 2013-4-26
 * @version v1.0
 */
public class FuncConsumeStatics {
	
	private static List<Integer> idList = new ArrayList<Integer>();
	static{
		idList = Arrays.asList(
			
		);
	}
	
	private Map<Integer, Integer> consumeMap;
	private Map<Integer, Integer> tipsMap;
	private Player player;
	
	public FuncConsumeStatics(Player player){
		this.player = player;
	}
	
	/**
	 * 初始化
	 * @param consume void
	 */
	public void initData(String consume){
		JsonWrapper json = new JsonWrapper(consume);
		String tips = json.getString(SharedConstants.FUNC_NO_TIPS);
		//消费提示

		if(!StringUtils.isBlank(tips)){
			this.tipsMap = JacksonUtil.readValue(tips, new TypeReference<Map<Integer, Integer>>() {});
		}else{
			this.tipsMap = new HashMap<Integer, Integer>();
		}
		//消费次数
		this.consumeMap = new HashMap<Integer, Integer>();
		for(int id:idList){
			consumeMap.put(id, json.getInt(id, 0));
		}
	}
	
	/**
	 * 转成JSON格式
	 * @return String
	 */
	public String toJson(){
		JsonWrapper json = new JsonWrapper("");
		Set<Integer> set = consumeMap.keySet();
        Iterator<Integer> it = set.iterator();
        Integer intVal;
        while(it.hasNext()){
        	intVal = it.next();
        	json.putInt(intVal, consumeMap.get(intVal));
        }
        json.putString(SharedConstants.FUNC_NO_TIPS, JacksonUtil.writeValueAsString(tipsMap));
		return json.toString();
	}
	
	/**
	 *  凌晨5点更新功能使用次数
	 */
	public void refreshFiveAM(){
		for(int id:idList){
			consumeMap.put(id, 0);
		}
		player.changeConsumeNum();
		//player.writeMessage(buildMsg());
	}
	
	/**
	 * 指定功能的最大使用次数
	 * @param jsonkey
	 * @return int
	 */
	public int getMaxConsumeNum(int jsonkey){
		int num=0;
		switch(jsonkey){
			
		}
		return num;
	}
	
	/**
	 * 是否超过了次数上限，超过返回true
	 * @param jsonkey
	 * @param inc 增长次数
	 * @return boolean
	 */
	public boolean isOverMaxNum(int jsonkey,int inc){
		if(inc <= 0)
			return false;
		if(consumeMap.containsKey(jsonkey)){
			int num = consumeMap.get(jsonkey);
			num += inc;
			if(num <= getMaxConsumeNum(jsonkey)){
				return false;
			}else{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 使用剩余次数
	 * @param jsonkey
	 * @return
	 */
	public int canUseNum(int jsonkey){
		int maxNum= getMaxConsumeNum(jsonkey);
		if(consumeMap.containsKey(jsonkey)){
			int num = consumeMap.get(jsonkey);
			if(num<0){
				return maxNum;
			}
			return maxNum-num;
		}
		return maxNum;
	}
	
	/**
	 * 批量使用指定功能当前消费需要的钻石 
	 * @param featureId
	 * @param inc 增量
	 * @return int
	 */
	public int getNeedYBNum(int featureId,int inc){
		int amount = 0;
		List<ConsumeRegion> crList = getConsumeList(featureId);
		//消费数额不随次数累加的功能
		if(crList.get(0).getRegion()==0){
			amount = crList.get(0).getAmount() * inc;
		}else{
			//当前次数
			int usedNum = consumeMap.get(featureId);
			int maxNum = usedNum+inc;
			int currentlyNum = usedNum;
			for(ConsumeRegion cr:crList){
				if(currentlyNum <= maxNum){
					if(currentlyNum>cr.getRegion()){
						continue;
					}else{
						for(;currentlyNum<cr.getRegion();currentlyNum++){
							if(currentlyNum < maxNum){
								amount += cr.getAmount();
							}else{
								break;
							}
						}
					}
				}
			}
			//过了区间的上限，用上限的消费钻石数额计算
			if(currentlyNum < maxNum){
				amount += (maxNum-currentlyNum) * crList.get(crList.size()-1).getAmount();
			}
		}
		return amount;
	}
	
	/**
	 * 获取消费提示
	 * @param jsonkey
	 * @return int
	 *//*
	private int getConsumeTips(int jsonkey){
		Integer result = tipsMap.get(jsonkey);
		return (result == null) ? 0 : 1;
	}*/
	
	/**
	 * 更新提示
	 * @param jsonkey void
	 */
	public void setConsumeTips(int jsonkey){
		tipsMap.put(jsonkey, 1);
		this.player.changeConsumeNum();
	}
	
	public Map<Integer, Integer> getTipsMap() {
		return tipsMap;
	}
	
	
	/**
	 * 找出属于指定功能的区间定义
	 * @param featureId
	 * @return List<ConsumeRegion>
	 */
	private List<ConsumeRegion> getConsumeList(int featureId){
		return StaticDataManager.consumeMap.get(featureId);
	}
	
	/**
	 * 更新一个功能点的消费次数
	 * @param jsonkey 功能的标识ID
	 * @param inc 变化量
	 */
	public void updateData(int jsonkey,int inc){
		if(consumeMap.containsKey(jsonkey)){
			int num = consumeMap.get(jsonkey);
			num += inc;
			if(num <= getMaxConsumeNum(jsonkey)){
				consumeMap.put(jsonkey, num);
				player.changeConsumeNum();
			}
		}
	}
	
	/**
	 * 指定功能的使用次数
	 * @param featureId
	 * @return int
	 */
	public int getCount(int featureId){
		return consumeMap.get(featureId);
	}

	public Map<Integer, Integer> getConsumeMapMsg() {
		Map<Integer,Integer> copy = new HashMap<Integer,Integer>();
		copy.putAll(consumeMap);	
		return copy;
	}
}
