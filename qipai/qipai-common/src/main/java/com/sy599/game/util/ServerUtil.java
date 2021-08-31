package com.sy599.game.util;

import com.sy.mainland.util.HttpUtil;
import com.sy599.game.db.bean.Server;
import com.sy599.game.manager.ServerManager;
import org.apache.commons.lang3.StringUtils;

public class ServerUtil {

    private static String getServerDomain(int serverId) {
        if (serverId <= 0) {
            return "";
        }
        Server server = ServerManager.loadServer(serverId);
        return getServerDomain(server);
    }

    private static String getServerDomain(Server server) {
        if (server == null) {
            return "";
        }
        String url;
        if (StringUtils.isNotBlank(server.getIntranet())) {
            url = server.getIntranet();
        } else {
            url = server.getHost();
        }
        if (StringUtils.isBlank(url)) {
            return "";
        }
        int idx = url.indexOf(".");
        if (idx > 0) {
            idx = url.indexOf("/", idx);
            if (idx > 0) {
                url = url.substring(0, idx);
            }
        }
        return url;
    }

    /**
     * 通知玩家钻石变动
     *
     * @param serverId
     * @param userId
     * @param cards
     * @param freeCards
     */
    public static void notifyPlayerCards(int serverId, long userId, long cards, long freeCards) {
        if (cards == 0 && freeCards == 0) {
            return;
        }
        String serverDomain = getServerDomain(serverId);
        if (StringUtil.isBlank(serverDomain)) {
            return;
        }
        String url = serverDomain + "/online/notice.do?type=playerCards&msgType=1&userId=" + userId;
        if (cards != 0) {
            url += "&cards=" + cards;
        }
        if (freeCards != 0) {
            url += "&freeCards=" + freeCards;
        }
        String noticeRet = HttpUtil.getUrlReturnValue(url + "&message=" + cards, 2);
        LogUtil.msgLog.info("notifyPlayerCards|result:url=" + url + ",ret=" + noticeRet);
    }

    /**
     * 通知玩家钻石变动
     *
     * @param serverId
     * @param userId
     */
    public static void notifyChangeGolds(int serverId, long userId, long curGold, long goldChange, long allGold) {
        String serverDomain = getServerDomain(serverId);
        if (StringUtil.isBlank(serverDomain)) {
            return;
        }
        String url = serverDomain + "/online/notice.do?type=notifyChangGolds&msgType=1&userId=" + userId + "&curGold=" + curGold + "&goldChange=" + goldChange + "&allGold=" + allGold;
        String noticeRet = HttpUtil.getUrlReturnValue(url, 2);
        LogUtil.msgLog.info("notifyPlayerCards|result:url=" + url + ",ret=" + noticeRet);
    }

    /**
     * 通知服务器创建房间
     *
     */
    public static String genGoldRoomTable(int serverId, String userIdList, long goldRoomConfigId) {
        try {
            String serverDomain = getServerDomain(serverId);
            if (StringUtil.isBlank(serverDomain)) {
                return null;
            }
            String url = serverDomain + "/goldRoom/match.do?type=genGoldRoomTable&msgType=1&userIdList=" + userIdList + "&goldRoomConfigId=" + goldRoomConfigId + "&timestamp=" + System.currentTimeMillis();
            String ret = HttpUtil.getUrlReturnValue(url, 30);
            LogUtil.msgLog.info("genGoldRoomTable|result:url=" + url + ",ret=" + ret);
            return ret;
        } catch (Exception e) {
            LogUtil.errorLog.error("genGoldRoomTable|error|" + serverId + "|" + userIdList + "|" + goldRoomConfigId, e);
        }
        return null;
    }

    /**
     * 通知服务器解散
     *
     * @param serverId
     * @param userId
     */
    public static String notifyDissGoldRoomTable(int serverId, long userId, long tableId) {
        String serverDomain = getServerDomain(serverId);
        if (StringUtil.isBlank(serverDomain)) {
            return null;
        }
        String url = serverDomain + "/goldRoom/match.do?type=notifyDissGoldRoomTable&msgType=1&userId=" + userId + "&tableId=" + tableId;
        String ret = HttpUtil.getUrlReturnValue(url, 10);
        LogUtil.msgLog.info("notifyDissGoldRoomTable|result:url=" + url + ",ret=" + ret);
        return ret;
    }


}
