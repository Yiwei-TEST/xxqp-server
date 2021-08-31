package com.sy599.game.assistant;

import com.sy.mainland.util.Base64Util;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.IpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.CommonPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.DaikaiTable;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

public class AssisServlet extends HttpServlet {
    private final static String APP_KEY = "asdw#$*(_~)jmh;58yys9*/-+.-8&^%$#@!";
    private final static String YJ_APP_KEY = "dw#$*kk1kw2_~)j4b;38fr96*/-+.-8wqw&^%$#@!";

    private static final long serialVersionUID = 1L;

    public AssisServlet() {
    }

    public void init() throws ServletException {
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doPost(req, resp);
        }finally {
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (GameUtil.isPlayYjGame()){
            String unionId = req.getParameter("vcUnionId");
            String vcChatRoomSerialNo = req.getParameter("vcChatRoomSerialNo");
            String vcSerialNo = req.getParameter("vcSerialNo");
            String ip = IpUtil.getIpAddr(req);
            String strParamsStr = req.getParameter("strParams");
            String sign = req.getParameter("vcSign");
            LogUtil.msg("assis create table start-->params:" + req.getParameterMap());
            String nResult;
            long tableId = 0;
            String mySign = MD5Util.getMD5String(vcSerialNo+YJ_APP_KEY);
            List<Integer> params = new ArrayList<>();
            if (SharedConstants.isAssisOpen() && (IpUtil.isIntranet(ip) || (mySign.equalsIgnoreCase(sign)))) {
                if (checkParams(req, params)) {
                    LogUtil.msg("assis params-->vcSerialNo:" + vcSerialNo + ",vcChatRoomSerialNo:" + vcChatRoomSerialNo + "," + params);
                    RegInfo user = UserDao.getInstance().selectUserByUnionId(unionId);
                    if (user == null) {
                        nResult = "15";
                        LogUtil.e("assis err-->user is null-->unionId:" + unionId);
                    } else {
                        int count;
                        List<Integer> wanfaIds;
                        if (!StringUtil.isBlank(strParamsStr)) {
                            wanfaIds = StringUtil.explodeToIntList(strParamsStr);
                        } else {
                            wanfaIds = new ArrayList<>();
                        }
                        if (wanfaIds.isEmpty())
                            count = TableDao.getInstance().getDaikaiTableCount(user.getUserId());
                        else
                            count = TableDao.getInstance().getWanfaDaikaiTableCount(user.getUserId(), wanfaIds);
                        if (count >= 10) {
                            nResult = "16";
                        } else {
                            tableId = daikaiTable(user, vcSerialNo, vcChatRoomSerialNo, params, strParamsStr == null ? "" : strParamsStr);
                            if (tableId == -1L) {
                                nResult = "12";
                            } else if (tableId == -2L) {
                                nResult = "13";
                            } else if (tableId < 0){
                                nResult = "14";
                            } else {
                                nResult = "1";
                            }
                        }
                    }
                } else {
                    nResult = "14";
                }
            } else {
                LogUtil.e("assis err-->sign:" + sign + ",mySign:" + mySign+",isAssisOpen:"+ SharedConstants.isAssisOpen()+",ip:"+ip);
                return;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("nResult", nResult);
            resultMap.put("vcResult", "success");
            resultMap.put("vcRoomId", tableId + "");
            resultMap.put("vcCallURL", "");
            resultMap.put("vcTitle", "");
            resultMap.put("vcDesc", "");
            resultMap.put("vcLogoImgURL", "http://res1.mgdzz.com/wzyx_icon.png");
            Writer writer = res.getWriter();
            writer.write(JacksonUtil.writeValueAsString(resultMap));

            LogUtil.msg("assis create table over-->resultMap:" + resultMap);
        }else{
            String unionId = req.getParameter("vcUnionId");
            String vcChatRoomSerialNo = req.getParameter("vcChatRoomSerialNo");
            String vcSerialNo = req.getParameter("vcSerialNo");
            String ip = IpUtil.getIpAddr(req);
            String strParamsStr = req.getParameter("strParams");
            String sign = req.getParameter("vcSign");
            LogUtil.msg("assis create table start-->params:" + req.getParameterMap());
            String nResult;
            long tableId = 0;
            String mySign = MD5Util.getMD5String(vcSerialNo+APP_KEY);
            List<Integer> params = new ArrayList<>();
            if (SharedConstants.isAssisOpen() && (IpUtil.isIntranet(ip) || (mySign.equalsIgnoreCase(sign)))) {
                if (checkParams(req, params)) {
                    LogUtil.msg("assis params-->vcSerialNo:" + vcSerialNo + ",vcChatRoomSerialNo:" + vcChatRoomSerialNo + "," + params);
                    RegInfo user = UserDao.getInstance().selectUserByUnionId(unionId);
                    if (user == null) {
                        nResult = "15";
                        LogUtil.e("assis err-->user is null-->unionId:" + unionId);
                    } else {
                        int count;
                        List<Integer> wanfaIds;
                        if (!StringUtil.isBlank(strParamsStr)) {
                            wanfaIds = StringUtil.explodeToIntList(strParamsStr);
                        } else {
                            wanfaIds = new ArrayList<>();
                        }
                        if (wanfaIds.isEmpty())
                            count = TableDao.getInstance().getDaikaiTableCount(user.getUserId());
                        else
                            count = TableDao.getInstance().getWanfaDaikaiTableCount(user.getUserId(), wanfaIds);
                        if (count >= 10) {
                            nResult = "16";
                        } else {
                            tableId = daikaiTable(user, vcSerialNo, vcChatRoomSerialNo, params, strParamsStr == null ? "" : strParamsStr);
                            if (tableId == -1L) {
                                nResult = "12";
                            } else if (tableId == -2L) {
                                nResult = "13";
                            } else if (tableId == -3L) {
                                nResult = "14";
                            } else {
                                nResult = "1";
                            }
                        }
                    }
                } else {
                    nResult = "14";
                }
            } else {
                LogUtil.e("assis err-->sign:" + sign + ",mySign:" + mySign+",isAssisOpen:"+SharedConstants.isAssisOpen()+",ip:"+ip);
                return;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("nResult", nResult);
            resultMap.put("vcResult", "success");
            resultMap.put("vcRoomId", tableId + "");
            resultMap.put("vcCallURL", "");
            resultMap.put("vcTitle", "");
            resultMap.put("vcDesc", "");
            resultMap.put("vcLogoImgURL", "http://mjres1.xiaoganyouxi.com/gsmj_icon.png");
            Writer writer = res.getWriter();
            writer.write(JacksonUtil.writeValueAsString(resultMap));
            LogUtil.msg("assis create table over-->resultMap:" + resultMap);
        }

    }

    /**
     * 当房间创建,解散,结束以及人员变动的时候回调
     */
    public static void sendRoomStatus(DaikaiTable daikaiTable, String nStatus) {
        sendRoomStatus(null, daikaiTable, nStatus);
    }

    /**
     * 当房间创建,解散,结束以及人员变动的时候回调
     */
    public static void sendRoomStatus(BaseTable table, String nStatus) {
        sendRoomStatus(table, null, nStatus);
    }

    /**
     * 当房间创建,解散,结束以及人员变动的时候回调
     */
    public static void sendRoomStatus(BaseTable table, DaikaiTable daikaiTable, String nStatus) {
        if (GameUtil.isPlayYjGame()){
            try {
                String addr = "http://api.kaiyiju.com.cn/Interface.asmx/RoomStatusInfo";
                Map<String, String> result = new HashMap<>();
                Map<String, Object> strContext = new HashMap<>();
                String strSign = "";
                // 调用方式为post
                // 格式为json
                strContext.put("vcMerchantNo", ResourcesConfigsUtil.loadServerPropertyValue("storeNo"));//商家编号
                String id;
                String assisCreateNo;
                String assisGroupNo;
                Map<Long, Player> playerMap;
                if (table != null) {
                    id = String.valueOf(table.getId());
                    assisCreateNo = String.valueOf(table.getAssisCreateNo());
                    assisGroupNo = String.valueOf(table.getAssisGroupNo());
                    playerMap = table.getPlayerMap();
                } else {
                    id = String.valueOf(daikaiTable.getTableId());
                    assisCreateNo = String.valueOf(daikaiTable.getAssisCreateNo());
                    assisGroupNo = String.valueOf(daikaiTable.getAssisGroupNo());
                    playerMap = new HashMap<>();
                }
                strContext.put("vcRoomNo", id);//房间号
                strContext.put("vcSerialNo", assisCreateNo);//开房编号
                strContext.put("vcChatRoomSerialNo", assisGroupNo);//群编号
                strContext.put("nStatus", nStatus);//房间状态 0 可加入 1关闭 2人满 3 游戏已开始
                List<Map<String, Object>> playerList = new ArrayList<>();

                Map<String, Object> infoMap;
                for (Player player : playerMap.values()) {
                    infoMap = new HashMap<>();
                    infoMap.put("vcUnionid", player.getIdentity()); // 玩家unionid
                    infoMap.put("vcWeixinName", URLEncoder.encode(player.getRawName(), "UTF-8")); // 玩家昵称  需要url编码一次
                    infoMap.put("vcHeadImgUrl", player.getHeadimgurl()); //  玩家头像
                    playerList.add(infoMap);
                }
                strContext.put("playerList", playerList);//
                result.put("strSign", strSign);
                result.put("strContext", JacksonUtil.writeValueAsString(strContext));
                String res = HttpUtil.getUrlReturnValue(addr, "UTF-8", "POST", result);
                String nResult = "fail";
                String vcResult = "fail";
                if (res!=null) {
                    Map resMap = JacksonUtil.readValue(res, Map.class);
                    nResult = (String) resMap.get("nResult");
                    vcResult = (String) resMap.get("vcResult");
                }
                LogUtil.msg("sendStatus-->result:" + result + "nResult" + nResult + ",vcResult:" + vcResult+(res==null?(",tableId:"+table.getId()):""));
            } catch (Exception e) {
                LogUtil.e("sendRoomStatus err-->", e);
            }
        }else{
            try {
                String addr = "http://api.kaiyiju.com.cn/Interface.asmx/RoomStatusInfo";
                Map<String, String> result = new HashMap<>();
                Map<String, Object> strContext = new HashMap<>();
                String strSign = "";
                // 调用方式为post
                // 格式为json
                String id;
                String assisCreateNo;
                String assisGroupNo;
                Map<Long, Player> playerMap;
                if (table != null) {
                    id = String.valueOf(table.getId());
                    assisCreateNo = String.valueOf(table.getAssisCreateNo());
                    assisGroupNo = String.valueOf(table.getAssisGroupNo());
                    playerMap = table.getPlayerMap();
                } else {
                    id = String.valueOf(daikaiTable.getTableId());
                    assisCreateNo = String.valueOf(daikaiTable.getAssisCreateNo());
                    assisGroupNo = String.valueOf(daikaiTable.getAssisGroupNo());
                    playerMap = new HashMap<>();
                }
                strContext.put("vcMerchantNo", ResourcesConfigsUtil.loadServerPropertyValue("storeNo"));//商家编号
                strContext.put("vcRoomNo", id);//房间号
                strContext.put("vcSerialNo", assisCreateNo);//开房编号
                strContext.put("vcChatRoomSerialNo", assisGroupNo);//群编号
                strContext.put("nStatus", nStatus);//房间状态 0 可加入 1关闭 2人满 3 游戏已开始
                List<Map<String, Object>> playerList = new ArrayList<>();
                Map<String, Object> infoMap;
                for (Player player : playerMap.values()) {
                    infoMap = new HashMap<>();
                    infoMap.put("vcUnionid", player.getIdentity()); // 玩家unionid
                    infoMap.put("vcWeixinName", URLEncoder.encode(player.getRawName(), "UTF-8")); // 玩家昵称  需要url编码一次
                    infoMap.put("vcHeadImgUrl", player.getHeadimgurl()); //  玩家头像
                    playerList.add(infoMap);
                }
                strContext.put("playerList", playerList);//
                result.put("strSign", strSign);
                result.put("strContext", JacksonUtil.writeValueAsString(strContext));
                String res = HttpUtil.getUrlReturnValue(addr, "UTF-8", "POST", result);
//            HttpUtils req = new HttpUtils(addr);
//            String res = req.post(result);
                Map resMap = JacksonUtil.readValue(res, Map.class);
                String nResult = (String) resMap.get("nResult");
                String vcResult = (String) resMap.get("vcResult");
                LogUtil.msg("sendStatus-->result:" + result + "nResult" + nResult + ",vcResult:" + vcResult);
            } catch (Exception e) {
                LogUtil.e("sendRoomStatus err-->", e);
            }
        }

    }

    /**
     * 当游戏结束时回传游戏结果
     */
    public static void sendRoomResult(BaseTable table) {
        if (GameUtil.isPlayYjGame()){
            try {
                // 当游戏结束时回传游戏结果
                String addr = "http://api.kaiyiju.com.cn/Interface.asmx/RoomResult";
                Map<String, String> result = new HashMap<>();
                Map<String, Object> strContext = new HashMap<>();
                String strSign = "";
                strContext.put("vcMerchantNo", ResourcesConfigsUtil.loadServerPropertyValue("storeNo")); //商家编号+
                strContext.put("vcSerialNo", table.getAssisCreateNo()); //开房编号
                strContext.put("vcLogNo", String.valueOf(table.getId() + TimeUtil.currentTimeMillis())); //商家这次游戏的唯一ID
                strContext.put("vcChatRoomSerialNo", table.getAssisGroupNo()); //群编号
                strContext.put("vcRoomNo", "" + table.getId()); //房间号
                strContext.put("vcGameType", "" + table.getPlayType()); //游戏类型
                strContext.put("vcRoomType", "0"); //游戏子类型
                strContext.put("vcCardNum", ""); //房卡数量或者金币数量
                strContext.put("vcPlayerCount", "" + table.getPlayerCount()); //玩家数量
                strContext.put("vcIsAARoom", "" + (table.getPayType() == 1 ? 1 : 0)); //是否AA开房  0 不是  1 是的
                List<Map<String, String>> playerList = new ArrayList<>();
                Map<Long, Player> playerMap = table.getPlayerMap();
                Map<String, String> infoMap;
                for (Player player : playerMap.values()) {
                    infoMap = new HashMap<>();
                    infoMap.put("vcUnionid", player.getIdentity()); // 玩家unionid
                    infoMap.put("vcWeixinName", URLEncoder.encode(player.getRawName(), "UTF-8")); // 玩家昵称  需要url编码一次
                    infoMap.put("vcHeadImgUrl", player.getHeadimgurl()); //  玩家头像
                    int winCount = player.getWinCount();
                    int lostCount = player.getLostCount();
                    infoMap.put("nWinCount", "" + winCount); //赢次数
                    infoMap.put("nlostCount", "" + lostCount);//输次数
                    infoMap.put("nMaxPoint", "" + player.getMaxPoint());//这局最大赢的分数
                    infoMap.put("nTotalPoint", "" + player.getTotalPoint());//总积分
                    playerList.add(infoMap);
                }
                strContext.put("playerList", playerList); //
                String unionId;
                Player creator = PlayerManager.getInstance().getPlayer(table.getCreatorId());
                if (creator == null) {
                    RegInfo creator1 = UserDao.getInstance().selectUserByUserId(table.getCreatorId());
                    unionId = creator1.getIdentity();
                } else {
                    unionId = creator.getIdentity();
                }
                strContext.put("vcRoomMasterId", unionId); //房主unionid
                result.put("strSign", strSign);
                result.put("strContext", JacksonUtil.writeValueAsString(strContext));
                String res = HttpUtil.getUrlReturnValue(addr, "UTF-8", "POST", result);
                String nResult = "fail";
                String vcResult = "fail";
                if (res!=null) {
                    Map resMap = JacksonUtil.readValue(res, Map.class);
                    nResult = (String) resMap.get("nResult");
                    vcResult = (String) resMap.get("vcResult");
                }
                LogUtil.msg("sendRoomResult-->result:" + result + "nResult" + nResult + ",vcResult:" + vcResult+(res==null?(",tableId:"+table.getId()):""));
            } catch (Exception e) {
                LogUtil.e("sendRoomResult err-->", e);
            }
        }else{
            try {
                // 当游戏结束时回传游戏结果
                String addr = "http://api.kaiyiju.com.cn/Interface.asmx/RoomResult";
                Map<String, String> result = new HashMap<>();
                Map<String, Object> strContext = new HashMap<>();
                String strSign = "";
                strContext.put("vcMerchantNo", ResourcesConfigsUtil.loadServerPropertyValue("storeNo")); //商家编号+
                strContext.put("vcSerialNo", table.getAssisCreateNo()); //开房编号
                strContext.put("vcLogNo", String.valueOf(table.getId() + TimeUtil.currentTimeMillis())); //商家这次游戏的唯一ID
                strContext.put("vcChatRoomSerialNo", table.getAssisGroupNo()); //群编号
                strContext.put("vcRoomNo", "" + table.getId()); //房间号
                strContext.put("vcGameType", "" + table.getPlayType()); //游戏类型
                strContext.put("vcRoomType", "0"); //游戏子类型
                strContext.put("vcCardNum", ""); //房卡数量或者金币数量
                strContext.put("vcPlayerCount", "" + table.getPlayerCount()); //玩家数量
                strContext.put("vcIsAARoom", "" + (table.getPayType() == 1 ? 1 : 0)); //是否AA开房  0 不是  1 是的
                List<Map<String, String>> playerList = new ArrayList<>();
                Map<Long, Player> playerMap = table.getPlayerMap();
                Map<String, String> infoMap;
                for (Player player : playerMap.values()) {
                    infoMap = new HashMap<>();
                    infoMap.put("vcUnionid", player.getIdentity()); // 玩家unionid
                    infoMap.put("vcWeixinName", URLEncoder.encode(player.getRawName(), "UTF-8")); // 玩家昵称  需要url编码一次
                    infoMap.put("vcHeadImgUrl", player.getHeadimgurl()); //  玩家头像
                    int winCount = 0;
                    int lostCount = 0;
                    if (player.getClass().getSimpleName().equals("MajiangPlayer")) {
                        winCount =  player.getWinCount();
                        lostCount = player.getLostCount();
                    }
                    infoMap.put("nWinCount", "" + winCount); //赢次数
                    infoMap.put("nlostCount", "" + lostCount);//输次数
                    infoMap.put("nMaxPoint", "" + player.getMaxPoint());//这局最大赢的分数
                    infoMap.put("nTotalPoint", "" + player.getTotalPoint());//总积分
                    playerList.add(infoMap);
                }
                strContext.put("playerList", playerList); //
                String unionId;
                Player creator = PlayerManager.getInstance().getPlayer(table.getCreatorId());
                if (creator == null) {
                    RegInfo creator1 = UserDao.getInstance().selectUserByUserId(table.getCreatorId());
                    unionId = creator1.getIdentity();
                } else {
                    unionId = creator.getIdentity();
                }
                strContext.put("vcRoomMasterId", unionId); //房主unionid
                result.put("strSign", strSign);
                result.put("strContext", JacksonUtil.writeValueAsString(strContext));
                String res = HttpUtil.getUrlReturnValue(addr, "UTF-8", "POST", result);
                Map resMap = JacksonUtil.readValue(res, Map.class);
                String nResult = (String) resMap.get("nResult");
                String vcResult = (String) resMap.get("vcResult");
                LogUtil.msg("sendRoomResult-->result:" + result + "nResult" + nResult + ",vcResult:" + vcResult);
            } catch (Exception e) {
                LogUtil.e("sendRoomStatus err-->", e);
            }
        }

    }

    /**
     * 检测是否是群成员
     */
    public static boolean chatRoomUserCheck(BaseTable table, Player player) {
        if (GameUtil.isPlayYjGame()){
            try {
                //如果限制只有群内成员才可以加入房间的话,可以在加入房间的时候调用这个接口检测该用户是否为群成员
                Map<String, String> result = new HashMap<>();
                String addr = "http://api.kaiyiju.com.cn/Interface.asmx/ChatRoomUserCheck";
                //传入参数
                Map<String, Object> strContext = new HashMap<>();
                String strSign = "";
                strContext.put("vcChatRoomSerialNo", table.getAssisGroupNo());//群编号
                strContext.put("vcUnionId", player.getIdentity());//用户unionid
                strContext.put("vcSerialNo", table.getAssisCreateNo());//开房编号
                String rawName = player.getRawName();
                strContext.put("vcName", URLEncoder.encode(rawName, "UTF-8"));//用户昵称  需要url编码一次
                strContext.put("vcRoomId", "" + table.getId());//房间号
                strContext.put("vcBase64Name", URLEncoder.encode((Base64Util.encode(rawName.getBytes())), "UTF-8"));//用户base64的昵称 需要url编码一次;
                result.put("strContext", JacksonUtil.writeValueAsString(strContext));
                result.put("strSign", strSign);
                String res = HttpUtil.getUrlReturnValue(addr, "UTF-8", "POST", result);
                Map resMap = JacksonUtil.readValue(res, Map.class);
                String nResult = (String) resMap.get("nResult");
                String vcResult = (String) resMap.get("vcResult");
                LogUtil.msg("chatRoomUserCheck:result:" + result + "nResult" + nResult + ",vcResult:" + vcResult+",userId:"+player.getUserId());
                return "1".equals(nResult);
            } catch (Exception e) {
                LogUtil.e("chatRoomUserCheck err-->", e);
            }
            return true;
        }else{
            try {
                //如果限制只有群内成员才可以加入房间的话,可以在加入房间的时候调用这个接口检测该用户是否为群成员
                Map<String, String> result = new HashMap<>();
                String addr = "http://api.kaiyiju.com.cn/Interface.asmx/ChatRoomUserCheck";
                //传入参数
                Map<String, Object> strContext = new HashMap<>();
                String strSign = "";
                strContext.put("vcChatRoomSerialNo", table.getAssisGroupNo());//群编号
                strContext.put("vcUnionId", player.getIdentity());//用户unionid
                strContext.put("vcSerialNo", table.getAssisCreateNo());//开房编号
                String rawName = player.getRawName();
                strContext.put("vcName", URLEncoder.encode(rawName, "UTF-8"));//用户昵称  需要url编码一次
                strContext.put("vcRoomId", "" + table.getId());//房间号
                strContext.put("vcBase64Name", URLEncoder.encode((Base64Util.encode(rawName.getBytes())), "UTF-8"));//用户base64的昵称 需要url编码一次;
                result.put("strContext", JacksonUtil.writeValueAsString(strContext));
                result.put("strSign", strSign);
                String res = HttpUtil.getUrlReturnValue(addr, "UTF-8", "POST", result);
                Map resMap = JacksonUtil.readValue(res, Map.class);
                String nResult = (String) resMap.get("nResult");
                String vcResult = (String) resMap.get("vcResult");
                LogUtil.msg("chatRoomUserCheck:result:" + result + "nResult" + nResult + ",vcResult:" + vcResult);
                return "1".equals(nResult);
            } catch (Exception e) {
                LogUtil.e("chatRoomUserCheck err-->", e);
            }
            return true;
        }

    }

    private boolean checkParams(HttpServletRequest req, List<Integer> params) throws UnsupportedEncodingException {
        if (GameUtil.isPlayYjGame()){
            String vcGameType = req.getParameter("vcGameType");
            if (StringUtil.isBlank(vcGameType) || !NumberUtils.isDigits(vcGameType)) {
                LogUtil.e("params err-->paramName:vcGameType" + ",val:" + vcGameType);
                return false;
            }
            String playType = req.getParameter("play");
            if (StringUtil.isBlank(playType)) {
                LogUtil.e("paramsNameStr is not exist-->play"+playType);
                return false;
            }
            List<String> paramsNameList;
            String paramsNameStr = ResourcesConfigsUtil.loadServerPropertyValue("paramsName_" + playType);
            if (StringUtil.isBlank(paramsNameStr)) {
                paramsNameStr = ResourcesConfigsUtil.loadServerPropertyValue("paramsName_" + Integer.parseInt(playType) / 10);
                if (StringUtil.isBlank(paramsNameStr)) {
                    LogUtil.e("paramsName_ is not exist-->play"+playType);
                    return false;
                }
            }
            paramsNameList = Arrays.asList(paramsNameStr.split(","));
            List<String> tempList = new ArrayList<>();
            for (String key : paramsNameList) {
                String val = req.getParameter(key);
                if (!"null".equals(key) && (StringUtil.isBlank(val) || !NumberUtils.isDigits(val))) {
                    if ("kaqiao".equals(key)) {
                        tempList.add("0");
                        continue;
                    }
                    LogUtil.e("params err-->paramName:" + key + ",val:" + val);
                    return false;
                } else {
                    if (StringUtil.isBlank(val)) {
                        tempList.add("0");
                    } else {
                        tempList.add(val);
                    }
                }
            }
            if (tempList.isEmpty()) {
                LogUtil.e("params err-->tempList is empty");
                return false;
            }
            for (String val : tempList) {
                params.add(Integer.parseInt(val));
            }
            if (Integer.parseInt(playType) == GameUtil.play_type_yuanjiang) {
                int yizhiqiao = StringUtil.getIntValue(params, 5, 0);// 一字撬
                int kaqiao = StringUtil.getIntValue(params, 6, 0);// 卡撬
                if (yizhiqiao == 0 && kaqiao == 1) {
                    LogUtil.e("daikaiTable err-->yizhiqiao:"+yizhiqiao+",kaqiao:"+kaqiao);
                    return false;
                }
            }
            return true;
        }else{
            String playType = req.getParameter("vcGameType");
            if (StringUtil.isBlank(playType) || !NumberUtils.isDigits(playType)) {
                LogUtil.e("params err-->paramName:vcGameType" + ",val:" + playType);
                return false;
            }
            List<String> paramsNameList;
            String paramsNameStr = ResourcesConfigsUtil.loadServerPropertyValue("paramsName_" + Integer.parseInt(playType));
            if (StringUtil.isBlank(paramsNameStr)) {
                paramsNameStr = ResourcesConfigsUtil.loadServerPropertyValue("paramsName_" + Integer.parseInt(playType) / 10);
                if (StringUtil.isBlank(paramsNameStr)) {
                    LogUtil.e("paramsNameStr is not exist");
                    return false;
                }
            }
            paramsNameList = Arrays.asList(paramsNameStr.split(","));
            List<String> tempList = new ArrayList<>();
            for (String key : paramsNameList) {
                String val = req.getParameter(key);
                if (!"null".equals(key) && (StringUtil.isBlank(val) || !NumberUtils.isDigits(val))) {
                    LogUtil.e("params err-->paramName:" + key + ",val:" + val);
                    return false;
                } else {
                    if (StringUtil.isBlank(val)) {
                        tempList.add("0");
                    } else {
                        tempList.add(val);
                    }
                }
            }
            if (tempList.isEmpty()) {
                LogUtil.e("params err-->tempList is empty");
                return false;
            }
            for (String val : tempList) {
                params.add(Integer.parseInt(val));
            }
            return true;
        }
    }

    // 代开牌桌
    private Long daikaiTable(RegInfo regInfo, String createNo, String groupNo, List<Integer> params, String strParamsStr) {
        if (GameUtil.isPlayYjGame()){
            int playType = StringUtil.getIntValue(params, 1, 0);
            int bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
            int playerCount = StringUtil.getIntValue(params, 7, 0);// 人数
            BaseTable table = TableManager.getInstance().getInstanceTable(playType);

            int payType = 0;
            if (playType == GameUtil.play_type_yuanjiang) {
                payType = StringUtil.getIntValue(params, 10, 2);
            } else if (playType == GameUtil.play_type_pdk_yj16 || playType == GameUtil.play_type_pdk_yj15) {
                payType = StringUtil.getIntValue(params, 9, 2);
            } else if (playType == GameUtil.play_type_yjghz) {
                payType = StringUtil.getIntValue(params, 2, 2);
                playerCount = 3;
            }
            if (payType <=0) {
                LogUtil.e("daikaiTable err-->payType:"+payType);
                return -4L;
            }

            int needCard = PayConfigUtil.get(playType, bureauCount, table.calcPlayerCount(playerCount), payType==1?0:1);
            if (payType == 1) {
                needCard = 0;
            }

            if (needCard < 0 || regInfo.getFreeCards() + regInfo.getCards() < needCard) {
                return -1L;
            }

            long userId = regInfo.getUserId();
            String createPara = StringUtil.implode(params, ",");

            long tableId;
            boolean existFlag;
            Server server = ServerManager.loadServer(playType, 1);
            int serverId = server != null ? server.getId() : GameServerConfig.SERVER_ID;
            do {
                tableId = TableManager.getInstance().generateId(userId, playType, 2, serverId);
                existFlag = TableDao.getInstance().checkTableIdExist(tableId);
            } while (existFlag);

            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableId", tableId);
            paramMap.put("daikaiId", userId);
            paramMap.put("serverId", serverId);
            paramMap.put("playType", playType);
            paramMap.put("needCard", needCard);
            paramMap.put("state", 0);
            paramMap.put("createFlag", 0);
            paramMap.put("createPara", createPara);
            paramMap.put("createStrPara", strParamsStr);
            paramMap.put("createTime", null);
            paramMap.put("daikaiTime", new Date());
            paramMap.put("returnFlag", 0);
            paramMap.put("playerInfo", "");
            paramMap.put("extend", null);
            paramMap.put("assisCreateNo", createNo);
            paramMap.put("assisGroupNo", groupNo);

            try {
                TableDao.getInstance().daikaiTable(paramMap);
            } catch (SQLException e) {
                LogUtil.e("daikaiTable err:", e);
                return -2L;
            }

            if (needCard != 0) {
                boolean isWrite = true;
                Player player = PlayerManager.getInstance().getPlayer(userId);
                if (player == null) {
                    player = new CommonPlayer();
                    player.loadFromDB(regInfo);
                    isWrite = false;
                }
                player.changeCards(0, -needCard, isWrite, playType,false, CardSourceType.daikaiTable_FZ);
            }
            return tableId;
        }else{
            int playType = StringUtil.getIntValue(params, 1, 0);
            int allowGroupMember = 0;
            if (GameUtil.isPlayDn(playType) && !SharedConstants.isKingOfBull()) {
                allowGroupMember = StringUtil.getIntValue(params, 18, 0);
            }
            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(regInfo.getUserId(),null);
            if (allowGroupMember == 1 && groupUser == null) {
                return -3L;
            }
            int bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
            if (GameUtil.isPlayBopi(playType)) {
                bureauCount = 50;
            }
            int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
            if( GameUtil.isPlayDzbp(playType)){
            	 bureauCount = 50;
            	 playerCount = 2;
            }
            BaseTable table = TableManager.getInstance().getInstanceTable(playType);

            int payType;
            if (GameUtil.isPlayTenthirty(playType)) {
                payType = StringUtil.getIntValue(params, 9, 2);
            } else {
                payType = StringUtil.getIntValue(params, 10, 2);
            }
            int needCard;
            if (payType == 1 && GameUtil.isPlayGSMajiang(playType)) {
                needCard = 0;
            } else {
                needCard = PayConfigUtil.get(playType, bureauCount, table.calcPlayerCount(playerCount), 1);
            }

            if (needCard < 0 || regInfo.getFreeCards() + regInfo.getCards() < needCard) {
                return -1L;
            }

            long userId = regInfo.getUserId();
            String createPara = StringUtil.implode(params, ",");

            long tableId;
            boolean existFlag;
            Server server = ServerManager.loadServer(playType, 1);
            int serverId = server != null ? server.getId() : GameServerConfig.SERVER_ID;
            do {
                tableId = TableManager.getInstance().generateId(userId, playType, 2, serverId);
                existFlag = TableDao.getInstance().checkTableIdExist(tableId);
            } while (existFlag);

            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableId", tableId);
            paramMap.put("daikaiId", userId);
            paramMap.put("serverId", serverId);
            paramMap.put("playType", playType);
            paramMap.put("needCard", needCard);
            paramMap.put("state", 0);
            paramMap.put("createFlag", 0);
            paramMap.put("createPara", createPara);
            paramMap.put("createStrPara", strParamsStr);
            paramMap.put("createTime", null);
            paramMap.put("daikaiTime", new Date());
            paramMap.put("returnFlag", 0);
            paramMap.put("playerInfo", "");
            paramMap.put("extend", null);
            paramMap.put("assisCreateNo", createNo);
            paramMap.put("assisGroupNo", groupNo);

            try {
                TableDao.getInstance().daikaiTable(paramMap);
            } catch (SQLException e) {
                LogUtil.e("daikaiTable err:", e);
                return -2L;
            }

            if (needCard != 0) {
                boolean isWrite = true;
                Player player = PlayerManager.getInstance().getPlayer(userId);
                if (player == null) {
                    player = new CommonPlayer();
                    player.loadFromDB(regInfo);
                    isWrite = false;
                }
                player.changeCards(0, -needCard, isWrite, playType, CardSourceType.daikaiTable_FZ);
            }
            return tableId;
        }
    }

    private void changeCards(int freeCards, int cards, int playType, RegInfo regInfo) {
        if (cards < 0) {
            long temp1 = 0;//free
            long temp2 = 0;//common
            long freeCards1 = regInfo.getFreeCards();
            long cards1 = regInfo.getCards();
            if (cards < 0) {
                // temp等于绑定房卡 + cards
                long temp = freeCards1 + cards;
                if (temp >= 0) {
                    // 房卡足够
                    freeCards1 = temp;
                    temp2 = 0;
                    temp1 = -cards;
                } else {
                    // 房卡不足，先用完绑定房卡，再用普通房卡
                    freeCards1 = 0;
                    temp2 = -temp;
                    temp1 = (-cards) - temp2;
                }
                cards1 -= temp2;
            } else {
                cards1 += cards;
            }

            freeCards1 += freeCards;

            Map<String, Object> log = new HashMap<>();
            log.put("freeCards", -temp1);
            log.put("cards", -temp2);
            log.put("playType", playType);
            log.put("isRecord", 0);

            LogUtil.msgLog.info("statistics:userId=" + regInfo.getUserId() + ",tableId=" + 0 + ",isRecord=" + false + " ,playType=" + playType + ",freeCards=" + freeCards + ",cards=" + cards + ",rest freeCards=" + freeCards1 + ",rest cards=" + cards1);

            Player player = new CommonPlayer();
            player.loadFromDB(regInfo);
            UdpLogger.getInstance().sendActionLog(player, LogConstants.reason_consumecards, JacksonUtil.writeValueAsString(log));

            if (temp2 != 0 || temp1 != 0) {
                UserDao.getInstance().updateUserCards(regInfo.getUserId(), regInfo.getFlatId(), regInfo.getPf(), -temp2, -temp1);
            }
        }
    }
}
