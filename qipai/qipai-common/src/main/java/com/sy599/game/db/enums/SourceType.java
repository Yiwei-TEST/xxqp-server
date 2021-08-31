package com.sy599.game.db.enums;


public enum SourceType {
    // 1-10000 给server机
    // 10001 - 20000 给登录机
    // 20001 - 30000 给代理后台
    unknown(-1, "未知来源"),
    table_ticket(1, "门票"),
    table_win(2, "牌桌输赢"),
    goldExchangeCard(3, "金币兑换钻石"),
    cardExchangeGold(4, "钻石兑换金币"),
    sign(5, "每日签到"),
    group_commission(6, "亲友圈抽水"),
    solo_room(7, "soloRoom"),
    broke_award(8, "破产补助"),
    broke_share(9, "破产分享"),
    day_mission(10, "每日任务"),
    AA_Gold(11, "AA支付"),
    groupTableGoldRoom(12, "亲友圈金币房"),
    AA_Gold_Return(13, "AA支付退款"),
    activeLZ(14, "龙舟活动"),
    DuanWuActivityGoldRoom(15,"端午粽子排行活动"),
	COMPETITION_PLAYING(16,"比赛场资源奖励"),
	COMPETITION_PLAYING_TICKER(17,"比赛场资源门票"),
    video_award(18,"视频奖励"),
    goldRoomGiftCert_award(19,"非亲友圈金币场礼券奖励"),
    video_sign(20,"视频签到"),
    video_broke(21,"破产视频奖励"),
    Twin_award(22,"累计胜利奖励"),
    goldRoomXjsWatchAds_reward(23,"金币场小结算观看广告白金豆奖励"),
    twin_video_award(24,"累计胜利视频奖励"),
    COMPETITION_PLAYING_AWARD2(25,"赛场礼券奖励"),
    goldRoom_robot_total_lose(26,"机器人总累计输的金币数"),
    goldRoom_robot_total_win(27,"机器人总累计赢的金币数"),
    goldRoom_robot_playType_lose(28,"机器人玩法累计输的金币数"),
    goldRoom_robot_playType_win(29,"机器人玩法累计赢的金币数"),
    goldRoom_robot_replenish(30,"机器人补金币数"),
    Activity7xi(31,"2020七夕活动排行奖励金币"),
    Activity7xiItem(32,"2020七夕活动集7统计"),
    activeQueQiao(33, "鹊桥活动"),
    activeQueQiaoVideo(34, "鹊桥活动视频领奖"),
    giftCertQueQiao(35, "鹊桥礼券")
	;


    private int type;

    private String name;

    SourceType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int type() {
        return type;
    }

    public String getSourceName() {
        return name;
    }

}
