package com.sy599.game.db.dao;

import com.sy599.game.db.bean.GoodsItem;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemExchangeDao extends BaseDao {
	private static ItemExchangeDao _inst = new ItemExchangeDao();

	public static ItemExchangeDao getInstance() {
		return _inst;
	}

	@SuppressWarnings("unchecked")
	public int sumItemExchange(long userId,Object type, String begin, String end) {
		return sumItemExchange(userId, type,null, begin, end);
	}

	public int sumItemExchange(long userId,Object type,Object itemId, String begin, String end) {
		try {
			Map<String, Object> map = new HashMap<>(8);
			map.put("userId", String.valueOf(userId));
			map.put("begin", begin);
			map.put("end", end);
			map.put("itemType", String.valueOf(type));
			if (itemId!=null){
				map.put("itemId", String.valueOf(itemId));
			}
			Number ret = ((Number) this.getSqlLoginClient().queryForObject("itemExchange.sumItemExchange", map));
			return ret==null?0:ret.intValue();
		} catch (Exception e) {
			LogUtil.e("itemExchange.sumItemExchange err:"+e.getMessage(), e);
		}
		return -1;
	}

	public long save(Map<String, Object> map) {
		try {
			Long ret = (Long)this.getSqlLoginClient().insert("itemExchange.save", map);
			return ret!=null?ret.longValue():0;
		} catch (Exception e) {
			LogUtil.e("itemExchange.sumItemExchange err:"+e.getMessage(), e);
		}
		return 0;
	}

	public List<HashMap<String,Object>> selectItemExchangePage(long userId,String itemType,String itemId,int pageNo,int pageSize){
		try {
			HashMap<String,Object> map = new HashMap<>(8);
			map.put("userId",String.valueOf(userId));
			map.put("startNo",(pageNo-1)*pageSize);
			map.put("pageSize",pageSize);
			if (StringUtils.isNotBlank(itemType)){
				map.put("itemType",itemType);
			}
			if (StringUtils.isNotBlank(itemId)){
				map.put("itemId",itemId);
			}
			return (List<HashMap<String,Object>>) getSqlLoginClient().queryForList("itemExchange.selectItemExchangePage",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:itemExchange.selectItemExchangePage>"+e.getMessage(),e);
		}
		return null;
	}

    public List<GoodsItem> loadAllGoodsItem(){
        try {

            return (List<GoodsItem>) this.getSqlLoginClient().queryForList("itemExchange.load_all_goods_item");
        } catch (Exception e) {
            LogUtil.errorLog.error("loadAllGoodsItem|error", e);
        }
        return null;
    }

}
