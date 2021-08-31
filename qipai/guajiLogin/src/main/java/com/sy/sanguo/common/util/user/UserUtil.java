package com.sy.sanguo.common.util.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.TypeReference;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.bean.RegInfo;

public class UserUtil {

	public static void tickOff(final int nowSId, final RegInfo userInfo) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (userInfo == null) {
					return;
				}
				String sid = userInfo.getPlayedSid();
				if (StringUtils.isBlank(sid)) {
					return;
				}
				List<Integer> servers = JacksonUtil.readValue(userInfo.getPlayedSid(), new TypeReference<List<Integer>>() {
				});

				for (int serverId : servers) {
					if (nowSId == serverId) {
						continue;
					}
					Map<String, String> map = new HashMap<String, String>();
					map.put("type", 17 + "");
					map.put("funcType", 19 + "");
					map.put("flatId", userInfo.getFlatId());
					map.put("serverId", serverId + "");
					map.put("pf", userInfo.getPf());
					map.put("forced", 0 + "");//1为强制踢人下线
					String post = GameUtil.send(serverId, map);
					if (!userInfo.getPf().equals("self") && !StringUtils.isBlank(post)) {
						GameBackLogger.SYS_LOG.info("tickoff :" + JacksonUtil.writeValueAsString(map) + " post:" + post);

					}
				}
			}
		}).start();

	}
}
