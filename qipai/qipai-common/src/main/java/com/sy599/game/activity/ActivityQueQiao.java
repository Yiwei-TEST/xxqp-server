package com.sy599.game.activity;

import com.sy.mainland.util.HttpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.common.bean.MissionState;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.dao.ActivityDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ActivityMsg;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityQueQiao {
    private long userId=0;
    //被邀请信息
    List<InviteQueQiao> inviteList =new ArrayList<>();
    //今日邀请次数
    private int sendNum=0;
    //队友id
    private volatile long teamateId=0;
    //队友微信名
    private String teamateWxName=null;
    //队友头像
    private String teamateIcon=null;
    //队友局数
    private int teamatePlayNum=0;
    private static ActivityDao dao;
    private int isRead=-1;
    static {
        try {
            dao = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN, "activity")?ActivityDao.getInstance():null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ActivityMsg.ActivityQueQiaoRes.Builder qqMsg=null;
    ActivityMsg.QueQiaoInviteBoardMsg.Builder inviteBoard=ActivityMsg.QueQiaoInviteBoardMsg.newBuilder();

    public ActivityQueQiao(Player self) {
        this.userId = self.getUserId();
        init(self);
        initQueQiaoMsg(self);
    }

    public void init(Player self){
        if(dao==null)
            return;
        InviteQueQiao teamInvite = dao.selectIsAllow(userId);
        if(teamInvite!=null){
            teamateId=teamInvite.getSendId()==userId?teamInvite.getAcceptId():teamInvite.getSendId();
            if(teamInvite.getIsRead()==0&&teamInvite.getSendId()==userId){
                isRead=0;
            }
            Map<String, String> map = dao.selectTeamateInf(teamateId);
            teamateWxName=map.get("name");
            teamateIcon=map.get("headimgurl");
            if(teamateIcon==null)
                teamateIcon="";
            String ext = map.get("ext");
            if(!StringUtil.isBlank(ext)){
                String substring = ext.substring(0,ext.indexOf(","));
                try{
                    teamatePlayNum=Integer.parseInt(substring);
                }catch (Exception e){
                    LogUtil.errorLog.info(e.getMessage());
                }
            }

            int finishNum =
                    MissionConfigUtil.missionIdAndConfig.get(MissionConfigUtil.getQueQiaoSpecialId()).getFinishNum();
            if(teamateId!=0&&teamatePlayNum>=finishNum)
                self.getMission().changeQueQiaoRed(1,null);
        }else {
            List<InviteQueQiao> iqqs = dao.selectBySendIdOrAcceptId(userId);
            if(isRead!=0){
                for (InviteQueQiao iqq:iqqs) {
                    if(iqq.getAcceptId()==userId&&iqq.getIsRead()==0){
                        self.getMission().changeQueQiaoRed(null,1);
                        break;
                    }
                }
            }
            for (InviteQueQiao iqq:iqqs) {
                if(iqq.getSendId()==userId&&iqq.getSendTime()>TimeUtil.getNowDayZeroMS()){
                    sendNum++;
                }
            }
        }
    }

    void initQueQiaoMsg(Player self){
        List<Activity> activity = ActivityUtil.getQueQiaoActive();
        if(activity.size()==0||!activity.get(0).show()){
            return;
        }
        qqMsg= ActivityMsg.ActivityQueQiaoRes.newBuilder();
        Activity atConfig = activity.get(0);
        qqMsg.setTag(MissionConfigUtil.tag_activeQueQiao);
        qqMsg.setActivityName(atConfig.getShowContent());
        qqMsg.setStartTime(TimeUtil.formatTime(atConfig.getBeginTime()));
        qqMsg.setEndTime(TimeUtil.formatTime(atConfig.getEndTime()));
        qqMsg.setDesc(atConfig.getExtend()==null?"":atConfig.getExtend());
        qqMsg.setGoldNum(self.getMission().getQueQiaoGoldNum());
        if(teamateId!=0){
            qqMsg.setTeammateId(""+teamateId);
            qqMsg.setTeammateIcon(this.teamateIcon);
            qqMsg.setTeammateWxName(this.teamateWxName);
            qqMsg.setTeammatPlayNum(teamatePlayNum);
        }

        Map<Integer, MissionState> idAndState = self.getMission().getIdAndState();
        List<Integer> queQiaoId = MissionConfigUtil.getQueQiaoId();
        if(idAndState!=null){
            for (Integer msId:queQiaoId){
                ActivityMsg.SelfQueQiaoMsg.Builder sqq=ActivityMsg.SelfQueQiaoMsg.newBuilder();
                if(idAndState.containsKey(msId)){
                    MissionState ms = idAndState.get(msId);
                    sqq.setId(msId);
                    sqq.setIsComplete(ms.isComplete()?1:0);
                    sqq.setIsObtain(ms.isObtain()?1:0);
                }else {
                    sqq.setId(msId);
                    sqq.setIsComplete(0);
                    sqq.setIsObtain(0);
                }
                qqMsg.addSqqm(sqq);
            }
        }
    }

    public ActivityMsg.ActivityQueQiaoRes getQueQiaoMsg(Player self) {
        qqMsg.setPlayNum(self.getMission().getDayPlayNum());
        return qqMsg.build();
    }

    public ActivityMsg.QueQiaoInviteBoardMsg getInviteList() {
        inviteBoard.clear();
        if(teamateId!=0)//有约会对象之后，邀请列表无需返回
            return inviteBoard.build();
        inviteBoard.setUserId(""+userId);
        List<InviteQueQiaoWithSendMsg> inviteList = dao.selectAllInvite(userId);
        dao.updateIsRead(userId);
        for (InviteQueQiaoWithSendMsg inviteOne:inviteList) {
            ActivityMsg.QueQiaoInviteMsg.Builder qqiMsg=ActivityMsg.QueQiaoInviteMsg.newBuilder();
            qqiMsg.setId(inviteOne.getId());
            qqiMsg.setSendId(""+inviteOne.getSendId());
            qqiMsg.setAcceptId(""+inviteOne.getAcceptId());
            qqiMsg.setIsAllow(inviteOne.getIsAllow());
            qqiMsg.setTeammateWxName(inviteOne.getName());
            qqiMsg.setTeammateIcon(inviteOne.getHeadimgurl()==null?"":inviteOne.getHeadimgurl());
            qqiMsg.setTime(TimeUtil.parseTime(inviteOne.getSendTime(),""));
            inviteBoard.addQqim(qqiMsg);
        }
        return inviteBoard.build();
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public void ZeroReset(){
        sendNum=0;
        teamatePlayNum=0;
        initQueQiaoMsg(PlayerManager.getInstance().getPlayer(userId));
    }


    public void teamateChangePlayNum(int num){
        teamatePlayNum=num;
        qqMsg.setTeammatPlayNum(teamatePlayNum);
        Player self = PlayerManager.getInstance().getPlayer(userId);
        int finishNum =
                MissionConfigUtil.missionIdAndConfig.get(MissionConfigUtil.getQueQiaoSpecialId()).getFinishNum();
        if(teamatePlayNum>=finishNum&&self.getMission().getDayPlayNum()>=finishNum)
            self.getMission().changeQueQiaoRed(1,null);
    }

    public void changeMissionState(Player self, MissionState ms){
        if(qqMsg==null)
            return;
        if(self!=null) {
            qqMsg.setGoldNum(self.getMission().getQueQiaoGoldNum());
            if(ms!=null){
                List<ActivityMsg.SelfQueQiaoMsg.Builder> sqqmList = qqMsg.getSqqmBuilderList();
                for (ActivityMsg.SelfQueQiaoMsg.Builder sqq:sqqmList) {
                    if(sqq.getId()==ms.getId()){
                        sqq.setIsComplete(ms.isComplete()?1:0);
                        sqq.setIsObtain(ms.isObtain()?1:0);
                    }
                }
            }
        }
    }

    public int getTeamatePlayNum() {
        return teamatePlayNum;
    }

    public long getTeamateId() {
        return teamateId;
    }

    public int getAimServerId(long aimId) {
        try {
            return UserDao.getInstance().getUserServerId("" + aimId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*-------------------------------------------------------------------*/

    /**
     * 完成一局对局，告知队友（暂定）
     */
    public void sendTeamateMsgChange(int num){
        if(teamateId==0)
            return;
        int userServerId=getAimServerId(teamateId);
        if (userServerId == GameServerConfig.SERVER_ID) {
            Player teamate = PlayerManager.getInstance().getPlayer(teamateId);
            if (teamate == null)
                return;
            teamate.getAqq().teamateChangePlayNum(num);
        } else {
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("type", "sendUser");
            paramsMap.put("method", "ActiveQueQiao_acceptTeamateChangePlayNum");
            paramsMap.put("aimId", ""+teamateId);

            paramsMap.put("teamatePlayNum", ""+PlayerManager.getInstance().getPlayer(userId).getMission().getDayPlayNum());
            HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(userServerId) + "/goldActivity/msg.do", "UTF-8", "POST", paramsMap);
        }
    }

    /**
     * 收到队友局数变化
     */
    public void acceptTeamateChangePlayNum(Map<String,String> parms){
        try {
            Long aimId = Long.parseLong(parms.get("aimId"));
            if(teamateId!=aimId)
                return;
            Integer teamatePlayNum = Integer.parseInt(parms.get("teamatePlayNum"));
            this.teamatePlayNum=teamatePlayNum;
            Player self = PlayerManager.getInstance().getPlayer(userId);
            int finishNum =
                    MissionConfigUtil.missionIdAndConfig.get(MissionConfigUtil.getQueQiaoSpecialId()).getFinishNum();
            if(teamatePlayNum>=finishNum&&self.getMission().getDayPlayNum()>=finishNum)
                self.getMission().changeQueQiaoRed(1,null);

        }catch (Exception e){
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    /*-------------------------------------------------------------------*/


    /**
     * 同意邀请
     */
    public void sendAllowInvite(long aimId,Player self) throws SQLException {
        if(teamateId!=0){
            self.writeComMessage(WebSocketMsgType.res_code_err,"不能约会多人");
            return;
        }
        int flag = dao.updateAllowTeamInvite(aimId, userId);
        if(flag==0){
            //修改失败 邀请信息已过期
            self.writeComMessage(WebSocketMsgType.res_code_err,"对方已约会");
            dao.deleteOneInvite(aimId,userId);
        }else {
            //同意邀请后删除自己曾经发送的邀请
            dao.updateAllowIsRead(aimId,userId);
            dao.deleteWhereTeamSuccess(userId);
            RegInfo user = UserDao.getInstance().getUser(aimId);
            //填入自己队友信息
            this.teamSuccess(aimId,user.getHeadimgurl(),user.getName(),teamatePlayNum);
            int userServerId=getAimServerId(aimId);
            if (userServerId == GameServerConfig.SERVER_ID) {
                Player aimPlay = PlayerManager.getInstance().getPlayer(aimId);
                if (aimPlay == null)
                    return;
                //将自己信息填入对方aqq对象中
                aimPlay.getAqq().teamSuccess(self.getUserId(),self.getHeadimgurl(),self.getName(),self.getMission().getDayPlayNum());
            } else {
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("type", "sendUser");
                paramsMap.put("method", "ActiveQueQiao_teamSuccess");
                paramsMap.put("aimId", ""+aimId);

                paramsMap.put("sendId", ""+self.getUserId());
                paramsMap.put("sendWxName", ""+self.getName());
                paramsMap.put("sendIcon", ""+self.getHeadimgurl());
                paramsMap.put("sendPlayNum", ""+self.getMission().getDayPlayNum());
                //同意邀请，队友人在同一服务器
                HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(userServerId) + "/goldActivity/msg.do", "UTF-8", "POST", paramsMap);
            }

            self.writeComMessage(WebSocketMsgType.res_code_QueQiaoAllowInvite);
        }
    }

    public void teamSuccess(long teamateId,String teamateIcon,String teamateWxName,int teamatePlayNum){
        this.teamateId=teamateId;
        if(teamateIcon==null)
            teamateIcon="";
        this.teamateIcon=teamateIcon;
        this.teamateWxName=teamateWxName;
        this.teamatePlayNum=teamatePlayNum;
        qqMsg.setTeammateId(""+teamateId);
        qqMsg.setTeammateIcon(this.teamateIcon);
        qqMsg.setTeammateWxName(this.teamateWxName);
        qqMsg.setTeammatPlayNum(teamatePlayNum);
        PlayerManager.getInstance().getPlayer(userId).getActivityQueQiaoMsg();
    }

    public void teamSuccess(Map<String,String> parms){
        try {
            long sendId = Long.parseLong(parms.get("sendId"));
            String sendIcon = parms.get("sendIcon");
            String sendWxName = parms.get("sendWxName");
            int sendPlayNum = Integer.parseInt(parms.get("sendPlayNum"));
            teamSuccess(sendId,sendIcon,sendWxName,sendPlayNum);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    /*-------------------------------------------------------------------*/

    public void invitePeople(long aimId){
        if(aimId==userId)
            return;
        Player player = PlayerManager.getInstance().getPlayer(userId);
        if(sendNum>=5){
            player.writeComMessage(WebSocketMsgType.res_code_err,"每天只能发送5次邀请");
            return;
        }
        if(teamateId!=0){
            player.writeComMessage(WebSocketMsgType.res_code_err,"已和他人约会，不能发送邀请");
            return;
        }
        List<InviteQueQiao> iqqList = dao.select10MinInvite(userId, aimId);
        if(iqqList!=null&&iqqList.size()>0){
            player.writeComMessage(WebSocketMsgType.res_code_err,"同一个人10分钟之内不能邀请第二次");
            return;
        }
        //插入返回Id 尚未完成
        long sendTime=System.currentTimeMillis();
        Integer flag = dao.insertInvite(userId, aimId, sendTime);
        if(flag==null)
            return;
        sendNum++;
        sendInvite(aimId,flag,sendTime);
    }

   /**
    * 发送邀请信息
    */
    public void sendInvite(long aimId, Integer id,long sendTime) {
        if(aimId==userId)
            return;
        int userServerId=getAimServerId(aimId);
        if (userServerId == GameServerConfig.SERVER_ID) {
            Player aimPlayer = PlayerManager.getInstance().getPlayer(aimId);
            if (aimPlayer == null)
                return;
            aimPlayer.getAqq().acceptInvite();
        } else {
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("type", "sendUser");
            paramsMap.put("method", "ActiveQueQiao_acceptInvite");
            paramsMap.put("aimId", ""+aimId);
            HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(userServerId) + "/goldActivity/msg.do", "UTF-8", "POST", paramsMap);
        }
        PlayerManager.getInstance().getPlayer(userId).writeComMessage(WebSocketMsgType.res_code_QueQiaoInvite);
    }

    /**
     * 收到邀请
     */
    public void acceptInvite() {
        if(teamateId==0)
            PlayerManager.getInstance().getPlayer(userId).getMission().changeQueQiaoRed(null,1);
    }

    /*-------------------------------------------------------------------*/
}
