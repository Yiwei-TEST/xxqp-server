package com.sy.sanguo.game.pdkuai.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.*;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.bean.enums.SourceType;
import com.sy.sanguo.game.bean.gold.GoldRoom;
import com.sy.sanguo.game.bean.group.GroupTable;
import com.sy.sanguo.game.bean.group.GroupTableConfig;
import com.sy.sanguo.game.dao.*;
import com.sy.sanguo.game.dao.gold.GoldRoomDao;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.msg.PayGItemMsg;
import com.sy.sanguo.game.msg.UserPlayTableMsg;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.bean.*;
import com.sy.sanguo.game.pdkuai.db.dao.*;
import com.sy.sanguo.game.pdkuai.game.*;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.pdkuai.util.PlayLogTool;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import com.sy.sanguo.game.utils.BjdUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PdkAction extends GameStrutsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger("sys");
	private static final long serialVersionUID = 1L;
	private OrderDaoImpl orderDao;
	private UserDaoImpl userDao;
	private GameUserDao gameUserDao;
	private OrderValiDaoImpl orderValiDao;
	private RoomCardDaoImpl roomCardDao;
	private GroupDao groupDao;
	private UserRelationDaoImpl userRelationDao;
	private UserLotteryStatisticsDaoImpl userLotteryStatisticsDao;

	private String result = "";

	private static Map<String, Class<? extends BaseAction>> actionSignMap = new HashMap<String, Class<? extends BaseAction>>();
	private static Map<String, Class<? extends BaseAction>> actionPassSignMap = new HashMap<String, Class<? extends BaseAction>>();

	static {
		actionPassSignMap.put("1", GameAction.class);
		actionSignMap.put("2", FirstMythAction.class);
		actionSignMap.put("3", PromotionAction.class);
		actionSignMap.put("4", GameSiteAction.class);
		actionSignMap.put("5", MonitorAction.class);
		actionSignMap.put("6", DaikaiAction.class);
		actionSignMap.put("7", RedPacketAction.class);
		actionSignMap.put("9", LuckyAction.class);
		actionSignMap.put("10", ToServerAction.class);
		actionSignMap.put("11", PlayerPromotionAction.class);
	}

	public String execute() throws Exception {
		String type = this.getString("actionType");
		if (actionPassSignMap.containsKey(type)) {
			BaseAction baseAction = actionPassSignMap.get(type).newInstance();
			baseAction.setGameUserDao(gameUserDao);
			baseAction.setRequest(getRequest());
			baseAction.setResponse(getResponse());
			baseAction.setUserDao(userDao);
			this.result = baseAction.execute();
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String exec() {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		String type = this.getString("actionType");
		if (actionSignMap.containsKey(type)) {
			BaseAction baseAction;
			try {
				baseAction = actionSignMap.get(type).newInstance();
				baseAction.setGameUserDao(gameUserDao);
				baseAction.setRequest(getRequest());
				baseAction.setResponse(getResponse());
				baseAction.setUserDao(userDao);
				this.result = baseAction.execute();
			} catch (Exception e) {
				e.printStackTrace();
				GameBackLogger.SYS_LOG.error("exec err:", e);
			}
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String getHeadImg() {
		Enumeration<String> e = getRequest().getHeaderNames();
		while (e.hasMoreElements()) {
			String object = e.nextElement();
			String val = getRequest().getHeader(object);
			GameBackLogger.SYS_LOG.info("getHeadImg:" + object + " -->" + val);
		}
		// GameBackLogger.SYS_LOG.info("getHeadImg-->"+NetTool.receivePost(getRequest()));
		GameBackLogger.SYS_LOG.info("getHeadImg-->" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
		// Map<String,String> map=new HashMap<>();
		// // Accept-Ranges bytes
		// // Cache-Control no-cache
		// // Content-Length 44218
		// // Content-Type image/png
		// // Date Fri, 09 Sep 2016 06:23:16 GMT
		// // Expires Fri, 09 Sep 2016 06:23:15 GMT
		// // Last-Modified Mon, 02 Mar 2015 02:42:50 GMT
		// // Server nginx
		// map.put("Accept-Ranges", "bytes");
		// map.put("Cache-Control", "no-cache");
		// map.put("Content-Length", "44218");
		// // map.put("Content-Type", "image/png");
		// map.put("Date", "Fri, 09 Sep 2016 06:23:16 GMT");
		// map.put("Expires", "Fri, 09 Sep 2016 06:23:15 GMT");
		// map.put("Last-Modified", "Mon, 02 Mar 2015 02:42:50 GMT");
		// map.put("Server", "nginx");
		//
		// for(Entry<String, String> entry: map.entrySet()){
		// getResponse().addHeader(entry.getKey(), entry.getValue());
		// }
		// this.result="213";
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String getLog() {
//		if (!checkPdkSign()) {
//			return StringResultType.RETURN_ATTRIBUTE_NAME;
//		}
		String shell = "";
		try {
			String key = "";
			StringBuilder strBuilder=new StringBuilder("getLog-->");
			// 默认登录工程目录存放地址
			String defaultAddr = "/data/upstxsg/login/jboss/webapps/myzLogin/logs/";
			String addr = this.getString("addr", defaultAddr);
			// 日志时期
			String date = this.getString("date");
			// 日志类型
			String type = this.getString("type", "sanguo_sys");
			// 日志服id
			int serverId = this.getInt("serverId", 0);
			// 拿到某个桌子的日志
//			int tableId = this.getInt("tableId", 0);
			// 拿到某个玩家的日志
//			int userId = this.getInt("userId", 0);
			String keyWords = this.getString("keyWords");
			String freeShell = this.getString("freeShell");
			String sign = this.getString("sign");
			String mySign = MD5Util.getStringMD5(addr+key);
			strBuilder.append(",addr=").append(addr);
			strBuilder.append(",date=").append(date);
			strBuilder.append(",type=").append(type);
			strBuilder.append(",serverId=").append(serverId);
			strBuilder.append(",keyWords=").append(keyWords);
			strBuilder.append(",freeShell=").append(freeShell);
			strBuilder.append(",sign=").append(sign);
			LogUtil.i(strBuilder.toString());
			Process process;
			if (!StringUtils.isBlank(freeShell)) {
				if (freeShell.contains("rm ")) {
					writeErrMsg("Illegal opt");
					LogUtil.e("Illegal opt");
					return StringResultType.RETURN_ATTRIBUTE_NAME;
				}
				process = Runtime.getRuntime().exec(freeShell);
				InputStream is = process.getInputStream();
				BufferedReader input = new BufferedReader(new InputStreamReader(is,"utf8"));
				this.getResponse().setHeader("Content-Type", "application/force-download;charset=utf-8");
				this.getResponse().setHeader("Content-Disposition", "attachment; filename=log.txt");
				LogUtil.i("success-->"+ process.getInputStream());
				String line;
				int len = 0;
				this.getResponse().getWriter().write(shell+"-->");
				while (len<2*1024*1024&&(line = input.readLine()) != null) {
					len+=line.getBytes().length;
					this.getResponse().getWriter().write(line+"\n");
				}
				input.close();
				this.result = JacksonUtil.writeValueAsString("success"+",len:"+len);
			} else if (!StringUtils.isBlank(type)) {
				String name = type+".log";
				if (!StringUtils.isBlank(date)) {
					name += "."+date;
				}
				shell = "cat "+addr+name;
				if (!StringUtils.isBlank(keyWords)) {
					shell += " | grep " + "'"+keyWords+"'";
 				}
				LogUtil.i("shell:"+shell);
				process = Runtime.getRuntime().exec(shell);
				InputStream is = process.getInputStream();
				BufferedReader input = new BufferedReader(new InputStreamReader(is,"utf8"));
				this.getResponse().setHeader("Content-Type", "application/force-download;charset=utf-8");
				this.getResponse().setHeader("Content-Disposition", "attachment; filename=\""+name+".txt\"");
				String line;
				int len = 0;
				this.getResponse().getWriter().write(shell+"-->");
				while (len<2*1024*1024&&(line = input.readLine()) != null) {
					len+=line.getBytes().length;
					this.getResponse().getWriter().write(line+"\n");
				}
				input.close();
				this.result = JacksonUtil.writeValueAsString("success"+",len:"+len);
			}
		} catch (Exception e) {
			this.result = JacksonUtil.writeValueAsString(e.getMessage()+",shell:"+shell);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}


	/**
	 * 活动接口
	 */
	public String isActivityOpen() {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		Map<String, Object> result = new HashMap<>();
		// 活动id
		String aId = this.getString("aId");
		if (!StringUtils.isBlank(aId)) {
			Activity activityBean = ActivityDao.getInstance().getActivityById(Integer.parseInt(aId));
			if (activityBean != null) {
				buildActivityRes(result, activityBean);
				this.writeMsg(0, result);
			} else {
				result.put("msg", "活动未开启");
				this.writeMsg(-1, result);
			}
		} else {
			this.writeErrMsg(LangMsg.getMsg(LangMsg.code_3));
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	private void buildActivityRes(Map<String, Object> result, Activity activityBean) {
		Date now = TimeUtil.now();
		int isOpen = 0;
		if(TimeUtil.isInTime(now, activityBean.getBeginTime(), activityBean.getEndTime())) {
            isOpen = 1;
        }
		result.put("activity", isOpen);
		result.put("them", activityBean.getThem());
		result.put("showContent", activityBean.getShowContent());
		result.put("beginTime", TimeUtil.formatTime(activityBean.getBeginTime()));
		result.put("endTime", TimeUtil.formatTime(activityBean.getEndTime()));
		result.put("extend", activityBean.getExtend());
	}

	/**
	 * 验证玩家是否能金币场首充
	 * @return  isFirstRecharge 1能首冲，0不能
	 */
	public String isFirstRecharge() {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		Map<String, Object> result = new HashMap<>();
		long uid = this.getLong("userId");
		String remark = this.getString("remark");
		try {
			if ("gold".equals(remark)) {
				int res = orderDao.isFirstRechargeGold(uid);
				result.put("code", 0);
				result.put("isFirstRechargeGold", res<=0?1:0);
			} else {
			    LogUtil.e("param remark err-->userId:"+uid);
            }
			this.result = JacksonUtil.writeValueAsString(result);
		} catch (SQLException e) {
			LogUtil.e("isFirstRecharge err-->", e);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}


	/**
	 * 验证被代充玩家
	 *
	 * @return
	 *
	 */
	public String ovaliReplacedPay() {
		Map<String, Object> result = new HashMap<String, Object>();
		// 获得代充玩家的信息
		try {
			// 被代充的玩家id值
			long userId = this.getLong("fid");
			RegInfo userInfo = userDao.getUser(userId);
			if (userInfo != null) {
				result.put("userId", userInfo.getUserId());
				result.put("userName", userInfo.getName());
				result.put("pf", userInfo.getPf());

				String agencyPf;
				if (userInfo.getPayBindId()>0){
					HashMap<String,Object> agencyInfo = roomCardDao.queryAgencyByAgencyId(userInfo.getPayBindId());
					if (agencyInfo!=null&&agencyInfo.size()>0){
						agencyPf = String.valueOf(agencyInfo.get("pf"));
						if (org.apache.commons.lang3.StringUtils.isNotBlank(agencyPf)&&(!"null".equalsIgnoreCase(agencyPf))){

						}else{
							agencyPf = "";
						}
					}else{
						agencyPf = "";
					}
				}else{
					agencyPf = "";
				}
				result.put("agencyPf", agencyPf);

				result.put("code", 0);
			} else {
				result.put("code", -1);
				result.put("msg", "没有找到ID:" + userId + "的玩家");
			}
		} catch (Exception e) {
			result.put("code", -2);
		}
		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 分享数据统计
	 * @return
	 */
	public String shareStatics() throws Exception{
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		Map<String,String> params= com.sy.mainland.util.UrlParamUtil.getParameters(getRequest());
		if (CommonUtil.isPureNumber(params.get("userId"))&&CommonUtil.isPureNumber(params.get("sourceType"))&&CommonUtil.isPureNumber(params.get("shareType"))) {
			long userId = this.getLong("userId", 0);
			int sourceType = this.getInt("sourceType", 0);// 分享来源  0未知 1表示瓜分红包
			int shareType = this.getInt("shareType", 1); //分享的类型（0好友/群  1朋友圈）
			ShareStaticData data = new ShareStaticData(userId, new Date(), shareType, sourceType);
			ShareStaticDao.getInstance().addShareData(data);
			GameBackLogger.SYS_LOG.info("userId：" + userId + "---shareType:" + shareType + "---sourceType:" + sourceType);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 玩家分享
	 */
	public void share() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBackStageManagement|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			RegInfo info;
			if (userId != 0) {
				try {
					// 验证玩家
					info = userDao.getUser(userId);
					if (info == null) {
						OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
						return;
					}

					Calendar now = Calendar.getInstance();

					String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(now.getTime());

					List<UserShare> lists = UserShareDao.getInstance().getUserShare(userId,currentDate);
					if(lists != null && lists.size() > 0) {
						OutputUtil.output(-1, "已经领取过分享奖励", getRequest(), getResponse(), false);
						return;
					}

					int gold = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "gold_award_share",600);
					if (gold>0) {
						UserShare userShare = new UserShare();
						userShare.setUserId(userId);
						userShare.setShareDate(now.getTime());
						userShare.setDiamond(gold);
						userShare.setExtend("gold");
						UserShareDao.getInstance().addUserShare(userShare);

						GoldDao.getInstance().addUserGold(userId,gold,0, SourceType.share_award);
						JSONObject json = new JSONObject();
						json.put("msg", "分享奖励金币*：" + gold);
						json.put("gold", gold);
						OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
					}
				} catch (Exception e) {
					GameBackLogger.SYS_LOG.error("share|error|" + userId, e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 *领钻配置(_玩法) 基础钻石,浮动钻石,概率(多个以分号隔开)<br/>
	 diamond_config_game=10,11,100 <br/>
	 diamond_config_game=20,0,100 <br/>
	 diamond_config_game=15,6,40;21,5,40;26,5,20 <br/>
	 diamond_config_game=10,11,100 <br/>
	 diamond_config_game=15,6,40;21,5,40;26,5,20 <br/>
	 diamond_config_game=30,11,79;41,5,20;46,5,1 <br/>
	 diamond_config_game=10,5,30;15,4,40;19,2,30 <br/>
	 * @param playType
	 * @return
	 */
	private int randomDiamond(int playType,int currency) {
		int diamond = -1;
		try {
			int tatalRatio=0;
			int configs[][];
			String configStr;
			if (currency == 2) {
                configStr = PropertiesCacheUtil.getValue("diamond_gold_game_" + playType,Constants.GAME_FILE);
                if (StringUtils.isBlank(configStr)) {
                    configStr = PropertiesCacheUtil.getValue("diamond_gold_game",Constants.GAME_FILE);
                }
			} else {
                configStr = PropertiesCacheUtil.getValue("diamond_config_game_" + playType,Constants.GAME_FILE);
                if (StringUtils.isBlank(configStr)) {
                    configStr = PropertiesCacheUtil.getValue("diamond_config_game",Constants.GAME_FILE);
                }
			}

			if (StringUtils.isNotBlank(configStr)) {
				if (configStr.contains(";")) {
					String[] strs=configStr.split(";");
					configs = new int[strs.length][4];
					int i=0;
					for (String str:strs){
						String[] temps=str.split(",");
						if (temps.length==3){
							configs[i][0]=Integer.parseInt(temps[0]);
							configs[i][1]=Integer.parseInt(temps[1]);
							int ratio=Integer.parseInt(temps[2]);
							configs[i][2]=ratio;
							tatalRatio+=ratio;
							configs[i][3]=tatalRatio;
							i++;
						}
					}
				} else {
					configs = new int[1][4];
					String[] strs=configStr.split(",");
					if (strs.length==3){
						configs[0][0]=Integer.parseInt(strs[0]);
						configs[0][1]=Integer.parseInt(strs[1]);
						int ratio=Integer.parseInt(strs[2]);
						configs[0][2]=ratio;
						tatalRatio+=ratio;
						configs[0][3]=tatalRatio;
					}
				}
			}else{
				if (currency == 2) {
					//3000,100
					configs=new int[1][4];
					configs[0][0]=3000;
					configs[0][1]=0;
					configs[0][2]=100;
					configs[0][3]=100;
					tatalRatio=100;
				} else {
					//10,11,100
					configs=new int[1][4];
					configs[0][0]=10;
					configs[0][1]=11;
					configs[0][2]=100;
					configs[0][3]=100;
					tatalRatio=100;
				}
			}

			if (tatalRatio!=100){
				LogUtil.e("diamond_config_game config error:tatalRatio="+tatalRatio+",currency="+currency);
				return 0;
			}else{
				Random random=new SecureRandom();
				int value=random.nextInt(100)+1;
				for (int[] ints:configs){
					if (value<=ints[3]){
						diamond=ints[0];
						if (ints[1]!=0){
							diamond+=((int)(ints[1]*random.nextDouble()));
						}
						break;
					}
				}
			}

		}catch (Exception e){
			LogUtil.e("diamond_config_game config error:"+e.getMessage(),e);
			return 0;
		}
		if (diamond<0){
			LogUtil.e("diamond_config_game config error");
			return 0;
		}
		return diamond;
	}

	public String getPayItems() {
		String referer = request.getHeader("Referer");
		if (StringUtils.contains(referer,"/h5/pay/index.jsp")&&StringUtils.contains(referer,getRequest().getScheme())){
		}else if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		int os = this.getInt("os", 0);
		int pay = this.getInt("pay", 0);
		String currency = this.getString("currency");

        int version = 0;
        if (NumberUtils.isDigits(this.getString("version"))) {
            version = this.getInt("version", 0);
        }

		boolean isAndroid = true;
		Map<String, Object> result = new HashMap<String, Object>();
		List<PayGItemMsg> messages = new ArrayList<>();
		// 活动开关
		boolean isOpen = false;

		// 一些特殊的支付，如支付解绑邀请码,不显示再列表中，
		String payRemoveBindStr = getSpecialPay("payRemoveBind");
		if (pay == 0) {
			for (PayBean bean : GameServerManager.payBeans.values()) {
				if ("gold".equals(currency)) {
					if (bean.getId() < 1000){
						continue;
					}
				}else {
					if (bean.getId() > 1000){
						continue;
					}
				}
				int mod = bean.getId()>1000?bean.getId()%1000:bean.getId();
				if (isAndroid && mod >= 10) {
					continue;
				}
				if (payRemoveBindStr.contains(bean.getId()+"")) {
					continue;
				}
				PayGItemMsg msg = bean.buildItemMsg();
				if (bean.getSpecialGive()>0) {
					isOpen=true;
				}
				if (bean.isDouble()) {
					msg.setIsDouble(1);
				} else {
					msg.setIsDouble(0);
				}
				msg.setOrder(bean.getOrder());
				messages.add(msg);
			}

		} else {
			for (PayBean bean : GameServerManager.payBeans.values()) {
				if ("gold".equals(currency)) {
					if (bean.getId() < 1000){
						continue;
					}
				}else {
					if (bean.getId() > 1000){
						continue;
					}
				}
				int mod = bean.getId()>1000?bean.getId()%1000:bean.getId();
				if (mod / 10 != pay) {
					continue;
				}
				if (payRemoveBindStr.contains(bean.getId()+"")) {
					continue;
				}
				PayGItemMsg msg = bean.buildItemMsg();
				if (bean.getSpecialGive()>0) {
					isOpen=true;
				}
				if (bean.isDouble()) {
					msg.setIsDouble(1);
				} else {
					msg.setIsDouble(0);
				}
				msg.setOrder(bean.getOrder());
				messages.add(msg);
			}
		}

		result.put("payItem", messages);
		String gameCode=getString("gameCode");

		String payType = null;
		String sparePay = null;
		String iosState = null;

		List<HashMap<String,Object>> payTypes;

		String hasMinProgram = getString("hasMinProgram");
		if ("1".equals(hasMinProgram)){
			payTypes = SystemCommonInfoDao.getInstance().select("defaultPayType","sparePayType","iosPayState","mpDefaultPayType","mpSparePayType");

		}else{
			payTypes = SystemCommonInfoDao.getInstance().select("defaultPayType","sparePayType","iosPayState");
		}

		if (payTypes!=null&&payTypes.size()>0){
			for (HashMap<String,Object> map : payTypes){
				if ("defaultPayType".equals(map.get("type"))){
					if (StringUtils.isBlank(payType)){
						payType = String.valueOf(map.get("content"));
						if(version == 1) {
							//临时版本增加竣付通、掌宜付支付方式
							payType = payType.split("\\|")[1];
						}else {
							payType = payType.split("\\|")[0];
						}
					}
				}else if ("sparePayType".equals(map.get("type"))){
					if (StringUtils.isBlank(sparePay)){
						sparePay = String.valueOf(map.get("content"));
						if(version == 1) {
							//临时版本增加竣付通、掌宜付支付方式
							sparePay = sparePay.split("\\|")[1];
						}else {
							sparePay = sparePay.split("\\|")[0];
						}
					}
				}else if ("iosPayState".equals(map.get("type"))){
					iosState = String.valueOf(map.get("content"));
				}else if ("mpDefaultPayType".equals(map.get("type"))){
					payType = String.valueOf(map.get("content"));
				}else if ("mpSparePayType".equals(map.get("type"))){
					sparePay = String.valueOf(map.get("content"));
				}
			}
		}
		if (StringUtils.isBlank(payType)||"null".equals(payType)){
			payType=PropertiesCacheUtil.getValue("pay_"+gameCode,Constants.GAME_FILE);
		}
		if (StringUtils.isBlank(sparePay)||"null".equals(sparePay)){
			sparePay=PropertiesCacheUtil.getValue("spare_pay_"+gameCode,Constants.GAME_FILE);
		}

		String payType0 = request.getParameter("specialPayType");
		if (StringUtils.isNotBlank(payType0)){
		    PfSdkConfig config = PfCommonStaticData.getConfig(payType0);
		    if (config!=null){
				result.put("appid",config.getAppId());
			}
        }

		result.put("payType",StringUtils.isBlank(payType)?"":payType);
		result.put("sparePay",StringUtils.isBlank(sparePay)?"":sparePay);
        result.put("activity", isOpen?1:0);
        String userId = getString("mUserId");
        if (NumberUtils.isDigits(userId)&&!"gold".equals(currency)){
            try {
                if(os == 1) {
                    RegInfo regInfo = UserDao.getInstance().getUser(NumberUtils.toLong(userId));
                    if((regInfo != null && regInfo.getTotalCount() > 100)) {
                        result.put("iosChargeAble", 1);
                    } else {
                        HashMap<String, Object> extendMap = UserDao.getInstance().queryUserExtend(String.valueOf(userId), 201);// IOS允许充值标识201
                        if(extendMap != null)
                            result.put("iosChargeAble", 1);
                        else
                            result.put("iosChargeAble", 0);
                    }
                } else
                    result.put("iosChargeAble", 1);
                boolean isFirstPay = UserDao.getInstance().isFirstPay(NumberUtils.toLong(userId), 1, 9);
                result.put("hasPay", !isFirstPay);
                String firstPayGive = PropertiesCacheUtil.getValue("first_pay_give",Constants.GAME_FILE);
                if (StringUtils.isNotBlank(firstPayGive)){
                    result.put("firstPayGive",firstPayGive);
                }
            }catch (Exception e){
            }
        }

		result.put("iosState", (StringUtils.isBlank(iosState)||"null".equals(iosState))?"1":iosState);

		LogUtil.i("load pay msg:gameCode="+gameCode+",payType="+payType+",sparePay="+sparePay+",activity="+isOpen+",hasPay="+result.get("hasPay")+",hasMinProgram="+hasMinProgram);

		this.writeMsg(0, result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 根据名称获得某些特殊支付的配置
	 * @param name 名称
	 */
	public static String getSpecialPay(String name) {
		String specialPay = PropertiesCacheUtil.getValue("specialPay",Constants.GAME_FILE);
		if (StringUtils.isBlank(specialPay)) {
			return "";
		}
		String strs[] = specialPay.split(";");
		for (String str : strs) {
			if (str.startsWith(name)) {
				return str;
			}
		}
		return "";
	}

	/**
	 * 比赛场获取正确的server
	 *
	 * @return
	 */
	public String getMatchServerById() {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		int gameType = this.getInt("gameType", 0);
		Server server = null;

		if (gameType != 0) {
			// 创建房间的时候
			server = SysInfManager.loadServer(gameType, 1,true);
		}

		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> serverMap = new HashMap<String, Object>();
		serverMap.put("serverId", server.getId());
		serverMap.put("httpUrl", server.getHost());
		serverMap.put("connectHost", server.getChathost());

		result.put("server", serverMap);
		this.writeMsg(0, result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * tableId获取正确的server
	 *
	 * @return
	 */
	public void getServerById() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBackStageManagement|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}

			int gameType = NumberUtils.toInt(params.get("gameType"), 0);
			long tableId = NumberUtils.toLong(params.get("tableId"), 0);
			long userId = NumberUtils.toLong(params.get("userId"), 0);

			long modeId = NumberUtils.toLong(params.get("modeId"), 0);//牌局模式ID，创建房间参数具体信息需查看数据库

			int serverType = NumberUtils.toInt(params.get("serverType"), 1);//游戏服类型0练习场1普通场

			StringBuilder strBuilder = new StringBuilder("load server:");
			strBuilder.append("gameType=").append(gameType);
			strBuilder.append(",tableId=").append(tableId);
			strBuilder.append(",userId=").append(userId);
			strBuilder.append(",modeId=").append(modeId);
			strBuilder.append(",serverType=").append(serverType);

			RegInfo info = null;
			long totalCount = 0;
			if (userId != 0) {
//			UserGameSite userGameSite = GameSiteDao.getInstance().queryUserGameSite(userId);
//			if (userGameSite != null && userGameSite.getGameSiteId() > 0 && userGameSite.getRoundNum() > 0) {
//				this.writeMsg(-1, null);
//				return StringResultType.RETURN_ATTRIBUTE_NAME;
//			}
				try {
					info = userDao.getUser(userId);
					if (info != null) {
						//局数+充值》》》用于用户分级
						//((-usedCards+cards)/150+totalCount)
						totalCount = (-info.getUsedCards() + info.getCards()) / 150 + info.getTotalCount();
					}
				} catch (SQLException e) {
					GameBackLogger.SYS_LOG.error("数据库load玩家数据出错,玩家Id:" + userId, e);
				}

				if (info != null && info.getPlayingTableId() != 0) {
					tableId = info.getPlayingTableId();

					strBuilder.append(",playingTableId=").append(tableId);
				}
			}
			boolean loadFromCheckNet = true;
			Server server = null;

			if (tableId <= 0 && modeId > 0) {
				try {
					GroupTable groupTable = groupDao.loadRandomGroupTable(modeId);

					if (groupTable != null) {
						tableId = groupTable.getTableId();

						strBuilder.append(",groupTableId=").append(tableId);
					}

					if (gameType <= 0) {
						GroupTableConfig groupTableConfig = groupDao.loadGroupTableConfig(modeId);
						if (groupTableConfig != null) {
							gameType = groupTableConfig.getGameType();

							strBuilder.append(",groupGameType=").append(gameType);
						}
					}
				} catch (Exception e) {
					LogUtil.e("Exception:" + e.getMessage(), e);
				}
			}

			String[] gameUrls = null;
			int playType = -1;
			if (tableId != 0) {
				int serverId;
				if (tableId < Constants.MIN_GOLD_ID) {
					serverId = Manager.getInstance().getServerId(tableId);
				} else {
					try {
						GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(tableId);
						serverId = goldRoom != null ? goldRoom.getServerId() : 0;
					} catch (Exception e) {
						serverId = 0;
						LogUtil.e("Exception:" + e.getMessage(), e);
					}
				}

				server = SysInfManager.getInstance().getServer(serverId);
				if (server == null) {
					gameUrls = CheckNetUtil.loadGameUrl(serverId, totalCount);
					if (gameUrls != null) {
						server = new Server();
						server.setId(serverId);
						server.setChathost(gameUrls[0]);
						loadFromCheckNet = false;
					} else {
						OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_24), getRequest(), getResponse(), false);
						return;
					}
				}
				try {
					Room room = RoomDaoImpl.getInstance().queryRoomByRoomId(tableId);
					if (room != null) {
						playType = room.getType();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (gameType != 0 && tableId == 0) {
				// 创建房间的时候
				server = SysInfManager.loadServer(gameType, serverType);
			}

			JSONObject json = new JSONObject();
			Map<String, Object> serverMap = new HashMap<String, Object>();
			serverMap.put("serverId", server.getId());

			if (loadFromCheckNet) {
				serverMap.put("httpUrl", server.getHost());
				gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
			}

			if (gameUrls == null) {
				serverMap.put("connectHost", server.getChathost());
				serverMap.put("connectHost1", "");
				serverMap.put("connectHost2", "");
			} else {
				serverMap.put("connectHost", StringUtils.isNotBlank(gameUrls[0]) ? gameUrls[0] : server.getChathost());
				serverMap.put("connectHost1", gameUrls[1]);
				serverMap.put("connectHost2", gameUrls[2]);
			}
			serverMap.put("playType", playType);
			json.put("server", serverMap);
			json.put("blockIconTime", SharedConstants.blockIconTime);
			OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);

			strBuilder.append(",result=").append(result);
			LogUtil.i(strBuilder.toString());
		}catch (Exception e){
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}

	}

	/**
	 * 比赛场返回普通场接口
	 *
	 * @return
	 */
	public String backServerByPlayTableId() {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		long userId = this.getLong("userId");
		RegInfo info = null;
		try {
			info = userDao.getUser(userId);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("数据库load玩家数据出错,玩家Id:" + userId, e);
		}
		long tableId = info.getPlayingTableId();
		Server server;
		long totalCount=(-info.getUsedCards()+info.getCards())/150+info.getTotalCount();
		boolean loadFromCheckNet=true;
		String[] gameUrls=null;
		Map<String, Object> result = new HashMap<String, Object>();
		if (tableId != 0) {
			int serverId = Manager.getInstance().getServerId(tableId);
			server = SysInfManager.getInstance().getServer(serverId);
			if (server == null) {

                gameUrls= CheckNetUtil.loadGameUrl(serverId,totalCount);
				if (gameUrls!=null){
					server=new Server();
					server.setId(serverId);
					server.setChathost(gameUrls[0]);
					loadFromCheckNet=false;
				}else{
					// 是否是代开
					DaikaiTable DaikaiTable = DaikaiTableDao.getInstance().getDaikaiTable(tableId);
					if (DaikaiTable == null) {
						writeErrMsg("没有找到该房间");
						return StringResultType.RETURN_ATTRIBUTE_NAME;
					}

					server = SysInfManager.loadServer(DaikaiTable.getPlayType(),1,false);
				}
			}

		} else {
			server = SysInfManager.loadServer(-1,1,false);
		}

		Map<String, Object> serverMap = new HashMap<String, Object>();
		serverMap.put("serverId", server.getId());

		if (loadFromCheckNet){
			serverMap.put("httpUrl", server.getHost());

			gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
		}

        if (gameUrls==null){
            serverMap.put("connectHost",server.getChathost());
            serverMap.put("connectHost1","");
            serverMap.put("connectHost2","");
        }else{
            serverMap.put("connectHost",StringUtils.isNotBlank(gameUrls[0])?gameUrls[0]:server.getChathost());
            serverMap.put("connectHost1",gameUrls[1]);
            serverMap.put("connectHost2",gameUrls[2]);
        }

		result.put("server", serverMap);
		result.put("playingTableId", tableId);
		this.writeMsg(0, result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public void checkIp() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBackStageManagement|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			int serverId = NumberUtils.toInt(params.get("serverId"), 0);;
			String connectHost = params.get("connectHost");
			String totalCount = params.get("totalCount");
			if (serverId == 0) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
//		SysInfManager.getInstance().checkServerIp(serverId, connectHost);
			Server server = SysInfManager.getInstance().getServer(serverId);
			boolean loadFromCheckNet = true;
			String[] gameUrls = null;
			if (server == null) {
				gameUrls = CheckNetUtil.loadGameUrl(serverId, NumberUtils.toLong(totalCount, 0));
				loadFromCheckNet = false;
				server = new Server();
				server.setId(serverId);
				if (gameUrls != null) {
					server.setChathost(gameUrls[0]);
				}
			}

			if (loadFromCheckNet) {
				gameUrls = CheckNetUtil.loadGameUrl(serverId, NumberUtils.toLong(totalCount, 0));
				if (gameUrls != null) {
					server.setChathost(gameUrls[0]);
				}
			}
			JSONObject json = new JSONObject();
			if (gameUrls != null) {
				json.put("connectHost", StringUtils.isNotBlank(gameUrls[0]) ? gameUrls[0] : server.getChathost());
				json.put("connectHost1", gameUrls[1]);
				json.put("connectHost2", gameUrls[2]);
			} else if (StringUtils.isNotBlank(server.getChathost())) {
				json.put("connectHost", server.getChathost());
				json.put("connectHost1", "");
				json.put("connectHost2", "");
			}
			OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
		}catch (Exception e){
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 回访码查看战绩getPlayBackLog  传入参数  userId:查看玩家ID  logId:回访码ID
	 * @return
	 */
	public void getPlayBackLog() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBackStageManagement|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);// 查看玩家ID
			long logId = NumberUtils.toLong(params.get("logId"), 0);// 回访码ID
			if (userId <= 0 || logId <= 0) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
				return;
			}
			List<Long> ids = new ArrayList<>();
			ids.add(logId);
			List<UserPlaylog> logList = TableLogDao.getInstance().selectUserLogByLogId(ids);
			List<UserPlayTableMsg> playLog = Manager.getInstance().buildUserPlayTbaleMsg(logId, logList, userId);
			JSONObject json = new JSONObject();
			json.put("playLog", playLog);
			json.put("userId", userId);
			json.put("logId", logId);
			OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
		}catch (Exception e){
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 打牌日志(战绩)
	 *
	 * @return
	 */
	public void getUserPlayLog() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBackStageManagement|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			// String flatId = this.getString("flatId", "vkjcx9983071");
			// String pf = this.getString("pf", "self");
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			// int viewIndex = this.getInt("viewIndex", -1);
			long logId = NumberUtils.toLong(params.get("logId"), 0);
			int logType = NumberUtils.toInt(params.get("logType"), 0);
			long searchUserId = NumberUtils.toLong(params.get("searchUserId"), 0);
			if (searchUserId > 0)
				userId = searchUserId;
			String wanfas = params.get("wanfa");
			List<Integer> wanfaIds = new ArrayList<>();
			if (!StringUtils.isBlank(wanfas))// 查固定玩法战绩
				wanfaIds = StringUtil.explodeToIntList(wanfas);
			RegInfo info;
			try {
				JSONObject json = new JSONObject();
				info = userDao.getUser(userId);
				if (info == null) {
					OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
					return;
				}
				String record = info.getRecord();
				List<List<Long>> lists = StringUtil.explodeToLongLists(record);
				if (lists == null) {
					json.put("playLog", Collections.EMPTY_LIST);
					OutputUtil.output(0, json, getRequest(), getResponse(), false);
					return;
				}

				for (List<Long> tempList : lists) {
					if (tempList.size() > 0 && tempList.get(0) == 0L) {
						tempList.remove(0);
					}
				}

				List<Long> selList;
				// if (viewIndex >= 0) {
				// Collections.reverse(lists);
				// if (viewIndex >= lists.size()) {
				// playLogMap.put("playLog", Collections.EMPTY_LIST);
				// this.writeMsg(0, playLogMap);
				// return StringResultType.RETURN_ATTRIBUTE_NAME;
				// }
				// selList = lists.get(viewIndex);
				//
				// } else
				//
				if (logId != 0) {
					selList = new ArrayList<Long>();
					for (List<Long> list : lists) {
						if (list != null && !list.isEmpty() && list.contains(logId)) {
							selList = list;
						}
					}

				} else {
					// 查看每一大局的最后一局
					selList = new ArrayList<Long>();
					for (List<Long> list : lists) {
						if (list != null && !list.isEmpty()) {
							selList.add(list.get(list.size() - 1));
						}
					}
				}

				List<UserPlaylog> logList = null;
				if (selList.isEmpty()) {
					logList = new ArrayList<>();
				} else {
					logList = TableLogDao.getInstance().selectUserLogByLogId(selList);
				}
				// 筛选一下
				PlayLogTool.screen(logList, logType, wanfaIds);

				if (!logList.isEmpty() && logId == 0) {
					Collections.reverse(logList);
				}
				List<UserPlayTableMsg> playLog = Manager.getInstance().buildUserPlayTbaleMsg(logId, logList, info.getUserId());
				json.put("playLog", playLog);
				json.put("logType", logType);
				json.put("logId", logId);
				OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
				return;
			} catch (SQLException e) {
				GameBackLogger.SYS_LOG.error("getUserPlayLog err", e);
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
				return;
			}
		}catch (Exception e){
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 解绑
	 */
	public String removeBindRelationship() {
		try {
			if (!checkPdkSign()) {
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			Map<String, String> params = UrlParamUtil.getParameters(getRequest());
			LogUtil.i("removeBindRelationship params:"+JacksonUtil.writeValueAsString(params));
			Map<String, Object> result = new HashMap<>();
			String userId = getRequest().getParameter("userId");
			// 1点击解绑后触发，2点击充值成功后触发
			String action = getRequest().getParameter("action");
			// 充值成功后传过来订单id
			String orderid = getRequest().getParameter("transId");
			if (StringUtils.isBlank(userId)) {
				result.put("msg", "参数错误:" + userId);
				this.writeMsg(-1, result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			// 玩家不存在不能解绑
			RegInfo user = UserDao.getInstance().getUser(Long.parseLong(userId));
			if (user == null) {
				result.put("msg", "没有找到ID:" + userId + "的玩家");
				this.writeMsg(-1, result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			if ("1".equals(action)) {
				// 玩家没有绑定邀请码不能解绑
				if (user.getPayBindId() == 0) {
					result.put("msg", "ID:" + userId + "的玩家未绑定邀请码");
					this.writeMsg(-1, result);
					return StringResultType.RETURN_ATTRIBUTE_NAME;
				}
				List<PayBean> beanList = new ArrayList<>();
				for (PayBean bean : GameServerManager.payBeans.values()) {
					String payRemoveBindStr = getSpecialPay("payRemoveBind");
					if (payRemoveBindStr.contains(bean.getId()+"")) {
						beanList.add(bean);
					}
				}
				result.put("payBeanList", beanList);

				String gameCode=getString("gameCode");
				String payType=PropertiesCacheUtil.getValue("pay_"+gameCode,Constants.GAME_FILE);
				result.put("payType",StringUtils.isBlank(payType)?"":payType);

				canRemoveBind(user, result);
				this.writeMsg(0, result);
			} else if ("2".equals(action)) {
				// 充值成功即可解绑
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("order_id", orderid);
				map.put("flat_id", user.getFlatId());
				OrderInfo info = orderDao.getOne(map);
				// 已经充值
				if (info != null) {
					userDao.removeBindInfo(user);
					removeBind(user);
					result.put("msg", "ID:" + user.getUserId() + "的玩家解绑成功");
					this.writeMsg(0, result);
				} else {
					result.put("msg", "没有找到ID:" + user.getUserId() + "的玩家解绑充值订单");
					this.writeMsg(0, result);
				}
			} else {
				result.put("msg", "参数错误:" + userId);
				this.writeMsg(-1, result);
			}
		} catch (Exception e) {
			LogUtil.e("RemoveBind Exception:" + e.getMessage(), e);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public static void removeBind(RegInfo user) {
		// 添加解绑记录
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("userId", user.getUserId());
		paramMap.put("agencyId", user.getPayBindId());
		paramMap.put("createUserId", user.getUserId());
		paramMap.put("createTime", TimeUtil.formatTime(new Date()));
		paramMap.put("bindType", 1);
		UserDao.getInstance().insertRBInfo(paramMap);
	}

	public static boolean canRemoveBind(RegInfo regInfo, Map<String, Object> result) {
		long userId = regInfo.getUserId();
		// 查询玩家的解绑次数,最多两次解绑
		Integer rbCount = UserDao.getInstance().getRemoveBindCount(userId);
		result.put("rbCount", rbCount);
		result.put("maxCount", 2);
		if (rbCount >= 2) {
			result.put("msg", "ID:" + userId + "的玩家解绑次数已达上限");
			return false;
		}
		return true;
	}


	/**
	 * 玩家绑家支付代理ID
	 *
	 * @return
	 * @throws Exception
	 */
	public String bindPayAgencyId() throws Exception {
		long userId = this.getLong("userId");
		String flatId = this.getString("flatId");
		String paySign = this.getString("paySign");

		if(flatId!=null){
			flatId = flatId.replace(" ","+");
		}

		if (!StringUtils.isBlank(paySign)) {
			String payTime = this.getString("payTime");
			String md5 = MD5Util.getStringMD5(payTime + flatId + userId + "7HGO4K61M8N2D9LARSPU");
			if (!md5.equals(paySign)) {
				GameBackLogger.SYS_LOG.info("bindPayAgencyId-->" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}

		} else {
			if (!checkPdkSign()) {
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
		}

		int payBindId = NumberUtils.toInt(this.getString("payBindId"),0);

		Map<String, Object> result = new HashMap<>();

		if(payBindId<=0){
			result.put("msg", "请输入正确的邀请码");
			this.writeMsg(-6, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}else if (payBindId <= 100000) {
			result.put("msg", "暂不能绑定预留ID");
			this.writeMsg(-6, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		// if (888800 == payBindId) {
		// result.put("msg", "错误的邀请码");
		// this.writeMsg(-7, result);
		// return StringResultType.RETURN_ATTRIBUTE_NAME;
		// }

		RegInfo user = userDao.getUser(userId);
		if (user == null) {
			result.put("msg", "没有找到ID:" + userId + "的玩家");
			this.writeMsg(-1, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		if (!flatId.equals(user.getFlatId())) {
			result.put("msg", "flatId与userId不匹配");
			this.writeMsg(-4, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		if (user.getPayBindId() > 0) {
			result.put("msg", "已绑定支付代理ID：" + user.getPayBindId());
			this.writeMsg(-2, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		HashMap<String,Object> agencyInfo = roomCardDao.queryAgencyByAgencyId(payBindId);

		if (agencyInfo == null||agencyInfo.size()==0) {
			result.put("msg", "没有ID为" + payBindId + "的代理商");
			this.writeMsg(-3, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		if (1 == NumberUtils.toInt(String.valueOf(agencyInfo.get("partAdmin")),1)) {
			result.put("msg", "错误的邀请码");
			this.writeMsg(-5, result);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		Map<String, Object> modify = new HashMap<String, Object>();
		modify.put("userId",user.getUserId());
		modify.put("payBindId", payBindId);
		modify.put("payBindTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

		int ret=userDao.updateUserBindPayId(modify);

		GameBackLogger.MONITOR_LOG.info(modify+" result="+ret);
		if (ret>0){
			result.put("msg", "邀请码绑定成功");

			String agencyPf=String.valueOf(agencyInfo.get("pf"));
			if (StringUtils.isNotBlank(agencyPf)&&(!"null".equalsIgnoreCase(agencyPf))){
				if ("1".equals(PropertiesCacheUtil.getValue("open_user_relation",Constants.GAME_FILE))){
					List<UserRelation> list=userRelationDao.selectBaseAll(userId);
					if (list!=null){
						for (UserRelation ur:list){
							ur.setGameCode(ur.getGameCode()+"_"+agencyPf);
							ur.setRegTime(new Date());
							ur.setLoginTime(ur.getRegTime());
							userRelationDao.insert(ur);
						}
					}
				}
				result.put("agencyPf", agencyPf);
			}
		}

		UserExtendInfo userExtendInfo = userDao.getUserExtendinfByUserId(userId);
		int status = 0;
		if (userExtendInfo == null) {
			status = 1;
		} else {
			if (userExtendInfo.getBindSongCard() <= 0) {
				status = 2;
			}
		}
		boolean giveRoomCard = status > 0 && SharedConstants.bindGiveRoomCards > 0;
		result.put("giveRoomCard", giveRoomCard?SharedConstants.bindGiveRoomCards:0);
		this.writeMsg(0, result);
		if (giveRoomCard) {
			UserMessage message = new UserMessage();
			message.setUserId(userId);
			message.setContent("绑定邀请码" + payBindId + "成功，获得房卡 * " + SharedConstants.bindGiveRoomCards);
			message.setTime(new Date());

			int count = UserDao.getInstance().addUserCards(user, 0, SharedConstants.bindGiveRoomCards, 0, null, message, CardSourceType.bindGiveRoomCards);

			if (count > 0) {
				if (1 == status) {
					userDao.insertUserExtendinf(userId, "", payBindId);
				} else if (2 == status) {
					userDao.updateUserBindSongCard(userId, payBindId);
				}
			}
		}

		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 礼包码兑换
	 **/
	public String verifyCdk() {
		Map<String, Object> result = new HashMap<String, Object>();

		// if (!checkPdkSign()) {
		// result.put("code", 4);
		// result.put("msg", "验证出错");
		// this.result = JacksonUtil.writeValueAsString(result);
		// return StringResultType.RETURN_ATTRIBUTE_NAME;
		// }

		try {
			String cdkid = getRequest().getParameter("cdkid");
			long userId = Long.parseLong(getRequest().getParameter("userId"));
			String flatId = getRequest().getParameter("flatId");
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				result.put("code", 6);
				result.put("msg", "不存在角色ID为" + userId + "玩家");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			if (!flatId.equals(user.getFlatId())) {
				result.put("code", 7);
				result.put("msg", "flatId不匹配");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}

			if (StringUtils.isBlank(cdkid)) {
				result.put("code", 1);
				result.put("msg", "兑换码错误");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}

			String cdkKey = cdkid.substring(0, 3);
			CdkAward flag = SysInfManager.getInstance().getCdkAwards().get(cdkKey);
			if (flag == null) {
				result.put("code", 8);
				result.put("msg", "兑换码错误");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			int type = flag.getType();
			if (type == 4) {
				// cdkid不唯一，玩家领过的cdktype唯一
				result = Manager.getInstance().getCdk(userDao, userLotteryStatisticsDao, cdkid, flag, user, false, true, true);
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}

			if (type == 7) {
				// cdkid唯一，玩家领过的cdktype唯一
				result = Manager.getInstance().getCdk(userDao, userLotteryStatisticsDao, cdkid, flag, user, true, false, true);
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}

			// 下面是以前的老代码
			SystemCdk systemCdk = userDao.getSystemCdk(cdkid);
			if (systemCdk == null) {
				result.put("code", 2);
				result.put("msg", "兑换码错误");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			if (!StringUtils.isBlank(systemCdk.getFlatid())) {
				result.put("code", -100);
				result.put("msg", "该兑换码已领取");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}

			// 如果cdk类型是6需要做如下处理
			if (type == 6) {
				// 用户注册时间大于CDK有效开始时间
				if (user.getRegTime().getTime() < TimeUtil.ParseTime(flag.getRegTime()).getTime()) {
					result.put("code", 10);
					result.put("msg", "没有领取资格");
					this.result = JacksonUtil.writeValueAsString(result);
					return StringResultType.RETURN_ATTRIBUTE_NAME;
				}

				// 用户领取时间在CDK失效时间内
				if (new Date().getTime() > TimeUtil.ParseTime(flag.getEndTime()).getTime()) {
					result.put("code", 11);
					result.put("msg", "CDK已失效");
					this.result = JacksonUtil.writeValueAsString(result);
					return StringResultType.RETURN_ATTRIBUTE_NAME;
				}

				// 是否领取过新注册的CDK
				String cdkIds = userDao.getUserExtendinfByUid(userId);
				if (!StringUtils.isBlank(cdkIds)) {
					String[] idArr = cdkIds.split(",");
					String idParm = "";
					boolean temp = false;
					for (int i = 0; i < idArr.length; i++) {
						idParm += idArr[i] + ",";
						if (Integer.parseInt(idArr[i]) == flag.getId()) {
							temp = true;
						}
					}
					if (temp) {
						result.put("code", 12);
						result.put("msg", "已领取过奖励");
						this.result = JacksonUtil.writeValueAsString(result);
						return StringResultType.RETURN_ATTRIBUTE_NAME;
					} else {
						int update = userDao.updateUserCdk(userId, idParm + flag.getId() + ",");
						if (update == 0) {
							result.put("code", 9);
							result.put("msg", "发送房卡失败");
							this.result = JacksonUtil.writeValueAsString(result);
							return StringResultType.RETURN_ATTRIBUTE_NAME;
						}
					}
				} else {
					// userDao.insertUserExtendinf(userId, flag.getId() + ",");
					if (cdkIds == null) {
						userDao.insertUserExtendinf(user.getUserId(), flag.getId() + ",", 0);
					} else {
						int update = userDao.updateUserCdk(user.getUserId(), flag.getId() + ",");
						if (update == 0) {
							result.put("code", 9);
							result.put("msg", "发送房卡失败");
							this.result = JacksonUtil.writeValueAsString(result);
							return StringResultType.RETURN_ATTRIBUTE_NAME;
						}
					}
				}
			}

			// ////////
			// 避免重复领取
			// ////////
			systemCdk = userDao.getSystemCdk(cdkid);
			if (systemCdk == null) {
				result.put("code", 2);
				result.put("msg", "兑换码错误");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			if (!StringUtils.isBlank(systemCdk.getFlatid())) {
				result.put("code", -100);
				result.put("msg", "该兑换码已领取");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			// ////////
			// 避免重复领取
			// ////////
			userDao.updateSystemCdk(flatId, Integer.parseInt("1"), type, cdkid);
			int update = userDao.addUserCards(user, 0, flag.getAwardId(), 0, CardSourceType.receive_cdk);
			if (update == 0) {
				result.put("code", 9);
				result.put("msg", "发送房卡失败");
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			int day = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			userLotteryStatisticsDao.saveUserLotteryStatistics(new UserLotteryStatistics(day, userId, user.getName(), 3, 1));
			result.put("code", 0);
			result.put("cardNumber", flag.getAwardId());
			result.put("msg", "领取成功");

		} catch (Exception e) {
			// exception(result, "userAction--payback.exception", e);
			result.put("code", 5);
			result.put("msg", "领取兑换码出错，请稍后再试");
			GameBackLogger.SYS_LOG.error("领取兑换码出错，请稍后再试", e);
		}

		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	/**
	 * 点击彩票统计
	 */
	public void lotteryStatistics(){
		try{
			long userId = Long.parseLong(getRequest().getParameter("userId"));
			int statisticsType = Integer.parseInt(getRequest().getParameter("statisticsType"));
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				return;
			}
			int day = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			switch(statisticsType){
				case 1:
				case 2:
					userLotteryStatisticsDao.saveUserLotteryStatistics(new UserLotteryStatistics(day, userId, user.getName(), statisticsType, 1));
					break;
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("彩票点击统计出错，请稍后再试", e);
		}
	}

	/**
	 * 大厅代理信息展示
	 */
	public String angencyShow() {
        if (!checkPdkSign()) {
            return StringResultType.RETURN_ATTRIBUTE_NAME;
        }
		Map<String, Object> result = new HashMap<>();
        List<String> list  = AgencyShowDao.getInstance().selectAgencyShow();
        if (list == null) {
            result.put("code", 1);
            result.put("msg", "无可展示代理");
        } else {
            result.put("code", 0);
            result.put("angency", JacksonUtil.writeValueAsString(list));
        }
        this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String redBagAction() throws Exception {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		long userId = Long.parseLong(getRequest().getParameter("userId"));
		RegInfo userInfo = userDao.getUser(userId);
		int requestType = Integer.parseInt(getRequest().getParameter("requestType"));
		int redBagType = 0;
		if(getRequest().getParameter("redBagType") != null) {
			redBagType = Integer.parseInt(getRequest().getParameter("redBagType"));
		}
		Map<String, Object> result = RedBagAction.execute(userDao, userInfo, requestType, redBagType);
		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public OrderDaoImpl getOrderDao() {
		return orderDao;
	}

	public void setOrderDao(OrderDaoImpl orderDao) {
		this.orderDao = orderDao;
	}

	public UserDaoImpl getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public OrderValiDaoImpl getOrderValiDao() {
		return orderValiDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setGameUserDao(GameUserDao gameUserDao) {
		this.gameUserDao = gameUserDao;
	}

	/**
	 * 返回msg
	 *
	 * @param code
	 * @param map
	 * @throws Exception
	 */
	public void writeMsg(int code, Map<String, Object> map) {
		if (map == null) {
			map = new HashMap<String, Object>();
		}
		map.put("code", code);
		this.result = JacksonUtil.writeValueAsString(map);
	}

	/**
	 * 返回msg
	 *
	 * @throws Exception
	 */
	public void writeErrMsg(String errMsg) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", 999);
		map.put("errMsg", errMsg);
		this.result = JacksonUtil.writeValueAsString(map);
	}

	public RoomCardDaoImpl getRoomCardDao() {
		return roomCardDao;
	}

	public void setRoomCardDao(RoomCardDaoImpl roomCardDao) {
		this.roomCardDao = roomCardDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public void setUserRelationDao(UserRelationDaoImpl userRelationDao) {
		this.userRelationDao = userRelationDao;
	}
	public UserLotteryStatisticsDaoImpl getUserLotteryStatisticsDao() {
		return userLotteryStatisticsDao;
	}
	public void setUserLotteryStatisticsDao(UserLotteryStatisticsDaoImpl userLotteryStatisticsDao) {
		this.userLotteryStatisticsDao = userLotteryStatisticsDao;
	}


	/**
	 *
	 * 玩家绑家白金岛代理邀请码
	 *
	 * @return
	 */
	public void bindBjdAgency() {

		long userId = this.getLong("userId");
		String flatId = this.getString("flatId");
		String paySign = this.getString("paySign");
		int agencyId = NumberUtils.toInt(this.getString("payBindId"), 0);
		GameBackLogger.SYS_LOG.info("bindBjdAgency|" + userId + "|" + flatId + "|" + agencyId);
		if (flatId != null) {
			flatId = flatId.replace(" ", "+");
		}
		Map<String, Object> result = new HashMap<>();
		if (!StringUtils.isBlank(paySign)) {
			String payTime = this.getString("payTime");
			String md5 = MD5Util.getStringMD5(payTime + flatId + userId + "7HGO4K61M8N2D9LARSPU");
			if (!md5.equals(paySign)) {
				GameBackLogger.SYS_LOG.info("bindPayAgencyId-->" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
		} else {
			if (!checkPdkSign()) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
		}
		try {
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}else if (!flatId.equals(user.getFlatId())) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
				return;
			} else if (user.getPayBindId() > 0) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_26), getRequest(), getResponse(), false);
				return;
			}
			int bindId = BjdUtil.getBindAgency(user);
			if (bindId > 0) {
				if (user.getPayBindId() == 0) {
					// 修复绑定失败的数据
					Map<String, Object> modify = new HashMap<>();
					modify.put("userId", user.getUserId());
					modify.put("payBindId", bindId);
					modify.put("payBindTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					userDao.updateUserBindPayId(modify);
				}
				if (bindId != agencyId) {
					result.put("msg", "已绑定支付代理ID：" + bindId);
					this.writeMsg(-3, result);
				} else {
					result.put("msg", "邀请码绑定成功");
					this.writeMsg(0, result);
				}
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_26), getRequest(), getResponse(), false);
				return;
			}

			String bindRes = BjdUtil.bindAgencyId(user, agencyId);
			if (!"".equals(bindRes)) {
				result.put("msg", bindRes);
				this.writeMsg(-3, result);
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_26), getRequest(), getResponse(), false);
				return;
			}
			GameBackLogger.SYS_LOG.info("bindBjdAgency|" + userId + "|" + agencyId);

			// 本地写入绑定数据
			Map<String, Object> modify = new HashMap<>();
			modify.put("userId", user.getUserId());
			modify.put("payBindId", agencyId);
			modify.put("payBindTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			int ret = userDao.updateUserBindPayId(modify);
			if (ret > 0) {
				result.put("msg", "邀请码绑定成功");
			}

			UserExtendInfo userExtendInfo = userDao.getUserExtendinfByUserId(userId);
			int status = 0;
			if (userExtendInfo == null) {
				status = 1;
			} else {
				if (userExtendInfo.getBindSongCard() <= 0) {
					status = 2;
				}
			}
			boolean giveRoomCard = status > 0 && SharedConstants.bindGiveRoomCards > 0;
			result.put("giveRoomCard", giveRoomCard ? SharedConstants.bindGiveRoomCards : 0);
			this.writeMsg(0, result);
			if (giveRoomCard) {
				UserMessage message = new UserMessage();
				message.setUserId(userId);
				message.setContent("绑定邀请码" + agencyId + "成功，获得房卡 * " + SharedConstants.bindGiveRoomCards);
				message.setTime(new Date());

				int count = UserDao.getInstance().addUserCards(user, 0, SharedConstants.bindGiveRoomCards, 0, null, message, CardSourceType.bindGiveRoomCards);

				if (count > 0) {
					if (1 == status) {
						userDao.insertUserExtendinf(userId, "", agencyId);
					} else if (2 == status) {
						userDao.updateUserBindSongCard(userId, agencyId);
					}
				}
			}
		} catch (Exception e) {
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_26), getRequest(), getResponse(), false);
			return;
		}
	}


    public boolean checkSessCode(long userId, String sessCode) throws Exception {
        RegInfo user = userDao.getUser(userId);
        if (sessCode == null || user == null || !sessCode.equals(user.getSessCode())) {
            OutputUtil.output(4, "登录信息验证失败！", getRequest(), getResponse(), false);
            return false;
        }
        return true;
    }

    /**
     * 通过玩家ip查询地址信息
     */
    public String getAddressFromIp() {
        long userId = this.getLong("userId");
        Map<String, Object> result = new HashMap<>();
        if (!checkPdkSign()) {
            this.writeMsg(-1, result);
            result.put("msg", "验证失败");
            return StringResultType.RETURN_ATTRIBUTE_NAME;
        }
        try {
            if (!checkSessCode(userId, this.getString("sessCode"))) {
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            RegInfo user = userDao.getUserForceMaster(userId);
            if (user == null) {
                result.put("msg", "没有找到ID:" + userId + "的玩家");
                this.writeMsg(-1, result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }

            String ip = IpUtil.getIpAddr(request);
            LOGGER.info("getAddressFromIp|" + userId + "|" + ip);
            Map<String, Object> map = new HashMap<>();
            map.put("ip", ip);
            int ret = userDao.updateUser(user.getUserId(), map);
            if (ret > 0) {
                int serverId = user.getEnterServer();
                Server server = SysInfManager.getInstance().getServer(serverId);
                if (server != null) {
                    String url = SysInfManager.loadRootUrl(server);
                    if (StringUtils.isNotBlank(url)) {
                        com.sy.mainland.util.HttpUtil.getUrlReturnValue(url + "/online/notice.do?type=SetIp&userId=" + userId + "&timestamp=" + System.currentTimeMillis() + "&message=" + ip);
                    }
                }
            }

            Map<String, String> address = IpAddressUtil.getIpAddress(ip);
            if (address != null) {
                address.put("ip", ip);
                result.put("address", JSON.toJSONString(address));
            }
            this.writeMsg(0, result);
        } catch (Exception e) {
            result.put("msg", "系统异常");
            this.writeMsg(-1, result);
            GameBackLogger.SYS_LOG.error("error", e);
        }
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    /**
     * 用户举报
     */
    public String userReport() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!checkPdkSign()) {
                this.writeMsg(-1, result);
                result.put("msg", "验证失败");
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            long userId = this.getLong("userId", 0);
            String email = this.getString("email");
            String content = this.getString("content");

            if (userId == 0 || StringUtils.isBlank(email) || StringUtils.isBlank(content)) {
                LOGGER.info("BjdAction|userReport|fail|" + userId + "|" + email + "|" + content);
                result.put("msg", "举报失败，参数错误");
                this.writeMsg(-1, result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }

            String res = BjdUtil.userReport(userId, email, content);
            if (!"".equals(res)) {
                LOGGER.info("BjdAction|userReport|fail|" + userId + "|" + email + "|" + content + "|" + res);
                result.put("msg", "举报失败" + res);
                this.writeMsg(-1, result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }

            LOGGER.info("BjdAction|userReport|succ|");
            result.put("msg", LangMsg.getMsg(LangMsg.code_0));
            this.writeMsg(0, result);
            return StringResultType.RETURN_ATTRIBUTE_NAME;
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("error", e);
            result.put("msg", LangMsg.getMsg(LangMsg.code_4));
            this.writeMsg(-1, result);
            return StringResultType.RETURN_ATTRIBUTE_NAME;
        }
    }



	/**
	 * 打开后台管理界面
	 */
	public void getBackStageManagement() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBackStageManagement|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}

			String payBindId = params.get("payBindId");
			List list = userDao.getBindAllUserMsg(Long.parseLong(payBindId));
			JSONArray json = new JSONArray(list);
			OutputUtil.outputJsonArray(json, getRequest(), getResponse(), false);
			LOGGER.info("getBackStageManagement|userReport|succ|");
		} catch (Exception e) {
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 打开后台管理界面
	 */
	public void getBindOneMsg() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("getBindOneMsg|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}

			String payBindId = params.get("payBindId");
			Long armId= NumberUtils.toLong(params.get("armId"),0);
			Map map = userDao.getBindOneMsg(Long.parseLong(payBindId),armId);
			if(map==null)
				map=new HashMap();
			JSONObject json = new JSONObject();
			json.putAll(map);
			OutputUtil.outputJson(0,json, getRequest(), getResponse(), false);
			LOGGER.info("getBindOneMsg|getBindOneMsg|succ|");
		} catch (Exception e) {
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 开启亲友圈权限
	 */
	public void openCreateGroup() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("openCreateGroup|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}
			Long payBindId= NumberUtils.toLong(params.get("payBindId"),0);
			if(userId==payBindId){
				Long armId= NumberUtils.toLong(params.get("armId"),0);
				userDao.updateUserCreateGroup(armId,userId);
			}
			JSONObject json = new JSONObject();
			json.put("msg", LangMsg.getMsg(LangMsg.code_0));
			OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
			LOGGER.info("openCreateGroup|openCreateGroup|succ|");
		} catch (Exception e) {
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 修改靓号
	 */
	public void updateGroupId() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("updateGroupId|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = this.getLong("userId", 0);
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}
			int beforeId= Integer.parseInt(this.getString("beforeId"));
			int afterId= Integer.parseInt(this.getString("afterId"));
			if(userDao.selectGroupBindId(beforeId)!=userId){
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
				return;
			}
			if(userDao.updateGroupId(beforeId,afterId)==0){
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
				return;
			}
			userDao.updateGroupAllUserId(beforeId,afterId);
			JSONObject json = new JSONObject();
			json.put("msg", LangMsg.getMsg(LangMsg.code_0));
			OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
			LOGGER.info("updateGroupId|updateGroupId|succ|");
		} catch (Exception e) {
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			GameBackLogger.SYS_LOG.error("error", e);
		}
	}

	/**
	 * 打开后台管理界面
	 */
	public void queryBindConsumption() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("queryBindConsumption|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}
			Integer payBindId = NumberUtils.toInt(params.get("payBindId"), 0);
			Long dataDate = NumberUtils.toLong(params.get("dataDate"),0);
			if(dataDate==null)
				dataDate=Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			List list = userDao.getBindAllGroupMsg(payBindId,dataDate);
			JSONArray json = new JSONArray(list);
			OutputUtil.outputJsonArray(json, getRequest(), getResponse(), false);
			LOGGER.info("queryBindConsumption|userReport|succ|");
		} catch (Exception e) {
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

	/**
	 * 绑定邀请码
	 */
	public void bindInviteId() {
		Map<String, String> params = null;
		try {
			params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("bindInviteId|params:{}", params);
			if (!checkPdkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long userId = NumberUtils.toLong(params.get("userId"), 0);
			RegInfo user = userDao.getUser(userId);
			if (user == null) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_25), getRequest(), getResponse(), false);
				return;
			}
			Integer payBindId = NumberUtils.toInt(params.get("payBindId"),0);
			userDao.updatebindInviteId(payBindId,userId);
			if(userDao.updatebindInviteId(payBindId,userId)>0){
				JSONObject json = new JSONObject();
				json.put("msg", LangMsg.getMsg(LangMsg.code_0));
				OutputUtil.outputJson(0, json, getRequest(), getResponse(), false);
				LOGGER.info("bindInviteId|userReport|succ|");
			}else {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
				return;
			}
			OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
		} catch (Exception e) {
			LOGGER.error("error|" + e.getMessage(), e);
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
			return;
		}
	}

}
