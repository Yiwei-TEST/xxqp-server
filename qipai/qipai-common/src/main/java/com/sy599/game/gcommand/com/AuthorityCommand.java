package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Authority;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.dao.AuthorityDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class AuthorityCommand extends BaseCommand {
    private static final int zuanshi=1;
    private static final int yewuyuan=2;
    private static final int qinyouquan=3;

    private static final String spilt = "|+|";

    private static final int pageNumber = 10;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> lists = req.getParamsList();
        if (lists == null || lists.size() == 0 || lists.size() < 3) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        // 获得传递过来的操作指令
        int command = req.getParams(0);
        int type = req.getParams(1);
        int id = req.getParams(2);

        if(command == 1){//请求列表
            int page = 1;
            if(lists.size() >= 4){
                page = req.getParams(3);
            }
            int indexBegin = (page - 1) * pageNumber;
            if(indexBegin < 0){
                indexBegin = 0;
            }
            if(type == zuanshi || type == yewuyuan){
                List<Authority> list = AuthorityDao.getInstance().getAuthorityByQxId(id,type,indexBegin);
                if(list == null || list.size() <= 0){
//                    ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian);
//                    player.writeSocket(com.build());
                    player.writeErrMsg("没有更多数据");
                    return;
                }
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian);
                com.addParams(type);//权限类型
                com.addParams(page);
                for (Authority au : list){
                    com.addStrParams(au.getUserId()+spilt+au.getName()+spilt+au.getCreateTime());
                }
                player.writeSocket(com.build());
                return;
            }else if(type == qinyouquan){
                List<GroupInfo> list = GroupDao.getInstance().getGroupAllCredit(id,indexBegin);
                if(list == null || list.size() <= 0){
//                    ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian);
//                    player.writeSocket(com.build());
                    player.writeErrMsg("没有更多数据");
                    return;
                }
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian);
                com.addParams(type);//权限类型
                com.addParams(page);
                for (GroupInfo au : list){
                    com.addStrParams(au.getGroupId()+spilt+au.getGroupName()+spilt+au.getIsCreditUpTime());
                }
                player.writeSocket(com.build());
                return;
            }
            return;
        }else if(command == 2){
            if(type == zuanshi || type == yewuyuan){
                RegInfo info = UserDao.getInstance().selectUserByUserId(id);
                if(info == null){
                    player.writeErrMsg("玩家不存在");
                    return;
                }
                List<Authority> list = AuthorityDao.getInstance().getAuthorityByQxId(id,type,0);
                if(list != null && list.size() > 0){
                    player.writeErrMsg("玩家已有该权限");
                    return;
                }
                Authority au = new Authority();
                au.setQuanxianId(type);
                au.setUserId(id);
                au.setCreateTime(TimeUtil.currentTimeMillis());
                AuthorityDao.getInstance().insertAuthority(au);
                player.writeErrMsg("添加成功");
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian_add);
                com.addParams(type);//权限类型
                com.addStrParams(au.getUserId()+spilt+info.getName()+spilt+au.getCreateTime());
                player.writeSocket(com.build());
                return;
            }else if(type == qinyouquan){
                GroupInfo info = GroupDao.getInstance().loadGroupInfo(id);
                if(info == null){
                    player.writeErrMsg("亲友圈不存在");
                    return;
                }
                if(info.getIsCredit() == 1){
                    player.writeErrMsg("该亲友圈已是比赛房，不需重新设置");
                    return;
                }
                GroupDao.getInstance().updateGroupIsCredit((long) id,1);
                player.writeErrMsg("添加成功");
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian_add);
                com.addParams(type);//权限类型
                com.addStrParams(id+spilt+info.getGroupName()+spilt+info.getIsCreditUpTime());
                player.writeSocket(com.build());
                return;
            }
            return;
        }else if(command == 3){
            if(type == zuanshi || type == yewuyuan){
                List<Authority> list = AuthorityDao.getInstance().getAuthorityByQxId(id,type,0);
                if(list == null || list.size() <= 0){
                    player.writeErrMsg("玩家没有该权限");
                    return;
                }
                AuthorityDao.getInstance().deleteAuthority(id,type);
                player.writeErrMsg("删除成功");
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian_delete);
                com.addParams(type);//权限类型
                com.addStrParams(id+"");
                player.writeSocket(com.build());
                return;
            }else if(type == qinyouquan){
                GroupInfo info = GroupDao.getInstance().loadGroupInfo(id);
                if(info == null){
                    player.writeErrMsg("亲友圈不存在");
                    return;
                }
                if(info.getIsCredit() == 0){
                    player.writeErrMsg("该亲友圈还不是比赛房");
                    return;
                }
                GroupDao.getInstance().updateGroupIsCredit((long) id,0);
                player.writeErrMsg("删除成功");
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_quanxian_delete);
                com.addParams(type);//权限类型
                com.addStrParams(id+"");
                player.writeSocket(com.build());
                return;
            }
            return;
        }

    }

    @Override
    public void setMsgTypeMap() {

    }

}