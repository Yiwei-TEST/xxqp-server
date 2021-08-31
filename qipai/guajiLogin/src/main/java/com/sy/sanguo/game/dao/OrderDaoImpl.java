package com.sy.sanguo.game.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.OrderInfo;
import com.sy.sanguo.game.bean.RoomcardOrder;

public class OrderDaoImpl extends CommonDaoImpl {

	public void insertRoomCards(RoomcardOrder orderInfo) throws SQLException {
		this.getSqlMapClient().insert("order.insertCardOrder", orderInfo);
	}

	public RoomcardOrder getCardOrder(long userId, String orderId) throws SQLException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("roleId", userId);
		param.put("orderId", orderId);
		RoomcardOrder orderInfo = null;
		try {
			orderInfo = (RoomcardOrder) this.getSqlMapClient().queryForObject("order.getOneCardOrder", param);
		} catch (SQLException e) {
			throw e;
		}
		return orderInfo;
	}

	public int updateCardOrder(Map<String, Object> param) throws SQLException {
		return this.getSqlMapClient().update("order.updateCardOrder", param);
	}
	
	public int updateStatisticsInf(Map<String, Object> param) throws SQLException {
		return this.getSqlMapClient().update("order.updateStatisticsInf", param);
	}

	// ////////////////////////////////////////////////////////////////////////////////

	public void insert(OrderInfo orderInfo) throws SQLException {
		if (orderInfo.getPayMoney()==null){
			orderInfo.setPayMoney(new BigDecimal(orderInfo.getOrder_amount()/(10.0)).setScale(2,BigDecimal.ROUND_HALF_UP));
		}
		orderInfo.setCreate_time(new Date());
		this.getSqlMapClient().insert("order.insert", orderInfo);
	}

	public OrderInfo getOne(Map<String, Object> param) throws SQLException {
		OrderInfo orderInfo = null;
		try {
			orderInfo = (OrderInfo) this.getSqlMapClient().queryForObject("order.getOne", param);
		} catch (SQLException e) {
			throw e;
		}
		return orderInfo;
	}

	public int isFirstRecharge(Long roleId) throws SQLException {
		Object o = this.getSqlMapClient().queryForObject("order.isFirstRecharge", roleId);
		if (o == null) {
			return 0;
		}
		return (Integer) o;
	}

	public int isFirstRechargeGold(Long roleId) throws SQLException {
		Object o = this.getSqlMapClient().queryForObject("order.isFirstRechargeGold", roleId);
		if (o == null) {
			return 0;
		}
		return (Integer) o;
	}

	public List<Map<String,String>> selectMyPlayersByDatetime(Map<String,Object> params) throws SQLException {
		List<Map<String,String>> o = this.getSqlMapClient().queryForList("order.selectMyPlayersByDatetime",params);

		return o;
	}

	public List<Map<String,String>> selectMyPlayersDetailByDatetime(Map<String,Object> params) throws SQLException {
		List<Map<String,String>> o = this.getSqlMapClient().queryForList("order.selectMyPlayersDetailByDatetime", params);

		return o;
	}
}
