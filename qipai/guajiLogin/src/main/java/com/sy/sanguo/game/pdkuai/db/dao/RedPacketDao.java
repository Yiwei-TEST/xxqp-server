package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.pdkuai.db.bean.RedPacketMoneyInfo;
import com.sy.sanguo.game.pdkuai.db.bean.RedPacketRecord;

/**
 * 红包Dao
 * @author Ysy
 *
 */
public class RedPacketDao extends BaseDao {

	private static RedPacketDao _inst= new RedPacketDao();
	
	public static RedPacketDao getInstance() {
		return _inst;
	}
	
	/**
	 * 查询用户红包记录
	 */
	@SuppressWarnings("unchecked")
	public List<RedPacketRecord> userMoneyInfo(Long userId,int hbType){
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		sqlMap.put("hbType", hbType);
		List<RedPacketRecord> list=null;
		try {
			list=getSql().queryForList("redpacket.userMoneyInfo",sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("userMoneyInfo err ", e);
		}
		return list;
	}
	
	/**
	 * 查询所有红包记录
	 */
	@SuppressWarnings("unchecked")
	public List<RedPacketRecord> allMoneyInfo(Long userId,int hbType){
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		sqlMap.put("hbType", hbType);
		List<RedPacketRecord> list=null;
		try {
			list=getSql().queryForList("redpacket.allMoneyInfo",sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("allMoneyInfo err ", e);
		}
		return list;
	}
	
	/**
	 * 查询用户红包金额
	 */
	public RedPacketMoneyInfo userMoeny(Long  userId){	
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		RedPacketMoneyInfo redPacketMoneyInfo=null;
		sqlMap.put("userId", userId);
		try {
			redPacketMoneyInfo=(RedPacketMoneyInfo) getSql().queryForObject("redpacket.userMoeny", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("userMoeny err ", e);
		}
		return redPacketMoneyInfo;
	}
	
	/**
	 * 查询用户红包已兑换金额
	 */
	public double notMoney(Long userId){
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		double num=0;
		try {
			num=(double) getSql().queryForObject("redpacket.notMoney", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("notMoney err ", e);
		}
		return num;
	}
	
	/**
	 * 兑换红包
	 */
	public void updateExchange(Long userId,double money){
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		sqlMap.put("money", money);
		try {
			getSql().update("redpacket.updateExchange", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("updateExchange err ", e);
		}
	}
	
	/**
	 * 添加红包兑换记录
	 */
	public void addExchange(Long userId,double money,String wxname,String phone,String time){
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		sqlMap.put("money", money);
		sqlMap.put("wxname", wxname);
		sqlMap.put("phone", phone);
		sqlMap.put("time", time);
		try {
			getSql().insert("redpacket.addExchange", sqlMap);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			GameBackLogger.SYS_LOG.error("addExchange err ", e);
		}
	}
	
	/**
	 * 查询已兑换金额
	 */
	public double endMoney(Long userId){
		double Money=0;
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		try {
			Money=(double) getSql().queryForObject("redpacket.endMoney", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("endMoney err ", e);
		}
		return Money;
	}
	
	/**
	 * 排行榜查询
	 */
	@SuppressWarnings("unchecked")
	public List<HashMap<String,Object>> RedRanking(){
		List<HashMap<String,Object>> list=null;
		try {
			list=(List<HashMap<String,Object>>) getSql().queryForList("redpacket.RedRanking");
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("RedRanking err ", e);
		}
		return list;
	}
	
	
	/**
	 * 修改排行榜用户领奖状态
	 */
	public int updatePrizeFlag(Long userId,int prizeFlag){
		int num=0;
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		sqlMap.put("prizeFlag", prizeFlag);
		try {
			num=getSql().update("redpacket.updatePrizeFlag", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("updatePrizeFlag err ", e);
		}
		return num;
	}


	public int addDrawPrize(Map<String, Object> map) {
		int num = 0;
		try {
			Object o = getSql().insert("redpacket.addDrawPrize", map);
			if (o != null) {
				num = (int) o;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("addDrawPrize err ", e);
		}
		return num;
	}

	public List<RedPacketRecord> getDrawPrize(Map<String, Object> map) {
		List<RedPacketRecord> list=null;
		try {
			list  = getSql().queryForList("redpacket.selectDrawPrizeRecord", map);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}
