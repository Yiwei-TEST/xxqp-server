package com.sy599.game.gameSite;

import com.sy.mainland.util.IpUtil;
import com.sy599.game.character.Player;
import com.sy599.game.common.action.BaseAction;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.Md5CheckUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

public class GameSiteAction extends BaseAction {

	@Override
	public void execute() throws Exception {
		switch (this.getInt("funcType")) {
		case 1:
			gameSitePush();
			break;
		case 2:
			pushApplyNumber();
			break;
		case 3:
			pushDismissMatch();
			break;
		case 4:
			consumeUserCards();
			break;
		default:
			break;
		}
	}

	private void consumeUserCards() throws Exception {
		String flatId = this.getString("flatId");
		String signal = this.getString("sign");
		String time = this.getString("time");
		int cards = this.getInt("cards");

		if (IpUtil.isNotIntranet(request)&&!Md5CheckUtil.checkConsumeMd5(time, flatId, signal)) {
			this.writeErrMsg(1, "md5 err");
			LogUtil.e("consumeUserCards error,md5 err,flatId:" + flatId + "cards:" + cards);
			return;
		}

		String ip = request.getRemoteAddr();

		if (StringUtils.isBlank(ip)) {
			this.writeErrMsg(2, "ip is null");
			return;
		}

		// LogUtil.monitor_i("consumeUserCards execute-->ip:" +
		// request.getRemoteAddr() + ":" + flatId + " -" + flatId + " -" + cards
		// + " -" + cards);

		Player player = NumberUtils.isDigits(flatId)?PlayerManager.getInstance().getPlayer(Long.parseLong(flatId)):PlayerManager.getInstance().getPlayer(flatId,"");
		if (player == null) {
			this.writeErrMsg(3, "player is null");
			return;
		}

		if (player.isHasCards(cards)) {
			player.changeCards(0, -cards, true, CardSourceType.unknown);
		} else {
			this.writeErrMsg(4, "cards not enough");
			return;
		}

		// LogUtil.monitor_i("---------" + flatId + "---pay:orderId" + orderId +
		// " :amount:" + amount + "----");
		this.writeErrMsg(0, "success");
	}

	private void pushDismissMatch() {
		String userIds = this.getString("userIds");
		String pushContent = this.getString("pushContent");

		if (StringUtils.isBlank(userIds) || StringUtils.isBlank(pushContent)) {
			return;
		}

		String[] ids = userIds.split(",");

		for (String userId : ids) {
			player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
			if (player != null) {
				player.writeComMessage(WebSocketMsgType.res_com_code_dismissmatch, pushContent);
			}
		}
	}

	private void pushApplyNumber() {
		String userIdStr = this.getString("userIdStr");
		String[] ids = userIdStr.split(",");
		int gameSiteId = this.getInt("gameSiteId");
		int applyNumber = this.getInt("applyNumber");

		Player player = null;
		Map<String, Object> msgMap = null;
		for (String userId : ids) {
			player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));

			if (player != null) {
				msgMap = new HashMap<>();
				msgMap.put("gameSiteId", gameSiteId);
				msgMap.put("applyNumber", applyNumber);
				player.writeComMessage(WebSocketMsgType.sc_code_pushapplynumber, JacksonUtil.writeValueAsString(msgMap));
			}
		}

	}

	private void gameSitePush() {
		String userIds = this.getString("userIds");
		if (StringUtils.isBlank(userIds)) {
			return;
		}
		String[] ids = userIds.split(",");
		String gameSiteName = this.getString("gameSiteName");
		long countDown = this.getLong("countDown");
		int gameSiteId = this.getInt("gameSiteId");
		int durationTime = this.getInt("durationTime");
		String configRound = this.getString("configRound");
		String configBout = this.getString("configBout");
		String connectHost = this.getString("connectHost");

		Player player = null;
		Map<String, Object> msgMap = null;
		for (String userId : ids) {
			player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
			if (player != null) {
				msgMap = new HashMap<>();
				msgMap.put("gameSiteId", gameSiteId);
				msgMap.put("gameSiteName", gameSiteName);
				msgMap.put("durationTime", durationTime);
				msgMap.put("configRound", configRound);
				msgMap.put("configBout", configBout);
				msgMap.put("countDown", countDown);
				msgMap.put("currTime", System.currentTimeMillis());
				msgMap.put("connectHost", connectHost);
				player.writeComMessage(WebSocketMsgType.sc_code_gamesitepush, JacksonUtil.writeValueAsString(msgMap));
				LogUtil.monitor_i("macth--> gamesitepush uid: " + player.getUserId() + " name:" + player.getName());
			}
		}
	}

}
