package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.UserShare;
import com.sy599.game.db.dao.UserShareDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.ShareAcitivityConfig;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * @author liuping
 * 分享送钻活动处理类
 */
public class ShareActivityCmd extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> reqParams = req.getParamsList();
        int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
        int requestSource = 0;
        if (reqParams.size() >= 2)
            requestSource = reqParams.get(1);// params参数第1位  0表示从精彩活动打开  1表示从大厅分享按钮打开
        ShareAcitivityConfig configInfo = (ShareAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_share);
        long userId = player.getUserId();
        int playType = 0;// 玩法ID
        String currency = "";// 货币类型
        if (req.getStrParamsCount() > 1) {
            currency = req.getStrParams(1);
        }

        JSONObject userJsonObj = new JSONObject();
        Calendar now = Calendar.getInstance();
        int isShared = isShared(player, currency);
        boolean hasDiamond, hasGold;
        if (StringUtils.isBlank(currency)) {
            hasDiamond = true;
            hasGold = false;
        } else {
            hasGold = currency.contains("gold");
            hasDiamond = currency.contains("diamond");
        }
        if (requestType == 0) {
            userJsonObj.put("requestSource", requestSource);
            userJsonObj.put("isShared", isShared);
            userJsonObj.put("popup", 1);
            ActivityCommand.sendActivityInfo(player, configInfo, userJsonObj);
        } else if (requestType == 1) {
            if (isShared == 1) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_219));
                return;
            }

            if (hasGold) {
                int gold = randomDiamond(playType, 2, configInfo);
                if (gold > 0) {
                    player.changeGold(gold, 0, playType);
                    player.writeComMessage(WebSocketMsgType.res_code_com, "分享成功！恭喜您获得：金币x" + gold);
                    UserShare userShare = new UserShare(userId, now.getTime(), gold, "gold");
                    UserShareDao.getInstance().addUserShare(userShare);
                    LogUtil.msgLog.info(player.getName() + "分享朋友圈获得奖励金币*" + gold);

                    MessageUtil.sendMessage(UserMessageEnum.TYPE0, player, "您在分享中获得:金币x" + gold, null);
                }
            }
            if (hasDiamond) {
                int diamond = randomDiamond(playType, 1, configInfo);
                if (diamond > 0) {
                    UserShare userShare = new UserShare(userId, now.getTime(), diamond, "diamond");
                    UserShareDao.getInstance().addUserShare(userShare);

                    player.changeCards(diamond, 0, true, playType, CardSourceType.activity_share);

                    if (GameUtil.isPlayAhGame()) {
                        MessageUtil.sendMessage(UserMessageEnum.TYPE0, player, "您在分享中获得:房卡x" + diamond, null);
                        player.writeComMessage(WebSocketMsgType.res_code_com, "分享成功！恭喜您获得：房卡x" + diamond);
                        LogUtil.msgLog.info(player.getName() + "分享朋友圈获得奖励房卡*" + diamond);
                    } else {
                        MessageUtil.sendMessage(UserMessageEnum.TYPE0, player, "您在分享中获得:钻石x" + diamond, null);
                        player.writeComMessage(WebSocketMsgType.res_code_com, "分享成功！恭喜您获得：钻石x" + diamond);
                        LogUtil.msgLog.info(player.getName() + "分享朋友圈获得奖励钻石*" + diamond);
                    }
                }
            }
        }
    }

    /**
     * 玩家是否当天已分享
     *
     * @param player
     * @param currency
     * @return
     */
    public static int isShared(Player player, String currency) {
        Calendar now = Calendar.getInstance();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(now.getTime());
        int isShared = 0;
        List<UserShare> lists = UserShareDao.getInstance().getUserShare(player.getUserId(), currentDate);
        boolean hasDiamond, hasGold;
        if (StringUtils.isBlank(currency)) {
            hasDiamond = true;
            hasGold = false;
        } else {
            hasGold = currency.contains("gold");
            hasDiamond = currency.contains("diamond");
        }
        if (lists != null && lists.size() > 0) {
            for (UserShare userShare : lists) {
                if (hasGold && StringUtils.contains(userShare.getExtend(), "gold")) {
                    isShared = 1;
                    break;
                } else if (hasDiamond && (StringUtils.isBlank(userShare.getExtend()) || (StringUtils.contains(userShare.getExtend(), "diamond")))) {
                    isShared = 1;
                    break;
                }
            }
        }
        return isShared;
    }

    /**
     * 领钻配置(_玩法) 基础钻石,浮动钻石,概率(多个以分号隔开)<br/>
     * diamond_config_game=10,11,100 <br/>
     * diamond_config_game=20,0,100 <br/>
     * diamond_config_game=15,6,40;21,5,40;26,5,20 <br/>
     * diamond_config_game=10,11,100 <br/>
     * diamond_config_game=15,6,40;21,5,40;26,5,20 <br/>
     * diamond_config_game=30,11,79;41,5,20;46,5,1 <br/>
     * diamond_config_game=10,5,30;15,4,40;19,2,30 <br/>
     *
     * @param playType
     * @return
     */
    private int randomDiamond(int playType, int currency, ShareAcitivityConfig configInfo) {

        int diamond = -1;
        try {
            int tatalRatio = 0;
            int configs[][];
            String configStr = null;// 暂时只支持小甘麻将
            if (currency == 2) {
                String key = "diamond_gold_game_" + playType;
                if (configInfo.getDiamondGoldGames().containsKey(key)) {
                    configStr = configInfo.getDiamondGoldGames().get(key);
                } else {
                    configStr = configInfo.getDiamondGoldGames().get("diamond_gold_game");
                }
            } else {
                String key = "diamond_config_game_" + playType;
                if (configInfo.getDiamondConfigGames().containsKey(key)) {
                    configStr = configInfo.getDiamondConfigGames().get(key);
                } else {
                    configStr = configInfo.getDiamondConfigGames().get("diamond_config_game");
                }
            }
            if (StringUtils.isNotBlank(configStr)) {
                if (configStr.contains(";")) {
                    String[] strs = configStr.split(";");
                    configs = new int[strs.length][4];
                    int i = 0;
                    for (String str : strs) {
                        String[] temps = str.split("_");
                        if (temps.length == 3) {
                            configs[i][0] = Integer.parseInt(temps[0]);
                            configs[i][1] = Integer.parseInt(temps[1]);
                            int ratio = Integer.parseInt(temps[2]);
                            configs[i][2] = ratio;
                            tatalRatio += ratio;
                            configs[i][3] = tatalRatio;
                            i++;
                        }
                    }
                } else {
                    configs = new int[1][4];
                    String[] strs = configStr.split("_");
                    if (strs.length == 3) {
                        configs[0][0] = Integer.parseInt(strs[0]);
                        configs[0][1] = Integer.parseInt(strs[1]);
                        int ratio = Integer.parseInt(strs[2]);
                        configs[0][2] = ratio;
                        tatalRatio += ratio;
                        configs[0][3] = tatalRatio;
                    }
                }
            } else {
                if (currency == 2) {
                    // 3000,100
                    configs = new int[1][4];
                    configs[0][0] = 3000;
                    configs[0][1] = 0;
                    configs[0][2] = 100;
                    configs[0][3] = 100;
                    tatalRatio = 100;
                } else {
                    // 10,11,100
                    configs = new int[1][4];
                    configs[0][0] = 10;
                    configs[0][1] = 41;// 上调分享送钻的钻石数为50
                    configs[0][2] = 100;
                    configs[0][3] = 100;
                    tatalRatio = 100;
                }
            }
            if (tatalRatio != 100) {
                LogUtil.e("diamond_config_game config error:tatalRatio="
                        + tatalRatio + ",currency=" + currency);
                return 0;
            } else {
                Random random = new SecureRandom();
                int value = random.nextInt(100) + 1;
                for (int[] ints : configs) {
                    if (value <= ints[3]) {
                        diamond = ints[0];
                        if (ints[1] != 0) {
                            diamond += ((int) (ints[1] * random.nextDouble()));
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("diamond_config_game config error:" + e.getMessage(), e);
            return 0;
        }
        if (diamond < 0) {
            LogUtil.e("diamond_config_game config error");
            return 0;
        }
        return diamond;
    }

    @Override
    public void setMsgTypeMap() {
    }
}
