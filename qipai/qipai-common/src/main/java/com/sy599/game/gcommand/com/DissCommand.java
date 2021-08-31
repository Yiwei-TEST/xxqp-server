package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.GroupRoomUtil;
import com.sy599.game.util.HttpGameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.ServerUtil;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解散
 *
 * @author lc
 */
public class DissCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }

        long tableId;
        long groupId = 0;
        if (message != null) {
            ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
            List<String> strParams = req.getStrParamsList();
            if (strParams != null && strParams.size() > 0 && NumberUtils.isDigits(strParams.get(0))) {
                String tableIdStr = strParams.get(0);
                tableId = NumberUtils.toLong(tableIdStr);
                BaseTable table = TableManager.getInstance().getTable(tableId);
                GroupTable gt;
                // 是否俱乐部军团长外部解散
                boolean isGMOuterDiss = false;
                if (strParams.size() > 1) {
                    String keyId = strParams.get(1);
                    // 检查是否有这个俱乐部房间
                    gt = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
                    if (gt != null) {
                        // 检查房间状态
                        if (gt.isOver()) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_8, gt.getTableId()));

                            if (Redis.isConnected()) {
                                RedisUtil.zrem(GroupRoomUtil.loadGroupKey(gt.getGroupId().toString(), gt.loadGroupRoom()), gt.getKeyId().toString());
                                RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(gt.getGroupId().toString(), gt.loadGroupRoom()), gt.getKeyId().toString());
                            }

                            return;
                        }

                        // 检查是否群主
                        GroupUser groupUser = player.getGroupUser();
                        if (groupUser != null && groupUser.getGroupId().longValue() == gt.getGroupId().longValue()) {
                        } else {
                            groupUser = player.loadGroupUser(gt.getGroupId().toString());
                        }

                        groupId = gt.getGroupId().longValue();
                        if (groupUser == null || groupUser.getUserRole().intValue() != 0) {
                            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("wzqf_admin_role"))) {
                                if (groupUser.getUserRole().intValue() != 1 && groupUser.getUserRole().intValue() != 2) {
                                    player.writeErrMsgs(LangHelp.getMsg(LangMsg.code_43));
                                    return;
                                }
                            } else {
                                player.writeErrMsgs(LangHelp.getMsg(LangMsg.code_43));
                                return;
                            }

                        }


                        // 如果房间不在本服 则通知其他服解散房间
                        int serverId = NumberUtils.toInt(gt.getServerId(), -1);
                        if (serverId > 0 && serverId != GameServerConfig.SERVER_ID) {
                            Map<String, String> map = new HashMap<>();
                            map.put("tableIds", tableIdStr);
                            map.put("keyIds", keyId);
                            map.put("specialDiss", "1");
                            int checkCode = player.getMsgCheckCode();
                            String res = HttpGameUtil.sendDissInfo(serverId, map);
                            LogUtil.msg("sendDissInfo-->serverId:" + serverId + ",infoMap:" + JSON.toJSONString(ServerManager.loadServer(serverId)) + ",res:" + res
                                    + ",checkCode=" + checkCode + ":" + player.getMsgCheckCode());
                            if (tableIdStr.equals(res)) {
                                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, table == null ? "1" : String.valueOf(table.calcTableType()), 0, gt.getGroupId().intValue(), 0);
                                GeneratedMessage msg2 = com.build();
                                player.writeSocket(msg2);
                                LogUtil.msgLog.info("gm outer diss group table success:userId=" + player.getUserId() + ",msg=" + JacksonUtil.writeValueAsString(gt));
                            } else {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_59));
                            }
                            return;
                        }

                        // 在本服则继续解散房间
                        isGMOuterDiss = true;
                    }
                } else {
                    gt = GroupDao.getInstance().loadGroupTable(player.getUserId(), tableId);
                }
                if (gt != null) {
                    String[] tempMsgs = new JsonWrapper(gt.getTableMsg()).getString("strs").split(";")[0].split("_");
                    String payType = tempMsgs[0];
                    String userId = tempMsgs[1];
                    if ((userId.equals(String.valueOf(player.getUserId())) && (gt.getCurrentCount().intValue() <= 0 || table != null)) || isGMOuterDiss) {
                        if (table != null) {
                            if (isGMOuterDiss) {
                                table.setSpecialDiss(1);
                            }

                            ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,table == null ? "1" : String.valueOf(table.calcTableType()), table.getPlayType(), gt.getGroupId().intValue());
                            GeneratedMessage msg = com.build();

                            for (Player player0 : table.getSeatMap().values()) {
                                player0.writeSocket(msg);
                                player0.writeErrMsg(LangHelp.getMsg(table.isGroupMasterDiss() ? LangMsg.code_60 : LangMsg.code_8, table.getId()));
                            }

                            for (Player player0 : table.getRoomPlayerMap().values()) {
                                player0.writeSocket(msg);
                                player0.writeErrMsg(LangHelp.getMsg(table.isGroupMasterDiss() ? LangMsg.code_60 : LangMsg.code_8, table.getId()));
                            }

                            if (isGMOuterDiss && table.isDissSendAccountsMsg()) {
                                try {
                                    table.sendAccountsMsg();
                                } catch (Throwable e) {
                                    LogUtil.errorLog.error("tableId=" + table.getId() + ",total calc Exception:" + e.getMessage(), e);
                                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + table.getId() + "被解散").build();
                                    for (Player player0 : table.getPlayerMap().values()) {
                                        player0.writeSocket(errorMsg);
                                    }
                                }
                                table.calcOver3();
                                table.setTiqianDiss(true);
                            }
                            LogUtil.msgLog.info("BaseTable|dissReason|DissCommand|1|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId());
                            table.diss();
                        } else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("currentState", "3");
                            map.put("currentCount", "0");
                            map.put("keyId", gt.getKeyId().toString());

                            if (Redis.isConnected()) {
                                RedisUtil.zrem(GroupRoomUtil.loadGroupKey(gt.getGroupId().toString(), gt.loadGroupRoom()), gt.getKeyId().toString());
                                RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(gt.getGroupId().toString(), gt.loadGroupRoom()), gt.getKeyId().toString());
                            }

                            GroupDao.getInstance().updateGroupTableByKeyId(map);
                            GroupDao.getInstance().deleteTableUser(gt.getKeyId().toString(), gt.getGroupId());

                            LogUtil.msgLog.info("diss group table success:userId=" + player.getUserId() + ",msg=" + JacksonUtil.writeValueAsString(gt));

                            //根据payType返回钻石currentState
                            boolean repay = TableManager.repay(null, player, gt);
                            if (!repay) {
                                if (tempMsgs.length >= 4) {
                                    if ("2".equals(payType) || "3".equals(payType)) {
                                        CardSourceType sourceType;
                                        if ("2".equals(payType)) {
                                            sourceType = CardSourceType.groupTable_diss_FZ;
                                        } else
                                            sourceType = CardSourceType.groupTable_diss_QZ;
                                        Player payPlayer = PlayerManager.getInstance().getPlayer(Long.valueOf(tempMsgs[2]));
                                        if (payPlayer != null) {
                                            payPlayer.changeCards(Integer.parseInt(tempMsgs[3]), 0, true, sourceType);
                                        } else {
                                            RegInfo user = UserDao.getInstance().selectUserByUserId(Long.valueOf(tempMsgs[2]));
                                            payPlayer = ObjectUtil.newInstance(player.getClass());
                                            payPlayer.loadFromDB(user);
                                            payPlayer.changeCards(Integer.parseInt(tempMsgs[3]), 0, true, sourceType);

                                            if (payPlayer.getEnterServer() > 0 && user.getIsOnLine() == 1) {
                                                ServerUtil.notifyPlayerCards(payPlayer.getEnterServer(), payPlayer.getUserId(), 0, Long.valueOf(tempMsgs[3]));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isGMOuterDiss) {
                            ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,table == null ? "1" : String.valueOf(table.calcTableType()), 0, gt.getGroupId().intValue(), 0);
                            GeneratedMessage msg2 = com.build();
                            player.writeSocket(msg2);
                            LogUtil.msgLog.info("gm outer diss group table success:userId=" + player.getUserId() + ",msg=" + JacksonUtil.writeValueAsString(gt));
                        }
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
                    }

                    return;
                }
            }
        }

        if (GoldRoomUtil.isGoldRoom(player)) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
            return;
        }

        BaseTable table = player.getPlayingTable();

        if (table == null) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1));
            return;
        }
        if(table.isGoldRoom()){
            player.writeErrMsg(LangMsg.code_259);
            return;
        }
        if (table.getGroupTable() != null) {
            groupId = table.getGroupTable().getGroupId();
        }
        GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupId, 0);
        if (groupInfo != null) {
            JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
            Integer obj = jsonObject.getInteger(GroupConstants.groupExtKey_dismissCount);
            if (obj != null && obj > 0) {
                if (player.getDissCount() >= obj) {
                    player.writeErrMsg(LangMsg.code_260);
                    return;
                } else {
                    player.addDissCount();
                }
            }
            obj = jsonObject.getInteger(GroupConstants.groupExtKey_forbiddenDiss);
            if (obj != null && obj == 1) {
                player.writeErrMsg(LangMsg.code_261);
                return;
            }
        }
        synchronized (table) {
            if (table.isCompetition()) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_19));
                return;
            }
            if (!table.getPlayerMap().containsKey(player.getUserId()) && (table.getServerKey() == null || !table.getServerKey().startsWith("group"))) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
                return;
            }
            LogUtil.msgLog.info("DissCommand|" + table.getId() + "|" + table.getPlayBureau() + "|" + table.getPlayType() + "|" + player.getUserId() + "|" + player.getSeat());
            if (!table.canDissTable(player)) {
                return;
            }
            table.clearAnswerDiss();
            table.answerDiss(player.getSeat(), 1);
            table.checkDiss(player);

        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
