package com.sy.sanguo.game.action;

import com.sy.mainland.util.OutputUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.game.bean.redbag.RedBagConfig;
import com.sy.sanguo.game.bean.redbag.RedBagInfo;
import com.sy.sanguo.game.bean.redbag.SelfRedBagReceiveRecord;
import com.sy.sanguo.game.bean.redbag.UserRedBagRecord;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.db.dao.RedBagInfoDao;
import com.sy.sanguo.game.pdkuai.db.dao.UserRedBagRecordDao;
import com.sy.sanguo.game.redpack.*;
import com.sy.sanguo.game.redpack.MessageUtil;
import com.sy599.sanguo.util.JacksonUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.TimeUtil;
import com.sy599.sanguo.util.TimeUtil1;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 微信红包提现接口
 * 只需配置  公众号后台服务器回调地址配置 以及  WeixinRedbagConfig 类相关参数配置即可
 */
public class RedBagReceiveAction extends SupportAction {

    public String execute() throws Exception {
        return null;
    }

    /**
     * 小甘瓜分现金红包活动提现
     * @throws Exception
     */
    public void withDraw() throws Exception {
        String signature = request.getParameter("signature");// 微信加密签名
        String timestamp = request.getParameter("timestamp");// 时间戳
        String nonce = request.getParameter("nonce");// 随机数
        String echostr = request.getParameter("echostr");// 登陆验证
        if(echostr != null && !echostr.isEmpty()) {//这里 echostr 的值必须返回，否则微信认为请求失败
            PrintWriter pw = response.getWriter();
            pw.write(echostr);  //这里 echostr 的值必须返回，否则微信认为请求失败
            pw.flush();
            pw.close();
            return;
        }
        String openid = request.getParameter("openid");// 客户端主动回复口令会带过来openid
        if(openid == null || openid.isEmpty()) {
            GameBackLogger.SYS_LOG.error("openId不存在！");
            return;
        }
        String fromUserName = request.getParameter("FromUserName");// 公众帐号
        String toUserName = request.getParameter("ToUserName");// 玩家openId
        String msgType = request.getParameter("MsgType");// 消息类型
        String content = request.getParameter("Content");// 消息内容
        String redBagConfigStr = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_config");
        if (redBagConfigStr == null) {
            sendText(fromUserName, toUserName,"后台系统活动配置异常");
            return;
        }
        String redBagRewardDiamondGrades = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_reward_diamond_grade");// 1:30,6:20,3:90,4:120;5:160
        if (redBagRewardDiamondGrades == null) {
            sendText(fromUserName, toUserName,"后台系统活动配置异常");
            return ;
        }
        RedBagConfig config = new RedBagConfig(redBagConfigStr, redBagRewardDiamondGrades);
        Date drawStartDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowStartDate() + " 00:00:00"));
        Date drawEndDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowEndDate() + " 00:00:00"));
        Date date = new Date();
        if(!(msgType.equals("text") && content.equals(WeixinRedbagConfig.getToken()))) {
            sendText(fromUserName, toUserName, "欢迎关注小甘娱乐中心参与小甘瓜分现金红包活动，请输入领取口令：红包");
            return;
        }
        if(date.before(drawStartDate)) {
            sendText(fromUserName, toUserName,config.getDrowStartDate() + "开始领取，请稍后再来哦！");
            return;
        }
        if(date.after(drawEndDate)) {
            sendText(fromUserName, toUserName,"红包活动已结束！");
            return;
        }
        String accessToken = WechatUtils.getNewAccessToken();
        GameBackLogger.SYS_LOG.info("accessToken:" + accessToken + "---toUserName:" + toUserName + "---openid:" + openid);
        WeixinUser user = WechatUtils.getUserInfo(accessToken, openid);// 通过玩家openId 获得unionId
        if(user == null) {
            GameBackLogger.SYS_LOG.error("用户信息获取失败！---accessToken:" + accessToken + "openid:" + openid);
            sendText(fromUserName, toUserName,"用户信息获取失败！");
            return;
        }
        String unionId = user.getUnionid();
        long userId = UserDao.getInstance().getIdentityUserId(unionId);
        GameBackLogger.SYS_LOG.info("unionId:" + unionId + "---userId:" + userId);
        if(userId <= 0) {
            sendText(fromUserName, toUserName,user.getNickname() + "，您好！请您用已领取游戏红包的微信账号关注小甘娱乐中心！");
            return;
        }
        List<UserRedBagRecord> list = UserRedBagRecordDao.getInstance().getUserRedBagRecord(userId);
        float canReceiveRedBag = getUserRedBagRecords(list);
        DecimalFormat fnum = new DecimalFormat("#0.00");
        float sendAccRedBagNum = Float.parseFloat(fnum.format(canReceiveRedBag));
        GameBackLogger.SYS_LOG.info("userId:" + userId + "---canReceiveRedBag:" + sendAccRedBagNum);
        if(sendAccRedBagNum < 5.0f) {// 总金额低于5元时
            sendText(fromUserName, toUserName,"抱歉！您的累计红包金额低于5元不能提现哦！");
            return;
        }
        if(sendAccRedBagNum >= 100) {// 总金额大于100元时
            sendText(fromUserName, toUserName,"抱歉！您的累计红包金额大于100元，不能提现哦，请联系客服！");
            return;
        }
        WechatRedPackResponse sendResponse = RedPackSender.sendRedBag(fromUserName, sendAccRedBagNum);
        if(sendResponse == null) {
            sendText(fromUserName, toUserName,"红包发送失败，系统异常！");
            return;
        }
        if(sendResponse != null && sendResponse.getErr_code_des().equals("发放失败，此请求可能存在风险，已被微信拦截")) {
            sendText(fromUserName, toUserName,"您未实名认证，请先实名认证后，再提现！");
            return;
        }
        if(sendResponse != null && !sendResponse.getResult_code().equals("SUCCESS")) {
            sendText(fromUserName, toUserName,"红包发送失败，服务出现未知故障，请联系客服！");
            return;
        }
        if(sendResponse != null && sendResponse.getResult_code().equals("SUCCESS")) {// 发送成功
            GameBackLogger.SYS_LOG.info("红包发送成功:" + sendAccRedBagNum);
            for(UserRedBagRecord record : list) {// 更新红包状态
                boolean update = false;
                List<SelfRedBagReceiveRecord> tempList = new ArrayList<>();
                for(SelfRedBagReceiveRecord receiveRecord : record.getReceiveRecordList()) {
                    if(!receiveRecord.isWithDraw()) {// 提现更新红包状态
                        receiveRecord.setWithDraw(true);
                        update = true;
                    }
                    tempList.add(receiveRecord);
                }
                if(update == true) {
                    record.setReceiveRecords(JacksonUtil.writeValueAsString(tempList));
                    UserRedBagRecordDao.getInstance().saveUserRedBagRecord(record);
                }
            }
            GameBackLogger.SYS_LOG.info("更新红包状态成功");
            sendText(fromUserName, toUserName,"恭喜您提现成功！获得" + sendAccRedBagNum + "元红包！");
        }
    }

    /**
     * 转盘抽奖活动提现
     * @throws Exception
     */
    public void luckyDraw() throws Exception {
        synchronized (RedBagReceiveAction.class) {
            String signature = request.getParameter("signature");// 微信加密签名
            String timestamp = request.getParameter("timestamp");// 时间戳
            String nonce = request.getParameter("nonce");// 随机数
            String echostr = request.getParameter("echostr");// 登陆验证
            if (echostr != null && !echostr.isEmpty()) {//这里 echostr 的值必须返回，否则微信认为请求失败
                PrintWriter pw = response.getWriter();
                pw.write(echostr);  //这里 echostr 的值必须返回，否则微信认为请求失败
                pw.flush();
                pw.close();
                return;
            }
            String openid = request.getParameter("openid");// 客户端主动回复口令会带过来openid
            if (openid == null || openid.isEmpty()) {
                GameBackLogger.SYS_LOG.error("openId不存在！");
                return;
            }
            String fromUserName = request.getParameter("FromUserName");// 公众帐号
            String toUserName = request.getParameter("ToUserName");// 玩家openId
            String msgType = request.getParameter("MsgType");// 消息类型
            String content = request.getParameter("Content");// 消息内容
            Date drawStartDate = new Date(TimeUtil1.parseTimeInMillis(WeixinRedbagConfig.getStartTime() + " 00:00:00"));
            Date drawEndDate = new Date(TimeUtil1.parseTimeInMillis(WeixinRedbagConfig.getEndTime() + " 00:00:00"));
            Date date = new Date();
            if (!(msgType.equals("text") && content.equals(WeixinRedbagConfig.getToken()))) {
                sendText(fromUserName, toUserName, "欢迎关注" + WeixinRedbagConfig.getGameName() + "，参与游戏内" + WeixinRedbagConfig.getActivityName() + "获得现金奖励的朋友，请输入口令：" + WeixinRedbagConfig.getToken());
                return;
            }
            if (date.before(drawStartDate)) {
                sendText(fromUserName, toUserName, "活动还未开始，请稍后再来哦！");
                return;
            }
            if (date.after(drawEndDate)) {
                sendText(fromUserName, toUserName, "红包活动已结束！");
                return;
            }
            String accessToken = WechatUtils.getNewAccessToken();
            GameBackLogger.SYS_LOG.info("accessToken:" + accessToken + "---toUserName:" + toUserName + "---openid:" + openid);
            WeixinUser user = WechatUtils.getUserInfo(accessToken, openid);// 通过玩家openId 获得unionId
            if (user == null) {
                GameBackLogger.SYS_LOG.error("用户信息获取失败！---accessToken:" + accessToken + "openid:" + openid);
                sendText(fromUserName, toUserName, "用户信息获取失败！");
                return;
            }
            String unionId = user.getUnionid();
            long userId = UserDao.getInstance().getIdentityUserId(unionId);
            GameBackLogger.SYS_LOG.info("unionId:" + unionId + "---userId:" + userId);
            if (userId <= 0) {
                sendText(fromUserName, toUserName, user.getNickname() + "，您好！请用您登陆" + WeixinRedbagConfig.getGameName() + "的微信账号关注" + WeixinRedbagConfig.getAppName() + "才可以领取红包奖励哦!");
                return;
            }
            List<RedBagInfo> list = RedBagInfoDao.getInstance().getUserRedBagInfos(userId);
            float canReceiveRedBag = getRedbagReceiveAmount(list);
            DecimalFormat fnum = new DecimalFormat("#0.00");
            float sendAccRedBagNum = Float.parseFloat(fnum.format(canReceiveRedBag));
            GameBackLogger.SYS_LOG.info("userId:" + userId + "---canReceiveRedBag:" + sendAccRedBagNum);
            if (sendAccRedBagNum == 0) {// 总金额低于5元时
                sendText(fromUserName, toUserName, "抱歉！您的红包金额为0，不能提现哦！");
                return;
            }
            if (sendAccRedBagNum < 1) {// 总金额低于5元时
                sendText(fromUserName, toUserName, "抱歉！您的累计红包金额低于1元，不能提现哦！");
                return;
            }
            float todayTotalNum = RedBagInfoDao.getInstance().getTodayUserRedBagNum();
            if (todayTotalNum > WeixinRedbagConfig.getRedbagMaxNum()) {
                sendText(fromUserName, toUserName, "活动太火爆了，今日系统红包已发放完毕，请您明日再领噢！");
                return;
            }
            WechatRedPackResponse sendResponse = RedPackSender.sendRedBag(fromUserName, sendAccRedBagNum);
            if (sendResponse == null) {
                sendText(fromUserName, toUserName, "红包发送失败，系统异常！");
                return;
            }
            if (sendResponse != null && sendResponse.getErr_code_des().equals("发放失败，此请求可能存在风险，已被微信拦截")) {
                sendText(fromUserName, toUserName, "发放失败，此请求可能存在风险，已被微信拦截！（如您未实名认证，请先实名认证后再提现！）");
                return;
            }
            if (sendResponse != null && !sendResponse.getResult_code().equals("SUCCESS")) {
                sendText(fromUserName, toUserName, "红包发送失败，服务出现未知故障，请联系客服！");
                return;
            }
            if (sendResponse != null && sendResponse.getResult_code().equals("SUCCESS")) {// 发送成功
                GameBackLogger.SYS_LOG.info("红包发送成功:" + sendAccRedBagNum);
                for (RedBagInfo record : list) {// 更新红包状态
                    if (record.getRedBagType() == 2 && record.getDrawDate() == null) {
                        record.setDrawDate(new Date());
                        RedBagInfoDao.getInstance().saveRedBagInfo(record);
                    }
                }
                GameBackLogger.SYS_LOG.info("更新红包状态成功");
                sendText(fromUserName, toUserName, "恭喜您提现成功！获得" + sendAccRedBagNum + "元红包！");
            }
        }
    }

    private void sendText(String fromUserName, String toUserName, String content) {
        TextMessage text = new TextMessage();
        text.setContent(content);
        text.setToUserName(fromUserName);
        text.setFromUserName(toUserName);
        text.setCreateTime(new Date().getTime());
        text.setMsgType("text");
        String respMessage = MessageUtil.textMessageToXml(text);
        OutputUtil.output(1000, respMessage, request, response, false);
    }

    private float getRedbagReceiveAmount(List<RedBagInfo> list) {
        float canReceiveRedBag = 0.0f;
        if (list != null && !list.isEmpty()) {
            for(RedBagInfo record : list) {
                if(record.getRedBagType() == 2 && record.getDrawDate() == null) {
                    canReceiveRedBag += record.getRedbag();
                }
            }
        }
        return canReceiveRedBag;
    }

    /**
     * 今日已累计领取红包金额
     * @param list
     * @return
     */
    private float getReceivedRedbagNum(List<RedBagInfo> list) {
        float receivedRedBag = 0.0f;
        long curTime = System.currentTimeMillis();
        if (list != null && !list.isEmpty()) {
            for (RedBagInfo record : list) {
                if(record.getRedBagType() == 2 && record.getDrawDate() != null && TimeUtil.isSameDay(curTime, record.getDrawDate().getTime())) {
                    receivedRedBag += record.getRedbag();
                }
            }
        }
        return receivedRedBag;
    }

    private float getUserRedBagRecords(List<UserRedBagRecord> list) {
        float canReceiveRedBag = 0;
        if(list != null && !list.isEmpty()) {
            for(UserRedBagRecord record : list) {
                for(SelfRedBagReceiveRecord receiveRecord : record.getReceiveRecordList()) {
                    if(!receiveRecord.isWithDraw()) {
                        canReceiveRedBag += receiveRecord.getReceiveNum();
                    }
                }
            }
        }
        return canReceiveRedBag;
    }
}
