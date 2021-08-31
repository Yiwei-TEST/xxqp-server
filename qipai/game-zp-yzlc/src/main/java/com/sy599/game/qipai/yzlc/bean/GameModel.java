package com.sy599.game.qipai.yzlc.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.sy599.game.qipai.yzlc.constant.HuType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Guang.OuYang
 * @date 2019/9/4-14:08
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameModel {
    //游戏局数 5/6/8/10
    private int gameFinishRound;
    //游戏遇到最大胡息总结算 解散房间
    private int gameFinishMaxHuXi;
    //游戏最大人数 2/3
    private int gameMaxHuman;
    //支持方式 1AA 2房主
    private int payType;
    //抽取底牌数量 0/10/20
    private int discardHoleCards;
    //单局最低胡息起胡 10/15
	//放戳：即宣布自己胡牌。2人场和3人场当自己面前的“戳子”张数大于等于14张或15张时（4人场为11张），即可宣布胡牌。胡牌后此局结束，胡牌时以面前的“戳子”数目作为自己胡牌的大小，比如面前有14张牌为14戳，有18张牌为18戳。注意宣布胡牌时牌权要在自己手里，比如本来面前戳子数目已有14戳，但又打出一手牌，结果被别的玩家大了，这时不能宣布胡牌，必须再次拿到牌权才能宣布胡牌。
    private int roundFinishLowestHuXi;
    //支持的特殊玩法 1自摸+3胡,2一五十,3 30胡翻倍,4黄庄5胡,5一点红,6天地胡,7大小字,8碰碰胡,9红黑胡,10名堂叠加,11吃边打边
    private SpecialPlay specialPlay = new SpecialPlay();
    //0随机 1先进房坐庄
    private int changeBankerWay;
//    //自动托管0无, 60s后, 120s后, 180s后, 300s后
//    private int autoOutCard;
    //加倍0不加倍,1加倍
    private int doubleChip;
    //加倍生效分, 1小于10分时加倍
    private int doubleChipLeChip;
    //加倍倍数1翻两倍 3翻三倍 4翻四倍
    private int doubleRatio;
    //低于多少号分阈值
    private int lowScoreLimit;
    //低于多少分+多少分
    private int lowScoreAdd;
    //底分加成, 这里的加成用于总胡息-最低胡牌胡息后多增加得到的胡息数
    private int basicSocreAdd;
	//16玩法 0曲戳 1定戳
	//定戳：胡牌时无论戳子数多少均计基本分1分。如果勾选起胡2分则胡牌时计基本分2分。
	//曲戳：每多1戳多1分。比如三人场14戳起胡1分，则15戳记基本分2分，16戳记基本分3分，17戳记基本分4分，以此类推。
    private int calcScoreType;


    /**
     * @param currentScore 当前分
     * @return 1不加倍, 其他加倍
     * @description 检测和返回当前可用的倍数,
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    @JSONField(serialize = false)
    public int doubleChipEffect(int currentScore) {
        return Math.max(isDoubleChip() && Math.abs(currentScore) < doubleChipLeChip ? doubleRatio : 1, 1);
    }

    @JSONField(serialize = false)
    public int lowScoreEffect(int currentScore) {
        return lowScoreLimit > 0 && Math.abs(currentScore) < lowScoreLimit ? currentScore > 0 ? lowScoreAdd : -lowScoreAdd : 0;
    }

    @JSONField(serialize = false)
    public boolean isDoubleChip() {
        return doubleChip == 1;
    }

    //番戳必须达到的戳子数量条件
	public int getDoubleChuoHuXi() {
		return getGameMaxHuman() == 4 ? 15 : 18;
	}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialPlay {
        //名堂叠加
        private boolean repeatedEffect;
        //红黑胡
        private boolean redBlackHu;
		//20红戳4番 1是
		private int redChuoNFan;
		//18见红加分 1是
		private boolean redAddScore;
		//21番戳 2人和3人场当戳子数大于等于18戳时（4人场为15戳时）称为“番戳”，基本分大为提高。18戳记基本分14分，每多1戳多2分。19戳记基本分16分，20戳记基本分18分，以此类推。
		private boolean doubleChuo;
		//一五十
		private boolean oneFiveTen;
		//起胡2分 19起胡2分：起胡时计基本分2分
		private boolean hu2Score;

    }
}
