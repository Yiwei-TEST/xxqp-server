package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.UserDatasDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.staticdata.bean.GradeExpConfig;
import com.sy599.game.staticdata.bean.GradeExpConfigInfo;
import com.sy599.game.util.GameUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.Arrays;
import java.util.List;

/**
 * 芒果玩家用户信息获取
 */
public class MGPlayerInfoCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		long userId = player.getUserId();
		JSONObject json = new JSONObject();
		json.put("userId", player.getUserId());
		json.put("name", player.getName());
		json.put("icon", player.getHeadimgurl());
		json.put("ip", player.getIp());
		json.put("diamond", player.loadAllCards());
		json.put("gold", player.loadAllGolds());
		json.put("totalCount", player.getTotalCount());
		int grade = player.getGoldPlayer().getGrade();
		GradeExpConfigInfo gradeExpConfigInfo = GradeExpConfig.getGradeExpConfigInfo(grade);
		String gradeDesc = (gradeExpConfigInfo != null ) ? gradeExpConfigInfo.getDesc() : "";
		String winStreakNum = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId), "pdk", "all", "maxLs");
		json.put("grade", grade);
		json.put("gradeName", gradeDesc);
		if(winStreakNum == null)
            json.put("winStreakNum", "0");
		else
		    json.put("winStreakNum", winStreakNum);
		List<Integer> wanfas = Arrays.asList(GameUtil.game_type_ah_pdk15, GameUtil.game_type_ah_pdk16);// 芒果跑得快玩法
		json.put("winRate", UserDao.getInstance().getUserWinRate(userId, wanfas));
		json.put("championNum", MatchDao.getInstance().selectMatchRankNum(userId, 1));
		json.put("secondNum", MatchDao.getInstance().selectMatchRankNum(userId, 2));
		json.put("thirdNum", MatchDao.getInstance().selectMatchRankNum(userId, 3));
		player.writeComMessage(WebSocketMsgType.sc_mgPlayerInfo, json.toJSONString());
	}

	@Override
	public void setMsgTypeMap() {
	}
}
