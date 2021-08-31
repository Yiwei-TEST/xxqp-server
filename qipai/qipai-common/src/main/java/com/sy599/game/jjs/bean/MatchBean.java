package com.sy599.game.jjs.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public class MatchBean {

    private Long keyId;
    private Integer serverId;
    private Integer tableCount;
    private volatile String currentState;
    private String matchType;
    private String matchProperty;
    private String matchRule;
    private Integer currentCount;
    private Integer minCount;
    private Integer maxCount;
    private Date createdTime;
    private Date finishedTime;
    private String matchPay;
    private String matchName;
    private String matchDesc;
    private volatile Integer restTable;
    private String matchExt;
    private String tableMsg;
    private Long startTime;

    /**
     * 不是表的字段
     **/
    private volatile int tableTotal = 0;
    private volatile int restUserCount = 0;
    /**userId，轮数，分数，排名**/
    private volatile List<String> userList = new ArrayList<>();
    private JSONObject extJson;
    private JSONObject tableMsgJson;
    private volatile boolean first = true;

    private static final JSONObject JSON_EMPTY = new JSONObject();

    public MatchBean() {

    }

    public MatchBean copy() {
        MatchBean mb = new MatchBean();

        mb.matchType = this.matchType;
        mb.tableMsg = this.tableMsg;
        mb.matchProperty = this.matchProperty;
        mb.matchRule = this.matchRule;
        mb.tableCount = this.tableCount;
        mb.serverId = this.serverId;
        mb.currentCount = 0;
        mb.restTable = 0;
        mb.minCount = this.minCount;
        mb.maxCount = this.maxCount;
        mb.currentState = "0";
        mb.createdTime = new Date();
        mb.finishedTime = new Date();
        mb.matchDesc = this.matchDesc;
        mb.matchName = this.matchName;
        mb.matchPay = this.matchPay;
        mb.extJson = this.extJson;
        mb.tableMsgJson = this.tableMsgJson;
        mb.matchExt = this.matchExt;
        mb.startTime = 0L;
        return mb;
    }

    public boolean first(){
        return this.first;
    }

    public void first(boolean first){
        this.first = first;
    }

    public List<Integer> loadTableInts() {
        return StringUtil.explodeToIntList(tableMsgJson.getString("ints"), ",");
    }

    public int loadGameType() {
        return tableMsgJson.getIntValue("type");
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public List<String> loadTableStrings() {
        return StringUtil.explodeToStringList(tableMsgJson.getString("strs"), ",");
    }

    public JSONObject loadAward(){
        JSONObject json = extJson.getJSONObject("reward");
        return json == null ? JSON_EMPTY: json;
    }

    public String loadAward(int rank){
        JSONObject json = extJson.getJSONObject("reward");
        String award = null;
        if (json!=null){
            for (Map.Entry<String,Object> kv : json.entrySet()){
                String key = kv.getKey();
                int idx=key.indexOf("-");
                if (idx>0){
                    if (rank>=NumberUtils.toInt(key.substring(0,idx),Integer.MAX_VALUE)&&rank<=NumberUtils.toInt(key.substring(idx+1),Integer.MAX_VALUE)){
                        award = Objects.toString(kv.getValue());
                        break;
                    }
                }else if (rank==NumberUtils.toInt(key,Integer.MIN_VALUE)){
                    award = Objects.toString(kv.getValue());
                    break;
                }
            }
        }
        return award == null?"":award;
    }

    public String[] loadReliveMsg(){
        String str = extJson.getString("relive");
        if (StringUtils.isBlank(str)){
            return null;
        }else{
            return str.split(";");
        }
    }

    public String loadExtFieldVal(String name) {
        return extJson.getString(name);
    }

    public int loadExtFieldIntVal(String name) {
        return extJson.getIntValue(name);
    }

    public int loadRestUserCount() {
        synchronized (this) {
            return restUserCount;
        }
    }

    public void initRestUserCount(int restUserCount) {
        this.restUserCount = restUserCount;
    }

    public int addRestUserCount(int count) {
        synchronized (this) {
            if (count != 0) {
                this.restUserCount += count;
            }
            return this.restUserCount;
        }
    }

    public Integer getRestTable() {
        return restTable;
    }

    public void setRestTable(Integer restTable) {
        this.restTable = restTable;
    }

    public String getMatchExt() {
        return matchExt;
    }

    public void setMatchExt(String matchExt) {
        this.matchExt = matchExt;
        if (StringUtils.isBlank(matchExt)) {
            extJson = new JSONObject();
        } else {
            extJson = JSON.parseObject(matchExt);
        }
    }

    public String getMatchPay() {
        return matchPay;
    }

    public void setMatchPay(String matchPay) {
        this.matchPay = matchPay;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public String getMatchDesc() {
        return matchDesc;
    }

    public void setMatchDesc(String matchDesc) {
        this.matchDesc = matchDesc;
    }

    public String getMatchRule() {
        return matchRule;
    }

    public void setMatchRule(String matchRule) {
        this.matchRule = matchRule;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getTableCount() {
        return tableCount;
    }

    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }

    public String getMatchProperty() {
        return matchProperty;
    }

    public void setMatchProperty(String matchProperty) {
        this.matchProperty = matchProperty;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;
        if (StringUtils.isBlank(tableMsg)) {
            tableMsgJson = new JSONObject();
        } else {
            tableMsgJson = JSON.parseObject(tableMsg);
        }
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Date finishedTime) {
        this.finishedTime = finishedTime;
    }

    public int loadTotalTable() {
        return tableTotal;
    }

    public int add(int count) {
        if (count != 0) {
            synchronized (this) {
                tableTotal += count;
                restTable = tableTotal;
            }
        }

        return tableTotal;
    }

    public void initTotalTable(int count) {
        synchronized (this) {
            tableTotal = count;
            restTable = tableTotal;
        }
    }

    public void clear() {
        synchronized (this) {
            userList.clear();
        }
    }

    public int loadUserMingCi(long userId) {
        String userStr = new StringBuilder().append(userId).append(",").toString();
        synchronized (this) {
            for (int i = 0, len = userList.size(); i < len; i++) {
                if (userList.get(i).startsWith(userStr)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public boolean hasRank(long userId) {
        String userStr = new StringBuilder().append(userId).append(",").toString();
        synchronized (this) {
            for (int i = 0, len = userList.size(); i < len; i++) {
                if (userList.get(i).startsWith(userStr)) {
                    return Integer.parseInt(userList.get(i).split(",")[3])>0;
                }
            }
        }
        return false;
    }

    public String loadUserMsg(long userId) {
        String userStr = new StringBuilder().append(userId).append(",").toString();
        synchronized (this) {
            for (String str : userList) {
                if (str.startsWith(userStr)) {
                    return str.substring(userStr.length());
                }
            }
        }
        return null;
    }

    public int loadUserScore(long userId) {
        String userStr = new StringBuilder().append(userId).append(",").toString();
        synchronized (this) {
            for (String str : userList) {
                if (str.startsWith(userStr)) {
                    return Integer.parseInt(str.split(",")[2]);
                }
            }
        }
        return 0;
    }

    public int[] loadUserRank(int leastLevel, String userId) {
        int num = 0;
        int total = 0;
        String userStr = new StringBuilder().append(userId).append(",").toString();
        synchronized (this) {
            if (leastLevel <= 0) {
                total = userList.size();
                for (int i = 0; i < total; i++) {
                    if (userList.get(i).startsWith(userStr)) {
                        num = i + 1;
                        break;
                    }
                }
            } else {
                for (int i = 0, len = userList.size(); i < len; i++) {
                    String[] msg = userList.get(i).split(",");
                    if (Integer.parseInt(msg[1]) >= leastLevel) {
                        total++;

                        if (msg[0].equals(userId)) {
                            num = i + 1;
                        }
                    } else {
                        break;
                    }
                }
            }

            if (total == 0) {
                total = userList.size();
            }
            if (num == 0) {
                num = total;
            }
        }

        return new int[]{num, total};
    }

    /**
     * 获取本轮参加的玩家信息
     * @return
     */
    public List<String> loadCurrentUserMsgs(){
        List<String> list;
        synchronized (this){
            int current = JjsUtil.loadMatchCurrentGameNo(this);
            if (current<=0){
                list = new ArrayList<>(userList);
            }else{
                list = new ArrayList<>();

                for (String str : userList){
                    String[] msgs = str.split(",");
                    if (Integer.parseInt(msgs[1])>=current){
                        list.add(str);
                    }else{
                        break;
                    }
                }
            }
        }
        return list;
    }

    public List<String> loadAliveUserIds(){
        List<String> list = new ArrayList<>();
        synchronized (this){
            for (String str : userList){
                String[] msgs = str.split(",");
                if ("0".equals(msgs[3])||"-1".equals(msgs[3])){
                    list.add(msgs[0]);
                }
            }
        }
        return list;
    }

    public List<String> loadAliveUserMsgs(){
        List<String> list = new ArrayList<>();
        synchronized (this){
            for (String str : userList){
                String[] msgs = str.split(",");
                if ("0".equals(msgs[3])||"-1".equals(msgs[3])){
                    list.add(str);
                }
            }
        }
        return list;
    }

    public List<String> loadUserMsgs() {
        return userList;
    }

    public List<String>[] loadUserMsgs(int count) {
        List<String>[] lists = new List[2];
        if (count <= 0) {
            synchronized (this) {
                lists[0] = new ArrayList<>(userList);
                lists[1] = Collections.emptyList();
                return lists;
            }
        } else {
            List<String> list0 = new ArrayList<>(count);
            List<String> list1 = userList.size() > count ? new ArrayList<>(userList.size() - count) : new ArrayList<>(2);
            synchronized (this) {
                for (int i = 0, len = userList.size(); i < len; i++) {
                    if (i >= count) {
                        list1.add(userList.get(i));
                    } else {
                        list0.add(userList.get(i));
                    }
                }
            }
            lists[0] = list0;
            lists[1] = list1;
            return lists;
        }
    }

    public void resetUserMsg(long userId, int currentNo, int score, int rank) {
        synchronized (this) {
            String userStr = new StringBuilder().append(userId).append(",").toString();
            int idx = -1;
            for (int i = 0, len = userList.size(); i < len; i++) {
                if (userList.get(i).startsWith(userStr)) {
                    idx = i;
                    break;
                }
            }

            if (idx == -1) {
                userList.add(new StringBuilder().append(userId).append(",").append(currentNo).append(",").append(score).append(",").append(rank >= -1 ? rank : 0).toString());
            }else{
                userList.set(idx, new StringBuilder().append(userId).append(",").append(currentNo).append(",").append(score).append(",").append(rank >= -1 ? rank : 0).toString());
            }
        }
    }

    public int addUserMsg(long userId, int currentNo, int score, int rank, boolean updateScore) {
        synchronized (this) {
            String userStr = new StringBuilder().append(userId).append(",").toString();
            int idx = -1;
            for (int i = 0, len = userList.size(); i < len; i++) {
                if (userList.get(i).startsWith(userStr)) {
                    idx = i;
                    break;
                }
            }

            if (idx == -1) {
                userList.add(new StringBuilder().append(userId).append(",").append(currentNo).append(",").append(score).append(",").append(rank>=-1?rank:0).toString());
            } else {
                String[] msgs = userList.get(idx).split(",");
                String rank0 = rank>=-1?String.valueOf(rank): msgs[3];
                if (updateScore) {
                    int score0 = Integer.parseInt(msgs[2]) + score;
                    userList.set(idx, new StringBuilder().append(userId).append(",").append(currentNo).append(",").append(score0).append(",").append(rank0).toString());

                    return score0;
                } else {
                    userList.set(idx, new StringBuilder().append(userId).append(",").append(currentNo).append(",").append(score).append(",").append(rank0).toString());
                }
            }
        }

        return score;
    }

    public void sort(boolean bl) {
        if (bl){
            synchronized (this) {
                Map<Integer, String> map = new HashMap<>();
                for (int i=0,len = userList.size();i<len;i++) {
                    String[] strs = userList.get(i).split(",");
                    if (Integer.parseInt(strs[3]) < 0) {
                        map.put(i,new StringBuilder().append(strs[0]).append(",").append(strs[1]).append(",").append(strs[2]).append(",0").toString());
                    }
                }
                for (Map.Entry<Integer,String> kv : map.entrySet()){
                    userList.set(kv.getKey().intValue(),kv.getValue());
                }
            }
        }
        sort();
    }

    public void sort() {
        synchronized (this) {
            Collections.sort(userList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    //ID,轮数,分数,排名
                    String[] msgs1 = o1.split(",");
                    String[] msgs2 = o2.split(",");
                    if (msgs1.length >= 4 && msgs2.length >= 4) {
                        int m13 = Integer.parseInt(msgs1[3]);
                        int m23 = Integer.parseInt(msgs2[3]);
                        if (m23 > m13) {
                            return -1;
                        } else if (m13 > m23) {
                            return 1;
                        } else {
                            int m10 = Integer.parseInt(msgs1[1]);
                            int m20 = Integer.parseInt(msgs2[1]);
                            if (m10 > m20) {
                                return -1;
                            } else if (m20 > m10) {
                                return 1;
                            } else {
                                int m11 = Integer.parseInt(msgs1[2]);
                                int m21 = Integer.parseInt(msgs2[2]);
                                if (m11 > m21) {
                                    return -1;
                                } else if (m21 > m11) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        }
                    }
                    return 0;
                }
            });
        }
    }

}
