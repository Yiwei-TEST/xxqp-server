package com.sy599.game.common.service;


import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 指定时间点，刷新玩家的各个功能点计数的管理类
 * @author zhoufan
 * @date 2013-4-25
 * @version v1.0
 */
public class PlayerTimedRefresh {
	//时间点定义
	private static final String FIVE_AM   = "05:00";
	private static final String TWELVE_AM = "12:00";
	private static final String TWO_PM    = "14:00";
	private static final String EIGHT_PM  = "20:00";
	private static final String NINE_PM  = "21:00";
	private static final String TEN_PM  = "22:00";
	private static Map<Integer, String> timeMap = new HashMap<Integer, String>();
	static{
		timeMap.put(SharedConstants.REFRESH_TIME_FIVE_AM, FIVE_AM);
		timeMap.put(SharedConstants.REFRESH_TIME_TWELVE_AM, TWELVE_AM);
		timeMap.put(SharedConstants.REFRESH_TIME_TWO_PM, TWO_PM);
		timeMap.put(SharedConstants.REFRESH_TIME_EIGHT_PM, EIGHT_PM);
		timeMap.put(SharedConstants.REFRESH_TIME_NINE_PM, NINE_PM);
		timeMap.put(SharedConstants.REFRESH_TIME_TEN_PM, TEN_PM);
	}
	/** 数据库存储的player的时间点map **/
	private Map<Integer, Long> refreshTimeMap;
	private Player player;
	
	public PlayerTimedRefresh(Player player){
		this.player = player;
	}
	
	public void initData(String refreshTime){
		this.refreshTimeMap = json2Map(refreshTime);
		//新用户进来后,将这些时间初始化
		for(Map.Entry<Integer, String> entry:timeMap.entrySet()){
			if(refreshTimeMap.get(entry.getKey()) == 0)
				refreshedInLogin(entry.getKey(), entry.getValue());
		}
	}
	
	private Map<Integer, Long> json2Map(String refreshTime){
		Map<Integer, Long> map = new HashMap<Integer, Long>();
		JsonWrapper json = new JsonWrapper(refreshTime);
		for(Map.Entry<Integer, String> entry:timeMap.entrySet()){
			map.put(entry.getKey(), json.getLong(entry.getKey(), 0));
		}
		return map;
	}
	
	public String toJson(){
		JsonWrapper json = new JsonWrapper("");
		Set<Integer> set = refreshTimeMap.keySet();
        Iterator<Integer> it = set.iterator();
        Integer intVal;
        while(it.hasNext()){
        	intVal = it.next();
        	json.putLong(intVal, refreshTimeMap.get(intVal));
        }
		return json.toString();
	}
	
	/**
	 *  05:00:00时间点的触发处理
	 */
	public void fiveAMHandler(){
		//刷新功能使用次数
		player.getFuncConsume().refreshFiveAM();
	}
	
	/**
	 *  12:00:00时间点的触发处理
	 */
	private void twelveAMHandler(){
	}
	/**
	 *  14:00:00时间点的触发处理
	 */
	private void twoPMHandler(){
		
	}
	/**
	 *  20:00:00时间点的触发处理
	 */
	private void eightPMHandler(){
	}
	
	/**
	 *  21:00:00时间点的触发处理
	 */
	private void ninePMHandler(){
		
	}
	
	/**
	 *  22:00:00时间点的触发处理
	 */
	private void tenPMHandler(){
		
	}
	
	/**
	 * 指定功能需要刷新
	 * @param jsonkey
	 * @return boolean
	 */
	private boolean isRefresh(int jsonkey){
		long dbMillis = refreshTimeMap.get(jsonkey);
		if(dbMillis == 0){
			return true;
		}
		return !TimeUtil.isSameDay(TimeUtil.currentTimeMillis(), dbMillis);
	}
	
	/**
	 * 刷新时间map
	 * @param jsonkey void
	 * @param player
	 */
	private void refreshed(int jsonkey){
		//更新数据库的时间
		if(refreshTimeMap.containsKey(jsonkey)){
			refreshTimeMap.put(jsonkey, TimeUtil.currentTimeMillis());
			player.changeRefreshTime();
		}
	}
	
	/**
	 * 刷新时间map
	 * @param jsonkey
	 * @param time 格式E.g. 08:30
	 */
	private void refreshedInLogin(int jsonkey, String time){
		Calendar cale = Calendar.getInstance();
		StringBuilder sb = new StringBuilder();
		try{
			SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat formatHour = new SimpleDateFormat("yyyyMMdd HH:mm");
			if(!TimeUtil.isPass(time)){
				//没到今天的刷新时间点，把数据库时间更新成昨天的时间点
				cale.add(Calendar.DAY_OF_MONTH, -1);
			}
			cale.setTime(formatHour.parse(
				sb.append(formatDay.format(cale.getTime())).append(" ").append(time).toString()
			));
		}catch(Exception e){
			LogUtil.e("date format error"+e);
		}
		
		refreshTimeMap.put(jsonkey, cale.getTimeInMillis());
		player.changeRefreshTime();
	}
	
	/**
	 * 登录的时间检查,过了一天即需要刷新
	 * @param jsonKey
	 * @return boolean
	 */
	private boolean timeDiffInLogin(int jsonKey){
		long now = TimeUtil.currentTimeMillis();
		long dbMillis = refreshTimeMap.get(jsonKey);
		//做3秒的容错时间
		return (now-dbMillis)>(86400000-3000);
	}
	
	/**
	 *  登录游戏检查
	 */
	public void loginCheck(){
		//05:00:00
		if(timeDiffInLogin(SharedConstants.REFRESH_TIME_FIVE_AM)){
			fiveAMHandler();
			refreshedInLogin(SharedConstants.REFRESH_TIME_FIVE_AM, FIVE_AM);
		}
		//12:00:00
		if(timeDiffInLogin(SharedConstants.REFRESH_TIME_TWELVE_AM)){
			twelveAMHandler();
			refreshedInLogin(SharedConstants.REFRESH_TIME_TWELVE_AM, TWELVE_AM);
		}
		//14:00:00
		if(timeDiffInLogin(SharedConstants.REFRESH_TIME_TWO_PM)){
			twoPMHandler();
			refreshedInLogin(SharedConstants.REFRESH_TIME_TWO_PM, TWO_PM);
		}
		//20:00:00
		if(timeDiffInLogin(SharedConstants.REFRESH_TIME_EIGHT_PM)){
			eightPMHandler();
			refreshedInLogin(SharedConstants.REFRESH_TIME_EIGHT_PM, EIGHT_PM);
		}
		//21:00:00
		if(timeDiffInLogin(SharedConstants.REFRESH_TIME_NINE_PM)){
			ninePMHandler();
			refreshedInLogin(SharedConstants.REFRESH_TIME_NINE_PM, NINE_PM);
		}
		//22:00:00
		if(timeDiffInLogin(SharedConstants.REFRESH_TIME_TEN_PM)){
			tenPMHandler();
			refreshedInLogin(SharedConstants.REFRESH_TIME_TEN_PM, TEN_PM);
		}
		
		/*if(!TimeUtil.isSameDay(TimeUtil.currentTimeMillis(), player.getMyRankConsume().getRefreshTime())){
			player.getMyPayAndConsume().refreshZeroAM();
			player.getMyRankConsume().refresh();
			player.getMyRankConsume().setRefreshTime(GameServerConfig.today_zero_Millis-SharedConstants.DAY_IN_MINILLS);
		}*/

	}
	
	/**
	 * 如果系统时间到了指定的刷新时间点</br>
	 * 为在线的玩家刷新数据并推送消息
	 */
	public void refresh(int jsonkey){
		if(isRefresh(jsonkey)){
			switch(jsonkey){
				case SharedConstants.REFRESH_TIME_FIVE_AM:
					fiveAMHandler();
					break;
				case SharedConstants.REFRESH_TIME_TWELVE_AM:
					twelveAMHandler();
					break;
				case SharedConstants.REFRESH_TIME_TWO_PM:
					twoPMHandler();
					break;
				case SharedConstants.REFRESH_TIME_EIGHT_PM:
					eightPMHandler();
					break;
				case SharedConstants.REFRESH_TIME_NINE_PM:
					ninePMHandler();
					break;
				case SharedConstants.REFRESH_TIME_TEN_PM:
					tenPMHandler();
					break;
			}
			refreshed(jsonkey);
		}
	}
}
