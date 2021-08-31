package com.sy.sanguo.common.util.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.bean.Server;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.service.SysInfManager;

public class GameUtil {

    public static final String notify_type_updateUser = "notifyUpdateUser";
    public static final String notify_type_changeGolds = "notifyChangGolds";
    public static final String notify_type_goldRoomGroupId = "goldRoomGroupId";
    public static final String notify_type_changCoinAndNotify = "changCoinAndNotify";
    public static final String notify_type_groupUserLevelUp = "notifyGroupUserLevelUp";
    public static final String notify_type_creditUpdate = "notifyCreditUpdate";
    public static final String notify_type_changeCards = "notifyChangCards";


	public static String send(int serverId, Map<String, String> map) {
		Server server = SysInfManager.getInstance().getServer(serverId);
		if (server == null||server.getCheck()==0) {
			return null;
		}

		String sytime = String.valueOf(System.currentTimeMillis());
		String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU", "utf-8");
		map.put("sytime", sytime);
		map.put("sysign", md5);
		try {
			HttpUtil res = new HttpUtil(server.getIntranet());
			String post = res.post(map);
			return post;

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("gameutil send err", e);
		}
		return null;
	}

	public static String sendCreateTable(int serverId, Map<String, String> map) {
		Server server = SysInfManager.getInstance().getServer(serverId);
		if (server == null||server.getCheck()==0||map==null||map.isEmpty()) {
			return null;
		}
		map.put("type", 1 + "");
		map.put("funcType", 5 + "");
		LogUtil.i("server :"+serverId+","+server.getIntranet()+","+map);
		return send(serverId, map);
	}
	
	public static String sendDissInfo(int serverId, Map<String, String> map) {
		Server server = SysInfManager.getInstance().getServer(serverId);
		if (server == null||server.getCheck()==0||map==null||map.isEmpty()) {
			return null;
		}
		map.put("type", 1 + "");
		map.put("funcType", 4 + "");
		LogUtil.i("server :"+serverId+","+server.getIntranet()+","+map);
		return send(serverId, map);
	}

	public static String sendPay(int serverId, long userId, int cards, int freeCards, UserMessage info, String currency) {
		return sendPay(serverId,userId,cards,freeCards,info,currency,null);
	}
	
	
	
	
	public static String refreshState(int serverId, long userId,int code,String value) {
		Server server = SysInfManager.getInstance().getServer(serverId);
		if (server == null||server.getCheck()==0) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();
		String sytime = String.valueOf(System.currentTimeMillis());
		String md5 = MD5Util.getStringMD5(sytime + "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3" + userId, "utf-8");
		if(code == 1) {
			map.put("type", "groupApply");
		}else{
			map.put("type", "commonApply");
		}   
		
		map.put("funcType", 1 + "");
		map.put("userId", String.valueOf(userId));
		map.put("time", sytime);
		map.put("sign", md5);
		
	
		String url = server.getIntranet();
		String sub1 = "online/notice.do";
		url = url.replace("qipai/pdk.do",sub1);
		
		LogUtil.i("server url:"+serverId+",   "+url);
		
		try {
			HttpUtil res = new HttpUtil(url);
			String post = res.post(map);
			return post;

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("gameutil send err:"+e.getMessage(), e);
		}
		return null;
		
	}
	
	
	
	
	
	
	

	public static String sendPay(int serverId, long userId, int cards, int freeCards, UserMessage info, String currency, String payRemoveBind) {
		Server server = SysInfManager.getInstance().getServer(serverId);
		if (server == null||server.getCheck()==0) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();
		String sytime = String.valueOf(System.currentTimeMillis());
		String md5 = MD5Util.getStringMD5(sytime + "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3" + userId, "utf-8");
		map.put("type", 2 + "");
		map.put("funcType", 1 + "");
		map.put("flatId", String.valueOf(userId));
		map.put("time", sytime);
		map.put("sign", md5);
		map.put("cards", cards + "");
		map.put("freeCards", freeCards + "");
		map.put("amount", (freeCards + cards) + "");
		map.put("info", info==null?"":JacksonUtil.writeValueAsString(info));
		map.put("currency", currency);
		map.put("payRemoveBind", payRemoveBind);
		LogUtil.i("server :"+serverId+","+server.getIntranet()+","+map);

		try {
			HttpUtil res = new HttpUtil(server.getIntranet());
			String post = res.post(map);
			return post;

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("gameutil send err:"+e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 尝试外网连接服务器
	 * 
	 * @param server
	 * @return
	 */
	public static boolean tryConnToServer(Server server) {
		try {
			HttpUtil res = new HttpUtil(server.getHost());
			res.setConnectTimeout(1000);
			res.setReadTimeout(500);
			String post = res.post("");
			if (StringUtils.isBlank(post)) {
				return true;
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("连接不到服务器-->" + server.getId() + " ip:" + server.getHost());
			return false;
		}
		return false;

	}

	/**
	 * 尝试外网连接服务器
	 * 
	 * @param host
	 * @return
	 */
	public static boolean tryConnToServer(String host) {
		try {
			HttpUtil res = new HttpUtil(host);
			res.setConnectTimeout(1000);
			res.setReadTimeout(500);
			String post = res.post("");
			if (StringUtils.isBlank(post)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;

	}

	/**
	 * 找到服务器备用的Ip
	 * 
	 * @param server
	 * @return
	 */
	public static String findSpareConnIp(Server server) {
		if (server == null||server.getCheck()==0) {
			GameBackLogger.SYS_LOG.error("findConnIp server is null");
			return null;
		}
		List<String> ipList = SysInfManager.getInstance().getIpList(server.getIpConifg());

		if (ipList == null) {
			GameBackLogger.SYS_LOG.error("findConnIp ipList is null-->" + server.getId() + " config:" + server.getIpConifg());
			return null;
		}

		String old_address = server.getHost();
		String[] strs = old_address.split("/");
		String domainname = StringUtil.getValue(strs, 2);
		if (StringUtils.isBlank(domainname)) {
			GameBackLogger.SYS_LOG.error("findConnIp domainname is null-->" + server.getHost());
			return null;
		}

		if (domainname.indexOf(":") >= 0) {
			domainname = domainname.substring(0, domainname.indexOf(":"));

		}

		for (String ip : ipList) {
			String address = old_address.replace(domainname, ip);
			try {
				HttpUtil res = new HttpUtil(address);
				res.setConnectTimeout(1000);
				res.setReadTimeout(500);
				String post = res.post("");
				if (!StringUtils.isBlank(post)) {
					// 长连接地址
					return server.getChathost().replace(domainname, ip);
				}
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error("findConnIp err-->" + address, e);
			}
		}
		return null;
	}

	public static void pushInfoToServer(int pushType, String pushInfo) {

		Collection<Server> servers = SysInfManager.loadServers();
		for (Server server : servers) {
			if (server.getCheck()==0){
				continue;
			}

			Map<String, String> map = new HashMap<String, String>();
			String sytime = String.valueOf(System.currentTimeMillis());
			String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU", "utf-8");
			// String md5_bak = MD5Util.getStringMD5(sytime +
			// "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3", "utf-8");
			map.put("type", 4 + "");
			map.put("funcType", pushType + "");
			// map.put("time", sytime);
			// map.put("sign", md5_bak);
			map.put("sytime", sytime);
			map.put("sysign", md5);
			map.put("info", pushInfo);
			try {
				HttpUtil res = new HttpUtil(server.getIntranet());
				res.post(map);
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error("pushHongBaoInfo send:"+server.getIntranet()+" err:"+e.getMessage(), e);
			}
		}
	}

	public static String consumeUserCards(int serverId, long userId, int cards) {
		Server server = SysInfManager.getInstance().getServer(serverId);
		if (server == null||server.getCheck()==0) {
			GameBackLogger.SYS_LOG.error("gameSite consumeCards err : server is null " + serverId);
			return "{\"extra\":\"server is null\",\"code\":5}";
		}

		Map<String, String> map = new HashMap<String, String>();
		String sytime = String.valueOf(System.currentTimeMillis());
		String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU", "utf-8");
		String md5_bak = MD5Util.getStringMD5(sytime + "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3" + userId, "utf-8");
		map.put("type", 3 + "");
		map.put("funcType", 4 + "");
		map.put("flatId", String.valueOf(userId));
		map.put("sytime", sytime);
		map.put("sysign", md5);
		map.put("time", sytime);
		map.put("sign", md5_bak);
		map.put("cards", cards + "");

		try {
			HttpUtil res = new HttpUtil(server.getIntranet());
			String post = res.post(map);
			return post;
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("gameSite consumeCards err", e);
		}
		return null;
	}

    public static void sendCreditUpdate(int serverId, long userId, long groupId) {
        Map<String, String> map = new HashMap<>();
        map.put("groupId", groupId + "");
        sendNotify(serverId, userId, notify_type_creditUpdate, map);
    }

    public static void sendGroupUserLevelUp(int serverId, long userId, long groupId, int level) {
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("level", String.valueOf(level));
        sendNotify(serverId, userId, notify_type_groupUserLevelUp, map);
    }

    public static void changCoinAndNotify(int serverId, long userId, long coin, long freeCoin, String sourceName) {
        Map<String, String> map = new HashMap<>();
        map.put("sourceName", sourceName);
        map.put("coin", String.valueOf(coin));
        map.put("freeCoin", String.valueOf(freeCoin));
        sendNotify(serverId, userId, notify_type_changCoinAndNotify, map);
    }

    public static void sendGoldRoomGroupId(int serverId, long userId) {
        sendNotify(serverId, userId, notify_type_goldRoomGroupId, null);
    }

    public static void notifyChangGolds(int serverId, long userId, long curGold, long goldChange, long allGold) {
        Map<String, String> map = new HashMap<>();
        map.put("curGold", String.valueOf(curGold));
        map.put("goldChange", String.valueOf(goldChange));
        map.put("allGold", String.valueOf(allGold));
        sendNotify(serverId, userId, notify_type_changeGolds, map);
    }

    public static void sendUpdateUser(int serverId, long userId, String name, String headimgurl, String pw) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(name)) {
            map.put("name", name);
        }
        if (StringUtils.isNotBlank(headimgurl)) {
            map.put("headimgurl", headimgurl);
        }
        if (StringUtils.isNotBlank(pw)) {
            map.put("pw", pw);
        }
        sendNotify(serverId, userId, notify_type_updateUser, map);
    }

    public static void sendNotify(int serverId, long userId, String type, Map<String, String> data) {
        Server server = SysInfManager.getInstance().getServer(serverId);
        if (server == null || server.getCheck() == 0) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String md5 = MD5Util.getStringMD5(time + "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3" + userId, "utf-8");
        map.put("funcType", "1");
        map.put("type", type);
        map.put("userId", String.valueOf(userId));
        if (data != null && data.size() > 0) {
            map.putAll(data);
        }
        map.put("time", time);
        map.put("sign", md5);
        String url = server.getIntranet().replace("qipai/pdk.do", "online/notice.do");
        try {
            HttpUtil res = new HttpUtil(url);
            String post = res.post(map);
            GameBackLogger.SYS_LOG.info("sendNotify|succ|" + post);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("sendNotify|error|" + serverId + "|" + userId + "" + type + "|" + JSON.toJSONString(data), e);
        }
    }

    public static void notifyChangCards(int serverId, long userId, long cards, long freeCards, boolean saveRecord) {
        Map<String, String> map = new HashMap<>();
        map.put("cards", String.valueOf(cards));
        map.put("freeCards", String.valueOf(freeCards));
        map.put("saveRecord", saveRecord ? "1" : "0");
        sendNotify(serverId, userId, notify_type_changeCards, map);
    }

}
