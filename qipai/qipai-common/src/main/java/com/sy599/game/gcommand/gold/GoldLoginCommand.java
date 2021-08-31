package com.sy599.game.gcommand.gold;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoldLoginCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
		List<String> params = req.getStrParamsList();
		List<Integer> integers = req.getParamsList();
		int strSize = params==null?0:params.size();
		int intSize = integers==null?0:integers.size();

		String gameCodes;
		if (strSize>0){
			gameCodes=params.get(0);
		}else{
			gameCodes="dtz";//默认 打筒子金币场
		}
		int int0 = intSize > 0?integers.get(0).intValue():0;//是否加载在线人数

		String[] gcs = gameCodes.split(",");
		List<String> retList = new ArrayList<>(2*gcs.length);

		for (String gameCode:gcs){
			if (StringUtils.isNotBlank(gameCode)){
				String modeIds = ResourcesConfigsUtil.loadServerPropertyValue(new StringBuilder().append("game_").append(gameCode).append("_gold_room_modes").toString());
				List<JSONObject> jsonObjects;
				if (StringUtils.isNotBlank(modeIds)){
					String[] modes=modeIds.split(",");
					jsonObjects=new ArrayList<>(modes.length);
					for (String modeId:modes){
						if (NumberUtils.isDigits(modeId)){
							JSONObject jsonObject=new JSONObject();
							jsonObject.put("modeId",modeId);
							List<Integer> msgs = GameConfigUtil.getIntsList(1,modeId);
							if (msgs!=null&&msgs.size()>=6){
								jsonObject.put("min",msgs.get(0));
								jsonObject.put("max",msgs.get(1));
								jsonObject.put("pay", PayConfigUtil.get(msgs.get(5),msgs.get(2),msgs.get(3),0,modeId));
							}
							int ratio=GameConfigUtil.loadGoldRatio(modeId);
							jsonObject.put("ratio",ratio);

							if (int0==1){
								jsonObject.put("online",GoldRoomDao.getInstance().countOnline(modeId));
							}

							jsonObjects.add(jsonObject);
						}
					}
				}else{
					jsonObjects=Collections.emptyList();
				}

				retList.add(JSON.toJSONString(jsonObjects));
				retList.add(gameCode);
			}
		}

		player.writeComMessage(WebSocketMsgType.res_code_gold_room_level, retList);

		player.loadGoldPlayer(true);
	}

	@Override
	public void setMsgTypeMap() {
	}

}
