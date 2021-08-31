package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSONArray;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.serverSign.SevenSignInfo;
import com.sy599.game.db.bean.serverSign.UserSevenSignInfo;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 七日签到活动
 */
public class SevenSignCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> lists = req.getParamsList();
        int requestType = lists.get(0);// 请求类型 0打开 1领取
        String serverSignReward = ResourcesConfigsUtil.loadServerPropertyValue("seven_sign_reward");
        if(serverSignReward == null) {// 配置格式500,1000,1500,2000,2500,3000,20_11_70;31_10_29;41_10_1
            player.writeErrMsg("数据库未配置七日奖励");
            return;
        }
        UserSevenSignInfo signInfo = player.getMyExtend().getUserSevenSignInfo();
        String[] rewards = serverSignReward.split(",");
        long lastSignTime = signInfo.getLastSignTime();
        List<Integer> grades = signInfo.getSignGrades();
        int nextGrade = getNextGrade(signInfo);// 下一个可领取的档次
        int canReceiveNext = signInfo.canReceive();// 0不能领取 1领取下一个档次 2领取档次1
        if((canReceiveNext == 1 && signInfo.getSignGrades().size() >= 7) || canReceiveNext == 2) {// 到第8天时需清理前七天记录
            signInfo.setSignGrades(new ArrayList<>());
            player.getMyExtend().UpdateUserSevenSignInfo(signInfo);
        }
        if(requestType == 1) {// 签到
            if(canReceiveNext == 0) {
                player.writeErrMsg("今日不能签到");
                return;
            }
            if(canReceiveNext == 2) {
                nextGrade = 1;
            }
            int rewardGold = 0;
            if(nextGrade >= 1 && nextGrade <= 6) {
                if(nextGrade == 1)
                    signInfo.getSignGrades().clear();
                rewardGold = Integer.parseInt(rewards[nextGrade - 1]);
                player.changeGold(rewardGold, 0, false, 0, true);
                LogUtil.msgLog.info("玩家签到第：" + nextGrade + "天，获得金币：" + rewardGold);
            } else if(nextGrade == 7) {
                rewardGold = randomSignGold(rewards[nextGrade - 1]);// 随机获得宝箱奖励
                player.changeGold(rewardGold, 0, false, 0, true);
                LogUtil.msgLog.info("玩家签到第：" + nextGrade + "天，获得金币：" + rewardGold);
            }
            signInfo.getSignGrades().add(nextGrade);
            signInfo.setLastSignTime(System.currentTimeMillis());
            player.getMyExtend().UpdateUserSevenSignInfo(signInfo);
            String sendSignInfos = getSendSignInfos(signInfo, nextGrade, rewards);
            player.writeComMessage(WebSocketMsgType.res_code_seven_sign, (signInfo.canReceive() > 0) ? 1 : 0, rewardGold, sendSignInfos);
        } else {
            String sendSignInfos = getSendSignInfos(signInfo, nextGrade, rewards);
            player.writeComMessage(WebSocketMsgType.res_code_seven_sign, (signInfo.canReceive() > 0) ? 1 : 0, sendSignInfos);
        }

    }

    /**
     * 是否能七日签到
     * @param player
     * @return
     */
    public static boolean canSevenSign(Player player) {
        UserSevenSignInfo signInfo = player.getMyExtend().getUserSevenSignInfo();
        if(signInfo.canReceive() > 0)
            return true;
        else
            return false;
    }

    private int getNextGrade(UserSevenSignInfo signInfo) {
        List<Integer> grades = signInfo.getSignGrades();
        int nextGrade = 0;// 下一个可领取的档次
        if(grades == null || grades.isEmpty()) {
            nextGrade = 1;
        } else {
            int maxGrades = grades.get(grades.size() - 1);
            if(maxGrades == 7)
                nextGrade = 1;
            else
                nextGrade = maxGrades + 1;
        }
        return nextGrade;
    }

    private String getSendSignInfos(UserSevenSignInfo signInfo, int nextGrade, String[] rewards) {
        List<SevenSignInfo> signInfos = new ArrayList<>();
        int canReceiveNext = signInfo.canReceive();// 0不能领取 1领取下一个档次 2领取档次1
        for(int grade = 1; grade <= 7; grade ++) {
            //0未领取 1可领取 2已领取
            int state = 0;
            if(signInfo.getSignGrades().contains(grade))// 不能领取
                state = 2;
            else if(canReceiveNext == 1 && nextGrade == grade)// 领取下一个档次
                state = 1;
            else if(canReceiveNext == 2 && grade == 1)// 领取档次1
                state = 1;
            signInfos.add(new SevenSignInfo(grade, state, rewards[grade - 1]));
        }
        String sendSignInfos = JSONArray.toJSONString(signInfos);
        return sendSignInfos;
    }

    private int randomSignGold(String configStr) {
        int tatalRatio = 0;
        int configs[][];
        int diamond = 0;
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

        if (tatalRatio != 100) {
            LogUtil.e("seven_sign_reward config error:tatalRatio="
                    + tatalRatio);
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
            return diamond;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }
}
