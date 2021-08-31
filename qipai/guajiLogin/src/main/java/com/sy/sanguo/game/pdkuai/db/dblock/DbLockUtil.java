package com.sy.sanguo.game.pdkuai.db.dblock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DbLockUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("sys");
    private static final Map<String, Integer> KeyMap = new ConcurrentHashMap<>();

    public static String lock(DbLockEnum lockEnum, String identity) {
        String lockKey = lockEnum.getLockKey(identity);
        boolean lock = false;
        String unLockKey = UUID.randomUUID().toString();
        try {
            if (!KeyMap.containsKey(lockKey)) {
                if (!DbLockDao.getInstance().hasKey(lockKey)) {
                    long ret = DbLockDao.getInstance().insert(lockKey);
                    if (ret > 0) {
                        KeyMap.put(lockKey, 1);
                    }
                } else {
                    KeyMap.put(lockKey, 1);
                }
            }
            int ret = DbLockDao.getInstance().lock(lockKey, unLockKey, lockEnum.getOverTimeSecond());
            lock = ret > 0;
            if (lock) {
                LOGGER.info("DbLockUtil|lock|succ|" + lockKey + "|" + unLockKey);
                return unLockKey;
            }
        } catch (Exception e) {
            LOGGER.error("DbLockUtil|lock|error|" + e.getMessage(), e);
            if (lock) {
                unLock(lockEnum, identity, unLockKey);
            }
        }
        return null;
    }

    public static int unLock(DbLockEnum lockEnum, String identity, String unLockKey) {
        String lockKey = lockEnum.getLockKey(identity);
        try {
            int ret = DbLockDao.getInstance().unLock(lockKey, unLockKey);
            if (ret <= 0) {
                LOGGER.error("DbLockUtil|unLock|fail|" + lockKey + "|" + unLockKey);
            }
            return ret;
        } catch (Exception e) {
            LOGGER.error("DbLockUtil|unLock|error|" + e.getMessage(), e);
        }
        return 0;
    }

}
