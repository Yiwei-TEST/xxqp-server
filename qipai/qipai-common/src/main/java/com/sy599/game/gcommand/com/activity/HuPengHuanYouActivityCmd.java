package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.RedBagSourceType;
import com.sy599.game.db.bean.RedBagInfo;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.RedBagInfoDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.CommonAcitivityConfig;
import com.sy599.game.staticdata.bean.OldDaiNewAcitivityConfig;
import com.sy599.game.util.*;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author liuping
 * 呼朋唤友活动处理类
 */
public class HuPengHuanYouActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
	    int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
		int requestArea = 0;
		if(reqParams.size() > 1)
			requestArea = reqParams.get(1);// params参数第1位 传递请求区域 0活动专区入口 1单独活动入口
	    CommonAcitivityConfig config = (CommonAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_hu_peng_huan_you);
		Date startTime = config.getStartTime();// 活动开始时间
		String []rewards = config.getRewards().split("_");
		String []grades = config.getParams().split("_");
		int maxProcess = Integer.parseInt(grades[grades.length - 1]);
		int processPerRedbag = Integer.parseInt(grades[0]);
		int finishGameNum = Integer.parseInt(rewards[0]);
		int perMenRedbag = Integer.parseInt(rewards[1]);
		Date endTime = new Date(TimeUtil.parseTimeInMillis(rewards[2]));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        long userId = player.getUserId();
		List<RedBagInfo> redbags = RedBagInfoDao.getInstance().getUserRedBagInfosBySourceType(userId, RedBagSourceType.hupenghuanyou_redbag.getType());
		List<Map> totalDownloads = UserDao.getInstance().getDownloadData(startTime, endTime, player.getUserId());// 获得累计下载
		int redbagNum = (redbags != null) ? redbags.size() : 0;
		int downLoadNum = (totalDownloads != null) ? totalDownloads.size() : 0;
		int index = 1;
		int process = 0;
		JSONArray ja = new JSONArray();
		Set<String> deviceCodes = new HashSet<>();
		for (Map map : totalDownloads) {// 活动期间总下载个数
			String name = (String) (map.get("name"));
			String deviceCode = (String) (map.get("deviceCode"));
			if(!StringUtil.isBlank(deviceCode)) {// 空设备号默认允许
				if(!deviceCodes.contains(deviceCode))
					deviceCodes.add(deviceCode);
				else {
					LogUtil.msgLog.error("相同设备：" + deviceCode + "--玩家：" + player.getName() + "---邀请玩家：" + name);
					continue;//同设备登录的，只计算一个邀请名额
				}
			}
			String extend = (String) map.get("extend");
			JsonWrapper wrapper = new JsonWrapper(extend);
			String victoryStr = wrapper.getString(5);
			int winCount = 0;// 玩家赢的局数
			if (!StringUtils.isBlank(victoryStr)) {
				Map<Integer, Integer> victoryCountMap = DataMapUtil.implode(victoryStr);
				for (Map.Entry<Integer,Integer> kv : victoryCountMap.entrySet()){
					if(kv.getValue() != null) {
						winCount += kv.getValue().intValue();
					}
				}
			}
			Date date = (Date) (map.get("regTime"));
			String dateStr = dataFormat.format(date);
			long downLoadUserId = (long) (map.get("userId"));
			long totalBureau = (Long) (map.get("totalBureau"));
			JSONObject jo = new JSONObject();
			jo.put("index", index);
			jo.put("userId", downLoadUserId);
			jo.put("name", name);
			jo.put("regTime", dateStr);
			boolean active = (totalBureau >= finishGameNum && winCount >= 1) ? true : false;
			jo.put("active", active);
			if(active)// 玩家必须完成固定局数 并且至少赢一局
				process ++;
			ja.add(jo);
			index++;
		}
		if(process > maxProcess)// 只能领取3个红包
			process = maxProcess;
		int addRedbagNum = process / processPerRedbag - redbagNum;
		if(addRedbagNum > 0) {// 主动触发生成红包
			if(redbags == null)
				redbags = new ArrayList<>();
			for (int addNum = 1; addNum <= addRedbagNum; addNum++) {
				RedBagInfo redBagInfo = new RedBagInfo(userId, 2, perMenRedbag, new Date(), null, RedBagSourceType.hupenghuanyou_redbag);
				RedBagInfoDao.getInstance().saveRedBagInfo(redBagInfo);
				redbags.add(redBagInfo);
			}
		}
		int receiveRedbag = 0;
		if(redbags != null) {// 已领取的红包个数
			for (RedBagInfo redBagInfo : redbags) {
				if(redBagInfo.getDrawDate() != null)
					receiveRedbag++;
			}
		}
    	JSONObject userJsonInfo = new JSONObject();
		userJsonInfo.put("process", process);// 进度值
		userJsonInfo.put("receiveRedbag", receiveRedbag);// 已领取的红包个数
		userJsonInfo.put("myInvites", ja);// 我的邀请记录
		userJsonInfo.put("requestArea", requestArea);
        ActivityCommand.sendActivityInfo(player, config, userJsonInfo);
	}
	
	@Override
	public void setMsgTypeMap() {
	}
}
