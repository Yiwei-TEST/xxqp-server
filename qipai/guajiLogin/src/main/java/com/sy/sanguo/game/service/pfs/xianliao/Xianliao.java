package com.sy.sanguo.game.service.pfs.xianliao;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Xianliao extends BaseSdk {
    @Override
    public String payExecute() {
        return null;
    }

    @Override
    public String loginExecute() {
        String result = "";
        String access_token ;
        String refresh_token ;
        long time1 = System.currentTimeMillis();
        if ("refresh".equals(getOpt())){
            PfSdkConfig pfSdkConfig = PfCommonStaticData.getConfig(getPf());
            JsonWrapper temp = XianliaoUtil.refreshAccessToken(pfSdkConfig.getAppId(),pfSdkConfig.getAppKey(),this.getString("refresh_token"));
            if (temp==null){
                return result;
            }
            access_token = temp.getString("access_token");
            refresh_token = temp.getString("refresh_token");
        }else if (getOpt()!=null&&getOpt().startsWith("auth:")){
            access_token = getOpt().substring(5);
            refresh_token = "refresh_token";
        }else{
            access_token = this.getString("access_token");
            refresh_token = this.getString("refresh_token");
        }

        JsonWrapper json = XianliaoUtil.getUserinfo(access_token);

        if ("1".equals(PropertiesCacheUtil.getValueOrDefault("xl_login_time","1",Constants.GAME_FILE))) {
            LogUtil.i("xianliao login time(ms):" + (System.currentTimeMillis() - time1));
        }

        if (json != null) {
            if (json.hasKey("openId")) {
                setSdkId(json.getString("openId"));
                json.putString("access_token",access_token);
                json.putString("refresh_token",refresh_token);
                result = json.toString();
            } else {
                GameBackLogger.SYS_LOG.error(pf + " loginExecute err:" + json.toString());
            }

        }
        return result;
    }

    @Override
    public void createRole(RegInfo regInfo, String info) throws Exception {
        JsonWrapper wrapper = new JsonWrapper(info);
        String openid = wrapper.getString("openId");
        String nickname = wrapper.getString("nickName");
        nickname = StringUtil.filterEmoji(nickname);
        String headimgurl = wrapper.getString("smallAvatar");
        String unionid = wrapper.getString("openId");
        int sex = wrapper.getInt("sex", 1);
        if (sex == 0) {
            sex = 1;
        }
        if (headimgurl==null){
            headimgurl="";
        }
        regInfo.setFlatId(openid);
        regInfo.setName(nickname);
        String imageName = MD5Util.getStringMD5(headimgurl);
        regInfo.setHeadimgraw(imageName);
        regInfo.setHeadimgurl(headimgurl);
        regInfo.setSex(sex);
        regInfo.setIdentity(unionid);
        String password = "xsg_" + regInfo.getPf() + "_pw_default_" + regInfo.getFlatId();
        regInfo.setPw(RegUtil.genPw(password));
        regInfo.setSessCode(RegUtil.genSessCode(regInfo.getFlatId()));

        JsonWrapper jsonWrapper = new JsonWrapper("");
        jsonWrapper.putString("xl",nickname);
        regInfo.setLoginExtend(jsonWrapper.toString());
    }

    @Override
    public Map<String, Object> refreshRole(RegInfo regInfo, String info) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonWrapper wrapper = new JsonWrapper(info);
        String nickname = wrapper.getString("nickName");
//        String headimgurl = wrapper.getString("smallAvatar");
        String unionid = wrapper.getString("openId");

//        int sex = wrapper.getInt("sex", 1);
//        if (sex == 0) {
//            sex = 1;
//        }

        if (StringUtils.isBlank(regInfo.getIdentity())) {
            map.put("identity", unionid);
            regInfo.setIdentity(unionid);
        }

        nickname = StringUtil.filterEmoji(nickname);
        if (!regInfo.getName().equals(nickname)) {
            map.put("name", nickname);
            regInfo.setName(nickname);
        }

        JsonWrapper jsonWrapper = new JsonWrapper(regInfo.getLoginExtend());
        if (!org.apache.commons.lang3.StringUtils.equals(nickname,jsonWrapper.getString("xl"))){
            jsonWrapper.putString("xl",nickname);
            regInfo.setLoginExtend(jsonWrapper.toString());
            map.put("loginExtend", regInfo.getLoginExtend());
        }

//        if (regInfo.getSex() != sex) {
//            map.put("sex", sex);
//            regInfo.setSex(sex);
//        }

//        if (!headimgurl.equals(regInfo.getHeadimgurl())) {
//            map.put("headimgurl", headimgurl);
//            map.put("headimgraw", MD5Util.getStringMD5(headimgurl));
//            regInfo.setHeadimgurl(headimgurl);
//        }

        return map;
    }

}
