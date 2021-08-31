package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSON;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.GroupRoomUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class GroupRoomInvitationCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> intParams = req.getParamsList();
        if (intParams == null || intParams.size() < 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        int optType = intParams.get(0);

        if (optType == 3) {
            // 删除好友列表
            deleteGroupUserFriend(player, req);
            return;
        }

        BaseTable table = player.getPlayingTable();
        if (table == null) {
            player.writeErrMsg("您不在房间里，无法获取可邀请的成员");
            return;
        }
        if (!table.isGroupRoom()) {
            player.writeErrMsg("不是亲友圈房间，无法获取可邀请的成员");
            return;
        }
        if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau() > 0 || table.getPlayBureau() > 1) {
            player.writeErrMsg("牌局已开始，不能邀请其他玩家加入");
            return;
        }
        if (table.getPlayerCount() >= table.getMaxPlayerCount()) {
            player.writeErrMsg("房间已满员，不可邀请");
            return;
        }

        if (optType == 0) {
            //获取邀请列表
            userList(player, req);
        } else if (optType == 1) {
            //一键邀请所有人或指定人员
            inviteUser(player, req);
        } else if (optType == 2) {
            // 私密房邀请人列表
            userListForPrivate(player, req);
        }
    }

    /**
     * 邀请列表
     *
     * @param player
     * @param req
     */
    public void userList(Player player, ComMsg.ComReq req) {
        try {
            List<Integer> intParams = req.getParamsList();
            List<String> strParams = req.getStrParamsList();
            int optType = intParams.get(0);
            int intSize = intParams == null ? 0 : intParams.size();
            int count = intSize > 1 ? intParams.get(1) : 21;
            if (count <= 0) {
                count = 21;
            } else if (count > 50) {
                count = 21;
            }
            BaseTable table = player.getPlayingTable();
            String groupId = table.loadGroupId();
            List<HashMap<String, Object>> list;
            if (GameUtil.isPlayWzq(table.getPlayType())) {
                if (strParams == null || strParams.size() < 1) {
                    player.writeErrMsg(LangMsg.code_3);
                    return;
                }
                long searchId = Long.valueOf(strParams.get(0));

                GroupUser guSelf = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                GroupUser guOther = GroupDao.getInstance().loadGroupUser(searchId, groupId);
                if (guOther == null || guSelf == null) {
                    list = Collections.emptyList();
                } else {
                    if (!GroupConstants.isCanPlayWzq(guSelf, guOther)) {
                        player.writeErrMsg("你无权限邀请该玩家id[" + searchId + "]");
                        return;
                    }
                    RegInfo regInfo = UserDao.getInstance().getUser(searchId);
                    if (regInfo == null) {
                        list = Collections.emptyList();
                    } else {
                        HashMap<String, Object> d = new HashMap<>();
                        d.put("userId", regInfo.getUserId());
                        d.put("name", regInfo.getName());
                        d.put("headimgurl", regInfo.getHeadimgurl());
                        d.put("playingTableId", regInfo.getPlayingTableId());
                        list = new ArrayList<>();
                        list.add(d);
                    }
                }
            } else {
                list = GroupDao.getInstance().loadRandomGroupUsers(groupId, count);
            }

            if (list == null || list.size() == 0) {
                player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, count, groupId, "[]");
                return;
            }

            List<HashMap<String, Object>> list2 = new ArrayList<>(list);
            for (HashMap<String, Object> entry : list2) {
                Object id = entry.get("userId");
                if (id != null && Long.valueOf(String.valueOf(id)) == player.getUserId()) {
                    list.remove(entry);
                    break;
                }
            }
            if (list.size() > 20) {
                list.remove(0);
            }
            player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, count, groupId, JSON.toJSONString(list));
        } catch (Exception e) {
            LogUtil.errorLog.error("userList|error|", e);
        }
    }

    /**
     * 群邀请
     *
     * @param player
     * @param req
     */
    public void inviteUser(Player player, ComMsg.ComReq req) {
        try {
            List<Integer> intParams = req.getParamsList();
            int optType = intParams.get(0);
            BaseTable table = player.getPlayingTable();

            String groupId = table.loadGroupId();
            GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(Long.parseLong(groupId), 0);
            if (groupInfo == null) {
                player.writeErrMsg("亲友圈不存在，不可邀请");
                return;
            }

            GroupTable groupTable = table.loadGroupTable();

            List<HashMap<String, Object>> members = groupTable.getCurrentCount() <= 0 ? null : GroupDao.getInstance().loadTableUserInfo(groupTable.getKeyId().toString(), groupTable.getGroupId());

            List<Integer> noticeInts = new ArrayList<>(2);
            noticeInts.add(100);

            List<String> noticeStrs = new ArrayList<>(2);
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builder("name", player.getRawName());
            msg.builder("userId", player.getUserId());
            msg.builder("icon", player.getHeadimgurl() == null ? "" : player.getHeadimgurl());
            msg.builder("currentCount", table.getPlayerCount());
            msg.builder("maxCount", groupTable.getMaxCount());
            msg.builder("groupId", groupTable.getGroupId());
            msg.builder("tableKey", groupTable.getKeyId());
            msg.builder("tableId", groupTable.getTableId());
            msg.builder("tableMsg", groupTable.getTableMsg());
            msg.builder("creditMsg", groupTable.getCreditMsg());
            msg.builder("tableName", groupTable.getTableName());
            msg.builder("groupName", groupInfo.getGroupName());
            msg.builderIfAbsent("members", members == null ? Collections.emptyList() : members);
            noticeStrs.add(msg.toString());


            List<String> strParams = req.getStrParamsList();
            String userIdStr = strParams != null && strParams.size() > 0 ? strParams.get(0) : "";
            LogUtil.msgLog.debug("GroupRoomInvitationCommand|send|0|" + userIdStr);
            if (StringUtils.isBlank(userIdStr)) {
                //邀请所有在线玩家
                List<HashMap<String, Object>> list = GroupDao.getInstance().loadOnlineGroupUsers(groupId, null);
                if (list == null || list.size() == 0) {
                    player.writeErrMsg("邀请失败：该亲友圈没有可邀请的成员");
                    return;
                }
                send(list, noticeInts, noticeStrs);
                player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, groupId, "[]");

            } else if (CommonUtil.isPureNumber(userIdStr)) {
                //邀请单个在线玩家
                GroupUser groupUser = GroupDao.getInstance().loadGroupUser(Long.parseLong(userIdStr), groupId);
                if (groupUser == null) {
                    player.writeErrMsg("邀请失败：该玩家不是亲友圈成员");
                    return;
                }
                if (groupUser.getRefuseInvite() == 0) {
                    player.writeErrMsg("邀请失败：该玩家设置拒绝亲友圈游戏邀请");
                    return;
                }

                Player player1 = PlayerManager.getInstance().getPlayer(groupUser.getUserId());
                int serverId;
                if (player1 != null) {
                    serverId = GameServerConfig.SERVER_ID;
                } else {
                    serverId = CommonUtil.object2Int(UserDao.getInstance().getUserServerId(groupUser.getUserId() + ""));
                }

                List<HashMap<String, Object>> list = new ArrayList<>();
                HashMap<String, Object> map = new HashMap<>();
                map.put("userId", groupUser.getUserId());
                map.put("enterServer", serverId);
                list.add(map);
                send(list, noticeInts, noticeStrs);

                player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, groupId, "[]");
            } else {
                //邀请多个在线玩家

                StringJoiner userIds = new StringJoiner(",");
                String[] strs = userIdStr.split(",");
                for (String str : strs) {
                    if (CommonUtil.isPureNumber(str)) {
                        userIds.add(str);
                    }
                }

                List<HashMap<String, Object>> list = GroupDao.getInstance().loadOnlineGroupUsers(groupId, userIds.toString());
                if (list == null || list.size() == 0) {
                    player.writeErrMsg("邀请失败：被邀请的玩家不在线或已加入房间");
                    return;
                }
                send(list, noticeInts, noticeStrs);
                player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, groupId, "[]");
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("inviteUser|error|", e);
        }
    }

    /**
     * 私密房间玩家列表
     * 不查询用户id时，显示用户好友列表
     *
     * @param player
     * @param req
     */
    public void userListForPrivate(Player player, ComMsg.ComReq req) {
        try {
            List<Integer> intParams = req.getParamsList();
            List<String> strParams = req.getStrParamsList();
            int optType = intParams.get(0);
            int intSize = intParams == null ? 0 : intParams.size();
            BaseTable table = player.getPlayingTable();
            int pageNo = intSize > 1 ? intParams.get(1) : 1;
            int pageSize = intSize > 2 ? intParams.get(2) : 10;

            long searchId = strParams != null && strParams.size() > 0 ? Long.valueOf(strParams.get(0)) : 0;

            String groupId = table.loadGroupId();
            List<HashMap<String, Object>> list;
            if (searchId > 0) {
                list = GroupDao.getInstance().searchGroupUserListForInvite(groupId, searchId);
            } else {
                list = GroupDao.getInstance().loadGroupUserFriendList(groupId, player.getUserId(), pageNo, pageSize);
            }
            if (list == null || list.size() == 0) {
                player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, searchId, String.valueOf(groupId), "[]");
                return;
            }
            player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, String.valueOf(groupId), JSON.toJSONString(list));
        } catch (Exception e) {
            LogUtil.errorLog.error("userListForPrivate|error|", e);
        }
    }

    /**
     * 发送邀请信息
     *
     * @param list
     * @param noticeInts
     * @param noticeStrs
     */
    private void send(List<HashMap<String, Object>> list, List<Integer> noticeInts, List<String> noticeStrs) {

        Map<Integer, StringBuilder> userServer = new HashMap<>();
        for (HashMap<String, Object> map : list) {
            int serverId = CommonUtil.object2Int(map.getOrDefault("enterServer", 0));
            if (serverId == GameServerConfig.SERVER_ID) {
                Player player = PlayerManager.getInstance().getPlayer(CommonUtil.object2Long(map.getOrDefault("userId", 0)));
                if (player == null || player.getUserId() == this.player.getUserId()) {
                    continue;
                }
                if (GroupRoomUtil.canJoinInviteTable(player)) {
                    player.writeComMessage(WebSocketMsgType.com_group_room_invite, noticeInts, noticeStrs);
                    LogUtil.msgLog.debug("GroupRoomInvitationCommand|send|1|" + player.getUserId());
                }
            } else {
                StringBuilder strBuilder = userServer.get(serverId);
                if (strBuilder == null) {
                    strBuilder = new StringBuilder();
                    userServer.put(serverId, strBuilder);
                }
                strBuilder.append(",").append(map.getOrDefault("userId", 0));
            }
        }

        for (Map.Entry<Integer, StringBuilder> kv : userServer.entrySet()) {
            Server server = ServerManager.loadServer(kv.getKey());
            if (server != null) {
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("type", "notice");
                paramsMap.put("checkRoom", "1");
                paramsMap.put("userId", kv.getValue().toString());
                paramsMap.put("code", String.valueOf(WebSocketMsgType.com_group_room_invite));
                paramsMap.put("ints", JSON.toJSONString(noticeInts));
                paramsMap.put("strs", JSON.toJSONString(noticeStrs));
                HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                LogUtil.msgLog.debug("GroupRoomInvitationCommand|send|2|" + kv.getValue());
            }
        }
    }

    private void deleteGroupUserFriend(Player player, ComMsg.ComReq req) {
        try {
            List<Integer> intParams = req.getParamsList();
            List<String> strParams = req.getStrParamsList();
            int optType = intParams.get(0);
            long groupId = strParams.size() > 0 ? Long.valueOf(strParams.get(0)) : 0;
            long keyId = strParams.size() > 1 ? Long.valueOf(strParams.get(1)) : 0;
            GroupUser gu = GroupDao.getInstance().loadGroupUser(player.getUserId(), String.valueOf(groupId));
            if (gu == null) {
                player.writeErrMsg("解除绑定失败：参数错误");
                return;
            }
            GroupDao.getInstance().deleteGroupUserFriend(keyId, groupId);
            player.writeComMessage(WebSocketMsgType.com_group_room_invite, optType, String.valueOf(groupId), String.valueOf(keyId));
        } catch (Exception e) {
            LogUtil.errorLog.error("userListForPrivate|error|", e);
        }
    }

}
