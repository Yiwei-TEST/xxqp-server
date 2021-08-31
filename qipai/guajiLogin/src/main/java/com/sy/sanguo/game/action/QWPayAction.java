package com.sy.sanguo.game.action;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.struts.StringResultType;
import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.server.Server;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.HttpRequester;
import com.sy.sanguo.common.util.HttpRespons;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.OrderInfo;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;

/**
 * QQ玩吧支付回调
 * @author zhoufan
 */
public class QWPayAction extends GameStrutsAction {
	
	private static String signstr = "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3";
	
	private static final long serialVersionUID = -3788678829039402523L;
	private OrderDaoImpl orderDao;
	private OrderValiDaoImpl orderValiDao;
	private String result = "";
	
	public String execute() throws Exception {
		
        
        
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	
	protected int newpay(String platform, String orderid, String uid, int serverId, PayBean bean) {
		int order_amount = bean.getAmount()*10;
		// 处理uid
		uid += "_" + serverId;
		uid += "_" + platform;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("order_id", orderid);
			map.put("flat_id", uid);
			OrderInfo info = orderDao.getOne(map);
			// 已经充值
			if (info != null) {
				return 1;
			}
			// 验证游戏服务器
			if (!GameServerManager.getInstance().isCorrectServerId(serverId)) {
				GameBackLogger.SYS_LOG.info("#pay error--serverId "+serverId+" is not correctly");
				return 2;
			}
			// 开始充值流程
			try{
				Server server = GameServerManager.getInstance().getServer(serverId);
				Map<String, String> payParams = new HashMap<String, String>();
				payParams.put("type", "15");
				payParams.put("funcType", "2");
				payParams.put("flatId", uid);
				payParams.put("orderId", orderid);
				payParams.put("itemId", String.valueOf(bean.getId()));
				payParams.put("flatId", uid);
				payParams.put("time", String.valueOf(System.currentTimeMillis()));
				String verifystr = payParams.get("time") + signstr + payParams.get("flatId");
				payParams.put("sign", MD5Util.getStringMD5(verifystr));
				HttpRespons res = HttpRequester.sendPost(server.getHost(), payParams);
				String resp = res.getContent();
				if(!StringUtils.isBlank(resp)){
					JSONObject json = JSONObject.parseObject(resp);
					int code = json.getIntValue("code");
		    		if(code == 0){
		    			// 写入数据库
						info = new OrderInfo();
						info.setFlat_id(String.valueOf(uid));
						info.setItem_id(bean.getId());
						info.setItem_num(1);
						info.setOrder_amount(order_amount);
						info.setOrder_id(orderid);
						info.setPlatform(platform);
						info.setServer_id(String.valueOf(serverId));
						orderDao.insert(info);
		    		}else{
		    			GameBackLogger.SYS_LOG.info("BaseSdk pay code not 0");
		    			errorlog(platform, orderid, order_amount, uid, "res code:"+code);
		    			return 3;
		    		}
	    		}
			}catch(Exception e){
				GameBackLogger.SYS_LOG.error("BaseSdk pay error",e);
				exception(platform, orderid, order_amount, uid, e);
				return 3;
			}
			return 0;
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("BaseSdk pay error",e);
			exception(platform, orderid, order_amount, uid, e);
			return 3;
		}
	}
	
	protected void exception(String platform, String orderid, int amount,
		String uid, Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append("#pay error--pf::").append(platform);
		sb.append(" orderId::").append(orderid);
		sb.append(" amount::").append(amount);
		sb.append(" userId::").append(uid);
		GameBackLogger.MONITOR_LOG.error(sb.toString(), e);
	}
	
	protected void errorlog(String platform, String orderid, int amount,String uid, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("#pay error--pf::").append(platform);
		sb.append(" orderId::").append(orderid);
		sb.append(" amount::").append(amount);
		sb.append(" userId::").append(uid);
		sb.append(" msg::").append(msg);
		GameBackLogger.SYS_LOG.info(sb.toString());
	}
	
	public OrderValiDaoImpl getOrderValiDao() {
		return orderValiDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}

	public OrderDaoImpl getOrderDao() {
		return orderDao;
	}

	public void setOrderDao(OrderDaoImpl orderDao) {
		this.orderDao = orderDao;
	}

	public String getResult() {
		return result;
	}
	
}
