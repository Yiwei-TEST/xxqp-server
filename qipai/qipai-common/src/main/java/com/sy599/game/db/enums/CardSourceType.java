package com.sy599.game.db.enums;

/**
 * 房卡来源类型
 */
public enum CardSourceType {
    unknown(0, "未知来源"),
    daikaiTable_AA(1, "代开牌桌AA支付"),
    daikaiTable_FZ(2, "代开牌桌房主支付"),
    commonTable_AA(3, "普通牌桌AA支付"),
    commonTable_FZ(4, "普通牌桌房主支付"),
    groupTable_AA(5, "亲友圈牌桌AA支付"),
    groupTable_FZ(6, "亲友圈牌桌房主支付"),
    groupTable_QZ(7, "亲友圈牌桌群主支付"),
    groupTable_diss_FZ(8, "亲友圈牌桌解散房主支付"),
    groupTable_diss_QZ(9, "亲友圈牌桌解散群主支付"),
    daikaiTable_diss_QZ(10, "代开牌桌解散群主支付"),
    daikaiTable_diss_FZ(11, "代开牌桌解散房主支付"),
    goldExchangeCard(12, "积分兑换钻石"),
    cardExchangeGold(13, "钻石兑换积分"),
    cardExchangeTili(14, "钻石兑换体力"),
    sign(15, "每日签到"),
    activity_smashEgg(16, "砸金蛋活动"),
    activity_luckRedBag(17, "转盘抽奖活动"),
    activity_consumeDiam(18, "开房送钻活动"),
    activity_drawLottery(19, "大转盘活动"),
    activity_oldPlayerBack(20, "老玩家回归活动"),
    activity_inviteUser(21, "邀请好友送钻"),
    activity_oldDaiNew(22, "老带新活动"),
    activity_share(23, "分享送钻活动"),
    activity_invite(24, "打筒子邀请活动"),
    activity_oldBackGift(25, "回归礼包活动"),
    activity_newPlayerGift(26, "新人有礼活动"),
    activity_firstMyth(28, "封神榜活动"),
    activity_dididache(29, "滴滴打车活动"),
    activity_RedBag(30, "小甘瓜分现金红包活动"),
    match_recover(31,"比赛场复活"),
    match_award(32,"比赛场奖励"),
    match_diss(33, "比赛场解散"),
    daikaiTable_clear_returnCard(34, "清理代开房间"),
    bindGiveRoomCards(35, "玩家绑码返还代理钻石"),
    receive_cdk(36, "领取兑换码cdk"),
    receive_rank_reward(37, "领取排行榜奖励"),
    yypt_send_card(38, "运营平台发送房卡"),
    user_pay(39, "玩家充值"),
    drawPrize(40, "百万大奖领奖"),
    payFirstAward(41, "领取首充奖励"),
    openLucky(42, "开启福袋"),
    match_fee_card(43, "比赛场收取报名费"),
    match_fee_card_refund(44, "比赛场退还报名费"),
    ticket_card(45, "礼券兑换钻石"),
    ticket_gold(46, "礼券兑换金币"),
    activity_gold_room_share(47, "欢乐金币场局数奖励活动"),
    bjd_changeCards(48, "白金岛修改用户钻石"),
    jiangnan_sendCards(49, "江南棋牌赠送钻石"),
    ;
    private int sourceType;

    private String sourceName;

    CardSourceType(int sourceType, String sourceName) {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
    }

    public int getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

}
