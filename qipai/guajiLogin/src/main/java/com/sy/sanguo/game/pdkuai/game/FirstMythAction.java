package com.sy.sanguo.game.pdkuai.game;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.FirstMyth;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.msg.FirstMythMsg;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.pdkuai.db.dao.FirstMythDao;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.RankEvent;
import com.sy599.sanguo.util.TimeUtil;

/**
 * 封神榜
 *
 * @author wujun
 */
public class FirstMythAction extends BaseAction {
    private static int testDay = 20170807;

    @Override
    public String execute() throws Exception {

        int functype = this.getInt("funcType");

        switch (functype) {
            case 1:
                rankingList();
                break;
            case 2:
                getReward();
                break;
            default:
                break;
        }
        return result;
    }

    public void getReward() throws Exception {
        long userId = this.getLong("userId", 0);
        int day = this.getInt("day", 0);
        int viewIndex = this.getInt("view", 0);
        // 是否领取周排行奖励
        int week = this.getInt("week", 0);

        Map<String, Object> result = new HashMap<>();
        if (week == 0) {
            boolean isToDay = Integer.parseInt(TimeUtil.getSimpleDay(TimeUtil.now())) == day;
            RankEvent rankEvent = StaticDataManager.getRankEvent(day);
            if (rankEvent == null) {
                result.put("msg", "活动错误:" + day);
                this.writeMsg(-1, result);
                return;
            }
            if (isToDay) {
                result.put("msg", "只能领取昨天的奖励");
                this.writeMsg(-1, result);
                return;
            }
            if (viewIndex >= rankEvent.getEvents().size()) {
                // viewIndex = 0;
                result.put("msg", "查看活动错误:" + viewIndex);
                this.writeMsg(-1, result);
                return;
            }

            FirstMyth firstMyth = null;
            List<FirstMyth> rank = null;
            int func = rankEvent.getEvents().get(viewIndex);
            rank = FirstMythDao.getInstance().getFirstMythList(func, day, rankEvent);
            int j = 1;
            if (rank != null && !rank.isEmpty()) {
                for (FirstMyth bean : rank) {
                    Map<String, Object> msgMap = FirstMythDao.getInstance().buildFirstMythMap(bean, j, func, rankEvent);
                    int val = (int) msgMap.get("val");
                    if (val == 0) {
                        continue;
                    }
                    if (func == 1 || func == 8) {
                        if (val <= 0) {
                            continue;
                        }
                    } else if (func == 3 || func == 10) {
                        if (val >= 0) {
                            continue;
                        }
                    }
                    if (bean.getUserId() == userId) {
                        firstMyth = bean;
                        break;
                    }
                    j++;
                }
            }
            if (firstMyth == null) {
                result.put("msg", "没有进入排名");
                this.writeMsg(-1, result);
                return;
            }
            boolean isAward = false;
            if (!StringUtils.isBlank(firstMyth.getRewardRecord())) {
                isAward = firstMyth.getRewardRecord().contains(func + "");
            }
            // 已经领过了奖励
            if (isAward) {
                result.put("msg", "已经领取过了");
                this.writeMsg(-1, result);
                return;
            }
            int freeCards = rankEvent.getAward(j);
            GameBackLogger.SYS_LOG.info(day + "-" + freeCards + "-" + j);
            if (freeCards > 0) {
                // 领奖
                String record = "";
                if (!StringUtils.isBlank(firstMyth.getRewardRecord())) {
                    record = firstMyth.getRewardRecord();
                }
                int update;
                RegInfo user = userDao.getUser(userId);
                if (FirstMythDao.getInstance().getFirstMyth(day, userId) == null) {
                    FirstMythDao.getInstance().insertFirstMyth(user, day);
                }
                update = FirstMythDao.getInstance().updateFirstMyth(userId, day, record + func + ",");
                if (update == 1) {
                    userDao.addUserCards(user, 0, freeCards, 0, CardSourceType.activity_firstMyth);
                    Calendar ca = Calendar.getInstance();
                    int hour = ca.get(Calendar.HOUR_OF_DAY);
                    int min = ca.get(Calendar.MINUTE);
                    LogUtil.i("get Award success:userId:" + userId + " userName:" + user.getName() + "freeCards:" + freeCards + " day:" + day + " time:" + hour + ":" + min);
                } else {
                    result.put("msg", "修改状态错误");
                    this.writeMsg(-1, result);
                    return;
                }
            }

            result.put("cards", freeCards);
            this.writeMsg(0, result);
        } else {
            getWeekAward(userId, viewIndex, week, result);
        }
    }

    /**
     * 周排行领取奖励
     */
    private void getWeekAward(long userId, int viewIndex, int week, Map<String, Object> result) throws SQLException {
        if (week != 2) {
            result.put("msg", "只能领取上周的奖励");
            this.writeMsg(-1, result);
            return;
        }
        // 得到今天的日期
        int day = TimeUtil.getSimpleToDay();
        RankEvent rankEvent = StaticDataManager.getRankEvent(day);
        int func = rankEvent.getEvents().get(viewIndex);
        // 获得上周的起止时间
        int startDay = getStartDay(day, 2);
        int endDay = getEndDay(day, 2);
        if (startDay < StaticDataManager.rankEventStartDay) {
            result.put("msg", "未开始排行");
            this.writeMsg(-1, result);
            return;
        }
        if (endDay > StaticDataManager.rankEventEndDay) {
            endDay = StaticDataManager.rankEventEndDay;
        }
        Map myRank = FirstMythDao.getInstance().getWeekSelfRanking(func, rankEvent, startDay, endDay, userId);
        if (myRank == null || myRank.isEmpty()) {
            result.put("msg", "没有进入排名");
            this.writeMsg(-1, result);
            return;
        }
        boolean isAward = false;
        Map awardBean = FirstMythDao.getInstance().getAwardRecord(userId, getStartDay(day, 1), getEndDay(day, 1));
        if (awardBean != null && !StringUtils.isBlank((String) awardBean.get("rewardRecord"))) {
            isAward = ((String) awardBean.get("rewardRecord")).contains(func + "");
        }
        // 已经领过了奖励
        if (isAward) {
            result.put("msg", "已经领取过了");
            this.writeMsg(-1, result);
            return;
        }
        int freeCards = rankEvent.getAward(((Double) myRank.get("rank")).intValue());
        if (freeCards > 0) {
            String record = "";
            // 领奖
            int update;
            RegInfo user = userDao.getUser(userId);
            FirstMyth myth = FirstMythDao.getInstance().getFirstMyth(day, userId);
            if (myth == null) {
                FirstMythDao.getInstance().insertFirstMyth(user, day);
            } else {
                record = myth.getRewardRecord();
            }
            update = FirstMythDao.getInstance().updateFirstMyth(userId, day, record + func + ",");
            if (update == 1) {
                userDao.addUserCards(user, 0, freeCards, 0, CardSourceType.activity_firstMyth);
                Calendar ca = Calendar.getInstance();
                int hour = ca.get(Calendar.HOUR_OF_DAY);
                int min = ca.get(Calendar.MINUTE);
                LogUtil.i("get Award success:userId:" + userId + " userName:" + user.getName() + " freeCards:" + freeCards + " day:" + day + " time:" + hour + ":" + min);
            } else {
                result.put("msg", "修改状态错误");
                this.writeMsg(-1, result);
                return;
            }
        }
        result.put("cards", freeCards);
        this.writeMsg(0, result);
    }

    public static int getEndDay(int day, int week) {
        int endDay = 0;
        Calendar ca = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            ca.setTime(sdf.parse("" + day));
            if (ca.get(Calendar.DAY_OF_WEEK) == 1) {
                if (week == 2) {
                    ca.add(Calendar.DATE, -7);
                }
            } else {
                ca.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                if (week == 2) {
                    ca.add(Calendar.DATE, -1);
                } else {
                    ca.add(Calendar.DATE, 6);
                }
            }
            String endDayStr = sdf.format(ca.getTime());
            endDay = Integer.parseInt(endDayStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return endDay;
    }

    public static int getStartDay(int day, int week) {
        int startDay = 0;
        try {
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            ca.setTime(sdf.parse("" + day));
            if (ca.get(Calendar.DAY_OF_WEEK) == 1) {
                if (week == 2) {
                    ca.add(Calendar.DATE, -13);
                } else {
                    ca.add(Calendar.DATE, -6);
                }
            } else {
                ca.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                if (week == 2) {
                    ca.add(Calendar.DATE, -7);
                }
            }
            String startDayStr = sdf.format(ca.getTime());
            startDay = Integer.parseInt(startDayStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDay;
    }

    public void rankingList() {
        long userId = this.getLong("userId", 0);
        int day = this.getInt("day", 0);
        int view = this.getInt("view", 0);
        // 周排行 1表示本周排行 2表示上周排行
        int week = this.getInt("week", 0);
        // 金币场标识
        int gold = this.getInt("gold", 0);
        if (week == 0) {
            if (gold == 1) {
                int toDay = TimeUtil.getSimpleToDay();
                if (toDay < StaticDataManager.rankEventStartDay || toDay > StaticDataManager.rankEventEndDay) {
                    LogUtil.e("get rankingList err:day err-->startDay:"+StaticDataManager.rankEventStartDay
                            + ",endDay:"+StaticDataManager.rankEventEndDay+",day:"+day);
                    this.writeMsg(1, result);
                }
                if (day == 0) {
                    day = toDay;
                }
            } else {
                if (day == 0) {
                    int toDay = TimeUtil.getSimpleToDay();
                    if (toDay < StaticDataManager.rankEventStartDay || toDay > StaticDataManager.rankEventEndDay) {
                        toDay = StaticDataManager.rankEventEndDay;
                    }
                    day = toDay;
                }
            }
        } else {
            day = TimeUtil.getSimpleToDay();
        }
        FirstMythDao firstMythDao = FirstMythDao.getInstance();
        FirstMythMsg rankings;
        if (gold == 1 && view == 0) {
            rankings = firstMythDao.wealthRankingList(userId, day);
        } else {
            rankings = firstMythDao.rankingList(userId, day, view, week);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("msg", rankings);
        result.put("day", day);
        result.put("showEndDay", StaticDataManager.getShowRankEndTime());
        result.put("startDay", StaticDataManager.rankEventStartDay);
        result.put("endDay", StaticDataManager.rankEventEndDay);
        result.put("dayList", StaticDataManager.getRankEventDayList());
        result.put("funcList", StaticDataManager.getFuncList(view));

        result.put("nowDay", TimeUtil.getSimpleToDay());
        this.writeMsg(0, result);
    }

    public static void main(String[] args) {

    }


}
