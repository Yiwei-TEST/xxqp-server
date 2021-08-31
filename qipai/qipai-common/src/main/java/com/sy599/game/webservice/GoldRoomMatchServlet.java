package com.sy599.game.webservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoldRoomMatchServlet extends HttpServlet {

    private final static String APP_KEY = "qweh#$*(_~)lpslot;589*/-+.-8&^%$#@!";

    private static final long serialVersionUID = 1L;

    private static void writeMsg(HttpServletResponse response, String msg) {
        try {
            Writer writer = response.getWriter();
            writer.write(msg);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    private static void writeMsg(HttpServletResponse response, int code) {
        writeMsg(response, code, null);
    }

    private static void writeMsg(HttpServletResponse response, int code, String msg) {
        JSONObject res = new JSONObject();
        res.put("code", code);
        res.put("msg", msg);
        writeMsg(response, res.toJSONString());
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        String type = params.get("type");
        String timestamp = params.get("timestamp");
        String sign = params.get("sign");
        String ip = IpUtil.getIpAddr(request);
        LogUtil.msgLog.info("GoldRoomMatchServlet|ip={},type={},userId={},message={},timestamp={},sign={}", ip, type, timestamp, sign);

        if (!IpUtil.isIntranet(ip)
                && !"127.0.0.1".equals(ip)
                && !(NumberUtils.isDigits(timestamp)
                && (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) <= 5 * 60 * 1000)
                && MD5Util.getMD5String(APP_KEY + type + timestamp).equalsIgnoreCase(sign))) {
            writeMsg(response, -1, "ip or sign invalid");
            return;
        }
        try {
            switch (type) {
                case "genGoldRoomTable":
                    genGoldRoomTable(response, params);
                    break;
                case "notifyDissGoldRoomTable":
                    notifyDissGoldRoomTable(response, params);
                    break;
                default:
                    writeMsg(response, -2);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            writeMsg(response, -3);
        }
    }

    public void genGoldRoomTable(HttpServletResponse response, Map<String, String> params) throws Exception {
        long tableId = 0;
        long start = System.currentTimeMillis();
        String userIdListStr = params.get("userIdList");
        long goldRoomConfigId = Long.parseLong(params.get("goldRoomConfigId"));
        try {
            Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
            if (server == null || server.getServerType() != Server.SERVER_TYPE_GOLD_ROOM) {
                writeMsg(response, 7);
                return;
            }
            if (StringUtils.isBlank(userIdListStr)) {
                writeMsg(response, 1);
                return;
            }
            String timestamp = params.get("timestamp");
            if (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) > 5 * 1000) {
                writeMsg(response, 2);
                return;
            }
            List<Long> userIdList = new ArrayList<>();
            String[] splits = userIdListStr.split(",");
            for (String split : splits) {
                userIdList.add(Long.valueOf(split));
            }
            if (userIdList.size() == 0) {
                writeMsg(response, 3);
                return;
            }
            GoldRoomConfig config = GoldRoomUtil.getGoldRoomConfig(goldRoomConfigId);
            if (config == null) {
                writeMsg(response, 4);
                return;
            }
            start = System.currentTimeMillis();
            List<Player> playerList = new ArrayList<>();
            for (Long playerId : userIdList) {
                Player player = PlayerManager.getInstance().getPlayer(playerId);
                if (player == null) {
                    player = PlayerManager.getInstance().loadPlayer(playerId, config.getPlayType());
                    if(player.isRobot()){
                        player.setIsOnline(1);
                    }else{
                        player.setIsOnline(0);
                    }
                }
                if (player.getPlayingTableId() > 0) {
                    writeMsg(response, 5);
                    return;
                }
                playerList.add(player);
            }
            synchronized (config) {
                BaseTable table = GoldRoomUtil.matchSucc(playerList, config, 3000);
                if (table != null) {
                    tableId = table.getId();
                } else {
                    writeMsg(response, 6);
                    return;
                }
            }
            JSONObject res = new JSONObject();
            res.put("code", 0);
            res.put("tableId", tableId);
            writeMsg(response, res.toJSONString());
        } catch (Exception e) {
            LogUtil.errorLog.error("genGoldRoomTable|error|" + JSON.toJSONString(params), e);
        } finally {
            long timeUse = System.currentTimeMillis() - start;
            if (timeUse > 500) {
                LogUtil.monitorLog.info("matchSucc|" + timeUse + "|" + userIdListStr + "|" + goldRoomConfigId);
                if (timeUse > 10 * 1000) {
                    // 超过10秒，就否考虑删除数据并解散房间
                }
            }
        }
    }

    public void notifyDissGoldRoomTable(HttpServletResponse response, Map<String, String> params) throws Exception {
        Long userId = Long.parseLong(params.get("userId"));
        Long tableId = Long.parseLong(params.get("tableId"));
        BaseTable table = TableManager.getInstance().getTable(tableId);
        if (table != null && table.isGoldRoom() && table.getGoldRoomUserMap().get(userId) != null) {
            table.diss();
        }
        JSONObject res = new JSONObject();
        res.put("code", 0);
        res.put("tableId", tableId);
        writeMsg(response, res.toJSONString());
    }
}
