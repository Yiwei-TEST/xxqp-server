package com.sy599.game.gcommand.gold;

import com.alibaba.fastjson.JSON;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SystemCommonInfoType;
import com.sy599.game.db.bean.GoodsItem;
import com.sy599.game.db.dao.ItemExchangeDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.SystemCommonInfoManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldExchangeCommand extends BaseCommand {


    @Override
    public void setMsgTypeMap() {
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.getMyExtend().isGroupMatch()) {
            player.writeErrMsg(LangMsg.code_257);
            return;
        }

        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> intParams = req.getParamsList();
        int intParamsSize = intParams != null ? intParams.size() : 0;
        int optType = req.getOptType();
        if (optType == 1) {
            // 商品列表
            if (intParamsSize < 1) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }

            int type = intParams.get(0); // 1:金币兑换表
            Map<Integer, GoodsItem> map = StaticDataManager.loadGoodsItems(type);
            if (map == null || map.size() == 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_213));
                return;
            }
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), type, date + " 00:00:00", date + " 23:59:59");

            List<Integer> intList = new ArrayList<>();
            intList.add(optType);
            List<String> strList = new ArrayList<>();
            strList.add(String.valueOf(sum));
            strList.add(JSON.toJSONString(map.values()));
            player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, intList, strList);
        } else if (optType == 2) { // 金币兑换
            int itemType = intParams.get(0); // 1:钻石兑换金币 2:金币兑换钻石
            if (intParamsSize < 2) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            int itemId = intParams.get(1);
            if (itemId <= 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            GoodsItem goodsItem = StaticDataManager.loadGoodsItem(itemType, itemId);
            if (goodsItem == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_214));
                return;
            }

            if (itemType == GoodsItem.type_cards_2_golds) {
                // 兑换金币
                long addCards = Math.round(goodsItem.getAmount() * (goodsItem.getDiscount() * 1.0 / 100));
                if (addCards <= 0) {
                    player.writeErrMsg("配置错误：1");
                    return;
                } else {
                    if (player.loadAllCards() < addCards) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return;
                    }
                    int freeCardsLimit = ResourcesConfigsUtil.loadServerConfigIntegerValue("card_2_gold_freeCardsLimit", 10000);
                    if (player.getFreeCards() > freeCardsLimit) {
                        player.writeErrMsg("免费钻过多，不可兑换白金豆");
                        return;
                    }
                }


                if (player.getGoldPlayer() == null || player.loadAllGolds() <= 0) {
                    player.loadGoldPlayer(true);
                }

                int addGolds = (int) Math.round((goodsItem.getCount() * goodsItem.getRatio() * 1.0 / 100) + goodsItem.getGive());
                boolean isOK = player.changeCards(0, -addCards, true, 0, false, CardSourceType.cardExchangeGold);
                if (!isOK) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                    return;
                }
                player.changeGold(0, addGolds, 0, SourceType.cardExchangeGold);
                LogUtil.msgLog.info("diamond2gold|" + player.getUserId() + "|" + goodsItem.getId() + "|" + addCards + "|" + addGolds);
                Date currentDate = new Date();
                Map<String, Object> map = new HashMap<>();
                map.put("userId", String.valueOf(player.getUserId()));
                map.put("itemType", goodsItem.getType());
                map.put("itemId", goodsItem.getId());
                map.put("itemName", goodsItem.getName());
                map.put("itemAmount", addCards);
                map.put("itemCount", addGolds);
                map.put("itemGive", goodsItem.getGive());
                map.put("itemMsg", JacksonUtil.writeValueAsString(goodsItem));
                map.put("createdTime", currentDate);
                ItemExchangeDao.getInstance().save(map);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
                int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), itemType, date + " 00:00:00", date + " 23:59:59");

                List<Integer> intList = new ArrayList<>();
                intList.add(optType);
                intList.add(itemType);
                intList.add(itemId);
                List<String> strList = new ArrayList<>();
                strList.add(String.valueOf(sum));
                strList.add(String.valueOf(player.loadAllGolds()));
                strList.add(String.valueOf(player.loadAllCards()));
                player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, intList, strList);

            } else if (itemType == GoodsItem.type_goods_2_cards) {
                if (true) {
                    return;
                }
                // 兑换钻石
                int rest = NumberUtils.toInt(SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.goldGive).getContent(), Integer.parseInt(SystemCommonInfoType.goldGive.getContent()));
                int addGolds = (int) Math.round(goodsItem.getAmount() * (goodsItem.getDiscount() * 1.0 / 100));
                if (player.loadAllGolds() < addGolds) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_215));
                    return;
                } else if (player.loadAllGolds() - addGolds < rest) {
                    player.writeErrMsg(LangMsg.code_216, rest);
                    return;
                }
                int addCards = (int) Math.round((goodsItem.getCount() * goodsItem.getRatio() * 1.0 / 100) + goodsItem.getGive());

                player.changeCards(addCards, 0, true, 0, false, CardSourceType.goldExchangeCard);
                player.changeGold(0, -addGolds, 0, SourceType.goldExchangeCard);

                LogUtil.msgLog.info("gold2diamond|" + player.getUserId() + "|" + addGolds + "|" + addCards);

                Date currentDate = new Date();
                Map<String, Object> map = new HashMap<>();
                map.put("userId", String.valueOf(player.getUserId()));
                map.put("itemType", goodsItem.getType());
                map.put("itemId", goodsItem.getId());
                map.put("itemName", goodsItem.getName());
                map.put("itemAmount", addGolds);
                map.put("itemCount", addCards);
                map.put("itemGive", goodsItem.getGive());
                map.put("itemMsg", JacksonUtil.writeValueAsString(goodsItem));
                map.put("createdTime", currentDate);
                ItemExchangeDao.getInstance().save(map);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
                int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), itemType, date + " 00:00:00", date + " 23:59:59");

                List<Integer> intList = new ArrayList<>();
                intList.add(optType);
                intList.add(itemType);
                intList.add(itemId);
                List<String> strList = new ArrayList<>();
                strList.add(String.valueOf(sum));
                strList.add(String.valueOf(player.loadAllGolds()));
                strList.add(String.valueOf(player.loadAllCards()));
                player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, intList, strList);
            }
        }
    }


}
