package com.sy.sanguo.game.pdkuai.manager;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.db.bean.DaikaiTable;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.dao.DaikaiTableDao;
import com.sy599.sanguo.util.TimeUtil;

public class DaikaiManager {

	private static DaikaiManager _inst = new DaikaiManager();

	public static DaikaiManager getInstance() {
		return _inst;
	}

	// 清理代开房间
	public void clearDaikaiTable() {
		Calendar ca = Calendar.getInstance();//得到一个Calendar的实例 
		ca.setTime(new Date()); //设置时间为当前时间 
		ca.add(Calendar.DATE, -1);		
		Date overdueTime = ca.getTime(); //结果
		
		List<DaikaiTable> tables = DaikaiTableDao.getInstance().getNeedClearDaikaiTable(overdueTime);
	
		long daikaiId = 0;
		int state = 0;
		int returnCard = 0;
		long tableId = 0;
		UserMessage message = null;
		String content = null;
		RegInfo user = null;
		for (DaikaiTable table : tables) {
			daikaiId = table.getDaikaiId();
			try {
				user = UserDao.getInstance().getUser(daikaiId);
			} catch (SQLException e) {
				GameBackLogger.SYS_LOG.error("load user_inf error userId : " + daikaiId, e);
			}
			if (user == null) {
				continue;
			}
			message = new UserMessage();
			state = table.getState();
			returnCard = table.getNeedCard();
			tableId = table.getTableId();
			if (0 == state) {// 逾期
				content = "您于" + TimeUtil.formatTime(table.getDaikaiTime()) + "代开的房间号:" + tableId + "已逾期，退还：房卡 * " + returnCard;
			} else {// 没有打完一局
				content = "您于" + TimeUtil.formatTime(table.getDaikaiTime()) + "代开的房间号:" + tableId + "已被解散，退还：房卡 * " + returnCard;
			}
			
			message.setUserId(daikaiId);
			message.setContent(content);
			message.setTime(new Date());
			int count = 0;
			try {
				count = UserDao.getInstance().addUserCards(user, 0, returnCard, 0, null, message, CardSourceType.daikaiTable_clear_returnCard);
			} catch (SQLException e) {
				GameBackLogger.SYS_LOG.error("clearDaikaiTable return card error tableId:" + tableId + " daikaiId:" + daikaiId, e);
			}
			if (count < 0) {
				continue;
			}
//			UserMessageDao.getInstance().saveUserMessage(message);
			DaikaiTableDao.getInstance().clearDaikaiTable(tableId, daikaiId);
		}
	}
	
}
