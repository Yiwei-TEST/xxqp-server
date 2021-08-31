package com.sy599.game.db.dao;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.InviteQueQiao;
import com.sy599.game.db.bean.InviteQueQiaoWithSendMsg;
import com.sy599.game.db.bean.activityRecord.ActivityReward;
import com.sy599.game.db.bean.Activity;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;

import java.sql.SQLException;
import java.util.*;

public class ActivityDao extends BaseDao {

	private static ActivityDao _inst = new ActivityDao();

	public static ActivityDao getInstance() {
		return _inst;
	}
	
	/**
	 * 修改福袋数据
	 *
	 * @param cards
	 * @return
	 */
	public int updateFudai(Player player, int cards) {
		
		// userId, username, sex, inviteeCount, invitorId, feedbackCount, openCount, activityStartTime, prizeFlag
		
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", player.getUserId());
		paramMap.put("feedbackCount", cards);
		paramMap.put("username", player.getName());
		paramMap.put("sex", player.getSex());
		paramMap.put("inviteeCount", 0);
		paramMap.put("invitorId", 0);
		paramMap.put("openCount", 0);
		paramMap.put("activityStartTime", new Date());
		paramMap.put("prizeFlag", 0);

		try {
			return getSqlLoginClient().update("activity.updateFudai", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("activity.updateFudai err:", e);
		}
		
		return 0;
	}
	
	/**
	 * 增加新年红包记录
	 * 
	 * @param paramMap
	 */
	public void addHbFafangRecord(Map<String, Object> paramMap) {
		try {
			getSqlLoginClient().insert("activity.addHbFafangRecord", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("activity.addHbFafangRecord err:", e);
		}
	}

	/**
	 * 增加玩家红包总额
	 *
	 * @param money
	 */
	public int insertUserTotalMoney(Player player, double money) {
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", player.getUserId());
		paramMap.put("cdk", "");
		paramMap.put("extend", "");
		paramMap.put("totalMoney", money);
		paramMap.put("myConsume", "");
		paramMap.put("shengMoney", 0);
		paramMap.put("prizeFlag", 0);
		paramMap.put("name", player.getName());

		try {
			return getSqlLoginClient().update("activity.insertUserTotalMoney", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("activity.insertUserTotalMoney err:", e);
		}
		
		return 0;
	}
	
	/**
	 * 增加玩家领取活动奖励
	 * @param activityReward
	 */
	public void addActivityReward(ActivityReward activityReward){
		try{
			this.getSqlLoginClient().insert("activity.addActivityReward", activityReward);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.addActivityReward err:", e);
		}
	}

	public List<Activity> loadActiveConfig() throws Exception {
		return (List<Activity>)this.getSqlLoginClient().queryForList("activity.loadActiveConfig");
	}

	public InviteQueQiao selectIsAllow(Long userId){
		try{
			Map<String, Object> map = new HashMap<>(2);
			map.put("userId", userId);
			return (InviteQueQiao)this.getSqlLoginClient().queryForObject("activity.selectIsAllow", map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.selectIsAllow err:", e);
		}
		return null;
	}

	public List<InviteQueQiao> selectBySendIdOrAcceptId(Long userId){
		try{
			Map<String, Object> map = new HashMap<>(2);
			map.put("userId", userId);
			return this.getSqlLoginClient().queryForList("activity.selectBySendIdOrAcceptId", map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.selectBySendIdOrAcceptId err:", e);
		}
		return Collections.emptyList();
	}

	public void deleteWhereTeamSuccess(long sendId){
		try{
			Map<String, Object> map = new HashMap<>(2);
			map.put("sendId", sendId);
			this.getSqlLoginClient().delete("activity.deleteWhereTeamSuccess",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.deleteWhereTeamSuccess err:", e);
		}
	}

	public void deleteOneInvite(long sendId,long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("sendId", sendId);
			map.put("acceptId",acceptId);
			this.getSqlLoginClient().delete("activity.deleteOneInvite",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.deleteOneInvite err:", e);
		}
	}

	public Integer insertInvite(long sendId,long acceptId,long sendTime){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("sendId", sendId);
			map.put("acceptId",acceptId);
			map.put("sendTime", sendTime);
			return (Integer) this.getSqlLoginClient().insert("activity.insertInvite", map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.insertInvite err:", e);
		}
		return 0;
	}

	public List<InviteQueQiao> select10MinInvite(Long sendId,Long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("sendId", sendId);
			map.put("acceptId",acceptId);
			map.put("sendTime", System.currentTimeMillis()-600000);
			return (List<InviteQueQiao>)this.getSqlLoginClient().queryForList("activity.select10MinInvite",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.selectBySendIdOrAcceptId err:", e);
		}
		return Collections.emptyList();
	}

	public int updateAllowTeamInvite(Long sendId,Long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("sendId", sendId);
			map.put("acceptId",acceptId);
			map.put("sendTime", TimeUtil.getNowDayZeroMS());
			return this.getSqlLoginClient().update("activity.updateAllowTeamInvite",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.updateAllowTeamInvite err:", e);
		}
		return 0;
	}

	public List<InviteQueQiaoWithSendMsg> selectAllInvite(Long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("acceptId",acceptId);
			return this.getSqlLoginClient().queryForList("activity.selectAllInvite",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.selectAllInvite err:", e);
		}
		return Collections.emptyList();
	}

	public int updateIsRead(Long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(2);
			map.put("acceptId",acceptId);
			map.put("sendTime", TimeUtil.getNowDayZeroMS());
			return this.getSqlLoginClient().update("activity.updateIsRead",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.updateIsRead err:", e);
		}
		return 0;
	}

	public int updateAllowIsRead(Long sendId,Long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("sendId",sendId);
			map.put("acceptId",acceptId);
			return this.getSqlLoginClient().update("activity.updateAllowIsRead",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.updateAllowIsRead err:", e);
		}
		return 0;
	}

	public int updateReadQueQiaoMsg(Long sendId,Long acceptId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("sendId",sendId);
			map.put("acceptId",acceptId);
			return this.getSqlLoginClient().update("activity.updateReadQueQiaoMsg",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.updateReadQueQiaoMsg err:", e);
		}
		return 0;
	}

	public Map<String,String> selectTeamateInf(Long userId){
		try{
			Map<String, Object> map = new HashMap<>(3);
			map.put("userId",userId);
			return (Map<String,String>)this.getSqlLoginClient().queryForObject("activity.selectTeamateInf",map);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.selectTeamateInf err:", e);
		}
		return Collections.emptyMap();
	}

}
