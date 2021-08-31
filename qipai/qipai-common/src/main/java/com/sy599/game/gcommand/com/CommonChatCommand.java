package com.sy599.game.gcommand.com;

import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.ChatMessageDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.KeyWordsFilter;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 普通聊天
 */
public class CommonChatCommand extends BaseCommand {
    //	private final static String PROPERTY_KEY = "CommonChat";
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);

        List<Integer> ints = req.getParamsList();
        List<String> strs = req.getStrParamsList();

        GroupUser groupUser = player.getGroupUser();
        if (groupUser == null) {
            groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(),strs.size()>1?strs.get(1):null);
            if (groupUser == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_205));
                return;
            }
        }

        int opreate = 0;//0接收消息，1发送消息
        if (ints.size() > 0) {
            opreate = ints.get(0);
        }

        if (opreate == 0) {
            int pageNo = 1;
            int pageSize = 10;
            if (ints.size() >= 3) {
                pageNo = ints.get(1);
                pageSize = ints.get(2);
                if (pageNo <= 0) {
                    pageNo = 1;
                }
                if (pageSize >= 30) {
                    pageSize = 30;
                }
                if (pageSize <= 0) {
                    pageSize = 10;
                }
            }
            List<HashMap<String, Object>> list = ChatMessageDao.getInstance().select(groupUser.getGroupId().toString(), pageNo, pageSize);

            if (list != null) {
                Map<Object, Object> userMap = new HashMap<>();
                for (HashMap<String, Object> map : list) {
                    Long tempUser = (Long) map.get("fromUser");
                    Object user = userMap.get(tempUser);
                    if (user == null) {
                        user = PlayerManager.getInstance().getPlayer(tempUser);
                        if (user == null) {
                            user = UserDao.getInstance().selectUserByUserId(tempUser);
                            if (user == null) {
                                user = tempUser;
                            }
                        }
                        userMap.put(tempUser, user);
                    }

                    map.put("userName", (user instanceof Player) ? ((Player) user).getName() : ((user instanceof RegInfo) ? ((RegInfo) user).getName() : user));
                }
            }

            ComMsg.ComRes.Builder comMsgBuilder = ComMsg.ComRes.newBuilder();
            comMsgBuilder.setCode(WebSocketMsgType.sc_chat);
            comMsgBuilder.addParams(0);
            comMsgBuilder.addParams(0);
            comMsgBuilder.addStrParams(list == null ? JacksonUtil.writeValueAsString(Collections.emptyList()) : JacksonUtil.writeValueAsString(list));
            player.writeSocket(comMsgBuilder.build());
        } else if (opreate == 1) {
            if (strs.size() > 0) {
                String msg = strs.get(0);
                if (msg.length() > 100) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_206));
                } else {
                    msg = KeyWordsFilter.getInstance().filt(msg);
                    HashMap<String, Object> msgMap = new HashMap<>();
                    msgMap.put("msgType", "1");
                    msgMap.put("msgContent", msg);
                    msgMap.put("createdTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    msgMap.put("groupId", groupUser.getGroupId().toString());
                    msgMap.put("fromUser", String.valueOf(player.getUserId()));
                    msgMap.put("toUser", "0");

                    long ret = ChatMessageDao.getInstance().insert(msgMap);

                    ComMsg.ComRes.Builder comMsgBuilder = ComMsg.ComRes.newBuilder();
                    comMsgBuilder.setCode(WebSocketMsgType.sc_chat);
                    comMsgBuilder.addParams(1);
                    if (ret > 0) {
                        comMsgBuilder.addParams(1);
                        HashMap<String, Object> msgMap0 = new HashMap<>();
                        msgMap0.put("ret",ret);
                        msgMap0.put("msg",msg);
                        comMsgBuilder.addStrParams(JacksonUtil.writeValueAsString(msgMap0));
                        msgMap.put("keyId", ret);
                        msgMap.put("userName", player.getName());
                    } else {
                        comMsgBuilder.addParams(0);
                    }
                    player.writeSocket(comMsgBuilder.build());

                    if (ret > 0) {
                        //推送消息给其他玩家
                        List<HashMap<String, Object>> list = GroupDao.getInstance().loadAllGroupUser(groupUser.getGroupId());
                        if (list != null) {
                            ComMsg.ComRes.Builder comMsgBuilder0 = ComMsg.ComRes.newBuilder();
                            comMsgBuilder0.setCode(WebSocketMsgType.sc_chat);
                            comMsgBuilder0.addParams(0);
                            comMsgBuilder0.addParams(1);
                            comMsgBuilder0.addStrParams(JacksonUtil.writeValueAsString(msgMap));

                            GeneratedMessage msg0 = comMsgBuilder0.build();

                            for (HashMap<String, Object> map : list) {
                                long tempUserId = ((Number) map.get("userId")).longValue();
                                if (player.getUserId() != tempUserId) {
                                    MyWebSocket myWebSocket = WebSocketManager.webSocketMap.get(tempUserId);
                                    if (myWebSocket != null) {
                                        myWebSocket.send(msg0);
                                    }
                                }
                            }
                            Collection<Server> serverList = ServerManager.loadAllServers().values();
                            for (Server server : serverList) {
                                if (server.getId() != GameServerConfig.SERVER_ID) {
                                    String url = ServerManager.loadRootUrl(server);
                                    if (StringUtils.isNotBlank(url)) {
                                        url += "/online/notice.do?type=chat&userId=" + player.getUserId() + "&timestamp=" + System.currentTimeMillis() + "&message=" + ret;
                                        String ret0 = HttpUtil.getUrlReturnValue(url, 2);
                                        LogUtil.msgLog.info("url=" + url + ",ret=" + ret0);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_207));
            }
        } else {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_208));
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
