package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.UserSign;
import com.sy599.game.db.dao.UserSignDao;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class SignCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> lists = req.getParamsList();
        List<String> strList = req.getStrParamsList();
        if (lists == null || lists.size() == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        String signType = "month";//月
        if (strList != null && strList.size() > 0) {
            signType = strList.get(0);//series5 连续5天
        }

        // 获得传递过来的操作指令
        int command = req.getParams(0);
        if (command == 1) {
            if (signType.startsWith("series")) {
                int days = NumberUtils.toInt(signType.substring(6), 5);
                if (days < 2) {
                    days = 5;
                }
                if (days > 30) {
                    days = 30;
                }
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                String end = sdf.format(cal.getTime());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.add(Calendar.DAY_OF_YEAR, 1 - days);
                String start = sdf.format(cal.getTime());
                player.loadSigns(start, end, 2);

                player.writeSignInfo("1", "0", 2);
            } else {
                player.loadSigns();

                player.writeSignInfo("1", "0", 1);
            }
        } else if (command == 2) {
            // 今日签到
            if (lists.size() < 2) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }

            // 获得传递过来的请求签到的号
            int reqDate = req.getParams(1);

            // 获得系统当前时间
            Calendar ca = Calendar.getInstance();
            Date myDate = ca.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String ymdDate = sdf.format(myDate);

            if ("2".equals(ResourcesConfigsUtil.loadServerPropertyValue("signType"))) {
                if (reqDate != Integer.parseInt(ymdDate)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                    return;
                }

                // 签过到不能再签
                if (player.getYmdSigns().contains(ymdDate)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_39));
                    return;
                }
            } else {
                if (reqDate != ca.get(Calendar.DATE)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                    return;
                }

                // 签过到不能再签
                if (player.getSigns() != null && player.getSigns().contains(reqDate)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_39));
                    return;
                }
            }

            int diamond = randomDiamond(lists.size() > 2 ? req.getParams(2) : 0);
            int gold = 0;
            int index = 1;
            String goldConfig = ResourcesConfigsUtil.loadServerPropertyValue("gold_" + signType + "_config");
            if (StringUtils.isNotBlank(goldConfig) && signType.startsWith("series")) {
                List<String> ymdSigns = player.getYmdSigns();
                int signDays = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("signDays"), 7);
                if (ymdSigns.size() == 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(myDate);
                    SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    String end = sdf0.format(cal.getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.add(Calendar.DAY_OF_YEAR, 1 - signDays);
                    String start = sdf0.format(cal.getTime());
                    player.loadSigns(start, end, 2);
                    ymdSigns = player.getYmdSigns();
                }

                int len = ymdSigns.size();

                String[] golds = goldConfig.trim().split(",");

                if (len > 0) {
                    if (ymdSigns.contains(ymdDate) || Integer.parseInt(ymdSigns.get(ymdSigns.size()-1)) >= reqDate) {
                        gold = 0;
                    } else {
                        SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(myDate);
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        String ymdDate0 = sdf0.format(calendar.getTime());
                        List<UserSign> userSigns = UserSignDao.getInstance().getUserSign(player.getUserId(),ymdDate0+" 00:00:00",ymdDate0+" 23:59:59");

                        if (userSigns==null || userSigns.size()==0){
                            gold = Integer.parseInt(golds[0]);
                        }else{
                            String[] msgs=userSigns.get(0).getExtend().split(",");
                            if (msgs.length>=3&&NumberUtils.isDigits(msgs[2])){
                                int cur = NumberUtils.toInt(msgs[2]);
                                index = cur>=signDays?1:(cur+1);
                                gold = Integer.parseInt(golds.length >= index ? golds[index-1] : golds[golds.length - 1]);
                            }else{
                                if (len == 1) {
                                    ca.add(Calendar.DAY_OF_YEAR, -1);
                                    if (ymdSigns.get(0).equals(sdf.format(ca.getTime()))) {
                                        gold = golds.length > 1 ? Integer.parseInt(golds[1]) : Integer.parseInt(golds[0]);
                                        index = 2;
                                    } else {
                                        gold = Integer.parseInt(golds[0]);
                                    }
                                } else {
                                    len = len - 1;
                                    ca.set(Calendar.HOUR_OF_DAY, 12);
                                    ca.add(Calendar.DAY_OF_YEAR, -1);
                                    if (Integer.parseInt(ymdSigns.get(len)) >= reqDate) {
                                        gold = 0;
                                    } else if (!ymdSigns.get(len).equals(sdf.format(ca.getTime()))) {
                                        gold = Integer.parseInt(golds[0]);
                                    } else {
                                        int series = 1;
                                        for (int i = len; i > 0; i--) {
                                            ca.setTime(sdf.parse(ymdSigns.get(i)));
                                            ca.add(Calendar.DAY_OF_YEAR, -1);
                                            if (!ymdSigns.get(i - 1).equals(sdf.format(ca.getTime()))) {
                                                break;
                                            } else {
                                                series++;
                                            }
                                        }

                                        gold = Integer.parseInt(golds.length > series ? golds[series] : golds[golds.length - 1]);
                                        index = series + 1;
                                    }
                                }
                            }
                        }

                    }
                } else {
                    gold = Integer.parseInt(golds[0]);
                }
            }

            if (diamond > 0 || gold > 0) {
                // 签到
                player.sign(reqDate, myDate, diamond, gold, index);

                // 存到消息
                if (diamond > 0) {
                    MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "您在签到中获得:钻石x" + diamond,null);
                }

                if (gold > 0){
                    MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "您在签到中获得:金币x" + gold,null);
                }

                // 发送签到成功的
                player.writeComMessage(WebSocketMsgType.res_code_sign, 1, diamond, gold);
            } else {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            }
        } else {
            LogUtil.e("SignCommand err-->command err-->" + command);
        }
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
    private int randomDiamond(int playType) {
        int diamond = -1;
        try {
            int tatalRatio = 0;
            int configs[][];
            String configStr = ResourcesConfigsUtil.loadServerPropertyValue("diamond_config_game_" + playType);
            if (StringUtils.isBlank(configStr)) {
                configStr = ResourcesConfigsUtil.loadServerPropertyValue("diamond_config_game");
            }

            if (StringUtils.isNotBlank(configStr)) {
                if (configStr.contains(";")) {
                    String[] strs = configStr.split(";");
                    configs = new int[strs.length][4];
                    int i = 0;
                    for (String str : strs) {
                        String[] temps = str.split(",");
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
                    String[] strs = configStr.split(",");
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
                //5,3,90;8,3,10
                configs = new int[2][4];
                configs[0][0] = 5;
                configs[0][1] = 3;
                configs[0][2] = 90;
                configs[0][3] = 90;

                configs[1][0] = 8;
                configs[1][1] = 3;
                configs[1][2] = 10;
                configs[1][3] = 100;

                tatalRatio = 100;
            }

            if (tatalRatio != 100) {
                LogUtil.errorLog.error("diamond_config_game config error:tatalRatio=" + tatalRatio);
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
            LogUtil.errorLog.error("diamond_config_game config error:" + e.getMessage(), e);
            return 0;
        }
        if (diamond < 0) {
            LogUtil.errorLog.error("diamond_config_game config error");
            return 0;
        }
        return diamond;
    }

    @Override
    public void setMsgTypeMap() {

    }

}
