package com.sy599.game.message;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.DBNotice;
import com.sy599.game.db.bean.UserMessage;
import com.sy599.game.db.dao.NoticeDao;
import com.sy599.game.db.dao.UserMessageDao;
import com.sy599.game.msg.serverPacket.MessageResMsg.NoticeRes;
import com.sy599.game.msg.serverPacket.MessageResMsg.NoticelistRes;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyMessage {
	private boolean isLoad;
	private Player player;
	private Map<Long, UserMessage> messageMap;

	public MyMessage(Player player) {
		this.player = player;
		this.messageMap = new ConcurrentHashMap<>();
	}

	private void loadFromDB() {
		List<UserMessage> list = UserMessageDao.getInstance().selectUserMessage(player.getUserId());
		if (list != null) {
			for (UserMessage message : list) {
				messageMap.put(message.getId(), message);
			}
		}
	}

	public List<UserMessage> getAllMessage() {
		if (!isLoad) {
			loadFromDB();
		}

		List<UserMessage> list = new ArrayList<>(messageMap.values());
		Collections.sort(list, new Comparator<UserMessage>() {
			@Override
			public int compare(UserMessage o1, UserMessage o2) {
				if (o1.getTime().getTime() > o2.getTime().getTime()) {
					return -1;
				}
				if (o1.getTime().getTime() < o2.getTime().getTime()) {
					return 1;
				}
				return 0;
			}
		});
		return list;
	}

	public NoticelistRes buildMessageListRes() {
		if (!isLoad) {
			loadFromDB();
		}

		List<NoticeRes> list = new ArrayList<>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String temp = ResourcesConfigsUtil.loadServerPropertyValue("tipWithCard","2");

		List<DBNotice> notices = NoticeDao.getInstance().loadNotice();
		if (notices != null) {
			for (DBNotice notice : notices) {
				if (StringUtils.isBlank(notice.getContent()) || notice.getUpdatetime() == null){
					continue;
				}
				NoticeRes.Builder messageBuilder = NoticeRes.newBuilder();
				messageBuilder.setId(String.valueOf(notice.getId()));
				messageBuilder.setType(99);
				messageBuilder.setUserId(String.valueOf(player.getUserId()));

				if ("1".equals(temp)){
					messageBuilder.setContent(notice.getContent().replace("钻石","房卡"));
				}else if ("2".equals(temp)){
					messageBuilder.setContent(notice.getContent().replace("房卡","钻石"));
				}else{
					messageBuilder.setContent(notice.getContent());
				}

				messageBuilder.setTime(df.format(notice.getUpdatetime()));
				list.add(messageBuilder.build());
			}
		}

		List<UserMessage> messageList = getAllMessage();
		int i = 0;
		for (UserMessage message : messageList) {
			if (StringUtils.isBlank(message.getContent()) || message.getTime() == null){
				continue;
			}

			NoticeRes.Builder messageBuilder = NoticeRes.newBuilder();
			messageBuilder.setId(String.valueOf(message.getId()));
			messageBuilder.setUserId(String.valueOf(message.getUserId()));
			messageBuilder.setType(message.getType());

			if ("1".equals(temp)){
				messageBuilder.setContent(message.getContent().replace("钻石","房卡"));
			}else if ("2".equals(temp)){
				messageBuilder.setContent(message.getContent().replace("房卡","钻石"));
			}else{
				messageBuilder.setContent(message.getContent());
			}

			messageBuilder.setTime(df.format(message.getTime()));
			if (StringUtils.isNotBlank(message.getAward())) {
				if ("1".equals(temp)){
					messageBuilder.setAward(message.getAward().replace("钻石","房卡"));
				}else if ("2".equals(temp)){
					messageBuilder.setAward(message.getAward().replace("房卡","钻石"));
				}else{
					messageBuilder.setAward(message.getAward());
				}

			}
			list.add(messageBuilder.build());
			i++;
			if (i > 30) {
				break;
			}
		}
		if (list.size()==0){
			return NoticelistRes.newBuilder().build();
		}else {
			NoticelistRes.Builder res = NoticelistRes.newBuilder();
			res.addAllMessages(list);
			return res.build();
		}
	}

	public UserMessage addMessage(UserMessage message, boolean saveDB) {
		if (saveDB) {
			long id = UserMessageDao.getInstance().saveUserMessage(message);
			if (id == 0) {
				return null;
			}
		}

		messageMap.put(message.getId(), message);
		return message;
	}

	public void delete(long id) {
		messageMap.remove(id);
		UserMessageDao.getInstance().delete(id);
	}

	public boolean isLoad() {
		return isLoad;
	}

	public void setLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}
}
