package com.sy599.game.manager;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MD5Util;
import com.sy599.game.GameServerConfig;
import com.sy599.game.db.bean.SystemCommonInfo;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

public class GotyeChatManager {
    private String gcUrl;
    private String gcUrl2;

    private static final GotyeChatManager _inst = new GotyeChatManager();

    public static GotyeChatManager getInstance() {
        return _inst;
    }

    public void loadFromDB() {
        SystemCommonInfo gcUrl2Info = SystemCommonInfoDao.getInstance().selectLogin("gcUrl2");
        if (gcUrl2Info != null) {
            if (StringUtils.isNotBlank(gcUrl2Info.getContent())) {
                gcUrl2 = gcUrl2Info.getContent();
            }
        }

        SystemCommonInfo gcUrlInfo = SystemCommonInfoDao.getInstance().selectLogin("gcUrl");
        if (gcUrlInfo != null) {
            if (StringUtils.isNotBlank(gcUrlInfo.getContent())) {
                gcUrl = gcUrlInfo.getContent();
            }
        }
    }

    /**
     * 获取语音房间id
     *
     * @return
     */
    public long loadGotyeRoomId(long tableId, int playType, String os, String vc) {
        if (!GameUtil.isPlayAhGame() || GameServerConfig.isTest() || "0".equals(ResourcesConfigsUtil.loadServerPropertyValue("load_gotye","1"))) {
            return 0;
        }

        if (StringUtils.isNotBlank(gcUrl) || StringUtils.isNotBlank(gcUrl2)) {
            try {
                Map<String, String> param = new HashMap<>();
                param.put("type", "1");
                param.put("funcType", "1");
                param.put("tableId", String.valueOf(tableId));
                param.put("playType", String.valueOf(playType));
                param.put("serverId", String.valueOf(GameServerConfig.SERVER_ID));

                String syTime = String.valueOf(System.currentTimeMillis());
                String md5 = MD5Util.getMD5String(syTime + "HJIFDHUSAFDDSA787d");
                param.put("sytime", syTime);
                param.put("sysign", md5);

                String url = gcUrl;
                if (StringUtils.isNotBlank(gcUrl) && StringUtils.isNotBlank(gcUrl2) && StringUtils.isNotBlank(os) && StringUtils.isNotBlank(vc)) {
                    if (os.equalsIgnoreCase("Android")) {
                        if (NumberUtils.toInt(vc, 0) >= 214) {
                            url = gcUrl2;
                        }
                    } else if (os.equalsIgnoreCase("iOS")) {
                        if (NumberUtils.toInt(vc.replace(".", ""), 0) >= 105) {
                            url = gcUrl2;
                        }
                    }
                } else if (StringUtils.isNotBlank(gcUrl)) {
                } else if (StringUtils.isNotBlank(gcUrl2)) {
                    url = gcUrl2;
                }

                String result;
                if (StringUtils.isNotBlank(url)) {
                    result = HttpUtil.getUrlReturnValue(url, "UTF-8", "POST", param, 1);

                    LogUtil.msgLog.info("loadGotyeRoomId:url={},params={},result={}", url, param, result);
                } else {
                    result = null;
                }

                if (StringUtils.isNotBlank(result)) {
                    JSONObject wrapper = JSONObject.parseObject(result);
                    if ("0".equals(wrapper.getString("code"))) {
                        long gcRoomId = NumberUtils.toLong(wrapper.getString("gcRoomId"), 0);
                        return gcRoomId;
                    }
                }

            } catch (Exception e) {
                LogUtil.e("loadGotyeRoomId fail!" + e.getMessage(), e);
            }
        }

        return 0;
    }

    /**
     * 删除语音房间id
     *
     * @param roomId
     */
    public String deleteGotyeRoomId(long roomId) {

        if (roomId <= 0 || !GameUtil.isPlayAhGame() || GameServerConfig.isTest()) {
            return null;
        } else {
            try {
                Map<String, String> param = new HashMap<>();
                param.put("type", "1");
                param.put("funcType", "2");
                param.put("roomId", String.valueOf(roomId));

                String syTime = String.valueOf(System.currentTimeMillis());
                String md5 = MD5Util.getMD5String(syTime + "HJIFDHUSAFDDSA787d");
                param.put("sytime", syTime);
                param.put("sysign", md5);

                if (StringUtils.isNotBlank(gcUrl)) {
                    String result = HttpUtil.getUrlReturnValue(gcUrl, "UTF-8", "POST", param, 1);
                    LogUtil.msgLog.info("delGCRoomId result:roomId -->{},url={},ret={}", roomId, gcUrl, result);
                }
                if (StringUtils.isNotBlank(gcUrl2)) {
                    String result = HttpUtil.getUrlReturnValue(gcUrl2, "UTF-8", "POST", param, 1);
                    LogUtil.msgLog.info("delGCRoomId result:roomId -->{},url={},ret={}", roomId, gcUrl2, result);
                }

                return "success";
            } catch (Exception e) {
                LogUtil.e("delGCRoomId 失败!" + roomId);
                return "fail";
            }
        }
    }
}
