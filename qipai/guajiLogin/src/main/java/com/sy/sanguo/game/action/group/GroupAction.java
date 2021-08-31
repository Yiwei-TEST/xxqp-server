package com.sy.sanguo.game.action.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CacheUtil;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy.sanguo.common.executor.MinuteTask;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.request.HttpUtil;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.Activity;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.Server;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.bean.group.*;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.ActivityDao;
import com.sy.sanguo.game.dao.DataStatisticsDao;
import com.sy.sanguo.game.dao.RoomDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupCreditDao;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.dao.group.GroupDaoManager;
import com.sy.sanguo.game.dao.group.GroupDaoNew;
import com.sy.sanguo.game.pdkuai.db.bean.RedPacketRecord;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.bean.UserPlaylog;
import com.sy.sanguo.game.pdkuai.db.dao.RedPacketDao;
import com.sy.sanguo.game.pdkuai.db.dao.TableLogDao;
import com.sy.sanguo.game.pdkuai.helper.FakeTableHepler;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.pdkuai.util.PlayLogTool;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.utils.BjdUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.SysPartitionUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupAction.class);

    private UserDaoImpl userDao;
    private GroupDao groupDao;
    private GroupCreditDao groupCreditDao;
    private DataStatisticsDao dataStatisticsDao;
    private GroupDaoNew groupDaoNew;
    private static final int min_group_id = 1000;

    public void setGroupCreditDao(GroupCreditDao groupCreditDao) {
        this.groupCreditDao = groupCreditDao;
    }

    /**
     * 百万大奖领奖
     */
    public void drawPrize() throws Exception{
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String userId = this.getString("userId");
            String groupId = this.getString("groupId");
            if (!NumberUtils.isDigits(userId) || !NumberUtils.isDigits(groupId)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }


            // 百万大奖领奖期间
            Activity activityBean = ActivityDao.getInstance().getActivityById(3);
            if (activityBean == null) {
                OutputUtil.output(1, "活动未开启", getRequest(), getResponse(), false);
                return;
            }

            JSONObject json = new JSONObject();
            Date startDate1= activityBean.getBeginTime();
            Date endDate1 = activityBean.getEndTime();
            if (!TimeUtil.isInTime(new Date(), startDate1, endDate1)) {
                OutputUtil.output(1, "不在活动期间", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
            if (groupUser == null || groupUser.getUserRole() > 0) {
                OutputUtil.output(1, "领奖人信息有误", getRequest(), getResponse(), false);
                return;
            }

            Map<String, Object> map1 = new HashMap<>();
            map1.put("tableId", groupId);
            map1.put("hbType", Integer.parseInt(activityBean.getThem()));
            map1.put("startDate", activityBean.getBeginTime());
            map1.put("endDate", activityBean.getEndTime());
            List<RedPacketRecord> recordList = RedPacketDao.getInstance().getDrawPrize(map1);
            if (recordList != null && !recordList.isEmpty()) {
                OutputUtil.output(1, "已领取过奖励", getRequest(), getResponse(), false);
                return;
            }

            String extend = activityBean.getExtend();
            if (StringUtils.isBlank(extend)) {
                OutputUtil.output(1, "奖池有误", getRequest(), getResponse(), false);
                return;
            }
            // 局数
            int count = dataStatisticsDao.loadDataStatisticsList6(NumberUtils.toInt(activityBean.getThem()), "jlbCount", "group"+groupId);

            int awardKey = 0;
            HashMap<String, String> awardMap = JacksonUtil.readValue(extend, HashMap.class);
            List<Integer> keyList = new ArrayList<>();
            for (String key : awardMap.keySet()){
                keyList.add(Integer.parseInt(key));
            }
            Collections.sort(keyList, Collections.reverseOrder());
            for (Integer key : keyList) {
                if (count >= key) {
                    awardKey = key;
                    break;
                }
            }
            // 奖励的钻石数
            int award = NumberUtils.toInt(awardMap.get(String.valueOf(awardKey)));
            json.put("award", award);
            json.put("userId", userId);
            json.put("groupId", groupId);
            if (award>0) {
                RegInfo user = userDao.getUser(Long.parseLong(userId));
                if (user == null) {
                    LOGGER.error("drawPrize err-->"+ json);
                    OutputUtil.output(1, "玩家信息有误", getRequest(), getResponse(), false);
                    return;
                }

                Map<String, Object> map = new HashMap<>();
                map.put("userId", userId);
                map.put("tableId", groupId);
                map.put("hbType", activityBean.getThem());
                map.put("money", award);
                map.put("userName", "");
                map.put("createTime", new Date());
                RedPacketDao.getInstance().addDrawPrize(map);
                userDao.addUserCards(user, 0, award, CardSourceType.drawPrize);
                // 领奖状态 2成功，0失败
                json.put("status", 2);
            } else {
                // 领奖状态 1成功，0失败
                json.put("status", 0);
            }
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 俱乐部牌局统计
     */
    @Deprecated
    public void loadTableCount() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("userId");
            if (StringUtils.isAnyBlank(groupId, userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(NumberUtils.toLong(userId), NumberUtils.toLong(groupId));
            if (groupUser == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }

            if (!GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }

            List<HashMap<String, Object>> list = groupDao.getTableCount(groupId);
           List<HashMap<String, Object>> list2 = dataStatisticsDao.loadGroupDecDiamond(groupId);
            if (list == null) {
                list = new ArrayList<>();
            }
            Calendar ca = Calendar.getInstance();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            List<String> dateStrList = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                String dateStr = sdf.format(ca.getTime());
                dateStrList.add(dateStr);
                ca.set(Calendar.DATE, ca.get(Calendar.DATE) - 1);
            }
            HashMap<String, Object> map;
            HashMap<String, Object> map2 = new HashMap<>();
            HashMap<String, Object> map3 = new HashMap<>();
            if (list.isEmpty()) {
                for (String dateStr : dateStrList) {
                    map = new HashMap<>();
                    map.put("ctime", dateStr);
                    map.put("c", "0");
                    map.put("decdiamond", "0");
                    list.add(map);
                }
            } else {
                for (HashMap<String, Object> m : list) {
                    map2.put(String.valueOf(m.get("ctime")), m.get("c"));
                }
                for (HashMap<String, Object> m : list2) {
                    map3.put(String.valueOf(m.get("ctime")), m.get("c"));
                }
                
                list.clear();
                for (String dateStr : dateStrList) {
                    map = new HashMap<>();
                    map.put("ctime", dateStr);
                    map.put("c", map2.containsKey(dateStr) ? map2.get(dateStr) : "0");
                    map.put("decDiamond", map3.containsKey(dateStr) ? map3.get(dateStr) : "0");
                    list.add(map);
                }
            }

            JSONObject json = new JSONObject();
            json.put("list", list);
            int activity = 0;
            // 百万大奖活动期间
            Activity activityBean = ActivityDao.getInstance().getActivityById(2);
            if (activityBean != null) {
                Date startDate1= activityBean.getBeginTime();
                Date endDate1 = activityBean.getEndTime();
                if (TimeUtil.isInTime(new Date(), startDate1, endDate1)) {
                    String extend = activityBean.getExtend();
                    int awardKey = 0;
                    int nextKey = 0;
                    if (!StringUtils.isBlank(extend) ) {
                        activity = 1;
                        // 局数
                        int count = dataStatisticsDao.loadDataStatisticsList6(NumberUtils.toInt(activityBean.getThem()), "jlbCount", "group"+groupId);
                        HashMap<String, String>awardMap = JacksonUtil.readValue(extend, HashMap.class);
                        List<Integer> keyList = new ArrayList<>();
                        for (String key : awardMap.keySet()){
                            keyList.add(Integer.parseInt(key));
                        }
                        Collections.sort(keyList, Collections.reverseOrder());
                        for (Integer key : keyList) {
                            if (count >= key) {
                                awardKey = key;
                                break;
                            } else {
                                nextKey = key;
                            }
                        }
                        // 局数
                        json.put("count", count);
                        // 奖励的钻石数
                        json.put("award", awardKey>0?awardMap.get(String.valueOf(awardKey)):0);
                        // 下一档奖励局数
                        json.put("nextCount", nextKey);
                        //下一档奖励钻石数
                        json.put("nextAward", nextKey>0?awardMap.get(String.valueOf(nextKey)):0);
                        // 领奖状态
                        json.put("status", -1);
                    }
                }
            }
            if (activity <=0) {
                // 百万大奖领奖期间
                Activity activityBean1 = ActivityDao.getInstance().getActivityById(3);
                if (activityBean1 != null) {
                    Date startDate1= activityBean1.getBeginTime();
                    Date endDate1 = activityBean1.getEndTime();
                    if (TimeUtil.isInTime(new Date(), startDate1, endDate1)) {
                        String extend = activityBean.getExtend();
                        int awardKey = 0;
                        if (!StringUtils.isBlank(extend) ) {
                            activity = 2;
                            // 局数
                            int count = dataStatisticsDao.loadDataStatisticsList6(NumberUtils.toInt(activityBean.getThem()), "jlbCount", "group"+groupId);
                            HashMap<String, String> awardMap = JacksonUtil.readValue(extend, HashMap.class);
                            List<Integer> keyList = new ArrayList<>();
                            for (String key : awardMap.keySet()){
                                keyList.add(Integer.parseInt(key));
                            }
                            Collections.sort(keyList, Collections.reverseOrder());
                            for (Integer key : keyList) {
                                if (count >= key) {
                                    awardKey = key;
                                    break;
                                }
                            }
                            // 局数
                            json.put("count", count);
                            // 奖励的钻石数
                            json.put("award", awardKey>0?awardMap.get(String.valueOf(awardKey)):0);
                            // 领奖状态 1可领 2已领
                            Map<String, Object> map1 = new HashMap<>();
                            map1.put("tableId", groupId);
                            map1.put("hbType", Integer.parseInt(activityBean1.getThem()));
                            map1.put("startDate", activityBean1.getBeginTime());
                            map1.put("endDate", activityBean1.getEndTime());
                            List<RedPacketRecord> recordList = RedPacketDao.getInstance().getDrawPrize(map1);
                            json.put("status", awardKey>0?recordList==null||recordList.isEmpty()?1:2:-1);
                        }
                    }
                }
            }
            json.put("activity", activity);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 俱乐部牌局统计详情
     */
    public void loadTableCountDetails() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("userId");
            String queryDate = params.get("queryDate");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            // 2,4表示查询所有 4表示查询中途解散的；
            String condition = params.get("condition");
            // 默认取当天数据
            if (StringUtils.isBlank(queryDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                queryDate = sdf.format(new Date());
            }
            if (StringUtils.isAnyBlank(groupId, userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            String startDate = "";
            String endDate = "";
            if (!queryDate.contains(" ")) {
                startDate = queryDate + " 00:00:00";
            }
            if (!queryDate.contains(" ")) {
                endDate = queryDate + " 23:59:59";
            }

            GroupUser groupUser = groupDao.loadGroupUser(NumberUtils.toLong(userId), NumberUtils.toLong(groupId));
            if (groupUser == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }

            if (groupUser.getUserRole() > 1) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }

            if (StringUtils.isBlank(condition)) {
                // 正常解散和中途解散
                condition = "2,4";
            }
            List<GroupTable> list = groupDao.getTableCountDetails(groupId, startDate, endDate, pageNo-1, pageSize, "0", condition);
            int count = groupDao.getTableCountByDate(groupId, startDate, endDate, "0", condition);
            List<Map<String, Object>> results = new ArrayList<>();

            if (list != null && !list.isEmpty()) {
                for (GroupTable groupTable : list) {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    map.put("overTime", sdf.format(groupTable.getOverTime()));
                    map.put("tableId", groupTable.getTableId());
                    String tableMsg = groupTable.getTableMsg();
                    String playType;
                    if (StringUtils.isNotBlank(tableMsg)) {
                        if(tableMsg.startsWith("{") && tableMsg.endsWith("}")){
                            JSONObject jsonObject = JSONObject.parseObject(tableMsg);
                            playType = jsonObject.getString("type");
                            if (StringUtils.isBlank(playType)){
                                String ints = jsonObject.getString("ints");
                                playType=ints.split(",")[1];
                            }
                        }else{
                            playType=tableMsg.split(",")[1];
                        }
                    }else{
                        playType = "0";
                    }
                    map.put("playType", playType);
                    map.put("players", groupTable.getPlayers()==null?"":groupTable.getPlayers());
                    map.put("playedBureau", groupTable.getPlayedBureau());
                    map.put("overDetail", groupTable.getCurrentState());
                    results.add(map);
                }
            }

            JSONObject json = new JSONObject();
            json.put("list", results);
            json.put("tables", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 加载军团基础配置
     */
    public void loadAllGroupConfig() {
        try {

            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            OutputUtil.output(0, groupDao.loadALLGroupConfig(), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 申请加入军团
     */
    public void applyJoinGroup() {
        try {

            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            String groupIdStr = params.get("groupId");
            String userIdStr = params.get("userId");
            if (NumberUtils.isDigits(groupIdStr) && NumberUtils.isDigits(userIdStr)) {
                Long groupId = Long.parseLong(groupIdStr);
                Long userId = Long.parseLong(userIdStr);
                RegInfo user = userDao.getUser(userId);
                if (user == null) {
                    OutputUtil.output(-1, "用户不存在", getRequest(), getResponse(), false);
                    return;
                }
                GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser != null) {
                    OutputUtil.output(2, "已加入军团", getRequest(), getResponse(), false);
                    return;
                }

                int groupCount = groupDao.loadGroupCount(userId);
                if (groupCount >= 20) {
                    OutputUtil.output(3, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                    return;
                }

                GroupReview groupReview = groupDao.loadGroupReview0(groupId, userId,1);
                if (groupReview != null && groupReview.getReviewMode() ==1) {
                    if(groupReview.getCurrentOperator() == null)
                        OutputUtil.output(3, "已申请加入军团", getRequest(), getResponse(), false);
//                    else
//                    	
//                        OutputUtil.output(3, "请在军团+号处打开邀请通知同意即可", getRequest(), getResponse(), false);
                    return;
                }

                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                } else if (groupInfo.getGroupMode().intValue() != 0) {
                    OutputUtil.output(5, "军团不接受申请", getRequest(), getResponse(), false);
                    return;
                } else if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                    OutputUtil.output(6, "军团人员已满", getRequest(), getResponse(), false);
                    return;
                }

                groupReview = new GroupReview();
                groupReview.setCreatedTime(new Date());
                groupReview.setCurrentState(0);
                groupReview.setGroupId(groupId);
                groupReview.setGroupName(groupInfo.getGroupName());
                groupReview.setReviewMode(1);
                groupReview.setUserId(userId);
                groupReview.setUserName(user.getName());
                if(groupDao.createGroupReview(groupReview)<=0){
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }

                OutputUtil.output(0, "申请成功", getRequest(), getResponse(), false);
                
                //通知游戏服刷新\
                
               List<Long> listm =  groupDao.loadGroupUserIdsByRole(1);
               
               if(listm.size()>0) {
            	   for(Long m : listm){
                	   RegInfo user3 = userDao.getUser(m);
                	   if(user3 != null && user3.getIsOnLine() ==1) {
                		   GameUtil.refreshState( user3.getEnterServer(), m, 1, "");
                	   }
                   }
               }
               
                GroupUser master = groupDao.loadGroupMaster(groupId+"");
                if(master!=null) {
                	   RegInfo user2 = userDao.getUser(master.getUserId());
                	   if(user2.getIsOnLine() ==1) {
                		   GameUtil.refreshState( user2.getEnterServer(), master.getUserId(), 1, "");
                	   }
                }

                LOGGER.info("apply success:{}", JSON.toJSONString(groupReview));
            } else {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 邀请加入军团
     */
    public void inviteJoinGroup() {
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            String groupIdStr = params.get("groupId");
            String userIdStr = params.get("oUserId");
            String mUserIdStr = params.get("mUserId");
            String checked = params.get("checked");//checked不需要玩家确认直接加入军团
            if (NumberUtils.isDigits(groupIdStr) && NumberUtils.isDigits(userIdStr) && NumberUtils.isDigits(mUserIdStr)) {
                Long groupId = Long.parseLong(groupIdStr);
                Long userId = Long.parseLong(userIdStr);
                Long mUserId = Long.parseLong(mUserIdStr);
                RegInfo user = userDao.getUser(userId);
                if (user == null) {
                    OutputUtil.output(-1, "用户不存在", getRequest(), getResponse(), false);
                    return;
                }

                GroupUser mGroupUser = groupDao.loadGroupUser(mUserId, groupId);
                if (mGroupUser == null) {
                    OutputUtil.output(2, "您还没有加入军团", getRequest(), getResponse(), false);
                    return;
                } else if (mGroupUser.getUserRole().intValue() != 0&&mGroupUser.getUserRole().intValue() != 10) {
                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("wzqf_admin_role"))) {
                        if (mGroupUser.getUserRole().intValue() != 1) {
                            OutputUtil.output(3, "权限不够：仅限(副)群主(组长)", getRequest(), getResponse(), false);
                            return;
                        }
                    }else if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("admin_invite_by_id"))){
                        // 管理员通过id邀请玩家进群
                        if (mGroupUser.getUserRole().intValue() != 1) {
                            OutputUtil.output(3, "权限不够：仅限(副)群主(组长)", getRequest(), getResponse(), false);
                            return;
                        }
                    }else{
                        OutputUtil.output(3, "权限不够：仅限群主(组长)", getRequest(), getResponse(), false);
                        return;
                    }
                } else if (mGroupUser.getGroupId().intValue() != groupId) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                    return;
                }

                GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser != null) {
                    OutputUtil.output(2, "已加入军团", getRequest(), getResponse(), false);
                    return;
                }

                int groupCount = groupDao.loadGroupCount(userId);
                if (groupCount >= 20) {
                    OutputUtil.output(3, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                    return;
                }

                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                } else if (groupInfo.getGroupMode().intValue() != 1) {
//                    OutputUtil.output(5, "军团不接受邀请:" + groupId, getRequest(), getResponse(), false);
//                    return;
                }

                if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                    OutputUtil.output(6, "军团人员已满", getRequest(), getResponse(), false);
                    return;
                }

                if ("checked".equals(checked)) {
                    groupUser = new GroupUser();
                    groupUser.setCreatedTime(new Date());
                    groupUser.setGroupId(groupInfo.getGroupId());
                    groupUser.setGroupName(groupInfo.getGroupName());
                    groupUser.setInviterId(mUserId);
                    groupUser.setPlayCount1(0);
                    groupUser.setPlayCount2(0);
                    groupUser.setUserId(userId);
                    groupUser.setUserLevel(1);
                    groupUser.setUserRole(2);
                    groupUser.setUserName(user.getName());
                    groupUser.setUserNickname(user.getName());
                    groupUser.setUserGroup(mGroupUser.getUserGroup());
                    groupUser.setCredit(0l);
                    groupUser.setRefuseInvite(1);
                    if("0".equals(mGroupUser.getUserGroup())){
                        groupUser.setPromoterLevel(1);
                    }
                    if(groupDao.createGroupUser(groupUser)<=0){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                        return;
                    }

                    groupDao.updateGroupInfoCount(1, groupInfo.getGroupId());

                    OutputUtil.output(0, "邀请加入俱乐部成功", getRequest(), getResponse(), false);

                    LOGGER.info("invite join success groupUser={}", JSON.toJSONString(groupUser));

                    return;
                } else {
                    GroupReview groupReview = groupDao.loadGroupReview0(groupId, userId,0);
                    if (groupReview != null) {
                        if(groupReview.getCurrentOperator() != null && groupReview.getReviewMode() ==0)
                            OutputUtil.output(3, "已邀请加入军团", getRequest(), getResponse(), false);
                        //else
//                            OutputUtil.output(3, "请打开管理菜单选择军团消息中同意即可", getRequest(), getResponse(), false);
                        return;
                    }

                    groupReview = new GroupReview();
                    groupReview.setCreatedTime(new Date());
                    groupReview.setCurrentState(0);
                    groupReview.setGroupId(groupId);
                    groupReview.setGroupName(groupInfo.getGroupName());
                    groupReview.setReviewMode(0);
                    groupReview.setUserId(userId);
                    groupReview.setUserName(user.getName());
                    groupReview.setCurrentOperator(mUserId);
                    if(groupDao.createGroupReview(groupReview)<=0){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                        return;
                    }
                    
                    
                    //通知游戏服刷新\
                    GameUtil.refreshState(user.getEnterServer(), userId, 2, "");

                    OutputUtil.output(0, "邀请成功", getRequest(), getResponse(), false);

                    LOGGER.info("invite success:{}", JSON.toJSONString(groupReview));
                }

            } else {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 更改军团信息
     */
    public void updateGroupInfo() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            String payConfig = params.get("payConfig");//支付
            String tableConfig = params.get("tableConfig");//创房
            String quickConfig = params.get("quickConfig");//快速加入
            String autoConfig = params.get("autoConfig");//自动补房
            String matchConfig = params.get("matchConfig");//匹配模式
            String intelligent = params.get("intelligent");//智能组房
            String chatConfig = params.get("chatConfig");  //聊天设置
            String autoQuit = params.get("autoQuit");      //长时间未准备，踢出房间,0关闭功能，单位秒
            String negativeCredit = params.get("negativeCredit");  //是否可负分，0：否，1：是
            String stopCreate = params.get("stopCreate");  // 暂停开房，0否1是
            String tableOrder = params.get("tableOrder");  // 空桌/进行桌显示置后，牌桌列表使用,1：空桌置后，2：进行桌置后

            String tableInvite = params.get("tableInvite");  //是否禁止邀请进房，0：否，1：是
            
            String dismissCount = params.get("dismissCount");  //解散房间次数，0：无限，

            if (payConfig != null) {
                payConfig = payConfig.replace(" ", "+");
                params.put("payConfig", payConfig);
            }
            if (tableConfig != null) {
                tableConfig = tableConfig.replace(" ", "+");
                params.put("tableConfig", tableConfig);
            }
            if (quickConfig != null) {
                quickConfig = quickConfig.replace(" ", "+");
                params.put("quickConfig", quickConfig);
            }
            if (autoConfig != null) {
                autoConfig = autoConfig.replace(" ", "+");
                params.put("autoConfig", autoConfig);
            }
            if (chatConfig != null) {
                chatConfig = chatConfig.replace(" ", "+");
                params.put("chatConfig", chatConfig);
            }

            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            long userId = NumberUtils.toLong(params.get("userId"), -1);
            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            long subId = NumberUtils.toLong(params.get("subId"), 0);

            if(!checkSessCode(userId,params.get("sessCode"))){
                return;
            }

            String groupName = params.get("groupName");
            String descMsg = params.get("descMsg");
            int groupMode = NumberUtils.toInt(params.get("groupMode"), -1);

            if (userId <= 0 || keyId <= 0 || subId < 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupInfo groupInfo = groupDao.loadGroupInfoByKeyId(keyId);
            if (groupInfo == null) {
                OutputUtil.output(2, "军团(或包厢)不存在", getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupInfo.getParentGroup().intValue() == 0 ? groupInfo.getGroupId() : groupInfo.getParentGroup());
            if (groupUser == null) {
                OutputUtil.output(3, "您不是亲友圈成员", getRequest(), getResponse(), false);
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            if (StringUtils.isNotBlank(groupName) && !groupName.equals(groupInfo.getGroupName())) {
                String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
                if (!groupName.matches(regex)) {
                    OutputUtil.output(1, "俱乐部名称仅限字母数字和汉字", getRequest(), getResponse(), false);
                    return;
                }

                groupName = groupName.trim();
                String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
                if (!groupName.equals(groupName0)) {
                    OutputUtil.output(1, "军团名不能包含敏感字符", getRequest(), getResponse(), false);
                    return;
                }
                groupName = filterGroupName(groupName);
                if (!groupName.equals(groupInfo.getGroupName())) {
                    map.put("groupName", groupName);
                }
            }

            String content = params.get("content");
            if (StringUtils.isNotBlank(content)) {
                content = content.trim();
                String content0 = KeyWordsFilter.getInstance().filt(content);
                if (!content.equals(content0)) {
                    OutputUtil.output(1, "公告内容不能包含敏感字符", getRequest(), getResponse(), false);
                    return;
                }
                content = filterGroupName(content);
                if (!content.equals(groupInfo.getContent())) {
                    map.put("content", content);
                }
            }

            String groupState = params.get("groupState");
            if (groupInfo.getParentGroup().intValue() > 0) {
                if (StringUtils.isNotBlank(groupState) && !groupState.equals(groupInfo.getGroupState())) {
                    if ("0".equals(groupState) || "1".equals(groupState) || "-1".equals(groupState)) {
                        // 0关闭，1开启，-1关闭
                        map.put("groupState", groupState);
                    }
                    if ("0".equals(groupState)) {
                        Integer count = groupDao.countSubGroup(groupUser.getGroupId().toString());
                        if (count == 1) {
                            OutputUtil.output(8, "当前只有一个包厢，无法关闭入口", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                }
                if(!GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())){
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                    return;
                }
                if (subId > 0 && subId != groupInfo.getGroupId().intValue()) {
                    GroupInfo groupInfo1 = groupDao.loadGroupInfo(subId, groupInfo.getParentGroup());
                    if (groupInfo1 == null) {
                        map.put("groupId", subId);
                    } else {
                        OutputUtil.output(6, "包厢已存在", getRequest(), getResponse(), false);
                        return;
                    }
                }
            } else {
                if(!GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                    return;
                }
            }

            if (StringUtils.isNotBlank(descMsg)) {
                map.put("descMsg", descMsg);
            }
            if ((groupMode == 0 || groupMode == 1) && groupInfo.getParentGroup().intValue() == 0) {
                map.put("groupMode", groupMode);
            }

            //支付设置
            if (StringUtils.isNotBlank(payConfig)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                if (payConfig.startsWith("-p") || payConfig.startsWith("+p")) {
                    String str = jsonObject.getString("pc");
                    if (str == null) {
                        str = payConfig;
                    } else {
                        String[] tmps = str.split(",");
                        int idx = -1;
                        String pv = payConfig.substring(1);
                        for (int i = 0; i < tmps.length; i++) {
                            if (tmps[i].equals("+" + pv) || tmps[i].equals("-" + pv)) {
                                idx = i;
                                break;
                            }
                        }

                        StringBuilder strBuilder = new StringBuilder();
                        if (idx >= 0) {
                            tmps[idx] = payConfig;
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        } else {
                            strBuilder.append(",").append(payConfig);
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        }
                        str = strBuilder.substring(1);
                    }

                    jsonObject.put("pc", str);
                    map.put("extMsg", jsonObject.toString());
                } else if (payConfig.startsWith("-u") || payConfig.startsWith("+u")) {
                    String str = jsonObject.getString("pu");
                    if (str == null) {
                        str = payConfig;
                    } else {
                        String[] tmps = str.split(",");
                        int idx = -1;
                        String pv = payConfig.substring(1);
                        for (int i = 0; i < tmps.length; i++) {
                            if (tmps[i].equals("+" + pv) || tmps[i].equals("-" + pv)) {
                                idx = i;
                                break;
                            }
                        }

                        StringBuilder strBuilder = new StringBuilder();
                        if (idx >= 0) {
                            tmps[idx] = payConfig;
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        } else {
                            strBuilder.append(",").append(payConfig);
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        }
                        str = strBuilder.substring(1);
                    }

                    jsonObject.put("pu", str);
                    map.put("extMsg", jsonObject.toString());
                } else {
                    OutputUtil.output(5, "payConfig参数错误", getRequest(), getResponse(), false);
                    return;
                }
            }

            //创房设置
            if (StringUtils.isNotBlank(tableConfig)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                if (tableConfig.startsWith("-r") || tableConfig.startsWith("+r")) {
                    String str = jsonObject.getString("cr");
                    if (str == null) {
                        str = tableConfig;
                    } else {
                        String[] tmps = str.split(",");
                        int idx = -1;
                        String pv = tableConfig.substring(1);
                        for (int i = 0; i < tmps.length; i++) {
                            if (tmps[i].equals("+" + pv) || tmps[i].equals("-" + pv)) {
                                idx = i;
                                break;
                            }
                        }

                        StringBuilder strBuilder = new StringBuilder();
                        if (idx >= 0) {
                            tmps[idx] = tableConfig;
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        } else {
                            strBuilder.append(",").append(tableConfig);
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        }
                        str = strBuilder.substring(1);
                    }

                    jsonObject.put("cr", str);
                    map.put("extMsg", jsonObject.toString());
                } else {
                    OutputUtil.output(5, "tableConfig参数错误", getRequest(), getResponse(), false);
                    return;
                }
            }

            // 开启快速创房/开启自由开房
            if (StringUtils.isNotBlank(quickConfig)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                if (quickConfig.startsWith("-q") || quickConfig.startsWith("+q")) {
                    String str = jsonObject.getString("oq");
                    if (str == null) {
                        str = quickConfig;
                    } else {
                        String[] tmps = str.split(",");
                        int idx = -1;
                        String pv = quickConfig.substring(1);
                        for (int i = 0; i < tmps.length; i++) {
                            if (tmps[i].equals("+" + pv) || tmps[i].equals("-" + pv)) {
                                idx = i;
                                break;
                            }
                        }

                        StringBuilder strBuilder = new StringBuilder();
                        if (idx >= 0) {
                            tmps[idx] = quickConfig;
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        } else {
                            strBuilder.append(",").append(quickConfig);
                            for (String tmp : tmps) {
                                strBuilder.append(",").append(tmp);
                            }
                        }
                        str = strBuilder.substring(1);
                    }

                    jsonObject.put("oq", str);
                    if (quickConfig.startsWith("-q")) {
                        jsonObject.remove("ac");//关闭智能开房
                    }
                    map.put("extMsg", jsonObject.toString());
                } else {
                    OutputUtil.output(5, "quickConfig参数错误", getRequest(), getResponse(), false);
                    return;
                }
            }

            // 智能开房
            if (StringUtils.isNotBlank(autoConfig)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                if (groupInfo.getExtMsg() != null && !groupInfo.getExtMsg().contains("-q")) {
                    if (autoConfig.startsWith("-a") || autoConfig.startsWith("+a")) {
                        if (jsonObject.containsKey("match") && autoConfig.startsWith("+a")) {
                            OutputUtil.output(5, "当前为匹配模式，不支持自动补房", getRequest(), getResponse(), false);
                            return;
                        }
                        String str = jsonObject.getString("ac");
                        if (str == null) {
                            str = autoConfig;
                        } else {
                            String[] tmps = str.split(",");
                            int idx = -1;
                            String pv = autoConfig.substring(1);
                            for (int i = 0; i < tmps.length; i++) {
                                if (tmps[i].equals("+" + pv) || tmps[i].equals("-" + pv)) {
                                    idx = i;
                                    break;
                                }
                            }

                            StringBuilder strBuilder = new StringBuilder();
                            if (idx >= 0) {
                                tmps[idx] = autoConfig;
                                for (String tmp : tmps) {
                                    strBuilder.append(",").append(tmp);
                                }
                            } else {
                                strBuilder.append(",").append(autoConfig);
                                for (String tmp : tmps) {
                                    strBuilder.append(",").append(tmp);
                                }
                            }
                            str = strBuilder.substring(1);
                        }

                        jsonObject.put("ac", str);
                        map.put("extMsg", jsonObject.toString());
                    } else {
                        OutputUtil.output(5, "autoConfig参数错误", getRequest(), getResponse(), false);
                        return;
                    }
                } else {
                    OutputUtil.output(5, "请先开启快速开房", getRequest(), getResponse(), false);
                    return;
                }
            }

            // 匹配模式
            if (StringUtils.isNotBlank(matchConfig)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                if (matchConfig.startsWith("match")) {
                    String str = matchConfig.substring(5);
                    jsonObject.put("match", str);
                    jsonObject.remove("ac");
                    map.put("extMsg", jsonObject.toString());
                    autoConfig = null;
                } else {
                    jsonObject.remove("match");
                    map.put("extMsg", jsonObject.toString());
                    //删除匹配中的人员
                    groupDao.deleteGroupMatch(groupInfo.getGroupId().toString());
                }
            }

            //智能组房设置
            if (StringUtils.isNotBlank(intelligent)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                if (!intelligent.equals(jsonObject.put("znZf", intelligent))) {
                    map.put("extMsg", jsonObject.toString());
                }
            }

            //聊天设置
            if (StringUtils.isNotBlank(chatConfig)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("chat", chatConfig);
                map.put("extMsg", jsonObject.toString());
            }

            //长时间未准备，踢出房间，整数，时间单位：秒，0表示关闭此功能
            if (StringUtils.isNotBlank(autoQuit)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("autoQuit", autoQuit);
                map.put("extMsg", jsonObject.toString());
            }

            // 俱乐部是否允许负信用分，0关，1开
            if (StringUtils.isNotBlank(negativeCredit)) {
                if(!GroupConstants.isHuiZhang(groupUser.getUserRole())){
                    OutputUtil.output(4, "权限不够：仅限会长", getRequest(), getResponse(), false);
                    return;
                }
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("negativeCredit", negativeCredit);
                map.put("extMsg", jsonObject.toString());
            }

            // 俱乐部暂停开房，0关，1开
            if (StringUtils.isNotBlank(stopCreate)) {
                if(GroupConstants.isGroupForbidden(groupInfo)){
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                    return;
                }
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("stopCreate", stopCreate);
                map.put("extMsg", jsonObject.toString());
            }

            // 空桌/进行桌显示置后
            if (StringUtils.isNotBlank(tableOrder)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("tableOrder", tableOrder);
                map.put("extMsg", jsonObject.toString());
            }
			
			 // 是否禁止邀请进房，0：否，1：是
            if (StringUtils.isNotBlank(tableInvite)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("tableInvite", tableInvite);
                map.put("extMsg", jsonObject.toString());
            }
            // 申请房间解散次数，0：否，1：是
            if (StringUtils.isNotBlank(dismissCount)) {
                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                jsonObject.put("dismissCount", dismissCount);
                map.put("extMsg", jsonObject.toString());
            }
            
			


            if (map.size() == 0) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else {
                map.put("keyId", groupInfo.getKeyId().toString());

                int ret = groupDao.updateGroupInfoByKeyId(map);

                if (ret > 0 && map.containsKey("groupState") && "0".equals((String) map.get("groupState"))) {
                    //包间关闭入口，同时解散未开局的所有房间
                    String groupId = groupUser.getGroupId().toString();
                    dissGroupTable(groupId, null, groupInfo.getGroupId().toString(), 0);
                }
                OutputUtil.output(0, "军团(或包厢)信息修改成功", getRequest(), getResponse(), false);

                LOGGER.info("update group info success:result={},msg={}", ret, JSON.toJSONString(map));

                if (ret > 0) {
                    if (map.containsKey("groupName")) {
                        if (groupInfo.getParentGroup().intValue() == 0) {
                            //修改俱乐部名字时同步修改玩家身上的俱乐部名
                            groupDao.updateGroupUser(groupName, groupInfo.getGroupId());
                        } else if (groupInfo.getParentGroup().intValue() > 0) {
                            //修改包间名字时，同步修改玩法配置的俱乐部名
                            GroupTableConfig config = groupDao.loadLastGroupTableConfig(groupInfo.getGroupId(), groupInfo.getParentGroup());
                            if (config != null) {
                                HashMap<String, Object> configMap = new HashMap<>();
                                configMap.put("keyId", config.getKeyId());
                                configMap.put("tableName", map.get("groupName"));
                                groupDao.updateGroupTableConfigByKeyId(configMap);
                            }
                        }
                    }
                    if(StringUtils.isNotBlank(groupState)){
                        if("1".equals(groupState)){
                            GroupTableConfig config = groupDao.loadLastGroupTableConfig(groupInfo.getGroupId(), groupInfo.getParentGroup());
                            if(config != null){
                                if(GroupConstants.isAdmin(groupUser.getUserRole())){
                                    GroupUser master = groupDao.loadGroupMaster(groupUser.getGroupId().toString());
                                    if(master != null){
                                        userId = master.getUserId();
                                    }
                                }
                                this.autoCreateGroupTableNew(userId,groupUser.getGroupId(),config.getKeyId());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 更改军团成员信息
     */
    public void updateGroupUser() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            long userId = NumberUtils.toLong(params.get("oUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            String userNickname = params.get("userNickname");
            int userRole = NumberUtils.toInt(params.get("userRole"), -1);
            int credit = NumberUtils.toInt(params.get("credit"), 0);
            int creditLock = NumberUtils.toInt(params.get("creditLock"), -1);
            String userState = params.get("userState");
            if (userId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            }

            if ((userRole >= 0 && userRole <= 2)||userRole==10) {
                long mUserId = NumberUtils.toLong(params.get("mUserId"), -1);
                GroupUser groupUser0 = mUserId <= 0 ? null : groupDao.loadGroupUser(mUserId, groupId);

                if (groupUser0 == null || userId == mUserId) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                if (groupId != groupUser0.getGroupId().longValue()) {
                    groupId = groupUser0.getGroupId();
                    groupUser = groupDao.loadGroupUser(userId, groupId);
                    if (groupUser == null) {
                        OutputUtil.output(5, "不在同一军团", getRequest(), getResponse(), false);
                        return;
                    }
                }

                if (!groupUser.getUserGroup().equals(groupUser0.getUserGroup())){
                    OutputUtil.output(6, "不在同一组，无法操作！", getRequest(), getResponse(), false);
                    return;
                }

                int userRole0 = groupUser0.getUserRole().intValue();

                if (userRole0!= 0 && userRole0 != 10) {
                    OutputUtil.output(6, "您不是群主(或组长)，无法操作！", getRequest(), getResponse(), false);
                    return;
                }

                if (userRole0==0&&!(userRole>=0&&userRole<=2)){
                    OutputUtil.output(4, "不在同一组，无法操作！", getRequest(), getResponse(), false);
                    return;
                }else if (userRole0==10&&!(userRole==10)){
                    OutputUtil.output(4, "小组组长只能转让,不支持升降级", getRequest(), getResponse(), false);
                    return;
                }

                if (userRole == groupUser.getUserRole().intValue()) {
                    OutputUtil.output(6, userRole == 1 ? "已经是管理员，无法晋升！" : "已经是普通成员，无法降级！", getRequest(), getResponse(), false);
                    return;
                }

                if (userRole == 0 || userRole == 10) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("userRole", "2");
                    map.put("keyId", groupUser0.getKeyId().toString());
                    groupDao.updateGroupUserByKeyId(map);

                    LOGGER.info("update group user success:{}", JSON.toJSONString(map));

                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("userRole", userRole);
                map.put("keyId", groupUser.getKeyId().toString());
                groupDao.updateGroupUserByKeyId(map);
                OutputUtil.output(0, (userRole == 0 || userRole == 10) ? "转让成功！" : (groupUser.getUserRole().intValue() > userRole ? "晋升成功！" : "降级成功！"), getRequest(), getResponse(), false);

                LOGGER.info("update group user success:{}", JSON.toJSONString(map));
            } else if ("0".equals(userState)||"1".equals(userState)) {
                long mUserId = NumberUtils.toLong(params.get("mUserId"), -1);
                GroupUser groupUser0 = mUserId <= 0 ? null : groupDao.loadGroupUser(mUserId, groupId);

                if (groupUser0 == null || userId == mUserId) {
                    OutputUtil.output(4, userId == mUserId ? LangMsg.getMsg(LangMsg.code_7) : LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                if (groupId != groupUser0.getGroupId().longValue()) {
                    groupId = groupUser0.getGroupId();
                    groupUser = groupDao.loadGroupUser(userId, groupId);
                    if (groupUser == null) {
                        OutputUtil.output(5, "不在同一军团", getRequest(), getResponse(), false);
                        return;
                    }
                }

                int userRole0 = groupUser0.getUserRole().intValue();

                if (userRole0 == 0) {

                }else if (userRole0 == 1) {
                    int tempRole = groupUser.getUserRole().intValue();
                    if (tempRole==0||tempRole==1||tempRole==10){
                        OutputUtil.output(6, "您无权操作！", getRequest(), getResponse(), false);
                        return;
                    }
                }else if (userRole0 == 10) {
                    if (!groupUser.getUserGroup().equals(groupUser0.getUserGroup())){
                        OutputUtil.output(6, "不在同一组，无法操作！", getRequest(), getResponse(), false);
                        return;
                    }
                }else{
                    OutputUtil.output(6, "您无权操作！", getRequest(), getResponse(), false);
                    return;
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("userLevel", userState);
                map.put("keyId", groupUser.getKeyId().toString());
                groupDao.updateGroupUserByKeyId(map);
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);

                LOGGER.info("update group user success:{}", JSON.toJSONString(map));
            }else if (StringUtils.isNotBlank(userNickname)) {
//                userNickname=new String(userNickname.getBytes("iso-8859-1"),"UTF-8");
                HashMap<String, Object> map = new HashMap<>();
                map.put("userNickname", userNickname);
                map.put("keyId", groupUser.getKeyId());
                groupDao.updateGroupUserByKeyId(map);
                OutputUtil.output(0, "修改昵称成功", getRequest(), getResponse(), false);

                LOGGER.info("update group user success:{}", JSON.toJSONString(map));
            } else if(creditLock != -1){
                // 信用分上锁和解锁
                if(GroupConstants.isMaster(groupUser.getUserRole())){
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                if (creditLock != 1) {
                    creditLock = 0;
                }
                RegInfo user = userDao.getUser(userId);
                if(creditLock == 1 && user.getPlayingTableId() > 0  && !groupCreditDao.isGroupTableOver(groupId, user.getPlayingTableId())){
                    OutputUtil.output(3, "你在牌桌中无法上锁", getRequest(), getResponse(), false);
                    return;
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", groupUser.getKeyId());
                map.put("creditLock", creditLock);
                int ret = groupDao.updateGroupUserByKeyId(map);
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
                LOGGER.info("updateGroupUser|succ|" + ret + "|" + JSON.toJSONString(map));
            }else {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 军团批量踢人
     */
    public void batchFire() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            long mUserId = NumberUtils.toLong(params.get("mUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            if (mUserId<=0||groupId<=0){
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            String userIdsStr = params.get("userIds");
            if (StringUtils.isBlank(userIdsStr)){
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId,0);
            if (groupInfo==null){
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(mUserId,groupId);
            if (groupUser==null){
                OutputUtil.output(3, "您不是该亲友圈成员，无法操作", getRequest(), getResponse(), false);
            }else if (groupUser.getUserRole().intValue()==0){
                if ("all".equals(userIdsStr)){
                    int ret = groupDao.deleteAllGroupUsers(String.valueOf(groupId),null,String.valueOf(mUserId));
                    if (ret>0) {
                        groupDao.updateGroupInfoCount(ret,groupId);
                    }
                }else{
                    String[] userIds = userIdsStr.split(",");
                    List<String> userList = new ArrayList<>(userIds.length);
                    for (String userId : userIds){
                        if (CommonUtil.isPureNumber(userId)){
                            userList.add(userId);
                        }
                    }
                    if (userList.size()>0){
                        int ret = groupDao.deleteGroupUsers(userList,String.valueOf(groupId),null);
                        if (ret>0) {
                            groupDao.updateGroupInfoCount(ret,groupId);
                        }
                    }
                }
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            }else if (groupUser.getUserRole().intValue()==10){
                if ("all".equals(userIdsStr)){
                    int ret = groupDao.deleteAllGroupUsers(String.valueOf(groupId),groupUser.getUserGroup(),String.valueOf(mUserId));
                    if (ret>0) {
                        groupDao.updateGroupInfoCount(ret,groupId);
                    }
                }else{
                    String[] userIds = userIdsStr.split(",");
                    List<String> userList = new ArrayList<>(userIds.length);
                    for (String userId : userIds){
                        if (CommonUtil.isPureNumber(userId)){
                            userList.add(userId);
                        }
                    }
                    if (userList.size()>0){
                        int ret = groupDao.deleteGroupUsers(userList,String.valueOf(groupId),groupUser.getUserGroup());
                        if (ret>0) {
                            groupDao.updateGroupInfoCount(ret,groupId);
                        }
                    }
                }
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            }else{
                OutputUtil.output(5, "权限不够，无法操作", getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 军团踢人
     */
    public void fire() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("oUserId"), -1);
            long mUserId = NumberUtils.toLong(params.get("mUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (userId <= 0 || mUserId <= 0 || mUserId == userId) {
                OutputUtil.output(1, mUserId == userId ? "不可踢除自己" : LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser0 = groupDao.loadGroupUser(mUserId, groupId);
            if (groupUser0 == null) {
                OutputUtil.output(2, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            } 
            
            int userRole0 =groupUser0.getUserRole().intValue();
            if (userRole0==0 || userRole0==10 ||userRole0 ==1) {

            }else{
                OutputUtil.output(3, "您不是群主(或组长)，无法删除！", getRequest(), getResponse(), false);
                return;
            }

            if (groupId != groupUser0.getGroupId().longValue()) {
                groupId = groupUser0.getGroupId();
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);

            if (groupUser == null) {
                OutputUtil.output(4, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            } else if (!groupUser.getGroupId().equals(groupUser0.getGroupId())) {
                OutputUtil.output(5, "不在同一军团，无权操作", getRequest(), getResponse(), false);
                return;
            } else if (userRole0 != 0 && groupUser.getUserRole().intValue() <= 1) {
                OutputUtil.output(6, "您不是群主，无法删除！", getRequest(), getResponse(), false);
                return;
            } else if(groupUser.getUserRole().intValue()==10){
                OutputUtil.output(6, "不能踢除小组长", getRequest(), getResponse(), false);
                return;
            } else if(userRole0==10&&!groupUser.getUserGroup().equals(groupUser0.getUserGroup())){
                OutputUtil.output(5, "不在同一小组，无权操作", getRequest(), getResponse(), false);
                return;
            } else if( groupUser.getCredit() != 0 || groupUser.getCreditPurse() != 0){
                OutputUtil.output(7, "该玩家当前比赛分不为0无法踢出", getRequest(), getResponse(), false);
                return;
            } else if( GroupConstants.isPromotor(groupUser.getUserRole())){
                OutputUtil.output(7, "该玩家是拉手，不能在此处踢出", getRequest(), getResponse(), false);
                return;
            }else {

                int ret = groupDao.deleteGroupUserByKeyId(groupUser.getKeyId());
                if (ret > 0) {
                    LOGGER.info("fire group user success:userId={},groupUser={}",groupUser.getUserId(), JSON.toJSONString(groupUser));

                    groupDao.updateGroupInfoCount(-ret, groupUser.getGroupId());
                    OutputUtil.output(0, "删除成功！", getRequest(), getResponse(), false);

                    GroupReview groupReview = new GroupReview();
                    groupReview.setCreatedTime(new Date());
                    groupReview.setCurrentState(4);
                    groupReview.setGroupId(groupUser.getGroupId().longValue());
                    groupReview.setGroupName(groupUser.getGroupName());
                    groupReview.setReviewMode(2);
                    groupReview.setUserId(groupUser.getUserId());
                    groupReview.setUserName(groupUser.getUserName());
                    groupReview.setCurrentOperator(mUserId);
                    groupReview.setOperateTime(groupReview.getCreatedTime());
                    groupDao.createGroupReview(groupReview);

                    LOGGER.info("fire:groupReview={}", JSON.toJSONString(groupReview));

                    // 写入日志
                    insertGroupUserAlert(groupUser.getGroupId(), groupUser.getUserId(), mUserId, GroupConstants.TYPE_USER_ALERT_DELETE);
                } else {
                    OutputUtil.output(7, "尚未加入军团", getRequest(), getResponse(), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 解散军团或者包厢
     */
    public void dissGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
//            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (userId <= 0 || keyId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfoByKeyId(keyId);
            if (groupInfo == null) {
                OutputUtil.output(6, "军团(或包厢)不存在", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupInfo.getParentGroup().intValue() == 0 ? groupInfo.getGroupId() : groupInfo.getParentGroup());
            if (groupUser == null) {
                OutputUtil.output(2, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            } else if (groupUser.getUserRole().intValue() != 0) {
                OutputUtil.output(3, "无权解散军团", getRequest(), getResponse(), false);
                return;
            } else {
                if ((groupInfo.getParentGroup().intValue() == 0 && groupUser.getGroupId().intValue() != groupInfo.getGroupId().intValue())
                        || (groupInfo.getParentGroup().intValue() > 0 && groupUser.getGroupId().intValue() != groupInfo.getParentGroup().intValue())) {
                    OutputUtil.output(4, "不是该军团成员", getRequest(), getResponse(), false);
                    return;
                } else if (groupInfo.getParentGroup().intValue() > 0) {
                    int ret = groupDao.deleteGroupInfoByGroupId(groupInfo.getGroupId(), groupInfo.getParentGroup());
                    int ret0 = groupDao.deleteGroupTableConfig(groupInfo.getGroupId().toString(), groupInfo.getParentGroup().toString());

                    OutputUtil.output(0, "解散包厢成功", getRequest(), getResponse(), false);

                    LOGGER.info("dissSubGroup success:groupId={},name={},userId={},ret={},config={}", groupUser.getGroupId(), groupInfo.getGroupName(), userId, ret, ret0);
                } else {
                    int count = groupDao.countGroupUser(groupUser.getGroupId());
                    if (count <= 1) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("keyId", groupInfo.getKeyId().toString());
                        map.put("groupState", "0");
                        int ret1 = groupDao.deleteGroupUserByGroupId(groupUser.getGroupId());
//                        int ret2 = groupDao.deleteGroupInfoByGroupId(groupUser.getGroupId(),0);
                        int ret2 = groupDao.updateGroupInfoByKeyId(map);//军团逻辑删除
                        int ret3 = groupDao.deleteGroupInfoByParentGroup(groupUser.getGroupId());
                        groupDao.deleteTeamByGroupKey(groupUser.getGroupId().toString());

                        OutputUtil.output(0, "解散军团成功", getRequest(), getResponse(), false);

                        LOGGER.info("dissGroup success:groupId={},userId={},ret1={},ret2={},ret3={}", groupUser.getGroupId(), userId, ret1, ret2, ret3);
                    } else {
                        OutputUtil.output(5, "请先踢除成员再解散", getRequest(), getResponse(), false);

                        LOGGER.info("dissGroup fail:groupId={},userId={},count={}", groupUser.getGroupId(), userId, count);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 主动退出军团
     */
    public void exitGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (userId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            int userRole;
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            Map<String, Object> mapCount = new HashMap<>(8);
            mapCount.put("groupId", String.valueOf(groupId));
            mapCount.put("keyWord", userId);
            mapCount.put("startDate", TimeUtil.getStartOfDay(-15));
            mapCount.put("endDate", TimeUtil.getEndOfDay(0));
            int count = this.groupDao.countAnyGroupCreditLog(mapCount);

            if (groupUser == null) {
                OutputUtil.output(2, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            } else if ((userRole=groupUser.getUserRole().intValue()) == 0||userRole == 10) {
                OutputUtil.output(3, "群主(或组长)不能退出", getRequest(), getResponse(), false);
                return;
            } else if ("1".equals(PropertiesCacheUtil.getValueOrDefault("club_group_exit","1",Constants.GAME_FILE))&&!"0".equals(groupUser.getUserGroup()) && (groupInfo.getIsCredit() == 0 || count > 0)) {
                //15天没有产生上下分记录的组内成员才可以自主退出
                OutputUtil.output(5, "您不能主动退出，请联系群主(或组长)", getRequest(), getResponse(), false);
                return;
            } else if (groupUser.getCredit() > 0) {
                OutputUtil.output(6, "您的比赛分不为零，暂时不能退出，请联系群主或组长！", getRequest(), getResponse(), false);
                return;
            } else if (groupUser.getCreditPurse() > 0) {
                OutputUtil.output(7, "您的零钱包不为零，暂时不能退出，请联系群主或组长！", getRequest(), getResponse(), false);
                return;
            }else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
                OutputUtil.output(8, "您是拉手，暂时不能退出，请联系群主或组长！", getRequest(), getResponse(), false);
                return;
            } else {
                long credit = groupUser.getCredit();
                int ret = groupDao.deleteGroupUserByKeyId(groupUser.getKeyId());
                if (ret > 0) {
                    groupDao.updateGroupInfoCount(-ret, groupUser.getGroupId());
                    GroupUser master = groupDao.loadGroupMaster(groupUser.getGroupId()+"");
                    if(master != null && credit > 0){
                        // groupDao.updateGroupUserCredit(map);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("credit", credit);
                        map.put("keyId", master.getKeyId());
                        groupDao.updateGroupUserCredit(map);
                    }

                    OutputUtil.output(0, "退出军团成功", getRequest(), getResponse(), false);
                    LOGGER.info("exit group scuccess:userId={},groupUser={}",userId, JSON.toJSONString(groupUser));

                    GroupReview groupReview = new GroupReview();
                    groupReview.setCreatedTime(new Date());
                    groupReview.setCurrentState(3);
                    groupReview.setGroupId(groupUser.getGroupId().longValue());
                    groupReview.setGroupName(groupUser.getGroupName());
                    groupReview.setReviewMode(2);
                    groupReview.setUserId(groupUser.getUserId());
                    groupReview.setUserName(groupUser.getUserName());
                    groupDao.createGroupReview(groupReview);

                    LOGGER.info("exit:groupReview={}", JSON.toJSONString(groupReview));
                } else {
                    OutputUtil.output(4, "尚未加入军团", getRequest(), getResponse(), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 玩家应答军团消息
     */
    public void responseGroupReview() {
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String type = params.get("msgType");
            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            String value = params.get("value");

            if (keyId <= 0 || userId <= 0 || (!"0".equals(value) && !"1".equals(value)) || (!"0".equals(type) && !"1".equals(type))) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            synchronized (GroupAction.class) {
                GroupReview groupReview = groupDao.loadGroupReviewByKeyId(keyId);
                if (groupReview == null) {
                    OutputUtil.output(3, "消息ID错误", getRequest(), getResponse(), false);
                    return;
                } else if (groupReview.getCurrentState().intValue() > 0) {
                    OutputUtil.output(4, "消息已处理", getRequest(), getResponse(), false);
                    return;
                }

                GroupUser groupUser = groupDao.loadGroupUser(userId, groupReview.getGroupId());

                if ("0".equals(type)) {//玩家应答

                    if (groupReview.getUserId().longValue() != userId) {
                        OutputUtil.output(5, "userId错误", getRequest(), getResponse(), false);
                        return;
                    }

                    if ("1".equals(value)) {
                        if (groupReview.getReviewMode().intValue() != 0) {
                            OutputUtil.output(6, "type错误", getRequest(), getResponse(), false);
                            return;
                        } else if (groupUser != null) {
                        	  HashMap<String, Object> map = new HashMap<>();
                              map.put("keyId", String.valueOf(keyId));
                              map.put("currentState", "2");
                              map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                              groupDao.updateGroupReviewByKeyId(map);
                            OutputUtil.output(4, "已加入军团", getRequest(), getResponse(), false);
                            return;
                        }

                        GroupInfo groupInfo = groupDao.loadGroupInfo(groupReview.getGroupId(), 0);

                        if (groupInfo == null) {
                            OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                            return;
                        } else if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                            OutputUtil.output(8, "军团人员已满", getRequest(), getResponse(), false);
                            return;
                        }

                        int groupCount = groupDao.loadGroupCount(groupReview.getUserId());
                        if (groupCount >= 20) {
                            OutputUtil.output(3, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                            return;
                        }

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("keyId", String.valueOf(keyId));
                        map.put("currentState", "1");
                        map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        map.put("currentState", "1");
                        groupDao.updateGroupReviewByKeyId(map);

                        LOGGER.info("response groupReview={}", JSON.toJSONString(map));

                        Long operatorUid =  groupReview.getCurrentOperator();
                        GroupUser operatorGroupUser = groupDao.loadGroupUser(operatorUid, groupReview.getGroupId().intValue());
                        String userGroup = "0";
                        if (operatorGroupUser != null) {
                            userGroup = operatorGroupUser.getUserGroup();
                        }

                        groupUser = new GroupUser();
                        groupUser.setCreatedTime(new Date());
                        groupUser.setGroupId(groupReview.getGroupId().intValue());
                        groupUser.setGroupName(groupReview.getGroupName());
                        groupUser.setInviterId(groupReview.getReviewMode().intValue() == 1 ? userId : groupReview.getCurrentOperator());
                        groupUser.setPlayCount1(0);
                        groupUser.setPlayCount2(0);
                        groupUser.setUserId(userId);
                        groupUser.setUserLevel(1);
                        groupUser.setUserRole(2);
                        groupUser.setUserName(groupReview.getUserName());
                        groupUser.setUserNickname(groupReview.getUserName());
                        groupUser.setUserGroup(userGroup);
                        groupUser.setRefuseInvite(1);
                        groupUser.setCredit(0l);
                        if(!"0".equals(userGroup)){
                            groupUser.setPromoterLevel(1);
                        }
                        if(groupDao.createGroupUser(groupUser)<=0){
                            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                            return;
                        }

                        groupDao.updateGroupInfoCount(1, groupInfo.getGroupId());

                        OutputUtil.output(0, "加入军团成功", getRequest(), getResponse(), false);

                        // 写入变动日志
                        insertGroupUserAlert(groupUser.getGroupId(), groupUser.getUserId(), operatorUid, GroupConstants.TYPE_USER_ALERT_INVITE);

                        
                        //通知游戏服刷新\
                        RegInfo user = userDao.getUser(userId);
                        GameUtil.refreshState( user.getEnterServer(), userId, 1, "");
                        
                        LOGGER.info("response groupUser={}", JSON.toJSONString(groupUser));

                    } else {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("keyId", String.valueOf(keyId));
                        map.put("currentState", "2");
                        map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        groupDao.updateGroupReviewByKeyId(map);

                        OutputUtil.output(0, "拒绝加入军团成功", getRequest(), getResponse(), false);

                        LOGGER.info("response groupReview={}", JSON.toJSONString(map));
                    }

                } else if ("1".equals(type)) {//管理员应答

                    if (groupUser == null) {
                        OutputUtil.output(4, "userId错误", getRequest(), getResponse(), false);
                        return;
                    }

                    if (groupUser.getUserRole().intValue() > 1) {
                        OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                        return;
                    }

                    GroupUser groupUser0 = groupDao.loadGroupUser(groupReview.getUserId(), groupReview.getGroupId());
                    if ("1".equals(value)) {
                        if (groupReview.getReviewMode().intValue() != 1) {
                            OutputUtil.output(6, "type错误", getRequest(), getResponse(), false);
                            return;
                        } else if (groupUser0 != null) {
                            OutputUtil.output(4, "已加入军团", getRequest(), getResponse(), false);
                            return;
                        }

                        GroupInfo groupInfo = groupDao.loadGroupInfo(groupReview.getGroupId(), 0);

                        if (groupInfo == null) {
                            OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                            return;
                        } else if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                            OutputUtil.output(8, "军团人员已满", getRequest(), getResponse(), false);
                            return;
                        }

                        int groupCount = groupDao.loadGroupCount(groupReview.getUserId());
                        if (groupCount >= 20) {
                            OutputUtil.output(3, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                            return;
                        }

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("keyId", String.valueOf(keyId));
                        map.put("currentState", "1");
                        map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        map.put("currentState", "1");
                        map.put("currentOperator", groupUser.getUserId());
                        groupDao.updateGroupReviewByKeyId(map);

                        LOGGER.info("response groupReview={}", JSON.toJSONString(map));
                        long inviterId = groupReview.getReviewMode().intValue() == 1 ? userId : groupReview.getCurrentOperator();
                        groupUser = new GroupUser();
                        groupUser.setCreatedTime(new Date());
                        groupUser.setGroupId(groupReview.getGroupId().intValue());
                        groupUser.setGroupName(groupReview.getGroupName());
                        groupUser.setInviterId(inviterId);
                        groupUser.setPlayCount1(0);
                        groupUser.setPlayCount2(0);
                        groupUser.setUserId(groupReview.getUserId());
                        groupUser.setUserLevel(1);
                        groupUser.setUserRole(2);
                        groupUser.setUserName(groupReview.getUserName());
                        groupUser.setUserNickname(groupReview.getUserName());
                        groupUser.setUserGroup("0");
                        groupUser.setRefuseInvite(1);
                        groupUser.setCredit(0l);
                        if(groupDao.createGroupUser(groupUser)<=0){
                            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                            return;
                        }

                        groupDao.updateGroupInfoCount(1, groupInfo.getGroupId());

                        // 写入变动日志
                        insertGroupUserAlert(groupUser.getGroupId(), groupUser.getUserId(), inviterId, GroupConstants.TYPE_USER_ALERT_APPLY);

                        //通知游戏服刷新\
                        RegInfo user = userDao.getUser(groupReview.getUserId());
                        GameUtil.refreshState( user.getEnterServer(), groupReview.getUserId(), 1, "");
                        
                        OutputUtil.output(0, "加入军团成功", getRequest(), getResponse(), false);

                        LOGGER.info("response groupUser={}", JSON.toJSONString(groupUser));

                    } else {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("keyId", String.valueOf(keyId));
                        map.put("currentState", "2");
                        map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        map.put("currentOperator", groupUser.getUserId());
                        groupDao.updateGroupReviewByKeyId(map);

                        OutputUtil.output(0, "拒绝加入军团成功", getRequest(), getResponse(), false);

                        LOGGER.info("response groupReview={}", JSON.toJSONString(map));
                    }

                } else {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    public void groudReadPiont(){
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("oUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);

            JSONObject json = new JSONObject();

            //被邀请的红点
            List<GroupReview> list = groupDao.loadGroupReviewByUserId(userId);
            for (GroupReview gr : list ) {
                if(gr.getCurrentOperator() != null){
                    json.put("inviteReadPoint",1);
                    break;
                }
            }
            if(groupUser != null) {
                if (groupUser.getUserRole().intValue() < 2 && groupId > 0) {        //仅限群主和管理员 且待带 groupId参数时
                    //申请的红点
                    List<GroupReview> listA = groupDao.loadGroupReviewByUserId(userId);
                    for (GroupReview gr : listA) {
                        if (gr.getCurrentOperator() == null) {
                            json.put("applyReadPoint", 1);
                            break;
                        }
                    }
                }
            }
            OutputUtil.output(0, json, getRequest(), getResponse(), false);

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 查询未处理的军团消息
     */
    public void searchGroupReview() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String type = params.get("msgType");
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (userId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            if ("0".equals(type)) {//玩家消息
                List<GroupReview> list = groupDao.loadGroupReviewByUserId(userId);
                MessageBuilder mb = MessageBuilder.newInstance();
                if (list == null || list.size() == 0) {
                    list = Collections.emptyList();
                } else {
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    HashSet<Long> groupIds = new HashSet<>();
                    for (GroupReview gr : list) {
                        if(gr.getReviewMode() == 0){
                            if (groupIds.add(gr.getGroupId())) {
                                GroupInfo groupInfo = groupDao.loadGroupInfo(gr.getGroupId(), 0);
                                if (groupInfo != null) {
                                    GroupUser gu = groupDao.loadGroupMaster(gr.getGroupId().toString());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("groupId", gr.getGroupId());
                                    map.put("currentCount", groupInfo.getCurrentCount());
                                    if (gu != null) {
                                        map.put("masterName", gu.getUserName());
                                        Map<String, Object> userMap =  userDao.loadUserBase(gu.getUserId().toString());
                                        if(userMap.get("headimgurl") != null)
                                            map.put("headimgurl",userMap.get("headimgurl"));
                                    }
                                    dataList.add(map);
                                }
                            }
                        }else if(gr.getReviewMode() == 3){
                            GroupInfo groupInfo = groupDao.loadGroupInfo(gr.getGroupId(), 0);
                            if (groupInfo != null) {
                                GroupUser gu = groupDao.loadGroupUser(gr.getCurrentOperator(), gr.getGroupId());
                                Map<String, Object> map = new HashMap<>();
                                map.put("groupId", gr.getGroupId());
                                map.put("currentCount", groupInfo.getCurrentCount());
                                if (gu != null) {
                                    map.put("masterName", gu.getUserName());
                                    Map<String, Object> userMap =  userDao.loadUserBase(gu.getUserId().toString());
                                    if(userMap.get("headimgurl") != null)
                                        map.put("headimgurl",userMap.get("headimgurl"));
                                }
                                dataList.add(map);
                            }
                        }
                    }
                    mb.builder("data", dataList);
                }
                mb.builderCodeMessage(0, list);
                OutputUtil.output(mb, getRequest(), getResponse(), null, false);
            } else if ("1".equals(type)) {//管理员消息
                long groupId = NumberUtils.toLong(params.get("groupId"), -1);
                if (groupId <= 0) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                GroupUser self = groupDao.loadGroupUser(userId, groupId);
                if (self == null) {
                    OutputUtil.output(2, "您还没有加入军团", getRequest(), getResponse(), false);
                    return;
                } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                    return;
                }
                List<GroupReview> list = groupDao.loadGroupReviewByGroupId(groupId);
                if (list == null || list.size() == 0) {
                    OutputUtil.output(0, Collections.emptyList(), getRequest(), getResponse(), false);
                } else {
                    List<Map<String, Object>> list1 = new ArrayList<>(list.size());
                    for (GroupReview obj : list) {
                        JSONObject json = (JSONObject) JSONObject.toJSON(obj);
                        Map<String, Object> map = userDao.loadUserBase(obj.getUserId().toString());
                        if (map != null) {
                            json.putAll(map);
                        }
                        list1.add(json);
                    }
                    OutputUtil.output(0, list1, getRequest(), getResponse(), false);
                }
            } else {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 如果俱乐部名字中包含了：元 角 毛 分 块 的，用*号代替
     * @param groupName
     * @return
     */
    private static final String filterGroupName(String groupName) {
        if(InitData.groupFilterWords.isEmpty())
            return groupName;
        List<String> filterMoneyName = InitData.groupFilterWords;
        for(String moneyName : filterMoneyName) {
            if(groupName.contains(moneyName)) {
                groupName = groupName.replace(moneyName, "*");
            }
        }
        return groupName;
    }
    /**
     * 设置亲友圈是否拒绝游戏邀请
     */
	public void setGroupIsRefuseInvite() {
		try {
			Map<String, String> params = UrlParamUtil.getParameters(getRequest());
			LOGGER.info("params:{}", params);
			if (!checkSign(params)) {
				OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
				return;
			}
			long uid = NumberUtils.toLong(params.get("uid"), -1);
			long gid = NumberUtils.toLong(params.get("gid"), -1);
			int status = NumberUtils.toInt(params.get("status"), -1);
			if (uid < 0 || gid < 0 || status < 0) {
				OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
			} else {
				GroupUser groupUser = groupDao.loadGroupUser(uid, gid);
				if (groupUser != null) {
					if (groupUser.getRefuseInvite() != status) {
						groupUser.setRefuseInvite(status);
						groupDao.updateGroupUserGameInvite(gid, uid, status);
					}
					String msg = "";
					if (status == 0) {
						msg = "拒绝接收亲友圈成员邀请信息";
					} else {
						msg = "开启接收亲友圈成员邀请信息";
					}
					OutputUtil.output(0, msg, getRequest(), getResponse(), false);
				} else {
					OutputUtil.output(0, "无效的亲友圈ID或亲友圈玩家ID", getRequest(), getResponse(), false);
				}
			}
		} catch (Exception e) {
			OutputUtil.output(0, "无效的玩家状态", getRequest(), getResponse(), false);
			LOGGER.error("Exception:" + e.getMessage(), e);
		}
	}

    /**
     * 获取玩家信息
     */
    public void loadUser() {
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("oUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            String checkGroup = params.get("checkGroup");
            if (userId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                RegInfo user = userDao.getUser(userId);
                if (user == null) {
                    OutputUtil.output(2, "ID错误:" + userId, getRequest(), getResponse(), false);
                    return;
                }

                JSONObject json = new JSONObject();
                json.put("userId", user.getUserId());
                json.put("name", user.getName());
                json.put("sex", user.getSex());
                json.put("enterServer", user.getEnterServer());
                json.put("headimgurl", user.getHeadimgurl());
                json.put("isOnLine", user.getIsOnLine());

                if ("1".equals(checkGroup)) {
                    GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                    if (groupUser != null) {
                        json.put("userLevel", groupUser.getUserLevel());
                        json.put("userRole", groupUser.getUserRole());
                        json.put("refuseInvite", groupUser.getRefuseInvite());
                        GroupInfo groupInfo = groupDao.loadGroupInfo(groupUser.getGroupId(), 0);
                        if (groupInfo != null) {
                            json.put("groupKeyId", groupInfo.getKeyId());
                            json.put("groupId", groupInfo.getGroupId());
                            json.put("groupName", filterGroupName(groupInfo.getGroupName()));
                            json.put("currentCount", groupInfo.getCurrentCount());
                            
                            json.put("content",groupInfo.getContent() );
                            JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                            if ("1".equals(PropertiesCacheUtil.getValueOrDefault("group_kf_oq", "0",Constants.GAME_FILE))) {
                                String str = jsonObject.getString("oq");
                                if (StringUtils.isBlank(str)) {
                                    jsonObject.put("oq", "-q");
                                }
                            }
                            if (!jsonObject.containsKey("ac")) {
                                jsonObject.put("ac", "-a");
                            }
                            json.put("extMsg", jsonObject.toString());
                            json.put("creditOpen", groupInfo.getIsCredit());
                            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
                        } else {
                            json.put("groupId", groupUser.getGroupId());
                            json.put("groupName", filterGroupName(groupUser.getGroupName()));
                            json.put("creditOpen", 0); //是否开启信用房
                            json.put("creditAllotMode", 1);
                        }

                        if (groupUser.getUserRole().intValue() == 0) {
                            Map<String, Object> map1 = userDao.loadUserBaseNoCache(groupUser.getUserId().toString());
                            if(map1!=null) {
                                json.put("masterImg", map1.get("headimgurl"));
                                json.put("payBindId", map1.get("payBindId"));
                            }
                        } else {
                            GroupUser groupUser0 = groupDao.loadGroupMaster(groupUser.getGroupId().toString());
                            if (groupUser0 != null) {
                                Map<String, Object> map1 = userDao.loadUserBaseNoCache(groupUser0.getUserId().toString());
                                if(map1!=null) {
                                    json.put("masterImg", map1.get("headimgurl"));
                                    json.put("payBindId", map1.get("payBindId"));
                                }
                            } else {
                                json.put("masterImg", "");
                            }
                        }

                        json.put("tables", groupDao.countGroupTables(groupUser.getGroupId()));
                        json.put("groupUserLevel", groupUser.getUserLevel());
                        json.put("groupUserRole", groupUser.getUserRole());
                        json.put("userGroup",groupUser.getUserGroup());
                        json.put("refuseInvite", groupUser.getRefuseInvite());
                        json.put("promoterLevel", groupUser.getPromoterLevel());
                        json.put("credit", groupUser.getCredit());
                        json.put("creditLock",groupUser.getCreditLock());
                    } else {
                        json.put("groupId", 0);
                    }
                } else if ("2".equals(checkGroup)) {
                    int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
                    int pageSize = NumberUtils.toInt(params.get("pageSize"), 10);
                    if (pageNo < 1) {
                        pageNo = 1;
                    }
                    if (pageSize < 1) {
                        pageSize = 10;
                    }
                    List<GroupUser> groupUsers = groupDao.loadGroupUsersByUser(userId, -1, pageNo, pageSize);
                    if (groupUsers != null && groupUsers.size() > 0) {
                        List<HashMap<String, Object>> list = new ArrayList<>(groupUsers.size());
                        for (GroupUser gu : groupUsers) {
                            HashMap<String, Object> map = new HashMap<>();

                            GroupInfo groupInfo = groupDao.loadGroupInfo(gu.getGroupId(), 0);
                            if (groupInfo != null) {
                                map.put("groupKeyId", groupInfo.getKeyId());
                                map.put("groupId", groupInfo.getGroupId());
                                map.put("groupName", filterGroupName(groupInfo.getGroupName()));
                                map.put("content",groupInfo.getContent() );
                                map.put("currentCount", groupInfo.getCurrentCount());
                                map.put("refuseInvite", gu.getRefuseInvite());
                                JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
                                if ("1".equals(PropertiesCacheUtil.getValueOrDefault("group_kf_oq", "0",Constants.GAME_FILE))) {
                                    String str = jsonObject.getString("oq");
                                    if (StringUtils.isBlank(str)) {
                                        jsonObject.put("oq", "-q");
                                    }
                                }
                                if (!jsonObject.containsKey("ac")) {
                                    jsonObject.put("ac", "-a");
                                }
                                map.put("extMsg", jsonObject.toString());
                                map.put("creditOpen", groupInfo.getIsCredit());
                                map.put("creditAllotMode", groupInfo.getCreditAllotMode());
                            } else {
                                map.put("creditOpen", 0); //是否开启信用房
                                map.put("creditAllotMode", 1);
                                int t = 0;//groupDao.deleteGroupUserByGroupId(gu.getGroupId());
                                LOGGER.info("group is not exists,delete all group user:groupId={},count={}", gu.getGroupId(), t);
                                continue;
//                                map.put("groupId", gu.getGroupId());
//                                map.put("groupName", gu.getGroupName());
                            }
                            map.put("userLevel", gu.getUserLevel());
                            map.put("userRole", gu.getUserRole());
                            map.put("promoterLevel", gu.getPromoterLevel());

                            if (gu.getUserRole().intValue() == 0) {
                                Map<String, Object> map1 = userDao.loadUserBaseNoCache(gu.getUserId().toString());
                                if(map1!=null) {
                                    map.put("masterImg", map1.get("headimgurl"));
                                    map.put("payBindId", map1.get("payBindId"));
                                }
                            } else {
                                GroupUser groupUser = groupDao.loadGroupMaster(gu.getGroupId().toString());
                                if (groupUser != null) {
                                    Map<String, Object> map1 = userDao.loadUserBaseNoCache(groupUser.getUserId().toString());
                                    if(map1!=null) {
                                        map.put("masterImg", map1.get("headimgurl"));
                                        map.put("payBindId", map1.get("payBindId"));
                                    }
                                } else {
                                    map.put("masterImg", "");
                                }
                            }

                            map.put("tables", groupDao.countGroupTables(gu.getGroupId()));
                            map.put("userGroup",gu.getUserGroup());
                            map.put("creditLock",gu.getCreditLock());
                            if (userId == gu.getUserId().longValue()) {
                                map.put("credit", gu.getCredit());
                            }
                            list.add(map);
                        }
                        json.put("list", list);
                    } else {
                        json.put("list", Collections.<GroupUser>emptyList());
                    }
                }
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取军团成员信息
     */
    public void loadGroupUsers() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            String keyWord = params.get("keyWord");
            String credit = params.get("credit");
            
          //白金岛传入排序规则
            String orderRule = params.get("orderRule");
            
            if(orderRule == null || orderRule == "") {
            	orderRule = "DESC";
            }else {
            	if(!"asc".equalsIgnoreCase(orderRule) && !"desc".equalsIgnoreCase(orderRule)){
            		orderRule = "DESC";
            	}
            }
            

            if (groupId <= 0 || pageNo <= 0 || pageSize <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else if(!"1".equals(credit)){
                String userGroup = params.get("userGroup");
                //俱乐部玩家列表
                if (pageSize > 1000) {
                    pageSize = 1000;
                }

                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                }

                List<HashMap<String,Object>> list;
                JSONObject json = new JSONObject();
                json.put("pageNo", pageNo);
                json.put("pageSize", pageSize);

                if (StringUtils.isEmpty(keyWord)&&StringUtils.isBlank(userGroup)) {
                    list = groupDao.loadGroupUserMsgs(groupId, pageNo, pageSize, userGroup,orderRule);
                    json.put("pages", (int) Math.ceil(groupInfo.getCurrentCount() * 1.0 / pageSize));
                } else {
                    list = groupDao.loadGroupUserMsgs(groupId, pageNo, pageSize, keyWord, userGroup,orderRule);
                    json.put("pages", (int) Math.ceil(groupDao.countGroupUser(groupId, keyWord, userGroup) * 1.0 / pageSize));
                }

                //对不同身份关系做屏蔽处理
                long userId = NumberUtils.toLong(params.get("userId"), 0);
                GroupUser mGroupUser = groupDao.loadGroupUser(userId, groupId);
                if (mGroupUser != null) {
                    // 处理是否隐藏用户id
                    if (GroupConstants.isTeamLeader(mGroupUser.getUserRole())) {
                        // 小组长：能查看自己所有关系链的成员id，群主管理员id；
                        for (HashMap<String, Object> map : list) {
                            Long uId = Long.parseLong(String.valueOf(map.get("userId")));
                            if (uId.equals(mGroupUser.getUserId())) {// 自己
                                continue;
                            }
                            int ur = Integer.parseInt(String.valueOf(map.get("userRole")));
                            if (GroupConstants.isMasterOrAdmin(ur)) {// 群主、管理员
                                continue;
                            }
                            String ug = String.valueOf(map.get("userGroup"));
                            if (mGroupUser.getUserGroup().equals(ug)) { // 组内成员
                                continue;
                            }
                            map.put("hideId", 1);
                        }
                    } else if (GroupConstants.isPromotor(mGroupUser.getUserRole())) {
                        // 拉手：能看到自己下级关系链的成员id，直接上级id，群主管理员id；
                        for (HashMap<String, Object> map : list) {
                            Long uId = Long.parseLong(String.valueOf(map.get("userId")));
                            if (uId.equals(mGroupUser.getUserId())) {// 自己
                                continue;
                            }
                            int ur = Integer.parseInt(String.valueOf(map.get("userRole")));
                            if (GroupConstants.isMasterOrAdmin(ur)) {// 群主、管理员
                                continue;
                            }
                            if (mGroupUser.getPromoterLevel() == 1) {
                                String ug = String.valueOf(map.get("userGroup"));
                                if (mGroupUser.getUserGroup().equals(ug) && GroupConstants.isTeamLeader(ur)) {// 小组长
                                    continue;
                                }
                                long promoterId1 = Long.parseLong(String.valueOf(map.get("promoterId1")));
                                if (mGroupUser.getUserId() == promoterId1) {// 下级成员
                                    continue;
                                }
                            } else if (mGroupUser.getPromoterLevel() == 2) {
                                if (mGroupUser.getPromoterId1() == uId) {// 直接上级
                                    continue;
                                }
                                long promoterId2 = Long.parseLong(String.valueOf(map.get("promoterId2")));
                                if (mGroupUser.getUserId() == promoterId2) {// 下级成员
                                    continue;
                                }
                            } else if (mGroupUser.getPromoterLevel() == 3) {
                                if (mGroupUser.getPromoterId2() == uId) {// 直接上级
                                    continue;
                                }
                                long promoterId3 = Long.parseLong(String.valueOf(map.get("promoterId3")));
                                if (mGroupUser.getUserId() == promoterId3) {// 下级成员
                                    continue;
                                }
                            } else if (mGroupUser.getPromoterLevel() == 4) {
                                if (mGroupUser.getPromoterId3() == uId) {// 直接上级
                                    continue;
                                }
                                long promoterId4 = Long.parseLong(String.valueOf(map.get("promoterId4")));
                                if (mGroupUser.getUserId() == promoterId4) {// 下级成员
                                    continue;
                                }
                            }
                            map.put("hideId", 1);
                        }
                    } else if (GroupConstants.isMember(mGroupUser.getUserRole())) {
                        // 普通成员：能看到直接上级id，群主管理员id；
                        for (HashMap<String, Object> map : list) {
                            Long uId = Long.parseLong(String.valueOf(map.get("userId")));
                            if (uId.equals(mGroupUser.getUserId())) {// 自己
                                continue;
                            }
                            int ur = Integer.parseInt(String.valueOf(map.get("userRole")));
                            if (GroupConstants.isMasterOrAdmin(ur)) {// 群主、管理员
                                continue;
                            }
                            if (mGroupUser.getPromoterLevel() == 0) {

                            } else if (mGroupUser.getPromoterLevel() == 1) {
                                String ug = String.valueOf(map.get("userGroup"));
                                if (mGroupUser.getUserGroup().equals(ug) && GroupConstants.isTeamLeader(ur)) { // 直接上级
                                    continue;
                                }
                            } else if (mGroupUser.getPromoterLevel() == 2 && mGroupUser.getPromoterId1() == uId) {// 直接上级
                                continue;
                            } else if (mGroupUser.getPromoterLevel() == 3 && mGroupUser.getPromoterId2() == uId) {// 直接上级
                                continue;
                            } else if (mGroupUser.getPromoterLevel() == 4 && mGroupUser.getPromoterId3() == uId) {// 直接上级
                                continue;
                            } else if (mGroupUser.getPromoterLevel() == 5 && mGroupUser.getPromoterId4() == uId) {// 直接上级
                                continue;
                            }
                            map.put("hideId", 1);
                        }
                    }
                }
                if (list == null || list.size() == 0) {
                    json.put("list", Collections.emptyList());
                } else {
//                    List<Map<String, Object>> list1 = new ArrayList<>(list.size());
//
//                    for (GroupUser obj : list) {
//                        JSONObject json0 = (JSONObject) JSONObject.toJSON(obj);
//                        Map<String, Object> map = userDao.loadUserBase(obj.getUserId().toString());
//                        if (map != null) {
//                            json0.putAll(map);
//                        }
//                        list1.add(json0);
//                    }
                    json.put("list", list);
                }

                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            }else if("1".equals(credit)){
                //信用管理界面玩家列表
                if (pageSize > 1000) {
                    pageSize = 1000;
                }
                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                }
                long userId = NumberUtils.toLong(params.get("userId"), 0);
                if (!checkSessCode(userId, params.get("sessCode"))) {
                    return;
                }

                GroupUser groupUser = this.groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null) {
                    OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                    return;
                }

                if (groupUser.getUserRole().intValue() > 1) {
                    keyWord = userId + "";
                }

                String creditOrder = params.get("creditOrder");
                if (StringUtils.isBlank(creditOrder) || ("asc".equalsIgnoreCase(creditOrder) && "desc".equalsIgnoreCase(creditOrder))) {
                    creditOrder = "";
                }

                List<HashMap<String, Object>> list = groupDao.loadGroupUserMsgsCredit(groupId, pageNo, pageSize, keyWord, creditOrder);
                int pages = 0;
                if (list == null || list.size() == 0) {
                    list = Collections.emptyList();
                } else {
                    pages = (int) Math.ceil(groupDao.countGroupUser(groupId, keyWord, null) * 1.0 / pageSize);
                }

                JSONObject json = new JSONObject();
                json.put("pageNo", pageNo);
                json.put("pageSize", pageSize);
                json.put("pages", pages);
                json.put("list", list);
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            }else{
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取玩家基础信息
     */
    public void loadUserBase() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String userIds = params.get("userIds");
            if (StringUtils.isBlank(userIds)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                String[] ids = userIds.split(",");
                List<Map<String, Object>> list = new ArrayList<>();
                for (String id : ids) {
                    if (StringUtils.isNotBlank(id)) {
                        Map<String, Object> map = userDao.loadUserBase(id);
                        if (map != null)
                            list.add(map);
                    }
                }
                OutputUtil.output(0, list, getRequest(), getResponse(), false);
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取玩家图像
     */
    public void loadUserImage() {
        loadUserBase();
    }

    /**
     * 获取牌局配置
     */
    public void loadTableConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int subId = NumberUtils.toInt(params.get("subId"), 0);
            if (groupId <= 0 || subId < 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                List<GroupTableConfig> list = subId == 0 ? groupDao.loadGroupTableConfig(groupId, 0) : groupDao.loadGroupTableConfig(subId, groupId);
                if (list == null) {
                    list = Collections.emptyList();
                }

                if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("groupTableConfigWithStatistics"))) {

                    Map<Long, Map<String,Integer>> map = new HashMap<Long, Map<String,Integer>>();
                    if (list.size() > 0) {
                        for (GroupTableConfig config : list) {
                            map.put(config.getKeyId(), new HashMap<String,Integer>());
                        }
                        List<GroupTable> tableList = groupDao.loadGroupTablesByConfigIds(groupId, StringUtils.join(map.keySet(), ","));
                        if (tableList != null) {
                            for (GroupTable table : tableList)  {
                                Map<String,Integer> info = map.get(table.getConfigId());
                                if (table.getCurrentState().equals("1")) {
                                    int count = info.containsKey("playingCount") ? info.get("playingCount") + 1 : 1;
                                    info.put("playingCount", count);
                                }
                                else {
                                    int count = info.containsKey("notStartCount") ? info.get("notStartCount") + 1 : 1;
                                    info.put("notStartCount", count);
                                }
                            }
                        }
                    }

                    JSONArray array = new JSONArray();
                    for (GroupTableConfig config : list) {
                        JSONObject obj = (JSONObject) JSONObject.toJSON(config);
                        Map<String,Integer> info = map.get(config.getKeyId());
                        if (info != null) {
                            obj.put("statistics", JSONObject.toJSON(info));
                        }
                        array.add(obj);
                    }
                    OutputUtil.output(0, array, getRequest(), getResponse(), false);
                }
                else {
                    OutputUtil.output(0, list, getRequest(), getResponse(), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 创建牌局配置
     */
    public void createTableConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            //玩法、付费方式、局数、人数
            int gameType = NumberUtils.toInt(params.get("gameType"), -1);
            int payType = NumberUtils.toInt(params.get("payType"), -1);
            int gameCount = NumberUtils.toInt(params.get("gameCount"), -1);
            int playerCount = NumberUtils.toInt(params.get("playerCount"), -1);

            String modeMsg = params.get("modeMsg");//牌局详细参数
            String tableName = params.get("tableName");
            String descMsg = params.get("descMsg");
            String tableMode = params.get("tableMode");
            String creditMsg = params.get("creditMsg");
            String goldMsg = params.get("goldMsg");
            if(StringUtils.isBlank(creditMsg)){
                creditMsg = "";
            }
            if(StringUtils.isBlank(goldMsg)){
                goldMsg = "";
            }
            int tableOrder = NumberUtils.toInt(params.get("tableOrder"), 1);

            long userId = NumberUtils.toLong(params.get("userId"), -1);

            int subId = NumberUtils.toInt(params.get("subId"), -1);

            if (tableOrder <= 0 || tableOrder >= 1000000) {
                OutputUtil.output(1, "牌局序号错误", getRequest(), getResponse(), false);
                return;
            } else if (gameType <= -1 || payType <= -1 || gameCount <= 0 || playerCount <= 0 || userId <= 0 || subId < 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (StringUtils.isBlank(modeMsg)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);

            GroupInfo group = groupDao.loadGroupInfo(groupId,0);

            if (groupUser == null || group == null) {
                OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            }
            if (group.getIsCredit() != GroupInfo.isCredit_credit) {
                creditMsg = "";
            } else if (group.getIsCredit() != GroupInfo.isCredit_gold) {
                goldMsg = "";
            }


            if (groupUser.getUserRole().intValue() > 1) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }

            synchronized (GroupAction.class) {
                int maxNum = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "maxGroupConfigNum", 0);
                if (maxNum > 0) {
                    List<GroupTableConfig> configList = groupDao.loadGroupTableConfig(groupUser.getGroupId(), 0);
                    if (configList != null && configList.size() >= maxNum) {
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "玩法数量已经达到上限，无法创建"), getRequest(), getResponse(), null, false);
                        return;
                    }
                }

                GroupTableConfig groupTableConfig = subId == 0 ? groupDao.loadGroupTableConfig(groupUser.getGroupId(), 0, modeMsg) : groupDao.loadGroupTableConfig(subId, groupUser.getGroupId(), "");
                if (groupTableConfig != null) {


                    HashMap<String, Object> map = new HashMap<>();
                    if (StringUtils.isNotBlank(descMsg)) {
                        map.put("descMsg", descMsg);
                    }
                    if (StringUtils.isNotBlank(tableMode)) {
                        map.put("tableMode", tableMode);
                    }
                    map.put("creditMsg",creditMsg);
                    map.put("modeMsg", modeMsg);
                    map.put("tableName", tableName);
                    map.put("tableOrder", tableOrder);
                    map.put("gameType", gameType);
                    map.put("gameCount", gameCount);
                    map.put("payType", payType);
                    map.put("playerCount", playerCount);
                    map.put("configState", "1");
                    map.put("createdTime", CommonUtil.dateTimeToString());
                    map.put("keyId",groupTableConfig.getKeyId().toString());
                    groupDao.updateGroupTableConfigByKeyId(map);
                    if("1".equals(groupTableConfig.getConfigState()) && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa"))){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "该玩法已存在"), getRequest(), getResponse(), null, false);
                        return;
                    }else{
                        OutputUtil.output(0, "SUCCESS", getRequest(), getResponse(), false);
                    }

                } else {
                    groupTableConfig = new GroupTableConfig();
                    groupTableConfig.setCreatedTime(new Date());
                    groupTableConfig.setDescMsg(descMsg);

                    if (subId == 0) {
                        groupTableConfig.setGroupId(groupUser.getGroupId().longValue());
                        groupTableConfig.setParentGroup(Long.valueOf(0));
                    } else {
                        groupTableConfig.setGroupId(Long.valueOf(subId));
                        groupTableConfig.setParentGroup(groupUser.getGroupId().longValue());
                    }

                    groupTableConfig.setModeMsg(modeMsg);
                    groupTableConfig.setPlayCount(0L);
                    groupTableConfig.setTableMode(tableMode);
                    groupTableConfig.setTableName(tableName);
                    groupTableConfig.setTableOrder(tableOrder);
                    groupTableConfig.setGameType(gameType);
                    groupTableConfig.setGameCount(gameCount);
                    groupTableConfig.setPayType(payType);
                    groupTableConfig.setPlayerCount(playerCount);
                    groupTableConfig.setConfigState("1");
                    groupTableConfig.setCreditMsg(creditMsg);
                    groupTableConfig.setGoldMsg(goldMsg);

                    long configId = groupDao.createGroupTableConfig(groupTableConfig);
                    if(configId<=0){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                        return;
                    }

                    OutputUtil.output(0, "SUCCESS", getRequest(), getResponse(), false);
                    LOGGER.info("create groupTableConfig:userId={},config={}", userId, JSON.toJSONString(groupTableConfig));
                }
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 删除牌局配置
     */
    public void deleteTableConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (userId <= 0 || keyId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupTableConfig groupTableConfig0 = groupDao.loadGroupTableConfig(keyId);

            if (groupTableConfig0 == null) {
                OutputUtil.output(6, "该牌局配置不存在", getRequest(), getResponse(), false);
                return;
            }
            if (groupId == -1) {
                groupId = groupTableConfig0.getParentGroup() > 0 ? groupTableConfig0.getParentGroup() : groupTableConfig0.getGroupId();
            }
            GroupInfo groupInfo = null;
            if(groupTableConfig0.getParentGroup() > 0) {
                //包间模式删除包间
                groupInfo = groupDao.loadGroupInfoAll(groupTableConfig0.getGroupId(), groupTableConfig0.getParentGroup());
                if (groupInfo == null || "-1".equals(groupInfo.getGroupState())) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);

            if (groupUser == null) {
                OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            }

            if (!GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }

            if (groupTableConfig0.getGroupId().intValue() != groupUser.getGroupId().intValue() && groupTableConfig0.getParentGroup().intValue() != groupUser.getGroupId().intValue()) {
                OutputUtil.output(7, "无权操作", getRequest(), getResponse(), false);
                return;
            }

            Integer count = groupDao.countSubGroup(String.valueOf(groupId));
            if (count == 1) {
                OutputUtil.output(8, "当前只有一个包厢，无法删除", getRequest(), getResponse(), false);
                return;
            }
            //逻辑删除
            HashMap<String, Object> map = new HashMap<>();
            map.put("configState", "0");
            map.put("keyId", String.valueOf(keyId));
            int delResult = groupDao.updateGroupTableConfigByKeyId(map);

            if(groupTableConfig0.getParentGroup() > 0){
                //包间模式删除包间
                map.clear();
                map.put("groupState", "-1");
                map.put("keyId", groupInfo.getKeyId());
                groupDao.updateGroupInfoByKeyId(map);
                if(delResult == 1){
                    dissGroupTable(String.valueOf(groupId),null,String.valueOf(groupTableConfig0.getGroupId()),0);
                }
            }
            if(delResult == 1) {
                //删除绑定的假桌子 回收头像
                List<GroupFakeTable> fakeTableList = GroupDaoManager.getInstance().loadGroupFakeTable(groupId, null, keyId);
                if (fakeTableList != null && fakeTableList.size() > 0) {
                    GroupDaoManager.getInstance().delFakeTableByConfigId(keyId);
                    for (GroupFakeTable fakeTable : fakeTableList) {
                        FakeTableHepler.setFreeFakeTableHeadImg(fakeTable.getKeyId());
                    }
                }
            }


            OutputUtil.output(0, "SUCCESS", getRequest(), getResponse(), false);

            LOGGER.info("delete groupTableConfig:userId={},config={}", userId, JSON.toJSONString(groupTableConfig0));
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 修改牌局配置
     */
    public void updateTableConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateTableConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            //玩法、付费方式、局数、人数
            int gameType = NumberUtils.toInt(params.get("gameType"), -1);
            int payType = NumberUtils.toInt(params.get("payType"), -1);
            int gameCount = NumberUtils.toInt(params.get("gameCount"), -1);
            int playerCount = NumberUtils.toInt(params.get("playerCount"), -1);
            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            int fakeCount = NumberUtils.toInt(params.get("fakeCount"), -1);//创建假桌子的数量

            String modeMsg = params.get("modeMsg");//牌局详细参数
            String tableName = params.get("tableName");
            String descMsg = params.get("descMsg");
            String tableMode = params.get("tableMode");
            String creditMsg = params.get("creditMsg");
            String goldMsg = params.get("goldMsg");
            int tableOrder = NumberUtils.toInt(params.get("tableOrder"), -1);

            long userId = NumberUtils.toLong(params.get("userId"), -1);

            if (tableOrder <= 0 || tableOrder >= 1000000) {
                OutputUtil.output(1, "牌局序号错误", getRequest(), getResponse(), false);
                return;
            } else if (gameType <= -1 || payType <= -1 || gameCount <= 0 || playerCount <= 0 || userId <= 0 || keyId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (StringUtils.isBlank(tableName) || StringUtils.isBlank(modeMsg)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupTableConfig config  = groupDao.loadGroupTableConfig(keyId);
            if (config  == null) {
                OutputUtil.output(6, "该牌局配置不存在", getRequest(), getResponse(), false);
                return;
            }

            String config0 = PropertiesCacheUtil.getValue("table_config_type" + config .getGameType(),Constants.GAME_FILE);
            String config1 = PropertiesCacheUtil.getValue("table_config_type" + gameType,Constants.GAME_FILE);
            if ((config0 == null && config1 == null) || (config0 != null && config1 != null && config0.equals(config1))) {

            } else {
                OutputUtil.output(6, "请创建新俱乐部保存新玩法设置", getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, config .getParentGroup() > 0 ? config .getParentGroup() : config .getGroupId());
            if (groupUser == null) {
                OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo group = groupDaoNew.loadGroupForceMaster(groupUser.getGroupId());
            if (group == null) {
                OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (group.getIsCredit() != GroupInfo.isCredit_credit) {
                creditMsg = "";
            } else if (group.getIsCredit() != GroupInfo.isCredit_gold) {
                goldMsg = "";
            }

            if (!GroupConstants.canCreatePlayType(group, gameType) && config.getGameType() != gameType) {
                OutputUtil.output(6, "您无法添加该玩法", getRequest(), getResponse(), false);
                return;
            }

            synchronized (GroupAction.class) {

                if (config .getGroupId().intValue() != groupUser.getGroupId().intValue() && config .getParentGroup().intValue() != groupUser.getGroupId().intValue()) {
                    OutputUtil.output(7, "无权操作", getRequest(), getResponse(), false);
                    return;
                }

                HashMap<String, Object> map = new HashMap<>();
                if (StringUtils.isNotBlank(descMsg)) {
                    map.put("descMsg", descMsg);
                }
                if (StringUtils.isNotBlank(tableMode)) {
                    map.put("tableMode", tableMode);
                }
                map.put("modeMsg", modeMsg);
//                if(tableOrder > 0){
//                    map.put("tableOrder", tableOrder);
//                }
                map.put("gameType", gameType);
                map.put("gameCount", gameCount);
                map.put("payType", payType);
                map.put("playerCount", playerCount);
                map.put("configState", "1");
                map.put("createdTime", CommonUtil.dateTimeToString());
                if(StringUtils.isBlank(creditMsg)){
                    creditMsg="";
                }
                map.put("creditMsg", creditMsg);
                map.put("goldMsg", goldMsg);
                map.put("keyId", String.valueOf(keyId));
                groupDao.updateGroupTableConfigByKeyId(map);

                //返回要删除的空桌子数量
                String gRoom = params.get("room");
                Integer count = groupDao.getGroupTablesCount(config.getParentGroup().toString(), null,gRoom,1);

                OutputUtil.output(0, count.toString(), getRequest(), getResponse(), false);
                LOGGER.info("updateTableConfig|userId={}|config={}|update={}", userId, JSON.toJSONString(config ), JSON.toJSONString(map));

                if(GroupConstants.isFuHuiZhang(groupUser.getUserRole())){
                    GroupUser master = groupDaoNew.loadGroupMaster(groupUser.getGroupId());
                    if(master != null){
                        userId = master.getUserId();
                    }
                }
                //先解散未开局的
                dissGroupTable(groupUser.getGroupId().toString(), String.valueOf(userId), config.getGroupId().toString(), 0);

                // 再补房
                autoCreateGroupTableNew(userId, groupUser.getGroupId(), keyId);

                //检查是否要修改人气卓
                if(fakeCount >= 0) {
                    //修改现有的人气桌
                    int overCount = gameCount >= 100 ? MathUtil.mt_rand(5, 10) :gameCount;
                    map.clear();
                    map.put("configId", keyId);
                    map.put("tableName", tableName);
                    map.put("gameCount", gameCount);
                    map.put("overCount", overCount);
                    map.put("playerCount",playerCount);
                    GroupDaoManager.getInstance().updateFakeTableByConfigId(map);
                    LOGGER.info("updateTableConfig|updateFakeTableByConfigId|userId={}|config={}|update={}", userId, JSON.toJSONString(config ), JSON.toJSONString(map));


                    //检查是否要增减人气桌子
                    List<Long> keyIdList = groupDaoNew.loadFakeTableKeyIds(keyId);
                    int curFakeCount = 0;
                    if(keyIdList != null && keyIdList.size() > 0){
                        curFakeCount = keyIdList.size();
                    }
                    if (curFakeCount < fakeCount) { //加人气卓
                        int addCount = fakeCount - curFakeCount;
                        for(int i = 1; i <= addCount; i++) {
                            GroupFakeTable fakeTable = new GroupFakeTable();
                            if (StringUtils.isNotBlank(descMsg)) {
                                fakeTable.setDescMsg(descMsg);
                            }
                            fakeTable.setConfigId(keyId);
                            fakeTable.setCreatedTime(new Date());
                            fakeTable.setDescMsg(descMsg);
                            fakeTable.setGroupId(config.getGroupId());
                            fakeTable.setParentGroup(groupUser.getGroupId().longValue());
                            fakeTable.setModeMsg(modeMsg);
                            fakeTable.setPlayCount(0L);
                            fakeTable.setTableMode(tableMode);
                            fakeTable.setTableName(tableName);
                            fakeTable.setTableOrder(tableOrder);
                            fakeTable.setGameType(gameType);
                            fakeTable.setGameCount(gameCount);
                            fakeTable.setPayType(payType);
                            fakeTable.setPlayerCount(playerCount);
                            fakeTable.setConfigState("1");
                            fakeTable.setCreditMsg(creditMsg);
                            fakeTable.setGoldMsg(goldMsg);
                            fakeTable.setOverCount(overCount);
                            fakeTable.setPlayedBureau(0);

                            fakeTable.setRoundRefrshTime(System.currentTimeMillis() / 1000 + MathUtil.mt_rand(GroupConstants.FAKE_TABLE_REFRESH_TIME_MINI, GroupConstants.FAKE_TABLE_REFRESH_TIME_MAX));

                            GroupDaoManager.getInstance().insertGroupFakeTable(fakeTable);
                            //关联假头像
                            FakeTableHepler.setFakeTableHeadImg(fakeTable);
                        }
                    } else if (curFakeCount > fakeCount) {  //删人气卓
                        int deleteCount = curFakeCount - fakeCount;
                        StringBuilder idBuilder = new StringBuilder();
                        for(Long kId : keyIdList){
                            if(deleteCount > 0) {
                                idBuilder.append(kId).append(",");
                            }
                            deleteCount--;
                        }
                        if(idBuilder.length() > 0){
                            idBuilder.deleteCharAt(idBuilder.length() - 1);
                        }
                        GroupDaoManager.getInstance().delFakeTableBatch(idBuilder.toString());
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 查询军团
     */
    public void searchGroupInfo() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupIdStr = params.get("groupId");
            String subIdStr = params.get("subId");
            if (NumberUtils.isDigits(groupIdStr) && NumberUtils.isDigits(subIdStr)) {
                long groupId = Long.parseLong(groupIdStr);
                long subId = Long.parseLong(subIdStr);
                Map<String, Object> map = groupDao.searchGroupInfo(groupId, 0);

                if (map == null) {
                    OutputUtil.output(2, "不存在:" + groupId, getRequest(), getResponse(), false);
                } else {
                    if (subId == 0) {
                        String userIdStr = params.get("userId");
                        if (NumberUtils.isDigits(userIdStr)) {
                            long userId = Long.parseLong(userIdStr);
                            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                            if (groupUser != null) {
                                map.put("groupUser", groupUser);
                            } else {
                                GroupReview groupReview = groupDao.loadGroupReview0(groupId, userId,0);
                                if (groupReview != null) {
                                    map.put("groupReview", groupReview);
                                }
                            }
                        }
                    } else if (subId > 0) {
                        GroupInfo groupInfo = groupDao.loadGroupInfo(subId, groupId);
                        if (groupInfo != null) {
                            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(groupInfo);
                            map.putAll(jsonObject);
                        }
                    }
                    OutputUtil.output(0, map, getRequest(), getResponse(), false);
                }
            } else {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 查询包厢
     */
    public void searchSubGroups() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupIdStr = params.get("groupId");
            if (NumberUtils.isDigits(groupIdStr)) {
                List<GroupInfo> list = groupDao.loadSubGroups(groupIdStr);
                OutputUtil.output(0, list == null ? Collections.emptyList() : list, getRequest(), getResponse(), false);
            } else {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 创建军团(包厢)
     */
    public void createGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupName = params.get("groupName");
            int groupLevel = NumberUtils.toInt(params.get("groupLevel"), 1);
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int subId = NumberUtils.toInt(params.get("subId"), 0);
            int gId = NumberUtils.toInt(params.get("groupId"), 0);
            int createRoom = NumberUtils.toInt(params.get("createRoom"), 0);
            String wanfaIds = params.get("allWanfas");

            if (StringUtils.isBlank(groupName)) {
                OutputUtil.output(1, "请输入俱乐部名字！", getRequest(), getResponse(), false);
                return;
            } else if (userId <= 0 || subId < 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
            if (!groupName.matches(regex)) {
                OutputUtil.output(1, "俱乐部名称仅限字母数字和汉字", getRequest(), getResponse(), false);
                return;
            }
            groupName = groupName.trim();
            String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
            if (!groupName.equals(groupName0)) {
                OutputUtil.output(1, "军团名不能包含敏感字符", getRequest(), getResponse(), false);
                return;
            }

            GroupConfig groupConfig = null;

            if (subId == 0 && createRoom == 0) {
                groupConfig = groupDao.loadGroupConfig(groupLevel);
                if (groupConfig == null) {
                    OutputUtil.output(1, "俱乐部等级配置错误", getRequest(), getResponse(), false);
                    return;
                }
            }

            RegInfo user = userDao.getUser(userId);
            boolean isCreateGroup = (gId == 0); //是否是创建俱乐部
            if (user == null) {
                OutputUtil.output(2, "玩家ID错误", getRequest(), getResponse(), false);
                return;
            } else if (subId == 0 && createRoom == 0 && user.getCards() + user.getFreeCards() < groupConfig.getGroupCoin().longValue()) {
                OutputUtil.output(3, "钻石不足", getRequest(), getResponse(), false);
                return;
            } else if (isCreateGroup && iSphoneBangding() && StringUtils.isBlank(user.getPhoneNum())) {
                OutputUtil.output(1401, "请先进行手机绑定", getRequest(), getResponse(), false);
                return;
            } else if (isCreateGroup && user.getPayBindId() <= 0) {
                // 白金岛 未绑定邀请码的用户不可以创建俱乐部
                String s = ResourcesConfigsUtil.loadServerPropertyValue("CreateGroupLimit.bindAgency", "0");
                if ("1".equals(s)) {
                    OutputUtil.output(4, "请先绑定邀请码", getRequest(), getResponse(), false);
                    return;
                }
            }

            int groupCount = groupDao.loadGroupCount(userId);
            if (subId == 0 && createRoom == 0 && groupCount >= 20) {
                OutputUtil.output(3, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                return;
            }

            if ((subId == 0 && createRoom == 0)&& groupConfig != null) {
                String createGroupNeedCards = PropertiesCacheUtil.getValue("createGroupNeedCards",Constants.GAME_FILE);
                if (StringUtils.isNotBlank(createGroupNeedCards)) {
                    String[] temps = createGroupNeedCards.split(",");
                    int groupCount0 = groupDao.loadMyGroupCount(userId);
                    if (user.getCards() + user.getFreeCards() < Integer.parseInt(temps.length >= (groupCount0 + 1) ? temps[groupCount0] : temps[temps.length - 1])) {
                        OutputUtil.output(3, "创建失败！(" + (temps.length >= (groupCount0 + 1) ? temps[groupCount0] : temps[temps.length - 1]) + "钻石可免费创建第" + (groupCount0 + 1) + "个俱乐部)", getRequest(), getResponse(), false);
                        return;
                    }
                } else {
                    int groupCoin = groupConfig.getGroupCoin().intValue();
                    if (groupCoin <= 0) {
                        int groupCount0 = groupDao.loadMyGroupCount(userId);
                        if (user.getCards() + user.getFreeCards() < -groupCoin * (groupCount0 + 1)) {
                            OutputUtil.output(3, "创建失败！(" + (-groupCoin * (groupCount0 + 1)) + "钻石可免费创建第" + (groupCount0 + 1) + "个俱乐部)", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                }
            }

            int count = 0;

            GroupUser groupUser;
            GroupInfo groupInfo;
            synchronized (GroupAction.class) {
                groupUser = groupDao.loadGroupUser(userId, gId);

                if (subId == 0 && groupUser != null) {
//                    OutputUtil.output(6, "已加入军团:" + groupUser.getGroupName(), getRequest(), getResponse(), false);
//                    return;
                } else if (subId > 0) {
                    if (groupUser == null) {
                        OutputUtil.output(7, "尚未加入军团", getRequest(), getResponse(), false);
                        return;
                    } else if (groupUser.getUserRole().intValue() > 1) {
                        OutputUtil.output(9, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                        return;
                    }
                }

                if (subId > 0) {
                    count = groupDao.countSubGroup(groupUser.getGroupId().toString());
                    if (count >= 50) {
                        OutputUtil.output(8, "包厢数量超过上限", getRequest(), getResponse(), false);
                        return;
                    }
                }

//                groupInfo = groupDao.loadGroupInfo(groupName, subId == 0 ? "0" : groupUser.getGroupId().toString());
//                if (groupInfo != null) {
//                    OutputUtil.output(4, "军团(或包厢)名已存在", getRequest(), getResponse(), false);
//                    return;
//                }

                if (subId == 0 && createRoom == 0) {
                    SecureRandom random = new SecureRandom();
                    int base = 9000;
                    int groupId = min_group_id + random.nextInt(base);//groupDao.loadMaxGroupId();

                    boolean canCreate = false;
                    int c = 0;
                    while (c < 3) {
                        c++;
                        if (groupDao.existsGroupInfo(groupId, 0)) {
                            groupId = min_group_id * c * 10 + random.nextInt(base * c * 10);
                        } else {
                            canCreate = true;
                            break;
                        }
                    }

                    if (!canCreate) {
                        OutputUtil.output(5, "请稍后再试", getRequest(), getResponse(), false);
                        return;
                    }

//                    if (groupId == null) {
//                        groupId = min_group_id;
//                    } else {
//                        groupId += 1;
//                    }

                    groupInfo = new GroupInfo();
                    groupInfo.setCreatedTime(new Date());
                    groupInfo.setCreatedUser(userId);
                    groupInfo.setCurrentCount(1);
                    groupInfo.setDescMsg("");
                    groupInfo.setGroupId(groupId);
                    JSONObject jsonObject = new JSONObject();
                    String defaultStr = PropertiesCacheUtil.getValueOrDefault("group_kf_default", "",Constants.GAME_FILE);
                    if (!StringUtils.isBlank(defaultStr)) {
                        String[] strs = defaultStr.split(",");
                        for (String value : strs) {
                            if ((value.startsWith("+q") || value.startsWith("-q"))) {
                                jsonObject.put("oq", value);
                            } else if ((value.startsWith("+p3") || value.startsWith("-p3"))) {
                                jsonObject.put("pc", value);
                            } else if ((value.startsWith("+r") || value.startsWith("-r"))) {
                                jsonObject.put("cr", value);
                            }
                        }
                    }
                    if (!StringUtils.isBlank(wanfaIds)) {
                        jsonObject.put("wanfaIds", wanfaIds);
                    }
                    groupInfo.setExtMsg(jsonObject.toString());
                    groupInfo.setGroupLevel(groupConfig.getGroupLevel());
                    groupInfo.setGroupMode(0);
                    groupInfo.setGroupName(groupName);
                    groupInfo.setMaxCount(groupConfig.getMaxCount());
                    groupInfo.setParentGroup(0);
                    groupInfo.setGroupState("1");
                    groupInfo.setModifiedTime(groupInfo.getCreatedTime());
                    if(groupDao.createGroup(groupInfo)<=0){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                        return;
                    }
                    if (subId == 0) {// 初始化俱乐部成员数据
                        groupUser = new GroupUser();
                        groupUser.setCreatedTime(new Date());
                        groupUser.setGroupId(groupInfo.getGroupId());
                        groupUser.setGroupName(groupInfo.getGroupName());
                        groupUser.setInviterId(userId);
                        groupUser.setPlayCount1(0);
                        groupUser.setPlayCount2(0);
                        groupUser.setUserId(userId);
                        groupUser.setUserLevel(1);
                        groupUser.setUserRole(0);
                        groupUser.setUserName(user.getName());
                        groupUser.setUserNickname(user.getName());
                        groupUser.setUserGroup("0");
                        groupUser.setCredit(0l);
                        groupUser.setRefuseInvite(1);
                        if(groupDao.createGroupUser(groupUser)<=0){
                            groupDao.deleteGroupInfoByGroupId(groupInfo.getGroupId(),0);
                            return;
                        }

                        LOGGER.info("create groupUser success:{}", JSON.toJSONString(groupUser));

                        if (groupConfig.getGroupCoin().intValue() > 0) {
                            int ret = userDao.consumeUserCards(user, groupConfig.getGroupCoin().intValue());

                            LOGGER.info("create group consumeUserCards ret:{},userId:{}", ret, user.getUserId());
                        }
                    }

                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(0, "创建成功").builder("groupId", groupInfo.getGroupId()), getRequest(), getResponse(), null, false);

                    LOGGER.info("create groupInfo success:{}", JSON.toJSONString(groupInfo));
                } else {
                    if (createRoom == 0 && (subId <= 0 || subId >= 1000000)) {
                        OutputUtil.output(4, "大侠，请换个包厢序号吧", getRequest(), getResponse(), false);
                        return;
                    }
                    if(createRoom == 1){
                        //取个未使用包间号
                        List<Integer> roomIds = groupDao.loadGroupRoomId(groupUser.getGroupId().toString());
                        for(int i = 1 ; i <= 50 ; i++){
                            if(!roomIds.contains(i)){
                                subId = i;
                                break;
                            }
                        }
                    }
                    GroupInfo exitGroupInfo = groupDao.loadGroupInfoAll(subId,groupUser.getGroupId());
                    if(exitGroupInfo == null) {
                        groupInfo = new GroupInfo();
                        groupInfo.setCreatedTime(new Date());
                        groupInfo.setCreatedUser(userId);
                        groupInfo.setCurrentCount(1);
                        groupInfo.setDescMsg("");
                        groupInfo.setGroupId(subId);
                        groupInfo.setParentGroup(groupUser.getGroupId());
                        groupInfo.setExtMsg("");
                        groupInfo.setGroupLevel(0);
                        groupInfo.setGroupMode(0);
                        groupInfo.setGroupName(groupName0);
                        groupInfo.setMaxCount(0);
                        groupInfo.setGroupState("1");
                        groupInfo.setModifiedTime(groupInfo.getCreatedTime());
                        if (groupDao.createGroup(groupInfo) <= 0) {
                            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                            return;
                        }
                    }else{
                        groupInfo = exitGroupInfo;
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("keyId",groupInfo.getKeyId());
                        map.put("groupName",groupName);
                        map.put("createdUser",userId);
                        map.put("createdTime",new Date());
                        map.put("maxCount",0);
                        map.put("groupState","1");
                        map.put("ModifiedTime",groupInfo.getCreatedTime());
                        groupDao.updateGroupInfoByKeyId(map);
                    }
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(0, "包厢创建成功").builder("groupId", groupInfo.getParentGroup()).builder("subId",subId), getRequest(), getResponse(), null, false);
                    LOGGER.info("create groupSubInfo success:{},total={}", JSON.toJSONString(groupInfo), count + 1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }

    }

    /**
     * 牌局统计
     */
    public void statisticsMilitaryExploits() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (StringUtils.isAnyBlank(groupId, startDate, endDate)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            if (!startDate.contains(" ")) {
                startDate = startDate + " 00:00:00";
            }
            if (!endDate.contains(" ")) {
                endDate = endDate + " 23:59:59";
            }

            if (pageSize > 30) {
                pageSize = 30;
            }

            List<HashMap<String, Object>> list = groupDao.statisticsMilitaryExploits(groupId, startDate, endDate, pageNo, pageSize);
            if (list != null && list.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("userId")).append("\"");
                }
                strBuilder.deleteCharAt(0);
                List<HashMap<String, Object>> list1 = userDao.loadUsersByUserId(strBuilder.toString());
                for (HashMap<String, Object> map : list) {
                    String userId = String.valueOf(map.get("userId"));
                    for (HashMap<String, Object> map1 : list1) {
                        if (userId.equals(String.valueOf(map1.get("userId")))) {
                            map.put("userName", map1.get("userName"));
                            break;
                        }
                    }
                }
            } else {
                if (pageNo > 1) {
                    OutputUtil.output(2, "当前是最后一页", getRequest(), getResponse(), false);
                    return;
                }
                list = Collections.emptyList();
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("list", list);

            OutputUtil.output(0, json, getRequest(), getResponse(), false);

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 组员战绩
     */
    public void loadGroupUserRecords() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String userId = params.get("oUserId");
            String groupId = params.get("groupId");
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (StringUtils.isAnyBlank(userId, groupId, startDate, endDate)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            if (!startDate.contains(" ")) {
                startDate = startDate + " 00:00:00";
            }
            if (!endDate.contains(" ")) {
                endDate = endDate + " 23:59:59";
            }

            if (pageSize > 30) {
                pageSize = 30;
            }

            List<HashMap<String, Object>> list = groupDao.loadGroupUserRecords(groupId, userId, startDate, endDate, pageNo, pageSize);
            if (list != null && list.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("tableNo")).append("\"");
                }
                strBuilder.deleteCharAt(0);
                String tableNos = strBuilder.toString();
                List<HashMap<String, Object>> list1 = groupDao.loadTableUserInfo(tableNos,Long.valueOf(groupId));
//                List<HashMap<String, Object>> list2 = groupDao.loadGroupTableConfigNum(tableNos);
                List<HashMap<String, Object>> list3 = groupDao.loadGroupTables(tableNos,groupId);
                List<HashMap<String, Object>> list4 = null;
                if (list3 != null && list3.size() > 0) {
                    Set<Object> set = new HashSet<>();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (HashMap<String, Object> map : list3) {
                        Object configId = map.get("configId");
                        if (configId != null && set.add(configId)) {
                            stringBuilder.append(",\"").append(configId).append("\"");
                        }
                    }

                    if (stringBuilder.length() > 0) {
                        list4 = groupDao.loadGroupTableConfigs(stringBuilder.substring(1));
                    }
                }

                for (HashMap<String, Object> map : list) {
                    String tableNo = String.valueOf(map.get("tableNo"));
                    if (list1 != null) {
                        for (HashMap<String, Object> map1 : list1) {
                            if (tableNo.equals(String.valueOf(map1.get("tableNo")))) {
                                List<HashMap<String, Object>> tempList = (List<HashMap<String, Object>>) map.get("users");
                                if (tempList == null) {
                                    tempList = new ArrayList<>();
                                    tempList.add(map1);
                                    map.put("users", tempList);
                                } else {
                                    tempList.add(map1);
                                }
                            }
                        }
                    }
                    if (list3 != null && list4 != null) {
                        for (HashMap<String, Object> map1 : list3) {
                            String id1 = String.valueOf(map1.get("keyId"));
                            if (tableNo.equals(id1)) {
                                String id2 = String.valueOf(map1.get("configId"));
                                for (HashMap<String, Object> map2 : list4) {
                                    if (id2.equals(String.valueOf(map2.get("keyId")))) {
                                        map.put("modeName", map2.get("tableMode"));
                                        map.put("gameCount", map2.get("gameCount"));
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            } else {
                list = Collections.emptyList();
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("list", list);

            OutputUtil.output(0, json, getRequest(), getResponse(), false);

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 战绩详情
     */
    public void loadTableRecord() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadTableRecord|params|{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String tableNo = params.get("tableNo");
            long userId = NumberUtils.toLong(params.get("oUserId"), 0);
            String isClub = params.get("isClub");

            if (StringUtils.isBlank(tableNo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            List<HashMap<String, Object>> list = groupDao.loadTableRecordByTableNo(tableNo);
            if (list != null && list.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                String modeMsg = "",resultMsg = "";
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("logId")).append("\"");
                    if(map.containsKey("recordType") && "1".equals(String.valueOf(map.get("recordType")))){
                        modeMsg = map.containsKey("modeMsg") ? String.valueOf(map.get("modeMsg")) : "";
                        resultMsg = map.containsKey("resultMsg") ? String.valueOf(map.get("resultMsg")) : "";
                    }
                }
                strBuilder.deleteCharAt(0);
                List list1 = TableLogDao.getInstance().selectUserLogs(strBuilder.toString());

                if (list1 != null && list1.size() > 0) {
                    list1 = Manager.getInstance().buildUserPlayTbaleMsg(1, list1, userId);
                }

                if (!StringUtils.isBlank(isClub) && "1".equals(isClub)) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("playLog", list1 == null ? "" : list1);
                    jsonObject.put("modeMsg", modeMsg);
                    jsonObject.put("resultMsg", resultMsg);
                    OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                } else {
                    OutputUtil.output(0, list1 == null ? Collections.emptyList() : list1, getRequest(), getResponse(), false);
                }
            } else {
                OutputUtil.output(0, Collections.emptyList(), getRequest(), getResponse(), false);
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }
    
    
    /**
     * 通过tableId查俱乐部详情
     */
    public void loadTableRecordTableId() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadTableRecordTableId|params|{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String tableNo = params.get("tableNo");
            long userId = NumberUtils.toLong(params.get("oUserId"), 0);
            String isClub = params.get("isClub");

            if (StringUtils.isBlank(tableNo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            List<HashMap<String, Object>> list = groupDao.loadTableRecordByTableId(tableNo);
            if (list != null && list.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                String modeMsg = "",resultMsg = "";
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("logId")).append("\"");
                    if(map.containsKey("recordType") && "1".equals(String.valueOf(map.get("recordType")))){
                        modeMsg = map.containsKey("modeMsg") ? String.valueOf(map.get("modeMsg")) : "";
                        resultMsg = map.containsKey("resultMsg") ? String.valueOf(map.get("resultMsg")) : "";
                    }
                }
                strBuilder.deleteCharAt(0);
                List list1 = TableLogDao.getInstance().selectUserLogs(strBuilder.toString());

                if (list1 != null && list1.size() > 0) {
                    list1 = Manager.getInstance().buildUserPlayTbaleMsg(1, list1, userId);
                }

                if (!StringUtils.isBlank(isClub) && "1".equals(isClub)) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("playLog", list1 == null ? "" : list1);
                    jsonObject.put("modeMsg", modeMsg);
                    jsonObject.put("resultMsg", resultMsg);
                    OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                } else {
                    OutputUtil.output(0, list1 == null ? Collections.emptyList() : list1, getRequest(), getResponse(), false);
                }
            } else {
                OutputUtil.output(0, Collections.emptyList(), getRequest(), getResponse(), false);
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取练习场配置信息
     */
    public void loadDrivingRangeConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String gameCode = params.get("gameCode");

            if (StringUtils.isBlank(gameCode)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            String result = PropertiesCacheUtil.getValue("driving_range_" + gameCode,Constants.GAME_FILE);
            JSONArray jsonArray;
            if (StringUtils.isBlank(result)) {
                jsonArray = new JSONArray();
            } else {
                jsonArray = JSONArray.parseArray(result);
            }

            OutputUtil.output(0, jsonArray, getRequest(), getResponse(), false);

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }
    /**
     * 创建桌
     */
    public void createTable(){
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String userId = params.get("oUserId");
            String groupId = params.get("groupId");
            String tableCount = params.get("tableCount");
            String modeId = params.get("modeId");
            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId) || !NumberUtils.isDigits(tableCount) || !NumberUtils.isDigits(modeId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
            if(groupUser == null || groupUser.getUserRole().intValue() > 1){
                OutputUtil.output(2, "您不是群主(管理员)，无法操作！", getRequest(), getResponse(), false);
                return;
            }
            GroupTableConfig groupTableConfig0 = groupDao.loadGroupTableConfig(Long.parseLong(modeId));
            if (groupTableConfig0 == null) {
                OutputUtil.output(3, "该牌局配置不存在", getRequest(), getResponse(), false);
                return;
            }
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put("oUserId", userId);
            infoMap.put("groupId", groupId);
            infoMap.put("tableCount", tableCount);
            infoMap.put("tableVisible", "1");
            infoMap.put("modeId", modeId);
            String res = "";
            RegInfo user = userDao.getUser(Long.parseLong(userId));
            int serverId = user.getEnterServer();
            if(serverId<=0){
                Server server = SysInfManager.loadServer(groupTableConfig0.getGameType(),1);
                if(server != null){
                    serverId = server.getId();
                }
            }
            if(serverId > 0){
                res = GameUtil.sendCreateTable(serverId, infoMap);
                GameBackLogger.SYS_LOG.info("sendCreateTable-->serverId:" + serverId + ",infoMap:" + infoMap + ",res:" + res);
                JSONObject jsonObject = new JSONObject();
                if("succeed".equals(res)){
                    jsonObject.put("code", 0);
                    jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
                    OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                    return;
                }else{
                    OutputUtil.output(4, res, getRequest(), getResponse(), false);
                    return;
                }
            }else{
                OutputUtil.output(3, "找不到服务器", getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }
    /**
     * 解散单个房间
     */
    public void dissSingleTable(){
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("oUserId");
            String keyId = params.get("keyId");
            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
            if(groupUser == null || groupUser.getUserRole().intValue() > 1){
                OutputUtil.output(2, "您不是群主(管理员)，无法操作！", getRequest(), getResponse(), false);
                return;
            }
            GroupTable groupTable = groupDao.loadGroupTable(Long.parseLong(keyId),groupId);
            if(groupTable == null || Integer.parseInt(groupTable.getCurrentState()) >= 2 || groupUser.getGroupId().intValue() != Integer.parseInt(groupId)){
                OutputUtil.output(2, "桌子不存在，无法操作！", getRequest(), getResponse(), false);
                return;
            }
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put("tableIds", groupTable.getTableId()+"");
            infoMap.put("keyIds", keyId);
            infoMap.put("specialDiss", "1");
            String res = "";
            int c = 0;
            while (StringUtils.isBlank(res) && c <= 5) {
                c++;
                int serverId = Integer.parseInt(groupTable.getServerId());
                res = GameUtil.sendDissInfo(serverId, infoMap);
                GameBackLogger.SYS_LOG.info("sendDissInfo-->serverId:" + serverId + ",infoMap:" + infoMap + ",res:" + res);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));

            OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 解散房间
     */
    public void dissTable() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("oUserId");
            String all = params.get("all");
            String needAdd = params.get("needAdd");

            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            if ("all".equals(all)) {
                GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
                if (groupUser != null && groupUser.getUserRole().intValue() == 0) {
                    userId = null;
                }else if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("admin_diss_all_table"))){
                    // 管理员解散所有房间
                    if (groupUser.getUserRole().intValue() != 1) {
                        OutputUtil.output(3, "权限不够：仅限(副)群主(组长)", getRequest(), getResponse(), false);
                        return;
                    }
                    userId = null;
                } else {
                    OutputUtil.output(2, "您不是群主，无法操作！", getRequest(), getResponse(), false);
                    return;
                }
            }

//            int count = groupDao.updateGroupTable(groupId,userId);

            String gRoom = params.get("room");
            if (StringUtils.isNotBlank(gRoom)){
                GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
                if(groupUser == null || groupUser.getUserRole().intValue() > 1){
                    OutputUtil.output(2, "您不是群主(管理员)，无法操作！", getRequest(), getResponse(), false);
                    return;
                }
                userId = null;
            }

            int count = dissGroupTable(groupId, userId, gRoom,1);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("count", count);
            jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));

            OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 解散俱乐部房间：默认解散未开局的房间
     * @param groupId
     * @param userId
     * @param gRoom 包间id
     * @param currentCount0 房间内有玩家的不解散
     * @return
     * @throws Exception
     */
    private int dissGroupTable(String groupId,String userId,String gRoom,int currentCount0) throws Exception{
        int count = 0 ;
        List<HashMap<String, Object>> list = groupDao.loadGroupTables(groupId, userId,gRoom,currentCount0);
        Map<Integer, Map<String, String>> serverMap = new HashMap<>();
        if (list != null && list.size() > 0) {
            Map<String, Integer> userMap = new HashMap<>();
            Map<String,ArrayList<String>> tableKeys = new HashMap<>();
            for (HashMap<String, Object> map : list) {
                String key = String.valueOf(map.get("keyId"));
                if (groupDao.updateGroupTableByKeyId(key,currentCount0,groupId) > 0) {
                    count++;
                    int serverId = NumberUtils.toInt(String.valueOf(map.get("serverId")), 0);
                    if (serverId > 0) {
                        Map<String, String> infoMap = serverMap.get(serverId);
                        if (infoMap == null || infoMap.isEmpty()) {
                            infoMap = new HashMap<>();
                            infoMap.put("tableIds", String.valueOf(map.get("tableId")));
                            infoMap.put("keyIds", key);
                            serverMap.put(serverId, infoMap);
                        } else {
                            String tableIds = String.valueOf(infoMap.get("tableIds"));
                            String keyIds = String.valueOf(infoMap.get("keyIds"));
                            infoMap.put("tableIds", tableIds + "," + String.valueOf(map.get("tableId")));
                            infoMap.put("keyIds", keyIds + "," + key);
                            serverMap.put(serverId, infoMap);
                        }
                    }
                    RoomDaoImpl.getInstance().clearRoom(Long.parseLong(String.valueOf(map.get("tableId"))));
                    JSONObject tableJson = JSONObject.parseObject(String.valueOf(map.get("tableMsg")));
                    String groupRoom = tableJson.getString("room");
                    if (groupRoom==null){
                        groupRoom="0";
                    }
                    ArrayList<String> rooms = tableKeys.get(groupRoom);
                    if (rooms==null){
                        rooms=new ArrayList<>();
                        rooms.add(key);
                        tableKeys.put(groupRoom,rooms);
                    }else{
                        rooms.add(key);
                    }

                    String[] tempMsgs = tableJson.getString("strs").split(";")[0].split("_");
                    if (tempMsgs.length >= 4) {
                        String payType = tempMsgs[0];
                        String tempUserId = tempMsgs[2];
                        int tempNum = Integer.parseInt(tempMsgs[3]);
                        if (("2".equals(payType) || "3".equals(payType)) && tempNum > 0) {
                            Integer integer = userMap.get(tempUserId);
                            if (integer == null) {
                                userMap.put(tempUserId, tempNum);
                            } else {
                                userMap.put(tempUserId, tempNum + integer.intValue());
                            }
                        }
                    }
                }
            }

            if (tableKeys.size()>0&&Redis.isConnected()) {
                for (Map.Entry<String, ArrayList<String>> kv : tableKeys.entrySet()) {
                    if ("0".equals(kv.getKey())) {
                        RedisUtil.zrem(CacheUtil.loadStringKey(null, new StringBuilder("group_tables_").append(groupId).toString()), kv.getValue().toArray(new String[0]));
                        RedisUtil.hdel(CacheUtil.loadStringKey(null, new StringBuilder("msg_group_tables_").append(groupId).toString()), kv.getValue().toArray(new String[0]));
                    }else{
                        RedisUtil.zrem(CacheUtil.loadStringKey(null, new StringBuilder("group_tables_").append(groupId).append("_").append(kv.getKey()).toString()), kv.getValue().toArray(new String[0]));
                        RedisUtil.hdel(CacheUtil.loadStringKey(null, new StringBuilder("msg_group_tables_").append(groupId).append("_").append(kv.getKey()).toString()), kv.getValue().toArray(new String[0]));
                    }
                }
            }

            for (Map.Entry<String, Integer> kv : userMap.entrySet()) {
                try {
                    RegInfo user = userDao.getUser(Long.parseLong(kv.getKey()));
                    if (user != null) {
                        UserMessage message = new UserMessage();
                        message.setUserId(user.getUserId());
                        if(InitData.groupToQinYouQuan == 1)
                            message.setContent(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 亲友圈房间未开局被解散，获得钻石x" + kv.getValue());
                        else
                            message.setContent(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 俱乐部房间未开局被解散，获得钻石x" + kv.getValue());
                        message.setTime(new Date());
                        message.setType(0);
                        UserDao.getInstance().addUserCards(user, 0, kv.getValue(), 0, null, message, CardSourceType.groupTable_diss_QZ);
                    }
                } catch (Exception e) {
                    GameBackLogger.SYS_LOG.error("dissTable return card error tableId:" + kv.getKey() + ",count=" + kv.getValue(), e);
                }
            }

            for (Integer serverId : serverMap.keySet()) {
                String res = "";
                int c = 0;
                while (StringUtils.isBlank(res) && c <= 5) {
                    c++;
                    res = GameUtil.sendDissInfo(serverId, serverMap.get(serverId));
                    GameBackLogger.SYS_LOG.info("sendDissInfo-->serverId:" + serverId + ",infoMap:" + serverMap.get(serverId) + ",res:" + res);
                }
            }
        }
        return count;
    }

    /**
     * 俱乐部战绩
     */
    public void loadTableLogs() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("oUserId");
            String all = params.get("all");
            String logType = params.get("logType");

            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 100);
            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 100 : pageSize;

            String wanfas = params.get("wanfas");
            List<Integer> wanfaIds = new ArrayList<>();
            if (StringUtils.isNotBlank(wanfas))// 查固定玩法战绩
                wanfaIds = StringUtil.explodeToIntList(wanfas);

            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            List<HashMap<String, Object>> list;
            int dataCount = 0 ;
            if ("all".equals(all)) {
                GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
                if (groupUser == null || groupUser.getUserRole().intValue() >= 2) {
                    list = groupDao.loadTableRecords(groupId, userId,pageNo,pageSize);
                    dataCount = groupDao.loadTableRecordsCount(groupId, userId);
                } else {
                    list = groupDao.loadTableRecords(groupId, null,pageNo,pageSize);
                    dataCount = groupDao.loadTableRecordsCount(groupId, null);
                }
            } else {
                list = groupDao.loadTableRecords(groupId, userId,pageNo,pageSize);
                dataCount = groupDao.loadTableRecordsCount(groupId, userId);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("pageNo", pageNo);
            jsonObject.put("pageSize", pageSize);
            jsonObject.put("total", dataCount);
            jsonObject.put("pages", (int) Math.ceil(dataCount * 1.0 / pageSize));
            if (list != null && list.size() > 0) {
                jsonObject.put("tableLogs", list);
                StringBuilder strBuilder = new StringBuilder();
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("logId")).append("\"");
                }
                strBuilder.deleteCharAt(0);
                List list1 = TableLogDao.getInstance().selectUserLogs(strBuilder.toString());

                if (list1 != null && list1.size() > 0) {
                    int lt = StringUtils.isBlank(logType) ? 0 : NumberUtils.toInt(logType, 0);
                    if (wanfaIds == null) {
                        wanfaIds = new ArrayList<>();
                    }
                    if (wanfaIds.size() > 0 || lt != 0) {
                        PlayLogTool.screen(list1, lt, wanfaIds);
                    }
                    if (list1.size() > 0) {
                        Collections.sort(list1, new Comparator<Object>() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                UserPlaylog log1 = (UserPlaylog) o1;
                                UserPlaylog log2 = (UserPlaylog) o2;
                                return log1.getTime() != null && log2.getTime() != null ? (log1.getTime().before(log2.getTime()) ? 1 : -1) : (int) (log2.getId() - log1.getId());
                            }
                        });
                        list1 = Manager.getInstance().buildUserPlayTbaleMsg(0, list1, Long.parseLong(userId));

                        jsonObject.put("playLog", list1 == null ? Collections.emptyList() : list1);
                    } else {
                        jsonObject.put("playLog", Collections.emptyList());
                    }
                }
            } else {
                jsonObject.put("playLog", Collections.emptyList());
            }
            OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }
    
    
    
    /**
     * 小甘麻将俱乐部战绩
     */
    public void loadTableXgmjLogs() {
        try {
        	
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("oUserId");
            String all = params.get("all");
            String logType = params.get("logType");
            String wanfas = params.get("wanfas");
            
            int queryDate = NumberUtils.toInt(params.get("queryDate"), 1);//1：当日，2：昨日，3：前日
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);//页码
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);//一页多少条
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), 0);//按玩家id查询
            long queryTableId = NumberUtils.toLong(params.get("queryTableId"), 0);//按房号查询
            
            
            
            
            
            // 2,4表示查询所有 4表示查询中途解散的；
            String condition = params.get("condition");
            if (StringUtils.isBlank(condition)) {
                // 正常解散和中途解散
                condition = "2,4";
            }
            
            
            //编辑所选时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar ca = Calendar.getInstance();
            switch(queryDate){
                case 2:
                    ca.set(Calendar.DATE, ca.get(Calendar.DATE) - 1);
                    break;
                case 3:
                    ca.set(Calendar.DATE, ca.get(Calendar.DATE) - 2);
                    break;
            }
            String strDate = sdf.format(ca.getTime());
            String startDate = "";
            String endDate = "";
            if (!strDate.contains(" ")) {
                startDate = strDate + " 00:00:00";
            }
            if (!strDate.contains(" ")) {
                endDate = strDate + " 23:59:59";
            }
            
            
            
            
            List<Integer> wanfaIds = new ArrayList<>();
            if (StringUtils.isNotBlank(wanfas))// 查固定玩法战绩
                wanfaIds = StringUtil.explodeToIntList(wanfas);

            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if(queryUserId != 0) {
	            GroupUser groupQueryUser = groupDao.loadGroupUser(queryUserId, NumberUtils.toLong(groupId));
	            if (groupQueryUser == null) {
	                OutputUtil.output(3, "所查询的玩家不是该军团成员", getRequest(), getResponse(), false);
	                return;
	            }
            }

            List<HashMap<String, Object>> list = null;
            int count = 0;
            if ("all".equals(all)) {
                GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
                if (groupUser == null || groupUser.getUserRole().intValue() >= 2) {
                	count = groupDao.loadTableRecordsXgmjCount(groupId, userId,condition, queryTableId, startDate, endDate);
                	if(count > 0) {
                		list = groupDao.loadTableRecordsXgmj(groupId, userId,condition, queryTableId, startDate, endDate, pageNo, pageSize);
                	}
                } else {
                	if(queryUserId != 0) {
                		count = groupDao.loadTableRecordsXgmjCount(groupId, ""+queryUserId,condition, queryTableId, startDate, endDate);
                		if(count > 0) {
                			list = groupDao.loadTableRecordsXgmj(groupId, ""+queryUserId,condition, queryTableId, startDate, endDate, pageNo, pageSize);
                		}
                	}else {
                		count = groupDao.loadTableRecordsXgmjCount(groupId, null,condition, queryTableId, startDate, endDate);
                		if(count > 0) {
                			list = groupDao.loadTableRecordsXgmj(groupId, null,condition, queryTableId, startDate, endDate, pageNo, pageSize);
                		}
                	}
                    
                }
            } else {
            	count = groupDao.loadTableRecordsXgmjCount(groupId, userId,condition, queryTableId, startDate, endDate);
        		if(count > 0) {
        			list = groupDao.loadTableRecordsXgmj(groupId, userId,condition, queryTableId, startDate, endDate, pageNo, pageSize);
        		}
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);

            if (list != null && list.size() > 0) {
                jsonObject.put("tableLogs", list);
                StringBuilder strBuilder = new StringBuilder();
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("logId")).append("\"");
                }
                strBuilder.deleteCharAt(0);
                List list1 = TableLogDao.getInstance().selectUserLogs(strBuilder.toString());

                if (list1 != null && list1.size() > 0) {
                    int lt = StringUtils.isBlank(logType) ? 0 : NumberUtils.toInt(logType, 0);
                    if (wanfaIds == null) {
                        wanfaIds = new ArrayList<>();
                    }
                    if (wanfaIds.size() > 0 || lt != 0) {
                        PlayLogTool.screen(list1, lt, wanfaIds);
                    }
                    if (list1.size() > 0) {
                        Collections.sort(list1, new Comparator<Object>() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                UserPlaylog log1 = (UserPlaylog) o1;
                                UserPlaylog log2 = (UserPlaylog) o2;
                                return log1.getTime() != null && log2.getTime() != null ? (log1.getTime().before(log2.getTime()) ? 1 : -1) : (int) (log2.getId() - log1.getId());
                            }
                        });
                        list1 = Manager.getInstance().buildUserPlayTbaleMsg(0, list1, Long.parseLong(userId));

                        jsonObject.put("playLog", list1 == null ? Collections.emptyList() : list1);
                    } else {
                        jsonObject.put("playLog", Collections.emptyList());
                    }
                }
            } else {
                jsonObject.put("playLog", Collections.emptyList());
            }
            jsonObject.put("count", count);
            jsonObject.put("queryDate", queryDate);
            jsonObject.put("pageNo", pageNo);
            jsonObject.put("pageSize", pageSize);
            if(queryUserId != 0) {
            	jsonObject.put("queryUserId", queryUserId);
            }
            if(queryTableId != 0) {
            	jsonObject.put("queryTableId", queryTableId);
            }
            
            OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取军团房间信息
     */
    public void loadGroupTables() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (groupId <= 0 || pageNo < 1 || pageSize < 1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                if (pageSize > 50) {
                    pageSize = 50;
                }
                List<Map<String, Object>> retList;
                List<GroupTable> list = groupDao.loadGroupTables(groupId, pageNo, pageSize);
                MessageBuilder messageBuilder = MessageBuilder.newInstance()
                        .builder("code", 0).builder("groupId", groupId).builder("pageNo", pageNo).builder("pageSize", pageSize);

                if (list != null) {
                    retList = new ArrayList<>(list.size());
                    boolean loadMember = "1".equals(params.get("member"));
                    ArrayList<Map<String, Object>> baseList = new ArrayList<>();
                    Set<String> userSet = new HashSet<>();
                    for (GroupTable gt : list) {
                        Map<String, Object> tableMap = (JSONObject) JSONObject.toJSON(gt);
                        if (loadMember) {
                            List<HashMap<String, Object>> members = gt.getCurrentCount().intValue() <= 0 ? null : groupDao.loadTableUserInfo(gt.getKeyId().toString(),groupId);
                            if (members == null || members.size() == 0) {
                                tableMap.put("members", Collections.emptyList());
                            } else {
                                List<Map<String, Object>> members0 = new ArrayList<>(members.size());
                                for (HashMap<String, Object> map : members) {
                                    Map<String, Object> map0;
                                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("get_user_base_msg_nocache"))){  //取战绩 费缓存
                                        map0 = userDao.loadUserBaseNoCache(String.valueOf(map.get("userId")));
                                    }else{
                                        map0 = userDao.loadUserBase(String.valueOf(map.get("userId")));
                                    }
                                    if (map0 != null) {
                                        members0.add(map0);
                                    }
                                }
                                tableMap.put("members", members0);
                            }
                        }
                        String msg = gt.getTableMsg();
                        if (msg != null && msg.startsWith("{") && msg.endsWith("}")) {
                            JSONObject json = JSONObject.parseObject(msg);
                            String str = json.getString("strs");
                            if (str != null && str.contains(";")) {
                                str = str.substring(0, str.indexOf(";"));
                                if (str.contains("_")) {
                                    str = str.split("_")[1];
                                    if (userSet.add(str)) {
                                        Map<String, Object> map = userDao.loadUserBase(str);
                                        if (map != null) {
                                            baseList.add(loadMember ? new HashMap<>(map) : map);
                                        }
                                    }
                                }
                            }
                        }
                        tableMap.put("progress0",gt.getDealCount());
                        retList.add(tableMap);
                    }

                    messageBuilder.builder("users", baseList);

                    messageBuilder.builder("tableCount", groupDao.countGroupTables(groupId));
                } else {
                    retList = Collections.emptyList();

                    messageBuilder.builder("tableCount", 0);
                }

                messageBuilder.builder("list", retList);
                OutputUtil.output(messageBuilder, getRequest(), getResponse(), null, false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取我的军团
     */
    public void loadGroups() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            long userId = NumberUtils.toLong(params.get("mUserId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 10);
            int userRole = NumberUtils.toInt(params.get("userRole"), 0);
            int isTeamGroup = NumberUtils.toInt(params.get("isTeamGroup"), 0);  //是否是组
            if (userId <= 0 || pageNo < 1 || pageSize < 1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                if (pageSize > 30) {
                    pageSize = 30;
                }
                List<GroupUser> groupUserList = groupDao.loadGroupUsersByUser(userId, userRole, pageNo, pageSize);

                MessageBuilder messageBuilder = MessageBuilder.newInstance()
                        .builder("code", 0).builder("pageNo", pageNo).builder("pageSize", pageSize);
                if (groupUserList == null || groupUserList.size() == 0) {
                    messageBuilder.builder("groups", Collections.emptyList());
                } else {
                    List<GroupInfo> groupInfoList = new ArrayList<>(groupUserList.size());
                    for (GroupUser groupUser : groupUserList) {
                        if(isTeamGroup == 0) {
                            GroupInfo groupInfo = groupDao.loadGroupInfo(groupUser.getGroupId(), 0);
                            if (groupInfo != null) {
                                groupInfo.setGroupName(filterGroupName(groupInfo.getGroupName()));
                                groupInfoList.add(groupInfo);
                            }
                        } else {
                            if("0".equals(groupUser.getUserGroup()) || !GroupConstants.isTeamLeader(groupUser.getUserRole())){
                                continue;
                            }
                            GroupInfo groupInfo = groupDao.loadGroupInfo(groupUser.getGroupId(), 0);
                            HashMap<String,Object> mapTeam = groupDao.loadOneTeam(groupUser.getUserGroup());
                            int userCount = groupDao.countGroupUser(groupUser.getGroupId(), "", groupUser.getUserGroup());
                            if (groupInfo != null ) {
                                if(mapTeam != null) {
                                    groupInfo.setDescMsg(mapTeam.get("keyId").toString());      //userGroup 置入descMsg
                                    groupInfo.setExtMsg(filterGroupName(mapTeam.get("teamName").toString())); //组名置入extMsg
                                    groupInfo.setCurrentCount(userCount);    //人数换为组内人数
                                }
                                groupInfoList.add(groupInfo);
                            }
                        }
                    }
                    messageBuilder.builder("groups", groupInfoList);
                }

                OutputUtil.output(messageBuilder, getRequest(), getResponse(), null, false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 军团成员导入
     */
    public void copyGroups() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }

            long userId = NumberUtils.toLong(params.get("mUserId"), -1);
            int fromGroupId = NumberUtils.toInt(params.get("fromGroupId"), 1);
            int userGroup = NumberUtils.toInt(params.get("userGroup"), 0);
            int toGroupId = NumberUtils.toInt(params.get("toGroupId"), 10);
            if(fromGroupId <1 ){
                OutputUtil.output(1, "请选择需要导入的军团", getRequest(), getResponse(), false);
            }
            if (userId <= 0 || toGroupId < 1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                GroupInfo groupInfo0 = groupDao.loadGroupInfo(fromGroupId, 0);
                GroupInfo groupInfo1 = groupDao.loadGroupInfo(toGroupId, 0);
                if (groupInfo0 == null || groupInfo1 == null) {
                    OutputUtil.output(2, "俱乐部不存在", getRequest(), getResponse(), false);
                    return;
                }

                if (groupInfo1.getCurrentCount().intValue() >= groupInfo1.getMaxCount().intValue()) {
                    OutputUtil.output(2, "俱乐部人数已达到上限，无法导入", getRequest(), getResponse(), false);
                    return;
                }

                GroupUser groupUser0 = groupDao.loadGroupUser(userId, fromGroupId);
                GroupUser groupUser1 = groupDao.loadGroupUser(userId, toGroupId);
                if (groupUser0 == null || (groupUser0.getUserRole().intValue() != 0 && userGroup == 0) || (groupUser0.getUserRole().intValue() != 10 && userGroup > 0) || groupUser1 == null || (groupUser1.getUserRole().intValue() != 0 && groupUser1.getUserRole().intValue() != 10)) {
                    OutputUtil.output(2, "您不是群主（组长），无权操作", getRequest(), getResponse(), false);
                    return;
                }

                List<GroupUser> groupUserList0 = groupDao.loadGroupUsers(fromGroupId, 1, groupInfo0.getMaxCount().intValue(), Integer.toString(userGroup));

                int count = 0;
                if (groupUserList0 != null) {
                    for (GroupUser groupUser : groupUserList0) {
                        if (groupInfo1.getCurrentCount().intValue() + count >= groupInfo1.getMaxCount().intValue()) {
                            LOGGER.info("warn copy groupUser limit={},success={}", groupInfo1.getMaxCount(), count);
                            break;
                        }

                        GroupUser tempGroupUser = groupDao.loadGroupUser(groupUser.getUserId(), toGroupId);
                        if (tempGroupUser == null) {
                            tempGroupUser = new GroupUser();
                            tempGroupUser.setCreatedTime(new Date());
                            tempGroupUser.setGroupId(toGroupId);
                            tempGroupUser.setGroupName(groupInfo1.getGroupName());
                            tempGroupUser.setInviterId(userId);
                            tempGroupUser.setPlayCount1(0);
                            tempGroupUser.setPlayCount2(0);
                            tempGroupUser.setUserId(groupUser.getUserId());
                            tempGroupUser.setUserLevel(1);
                            tempGroupUser.setUserRole(2);
                            tempGroupUser.setUserName(groupUser.getUserName());
                            tempGroupUser.setUserNickname(groupUser.getUserNickname());
                            tempGroupUser.setUserGroup(groupUser1.getUserGroup());
                            tempGroupUser.setRefuseInvite(1);
                            if(GroupConstants.isTeamLeader(groupUser1.getUserRole())){
                                // 小组长的下一级
                                tempGroupUser.setPromoterLevel(1);
                            }
                            tempGroupUser.setCredit(0l);
                            try {
                                if(groupDao.createGroupUser(tempGroupUser)>0) {
                                    count++;
                                    LOGGER.info("copy join success groupUser={}", JSON.toJSONString(tempGroupUser));

                                    // 写入日志
                                    insertGroupUserAlert(toGroupId, groupUser.getUserId(), userId, GroupConstants.TYPE_USER_ALERT_INVITE);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    if (count > 0) {
                        groupDao.updateGroupInfoCount(count, groupInfo1.getGroupId());
                        LOGGER.info("copy join success count={},from={},to={}", count, fromGroupId, toGroupId);
                    }
                }


                MessageBuilder messageBuilder = MessageBuilder.newInstance()
                        .builderCodeMessage(0, "成功导入群员成员").builder("count", count).builder("total", groupInfo1.getCurrentCount() + count);

                OutputUtil.output(messageBuilder, getRequest(), getResponse(), null, false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 俱乐部新战绩
     */
    public void loadTablePlayLogs(){
        try{
            if(true){
                interfaceDeprecated();
                return;
            }
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("GroupAction|loadTablePlayLogs|params|{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("userId");
            if (StringUtils.isAnyBlank(groupId, userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            int queryDate = NumberUtils.toInt(params.get("queryDate"), 1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            int tableType = NumberUtils.toInt(params.get("tableType"), -1); //-1：全部，1：非信用房，2：信用房

            // 默认取当天数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar ca = Calendar.getInstance();
            switch(queryDate){
                case 2:
                    ca.set(Calendar.DATE, ca.get(Calendar.DATE) - 1);
                    break;
                case 3:
                    ca.set(Calendar.DATE, ca.get(Calendar.DATE) - 2);
                    break;
            }
            String strDate = sdf.format(ca.getTime());
            String startDate = "";
            String endDate = "";
            if (!strDate.contains(" ")) {
                startDate = strDate + " 00:00:00";
            }
            if (!strDate.contains(" ")) {
                endDate = strDate + " 23:59:59";
            }
            GroupUser groupUser = groupDao.loadGroupUser(NumberUtils.toLong(userId), NumberUtils.toLong(groupId));
            if (groupUser == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            List<Map<String, Object>> results = new ArrayList<>();
            int count = 0;
            List<GroupTable> groupTables = null;
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), 0);
            long tableId = NumberUtils.toLong(params.get("queryTableId"), 0);
            if (groupUser.getUserRole() > 1 || queryUserId!=0) {//会员

                String userGroup = null;
                if(GroupConstants.isTeamLeader(groupUser.getUserRole())){
                    // 小组长查看自己成员的
                    userGroup = groupUser.getUserGroup();
                }
                long promoterId = 0 ;
                int promoterLevel = 0;
                if(GroupConstants.isPromotor(groupUser.getUserRole())){
                    // 拉手查看自己的拉手组的信息
                    promoterId = groupUser.getUserId();
                    promoterLevel = groupUser.getPromoterLevel();
                }
                if(queryUserId == 0 && GroupConstants.isMember(groupUser.getUserRole())){
                    queryUserId = NumberUtils.toLong(userId);
                }
                List<Integer> tableIdList = groupDao.getUserGroupTableId(groupId, userGroup,queryUserId, tableType,promoterId,promoterLevel,tableId, startDate, endDate, pageNo, pageSize);
                if(tableIdList != null && !tableIdList.isEmpty()){
                    count = groupDao.getUserGroupTableIdCount(groupId,userGroup, queryUserId, tableType ,promoterId,promoterLevel,tableId,startDate, endDate);
                    StringBuilder tableNoStr = new StringBuilder();
                    for(Integer tabId : tableIdList){
                        tableNoStr.append(tabId).append(",");
                    }
                    tableNoStr.deleteCharAt(tableNoStr.length()-1);
                    groupTables = groupDao.getUserPlayLogGroupTable(tableNoStr.toString(),groupId);
                }
            }else{//军团长和副局团长
                // 2,4表示查询所有 4表示查询中途解散的；
                String condition = params.get("condition");
                if (StringUtils.isBlank(condition)) {
                    // 正常解散和中途解散
                    condition = "2,4";
                }
                count = groupDao.getPlayLogGroupTableCount(groupId, condition, tableId, tableType, startDate, endDate);
                if(count > 0){
                    groupTables = groupDao.getPlayLogGroupTable(groupId, condition, tableId, tableType, startDate, endDate, pageNo, pageSize);
                }
            }
            if (groupTables != null && !groupTables.isEmpty()) {
                StringBuilder tableNo = new StringBuilder();
                for (GroupTable groupTable : groupTables){
                    tableNo.append(groupTable.getKeyId()).append(",");
                }
                tableNo.deleteCharAt(tableNo.length()-1);
                List<HashMap<String,Object>> users = groupDao.loadTableUserByTableNo(tableNo.toString(),Long.valueOf(groupId));//tableNo userId playResult isWinner
                Map<String,List<Map<String,Object>>> tableUserMap = new HashMap<String, List<Map<String,Object>>>();
                List<String> userIdList = new ArrayList<>();
                StringBuilder userIds = new StringBuilder();
                for(Map<String,Object> user : users){
                    String uId = String.valueOf(user.get("userId"));
                    if(!userIdList.contains(uId)){
                        userIds.append(user.get("userId")).append(",");
                    }
                    String tNo = String.valueOf(user.get("tableNo"));
                    if(tableUserMap.containsKey(tNo)){
                        tableUserMap.get(tNo).add(user);
                    }else{
                        List<Map<String,Object>> list = new ArrayList<>();
                        list.add(user);
                        tableUserMap.put(tNo, list);
                    }
                }
                userIds.deleteCharAt(userIds.length()-1);
                List<HashMap<String,Object>> userNameList = userDao.loadUsersByUserId(userIds.toString());//userId userName
                Map<String,String> userNameMap = new HashMap<>();
                for(HashMap<String,Object> user : userNameList){
                    userNameMap.put(String.valueOf(user.get("userId")), String.valueOf(user.get("userName")));
                }

                SimpleDateFormat hfm = new SimpleDateFormat("HH:mm:ss");
                for (GroupTable groupTable : groupTables) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("overTime", hfm.format(groupTable.getOverTime()));
                    map.put("tableId", groupTable.getTableId());
                    String tableMsg = groupTable.getTableMsg();

                    String playType;
                    if (StringUtils.isNotBlank(tableMsg)) {
                        if(tableMsg.startsWith("{") && tableMsg.endsWith("}")){
                            JSONObject jsonObject = JSONObject.parseObject(tableMsg);
                            playType = jsonObject.getString("type");
                            if (StringUtils.isBlank(playType)){
                                String ints = jsonObject.getString("ints");
                                playType=ints.split(",")[1];
                            }
                        }else{
                            playType=tableMsg.split(",")[1];
                        }
                    }else{
                        playType = "0";
                    }
                    map.put("playType", playType);
                    List<Map<String,Object>> list = tableUserMap.get(String.valueOf(groupTable.getKeyId()));
                    StringBuilder players = new StringBuilder();
                    StringBuilder point = new StringBuilder();
                    StringBuilder isWinner = new StringBuilder();
                    StringBuilder winLoseCredit = new StringBuilder();
                    StringBuilder commissionCredit = new StringBuilder();
                    long minKeyId = 0;
                    int index = 0,masterNameIndex = 0;
                    if (list!=null) {
                        for (Map<String, Object> userMap : list) {
                            if (userMap.containsKey("userId")) {
                                String name = userNameMap.get(String.valueOf(userMap.get("userId")));
                                name = name == null ? "" : name.replace(",", "");
                                players.append(name).append(",");
                                point.append(userMap.get("playResult")).append(",");
                                isWinner.append(userMap.get("isWinner")).append(",");
                                winLoseCredit.append(userMap.get("winLoseCredit")).append(",");
                                commissionCredit.append(userMap.get("commissionCredit")).append(",");
                                long keyId = Long.valueOf(String.valueOf(userMap.get("keyId"))).longValue();
                                if (minKeyId == 0 || minKeyId > keyId) {
                                    minKeyId = keyId;
                                    masterNameIndex = index;
                                }
                                index++;
                            }
                        }
                    }
                    if(players.length()>0){
                        players.deleteCharAt(players.length()-1);
                        point.deleteCharAt(point.length()-1);
                        isWinner.deleteCharAt(isWinner.length()-1);
                        winLoseCredit.deleteCharAt(winLoseCredit.length()-1);
                        commissionCredit.deleteCharAt(commissionCredit.length()-1);
                        map.put("players", players.toString());
                        map.put("point", point.toString());
                        map.put("isWinner", isWinner.toString());
                        map.put("masterNameIndex", masterNameIndex);
                        map.put("winLoseCredit", winLoseCredit.toString());
                        map.put("commissionCredit", commissionCredit.toString());

                    }
                    //map.put("players", groupTable.getPlayers()==null?"":groupTable.getPlayers());
                    map.put("playedBureau", groupTable.getPlayedBureau());
                    map.put("currentState", groupTable.getCurrentState());
                    map.put("tableNo", groupTable.getKeyId());
                    map.put("roomName",groupTable.getTableName());
                    results.add(map);
                }
            }
            JSONObject json = new JSONObject();
            json.put("list", results);
            json.put("tables", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 更新俱乐部下的分组
     */
    public void updateGroupTeam(){
        try{
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String mUserId = params.get("mUserId");
            String keyId = params.get("keyId");
            String teamName = params.get("teamName");
            String type = params.get("msgType");
            String creditCommissionRate = params.get("creditCommissionRate");

            if (!CommonUtil.isPureNumber(mUserId)){
                OutputUtil.output(-1, "ID错误", getRequest(), getResponse(), false);
                return;
            }

            boolean isDelete = "delete".equals(type);

            HashMap<String,Object> map = groupDao.loadOneTeam(keyId);
            if (map==null||map.size()==0){
                OutputUtil.output(1, "小组不存在", getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(mUserId),Long.parseLong(map.get("groupKey").toString()));
            if (groupUser==null){
                OutputUtil.output(2, "您不是群组成员", getRequest(), getResponse(), false);
                return;
            }

            String userGroup = String.valueOf(map.get("teamGroup"));
            if ("0".equals(userGroup)){
                if (0!=groupUser.getUserRole().intValue()){
                    OutputUtil.output(3, "您不是群主,无法操作", getRequest(), getResponse(), false);
                    return;
                }
            }else if(!userGroup.equals(groupUser.getUserGroup())){
                OutputUtil.output(4, "您无权操作", getRequest(), getResponse(), false);
                return;
            }

            if (isDelete){
                int countUser = groupCreditDao.countUserHaveCreditForTeamLeader(groupUser.getGroupId(), keyId);
                if(countUser > 0){
                    OutputUtil.output(7, "组内有成员比赛分不为零，无法删除！", getRequest(), getResponse(), false);
                    return;
                }
                List<GroupUser> list = groupCreditDao.allUserForTeamLeader(groupUser.getGroupId(), String.valueOf(map.get("keyId")));
                int count = groupDao.deleteGroupUserByTeam(String.valueOf(map.get("groupKey")),String.valueOf(map.get("keyId")));
                if (count>0){
                    groupDao.updateGroupInfoCount(-count,groupUser.getGroupId());
                }
                groupDao.deleteTeam(keyId);

                // 写入日志
                if(list != null && list.size() > 0){
                    for(GroupUser gu : list){
                        insertGroupUserAlert(groupUser.getGroupId(), gu.getUserId(), groupUser.getUserId(), GroupConstants.TYPE_USER_ALERT_DELETE);
                    }
                }
            }else{
                HashMap<String,Object> map1 = new HashMap<>();
                if(!StringUtils.isBlank(creditCommissionRate)){
                    //信用分佣金分成模式,数据范围(0-10):如1代表分成比例为小组长10%,群主90%,
                    int rate = Integer.valueOf(creditCommissionRate);
                    if(rate < 0 || rate > 100){
                        OutputUtil.output(5, "设置非法的比例值["+rate+"],请输入(0-10)之间的整数", getRequest(), getResponse(), false);
                        return;
                    }
                    map1.put("creditCommissionRate",rate);
                }
                if (!StringUtils.isBlank(teamName)) {
                    if(teamName.length() > 16) {
                        OutputUtil.output(-1, "名字太长", getRequest(), getResponse(), false);
                        return;
                    }
                    map1.put("teamName",teamName);
                }
                if(map.size() > 0){
                    map1.put("keyId",keyId);
                    groupDao.updateTeam(map1);
                }
            }

            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(4, "系统异常,请稍后再试", getRequest(), getResponse(), false);
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 委任组长
     */
    public void appointTeamMaster(){
        try{
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String mUserId = params.get("mUserId");
            String oUserId = params.get("oUserId");
            String groupKey = params.get("groupId");
            String keyId = params.get("keyId");

            if (!CommonUtil.isPureNumber(mUserId)||!CommonUtil.isPureNumber(oUserId)||!CommonUtil.isPureNumber(keyId)){
                OutputUtil.output(-1, "ID错误", getRequest(), getResponse(), false);
                return;
            }

            if (!CommonUtil.isPureNumber(groupKey)){
                OutputUtil.output(-1, "俱乐部ID错误", getRequest(), getResponse(), false);
                return;
            }

            HashMap<String,Object> map = groupDao.loadOneTeam(keyId);
            if (map==null||map.size()==0){
                OutputUtil.output(1, "小组不存在", getRequest(), getResponse(), false);
                return;
            }

            GroupUser mGroupUser = groupDao.loadGroupUser(Long.parseLong(mUserId),Long.parseLong(groupKey));
            if (mGroupUser==null){
                OutputUtil.output(1, "您不是该俱乐部成员", getRequest(), getResponse(), false);
                return;
            }

            String teamGroup = String.valueOf(map.get("teamGroup"));
            if (teamGroup.equals("0")){
                if (mGroupUser.getUserRole().intValue()!=0||!teamGroup.equals(mGroupUser.getUserGroup())){
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }else{
                if (mGroupUser.getUserRole().intValue()!=10||!teamGroup.equals(mGroupUser.getUserGroup())){
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }

            GroupUser groupUser = groupDao.loadGroupTeamMaster(groupKey,keyId);
            if (groupUser!=null){
                OutputUtil.output(6, "已经设置了小组长", getRequest(), getResponse(), false);
                return;
            }

            GroupUser oGroupUser = groupDao.loadGroupUser(Long.parseLong(oUserId),Long.parseLong(groupKey));
            if (oGroupUser!=null){
                if (!StringUtils.equals(mGroupUser.getUserGroup(),oGroupUser.getUserGroup())){
                    OutputUtil.output(3, "不在同一分组", getRequest(), getResponse(), false);
                    return;
                }else if(oGroupUser.getUserRole().intValue()==0||oGroupUser.getUserRole().intValue()==10){
                    OutputUtil.output(7, "此人是群主(或组长)", getRequest(), getResponse(), false);
                    return;
                }else{
                    oGroupUser.setUserRole(10);
                    oGroupUser.setUserGroup(keyId);
                    HashMap<String,Object> map1 = new HashMap<>();
                    map1.put("keyId",oGroupUser.getKeyId().toString());
                    map1.put("userRole",oGroupUser.getUserRole().toString());
                    map1.put("userGroup",oGroupUser.getUserGroup());
                    groupDao.updateGroupUserByKeyId(map1);
                }
            }else{
                RegInfo user = userDao.getUser(Long.parseLong(oUserId));
                if (user==null){
                    OutputUtil.output(5, "玩家ID错误", getRequest(), getResponse(), false);
                    return;
                }

                oGroupUser = new GroupUser();
                oGroupUser.setCreatedTime(new Date());
                oGroupUser.setGroupId(mGroupUser.getGroupId());
                oGroupUser.setGroupName(mGroupUser.getGroupName());
                oGroupUser.setInviterId(mGroupUser.getUserId());
                oGroupUser.setPlayCount1(0);
                oGroupUser.setPlayCount2(0);
                oGroupUser.setUserId(user.getUserId());
                oGroupUser.setUserLevel(1);
                oGroupUser.setUserRole(10);
                oGroupUser.setUserName(user.getName());
                oGroupUser.setUserNickname(user.getName());
                oGroupUser.setUserGroup(keyId);
                oGroupUser.setCredit(0l);
                oGroupUser.setRefuseInvite(1);
                if(groupDao.createGroupUser(oGroupUser)<=0){
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }
                groupDao.updateGroupInfoCount(1,mGroupUser.getGroupId());
            }

            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(4, "系统异常,请稍后再试", getRequest(), getResponse(), false);
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 创建俱乐部下的分组
     */
    public void createGroupTeam(){
        try{
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String mUserId = params.get("mUserId");
            String oUserId = params.get("oUserId");
            String groupId = params.get("groupId");
            String teamName = params.get("teamName");

            if (!CommonUtil.isPureNumber(mUserId)||StringUtils.isNotBlank(oUserId)&&!CommonUtil.isPureNumber(oUserId)){
                OutputUtil.output(-1, "ID错误", getRequest(), getResponse(), false);
                return;
            }

            if (!CommonUtil.isPureNumber(groupId)){
                OutputUtil.output(-1, "俱乐部ID错误", getRequest(), getResponse(), false);
                return;
            }

            if (StringUtils.isBlank(teamName)){
                OutputUtil.output(-1, "组名不能为空", getRequest(), getResponse(), false);
                return;
            }else if (teamName.length()>16){
                OutputUtil.output(-1, "名字太长", getRequest(), getResponse(), false);
                return;
            }

            GroupUser mGroupUser = groupDao.loadGroupUser(Long.parseLong(mUserId),Long.parseLong(groupId));
            if (mGroupUser==null){
                OutputUtil.output(1, "您不是该俱乐部成员", getRequest(), getResponse(), false);
                return;
            }

            String teamGroup = mGroupUser.getUserGroup();

            if (teamGroup.equals("0")){
                if (mGroupUser.getUserRole().intValue()!=0){
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }else{
                if (mGroupUser.getUserRole().intValue()!=10){
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }

            if (NumberUtils.toInt(PropertiesCacheUtil.getValue("group_team_max",Constants.GAME_FILE),100) < groupDao.loadGroupTeamCount(groupId,teamGroup)){
                OutputUtil.output(8, "小组个数已达到上限", getRequest(), getResponse(), false);
                return;
            }

            if (CommonUtil.isPureNumber(oUserId)) {
                GroupUser oGroupUser = groupDao.loadGroupUser(Long.parseLong(oUserId), Long.parseLong(groupId));
                if (oGroupUser != null) {
                    if (!StringUtils.equals(mGroupUser.getUserGroup(), oGroupUser.getUserGroup())) {
                        OutputUtil.output(3, "不在同一分组", getRequest(), getResponse(), false);
                        return;
                    } else if (oGroupUser.getUserRole().intValue() == 0 || oGroupUser.getUserRole().intValue() == 10) {
                        OutputUtil.output(7, "此人是群主(或组长)", getRequest(), getResponse(), false);
                        return;
                    } else {
                        oGroupUser.setUserRole(10);
                        oGroupUser.setUserGroup(saveGroupRelation(groupDao, groupId, teamName, teamGroup).toString());
                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("keyId", oGroupUser.getKeyId().toString());
                        map1.put("userRole", oGroupUser.getUserRole().toString());
                        map1.put("userGroup", oGroupUser.getUserGroup());
                        groupDao.updateGroupUserByKeyId(map1);
                    }
                } else {
                    if(true){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(9, "玩家非本群成员，请先添加为本群成员"), getRequest(), getResponse(), null, false);
                        return;
                    }

                    RegInfo user = userDao.getUser(Long.parseLong(oUserId));
                    if (user == null) {
                        OutputUtil.output(5, "玩家ID错误", getRequest(), getResponse(), false);
                        return;
                    }

                    oGroupUser = new GroupUser();
                    oGroupUser.setCreatedTime(new Date());
                    oGroupUser.setGroupId(mGroupUser.getGroupId());
                    oGroupUser.setGroupName(mGroupUser.getGroupName());
                    oGroupUser.setInviterId(mGroupUser.getUserId());
                    oGroupUser.setPlayCount1(0);
                    oGroupUser.setPlayCount2(0);
                    oGroupUser.setUserId(user.getUserId());
                    oGroupUser.setUserLevel(1);
                    oGroupUser.setUserRole(10);
                    oGroupUser.setUserName(user.getName());
                    oGroupUser.setUserNickname(user.getName());
                    oGroupUser.setUserGroup(saveGroupRelation(groupDao, groupId, teamName, teamGroup).toString());
                    oGroupUser.setCredit(0l);
                    oGroupUser.setRefuseInvite(1);
                    if (groupDao.createGroupUser(oGroupUser) <= 0) {
                        groupDao.deleteTeam(oGroupUser.getUserGroup());
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                        return;
                    }
                    groupDao.updateGroupInfoCount(1, mGroupUser.getGroupId());
                }
            } else {
                saveGroupRelation(groupDao, groupId, teamName, teamGroup);
            }

            OutputUtil.output(0, "创建小组成功", getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(4, "系统异常,请稍后再试", getRequest(), getResponse(), false);
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    private final static Long saveGroupRelation(GroupDao groupDao,String groupKey,String teamName,String teamGroup) throws Exception{
        HashMap<String,Object> map = new HashMap<>();
        map.put("groupKey",groupKey);
        map.put("teamName",teamName);
        map.put("teamGroup",teamGroup);
        map.put("createdTime",CommonUtil.dateTimeToString());
        long ret = groupDao.saveGroupRelation(map);
        if (ret<=0){
            throw new SQLException("saveGroupRelation error:"+ret);
        }else{
            return ret;
        }
    }

    private final static void responseEmptyTeams(HttpServletRequest request, HttpServletResponse response,boolean isLoadZjs,boolean isLoadDyj){
        MessageBuilder messageBuilder = MessageBuilder.newInstance().builderCodeMessage(0,Collections.emptyList());
        if (isLoadZjs){
            messageBuilder.builder("zjsCount0",0);
        }
        if (isLoadDyj){
            messageBuilder.builder("dyjCount0",0);
        }
        OutputUtil.output(messageBuilder, request, response, null,false);
    }

    /**
     * 加载俱乐部下的分组
     */
    public void loadGroupTeams(){
        try{
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupKey = params.get("groupId");
            String userGroup = params.get("userGroup");
            if (StringUtils.isBlank(groupKey)){
                OutputUtil.output(-1, "俱乐部ID错误", getRequest(), getResponse(), false);
                return;
            }

            if (StringUtils.isBlank(userGroup)){
                userGroup = "0";
            }

            boolean isLoadZjs = "1".equals(params.get("zjs"));
            boolean isLoadDyj = "1".equals(params.get("dyj"));

            List<HashMap<String,Object>> teamList2 = groupDao.loadGroupTeams2(groupKey,userGroup);

            if (teamList2==null||teamList2.size()==0){
                responseEmptyTeams(request,response,isLoadZjs,isLoadDyj);
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (HashMap<String,Object> hm:teamList2){
                stringBuilder.append(",'").append(hm.get("keyId")).append("'");
            }

            String ugs = stringBuilder.substring(1);
            List<HashMap<String,Object>> teamList = groupDao.loadGroupTeams0(groupKey,ugs);

            if (teamList==null||teamList.size()==0){
                responseEmptyTeams(request,response,isLoadZjs,isLoadDyj);
                return;
            }

            List<HashMap<String,Object>> teamList1 = groupDao.loadGroupTeams1(groupKey,ugs);

            if (teamList1==null||teamList1.size()==0){
                responseEmptyTeams(request,response,isLoadZjs,isLoadDyj);
                return;
            }

            //各小组总局数
//            List<HashMap<String,Object>> zjsList = isLoadZjs ? groupDao.loadGroupData("zjs",groupKey,params.get("startDate"),params.get("endDate")):null;
//            //各小组大赢家数
//            List<HashMap<String,Object>> dyjList = isLoadDyj ? groupDao.loadGroupData("dyj",groupKey,params.get("startDate"),params.get("endDate")):null;

            List<HashMap<String, Object>> dataList = isLoadDyj ? groupDao.loadGroupDataZjsDyj(groupKey, params.get("startDate"), params.get("endDate")) : null;
            Map<String, HashMap<String, Object>> dataMap = new HashMap<>();
            if (dataList != null && dataList.size() > 0) {
                for (HashMap<String, Object> data : dataList) {
                    dataMap.put(String.valueOf(data.get("userGroup")), data);
                }
            }

            int zjsTotal = 0;
            int dyjTotal = 0;
            Iterator<HashMap<String,Object>> it = teamList.iterator();
            while (it.hasNext()){
                HashMap<String,Object> map = it.next();
                String userGroup0 = String.valueOf(map.get("userGroup"));
                if (teamList1!=null&&teamList2!=null&&StringUtils.isNotBlank(userGroup0)&&!"null".equals(userGroup0)){
                    boolean bl = true;
                    for (HashMap<String,Object> map1 :teamList1){
                        if (userGroup0.equals(String.valueOf(map1.get("userGroup")))){
                            map.put("userCount",map1.get("userCount"));
                            bl = false;
                            break;
                        }
                    }
                    for (HashMap<String,Object> map2 :teamList2){
                        if (userGroup0.equals(String.valueOf(map2.get("keyId")))){
                            map.put("teamName",map2.get("teamName"));
                            bl = false;
                            break;
                        }
                    }

                    if (isLoadZjs){
                        Object tempObj = 0;
                        HashMap<String, Object> data = dataMap.get(userGroup0);
                        if(data != null){
                            tempObj = data.get("zjsCount");
                        }
//                        if (zjsList!=null){
//                            for (HashMap<String,Object> map3 :zjsList){
//                                if (userGroup0.equals(String.valueOf(map3.get("userGroup")))){
//                                    tempObj = map3.get("mycount");
//                                    break;
//                                }
//                            }
//                        }
                        zjsTotal += ((Number)tempObj).intValue();
                        map.put("zjsCount",tempObj);
                    }
                    if (isLoadDyj){
                        Object tempObj = 0;
                        HashMap<String, Object> data = dataMap.get(userGroup0);
                        if(data != null){
                            tempObj = data.get("dyjCount");
                        }
//                        if (dyjList!=null){
//                            for (HashMap<String,Object> map3 :dyjList){
//                                if (userGroup0.equals(String.valueOf(map3.get("userGroup")))){
//                                    tempObj = map3.get("mycount");
//                                    break;
//                                }
//                            }
//                        }
                        dyjTotal += ((Number)tempObj).intValue();
                        map.put("dyjCount",tempObj);
                    }

                    if (bl){
                        it.remove();
                    }
                }else{
                    it.remove();
                }
            }

            MessageBuilder messageBuilder = MessageBuilder.newInstance().builderCodeMessage(0,teamList);
            if (isLoadZjs){
                Object tempObj = 0;
                HashMap<String, Object> data = dataMap.get("0");
                if(data != null){
                    tempObj = data.get("zjsCount");
                }
//                if (zjsList!=null){
//                    for (HashMap<String,Object> map3 :zjsList){
//                        if ("0".equals(String.valueOf(map3.get("userGroup")))){
//                            tempObj = map3.get("mycount");
//                            break;
//                        }
//                    }
//                }
//                zjsTotal += ((Number)tempObj).intValue();
                messageBuilder.builder("zjsCount0",tempObj);
                messageBuilder.builder("zjsTotal",zjsTotal);
            }
            if (isLoadDyj){
                Object tempObj = 0;
                HashMap<String, Object> data = dataMap.get("0");
                if(data != null){
                    tempObj = data.get("dyjCount");
                }
//                if (dyjList!=null){
//                    for (HashMap<String,Object> map3 :dyjList){
//                        if ("0".equals(String.valueOf(map3.get("userGroup")))){
//                            tempObj = map3.get("mycount");
//                            break;
//                        }
//                    }
//                }
//                dyjTotal += ((Number)tempObj).intValue();
                messageBuilder.builder("dyjCount0",tempObj);
                messageBuilder.builder("dyjTotal",dyjTotal);
            }

            OutputUtil.output(messageBuilder, getRequest(), getResponse(),null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取军团小组成员信息
     */
    public void loadGroupTeamUsers() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            String keyWord = params.get("keyWord");
            String userGroup = params.get("userGroup");
            if (groupId <= 0 || pageNo <= 0 || pageSize <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
            } else {
                if (pageSize > 30) {
                    pageSize = 30;
                }

                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                }

                List<GroupUser> list;
                JSONObject json = new JSONObject();
                json.put("pageNo", pageNo);
                json.put("pageSize", pageSize);

                if (StringUtils.isBlank(userGroup)){
                    userGroup="0";
                }

                int userCount = groupDao.countGroupUser(groupId, keyWord, userGroup);
                json.put("total",userCount);

                GroupUser masterGroupUser = "0".equals(userGroup)?groupDao.loadGroupMaster(String.valueOf(groupId)):groupDao.loadGroupTeamMaster(String.valueOf(groupId),userGroup);
                if (masterGroupUser != null){
                    Map<String,Object> tempMap = userDao.loadUserBase(masterGroupUser.getUserId().toString());
                    if (tempMap == null){
                        tempMap = new HashMap<>();
                        tempMap.put("userId",masterGroupUser.getUserId());
                        tempMap.put("userName",masterGroupUser.getUserNickname());
                    }else{
                        json.put("master",new HashMap<>(tempMap));
                    }
                }

                list = groupDao.loadGroupUsers(groupId, pageNo, pageSize, keyWord, userGroup);
                json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));

                if (list == null || list.size() == 0) {
                    json.put("list", Collections.emptyList());
                } else {
                    boolean isLoadZjs = "1".equals(params.get("zjs"));
                    boolean isLoadDyj = "1".equals(params.get("dyj"));

                    String userIds;
                    if (isLoadZjs||isLoadDyj){
                        StringBuilder stringBuilder = new StringBuilder();
                        for (GroupUser obj : list) {
                            stringBuilder.append(",'").append(obj.getUserId()).append("'");
                        }
                        userIds=stringBuilder.substring(1);
                    }else{
                        userIds="";
                    }

                    //各成员总局数
                    List<HashMap<String,Object>> zjsList = isLoadZjs ? groupDao.loadGroupTeamUserData("zjs",params.get("groupId"),userGroup,userIds,params.get("startDate"),params.get("endDate")):null;
                    //各成员大赢家数
                    List<HashMap<String,Object>> dyjList = isLoadDyj ? groupDao.loadGroupTeamUserData("dyj",params.get("groupId"),userGroup,userIds,params.get("startDate"),params.get("endDate")):null;

                    List<Map<String, Object>> list1 = new ArrayList<>(list.size());
                    for (GroupUser obj : list) {
                        JSONObject json0 = (JSONObject) JSONObject.toJSON(obj);
                        Map<String, Object> map = userDao.loadUserBase(obj.getUserId().toString());
                        if (map == null || map.size() == 0){
                            map = json0;
                        }

                        String userIdStr = obj.getUserId().toString();
                        if (isLoadZjs){
                            Object tempObj = 0;
                            if (zjsList!=null){
                                for (HashMap<String,Object> map3 :zjsList){
                                    if (userIdStr.equals(String.valueOf(map3.get("userId")))){
                                        tempObj = map3.get("mycount");
                                        break;
                                    }
                                }
                            }
                            map.put("zjsCount",tempObj);
                        }
                        if (isLoadDyj){
                            Object tempObj = 0;
                            if (dyjList!=null){
                                for (HashMap<String,Object> map3 :dyjList){
                                    if (userIdStr.equals(String.valueOf(map3.get("userId")))){
                                        tempObj = map3.get("mycount");
                                        break;
                                    }
                                }
                            }
                            map.put("dyjCount",tempObj);
                        }

                        list1.add(map);
                    }
                    json.put("list", list1);
                }

                boolean isLoadZjs = "1".equals(params.get("allZjs"));
                boolean isLoadDyj = "1".equals(params.get("allDyj"));
                //各成员总局数
                int allZjs = isLoadZjs ? groupDao.loadGroupTeamData("zjs",params.get("groupId"),userGroup,params.get("startDate"),params.get("endDate")):0;
                //各成员大赢家数
                int allDyj = isLoadDyj ? groupDao.loadGroupTeamData("dyj",params.get("groupId"),userGroup,params.get("startDate"),params.get("endDate")):0;

                if (isLoadZjs){
                    json.put("allZjs",allZjs);
                }
                if (isLoadDyj){
                    json.put("allDyj",allDyj);
                }

                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 信用分上下分记录
     */
    public void loadCreditLog() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int userId = NumberUtils.toInt(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            int isPositive = NumberUtils.toInt(params.get("isPositive"), 0);
            String keyWord = params.get("keyWord");
            String dateType = params.get("dateType");
            String selectType = params.get("selectType");        //1,通过keyWord查操作人 2,keyWord查被操作人 0都查
//            int onlySeft = NumberUtils.toInt(params.get("onlySeft"), 0);    //子看

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isMember(groupUser.getUserRole())) {
                // 普通成员只能看到自己的信用分
                keyWord = groupUser.getUserId() + "";
            }else if(GroupConstants.isPromotor(groupUser.getUserRole())){
                // 拉手只能看到自己的信用分
                keyWord = groupUser.getUserId() + "";
            }

            Map<String, Object> map = new HashMap<>(8);
            String userGroup = null;
            if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                // 组长看到本小组的信用分
                userGroup = groupUser.getUserGroup();
                //还看到自己相关的
                map.put("self", userId);
            }

            map.put("groupId", groupId);
            map.put("type", 1);
            map.put("startNo", (pageNo - 1) * pageSize);
            map.put("pageSize", pageSize);
            if (isPositive > 0) {
                map.put("isPositive", 1);
            } else if (isPositive < 0) {
                map.put("isNegative", 1);
            }
            if (StringUtils.isNotBlank(keyWord)) {
                map.put("keyWord", keyWord);
            }
            int dayOffset = 0;
            if ("2".equals(dateType)) {
                //昨天
                dayOffset = -1;
            } else if ("3".equals(dateType)) {
                //前天
                dayOffset = -2;
            }
            if (StringUtils.isNotBlank(userGroup)) {
                map.put("userGroup", Long.valueOf(userGroup));
            }
            if(StringUtils.isBlank(selectType)){
                selectType = "0";
            }
            map.put("selectType", selectType);
            map.put("startDate", TimeUtil.getStartOfDay(dayOffset));
            map.put("endDate", TimeUtil.getEndOfDay(dayOffset));
            int count = this.groupDao.countGroupCreditLog(map);
            List<HashMap<String, Object>> resList = new ArrayList<>();
            if (count > 0) {
                resList = this.groupDao.loadGroupCreditLog(map);
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("list", resList);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("total", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditLog|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * 加载俱乐部下的分组
     * 信用分相关
     */
    public void loadGroupTeamsCredit() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            String groupId = params.get("groupId");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            if (StringUtils.isBlank(groupId) || StringUtils.isBlank(groupId)) {
                OutputUtil.output(1, "俱乐部ID错误", getRequest(), getResponse(), false);
                return;
            }
            if (pageNo <= 0 || pageSize <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.valueOf(userId), Long.valueOf(groupId));
            if (groupUser == null) {
                OutputUtil.output(2, "非俱乐部成员", getRequest(), getResponse(), false);
                return;
            }
            if (groupUser.getUserRole() == 2) {
                OutputUtil.output(3, "非群主、管理员、小组长，权限不够", getRequest(), getResponse(), false);
                return;
            }
            String userGroup = "0";
            if (groupUser.getUserRole() <= 1) {
                //群主或管理员
                userGroup = null;
            } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                //小组长
                userGroup = groupUser.getUserGroup();
            }

            //小组人数量,总信用分
            List<HashMap<String, Object>> teamDataList = groupDao.loadGroupTeamsCredit(groupId, userGroup, pageNo, pageSize);
            //小组组长信息
            List<HashMap<String, Object>> teamMsgList = groupDao.loadGroupRelationCredit(groupId, userGroup);

            Map<String, HashMap<String, Object>> teamMsgMap = new HashMap<>();
            for (HashMap<String, Object> teamMsg : teamMsgList) {
                teamMsgMap.put(teamMsg.get("userGroup").toString(), teamMsg);
            }
            for (HashMap<String, Object> teamData : teamDataList) {
                HashMap<String, Object> teamMsg = teamMsgMap.get(teamData.get("userGroup").toString());
                if (teamMsg != null) {
                    teamData.putAll(teamMsg);
                } else {
                    if (teamData.get("userGroup").equals("0")) {
                        teamData.put("teamName", "本群");
                        teamData.put("userId", "");
                        teamData.put("userName", "群主");
                        teamData.put("headimgurl", "");
                        teamData.put("creditCommissionRate", "10");
                        teamData.put("userGroup", "0");
                        GroupUser master = null;
                        if (groupUser.getUserRole() == 0) {
                            master = groupUser;
                        } else {
                            master = groupDao.loadGroupMaster(groupId);
                        }
                        if (master == null) {
                            continue;
                        }
                        RegInfo regInfo = userDao.getUser(master.getUserId());
                        if (regInfo == null) {
                            continue;
                        }
                        teamData.put("userId", regInfo.getUserId());
                        teamData.put("userName", regInfo.getName());
                        teamData.put("headimgurl", regInfo.getHeadimgurl());
                    }
                }

            }
            JSONObject json = new JSONObject();
            json.put("teamList", teamDataList);
            json.put("myCredit", groupUser.getCredit());
            json.put("totalCredit", groupDao.sumTeamCredit(groupId, userGroup));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取军团小组成员信息
     * 信用分相关
     */
    public void loadGroupTeamUsersCredit() {
        try {
            long currTime = System.currentTimeMillis();

            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            String keyWord = params.get("keyWord");
            String userGroup = params.get("userGroup");  //-1 则不限定userGroup
            if (pageNo < 0) {
                pageNo = 1;
            }
            if (pageSize > 30 || pageSize < 0) {
                pageSize = 30;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (StringUtils.isBlank(userGroup)) {
                userGroup = "0";
            } else if(userGroup.equals("-1")) {
                userGroup = StringUtils.EMPTY;
            }
            if (GroupConstants.isMember(groupUser.getUserRole()) && !StringUtils.isBlank(userGroup)) {
                //普通成员默认查看自己的数据
                keyWord = groupUser.getUserId().toString();
                userGroup = groupUser.getUserGroup();
            }
            JSONObject json = new JSONObject();
            int userCount = groupDao.countGroupUser(groupId, keyWord, userGroup);
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));

            //小组长信息
            GroupUser teamMaster = null;
            if ("0".equals(userGroup) || StringUtils.isBlank(userGroup)) {
                teamMaster = groupDao.loadGroupMaster(String.valueOf(groupId));
            } else {
                teamMaster = groupDao.loadGroupTeamMaster(String.valueOf(groupId), userGroup);
            }
            String masterUid = "";
            if (teamMaster != null) {
                masterUid = teamMaster.getUserId().toString();
                Map<String, Object> tempMap = userDao.loadUserBase(teamMaster.getUserId().toString());
                if (tempMap != null) {
                    json.put("master", tempMap);
                } else {
                    tempMap = new HashMap<>();
                    tempMap.put("userId", teamMaster.getUserId());
                    tempMap.put("userName", teamMaster.getUserNickname());
                }
            }
            List<HashMap<String, Object>> userList = groupDao.loadGroupTeamUsersCredit(String.valueOf(groupId), userGroup, keyWord, pageNo, pageSize);

            List<HashMap<String, Object>> masterList =  null;
            if(StringUtils.isNotBlank(masterUid) && Long.parseLong(masterUid) != userId && GroupConstants.isMember(groupUser.getUserRole())) {
                masterList = groupDao.loadGroupTeamUsersCredit(String.valueOf(groupId), "", masterUid, 1, pageSize);
            }
            if (userList == null || userList.size() == 0) {
                json.put("userList", Collections.emptyList());
            } else {
                if(masterList != null){
                    userList.addAll(0, masterList);
                }
                for(HashMap<String, Object> map : userList){
                    int role = Integer.parseInt(map.get("userRole").toString());
                    String uGroup = map.get("userGroup").toString();
                    if(GroupConstants.isMaster(groupUser.getUserRole())){
                        map.put("opType",1);       //管理员可以给群内任意人上下分
                    } else if(GroupConstants.isAdmin(groupUser.getUserRole())){
                        //管理员只能给群主和管理员上分 其他都可以上下分
                        if(GroupConstants.isMaster(role) || GroupConstants.isAdmin(role)){
                            map.put("opType",2);
                        } else {
                            map.put("opType", 1);
                        }
                    } else if(GroupConstants.isTeamLeader(groupUser.getUserRole())){
                        //组长只能给管理员、群主、组长上分； 可以给自己组内成员上下分； 不能操作非组内普通成员
                        if(GroupConstants.isMaster(role) || GroupConstants.isAdmin(role) || GroupConstants.isTeamLeader(role)) {
                            map.put("opType", 2);
                        } else if(GroupConstants.isMember(role) && uGroup.equals(groupUser.getUserGroup())){
                            map.put("opType", 1);
                        } else {
                            map.put("opType", 0);
                        }
                    } else if(GroupConstants.isMember(groupUser.getUserRole())){
                        //成员只能给群主，管理员，自己的组长上分
                        if(GroupConstants.isMaster(role) || GroupConstants.isAdmin(role) || (GroupConstants.isTeamLeader(role) && uGroup.equals(groupUser.getUserGroup()))){
                            map.put("opType", 2);
                        } else {
                            map.put("opType", 0);
                        }
                    }
                    if(map.get("userId").toString().equals(groupUser.getUserId().toString()) && !GroupConstants.isMaster(groupUser.getUserRole())){
                        map.put("opType", 0);
                    }
                }
                for(int i = 0; i< userList.size(); i++){
                    HashMap<String, Object> map =  userList.get(i);
                    if(map.get("opType").toString().equals("0") && !map.get("userId").toString().equals(groupUser.getUserId().toString())){
                        userList.remove(map);
                    }
                }
                json.put("userList", userList);
            }

            json.put("myCredit", groupUser.getCredit());
            json.put("totalCredit", groupDao.sumTeamCredit(groupId + "", userGroup));

            //限群主和管理员，根据id查询额外显示用户的小组信息
            if ((GroupConstants.isMaster(groupUser.getUserRole()) || GroupConstants.isAdmin(groupUser.getUserRole())) && StringUtils.isNotBlank(keyWord)) {
                if (userList != null && userList.size() > 0) {
                    String ug = (String) userList.get(0).get("userGroup");
                    if (StringUtils.isNotBlank(ug)) {
                        if (ug.equals("0")) {
                            GroupUser tm = groupDao.loadGroupMaster(groupId + "");
                            Map<String, Object> tmMap = new HashMap();
                            tmMap.put("userName", tm.getUserName());
                            tmMap.put("userId", tm.getUserId());
                            tmMap.put("userGroup", tm.getUserGroup());
                            tmMap.put("teamName", "本群");
                            json.put("teamMsg", tmMap);
                        } else {
                            GroupUser tm = groupDao.loadGroupTeamMaster(groupId + "", ug);
                            if (tm != null) {
                                Map<String, Object> tmMap = new HashMap();
                                tmMap.put("userName", tm.getUserName());
                                tmMap.put("userId", tm.getUserId());
                                tmMap.put("userGroup", tm.getUserGroup());
                                tmMap.put("teamName", tm.getUserName());
                                HashMap<String, Object> gr = groupDao.loadOneTeam(ug);
                                if (gr != null) {
                                    tmMap.put("teamName", gr.get("teamName"));
                                }
                                json.put("teamMsg", tmMap);
                            }
                        }
                    }
                }
            }
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
//            System.out.println("loadGroupTeamUsersCredit useTime:"+ (System.currentTimeMillis() - currTime) + "  userList.size = " + userList.size());
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 修改军团成员信用分
     * 信用分相关
     */
    @Deprecated
    public void updateGroupUserCredit() {
        try {

            if(true){
                interfaceDeprecated();
                return;
            }

            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (!checkSessCode(Long.valueOf(userId), params.get("sessCode"))) {
                return;
            }
            long destUserId = NumberUtils.toLong(params.get("destUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            if (userId == -1 || groupId == -1 || destUserId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, Long.valueOf(groupId));
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
//            if (GroupConstants.isMember(groupUser.getUserRole())) {
//                //普通成员无权操作
//                OutputUtil.output(2, "群主或管理员或小组长才有权限", getRequest(), getResponse(), false);
//                return;
//            }

            //被操作的成员
            GroupUser destGroupUser = groupDao.loadGroupUser(destUserId, groupId);
            if (destGroupUser == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            RegInfo destRegInfo = this.userDao.getUser(destUserId);
            if (destRegInfo == null || destRegInfo.getPlayingTableId() > 0) {
                OutputUtil.output(6, "成员在牌局中，不能进行此操作", getRequest(), getResponse(), false);
                return;
            }

            int credit = NumberUtils.toInt(params.get("credit"), 0);

            if (GroupConstants.isMaster(groupUser.getUserRole()) && userId == destUserId) {
                //只有群主可以对自己信用分进行操作
                if (credit < 0 && destGroupUser.getCredit() < Math.abs(credit)) {
                    OutputUtil.output(7, "成员比赛分不够本次扣除", getRequest(), getResponse(), false);
                    return;
                }
                int updateResult = updateGroupUserCredit(groupUser, destGroupUser, credit);
                if (updateResult == 1) {
                    if (destRegInfo.getEnterServer() > 0) {
                        GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), userId, groupId);
                    }
                }
                LOGGER.info("updateGroupUserCredit|1|" + groupId + "|" + updateResult + "|" + userId + "|" + destUserId + "|" + credit);
            } else {
                //信用分转移
                if (userId == destUserId) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                if (GroupConstants.isAdmin(groupUser.getUserRole()) && GroupConstants.isMaster(destGroupUser.getUserRole()) && credit < 0) {
                    //管理员无权对群主进行下分操作
                    OutputUtil.output(7, "无权对群主进行操作!", getRequest(), getResponse(), false);
                    return;
                }
                if (GroupConstants.isTeamLeader(groupUser.getUserRole()) && !groupUser.getUserGroup().equals(destGroupUser.getUserGroup()) && !((GroupConstants.isTeamLeader(destGroupUser.getUserRole()) || destGroupUser.getUserRole() < 2)  && credit > 0)) {
                    //小组长,只能对本小组成员进行操作 和对小组长或群主管理员进行上分操作
                    OutputUtil.output(7, "非本组成员无权操作!", getRequest(), getResponse(), false);
                    return;
                }
                if (GroupConstants.isMember(groupUser.getUserRole()) && !(credit >0 && (GroupConstants.isMaster(destGroupUser.getUserRole()) || GroupConstants.isAdmin(destGroupUser.getUserRole()) || GroupConstants.isTeamLeader(destGroupUser.getUserRole()) && destGroupUser.getUserGroup().equals(groupUser.getUserGroup())))) {
                    //普通成员只能给群主 管理员 本组组长上分
                    OutputUtil.output(7, "无权操作!", getRequest(), getResponse(), false);
                    return;
                }
                if (credit < 0 && destGroupUser.getCredit() < Math.abs(credit)) {
                    OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_16), getRequest(), getResponse(), false);
                    return;
                } else if (credit > 0 && groupUser.getCredit() < credit) {
                    OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_15), getRequest(), getResponse(), false);
                    return;
                }

                //信用分从fromId转移到destId
                long fromId = groupUser.getUserId();
                long destId = destGroupUser.getUserId();
                if (credit < 0) {
                    //当信用分为负数时，表示信用分从destGroupUser转移到groupUser
                    fromId = destGroupUser.getUserId();
                    destId = groupUser.getUserId();
                }
                int transferResult = groupDao.transferGroupUserCredit(fromId, destId, groupUser.getGroupId(), Math.abs(credit));
                if (transferResult == 2) {
                    // 写入日志
                    HashMap<String, Object> logFrom = new HashMap<>();
                    logFrom.put("groupId", groupUser.getGroupId());
                    logFrom.put("optUserId", fromId);
                    logFrom.put("userId", destId);
                    logFrom.put("tableId", 0);
                    logFrom.put("credit", Math.abs(credit));
                    logFrom.put("type", 1);
                    logFrom.put("flag", 1);
                    logFrom.put("userGroup", destGroupUser.getUserGroup());
                    logFrom.put("mode", fromId == userId ? 1 : 0);
                    groupDao.insertGroupCreditLog(logFrom);

                    // 写入日志
                    HashMap<String, Object> logDest = new HashMap<>();
                    logDest.put("groupId", groupUser.getGroupId());
                    logDest.put("optUserId", destId);
                    logDest.put("userId", fromId);
                    logDest.put("tableId", 0);
                    logDest.put("credit", -Math.abs(credit));
                    logDest.put("type", 1);
                    logDest.put("flag", 1);
                    logDest.put("userGroup", destGroupUser.getUserGroup());
                    logDest.put("mode", fromId == destUserId ? 1 : 0);
                    groupDao.insertGroupCreditLog(logDest);
                } else {
                    OutputUtil.output(7, "比赛分不够", getRequest(), getResponse(), false);
                    return;
                }
                LOGGER.info("updateGroupUserCredit|2|" + transferResult + "|" + groupId + "|" + groupUser.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit);
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    private int updateGroupUserCredit(GroupUser groupUser, GroupUser destGroupUser, int credit) {
        int updateResult = -1;
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("credit", credit);
            map.put("keyId", destGroupUser.getKeyId().toString());
            updateResult = groupDao.updateGroupUserCredit(map);
            if (updateResult == 1) {
                JSONObject json = new JSONObject();
                GroupUser tmp = this.groupDao.loadGroupUser(groupUser.getUserId(), groupUser.getGroupId());
                json.put("credit", tmp != null ? tmp.getCredit() : 0);
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            } else {
                OutputUtil.output(0, "操作失败", getRequest(), getResponse(), false);
            }
            if (updateResult == 1) {
                // 写入日志
                HashMap<String, Object> creditLog = new HashMap<>();
                creditLog.put("groupId", groupUser.getGroupId());
                creditLog.put("optUserId", groupUser.getUserId());
                creditLog.put("userId", destGroupUser.getUserId());
                creditLog.put("tableId", 0);
                creditLog.put("credit", credit);
                creditLog.put("type", 1);
                creditLog.put("flag", 1);
                creditLog.put("userGroup", destGroupUser.getUserGroup());
                creditLog.put("mode", 1);
                groupDao.insertGroupCreditLog(creditLog);
            }
        } catch (Exception e) {
            LOGGER.error("updateGroupUserCredit|error|" + updateResult + "|" + groupUser.getGroupId() + "|" + groupUser.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit+"|", e);
        }
        return updateResult;
    }


    /**
     * 小组信用分佣金记录
     * 以小组为单位统计佣金
     */
    public void groupTeamCreditCommission() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int userId = NumberUtils.toInt(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isMember(groupUser.getUserRole())) {
                //普通成员无权查看
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                //管理员以群主的身份查看
                groupUser = groupDao.loadGroupMaster(String.valueOf(groupId));
            }

            String dateType = params.get("dateType"); // 1:今天,2:昨天,3:前天

            //小组信用房总局数
            List<HashMap<String, Object>> zjsList = groupDao.loadGroupTeamCreditZjs(String.valueOf(groupId), dateType);
            Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
            for (HashMap<String, Object> zjs : zjsList) {
                zjsMap.put(zjs.get("userGroup").toString(), zjs);
            }

            //小组组长信息
            List<HashMap<String, Object>> teamMsgList = groupDao.loadGroupRelationCredit(String.valueOf(groupId), null);
            Map<String, HashMap<String, Object>> teamMsgMap = new HashMap<>();
            for (HashMap<String, Object> teamMsg : teamMsgList) {
                teamMsgMap.put(teamMsg.get("userGroup").toString(), teamMsg);
            }
            List<HashMap<String, Object>> resList = groupDao.loadGroupTeamCommissionCreditLog(String.valueOf(groupId), groupUser.getUserId().toString(), dateType);

            for (HashMap<String, Object> res : resList) {
                String userGroup1 = res.get("userGroup").toString();
                HashMap<String, Object> zjs = zjsMap.get(userGroup1);
                if (zjs != null) {
                    res.putAll(zjs);
                } else {
                    res.put("zjs", 0);
                }
                HashMap<String, Object> teamMsg = teamMsgMap.get(userGroup1);
                if (teamMsg != null) {
                    res.putAll(teamMsg);
                } else {
                    if("0".equals(userGroup1)){
                        //群主的小组
                        res.put("teamName", "本群");
                        res.put("userId", "000000");
                        res.put("userName", "群主");
                        res.put("headimgurl", "" );

                        GroupUser master = null;
                        if(GroupConstants.isMaster(groupUser.getUserRole())){
                            master = groupUser;
                        }else{
                            master = groupDao.loadGroupMaster(groupId+"");
                        }
                        if (master == null) {
                            continue;
                        }
                        RegInfo regInfo = userDao.getUser(master.getUserId());
                        if (regInfo == null) {
                            continue;
                        }
                        res.put("userId", master.getUserId());
                        res.put("userName", regInfo.getName());
                        res.put("headimgurl", regInfo.getHeadimgurl());
                    }else{
                        res.put("teamName", "已删除小组");
                        res.put("userId", "000000");
                        res.put("userName", "小组长");
                        res.put("headimgurl", "");
                    }
                }
            }

            JSONObject json = new JSONObject();
            json.put("list", resList);
            json.put("totalCommissionCredit", groupDao.sumGroupTeamCommissionCreditLog(String.valueOf(groupId), groupUser.getUserId().toString(), null, dateType));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditLog|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 小组成员信用分佣金记录
     * 以小组内成员为单位统计佣金
     */
    public void groupTeamUserCreditCommission() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int userId = NumberUtils.toInt(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            String userGroup = params.get("userGroup");
            String keyWord = params.get("keyWord");

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isMember(groupUser.getUserRole())) {
                //普通成员无权查看
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                //管理员以群主的身份查看
                groupUser = groupDao.loadGroupMaster(String.valueOf(groupId));
            } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                //只能查看自己小组的数据
                userGroup = groupUser.getUserGroup();
            }
            String dateType = params.get("dateType"); // 1:今天,2:昨天,3:前天

            JSONObject json = new JSONObject();
            int userCount = groupDao.countGroupTeamUserCreditLog(groupId + "", groupUser.getUserId().toString(), userGroup, keyWord, dateType);
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));

            List<HashMap<String, Object>> resList = groupDao.loadGroupTeamUserCreditLog(groupId + "", groupUser.getUserId().toString(), userGroup, keyWord, dateType, pageNo, pageSize);

            List<HashMap<String, Object>> zjsList = groupDao.loadGroupTeamUserCreditZjs(groupId + "", groupUser.getUserGroup(), dateType);
            Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
            for (HashMap<String, Object> zjs : zjsList) {
                zjsMap.put(zjs.get("userId").toString(), zjs);
            }
            for (HashMap<String, Object> res : resList) {
                HashMap<String, Object> zjs = zjsMap.get(res.get("userId").toString());
                if (zjs != null) {
                    res.putAll(zjs);
                } else {
                    res.put("zjs", 0);
                }
            }
            json.put("list", resList);
            json.put("totalCommissionCredit", groupDao.sumGroupTeamCommissionCreditLog(String.valueOf(groupId), groupUser.getUserId().toString(), userGroup, dateType));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditLog|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }



    /**
     *  是否开启信用房
     * @param groupId
     * @return
     */
    private boolean isOpenCredit(long groupId) {
        try {
            GroupInfo groupInfo = groupDao.loadGroupInfoAll(groupId, 0);
            if (groupInfo == null)
                return false;
            if (groupInfo.getIsCredit() == 0)
                return false;
            return true;
        } catch (Exception e) {
            LOGGER.error("loadCreditLog|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            return false;
        }
        //        String credit_white_group_ids = ResourcesConfigsUtil.loadServerPropertyValue("credit_white_group_ids", "");
        //        return "".equals(credit_white_group_ids) || credit_white_group_ids.contains("," + groupId + ",");
    }

    public boolean checkSessCode(long userId, String sessCode) throws Exception {
        if (userId <= 0 || StringUtils.isBlank(sessCode)) {
            return false;
        }
        RegInfo user = userDao.getUserForceMaster(userId);
        if (user == null || !sessCode.equals(user.getSessCode())) {
            OutputUtil.output(4, "登录信息验证失败！", getRequest(), getResponse(), false);
            return false;
        }
        return true;
    }

    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setDataStatisticsDao(DataStatisticsDao dataStatisticsDao) {
        this.dataStatisticsDao = dataStatisticsDao;
    }

    public void setGroupDaoNew(GroupDaoNew groupDaoNew) {
        this.groupDaoNew = groupDaoNew;
    }

    private boolean iSphoneBangding(){
        String s = ResourcesConfigsUtil.loadServerPropertyValue("phoneBangding", "0");
        if ("1".equals(s)){
            return true;
        }
        return false;
    }

    /**
     * 不活跃成员
     */
    public void loadInactiveUserList() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadInactiveUserList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int userId = NumberUtils.toInt(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 300);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                //普通成员无权查看
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            String dateType = params.get("dateType"); // 1:一个月,2:二个月,3:三个月
            JSONObject json = new JSONObject();
            int userCount = groupDao.countInactiveUser(groupId, dateType);
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));
            List<HashMap<String, Object>> resList = groupDao.loadInactiveUser(groupId, dateType, pageNo, pageSize);
            json.put("list", resList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("loadInactiveUserList|error|" + e.getMessage(), e);
        }
    }

    /**
     * 踢出非活跃用户
     */
    public void fireInactiveUser() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("fireInactiveUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int userId = NumberUtils.toInt(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            if(groupId <= 0 ){
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                //普通成员无权查看
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            String userIdsStr = params.get("userIds");
            if(StringUtils.isBlank(userIdsStr)){
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String[] userIds = userIdsStr.split(",");
            List<String> delIdList = new ArrayList<>(userIds.length);
            for (String delId : userIds) {
                if (CommonUtil.isPureNumber(delId)) {
                    delIdList.add(delId);
                }
            }
            int delCount = 0;
            if (delIdList.size() > 0) {
                delCount = groupDao.fireInactiveUser(delIdList, groupId);
                if (delCount > 0) {
                    groupDao.updateGroupInfoCount(-delCount, groupId);
                }
            }
            OutputUtil.output(0, "操作成功：踢出" + delCount + "人", getRequest(), getResponse(), false);
            LOGGER.info("fireInactiveUser|succ|params:{}", params);
        } catch (Exception e) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("fireInactiveUser|error|" + e.getMessage(), e);
        }
    }

    /**
     * 新：创建军团包厢
     */
        public void createGroupRoom() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("createGroupRoom|params|{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            // 创建t_group的参数
            String groupName = params.get("groupName");
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int groupId = NumberUtils.toInt(params.get("groupId"), 0);

            // 创建t_group_table_config的参数
            int gameType = NumberUtils.toInt(params.get("gameType"), -1);
            int payType = NumberUtils.toInt(params.get("payType"), -1);
            int gameCount = NumberUtils.toInt(params.get("gameCount"), -1);
            int playerCount = NumberUtils.toInt(params.get("playerCount"), -1);
            int fakeCount = NumberUtils.toInt(params.get("fakeCount"), -1);//创建假桌子的数量
            String modeMsg = params.get("modeMsg");//牌局详细参数
            String tableName = params.get("tableName");
            String descMsg = params.get("descMsg");
            String tableMode = params.get("tableMode");
            String creditMsg = params.get("creditMsg");
            String goldMsg = params.get("goldMsg");
            if (StringUtils.isBlank(creditMsg)) {
                creditMsg = "";
            }
            if(StringUtils.isBlank(goldMsg)){
                goldMsg = "";
            }

            int tableOrder = NumberUtils.toInt(params.get("tableOrder"), 1);

            if (StringUtils.isBlank(groupName)) {
                OutputUtil.output(1, "请输入包间名字！", getRequest(), getResponse(), false);
                return;
            } else if (userId <= 0 || groupId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (tableOrder <= 0 || tableOrder >= 1000000) {
                OutputUtil.output(1, "牌局序号错误", getRequest(), getResponse(), false);
                return;
            } else if (gameType <= -1 || payType <= -1 || gameCount <= 0 || playerCount <= 0 || userId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (StringUtils.isBlank(modeMsg)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
            if (!groupName.matches(regex)) {
                OutputUtil.output(1, "俱乐部名称仅限字母数字和汉字", getRequest(), getResponse(), false);
                return;
            }
            groupName = groupName.trim();
            String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
            if (!groupName.equals(groupName0)) {
                OutputUtil.output(1, "军团名不能包含敏感字符", getRequest(), getResponse(), false);
                return;
            }
            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                OutputUtil.output(2, "玩家ID错误", getRequest(), getResponse(), false);
                return;
            }

            int count = 0;
            synchronized (GroupAction.class) {
                GroupInfo group = groupDaoNew.loadGroupForceMaster(groupId);
                if (group == null || !GroupConstants.canCreatePlayType(group, gameType)) {
                    OutputUtil.output(2, "您无法添加该玩法", getRequest(), getResponse(), false);
                    return;
                }
                if (group.getIsCredit() != GroupInfo.isCredit_credit) {
                    creditMsg = "";
                } else if (group.getIsCredit() != GroupInfo.isCredit_gold) {
                    goldMsg = "";
                }

                GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null || !GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                    return;
                }
                int groupRoomCount = groupDao.countGroupRoom(groupUser.getGroupId());
                int limit = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "GroupTableConfigCount",80);
                if (groupRoomCount >= limit) {
                    OutputUtil.output(3, "已达包间数量上限" + limit + "个", getRequest(), getResponse(), false);
                    return;
                }
                // 取个未使用包间号
                int subId = 0;
                List<Integer> roomIds = groupDao.loadGroupRoomId(groupUser.getGroupId().toString());
                for (int i = 1; i <= 50000; i++) {
                    if (!roomIds.contains(i)) {
                        subId = i;
                        break;
                    }
                }
                if (subId == 0) {
                    LOGGER.info("createGroupRoom|fail|subId|" + subId);
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
                    return;
                }

                GroupInfo exist = groupDao.loadGroupInfoAll(subId, groupUser.getGroupId());
                if (exist != null) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
                    return;
                }

                // 创建t_group
                GroupInfo newGroup = new GroupInfo();
                newGroup.setCreatedTime(new Date());
                newGroup.setCreatedUser(userId);
                newGroup.setCurrentCount(1);
                newGroup.setDescMsg("");
                newGroup.setGroupId(subId);
                newGroup.setParentGroup(groupUser.getGroupId());
                newGroup.setExtMsg("");
                newGroup.setGroupLevel(0);
                newGroup.setGroupMode(0);
                newGroup.setGroupName(groupName0);
                newGroup.setMaxCount(0);
                newGroup.setGroupState("1");
                newGroup.setModifiedTime(newGroup.getCreatedTime());
                long newGroupKeyId = groupDao.createGroup(newGroup);
                if (newGroupKeyId <= 0) {
                    LOGGER.info("createGroupRoom|fail|insertGroup|" + newGroupKeyId);
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }

                // 创建t_group_table_config
                GroupTableConfig newConfig = new GroupTableConfig();
                newConfig.setCreatedTime(new Date());
                newConfig.setDescMsg(descMsg);
                newConfig.setGroupId(Long.valueOf(subId));
                newConfig.setParentGroup(groupUser.getGroupId().longValue());
                newConfig.setModeMsg(modeMsg);
                newConfig.setPlayCount(0L);
                newConfig.setTableMode(tableMode);
                newConfig.setTableName(groupName0);
                newConfig.setTableOrder(tableOrder);
                newConfig.setGameType(gameType);
                newConfig.setGameCount(gameCount);
                newConfig.setPayType(payType);
                newConfig.setPlayerCount(playerCount);
                newConfig.setConfigState("1");
                newConfig.setCreditMsg(creditMsg);
                newConfig.setGoldMsg(goldMsg);
                long newConfigKeyId = groupDao.createGroupTableConfig(newConfig);
                if (newConfigKeyId <= 0) {
                    LOGGER.info("createGroupRoom|fail|insertConfig|" + newGroupKeyId);
                    groupDao.deleteGroupInfoByKeyId(String.valueOf(newGroupKeyId));
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }

                MessageBuilder resMsg = MessageBuilder.newInstance();
                resMsg.builderCodeMessage(0, "包厢创建成功");
                resMsg.builder("groupId", groupId);
                resMsg.builder("subId", subId);
                resMsg.builder("configId", newConfigKeyId);
                OutputUtil.output(resMsg, getRequest(), getResponse(), null, false);
                LOGGER.info("createGroupRoom|success|{}|{}|{}", JSON.toJSONString(newGroup), count + 1, JSON.toJSONString(newConfig));

                if(GroupConstants.isFuHuiZhang(groupUser.getUserRole())){
                    GroupUser master = groupDaoNew.loadGroupMaster(groupUser.getGroupId());
                    if(master != null){
                        userId = master.getUserId();
                    }
                }

                autoCreateGroupTableNew(userId, groupUser.getGroupId(), newConfigKeyId);


                if (fakeCount > 0){
                    //创建假桌子
                    if(fakeCount > 50){
                        fakeCount = 50;     //一次最多带50张假桌子
                    }
                    int overCount =  NumberUtils.toInt(params.get("overCount"), -1);
                    if (overCount <= 0){
                        overCount = gameCount >= 100 ? MathUtil.mt_rand(5, 10) :gameCount;
                    }
                    for(int i = 1; i <= fakeCount; i++) {
                        GroupFakeTable fakeTable = new GroupFakeTable();
                        if (StringUtils.isNotBlank(descMsg)) {
                            fakeTable.setDescMsg(descMsg);
                        }
                        fakeTable.setGroupId((long)groupId);
                        fakeTable.setConfigId(newConfigKeyId);
                        fakeTable.setCreatedTime(new Date());
                        fakeTable.setDescMsg(descMsg);
                        fakeTable.setGroupId(Long.valueOf(subId));
                        fakeTable.setParentGroup(groupUser.getGroupId().longValue());
                        fakeTable.setModeMsg(modeMsg);
                        fakeTable.setPlayCount(0L);
                        fakeTable.setTableMode(tableMode);
                        fakeTable.setTableName(groupName0);
                        fakeTable.setTableOrder(tableOrder);
                        fakeTable.setGameType(gameType);
                        fakeTable.setGameCount(gameCount);
                        fakeTable.setPayType(payType);
                        fakeTable.setPlayerCount(playerCount);
                        fakeTable.setConfigState("1");
                        fakeTable.setCreditMsg(creditMsg);
                        fakeTable.setGoldMsg(goldMsg);
                        fakeTable.setOverCount(overCount);
                        fakeTable.setPlayedBureau(0);

                        fakeTable.setRoundRefrshTime(System.currentTimeMillis() / 1000 + MathUtil.mt_rand(GroupConstants.FAKE_TABLE_REFRESH_TIME_MINI, GroupConstants.FAKE_TABLE_REFRESH_TIME_MAX));

                        GroupDaoManager.getInstance().insertGroupFakeTable(fakeTable);
                        //关联假头像
                        FakeTableHepler.setFakeTableHeadImg(fakeTable);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("createGroupRoom|error|" + e.getMessage(), e);
        }
    }

    /**
     * 假桌设置列表
     */
    public void fakeTableList() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int opType = NumberUtils.toInt(params.get("opType"), 1);// 1获取列表 2删除假桌子  3修改隐藏状态

            if (groupId <= 0 || userId < 0) {
                OutputUtil.output(1, "参数错误", getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                OutputUtil.output(4, "权限不够：仅限群主和管理员", getRequest(), getResponse(), false);
            }

            if(opType == 1){
                int gameType = NumberUtils.toInt(params.get("gameType"), 0);
                String tableName = params.get("tableName");
                int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
                int pageSize = NumberUtils.toInt(params.get("pageSize"), 10);
                if (pageNo < 1 || pageSize < 1) {
                    OutputUtil.output(1, "参数错误", getRequest(), getResponse(), false);
                    return;
                }

                List<Map<String,Object>> fakeTableList = GroupDaoManager.getInstance().loadFakeTableByGroupId(groupId, gameType, tableName, pageNo, pageSize);
                JSONArray ja = new JSONArray();
                for(Map<String,Object> map : fakeTableList) {
                    JSONObject jo = new JSONObject();
                    jo.put("keyId", map.get("keyId"));
                    jo.put("gameType", map.get("gameType"));
                    jo.put("tableName", map.get("tableName"));
                    jo.put("overCount", map.get("overCount"));
                    jo.put("hiding", map.get("hiding"));
                    ja.add(jo);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", 0);
                jsonObject.put("list", ja);
                jsonObject.put("pageNo", pageNo);
                jsonObject.put("pageSize", pageSize);
                OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                return;
            } else if (opType == 2){        //删除
                long keyId = NumberUtils.toLong(params.get("keyId"), 0);
                if(keyId <= 0){
                    OutputUtil.output(1, "参数错误", getRequest(), getResponse(), false);
                    return;
                }
                GroupDaoManager.getInstance().delFakeTable(keyId);
                FakeTableHepler.setFreeFakeTableHeadImg(keyId);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", 0);
                jsonObject.put("message", "操作成功");
                OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                return;
            } else if (opType == 3) {        //更改隐藏
                long keyId = NumberUtils.toLong(params.get("keyId"), 0);
                if (keyId <= 0) {
                    OutputUtil.output(1, "参数错误", getRequest(), getResponse(), false);
                    return;
                }
                GroupDaoManager.getInstance().changeFakeTableHiding(keyId);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", 0);
                jsonObject.put("message", "操作成功");
                OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                return;
            } else {
                OutputUtil.output(3, "不支持的操作!", getRequest(), getResponse(), false);
                return;
            }

        } catch (Exception e) {
            OutputUtil.output(4, "系统异常,请稍后再试", getRequest(), getResponse(), false);
            LOGGER.error("fakeTableList|error|" + e.getMessage(), e);
        }
    }



    /**
     * 白金岛
     * 转让俱乐部
     */
    public void transferGroupForBjd() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("transferGroup|params|" + params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            long toUserId = NumberUtils.toLong(params.get("toUserId"), 0);

            if (userId <= 0 || groupId <= 0 || toUserId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                OutputUtil.output(2, "玩家ID错误", getRequest(), getResponse(), false);
                return;
            }
            RegInfo toUser = userDao.getUser(toUserId);
            if (toUser == null) {
                OutputUtil.output(2, "玩家ID错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser;
            GroupUser toGroupUser;
            GroupInfo groupInfo;
            synchronized (GroupAction.class) {
                groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                }
                groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null || !GroupConstants.isMaster(groupUser.getUserRole())) {
                    OutputUtil.output(2, "非群主，不能转移", getRequest(), getResponse(), false);
                    return;
                }

                toGroupUser = groupDao.loadGroupUser(toUserId, groupId);
                if (toGroupUser != null && (!GroupConstants.isMember(toGroupUser.getUserRole()) || toGroupUser.getPromoterLevel() != 0)) {
                    OutputUtil.output(2, "非本群普通成员，不能转移", getRequest(), getResponse(), false);
                    return;
                }

                String transferRes = BjdUtil.transferGroup(userId, toUserId, groupId);
                if (!"".equals(transferRes)) {
                    OutputUtil.output(2, transferRes, getRequest(), getResponse(), false);
                    return;
                }

                MessageBuilder resMsg = MessageBuilder.newInstance();
                resMsg.builderCodeMessage(0, "转移成功");
                resMsg.builder("userId", userId);
                resMsg.builder("toUserId", toUserId);
                resMsg.builder("groupId", groupId);
                OutputUtil.output(resMsg, getRequest(), getResponse(), null, false);
                LOGGER.info("transferGroup|succ|" + JSON.toJSONString(groupInfo));
            }
        } catch (Exception e) {
            OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("transferGroup|error|" + e.getMessage(), e);
        }
    }

    /**
     * 补房
     *
     * @param userId
     * @param groupId
     * @param configId 大于0：表示对单个玩法，小于0：表示对所有玩法
     * @throws Exception
     */
    private void autoCreateGroupTableNew(long userId, int groupId, long configId) throws Exception {
        RegInfo user = userDao.getUser(userId);
        if (user == null) {
            return;
        }
        List<GroupTableConfig> configList = new ArrayList<>();
        if (configId > 0) {
            GroupTableConfig config = groupDao.loadGroupTableConfig(configId);
            if (config == null) {
                return;
            }
            configList.add(config);
        } else {
            List<GroupInfo> groupInfos = groupDao.loadSubGroups(String.valueOf(groupId));
            if (groupInfos != null && groupInfos.size() > 0) {
                for (GroupInfo groupInfo : groupInfos) {
                    if (!"1".equals(groupInfo.getGroupState())) {
                        continue;
                    }
                    GroupTableConfig config = groupDao.loadLastGroupTableConfig(groupInfo.getGroupId(), groupInfo.getParentGroup());
                    if (config != null && "1".equals(config.getConfigState())) {
                        configList.add(config);
                    }
                }
            }
        }
        int delayTime = 1;
        for (GroupTableConfig config : configList) {
            TaskExecutor.getInstance().submitSchTask(new Runnable() {
                @Override
                public void run() {
                    autoCreateGroupTableByServer(groupId, user, config);
                }
            }, delayTime);
            delayTime +=20;
        }
    }

    /**
     *
     * 由于游戏服那边自动补房是异步的，这个接口调过去自动补房有可能不同的服同时创房，群主支付会产生并发问题，故这里依次延时补房，错开时间
     * @param groupId
     * @param user
     * @param config
     */
    private void autoCreateGroupTableByServer(int groupId, RegInfo user, GroupTableConfig config) {

       // System.out.println("执行 结果。。。。。。==========================time1 ===" + System.currentTimeMillis());

        Server server = SysInfManager.loadServer(config.getGameType(), 1);
        if (server == null) {
            return;
        }
        String url = StringUtils.isNotBlank(server.getIntranet()) ? server.getIntranet() : server.getHost();
        if (StringUtils.isNotBlank(url)) {
            int idx = url.indexOf(".");
            if (idx > 0) {
                idx = url.indexOf("/", idx);
                if (idx > 0) {
                    url = url.substring(0, idx);
                }
                url += "/online/notice.do?type=autoCreateGroupTableNew&userId=" + user.getUserId() + "&message=" + groupId + "&configId=" + config.getKeyId();
                String noticeRet = HttpUtil.getUrlReturnValue(url, 5);
                LOGGER.info("notice result:url={},ret={}", url, noticeRet);
            }
        }
    }

    /**
     * 补房
     * 触发一次亲友圈全部已开启的玩法房间检测，当玩法包厢没有空的牌桌时则系统自动补一间房
     */
    public void replenishGroupTable() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("replenishGroupTable|params|" + params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), 0);

            if (userId <= 0 || groupId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser;
            GroupInfo groupInfo;
            synchronized (GroupAction.class) {
                groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                }
                groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null || !GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole())) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                    return;
                }
                autoCreateGroupTableNew(userId, groupId, 0);
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
                LOGGER.info("replenishGroupTable|succ|" + JSON.toJSONString(groupInfo));
            }
        } catch (Exception e) {
            OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("replenishGroupTable|error|" + e.getMessage(), e);
        }
    }

    private void insertGroupUserAlert(long groupId, long userId, long optUserId, int type) {
        LogGroupUserAlert log = new LogGroupUserAlert();
        log.setGroupId(groupId);
        log.setUserId(userId);
        log.setOptUserId(optUserId);
        log.setType(type);
        try {
            groupCreditDao.insertGroupUserAlert(log);
        } catch (Exception e) {
            LOGGER.error("insertGroupUserAlert|error|" + e.getMessage(), e);
        }
    }

}
