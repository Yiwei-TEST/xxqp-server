package com.sy599.game.util;

import com.sy599.game.common.constant.KeyConstants;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.UserMessage;
import com.sy599.game.manager.ServerManager;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpGameUtil {
    public static String send(int serverId, Map<String, String> map) {
        Server server = ServerManager.loadServer(serverId);
        if (server == null) {
            return null;
        }

        String sytime = String.valueOf(System.currentTimeMillis());
        String md5 = MD5Util.getMD5String(sytime + KeyConstants.md5_http);
        map.put("sytime", sytime);
        map.put("sysign", md5);
        try {
            HttpUtils res = new HttpUtils(server.getIntranet());
            String post = res.post(map);
            return post;

        } catch (Exception e) {
            LogUtil.e("gameutil send err", e);
        }
        return null;
    }

    public static String sendDissInfo(int serverId, Map<String, String> map) {
        Server server = ServerManager.loadServer(serverId);
        if (server == null||map==null||map.isEmpty()) {
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

    public static String sendPay(int serverId, long userId, int cards, int freeCards, UserMessage info, String currency, String payRemoveBind) {
        Server server = ServerManager.loadServer(serverId);
        if (server == null) {
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        String sytime = String.valueOf(System.currentTimeMillis());
        String md5 = MD5Util.getMD5String(sytime + KeyConstants.md5_pay + userId);
        map.put("type", 2 + "");
        map.put("funcType", 1 + "");
        map.put("flatId", String.valueOf(userId));
        map.put("time", sytime);
        map.put("sign", md5);
        map.put("cards", cards + "");
        map.put("freeCards", freeCards + "");
        map.put("amount", (freeCards + cards) + "");
        map.put("info", JacksonUtil.writeValueAsString(info));
        map.put("currency", currency);
        map.put("payRemoveBind", payRemoveBind);
        LogUtil.i("server :"+serverId+","+server.getIntranet()+","+map);

        try {
            HttpUtils res = new HttpUtils(server.getIntranet());
            String post = res.post(map);
            return post;

        } catch (Exception e) {
            LogUtil.e("gameutil send err", e);
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
            HttpUtils res = new HttpUtils(server.getHost());
            res.setConnectTimeout(1000);
            res.setReadTimeout(500);
            String post = res.post("");
            if (StringUtils.isBlank(post)) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("连接不到服务器-->" + server.getId() + " ip:" + server.getHost());
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
            HttpUtils res = new HttpUtils(host);
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
}
