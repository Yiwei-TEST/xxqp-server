package com.sy.sanguo.game.pdkuai.db.dblock;

import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DbLockDao extends BaseDao {
    private static final Logger LOGGER = LoggerFactory.getLogger("db");

    private static DbLockDao INST = new DbLockDao();

    public static DbLockDao getInstance() {
        return INST;
    }

    public boolean hasKey(String lockKey) {
        Integer count = 0;
        Map<String, Object> map = new HashMap<>();
        map.put("lockKey", lockKey);
        try {
            count = (Integer) this.getSql().queryForObject("dbLock.countLockKey", map);
        } catch (Exception e) {
            LOGGER.error("DbLockDao|hasKey|error|" + e.getMessage(), e);
        }
        return count != null && count > 0;
    }

    public long insert(String lockKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("lockKey", lockKey);
        map.put("unLockKey", UUID.randomUUID().toString());
        Long ret = -1L;
        try {
            ret = (Long) this.getSql().insert("dbLock.insertDbLock", map);
        } catch (Exception e) {
            LOGGER.error("DbLockDao|insert|error|" + e.getMessage(), e);
        }
        return ret;
    }

    public int lock(String lockKey, String unLockKey, int timeSecond) {
        Map<String, Object> map = new HashMap<>();
        map.put("lockKey", lockKey);
        map.put("unLockKey", unLockKey);
        map.put("timeSecond", timeSecond);
        try {
            return this.getSql().update("dbLock.lock", map);
        } catch (Exception e) {
            LOGGER.error("DbLockDao|lock|error|" + e.getMessage(), e);
        }
        return -1;
    }

    public int unLock(String lockKey, String unLockKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("lockKey", lockKey);
        map.put("unLockKey", unLockKey);
        try {
            return this.getSql().update("dbLock.unLock", map);
        } catch (Exception e) {
            LOGGER.error("DbLockDao|lock|error|" + e.getMessage(), e);
        }
        return -1;
    }
}
