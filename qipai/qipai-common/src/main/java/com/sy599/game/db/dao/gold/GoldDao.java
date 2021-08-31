package com.sy599.game.db.dao.gold;

import com.sy599.game.character.GoldPlayer;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.GoodsItem;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SysPartitionUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldDao extends BaseDao {

    private static GoldDao groupDao = new GoldDao();

    public static GoldDao getInstance(){
        return groupDao;
    }

    /**
     * 创建一个金币玩家身份
     */
    public void createGoldUser(GoldPlayer goldPlayer) throws Exception{
        this.getSqlLoginClient().insert("gold.insertGoldUser", goldPlayer);
    }

    /**
     * 根据userId获得玩家的金币玩家身份
     */
    public GoldPlayer selectGoldUserByUserId(long userId){
        try {
            Object o = this.getSqlLoginClient().queryForObject("gold.selectGoldUserById", userId);
            if (o != null) {
                return (GoldPlayer)o;
            }
        } catch (SQLException e) {
            LogUtil.e("selectGoldUserByUserId err-->userId:"+userId+"-->"+e);
        }
        return null;
    }

    /**
     * 更新金币玩家信息
     */
    public int updateGoldUser(Map<String, Object> changeMap){
        try {
            return this.getSqlLoginClient().update("gold.updateGoldUser", changeMap);
        } catch (SQLException e) {
            LogUtil.e("updateGoldUser err-->"+e);
        }
        return 0;
    }

    /**
     * 更新金币玩家信息
     */
    public int updateGoldUserGrade(long userId,int grade,int grateExp){
        try {
            HashMap<String ,Object> map =new HashMap<>();
            map.put("userId",String.valueOf(userId));
            map.put("grade",grade);
            map.put("gradeExp",grateExp);
            return this.getSqlLoginClient().update("gold.updateGoldUserGrade", map);
        } catch (SQLException e) {
            LogUtil.e("updateGoldUserGrade err-->"+e.getMessage(),e);
        }
        return -1;
    }

    /**
     *
     * @param userId
     * @param win
     * @param lose
     * @param even
     * @param count
     * @return
     */
    public int updateGoldUserCount(long userId,int win,int lose,int even,int count){
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", String.valueOf(userId));
            if (win!=0){
                map.put("playCountWin", win);
            }
            if (lose!=0){
                map.put("playCountLose", lose);
            }
            if (even!=0){
                map.put("playCountEven", even);
            }
            if (count!=0){
                map.put("playCount", count);
            }
            if (map.size()>=2){
                return this.getSqlLoginClient().update("gold.updateGoldUserCount", map);
            }else {
                return 0;
            }
        }catch (Exception e){
            return -1;
        }
    }

    /***
     * 获得指定日期领取补救金的次数
     * @param date 当天日期 yyyyMMdd
     */
    public int selectDrawRemedyCount(String date, long userId) throws Exception{
        Map<String, Object> map = new HashMap<>();
        String str = "'"+date+"%'";
        map.put("drawTime", str);
        map.put("userId", userId);
        Object o = this.getSqlLoginClient().queryForObject("gold.selectDrawRemedyCount", map);
        if (o == null) {
            return 0;
        } else {
            return (int) o;
        }
    }

    /***
     * 获得指定日期领取补救金的次数
     *
     * @param date 当天日期 yyyy-MM-dd
     */
    public int selectRemedyCount(String date, long userId) throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", date+" 00:00:00");
        map.put("endDate", date+" 23:59:59");
        map.put("userId", userId);
        Object o = this.getSqlLoginClient().queryForObject("gold.selectRemedyCount", map);
        if (o == null) {
            return 0;
        } else {
            return (int) o;
        }
    }

    /**
     * 领取补救金
     */
    public void drawRemedy(String date, long userId, int remedy) throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put("drawTime", date);
        map.put("userId",  userId);
        map.put("remedy", remedy);
        this.getSqlLoginClient().insert("gold.drawRemedy", map);
    }

    /**
     * 更新表中的玩家金币数量
     */
    public int updateUserGold(long userId, long freeGold, long gold, long usedGold) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("freeGold", freeGold);
        modify.put("gold", gold);
        if (usedGold != 0) {
            modify.put("usedGold", usedGold);
        }
        int update = 0;
        try {
            update = this.getSqlLoginClient().update("gold.updateUserGold", modify);
        } catch (SQLException e) {
            LogUtil.dbLog.error("#GoldDao.updateUserGold:", e);
        }
        return update;
    }

    /**
     * 更新根据玩法的金币消耗统计
     */
    public int updateConsumeGold(int date, int gold, int freeGold, Map<Integer, Integer> playTypeMap) {
        StringBuilder builder1=new StringBuilder();
        StringBuilder builder2=new StringBuilder();
        StringBuilder builder3=new StringBuilder();

        builder1.append("insert into roomgold_consume_statistics(consumeDate,commonGold,freeGold");
        builder2.append("\"").append(date).append("\"").append(",")
                .append(gold).append(",").append(freeGold);
        builder3.append("consumeDate=").append("\"").append(date).append("\"").append(",")
                .append("commonGold=commonGold+").append(gold).append(",")
                .append("freeGold=freeGold+").append(freeGold);

        for (Map.Entry<Integer,Integer> kv: playTypeMap.entrySet()) {
            int key = kv.getKey();
            int val = kv.getValue();
            if (key>0&&val!=0){
                builder1.append(",").append("playType").append(key);
                builder2.append(",").append(val);
                builder3.append(",").append("playType").append(key).append("=").append("playType").append(key).append("+").append(val);
            }
        }
        builder1.append(") values (").append(builder2).append(") on duplicate key update ").append(builder3);

        String sql=builder1.toString();

        int update = 0;
        try {
            update = getSqlLoginClient().update("gold.updateConsumeGold", sql);
        } catch (Exception e) {
//			LogUtil.e("log.updateConsumeCards err:"+JacksonUtil.writeValueAsString(map), e);
            LogUtil.e("gold.updateConsumeGold err:"+e.getMessage(), e);
        }finally {
            LogUtil.msgLog.info("roomgold_consume_statistics:result="+update+",sql="+sql);
        }
        return update;
    }

    /**
     * 清理领取救济金表(清理三天前的数据）
     */
    public void clearDrawRemedy() {
        try {
            this.getSqlLoginClient().update("gold.clear_drawRemedy", 72);
        } catch (SQLException e) {
            LogUtil.e("clearDrawRemedy err-->", e);
        }
    }

    public List<HashMap<String, Object>> selectGoldPlayerCountRank(int limitNum) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("limitNum", limitNum);
        return (List<HashMap<String,Object>>)getSqlLoginClient().queryForList("gold.selectGoldPlayerCountRank", map);
    }

    public List<HashMap<String, Object>> selectGoldPlayerGoldRank(int limitNum) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("limitNum", limitNum);
        return (List<HashMap<String,Object>>)getSqlLoginClient().queryForList("gold.selectGoldPlayerGoldRank", map);
    }

    public List<HashMap<String, Object>> selectGoldPlayerJiFen(String pf, int limitNum) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("pf", pf);
        map.put("limitNum", limitNum);
        return (List<HashMap<String,Object>>)getSqlLoginClient().queryForList("gold.selectGoldPlayerJiFen", map);
    }

    /**
     * 提交用户更新
     *
     * @return
     * @throws SQLException
     */
    public int changeUserGold(long userId,long oldGold ,long oldFreeGold , long gold, long freeGold) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("oldGold",oldGold);
        modify.put("oldFreeGold",oldFreeGold);
        modify.put("gold", gold);
        modify.put("freeGold", freeGold);
        int update = 0;
        try {
            update = this.getSqlLoginClient().update("gold.change_user_gold", modify);
        } catch (SQLException e) {
            LogUtil.dbLog.error("#PlayerDao.changeUserGold:", e);
        }
        return update;
    }

    /**
     * 获取玩家的房卡 0cards，1freeCards
     * @param userId
     * @return
     */
    public long[] loadUserGold(long userId){
        try {
            HashMap<String,Object> map = (HashMap<String,Object>)this.getSqlLoginClient().queryForObject("gold.load_user_gold",userId);
            if (map==null||map.size()==0){
                return new long[]{0,0};
            }else{
                Object gold = map.getOrDefault("gold",0);
                Object freeGold = map.getOrDefault("freeGold",0);
                return new long[]{(gold instanceof Number)?((Number)gold).longValue():0,(freeGold instanceof Number)?((Number)freeGold).longValue():0};
            }
        } catch (SQLException e) {
            LogUtil.dbLog.error("#PlayerDao.loadUserCards:"+e.getMessage(), e);
            return new long[]{0,0};
        }
    }

    /**
     * 提交用户更新
     *
     * @return
     * @throws SQLException
     */
    public int changeUserGoldDirect(long userId, long gold, long freeGold) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("gold", gold);
        modify.put("freeGold", freeGold);
        int update = 0;
        try {
            update = this.getSqlLoginClient().update("gold.change_user_gold_direct", modify);
        } catch (SQLException e) {
            LogUtil.dbLog.error("#PlayerDao.changeUserGold:", e);
        }
        return update;
    }

}
