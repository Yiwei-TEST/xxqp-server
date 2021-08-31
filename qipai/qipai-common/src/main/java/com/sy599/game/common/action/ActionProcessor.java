
package com.sy599.game.common.action;

import com.sy599.game.activity.ActivityAction;
import com.sy599.game.character.Player;
import com.sy599.game.gameSite.GameSiteAction;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.pay.PayAction;
import com.sy599.game.shutdown.ShutDownAction;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.webservice.WebserviceAction;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActionProcessor {
    private static ActionProcessor processor = new ActionProcessor();
    private static String timeFormat = "action totalTime:%d,userId:%s,type:%s,funcType:%s";
    private static Map<String, Class<? extends BaseAction>> actionMap = new HashMap();

    static {
        actionMap.put("1", WebserviceAction.class);
        actionMap.put("2", PayAction.class);
        actionMap.put("3", GameSiteAction.class);
        actionMap.put("4", ActivityAction.class);
        actionMap.put("15", ShutDownAction.class);
    }

    private ActionProcessor() {
    }

    public static ActionProcessor getInstance() {
        return processor;
    }

    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String type = request.getParameter("type");
            String userId = request.getParameter("userId");
            String sessionId = request.getParameter("sessionId");
            String funcType = request.getParameter("funcType");
            if (StringUtils.isBlank(funcType)) {
                funcType = request.getParameter("code");
            }

            if (!StringUtils.isBlank(request.getParameter("battleType"))) {
                funcType = request.getParameter("battleType");
            }

            if (type != null && !StringUtils.isBlank(type)) {
                long beginTime = TimeUtil.currentTimeMillis();
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setContentType("text/html;charset=UTF-8");
                Player player = null;
                boolean isLock = false;

                try {
                    HashMap temp;
                    try {
                        BaseAction action = ObjectUtil.newInstance(actionMap.get(type));
                        action.setRequest(request);
                        action.setResponse(response);
                        if (userId != null && !StringUtils.isBlank(userId)) {
                            player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                            if (player == null || !sessionId.equals(player.getSessionId())) {
                                temp = new HashMap(4);
                                temp.put("code", -2);
                                response.getWriter().write(JacksonUtil.writeValueAsString(temp));
                                response.getWriter().flush();
                                response.getWriter().close();
                                LogUtil.msg("r2,no player," + userId + "-" + type + "-" + funcType);
                                return;
                            }

                            action.setPlayer(player);
                            if (!player.tryLock(5L, TimeUnit.SECONDS)) {
                                temp = new HashMap(4);
                                temp.put("code", -1);
                                response.getWriter().write(JacksonUtil.writeValueAsString(temp));
                                response.getWriter().flush();
                                response.getWriter().close();
                                LogUtil.e("request end,tryLock fail,userId:" + userId + ",type:" + type + ",funcType:" + funcType);
                                return;
                            }

                            isLock = true;
                            action.execute();
                            player.setActionTime(TimeUtil.currentTimeMillis());
                        } else {
                            action.execute();
                            userId = request.getParameter("flatId");
                        }

                        long totalTime = TimeUtil.currentTimeMillis() - beginTime;
                        LogUtil.msg("rq," + userId + "-" + type + "-" + funcType);
                        if (totalTime > 50L) {
                            LogUtil.monitor_i(String.format(timeFormat, totalTime, userId, type, funcType));
                            return;
                        }
                    } catch (Exception var17) {
                        LogUtil.e("request end,exception,userId:" + userId + ",type:" + type + ",funcType:" + funcType, var17);
                        temp = new HashMap(4);
                        temp.put("code", -1);
                        response.getWriter().write(JacksonUtil.writeValueAsString(temp));
                        response.getWriter().flush();
                        response.getWriter().close();
                    }

                } finally {
                    if (isLock) {
                        player.unLock();
                    }

                }
            } else {
                LogUtil.msg("rq2," + userId + "-" + type + "-" + funcType);
            }
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
        }finally {
            response.getWriter().flush();
            response.getWriter().close();
        }
    }
}
