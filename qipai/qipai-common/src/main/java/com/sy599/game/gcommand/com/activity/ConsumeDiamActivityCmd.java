package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.ConsumeDiamAcitivityConfig;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.List;

/**
 * @author liuping
 * 开房送钻活动处理类
 */
public class ConsumeDiamActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
        MyActivity myactivity = player.getMyActivity();
		UserActivityRecord record = myactivity.getUserActivityRecord();
		ConsumeDiamAcitivityConfig config = (ConsumeDiamAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_comsume_diam);
        int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
    	if(requestType == 1) {// 领取玩家开房送钻档次   领取方式为一次次领取
    		int comsumeDiam = record.getComsumeDiam();// 当天已累计消耗的钻石数量
    		int curReceiveGrade = record.obtainNextRewardGrades();// 当前要领取的档次
    		int needComsumeDiam = config.getNeedConsumeDaim(curReceiveGrade);
    		if(comsumeDiam < needComsumeDiam) {// 未达成条件
    			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_209));
    			return;
    		}
            player.changeCards(needComsumeDiam, 0, true, CardSourceType.activity_consumeDiam);// 获得钻石
			player.writeComMessage(WebSocketMsgType.res_code_com, "您在开房送钻活动中获得钻石x" + needComsumeDiam);
            record.getReceiveGrades().add(curReceiveGrade);
            myactivity.updateActivityRecord(record);
			LogUtil.msgLog.info(player.getName() + "在开房送钻活动中获得钻石x" + needComsumeDiam);
    	}
		JSONObject userJsonObj = getUserJson(config, record);
		ActivityCommand.sendActivityInfo(player, config, userJsonObj);
	}
	
	/**
	 * 获取玩家用户数据
	 * @param config
	 * @param record
	 * @return
	 */
	public JSONObject getUserJson(ConsumeDiamAcitivityConfig config, UserActivityRecord record) {
		JSONObject userJsonObj = new JSONObject();
		userJsonObj.put("comsumeDiam", record.getComsumeDiam());
		userJsonObj.put("receiveGrades", record.getReceiveGrades());
		int comsumeDiam = record.getComsumeDiam();// 当天已累计消耗的钻石数量
		int curReceiveGrade = record.obtainNextRewardGrades();// 当前要领取的档次
		boolean canReceive = false;
		int needComsumeDiam = config.getNeedConsumeDaim(curReceiveGrade);
		if(comsumeDiam >= needComsumeDiam) {// 是否可领取
			canReceive = true;
		}
		userJsonObj.put("canReceive", canReceive);
		return userJsonObj;
	}
	
	@Override
	public void setMsgTypeMap() {	
	}
}
