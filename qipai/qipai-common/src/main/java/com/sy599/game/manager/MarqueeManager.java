package com.sy599.game.manager;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.SystemMarquee;
import com.sy599.game.db.dao.MarqueeDao;
import com.sy599.game.msg.MarqueeMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarqueeManager {
	private static MarqueeManager _inst = new MarqueeManager();
	private List<SystemMarquee> marqueeList;
	private long updateTime;

	public static MarqueeManager getInstance() {
		return _inst;
	}

	public boolean refreshDate() {
		Date date = MarqueeDao.getInstance().selectNewMarqueeTime();
		if (date != null && date.getTime() > updateTime) {
			updateTime = date.getTime();
			return true;
		}
		return false;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void initData() {
		if (refreshDate()) {
			marqueeList = MarqueeDao.getInstance().loadMarquee();
		}
	}

	public void check() {
		if (refreshDate()) {
			marqueeList = MarqueeDao.getInstance().loadMarquee();
			List<MarqueeMsg> list = getMarquee();
			String marqueeMsg = JacksonUtil.writeValueAsString(list);
			LogUtil.monitor_i("check marqueeMsg" + marqueeMsg);
			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_marquee, marqueeMsg);
			WebSocketManager.broadMsg(builder.build());
		}
	}

	public List<MarqueeMsg> getMarquee() {
		List<MarqueeMsg> msgList = new ArrayList<>();
		if (marqueeList != null) {
			long now = TimeUtil.currentTimeMillis();
			for (SystemMarquee marquee : marqueeList) {
				// if (marquee.getStartTime() != null &&
				// marquee.getStartTime().getTime() > now) {
				// continue;
				// }

				if (marquee.getEndTime() != null && marquee.getEndTime().getTime() < now) {
					continue;
				}

				MarqueeMsg msg = new MarqueeMsg();
				msg.setContent(marquee.getContent());
				msg.setId(marquee.getId());
				msg.setType(marquee.getType());
				msg.setDelay(marquee.getDelay());
				msg.setRound(marquee.getRound());
				if (marquee.getEndTime() != null) {
					msg.setEndtime(marquee.getEndTime().getTime());
				}
				if (marquee.getStartTime() != null) {
					msg.setStarttime(marquee.getStartTime().getTime());
				}
				msgList.add(msg);
			}
		}
		return msgList;

	}

	public void sendMarquee(String content, int round) {
		sendMarquee(content,round,1,null);
	}

	/**
	 * 发送跑马灯
	 *
	 * @param content 消息内容
	 * @param round 次数
	 * @param type 类型0房间外1房间内2房间内和外3金币场房间内
	 * @param player 为空时发给所有在线玩家，否则发给单个玩家
	 */
	public void sendMarquee(String content, int round, int type, Player player) {
		MarqueeMsg msg = new MarqueeMsg();
		msg.setContent(content);
		msg.setRound(round);
		msg.setType(type);

		List<MarqueeMsg> list = new ArrayList<>(1);
		list.add(msg);
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_marquee, JacksonUtil.writeValueAsString(list));
		if (player == null) {
			WebSocketManager.broadMsg(builder.build());
		}else{
			player.writeSocket(builder.build());
		}
	}

	public void sendMarquee(MarqueeMsg msg) {
		List<MarqueeMsg> list = new ArrayList<>(1);
		list.add(msg);
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_marquee, JacksonUtil.writeValueAsString(list));
		WebSocketManager.broadMsg(builder.build());
	}
}
