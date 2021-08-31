package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.LHDRecord;
import com.sy599.game.db.bean.UserCardRecordInfo;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LHDRecordDao extends BaseDao {
    private static LHDRecordDao _inst = new LHDRecordDao();

    public static LHDRecordDao getInstance() {
        return _inst;
    }

    public Map getSSZRecord(List<Long> onlineIds) {
        try {
            String userIds = "("+ StringUtil.implode(onlineIds)+")";
            return (Map) getSqlLoginClient().queryForObject("lhdRecord.getSSZRecord", userIds);
        }catch (Exception e) {
            LogUtil.errorLog.error("lhdRecord.getLHDRecord Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public List<Map> getRecent20RichPlayer(List<Long> onlineIds) {
        try {
            String userIds = "("+ StringUtil.implode(onlineIds)+")";
            return (List<Map>) getSqlLoginClient().queryForList("lhdRecord.getRecent20RichPlayer", userIds);
        }catch (Exception e) {
            LogUtil.errorLog.error("lhdRecord.getRecent20RichPlayer Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public void batchSaveLHDRecord(List<LHDRecord> list) {
        try {
            if (list != null && list.size() > 0) {
                SqlMapClient sqlMapClient = getSqlLoginClient();
                sqlMapClient.startBatch();
                for ( int i = 0, n = list.size(); i < n; i++) {
                    sqlMapClient.insert("lhdRecord.saveLHDRecord", list.get(i));
                }
                sqlMapClient.executeBatch();
                LogUtil.msgLog.info("batchInsert saveLHDRecord success:count={}",list.size());
            }
        } catch (Exception e) {
            LogUtil.dbLog.error("batchInsert saveLHDRecord Exception:"+e.getMessage(), e);
        }
    }
}
