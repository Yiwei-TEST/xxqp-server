package com.sy.sanguo.game.action;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.Activity;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.ActivityDao;
import com.sy.sanguo.game.dao.DataStatisticsDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataStatisticsAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStatisticsAction.class);

    private UserDaoImpl userDao;
    private GroupDao groupDao;
    private DataStatisticsDao dataStatisticsDao;

    /**
     * 加载俱乐部数据统计排名
     */
    public void loadGroupDataRank() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String dataDate=params.get("dataDate");
            String startDate=params.get("startDate");
            String endDate=params.get("endDate");
            String dataCode=params.get("dataCode");
            String dataTypes=params.get("dataTypes");
            String userIdKey=params.get("userIdKey");   //用户id查询
            String dataTypeSum=params.get("dataTypeSum"); //dataType求和
            
            //白金岛传入排序规则
            String orderRule = params.get("orderRule");
            
            if(orderRule == null || orderRule == "") {
            	orderRule = "DESC";
            }else {
            	if(!"asc".equalsIgnoreCase(orderRule) && !"desc".equalsIgnoreCase(orderRule)){
            		orderRule = "DESC";
            	}
            }
            
            

            long userId = NumberUtils.toLong(params.get("userId")); // 自己的id
            long groupId = NumberUtils.toLong(params.get("groupId"));// 俱乐部
            if ("jlbCount".equals(dataTypes)) {
                Date startDate1 ;
                Date endDate1;
                Activity activityBean = ActivityDao.getInstance().getActivityById(1);
                if (activityBean != null) {
                    startDate1= activityBean.getBeginTime();
                    endDate1 = activityBean.getEndTime();
                    startDate = TimeUtil.getSimpleDay(startDate1);
                    endDate = TimeUtil.getSimpleDay(endDate1);
                } else {
                    OutputUtil.output(1, "活动未开启", getRequest(), getResponse(), false);
                    return;
                }

                if (!TimeUtil.isInTime(new Date(), startDate1, endDate1)) {
                    OutputUtil.output(1, "活动未开启", getRequest(), getResponse(), false);
                    return;
                }
            }

            // 连着请求总积分和总局数
            String returnOne = params.get("returnOne");
            int pageNo = NumberUtils.toInt(params.get("pageNo"),1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"),30);
            if (StringUtils.isBlank(dataCode)||StringUtils.isBlank(dataTypes)||pageNo<1||pageSize<1){
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            boolean isRange = false;
            if (NumberUtils.isDigits(startDate)&&NumberUtils.isDigits(endDate)&&startDate.length()==8&&endDate.length()==8){
                if (!startDate.equals(endDate)){
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
                    long date1=sdf.parse(startDate).getTime();
                    long date2=sdf.parse(endDate).getTime();

                    if (date2<=date1||(date2-date1>32*24*60*60*1000L)){
                        OutputUtil.output(3, "日期错误,最多支持一个月", getRequest(), getResponse(), false);
                        return;
                    }

                    isRange = true;
                }else{
                    dataDate = startDate;
                }
            }else if (NumberUtils.isDigits(dataDate)){

            }else{
                OutputUtil.output(2, "日期错误", getRequest(), getResponse(), false);
                return;
            }

            if (pageSize>50){
                pageSize=50;
            }

            MessageBuilder messageBuilder=MessageBuilder.newInstance().builder("code",0).builder("pageNo",pageNo).builder("pageSize",pageSize);
            String[] dts=dataTypes.split(",");
            Map<String,Map<String,Object>> userBaseMap=new HashMap<>();
            if ("1".equals(returnOne) && dts.length>1) {
                List<Map<String,Object>> list;
                String dataType = dts[0];
                Integer start=Integer.valueOf(startDate);
                Integer end=Integer.valueOf(endDate);
                if (isRange){
                    switch (dataType){
                        case "djsCount":
                        case "zjfCount":
                        case "dyjCount":
                        case "creditCommisionCount":
                        case "dyjCountCredit":
                            list = dataStatisticsDao.loadRangeDataStatisticsList1(start,end,dataCode,dataType,userIdKey,pageNo,pageSize,orderRule);
                            break;
                        default:
                            list = null;
                    }
                } else {
                	
                	if("desc".equalsIgnoreCase(orderRule)) {
                		list = dataStatisticsDao.loadCurrentDataStatisticsList1(dataDate,dataCode,dataType,userIdKey,pageNo,pageSize);
                	}else {
                		  list = dataStatisticsDao.loadCurrentDataStatisticsList2(dataDate,dataCode,dataType,pageNo,pageSize);
                	}
                }
                if (list==null||list.size()==0){
                    messageBuilder.builder(dataTypes,Collections.emptyList());
                }else{
                    StringBuilder userIdStr = new StringBuilder();
                    for (Map<String,Object> map:list){
                        String uid=String.valueOf(map.get("userId"));
                        userIdStr.append(uid).append(",");
                        Map<String,Object> map1 = userBaseMap.get(uid);
                        if (map1==null){
                            map1=userDao.loadUserBase(uid);
                            if (map1==null){
                                map1=new HashMap<>();
                            }
                            userBaseMap.put(uid,map1);
                        }
                        if (map1!=null&&map1.size()>0){
                            map.putAll(map1);
                        }
                        map.put("dataType", dataTypes);
                        map.put(dataType, map.get("dataValue"));
                    }
                    userIdStr.deleteCharAt(userIdStr.length()-1);
                    List<Map<String,Object>> list2;
                    String dataType2 = dts[1];
                    if (isRange) {
                        list2 = dataStatisticsDao.loadRangeDataStatisticsList4(start,end,dataCode,dataType2,pageNo,pageSize, userIdStr.toString());
                    } else {
                        list2 = dataStatisticsDao.loadCurrentDataStatisticsList4(dataDate,dataCode,dataType2,pageNo,pageSize, userIdStr.toString());
                    }

                    Map<String, Object> dataType2Map = new HashMap<>();
                    for (Map<String, Object> tmp : list2) {
                        dataType2Map.put(String.valueOf(tmp.get("userId")), tmp);
                    }
                    
                    
                    
                    List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();
                    String dataType3 = "";
                    if(dts.length >2) {
                    	  dataType3 = dts[2];
                         if (isRange) {
                             list3 = dataStatisticsDao.loadRangeDataStatisticsList4(start,end,dataCode,dataType3,pageNo,pageSize, userIdStr.toString());
                         } else {
                             list3 = dataStatisticsDao.loadCurrentDataStatisticsList4(dataDate,dataCode,dataType3,pageNo,pageSize, userIdStr.toString());
                         }
                    }

                    Map<String, Object> dataType3Map = new HashMap<>();
                    for (Map<String, Object> tmp : list3) {
                        dataType3Map.put(String.valueOf(tmp.get("userId")), tmp);
                    }
                    
                    

                    for (Map<String, Object> map : list) {
                        Map<String, Object> data = (Map<String, Object>) dataType2Map.get(String.valueOf(map.get("userId")));
                        
                        Map<String, Object> data2 = (Map<String, Object>) dataType3Map.get(String.valueOf(map.get("userId")));
                        if (data2 != null) {
                            map.put(dataType3, data2.get(dataType3));
                        } else {
                            map.put(dataType3, 0);
                        }
                        
                        if (data != null) {
                            map.put(dataType2, data.get(dataType2));
                        } else {
                            map.put(dataType2, 0);
                        }
                    }
                    messageBuilder.builder(dataTypes, list);
                }
            } else {
                for (String dataType:dts){
                    if (dataType.length()>0){
                        List<Map<String,Object>> list;
                        if (isRange){
                            Integer start=Integer.valueOf(startDate);
                            Integer end=Integer.valueOf(endDate);
                            switch (dataType){
                                case "dyjCount":
                                case "dfhCount":
                                case "xjsCount":
                                case "djsCount":
                                case "zjfCount":
                                case "creditCommisionCount":
                                case "dyjCountCredit":
                                    list = dataStatisticsDao.loadRangeDataStatisticsList1(start,end,dataCode,dataType,userIdKey,pageNo,pageSize,orderRule);
                                    break;
                                case "winMaxScore":
                                    list = dataStatisticsDao.loadRangeDataStatisticsList2(start,end,dataCode,dataType,pageNo,pageSize);
                                    break;
                                case "loseMaxScore":
                                    list = dataStatisticsDao.loadRangeDataStatisticsList3(start,end,dataCode,dataType,pageNo,pageSize);
                                    break;
                                case "jlbCount":
                                    list = dataStatisticsDao.loadCurrentDataStatisticsList5(15, dataType, pageNo, pageSize);
                                    break;
                                default:
                                    list = null;
                            }
                        }else{
                            if ("loseMaxScore".equalsIgnoreCase(dataType)){
                                list = dataStatisticsDao.loadCurrentDataStatisticsList2(dataDate,dataCode,dataType,pageNo,pageSize);
                            }else{
                                list = dataStatisticsDao.loadCurrentDataStatisticsList1(dataDate,dataCode,dataType,userIdKey,pageNo,pageSize);
                            }
                        }

                        if (list==null||list.size()==0){
                            messageBuilder.builder(dataType,Collections.emptyList());
                            if(!StringUtils.isBlank(dataTypeSum)){
                                messageBuilder.builder("dataTypeSumValue",0);
                            }
                        }else{
                            int rank = 1;
                            boolean inRank = false;
                            List<String> groupIds = new ArrayList<>();
                            GroupUser guSelf = groupDao.loadGroupUser(userId,groupId);
                            for (Map<String,Object> map:list){
                                String uid = String.valueOf(map.get("userId"));
                                if ("jlbCount".equals(dataType)) {
                                    if (inRank || dataCode.equals(map.get("dataCode"))) {
                                        inRank = true;
                                    } else {
                                        rank++;
                                    }
                                    String dataCode2=String.valueOf(map.get("dataCode"));
                                    groupIds.add(dataCode2.substring(5));
                                } else {
                                    Map<String,Object> map1 = userBaseMap.get(uid);
                                    if (map1==null){
                                        map1=userDao.loadUserBase(uid);
                                        if (map1==null){
                                            map1=new HashMap<>();
                                        }
                                        userBaseMap.put(uid,map1);
                                    }
                                    if ( map1!=null&&map1.size()>0){
                                        map.putAll(map1);
                                    }
                                }
                                // 是否隐藏id
                                int hideId = 0;
                                GroupUser guOther = groupDao.loadGroupUser(Long.valueOf(uid), groupId);
                                if (!GroupConstants.isLower(guSelf, guOther)) {
                                    hideId = 1;
                                }
                                map.put("hideId",hideId);
                            }

                            if ("jlbCount".equals(dataType)) {
                                // 白名单
                                String whiteStr =PropertiesCacheUtil.getValue("whiteStr",Constants.GAME_FILE);
                                boolean show = false;
                                if (!StringUtils.isBlank(whiteStr)){
                                    String gid = dataCode.substring(5);
                                    String[] strs = whiteStr.split(",");
                                    for (String str : strs) {
                                        if (str.equals(gid)) {
                                            show = true;
                                        }
                                    }
                                }
                                if (!inRank && !show) {
                                    messageBuilder.builder(dataType, new ArrayList<>());
                                    messageBuilder.builder("myRank", inRank?rank:0);
                                } else {
                                    messageBuilder.builder(dataType,list);
                                    StringBuilder sb = new StringBuilder();
                                    for (String grouId : groupIds) {
                                        sb.append(grouId).append(",");
                                    }
                                    sb.deleteCharAt(sb.length()-1);
                                    List<HashMap<String, Object>> list2 = groupDao.loadGroupMasterIdsByGroupIds(sb.toString());
                                    Map<String, String> map3 = new HashMap<>();
                                    if (list2 != null && !list2.isEmpty()) {
                                        for (HashMap<String, Object> map2 : list2) {
                                            map3.put("group"+map2.get("groupId"), String.valueOf(map2.get("userId")));
                                        }
                                    }
                                    for (Map<String,Object> map:list){
                                        String uid=String.valueOf(map3.get(String.valueOf(map.get("dataCode"))));
                                        Map<String,Object> map2 = userBaseMap.get(uid);
                                        if (map2==null){
                                            map2=userDao.loadUserBase(uid);
                                            if (map2==null){
                                                map2=new HashMap<>();
                                            }
                                            userBaseMap.put(uid,map2);
                                        }
                                        if ( map2!=null&&map2.size()>0){
                                            map.putAll(map2);
                                        }
                                    }
                                    messageBuilder.builder("myRank", inRank?rank:0);
                                }
                            } else {
                                messageBuilder.builder(dataType,list);
                            }
                            if(!StringUtils.isBlank(dataTypeSum)){
                                Integer start=Integer.valueOf(startDate);
                                Integer end=Integer.valueOf(endDate);
                                long dataTypeSumValue = dataStatisticsDao.loadSumDataStatisticsList(start,end,dataCode,dataType,userIdKey);
                                messageBuilder.builder("dataTypeSumValue",dataTypeSumValue);
                            }
                        }

                    }
                }
            }
            OutputUtil.output(messageBuilder, getRequest(), getResponse(),null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
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
}
