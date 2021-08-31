package com.sy.sanguo.game.dao.gold;

import com.sy.sanguo.game.bean.gold.GoldRoom;
import com.sy.sanguo.game.bean.gold.GoldRoomUser;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldRoomDao extends BaseDao {

    private static GoldRoomDao groupDao = new GoldRoomDao();

    public static GoldRoomDao getInstance(){
        return groupDao;
    }

    public GoldRoomUser loadGoldRoomUser(long roomId, long userId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("roomId",String.valueOf(roomId));
        map.put("userId",String.valueOf(userId));
        return (GoldRoomUser)this.getSql().queryForObject("goldRoom.select_gold_room_user",map);
    }

    public GoldRoom loadGoldRoom(long keyId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("keyId",String.valueOf(keyId));
        return (GoldRoom)this.getSql().queryForObject("goldRoom.select_gold_room",map);
    }

    public List<HashMap<String,Object>> loadRoomUsers(Long roomId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("roomId",roomId.toString());
        return this.getSql().queryForList("goldRoom.select_gold_room_users",map);
    }

    /**
     * 随机获取一个未开局未满人的房间
     */
    public GoldRoom loadCanJoinGoldRoom(long modeId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("modeId",String.valueOf(modeId));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        map.put("myDate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return (GoldRoom)this.getSql().queryForObject("goldRoom.one_gold_room_random",map);
    }

    /**
     * 随机获取一个未开局未满人的房间
     */
    public GoldRoom loadCanJoinGoldRoom(long modeId, int serverId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("modeId",String.valueOf(modeId));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        map.put("myDate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        map.put("serverId",String.valueOf(serverId));
        return (GoldRoom)this.getSql().queryForObject("goldRoom.one_gold_room_random_server",map);
    }

    public Long saveGoldRoom(GoldRoom goldRoom) throws Exception{
        return (Long) this.getSql().insert("goldRoom.insert_gold_room",goldRoom);
    }

    public Long saveGoldRoomUser(GoldRoomUser goldRoomUser) throws Exception{
        return (Long) this.getSql().insert("goldRoom.insert_gold_room_user",goldRoomUser);
    }

    public int deleteGoldRoomUser(long roomId,long userId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("roomId",String.valueOf(roomId));
        map.put("userId",String.valueOf(userId));
        return this.getSql().delete("goldRoom.delete_gold_room_user",map);
    }

    public int updateGoldRoomUser(long roomId,long userId,int gameResult,String logIds) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("roomId",String.valueOf(roomId));
        map.put("userId",String.valueOf(userId));
        map.put("gameResult",String.valueOf(gameResult));
        if (StringUtils.isNotBlank(logIds)){
            map.put("logIds",logIds);
        }
        return this.getSql().update("goldRoom.update_gold_room_user",map);
    }

    public int updateGoldRoom(long keyId,int addCount,String currentState) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("keyId",String.valueOf(keyId));
        if (StringUtils.isNotBlank(currentState)){
            map.put("currentState",currentState);
        }

        if (addCount==1){
            return this.getSql().update("goldRoom.update_gold_room_jia",map);
        }else if (addCount==-1){
            return this.getSql().update("goldRoom.update_gold_room_jian",map);
        }else{
            if (StringUtils.isNotBlank(currentState)){
                return this.getSql().update("goldRoom.update_gold_room_state",map);
            }
        }

        return -1;
    }

}
