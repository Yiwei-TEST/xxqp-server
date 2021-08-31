package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.CommonAcitivityConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 免费开房活动
 * @author liuping
 */
public class FreeGameActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
	    int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
	    if(requestType != 0 && requestType != 1){
	    	player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
	    	return;
	    }
	    CommonAcitivityConfig config = (CommonAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_free_game);
	    JSONObject userJsonObj = new JSONObject();
	    DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
	    String activityTime = df.format(config.getStartTime()) + "-" + df.format(config.getEndTime()) + config.getRewards();
	    userJsonObj.put("activityDate", activityTime);
	    userJsonObj.put("freeTime", config.getParams());
	    ActivityCommand.sendActivityInfo(player, config, userJsonObj);
	}

	@Override
	public void setMsgTypeMap() {
	}

}
