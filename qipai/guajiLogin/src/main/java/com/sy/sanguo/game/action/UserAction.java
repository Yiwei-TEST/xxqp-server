package com.sy.sanguo.game.action;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CacheUtil;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.*;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.bean.enums.SourceType;
import com.sy.sanguo.game.bean.gold.GoldRoom;
import com.sy.sanguo.game.dao.*;
import com.sy.sanguo.game.dao.gold.GoldRoomDao;
import com.sy.sanguo.game.dao.match.MatchDao;
import com.sy.sanguo.game.pdkuai.action.PdkAction;
import com.sy.sanguo.game.pdkuai.action.RedBagAction;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.dao.MGauthorizationDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.pdkuai.util.SMSUtil;
import com.sy.sanguo.game.service.pfs.qq.QQ;
import com.sy.sanguo.game.service.pfs.qq.QqUtil;
import com.sy.sanguo.game.service.pfs.weixin.Weixin;
import com.sy.sanguo.game.service.pfs.weixin.util.WeixinUtil;
import com.sy.sanguo.game.service.pfs.xianliao.Xianliao;
import com.sy.sanguo.game.service.pfs.xianliao.XianliaoUtil;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import com.sy.sanguo.game.utils.BjdUtil;
import com.sy.sanguo.game.utils.LoginUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import com.alibaba.fastjson.TypeReference;

import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.dao.GameUserDao;
import com.sy.sanguo.game.pdkuai.game.PromotionAction;
import com.sy.sanguo.game.pdkuai.helper.CheckUserHelper;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.service.IMobileSdk;
import com.sy.sanguo.game.service.SdkFactory;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy599.sanguo.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAction.class);
    private static final Logger LOGIN_LOGGER = LoggerFactory.getLogger("login");
    private static final Logger LOGIN_SYS = LoggerFactory.getLogger("sys");
    private static final long serialVersionUID = 9102807327813284888L;
    private static Map<String, Long> ipRegTimeMap = new HashMap<>();
    private UserDaoImpl userDao;
    private ServerDaoImpl serverDao;
    private GameUserDao gameUserDao;
    private RoomCardDaoImpl roomCardDao;
    private NoticeDaoImpl noticeDaoImpl;
    private UserRelationDaoImpl userRelationDao;
    private InitData initData;
    private String result = "";


    public InitData getInitData() {
        return initData;
    }

    public void setInitData(InitData initData) {
        this.initData = initData;
    }

    public void setGameUserDao(GameUserDao gameUserDao) {
        this.gameUserDao = gameUserDao;
    }

    public RoomCardDaoImpl getRoomCardDao() {
        return roomCardDao;
    }

    public void setRoomCardDao(RoomCardDaoImpl roomCardDao) {
        this.roomCardDao = roomCardDao;
    }

    public NoticeDaoImpl getNoticeDaoImpl() {
        return noticeDaoImpl;
    }

    public void setNoticeDaoImpl(NoticeDaoImpl noticeDaoImpl) {
        this.noticeDaoImpl = noticeDaoImpl;
    }

    public void setUserRelationDao(UserRelationDaoImpl userRelationDao) {
        this.userRelationDao = userRelationDao;
    }


    public String getLoginConfig() {
        String ip = getIpAddr(getRequest());
        // String ip = "17.123";
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        int ipFlag = 0;
        if (!StringUtils.isBlank(ip)) {
            if (ip.startsWith("17.")) {
                ipFlag = 1;

            }
        }
        map.put("ipFlag", ipFlag);
        String ipAddr=IpAddressUtil.getIpAddress(ip,true);
        String[] allowAreas = PropertiesCacheUtil.getValueOrDefault("allow_area","中国,香港,澳门,台湾,老挝,泰国,越南",Constants.GAME_FILE).split(",");
        int allow = 0;
        for (String str:allowAreas){
            if (ipAddr.startsWith(str)){
                allow = 1;
                break;
            }
        }

        map.put("isCN",IpUtil.isIntranet(ip)?1:allow);

        this.result = JacksonUtil.writeValueAsString(map);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    public void loadUserMsg() throws Exception{
        String userId = request.getParameter("userId");
        String referer = request.getHeader("Referer");
        if (CommonUtil.isPureNumber(userId)&&StringUtils.contains(referer,"/h5/pay/index.jsp")){
            RegInfo user = userDao.getUser(Long.parseLong(userId));
            if (user!=null){
                user.setSessCode("");
                user.setLoginExtend("");
                user.setPw("");
                user.setActivity("");
                user.setExtend("");
                user.setInfo("");
                user.setConfig("");
                user.setRecord("");
            }
            OutputUtil.output(0,user==null?"{}":JSON.toJSONString(user),request,response,false);
        }else{
            OutputUtil.output(1,"FAIL",request,response,false);
        }
    }

    public void load() throws Exception{
        Map<String,String> params = com.sy.mainland.util.UrlParamUtil.getParameters(request);

        String userId = params.get("userId");

        if (NumberUtils.isDigits(userId)){

            RegInfo user = userDao.getUser(Long.parseLong(userId));

            if (user!=null){
                String ip = IpUtil.getIpAddr(request);
                Map<String,Object> map = new HashMap<>();
                map.put("ip",ip);

                int ret = userDao.updateUser(user.getUserId(),map);

                if (ret>0){
                    int serverId = user.getEnterServer();
                    Server server = SysInfManager.getInstance().getServer(serverId);
                    if (server!=null){
                        String url = SysInfManager.loadRootUrl(server);
                        if (StringUtils.isNotBlank(url)){
                            com.sy.mainland.util.HttpUtil.getUrlReturnValue(url+"/online/notice.do?type=SetIp&userId=" + userId + "&timestamp=" + System.currentTimeMillis() + "&message=" + ip);
                        }
                    }
                }

                OutputUtil.output(0,ip,request,response,false);
                return;
            }
        }

        OutputUtil.output(1,"FAIL",request,response,false);
    }

    /**
     * 用户登录
     */
    public String login() {
        this.result = "";
        String ip = getIpAddr(getRequest());

//        String validIps=PropertiesCacheUtil.getValue("validIps",Constants.GAME_FILE);
//        if (StringUtils.isNotBlank(validIps)&&!validIps.contains(ip)){
//            Map<String,String> map = new HashMap<>();
//            map.put("code","1");
//            map.put("msg","test");
//            this.result = JacksonUtil.writeValueAsString(map);
//
//            LogUtil.i(this.result);
//
//            return StringResultType.RETURN_ATTRIBUTE_NAME;
//        }

        if (StringUtils.isBlank(SysInfManager.baseUrl)) {
            String requesturl = getRequest().getRequestURL().toString();
            SysInfManager.baseUrl = requesturl.substring(0, requesturl.lastIndexOf("/"));
        }

        Map<String, Object> result = new HashMap<>();

        StringBuilder loginBuilder = new StringBuilder(1024);
        loginBuilder.append("login params:");

        try {
            RegInfo regInfo = null;
            String username = getRequest().getParameter("u");
            String password = getRequest().getParameter("ps");
            if (password == null) {
                password = "123456";
            }
            String platform = getRequest().getParameter("p");
            // 整包更新用_版本号
            String vc = getRequest().getParameter("vc");

            // 游戏客户端_版本号
            String syvc = getRequest().getParameter("syvc");

            String os = this.getString("os");
            String mac = getRequest().getParameter("mac");
            String deviceCode = getRequest().getParameter("deviceCode");

            String channelId = "";

            Map<String, Object> params = getRequest().getParameterMap();
            if (params != null) {
                for (Map.Entry<String, Object> kv : params.entrySet()) {
                    Object v = kv.getValue();
                    if (v instanceof String[]) {
                        loginBuilder.append(kv.getKey()).append("=").append(((String[]) kv.getValue())[0]).append(",");
                    } else {
                        loginBuilder.append(kv.getKey()).append("=").append(kv.getValue().toString()).append(",");
                    }

                }
                loginBuilder.append("ip=").append(ip);
            }

            // 黑名单过滤
            Map<String, Object> blackListMap = blackListFilter(ip, mac, deviceCode, null);
            if (blackListMap != null) {
                this.result = JacksonUtil.writeValueAsString(blackListMap);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            boolean isNewReg = false;
            IMobileSdk sdk;
            Map<String, Object> modify = null;
            long sdkLoginTime = 0;
            long time1 = TimeUtil.currentTimeMillis();
            boolean fromThird = false;
            String info = null;
            String uid = null;
            String unionId = null;
            if (StringUtils.isNotBlank(platform)&&!LoginUtil.pf_phoneNum.equals(platform)) {// 使用平台SDK登录
                if (!PfCommonStaticData.isHasPf(platform) || (sdk = SdkFactory.getLoginInst(platform, getRequest(), userDao, gameUserDao))==null){
                    GameBackLogger.SYS_LOG.info("platform::" + platform + " uid::" + uid + " login auth error");
                    result.put("code", 997);
                    result.put("msg", "没有找到该平台" + platform);
                    this.result = JacksonUtil.writeValueAsString(result);
                    return StringResultType.RETURN_ATTRIBUTE_NAME;
                }

                boolean isWx = (sdk instanceof Weixin);
                boolean isXl = isWx ? false :(sdk instanceof Xianliao);
                boolean isQQ = isWx ? false :(sdk instanceof QQ);

                int trMark;
                ThirdRelation thirdRelation;

                if (isWx || isXl || isQQ){
                    uid = this.getString("openid");

                    if (uid!=null){
                        uid = uid.replace(" ","+");
                    }

                    String code = this.getString("code");
                    int loginMark = 0;
                    if (StringUtils.isBlank(uid)&&StringUtils.isNotBlank(code)){
                        PfSdkConfig pfSdkConfig = PfCommonStaticData.getConfig(platform);
                        if (pfSdkConfig==null){
                            Map<String,Integer> map=new HashMap<>();
                            map.put("code",2019);
                            this.result = JacksonUtil.writeValueAsString(map);
                            return StringResultType.RETURN_ATTRIBUTE_NAME;
                        }
                        String miniProgram = isWx ? this.getString("miniProgram") : null;
                        if (StringUtils.isBlank(miniProgram)){
                        	JsonWrapper jsonWrapper = isXl?XianliaoUtil.getAccessToken(pfSdkConfig.getAppId(),pfSdkConfig.getAppKey(),code)
                        			:WeixinUtil.getAccessToken(pfSdkConfig.getAppId(),pfSdkConfig.getAppKey(),code);
                            if (jsonWrapper!=null){
                                sdk.setOpt("auth:"+jsonWrapper.getString("access_token"));
                                if (isWx){
                                    sdk.setExt("openid:"+jsonWrapper.getString("openid"));
                                }
                                info = sdk.loginExecute();
                                uid = sdk.getSdkId();
                                if (StringUtils.isNotBlank(info)){
                                    String refresh_token = jsonWrapper.getString("refresh_token");
                                    jsonWrapper=new JsonWrapper(info);
                                    jsonWrapper.putString("refresh_token",refresh_token);
                                    info=jsonWrapper.toString();
                                }
                            }
                        }else{
                        	JsonWrapper jsonWrapper = WeixinUtil.jscode2session(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code);
                            if (jsonWrapper!=null){
                                Weixin weixin = (Weixin) sdk;
                                weixin.setCode(code);
                                weixin.setSessionJson(jsonWrapper);

                                info = weixin.loginExecute();
                                uid = weixin.getSdkId();
                            }
                        }
                        loginMark = 1;
                    }

                    thirdRelation = userRelationDao.selectThirdRelation(uid,platform);
                    if (thirdRelation == null){
                        if (loginMark!=1){
                            sdk.setOpt("create");
                            info = sdk.loginExecute();
                            uid = sdk.getSdkId();
                        }
                        trMark=1;
                    }else if (System.currentTimeMillis()-thirdRelation.getCheckedTime().getTime()>=NumberUtils.toInt(PropertiesCacheUtil.getValueOrDefault("third_timeout","4",Constants.GAME_FILE),4)*60*60*1000){
                        if (loginMark!=1){
                            sdk.setOpt("refresh");
                            info = sdk.loginExecute();
                            if (StringUtils.isNotBlank(uid)&&!uid.equalsIgnoreCase(sdk.getSdkId())){
                                Map<String,Integer> map=new HashMap<>();
                                map.put("code",2018);
                                this.result = JacksonUtil.writeValueAsString(map);
                                return StringResultType.RETURN_ATTRIBUTE_NAME;
                            }else {
                                uid = sdk.getSdkId();
                            }
                        }
                        trMark=2;
                    }else{
                        if (loginMark!=1){
                            info = MessageBuilder.newInstance().builder("access_token", getString("access_token"))
                                        .builder("refresh_token", getString("refresh_token")).toString();
                        }
                        trMark=0;
                    }

                    if (StringUtils.isBlank(info)){
                        Map<String,Integer> map=new HashMap<>();
                        map.put("code",2018);
                        this.result = JacksonUtil.writeValueAsString(map);
                        return StringResultType.RETURN_ATTRIBUTE_NAME;
                    }
                }else{
                    info = sdk.loginExecute();
                    uid = sdk.getSdkId();
                    thirdRelation = userRelationDao.selectThirdRelation(uid,platform);
                    if (thirdRelation==null){
                        trMark=1;
                    }else{
                        trMark=2;
                    }
                }

                // 验证成功
                if (!StringUtils.isBlank(uid)) {
                    fromThird = true;
                    // 给每个从第三方平台进来的用户初始化一个唯一的密码
                    password = "xsg_" + platform + "_pw_default_" + uid;

                    if (trMark==0){
                        regInfo = this.userDao.getUser(thirdRelation.getUserId());
                    }else{
                        if (isWx) {
                            if ("true".equals(PropertiesCacheUtil.getValue("weixin_openid",Constants.GAME_FILE))){
                                regInfo = this.userDao.getUser(uid, platform);
                            }else{
                                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(info);
                                unionId = jsonObject.getString("unionid");
                                if (StringUtils.isNotBlank(unionId)) {
                                    regInfo = this.userDao.getUser(unionId, "weixin", uid, platform);
                                } else {
                                    regInfo = this.userDao.getUser(uid, platform);
                                }
                            }
                        } else if (isXl){
                            regInfo = this.userDao.getUser(uid, platform);
                        }else {
                            regInfo = this.userDao.getUser(uid, platform);
                        }
                    }

                    sdkLoginTime = TimeUtil.currentTimeMillis() - time1;

                    if (regInfo == null && StringUtils.isNotBlank(platform)){
                        String bind_pf = getString("bind_pf");

                        if (StringUtils.isNotBlank(bind_pf)){
                            String bind_access_token = getString("bind_access_token");
                            String bind_openid = getString("bind_openid");
                            String bind_fresh_token = getString("bind_fresh_token");

                            if (bind_pf.startsWith("weixin")){
                                if (StringUtils.isNotBlank(bind_openid)) {
                                    JsonWrapper bindMsg = StringUtils.isNotBlank(bind_access_token)?WeixinUtil.getUserinfo(bind_access_token, bind_openid):null;
                                    if (bindMsg != null && bindMsg.hasKey("openid")) {
                                        ThirdRelation tr = userRelationDao.selectThirdRelation(bindMsg.getString("openid"), bind_pf);
                                        if (tr != null) {
                                            regInfo = userDao.getUser(tr.getUserId());
                                        }
                                    }
                                    if (regInfo==null&&StringUtils.isNotBlank(bind_fresh_token)){
                                        PfSdkConfig pfSdkConfig = PfCommonStaticData.getConfig(bind_pf);
                                        if (pfSdkConfig!=null){
                                            bindMsg = WeixinUtil.refreshAccessToken(pfSdkConfig.getAppId(),bind_fresh_token);
                                            if (bindMsg != null && bind_openid.equalsIgnoreCase(bindMsg.getString("openid"))){
                                                ThirdRelation tr = userRelationDao.selectThirdRelation(bind_openid, bind_pf);
                                                if (tr != null) {
                                                    regInfo = userDao.getUser(tr.getUserId());
                                                }
                                            }
                                        }
                                    }
                                }
                            }else if (bind_pf.startsWith("xianliao")){
                                JsonWrapper bindMsg = StringUtils.isNotBlank(bind_access_token)?XianliaoUtil.getUserinfo(bind_access_token):null;
                                if (bindMsg == null){
                                    PfSdkConfig pfSdkConfig = PfCommonStaticData.getConfig(bind_pf);
                                    if (pfSdkConfig!=null&&StringUtils.isNotBlank(bind_fresh_token)){
                                        bindMsg = XianliaoUtil.refreshAccessToken(pfSdkConfig.getAppId(),pfSdkConfig.getAppKey(),bind_fresh_token);
                                        if (bindMsg!=null){
                                            bindMsg = XianliaoUtil.getUserinfo(bindMsg.getString("access_token"));
                                        }
                                    }
                                }
                                if (bindMsg!=null){
                                    ThirdRelation tr = userRelationDao.selectThirdRelation(bindMsg.getString("openId"),bind_pf);
                                    if (tr!=null){
                                        regInfo=userDao.getUser(tr.getUserId());
                                    }
                                }
                            }else if (bind_pf.startsWith("qq")){
                                PfSdkConfig pfSdkConfig = PfCommonStaticData.getConfig(bind_pf);
                                if (pfSdkConfig!=null&&StringUtils.isNotBlank(bind_access_token)&&StringUtils.isNotBlank(bind_openid)){
                                    JsonWrapper bindMsg = QqUtil.getUserinfo(pfSdkConfig.getAppId(),bind_access_token,bind_openid);
                                    if (bindMsg!=null){
                                        ThirdRelation tr = userRelationDao.selectThirdRelation(bind_openid,bind_pf);
                                        if (tr!=null){
                                            regInfo=userDao.getUser(tr.getUserId());
                                        }
                                    }
                                }
                            }
                        }

                    }

                    // 自动注册
                    if (regInfo == null) {
                        Map<String, Object> filterMap = filterIpMac(ip, mac);
                        if (filterMap != null) {
                            this.result = JacksonUtil.writeValueAsString(filterMap);
                            return StringResultType.RETURN_ATTRIBUTE_NAME;
                        }

                        long maxId;
                        if (Redis.isConnected()){
                            String cacheKey = CacheUtil.loadStringKey(String.class,"user_max_id20180403");
                            if(RedisUtil.tryLock(cacheKey,2,2000)){
                                try {
                                    maxId = Manager.getInstance().generatePlayerId(userDao);
                                }catch (Exception e){
                                    maxId = 0;
                                    LogUtil.e("Exception:"+e.getMessage(),e);
                                }finally {
                                    RedisUtil.unlock(cacheKey);
                                }
                            }else{
                                maxId = 0;
                            }
                        }else{
                            maxId = Manager.getInstance().generatePlayerId(userDao);
                        }

                        if (maxId<=0){
                            result.put("code", 999);
                            result.put("msg", "登录异常,请稍后再试");

                            this.result = JacksonUtil.writeValueAsString(result);
                            return StringResultType.RETURN_ATTRIBUTE_NAME;
                        }

                        regInfo = new RegInfo();
                        String mangguoPf = "common";
                        if(unionId != null) {// 芒果跑得快渠道记录
                            MGauthorization mGauthorization = MGauthorizationDao.getInstance().getMGauthorization(unionId);
                            if(mGauthorization != null)
                                mangguoPf = mGauthorization.getPf();
                        }
                        regInfo.setChannel(mangguoPf);
                        regInfo.setOs(os);
                        Manager.getInstance().buildBaseUser(regInfo, platform, maxId);
                        sdk.createRole(regInfo, info);
                        this.userDao.addUser(regInfo);
                        isNewReg = true;

                        if (regInfo.getPayBindId()>0){
                            UserExtendInfo userExtendInfo = userDao.getUserExtendinfByUserId(regInfo.getUserId());
                            int status = 0;
                            if (userExtendInfo == null) {
                                status = 1;
                            } else {
                                if (userExtendInfo.getBindSongCard() <= 0) {
                                    status = 2;
                                }
                            }
                            boolean giveRoomCard = status > 0 && SharedConstants.bindGiveRoomCards > 0;
                            if (giveRoomCard) {
                                UserMessage message = new UserMessage();
                                message.setUserId(regInfo.getUserId());
                                message.setContent("绑定邀请码" + regInfo.getPayBindId() + "成功，获得房卡 * " + SharedConstants.bindGiveRoomCards);
                                message.setTime(new Date());

                                int count = UserDao.getInstance().addUserCards(regInfo, 0, SharedConstants.bindGiveRoomCards, 0, null, message, CardSourceType.bindGiveRoomCards);

                                if (count > 0) {
                                    if (1 == status) {
                                        userDao.insertUserExtendinf(regInfo.getUserId(), "", regInfo.getPayBindId());
                                    } else if (2 == status) {
                                        userDao.updateUserBindSongCard(regInfo.getUserId(), regInfo.getPayBindId());
                                    }
                                }
                            }
                        }
                    } else {
                        blackListMap = blackListFilter(null, null, null, platform + "_" + uid);
                        if (blackListMap != null) {
                            this.result = JacksonUtil.writeValueAsString(blackListMap);
                            return StringResultType.RETURN_ATTRIBUTE_NAME;
                        }

                        if (trMark==0){
                            modify = new HashMap<>();
                        }else{
                            modify = sdk.refreshRole(regInfo, info);
                        }
                    }

                    if (regInfo.getUserId()>0&&StringUtils.isNotBlank(platform)&&StringUtils.isNotBlank(uid)){
                        if (trMark==0){
                        }else if (trMark==1){
                            userRelationDao.insert(new ThirdRelation(regInfo.getUserId(),platform,uid));
                        }else if (trMark==2){
                            userRelationDao.updateCheckedTime(thirdRelation.getKeyId().toString());
                        }
                    }

                } else {
                    GameBackLogger.SYS_LOG.info("platform::" + platform + " uid::" + uid + " login auth error");
                    result.put("code", 997);
                    result.put("msg", "没有找到该平台" + platform);
                    this.result = JacksonUtil.writeValueAsString(result);
                    return StringResultType.RETURN_ATTRIBUTE_NAME;
                }
            } else if (StringUtils.isBlank(platform)||"self".equals(platform)){
                if (LoginCacheContext.isDebug) {
                    platform = "self";
                }
            }

            // 验证用户名和密码合法性
            if (regInfo == null && (!checkUserName(username) || StringUtils.isBlank(password) || StringUtils.isBlank(platform))) {
                result.put("code", 995);
                result.put("msg", "账号或密码不合法");
            } else {
                if (regInfo == null) {
                    regInfo = this.userDao.getUser(username, platform);

                }
                if (regInfo == null) {
                    // 验证成功
                    if (!StringUtils.isBlank(username)) {
                        // 给每个从第三方平台进来的用户初始化一个唯一的密码
                        password = "xsg_" + platform + "_pw_default_" + username;
                        regInfo = this.userDao.getUser(username, platform);
                    }
                }
                if (regInfo != null) {

                    if (regInfo.getUserState() != null && regInfo.getUserState().intValue() == 0) {
                        result.put("code", 604);
                        result.put("msg", "您已被禁止登录，请联系所在群主或客服！");
                        this.result = JacksonUtil.writeValueAsString(result);
                        return StringResultType.RETURN_ATTRIBUTE_NAME;
                    }
                    String md5Pw=genPw(password);
                    boolean isPhoneLogin = "phoneLogin".equals(platform) ? md5Pw.equals(regInfo.getPhonePw()) : md5Pw.equals(regInfo.getPw());
                    if (fromThird || isPhoneLogin) {

                        //如果是手机登录username存的是手机号码，需要替换成username
                        if (LoginUtil.pf_phoneNum.equals(platform)){
                            username=regInfo.getName();
                        }


                        String gamevc = getString("gamevc");
//                        String extend = regInfo.getExtend();
                        boolean checkVc = false;
                        VersionCheck versionCheck = SysInfManager.versionCheckMap.get(platform);
                        if (versionCheck == null) {
                            versionCheck = SysInfManager.versionCheckMap.get("all");
                        }
                        if (versionCheck != null && StringUtils.isNotBlank(versionCheck.getVersionStr()) && StringUtils.isNotBlank(gamevc)) {
//                            String temp1 = gamevc.replaceAll("\\D","");//v1.2.3
//                            int version = NumberUtils.toInt(temp1,0);
                            checkVc = CheckUserHelper.checkVersion(versionCheck.getVersionStr(), 1, gamevc, 1);
                        }
                        if (checkVc) {
                            // 版本错误
                            result.put("code", 2017);
                            result.put("msg", "有新的更新内容，请退出重登！");

                            this.result = JacksonUtil.writeValueAsString(result);
                            return StringResultType.RETURN_ATTRIBUTE_NAME;
                        }

                        if (modify == null) {
                            modify = new HashMap<String, Object>();
                        }
                        Date now = TimeUtil.now();
                        if (regInfo.getLogTime() != null && !TimeUtil.isSameDay(regInfo.getLogTime().getTime(), now.getTime())) {
                            modify.put("loginDays", 1);
                            regInfo.setPreLoginTime(regInfo.getLogTime());
                            modify.put("preLoginTime", regInfo.getPreLoginTime());
                        }

                        regInfo.setSessCode(genSessCode(username));
                        regInfo.setLogTime(now);

                        modify.put("sessCode", regInfo.getSessCode());
                        modify.put("logTime", regInfo.getLogTime());

                        if (!StringUtils.isBlank(os) && !os.equals(regInfo.getOs())) {
                            modify.put("os", os);
                        }

                        if (!StringUtils.isBlank(ip) && !ip.equals(regInfo.getIp())) {
                            modify.put("ip", ip);
                            operateIp(ip, regInfo.getIp());
                        }
                        // modify.put("ip", "");
                        if (!StringUtils.isBlank(mac) && !mac.equals(regInfo.getMac())) {
                            modify.put("mac", mac);
                            operateMac(mac, regInfo.getMac());
                        }
                        if (!StringUtils.isBlank(deviceCode) && !deviceCode.equals(regInfo.getSessCode())) {
                            modify.put("deviceCode", deviceCode);
                        }
                        if (!StringUtils.isBlank(syvc) && !syvc.equals(regInfo.getSyvc())) {
                            modify.put("syvc", syvc);
                        }
                        int totalCount = Manager.getInstance().checkTotalCount(regInfo);
                        if (regInfo.getTotalCount() != totalCount) {
                            modify.put("totalCount", totalCount);
                        }
                        // 检测房间号
                        int servserId = 0;
                        if (regInfo.getPlayingTableId() != 0) {
                            if (regInfo.getPlayingTableId()>Constants.MIN_GOLD_ID){
                                GoldRoom goldRoom=GoldRoomDao.getInstance().loadGoldRoom(regInfo.getPlayingTableId());
                                if (goldRoom!=null&&NumberUtils.toInt(goldRoom.getCurrentState())<2){
                                    servserId=goldRoom.getServerId().intValue();
                                }
                            }else{
                                servserId = Manager.getInstance().getServerId(regInfo.getPlayingTableId());
                            }

                            if (servserId != regInfo.getEnterServer()) {
                                regInfo.setEnterServer(servserId);
                                modify.put("enterServer", servserId);
                            }
                            if (servserId == 0) {
                                regInfo.setPlayingTableId(0);
                                modify.put("playingTableId", 0);
                            }
                        }

                        SyvcFilter syvcFilter = getFilterForSyvc(syvc);
                        if (syvcFilter != null) {
                            result.put("isIosAudit", syvcFilter.getIsIosAudit());
                            if (syvcFilter.getAuditServer() != 0 && syvcFilter.getAuditServer() != regInfo.getEnterServer()) {
                                modify.put("enterServer", syvcFilter.getAuditServer());
                                modify.put("playingTableId", 0);
                            }
                        } else {
                            result.put("isIosAudit", 0);
                        }

                        this.userDao.updateUser(regInfo, modify);
                        long now1 = TimeUtil.currentTimeMillis();
                        long updateTime = now1 - time1;
                        long updateTime2 = now1 - now.getTime();


                        if (StringUtils.isNotBlank(info)&&info.contains("access_token")&&StringUtils.isNotBlank(uid)){
                            JsonWrapper jsonWrapper = new JsonWrapper(info);
                            result.put("access_token",jsonWrapper.getString("access_token"));
                            result.put("refresh_token",jsonWrapper.getString("refresh_token"));
                            result.put("openid",uid);
                            result.put("pf",platform);
                        }
                        // 登录成功
                        result.put("code", 0);

                        // 查看当前服务器过滤规则(通告和服务器屏蔽或开启列表)
                        // ServerFilter myFilter = getFilter(platform);
                        // if (myFilter.getUseNotice() != 1 ||
                        // StringUtils.isEmpty(platform)) {
                        result.put("notices", new ArrayList<Notice>());
                        String systemMessage = CheckUserHelper.getSystemMessage(noticeDaoImpl.getAllSystemMessage());
                        if (!StringUtils.isBlank(systemMessage)) {
                            result.put("message", systemMessage);
                        }
                        // } else {
                        // if (myFilter.getNoticeId() != 0) {
                        // result.put("notices",
                        // SysInfManager.getInstance().getNoticesById(myFilter.getNoticeId()));
                        //
                        // } else {
                        // result.put("notices",
                        // SysInfManager.getInstance().getNoticesByPf(platform));
                        //
                        // }
                        // }
                        Manager.getInstance().check(regInfo);
                        regInfo.setPf(platform);
                        String timeRemoveBindStr = PropertiesCacheUtil.getValue("periodRemoveBind",Constants.GAME_FILE);
                        if (!StringUtils.isBlank(timeRemoveBindStr)&&!"0".equals(timeRemoveBindStr)) {
                            // 七天没玩并且绑码超过七天，解绑
                            if (regInfo.getPayBindId()>0&&TimeUtil.apartDays(regInfo.getPayBindTime(), new Date())>7&&regInfo.getLastPlayTime()!= null&&TimeUtil.apartDays(regInfo.getLastPlayTime(), new Date())>7) {
                                int res = autoRemoveBind(regInfo);
                                if (res == 1) {
                                    regInfo.setPayBindId(0);
                                }
                            }
                        }
                        User user = new User();
                        user.setIsNewReg(isNewReg ? 1 : 0);
                        regInfo.setPf(platform);
                        if (platform!=null){
                            if (platform.startsWith("weixin")){
                                String name=new JsonWrapper(regInfo.getLoginExtend()).getString("wx");
                                if (name!=null)
                                regInfo.setName(name);
                            }else if (platform.startsWith("xianliao")){
                                String name=new JsonWrapper(regInfo.getLoginExtend()).getString("xl");
                                if (name!=null)
                                regInfo.setName(name);
                            }else if (platform.startsWith("qq")){
                                String name=new JsonWrapper(regInfo.getLoginExtend()).getString("qq");
                                if (name!=null)
                                    regInfo.setName(name);
                            }
                        }

                        user = buildUser(regInfo,user);

                        result.put("user", user);
                        result.put("currTime", TimeUtil.currentTimeMillis());

                        result.put("blockIconTime", SharedConstants.blockIconTime);
                        // SyvcFilter syvcFilter = getFilterForSyvc(syvc);

                        int payBindId = regInfo.getPayBindId();
//                        if (payBindId == 0) {
//                            payBindId = regInfo.getRegBindId();
//                        }

                        HashMap<String, Object> agencyInfo = null;
                        if (payBindId > 0) {
                            agencyInfo = roomCardDao.queryAgencyByAgencyId(payBindId);
                            if (agencyInfo != null) {
                                Map<String, Object> retMap = new HashMap<>();
                                retMap.put("agencyId", agencyInfo.get("agencyId"));
                                retMap.put("agencyWechat", agencyInfo.get("agencyWechat"));
                                retMap.put("agencyPhone", agencyInfo.get("agencyPhone"));
                                retMap.put("agencyPf", agencyInfo.get("pf"));

                                result.put("agencyInfo", retMap);
                            }
                        }

                        // 更新版本
                        if (!StringUtils.isBlank(vc) && !StringUtils.isBlank(os) && os.equals("Android")) {
                            if (SysInfManager.getInstance().getVersionMap().containsKey(platform)) {
                                Version ver = SysInfManager.getInstance().getVersionMap().get(platform);
                                if (Integer.parseInt(vc) > 0 && Integer.parseInt(vc) < ver.getVersionId()) {
                                    result.put("vc", ver.getVersionId());
                                    result.put("url", ver.getDownloadUrl());
                                }
                            }
                        }
                        // 自有平台渠道号
                        if (!StringUtils.isBlank(channelId)) {
                            result.put("channelid", channelId);
                        }

                        if (updateTime > 1000) {
                            GameBackLogger.SYS_LOG.info("user login:uid-->" + regInfo.getUserId() + " sdktime:" + sdkLoginTime + " updateTime-->" + updateTime + " updateTime2-->" + updateTime2
                                    + " sId-->" + servserId);

                        }

                        if ("1".equals(PropertiesCacheUtil.getValue("open_user_relation",Constants.GAME_FILE))) {
                            String gameCode = getRequest().getParameter("gameCode");
                            if (org.apache.commons.lang3.StringUtils.isBlank(gameCode)) {
                                if (platform.startsWith("weixin") && platform.length() > 6) {
                                    gameCode = platform.substring(6);
                                } else {
                                    gameCode = platform;
                                }
                            }
                            String userId = String.valueOf(regInfo.getUserId());
                            UserRelation relation = userRelationDao.select(gameCode, userId);
                            if (relation == null) {
                                relation = new UserRelation();
                                relation.setGameCode(gameCode);
                                relation.setUserId(userId);
                                relation.setRegPf(platform);
                                relation.setRegTime(new Date());
                                relation.setLoginPf(platform);
                                relation.setLoginTime(relation.getRegTime());
                                userRelationDao.insert(relation);
                            } else {
                                userRelationDao.update(relation.getKeyId().toString(), platform, new Date());
                            }

                            if (agencyInfo != null && agencyInfo.size() > 0) {
                                String agencyPf = String.valueOf(agencyInfo.get("pf"));
                                if (org.apache.commons.lang3.StringUtils.isNotBlank(agencyPf) && (!"null".equalsIgnoreCase(agencyPf))) {
                                    gameCode = gameCode + "_" + agencyPf;
                                    relation = userRelationDao.select(gameCode, userId);
                                    if (relation == null) {
                                        relation = new UserRelation();
                                        relation.setGameCode(gameCode);
                                        relation.setUserId(userId);
                                        relation.setRegPf(platform);
                                        relation.setRegTime(new Date());
                                        relation.setLoginPf(platform);
                                        relation.setLoginTime(relation.getRegTime());
                                        userRelationDao.insert(relation);
                                    } else {
                                        userRelationDao.update(relation.getKeyId().toString(), platform, new Date());
                                    }
                                }
                            }

                        }
                    } else {
                        // 密码错误
                        result.put("code", 994);
                        result.put("msg", "密码错误");
                    }
                } else {
                    // 用户不存在
                    result.put("code", 996);
                    result.put("msg", "账号不存在");
                    GameBackLogger.SYS_LOG.error("code 996-->user not exist:" + username);
                }
            }
            result.put("redBagOpen", RedBagAction.isRedBagOpen() ? true : false);// 小甘瓜分现金红包活动是否开启
        } catch (Exception e) {
            exception(result, "login.exception:" + e.getMessage(), e);
        }finally {
            if (StringUtils.isBlank(this.result)){
                this.result = JacksonUtil.writeValueAsString(result);
            }
            loginBuilder.append(",login result=").append(this.result);

            LOGIN_LOGGER.info(loginBuilder.toString());
        }

        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    public void hasPay() throws Exception {
        long userId=getLong("mUserId");
        if (userId>0){
            boolean isFirstPay = UserDao.getInstance().isFirstPay(userId,1,9);

            String firstPayGive = PropertiesCacheUtil.getValue("first_pay_give",Constants.GAME_FILE);
            JSONObject json = new JSONObject();
            json.put("code",0);
            json.put("message",!isFirstPay);

            String award = "0";
            if (StringUtils.isNotBlank(firstPayGive)){
                json.put("firstPayGive",firstPayGive);
                if (!isFirstPay){
                    HashMap<String,Object> extendMap = UserDao.getInstance().queryUserExtend(String.valueOf(userId),200);
                    if (extendMap!=null){
                        UserExtend userExtend = CommonUtil.map2Entity(UserExtend.class,extendMap);
                        if ("1".equals(userExtend.getMsgState())){
                            award = userExtend.getMsgType()+"_"+userExtend.getMsgKey();
                        }else{
                            award = "2";
                        }
                    }
                }
            }
            json.put("award",award);

            OutputUtil.output(json,getRequest(),getResponse(),null,false);
        }
    }

    /**
     * 领取首充奖励
     * @throws Exception
     */
    public void payAward() throws Exception {
        String userId=getString("mUserId");
        String award=getString("award");
        if (CommonUtil.isPureNumber(userId)&&StringUtils.isNotBlank(award)){
            RegInfo user = userDao.getUser(Long.parseLong(userId));
            int idx = award.indexOf("_");
            String type = award.substring(0,idx);
            if (user!=null&&CommonUtil.isPureNumber(type)){
                HashMap<String,Object> extendMap = UserDao.getInstance().queryUserExtend(userId,Integer.parseInt(type));
                if (extendMap!=null) {
                    UserExtend userExtend = CommonUtil.map2Entity(UserExtend.class, extendMap);
                    if ("1".equals(userExtend.getMsgState())&&award.substring(idx+1).equals(userExtend.getMsgKey())) {
                        HashMap<String ,Object> map = new HashMap<>();
                        map.put("userId",userId);
                        map.put("msgType",userExtend.getMsgType().toString());
                        map.put("msgState","0");
                        if(UserDao.getInstance().updateUserExtend(map)>0){
                            String firstPayGive = userExtend.getMsgValue();
                            if (StringUtils.isNotBlank(firstPayGive)){
                                    String[] strs = firstPayGive.split(",");
                                    for (String str:strs){
                                        if (StringUtils.isNotBlank(str)){
                                            if (str.startsWith("cards_")){
                                                int cards = Integer.parseInt(str.substring(6));
                                                userDao.addUserCards(user,0, cards, CardSourceType.payFirstAward);
                                            }else if (str.startsWith("gold_")){
                                                HashMap<String,Object> goldMap = new HashMap<>();
                                                goldMap.put("userId",String.valueOf(user.getUserId()));
                                                goldMap.put("userName",user.getName());
                                                goldMap.put("userNickname",user.getName());
                                                goldMap.put("playCount","0");
                                                goldMap.put("playCountWin","0");
                                                goldMap.put("playCountLose","0");
                                                goldMap.put("playCountEven","0");
                                                int freeGold = Integer.parseInt(str.substring(5));
                                                goldMap.put("freeGold",freeGold);
                                                goldMap.put("Gold",0);
                                                goldMap.put("usedGold","0");
                                                goldMap.put("vipexp","0");
                                                goldMap.put("exp","0");
                                                goldMap.put("sex",user.getSex());
                                                goldMap.put("signature","");
                                                goldMap.put("headimgurl",user.getHeadimgurl());
                                                goldMap.put("headimgraw",user.getHeadimgraw());
                                                goldMap.put("extend","");
                                                goldMap.put("regTime",CommonUtil.dateTimeToString());
                                                goldMap.put("lastLoginTime",CommonUtil.dateTimeToString());
                                                userDao.saveOrUpdateUserGoldInfo(goldMap);
                                                GameUtil.sendPay(user.getEnterServer(),user.getUserId(),0,freeGold,null,"2");
                                            }
                                        }
                                    }
                            }

                            JSONObject json = new JSONObject();
                            json.put("code",0);
                            json.put("message","领取成功");

                            OutputUtil.output(json,getRequest(),getResponse(),null,false);
                            return;
                        }
                    }
                }
            }
        }
        OutputUtil.output(1,"领取失败",getRequest(),getResponse(),false);
    }

    private int autoRemoveBind(RegInfo regInfo) {
        int res = userDao.removeBindInfo(regInfo);
        if (res == 1) {
            PdkAction.removeBind(regInfo);
        }
        return res;
    }

    // #####################@ anysdk ########################

    private Map<String, Object> filterIpMac(String ip, String mac) {
        if (InitData.state == 0) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        // ip过滤
        if (!StringUtils.isBlank(ip)) {
            if (!InitData.ipWhite.contains(ip) && InitData.ipGroupMap.containsKey(ip)) {
                IpGroup ipGroup = InitData.ipGroupMap.get(ip);
                if (ipGroup.getCount() >= InitData.maxIpCount) {
                    GameBackLogger.SYS_LOG.info("UserAction login ip repeat max, ip:" + ip);
                    result.put("code", 500);
                    result.put("msg", "login ip repeat max");
                    return result;
                }
            }

        }
        // mac过滤
        if (!StringUtils.isBlank(mac)) {
            if (!InitData.macWhite.contains(mac) && InitData.macGroupMap.containsKey(mac)) {
                MacGroup macGroup = InitData.macGroupMap.get(mac);
                if (macGroup.getCount() >= InitData.maxMacCount) {
                    GameBackLogger.SYS_LOG.info("UserAction login mac repeat max, mac:" + mac);
                    result.put("code", 501);
                    result.put("msg", "login mac repeat max");
                    return result;
                }
            }

        }
        return null;
    }

    private Map<String, Object> blackListFilter(String ip, String mac, String dc, String flatId) {
        if (StringUtils.isBlank(ip) && StringUtils.isBlank(mac) && StringUtils.isBlank(dc) && StringUtils.isBlank(flatId)) {
            return null;
        }
        initData.refreshBlack();
        Map<String, Object> result = new HashMap<String, Object>();

        if (!StringUtils.isBlank(ip) && SysInfManager.getInstance().getIpBlackList().contains(ip)) {
            GameBackLogger.SYS_LOG.info("UserAction login blackListFilter:" + ip);
            result.put("code", 600);
            result.put("msg", "该IP已被禁止登陆");
            return result;
        }
        if (!StringUtils.isBlank(mac) && SysInfManager.getInstance().getMacBlackList().contains(mac)) {
            GameBackLogger.SYS_LOG.info("UserAction login blackListFilter:" + mac);
            result.put("code", 601);
            result.put("msg", "该MAC已被禁止登陆");
            return result;
        }
        if (!StringUtils.isBlank(dc) && SysInfManager.getInstance().getDcBlackList().contains(dc)) {
            GameBackLogger.SYS_LOG.info("UserAction login blackListFilter:" + dc);
            result.put("code", 602);
            result.put("msg", "该设备已被禁止登陆");
            return result;
        }
        if (!StringUtils.isBlank(flatId) && SysInfManager.getInstance().getFlatIdBlackList().contains(flatId)) {
            GameBackLogger.SYS_LOG.info("UserAction login blackListFilter:" + flatId);
            result.put("code", 603);
            result.put("msg", "您已被禁止登陆");
            return result;
        }
        return null;
    }

    private void operateIp(String ip, String oldIp) {
        if (InitData.state == 0) {
            return;
        }
        // ip过滤
        if (!StringUtils.isBlank(ip)) {
            if (!InitData.ipWhite.contains(ip)) {
                if (InitData.ipGroupMap.containsKey(ip)) {
                    IpGroup ipGroup = InitData.ipGroupMap.get(ip);
                    ipGroup.setCount(ipGroup.getCount() + 1);
                    InitData.ipGroupMap.put(ip, ipGroup);
                } else {
                    IpGroup ipGroup = new IpGroup(ip, 1);
                    InitData.ipGroupMap.put(ip, ipGroup);
                }

            }

        }

        if (!StringUtils.isBlank(oldIp)) {
            if (InitData.ipGroupMap.containsKey(oldIp)) {
                IpGroup ipGroup = InitData.ipGroupMap.get(oldIp);
                if (ipGroup.getCount() > 1) {
                    ipGroup.setCount(ipGroup.getCount() - 1);
                    InitData.ipGroupMap.put(oldIp, ipGroup);
                } else {
                    InitData.ipGroupMap.remove(oldIp);
                }

            }
        }

    }

    private void operateMac(String mac, String oldMac) {
        if (InitData.state == 0) {
            return;
        }

        // mac过滤
        if (!StringUtils.isBlank(mac)) {
            if (!InitData.macWhite.contains(mac)) {
                if (InitData.macGroupMap.containsKey(mac)) {
                    MacGroup macGroup = InitData.macGroupMap.get(mac);
                    macGroup.setCount(macGroup.getCount() + 1);
                    InitData.macGroupMap.put(mac, macGroup);
                } else {
                    MacGroup macGroup = new MacGroup(mac, 1);
                    InitData.macGroupMap.put(mac, macGroup);
                }
            }
        }

        if (!StringUtils.isBlank(oldMac)) {
            if (InitData.macGroupMap.containsKey(oldMac)) {
                MacGroup macGroup = InitData.macGroupMap.get(oldMac);
                if (macGroup.getCount() > 1) {
                    macGroup.setCount(macGroup.getCount() - 1);
                    InitData.macGroupMap.put(oldMac, macGroup);
                } else {
                    InitData.macGroupMap.remove(oldMac);
                }

            }

        }
    }

    /**
     * 选区后进入游戏
     *
     * @return
     */
    public String gotogame() {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String username = getRequest().getParameter("u");
            int sid = Integer.valueOf(getRequest().getParameter("sid"));
            String sessCode = getRequest().getParameter("c");
            String platform = getRequest().getParameter("p");

            // if("uc".equals(platform)){
            // result.put("code", 999);
            // result.put("msg", "the pf can not login");
            // this.result = JacksonUtil.writeValueAsString(result);
            // GameBackLogger.SYS_LOG.info("gotogame the pf can not login::" +
            // platform);
            // return StringResultType.RETURN_ATTRIBUTE_NAME;
            // }

            // 自有平台渠道号
            String channelid = getRequest().getParameter("channelid");
            if (!checkUserName(username) || !SysInfManager.getInstance().isCorrectServer(sid) || StringUtils.isBlank(platform)) {
                result.put("code", 2);
                result.put("msg", "param error");
            } else {
                if (!StringUtils.isBlank(channelid)) {
                    platform = platform + channelid;
                }
                RegInfo userInfo;
                userInfo = this.userDao.getUser(username, platform);
                if (userInfo != null) {
                    if (userInfo.getSessCode().equals(sessCode)) {
                        List<Integer> servers = new ArrayList<Integer>();
                        if (!StringUtils.isBlank(userInfo.getPlayedSid())) {
                            servers = JacksonUtil.readValue(userInfo.getPlayedSid(), new TypeReference<List<Integer>>() {
                            });
                        }
                        int index = servers.indexOf(sid);
                        if (index >= 0) {
                            servers.remove(index);
                            servers.add(0, sid);
                        } else {
                            servers.add(0, sid);
                        }
                        userInfo.setPlayedSid(JacksonUtil.writeValueAsString(servers));
                        Map<String, Object> modify = new HashMap<String, Object>();
                        modify.put("playedSid", userInfo.getPlayedSid());
                        this.userDao.updateUser(userInfo, modify);
                        userInfo.setPf(platform);
                        result.put("code", 0);
                        result.put("user", buildUser(userInfo, Collections.<Server>emptyList()));
                        long now = System.currentTimeMillis();
                        // 传给前台的参数
                        StringBuilder sub = new StringBuilder();
                        sub.append("deviceCode=").append(userInfo.getDeviceCode());
                        sub.append("&psid=").append(userInfo.getPlayedSid());
                        sub.append("&regtime=").append(userInfo.getRegTime().getTime());
                        sub.append("&time=");
                        sub.append(now);
                        sub.append("&serverId=");
                        sub.append(sid);
                        sub.append("&pf=");
                        sub.append(userInfo.getPf());

                        // 用作md5
                        StringBuilder sbmd5 = new StringBuilder();
                        sbmd5.append("userId=");
                        sbmd5.append(username);
                        sbmd5.append(sub.toString());
                        sbmd5.append(LoginCacheContext.gameloginkey);

                        sub.append("&sign=");
                        sub.append(MD5UtilSy.getStringMD5(sbmd5.toString()));
                        result.put("k", sub.toString());

                        // UserUtil.tickOff(sid,userInfo);
                    } else {
                        // session code不对
                        result.put("code", 1);
                        result.put("msg", "session code error");
                        GameBackLogger.SYS_LOG.info("gotogame session code error username::" + username);
                    }
                } else {
                    // 用户不存在
                    result.put("code", 993);
                    result.put("msg", "user not exist");
                }
            }
        } catch (Exception e) {
            exception(result, "gotogame.exception", e);
        }
        this.result = JacksonUtil.writeValueAsString(result);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    /**
     * 注册
     *
     * @return
     */
    public void register() {
//        if (!LoginCacheContext.isDebug) {
//            // 不允许注册
//            return StringResultType.RETURN_ATTRIBUTE_NAME;
//
//        }

        String ip = getIpAddr(getRequest());

//        String validIps=PropertiesCacheUtil.getValue("validIps",Constants.GAME_FILE);
//        if (StringUtils.isNotBlank(validIps)&&!validIps.contains(ip)){
//            Map<String,String> map = new HashMap<>();
//            map.put("code","1");
//            map.put("msg","test");
//            this.result = JacksonUtil.writeValueAsString(map);
//
//            LogUtil.i(this.result);
//
//            return StringResultType.RETURN_ATTRIBUTE_NAME;
//        }

        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            if (params != null) {
                StringBuilder stringBuilder = new StringBuilder("register params:");
                for (Map.Entry<String, String> kv : params.entrySet()) {
                    stringBuilder.append(kv.getKey()).append("=").append(kv.getValue().toString()).append(",");

                }
                stringBuilder.append("ip=").append(ip);
                LogUtil.i(stringBuilder.toString());
            }

            Map<String, Object> result = new HashMap<>();
            String platform = params.get("p");
            if ("self".equals(platform)) {
                result = this.selfRegister(params);
            } else {
                result.put("code", -3);
                result.put("msg", LangMsg.getMsg(LangMsg.code_3));
            }
            JSONObject json = new JSONObject();
            json.putAll(result);
            OutputUtil.outputJson(json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            return;
        }
        LogUtil.i("register:" + this.result);
    }

    private Map<String, Object> selfRegister(Map<String, String> params) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("u");
        String nickName = params.get("nickName");
        String password = params.get("ps");
        String sign = params.get("k");
        long time = NumberUtils.toLong(params.get("t"));
        String platform = "self";
        String channel = params.get("c");// 渠道
        String deviceCode = params.get("deviceCode");// 设备码
        String headimgurl = params.get("headimgurl");// 头像

        String secret = "mwFLeKLzNoL46dDn0vE2";
        StringBuilder md5 = new StringBuilder();
        md5.append(username == null ? "" : username);
        md5.append(password == null ? "" : password);
        md5.append(time);
        md5.append(platform);
        md5.append(channel);
        md5.append(secret);

        if (!MD5Util.getStringMD5(md5.toString()).equals(sign)) {
            result.put("code", 4);
            result.put("msg", "md5验证失败");
            return result;
        }

        // 验证用户名和密码合法性
        if (StringUtils.isBlank(username) || !StringUtil.checkUserNameForSelfRegister(username)) {
            result.put("code", 992);
            result.put("msg", "账号不合法，字母开头，6到20位");
            return result;
        }
        if (StringUtils.isBlank(nickName)) {
            result.put("code", 991);
            result.put("msg", "请输入昵称");
            return result;
        }
        if (nickName.equals(username)) {
            result.put("code", 991);
            result.put("msg", "昵称不能与账号相同");
            return result;
        }
        int len = StringUtil.lengthOfNickName(nickName);
        if (len == 0 || len > 12) {
            result.put("code", 994);
            result.put("msg", "昵称过长");
            return result;
        }
        String checkRes = StringUtil.checkPassword(password);
        if (null != checkRes) {
            result.put("code", 993);
            result.put("msg", checkRes);
            return result;
        }

        String filt = KeyWordsFilter.getInstance_1().filt(nickName);
        if (!nickName.equals(filt)) {
            result.put("code", 1);
            result.put("msg", "昵称不合法：\n【" + nickName + "】-->【" + filt + "】");
            return result;
        }

        RegInfo regInfo;
        regInfo = this.userDao.getUser(username, platform);
        if (regInfo != null) {
            // 用户已存在
            result.put("code", 1);
            result.put("msg", "用户名已存在");
            return result;
        }

        // 注册成功
        long maxId = Manager.getInstance().generatePlayerId();
        regInfo = new RegInfo();
        regInfo.setFlatId(username);
        int sex = new Random().nextInt(100) >= 70 ? Constants.SEX_FEMALE : Constants.SEX_MALE;
        regInfo.setSex(sex);
        regInfo.setName(nickName);
        regInfo.setPw(genPw(password));
        regInfo.setSessCode(genSessCode(username));
        if (StringUtils.isNotBlank(deviceCode)) {
            regInfo.setDeviceCode(deviceCode);
        }
        if (StringUtils.isNotBlank(headimgurl)) {
            regInfo.setHeadimgurl(headimgurl);
        }
        Manager.getInstance().buildBaseUser(regInfo, platform, maxId);
        long ret = this.userDao.addUser(regInfo);
        if (ret <= 0) {
            result.put("code", 1);
            result.put("msg", "用户名已存在");
            return result;
        }
        regInfo.setPf(platform);
        result.put("code", 0);
        result.put("user", buildUser(regInfo, Collections.<Server>emptyList()));
        return result;
    }

    private Map<String, Object> comRegister() throws Exception {
        Map<String, Object> result = new HashMap<>();
        String username = getRequest().getParameter("u");
        String nickName = getRequest().getParameter("nickName");
        String password = getRequest().getParameter("ps");
        long time = Long.valueOf(getRequest().getParameter("t"));
        String platform = getRequest().getParameter("p");
        String channel = getRequest().getParameter("c");// 渠道
        String sign = getRequest().getParameter("k");

        String secret = "mwFLeKLzNoL46dDn0vE2";
        StringBuilder md5 = new StringBuilder();
        md5.append(username == null ? "" : username);
        md5.append(password == null ? "" : password);
        md5.append(time);
        md5.append(platform);
        md5.append(channel);
        md5.append(secret);
        if (!MD5Util.getStringMD5(md5.toString()).equals(sign)) {
            result.put("code", 4);
            result.put("msg", "md5验证失败");
            return result;
        }

        if (!StringUtils.isBlank(username)) {
            // 非游客登陆
            platform = platform + channel;
            // 验证用户名和密码合法性
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || !checkUserName(username) || (password.length() < 6 || password.length() > 20)) {
                result.put("code", 992);
                result.put("msg", "账号或密码不合法");
                return result;
            }

            if (StringUtils.isNotBlank(nickName)) {
                String filt = KeyWordsFilter.getInstance_1().filt(nickName);
                if (!nickName.equals(filt)) {
                    result.put("code", 1);
                    result.put("msg", "昵称不合法");
                    return result;
                }
            } else {
                nickName = username;
            }

            RegInfo regInfo;
            regInfo = this.userDao.getUser(username, platform);
            if (regInfo != null) {
                // 用户已存在
                result.put("code", 1);
                result.put("msg", "账号已存在");
                return result;
            }
            // 注册成功
            long maxId = Manager.getInstance().generatePlayerId();
            regInfo = new RegInfo();
            regInfo.setFlatId(username);
            regInfo.setSex(1);

            regInfo.setName(nickName);
            regInfo.setPw(genPw(password));
            regInfo.setSessCode(genSessCode(username));
            Manager.getInstance().buildBaseUser(regInfo, platform, maxId);
            this.userDao.addUser(regInfo);
            regInfo.setPf(platform);
            result.put("code", 0);
            result.put("user", buildUser(regInfo, new User()));
        } else {
            String ip = getIpAddr(getRequest());
            boolean checkIp = checkIpReg(ip, time);
            if (checkIp) {
                result.put("code", 5);
                result.put("msg", "ip验证失败");
                return result;
            }

            platform = platform + channel;
            // 游客登陆
            // username = "vk" + String.valueOf(System.currentTimeMillis() /
            // 1000) + MathUtil.mt_rand(1000, 9999);
            username = "vk" + StringUtil.getRandomLowerString(3) + MathUtil.mt_rand(1000000, 9999999);
            // 游客默认密码为 :pw+用户名md5后的载一段字符(10到16号位置)
            password = "pw" + MD5Util.getStringMD5(username).substring(9, 15);

            RegInfo regInfo = this.userDao.getUser(username, platform);
            if (regInfo != null) {
                // 用户已存在
                result.put("code", 1);
                result.put("msg", "账号已存在,请重新登录!");
            } else {
                // 注册成功
                long maxId = Manager.getInstance().generatePlayerId(userDao);
                regInfo = new RegInfo();
                regInfo.setFlatId(username);
                regInfo.setSex(1);
                regInfo.setName(username);
                regInfo.setPw(genPw(password));
                regInfo.setSessCode(genSessCode(username));
                Manager.getInstance().buildBaseUser(regInfo, platform, maxId);
                this.userDao.addUser(regInfo);
                regInfo.setPf(platform);
                result.put("code", 0);
                result.put("user", buildUser(regInfo, new User()));
                result.put("password", password);
            }
        }
        return result;
    }


    private boolean checkIpReg(String ip, long time) {
        long now = TimeUtil.currentTimeMillis();
        boolean check = false;
        if (!ipRegTimeMap.containsKey(ip)) {
            ipRegTimeMap.put(ip, time);
            // 放入map中
        } else {
            long lastTime = ipRegTimeMap.get(ip);
            if (lastTime == time) {
                check = true;
                // 同一个ip注册多次并且时间是一样的
            }
        }

        // 这次注册的时间小于10分钟内(主要防止发包的时间)
        if (now - time < TimeUtil.MIN_IN_MINILLS * 10) {
            Iterator<String> iterator = ipRegTimeMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                long val = ipRegTimeMap.get(key);
                if (now - val > TimeUtil.MIN_IN_MINILLS * 10) {
                    // 删除10分钟以前的记录
                    iterator.remove();
                }
            }
        }
        return check;

    }

    public String getMaskWord() {
        // initData.refreshMaskWord();
        List<String> list = SysInfManager.getInstance().getMaskWordList();
        StringBuffer sb = new StringBuffer();
        for (String wold : list) {
            sb.append(wold).append(",");
        }
        this.result = sb.toString();
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

//    public String getServerInfo() {
//        Map<String, Object> result = new HashMap<String, Object>();
//        try {
//            result.put("server", SysInfManager.getInstance().getServers());
//        } catch (Exception e) {
//            exception(result, "getServerInfo.exception", e);
//        }
//        this.result = JacksonUtil.writeValueAsString(result);
//        return StringResultType.RETURN_ATTRIBUTE_NAME;
//    }

    public String log() {
        GameBackLogger.SYS_LOG.info(JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    private User buildUser(RegInfo userInfo,User user) throws Exception {
        Server server = null;
        boolean shenhe = false;
        SyvcFilter syvcFilter = getFilterForSyvc(userInfo.getSyvc());
        if (syvcFilter != null && syvcFilter.getIsIosAudit() == 1) {
            shenhe = true;
        }

        long totalCount=(-userInfo.getUsedCards()+userInfo.getCards())/150+userInfo.getTotalCount();

        boolean loadFromCheckNet = true;
        String[] gameUrls=null;
        Room room;

        String bst = PropertiesCacheUtil.getValue("base_server_type",Constants.GAME_FILE);
        int baseServerType = StringUtils.isNotBlank(bst) ? NumberUtils.toInt(bst,1) : 1;

        if (shenhe) {
            server = SysInfManager.getInstance().getServer(userInfo.getEnterServer());
            if (server == null) {
                gameUrls = CheckNetUtil.loadGameUrl(userInfo.getEnterServer(), totalCount);
                if (gameUrls!=null) {
                    server = new Server();
                    server.setId(userInfo.getEnterServer());
                    server.setChathost(gameUrls[0]);
                    loadFromCheckNet = false;
                }
            }
        }else if(userInfo.getPlayingTableId() >= Constants.MIN_GOLD_ID){
            GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(userInfo.getPlayingTableId());
            if (goldRoom!=null&&NumberUtils.toInt(goldRoom.getCurrentState())<2){
                server = SysInfManager.getInstance().getServer(goldRoom.getServerId());
                if (server == null) {
                    gameUrls = CheckNetUtil.loadGameUrl(goldRoom.getServerId(), totalCount);
                    if (gameUrls!=null) {
                        server = new Server();
                        server.setId(goldRoom.getServerId());
                        server.setChathost(gameUrls[0]);
                        loadFromCheckNet = false;
                    }
                }
            }
        } else if (userInfo.getPlayingTableId() > 0 && (room = RoomDaoImpl.getInstance().queryRoom(userInfo.getPlayingTableId())) != null && room.getUsed() > 0) {
            server = SysInfManager.getInstance().getServer(room.getServerId());
            if (server == null) {
                gameUrls = CheckNetUtil.loadGameUrl(room.getServerId(), totalCount);
                if (gameUrls!=null) {
                    server = new Server();
                    server.setId(room.getServerId());
                    server.setChathost(gameUrls[0]);
                    loadFromCheckNet = false;
                }
            }
        } else {
            String loginExtend = userInfo.getLoginExtend();
            if (StringUtils.isNotBlank(loginExtend)){
                String matchId = JSON.parseObject(loginExtend).getString("match");
                if (StringUtils.isNotBlank(matchId)) {
                    HashMap<String,Object> matchBean = MatchDao.getInstance().selectOne(matchId);
                    if (matchBean != null) {
                        String state = String.valueOf(matchBean.get("currentState"));
                        if ("0".equals(state)||"1".equals(state)||state.startsWith("1_")){
                            server = SysInfManager.loadServer(CommonUtil.object2Int(matchBean.get("serverId")));
                        }
                    }
                }
            }
            if (server == null){
                server = SysInfManager.loadServer(userInfo.getPf(), baseServerType);
            }
        }

        if (server == null) {
            GameBackLogger.SYS_LOG.info("buildUser server is null-->uId:" + userInfo.getUserId() + " enterServer:" + userInfo.getEnterServer());
            server = SysInfManager.loadServer(userInfo.getPf(), baseServerType);
            if (server == null) {
                server = SysInfManager.loadServer(userInfo.getPf(), 0);
            }
        }
        if (user==null){
            user = new User();
        }
        user.setServerId(server.getId());
        user.setUsername(userInfo.getFlatId());
        user.setUserId(userInfo.getUserId());
        user.setPf(userInfo.getPf());
        user.setName(userInfo.getName());
        user.setSex(userInfo.getSex());
        user.setHeadimgurl(userInfo.getHeadimgurl());//
        if ("1".equals(PropertiesCacheUtil.getValue("gold_on_off",Constants.GAME_FILE))) {
            GoldUserInfo goldInfo = GoldDao.getInstance().selectGoldUserByUserId(userInfo.getUserId());
            if (goldInfo != null) {
                user.setGoldUserInfo(goldInfo);
            }
        }
        if (loadFromCheckNet) {
            gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
        }

        if (gameUrls!=null){
            user.setConnectHost(StringUtils.isNotBlank(gameUrls[0])?gameUrls[0]:server.getChathost());
            user.setConnectHost1(gameUrls[1]);
            user.setConnectHost2(gameUrls[2]);
        }else{
            user.setConnectHost(server.getChathost());
            user.setConnectHost1("");
            user.setConnectHost2("");
        }

//        user.setTotalCount(userInfo.getTotalCount());
        user.setTotalCount(totalCount);
        user.setSessCode(userInfo.getSessCode());
        user.setCards(userInfo.getCards() + userInfo.getFreeCards());
        user.setPlayTableId(userInfo.getPlayingTableId());
        user.setIsShowRankActivity(StaticDataManager.isShowRankActivity() ? 1 : 0);
        if (userInfo.getRegBindId() == PromotionAction.BINDID) {
            Long uid = userDao.getUserPromotionByUid(userInfo.getUserId());
            if (uid == null) {
                user.setIsShowButton(1);
            } else {
                user.setIsShowButton(0);
            }
        } else {
            user.setIsShowButton(0);
        }
        user.setActivityList(StaticDataManager.getActivityMsgs());
        int payBindId = userInfo.getPayBindId();
        user.setPayBindId(payBindId);
        if (payBindId <= 0 && StringUtils.isNotBlank(userInfo.getIdentity())) {
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_isBjdApp", "0"))) {
                user.setInviterPayBindId(BjdUtil.getPreBindAgency(userInfo));
            } else {
                WeiXinAuthorization authorization = AuthorizationDaoImpl.getInstance().queryWeiXinAuthorization(userInfo.getIdentity());
                if (authorization != null) {
                    RegInfo inviter = userDao.getUser(authorization.getInviterId());
                    if (inviter != null && inviter.getPayBindId() > 0) {
                        user.setInviterPayBindId(inviter.getPayBindId());
                    }
                }
            }
        }
        user.setRegBindId(userInfo.getRegBindId());
        user.setHasPay(user.getIsNewReg()==1?false:!UserDao.getInstance().isFirstPay(userInfo.getUserId(),1,9));


        String phoneNum = userInfo.getPhoneNum();
        //用于返回前端该账号是否绑定手机提示
        user.setPhoneNum(userInfo.getPhoneNum());
        return user;
    }

    /**
     * 构建User对象
     *
     * @param userInfo
     * @return
     */
    private User buildUser(RegInfo userInfo, List<Server> serverList) {
        User user = new User();
        user.setUsername(userInfo.getFlatId());
        user.setPf(userInfo.getPf());
        if (!StringUtils.isBlank(userInfo.getPlayedSid())) {
            if (serverList != null&&serverList.size()>0) {
                // 服务器列表
                List<Integer> servers = JacksonUtil.readValue(userInfo.getPlayedSid(), new TypeReference<List<Integer>>() {
                });
                // 服务器ID列表
                List<Integer> serverIdList = new ArrayList<Integer>();
                for (Server server : serverList) {
                    serverIdList.add(server.getId());
                }
                // 不在服务器列表内
                Iterator<Integer> iterator = servers.iterator();
                while (iterator.hasNext()) {
                    int serverId = iterator.next();
                    if (!serverIdList.contains(serverId)) {
                        iterator.remove();
                    }
                }
                user.setPlayedSid(JacksonUtil.writeValueAsString(servers));
            } else {
                user.setPlayedSid(userInfo.getPlayedSid());
            }

        } else {
            user.setPlayedSid("[]");
        }
        // servers = JacksonUtil.readValue(userInfo.getPlayedSid(), new
        // TypeReference<List<Integer>>() {
        // });
        user.setSessCode(userInfo.getSessCode());
        return user;
    }

    /**
     * 异常处理
     *
     * @param result
     * @param e
     */
    private void exception(Map<String, Object> result, String msg, Exception e) {
        result.put("code", 999);
        result.put("msg", "登录异常");
        GameBackLogger.SYS_LOG.error(msg, e);
    }

    /**
     * 检测用户名合法性
     *
     * @param username
     * @return
     */
    private boolean checkUserName(String username) {
        // String reg = "^[a-zA-Z0-9]\\w{0,17}$";
        // Pattern p1 = Pattern.compile(reg);
        // Matcher mat = p1.matcher(username);
        // return mat.find();
        return true;
    }

    /**
     * 加密密码
     *
     * @param source
     * @return
     */
    private String genPw(String source) {
        return MD5Util.getStringMD5(source + "sanguo_shangyou_2013");
    }

    /**
     * 生成session code
     *
     * @param username
     * @return
     */
    private String genSessCode(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(MathUtil.mt_rand(10000, 99999));
        sb.append(System.currentTimeMillis());
        return MD5Util.getStringMD5(sb.toString());
    }

    /**
     * 支付返还
     *
     * @return
     */
    public String payback() {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String makeid = getRequest().getParameter("makeid");
            // huodong_4_self 特殊平台id出现下划线
            String[] idArr = makeid.split("_");
            int num = 0;
            if (idArr.length > 3) {
                num = idArr.length - 3;
            }
            String flatid = idArr[0];
            for (int i = 1; i <= num; i++) {
                flatid = flatid + "_" + idArr[i];
            }

            if (StringUtils.isBlank(flatid)) {
                result.put("amout", 0);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            PayBack payback = userDao.getPayBackAmount(flatid);
            if (payback == null || payback.getIssent() != 0) {
                result.put("amout", 0);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            if (payback.getPayamout() > 0) {
                result.put("amout", payback.getPayamout());
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
        } catch (Exception e) {
            exception(result, "userAction--payback.exception", e);
            result.put("amout", 0);
        }
        this.result = JacksonUtil.writeValueAsString(result);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    /**
     * 支付返还确认标记修改
     *
     * @return
     */
    public String ackPayback() {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String makeid = getRequest().getParameter("makeid");
            // huodong_4_self
            String[] idArr = makeid.split("_");
            int num = 0;
            if (idArr.length > 3) {
                num = idArr.length - 3;
            }
            String flatid = idArr[0];
            for (int i = 1; i <= num; i++) {
                flatid = flatid + "_" + idArr[i];
            }
            String serverid = idArr[num + 1];

            if (StringUtils.isBlank(flatid) || StringUtils.isBlank(serverid)) {
                result.put("code", 1);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            PayBack payback = userDao.getPayBackAmount(flatid);
            if (payback == null || payback.getIssent() != 0) {
                result.put("code", 2);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            userDao.updatePayback(flatid, serverid);
            result.put("code", 0);
        } catch (Exception e) {
            exception(result, "userAction--payback.exception", e);
            result.put("code", 5);
        }
        this.result = JacksonUtil.writeValueAsString(result);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    /**
     * 验证cdk是否有效
     */
    public String verifyCdk() {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String cdkid = getRequest().getParameter("cdkid");

            if (StringUtils.isBlank(cdkid)) {
                result.put("code", 1);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            SystemCdk systemCdk = userDao.getSystemCdk(cdkid);
            if (systemCdk == null) {
                result.put("code", 2);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            if (!StringUtils.isBlank(systemCdk.getFlatid())) {
                result.put("code", 3);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            result.put("code", 0);
        } catch (Exception e) {
            exception(result, "userAction--payback.exception", e);
            result.put("code", 5);
        }
        this.result = JacksonUtil.writeValueAsString(result);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    /**
     * cdk标记修改
     **/
    public String ackCdk() {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String cdkid = getRequest().getParameter("cdkid");
            String makeid = getRequest().getParameter("makeid");
            String cdkType = getRequest().getParameter("cdkType");
            // huodong_4_self
            String[] idArr = makeid.split("_");
            int num = 0;
            if (idArr.length > 3) {
                num = idArr.length - 3;
            }
            String flatid = idArr[0];
            for (int i = 1; i <= num; i++) {
                flatid = flatid + "_" + idArr[i];
            }
            String serverid = idArr[num + 1];

            if (StringUtils.isBlank(cdkid) || StringUtils.isBlank(cdkType) || StringUtils.isBlank(flatid) || StringUtils.isBlank(serverid)) {
                result.put("code", 1);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            SystemCdk systemCdk = userDao.getSystemCdk(cdkid);
            if (systemCdk == null) {
                result.put("code", 2);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            if (!StringUtils.isBlank(systemCdk.getFlatid())) {
                result.put("code", 3);
                this.result = JacksonUtil.writeValueAsString(result);
                return StringResultType.RETURN_ATTRIBUTE_NAME;
            }
            userDao.updateSystemCdk(flatid, Integer.parseInt(serverid), Integer.parseInt(cdkType), cdkid);
            result.put("code", 0);
        } catch (Exception e) {
            exception(result, "userAction--payback.exception", e);
            result.put("code", 5);
        }
        this.result = JacksonUtil.writeValueAsString(result);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    private String getIpAddr(HttpServletRequest request) {
        return IpUtil.getIpAddr(request);
    }

    public UserDaoImpl getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public String getResult() {
        return result;
    }

    public void setServerDao(ServerDaoImpl serverDao) {
        this.serverDao = serverDao;
    }

    public ServerDaoImpl getServerDao() {
        return serverDao;
    }

    /**
     * 获取服务器过滤
     *
     * @param platform
     * @return
     */
    private ServerFilter getFilter(String platform) {
        ServerFilter myFilter = null;
        List<ServerFilter> filters = SysInfManager.getInstance().getServerFilter();
        for (ServerFilter filter : filters) {
            int eq = filter.getEquals();
            if (eq == 1) {
                if (filter.getPfCid().equals(platform)) {
                    myFilter = filter;
                    break;
                }
            } else {
                if (platform.startsWith(filter.getPfCid())) {
                    myFilter = filter;
                    break;
                }
            }

        }
        if (myFilter == null) {
            myFilter = filters.get(0);
        }

        return myFilter;
    }

    /**
     * 获取服务器版本号过滤
     *
     * @param syvc
     * @return
     */
    private SyvcFilter getFilterForSyvc(String syvc) {
        if (StringUtils.isBlank(syvc)) {
            return null;
        }
        SyvcFilter myFilter = null;
        List<SyvcFilter> filters = SysInfManager.getInstance().getSyvcFilter();
        for (SyvcFilter filter : filters) {
            if (filter.getSyvc().equals(syvc)) {
                myFilter = filter;
                break;
            }
        }
        return myFilter;
    }

    public String getFightValue() {
        String flatId = getRequest().getParameter("flatId");
        int fightVal = 0;
        if (!StringUtils.isBlank(flatId)) {
            Collection<Server> servers = SysInfManager.loadServers();
            int count = servers.size();
            int loopCondition = count > 5 ? (count - 5) : 0;
            Iterator<Server> it = servers.iterator();

            for (int i = count - 1; i >= loopCondition; i--) {
                Server server = it.next();

                Map<String, String> map = new HashMap<String, String>();
                map.put("type", "17");
                map.put("funcType", "20");
                map.put("flatId", flatId);
                map.put("sId", server.getId() + "");
                map.put("pf", "webyy");

                String result = GameUtil.send(server.getId(), map);
                // GameBackLogger.MONITOR_LOG.info(String.format("##UserAction.getFightValue flatId:%s, serverId:%d, url:%s::result==%s",
                // flatId,server.getId(),server.getHost(),result));
                if (!StringUtils.isBlank(result)) {
                    try {
                        JSONObject json = JSONObject.parseObject(result);
                        int value = json.getIntValue("fightVal");
                        if (value > fightVal) {
                            fightVal = value;
                        }
                    } catch (Exception e) {
                        GameBackLogger.SYS_LOG.error("##UserAction.getFightValue error!", e);
                    }
                }
            }
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("fightValue", fightVal + "");
        this.result = JacksonUtil.writeValueAsString(result);

        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }

    /*获取用户剩余抽奖次数*/
    public void getLotteryNum() throws Exception {
        Map<String, String> paramsMap = UrlParamUtil.getParameters(getRequest());

        String userId = paramsMap.get("userId");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
            try {
                int number = userDao.getLottyNum(Long.parseLong(userId));
                OutputUtil.output(MessageBuilder.newInstance().builder("code", 0).builder("lotteryNumber", number), getRequest(), getResponse(), null, false);
            } catch (Exception e) {
                if (e instanceof NumberFormatException) {
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1002).builder("message", "userId必须是数字！"), getRequest(), getResponse(), null, false);
                } else {
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1002).builder("lotteryNumber", 0), getRequest(), getResponse(), null, false);
                }

            }
        } else

        {
            OutputUtil.output(1003, "userId is blank", getRequest(), getResponse(), false);
        }

    }

    /*获取抽奖结果*/
    public void getLotteryResult() throws Exception {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        if (new Date().getTime() > sdf.parse("2027-12-31 00:00:00").getTime()) {
//            OutputUtil.output(MessageBuilder.newInstance().builder("code", 1005).builder("message", "活动时间已过，无法抽奖!"), getRequest(), getResponse(), null, false);
//            return;
//        }
        Map<String, String> paramsMap = UrlParamUtil.getParameters(getRequest());
        String userId = paramsMap.get("userId");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
            try {
                if (null == userDao.getUser(Long.parseLong(userId))) {
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1002).builder("message", "用户不存在，不能参与抽奖！"), getRequest(), getResponse(), null, false);
                    return;
                }
                int number = userDao.getLottyNum(Long.parseLong(userId));
                if (0 >= number) {
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1001).builder("message", "没有抽奖机会了，继续玩游戏赚取抽奖机会！"), getRequest(), getResponse(), null, false);

                } else {
                    int index = userDao.getLottyResult(Long.parseLong(userId));
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 0).builder("lotteryIndex", index), getRequest(), getResponse(), null, false);
                }
            } catch (Exception e) {
                if (e instanceof NumberFormatException) {
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1003).builder("message", "userId必须是数字!"), getRequest(), getResponse(), null, false);
                } else {
                    e.printStackTrace();
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1003).builder("message", "系统异常,抽奖失败!"), getRequest(), getResponse(), null, false);
                }

                return;
            }
        } else

        {
            OutputUtil.output(1003, "userId is blank", getRequest(), getResponse(), false);
        }

    }




    /**
     * 获取短信验证码（绑定,更改绑定）
     * @throws Exception
     * @author wwj
     */
    public void getVerifyCode() throws Exception {
        String phoneNum=getRequest().getParameter("phoneNum");
        if (!isPhoneNum(phoneNum)){
            OutputUtil.output(1402, "非法手机号", getRequest(), getResponse(), false);
            return;
        }
        if (userDao.loadUserByPhoneNum(phoneNum)!=null){
            OutputUtil.output(1403, "手机已绑定其他账号，请更换手机尝试", getRequest(), getResponse(), false);
            return;
        }
        long userId=Long.valueOf(getRequest().getParameter("userId"));
        RegInfo user = userDao.getUser(userId);
        if (!verifySessCode(user,userId))
            return;
        String pn=user.getPhoneNum();

        switch (getRequest().getParameter("functionType")){
            case "2":
                break;
            case "1"://绑定
                if(pn!=null&&!"".equals(pn)){
                    OutputUtil.output(1334, "您的账号已绑定手机", getRequest(), getResponse(), false);
                    return;
                }
                break;
        }

        UserMsgVerify msgVerify = userDao.getMsgVerifyByUid(userId);
        sendMsg(msgVerify,phoneNum,userId,null);
        OutputUtil.output(0, "获取成功", getRequest(), getResponse(), false);
    }

    /**
     * 获取短信验证码（解绑）
     * @throws Exception
     * @author wwj
     */
    public void getVerifyCode1() throws Exception {
        long  userId= Long.valueOf(getRequest().getParameter("userId"));
        RegInfo user = userDao.getUser(userId);
        if (!verifySessCode(user,userId))
            return;
        String pn=user.getPhoneNum();
        if(pn==null&&"".equals(pn)){
            OutputUtil.output(1336, "手机未绑定", getRequest(), getResponse(), false);
            return;
        }
        UserMsgVerify msgVerify = userDao.getMsgVerifyByUid(userId);
//        sendMsg(msgVerify,LoginUtil.decryptPhoneNumAES(user.getPhoneNum()),userId,null);
        String s = LoginUtil.decryptPhoneNumAES(user.getPhoneNum());
        if(isPhoneNum(s)){
            sendMsg(msgVerify,s,userId,null);
        }else {
            s = LoginUtil.decryptPhoneNumAES(s);
            sendMsg(msgVerify,s,userId,null);
        }
        OutputUtil.output(0, "解绑", getRequest(), getResponse(), false);
    }


    /**
     * 获取验证码(忘记密码)
     * @throws Exception
     * @author wwj
     */
    public void getVerifyCode2() throws Exception {
        String phoneNum=getRequest().getParameter("phoneNum");
        if (!isPhoneNum(phoneNum)){
            OutputUtil.output(1402, "非法手机号", getRequest(), getResponse(), false);
            return;
        }
        RegInfo user = userDao.loadUserByPhoneNum(phoneNum);
        if (user==null){
            OutputUtil.output(1403, "该手机未绑定任何账号", getRequest(), getResponse(), false);
            return;
        }
        String ip = getIpAddr(getRequest());
        UserMsgVerify msgVerify = userDao.getMsgVerifyByIp(ip);
        sendMsg(msgVerify,phoneNum,user.getUserId(),ip);
        OutputUtil.output(0, "获取成功", getRequest(), getResponse(), false);
    }


    /**
     * 验证 短信验证码
     * @throws Exception
     * @author wwj
     */
    public void verifyCode()throws Exception{
        try {
            String codeType = getRequest().getParameter("codeType");
            String verifyCode = getRequest().getParameter("verifyCode");
            UserMsgVerify msgVerify = null;
            Long userId = null;
            switch (codeType) {
                case "1"://一般验证，包括绑定
                case "2"://解绑
                case "3"://更改绑定
                    userId = Long.valueOf(getRequest().getParameter("userId"));
                    RegInfo user = userDao.getUser(userId);
                    if (!verifySessCode(user, userId))
                        return;
                    msgVerify = userDao.getMsgVerifyByUid(userId);
                    break;
                case "4"://忘记密码
                    msgVerify = userDao.getMsgVerifyByIp(getIpAddr(getRequest()));
                    userId = Long.valueOf(msgVerify.getUserId());
                    break;
            }
            if (msgVerify == null) {
                OutputUtil.output(1335, "验证码不存在", getRequest(), getResponse(), false);
                return;
            }
            if (System.currentTimeMillis() - msgVerify.getSendTime().getTime() > 600 * 1000 || !verifyCode.equals(msgVerify.getVerifyCode())) {
                //验证码超时或者验证失败
                OutputUtil.output(1333, "验证码超时或者验证失败", getRequest(), getResponse(), false);
                return;
            }
            Map<String, Object> map = new HashMap<>();
            switch (codeType) {
                case "1"://一般验证，包括绑定和忘记密码时使用
                case "3"://更改绑定
                case "4"://忘记密码
                    userDao.updateSmsFlag(userId, 1);
                    OutputUtil.output(0, "验证成功", getRequest(), getResponse(), false);
                    break;
                case "2"://解绑
                    map.put("phoneNum", null);
                    map.put("phonePw", "");
                    userDao.updateUser(userId, map);
                    userDao.deleteSms(userId);
                    OutputUtil.output(0, "解绑成功", getRequest(), getResponse(), false);
                    break;
            }
        }catch (Exception e){
            LOGGER.error("verifyCode|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            return;
        }
    }

    public void verifyPhonePw() throws Exception{
        long  userId= Long.valueOf(getRequest().getParameter("userId"));
        String password = getRequest().getParameter("password");
        String errMsg=verifyPw(password);
        if(errMsg!=null){
            OutputUtil.output(1337, errMsg, getRequest(), getResponse(), false);
            return;
        }
        RegInfo user = userDao.getUser(userId);
        if(user!=null&&genPw(password).equals(user.getPhonePw())){
            OutputUtil.output(0, "", getRequest(), getResponse(), false);
        }else {
            OutputUtil.output(1337, "密码错误", getRequest(), getResponse(), false);
        }
    }


    /**
     * 验证短信成功后上传password
     */
    public void uploadPassword() throws Exception{
        try {
            String uid = getRequest().getParameter("userId");
            Long userId;
            UserMsgVerify msgVerify;
            int isGetAward = 1;
            RegInfo user;
            if (uid == null || "".equals(uid) || "0".equals(uid)) {
                //Ip获取
                msgVerify = userDao.getMsgVerifyByIp(getIpAddr(getRequest()));
                userId = msgVerify.getUserId();
                user = userDao.getUser(userId);
            } else {
                //验证userId
                userId = Long.valueOf(uid);
                user = userDao.getUser(userId);
                if (!verifySessCode(user, userId))
                    return;
                msgVerify = userDao.getMsgVerifyByUid(userId);
                isGetAward = user.getIsReceiveBDAward();
            }
            if (msgVerify == null || msgVerify.getIsUse() == 0) {
                if (user.getPhoneNum() == null)
                    OutputUtil.output(1334, "操作失败，请重新尝试", getRequest(), getResponse(), false);
                return;
            }

            String phoneNum = msgVerify.getPhoneNum();
//        if(!isPhoneNum(phoneNum)){
//            OutputUtil.output(1402, "非法手机号", getRequest(), getResponse(), false);
//            return;
//        }
            String password = getRequest().getParameter("password");
            String errMsg = verifyPw(password);
            if (errMsg != null) {
                OutputUtil.output(1337, errMsg, getRequest(), getResponse(), false);
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("phoneNum", phoneNum);
            String md5pw = genPw(password);
            map.put("phonePw", md5pw);
            if (isGetAward == 0) {
                map.put("isReceiveBDAward", 1);
                GoldDao.getInstance().addUserGold(userId, 3000, 0, SourceType.bind_phone);
            }
            userDao.updateUser(userId, map);
            userDao.deleteSms(userId);
            if (isGetAward == 0)
                OutputUtil.output(1340, "绑定成功获得3000豆子", getRequest(), getResponse(), false);
            else
                OutputUtil.output(0, "成功设置密码", getRequest(), getResponse(), false);
        }catch (Exception e){
            LOGGER.error("uploadPassword|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            return;
        }
    }

    private boolean verifySessCode(RegInfo user,long userId){
        String sessCode=getRequest().getParameter("sessCode");
        if (!user.getSessCode().equals(sessCode)||user.getUserId()!=userId){
            OutputUtil.output(1332, "Identity is wrong", getRequest(), getResponse(), false);
            return false;
        }
        return true;
    }

    private void  sendMsg(UserMsgVerify msgVerify,String phoneNum,long userId,String ip) throws Exception{
        LOGIN_SYS.info("sendMsg0|"+userId);
        //至少过一分钟才能发送第二份短信验证
        if (!(msgVerify==null||System.currentTimeMillis() - msgVerify.getSendTime().getTime()>60*1000)) {
            OutputUtil.output(1332, "一分钟内只能获取一次短信", getRequest(), getResponse(), false);
            return;
        }
        Properties properties = InitData.keyValueProperties;
        Random rand=new Random();
        int code=rand.nextInt(900000)+100000;
        String result=SMSUtil.sendSMSNew(properties.getProperty("sms_appId"), properties.getProperty("sms_appKey"), phoneNum,
                ResourcesConfigsUtil.loadServerPropertyValue("smsConfig","快乐玩游戏"),code,28681);
        LOGIN_SYS.info("sendMsg1|"+userId+"|"+result);
        userDao.updateUserMsgVerify(userId,phoneNum,ip,code);
    }

    /**
     * 验证短信合法性
     * 手机登录密码设定规则
     * 仅用 A-Z  a-z  0-9
     * 区分大小写
     * 不能纯数字 不能纯字母
     * 位数在6-8 位
     * @return
     */
    private String verifyPw(String passworld){
        if (passworld.length()<6||passworld.length()>8){
            return "密码长度不符6-8位规则";
        }
        if (!passworld.matches("^[A-Za-z0-9]+$")){
            return "密码需数字和字母组合";
        }
        boolean flag1=false,flag2=false;
        for (int i = 0; i < passworld.length(); i++) {
            if(passworld.charAt(i)>=65){
                flag1=true;
            }else if (passworld.charAt(i)<=57){
                flag2=true;
            }
        }
        if (flag1&&flag2){
            return null;
        }else {
            return "密码不能纯数字或不能纯字母";
        }

    }

    private static boolean checkAccount(String account) {
        String reg = "^[a-zA-Z0-9]{8,16}$";
        return account.matches(reg);
    }

    private boolean isPhoneNum(String phoneNum){
        if (phoneNum==null||phoneNum.equals(""))
            return false;
        return phoneNum.matches("[0-9]{11}");
    }

    public String  helloworld() throws Exception{
        Map<String, String> map = new HashMap<String, String>();
        map.put("msg", "hello");
        this.result=JacksonUtil.writeValueAsString(map);
        return StringResultType.RETURN_ATTRIBUTE_NAME;
    }



    public void editUserMsg() throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(getRequest());
        LOGGER.info("params:{}", params);
        if (!checkSign(params)) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
            return;
        }

        long userId = NumberUtils.toLong(params.get("userId"),-1);
//        RegInfo user;
        RegInfo info = userDao.getUser(userId);
        if (userId<=0||info==null){
            OutputUtil.output(1, "UID错误", getRequest(), getResponse(), false);
            return;
        }
        String msg=params.get("msg");
        if(StringUtils.isBlank(msg)){
            OutputUtil.output(2, "信息错误", getRequest(), getResponse(), false);
            return;
        }
        JSONObject jsonMsg;
        try {
            jsonMsg = JSON.parseObject(msg);
            if (jsonMsg==null||jsonMsg.size()==0){
                OutputUtil.output(2, "信息错误", getRequest(), getResponse(), false);
                return;
            }
        }catch (Exception e){
            OutputUtil.output(2, "信息错误", getRequest(), getResponse(), false);
            return;
        }



        boolean authSucc = false;
        for (Map.Entry<String,Object> kv:jsonMsg.entrySet()){
            Integer msgType = Constants.loadUserMsgType(kv.getKey());
            if (msgType != null){
                UserExtend userExtend=new UserExtend();
                userExtend.setUserId(String.valueOf(userId));
                userExtend.setMsgType(msgType);
                userExtend.setMsgKey(kv.getKey());
                userExtend.setMsgValue(kv.getValue().toString());
                userExtend.setMsgDesc("");
                if (!"null".equals(userExtend.getMsgValue())&&!"".equals(userExtend.getMsgValue())){
                    userExtend.setMsgState("1");

                    int mt=msgType.intValue();
                    if (mt==1){//真实姓名
                        if (!userExtend.getMsgValue().matches("[\\u4e00-\\u9fa5]{2,4}")){
                            OutputUtil.output(3, "请输入正确的真实姓名", getRequest(), getResponse(), false);
                            return;
                        }
                    }else if (mt==2){//身份证
                        if (!userExtend.getMsgValue().matches("^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$")){
                            OutputUtil.output(3, "请输入有效的18位身份证号", getRequest(), getResponse(), false);
                            return;
                        }
                    }else if (mt==3){//手机号码
                        if (!userExtend.getMsgValue().matches("^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$")){
                            OutputUtil.output(3, "请输入正确的手机号码", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                }else{
                    userExtend.setMsgState("0");
                }
                userExtend.setCreatedTime(new Date());
                userExtend.setModifiedTime(userExtend.getCreatedTime());
                HashMap<String, Object> existData = UserDao.getInstance().queryUserExtend(String.valueOf(userId), 2);
                int inserted = UserDao.getInstance().saveUserExtend(userExtend);
                if (msgType == 2 && inserted > 0 && (existData == null || existData.size() == 0)) {
                    authSucc = true;
                }
            }
        }
        OutputUtil.output(0, "提交成功", getRequest(), getResponse(), false);
        //实名认证钻奖励
        if (authSucc) {
            int diamond = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "realNameAuthDiamond", 0);
            if (diamond > 0) {
                //查询用户是否认证过身份证
                Date now = new Date();
                UserMessage message = new UserMessage();
                message.setUserId(userId);
                message.setContent(TimeUtil.formatTime(now) + " " + "您在实名认证后获得:钻石x" + diamond);
                message.setTime(now);
                UserDao.getInstance().addUserCards(info, 0, diamond, 0, null, message, CardSourceType.realNameAuth);
            }
        }
    }

    public void queryUserMsg() throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(getRequest());
        LOGGER.info("params:{}", params);
        if (!checkSign(params)) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
            return;
        }

        long userId = NumberUtils.toLong(params.get("userId"),-1);
//        RegInfo user;
        if (userId<=0||userDao.getUser(userId)==null){
            OutputUtil.output(1, "UID错误", getRequest(), getResponse(), false);
            return;
        }

        String msgStr=params.get("msg");
        Object ret;
        if (StringUtils.isBlank(msgStr)){
            ret = UserDao.getInstance().queryUserExtend(params.get("userId"));
        }else{
            Integer msgType = Constants.loadUserMsgType(msgStr);
            if (msgType != null){
                ret = UserDao.getInstance().queryUserExtend(params.get("userId"),msgType);
            }else{
                OutputUtil.output(2, "暂不支持", getRequest(), getResponse(), false);
                return;
            }
        }

        OutputUtil.output(0, ret, getRequest(), getResponse(), false);
    }


    /**
     * 更新库内所有phoneNum
     */
    public void updateAllPhoneNumAES() throws Exception {
        int pageNo = 1;
        int pageSize = 10000;
        List<HashMap<String, Object>> list = userDao.loadAllUserPhoneNum(pageNo, pageSize);
        List<HashMap<String, Object>> updateList = new ArrayList<>();
        while (list != null && list.size() > 0) {
            for (HashMap<String, Object> data : list) {
                long userId = (long) data.get("userId");
                String phoneNum = (String) data.get("phoneNum");
                if (StringUtils.isBlank(phoneNum)) {
                    continue;
                }
                HashMap<String, Object> modify = new HashMap<>();
                String realPhone = "";
                if (isPhoneNum(phoneNum)) {
                    modify.put("phoneNum", LoginUtil.encryptPhoneNumAES(phoneNum));
                    realPhone = phoneNum;
                } else {
                    String decrypt1 = LoginUtil.decryptPhoneNumAES(phoneNum);
                    if (isPhoneNum(decrypt1)) {
                        continue;
                    }
                    String decrypt2 = LoginUtil.decryptPhoneNumAES(decrypt1);
                    if (!isPhoneNum(decrypt2)) {
                        continue;
                    }
                    modify.put("phoneNum", decrypt1);
                    realPhone = decrypt2;
                }
                if (modify.size() == 0) {
                    continue;
                }
                int ret = userDao.updateUser(userId, modify);
                if (ret <= 0) {
//                    HashMap<String, Object> clear = new HashMap<>();
//                    clear.put("userId", userId);
//                    clear.put("phoneNum", null);
//                    clear.put("phonePw", null);
//                    updateList.add(clear);
                    if (StringUtils.isNotBlank(realPhone)) {
                        List<Long> userIds = userDao.loadAllUserIdByPhoneNum(realPhone);
                        LogUtil.i("updateAllPhoneNumAES|error|" + userIds);
                    }
                }
            }
//            if (updateList.size() > 0) {
//                userDao.batchUpdateUser(updateList);
//            }
            if (list.size() < pageSize) {
                break;
            }
            pageNo++;
            list = userDao.loadAllUserPhoneNum(pageNo, pageSize);
        }
        OutputUtil.output(0, "加密手机号成功", getRequest(), getResponse(), false);
    }

    /**
     * 修改用户信息
     */
    public void updateUser() {
        long userId = 0;
        String nickName = "";
        String headimgurl = "";
        int sex = -1;
        String pw = "";
        String oldPw = "";
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            userId = NumberUtils.toLong(params.get("userId"), 0);
            sex = NumberUtils.toInt(params.get("sex"), -1);
            nickName = params.get("nickName");
            headimgurl = params.get("headimgurl");
            pw = params.get("pw");
            oldPw = params.get("oldPw");

            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
                return;
            } else if (!"self".equals(user.getPf())) {
                OutputUtil.output(-3, "非账号登录，不允许修改", getRequest(), getResponse(), false);
                return;
            }
            Map<String, Object> modify = new HashMap<>();
            UserExtendInfo userExtendInfo = userDao.getUserExtendinfByUserId(userId);

            // 修改昵称
            if (StringUtils.isNotBlank(nickName)) {
                nickName = nickName.trim();
                if (StringUtil.hasSqlInjection(nickName)) {
                    OutputUtil.output(1, "数据不合法：" + nickName, getRequest(), getResponse(), false);
                    return;
                }
                if (nickName.equals(user.getFlatId())) {
                    OutputUtil.output(2, "昵称不能与账号相同", getRequest(), getResponse(), false);
                    return;
                }
                int len = StringUtil.lengthOfNickName(nickName);
                if (len == 0 || len > 12) {
                    OutputUtil.output(3, "昵称过长", getRequest(), getResponse(), false);
                    return;
                }
                String filt = KeyWordsFilter.getInstance_1().filt(nickName);
                if (!nickName.equals(filt)) {
                    OutputUtil.output(4, "昵称不合法【" + filt + "】", getRequest(), getResponse(), false);
                    return;
                }
                if (userExtendInfo != null && userExtendInfo.getLastUpNameTime() != null) {
                    if (userExtendInfo.getLastUpNameTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS > System.currentTimeMillis()) {
                        String errorMsg = "下次可修改日期【" + TimeUtil.formatTime(new Date(userExtendInfo.getLastUpNameTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS)) + "】";
                        OutputUtil.output(5, errorMsg, getRequest(), getResponse(), false);
                        return;
                    }
                }
                modify.put("name", nickName);
            }

            // 修改性别
            if (sex == 1 || sex == 2) {
                modify.put("sex", sex);
            }

            // 修改头像
            if (StringUtils.isNotBlank(headimgurl)) {
                if (StringUtil.hasSqlInjection(headimgurl)) {
                    OutputUtil.output(1, "数据不合法：" + headimgurl, getRequest(), getResponse(), false);
                    return;
                }
                if (userExtendInfo != null && userExtendInfo.getLastUpHeadimgTime() != null) {
                    if (userExtendInfo.getLastUpHeadimgTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS > System.currentTimeMillis()) {
                        String errorMsg = "下次可修改日期【" + TimeUtil.formatTime(new Date(userExtendInfo.getLastUpHeadimgTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS)) + "】";
                        OutputUtil.output(6, errorMsg, getRequest(), getResponse(), false);
                        return;
                    }
                }
                modify.put("headimgurl", headimgurl);
            }

            // 修改密码
            if (StringUtils.isNotBlank(pw)) {
                if (StringUtils.isBlank(oldPw) || !genPw(oldPw).equals(user.getPw())) {
                    OutputUtil.output(1, "修改密码失败，请输入正确的原密码", getRequest(), getResponse(), false);
                    return;
                }
                modify.put("pw", genPw(pw));
            }

            if (modify.size() > 0) {
                int ret = userDao.updateUser(userId, modify);
                if (ret > 0) {
                    GameUtil.sendUpdateUser(user.getEnterServer(), userId, nickName, headimgurl, pw);
                    Map<String, Object> map = new HashMap<>();
                    if (modify.containsKey("name") || modify.containsKey("headimgurl")) {
                        if (userExtendInfo == null) {
                            userDao.insertUserExtendinf(userId, "", 0);
                        }
                        if (modify.containsKey("name")) {
                            map.put("lastUpNameTime", new Date());
                        }
                        if (modify.containsKey("headimgurl")) {
                            map.put("lastUpHeadimgTime", new Date());
                        }
                        userDao.updateUserExtendinf(userId, map);
                    }
                }
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("updateUser|succ|" + userId + "|" + nickName + "|" + headimgurl);
        } catch (Exception e) {
            OutputUtil.output(2, "系统错误：请联系管理员", getRequest(), getResponse(), false);
            LogUtil.e("updateUser|error|" + userId + "|" + nickName + "|" + headimgurl, e);
            return;
        }
    }

    public void getUserUpdateMsg() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("getUserUpdateMsg|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
                return;
            }
            MessageBuilder msg = MessageBuilder.newInstance().builder("code", 0);
            msg.builder("upNameTime", 0);
            msg.builder("upHeadimgTime", 0);
            UserExtendInfo userExtendInf = userDao.getUserExtendinfByUserId(userId);
            if (userExtendInf != null) {
                long now = System.currentTimeMillis();
                if (userExtendInf.getLastUpNameTime() != null && userExtendInf.getLastUpNameTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS > now) {
                    msg.builder("upNameTime", userExtendInf.getLastUpNameTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS - now);
                }
                if (userExtendInf.getLastUpHeadimgTime() != null && userExtendInf.getLastUpHeadimgTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS > now) {
                    msg.builder("upHeadimgTime", userExtendInf.getLastUpHeadimgTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS - now);
                }
            }
            OutputUtil.output(msg.toString(), getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            OutputUtil.output(2, "系统错误：请联系管理员", getRequest(), getResponse(), false);
            LogUtil.e("getUserUpdateMsg|error|", e);
            return;
        }
    }

    /**
     * 绑定账号
     * */
    public void bindAccount() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);

            if (!checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }

            long userId = NumberUtils.toLong(params.get("userId"), -1);
            String account = params.get("account");
            String pwd = params.get("password");
            if (userId <= 0 || StringUtils.isBlank(account) || StringUtils.isBlank(pwd)) {
                OutputUtil.output(-1, "参数错误", getRequest(), getResponse(), false);
                return;
            }

            if (!checkAccount(account)) {
                OutputUtil.output(-1, "账号不合法", getRequest(), getResponse(), false);
                return;
            }

            String errMsg = verifyPw(pwd);
            if (errMsg != null) {
                OutputUtil.output(1337, errMsg, getRequest(), getResponse(), false);
                return;
            }

            RegInfo user = userDao.getUserByAccName(account);
            if (user != null) {
                OutputUtil.output(-1, "该账号已存在", getRequest(), getResponse(), false);
                return;
            }

            user = userDao.getUser(userId);
            if (user == null) {
                OutputUtil.output(-1, "获取用户信息失败", getRequest(), getResponse(), false);
                return;
            }

            if (!StringUtils.isBlank(user.getAccName())) {
                OutputUtil.output(-1, "不能重复绑定账号", getRequest(), getResponse(), false);
                return;
            }

            Map<String, Object> modify = new HashMap<>();
            modify.put("accName", account);
            modify.put("accPwd", genPw(pwd));
            int ret = userDao.updateUser(userId, modify);
            if (ret <= 0) {
                OutputUtil.output(-1, "绑定失败", getRequest(), getResponse(), false);
                return;
            }

            OutputUtil.output(0, "绑定成功", getRequest(), getResponse(), false);

        }
        catch (Exception e) {
            OutputUtil.output(4, "系统错误：请联系管理员", getRequest(), getResponse(), false);
            LogUtil.e("bindAccount|error|", e);
        }

    }

    /**
     * 查询是否已绑定账号
     * */
    public void queryAccountBindStatus() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);

            if (!checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }

            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                OutputUtil.output(-1, "获取用户信息失败", getRequest(), getResponse(), false);
                return;
            }

            JSONObject json = new JSONObject();
            json.put("isBinded", StringUtils.isBlank(user.getAccName()) ? 0 : 1);

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        }
        catch (Exception e){
            LOGGER.error(e.getMessage(), e);
            OutputUtil.output(4, "系统错误：请联系管理员", getRequest(), getResponse(), false);
            LogUtil.e("queryAccountBindStatus|error|", e);
        }
    }

    public RegInfo checkSessCodeNew(long userId, String sessCode) throws Exception {
        if (sessCode == null) {
            return null;
        }
        RegInfo user = userDao.getUserForceMaster(userId);
        if (user == null || !sessCode.equals(user.getSessCode())) {
            return null;
        }
        return user;
    }
}
