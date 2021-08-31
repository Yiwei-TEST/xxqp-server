package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.*;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.dao.group.GroupDaoManager;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.dao.UserMessageDao;
import com.sy.sanguo.game.pdkuai.util.ClassTool;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.utils.LoginUtil;
import com.sy599.sanguo.util.LotteryUtil;
import com.sy599.sanguo.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;

public class UserDaoImpl extends CommonDaoImpl {
    /**
     * 增加用户
     *
     * @param userInfo
     * @throws SQLException
     */
    public long addUser(RegInfo userInfo) throws SQLException {
        Long userId = (Long)this.getSqlMapClient().insert("user.addUser", userInfo);
        if (userId!=null&&userId.longValue()==userInfo.getUserId()){
            setToCache(userInfo);
            return userId;
        }
        String msg="insert userInfo fail:user="+JSON.toJSONString(userInfo) +",userId="+userId;
        LogUtil.e(msg);
        throw new SQLException(msg);
    }

    /**
     * 查找用户
     *
     * @param username
     * @return
     * @throws SQLException
     */
    public RegInfo getUser(String username, String pf) throws SQLException {
        RegInfo user;
        try {
//            user = getFromCache(username, pf);
            // if (user != null) {
            // if (user.getIsOnLine() == 0) {
            // return user;
            // }
            // }
            Map<String, Object> param = new HashMap<String, Object>();
            if (pf.equals(LoginUtil.pf_phoneNum)){
                user = (RegInfo) this.getSqlMapClient().queryForObject("getUserByPhoneNum", LoginUtil.encryptPhoneNumAES(username));
                if (null == user) {
                    user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserByPhoneNum", username);
                }
            }else {
                param.put("username", username);
                if ("true".equals(PropertiesCacheUtil.getValue("weixin_openid",Constants.GAME_FILE))){
                    param.put("pf", pf);
                    user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUser0", param);
                }else{
                    param.put("pf", pf.startsWith("weixin")?"weixin%":pf);
                    user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUser", param);
                }
            }
//            if (user != null)
//                setToCache(user);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    /**
     * 查找用户
     * @param unionId
     * @param unionPf
     * @param thirdId
     * @param thirdPf
     * @return
     * @throws SQLException
     */
    public RegInfo getUser(String unionId,String unionPf,String thirdId,String thirdPf) throws SQLException {
        RegInfo user;
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("unionId", unionId);
            param.put("unionPf", unionPf.endsWith("%")?unionPf:(unionPf+"%"));
            param.put("flatId", thirdId);
            param.put("pf", thirdPf);
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserUnion", param);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    public Map<String,Object> loadUserBase(String userId) throws SQLException {
        Map<String,Object> user;
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("userId", userId);
            user = (Map<String,Object>) this.getSqlMapClient().queryForObject("user.selectUserBaseMsg", param);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    /**
     * 查找用户
     *
     * @param unionid
     * @return
     * @throws SQLException
     */
    public RegInfo getUserByUnionid(String unionid) throws SQLException {
        RegInfo user = null;
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("identity", unionid);
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserByUnionid", param);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    /**
     * 查找用户
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public RegInfo getUser(long userId) throws SQLException {
        RegInfo user = null;
        try {
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserById", userId);
            if (user != null)
                setToCache(user);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    public List<HashMap<String,Object>> loadUsersByUserId(String userIds) throws SQLException {
        return (List<HashMap<String,Object>>) this.getSqlMapClient().queryForList("user.loadUsersByUserId", userIds);
    }

    public long getMaxId() throws SQLException {
        Object o = this.getSqlMapClient().queryForObject("user.getMaxId");
        if (o != null) {
            return Long.parseLong(o.toString());
        }
        return 0;
    }

    public void insertUserPromotion(long userId, String phone) throws SQLException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("userId", userId);
        sqlMap.put("phone", phone);
        this.getSqlMapClient().insert("user.addUserPromotion", sqlMap);
    }

    public void insertUserExtendinf(long userId, String cdkType, int bindSongCard) throws SQLException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("userId", userId);
        sqlMap.put("cdk", cdkType);
        sqlMap.put("extend", "");
        sqlMap.put("myConsume", "");
        sqlMap.put("shengMoney", 0);
        sqlMap.put("prizeFlag", 0);
        sqlMap.put("name", "");
        sqlMap.put("totalMoney", 0);
        sqlMap.put("bindSongCard", bindSongCard);
        this.getSqlMapClient().insert("user.addUserExtendinf", sqlMap);
    }

    public String getUserPhone(String phone) throws SQLException {
        Object o = this.getSqlMapClient().queryForObject("user.getPhone", phone);
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    public Long getUserPromotionByUid(Long userId) throws SQLException {
        Long uid = (Long) this.getSqlMapClient().queryForObject("user.getUserPromotionByUid", userId);
        if (uid != null) {
            return uid;
        }
        return null;
    }

    public String getUserExtendinfByUid(Long userId) throws SQLException {
        String type = (String) this.getSqlMapClient().queryForObject("user.getUserExtendinfByUid", userId);
        return type;
    }

    public UserExtendInfo getUserExtendinfByUserId(long userId) throws SQLException {
        return (UserExtendInfo) this.getSqlMapClient().queryForObject("user.getUserExtendinfByUserId", userId);
    }

    public int updateUserExtendinf(Long userId, Map<String, Object> params) throws SQLException {
        params.put("userId", userId);
        int update = this.getSqlMapClient().update("user.updateUserExtendinf", params);
        return update;
    }

    public int updateUserCdk(Long userId, String cdk) throws SQLException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("cdk", cdk);
        return updateUserExtendinf(userId, sqlMap);
    }

    public int updateUserBindSongCard(Long userId, int payBindId) throws SQLException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("bindSongCard", payBindId);
        return updateUserExtendinf(userId, sqlMap);
    }

    /**
     * 提交用户更新
     *
     * @param userId
     * @param modify
     * @return
     * @throws SQLException
     */
    public int updateUser(Long userId, Map<String, Object> modify) {
        int update;
        try{
            modify.put("userId", String.valueOf(userId));
            update = this.getSqlMapClient().update("user.updateUser", modify);
        }catch (Exception e){
            update = -1;
            GameBackLogger.SYS_LOG.error("userId:"+userId+"|Exception-->"+e.getMessage() ,e);
        }
        return update;
    }

    /**
     * 提交用户更新
     *
     * @param user
     * @param modify
     * @return
     * @throws SQLException
     */
    public int updateUser(RegInfo user, Map<String, Object> modify) throws SQLException {
        modify.put("userId", user.getUserId());
        long time = TimeUtil.currentTimeMillis();
        int update = this.getSqlMapClient().update("user.updateUser", modify);
        long time1 = TimeUtil.currentTimeMillis() - time;
        if (time1 > 50) {
            GameBackLogger.SYS_LOG.info("user updateUser-->" + user.getUserId() + " time:" + time1);

        }
        ClassTool.setValue(user, modify);
        setToCache(user);
        return update;
    }

    public int updateUserBindPayId(Map<String, Object> params) throws SQLException {
        return this.getSqlMapClient().update("user.updateUserBindPayId", params);
    }

    public int addUserCards(RegInfo user, long cards, long freeCards, Map<String, Object> modify, CardSourceType sourceType) throws SQLException {
        return addUserCards(user, cards, freeCards, 0, modify, sourceType);
    }

    public int addUserCards(RegInfo user, long cards, long freeCards, long payBindId, CardSourceType sourceType) throws SQLException {
        return addUserCards(user, cards, freeCards, payBindId, null, sourceType);
    }

    public int addUserCards(RegInfo user, long cards, long freeCards, CardSourceType sourceType) throws SQLException {
        return addUserCards(user, cards, freeCards, 0, null, sourceType);
    }

    /**
     * 提交用户更新
     *
     * @param user
     * @param modify
     * @return
     * @throws SQLException
     */
    public int addUserCards(RegInfo user, long cards, long freeCards, long payBindId, Map<String, Object> modify, CardSourceType sourceType) throws SQLException {
        if (modify == null) {
            modify = new HashMap<String, Object>();
        }
        modify.put("userId", user.getUserId());
        modify.put("cards", cards);
        modify.put("freeCards", freeCards);
        if (payBindId != 0) {
            modify.put("payBindId", payBindId);
            user.setPayBindId((int) payBindId);
        }
        int update = this.getSqlMapClient().update("user.addUserCards", modify);
        setToCache(user);

        String showContent = "";

        // 推送
        Date now = TimeUtil.now();

        String payRemoveBind= (String) modify.get("payRemoveBind");
        if (0 == payBindId) {
            showContent = TimeUtil.formatTime(now) + " 您获得了:房卡x" + (freeCards + cards);
        } else {
            showContent = TimeUtil.formatTime(now) + " 您充值获得了:房卡x" + (freeCards + cards);
        }

        UserMessage info = new UserMessage();
        info.setTime(now);
        info.setType(1);
        info.setUserId(user.getUserId());
        info.setContent(showContent);
        UserMessageDao.getInstance().saveUserMessage(info);
        if (user.getEnterServer() != 0) {
            String str = GameUtil.sendPay(user.getEnterServer(), user.getUserId(), (int) (cards), (int) freeCards, info, "1", payRemoveBind);
            GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   " + "sendPay cards"+cards+" freeCards="+freeCards +" currency=1"+" payRemoveBind="+payRemoveBind +",result="+str);
        }
        if(cards + freeCards != 0) {
            long curCard = user.getCards() + cards;
            curCard = (curCard < 0) ? 0 : curCard;
            long curFreeCard = user.getFreeCards() + freeCards;
            curFreeCard = (curFreeCard < 0) ? 0 : curFreeCard;
            UserCardRecordDao.getInstance().insert(new UserCardRecordInfo(user.getUserId(), curCard, curFreeCard, (int)cards, (int)freeCards, 0, sourceType));
        }
        return update;
    }

    public int addUserGold(RegInfo user, long gold, long freeGold, long payBindId) throws SQLException {
        return addUserGold(user, gold, freeGold, payBindId,null);
    }

    /**
     * 提交用户金币更新
     */
    public int addUserGold(RegInfo user, long gold, long freeGold, long payBindId, Map<String, Object> modify) throws SQLException {
        if (modify == null) {
            modify = new HashMap<>();
        }
        modify.put("userId", user.getUserId());
        modify.put("gold", gold);
        modify.put("freeGold", freeGold);
//        if (payBindId != 0) {
//            modify.put("payBindId", payBindId);
//            user.setPayBindId((int) payBindId);
//        }
        int update = this.getSqlMapClient().update("gold.addUserGold", modify);
        setToCache(user);

        String showContent;

        // 推送
        Date now = TimeUtil.now();

        if (0 == payBindId) {
            showContent = TimeUtil.formatTime(now) + " 您获得了:金币x" + (freeGold + gold);
        } else {
            showContent = TimeUtil.formatTime(now) + " 您充值获得了:金币x" + (freeGold + gold);
        }

        UserMessage info = new UserMessage();
        info.setTime(now);
        info.setType(1);
        info.setUserId(user.getUserId());
        info.setContent(showContent);
        UserMessageDao.getInstance().saveUserMessage(info);
        if (user.getEnterServer() != 0) {
            String str = GameUtil.sendPay(user.getEnterServer(), user.getUserId(), (int) (gold), (int) freeGold, info, "2");
            GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   " + "sendPay gold"+gold+" golds="+freeGold +" currency=2" +",result="+str);
        }
        return update;
    }

    /**
     * 比赛场报名消耗房卡
     *
     * @param user
     * @param cards
     * @return
     */
    public int consumeUserCards(RegInfo user, int cards) {
        String resp = GameUtil.consumeUserCards(user.getEnterServer(), user.getUserId(), cards);
        JsonWrapper json = new JsonWrapper(resp);
        int code = -1;
        if (json != null) {
            code = json.getInt("code", -1);
        }
        return code;
    }

    /**
     * 查找用户支付返还总数
     *
     * @param flatid
     * @return
     * @throws SQLException PayBack
     * @throws
     */
    public PayBack getPayBackAmount(String flatid) throws SQLException {
        PayBack info = null;
        try {
            info = (PayBack) this.getSqlMapClient().queryForObject("user.getPayback", flatid);
        } catch (SQLException e) {
            throw e;
        }
        return info;
    }

    public void updatePayback(String flatid, String serverid) throws SQLException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("flatid", flatid);
        param.put("serverid", serverid);
        this.getSqlMapClient().update("user.updatePayback", param);
    }

    /**
     * 查找cdk领取信息
     *
     * @param ckdid
     * @return
     * @throws SQLException PayBack
     * @throws
     */
    public SystemCdk getSystemCdk(String ckdid) throws SQLException {
        SystemCdk info = null;
        try {
            info = (SystemCdk) this.getSqlMapClient().queryForObject("user.getSystemCdk", ckdid);
        } catch (SQLException e) {
            throw e;
        }
        return info;
    }

    public void updateSystemCdk(String flatid, int serverid, int cdkType, String cdkid) throws SQLException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("flatid", flatid);
        param.put("serverid", serverid);
        param.put("cdkType", cdkType);
        param.put("cdkid", cdkid);
        this.getSqlMapClient().update("user.updateSystemCdk", param);
    }

    @SuppressWarnings("unchecked")
    public List<IpGroup> getIpGroup() throws SQLException {
        return this.getSqlMapClient().queryForList("user.getIpGroup");
    }

    @SuppressWarnings("unchecked")
    public List<MacGroup> getMacGroup() throws SQLException {
        return this.getSqlMapClient().queryForList("user.getMacGroup");

    }

    /****************** Cache *******************/
    private String getCacheKey(String username, String pf) {
        StringBuilder sb = new StringBuilder();
        sb.append("qipai_user_");
        sb.append(username);
        sb.append(pf);
        return sb.toString();
    }

    private void setToCache(RegInfo userInfo) {
    }

    private RegInfo getFromCache(String username, String pf) {
//        Object obj = McClientFactory.getClient().get(getCacheKey(username, pf));
//        if (obj != null) {
//            return (RegInfo) obj;
//        }
        return null;
    }

    public int getLottyNum(long userId) {

        int number = 0;
        try {
            Object o = this.getSqlMapClient().queryForObject("user.getUsedCards", userId);
            int usedLotteryNum = (Integer) this.getSqlMapClient().queryForObject("lottery.usedLotteryNum", userId);

            String activityRange = PropertiesCacheUtil.getValue("activity_range",Constants.GAME_FILE);
            if (StringUtils.isNotBlank(activityRange)){
                String[] strs = activityRange.split("\\;");
                for (String str:strs){
                    if (StringUtils.isNotBlank(str)){
                        String[] temps=str.split("\\_");
                        if (temps.length==2){
                            number += UserShareDao.getInstance().countUserShare(userId,temps[0],temps[1]);
                        }
                    }
                }
            }

            if("1".equals(PropertiesCacheUtil.getValue("open_group_reward",Constants.GAME_FILE))){
                GroupUser groupUser = GroupDaoManager.getInstance().loadGroupUser(userId);
                if (groupUser!=null){
                    number+=1;
                }
            }

            if (null != o) {
                number += ((((Integer) o)< 76 ? 0 : 1) + 1);
            }
            number = number - usedLotteryNum;
            return number >= 0? number : 0;
        } catch (Exception e) {
            LogUtil.e("Exception:"+e.getMessage(),e);
        }

        return 0;
    }

    public int getLottyResult(long userId) {

        int lottyIndex = 1;
        //创建奖品
        List<String> prize = new ArrayList<>();
        List<Double> list = new ArrayList<>();
        try {
            List<Lottery> lotteries = SysInfManager.getInstance().initLottery();
            //读取奖品概率集合
            for (Lottery lottery : lotteries) {
                list.add(lottery.getChance());
                prize.add(lottery.getName());
            }
            LotteryUtil ll = new LotteryUtil(list);
            lottyIndex = ll.randomColunmIndex();
            int prizeSum = getPrizeSum();
            int firstPrizeSum = getFirstPrizeSum();
            int secondPrizeSum = getSecondPrizeSum();
            if((prizeSum+1)%300==0)lottyIndex=0;
            if((prizeSum+1)%999==0)lottyIndex=0;
            if (firstPrizeSum >= 999&&lottyIndex==7) lottyIndex = 0;
            if (secondPrizeSum >= 9999&&lottyIndex==3) lottyIndex = 0;

            if (lotteries.get(lottyIndex).getState()==0){
                lottyIndex = 0;
            }

            Lottery lottery = lotteries.get(lottyIndex);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", userId);
            int cards = lottery.getRoomCard();
            userMap.put("cards", lotteries.get(lottyIndex).getRoomCard());
            Map<String, Object> lotteryMap = new HashMap<>();
            lotteryMap.put("prize", prize.get(lottyIndex));
            lotteryMap.put("prizeIndex", lottyIndex);
            lotteryMap.put("userId", userId);
            int res1 = this.getSqlMapClient().update("lottery.addPrize", lotteryMap);
            String log = "getLottyResult-->userId:"+userId+",res1:"+res1;
            if (cards>0) {
                int res2 = this.getSqlMapClient().update("user.addRoomCard", userMap);
                log += ",res2:"+res2;
            }
            LogUtil.i(log);
            UserMessage message = new UserMessage();
            message.setUserId(userId);
            message.setContent("恭喜通过幸运转盘获得:" + lottery.getName());
            message.setTime(new Date());
            UserMessageDao.getInstance().saveUserMessage(message);
//            RegInfo user=(RegInfo)this.getUser(userId);
//            Map<String, Object> marqueeMap = new HashMap<String, Object>();
//            marqueeMap.put("content","恭喜"+user.getName()+"抽中"+prize.get(lottyIndex));
            /*this.getSqlMapClient().update("systemMarquee.insertMarquee",marqueeMap);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lottyIndex;
    }

    public int getPrizeSum() {

        int number = 0;
        try {
            number = (Integer) this.getSqlMapClient().queryForObject("lottery.prizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return number;
    }

    public int getFirstPrizeSum() {

        int number = 0;
        try {
            number = (Integer) this.getSqlMapClient().queryForObject("lottery.fistPrizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return number;
    }

    public int getSecondPrizeSum() {

        int number = 0;
        try {
            number = (Integer) this.getSqlMapClient().queryForObject("lottery.secondPrizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return number;
    }

    public int removeBindInfo(RegInfo user) {
        int res = 0;
        // 清除玩家的绑定
        Map<String, Object> modifyMap = new HashMap<>();
        try {
            modifyMap.put("payBindId", 0);
            res = updateUser(user, modifyMap);
            LogUtil.i("removeBindInfo-->userId:"+user.getUserId()+",payBindId:"+user.getPayBindId()+",res:"+res);
        } catch (SQLException e) {
            LogUtil.e("removeBindInfo err-->"+e);
        }
        return res;
    }

    public int saveOrUpdateUserGoldInfo(HashMap<String,Object> map){
        try {
            return this.getSqlMapClient().update("user.saveOrUpdateUserGoldInfo",map);
        } catch (SQLException e) {
            LogUtil.e("saveOrUpdateUserGoldInfo err-->"+e.getMessage(),e);
        }
        return -1;
    }

    public RegInfo getUserBySessCode(String userId, String sessCode) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(sessCode)) {
            return null;
        }
        RegInfo user = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("sessCode", sessCode);
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserBySessCode", sessCode);
        } catch (SQLException e) {
            LogUtil.e("getUserBySessCode|error|" + sessCode, e);
        }
        return user;
    }

    public Map<String,Object> loadUserBaseNoCache(String userId) throws SQLException {
        Map<String,Object> user;
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("userId", userId);
            user = (Map<String,Object>) this.getSqlMapClient().queryForObject("user.selectUserBaseMsgNoCache", param);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }


    /**
     * 获取短信验证实体类
     * @param userId
     * @return
     * @throws SQLException
     */
    public UserMsgVerify getMsgVerifyByUid(long userId) throws SQLException {
        return (UserMsgVerify) this.getSqlMapClient().queryForObject("user.getMsgVerifyByUid", userId);
    }

    public UserMsgVerify getMsgVerifyById(long id) throws SQLException {
        return (UserMsgVerify) this.getSqlMapClient().queryForObject("user.getMsgVerifyById", id);
    }

    public UserMsgVerify getMsgVerifyByIp(String ip) throws SQLException{
        return (UserMsgVerify) this.getSqlMapClient().queryForObject("user.getMsgVerifyByIp", ip);
    }

    public void updateUserMsgVerify(long userId,String phoneNum,String ip,int code)throws SQLException{
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("verifyCode", code+"");
        param.put("sendTime", new Date());
        param.put("phoneNum", LoginUtil.encryptPhoneNumAES(phoneNum));
        param.put("userId", userId);
        param.put("ip",ip);
        this.getSqlMapClient().update("user.updateUserMsgVerify", param);
    }

    public void updateSmsFlag(long userId,int isUse) throws Exception{
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);
        param.put("isUse",isUse);
        this.getSqlMapClient().update("user.updateSmsFlag",param);
    }

    public void deleteSms(long userId) throws Exception{
        this.getSqlMapClient().delete("user.deleteSms",userId);
    }

    public RegInfo getUserByPhoneNum(String phoneNum) throws Exception{
        RegInfo user = null;
        try {
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserByPhoneNum", phoneNum);
            if(user == null){
                user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserByPhoneNum", LoginUtil.encryptPhoneNumAES(phoneNum));
            }
            if (user != null)
                setToCache(user);
        } catch (SQLException e) {
            throw e;
        }
        return user;

    }

    public List<HashMap<String,Object>> loadAllUserPhoneNum(int pageNo , int pageSize) throws SQLException {
        Map<String, Object> map = new HashMap<>(8);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String,Object>>) this.getSqlMapClient().queryForList("user.loadAllUserPhoneNum",map);
    }


    public void addUserCoin(long userId, long coin, long freeCoin) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("coin", coin);
        map.put("freeCoin", freeCoin);
        this.getSqlMapClient().update("user.addUserCards", map);
    }

    /**
     * 查找用户
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public RegInfo getUserForceMaster(long userId) throws SQLException {
        RegInfo user = null;
        try {
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.getUserByIdForceMaster", userId);
            if (user != null)
                setToCache(user);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    public RegInfo loadUserByPhoneNum(String phoneNum) throws SQLException {
        RegInfo user;
        if (StringUtils.isBlank(phoneNum)) {
            return null;
        }
        try {
            StringJoiner sj = new StringJoiner(",");
            String phoneNumAES1 = LoginUtil.encryptPhoneNumAES(phoneNum);
            String phoneNumAES2 = LoginUtil.encryptPhoneNumAES(phoneNumAES1);
            sj.add("'" + phoneNum + "'");
            sj.add("'" + phoneNumAES1 + "'");
            sj.add("'" + phoneNumAES2 + "'");
            user = (RegInfo) this.getSqlMapClient().queryForObject("user.load_user_by_phoneNum", sj.toString());
            if (user != null && phoneNumAES2.equals(user.getPhoneNum())) {
                // 手机号被两次加密的bug
                Map<String, Object> modify = new HashMap<>();
                modify.put("phoneNum", phoneNumAES1);
                updateUser(user.getUserId(), modify);
                LogUtil.i("updatePhoneNum|" + user.getUserId() + "|" + phoneNum);
            }
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    public List<Long> loadAllUserIdByPhoneNum(String phoneNum) throws SQLException {
        if (StringUtils.isBlank(phoneNum)) {
            return null;
        }
        StringJoiner sj = new StringJoiner(",");
        String phoneNumAES1 = LoginUtil.encryptPhoneNumAES(phoneNum);
        String phoneNumAES2 = LoginUtil.encryptPhoneNumAES(phoneNumAES1);
        sj.add("'" + phoneNum + "'");
        sj.add("'" + phoneNumAES1 + "'");
        sj.add("'" + phoneNumAES2 + "'");
        return (List<Long>) this.getSqlMapClient().queryForList("user.load_all_userId_by_phoneNum", sj.toString());
    }


    public int batchUpdateUser(List<HashMap<String, Object>> list) throws Exception {
        SqlMapClient sqlMapClient = this.getSqlMapClient();
        sqlMapClient.startBatch();
        for (HashMap<String, Object> map : list) {
            this.getSqlMapClient().update("user.updateUser", map);
        }
        sqlMapClient.executeBatch();
        return 0;
    }

    public List<HashMap<String,Object>> loadAllUserPhoneNum(String userIdList) throws SQLException {
        if (StringUtils.isBlank(userIdList)) {
            return null;
        }
        return (List<HashMap<String,Object>>) this.getSqlMapClient().queryForList("user.load_all_user_phoneNum", userIdList);
    }

    public List getBindAllUserMsg(Long payBindId) throws SQLException {
        try {
            return getSqlMapClient().queryForList("user.getBindAllUserMsg", payBindId);
        } catch (SQLException e) {
            LogUtil.e("getBindAllUserMsg err-->" + e);
        }
        return null;
    }

    public Map getBindOneMsg(Long payBindId,Long userId) throws SQLException {
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("payBindId",payBindId);
            map.put("userId",userId);
            return (Map)this.getSqlMapClient().queryForObject("user.selectBindOneMsg", map);
        } catch (SQLException e) {
            LogUtil.e("getBindAllUserMsg err-->" + e);
        }
        return null;
    }

    public List<GroupDayConsumption> getBindAllGroupMsg(Integer payBindId,long dataDate) throws SQLException {
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("payBindId",payBindId);
            map.put("dataDate",dataDate);
            return (List<GroupDayConsumption>)this.getSqlMapClient().queryForList("user.getBindAllGroupMsg", map);
        } catch (SQLException e) {
            LogUtil.e("getBindAllGroupMsg err-->" + e);
        }
        return null;
    }

    public int updateUserCreateGroup(Long armId,Long payBindId) throws Exception {
        Map<String,Object> map=new HashMap<>();
        map.put("payBindId",payBindId);
        map.put("userId",armId);
        this.getSqlMapClient().update("user.updateUserCreateGroup", map);
        return 0;
    }

    public int updateGroupId(int beforeId,int afterId) throws Exception {
        Map<String,Object> map=new HashMap<>();
        map.put("beforeId",beforeId);
        map.put("afterId",afterId);
        return this.getSqlMapClient().update("user.updateGroupId", map);
    }

    public int updateGroupAllUserId(int beforeId,int afterId) throws Exception {
        Map<String,Object> map=new HashMap<>();
        map.put("beforeId",beforeId);
        map.put("afterId",afterId);
        this.getSqlMapClient().update("user.updateGroupAllUserId", map);
        return 0;
    }

    public int updatebindInviteId(Integer payBindId,long userId) throws SQLException {
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("payBindId",payBindId);
            map.put("userId",userId);
            return this.getSqlMapClient().update("user.updateInviteId", map);
        } catch (SQLException e) {
            LogUtil.e("updateInviteId err-->" + e);
        }
        return 0;
    }

    public Long selectGroupBindId(Integer groupId) throws SQLException {
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("groupId",groupId);
            return (Long)this.getSqlMapClient().queryForObject("user.selectGroupBindId", map);
        } catch (SQLException e) {
            LogUtil.e("getBindAllGroupMsg err-->" + e);
        }
        return null;
    }

    public RegInfo getUserByAccName(String accName) throws SQLException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("accName", accName);

        RegInfo user =  (RegInfo) this.getSqlMapClient().queryForObject("user.getUserByAccName", param);
        if (user != null)
            setToCache(user);

        return user;
    }

}
