package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.dao.RoomCardDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端协议获取微信客服号
 */
public class KeFuHaoCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
		List<Integer> lists = req.getParamsList();
		int requestType = 0;
		if (lists == null || lists.size() == 0) {
			requestType = 0;
		} else {
			requestType = req.getParams(0);
		}
		if(requestType == 0) {
			String weixin_keFuHao = ResourcesConfigsUtil.loadServerPropertyValue("weixin_keFuHao");
			if (weixin_keFuHao == null) {
				LogUtil.msgLog.error("数据库未配置客服号！");
			} else
				player.writeComMessage(WebSocketMsgType.sc_kefuhao, requestType, weixin_keFuHao);
		} else {
			String diQuKefuHao = "";
			RegInfo regInfo = UserDao.getInstance().selectUserByUserId(player.getUserId());
			long payBindId = regInfo.getPayBindId();
			if(payBindId != 0) {
				HashMap<String, Object> agencyInfo = RoomCardDao.getInstance().queryAgencyByAgencyId((int)payBindId);
				if(agencyInfo != null) {
					String pf = (String)agencyInfo.get("pf");
					if(StringUtil.isBlank(pf)) {
						pf = "lanzhou";
					}
					try {
						String weixin_pf_keFuHao = ResourcesConfigsUtil.loadServerPropertyValue("weixin_pf_keFuHao");
						String[] arr = weixin_pf_keFuHao.split(";");
						for (int i = 0; i < arr.length; i++) {
							String[] tempArr = arr[i].split("_");
							if(tempArr[0].equals(pf)) {
								diQuKefuHao = arr[i];
								break;
							}
						}
					} catch (Exception e) {
						LogUtil.msgLog.error("数据库未配置pf对应区域客服号！");
					}
					if(StringUtil.isBlank(diQuKefuHao)) {
						diQuKefuHao = "";
					}
					System.out.println("diQuKefuHao:" + diQuKefuHao);
				}
			}
			player.writeComMessage(WebSocketMsgType.sc_kefuhao, requestType, diQuKefuHao);
		}
	}

	@Override
	public void setMsgTypeMap() {
	}
}
