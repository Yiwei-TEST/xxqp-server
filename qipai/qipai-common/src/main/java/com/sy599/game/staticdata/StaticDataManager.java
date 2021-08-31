package com.sy599.game.staticdata;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.GoodsItem;
import com.sy599.game.db.dao.BaseConfigDao;
import com.sy599.game.db.dao.ItemExchangeDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.staticdata.bean.*;
import com.sy599.game.staticdata.model.*;
import com.sy599.game.util.*;
import com.sy599.game.util.helper.ResourceHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 静态数据管理器</br> 管理所有静态数据
 *
 * @author taohuiliang
 * @version v1.0
 * @date 2013-2-27
 */
public class StaticDataManager extends ResourceHandler {
    /**
     * 通用分隔符 *
     */
    public final static String DELIMITER = ",";

    /**
     * csv文件名称
     */
    private static String csv_path = "";

    public static Map<Integer, List<ConsumeRegion>> consumeMap = new HashMap<Integer, List<ConsumeRegion>>();
    public static Map<Integer, DrawLottery> drawLotteryMap = new HashMap<>();
    public static List<KeyValuePair<Integer, String>> drawIdNameList = new ArrayList<>();
    public static String drawIdNameListStr;
    public static int drawType;
    public static Map<Integer, Long> drawLottery = new HashMap<>();
    public static Map<Integer, Award> awardMap = new HashMap<Integer, Award>();
    public static Map<Integer, Map<Integer, GoodsItem>> goodsItemMap = new HashMap<>();
    public static Map<Integer, Activity> activityMap = new HashMap<Integer, Activity>();
    public static List<ActivityCsvInfo> activityCsvList = new ArrayList<>();
    public static Map<Integer, List<ActivityBean>> activityCsvMap = new HashMap<>();

    public static Map<Integer, GameReBate> gameRebateMap = new HashMap<Integer, GameReBate>();

    public static void init(String path) throws Exception {
        csv_path = path;
        loadConfigs();
    }

    public static void loadConfigs(){
        loadAward();
        LogUtil.msgLog.info("loadAward finished");
        loadDrawLottery();
        LogUtil.msgLog.info("loadDrawLottery finished");
        loadActivity();
        LogUtil.msgLog.info("loadActivity finished");
//        loadActivityCsvData();
//        LogUtil.msgLog.info("loadActivityCsvData finished");
        loadGameRebate();
        LogUtil.msgLog.info("loadGameRebate finished");
        loadActivityConfig();
        LogUtil.msgLog.info("loadActivityConfig finished");
        LangMsg.loadLangMsg(csv_path);
        LogUtil.msgLog.info("loadLangMsg finished");
        loadTaskConfig();
        LogUtil.msgLog.info("loadTaskConfig finished");
        loadGradeExpConfig();
        LogUtil.msgLog.info("loadGradeExpConfig finished");

        // loadImg();
        // loadCards();
        // loadOrder();

        loadGoodItems();
        LogUtil.msgLog.info("loadGoodItems finished");
    }

    public static GoodsItem loadGoodsItem(int type,int id){
        Map<Integer,GoodsItem> map=goodsItemMap.get(type);
        return map!=null?map.get(id):null;
    }

    public static Map<Integer,GoodsItem> loadGoodsItems(int type){
        Map<Integer,GoodsItem> map=goodsItemMap.get(type);
        return map!=null?map:Collections.emptyMap();
    }

    private static void loadActivity() {
        Map<Integer, Activity> itemMap = new ConcurrentHashMap<>();
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("ActivityConfig"));
            if (list==null){
                list = readCSV("activity.csv", false);
            }
        }else{
            list = readCSV("activity.csv", false);
        }

        for (String[] values : list) {
            int i = 0;
            Activity bean = new Activity();
            bean.setId(getIntValue(values, i++));
            bean.setName(getValue(values, i++));
            bean.setConditions(getValue(values, i++));
            bean.setAwardStr(getValue(values, i++));

            if (!StringUtils.isBlank(bean.getAwardStr())) {
                List<Award> awardList = new ArrayList<>();
                for (int awardstr : StringUtil.explodeToIntArray(bean.getAwardStr())) {
                    Award award = awardMap.get(awardstr);
                    if (award != null) {
                        awardList.add(award);
                    }
                }

                bean.setAwardList(awardList);
            }

            itemMap.put(bean.getId(), bean);
        }

        activityMap = itemMap;

    }

    private static void loadDrawLottery() {
        Map<Integer, DrawLottery> drawLotteryMap0 = new ConcurrentHashMap<>();
        List<KeyValuePair<Integer, String>> drawIdNameList0 = new ArrayList<>();
        Map<Integer, Long> drawLottery0 = new ConcurrentHashMap<>();
        int drawType0 = 0;

        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("DrawLotteryConfig"));
            if (list==null){
                list = readCSV("drawLottery.csv", false);
            }
        }else{
            list = readCSV("drawLottery.csv", false);
        }

        for (String[] values : list) {
            int i = 0;
            DrawLottery bean = new DrawLottery();
            bean.setId(getIntValue(values, i++));
            bean.setName(getValue(values, i++));
            bean.setWeight(getLongValue(values, i++));
            bean.setItemId(getIntValue(values, i++));
            bean.setItemNum(getIntValue(values, i++));
            KeyValuePair<Integer, String> key = new KeyValuePair<>();
            key.setId(bean.getId());
            key.setValue(bean.getName());
            if (drawType0 == 0 && bean.getName().contains("现金红包")) {
                drawType0 = 1;
            }
            drawIdNameList0.add(key);
            drawLotteryMap0.put(bean.getId(), bean);
            drawLottery0.put(bean.getId(), bean.getWeight());
        }
        drawIdNameListStr = JacksonUtil.writeValueAsString(drawIdNameList0);

        drawType = drawType0;
        drawIdNameList = drawIdNameList0;
        drawLotteryMap = drawLotteryMap0;
        drawLottery = drawLottery0;
    }

    private static void loadAward() {
        Map<Integer, Award> itemMap = new ConcurrentHashMap<>();
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("AwardConfig"));
            if (list==null){
                list = readCSV("award.csv", false);
            }
        }else{
            list = readCSV("award.csv", false);
        }

        for (String[] values : list) {
            int i = 0;
            Award bean = new Award();
            bean.setId(getIntValue(values, i++));
            bean.setFreeCards(getIntValue(values, i++));
            bean.setCards(getIntValue(values, i++));
            itemMap.put(bean.getId(), bean);
        }
        awardMap=itemMap;
    }

    private static void loadGoodItems() {
        Map<Integer, Map<Integer, GoodsItem>> itemMap = new ConcurrentHashMap<>();
        List<GoodsItem> list = new ArrayList<>();
        if (TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN, "t_goods_item")) {
            list = ItemExchangeDao.getInstance().loadAllGoodsItem();
        }
        if (list == null || list.size() == 0) {
            return;
        }
        for (GoodsItem goodsItem : list) {
            Map<Integer, GoodsItem> map = itemMap.get(goodsItem.getType());
            if (map == null) {
                map = new LinkedHashMap<>();
                map.put(goodsItem.getId(), goodsItem);
                itemMap.put(goodsItem.getType(), map);
            } else {
                map.put(goodsItem.getId(), goodsItem);
            }
        }
        goodsItemMap = itemMap;
    }

    private static void parseGameRebate(List<String[]> csvList) {
        Map<Integer, GameReBate> itemMap = new ConcurrentHashMap<>();
        if(csvList.isEmpty())
            return;
        for (String[] values : csvList) {
            int i = 0;
            String wanfans=getValue(values, i++);
            String openServerDate=getValue(values, i++);
            int rebateRangeTime=getIntValue(values, i++);
            int baseBureau=getIntValue(values, i++);
            if (NumberUtils.isDigits(wanfans)){
                GameReBate bean = new GameReBate();
                bean.setWanfa(Integer.parseInt(wanfans));
                bean.setOpenServerDate(openServerDate);
                bean.setRebateRangeTime(rebateRangeTime);
                bean.setBaseBureau(baseBureau);
                itemMap.put(bean.getWanfa(), bean);
            }else{
                String[] wfs=wanfans.split("_");
                for (String wf:wfs){
                    if (NumberUtils.isDigits(wf)){
                        GameReBate bean = new GameReBate();
                        bean.setWanfa(Integer.parseInt(wf));
                        bean.setOpenServerDate(openServerDate);
                        bean.setRebateRangeTime(rebateRangeTime);
                        bean.setBaseBureau(baseBureau);
                        itemMap.put(bean.getWanfa(), bean);
                    }
                }
            }
        }
        gameRebateMap = itemMap;
    }

    private static void loadGameRebate() {
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("GameRebateConfig"));
            if (list==null){
                list = readCSV("gameRebate.csv", false);
            }
        }else{
            list = readCSV("gameRebate.csv", false);
        }

        parseGameRebate(list);
    }

    public static GameReBate getGameRebate(int wanfa) {
        return gameRebateMap.get(wanfa);
    }

    private static void loadActivityConfig() {
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("ActivityConfigConfig"));
            if (list==null){
                list = readCSV("activityConfig.csv", false);
            }
        }else{
            list = readCSV("activityConfig.csv", false);
        }
        parseActivityConfig(list);
    }

    private static void loadTaskConfig() {
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("TaskConfig"));
            if (list==null){
                list = readCSVResource(csv_path + "taskConfig.csv", false);
            }
        }else{
            list = readCSVResource(csv_path + "taskConfig.csv", false);
        }

        parseTaskConfig(list);
    }

    private static void loadGradeExpConfig() {
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("GradeExpConfig"));
            if (list==null) {
                list = readCSVResource(csv_path + "gradeExp.csv", false);
            }
        }else{
            list = readCSVResource(csv_path + "gradeExp.csv", false);
        }
        parseGradeExpConfig(list);
    }

    /**
     * 配置文件重新加载 目前加载热加载活动配置文件 可在此添加欲加载的csv文件
     * @param resourceFileName
     */
    @Override
    public void reload(String resourceFileName) {
        List<String[]> list = readCSVResource(resourceFileName, false);
        if(list.isEmpty())
            return;
        if(resourceFileName.contains("activityConfig.csv")) {
            parseActivityConfig(list);
        } else if(resourceFileName.contains("gameRebate.csv")) {
            parseGameRebate(list);
        } else if(resourceFileName.contains("taskConfig.csv")) {
            parseTaskConfig(list);
        } else if(resourceFileName.contains("gradeExp.csv")) {
            parseGradeExpConfig(list);
        }
    }

    /**
     *  解析活动专区活动配置文件
     */
    private static void parseActivityConfig(List<String[]> csvList) {
        if(csvList.isEmpty())
            return;
        Map<Integer, ActivityConfigInfo> map = new HashMap<>();
        for (String[] values : csvList) {
            int i = 0;
            int id = getIntValue(values, i++);
            String activityName = getValue(values, i++);
            int visible = getIntValue(values, i++);
            int open = getIntValue(values, i++);
            String wanfas = getValue(values, i++);
            List<Integer> wanfaIds = StringUtil.explodeToIntList(wanfas, "_");
            int type = getIntValue(values, i++);
            String startTime = getValue(values, i++);
            Date startDate = null;
            if(!StringUtil.isBlank(startTime)) {
                startDate = new Date(TimeUtil.parseTimeInMillis(startTime));
            }
            String endTime = getValue(values, i++);
            Date endDate = null;
            if(!StringUtil.isBlank(endTime)) {
                endDate = new Date(TimeUtil.parseTimeInMillis(endTime));
            }
            String params = getValue(values, i++);
            String rewards = getValue(values, i++);
            String desc = getValue(values, i++);
            int sort = getIntValue(values, i++);
            int singleEnter = getIntValue(values, i++);
            try {
                ActivityConfigInfo configInfo = ObjectUtil.newInstance(ActivityConfig.activityConfigClsMap.get(id));
                configInfo.loadActivityConfigInfo(id, activityName, desc, visible, open, wanfaIds, type, startDate, endDate, params, rewards, sort, singleEnter);
                configInfo.configParamsAndRewards();
                map.put(id, configInfo);
            } catch(Exception e) {
                LogUtil.msgLog.error(activityName + "活动配置初始化异常" + e.getMessage(),e);
            }
        }
        ActivityConfig.setActivityConfigMap(map);
    }

    /**
     *  解析任务配置文件
     */
    private static void parseTaskConfig(List<String[]> csvList) {
        if(csvList.isEmpty())
            return;
        Map<Integer, TaskConfigInfo> map = new HashMap<>();
        for (String[] values : csvList) {
            int i = 0;
            int taskId = getIntValue(values, i++);
            int taskType = getIntValue(values, i++);
            String param = getValue(values, i++);
            String rewardParam = getValue(values, i++);
            String taskDesc = getValue(values, i++);
            String rewardDesc = getValue(values, i++);
            try {
                TaskConfigInfo configInfo = new TaskConfigInfo(taskId, taskType, param, rewardParam, taskDesc, rewardDesc);
                map.put(taskId, configInfo);
            } catch(Exception e) {
                LogUtil.msgLog.error("任务配置初始化异常" + e.getMessage(),e);
            }
        }
        TaskConfig.initTaskConfigInfos(map);
    }

    /**
     *  解析任务配置文件
     */
    private static void parseGradeExpConfig(List<String[]> csvList) {
        if(csvList.isEmpty())
            return;
        Map<Integer, GradeExpConfigInfo> map = new HashMap<>();
        for (String[] values : csvList) {
            int i = 0;
            int grade = getIntValue(values, i++);
            int needExp = getIntValue(values, i++);
            String desc = getStrValue(values, i++);
            try {
                GradeExpConfigInfo configInfo = new GradeExpConfigInfo(grade, needExp, desc);
                map.put(grade, configInfo);
            } catch(Exception e) {
                LogUtil.msgLog.error("任务配置初始化异常" + e.getMessage(),e);
            }
        }
        GradeExpConfig.setGradeExpConfigMap(map);
    }



    /**
     * getValue读取csv
     *
     * @param values
     * @param index
     * @return
     */
    private static String getValue(String[] values, int index) {
        return StringUtil.getValue(values, index);
    }

    public static ActivityBean getActivityBean(int type) {
        List<ActivityBean> list = activityCsvMap.get(type);
        if (list == null || list.isEmpty()) {
            return null;
        }

        long now = TimeUtil.currentTimeMillis();
        Iterator<ActivityBean> iterator = list.iterator();
        while (iterator.hasNext()) {
            ActivityBean activityBean = iterator.next();
            if (activityBean.getEndTime() < now) {
                iterator.remove();
                continue;
            }

            if (activityBean.getStartTime() <= now && activityBean.getEndTime() >= now) {
                return activityBean;
            }
        }

        return null;
    }

    /**
     * 初始化activity
     *
     * @param activityCsvList
     */
    public static void setActivityCsvList(List<ActivityCsvInfo> activityCsvList) {
        activityCsvMap.clear();
        StaticDataManager.activityCsvList = activityCsvList;
        for (ActivityCsvInfo info : activityCsvList) {
            ActivityBean bean = new ActivityBean();
            bean.load(info);
            List<ActivityBean> list;
            if (activityCsvMap.containsKey(info.getType())) {
                list = activityCsvMap.get(info.getType());
            } else {
                list = new ArrayList<>();
                activityCsvMap.put(info.getType(), list);
            }
            list.add(bean);

        }

    }

    private static long getLongValue(String[] values, int index) {
        return StringUtil.getLongValue(values, index);
    }

    private static int getIntValue(String[] values, int index) {
        return StringUtil.getIntValue(values, index);
    }

    private static float getFloatValue(String[] values, int index) {
        return StringUtil.getFloatValue(values, index);
    }

    private static boolean isRead(String filename) {
        File file = new File(csv_path + filename);
        if (!file.exists()) {
            LogUtil.msgLog.error("--------" + filename + "is not exists----------");
            return false;
        }

        return true;
    }

    /**
     * 读取csv文件
     *
     * @param filePath      csv目录下的子文件夹目录名/csv的文件名
     * @param includeHeader list是否包含第一行
     * @return List<String[]> String[]的每个值依次为csv文件每一行从左到右的单元格的值
     */
    private static List<String[]> readCSV(String filePath, boolean includeHeader) {
        List<String[]> list = new ArrayList<String[]>();
        if (!isRead(filePath)) {
            return list;
        }
        CsvReader reader = null;
        try {
            reader = new CsvReader(csv_path + filePath, ',', Charset.forName("UTF-8"));
            /** csv的第一行 * */
            reader.readHeaders();
            String[] headers = reader.getHeaders();
            if (includeHeader) {
                // 读取UTF-8格式有bug 需去掉第一个字符的空格
                headers[0] = headers[0].substring(1);
                list.add(headers);
            }
            /** 从第二行开始读 * */
            while (reader.readRecord()) {
                String[] values = reader.getValues();
                if (values.length != 0 && !StringUtils.isBlank(values[0])) {
                    list.add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /** 关闭reader * */
            if (reader != null) {
                reader.close();
            }
        }
        return list;
    }

}
