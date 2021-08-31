package com.sy599.game.util;

import java.util.Date;

import com.sy.mainland.util.CacheUtil;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.table_state;

public final class GroupRoomUtil {

    public final static double MAX_WEIGHT = 100000000000000000000000D;
    public final static double MIN_WEIGHT = 0;
    public final static long BASE_TIME = 1525417200988L;

    /**
     * 获取俱乐部房间的权重值
     * @param state 0,1
     * @param currentCount 大于等于0，小于100
     * @param date long
     * @return
     */
    public static long loadWeight(String state,int currentCount,Date date){
        long weight = BASE_TIME - date.getTime();//<=13位
        if (currentCount>0){
            //15位
            weight += (currentCount*100000000000000L);
        }
        if ("1".equals(state)){
            //19位
            weight += 1000000000000000000L;
        }else  if ("0".equals(state)){
            //18位
            weight += 100000000000000000L;
        }
        return weight;
    }

    /**
     * 获取俱乐部房间列表的主key值
     * @param groupId
     * @return
     * @throws Exception
     */
    public static String loadGroupKey(String groupId,int room) throws Exception {
        StringBuilder strBuilder = new StringBuilder("group_tables_").append(groupId);
        if (room > 0){
            strBuilder.append("_").append(room);
        }
        return CacheUtil.loadStringKey(null,strBuilder.toString());
    }

    /**
     * 获取俱乐部房间列表详细信息的主key值
     * @param groupId
     * @return
     * @throws Exception
     */
    public static String loadGroupTableKey(String groupId,int room) throws Exception {
        StringBuilder strBuilder = new StringBuilder("msg_group_tables_").append(groupId);
        if (room > 0){
            strBuilder.append("_").append(room);
        }
        return CacheUtil.loadStringKey(null,strBuilder.toString());
    }

    /**
     * 获取匹配模式下能取消的最小时间(ms)
     * @return
     */
    public static int loadGroupMatchMinTimeForCancel(){
        return ResourcesConfigsUtil.loadIntegerValue("ServerConfig","group_match_min_time_cancel",3*60);
    }

    /**
     * 能否加入房间
     *
     * @return
     */
    public final static boolean canJoinTable(Player player) {
        return player.getPlayingTableId() <= 0 && !player.isPlayingMatch() && !player.getMyExtend().isGroupMatch();
    }
    
    
    
    /**
     * 邀请能否加入房间
     *
     * @return
     */
    public final static boolean canJoinInviteTable(Player player) {
    	
    	if(player.isPlayingMatch()) {
    		return false;
    	}
    	if(player.getMyExtend().isGroupMatch()) {
    		return false;
    	}
    	
    	BaseTable tb =player.getPlayingTable();
    	if(tb != null) {
    		if(tb.getState()!=table_state.ready) {
    			return false;
    		}
    	}
    	return true;
    }
    

}
