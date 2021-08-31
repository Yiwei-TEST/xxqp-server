package com.sy.sanguo.game.pdkuai.manager;

import com.alibaba.fastjson.TypeReference;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.bean.redbag.*;
import com.sy.sanguo.game.pdkuai.action.RedBagAction;
import com.sy.sanguo.game.pdkuai.db.dao.RedBagSystemInfoDao;
import com.sy.sanguo.game.pdkuai.db.dao.UserRedBagRecordDao;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.TimeUtil1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 现金红包管理类
 */
public class RedBagManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedBagManager.class);

    private static RedBagManager _inst = new RedBagManager();

    public static RedBagManager getInstance() {
        return _inst;
    }

//    private static List<RedBagReceiveRecord> fakeRecords = new ArrayList<>();

    private static int randomFakePos = 0;

//    static {
//        fakeRecords.add(new RedBagReceiveRecord("掌心的回忆", 2.88f));
//        fakeRecords.add(new RedBagReceiveRecord("无可替代", 2.88f));
//        fakeRecords.add(new RedBagReceiveRecord("小cc", 1.88f));
//        fakeRecords.add(new RedBagReceiveRecord("残影", 1.88f));
//        fakeRecords.add(new RedBagReceiveRecord("王星", 1.88f));
//        fakeRecords.add(new RedBagReceiveRecord("Shi谁在等待", 1.88f));
//        fakeRecords.add(new RedBagReceiveRecord("对温柔懂得不多。", 1.58f));
//        fakeRecords.add(new RedBagReceiveRecord("小茉莉", 1.58f));
//        fakeRecords.add(new RedBagReceiveRecord("黄土高坡", 1.58f));
//        fakeRecords.add(new RedBagReceiveRecord("KTH", 1.58f));
//        fakeRecords.add(new RedBagReceiveRecord("周姐", 1.58f));
//        fakeRecords.add(new RedBagReceiveRecord("张大兵", 1.58f));
//        randomFakePos = RandomUtils.nextInt(0, fakeRecords.size());
//    }

    /**
     * 获取系统现金红包活动数据
     * @return
     */
    public RedBagSystemInfo getRedBagSystemInfo(RedBagConfig config) {
        RedBagSystemInfo redBagSystemInfo = RedBagSystemInfoDao.getInstance().getRedBagSystemInfo();
        if (redBagSystemInfo == null) {
            redBagSystemInfo = new RedBagSystemInfo(config.getPoolNum());
            RedBagSystemInfoDao.getInstance().saveRedBagSystemInfo(redBagSystemInfo);
        } else {
            Date curDate = new Date();
            if (!TimeUtil1.isSameDay(curDate.getTime(), redBagSystemInfo.getCreatedTime().getTime())) {
                LOGGER.info("现金红包活动奖池重置！" + "重置时间：" + curDate + "重置前奖池：" + redBagSystemInfo.getDayPoolNum() + "重置后奖池：" + config.getPoolNum());
                redBagSystemInfo.setCreatedTime(curDate);
                redBagSystemInfo.setDayPoolNum(config.getPoolNum());
                redBagSystemInfo.setReceiveRecords("");
                RedBagSystemInfoDao.getInstance().updateRedBagSystemInfo(redBagSystemInfo);
            }
        }
        return redBagSystemInfo;
    }

    public boolean isReceiveFirstLoginRedBag(long userId) {
        List<UserRedBagRecord> list = getUserRedBagReords(userId);
        for (UserRedBagRecord record : list) {
            if (record.getReceiveNum() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isReceiveFirstGameRedBag(long userId) {
        List<UserRedBagRecord> list = getUserRedBagReords(userId);
        for (UserRedBagRecord record : list) {
            if (record.getReceiveNum() > 1) {
                return false;
            }
        }
        return true;
    }

    public List<UserRedBagRecord> getUserRedBagReords(long userId) {
        List<UserRedBagRecord> list = UserRedBagRecordDao.getInstance().getUserRedBagRecord(userId);
        if(list == null || list.isEmpty()) {
            list = new ArrayList<>();
            String receiveDate = TimeUtil1.formatDayTime2(new Date());
            UserRedBagRecord resultRecord = new UserRedBagRecord(userId, receiveDate, 0,  0);
            UserRedBagRecordDao.getInstance().saveUserRedBagRecord(resultRecord);
            list.add(resultRecord);
        }
        return list;
    }

    public UserRedBagRecord getTodayUserRedBagRecord(long userId) {
        String receiveDate = TimeUtil1.formatDayTime2(new Date());
        String key = userId + "_" + receiveDate;
        List<UserRedBagRecord> list = getUserRedBagReords(userId);
        UserRedBagRecord result = null;
        for(UserRedBagRecord record : list) {
            if (key.equals(record.getUserId() + "_" + record.getReceiveDate())) {
                result = record;
                break;
            }
        }
        if(result == null) {
            UserRedBagRecord initRecord = new UserRedBagRecord(userId, receiveDate, 0,  0);
            UserRedBagRecordDao.getInstance().saveUserRedBagRecord(initRecord);
            result = initRecord;
        }
        return result;
    }

    public List<SelfRedBagReceiveRecord> getUserAllReceiveRecords(long userId) {
        List<SelfRedBagReceiveRecord> result = new ArrayList<>();
        List<UserRedBagRecord> records = getUserRedBagReords(userId);
        if(records != null && !records.isEmpty()) {
            for(UserRedBagRecord record : records) {
                result.addAll(record.getReceiveRecordList());
            }
        }
        return result;
    }

    /**
     * @param userId
     * @return
     */
    public float getUserCanReceiveRedBag(long userId) {
        float totalNum = 0;
        List<UserRedBagRecord> records = getUserRedBagReords(userId);
        if(records != null && !records.isEmpty()) {
            for(UserRedBagRecord record : records) {
                for(SelfRedBagReceiveRecord receiveRecord : record.getReceiveRecordList()) {
                    if(!receiveRecord.isWithDraw()) {
                        totalNum += receiveRecord.getReceiveNum();
                    }
                }
            }
        }
        return totalNum;
    }


    public void updateUserRedBagRecord(UserRedBagRecord userRedBagRecord) {
        UserRedBagRecordDao.getInstance().saveUserRedBagRecord(userRedBagRecord);
    }

    public void updateTodayRedBagGameNum(long userId) {
        String redBagConfigStr = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_config");
        if(redBagConfigStr == null || redBagConfigStr.isEmpty()) {
            return;
        }
        RedBagConfig config = new RedBagConfig(redBagConfigStr, "");
        Date nowDate = TimeUtil1.now();
        Date startDate = new Date(TimeUtil1.parseTimeInMillis(config.getStartDate() + " 00:00:00"));
        Date endDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowEndDate() + " 00:00:00"));
        if (nowDate.after(startDate) && nowDate.before(endDate)) {// 活动期间玩牌
            UserRedBagRecord record = getTodayUserRedBagRecord(userId);
            if (record.getGameNum() < 4) {//低于4局
                record.alterGameNum(1);
                updateUserRedBagRecord(record);
            }
        }
    }

    /**
     * 现金红包奖池更新
     * @param subNum  本次抽取的奖金
     * @return 实际获得的奖金
     */
    public float subPoolRedBag(float subNum, RedBagConfig config) {
        RedBagSystemInfo info = getRedBagSystemInfo(config);
        float realNum = info.subDayPoolNum(subNum);
        RedBagSystemInfoDao.getInstance().updateRedBagSystemInfo(info);
        return realNum;
    }

    /**
     *  添加现金红包玩家领取记录
     * @param receiveRecord
     * @return
     */
    public LinkedBlockingQueue<RedBagReceiveRecord> addRedBagReceiveRecord(RedBagReceiveRecord receiveRecord, RedBagConfig config) throws Exception {
        RedBagSystemInfo info = getRedBagSystemInfo(config);
        LinkedBlockingQueue<RedBagReceiveRecord> list = info.getReceiveRecordList();
        if (list.size() >= 100) {
            List<RedBagReceiveRecord> records = new ArrayList<>(list);
            records.add(0, receiveRecord);
            List<RedBagReceiveRecord> bigRecords = new ArrayList<>();
            for(RedBagReceiveRecord record : list) {
                if(record.getReceiveNum() > 1.28f) {
                    bigRecords.add(record);
                    records.remove(record);
                }
            }
            Collections.sort(bigRecords);
            List<RedBagReceiveRecord> updateRecords = new ArrayList<>();
            updateRecords.addAll(bigRecords);
            updateRecords.addAll(records);
            updateRecords.remove(updateRecords.size() - 1);
            list = new LinkedBlockingQueue<>(updateRecords);
        } else {
            list.add(receiveRecord);
            List<RedBagReceiveRecord> records = new ArrayList<>(list);
            List<RedBagReceiveRecord> bigRecords = new ArrayList<>();
            for(RedBagReceiveRecord record : list) {
                if(record.getReceiveNum() > 1.28f) {
                    bigRecords.add(record);
                    records.remove(record);
                }
            }
            Collections.sort(bigRecords);
            list = new LinkedBlockingQueue<>(bigRecords);
            list.addAll(records);
        }
        String records = JacksonUtil.writeValueAsString(list);
        info.setReceiveRecords(records);
        RedBagSystemInfoDao.getInstance().updateRedBagSystemInfo(info);
        return list;
    }

    public static void main(String[] args) {
        String records = "[{\"receiveNum\":1.06,\"userName\":\"三十二三\"},{\"receiveNum\":0.8,\"userName\":\"平安是福\"},{\"receiveNum\":0.94,\"userName\":\"平安是福\"},{\"receiveNum\":1.2,\"userName\":\"牵你双\"},{\"receiveNum\":0.86,\"userName\":\"小海鸥\"},{\"receiveNum\":0.8,\"userName\":\"老农民\"},{\"receiveNum\":0.89,\"userName\":\"谁在前世约了你\"},{\"receiveNum\":1.11,\"userName\":\"如果爱   请深爱。\"},{\"receiveNum\":0.81,\"userName\":\"知足\"},{\"receiveNum\":1.05,\"userName\":\"独忆\"},{\"receiveNum\":1.12,\"userName\":\"Mr      Ye\"},{\"receiveNum\":0.99,\"userName\":\"hua\"},{\"receiveNum\":1.13,\"userName\":\"天无绝人之路，不死终会出头\"},{\"receiveNum\":1.13,\"userName\":\"、尛爺、半生沉浮゛°\"},{\"receiveNum\":0.98,\"userName\":\"ZXH\"},{\"receiveNum\":1.12,\"userName\":\"绿叶\"},{\"receiveNum\":0.85,\"userName\":\" 流浪汉\"},{\"receiveNum\":1.26,\"userName\":\"拼搏\"},{\"receiveNum\":1.11,\"userName\":\"千姿丽人\"},{\"receiveNum\":0.88,\"userName\":\"午后阳光\"},{\"receiveNum\":0.96,\"userName\":\"水墨丹青\"},{\"receiveNum\":0.86,\"userName\":\"爱已¥太久\"},{\"receiveNum\":0.83,\"userName\":\"蝶恋\"},{\"receiveNum\":0.83,\"userName\":\"草果儿\"},{\"receiveNum\":1.25,\"userName\":\"辰仔妈\"},{\"receiveNum\":0.88,\"userName\":\"q实、狠爱n1\"},{\"receiveNum\":0.84,\"userName\":\"刘志华\"},{\"receiveNum\":1.09,\"userName\":\"醉码头\"},{\"receiveNum\":1.15,\"userName\":\"林娜\"},{\"receiveNum\":0.81,\"userName\":\"Good tomorrow\"},{\"receiveNum\":1.16,\"userName\":\"美好明天\"},{\"receiveNum\":0.88,\"userName\":\"美好明天\"},{\"receiveNum\":0.92,\"userName\":\"幸福.有你\"},{\"receiveNum\":0.9,\"userName\":\"幸福.有你\"},{\"receiveNum\":1.21,\"userName\":\"勇敢的心\"},{\"receiveNum\":1.27,\"userName\":\"蒋海明\"},{\"receiveNum\":1.26,\"userName\":\"海纳百川\"},{\"receiveNum\":0.8,\"userName\":\"好\"},{\"receiveNum\":0.85,\"userName\":\"王\"},{\"receiveNum\":1.01,\"userName\":\"利平\"},{\"receiveNum\":0.98,\"userName\":\"马梓轩\"},{\"receiveNum\":0.91,\"userName\":\"马梓轩\"},{\"receiveNum\":1.27,\"userName\":\"不要再欺負人\"},{\"receiveNum\":0.87,\"userName\":\"平凡的人生\"},{\"receiveNum\":0.84,\"userName\":\"烟雨蒙蒙\"},{\"receiveNum\":1.1,\"userName\":\"烟雨蒙蒙\"},{\"receiveNum\":1.12,\"userName\":\"宁\"},{\"receiveNum\":1.18,\"userName\":\"AAA₂₀₁₈扬帆起航\uE418\"},{\"receiveNum\":0.81,\"userName\":\"传奇\"},{\"receiveNum\":1.21,\"userName\":\"看破红尘不剃头\"},{\"receiveNum\":1.27,\"userName\":\"看破红尘不剃头\"},{\"receiveNum\":0.86,\"userName\":\"诺@言\"},{\"receiveNum\":0.82,\"userName\":\"包\"},{\"receiveNum\":1.01,\"userName\":\"默\"},{\"receiveNum\":0.87,\"userName\":\"幸运草\"},{\"receiveNum\":0.88,\"userName\":\"开花地季节\"},{\"receiveNum\":1.04,\"userName\":\"回守、零点\"},{\"receiveNum\":7.98,\"userName\":\"～义\"},{\"receiveNum\":1.19,\"userName\":\":諾  諾  諾\"},{\"receiveNum\":0.88,\"userName\":\"老男孩\"},{\"receiveNum\":0.81,\"userName\":\"一切随变\"},{\"receiveNum\":0.83,\"userName\":\"可爱的你\"},{\"receiveNum\":1.2,\"userName\":\"心语情深\"},{\"receiveNum\":1.03,\"userName\":\" 明\"},{\"receiveNum\":0.82,\"userName\":\"more than what is due\"},{\"receiveNum\":1.0,\"userName\":\"阿莲\"},{\"receiveNum\":1.18,\"userName\":\"杨兴安\"},{\"receiveNum\":0.92,\"userName\":\"郭小童\"},{\"receiveNum\":1.19,\"userName\":\"        Sunshine\"},{\"receiveNum\":0.82,\"userName\":\"金丝侯\"},{\"receiveNum\":0.99,\"userName\":\"志诚粮油15117158009\"},{\"receiveNum\":1.07,\"userName\":\"金丝侯\"},{\"receiveNum\":0.95,\"userName\":\"℡*农村ಥ卡哇ǒ伊￡\"},{\"receiveNum\":0.8,\"userName\":\"天真遇上现实\"},{\"receiveNum\":1.05,\"userName\":\"狼嚎\"},{\"receiveNum\":1.16,\"userName\":\"回忆，是碎了的玻璃§\"},{\"receiveNum\":1.03,\"userName\":\"ξξsouven\"},{\"receiveNum\":0.82,\"userName\":\"二丫宝宝\"},{\"receiveNum\":1.27,\"userName\":\"இ*執ఠ姊ൠ之℡ず\"},{\"receiveNum\":1.17,\"userName\":\"天寳诱惑\"},{\"receiveNum\":0.91,\"userName\":\"笑对人生\"},{\"receiveNum\":1.25,\"userName\":\"Catsayer.\"},{\"receiveNum\":0.89,\"userName\":\"小哥哥\"},{\"receiveNum\":0.85,\"userName\":\"涛声依旧\"},{\"receiveNum\":0.8,\"userName\":\"馨儿\"},{\"receiveNum\":0.86,\"userName\":\"做不了你妞做你娘\"},{\"receiveNum\":0.82,\"userName\":\"其实丶我不懂\"},{\"receiveNum\":1.15,\"userName\":\"幸福人生！！\"},{\"receiveNum\":0.98,\"userName\":\"爷们 ↘很霸道@\"},{\"receiveNum\":0.91,\"userName\":\"寻梦香巴拉旅行社崔静13893389087\"},{\"receiveNum\":1.01,\"userName\":\"果断\"},{\"receiveNum\":0.86,\"userName\":\"好梦易醒\"},{\"receiveNum\":0.8,\"userName\":\"靳永斌\"},{\"receiveNum\":0.84,\"userName\":\"千姿丽人\"},{\"receiveNum\":0.83,\"userName\":\"被遗忘的爱\"},{\"receiveNum\":1.25,\"userName\":\"燕子\"},{\"receiveNum\":0.8,\"userName\":\"低调\"},{\"receiveNum\":1.0,\"userName\":\"低调\"},{\"receiveNum\":0.87,\"userName\":\"专业管道服务15103988687\"},{\"receiveNum\":0.82,\"userName\":\"梦要自己扛泪要自己尝\"}]";
        LinkedBlockingQueue<RedBagReceiveRecord> list = com.sy.sanguo.common.util.JacksonUtil.readValue(records, new TypeReference<LinkedBlockingQueue<RedBagReceiveRecord>>() {});
        List<RedBagReceiveRecord> all = new ArrayList<>(list);
        Collections.sort(all);
        list = new LinkedBlockingQueue<>(all);
        System.out.println(list);
        String sortrecords = JacksonUtil.writeValueAsString(list);
        System.out.println(sortrecords);
    }
}
