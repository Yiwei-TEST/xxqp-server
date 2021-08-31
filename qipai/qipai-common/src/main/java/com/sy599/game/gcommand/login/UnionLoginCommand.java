package com.sy599.game.gcommand.login;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.MD5Util;
import com.sy.mainland.util.MessageBuilder;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnionLoginCommand extends BaseCommand{

    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

    }

    public int login(MessageUnit message, MyWebSocket socket) throws Exception {
        ComMsg.ComReq comReq = ComMsg.ComReq.parseFrom(message.getContent());
        List<Integer> paramIntsList = comReq.getParamsList();
        List<String> paramsList = comReq.getStrParamsList();

        String paramStr = (paramsList==null||paramsList.size()==0)?"":paramsList.get(0);
        Integer playType = (paramIntsList==null||paramIntsList.size()==0)?0:paramIntsList.get(0);

        String ip = NettyUtil.userIpMap.get(socket.getCtx().channel());
        if (StringUtils.isBlank(ip)) {
            ip = NettyUtil.getRemoteAddr(socket.getCtx());
        }
        String channelId = socket.getCtx().channel().id().asShortText();

        if (StringUtils.isBlank(paramStr)){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","登陆错误，请稍后再试").toString());

            StringBuilder sb = new StringBuilder("UnionLoginCommand|error|paramStr");
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            LogUtil.errorLog.error(sb.toString());

            return -1;
        }
        JSONObject jsonObjectLogin = JSONObject.parseObject(paramStr);

        LogUtil.monitorLog.info("UnionLoginCommand|login|start|params={}", jsonObjectLogin);

        String sign = String.valueOf(jsonObjectLogin.remove("sign"));
        if ("null".equals(sign)||sign.length()==0){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","签名信息错误，请稍后再试").toString());

            StringBuilder sb = new StringBuilder("UnionLoginCommand|error|signError");
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            LogUtil.errorLog.error(sb.toString());

            return -1;
        }
        List<String> paramNames =new ArrayList<>(jsonObjectLogin.keySet());
        Collections.sort(paramNames);

        StringBuilder stringBuilder = new StringBuilder(1024);
        for (String str : paramNames){
            stringBuilder.append("&").append(str).append("=").append(jsonObjectLogin.getString(str));
        }

        stringBuilder.append("&key=");
        String signKey;

        if (playType == null || playType.intValue() <= 0){
            signKey = LoginUtil.DEFAULT_KEY;
        }else{
            BaseTable table = TableManager.getInstance().getInstanceTable(playType);
            if (table == null){
                signKey = LoginUtil.DEFAULT_KEY;
            }else{
                signKey = table.loadSignKey();
            }
        }

        stringBuilder.append(signKey);
        if (!MD5Util.getMD5String(stringBuilder.toString()).equalsIgnoreCase(sign)){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","签名错误，请稍后再试").toString());

            StringBuilder sb = new StringBuilder("UnionLoginCommand|error|md5Error");
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            sb.append("|").append(sign);
            LogUtil.errorLog.error(sb.toString());

            return -1;
        }

        long startTime = System.currentTimeMillis();
        Map<String,Object> retMap = LoginUtil.login(socket.getCtx(),jsonObjectLogin);

        String retStr;
        if (retMap == null){
            retStr = null;
        }else{
            retMap.put("currentServer",GameServerConfig.SERVER_ID);
            retStr = JSONObject.toJSONString(retMap);
        }

        StringBuilder sb = new StringBuilder("UnionLoginCommand|login|end");
        sb.append("|").append(jsonObjectLogin);
        sb.append("|").append(channelId);
        sb.append("|").append(ip);
        sb.append("|").append(retStr);
        sb.append("|").append(System.currentTimeMillis() - startTime);
        LogUtil.monitorLog.info(sb.toString());

        if (retStr == null){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","登陆异常，请稍后再试").toString());
            return -1;
        }
        Object code = retMap.get("code");

        if ((code instanceof Number)&&((Number)code).intValue()==0){
            socket.send(WebSocketMsgType.union_login_success, retStr);
            return 1;
        }else{
            socket.send(WebSocketMsgType.union_login_fail, retStr);
            return 0;
        }
    }
}
