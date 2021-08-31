package com.sy599.sanguo.util;

import com.sy.sanguo.game.dao.SqlDao;
import com.sy.sanguo.game.pdkuai.db.bean.SysPartition;
import com.sy.sanguo.game.pdkuai.db.dao.SysPartitionDao;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SysPartitionUtil {

    /*** 是否开启亲友圈分表功能***/
    public static final boolean switch_of_group_partition = true;

    /*** 是否亲友圈数据：写主表***/
    public static boolean switch_of_group_write_master = true;

    /*** 是否亲友圈数据：写分表***/
    public static boolean switch_of_group_write_partition = true;

    /*** 是否查分表***/
    public static boolean switch_of_group_read_partition = false;


    /*** 类型：俱乐部***/
    public static final int type_group = 1;
    public static Map<Long, Integer> groupIdSeqMap = new ConcurrentHashMap<>();
    public static ArrayList<Integer> groupHashSeqList = new ArrayList<>();
    public static ArrayList<Integer> allGroupSeqList = new ArrayList<>();
    public static Map<Integer, String> groupSeqStrMap = new HashMap<>();

    public static  int user_gold_record_table_count = 4;

    /**
     * 初始化
     */
    public static void init() throws Exception {
        List<SysPartition> list = SysPartitionDao.getInst().loadAllSysPartition();
        initGroupPartition(list);
        refreshConfig();

        user_gold_record_table_count = ResourcesConfigsUtil.loadServerConfigIntegerValue("user_gold_record_table_count", 1);
    }

    /**
     * 初始化是否读分表
     */
    public static void refreshConfig() {
        switch_of_group_write_master = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_of_group_write_master", "1"));
        switch_of_group_write_partition = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_of_group_write_partition", "0"));
        switch_of_group_read_partition = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_of_group_read_partition", "0"));

        user_gold_record_table_count = ResourcesConfigsUtil.loadServerConfigIntegerValue("user_gold_record_table_count", 1);
    }

    public static void initGroupPartition(List<SysPartition> list) throws Exception {
        if (!switch_of_group_partition) {
            return;
        }
        if (list == null || list.size() == 0) {
            return;
        }
        List<Integer> allTemp = new ArrayList<>();
        List<Integer> hashTemp = new ArrayList<>();
        for (SysPartition data : list) {
            if (data.getType() != type_group) {
                continue;
            }
            checkGroupPartition(data.getSeq());
            allTemp.add(data.getSeq());
            groupSeqStrMap.put(data.getSeq(), "_" + data.getSeq());
            if (data.getIsHash() == 1) {
                hashTemp.add(data.getSeq());
            }
            if (StringUtils.isNotBlank(data.getIds())) {
                String[] splits = data.getIds().trim().split(",");
                for (String split : splits) {
                    groupIdSeqMap.put(Long.valueOf(split.trim()), data.getSeq());
                }
            }
        }
        Collections.sort(allTemp);
        Collections.sort(hashTemp);
        allGroupSeqList = new ArrayList<>(allTemp);
        groupHashSeqList = new ArrayList<>(hashTemp);
    }

    public static boolean isWritePartition() {
        return switch_of_group_write_partition;
    }

    public static boolean isWriteMaster() {
        return switch_of_group_write_master;
    }

    private static String getGroupDbSeq(long groupId) {
        // 配置了俱乐部分表序号
        int seq = 1;
        if (groupIdSeqMap.containsKey(groupId)) {
            seq = groupIdSeqMap.get(groupId);
        } else {
            // 未配置时，散落
            if (groupHashSeqList.size() == 0) {
                seq = 1;
            } else {
                int seqSize = groupHashSeqList.size();
                seq = groupHashSeqList.get((int) (groupId % seqSize));
            }
            groupIdSeqMap.put(groupId, seq);
        }
        if (groupSeqStrMap.containsKey(seq)) {
            return groupSeqStrMap.get(seq);
        } else {
            return "";
        }
    }

    /**
     * 根据俱乐部groupId获取分表序号
     *
     * @param groupId
     * @return
     */
    public static String getGroupSeqForMaster(long groupId) {
        return "";
    }

    /**
     * 根据俱乐部groupId获取分表序号
     *
     * @param groupId
     * @return
     */
    public static String getGroupSeqForPartition(long groupId) {
        if (!switch_of_group_partition) {
            return "";
        }
        return getGroupDbSeq(groupId);
    }

    /**
     * 根据俱乐部groupId获取分表序号
     *
     * @param groupId
     * @return
     */
    public static String getGroupSeqForRead(long groupId) {
        if (!switch_of_group_partition) {
            return "";
        }
        if (switch_of_group_read_partition) {
            return getGroupDbSeq(groupId);
        }
        return "";
    }


    public static void checkGroupPartition(int seq) throws Exception {
        if (!SqlDao.getInstance().checkTableExists("t_table_user_" + seq)) {
            throw new RuntimeException("initGroupPartition|error|t_table_user_" + seq + "|notExist");
        }
        if (!SqlDao.getInstance().checkTableExists("t_group_credit_log_" + seq)) {
            throw new RuntimeException("initGroupPartition|error|t_group_credit_log_" + seq + "|notExist");
        }
    }


    public static void genGroupPartitionSeq() throws Exception {
        if (allGroupSeqList.size() == 0) {
            init();
        }
        if (allGroupSeqList.size() == 0) {
            return;
        }
        List<Long> gids = SysPartitionDao.getInst().loadAllGroupId();
        if (gids == null || gids.size() == 0) {
            return;
        }
        for (Long groupId : gids) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("groupId", groupId);
            map.put("seq", getGroupSeqForPartition(groupId).substring(1));
            SysPartitionDao.getInst().insertTempGroupPartition(map);
        }
    }

    public static String getUserGoldRecordSeq(long userId) {
        if (user_gold_record_table_count <= 1) {
            return "";
        }
        return "_" + (userId % user_gold_record_table_count + 1);
    }

}
