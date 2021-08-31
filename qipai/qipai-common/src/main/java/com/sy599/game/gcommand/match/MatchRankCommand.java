package com.sy599.game.gcommand.match;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.bean.MatchUser;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MatchRankCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> intsList = req.getParamsList();
//        List<String> strsList = req.getStrParamsList();
        int intSize = intsList != null ? intsList.size() : 0;
//        int strSize = strsList != null ? strsList.size() : 0;

        int type = intSize > 0 ?intsList.get(0).intValue() : 0;
        if (type == 0){
            int pageNo = intSize > 1 ?intsList.get(1).intValue() : 1;
            int pageSize = intSize > 2 ?intsList.get(2).intValue() : 10;
            if (pageSize>100){
                pageSize=100;
            }
            List<HashMap<String,Object>> list = MatchDao.getInstance().selectMatchUsersPage(player.getUserId(),pageNo,pageSize);
            if (list!=null&&list.size()>0){
                List<String> matchTypes = new ArrayList<>();
                for (HashMap<String,Object> map : list){
                    String matchName = String.valueOf(map.get("matchName"));
                    String mt = String.valueOf(map.get("matchType"));
                    if (("null".equalsIgnoreCase(matchName) || StringUtils.isBlank(matchName))&&(!"null".equalsIgnoreCase(mt))&&(!matchTypes.contains(mt))){
                        matchTypes.add(mt);
                    }
                }
                if (matchTypes.size()>0){
                    StringBuilder strBuilder = new StringBuilder();
                    for (String mt : matchTypes){
                        strBuilder.append(",'").append(mt).append("'");
                    }
                    List<HashMap<String,Object>> list1 = MatchDao.getInstance().selectMatchNames(strBuilder.substring(1));

                    if (list1!=null&&list1.size()>0){
                        for (HashMap<String,Object> map : list){
                            String mt = String.valueOf(map.get("matchType"));
                            for (HashMap<String,Object> map1 : list1){
                                if (mt.equalsIgnoreCase(String.valueOf(map1.get("matchType")))){
                                    map.put("matchName",map1.get("matchName"));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            player.writeComMessage(WebSocketMsgType.req_com_match_rank_code,intsList,list==null?"[]":JSON.toJSONString(list));
        }else if(type == 1){
            JSONObject resultJson = new JSONObject();
            JSONObject awardJson = null;
            JSONArray rankJson = null;
            List<String> strParams = req.getStrParamsList();
            if (strParams!=null&&strParams.size()>0){
                long matchId = NumberUtils.toLong(strParams.get(0),0);
                if (matchId>0){
                    MatchBean matchBean = JjsUtil.loadMatch(matchId);
                    if (matchBean!=null){
                        awardJson = matchBean.loadAward();
                        List<String> ranks = matchBean.loadCurrentUserMsgs();
                        rankJson = new JSONArray(ranks.size());
                        JSONObject[] array = new JSONObject[matchBean.getMaxCount()];
                        int i=0;
                        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("xiaogan_match_quit"))){   //小甘比赛场战绩获取单独处理
                            for (String rankMsg : ranks){
                                String[] msgs = rankMsg.split(",");

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("userId",msgs[0]);
                                MatchUser matchUser = MatchDao.getInstance().selectOneMatchUser(matchId+"", msgs[0]);
                                int rank = matchUser.getUserRank();
                                if(rank != 0){
                                    jsonObject.put("rank",rank);
                                }else{
                                    i ++;
                                    rank = i;
                                    jsonObject.put("rank",rank);
                                }
                                jsonObject.put("num",msgs[1]);
                                jsonObject.put("score",matchUser.getCurrentScore());
                                jsonObject.put("alive",matchUser.getCurrentState());
                                Long userId = Long.valueOf(msgs[0]);
                                Player player1 = PlayerManager.getInstance().getPlayer(userId);
                                if (player1!=null){
                                    jsonObject.put("userName",player1.getRawName());
                                }else {
                                    jsonObject.put("userName", UserDao.getInstance().loadUserBase(msgs[0]).get("userName"));
                                }
                                array[rank-1] = jsonObject;   //根据名次排序
                            }
                            for(JSONObject js : array){
                                if(js != null && js.size() > 0){
                                    rankJson.add(js);
                                }
                            }
                        }else{
                            for (String rankMsg : ranks){
                                i++;
                                String[] msgs = rankMsg.split(",");

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("userId",msgs[0]);
                                jsonObject.put("rank",i);
                                jsonObject.put("num",msgs[1]);
                                jsonObject.put("score",msgs[2]);
                                jsonObject.put("alive",Integer.parseInt(msgs[3])<=0 ? 1 : 0);

                                Long userId = Long.valueOf(msgs[0]);
                                Player player1 = PlayerManager.getInstance().getPlayer(userId);
                                if (player1!=null){
                                    jsonObject.put("userName",player1.getRawName());
                                }else {
                                    jsonObject.put("userName", UserDao.getInstance().loadUserBase(msgs[0]).get("userName"));
                                }
                                rankJson.add(jsonObject);
                            }
                        }
                    }
                }
            }
            resultJson.put("award",awardJson==null?new JSONObject():awardJson);
            resultJson.put("rank",rankJson==null?new JSONArray():rankJson);

            player.writeComMessage(WebSocketMsgType.req_com_match_rank_code,intsList,resultJson.toString());
        }
    }

    @Override
    public void setMsgTypeMap() {
    }
}
