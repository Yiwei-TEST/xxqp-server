package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.OrderValidate;

public class OrderValiDaoImpl extends CommonDaoImpl {

	public void insert(OrderValidate orderInfo) throws SQLException {
		this.getSqlMapClient().insert("orderVali.insert", orderInfo);
	}

	public OrderValidate getOne(String orderid) throws SQLException {
		OrderValidate orderInfo = null;
		try {
			orderInfo = (OrderValidate) this.getSqlMapClient().queryForObject("orderVali.getOne", orderid);
		} catch (SQLException e) {
			throw e;
		}
		return orderInfo;
	}
	
	public OrderValidate getOne(String flatId, String pf, int amount, String payChannel) throws SQLException {
		return getOne(flatId, pf, amount, payChannel, 0);
	}

	public OrderValidate getOne(String flatId, String pf, int amount, String payChannel,int server_id) throws SQLException {
		OrderValidate orderInfo = null;
		Map<String, Object> map = new HashMap<String, Object>();
		if (!StringUtils.isBlank(flatId)) {
			map.put("flat_id", flatId);

		}
		if (!StringUtils.isBlank(pf)) {
			map.put("pf", pf);

		}
		if (!StringUtils.isBlank(payChannel)) {
			map.put("pay_channel", payChannel);

		}
		if (amount != 0) {
			map.put("amount", amount);
			
		}
		if (server_id != 0) {
			map.put("server_id", server_id);

		}
		try {
			orderInfo = (OrderValidate) this.getSqlMapClient().queryForObject("orderVali.getOneByMap", map);
		} catch (SQLException e) {
			throw e;
		}
		return orderInfo;
	}

	public void validate(String orderid) throws SQLException {
		this.getSqlMapClient().update("orderVali.validate", orderid);
	}
}
