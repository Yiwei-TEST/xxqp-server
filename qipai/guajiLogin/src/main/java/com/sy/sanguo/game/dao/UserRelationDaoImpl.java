package com.sy.sanguo.game.dao;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.ThirdRelation;
import com.sy.sanguo.game.bean.UserRelation;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserRelationDaoImpl extends CommonDaoImpl {

   public Object insert(UserRelation userRelation){
       try {
           return getSqlMapClient().insert("user_relation.insert_user_relation", userRelation);
       }catch (Exception e){
           LogUtil.e("Exception:"+e.getMessage(),e);
       }
       return null;
   }

    public int update(String keyId,String loginPf,Date loginTime){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("keyId",keyId);
            map.put("loginPf",loginPf);
            map.put("loginTime",loginTime);
            return getSqlMapClient().update("user_relation.update_user_relation", map);
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return -1;
    }

    public UserRelation select(String gameCode,String userId){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("gameCode",gameCode);
            map.put("userId",userId);
            return (UserRelation)getSqlMapClient().queryForObject("user_relation.select_one_user_relation", map);
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return null;
    }

    public List<UserRelation> selectBaseAll(Object userId){
        try {
            return (List<UserRelation> )getSqlMapClient().queryForList("user_relation.select_user_relations_base", String.valueOf(userId));
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return null;
    }

    public Object insert(ThirdRelation thirdRelation){
        try {
            return getSqlMapClient().insert("user_relation.insert_third_relation", thirdRelation);
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return null;
    }

    public int updateCheckedTime(String keyId){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("keyId",keyId);
            map.put("checkedTime",new Date());
            return getSqlMapClient().update("user_relation.update_third_relation", map);
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return -1;
    }

    public ThirdRelation selectThirdRelation(String userId){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("userId",userId);
            return (ThirdRelation)getSqlMapClient().queryForObject("user_relation.select_third_relation", map);
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return null;
    }

    public ThirdRelation selectThirdRelation(String thirdId,String thirdPf){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("thirdId",thirdId);
            map.put("thirdPf",thirdPf);
            return (ThirdRelation)getSqlMapClient().queryForObject("user_relation.select_third_relation", map);
        }catch (Exception e){
            LogUtil.e("Exception:"+e.getMessage(),e);
        }
        return null;
    }

}
