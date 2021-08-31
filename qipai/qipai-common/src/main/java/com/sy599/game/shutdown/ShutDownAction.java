package com.sy599.game.shutdown;

import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.action.BaseAction;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SystemCommonInfoType;
import com.sy599.game.db.bean.SystemCommonInfo;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.SystemCommonInfoManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.WebSocketManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShutDownAction extends BaseAction {
	public static List<List<Integer>> testPai;
	public static int testWanFa;

	@Override
	public void execute() throws Exception {
		switch (this.getInt("funcType")) {
		case 1:
			shutdown();
			break;
		case 2:
			test();
			break;
		case 3:
			testPai();
			break;
		case 4:
			consumeCards();
			break;
		case 5:
			marquee();
			break;
		case 100:
			generateId();
			break;
		case 101:
		    moZp();
			break;
		case 102:
			zp();
			break;
		default:
			break;
		}
	}

	/**
	 * 摸牌做牌
	 * @throws Exception
	 */
	private void zp() throws Exception {
		if (!GameServerConfig.isDeveloper()) {
			writeMessage("非测试环境不能调牌！");
			return;
		}
		LogUtil.msg("shutdown testPai-->ip:" + request.getRemoteAddr() + ":" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
		long tableId = this.getLong("tableId", 0);
		int wanfa = this.getInt("wanfa", 0);
		long zpUser = this.getLong("zpUser");
		int zpValue = this.getInt("zpValue", 0);
		if(zpUser == 0 || zpValue ==0) {
			writeMessage("zpUser或者zpValue为空");
			return;
		}
		if (tableId == 0) {
			testPai = new ArrayList<>();
			testWanFa = wanfa;
			writeMessage("设置通用牌成功");
			return;
		}
		BaseTable table = TableManager.getInstance().getTable(tableId);
		if (table == null) {
			writeMessage("没有找到该房间-->" + tableId);
			return;
		}
		Map<Long, Player> seatPlayers = table.getPlayerMap();
		if(!seatPlayers.containsKey(zpUser)) {
			writeMessage("没有找到该用户-->" + zpUser);
			return;
		}
		table.setZpMap(zpUser, zpValue);
		LogUtil.msg(tableId + "房间,玩家" +zpUser + "摸牌做牌：" + zpValue);
		writeMessage("设置成功");
	}

	private void marquee() throws Exception {
		String content = this.getString("content");
		int round = this.getInt("round", 1);

		String ip = request.getRemoteAddr();
		if (StringUtils.isBlank(ip)) {
			return;
		}
		LogUtil.msg("marquee execute-->ip:" + request.getRemoteAddr() + " c:" + content + " r:" + round);
		if (!GameServerConfig.isDebug() && !ip.equals("127.0.0.1")) {
			return;
		}
		if (StringUtils.isBlank(content)) {
			writeMessage("content 内容不能为空");
			return;
		}

		if (round <= 0) {
			writeMessage("round不能为0");
			return;
		}

		if (round > 20) {
			writeMessage("round不能超过20");
			return;
		}
		MarqueeManager.getInstance().sendMarquee(content, round);
		writeMessage("发送:" + content + " 轮数:" + round);
	}

	private void generateId() throws Exception {
		String ip = request.getRemoteAddr();
		if (StringUtils.isBlank(ip)) {
			return;
		}
		if (!ip.equals("127.0.0.1")) {
			return;
		}
		long tableid = TableManager.getInstance().generateId(100l, 1,0);
		writeMessage("generateId -->" + tableid);
	}

	private void consumeCards() throws Exception {
		String ip = request.getRemoteAddr();
		if (StringUtils.isBlank(ip)) {
			return;
		}
		String consume = this.getString("consume");
		LogUtil.msg("consumeCards execute-->ip:" + request.getRemoteAddr() + ":" + consume);
		if (!ip.equals("127.0.0.1")) {
			return;
		}
		if (StringUtils.isBlank(consume)) {
			writeMessage("修改失败 consume -->" + consume);
			return;
		}
		SystemCommonInfo info = SystemCommonInfoDao.getInstance().select(SystemCommonInfoType.isConsumeCards.name());
		info.setContent(consume);
		SystemCommonInfoManager.getInstance().updateSystemCommonInfo(info);
		if (info.getContent().equals("1")) {
			SharedConstants.consumecards = true;
		} else {
			SharedConstants.consumecards = false;
		}
		writeMessage("修改成功 consumecards_type_csmajiang-->" + SharedConstants.consumecards);
	}

	private void shutdown() throws Exception {
		String ip = request.getRemoteAddr();
		if (StringUtils.isBlank(ip)) {
			return;
		}
		String msg = this.getString("msg");
		LogUtil.msg("shutdown execute-->ip:" + request.getRemoteAddr() + ":" + msg);
		if (!ip.equals("127.0.0.1")) {
			return;
		}
		if (!StringUtils.isBlank(msg)) {
			msg = msg.replace("\\n", "\n");
		}
		WebSocketManager.shudown(msg);
		this.writeErrMsg(0, msg);
	}

	private void test() {
		LogUtil.msg("test-->ip:" + request.getRemoteAddr());
	}

	private void testPai() throws Exception {
		/*if (!GameServerConfig.isDebug()) {
			writeMessage("该服不是测试服");
			return;
		}*/
		LogUtil.msg("shutdown testPai-->ip:" + request.getRemoteAddr() + ":" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
		long tableId = this.getLong("tableId", 0);
		int wanfa = this.getInt("wanfa", 0);
		String pais = this.getString("pais");
		int cutPai = this.getInt("cutPai", 0);

		if (StringUtils.isBlank(pais)) {
			writeMessage("没有pais-->" + tableId);
			return;
		}

		String[] values = pais.split("_");

		if (tableId == 0) {
			testPai = new ArrayList<>();
			testWanFa = wanfa;
			for (String value : values) {
				List<Integer> pai = StringUtil.explodeToIntList(value);
				testPai.add(pai);
			}
			writeMessage("设置通用牌成功");
			return;
		}
		BaseTable table = TableManager.getInstance().getTable(tableId);
		if (table == null) {
			writeMessage("没有找到该房间-->" + tableId);
			return;
		}

		/*List<List<Integer>> ap = new ArrayList<>();

		boolean find14 = false;
		for (String value : values) {
			List<Integer> majiangs = StringUtil.explodeToIntList(value);
			if (majiangs != null) {
				if (majiangs.size() != 14 && majiangs.size() != 13 && majiangs.size() != 1) {
					// writeMessage("牌数量错误-->" + majiangs.size());
					// return;
				}
				if (majiangs.size() == 14 || majiangs.size() == 1) {
					find14 = true;
					ap.add(0, majiangs);
				} else {
					ap.add(majiangs);
				}
			}

		}
		if (!find14) {
			writeMessage("没有14张-->");
			return;
		}

		table.setZp(ap);*/

		testPai = new ArrayList<>();
		testWanFa = wanfa;
		for (String value : values) {
			List<Integer> pai = StringUtil.explodeToIntList(value);
			testPai.add(pai);
		}
//		if(table instanceof TenthirtyTable) {
//			((TenthirtyTable) table).setCp(cutPai);
//		}
		table.setZp(testPai);
		writeMessage("设置成功");

	}

    /**
     * 摸牌做牌 （针对某个房间 某个玩家）
     *
     * @throws Exception
     */
    private void moZp() throws Exception {
        LogUtil.msg("shutdown|moZp|" + request.getRemoteAddr() + ":" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
        if(!GameServerConfig.isDebug()){
            writeMessage("非测试环境，无法使用该功能");
            return;
        }
        long tableId = this.getLong("tableId", 0);
        long userId = this.getLong("zpUser");
        int value = this.getInt("zpValue", 0);
        if (tableId <= 0 || userId <= 0 || value <= 0) {
            writeMessage("参数错误:tableId=" + tableId + ",userId=" + userId + ",value=" + value);
            return;
        }
        BaseTable table = TableManager.getInstance().getTable(tableId);
        if (table == null) {
            writeMessage("没有找到该房间-->" + tableId);
            return;
        }
        Map<Long, Player> seatPlayers = table.getPlayerMap();
        if (!seatPlayers.containsKey(userId)) {
            writeMessage("没有找到该用户-->" + userId);
            return;
        }
        table.setZpMap(userId, value);
        writeMessage("设置成功:tableId=" + tableId + ",userId=" + userId + ",value=" + value);
    }
}
