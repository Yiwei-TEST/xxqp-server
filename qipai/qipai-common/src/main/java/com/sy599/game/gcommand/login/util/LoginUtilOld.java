package com.sy599.game.gcommand.login.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CacheUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.GoldPlayer;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.login.base.BaseSdk;
import com.sy599.game.gcommand.login.base.PwErrorMsg;
import com.sy599.game.gcommand.login.base.SdkFactory;
import com.sy599.game.gcommand.login.base.msg.User;
import com.sy599.game.gcommand.login.base.pfs.configs.PfSdkConfig;
import com.sy599.game.gcommand.login.base.pfs.configs.PfUtil;
import com.sy599.game.gcommand.login.base.pfs.qq.QQ;
import com.sy599.game.gcommand.login.base.pfs.qq.QqUtil;
import com.sy599.game.gcommand.login.base.pfs.weixin.Weixin;
import com.sy599.game.gcommand.login.base.pfs.weixin.WeixinUtil;
import com.sy599.game.gcommand.login.base.pfs.xianliao.Xianliao;
import com.sy599.game.gcommand.login.base.pfs.xianliao.XianliaoUtil;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.util.*;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.netty.NettyUtil;
import com.sy599.game.websocket.netty.SslUtil;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public final class LoginUtilOld {

    /**
     * 签名密钥
     */
    public static final String DEFAULT_KEY = "A7E046F99965FB3EF151FE3357DBE828";

    public static final String pf_phoneNum = "phoneLogin";
    public static final String pf_self = "self";

    private static String md5_key_phoneNum = "sanguo_shangyou_2013";

    private static String aes_key_phoneNum = "bjdlimsam2019%@)";

    /**
     * 用户登录
     */
    public static Map<String, Object> login(ChannelHandlerContext ctx, JSONObject params) {

        String ip = NettyUtil.userIpMap.get(ctx.channel());
        if (StringUtils.isBlank(ip)) {
            ip = NettyUtil.getRemoteAddr(ctx);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            RegInfo regInfo = null;
            String username = params.getString("u");
            String password = params.getString("ps");
            if (password == null) {
                password = "123456";
            }
            String platform = params.getString("p");
            // 整包更新用_版本号
            String vc = params.getString("vc");

            // 游戏客户端_版本号
            String syvc = params.getString("syvc");

            String os = params.getString("os");
            String mac = params.getString("mac");
            String deviceCode = params.getString("deviceCode");

            String channelId = "";

            boolean isNewReg = false;
            BaseSdk sdk;
            Map<String, Object> modify = null;
            String info = null;
            String uid = null;
            String unionId = null;
            if (StringUtils.isNotBlank(platform) && !pf_phoneNum.equals(platform) && !pf_self.equals(platform)) {// 使用平台SDK登录
                if (!PfUtil.isHasPf(platform) || (sdk = SdkFactory.getInst(platform, params)) == null) {
                    LogUtil.msgLog.info("platform::" + platform + " uid::" + uid + " login auth error");
                    result.put("code", 997);
                    result.put("msg", "没有找到该平台" + platform);
                    return result;
                }

                boolean isWx = (sdk instanceof Weixin);
                boolean isXl = isWx ? false : (sdk instanceof Xianliao);
                boolean isQQ = isWx ? false : (sdk instanceof QQ);

                int trMark;
                ThirdRelation thirdRelation;

                if (isWx || isXl || isQQ) {
                    uid = params.getString("openid");
                    String code = params.getString("code");
                    int loginMark = 0;
                    if (StringUtils.isBlank(uid) && StringUtils.isNotBlank(code)) {
                        PfSdkConfig pfSdkConfig = PfUtil.getConfig(platform);
                        if (pfSdkConfig == null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", 2019);
                            return map;
                        }

                        String miniProgram = isWx ? params.getString("miniProgram") : null;

                        if (StringUtils.isBlank(miniProgram)) {
                            JsonWrapper jsonWrapper = isXl ? XianliaoUtil.getAccessToken(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code)
                                    : WeixinUtil.getAccessToken(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code);
                            if (jsonWrapper != null) {
                                String access_token = jsonWrapper.getString("access_token");
                                sdk.setOpt("auth:" + access_token);
                                if (isWx) {
                                    sdk.setExt("openid:" + jsonWrapper.getString("openid"));
                                }
                                info = sdk.loginExecute();
                                uid = sdk.getSdkId();
                                if (StringUtils.isNotBlank(info) && jsonWrapper.isHas("refresh_token")) {
                                    String refresh_token = jsonWrapper.getString("refresh_token");
                                    jsonWrapper = new JsonWrapper(info);
                                    jsonWrapper.putString("refresh_token", refresh_token);
                                    info = jsonWrapper.toString();
                                }
                            }
                        } else {
                            JsonWrapper jsonWrapper = WeixinUtil.jscode2session(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code);
                            if (jsonWrapper != null) {
                                Weixin weixin = (Weixin) sdk;
                                weixin.setCode(code);
                                weixin.setSessionJson(jsonWrapper);

                                info = weixin.loginExecute();
                                uid = weixin.getSdkId();
                            }
                        }
                        loginMark = 1;
                    }

                    thirdRelation = UserRelationDao.getInstance().selectThirdRelation(uid, platform);
                    if (thirdRelation == null) {
                        if (loginMark != 1) {
                            sdk.setOpt("create");
                            info = sdk.loginExecute();
                            uid = sdk.getSdkId();
                        }
                        trMark = 1;
                    } else if (System.currentTimeMillis() - thirdRelation.getCheckedTime().getTime() >= 4 * 60 * 60 * 1000) {
                        trMark = 2;
                        if (loginMark != 1) {
                            sdk.setOpt("refresh");
                            info = sdk.loginExecute();
                            if (StringUtils.isNotBlank(uid) && !uid.equalsIgnoreCase(sdk.getSdkId())) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("code", 2018);
                                return map;
                            } else {
                                uid = sdk.getSdkId();
                            }
                        }
                        if (StringUtils.isNotBlank(uid)) {
                            thirdRelation = UserRelationDao.getInstance().selectThirdRelation(uid, platform);
                            if (thirdRelation != null) {
                                trMark = 3;
                            }
                        }
                    } else {
                        if (loginMark != 1) {
                            info = MessageBuilder.newInstance().builder("access_token", params.getString("access_token"))
                                    .builder("refresh_token", params.getString("refresh_token")).toString();
                        }
                        trMark = 0;
                    }

                    if (StringUtils.isBlank(info)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", 2018);
                        return map;
                    }
                } else {
                    info = sdk.loginExecute();
                    uid = sdk.getSdkId();
                    thirdRelation = UserRelationDao.getInstance().selectThirdRelation(uid, platform);
                    if (thirdRelation == null) {
                        trMark = 1;
                    } else {
                        trMark = 2;
                    }
                }

                // 验证成功
                if (StringUtils.isBlank(uid)) {
                    LogUtil.msgLog.info("platform::" + platform + " uid::" + uid + " login auth error");
                    result.put("code", 997);
                    result.put("msg", "没有找到该平台" + platform);
                    return result;
                }
                // 给每个从第三方平台进来的用户初始化一个唯一的密码
                password = "xsg_" + platform + "_pw_default_" + uid;

                if (trMark == 0 || trMark == 3) {
                    regInfo = UserDao.getInstance().getUser(thirdRelation.getUserId());
                } else {
                    if (isWx) {
                        if ("true".equals(ResourcesConfigsUtil.loadServerPropertyValue("weixin_openid"))) {
                            regInfo = UserDao.getInstance().getUser(uid, platform);
                        } else {
                            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(info);
                            unionId = jsonObject.getString("unionid");
                            if (StringUtils.isNotBlank(unionId)) {
                                regInfo = UserDao.getInstance().getUser(unionId, "weixin", uid, platform);
                            } else {
                                regInfo = UserDao.getInstance().getUser(uid, platform);
                            }
                        }
                    } else if (isXl) {
                        regInfo = UserDao.getInstance().getUser(uid, platform);
                    } else {
                        regInfo = UserDao.getInstance().getUser(uid, platform);
                    }
                }

                if (regInfo == null && StringUtils.isNotBlank(platform)) {
                    String bind_pf = params.getString("bind_pf");
                    if (StringUtils.isNotBlank(bind_pf)) {
                        String bind_access_token = params.getString("bind_access_token");
                        String bind_openid = params.getString("bind_openid");
                        String bind_fresh_token = params.getString("bind_fresh_token");
                        if (bind_pf.startsWith("weixin")) {
                            JsonWrapper bindMsg = WeixinUtil.getUserinfo(bind_access_token, bind_openid);
                            if (bindMsg != null && bindMsg.isHas("openid")) {
                                ThirdRelation tr = UserRelationDao.getInstance().selectThirdRelation(bindMsg.getString("openid"), bind_pf);
                                if (tr != null) {
                                    regInfo = UserDao.getInstance().getUser(tr.getUserId());
                                }
                            }
                            if (regInfo == null && platform.startsWith("xianliao")) {
                                // 闲聊注册需要绑定微信登录账号
                                result.put("code", 998);
                                result.put("msg", "闲聊登陆失败，请先使用微信登陆！");
                                return result;
                            }
                        } else if (bind_pf.startsWith("xianliao")) {
                            JsonWrapper bindMsg = XianliaoUtil.getUserinfo(bind_access_token);
                            if (bindMsg == null) {
                                PfSdkConfig pfSdkConfig = PfUtil.getConfig(bind_pf);
                                if (pfSdkConfig != null) {
                                    bindMsg = XianliaoUtil.refreshAccessToken(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), bind_fresh_token);
                                    if (bindMsg != null) {
                                        bindMsg = XianliaoUtil.getUserinfo(bindMsg.getString("access_token"));
                                    }
                                }
                            }
                            if (bindMsg != null) {
                                ThirdRelation tr = UserRelationDao.getInstance().selectThirdRelation(bindMsg.getString("openId"), bind_pf);
                                if (tr != null) {
                                    regInfo = UserDao.getInstance().getUser(tr.getUserId());
                                }
                            }
                        } else if (bind_pf.startsWith("qq")) {
                            PfSdkConfig pfSdkConfig = PfUtil.getConfig(bind_pf);
                            if (pfSdkConfig != null) {
                                JsonWrapper bindMsg = QqUtil.getUserinfo(pfSdkConfig.getAppId(), bind_access_token, bind_openid);
                                if (bindMsg != null) {
                                    ThirdRelation tr = UserRelationDao.getInstance().selectThirdRelation(bind_openid, bind_pf);
                                    if (tr != null) {
                                        regInfo = UserDao.getInstance().getUser(tr.getUserId());
                                    }
                                }
                            }
                        }
                    } else {
                        if (platform.startsWith("xianliao")) {
                            // 闲聊注册需要绑定微信登录账号
                            result.put("code", 998);
                            result.put("msg", "闲聊登陆失败，请先使用微信登陆！");
                            return result;
                        }
                    }

                }

                // 自动注册
                if (regInfo == null) {
                    long maxId;
                    if (Redis.isConnected()) {
                        String cacheKey = CacheUtil.loadStringKey(String.class, "user_max_id20180403");
                        if (RedisUtil.tryLock(cacheKey, 2, 2000)) {
                            try {
                                maxId = generatePlayerId();
                            } catch (Exception e) {
                                maxId = 0;
                                LogUtil.e("Exception:" + e.getMessage(), e);
                            } finally {
                                RedisUtil.unlock(cacheKey);
                            }
                        } else {
                            maxId = 0;
                        }
                    } else {
                        maxId = generatePlayerId();
                    }

                    if (maxId <= 0) {
                        result.put("code", 999);
                        result.put("msg", "登录异常,请稍后再试");

                        return result;
                    }

                    regInfo = new RegInfo();
                    String mangguoPf = "common";
                    if (unionId != null) {// 芒果跑得快渠道记录
                        LogUtil.msgLog.info("unionId:" + unionId);
                        MGauthorization mGauthorization = MGauthorizationDao.getInstance().getMGauthorization(unionId);
                        if (mGauthorization != null) {
                            mangguoPf = mGauthorization.getPf();
                            LogUtil.msgLog.info("mangguoPf:" + mangguoPf);
                        }
                    }
                    regInfo.setChannel(mangguoPf);
                    regInfo.setOs(os);
                    buildBaseUser(regInfo, platform, maxId);
                    sdk.createRole(regInfo, info);

                    if (regInfo.getPayBindId() > 0) {
                        Integer bindGiveRoomCards = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "bindGiveRoomCards");
                        if (bindGiveRoomCards != null && bindGiveRoomCards.intValue() > 0) {
                            regInfo.setFreeCards(regInfo.getFreeCards() + bindGiveRoomCards.intValue());
                            MessageUtil.sendMessage(false, true, UserMessageEnum.TYPE0, regInfo, "绑定邀请码" + regInfo.getPayBindId() + "成功，获得房卡x" + bindGiveRoomCards, null);
                        }
                    }

                    isNewReg = UserDao.getInstance().addUser(regInfo) > 0L;
                } else {
                    if (trMark == 0) {
                        modify = new HashMap<>();
                    } else {
                        modify = sdk.refreshRole(regInfo, info);
                    }
                }

                if (regInfo.getUserId() > 0 && StringUtils.isNotBlank(platform) && StringUtils.isNotBlank(uid)) {
                    if (trMark == 0) {
                    } else if (trMark == 1) {
                        UserRelationDao.getInstance().insert(new ThirdRelation(regInfo.getUserId(), platform, uid));
                    } else if (trMark == 2 || trMark == 3) {
                        UserRelationDao.getInstance().updateCheckedTime(thirdRelation.getKeyId().toString());
                    }
                }
            } else if (pf_phoneNum.equals(platform)) {
            } else if (pf_self.equals(platform)) {
            } else {
                platform = "self";
            }

            // 验证用户名和密码合法性
            if (regInfo == null && (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(platform))) {
                result.put("code", 995);
                result.put("msg", "账号或密码不合法");
                return result;
            }

            if (regInfo == null) {
                regInfo = UserDao.getInstance().getUser(username, platform);
                if (regInfo == null) {
                    // 用户不存在
                    result.put("code", 996);
                    result.put("msg", "账号不存在");
                    LogUtil.msgLog.error("code 996-->user not exist:" + username);
                    return result;
                }
            }

            if (regInfo.getUserState() != null && regInfo.getUserState().intValue() == 0) {
                result.put("code", 604);
                result.put("msg", "您已被禁止登录，请联系所在群主或客服！");
                return result;
            }

            if (pf_self.equals(platform) || pf_phoneNum.equals(platform)) {
                PwErrorMsg pwErrorMsg = getPwErrorMsg(regInfo.getUserId());
                String limitMsg = pwErrorMsg.limitMsg();
                if (StringUtils.isNotBlank(limitMsg)) {
                    result.put("code", 996);
                    result.put("msg", "连续输错密码，请在 " + limitMsg + " 秒后再登录");
                    return result;
                }
                String md5Pw = genPw(password);
                boolean checkPasswd = true;
                if (pf_phoneNum.equals(platform)) {
                    checkPasswd = md5Pw.equals(regInfo.getPhonePw());
                } else if (pf_self.equals(platform)) {
                    checkPasswd = md5Pw.equals(regInfo.getPw());
                }
                if (!checkPasswd) {
                    // 密码错误
                    result.put("code", 994);
                    result.put("msg", "密码错误");
                    addPwErrorMsg(regInfo.getUserId(), pwErrorMsg);
                    return result;
                }
            }

            //如果是手机登录username存的是手机号码，需要替换成username
            if (pf_phoneNum.equals(platform)) {
                username = regInfo.getName();
            }

            String gamevc = params.getString("gamevc");
            boolean checkVc = false;
            String versionCheck = ResourcesConfigsUtil.loadServerPropertyValue("login_version_" + platform);
            if (versionCheck == null) {
                versionCheck = ResourcesConfigsUtil.loadServerPropertyValue("login_version_all");
            }
            if (StringUtils.isNotBlank(versionCheck) && StringUtils.isNotBlank(gamevc)) {
                checkVc = checkVersion(versionCheck, 1, gamevc, 1);
            }
            if (checkVc) {
                // 版本错误
                result.put("code", 2017);
                result.put("msg", "有新的更新内容，请退出重登！");
                return result;
            }

            if (modify == null) {
                modify = new HashMap<>();
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
            }
            if (!StringUtils.isBlank(mac) && !mac.equals(regInfo.getMac())) {
                modify.put("mac", mac);
            }
            if (!StringUtils.isBlank(deviceCode) && !deviceCode.equals(regInfo.getSessCode())) {
                modify.put("deviceCode", deviceCode);
            }
            if (!StringUtils.isBlank(gamevc) && !gamevc.equals(regInfo.getSyvc())) {
                modify.put("syvc", gamevc);
            }
            int totalCount = checkTotalCount(regInfo);
            if (regInfo.getTotalCount() != totalCount) {
                modify.put("totalCount", totalCount);
            }
            // 检测房间号
            int servserId = 0;
            if (regInfo.getPlayingTableId() != 0) {
                RoomBean room = TableDao.getInstance().queryUsingRoom(regInfo.getPlayingTableId());
                if (room != null) {
                    servserId = room.getServerId();
                }

                if (servserId != regInfo.getEnterServer()) {
                    regInfo.setEnterServer(servserId);
                    modify.put("enterServer", servserId);
                }
                if (servserId == 0) {
                    LogUtil.msgLog.info("playingTableId0|3|" + regInfo.getUserId() + "|" + regInfo.getEnterServer() + "|" + regInfo.getPlayingTableId());
                    regInfo.setPlayingTableId(0);
                    modify.put("playingTableId", 0);
                }
            }

            result.put("isIosAudit", 0);

            UserDao.getInstance().updateUser(regInfo, modify);

            if (StringUtils.isNotBlank(info) && info.contains("access_token") && StringUtils.isNotBlank(uid)) {
                JsonWrapper jsonWrapper = new JsonWrapper(info);
                result.put("access_token", jsonWrapper.getString("access_token"));
                result.put("refresh_token", jsonWrapper.getString("refresh_token"));
                result.put("openid", uid);
                result.put("pf", platform);
            }
            // 登录成功
            result.put("code", 0);

            result.put("notices", Collections.EMPTY_LIST);

            String systemMessage = NoticeDao.getInstance().loadSystemNotice();
            if (!StringUtils.isBlank(systemMessage)) {
                result.put("message", systemMessage);
            }

            String timeRemoveBindStr = ResourcesConfigsUtil.loadServerPropertyValue("periodRemoveBind");
            if (!StringUtils.isBlank(timeRemoveBindStr) && !"0".equals(timeRemoveBindStr)) {
                // 七天没玩并且绑码超过七天，解绑
                if (regInfo.getPayBindId() > 0 && TimeUtil.apartDays(regInfo.getPayBindTime(), new Date()) > 7 && regInfo.getLastPlayTime() != null && TimeUtil.apartDays(regInfo.getLastPlayTime(), new Date()) > 7) {
                    int res = autoRemoveBind(regInfo);
                    if (res == 1) {
                        regInfo.setPayBindId(0);
                    }
                }
            }
            User user = new User();
            user.setIsNewReg(isNewReg ? 1 : 0);
            if (platform != null) {
                if (platform.startsWith("weixin")) {
                    String name = new JsonWrapper(regInfo.getLoginExtend()).getString("wx");
                    if (name != null)
                        regInfo.setName(name);
                } else if (platform.startsWith("xianliao")) {
                    String name = new JsonWrapper(regInfo.getLoginExtend()).getString("xl");
                    if (name != null)
                        regInfo.setName(name);
                } else if (platform.startsWith("qq")) {
                    String name = new JsonWrapper(regInfo.getLoginExtend()).getString("qq");
                    if (name != null)
                        regInfo.setName(name);
                }
            }

            user = buildUser(regInfo, user, ctx);

            result.put("user", user);
            result.put("currTime", TimeUtil.currentTimeMillis());

            int payBindId = regInfo.getPayBindId();

            HashMap<String, Object> agencyInfo = null;
            if (payBindId > 0) {
                agencyInfo = RoomCardDao.getInstance().queryAgencyByAgencyId(payBindId);
                if (agencyInfo != null) {
                    Map<String, Object> retMap = new HashMap<>();
                    retMap.put("agencyId", agencyInfo.get("agencyId"));
                    retMap.put("agencyWechat", agencyInfo.get("agencyWechat"));
                    retMap.put("agencyPhone", agencyInfo.get("agencyPhone"));
                    retMap.put("agencyPf", agencyInfo.get("pf"));

                    result.put("agencyInfo", retMap);
                }
            }

            // 自有平台渠道号
            if (!StringUtils.isBlank(channelId)) {
                result.put("channelid", channelId);
            }

            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("open_user_relation"))) {
                String gameCode = params.getString("gameCode");
                if (org.apache.commons.lang3.StringUtils.isBlank(gameCode)) {
                    if (platform.startsWith("weixin") && platform.length() > 6) {
                        gameCode = platform.substring(6);
                    } else {
                        gameCode = platform;
                    }
                }
                String userId = String.valueOf(regInfo.getUserId());
                UserRelation relation = UserRelationDao.getInstance().select(gameCode, userId);
                if (relation == null) {
                    relation = new UserRelation();
                    relation.setGameCode(gameCode);
                    relation.setUserId(userId);
                    relation.setRegPf(platform);
                    relation.setRegTime(new Date());
                    relation.setLoginPf(platform);
                    relation.setLoginTime(relation.getRegTime());
                    UserRelationDao.getInstance().insert(relation);
                } else {
                    UserRelationDao.getInstance().update(relation.getKeyId().toString(), platform, new Date());
                }

                if (agencyInfo != null && agencyInfo.size() > 0) {
                    String agencyPf = String.valueOf(agencyInfo.get("pf"));
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(agencyPf) && (!"null".equalsIgnoreCase(agencyPf))) {
                        gameCode = gameCode + "_" + agencyPf;
                        relation = UserRelationDao.getInstance().select(gameCode, userId);
                        if (relation == null) {
                            relation = new UserRelation();
                            relation.setGameCode(gameCode);
                            relation.setUserId(userId);
                            relation.setRegPf(platform);
                            relation.setRegTime(new Date());
                            relation.setLoginPf(platform);
                            relation.setLoginTime(relation.getRegTime());
                            UserRelationDao.getInstance().insert(relation);
                        } else {
                            UserRelationDao.getInstance().update(relation.getKeyId().toString(), platform, new Date());
                        }
                    }
                }

            }
        } catch (Exception e) {
            LogUtil.errorLog.error("login.exception:" + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 加密密码
     *
     * @param source
     * @return
     */
    public static String genPw(String source) {
        return MD5Util.getMD5String(source + "sanguo_shangyou_2013");
    }

    /**
     * 生成session code
     *
     * @param username
     * @return
     */
    public static String genSessCode(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(UUID.randomUUID().toString());
        return MD5Util.getMD5String(sb.toString());
    }

    private static long generatePlayerIdOld() throws Exception {
        synchronized (LoginUtil.class) {
            long maxId = UserDao.getInstance().getMaxId();
            long min_player_id = Long.parseLong(ResourcesConfigsUtil.loadServerPropertyValue("min_player_id", "100000"));
            if (maxId < min_player_id) {
                maxId = min_player_id;
            }
            maxId++;
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("withFilterUserId", "0"))) {
                while (filterUserId(maxId)) {
                    maxId++;
                }
            }
            return maxId;
        }
    }

    /**
     * 新方法：随机生成
     *
     * @return
     * @throws Exception
     */
    private static long generatePlayerId() throws Exception {
        synchronized (LoginUtil.class) {
            long start = System.currentTimeMillis();
            Random rnd = new Random();
            long userId = randomUserId(rnd);
            int count = 1;
            while (!isUserIdOK(userId)) {
                userId = randomUserId(rnd);
                count++;
            }
            LogUtil.msgLog.info("generatePlayerId|" + userId + "|" + (System.currentTimeMillis() - start) + "|" + start + "|" + count);
            return userId;
        }
    }

    private static final int minRandomId = 120000;
    private static final int maxRandomId = 9880000;

    private static long randomUserId(Random rnd) {
        return minRandomId + rnd.nextInt(maxRandomId);
    }

    private static boolean isUserIdOK(long userId) {
        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("withFilterUserId", "0"))) {
            if (filterUserId(userId)) {
                return false;
            }
        }
        RegInfo regInfo = UserDao.getInstance().selectUserByUserId(userId);
        if (regInfo != null) {
            return false;
        }
        return true;
    }

    /**
     * 过滤(2017.04.11添加)<br/>
     * 4A/5A/6A，如：102222、85555<br/>
     * ABCD/ABCDE/ABCDEF，如：201234、56789<br/>
     * 3A/3B，如：111222<br/>
     *
     * @param userId
     * @return
     */
    private static boolean filterUserId(long userId) {
        String userIdStr = String.valueOf(userId);
        if (userIdStr.length() >= 4) {
            int count = 1;
            int temp = userIdStr.charAt(userIdStr.length() - 1) - userIdStr.charAt(userIdStr.length() - 2);

            switch (temp) {
                case 0:
                    boolean isAAABBB = false;
                    for (int i = userIdStr.length() - 2; i >= 1; i--) {
                        if (userIdStr.charAt(i) - userIdStr.charAt(i - 1) == 0) {
                            count++;
                            if (count >= 3 || (isAAABBB && count >= 2)) {
                                return true;
                            }
                        } else {
                            if (count >= 2) {
                                count = 0;
                                isAAABBB = true;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                case 1:
                    for (int i = userIdStr.length() - 2; i >= 1; i--) {
                        if (userIdStr.charAt(i) - userIdStr.charAt(i - 1) == 1) {
                            count++;
                            if (count >= 3) {
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                    break;
                case -1:
                    for (int i = userIdStr.length() - 2; i >= 1; i--) {
                        if (userIdStr.charAt(i) - userIdStr.charAt(i - 1) == -1) {
                            count++;
                            if (count >= 3) {
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                    break;
                default:
                    return false;
            }

        }
        return false;
    }

    private static void buildBaseUser(RegInfo regInfo, String platform, long maxId) {
        regInfo.setPf(platform);
        regInfo.setUserId(maxId);
        regInfo.setLoginDays(1);
        Integer giveRoomCards = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "giveRoomCards");
        regInfo.setFreeCards(giveRoomCards == null ? 0 : giveRoomCards.longValue());
        regInfo.setPlayedSid("[]");
        regInfo.setConfig("1,1");
    }

    /**
     * 检查玩家局数
     *
     * @param regInfo
     * @return
     */
    private static int checkTotalCount(RegInfo regInfo) {
        if (!StringUtils.isBlank(regInfo.getExtend())) {
            JsonWrapper wrapper = new JsonWrapper(regInfo.getExtend());
            String val5 = wrapper.getString(5);
            String val6 = wrapper.getString(6);
            int total5 = 0;
            if (!StringUtils.isBlank(val5)) {
                total5 = split(val5);
            }
            int total6 = 0;
            if (!StringUtils.isBlank(val6)) {
                total6 = split(val6);
            }
            return total5 + total6;
        }
        return 0;

    }

    /**
     * 分解出局数
     *
     * @param val
     * @return
     */
    private static int split(String val) {
        int total = 0;
        String[] values = val.split(";");
        for (String value : values) {
            String[] _values = value.split(",");
            if (_values.length < 2) {
                continue;
            }
            int valInt = Integer.parseInt(_values[1]);
            total += valInt;
        }
        return total;
    }

    private static User buildUser(RegInfo userInfo, User user, ChannelHandlerContext ctx) throws Exception {
        Server server = null;

        //局数+充值》》》用于用户分级
        //((-usedCards+cards)/150+totalCount)
        long totalCount = (-userInfo.getUsedCards() + userInfo.getCards()) / 150 + userInfo.getTotalCount();

        boolean loadFromCheckNet = true;
        String[] gameUrls = null;
        RoomBean room = null;

        if (userInfo.getPlayingTableId() > 0 && (room = TableDao.getInstance().queryRoom(userInfo.getPlayingTableId())) != null && room.getUsed() > 0) {
            server = ServerManager.loadServer(room.getServerId());
            if (server == null) {
                gameUrls = CheckNetUtil.loadGameUrl(room.getServerId(), totalCount);
                if (gameUrls != null) {
                    server = new Server();
                    server.setId(room.getServerId());
                    if (gameUrls[0].startsWith("ws:")) {
                        server.setChathost(gameUrls[0]);
                    } else if (gameUrls[0].startsWith("wss:")) {
                        server.setWssUri(gameUrls[0]);
                    }
                    loadFromCheckNet = false;
                }
            }
        } else if (userInfo.getPlayingTableId() > 0 && room == null) {
            BaseTable table = TableManager.getInstance().getTable(user.getPlayTableId());
            int serverId = -1;
            if (table != null) {
                serverId = GameServerConfig.SERVER_ID;
                user.setPlayType(table.getPlayType());
            } else {
                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(userInfo.getPlayingTableId());
                if (goldRoom != null && !goldRoom.isOver()) {
                    serverId = goldRoom.getServerId();
                    GoldRoomConfig goldRoomConfig = GoldRoomUtil.getGoldRoomConfig(goldRoom.getConfigId());
                    if (goldRoomConfig != null) {
                        user.setPlayType(goldRoomConfig.getPlayType());
                    }
                }

            }
            server = ServerManager.loadServer(serverId);
        } else {
            String loginExtend = userInfo.getLoginExtend();
            if (StringUtils.isNotBlank(loginExtend)) {
                String matchId = JSON.parseObject(loginExtend).getString("match");
                if (StringUtils.isNotBlank(matchId)) {
                    MatchBean matchBean = MatchDao.getInstance().selectOne(matchId);
                    if (matchBean != null && !JjsUtil.isOver(matchBean)) {
                        server = ServerManager.loadServer(matchBean.getServerId().intValue());
                    }
                }
            }
            if (server == null) {
                server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
            }
        }

        if (user == null) {
            user = new User();
        }
        user.setServerId(server.getId());
        user.setUsername(userInfo.getFlatId());
        user.setUserId(userInfo.getUserId());
        user.setPf(userInfo.getPf());
        user.setName(userInfo.getName());
        user.setSex(userInfo.getSex());
        user.setHeadimgurl(userInfo.getHeadimgurl());//
        if ("1".equals(GoldConstans.isGoldSiteOpen())) {
            GoldPlayer goldInfo = GoldDao.getInstance().selectGoldUserByUserId(userInfo.getUserId());
            if (goldInfo != null) {
                user.setGoldUserInfo(goldInfo);
            }
        }

        boolean useSsl = SslUtil.hasSslHandler(ctx);

        if (loadFromCheckNet) {
            gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
        }

        if (gameUrls == null) {
            user.setConnectHost(useSsl ? server.getWssUri() : server.getChathost());
            user.setConnectHost1("");
            user.setConnectHost2("");
        } else {
            String url0;
            if (useSsl) {
                url0 = (StringUtils.isNotBlank(gameUrls[0]) && gameUrls[0].startsWith("wss:")) ? gameUrls[0] : server.getWssUri();
            } else {
                url0 = (StringUtils.isNotBlank(gameUrls[0]) && gameUrls[0].startsWith("ws:")) ? gameUrls[0] : server.getChathost();
            }

            user.setConnectHost(url0);
            user.setConnectHost1(gameUrls[1]);
            user.setConnectHost2(gameUrls[2]);
        }

//        user.setTotalCount(userInfo.getTotalCount());
        user.setTotalCount(totalCount);
        user.setSessCode(userInfo.getSessCode());
        user.setCards(userInfo.getCards() + userInfo.getFreeCards());
        user.setPlayTableId(userInfo.getPlayingTableId());

        if (user.getPlayTableId() > 0 && user.getPlayType() == 0) {
            if (room != null) {
                user.setPlayType(room.getType());
            } else {
                BaseTable table = TableManager.getInstance().getTable(user.getPlayTableId());
                if (table != null) {
                    user.setPlayType(table.getPlayType());
                } else {
                    GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(user.getPlayTableId());
                    if (goldRoom != null) {
                        GoldRoomConfig goldRoomConfig = GoldRoomUtil.getGoldRoomConfig(goldRoom.getConfigId());
                        if (goldRoomConfig != null) {
                            user.setPlayType(goldRoomConfig.getPlayType());
                        }
                    }
                }
            }
        }

        int payBindId = userInfo.getPayBindId();
        user.setPayBindId(payBindId);
        user.setRegBindId(userInfo.getRegBindId());
        user.setHasPay(user.getIsNewReg() == 1 ? false : !UserDao.getInstance().isFirstPay(userInfo.getUserId(), 1, 9));

        user.setPayBindId(payBindId);
        if (payBindId <= 0 && StringUtils.isNotBlank(userInfo.getIdentity())) {
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_isBjdApp", SharedConstants.SWITCH_DEFAULT_OFF))) {
                user.setInviterPayBindId(BjdUtil.getPreBindAgency(userInfo));
            }
        }
        String phoneNum = userInfo.getPhoneNum();
        //用于返回前端该账号是否绑定手机提示
        if (StringUtils.isNotBlank(phoneNum)) {
            if (!isPhoneNum(phoneNum)) {
                phoneNum = decryptPhoneNumAES(phoneNum);
            }
            if (isPhoneNum(phoneNum)) {
                user.setPhoneNum(phoneNum);
            }
        }
        user.setIp(userInfo.getIp());
        return user;
    }

    public final static boolean checkVersion(String serverVersion, int serverIdx, String clientVersion, int clientIdx) {
        boolean result = false;
        int idxS1 = serverVersion.indexOf(".", serverIdx);
        int idxC1 = clientVersion.indexOf(".", clientIdx);
        if (idxS1 > 0 && idxC1 > 0) {
            int valS1 = NumberUtils.toInt(serverVersion.substring(serverIdx, idxS1), -1);
            int valC1 = NumberUtils.toInt(clientVersion.substring(clientIdx, idxC1), -1);
            if (valS1 >= 0 && valC1 >= 0) {
                if (valS1 > valC1) {
                    result = true;
                } else if (valS1 < valC1) {
                    result = false;
                } else {
                    return checkVersion(serverVersion, idxS1 + 1, clientVersion, idxC1 + 1);
                }
            }
        } else if (idxS1 > 0) {
            result = true;
        } else if (idxC1 > 0) {
            result = false;
        } else if (idxS1 == -1 && idxC1 == -1) {
            idxS1 = serverVersion.lastIndexOf(".");
            idxC1 = clientVersion.lastIndexOf(".");
            if (idxS1 > 0 && idxC1 > 0) {
                result = NumberUtils.toInt(serverVersion.substring(idxS1 + 1), -1) > NumberUtils.toInt(clientVersion.substring(idxC1 + 1), -1);
            } else if (NumberUtils.isDigits(serverVersion) && NumberUtils.isDigits(clientVersion)) {
                result = NumberUtils.toInt(serverVersion, -1) > NumberUtils.toInt(clientVersion, -1);
            }
        }
        return result;
    }

    private static int autoRemoveBind(RegInfo regInfo) {
        int res = UserDao.getInstance().removeBindInfo(regInfo);
        if (res == 1) {
            // 添加解绑记录
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", regInfo.getUserId());
            paramMap.put("agencyId", regInfo.getPayBindId());
            paramMap.put("createUserId", regInfo.getUserId());
            paramMap.put("createTime", TimeUtil.formatTime(new Date()));
            paramMap.put("bindType", 1);
            UserDao.getInstance().insertRBInfo(paramMap);
        }
        return res;
    }

    /**
     * 加密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String encryptPhoneNumMD5(String phoneNum) {
        return MD5Util.getMD5String(phoneNum + md5_key_phoneNum);
    }

    public static void setMd5KeyPhoneNum(String md5Key) {
        if (StringUtils.isNotBlank(md5Key)) {
            md5_key_phoneNum = md5Key;
        }
    }

    public static void setAESKeyPhoneNum(String aesKey) {
        if (StringUtils.isNotBlank(aesKey) && aesKey.length() == 16) {
            aes_key_phoneNum = aesKey;
        }
    }

    /**
     * AES加密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String encryptPhoneNumAES(String phoneNum) {
        return AESUtil.encrypt(phoneNum, aes_key_phoneNum);
    }

    /**
     * AES 解密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String decryptPhoneNumAES(String phoneNum) {
        return AESUtil.decrypt(phoneNum, aes_key_phoneNum);
    }

    public static boolean isPhoneNum(String phoneNum) {
        if (phoneNum == null || phoneNum.equals(""))
            return false;
        return phoneNum.matches("[0-9]{11}");
    }

    public static int addPwErrorMsg(long userId, PwErrorMsg msg) {
        try {
            msg.add();
            return UserDao.getInstance().updatePwErrorMsg(userId, msg.toString());
        } catch (Exception e) {
            LogUtil.errorLog.error("savePwErrorMsg|error|" + userId, e);
        }
        return 0;
    }

    public static PwErrorMsg getPwErrorMsg(long userId){
        String pwErrorMsgString = UserDao.getInstance().loadPwErrorMsg(userId);
        PwErrorMsg pwErrorMsg = PwErrorMsg.init(pwErrorMsgString);
        return pwErrorMsg;
    }

    public static void main(String[] args) {

        String phoneNum = "17775850883";
        System.out.println(encryptPhoneNumMD5(phoneNum));


        String encrypt = encryptPhoneNumAES(phoneNum);
        System.out.println("AES|encrypt|" + phoneNum + "|" + encrypt);

        String decrypt = decryptPhoneNumAES(encrypt);
        System.out.println("AES|decrypt|" + encrypt + "|" + decrypt);


        decrypt = decryptPhoneNumAES("4GpJWwWz/FuxwxRk1058Lw==");
        System.out.println("AES|decrypt|" + encrypt + "|" + decrypt);


    }
}
