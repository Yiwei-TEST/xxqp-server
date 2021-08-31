package com.sy599.game.db.dao;


import com.sy599.game.db.bean.sendDiamonds.SendDiamondsLog;
import com.sy599.game.db.bean.sendDiamonds.SendDiamondsPermission;

import java.util.HashMap;
import java.util.List;

import com.sy599.game.util.LogUtil;

public class PermissionDao extends BaseDao {
    private static PermissionDao permissionDao = new PermissionDao();

    public static PermissionDao getInstance() {
        return permissionDao;
    }
   // public static List<SendDiamondsPermission> = new ArrayList()
    public  SendDiamondsPermission queryPermissionBuUid(long userid){
        try {
            List<SendDiamondsPermission>  list = getSqlLoginClient().queryForList("sendDiamonds.queryPermissionBuUid", userid);
            if(null!=list && !list.isEmpty()){
                return list.get(0);
            }
            System.err.println("list "+list.get(0));
        }catch (Exception e){
            LogUtil.errorLog.error("sendDiamonds.queryPermissionBuUid Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public void insertSendDiamondsLog(HashMap<String, Object> map) {
        try {
             getSqlLoginClient().insert("sendDiamonds.insertSendDiamondsLog", map);
        }catch (Exception e){
            LogUtil.errorLog.error("sendDiamonds.insertSendDiamondsLog Exception:" + e.getMessage(), e);
        }

    }

    public   List<SendDiamondsLog> querySendDiamondsLog(HashMap<String, Object> map){
        try {
            List<SendDiamondsLog>  list = getSqlLoginClient().queryForList("sendDiamonds.querySendDiamondsLog", map);
            return list;
        }catch (Exception e){
            LogUtil.errorLog.error("sendDiamonds.querySendDiamondsLog Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public   int querySendDiamondsLogCount(HashMap<String, Object> map){
        try {
            int l = (int) getSqlLoginClient().queryForObject("sendDiamonds.querySendDiamondsLogCount", map);
            return l;
        }catch (Exception e){
            LogUtil.errorLog.error("sendDiamonds.querySendDiamondsLogCount Exception:" + e.getMessage(), e);
        }
        return 0;
    }


}
