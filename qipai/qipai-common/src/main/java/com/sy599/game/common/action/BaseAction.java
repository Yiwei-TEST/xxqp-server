
package com.sy599.game.common.action;

import com.sy599.game.character.Player;
import com.sy599.game.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseAction {
    protected static final String code = "code";
    protected Map<String, Object> reqParamMap;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String result;
    protected Player player;

    public BaseAction() {
    }

    public abstract void execute() throws Exception;

    public String getString(String name) {
        if (this.isHttpReq()) {
            return this.getHttpString(name);
        } else {
            return this.reqParamMap.containsKey(name) ? this.reqParamMap.get(name).toString() : null;
        }
    }

    private String getHttpString(String name) {
        String value = this.request.getParameter(name);
        return name.equals("name") ? URLDecoder.decode(value) : value;
    }

    public boolean hasParam(String name) {
        return !StringUtils.isBlank(this.getString(name));
    }

    public int getInt(String name) {
        return Integer.parseInt(this.getString(name));
    }

    public int getInt(String name, int def) {
        return this.hasParam(name) ? this.getInt(name) : def;
    }

    public long getLong(String name, long def) {
        return this.hasParam(name) ? this.getLong(name) : def;
    }

    public long getLong(String name) {
        return Long.parseLong(this.getString(name));
    }

    public long getUserId() {
        return Long.parseLong(this.getString("userId"));
    }

    public void writeMessage(String msg) throws Exception {
        this.response.getWriter().write(msg);
        response.getWriter().flush();
        response.getWriter().close();
    }

    public boolean isHttpReq() {
        return this.response != null;
    }

    public void writeErrMsg(int code, Object extra) throws Exception {
        Map<String, Object> temp = new HashMap(4);
        temp.put("code", code);
        if (extra != null) {
            temp.put("extra", extra);
        }

        this.writeMessage(JacksonUtil.writeValueAsString(temp));
    }

    public HttpServletRequest getRequest() {
        return this.request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return this.response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setReqParamMap(Map<String, Object> reqParamMap) {
        this.reqParamMap = reqParamMap;
    }

    public Map<String, Object> getReqParamMap() {
        return this.reqParamMap;
    }
}
