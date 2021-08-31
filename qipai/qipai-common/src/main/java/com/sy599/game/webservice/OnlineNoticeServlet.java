package com.sy599.game.webservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.general.GeneralHelper;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.CommonPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.UserCardRecordInfo;
import com.sy599.game.db.bean.competition.param.CompetitionClearingModelRes;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.ChatMessageDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.CoinSourceType;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.gcommand.com.competition.CompetitionJoinTableCommand;
import com.sy599.game.gcommand.com.competition.CompetitionServerCommand;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.*;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Future;

public class OnlineNoticeServlet extends HttpServlet {

    private final static String APP_KEY = "qweh#$*(_~)lpslot;589*/-+.-8&^%$#@!";

    private static final long serialVersionUID = 1L;

    public OnlineNoticeServlet() {
    }

    public void init() throws ServletException {
    }

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

    /**
     * 判断是否是数字（正整数、0、负整数）
     *
     * @param str
     * @return
     */
    private static boolean isDigits(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        } else {
            if (str.charAt(0) == '-') {
                return NumberUtils.isDigits(str.substring(1));
            } else if (str.charAt(0) == '+') {
                return NumberUtils.isDigits(str.substring(1));
            } else {
                return NumberUtils.isDigits(str);
            }
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String> params = UrlParamUtil.getParameters(request);
        String type = params.get("type");
        String userId = params.get("userId");
        String message = params.get("message");
        String timestamp = params.get("timestamp");
        String sign = params.get("sign");
        String ip = IpUtil.getIpAddr(request);
        LogUtil.msgLog.info("OnlineNoticeServlet|ip={},type={},userId={},message={},timestamp={},sign={}", ip, type, userId, message, timestamp, sign);

        if (!IpUtil.isIntranet(ip) && !"127.0.0.1".equals(ip) && !(NumberUtils.isDigits(timestamp) && (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) <= 5 * 60 * 1000) && MD5Util.getMD5String(APP_KEY + type + userId + message + timestamp).equalsIgnoreCase(sign))) {
            writeMsg(response, "ip or sign invalid");
            return;
        }
        try {
            switch (type) {
                case "playerIsGroup":
                    playerIsGroup(response, params);
                    break;
                case "agencyDissRoom":
                    agencyDissRoom(response, params);
                    break;
                case "playerUserState":
                    playerUserState(response, params);
                    break;
                case "playerCards":
                    playerCards(response, params);
                    break;
                case "chat":
                    chat(response, params);
                    break;
                case "autoCreateGroupTable":
                    autoCreateGroupTable(response, params);
                    break;
                case "setIp":
                    setIp(response, params);
                    break;
                case "online":
                    online(request, response);
                    break;
                case "marquee":
                    marquee(response, params);
                    break;
                case "groupApply":
                    groupApply(response, params);
                    break;
                case "commonApply":
                    commonApply(response, params);
                    break;
                case "notifyChangCards":
                    notifyChangCards(response, params);
                    break;
                case "autoCreateGroupTableNew":
                    autoCreateGroupTableNew(response, params);
                    break;
                case "notifyCreditUpdate":
                    notifyCreditUpdate(response, params);
                    break;
                case "notifyGroupUserLevelUp":
                    notifyGroupUserLevelUp(response, params);
                    break;
                case "changCoinAndNotify":
                    changCoinAndNotify(response, params);
                    break;
                case "goldRoomGroupId":
                    goldRoomGroupId(response, params);
                    break;
                case "notifyChangGolds":
                    notifyChangGolds(response, params);
                    break;
				case "playingRefreshNotify": 					//比赛场:比赛场刷新赛场报名人数
					refreshCompetitionNotify(response, params);
					break;
				case "playingChangePlayerServer":			 	//比赛场:通知客户端切服
					changePlayerServer(response, params);
					break;
				case "playingMatchPushBatchInnerRoom":			//比赛场:批量玩家匹配
					batchInnerRoom(response, params);
					break;
				case "playingShowClearingInfo":					//比赛场:展示结算
					showClearingInfo(response, params);
					break;
				case "clearingInvalidRoom":						//比赛场:手动清理房间
					clearingInvalidRoom(response, params);
					break;
				case "beginPlayingNotify":					//比赛场:赛前推送
					beginPlayingNotify(response, params);
					break;
				case "beginPlayingPush":					//比赛场:开赛推送,进入等待区
					beginPlayingPush(response, params);
					break;
				case "getServerAndCompetitionRoomId":					//比赛场:获得房间号
					getServerAndCompetitionRoomId(response, params);
					break;
				case "competitionJoinRoom":					//加入房间
					competitionJoinRoom(response, params);
					break;
				case "playingRankPush":					//榜单推送
					playingRankPush(response, params);
					break;
				case "notifyAndChangeGolds":				//刷新变更玩家资源
					notifyAndChangeGolds(response, params);
					break;
                case "notifyUpdateUser":
                    notifyUpdateUser(response, params);
                    break;
				case "cttConNotify":
					competitionCommonNotify(response,params);
				break;
				case "playingBatchCreateRoom":
					playingBatchCreateRoom(response,params);
				break;
				case "playingBatchRandomJoinRoom":
					playingBatchRandomJoinRoom(response,params);
				break;
				case "playingBatchStatRoomGame":
					playingBatchStatRoomGame(response,params);
				break;
                default:
                    writeMsg(response, "-2");
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     *@description 批量展示结算界面
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/8
     */
	public void showClearingInfo(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|showClearingInfo|start|" + params);
			if(!params.containsKey("user")){
				writeMsg(response, "param error");
				return;
			}
			CompetitionUtil.showClearingInfo(JSONObject.parseObject(params.get("clearingModel")/*HttpUtils.forSrcDecoder(params.get("clearingModel"))*/, CompetitionClearingModelRes.class));
			LogUtil.msgLog.info("CompetitionRoom|showClearingInfo|end"+params);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	 /**
     *@description 获取服务器和房间ID
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/8
     */
	public void getServerAndCompetitionRoomId(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|getServerAndCompetitionRoomId|start|" + params);
			if(!params.containsKey("user")){
				writeMsg(response, "param error");
				return;
			}

			String roomConfigId = params.get("roomConfigId");
			String playingId = params.get("playingId");
			int playType = Integer.valueOf(params.get("playType"));
			List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);
			Long pid = user.get(0);
			Player player = null;

			if (pid > 0) {
				player = PlayerManager.getInstance().getPlayer(user.get(0));
				if (player == null) {
					writeMsg(response, "param error");
					return;
				}
			} else {
				player = CompetitionJoinTableCommand.getRobot(pid, playType);
			}

			writeMsg(response, CompetitionServerCommand.process(player, ComReq.newBuilder().setCode(0).addStrParams(roomConfigId).addStrParams(playingId).build()));
			LogUtil.msgLog.info("CompetitionRoom|getServerAndCompetitionRoomId|end"+params);
		} catch (Exception e) {
			writeMsg(response, "error");
		}
	}

	 /**
     *@description 加入比赛房
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/8
     */
	public void competitionJoinRoom(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|competitionJoinRoom|start|" + params);
			if (!params.containsKey("user")) {
				writeMsg(response, "param error");
				return;
			}

			String roomConfigId = params.get("roomConfigId");
			String playingId = params.get("playingId");
			int playType = Integer.valueOf(params.get("playType"));
			List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);
			Long pid = user.get(0);
			Player player = null;
			if (pid > 0) {
				player = PlayerManager.getInstance().getPlayer(user.get(0));
				if (player == null) {
					writeMsg(response, "param error");
					return;
				}
			} else {
				player = CompetitionJoinTableCommand.getRobot(pid, playType);
			}

			CompetitionJoinTableCommand.process(player, ComReq.newBuilder().setCode(0).addStrParams(roomConfigId).addStrParams(playingId).build());

			CompetitionServerCommand.process(player, ComReq.newBuilder().addStrParams(params.get("roomConfigId")).addStrParams(params.get("playingId")).build());
			LogUtil.msgLog.info("CompetitionRoom|competitionJoinRoom|end" + params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     *@description 批量进入房间
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/3
     */
	public void batchInnerRoom(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|batchInnerRoom|start|"+params);
			if(!params.containsKey("user")){
				writeMsg(response, "param error");
				return;
			}
			/*HttpUtils.forSrcDecoder(params.get("clearingModel")*/
			//线程池依赖
			Future<List<Long>> submit = TaskExecutor.getInstance().EXECUTOR_SERVICE.submit(() -> {
				List<Long> action = null;
				try {
					//需要推送的玩家, 清算模型
					action = CompetitionJoinTableCommand.action(JSONArray.parseArray(params.get("user"), Long.class), JSONObject.parseObject(params.get("clearingModel")
							.replace("%2B", "+")
							.replace("%20", " ")
							.replace("%2F", "/")
							.replace("%3F", "?")
							.replace("%23", "%")
							.replace("%26", "&")
							.replace("%3D", "="), CompetitionClearingModelRes.class));

					LogUtil.msgLog.info("CompetitionRoom|batchInnerRoom|end" + params);
				} catch (Exception e) {
					LogUtil.msgLog.error("competition|batchInnerRoom|error,{}", e);
				}

				return action;
			});

			writeMsg(response, JSONArray.toJSONString(submit.get()));
		} catch (Exception e) {
			LogUtil.e("competition|batchInnerRoom|action|error|{}",e);
		}
	}

    /**
     *@description 批量进入房间
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/3
     */
	public void playingBatchCreateRoom(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|playingBatchCreateRoom|start|"+params);
			int tableCount = Integer.valueOf(String.valueOf(params.get("tableCount")));
			CompetitionClearingModelRes clearingModel = JSONObject.parseObject(params.get("clearingModel"), CompetitionClearingModelRes.class);
			CompetitionJoinTableCommand.tCreate(tableCount, CompetitionUtil.getCompetitionRoomConfig(clearingModel.getRoomConfigId()), clearingModel);
			writeMsg(response, "success");
		} catch (Exception e) {
			LogUtil.e("competition|playingBatchCreateRoom|action|error|{}",e);
		}
	}

    /**
     *@description 批量进入房间
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/3
     */
	public void playingBatchRandomJoinRoom(HttpServletResponse response, Map<String, String> params) {
		try {
			if(!params.containsKey("user")){
				LogUtil.msgLog.info("CompetitionRoom|playingBatchRandomJoinRoom|paramError");
				writeMsg(response, "param error");
				return;
			}

			CompetitionClearingModelRes clearingModel = JSONObject.parseObject(params.get("clearingModel"), CompetitionClearingModelRes.class);

			LogUtil.msgLog.info("CompetitionRoom|playingBatchRandomJoinRoom|start|" + clearingModel.getPlayingId() + "|" + clearingModel.getCurStep() + "|" + clearingModel.getCurRound() + "|" + clearingModel);

			CompetitionJoinTableCommand.batchJoinTable(CompetitionUtil.getCompetitionRoomConfig(clearingModel.getRoomConfigId()), clearingModel);
			writeMsg(response, "success");
		} catch (Exception e) {
			LogUtil.e("competition|playingBatchRandomJoinRoom|action|error|{}",e);
		}
	}

    /**
     *@description 批量进入房间
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/3
     */
	public void playingBatchStatRoomGame(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|playingBatchStatRoomGame|start|"+params);
			CompetitionClearingModelRes clearingModel = JSONObject.parseObject(params.get("clearingModel"), CompetitionClearingModelRes.class);
			CompetitionJoinTableCommand.batchStartGame(clearingModel);
			writeMsg(response, "success");
		} catch (Exception e) {
			LogUtil.e("competition|playingBatchStatRoomGame|action|error|{}",e);
		}
	}

    /**
     *@description 批量管理房间
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/3
     */
	public void clearingInvalidRoom(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|clearingInvalidRoom|start|"+params);
			if(!params.containsKey("user")){
				writeMsg(response, "param error");
				return;
			}

			CompetitionJoinTableCommand.clearInvalidTable(NumberUtils.toLong(params.get("playingId")), true, params.containsKey("force"), params.get("curStep"), params.get("curRound"));

			LogUtil.msgLog.info("CompetitionRoom|clearingInvalidRoom|end"+params);
		} catch (Exception e) {
			LogUtil.e("competition|clearingInvalidRoom|action|error|{}",e);
			writeMsg(response, e.getMessage());
		}
	}

    /**
     *@description 修改玩家所在服务器
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/2
     */
	public void changePlayerServer(HttpServletResponse response, Map<String, String> params) {
		if(!params.containsKey("user")){
			writeMsg(response, "param error");
			return;
		}

		Map<Long, Long> serverIds = JSONObject.parseObject(params.get("serverIds"), Map.class);
		List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);
		Long serverId = NumberUtils.toLong(params.get("bindServerId"));

		Server server = ServerManager.loadServer(serverId.intValue());

		Iterator<Long> iterator = user.iterator();
		while (iterator.hasNext()) {
			Long userId = iterator.next();
			if (userId <= 0) continue;
			if (serverIds != null && serverIds.size() > 0 && serverIds.containsKey(userId)) {
				server = ServerManager.loadServer(serverIds.get(userId).intValue());
			}
			try {
				if (CompetitionUtil.changePlayServer(userId, server)) {
					writeMsg(response, "success");
				} else {
					writeMsg(response, "play[" + userId + "] offline ");
				}
			} catch (Exception e) {
				LogUtil.e("competition|changePlayerServer|action|error|{}", e);
				writeMsg(response, e.getMessage());
			}
		}
	}

	/**
	 *@description 赛前推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/3
	 */
	public void beginPlayingNotify(HttpServletResponse response, Map<String, String> params) {
		try {
			params.put("args",params.get("args")/*HttpUtils.forSrcDecoder(params.get("args"))*/);
			LogUtil.msgLog.info("CompetitionRoom|beginPlayingNotify|start|"+params);
			if(!params.containsKey("user")){
				writeMsg(response, "param error");
				return;
			}
			List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);
			List<String> args = JSONObject.parseObject(params.get("args"), List.class);

			Iterator<Long> iterator = user.iterator();
			while (iterator.hasNext()) {
				Long pid = iterator.next();
				Player player = PlayerManager.getInstance().getPlayer(pid);
				if (player == null) continue;
				player.writeSocket(ComRes.newBuilder().setCode(WebSocketMsgType.competition_msg_before_playing).addAllStrParams(args).build());
			}

			LogUtil.msgLog.info("CompetitionRoom|beginPlayingNotify|end"+params);
		}
		catch (Exception e) {
			LogUtil.e("competition|beginPlayingNotify|action|error|{}",e);
		}
	}

	/**
	 *@description 赛前推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/3
	 */
	public void beginPlayingPush(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|beginPlayingPush|start|"+params);
			if(!params.containsKey("user") || !params.containsKey("playing")){
				writeMsg(response, "param error");
				return;
			}

			List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);

			String arg = params.get("playing");//HttpUtils.forSrcDecoder(params.get("playing"));

			Iterator<Long> iterator = user.iterator();
			while (iterator.hasNext()) {
				Long pid = iterator.next();
				Player player = PlayerManager.getInstance().getPlayer(pid);
				if (player == null) continue;
				player.writeSocket(ComRes.newBuilder().setCode(WebSocketMsgType.competition_msg_begin_playing).addStrParams(arg).build());
			}

			LogUtil.msgLog.info("CompetitionRoom|beginPlayingPush|end"+params);
		}
		catch (Exception e) {
			LogUtil.e("competition|beginPlayingPush|action|error|{}",e);
		}
	}

	/**
	 *@description 通用推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/28
	 */
	public void competitionCommonNotify(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|competitionCommonNotify|start|" + params);
			if (!params.containsKey("user") || !params.containsKey("code")) {
				writeMsg(response, "param error");
				return;
			}
			List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);
			List<String> args = JSONObject.parseObject(params.get("args"), List.class);

			Iterator<Long> iterator = user.iterator();
			while (iterator.hasNext()) {
				Long pid = iterator.next();
				Player player = PlayerManager.getInstance().getPlayer(pid);
				if (player == null) continue;
				player.writeSocket(ComRes.newBuilder().setCode(Integer.valueOf(params.get("code"))).addAllStrParams(args).build());
			}

			LogUtil.msgLog.info("CompetitionRoom|beginPlayingNotify|end" + params);
		}
		catch (Exception e) {
			LogUtil.e("competition|beginPlayingNotify|action|error|{}", e);
		}
	}


	/**
	 *@description 排名推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/3
	 */
	public void playingRankPush(HttpServletResponse response, Map<String, String> params) {
		try {
			LogUtil.msgLog.info("CompetitionRoom|playingRankPush|start|"+params);
			if (!params.containsKey("user") || !params.containsKey("arg")) {
				writeMsg(response, "param error");
				return;
			}

			List<Long> user = JSONArray.parseArray(params.get("user"), Long.class);
			Map<Integer, String> arg = JSONObject.parseObject(params.get("arg"), HashMap.class);

			Iterator<Long> iterator = user.iterator();
			while (iterator.hasNext()) {
				Long pid = iterator.next();
				Player player = PlayerManager.getInstance().getPlayer(pid);
				if (player == null) continue;
				player.writeSocket(ComRes.newBuilder().setCode(WebSocketMsgType.competition_msg_rank).addStrParams(arg.get(pid.intValue())).build());
			}

			LogUtil.msgLog.info("CompetitionRoom|playingRankPush|end"+params);
		}
		catch (Exception e) {
			LogUtil.e("competition|playingRankPush|action|error|{}",e);
		}
	}

	/**
	 *@description 比赛场刷新玩家当前报名的人数
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/21
	 */
	public void refreshCompetitionNotify(HttpServletResponse response, Map<String, String> params) throws Exception {
		long t = System.currentTimeMillis();
		if (params.containsKey("user") && !NumberUtils.isDigits(params.get("user"))) {        //批量处理接口
			refreshCompetitionBatchNotifyUser(response, params);
		} else {    //单个处理接口
			refreshCompetitionNotifyUser(response, params);
		}

		writeMsg(response, "success");

		LogUtil.msgLog.info("CompetitionPushService refresh apply human {}ms", (System.currentTimeMillis() - t));
	}

	public boolean refreshCompetitionNotifyUser(HttpServletResponse response, Map<String, String> params) {
		String userId = params.get("user");
		String params2 = params.get("params");

		if(NumberUtils.isDigits(userId) && !StringUtils.isBlank(params2)){
			competitionSingleNotifyUser(Long.valueOf(userId), params2);
		}

		return true;
	}

	/**
	 *@description 单个玩家推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	private void competitionSingleNotifyUser(Long userId, String params2) {
		Optional<Player> player = Optional.ofNullable(PlayerManager.getInstance().getPlayer(userId));
		if (player.isPresent()) {	//没有登录不做处理
			//参数
			List<Integer> params1 = JSONArray.parseArray(params2, Integer.class);
			player.get().writeSocket(ComRes.newBuilder().setCode(WebSocketMsgType.competition_msg_refresh).addAllParams(params1).build());
		}
	}

	/**
	 *@description 批量玩家推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public boolean refreshCompetitionBatchNotifyUser(HttpServletResponse response, Map<String, String> params) {
		if(params.containsKey("user")){
			String params2 = params.get("params");

			List<Long> users = JSONArray.parseArray(params.get("user"), Long.class);

			LogUtil.msgLog.info("OnlineNoticeServlet CompetitionPushService refresh apply human batch {}",(users.size()));

			Iterator<Long> iterator = users.iterator();
			while (iterator.hasNext()) {
				Long next = iterator.next();
				competitionSingleNotifyUser(next, params2);
			}
		}

		return true;
	}

    public void playerIsGroup(HttpServletResponse response, Map<String, String> params) {
        String userId = params.get("userId");
        String message = params.get("message");
        if (NumberUtils.isDigits(userId) && isDigits(message)) {
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                int isGroup = Integer.parseInt(message);
                player.setIsGroup(isGroup);
                if (isGroup == 1) {
                    GroupUser groupUser = GroupDao.getInstance().loadGroupUser(Long.parseLong(userId), null);
                    player.setGroupUser(groupUser);
                } else {
                    player.setGroupUser(null);
                }
                writeMsg(response, "1");
            } else {
                writeMsg(response, "0");
            }
        } else {
            writeMsg(response, "-1");
        }
    }

    public void agencyDissRoom(HttpServletResponse response, Map<String, String> params) {
        String roomId = params.get("roomId");//房间id
        String agencyId = params.get("agencyId");//代理id
        String role = params.get("role");//代理权限
        String serverId = params.get("serverId");//服id
        if (String.valueOf(GameServerConfig.SERVER_ID).equals(serverId)) {
            BaseTable table = TableManager.getInstance().getTable(Long.parseLong(roomId));
            if (table != null) {
                int playType = table.getPlayType();
                boolean canDiss = true;
                List<Player> players = new ArrayList<>(table.getPlayerMap().values());
                List<Long> idList = new ArrayList<>();
                for (Player player : players) {
                    if (!String.valueOf(player.getPayBindId()).equals(agencyId)) {
                        canDiss = false;
                    }
                    idList.add(player.getUserId());
                }
                if ("0".equals(role)) {
                    if (table.getPlayBureau() > 1) {
                        try {
                            table.sendAccountsMsg();
                        } catch (Throwable e) {
                            LogUtil.errorLog.error("tableId=" + table.getId() + ",total calc Exception:" + e.getMessage(), e);
                            GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + table.getId() + "被解散").build();
                            for (Player player : players) {
                                player.writeSocket(errorMsg);
                            }
                        }
                    }
                    table.setTiqianDiss(true);
                    LogUtil.msgLog.info("BaseTable|dissReason|agencyDissRoom|1|" + table.getId() + "|" + table.getPlayBureau());
                    int res = table.diss();
                    if (res == 1) {
                        for (Player player : players) {
                            if (player.getIsOnline() == 1) {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_57, "0".equals(role) ? "管理员" : "代理"));
                                player.writeComMessage(WebSocketMsgType.res_code_disstable,String.valueOf(table.calcTableType()), playType);
                            }
                        }
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("roomId", roomId);
                        paramMap.put("agencyId", agencyId);
                        paramMap.put("serverId", serverId);
                        paramMap.put("players", StringUtil.implode(idList));
                        paramMap.put("createTime", TimeUtil.now());
                        TableDao.getInstance().addDissInfo(paramMap);
                    }
                    // 0 解散失败 1解散成功
                    writeMsg(response, res + "");
                    if (res != 1) {
                        LogUtil.e("agencyDissRoom-->roomId:" + roomId + ",agencyId:" + agencyId + ",res:" + res);
                    }
                    return;
                } else {
                    // -3 当前房间有绑定其他代理邀请码的玩家参与，请联系客服处理！
                    writeMsg(response, "-3");
                }
            } else {
                LogUtil.e("agencyDissRoom table is null-->roomId:" + roomId + ",myServerId:" + GameServerConfig.SERVER_ID);
                // -2 房间不存在
                writeMsg(response, "-2");
            }
        } else {
            LogUtil.e("agencyDissRoom serverId err-->rqServerId:" + serverId + ",myServerId:" + GameServerConfig.SERVER_ID);
            //-4 服id不正确
            writeMsg(response, "-4");
        }
        return;
    }

    public void playerUserState(HttpServletResponse response, Map<String, String> params) {
        String userId = params.get("userId");
        String message = params.get("message");
        if (NumberUtils.isDigits(userId) && isDigits(message)) {
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                player.changeUserState(Integer.parseInt(message));
                writeMsg(response, "1");
            } else {
                writeMsg(response, "0");
            }
        } else {
            writeMsg(response, "-1");
        }
    }

    public void playerCards(HttpServletResponse response, Map<String, String> params) {
        String userId = params.get("userId");
        String notifyType = params.get("msgType");
        Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
        if (player == null) {
            writeMsg(response, "0");
            return;
        }
        if("1".equals(notifyType)){
            long cards = StringUtils.isBlank(params.get("cards")) ? 0 : Long.valueOf(params.get("cards"));
            long freeCards = StringUtils.isBlank(params.get("freeCards")) ? 0 : Long.valueOf(params.get("freeCards"));
            player.changeCards(freeCards, cards, false, true, CardSourceType.unknown);
            writeMsg(response, "1");
        }else{
            // 旧功能，待删除
            String message = params.get("message");
            if (isDigits(message)) {
                if ("1".equals(params.get("free"))) {
                    player.changeCards(Long.parseLong(message), 0, "1".equals(params.get("SAVEDB")), true, CardSourceType.unknown);
                } else {
                    player.changeCards(0, Long.parseLong(message), "1".equals(params.get("SAVEDB")), true, CardSourceType.unknown);
                }
                writeMsg(response, "1");
            } else {
                writeMsg(response, "-1");
            }
        }


    }

    public void chat(HttpServletResponse response, Map<String, String> params) throws Exception {
        writeMsg(response, "1");
        String message = params.get("message");
        HashMap<String, Object> msgMap = ChatMessageDao.getInstance().select(message);
        if (msgMap != null && msgMap.size() > 0) {
            long fromUser = (Long) msgMap.get("fromUser");
            RegInfo regInfo = UserDao.getInstance().selectUserByUserId(fromUser);
            msgMap.put("userName", regInfo == null ? fromUser : regInfo.getName());

            long groupId = (Long) msgMap.get("groupId");
            List<HashMap<String, Object>> list = GroupDao.getInstance().loadAllGroupUser(groupId);
            if (list != null) {
                ComMsg.ComRes.Builder comMsgBuilder0 = ComMsg.ComRes.newBuilder();
                comMsgBuilder0.setCode(WebSocketMsgType.sc_chat);
                comMsgBuilder0.addParams(0);
                comMsgBuilder0.addParams(1);
                comMsgBuilder0.addStrParams(JacksonUtil.writeValueAsString(msgMap));
                GeneratedMessage msg0 = comMsgBuilder0.build();
                for (HashMap<String, Object> map : list) {
                    long tempUserId = ((Number) map.get("userId")).longValue();
                    if (fromUser != tempUserId) {
                        MyWebSocket myWebSocket = WebSocketManager.webSocketMap.get(tempUserId);
                        if (myWebSocket != null) {
                            myWebSocket.send(msg0);
                        }
                    }
                }
            }
        }
    }

    public void autoCreateGroupTable(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        String message = params.get("message");
        String gameType = params.get("gameType");
        if (NumberUtils.isDigits(userId) && isDigits(message)) {
            BaseTable table = TableManager.getInstance().getInstanceTable(NumberUtils.toInt(gameType, -1));
            LogUtil.msgLog.info("service autoCreateGroupTable:userId=" + userId + ",groupId=" + message + ",gameType=" + gameType);
            String configId = params.get("configId");
            if (StringUtils.isBlank(configId)) {
                GameUtil.autoCreateGroupTable(message, table != null ? table.getPlayerClass() : CommonPlayer.class);
            } else {
                GameUtil.autoCreateGroupTable(message, table != null ? table.getPlayerClass() : CommonPlayer.class, Long.parseLong(configId));
            }
            writeMsg(response, "1");
        } else {
            writeMsg(response, "-1");
        }
    }

    public void setIp(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        String message = params.get("message");
        if (NumberUtils.isDigits(userId) && message != null && GeneralHelper.isStrIPAddress(message)) {
            Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
            if (player != null) {
                player.setIp(message, false);
                writeMsg(response, "1");
            } else {
                writeMsg(response, "0");
            }
        } else {
            writeMsg(response, "-1");
        }
    }

    public void online(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OutputUtil.output(0, NettyUtil.channelUserMap.size(), request, response, false);
    }

    public void marquee(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        String message = params.get("message");
        if (StringUtils.isNotBlank(message)) {
            if (CommonUtil.isPureNumber(userId)) {
                Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
                if (player != null) {
                    MarqueeManager.getInstance().sendMarquee(message, NumberUtils.toInt(params.get("round"), 1), NumberUtils.toInt(params.get("msgType"), 0), player);
                }
            } else {
                MarqueeManager.getInstance().sendMarquee(message, NumberUtils.toInt(params.get("round"), 1), NumberUtils.toInt(params.get("msgType"), 0), null);
            }
        }
        writeMsg(response, "1");
    }

    public void groupApply(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
        if (player != null) {
            player.writeComMessage(WebSocketMsgType.MULTI_CREATE_TABLE, 1, 0);
            writeMsg(response, "1");
        } else {
            writeMsg(response, "0");
        }
    }

    public void commonApply(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
        if (player != null) {
            player.writeComMessage(WebSocketMsgType.COMMON_FRESH_APPLY, 1, 0);
            writeMsg(response, "1");
        } else {
            writeMsg(response, "0");
        }
    }

    public void notifyChangCards(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        if (NumberUtils.isDigits(userId)) {
            long cards = Long.parseLong(params.get("cards"));
            long freeCards = Long.parseLong(params.get("freeCards"));
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            boolean saveRecord = true;
            if (params.containsKey("saveRecord") && "0".equals(params.get("saveRecord"))) {
                saveRecord = false;
            }
            if (player != null) {
                player.notifyChangeCards(cards, freeCards, saveRecord, CardSourceType.bjd_changeCards);
                writeMsg(response, "1");
            } else {
                RegInfo regInfo = UserDao.getInstance().getUser(Long.valueOf(userId));
                if (regInfo != null) {
                    PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(Long.valueOf(userId), regInfo.getFreeCards(), regInfo.getCards(), (int) freeCards, (int) cards, 0, CardSourceType.bjd_changeCards));
                    writeMsg(response, "1");
                } else {
                    writeMsg(response, "0");
                }
            }
        }
        writeMsg(response, "1");
    }

    public void autoCreateGroupTableNew(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        String message = params.get("message");
        if (NumberUtils.isDigits(userId) && isDigits(message)) {
            LogUtil.msgLog.info("service autoCreateGroupTableNew:userId=" + userId + ",groupId=" + message);
            String configId = params.get("configId");
            if (StringUtils.isNotBlank(configId)) {
                GameUtil.autoCreateGroupTableNew(message, Long.valueOf(configId));
            }
            writeMsg(response, "1");
        }
    }

    public void notifyCreditUpdate(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        long groupId = Long.parseLong(params.get("groupId"));
        Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
        if (player != null) {
            player.notifyCreditUpdate(groupId);
            writeMsg(response, "1");
        } else {
            writeMsg(response, "0");
        }
    }

    public void tmp(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        String message = params.get("message");
    }


    /**
     * 亲友圈玩家升级
     * @param response
     * @param params
     * @throws Exception
     */
    public void notifyGroupUserLevelUp(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        if (NumberUtils.isDigits(userId)) {
            String groupId = params.get("groupId");
            String level = params.get("level");
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                player.notifyGroupUserLevelUp(groupId, level);
            }
        }
        writeMsg(response, "1");
    }

    public void changCoinAndNotify(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        if (NumberUtils.isDigits(userId)) {
            long coin = Long.parseLong(params.get("coin"));
            long freeCoin = Long.parseLong(params.get("freeCoin"));
            String sourceName = params.get("sourceName");
            CoinSourceType coinSourceType = CoinSourceType.unknown;
            if (!StringUtil.isBlank(sourceName)) {
                coinSourceType = CoinSourceType.valueOf(sourceName);
            }
            if (coinSourceType == null) {
                coinSourceType = CoinSourceType.unknown;
            }
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                player.changeUserCoin(coin, freeCoin, true, -1, coinSourceType);
                writeMsg(response, "1");
            } else {
                RegInfo regInfo = UserDao.getInstance().getUser(Long.valueOf(userId));
                if (regInfo != null) {
                    UserDao.getInstance().changeUserCoin(regInfo.getUserId(), coin, freeCoin);
                    Map<String, Object> record = new HashMap<>();
                    record.put("userId", userId);
                    record.put("freeCoin", regInfo.getFreeCoin());
                    record.put("coin", regInfo.getCoin());
                    record.put("addFreeCoin", freeCoin);
                    record.put("addCoin", coin);
                    record.put("playType", 0);
                    record.put("sourceType", coinSourceType.getSourceType());
                    record.put("sourceName", coinSourceType.getSourceName());
                    record.put("createTime", new Date());
                    PlayerManager.getInstance().addUserCoinRecord(record);
                    writeMsg(response, "1");
                } else {
                    writeMsg(response, "0");
                }
            }
        }
        writeMsg(response, "1");
    }

    public void goldRoomGroupId(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        if (NumberUtils.isDigits(userId)) {
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                player.setGoldRoomGroupId(0);
            }
        }
        writeMsg(response, "1");
    }

    public void notifyChangGolds(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        if (NumberUtils.isDigits(userId)) {
            long goldChange = Long.parseLong(params.get("goldChange"));
            long allGold = Long.parseLong(params.get("allGold"));
            long curGold = Long.parseLong(params.get("curGold"));
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                player.refreshGoldFromDb();
                player.writeGoldMessage(curGold, goldChange, allGold);
                writeMsg(response, "1");
            }
        }
        writeMsg(response, "1");
    }

    public void notifyUpdateUser(HttpServletResponse response, Map<String, String> params) throws Exception {
        String userId = params.get("userId");
        if (NumberUtils.isDigits(userId)) {
            String name = params.get("name");
            String headimgurl = params.get("headimgurl");
            String pw = params.get("pw");
            Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
            if (player != null) {
                if (StringUtils.isNotBlank(name)) {
                    player.setName(name);
                }
                if (StringUtils.isNotBlank(headimgurl)) {
                    player.setHeadimgurl(headimgurl);
                    player.notifyHeadimgurl(headimgurl);
                }
                writeMsg(response, "1");
            }
        }
        writeMsg(response, "1");
    }

	public void notifyAndChangeGolds(HttpServletResponse response, Map<String, String> params) throws Exception {
		if(!params.containsKey("sourceType")){
			LogUtil.msg("competition|sign|consumer|error|sourceType");
			return;
		}
		List<Long> users = JSONArray.parseArray(params.get("user"), Long.class);
		LogUtil.msgLog.info("competition|sign|consumer|" + users + "|" + GameServerConfig.SERVER_ID);
		boolean res = false;

		if (!CollectionUtils.isEmpty(users)) {
			Long userId = users.get(0);
			//机器人
			if (userId <= 0) {
				return;
			}

			int sourceType = Integer.parseInt(params.get("sourceType"));
//			long freeNum = Long.parseLong(params.get("freeNum"));
			long freeNum = Long.parseLong(params.get("num"));
			int playType = Integer.parseInt(params.get("playType"));
			Player player = PlayerManager.getInstance().getPlayer(userId);
			boolean recycle = false;
			if (player == null) {
				player = PlayerManager.getInstance().loadPlayer(userId, 16);
				recycle = true;
				LogUtil.msgLog.info("competition|sign|consumer|recycle");
			}
			if (player != null) {
				SourceType type = SourceType.values()[sourceType - 1];

				switch (type) {
					case COMPETITION_PLAYING_AWARD2://礼券
						res = UserDao.getInstance().updateGoldenBeans(player.getUserId(), (int) freeNum) > 0;
					break;
					default:
						if ((res = player.changeGold(freeNum, 0, type))) {
							player.refreshGoldFromDb();
//							if (!recycle) {
//								player.writeGoldMessage(curGold, goldChange, allGold);
//							}

							LogUtil.msgLog.info("competition|sign|consumer|changeGold|success");
						}
						break;
				}

				if (recycle && (player.getMyWebSocket() == null || player.getMyWebSocket().getCtx() == null || !player.getMyWebSocket().isLoginSuccess())) {
					PlayerManager.getInstance().removePlayer(player);
				}
			}
		}

		writeMsg(response, res ? "1" : "0");
	}
}
