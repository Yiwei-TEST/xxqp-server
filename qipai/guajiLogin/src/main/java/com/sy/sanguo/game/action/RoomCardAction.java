package com.sy.sanguo.game.action;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.MessageBuilder;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.RoomCardDaoImpl;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2017/4/12.
 */
public class RoomCardAction extends GameStrutsAction{
    private OrderDaoImpl orderDao;
    private RoomCardDaoImpl roomCardDao;

    public void setOrderDao(OrderDaoImpl orderDao) {
        this.orderDao = orderDao;
    }

    public void setRoomCardDao(RoomCardDaoImpl roomCardDao) {
        this.roomCardDao = roomCardDao;
    }

    /**
     * 查询今日新增玩家
     * @throws Exception
     */
    public void todayMyPlayers() throws Exception{
        Map<String,String> paramsMap= UrlParamUtil.getParameters(getRequest());

        String rechargeBindAgencyId=paramsMap.get("agencyId");
        if (StringUtils.isBlank(rechargeBindAgencyId)){
            OutputUtil.output(1003,"agencyId is blank",getRequest(),getResponse(),false);
            return;
        }

        Map<String,Object> params=new HashMap<>();
        params.put("rechargeBindAgencyId",rechargeBindAgencyId);

        String date=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        params.put("startTime",date+" 00:00:00");
        params.put("endTime",date+" 23:59:59");

        List<Map<String,String>> result=orderDao.selectMyPlayersByDatetime(params);

        Map<String,Object> temp=new HashMap<>();
        temp.put("count",result==null?0:result.size());
        if (result!=null){
            temp.put("datas",result);
        }

        OutputUtil.output(MessageBuilder.newInstance(temp).builder("code",1000),getRequest(),getResponse(),null,false);
    }

    /**
     * 查询详情
     * @throws Exception
     */
    public void selectMyPlayers() throws Exception{
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String,String> paramsMap= UrlParamUtil.getParameters(getRequest());

        String rechargeBindAgencyId=paramsMap.get("agencyId");
        String startTime=paramsMap.get("startTime");
        String endTime=paramsMap.get("endTime");
        if (StringUtils.isBlank(rechargeBindAgencyId)){
            OutputUtil.output(1003,"agencyId is blank",getRequest(),getResponse(),false);
            return;
        }
        if (StringUtils.isBlank(startTime)){
            OutputUtil.output(1003,"startTime is blank",getRequest(),getResponse(),false);
            return;
        }
        if (StringUtils.isBlank(endTime)){
            OutputUtil.output(1003,"endTime is blank",getRequest(),getResponse(),false);
            return;
        }

        Map<String,Object> params=new HashMap<>();
        params.put("rechargeBindAgencyId",rechargeBindAgencyId);
        params.put("startTime",startTime);
        params.put("endTime",endTime);

        List<Map<String,String>> result=orderDao.selectMyPlayersDetailByDatetime(params);

        Map<String,Object> temp=new HashMap<>();
        temp.put("count",result==null?0:result.size());

        int totalAmount=0;
        if (result!=null){
            temp.put("datas",result);
            for (Map<String,String> tempMap:result){
                totalAmount+=Integer.parseInt(tempMap.get("orderAmount"));
            }
        }
        temp.put("totalAmount",totalAmount);

        OutputUtil.output(MessageBuilder.newInstance(temp).builder("code",1000),getRequest(),getResponse(),null,false);
    }

    public void selectMyAgencies() throws Exception {
        Map<String, String> paramsMap = UrlParamUtil.getParameters(getRequest());

        String userId = paramsMap.get("userId");
        String agencyId = paramsMap.get("agencyId");

        if (StringUtils.isNotBlank(userId)){
            List<Map<String, String>> list=roomCardDao.queryMyAgencyByMyUserId(Integer.parseInt(userId));
            OutputUtil.output(MessageBuilder.newInstance().builder("code",1000).builder("datas",list),getRequest(),getResponse(),null,false);
        }else if (StringUtils.isNotBlank(agencyId)){
            List<Map<String, String>> list=roomCardDao.queryMyAgencyByMyAgencyId(Integer.parseInt(agencyId));
            OutputUtil.output(MessageBuilder.newInstance().builder("code",1000).builder("datas",list),getRequest(),getResponse(),null,false);
        }else{
            OutputUtil.output(1003,"userId,agencyId both blank",getRequest(),getResponse(),false);
        }
    }

    public void selectAmount() throws Exception {
        Map<String, String> paramsMap = UrlParamUtil.getParameters(getRequest());
        String rechargeBindAgencyId=paramsMap.get("agencyId");
        String startTime=paramsMap.get("startTime");
        String endTime=paramsMap.get("endTime");

        if (StringUtils.isBlank(rechargeBindAgencyId)){
            OutputUtil.output(1003,"agencyId is blank",getRequest(),getResponse(),false);
            return;
        }
        if (StringUtils.isBlank(startTime)){
            OutputUtil.output(1003,"startTime is blank",getRequest(),getResponse(),false);
            return;
        }
        if (StringUtils.isBlank(endTime)){
            OutputUtil.output(1003,"endTime is blank",getRequest(),getResponse(),false);
            return;
        }

        Map<String,Object> params=new HashMap<>();
        params.put("rechargeBindAgencyId",rechargeBindAgencyId);
        params.put("startTime",startTime);
        params.put("endTime",endTime);

        int totalAmount1=0,totalAmount2=0;

        //直接玩家
        List<Map<String,String>> list=orderDao.selectMyPlayersDetailByDatetime(params);
        if (list!=null){
            for (Map<String,String> tempMap:list){
                totalAmount1+=Integer.parseInt(tempMap.get("orderAmount"));
            }
        }

        //一级代理
        List<Map<String, String>> list1=roomCardDao.queryMyAgencyByMyAgencyId(Integer.parseInt(rechargeBindAgencyId));
        if (list1!=null&&list1.size()>0){
            for (Map<String,String> tmpMap1:list1){
                params.put("rechargeBindAgencyId",tmpMap1.get("agencyId"));
                List<Map<String,String>> tempList1=orderDao.selectMyPlayersDetailByDatetime(params);
                if (tempList1!=null){
                    for (Map<String,String> tempMap:tempList1){
                        totalAmount2+=Integer.parseInt(tempMap.get("orderAmount"));
                    }
                }

                //二级代理
                List<Map<String, String>> list2=roomCardDao.queryMyAgencyByMyAgencyId(Integer.parseInt(String.valueOf(tmpMap1.get("agencyId"))));
                if (list2!=null&&list2.size()>0) {
                    for (Map<String, String> tmpMap2 : list2) {
                        params.put("rechargeBindAgencyId", tmpMap2.get("agencyId"));
                        List<Map<String, String>> tempList2 = orderDao.selectMyPlayersDetailByDatetime(params);
                        if (tempList2 != null) {
                            for (Map<String, String> tempMap : tempList2) {
                                totalAmount2 += Integer.parseInt(tempMap.get("orderAmount"));
                            }
                        }
                    }
                }
            }

        }

        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000,"success").builder("totalAmount1",totalAmount1)
                .builder("totalAmount2",totalAmount2),getRequest(),getResponse(),null,false);
    }
}
