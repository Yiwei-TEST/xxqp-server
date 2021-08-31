package com.sy599.game.message;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.UserMessage;
import com.sy599.game.db.dao.UserMessageDao;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.util.LogUtil;

import java.util.Date;

public final class MessageUtil {

	public static boolean sendMessage(UserMessageEnum msgType, Object user, String content,String award) {
		return sendMessage(true,true,msgType,user,content,award);
	}

	public static boolean sendMessage(boolean withTime, boolean saveDb, UserMessageEnum msgType, Object user, String content,String award) {
		try {
			Date now = new Date();

			UserMessage info = new UserMessage();
			info.setTime(now);
			info.setType(msgType.type());
			info.setAward(award);
			if (withTime) {
				info.setContent(new StringBuilder().append(CommonUtil.dateTimeToString(now)).append(" ").append(content).toString());
			} else {
				info.setContent(content);
			}

			long userId;
			boolean isPlayer = user instanceof Player;
			if (isPlayer) {
				userId = ((Player) user).getUserId();
			} else if (user instanceof RegInfo) {
				userId = ((RegInfo) user).getUserId();
			} else {
				userId = CommonUtil.object2Long(user);
			}
			info.setUserId(userId);

			if (isPlayer) {
				return ((Player) user).getMyMessage().addMessage(info, saveDb)!=null;
			}else if (saveDb){
				return UserMessageDao.getInstance().saveUserMessage(info)>0;
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
		return false;
	}
}
