package com.sy.sanguo.game.pdkuai.game;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.action.BaseAction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerPromotionAction extends BaseAction{

    private static final int DOWNLOAD_VAL=2;//一个下载奖励2元
    private static final int PLAYGAME_VAL=2;//一次有效牌局奖励2元

    @Override
    public String execute() throws Exception {
//        int funcType = getInt("funcType");
        String userId=getString("userId");

        RegInfo userInfo=UserDao.getInstance().getUser(Long.parseLong(userId));

//        ActivityBean activity = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);

        if (userInfo!=null){
//            switch (funcType){
//                case 1:{
                    response.sendRedirect(PropertiesCacheUtil.getValue("gm_url",Constants.GAME_FILE)+"d3/"+userId);
//                    return null;
//                }
//                case 2:{
//                        HashMap<String,String> params=new HashMap<>();
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
//                        String ymd=sdf0.format(new Date());
//                        Date date1=sdf.parse(ymd+" 00:00:00");
//                        if (activity.getStartDateTime().after(date1)){
//                            date1=activity.getStartDateTime();
//                        }
//                        Date date2=sdf.parse(ymd+" 23:59:59");
//                        if (activity.getEndDateTime().before(date2)){
//                            date2=activity.getEndDateTime();
//                        }
//                        params.put("date1",sdf.format(date1));
//                        params.put("date2",sdf.format(date2));
//                        params.put("date3",sdf.format(activity.getStartDateTime()));
//                        params.put("date4",sdf.format(activity.getEndDateTime()));
//                        params.put("date5",ymd+" 00:00:00");
//                        params.put("date6",ymd+" 23:59:59");
//                        params.put("inviterId",userId);
//
//                        List<Map<String,Object>> list= UserDao.getInstance().loadMyInviteeData(params);
//
//                        JSONObject json=new JSONObject(true);
//                        json.put("code",1000);
//
//                        int todayDownload=object2Int(list.get(0).get("mynum"));
//                        int todayPlay=object2Int(list.get(1).get("mynum"));
//                        int totalDownload=object2Int(list.get(2).get("mynum"));
//                        int totalPlay=object2Int(list.get(3).get("mynum"));
//                        int payCount=object2Int(list.get(4).get("mynum"));
//                        int cashCount=object2Int(list.get(5).get("mynum"));
//                        int todayPay=todayDownload*DOWNLOAD_VAL+todayPlay*PLAYGAME_VAL;
//                        int totalPay=totalDownload*DOWNLOAD_VAL+totalPlay*PLAYGAME_VAL;
//
//                        json.put("todayDownload",todayDownload);
//                        json.put("todayPlay",todayPlay);
//                        json.put("totalDownload",totalDownload);
//                        json.put("totalPlay",totalPlay);
//                        json.put("payCount",payCount);
//                        json.put("payRest",totalPay-payCount);
//                        json.put("todayPay",todayPay);
//                        json.put("totalPay",totalPay);
//                        json.put("cashCount",cashCount);
//
//                        OutputUtil.output(json,request,response,null,false);
//
//                    break;
//                }
//                case 3:{
//                        String today=request.getParameter("today");
//                        HashMap<String, String> params = new HashMap<>();
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
//                        Date date1;
//                        Date date2;
//
//                        if ("1".equals(today)){
//                            String ymd = sdf0.format(new Date());
//                            date1 = sdf.parse(ymd + " 00:00:00");
//                            if (activity.getStartDateTime().after(date1)) {
//                                date1 = activity.getStartDateTime();
//                            }
//                            date2 = sdf.parse(ymd + " 23:59:59");
//                            if (activity.getEndDateTime().before(date2)) {
//                                date2 = activity.getEndDateTime();
//                            }
//                        }else{
//                            date1=activity.getStartDateTime();
//                            date2=activity.getEndDateTime();
//                        }
//
//                        params.put("date1", sdf.format(date1));
//                        params.put("date2", sdf.format(date2));
//                        params.put("inviterId",userId);
//
//                        List<Map<String,Object>> list= UserDao.getInstance().loadMyTotayUsers(params);
//                        JSONObject json=new JSONObject(true);
//                        json.put("code",1000);
//                        json.put("datas",list==null?new ArrayList<>():list);
//                        OutputUtil.output(json,request,response,null,false);
//
//                    break;
//                }
//                case 4:{
//                        int count = getInt("count");
//                        if (count >= 1 && count <= 100) {
//                            HashMap<String,String> params=new HashMap<>();
//                            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
//                            String ymd=sdf0.format(new Date());
//
//                            params.put("date1",ymd+" 00:00:00");
//                            params.put("date2",ymd+" 23:59:59");
//                            params.put("userId",userId);
//
//                            int cashCount= UserDao.getInstance().loadMyTotayPayCount(params);
//                            JSONObject json=new JSONObject(true);
//                            if (cashCount>=1){
//                                json.put("cashCount",cashCount);
//                                json.put("code",1002);
//                                json.put("message", "每天可提取一次红包，单次提取红包金额为1~100元");
//                            }else{
//                                params=new HashMap<>();
//                                params.put("userId",userId);
//                                params.put("state","0");
//
////                                String openid=null;
////                                List<Map<String,Object>> wxMsg= UserDao.getInstance().load("user.loadHbExchangeRecord",params);
////                                if (wxMsg!=null&&wxMsg.size()>0&&StringUtils.isNotBlank(openid=object2Str(wxMsg.get(0).get("phone")))){
////                                }else{
////                                    re
////                                    return null;
////                                }
//
//                                synchronized (PlayerPromotionAction.class){
//                                    params=new HashMap<>();
//                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//                                    params.put("date1",sdf.format(activity.getStartDateTime()));
//                                    params.put("date2",sdf.format(activity.getEndDateTime()));
//                                    params.put("date3",ymd+" 00:00:00");
//                                    params.put("date4",ymd+" 23:59:59");
//                                    params.put("inviterId",userId);
//
//                                    List<Map<String,Object>> list= UserDao.getInstance().loadMyTotalData(params);
//
//
//                                    int totalDownload=object2Int(list.get(0).get("mynum"));
//                                    int totalPlay=object2Int(list.get(1).get("mynum"));
//                                    int payCount=object2Int(list.get(2).get("mynum"));
//                                    cashCount=object2Int(list.get(3).get("mynum"));
//                                    int totalPay=totalDownload*DOWNLOAD_VAL+totalPlay*PLAYGAME_VAL;
//                                    int payRest=totalPay-payCount;
//
//                                    if (cashCount>=1){
//                                        json.put("cashCount",cashCount);
//                                        json.put("code",1002);
//                                        json.put("message", "每天可提取一次红包，单次提取红包金额为1~100元");
//                                    }else if (payRest<1){
//                                        json.put("code",1003);
//                                        json.put("message", "红包金额不足");
//                                    }else if (payRest<count){
//                                        json.put("code",1004);
//                                        json.put("rest",payRest);
//                                        json.put("message", "可提现红包金额不足");
//                                    }else{
////<mch_appid>wxe062425f740c30d8</mch_appid>
////<mchid>10000098</mchid>
////<nonce_str>3PG2J4ILTKCH16CQ2502SI8ZNMTM67VS</nonce_str>
////<partner_trade_no>100000982014120919616</partner_trade_no>
////<openid>ohO4Gt7wVPxIT1A9GjFaMYMiZY1s</openid>
////<check_name>FORCE_CHECK</check_name>
////<re_user_name>张三</re_user_name>
////<amount>100</amount>//分
////<desc>节日快乐!</desc>
////<spbill_create_ip>10.2.3.10</spbill_create_ip>
////<sign>C97BDBACF37622775366F38B629F45E3</sign>
////</xml>
//                                            String temp=getSerializableDigit();
//                                            Map<String,String> map=new LinkedHashMap<>();
//                                            map.put("mch_appid",PropertiesCacheUtil.getValue("appid",Constants.GAME_FILE));
//                                            map.put("mchid",PropertiesCacheUtil.getValue("mchid",Constants.GAME_FILE));
//                                            map.put("nonce_str", MD5Util.getStringMD5(UUID.randomUUID().toString(),"UTF-8").toUpperCase());
//                                            map.put("partner_trade_no",temp);
//                                            map.put("openid",userInfo.getFlatId());
//                                            map.put("check_name","NO_CHECK");
////                                            map.put("re_user_name","");
//                                            map.put("amount",String.valueOf(count*100));
//                                            map.put("desc","《小甘十点半》邀请好友红包奖励");
//                                            map.put("spbill_create_ip",PropertiesCacheUtil.getValue("localip",Constants.GAME_FILE));
//
//                                            String[] keys=map.keySet().toArray(new String[0]);
//                                            Arrays.sort(keys);
//
//                                            StringBuilder strBuilder=new StringBuilder();
//                                            for (String key:keys){
//                                                String value=map.get(key);
//                                                if (org.apache.commons.lang3.StringUtils.isNotBlank(value)){
//                                                    strBuilder.append(key).append("=").append(value).append("&");
//                                                }
//                                            }
//
//                                            strBuilder.append("key=").append(PropertiesCacheUtil.getValue("paykey",Constants.GAME_FILE));
//
//                                            map.put("sign",MD5Util.getStringMD5(strBuilder.toString(),"UTF-8").toUpperCase());
//
//                                            StringBuilder paramBuilder=new StringBuilder();
//                                            paramBuilder.append("<xml>");
//                                            for (Map.Entry<String,String> kv:map.entrySet()){
//                                                paramBuilder.append("<").append(kv.getKey()).append(">");
//                                                paramBuilder.append(kv.getValue());
//                                                paramBuilder.append("</").append(kv.getKey()).append(">");
//                                            }
//                                            paramBuilder.append("</xml>");
//
//                                            String postContent=paramBuilder.toString();
//                                            String result= PayUtil.post(PayUtil.PAY_URL,postContent);
//
//                                            LogUtil.i("transfer: userId="+userInfo.getUserId()+",content="+postContent+",result="+result);
//
//                                            Map<String,String> retMap=PayUtil.toMap(result.getBytes("UTF-8"),"UTF-8");
//
//                                             LogUtil.i("wx transfer: userId="+userInfo.getUserId()+",content="+postContent+",result="+retMap);
//
//                                        HashMap<String,String> entityMap=new HashMap<>();
//                                        //hb_exchange_record (userId, money, wxname, phone, createTime, state)
//                                        entityMap.put("userId",userId);
//                                        entityMap.put("money",String.valueOf(count));
//                                        entityMap.put("wxname",userInfo.getName());
//                                        entityMap.put("phone",temp);
//                                        entityMap.put("createTime",sdf.format(new Date()));
//                                        if ("SUCCESS".equals(retMap.get("return_code"))&&"SUCCESS".equals(retMap.get("result_code"))){
//                                            entityMap.put("state","2");
//                                        }else{
//                                            entityMap.put("state","1");
//                                        }
//                                        Object saveRet=UserDao.getInstance().addHbExchangeRecord(entityMap);
//                                        LogUtil.i("save transfer: userId="+userInfo.getUserId()+",content="+entityMap+",result="+saveRet);
//
//                                        json.put("code",1000);
//                                        json.put("message", "提取成功");
//                                    }
//                                }
//                            }
//                            OutputUtil.output(json, request, response, null, false);
//                        } else {
//                            JSONObject json = new JSONObject(true);
//                            json.put("code", 1001);
//                            json.put("message", "每天可提取一次红包，单次提取红包金额为1~100元");
//
//                            OutputUtil.output(json, request, response, null, false);
//                        }
//                    break;
//                }
//            }
        }

        return null;
    }

    private static String object2Str(Object object){
        if (object==null){
            return null;
        }else{
            return object.toString();
        }
    }

    private static int object2Int(Object object){
        if (object==null){
            return 0;
        }else if (object instanceof Number){
            return ((Number)object).intValue();
        }else{
            return Integer.parseInt(object.toString());
        }
    }

    private static long sequence;
    private static String compareTime;
    private static NumberFormat numberFormat = NumberFormat.getInstance();
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");

    private static final synchronized String getSerializableDigit() {
        String currentTime = simpleDateFormat.format(new Date());
        if (compareTime != null && compareTime.compareTo(currentTime) == 0) {
            ++sequence;
        } else {
            compareTime = currentTime;
            sequence = 1L;
        }

        return currentTime + numberFormat.format(sequence);
    }

}
