package com.sy599.game.gcommand.login.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.base.BaseTable;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.util.HttpUtils;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MD5Util;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class BjdUtil {

    public static final String plat = "mjqz";
    public static final String sign_key = "0NUs3u0qpsfrB4k9";
    public static final int timeout_second = 3;

    /**
     * 获取用户预绑定的代理邀请码
     *
     * @return
     */
    public static int getPreBindAgency(RegInfo user) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account;
        String reg_type;
        if (StringUtils.isNotBlank(user.getIdentity())) {
            reg_type = "0";
            account = user.getIdentity();
        } else {
            reg_type = "1";
            account = user.getUserId() + "";
        }
        String sign = MD5Util.getMD5String(account + time);
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("reg_type", reg_type);
            map.put("sign", sign);
            String url_checkBind = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_checkBind", "http://bjdqp.firstmjq.club/agent/player/checkBind");
            String postRes = HttpUtil.getUrlReturnValue(url_checkBind, "UTF-8", "POST", map, timeout_second);
            LogUtil.msgLog.info("getPreBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return 0;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return 0;
            }
            if (obj.getIntValue("code") != 2) {
                // 未绑定
                return 0;
            }
            JSONObject data = obj.getJSONObject("data");
            if (data == null) {
                return 0;
            }
            String parentId = data.getString("parent_id");
            if (StringUtils.isBlank(parentId) || "null".equals(parentId)) {
                return 0;
            }
            return Integer.valueOf(parentId);
        } catch (Exception e) {
            LogUtil.errorLog.error("getPreBindAgency|error|" + account + "|" + time + "|" + sign, e);
        }
        return 0;
    }

    /**
     * 俱乐部房间：分享到闲聊
     */
    public static void share2XianLiaoGroup(BaseTable table) {
        try {
            if (!ResourcesConfigsUtil.isSwitchOn(ResourcesConfigsUtil.KEY_SWITCH_SHARE_TO_XIANLIAO_GROUP)) {
                return;
            }
            if (!table.isGroupRoom() || table.isCreditTable()) {
                return;
            }
            final String group_id = table.loadGroupId();
            final long tableId = table.getId();
            String msg_content = table.getTableMsgForXianLiao();
            String time = String.valueOf(System.currentTimeMillis());
            String sign = MD5Util.getMD5String(group_id + time + sign_key);
            final Map<String, String> map = new HashMap<>();
            map.put("group_id", group_id);
            map.put("msg_content", msg_content);
            map.put("time", time);
            map.put("sign", sign);
            String url_share2XianLiao = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_share2XianLiao", "http://bjdqp.firstmjq.club/agent/Xianliao/send/wx_plat/mjqz");
            TaskExecutor.SINGLE_EXECUTOR_SERVICE_BJD.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpUtils res = new HttpUtils(url_share2XianLiao);
                        String post = res.post(map);
                        LogUtil.msgLog.info("share2XianLiaoGroup|succ|" + group_id + "|" + tableId + post);
                    } catch (Exception e) {
                        LogUtil.e("share2XianLiaoGroup|error|1|" + group_id + "|" + tableId + "|", e);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.e("share2XianLiaoGroup|error|2|" + table.getId(), e);
        }
    }
}
