package com.sy599.game.qipai.wzq.bean;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.wzq.command.WzqCommandProcessor;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.constants.GroupConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WzqPlayer extends Player {

    /*** 座位 ***/
    private int seat;

    /*** 状态:1进入 2已准备 3正在玩 4已结束 ***/
    private player_state state;

    /*** 牌局是否在线 1离线 2在线 ***/
    private int isEntryTable;

    /*** 赢的次数 ***/
    private int winCount;

    /*** 输的次数 ***/
    private int lostCount;

    /*** 局分 ***/
    private int lostPoint;

    /*** 大局分 ***/
    private int point;

    /*** 颜色 ：-1：黑方、1：白方***/
    private int color;

    /*** 使用分：1：信用分、2：棋分***/
    private int costType;


    public WzqPlayer() {

    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        changeTableInfo();
    }

    public int getCostType() {
        return costType;
    }

    public void setCostType(int costType) {
        this.costType = costType;
        changeTableInfo();
    }

    public String toExtendStr() {
        StringBuffer sb = new StringBuffer();
        return sb.toString();

    }

    public void initExtend(String info) {
        if (StringUtils.isBlank(info)) {
            return;
        }
    }

    public String toInfoStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(getUserId()).append(",");
        sb.append(seat).append(",");

        if (state != null) {
            sb.append(state.getId()).append(",");
        } else {
            sb.append(0).append(",");
        }

        sb.append(isEntryTable).append(",");
        sb.append(winCount).append(",");
        sb.append(lostCount).append(",");
        sb.append(point).append(",");
        sb.append(getTotalPoint()).append(",");
        sb.append(lostPoint).append(",");
        sb.append(costType).append(",");
        sb.append(color).append(",");
        return sb.toString();
    }

    @Override
    public void initPlayInfo(String data) {
        if (!StringUtils.isBlank(data)) {
            int i = 0;
            String[] values = data.split(",");
            long uid = StringUtil.getLongValue(values, i++);
            if (uid != getUserId()) {
                return;
            }
            this.seat = StringUtil.getIntValue(values, i++);
            int stateVal = StringUtil.getIntValue(values, i++);
            this.state = SharedConstants.getPlayerState(stateVal);
            this.isEntryTable = StringUtil.getIntValue(values, i++);
            this.winCount = StringUtil.getIntValue(values, i++);
            this.lostCount = StringUtil.getIntValue(values, i++);
            this.point = StringUtil.getIntValue(values, i++);
            setTotalPoint(StringUtil.getIntValue(values, i++));
            this.lostPoint = StringUtil.getIntValue(values, i++);
            this.costType = StringUtil.getIntValue(values, i++);
            this.color = StringUtil.getIntValue(values, i++);
        }
    }

    public player_state getState() {
        return state;
    }

    public void changeState(player_state state) {
        this.state = state;
        changeTableInfo();
    }

    public int getIsEntryTable() {
        return isEntryTable;
    }

    public void setIsEntryTable(int isEntryTable) {
        this.isEntryTable = isEntryTable;
        changeTbaleInfo();
    }

    @Override
    public int getSeat() {
        return seat;
    }

    @Override
    public void setSeat(int seat) {
        this.seat = seat;
        changeTableInfo();
    }

    public PlayerInTableRes.Builder buildPlayInTableInfo() {
        return buildPlayInTableInfo(false);
    }

    /**
     * 吃碰杠过的牌
     *
     * @return
     */
    public List<Integer> getMoldIds() {
        List<Integer> list = new ArrayList<>();
        return list;
    }

    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        return list;
    }

    /**
     * @param isrecover 是否重连
     * @return
     */
    public PlayerInTableRes.Builder buildPlayInTableInfo(boolean isrecover) {
        PlayerInTableRes.Builder playerMsg = PlayerInTableRes.newBuilder();
        playerMsg.setUserId(userId + "");
        playerMsg.setName(name);
        playerMsg.setSeat(seat);
        playerMsg.setSex(sex);
        playerMsg.setIp(StringUtils.isNotBlank(ip) ? ip : "");
        playerMsg.setIcon(StringUtils.isNotBlank(getHeadimgurl()) ? getHeadimgurl() : "");
        playerMsg.setPoint(getTotalPoint() + getLostPoint());
        playerMsg.addAllMoldIds(getMoldIds());
        playerMsg.addAllMoldCards(buildDisCards(userId));
        WzqTable table = getPlayingTable(WzqTable.class);
        if (state == player_state.ready || state == player_state.play) {
            // 玩家装备已经准备和正在玩的状态时通知前台已准备
            playerMsg.setStatus(SharedConstants.state_player_ready);
        } else {
            playerMsg.setStatus(0);
        }

        //信用分
        GroupUser gu = getGroupUser();
        if (table.isCreditTable()) {
            String groupId = table.loadGroupId();
            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
            }
        }
        playerMsg.setCredit(gu != null ? gu.getCredit() : 0);
        playerMsg.setScore(gu != null ? gu.getScore() < GroupConstants.DEFAULT_SCORE ? GroupConstants.DEFAULT_SCORE : gu.getScore() : GroupConstants.DEFAULT_SCORE);
        playerMsg.addExt(costType);
        playerMsg.addExt(color);
        playerMsg.addExt(table.getMasterId() == userId ? 1 : 0);
        return buildPlayInTableInfo1(playerMsg);
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLostCount() {
        return lostCount;
    }

    public void setLostCount(int lostCount) {
        this.lostCount = lostCount;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setLostPoint(int lostPoint) {
        this.lostPoint = lostPoint;
        changeTbaleInfo();
    }

    public int getLostPoint() {
        return lostPoint;
    }

    public void changePoint(int point) {
        this.point += point;
        myExtend.changePoint(getPlayingTable().getPlayType(), point);
        changeTotalPoint(point);
        if (point > getMaxPoint()) {
            setMaxPoint(point);
        }
        changeTbaleInfo();
    }

    public void clearTableInfo() {
        setIsEntryTable(0);
        changeState(null);
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setSeat(0);
        setPlayingTableId(0);
        setColor(0);
        setCostType(0);
        setWinLoseCredit(0);
        saveBaseInfo();
    }

    /**
     * 单局详情
     *
     * @return
     */
    public ClosingPlayerInfoRes.Builder buildOneClosingPlayerInfoRes() {
        ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.setName(name);
        res.setSeat(seat);
        res.setPoint(point);
        res.setTotalPoint(getTotalPoint());
        res.setWinCount(getWinCount());
        res.setLostCount(getLostCount());
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        return res;
    }

    /**
     * 总局详情
     *
     * @return
     */
    public ClosingPlayerInfoRes.Builder buildTotalClosingPlayerInfoRes() {
        ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.setName(name);
        res.setSeat(seat);
        res.setPoint(point);
        res.setTotalPoint(getTotalPoint());
        res.setWinCount(getWinCount());
        res.setLostCount(getLostCount());
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        return res;
    }

    public void changeTbaleInfo() {
        BaseTable table = getPlayingTable();
        if (table != null)
            table.changePlayers();
    }

    @Override
    public void initNext() {
        setPoint(0);
        setLostPoint(0);
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
    }

    @Override
    public List<Integer> getHandPais() {
        return null;
    }

    @Override
    public void initPais(String handPai, String outPai) {
    }


    @Override
    public void endCompetition1() {

    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_wzq);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, WzqCommandProcessor.getInstance());
        }
    }

}
