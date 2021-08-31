package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSON;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoomArea;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomHall;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gold.GoldRoomMatch;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.GoldRoomMsg.GoldRoomHallProto;
import com.sy599.game.msg.serverPacket.GoldRoomMsg.GoldRoomHallList;
import com.sy599.game.msg.serverPacket.GoldRoomMsg.GoldRoomAreaList;
import com.sy599.game.msg.serverPacket.GoldRoomMsg.GoldRoomAreaProto;
import com.sy599.game.msg.serverPacket.GoldRoomMsg.GoldRoomConfigList;
import com.sy599.game.msg.serverPacket.GoldRoomMsg.GoldRoomConfigProto;
import com.sy599.game.util.GoldRoomMatchUtil;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldRoomCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);

        int optType = req.hasOptType() ? req.getOptType() : 0;
        if (optType == 0) {
            // 0:区域信息
            getGoldRoomAreaMsg(player, req);
        } else if (optType == 1) {
            // 1：根据区域id获取玩法信息列表
            getGoldRoomConfigMsgForArea(player, req);
        } else if (optType == 2) {
            // 2：亲友圈信息
            getGroupList(player, req);
        } else if (optType == 3) {
            // 3：金币场绑定亲友圈
            updateGoldRoomGroupId(player, req);
        } else if (optType == 4) {
            // 4：玩法id获取玩法信息列表
            getGoldRoomConfigMsgForIds(player, req);
        } else if (optType == 5) {
            // 5：大厅玩法列表
            getGoldRoomHallMsg(player, req);
        } else if (optType == 6) {
            // 6：根据大厅获取玩法配置列表
            getGoldRoomConfigMsgForHall(player, req);
        } else if (optType == 7) {
            // 7：加入金币场匹配
            joinGoldRoomMatch(player, req);
        } else if (optType == 8) {
            // 8：退出金币场匹配
            quitGoldRoomMatch(player, req);
        } else {
            player.writeErrMsg(LangMsg.code_3);
        }

    }

    private void getGroupList(Player player, ComMsg.ComReq req) throws Exception {
        Map<String, Object> data = new HashMap<>();
        List<HashMap<String, Object>> groups = GroupDao.getInstance().loadGroupNameByUser(player.getUserId());
        data.put("goldRoomGroupId", player.getGoldRoomGroupId());
        if (groups != null && groups.size() > 0) {
            data.put("groupList", groups);
        }
        player.writeComMessage(WebSocketMsgType.req_code_choose_group_for_gold_room, JSON.toJSONString(data));
    }


    private void getGoldRoomAreaMsg(Player player, ComMsg.ComReq req) {
        List<GoldRoomArea> all = GoldRoomUtil.getAllArea();
        GoldRoomAreaList.Builder list = GoldRoomAreaList.newBuilder();
        if (all != null && all.size() > 0) {
            for (GoldRoomArea area : all) {
                GoldRoomAreaProto.Builder areaProto = GoldRoomAreaProto.newBuilder();
                areaProto.setKeyId(area.getKeyId());
                areaProto.setParentId(area.getParentId());
                areaProto.setState(area.getState());
                areaProto.setName(area.getName());
                areaProto.setOrder(area.getOrder());
                list.addList(areaProto);
            }
        }
        player.writeSocket(list.build());
        player.sendBrokeAward(0);
    }

    private void getGoldRoomConfigMsgForArea(Player player, ComMsg.ComReq req) {
        int strCount = req.getStrParamsCount();
        if (strCount == 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        List<String> strParamsList = req.getStrParamsList();
        Long areaId = Long.parseLong(strParamsList.get(0));
        if (areaId <= 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        GoldRoomConfigList.Builder list = GoldRoomConfigList.newBuilder();
        List<GoldRoomConfig> areaConfigList = GoldRoomUtil.getAreaConfigList(areaId);
        if (areaConfigList != null && areaConfigList.size() > 0) {
            for (GoldRoomConfig config : areaConfigList) {
                GoldRoomConfigProto.Builder configProto = GoldRoomConfigProto.newBuilder();
                configProto.setKeyId(config.getKeyId());
                configProto.setName(config.getName());
                configProto.setState(config.getState());
                configProto.setPlayType(config.getPlayType());
                configProto.setPlayerCount(config.getPlayerCount());
                configProto.setTotalBureau(config.getTotalBureau());
                configProto.setTableMsg(config.getTableMsg());
                configProto.setGoldMsg(config.getGoldMsg());
                configProto.setAreaId(config.getAreaId());
                configProto.setOrder(config.getOrder());
                list.addList(configProto);
            }
        }
        list.setCode(req.getOptType());
        player.writeSocket(list.build());

    }

    private void updateGoldRoomGroupId(Player player, ComMsg.ComReq req) throws Exception {
        int strCount = req.getStrParamsCount();
        if (strCount == 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        List<String> strParamsList = req.getStrParamsList();
        Long groupId = Long.parseLong(strParamsList.get(0));
        if (groupId <= 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
        if (groupUser == null) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId);
        if (group == null) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        if (group.getGoldRoomSwitch() != 1) {
            player.writeErrMsg("暂未开放亲友圈金币场功能");
            return;
        }

        Map<String, Object> modify = new HashMap<>(8);
        modify.put("goldRoomGroupId", groupId);
        int ret = UserDao.getInstance().updateUser(String.valueOf(player.getUserId()), modify);
        long oldGroupId = player.getGoldRoomGroupId();
        if (ret > 0) {
            player.setGoldRoomGroupId(groupId);
        }

        StringBuilder sb = new StringBuilder("GoldRoom|setGoldRoomGroupId");
        sb.append("|").append(ret);
        sb.append("|").append(player.getUserId());
        sb.append("|").append(groupId);
        sb.append("|").append(oldGroupId);
        LogUtil.msgLog.info(sb.toString());

    }


    private void getGoldRoomConfigMsgForIds(Player player, ComMsg.ComReq req) {
        int strCount = req.getStrParamsCount();
        if (strCount == 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        List<String> strParamsList = req.getStrParamsList();
        String idStr = strParamsList.get(0);
        if (StringUtils.isBlank(idStr)) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        GoldRoomConfigList.Builder list = GoldRoomConfigList.newBuilder();
        String[] splits = idStr.split(",");
        for (String split : splits) {
            GoldRoomConfig config = GoldRoomUtil.getGoldRoomConfig(Long.valueOf(split));
            if (config == null) {
                continue;
            }
            GoldRoomConfigProto.Builder configProto = GoldRoomConfigProto.newBuilder();
            configProto.setKeyId(config.getKeyId());
            configProto.setName(config.getName());
            configProto.setState(config.getState());
            configProto.setPlayType(config.getPlayType());
            configProto.setPlayerCount(config.getPlayerCount());
            configProto.setTotalBureau(config.getTotalBureau());
            configProto.setTableMsg(config.getTableMsg());
            configProto.setGoldMsg(config.getGoldMsg());
            configProto.setAreaId(config.getAreaId());
            configProto.setOrder(config.getOrder());
            list.addList(configProto);
        }
        list.setCode(req.getOptType());
        player.writeSocket(list.build());
    }

    private void getGoldRoomHallMsg(Player player, ComMsg.ComReq req) {
        List<GoldRoomHall> all = GoldRoomUtil.getAllHall();
        GoldRoomHallList.Builder list = GoldRoomHallList.newBuilder();
        if (all != null && all.size() > 0) {
            for (GoldRoomHall data : all) {
                GoldRoomHallProto.Builder proto = GoldRoomHallProto.newBuilder();
                proto.setKeyId(data.getKeyId());
                proto.setName(data.getName());
                proto.setType(data.getType());
                proto.setPlayTypes(data.getPlayTypes());
                proto.setExtMsg(data.getExtMsg());
                proto.setDescription(data.getDescription());
                list.addList(proto);
            }
        }
        list.setCode(req.getOptType());
        player.writeSocket(list.build());
        player.sendBrokeAward(0);
    }


    private void getGoldRoomConfigMsgForHall(Player player, ComMsg.ComReq req) {
        int strCount = req.getStrParamsCount();
        if (strCount == 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        List<String> strParamsList = req.getStrParamsList();
        String hallId = strParamsList.get(0);
        if (StringUtils.isBlank(hallId)) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        GoldRoomConfigList.Builder list = GoldRoomConfigList.newBuilder();
        List<GoldRoomConfig> configList = GoldRoomUtil.getHallConfigList(Long.valueOf(hallId));
        if (configList != null && configList.size() > 0) {
            for (GoldRoomConfig config : configList) {
                GoldRoomConfigProto.Builder configProto = GoldRoomConfigProto.newBuilder();
                configProto.setKeyId(config.getKeyId());
                configProto.setName(config.getName());
                configProto.setState(config.getState());
                configProto.setPlayType(config.getPlayType());
                configProto.setPlayerCount(config.getPlayerCount());
                configProto.setTotalBureau(config.getTotalBureau());
                configProto.setTableMsg(config.getTableMsg());
                configProto.setGoldMsg(config.getGoldMsg());
                configProto.setAreaId(config.getAreaId());
                configProto.setOrder(config.getOrder());
                list.addList(configProto);
            }
        }
        list.setCode(req.getOptType());
        player.writeSocket(list.build());
    }

    /**
     * 金币场匹配
     *
     * @param player
     * @param req
     */
    private void joinGoldRoomMatch(Player player, ComMsg.ComReq req) {
        if (player.isMatching()) {
            return;
        }
        if (player.getPlayingTableId() > 0) {
            return;
        }
        int intCount = req.getParamsCount();
        if (intCount < 2) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        int playType = req.getParams(0); // 玩法
        int matchType = req.getParams(1);// 匹配类型：1快速加入、2智能匹配
        Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
        if (!server.getMatchType().contains(playType)) {
            player.writeErrMsg("匹配失败：服务器不支持匹配");
            LogUtil.errorLog.error("joinGoldRoomMatch|error|" + player.getUserId() + "|" + req.getParamsList() + "|" + req.getStrParamsList());
            return;
        }
        long configId = 0;
        if (matchType == GoldRoomMatch.MATCH_TYPE_FAST) {
            int strCount = req.getStrParamsCount();
            if (strCount > 0) {
                configId = Long.valueOf(req.getStrParams(0));
            }
        }

        boolean res = GoldRoomMatchUtil.joinMatch(player, playType, matchType, configId);
        if (res) {
            player.writeComMessage(WebSocketMsgType.res_code_joinGoldRoomMatch, playType, matchType, configId > 0 ? String.valueOf(configId) : null);
        }
    }

    /**
     * 退出金币场匹配
     *
     * @param player
     * @param req
     */
    private void quitGoldRoomMatch(Player player, ComMsg.ComReq req) {
        boolean res = GoldRoomMatchUtil.quitMatch(player);
        if (res) {
            player.writeComMessage(WebSocketMsgType.res_code_quitGoldRoomMatch);
        }
    }
}
