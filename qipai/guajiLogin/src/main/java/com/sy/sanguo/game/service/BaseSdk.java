package com.sy.sanguo.game.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.*;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.dao.*;
import com.sy.sanguo.game.pdkuai.action.PdkAction;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.game.pdkuai.db.dao.GameUserDao;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import com.sy.sanguo.game.staticdata.ReYunStaticData;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.math.NumberUtils;

public abstract class BaseSdk implements IMobileSdk {
	private static String signstr = "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3";
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected OrderDaoImpl orderDao;
	protected OrderValiDaoImpl orderValiDao;
	protected GameUserDao gameUserDao;
	protected String pf;
	protected String payType;
	protected SdkUrlConfig sdkConfig;
	protected UserDaoImpl userDao;
	protected RoomCardDaoImpl roomCardDao;
	private String loginInfo;
	private String opt;
	private String sdkId;

	private String ext;

	@Override
	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getExt() {
		return ext;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public void setSdkId(String sdkId) {
		this.sdkId = sdkId;
	}

	@Override
	public String getSdkId() {
		return sdkId;
	}

	@Override
	public Map<String, Object> refreshRole(RegInfo regInfo, String info) throws Exception {
		return null;
	}

	@Override
	public void createRole(RegInfo regInfo, String info) throws Exception {

	}

	@Override
	public void setOpt(String opt) {
		this.opt = opt;
	}

	public String getOpt() {
		return opt;
	}

	public void setPfConfig() {

	}

	public void setLoginInfo(String loginInfo) {
		this.loginInfo = loginInfo;
	}

	@Override
	public String getLoginInfo() {
		return loginInfo;
	}

	@Override
	public String common() {
		return null;
	}

	public String ovali() {
		return null;
	}

	public String getInfo(long userId, String key) {
		return getPlayerInfo(userId, key);
	}

	public String upInfo(long userId, String key) {
		return null;
	}

	public String upInfo(long userId, String key, String value) {
		if (updatePlayerInfo(userId, key, value)) {
			return "1";
		}
		return "0";
	}

	protected String getPlayerInfo(long userId, String key) {
		RegInfo info = getUser(userId);
		if (info != null) {
			JsonWrapper wrapper = new JsonWrapper(info.getInfo());
			if (StringUtils.isBlank(key)) {
				return wrapper.toString();
			} else {
				if (wrapper.hasKey(key))
					return wrapper.getString(key);
			}

		}
		return null;
	}

	protected boolean updatePlayerInfo(long userId, String key, String value) {
		RegInfo info = getUser(userId);
		if (info != null) {
			JsonWrapper wrapper = new JsonWrapper(info.getInfo());
			wrapper.putString(key, value);
			Map<String, Object> modify = new HashMap<String, Object>();
			modify.put("info", wrapper.toString());
			try {
				this.userDao.updateUser(info, modify);
				return true;
			} catch (SQLException e) {
				GameBackLogger.SYS_LOG.error("updatePlayerInfo err ", e);
			}
		}
		return false;

	}

	public void upInfo() {
	}

	protected RegInfo getUser(long userId) {
		if (this.userDao == null) {
			return null;
		}
		try {

			return this.userDao.getUser(userId);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("getUser err:" + userId, e);
		}
		return null;
	}

	/**
	 * 下单验证通用消息处理
	 * 
	 * @return
	 */
	protected OvaliMsg ovaliCom() {
		OvaliMsg msg = new OvaliMsg();
		try {
			String flat_id = getRequest().getParameter("flat_id");
			String server_id = getRequest().getParameter("server_id");
			String pf = getRequest().getParameter("p");
			int itemid = Integer.parseInt(getRequest().getParameter("itemid"));
			String sign = getRequest().getParameter("k");
			String secret = "mwFLeKLzNoL46dDn0vE2";
			String c = getRequest().getParameter("c");

			if (StringUtils.isBlank(flat_id)) {
				msg.setCode(1);
				msg.setMsg("param flat_id error");
				return msg;
			}else{
				flat_id = flat_id.replace(" ","+");
			}
			if (StringUtils.isBlank(pf)) {
				msg.setCode(1);
				msg.setMsg("param pf error");
				return msg;
			}
			if (StringUtils.isBlank(sign)) {
				msg.setCode(1);
				msg.setMsg("param sign error");
				return msg;
			}

			StringBuilder md5 = new StringBuilder();
			md5.append(flat_id);
			md5.append(server_id);
			md5.append(pf);
			md5.append(itemid);
			md5.append(secret);

			if (!MD5Util.getStringMD5(md5.toString()).equals(sign)) {
				msg.setCode(1);
				msg.setMsg("param md5 err");
				return msg;
			}

			PayBean bean = GameServerManager.getInstance().getPayBean(itemid);
			if (bean == null) {
				msg.setCode(1);
				msg.setMsg("param itemId cannot find");
				return msg;
			}
			msg.setPayItem(bean);
			long now = TimeUtil.currentTimeMillis();
			long time = now / 1000 + MathUtil.mt_rand(100, 999);
			String order_id = now + flat_id + server_id + pf + MathUtil.mt_rand(1000, 9999);
			order_id = MD5Util.getStringMD5(order_id).substring(8, 24);
			order_id += Long.toHexString(time);

			OrderValidate ov = new OrderValidate();
			ov.setFlat_id(flat_id);
			ov.setOrder_id(order_id);
			ov.setServer_id(server_id);
			ov.setPf(pf);
			ov.setItem_id(itemid);
			ov.setAmount(bean.getAmount());
			if (!StringUtils.isBlank(c)) {
				ov.setPay_channel(c);
			}
			if (!StringUtils.isBlank(payType)) {
				ov.setPay_channel(ov.getPay_channel() + "," + payType);
			}

			msg.setOv(ov);
			ovaliCom1(msg);
			buildOrderVali(ov);
			orderValiDao.insert(ov);
		} catch (Exception e) {
			msg.setCode(999);
			msg.setMsg(e.getMessage());
			GameBackLogger.SYS_LOG.error(pf + ".exception", e);
		}
		return msg;
	}

	protected void ovaliCom1(OvaliMsg msg) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 获取商品项
	 *
	 * @return
	 */
	protected PayBean loadItem(){
		int itemid = NumberUtils.toInt(getRequest().getParameter("itemid"),-1);
		String total_fee = this.getString("total_fee");
		PayBean bean = GameServerManager.getInstance().getPayBean(itemid);
		if(bean==null||!NumberUtils.isDigits(total_fee)){
			return null;
		}

		if (bean.getAmount()*100 != NumberUtils.toInt(total_fee,-1)) {
			if (!GameServerManager.getInstance().hasSameAmountPayItem()) {
				bean = GameServerManager.getInstance().getPayBeanByAmount(Integer.parseInt(total_fee) / 100);
				if (bean == null) {
					return null;
				}
			}else{
				return null;
			}
		}
		return bean;
	}

	/**
	 * 下单验证通用消息处理
	 * 
	 * @return
	 */
	protected OvaliMsg ovaliComMsg() {
		OvaliMsg msg = new OvaliMsg();
		try {
			Map<String,String> params=UrlParamUtil.getParameters(getRequest());

			LogUtil.i("create order params:"+params);

			String flat_id = getRequest().getParameter("flat_id");
			String server_id = getRequest().getParameter("server_id");
			String pf = getRequest().getParameter("p");
			int itemid = Integer.parseInt(getRequest().getParameter("itemid"));
			String sign = getRequest().getParameter("k");
			String secret = "mwFLeKLzNoL46dDn0vE2";
			String c = getRequest().getParameter("c");

			// 获得被代充人的id
			String agencyUserId = getString("agencyUserId");
            String userId = getString("userId");//玩家Id
			String total_fee = this.getString("total_fee");

			if (StringUtils.isBlank(flat_id)) {
				msg.setCode(1);
				msg.setMsg("param flat_id error");
				return msg;
			}else{
				flat_id = flat_id.replace(" ","+");
			}

			if (StringUtils.isBlank(pf)) {
				msg.setCode(1);
				msg.setMsg("param pf error");
				return msg;
			}
			if (StringUtils.isBlank(sign)) {
				msg.setCode(1);
				msg.setMsg("param sign error");
				return msg;
			}

            StringBuilder md5 = new StringBuilder();
            md5.append(flat_id);
            md5.append(server_id);
            md5.append(pf);
            md5.append(itemid);
            md5.append(secret);

            String referer = request.getHeader("Referer");
            if (StringUtils.contains(referer,"/h5/pay/index.jsp")&&StringUtils.contains(referer,getRequest().getScheme())){

            }else if (!MD5Util.getStringMD5(md5.toString()).equals(sign)) {
				msg.setCode(1);
				msg.setMsg("param md5 err");
				return msg;
			}

			PayBean bean = GameServerManager.getInstance().getPayBean(itemid);
			if (bean == null) {
				msg.setCode(1);
				msg.setMsg("param itemId cannot find");
				return msg;
			}
			if (StringUtils.isBlank(total_fee)) {
				msg.setCode(1);
				msg.setMsg("param total_fee err");
				return msg;
			}else if (bean.getAmount()*100 != Integer.parseInt(total_fee)){
			    if (!GameServerManager.getInstance().hasSameAmountPayItem()){
                    bean = GameServerManager.getInstance().getPayBeanByAmount(Integer.parseInt(total_fee)/100);
                    if (bean==null){
                        msg.setCode(1);
                        msg.setMsg("param total_fee err");
                        return msg;
                    }
                }else{
					msg.setCode(1);
					msg.setMsg("param total_fee err");
					return msg;
				}
            }

			msg.setPayItem(bean);
			long now = TimeUtil.currentTimeMillis();
			long time = now / 1000 + MathUtil.mt_rand(100, 999);
			String order_id = now + flat_id + server_id + pf + MathUtil.mt_rand(1000, 9999);
			order_id = MD5Util.getStringMD5(order_id).substring(8, 24);
			order_id += Long.toHexString(time);

			OrderValidate ov = new OrderValidate();
            long tempUserId;

            if (NumberUtils.isDigits(userId) && (tempUserId = Long.parseLong(userId)) > 0) {
                ov.setUserId(tempUserId);
                RegInfo regInfo = userDao.getUser(tempUserId);
                if (regInfo == null) {
                    msg.setCode(1);
                    msg.setMsg("ID错误");
                    return msg;
                }
                if (!checkSpecialPay(bean.getId(), regInfo)) {
					msg.setCode(1);
					msg.setMsg("特殊充值验证失败");
					return msg;
				}
            } else {
                RegInfo userInfo = userDao.getUser(flat_id, pf);
                if (userInfo != null) {
                    ov.setUserId(userInfo.getUserId());
					if (!checkSpecialPay(bean.getId(), userInfo)) {
						msg.setCode(1);
						msg.setMsg("特殊充值验证失败");
						return msg;
					}
                } else {
                    msg.setCode(1);
                    msg.setMsg("ID错误");
                    return msg;
                }
            }
			ov.setFlat_id(flat_id);
			ov.setOrder_id(order_id);
			ov.setServer_id(server_id);
			ov.setPf(pf);
			ov.setItem_id(bean.getId());
			ov.setAmount(bean.getAmount());
			if (!StringUtils.isBlank(c)) {
				ov.setPay_channel(c);
			}
			if (!StringUtils.isBlank(payType)) {
				ov.setPay_channel(ov.getPay_channel() + "," + payType);
			}

			// 添加被代充人的id信息
			if (!StringUtils.isEmpty(agencyUserId)) {
				long auid = Long.parseLong(agencyUserId);
                if (auid > 0L && userDao.getUser(auid) == null) {
                    msg.setCode(1);
                    msg.setMsg("ID错误1");
                    return msg;
                }
				ov.setAgencyUserId(auid);
			}
			buildOrderVali(ov);
			orderValiDao.insert(ov);
			msg.setOv(ov);

		} catch (Exception e) {
			msg.setCode(999);
			msg.setMsg(e.getMessage());
			GameBackLogger.SYS_LOG.error(pf + ".exception", e);
		}
		return msg;
	}

	private boolean checkSpecialPay(int itemid, RegInfo regInfo) throws SQLException {
		String payRemoveBindStr = PdkAction.getSpecialPay("payRemoveBind");
		String goldFirstCharge = PdkAction.getSpecialPay("goldFirstCharge");
		if (goldFirstCharge.contains(itemid+"")&&orderDao.isFirstRechargeGold(regInfo.getUserId())>0) {
			LogUtil.i("specialPay-->gold-->" + regInfo.getUserId());
			return false;
		}
		Map<String, Object> result = new HashMap<>();
		if (payRemoveBindStr.contains(itemid+"")&&!PdkAction.canRemoveBind(regInfo, result)) {
			String resInfo = (String)result.get("msg");
			LogUtil.i("specialPay-->payRemoveBind-->" + regInfo.getUserId()+",msg:"+resInfo);
			return false;
		}
		return true;
	}

	protected void buildOrderVali(OrderValidate ov) {

	}

	@Override
	public String callback(HttpServletResponse response) {
		return null;
	}

	protected String getString(String param, String def) {
		String value = getString(param);
		if (StringUtils.isBlank(value)) {
			return def;
		}
		return value;
	}

	protected String getString(String param) {
		return getRequest().getParameter(param);
	}

	protected Integer getInt(String param) {
		return Integer.parseInt(getString(param));
	}

	protected Long getLong(String param) {
		return Long.parseLong(getString(param));
	}

	public void loginH5Redirect() {
	}

	public String roleVali() {
		return "";
	}

	protected HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void buildResultMsg(Map<String, Object> map) {
		try {
			this.response.getWriter().write(JacksonUtil.writeValueAsString(map));
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("buildResultMsg", e);
		}
	}

	public void setOrderDao(OrderDaoImpl orderDao) {
		this.orderDao = orderDao;
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}

	public void setPf(String pf) {
		this.pf = pf;
	}

	public String getPf() {
		return this.pf;
	}

	protected int payCards(RegInfo user,OrderValidate ov, String orderid, PayBean bean) {
        return payCards(user, ov, orderid, bean, null);
	}

	/**
	 * @param user
	 * @param orderid
	 * @param bean
	 * @param extend
	 * @return 0成功 1充值重复 2找不到玩家 3游戏服务器发元宝失败
	 */
    protected int payCards(RegInfo user, OrderValidate ov, String orderid, PayBean bean, String extend) {
		int order_amount = bean.getAmount() * 10;
		try {
			if (user == null) {
				return 2;
			}

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("order_id", orderid);
			map.put("flat_id", user.getFlatId());
			OrderInfo info = orderDao.getOne(map);
			// 已经充值
			if (info != null) {
				return 1;
			}

            int yuanbao = bean.getYuanbao() + bean.getSpecialGive();
			if (bean.isDouble()) {
				yuanbao = yuanbao * 2;
			}else if(bean.getId()>=1&&bean.getId()<=9){
				int ratio=NumberUtils.toInt(PropertiesCacheUtil.getValueOrDefault("firstPayDouble","1",Constants.GAME_FILE),1);
				if (ratio>1){
					boolean isFirstPay = UserDao.getInstance().isFirstPay(user.getUserId(),1,9);
					if (isFirstPay){
						yuanbao=yuanbao*ratio;
					}
					LogUtil.i("first pay ratio="+ratio+",isFirstPay="+isFirstPay+",userId="+ user.getUserId()+",itemId="+bean.getId()+",money(yuan)="+bean.getAmount());
				}

                String firstPayGive = PropertiesCacheUtil.getValue("first_pay_give",Constants.GAME_FILE);
				if (StringUtils.isNotBlank(firstPayGive)){
				    HashMap<String,Object> extendMap = UserDao.getInstance().queryUserExtend(String.valueOf(user.getUserId()),200);
				    if (extendMap==null){
				        UserExtend userExtend = new UserExtend();
                        userExtend.setMsgState("1");
                        userExtend.setCreatedTime(new Date());
                        userExtend.setModifiedTime(userExtend.getCreatedTime());
                        userExtend.setMsgDesc(firstPayGive);
                        userExtend.setMsgKey("firstPayGive");
                        userExtend.setMsgType(200);
                        userExtend.setUserId(String.valueOf(user.getUserId()));
                        userExtend.setMsgValue(firstPayGive);
                        UserDao.getInstance().saveUserExtend(userExtend);
                    }
                }

//				String firstPayGive = PropertiesCacheUtil.getValue("first_pay_give",Constants.GAME_FILE);
//				if (StringUtils.isNotBlank(firstPayGive)){
//					boolean isFirstPay = UserDao.getInstance().isFirstPay(user.getUserId(),1,9);
//					if (isFirstPay){
//						String[] strs = firstPayGive.split(",");
//						LogUtil.i("firstPayGive:userId="+user.getUserId()+",msg="+firstPayGive);
//						for (String str:strs){
//							if (StringUtils.isNotBlank(str)){
//								if (str.startsWith("cards_")){
//									yuanbao+=Integer.parseInt(str.substring(6));
//								}else if (str.startsWith("gold_")){
//									HashMap<String,Object> goldMap = new HashMap<>();
//									goldMap.put("userId",String.valueOf(user.getUserId()));
//									goldMap.put("userName",user.getName());
//									goldMap.put("userNickname",user.getName());
//									goldMap.put("playCount","0");
//									goldMap.put("playCountWin","0");
//									goldMap.put("playCountLose","0");
//									goldMap.put("playCountEven","0");
//									int freeGold = Integer.parseInt(str.substring(5));
//									goldMap.put("freeGold",freeGold);
//									goldMap.put("Gold",0);
//									goldMap.put("usedGold","0");
//									goldMap.put("vipexp","0");
//									goldMap.put("exp","0");
//									goldMap.put("sex",user.getSex());
//									goldMap.put("signature","");
//									goldMap.put("headimgurl",user.getHeadimgurl());
//									goldMap.put("headimgraw",user.getHeadimgraw());
//									goldMap.put("extend","");
//									goldMap.put("regTime",CommonUtil.dateTimeToString());
//									goldMap.put("lastLoginTime",CommonUtil.dateTimeToString());
//									userDao.saveOrUpdateUserGoldInfo(goldMap);
//									GameUtil.sendPay(user.getEnterServer(),user.getUserId(),0,freeGold,null,"2");
//								}
//							}
//						}
//					}
//				}
			}

			int update;
			if (bean.getId()>1000) {
				// 充值金币
			    int isFirstRechargeGold = orderDao.isFirstRechargeGold(user.getUserId());
			    if (isFirstRechargeGold<=0) {
			    	// 首充送88888
			    	String goldFirstChargeStr = PdkAction.getSpecialPay("goldFirstCharge");
			    	if (!StringUtils.isBlank(goldFirstChargeStr)) {
						String[] strs = goldFirstChargeStr.split(",");
						yuanbao+= Integer.parseInt(strs[strs.length-1]);
					}
                }
				update = userDao.addUserGold(user, yuanbao, 0, user.getPayBindId());
			} else {
				Map<String, Object> modify = null;
				String payRemoveBindStr = PdkAction.getSpecialPay("payRemoveBind");
				if (payRemoveBindStr.contains(bean.getId()+"")) {
					modify = new HashMap<>();
					modify.put("payRemoveBind", "1");
				}
				update = userDao.addUserCards(user, yuanbao, 0, user.getPayBindId(), modify, CardSourceType.user_pay);
			}

			if (update == 0) {
				return 3;
			}

			info = new OrderInfo();
			info.setFlat_id(user.getFlatId());
			info.setItem_id(bean.getId());
			info.setItem_num(yuanbao);
			info.setOrder_amount(order_amount);
			info.setOrder_id(orderid);
			String payChannel=ov.getPay_channel();
			if (StringUtils.contains(payChannel,",")){
				payChannel = payChannel.split(",")[0];
			}
			if ("null".equals(payChannel)){
				payChannel="";
			}
			info.setPayType(payChannel);
			info.setPlatform(ov.getPf());
            String agencyPf = null;
			if (roomCardDao!=null){
				HashMap<String,Object> agencyInfo = roomCardDao.queryAgencyByAgencyId(user.getPayBindId());
				if (agencyInfo!=null&&agencyInfo.size()>0){
					agencyPf=String.valueOf(agencyInfo.get("pf"));
					if (StringUtils.isNotBlank(agencyPf)&&(!"null".equalsIgnoreCase(agencyPf))){
						info.setPlatform(ov.getPf()+"_"+agencyPf);
					}else{
                        agencyPf = null;
                    }
				}
			}

			info.setUserId(user.getUserId());

			if (checkOrderAgencyId()){
				info.setServer_id(String.valueOf(user.getPayBindId()));
			}else{
				info.setServer_id("0");
			}
            if (bean.getId()>1000) {
                if (!StringUtils.isBlank(extend)) {
                    extend = extend + ",gold";
                } else {
                    extend = "gold";
                }
            }
			if (!StringUtils.isBlank(extend)) {
				info.setExtend(extend);

			}
			info.setPayPf(agencyPf==null?getPf():(getPf()+"_"+agencyPf));
			orderDao.insert(info);
			String payRemoveBindStr = PdkAction.getSpecialPay("payRemoveBind");
			if (payRemoveBindStr.contains(bean.getId()+"")) {
				// 解除绑定
				PdkAction.removeBind(user);
				userDao.removeBindInfo(user);
			}
			PfSdkConfig config = ReYunStaticData.getYiwanConfigByChannel(ov.getPf());
			if (config != null && config.isLog()) {
				// 热云统计
				sendReYun(user, config.getReyunAppId(), orderid, bean);
				GameBackLogger.SYS_LOG.info("newpay sendlog-->" + user.getPf() + " channel:");
			}

			return 0;
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("BaseSdk pay error", e);
			exception(user.getPf(), orderid, order_amount, user.getFlatId(), e);
			return 3;
		}
	}

	/**
	 * 热云统计
	 * 
	 * @param info
	 * @param reyunAppId
	 * @param orderId
	 * @param item
	 */
	private static void sendReYun(RegInfo info, String reyunAppId, String orderId, PayBean item) {
		JSONObject content = new JSONObject();
		content.put("deviceid", "unknown");
		content.put("transactionid", orderId);
		content.put("paymenttype", "unknown");
		content.put("currencytype", "CNY");
		content.put("currencyamount", item.getAmount() + "");
		content.put("virtualcoinamount", item.getYuanbao() + "");
		content.put("iapname", item.getName());
		content.put("iapamount", 1 + "");
		content.put("serverid", 1 + "");
		content.put("channelid", info.getPf());
		content.put("level", 1);
		sendPay(info.getFlatId(), reyunAppId, content);
	}

	/**
	 * 记录日志 for(log.reyun.com)
	 * 
	 * @param flatId
	 * @param appId
	 * @param json
	 */
	public static void sendPay(final String flatId, final String appId, final JSONObject json) {
		final String url = "http://log.reyun.com/receive/rest/payment";
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i <= 3; i++) {
					try {
						GameBackLogger.SYS_LOG.info("sendPay -->i::" + i);
						JSONObject _json = JSONObject.parseObject("{}");
						_json.put("appid", appId);
						// 大于64位取后面的长度
						if (flatId.length() > 64) {
							_json.put("who", flatId.substring(flatId.length() - 64, 64));
						} else {
							_json.put("who", flatId);
						}
						_json.put("context", json);
						NetTool netTool = new NetTool();
						String result = netTool.sendPostJsonRequest(url, _json, "UTF-8");
						if (!StringUtils.isBlank(result)) {
							JSONObject result_json = JSONObject.parseObject(result);
							String code = result_json.getString("status");
							if (code.equals("0")) {
								GameBackLogger.SYS_LOG.info("sendPay code:" + result + "\n" + _json.toString());
								break;
							} else {
								GameBackLogger.SYS_LOG.error("sendPay err code:" + result + "\n" + _json.toString());
							}
						} else {
							GameBackLogger.SYS_LOG.error("sendPay error is null \n" + _json.toString());
						}
						long time = 10 * TimeUtil.SENCOND_IN_MINILLS;
						Thread.sleep(time);
					} catch (Exception e) {
						GameBackLogger.SYS_LOG.error("sendPay error", e);
					}
				}
			}

		}).start();
	}

	protected void insertRoomCard(OrderValidate ov, PayBean bean, String transid, RegInfo user) {
		// 记录直充记录
		try {
			RoomcardOrder order = new RoomcardOrder();
			order.setRoleId(user.getUserId());
			order.setOrderId(transid);
			order.setRegisterBindAgencyId(user.getRegBindId());
			order.setRechargeBindAgencyId(user.getPayBindId());
			int isFirstRecharge = orderDao.isFirstRecharge(user.getUserId());
			if (isFirstRecharge <= 0) {
				order.setIsFirstPayAmount(ov.getAmount() * 10);
				order.setIsFirstPayBindId(1);

			}
			order.setCommonCards(bean.getYuanbao());
			order.setFreeCards(0);
			order.setIsDirectRecharge(1);
			order.setRechargeWay(pf);
			order.setRechargeAgencyId(0);
			order.setOrderStatus(1);
			order.setCreateTime(TimeUtil.now());// 时间
            if (bean.getId()>1000) {
                if (isFirstRecharge<=0) {
                    order.setCommonCards(bean.getYuanbao()+88888);
                }
                order.setRemark("gold");
            }
			orderDao.insertRoomCards(order);

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("insertRoomCard err", e);
		}
	}

	protected void insertRoomCard(OrderValidate ov, PayBean bean, String transid, String pf) {
		try {
			RegInfo user = userDao.getUser(ov.getFlat_id(), ov.getPf());
			insertRoomCard(ov,bean,transid,user);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("insertRoomCard err", e);
		}
	}

	/**
	 * 支付出现异常 统一记录日志
	 * 
	 * @param platform
	 * @param orderid
	 * @param amount
	 * @param uid
	 * @param e
	 */
	protected void exception(String platform, String orderid, int amount, String uid, Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append("#pay error--pf::").append(platform);
		sb.append(" orderId::").append(orderid);
		sb.append(" amount::").append(amount);
		sb.append(" userId::").append(uid);
		GameBackLogger.MONITOR_LOG.error(sb.toString(), e);
	}

	protected void errorlog(String platform, String orderid, int amount, String uid, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("#pay error--pf::").append(platform);
		sb.append(" orderId::").append(orderid);
		sb.append(" amount::").append(amount);
		sb.append(" userId::").append(uid);
		sb.append(" msg::").append(msg);
		GameBackLogger.SYS_LOG.info(sb.toString());
	}

	protected String convert(String s) {
		String result = s;
		byte[] temp;
		try {
			temp = s.getBytes("iso-8859-1");
			result = new String(temp, "UTF-8");
		} catch (Exception e) {
		}
		return result;
	}

	protected String getIpAddr() {
		return getIpAddr(getRequest());
	}

	protected PayBean getItem(int amount) {
		for (PayBean key : GameServerManager.payBeans.values()) {
			if (key.getAmount() == amount) {
				return key;
			}
		}
		return null;
	}

	/**
	 * itemId 1-10 80-90
	 * 
	 * @param amount
	 * @param itemId
	 * @return
	 */
	protected int getYiwanItem(int amount, int itemId) {
		boolean android = false;
		if (itemId <= 10) {
			// itemId小于10是安卓平台
			android = true;
		}
		if (itemId > 90) {
			// 如果大于90 不在这个范围内
			return itemId;
		}
		if (itemId < 80 && itemId > 10) {
			// 10-80之间也不在范围内
			return itemId;
		}
		for (int key : GameServerManager.payBeans.keySet()) {
			if (android && key > 10) {
				// 如果是安卓平台并且item>10 不在范围内
				continue;
			}
			if (!android && (key < 80 || key > 90)) {
				// 如果不是安卓平台并且Item不在80和90 不在范围内
				continue;
			}
			PayBean pay_bean = GameServerManager.payBeans.get(key);
			if (pay_bean.getAmount() == amount) {
				return key;
			}
		}
		return itemId;
	}

	public String getUrl(String url, Map<String, String> params) {
		StringBuffer s = new StringBuffer();
		// k=v&k=v
		for (String k : params.keySet()) {
			s.append("&").append(k).append("=").append(params.get(k));
		}
		s.deleteCharAt(0);
		return url + "?" + s.toString();
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setSdkConfig(SdkUrlConfig sdkConfig) {
		this.sdkConfig = sdkConfig;
	}

	public SdkUrlConfig getSdkConfig() {
		return sdkConfig;
	}

	public PfSdkConfig getPfSdkConfig() {
		return getPfSdkConfig(pf);
	}

	public PfSdkConfig getPfSdkConfig(String pf) {
		return PfCommonStaticData.getConfig(pf);
	}

	public static void main(String[] args) {
		// GameServerManager.getInstance();
		// System.out.println(getYiwanItem(5000, 1));
	}

	public String getIpAddr(HttpServletRequest request) {
		return IpUtil.getIpAddr(request);
	}

	public GameUserDao getGameUserDao() {
		return gameUserDao;
	}

	public void setGameUserDao(GameUserDao gameUserDao) {
		this.gameUserDao = gameUserDao;
	}

	public void setRoomCardDao(RoomCardDaoImpl roomCardDao) {
		this.roomCardDao = roomCardDao;
	}

	public BaseSdk buildRoomCardDao(RoomCardDaoImpl roomCardDao){
		this.roomCardDao = roomCardDao;
		return this;
	}

    protected String loadPayUrl(HttpServletRequest request,String str1,String str2){
        String str0=request.getRequestURL().toString().replace(str1, str2);

        String str=PropertiesCacheUtil.getValue("payUrl",Constants.GAME_FILE);
        if (StringUtils.isNotBlank(str)){
            str0=str+str0.substring(str0.indexOf("/",str0.indexOf(".")));
        }
        GameBackLogger.SYS_LOG.info("loadPayUrl:"+str0);
        return str0;
    }

	protected String loadRootUrl(HttpServletRequest request){
		String str0=request.getRequestURL().toString();

		String str=PropertiesCacheUtil.getValue("payUrl",Constants.GAME_FILE);
		if (StringUtils.isNotBlank(str)){
			str0=str+str0.substring(str0.indexOf("/",str0.indexOf(".")));
		}

		int idx;
		if ((idx=str0.indexOf("support!ovali_com"))>0){
			str0=str0.substring(0,idx)+"operateSuccess.jsp";
		}

		GameBackLogger.SYS_LOG.info("loadRootUrl:"+str0);
		return str0;
	}

	/**
	 * 查单并补单
	 */
	public void queryOrder(){
        OutputUtil.output(1,"fail",getRequest(),getResponse(),false);
	}

	@Override
	public boolean checkOrderAgencyId() {
		return true;
	}
}
