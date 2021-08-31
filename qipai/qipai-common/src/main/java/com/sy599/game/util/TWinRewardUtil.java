package com.sy599.game.util;

import java.util.HashMap;
import java.util.List;

import com.sy599.game.db.bean.TWinReward;
import com.sy599.game.db.dao.ResourcesConfigsDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;

public class TWinRewardUtil {
	    public static HashMap<Integer,TWinReward> twinList=new HashMap<>();

	    public static void init(){
	        initwinReward();
	       
	    }
	    
	    public static synchronized void initwinReward(){
	        try {
	            List<TWinReward> list = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"t_twin_reward") ?
	                    ResourcesConfigsDao.getInstance().queryAllTwinReward() : null;
	            if(list!=null){
	            	twinList.clear();
	                for (TWinReward config:list){
	                	config.initBaijinBeanList(config.getBaijinBean());
	                	config.initGoldenBeanList(config.getGoldenBean());
	                	twinList.put(config.getWinCount(), config);
	                }
	            }
	        } catch (Exception e) {
	            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
	        }
	    }

	    
}
