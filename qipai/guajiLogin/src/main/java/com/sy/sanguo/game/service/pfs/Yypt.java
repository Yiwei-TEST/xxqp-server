package com.sy.sanguo.game.service.pfs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.RoomcardOrder;
import com.sy.sanguo.game.service.BaseSdk;

public class Yypt extends BaseSdk {
	private static String payKey = "Higf44fadsg8#Uf$dgd";
	@Override
	public String payExecute() {
		int code = -1;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", code);
		try {

			String sign = this.getString("sign");
			if (StringUtils.isBlank(sign)) {
				result.put("msg", LangMsg.getMsg(LangMsg.code_3));
				return JacksonUtil.writeValueAsString(result);
			}

			if (!check(sign)) {
				result.put("msg", "sign错误");
				return JacksonUtil.writeValueAsString(result);
			}
			String orderId = this.getString("orderId");
			long userId = this.getLong("userId");
			int payBindId = this.getInt("payBindId");
			int cards = this.getInt("commonCards");
			int freeCards = this.getInt("freeCards");
			RoomcardOrder roomcardOrder = orderDao.getCardOrder(userId, orderId);
			if (roomcardOrder == null) {
				result.put("msg", "没有找到记录");
				return JacksonUtil.writeValueAsString(result);
			}

			if (roomcardOrder.getOrderStatus() != 0) {
				result.put("msg", "状态错误");
				return JacksonUtil.writeValueAsString(result);
			}

			if (cards != roomcardOrder.getCommonCards() || freeCards != roomcardOrder.getFreeCards()) {
				result.put("msg", "房卡信息错误");
				return JacksonUtil.writeValueAsString(result);
			}

			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				result.put("msg", "找不到该用户");
				return JacksonUtil.writeValueAsString(result);
			}

			int setPayBindId = 0;
			int isFirstPayBindId = 0;
			if (user.getPayBindId() == 0) {
				if (cards > 0) {				
					// 玩家没有绑定支付代理，设置当前为代理人
					setPayBindId = payBindId;
					user.setPayBindId(payBindId);
					//isFirstPayBindId = 1;
				}
			} else {
				// 有设置支付代理，这次支付算代理人的上面
				payBindId = user.getPayBindId();
			}
			
			if (cards > 0) {				
				if (orderDao.isFirstRecharge(user.getUserId()) <= 0) {
					isFirstPayBindId = 1;
				}
			}

			Map<String, Object> orderMap = new HashMap<String, Object>();
			orderMap.put("roleId", userId);
			orderMap.put("orderId", orderId);
			orderMap.put("registerBindAgencyId", user.getRegBindId());
			orderMap.put("rechargeBindAgencyId", payBindId);
			orderMap.put("isFirstPayBindId", isFirstPayBindId);
			orderMap.put("orderStatus", 1);
			int updateOrder = orderDao.updateCardOrder(orderMap);
			if (updateOrder == 0) {
				result.put("msg", "修改状态失败");
				return JacksonUtil.writeValueAsString(result);
			}

			int update = userDao.addUserCards(user, cards, freeCards, payBindId, CardSourceType.yypt_send_card);
			if (update == 0) {
				result.put("msg", "发放房卡失败,请重试");
				return JacksonUtil.writeValueAsString(result);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("Yypt pay err:", e);
			result.put("msg", "发货异常");
			return JacksonUtil.writeValueAsString(result);
		}
		result.put("code", 0);
		result.put("msg", "");
		return JacksonUtil.writeValueAsString(result);
	}

	private boolean check(String sign) {
		Object[] oArr = getRequest().getParameterMap().keySet().toArray();
		Arrays.sort(oArr);
		StringBuffer sb = new StringBuffer();
		for (Object o : oArr) {
			String key = o.toString();
			if (key.equals("sign")) {
				continue;
			}
			sb.append(getString(key));
		}
		sb.append(payKey);
		if (sign.equals(MD5Util.getStringMD5(sb.toString()))) {
			return true;
		}
		return false;
	}

	@Override
	public String loginExecute() {
		return null;
	}

}
