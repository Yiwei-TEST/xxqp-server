package com.sy599.game.pay;

import com.sy.mainland.util.IpUtil;
import com.sy599.game.character.Player;
import com.sy599.game.common.action.BaseAction;
import com.sy599.game.db.bean.UserMessage;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.Md5CheckUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

public class PayAction extends BaseAction {

    public void execute() throws Exception {

        Map<String, String[]> params = getRequest().getParameterMap();
        if (params != null) {
            StringBuilder stringBuilder = new StringBuilder("pay params:");
            for (Map.Entry<String, String[]> kv : params.entrySet()) {
                stringBuilder.append(kv.getKey()).append("=").append(kv.getValue()[0]).append(",");
            }
            stringBuilder.append("ip=").append(IpUtil.getIpAddr(getRequest()));
            LogUtil.msgLog.info(stringBuilder.toString());
        }

        switch(this.getInt("funcType")) {
            case 1:
                this.pay();
                break;
            case 2:
                this.creditUpdate();
                break;
            default:
        }
    }

    private void pay() throws Exception {
        String flatId = this.getString("flatId");
        String orderId = this.getString("orderId");
        String signal = this.getString("sign");
        String time = this.getString("time");
        int amount = this.getInt("amount");
        int cards = this.getInt("cards");
        int freeCards = this.getInt("freeCards");
        String info = this.getString("info");
        String currency = this.getString("currency");
        String payRemoveBind = this.getString("payRemoveBind");
        if (IpUtil.isNotIntranet(request)&&!Md5CheckUtil.checkPayMd5(time, flatId, signal)) {
            this.writeErrMsg(1, null);
            LogUtil.e("pay error,md5 err,flatId:" + flatId + "amount:" + amount);
        } else {
            String ip = IpUtil.getIpAddr(getRequest());
            if (!StringUtils.isBlank(ip)) {
                LogUtil.monitor_i("pay execute-->ip:" + ip + ":" + flatId + " -" + orderId + " -" + amount + " -" + cards + " -" + freeCards + " -" + currency + " -" + payRemoveBind);
                Player player = NumberUtils.isDigits(flatId)?PlayerManager.getInstance().getPlayer(Long.parseLong(flatId)):PlayerManager.getInstance().getPlayer(flatId,"");
                if (player != null) {
                    if ("1".equals(payRemoveBind)) {
                        player.setPayBindId(0);
                        player.writeRemoveBindMessage();
                    }
                    if (!StringUtil.isBlank(currency)) {
                        if ("1".equals(currency)) {
                            player.updatePayCards(cards, freeCards);
                            player.writeCardsMessage(amount);
                        } else if ("2".equals(currency)){
                            player.updatePayGold(cards, freeCards);
                            player.writeGoldMessage(amount);
                        }
                    } else {
                        player.updatePayCards(cards, freeCards);
                        player.writeCardsMessage(amount);
                    }
                    UserMessage userMessage = JacksonUtil.readValue(info, UserMessage.class);
                    player.getMyMessage().addMessage(userMessage, false);
                    LogUtil.monitor_i("---------" + flatId + "---pay:orderId" + orderId + " :amount:" + amount + "----");
                    this.writeErrMsg(0, null);
                }
            }
        }
    }

    private void creditUpdate() throws Exception {
        String flatId = this.getString("flatId");
        String signal = this.getString("sign");
        String time = this.getString("time");
        long groupId = Long.valueOf(this.getString("groupId"));
        int credit = this.getInt("credit");
        if (IpUtil.isNotIntranet(request) && !Md5CheckUtil.checkPayMd5(time, flatId, signal)) {
            this.writeErrMsg(1, null);
            LogUtil.e("creditUpdate|err|md5|" + flatId + "|" + credit);
            return;
        }
        String ip = IpUtil.getIpAddr(getRequest());
        if (StringUtils.isBlank(ip)) {
            return;
        }

        LogUtil.monitor_i("creditUpdate|execute|" + flatId + "|" + credit + ip + "|");
        Player player = NumberUtils.isDigits(flatId) ? PlayerManager.getInstance().getPlayer(Long.parseLong(flatId)) : PlayerManager.getInstance().getPlayer(flatId, "");
        if (player == null) {
            return;
        }
        if (player.getGroupUser() == null || groupId != player.getGroupUser().getGroupId()) {
            return;
        }
        player.loadGroupUser(String.valueOf(groupId));
        LogUtil.monitor_i("creditUpdate|succ|" + flatId + "|" + credit + ip + "|");
        this.writeErrMsg(0, null);
    }
}
