package com.sy599.game.gcommand.ticket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.dao.BaseConfigDao;
import com.sy599.game.db.dao.ItemExchangeDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class TicketCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<String> params = req.getStrParamsList();
        List<Integer> integers = req.getParamsList();
        int strSize = params == null ? 0 : params.size();
        int intSize = integers == null ? 0 : integers.size();
        if (intSize == 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        int type = integers.get(0);
        if (type == 0) {//获取礼券数量
            int val = UserDao.getInstance().queryUserExtendValue(player.getUserId(), UserResourceType.TICKET.getType());
            player.writeComMessage(req.getCode(), type, val);
        } else if (type == 1 || type == 2) {//获取礼券任务列表
            List<HashMap<String, Object>> goodsList;
            if (type == 2) {
                //获取礼券兑换列表
                goodsList = BaseConfigDao.getInstance().selectAllByType("TicketGoodsConfig");
            } else {
                goodsList = null;
            }

            int val = UserDao.getInstance().queryUserExtendValue(player.getUserId(), UserResourceType.TICKET.getType());
            List<HashMap<String, Object>> taskList = BaseConfigDao.getInstance().selectAllByType("TicketTaskConfig");
            if (taskList == null || taskList.size() == 0) {
                player.writeComMessage(req.getCode(), type, val, "[]", goodsList == null ? "[]" : JSON.toJSONString(goodsList));
            } else {
                Date date = new Date();
                SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                String dateStr = sdf0.format(date);
                String dateInt = sdf1.format(date);

                JSONArray jsonArray = new JSONArray(taskList.size());

                for (HashMap<String, Object> map : taskList) {
                    String str = String.valueOf(map.getOrDefault("msgValue", ""));
                    if ("null".equals(str) || str.length() == 0) {
                        continue;
                    }
                    //id,name,type,totalCount,count,msg,order---currentCount,state(0未完成1已完成2已领奖)
                    JSONObject json = JSONObject.parseObject(str);
                    jsonArray.add(json);

                    String id = json.getString("id");
                    String taskType = json.getString("type");
                    int totalCount = json.getIntValue("totalCount");
                    if ("1".equals(id)) {//每天登陆
                        json.put("currentCount", 1);
                        int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                        json.put("state", sum > 0 ? 2 : 1);
                    } else if ("gold_js".equals(taskType)) {
                        //金币场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "gold", dateInt, gameTypes);
                        json.put("currentCount", currentCount >= totalCount ? totalCount : currentCount);
                        if (currentCount >= totalCount) {
                            int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                            json.put("state", sum > 0 ? 2 : 1);
                        } else {
                            json.put("state", 0);
                        }
                    } else if ("match_js".equals(taskType)) {
                        //比赛场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "match", dateInt, gameTypes);
                        json.put("currentCount", currentCount >= totalCount ? totalCount : currentCount);
                        if (currentCount >= totalCount) {
                            int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                            json.put("state", sum > 0 ? 2 : 1);
                        } else {
                            json.put("state", 0);
                        }
                    } else if ("group_js".equals(taskType)) {
                        //俱乐部场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "group", dateInt, gameTypes);
                        json.put("currentCount", currentCount >= totalCount ? totalCount : currentCount);
                        if (currentCount >= totalCount) {
                            int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                            json.put("state", sum > 0 ? 2 : 1);
                        } else {
                            json.put("state", 0);
                        }
                    } else if ("common_js".equals(taskType)) {
                        //普通场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "common", dateInt, gameTypes);
                        json.put("currentCount", currentCount >= totalCount ? totalCount : currentCount);
                        if (currentCount >= totalCount) {
                            int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                            json.put("state", sum > 0 ? 2 : 1);
                        } else {
                            json.put("state", 0);
                        }
                    }
                }

                Collections.sort(jsonArray, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        JSONObject json1 = (JSONObject) o1;
                        JSONObject json2 = (JSONObject) o2;

                        int state1 = json1.getIntValue("state");
                        int state2 = json2.getIntValue("state");

                        if (state1 == state2) {
                            int order1 = json1.getIntValue("order");
                            int order2 = json2.getIntValue("order");
                            return order1 - order2;
                        } else {
                            if (state1 >= 2) {
                                return 1;
                            } else if (state2 >= 2) {
                                return -1;
                            } else if (state1 == 1) {
                                return -1;
                            } else if (state2 == 1) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    }
                });

                player.writeComMessage(req.getCode(), type, val, jsonArray.toString(), goodsList == null ? "[]" : JSON.toJSONString(goodsList));
            }
        } else if (type == 3) {//获取礼券兑换列表
            //id,ticketCount,goodsName,goodsCount,goodsTotal,goodsAmount,goodsIcon
            List<HashMap<String, Object>> list = BaseConfigDao.getInstance().selectAllByType("TicketGoodsConfig");
            if (list == null || list.size() == 0) {
                player.writeComMessage(req.getCode(), type, "[]");
            } else {
                player.writeComMessage(req.getCode(), type, JSON.toJSONString(list));
            }
        } else if (type == 4) {//完成任务领取礼券
            String taskId = strSize >= 1 ? params.get(0) : null;
            List<HashMap<String, Object>> taskList;
            if (StringUtils.isBlank(taskId) || (taskList = BaseConfigDao.getInstance().selectAllByType("TicketTaskConfig")) == null || taskList.size() == 0) {
                player.writeErrMsg("领取失败：任务不存在");
                return;
            }

            Date date = new Date();
            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            String dateStr = sdf0.format(date);
            String dateInt = sdf1.format(date);

            int ticketState = 0;

            for (HashMap<String, Object> map : taskList) {
                String str = String.valueOf(map.getOrDefault("msgValue", ""));
                if ("null".equals(str) || str.length() == 0) {
                    continue;
                }
                //id,name,type,totalCount,count,msg,order---currentCount,state(0未完成1已完成2已领奖)
                JSONObject json = JSONObject.parseObject(str);

                String id = json.getString("id");

                if (!taskId.equals(id)) {
                    continue;
                }

                String taskType = json.getString("type");
                int totalCount = json.getIntValue("totalCount");
                int sum = -1;

                synchronized (player) {
                    if ("1".equals(id)) {//每天登陆
                        sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                    } else if ("gold_js".equals(taskType)) {
                        //金币场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "gold", dateInt, gameTypes);
                        if (currentCount >= totalCount) {
                            sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                        } else {
                            player.writeErrMsg("任务未完成");
                            return;
                        }
                    } else if ("match_js".equals(taskType)) {
                        //比赛场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "match", dateInt, gameTypes);
                        if (currentCount >= totalCount) {
                            sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                        } else {
                            player.writeErrMsg("任务未完成");
                            return;
                        }
                    } else if ("group_js".equals(taskType)) {
                        //俱乐部场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "group", dateInt, gameTypes);
                        if (currentCount >= totalCount) {
                            sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                        } else {
                            player.writeErrMsg("任务未完成");
                            return;
                        }
                    } else if ("common_js".equals(taskType)) {
                        //普通场N局
                        String gameTypes = json.getString("games");
                        int currentCount = UserDao.getInstance().countUserStatistics("b".equals(json.getString("dj")), String.valueOf(player.getUserId()), "common", dateInt, gameTypes);
                        if (currentCount >= totalCount) {
                            sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), "ticket", id, dateStr + " 00:00:00", dateStr + " 23:59:59");
                        } else {
                            player.writeErrMsg("任务未完成");
                            return;
                        }
                    }else {
                        player.writeErrMsg("不能领取，请稍后再试");
                        return;
                    }

                    if (sum > 0) {
                        ticketState = 2;
                    } else if (sum == 0) {
                        int ticketCount = json.getIntValue("count");
                        Map<String, Object> goodsMap = new HashMap<>();
                        goodsMap.put("userId", String.valueOf(player.getUserId()));
                        goodsMap.put("itemType", "ticket");
                        goodsMap.put("itemId", id);
                        goodsMap.put("itemName", json.getString("name"));
                        goodsMap.put("itemAmount", json.getIntValue("totalCount"));
                        goodsMap.put("itemCount", ticketCount);
                        goodsMap.put("itemGive", 0);
                        goodsMap.put("itemMsg", "完成任务获得");
                        goodsMap.put("createdTime", date);
                        if (ItemExchangeDao.getInstance().save(goodsMap) > 0) {
                            ticketState = 1;
                            UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType()
                                    , String.valueOf(player.getUserId()), UserResourceType.TICKET.name(), String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                            LogUtil.msgLog.info("get ticket:taskId={},userId={},ticket={}", taskId, player.getUserId(), ticketCount);
                        }
                    }
                }
                break;
            }

            if (ticketState == 1) {
                int val = UserDao.getInstance().queryUserExtendValue(player.getUserId(), UserResourceType.TICKET.getType());
                player.writeComMessage(req.getCode(), type, val, taskId);
            } else if (ticketState == 2) {
                player.writeErrMsg("已领取");
            } else {
                player.writeErrMsg("任务未完成");
            }
        } else if (type == 5) {//礼券兑换商品
            String goodsId = strSize >= 1 ? params.get(0) : null;
            List<HashMap<String, Object>> goodsList;
            if (StringUtils.isBlank(goodsId) || (goodsList = BaseConfigDao.getInstance().selectAllByType("TicketGoodsConfig")) == null || goodsList.size() == 0) {
                player.writeErrMsg("兑换失败：礼品不存在");
                return;
            }

            Date date = new Date();

            int sendMark = -1;
            for (HashMap<String, Object> map : goodsList) {
                String str = String.valueOf(map.getOrDefault("msgValue", ""));
                if ("null".equals(str) || str.length() == 0) {
                    continue;
                }
                //id,name,type,totalCount,count,msg,order---currentCount,state(0未完成1已完成2已领奖)
                JSONObject json = JSONObject.parseObject(str);

                String id = json.getString("id");

                if (!goodsId.equals(id)) {
                    continue;
                }

                String goodsType = json.getString("type");
                int ticketCount = json.getIntValue("ticketCount");
                int goodsCount = json.getIntValue("goodsCount");

                synchronized (player) {
                    int val = UserDao.getInstance().queryUserExtendValue(player.getUserId(), UserResourceType.TICKET.getType());
                    if (val < ticketCount) {
                        player.writeErrMsg("兑换失败，礼券不足");
                        return;
                    }

                    UserDao.getInstance().updateUserExtendIntValue(String.valueOf(player.getUserId()), UserResourceType.TICKET, -ticketCount);

                    if ("1".equals(goodsType)) {
                        sendMark = 1;
                        player.changeCards(goodsCount, 0, true, true, CardSourceType.ticket_card);
                    } else if ("2".equals(goodsType)) {
                        sendMark = 1;
                        player.changeGold(goodsCount, 0, 0);
                    } else {
                        sendMark = 2;
                    }

                    Map<String, Object> goodsMap = new HashMap<>();
                    goodsMap.put("userId", String.valueOf(player.getUserId()));
                    goodsMap.put("itemType", "ticketGoods");
                    goodsMap.put("itemId", id);
                    goodsMap.put("itemName", json.getString("goodsName"));
                    goodsMap.put("itemAmount", ticketCount);
                    goodsMap.put("itemCount", goodsCount);
                    goodsMap.put("itemGive", 0);
                    goodsMap.put("itemMsg", sendMark == 1 ? "1" : "0");//0未发放1已发放
                    goodsMap.put("createdTime", date);
                    if (ItemExchangeDao.getInstance().save(goodsMap) > 0) {
                        LogUtil.msgLog.info("consume ticket:goodsId={},userId={},ticket={},goodsMap={}", goodsId, player.getUserId(), ticketCount, goodsMap);
                    }

                    MessageUtil.sendMessage(UserMessageEnum.TYPE4, player, ticketCount + "礼券成功兑换" + json.getString("goodsName"), json.getString("goodsName"));
                }
                break;
            }

            int val = UserDao.getInstance().queryUserExtendValue(player.getUserId(), UserResourceType.TICKET.getType());

            if (sendMark == 1) {
                player.writeComMessage(req.getCode(), type, val, sendMark, goodsId, "礼品已发放");
            } else if (sendMark == 2) {
                player.writeComMessage(req.getCode(), type, val, sendMark, goodsId, "兑换成功，请联系客服领取礼品");
            } else {
                player.writeErrMsg("兑换失败：礼品不存在");
            }
        } else if (type == 6) {//金币抽奖礼券(中奖列表)

        } else if (type == 7) {//金币抽奖礼券(中奖结果)

        } else if (type == 8) {//查看兑换记录
            int pageNo = intSize > 1 ? integers.get(1).intValue() : 1;
            int pageSize = intSize > 2 ? integers.get(2).intValue() : 10;
            if (pageSize > 100) {
                pageSize = 100;
            }
            List<HashMap<String, Object>> list = ItemExchangeDao.getInstance().selectItemExchangePage(player.getUserId(), "ticketGoods", null, pageNo, pageSize);
            player.writeComMessage(req.getCode(), integers, list == null ? "[]" : JSON.toJSONString(list));
        }
    }

    @Override
    public void setMsgTypeMap() {
    }

}
