package com.sy599.game.jjs.dao;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.bean.MatchUser;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;


public class MatchDao extends BaseDao {

    private final static MatchDao matchDao = new MatchDao();

    public static MatchDao getInstance(){
        return matchDao;
    }

    public Long save(MatchBean matchBean){
        try {
            Object object = getSqlLoginClient().insert("match.insertMatchBean", matchBean);
            if (object != null && (object instanceof Number)) {
                matchBean.setKeyId(((Number) object).longValue());
                return matchBean.getKeyId();
            }
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.insertMatchBean>"+e.getMessage(),e);
        }
        return null;
    }

    public int updateUserCount(String matchId, boolean add){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            return getSqlLoginClient().update(add?"match.updateUserCount1":"match.updateUserCount0", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateUserCount>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateUserCount(String matchId, int currentCount){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            map.put("currentCount",currentCount);
            return getSqlLoginClient().update("match.updateUserCount2", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateUserCount>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateMatchForStarted(MatchBean matchBean){
        int ret = -1;
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchBean.getKeyId().toString());
            map.put("currentState",(matchBean.getMatchRule().contains(";"))?"1_0":"1_1");
            map.put("startTime",matchBean.getStartTime());
            ret = getSqlLoginClient().update("match.updateMatchBeanForStarted", map);
            if (ret>0){
                HashMap<String,Object> map0 = new HashMap<>();
                map0.put("matchId",matchBean.getKeyId().toString());
                map0.put("currentState","1");
                getSqlLoginClient().update("match.batch_update_match_users_state", map0);
            }
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchBeanForStarted>"+e.getMessage(),e);
        }
        return ret;
    }

    public int updateMatch(String matchId,int currentNo){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            if (currentNo>0){
                map.put("currentState","1_"+currentNo);
            }else{
                map.put("currentState","2");
            }
            map.put("finishedTime",CommonUtil.dateTimeToString());
            return getSqlLoginClient().update("match.updateMatchBean", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchBean>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateMatch(String matchId,String beforeState,String afterState){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            map.put("beforeState",beforeState);
            map.put("afterState",afterState);
            map.put("finishedTime",CommonUtil.dateTimeToString());
            return getSqlLoginClient().update("match.updateMatchBean0", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchBean>"+e.getMessage(),e);
        }
        return -1;
    }

    public Long save(MatchUser matchUser){
        try {
            Object object = getSqlLoginClient().insert("match.insertMatchUser", matchUser);
            if (object != null && (object instanceof Number)) {
                matchUser.setKeyId(((Number) object).longValue());
                return matchUser.getKeyId();
            }
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.insertMatchUser>"+e.getMessage(),e);
        }
        return null;
    }

    public int deleteMatch(Long matchId){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId.toString());
            return getSqlLoginClient().delete("match.deleteMatch", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.deleteMatch>"+e.getMessage(),e);
        }
        return -1;
    }

    public int deleteMatchUser(String matchId,String userId){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            map.put("userId",userId);
            return getSqlLoginClient().delete("match.deleteMatchUser", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.deleteMatchUser>"+e.getMessage(),e);
        }
        return -1;
    }

    public MatchUser selectOneMatchUser(String matchId,String userId){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            map.put("userId",userId);
            return (MatchUser) getSqlLoginClient().queryForObject("match.selectOneMatchUser",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectOneMatchUser>"+e.getMessage(),e);
        }
        return null;
    }

    public MatchUser selectPlayingMatchUser(String userId){
        try {
            HashMap<String,Object> map = new HashMap<>(4);
            map.put("userId",userId);
            return (MatchUser) getSqlLoginClient().queryForObject("match.selectPlayingMatchUser",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectPlayingMatchUser>"+e.getMessage(),e);
        }
        return null;
    }

    public MatchBean selectOne(String matchId){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            return (MatchBean) getSqlLoginClient().queryForObject("match.selectMatchBean",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchBean>"+e.getMessage(),e);
        }
        return null;
    }

    public MatchBean selectOne(String matchType,String currentState){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchType",matchType);
            if (currentState != null) {
                map.put("currentState", currentState);
            }
            return (MatchBean) getSqlLoginClient().queryForObject("match.selectMatchBean",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchBean>"+e.getMessage(),e);
        }
        return null;
    }

    public MatchBean selectOne(long matchId,String matchType){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",String.valueOf(matchId));
            map.put("matchType",matchType);
            return (MatchBean) getSqlLoginClient().queryForObject("match.selectMatchBean0",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchBean>"+e.getMessage(),e);
        }
        return null;
    }

    public List<HashMap<String,Object>> selectMatchUsers(String matchId, String currentNo){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId);
            if (StringUtils.isNotBlank(currentNo)){
                map.put("currentNo",currentNo);
            }
            return (List<HashMap<String,Object>>) getSqlLoginClient().queryForList("match.selectMatchUsers",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchUsers>"+e.getMessage(),e);
        }
        return null;
    }

    public List<HashMap<String,Object>> selectMatchUsersPage(long userId,int pageNo,int pageSize){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("userId",String.valueOf(userId));
            map.put("startNo",(pageNo-1)*pageSize);
            map.put("pageSize",pageSize);
            return (List<HashMap<String,Object>>) getSqlLoginClient().queryForList("match.selectMatchUsersPage",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchUsersPage>"+e.getMessage(),e);
        }
        return null;
    }

    public List<HashMap<String,Object>> selectMatchNames(String types){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("types",types);
            return (List<HashMap<String,Object>>) getSqlLoginClient().queryForList("match.selectMatchNames",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchNames>"+e.getMessage(),e);
        }
        return null;
    }

    public int updateMatchUserState(Long matchId,Long userId,String state){
        try {
            HashMap<String,Object> map = new HashMap<>(8);
            map.put("currentState",state);
            map.put("matchId",matchId.toString());
            map.put("userId",userId.toString());
            return getSqlLoginClient().update("match.updateMatchUserState", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchUserState>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateMatchUser(Long matchId,Long userId,HashMap<String,Object> map){
        try {
            map.put("matchId",matchId.toString());
            map.put("userId",userId.toString());
            map.put("modifiedTime",CommonUtil.dateTimeToString());
            return getSqlLoginClient().update("match.updateMatchUser", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchUser>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateMatchUser0(Long matchId,Long userId,HashMap<String,Object> map){
        try {
            map.put("matchId",matchId.toString());
            map.put("userId",userId.toString());
            map.put("modifiedTime",CommonUtil.dateTimeToString());
            return getSqlLoginClient().update("match.updateMatchUser0", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchUser0>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateRestTable(Long matchId,int count,boolean set){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId.toString());
            map.put("modifiedTime",CommonUtil.dateTimeToString());
            if (set){
                map.put("restTable",count);
                return getSqlLoginClient().update("match.updateRestTable", map);
            }else{
                return getSqlLoginClient().update("match.updateRestTable0", map);
            }
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateRestTable>"+e.getMessage(),e);
        }
        return -1;
    }

    public int updateMatchUsers(Long matchId,String currentState){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId.toString());
            map.put("modifiedTime",CommonUtil.dateTimeToString());
            map.put("currentState",currentState);
            return getSqlLoginClient().update("match.updateMatchUsers", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.updateMatchUsers>"+e.getMessage(),e);
        }
        return -1;
    }

    public int dissMatchUsers(Long matchId,String currentState){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("matchId",matchId.toString());
            map.put("modifiedTime",CommonUtil.dateTimeToString());
            map.put("currentState",currentState);
            return getSqlLoginClient().update("match.dissMatchUsers", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.dissMatchUsers>"+e.getMessage(),e);
        }
        return -1;
    }

    public List<MatchBean> selectPlayingMatchBeans(int serverId){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("serverId",String.valueOf(serverId));
            return (List<MatchBean>) getSqlLoginClient().queryForList("match.selectPlayingMatchBeans",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectPlayingMatchBeans>"+e.getMessage(),e);
        }
        return null;
    }

    public List<MatchBean> selectMatchBeans(int serverId,String property,String state){
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("serverId",String.valueOf(serverId));
            map.put("matchProperty",property);
            if (state!=null) {
                map.put("currentState", state);
            }
            return (List<MatchBean>) getSqlLoginClient().queryForList("match.selectMatchBeans",map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchBeans>"+e.getMessage(),e);
        }
        return null;
    }

    public int selectMatchRankNum(long userId, int rank) {
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("userId",String.valueOf(userId));
            map.put("rank", rank);
            return (Integer) getSqlLoginClient().queryForObject("match.selectMatchRankNum", map);
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:match.selectMatchRankNum>"+e.getMessage(),e);
        }
        return 0;
    }

}
