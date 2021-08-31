package com.sy599.game.qipai.tjmj.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.tjmj.bean.*;
import com.sy599.game.qipai.tjmj.constant.MjAction;
import com.sy599.game.qipai.tjmj.constant.MjConstants;
import com.sy599.game.qipai.tjmj.rule.*;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author liuping
 */
public class MjTool {


    public static synchronized List<List<Mj>> fapai(List<Integer> copy, int playerCount) {
        List<List<Mj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<Mj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Mj.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 14)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(14 + (i - 1) * 13, 14 + (i - 1) * 13 + 13)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(14 + (i) * 13, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<Mj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<Mj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<Mj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(MjHelper.find(copy, zp));
            }
        }
        List<Mj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Mj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<Mj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<Mj> pai = zpList.get(i);
                    int len = 13 - pai.size();
                    pai.addAll(allMjs.subList(count, count + len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, count + 13)));
                    count += 13;
                }
            }
            if (i == playerCount - 1) {
                if (zpList.size() > i + 1) {
                    List<Mj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }


//	/**
//	 * 麻将胡牌
//	 *
//	 * @param majiangIds
//	 * @param playType
//	 * @return
//	 */
//	public static boolean isHu(List<CsMj> majiangIds, int playType) {
//		if (playType == ZZMajiangConstants.play_type_zhuanzhuan) {
//			return isHuZhuanzhuan(majiangIds);
//
//		} else if (playType == ZZMajiangConstants.play_type_changesha) {
//			// return isHu(majiangIds);
//		} else if (playType == ZZMajiangConstants.play_type_hongzhong) {
//			return isHuZhuanzhuan(majiangIds);
//		}
//
//		return false;
//	}

    /**
     * 麻将胡牌
     * <p>
     * *@param handCardIds 手牌
     * *@param gang 杠的牌
     * *@param peng 碰的牌
     * *@param chi 吃的牌
     * *@param buzhang 补张
     * *@param isBegin 游戏开局
     * *@param jiang258 需要258做将
     * *@param table
     * *@param huMj 胡哪张牌
     * *@return
     *
     * @return 0平胡 1 碰碰胡 2将将胡 3清一色 4双豪华7小对 5豪华小对 6:7小对 7全求人 8大四喜 9板板胡 10缺一色 11六六顺
     */
    public static MjiangHu isHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isbegin, boolean jiang258, Mj huMj, TjMjTable table, TjMjPlayer player, int subKingNum) {
        return isHu(handCardIds, gang, peng, chi, buzhang, isbegin, jiang258, table, huMj, player, false, subKingNum);
    }


    /**
     * @param handCardIds 手牌
     * @param gang        杠的牌
     * @param peng        碰的牌
     * @param chi         吃的牌
     * @param buzhang     补张
     * @param isBegin     游戏开局
     * @param jiang258    需要258做将
     * @param table
     * @param huMj        胡哪张牌
	 * @param haveKingIce 存在不能用的王牌
	 * @param subKingNum  有没有打出来的王,打出来的王不能再变  -1有, 0所有王都可用, >0其他数量的王不能用
	 *
     * @return
     * @description 胡牌规则
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public static MjiangHu isHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isBegin, boolean jiang258, TjMjTable table, Mj huMj, TjMjPlayer player, boolean haveKingIce, int subKingNum, boolean noYingZhuangNoHu) {
		return table.getGameModel().isEightKing() ?
				eight8KingHu(handCardIds, gang, peng, chi, buzhang, isBegin, jiang258, table, huMj, player, haveKingIce,subKingNum, noYingZhuangNoHu) :
				four4KingHu(handCardIds, gang, peng, chi, buzhang, isBegin, jiang258, table, huMj, player, haveKingIce, subKingNum);
	}

    public static MjiangHu isHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isBegin, boolean jiang258, TjMjTable table, Mj huMj, TjMjPlayer player, boolean haveKingIce, int subKingNum) {
		return isHu(handCardIds, gang, peng, chi,  buzhang, isBegin, jiang258, table, huMj, player, haveKingIce, subKingNum, true);
	}
		
	/**
	 *@description 8王玩法
	 * 15. 8 王玩法
	 * ①天胡：手中有 4 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带 平 2+1，天胡+胡牌番型（炮胡不算天胡）
	 * ②天天胡：手中有 5 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天 胡带平 4+1，天胡+胡牌番型（炮胡不算天胡）
	 * ③天天天胡：手中有 6 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天 天天胡带平 6+1，天胡+胡牌番型（炮胡不算天胡）
	 * ④地胡：手中有 3 张“地牌”即可胡牌,若有其他牌型算地胡+胡牌番型,接炮不算地胡
	 * ⑤硬庄：胡牌时手中无“王”或“王”未替代其他任何牌使用,算硬庄+胡牌番型,（可炮胡）
	 * 4.针对八王玩法规则
	 * ①除硬庄和抢杠胡牌型外，任何有“王”代替的牌型，都不能炮胡。
	 * ②无硬庄天胡、硬庄天天胡、硬庄天天天胡，这三个牌型，当牌型冲突时按分高的牌型算分。
	 *@param
	 *@return 
	 *@author Guang.OuYang
	 *@date 2020/2/18
	 */
	private static MjiangHu eight8KingHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isBegin, boolean jiang258, TjMjTable table, Mj huMj, TjMjPlayer player, boolean haveKingIce, int subKingNum, boolean noYingZhuangNoHu) {
		MjiangHu hu = new MjiangHu();
		if (handCardIds == null || handCardIds.isEmpty()) {
			return hu;
		}

		boolean isZimo = isZimo(table, huMj, player);

		Mj floorCard = player.getPlayingTable(TjMjTable.class).getFloorCard();

		int kingN = sumAndGet(table.getKingCard(), handCardIds);

		//8王
		if (table.getGameModel().isEightKing()) {
			kingN += sumAndGet(table.getKingCard2(), handCardIds);
		}

		int kingNum = isZimo ? kingN : 0;

		//自行计算
		if (subKingNum == -1 && haveKingIce) {
			subKingNum = kingN -
					((sumAndGet(table.getKingCard(), player.getHandMajiang())) +
					(sumAndGet(table.getKingCard2(), player.getHandMajiang())));
		}

		int floorN = sumAndGet(floorCard, handCardIds);
		int floorNum = isZimo ? floorN : 0;

		//地胡, 能地胡, 则胡后如果想有地胡门子,必须包含当前能胡牌的门子
		MjiangHu diHuTemp = new MjiangHu();

		//地胡,移除地牌后是否依然能胡
		boolean canDiHu = isCanDiHu(diHuTemp, handCardIds, gang, peng, chi, buzhang, jiang258, table, player, floorN, true, subKingNum);

		//硬庄和天胡同时出现,需要校验王牌是否拆了
		//1.拆了不算硬庄, 算天胡
		boolean canTianHu = isCanTianHu(diHuTemp, handCardIds, gang, peng, chi, buzhang, jiang258, table, player, kingN, true, subKingNum);

		//⑤硬庄：胡牌时手中无“王”或“王”未替代其他任何牌使用,算硬庄+胡牌番型,（可炮胡）
		boolean canYingZhuang = table.getGameModel().getSpecialPlay().isFloorHu()/* && (kingCards == 0 || player.getKingCardNumHuFlag() >= table.getGameModel().getSpecialPlay().getFloorHuNum())*/;

		//能硬庄检测有限硬庄检测
		isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu, canYingZhuang, subKingNum);

		if (hu.isHu()) {
			LogUtil.printDebug("硬庄胡了:{}", dahuListToString(hu.buildDahuList()));

			//这里当王只有两张的时候,平胡是强行平胡的,所以这里要单独拎出来验证
			boolean falseYingZhuang = table.getKingCard() != null
					&& !CollectionUtils.isEmpty(handCardIds)
					&& handCardIds.size() == 2
					&& (((MjRule.isKingCard(handCardIds.get(0), new Mj[] { table.getKingCard(), table.getKingCard2() }))
							|| (MjRule.isKingCard(handCardIds.get(1), new Mj[] { table.getKingCard(), table.getKingCard2() })))
					&& (handCardIds.get(0).getVal() == handCardIds.get(1).getVal()));

			//剩余只有两张王牌一致时, 有特殊牌型无需258做将的算硬庄
			boolean yingZhuang = table.isKingCard(handCardIds.get(0))
					&& table.isKingCard(handCardIds.get(1))
					&& handCardIds.get(0).getVal() == handCardIds.get(1).getVal()
					&& (hu.isQingyiseHu());

			//这里里层的硬庄一定是王牌等于两张时的校验结果||这个位置理论上一定为硬庄,但是当牌为两张内层校验又不通过的时候,这里也不给通过
			hu.setYingZhuang(hu.isYingZhuang() || (handCardIds.size() != 2 && !falseYingZhuang) || yingZhuang);

			hu.initDahuList();

			//检测地胡和其他门子一致
			hu = checkDiHuMenZiEq(table, player, hu, diHuTemp, canDiHu);

			//天胡&天天胡
			checkTianHu(table, hu, kingNum);

			//地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
			//天胡需要满足胡牌牌型,光王牌无法接炮和自摸
			// 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
			// 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡
			//地胡&地地胡
			checkDihu(table, hu, floorNum, player);

			//天胡和硬庄同时出现,同时检测到王牌拆了,移除硬庄
			//8王,将将胡与硬庄共存
			if(!hu.isJiangjiangHu())
				checkRemoveYingZhuang(hu, canTianHu);

			//1.拆除地牌后不能胡牌,移除地胡
			//2.只有地胡能胡牌,这里不移除
			//3.存在7小对,特殊处理,不移除
			checkRemoveDiHu(hu, canDiHu);

			//天胡和硬庄互斥
			if (table.getGameModel().isEightKing() && (hu.isTianhu() || hu.isTiantianhu() || hu.isTiantiantianhu()) && hu.isYingZhuang()) {

				if (hu.isJiangjiangHu() && (table.getKingCard().isJiang() || table.getKingCard2().isJiang())) {
					//王没变或没王算硬庄，（同4王一样），硬庄与天胡不是一定互斥，有且只有一种情况不互斥：4个王为将当本牌使用，为将将胡+硬庄+天胡（这里特殊处理）
				}else{
					hu.setYingZhuang(false);
					hu.initDahuList();

					MjiangHu noTianHu = hu.clone();

					//计算没有硬庄的分数
					int noYingZhuangScore = MjiangHu.calcMenZiScore(table, hu.getDahuList(), player.noNeedMoCard());

					noTianHu.setYingZhuang(true);
					noTianHu.setTianhu(false);
					noTianHu.setTiantianhu(false);
					noTianHu.setTiantiantianhu(false);
					noTianHu.initDahuList();
					//计算没有天胡的分数
					int noKingScore = MjiangHu.calcMenZiScore(table, noTianHu.getDahuList(), player.noNeedMoCard());

					//天胡总分大于带硬庄总分
					if (noKingScore > noYingZhuangScore) {
						hu = noTianHu;
					}
				}
			}

			hu.initDahuList();
		}

		if (canYingZhuang/* && !haveKingIce*/) {
			int addYingZhuangScore = 0;
			MjiangHu hu2 = new MjiangHu();

			if (hu.isHu()) {
				//算入硬庄的分数
				addYingZhuangScore = MjiangHu.calcMenZiScore(table, hu.getDahuList(), player.noNeedMoCard());
			}

			//非硬庄检测
			isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu2, false, subKingNum);

			hu2.initDahuList();

			if(hu2.isHu()){
				diHuTemp = new MjiangHu();
				canDiHu = isCanDiHu(diHuTemp, handCardIds, gang, peng, chi, buzhang, jiang258, table, player, floorN, false, subKingNum);

				//检测地胡和其他门子一致
				hu2 = checkDiHuMenZiEq(table, player, hu2, diHuTemp, canDiHu);

				//天胡&天天胡
				checkTianHu(table, hu2, kingNum);

				//地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
				//天胡需要满足胡牌牌型,光王牌无法接炮和自摸
				// 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
				// 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡
				//地胡&地地胡
				checkDihu(table, hu2, floorNum, player);

				//1.拆除地牌后不能胡牌,移除地胡
				//2.只有地胡能胡牌,这里不移除
				//3.存在7小对,特殊处理,不移除
				checkRemoveDiHu(hu2, canDiHu);

				hu2.initDahuList();
			}

			//算入王的分数
			int notYingZhuangScore = MjiangHu.calcMenZiScore(table, hu2.getDahuList(), player.noNeedMoCard());

			LogUtil.printDebug("算入硬庄分数:{},门子:{},不算入硬庄分数:{},非硬庄门子:{}", addYingZhuangScore, dahuListToString(hu.getDahuList()), notYingZhuangScore, dahuListToString(hu2.getDahuList()));

			if (addYingZhuangScore < notYingZhuangScore) {
				hu = hu2;
			}

			LogUtil.printDebug("硬庄分:{},非硬庄分:{}, 最终门子:{}", addYingZhuangScore, notYingZhuangScore, dahuListToString(hu.buildDahuList()));
		}

		//八王不管手上几个王，只要变牌了，有门子也不能接炮
		if(noYingZhuangNoHu && (!isZimo) && !hu.isYingZhuang() && hu.isHu()){
			LogUtil.msg("8king,noyingzhuang,notselfmo.checkpinghu.");
			hu = new MjiangHu();
			if (isPingHu(handCardIds, jiang258, subKingNum,  null, null)) {
				hu.setPingHu(true);
				hu.setHu(true);
				hu.setYingZhuang(true);
				LogUtil.msg("8king,noyingzhuang,notselfmo.checkpinghu1.");
			}
		}

		//没得胡, 检测是不是有地胡直接倒
		if(!hu.isHu() && player.getGang().isEmpty()) {
			//天胡&天天胡
			checkTianHu(table, hu, kingNum);

			//地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
			//天胡需要满足胡牌牌型,光王牌无法接炮和自摸
			// 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
			// 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡
			//地胡&地地胡
			checkDihu(table, hu, floorNum, player);
		}

		//当前没地胡, 但是实际存在地胡
		//1.检测当前不带地胡的门子比较大, 还是地胡门子大
		if (hu.isHu() && !hu.isDihu() && !hu.isDidihu() && table.getGameModel().getSpecialPlay().isFloorHu() && floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum()) {
			MjiangHu onlyDiHu = new MjiangHu();
			checkDihu(table, onlyDiHu, floorNum, player);
			onlyDiHu.initDahuList();
			hu.initDahuList();
			if (MjiangHu.calcMenZiScore(table, onlyDiHu.buildDahuList(), table.getGameModel().getSpecialPlay().selfMoEqGunHu()) > MjiangHu.calcMenZiScore(table, hu.buildDahuList(), table.getGameModel().getSpecialPlay().selfMoEqGunHu())) {
				hu = onlyDiHu;
			}
		}

		//倒底胡,不与黑天胡叠加,其余叠加
		//庄家开局14张就可以胡牌，闲家没有倒底胡。
		if (table.getGameModel().getSpecialPlay().isOpeningHu() && hu.isHu() && !hu.isHeitianhu() && table.getDisCardRound() == 0 && isBegin && !hu.isDihu() && !hu.isDidihu()) {
			hu.setDaodihuh(true);
			//当前操作是为了让倒底胡因子一直跟随某一种操作
		}else if (!CollectionUtils.isEmpty(player.getDahu()) && player.getDahu().contains(12) && (table.getDisCardRound() <= 1)) {
			hu.setDaodihuh(true);
		}

		if (hu.isHu()) {
			hu.setShowMajiangs(handCardIds);
		}

		hu.initDahuList();

//        player.setDahu(hu.getDahuList());
//        LogUtil.printDebug("胡牌检测, 胡:{}, 大胡:{}, 地地胡需要牌:{}", hu.isHu(), dahuListToString(hu.getDahuList()), table.getGameModel().getSpecialPlay().getFloorHuNum() + 1);

		return hu;
	}

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/4
	 */
	private static int sumAndGet(Mj kingCard, List<Mj> handMajiang) {
		return kingCard != null ? handMajiang.stream().filter(v -> v.getVal() == kingCard.getVal()).mapToInt(v -> 1).sum() : 0;
	}

	/**
	 *@description 4王
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/4
	 */
	private static MjiangHu four4KingHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isBegin, boolean jiang258, TjMjTable table, Mj huMj, TjMjPlayer player, boolean haveKingIce, int subKingNum) {
		MjiangHu hu = new MjiangHu();
		if (handCardIds == null || handCardIds.isEmpty()) {
			return hu;
		}

		boolean isZimo = isZimo(table, huMj, player);

//		Mj floorCard = player.getPlayingTable(TjMjTable.class).getFloorCard();

		int kingN = sumAndGet(table.getKingCard(), handCardIds);
		int kingNum = isZimo ? kingN : 0 ;

		if (subKingNum == -1 && haveKingIce) {
			subKingNum = kingN - (sumAndGet(table.getKingCard(), player.getHandMajiang()));
		}

		int floorN = sumAndGet(table.getFloorCard(), handCardIds);
		int floorNum = isZimo ? floorN : 0;

		//地胡, 能地胡, 则胡后如果想有地胡门子,必须包含当前能胡牌的门子
		MjiangHu diHuTemp = new MjiangHu();

		//地胡,移除地牌后是否依然能胡
		boolean canDiHu = isCanDiHu(diHuTemp, handCardIds, gang, peng, chi, buzhang, jiang258, table, player, floorN,
				true, subKingNum);

		//硬庄和天胡同时出现,需要校验王牌是否拆了
		//1.拆了不算硬庄, 算天胡
		boolean canTianHu = isCanTianHu(diHuTemp, handCardIds, gang, peng, chi, buzhang, jiang258, table, player, kingN,
				true, subKingNum);

		//⑤硬庄：胡牌时手中无“王”或“王”未替代其他任何牌使用,算硬庄+胡牌番型,（可炮胡）
		boolean canYingZhuang = table.getGameModel().getSpecialPlay().isFloorHu()/* && (kingCards == 0 || player.getKingCardNumHuFlag() >= table.getGameModel().getSpecialPlay().getFloorHuNum())*/;

		//能硬庄检测有限硬庄检测
		isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu, canYingZhuang, subKingNum);

		if (hu.isHu()) {
			LogUtil.printDebug("硬庄胡了:{}", dahuListToString(hu.buildDahuList()));

			//这里当王只有两张的时候,平胡是强行平胡的,所以这里要单独拎出来验证
			//这里只要不是两张王牌就可以成为硬庄, 当手牌剩余两张王牌时在胡牌算法内部校验
			boolean falseYingZhuang = table.getKingCard() != null
					&& !CollectionUtils.isEmpty(handCardIds)
					&& handCardIds.size() == 2
					&& handCardIds.get(0).getVal() == table.getKingCard().getVal()
					&& handCardIds.get(1).getVal() == table.getKingCard().getVal();

			//剩余只有两张王牌一致时, 有特殊牌型无需258做将的算硬庄
			boolean yingZhuang = table.isKingCard(handCardIds.get(0))
					&& table.isKingCard(handCardIds.get(1))
					&& handCardIds.get(0).getVal() == handCardIds.get(1).getVal()
					&& (hu.isQingyiseHu() || hu.isPengpengHu());

			hu.setYingZhuang(hu.isYingZhuang() || !falseYingZhuang || yingZhuang);

			hu.initDahuList();

			//检测地胡和其他门子一致
			hu = checkDiHuMenZiEq(table, player, hu, diHuTemp, canDiHu);

			//天胡&天天胡
			checkTianHu(table, hu, kingNum);

			//地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
			//天胡需要满足胡牌牌型,光王牌无法接炮和自摸
			// 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
			// 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡
			//地胡&地地胡
			checkDihu(table, hu, floorNum, player);

			//天胡和硬庄同时出现,同时检测到王牌拆了,移除硬庄
			checkRemoveYingZhuang(hu, canTianHu);

			//1.拆除地牌后不能胡牌,移除地胡
			//2.只有地胡能胡牌,这里不移除
			//3.存在7小对,特殊处理,不移除
			checkRemoveDiHu(hu, canDiHu);

			hu.initDahuList();
		}

		if (canYingZhuang /*&& !onlyYingZhuangHu*/) {
			int addYingZhuangScore = 0;
			MjiangHu hu2 = new MjiangHu();

			if (hu.isHu()) {
				//算入硬庄的分数
				addYingZhuangScore = MjiangHu.calcMenZiScore(table, hu.getDahuList(), player.noNeedMoCard());
			}

			//非硬庄检测
			isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu2, false, subKingNum);

			hu2.initDahuList();

			if(hu2.isHu()){
				diHuTemp = new MjiangHu();
				canDiHu = isCanDiHu(diHuTemp, handCardIds, gang, peng, chi, buzhang, jiang258, table, player, floorN, false, subKingNum);

				//检测地胡和其他门子一致
				hu2 = checkDiHuMenZiEq(table, player, hu2, diHuTemp, canDiHu);

				//天胡&天天胡
				checkTianHu(table, hu2, kingNum);

				//地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
				//天胡需要满足胡牌牌型,光王牌无法接炮和自摸
				// 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
				// 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡
				//地胡&地地胡
				checkDihu(table, hu2, floorNum, player);

				//1.拆除地牌后不能胡牌,移除地胡
				//2.只有地胡能胡牌,这里不移除
				//3.存在7小对,特殊处理,不移除
				checkRemoveDiHu(hu2, canDiHu);

				hu2.initDahuList();
			}

			//算入王的分数
			int notYingZhuangScore = MjiangHu.calcMenZiScore(table, hu2.getDahuList(), player.noNeedMoCard());

			LogUtil.printDebug("算入硬庄分数:{},门子:{},不算入硬庄分数:{},非硬庄门子:{}", addYingZhuangScore, dahuListToString(hu.getDahuList()), notYingZhuangScore, dahuListToString(hu2.getDahuList()));

			if (addYingZhuangScore < notYingZhuangScore) {
				hu = hu2;
			}

			LogUtil.printDebug("硬庄分:{},非硬庄分:{}, 最终门子:{}", addYingZhuangScore, notYingZhuangScore, dahuListToString(hu.buildDahuList()));
		}

		//没得胡, 检测是不是有地胡直接倒
		if(!hu.isHu() && player.getGang().isEmpty()) {
		   //天胡&天天胡
		   checkTianHu(table, hu, kingNum);

		   //地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
		   //天胡需要满足胡牌牌型,光王牌无法接炮和自摸
		   // 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
		   // 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡
		   //地胡&地地胡
		   checkDihu(table, hu, floorNum, player);
		}

		//当前没地胡, 但是实际存在地胡
		//1.检测当前不带地胡的门子比较大, 还是地胡门子大
		if (hu.isHu() && !hu.isDihu() && !hu.isDidihu() && table.getGameModel().getSpecialPlay().isFloorHu() && floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum()) {
			MjiangHu onlyDiHu = new MjiangHu();
			checkDihu(table, onlyDiHu, floorNum, player);
			onlyDiHu.initDahuList();
			hu.initDahuList();
			if (MjiangHu.calcMenZiScore(table, onlyDiHu.buildDahuList(), table.getGameModel().getSpecialPlay().selfMoEqGunHu()) > MjiangHu.calcMenZiScore(table, hu.buildDahuList(), table.getGameModel().getSpecialPlay().selfMoEqGunHu())) {
				hu = onlyDiHu;
			}
		}

		//倒底胡,不与黑天胡叠加,其余叠加
		//庄家开局14张就可以胡牌，闲家没有倒底胡。
		if (table.getGameModel().getSpecialPlay().isOpeningHu() && hu.isHu() && !hu.isHeitianhu() && table.getDisCardRound() == 0 && isBegin && !hu.isDihu() && !hu.isDidihu()) {
			hu.setDaodihuh(true);
			//当前操作是为了让倒底胡因子一直跟随某一种操作
		}else if (!CollectionUtils.isEmpty(player.getDahu()) && player.getDahu().contains(12) && (table.getDisCardRound() <= 1)) {
			hu.setDaodihuh(true);
		}

		if (hu.isHu()) {
			hu.setShowMajiangs(handCardIds);
		}

		hu.initDahuList();

//        player.setDahu(hu.getDahuList());
//        LogUtil.printDebug("胡牌检测, 胡:{}, 大胡:{}, 地地胡需要牌:{}", hu.isHu(), dahuListToString(hu.getDahuList()), table.getGameModel().getSpecialPlay().getFloorHuNum() + 1);

		return hu;
	}

	/**
	 *@description 检测地胡和默认门子是否一致,不一致的情况下优先可以地胡的门子
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/30
	 */
	private static MjiangHu checkDiHuMenZiEq(TjMjTable table, TjMjPlayer player, MjiangHu hu, MjiangHu diHuTemp, boolean canDiHu) {
		if (canDiHu) {
			//增加硬庄
			diHuTemp.setYingZhuang(hu.isYingZhuang());
			//能地胡计算地胡和当前门子是否一致, 如不一致按分数高的算
			List<Integer> diHuTempMenZi = diHuTemp.buildDahuList();
			List<Integer> noDiHuTempMenZi = hu.buildDahuList();
			//验证门子一致性
			if (diHuTempMenZi != null && !CollectionUtils.isEmpty(diHuTempMenZi) &&
					noDiHuTempMenZi != null && !CollectionUtils.isEmpty(noDiHuTempMenZi)
					&& (diHuTempMenZi.size() == noDiHuTempMenZi.size() && IntStream.range(0, diHuTempMenZi.size()).anyMatch(i -> diHuTempMenZi.get(i) != noDiHuTempMenZi.get(i)))) {
				//算入王的分数
				int noDiHuScore = MjiangHu.calcMenZiScore(table, hu.getDahuList(), player.noNeedMoCard());
				int diHuScore = MjiangHu.calcMenZiScore(table, diHuTemp.getDahuList(), player.noNeedMoCard());
				//地牌移除后的某些门子不能直接替换(替换后门子错误,比如3张地牌花色移除后能组成清一色,实际不能), 这里仅为7对碰碰胡时替换
				if (diHuScore >= noDiHuScore && (diHuTempMenZi.contains(0) || diHuTempMenZi.contains(2)) && (noDiHuTempMenZi.contains(0)||noDiHuTempMenZi.contains(2))) {
					hu = diHuTemp;
				}
			}
		}
		return hu;
	}

	/**
	 *@description 特殊的自摸检验
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/4
	 */
	private static boolean isZimo(TjMjTable table, Mj huMj, TjMjPlayer player) {
		return player.noNeedMoCard()
				//杠上花
				|| ((table.getGameModel().getSpecialPlay().isGangUpGun() && huMj != null && table.isHasGangAction(player.getSeat()) && table.getGangSeatMap().containsKey(huMj.getId())) || table.getCurGangSeat() == player.getSeat())
				//杠上炮,胡牌者一定要有杠上炮门子------被人家胡
				|| (table.getGameModel().getSpecialPlay().isGangUpGun() && !table.getHuConfirmMap().containsKey(player.getSeat()) && huMj != null /*&& table.getNowGangSeat() == player.getSeat() */ && !table.getHuConfirmMap().isEmpty() &&
				table.getHuConfirmMap().keySet().stream().anyMatch(v -> {
					return v != player.getSeat() && !((TjMjPlayer) table.getSeatMap().get(v)).getDahu().isEmpty() && ((TjMjPlayer) table.getSeatMap().get(v)).getDahu().contains(7);
				}))
				//抢杠胡
				|| (table.getGameModel().getSpecialPlay().isRobGangHu() && table.getMoGangHuList().contains(player.getSeat()))
				//被抢杠,胡牌者一定要有抢杠胡门子------被人家胡
				|| (table.getGameModel().getSpecialPlay().isRobGangHu() && !table.getHuConfirmMap().containsKey(player.getSeat()) && !table.getMoGangHuList().isEmpty() && !table.getMoGangHuList().contains(player.getSeat()) && !table.getHuConfirmMap().isEmpty() &&
				table.getHuConfirmMap().keySet().stream().anyMatch(v -> {
					return v != player.getSeat() && !((TjMjPlayer) table.getSeatMap().get(v)).getDahu().isEmpty() && ((TjMjPlayer) table.getSeatMap().get(v)).getDahu().contains(9);
				}))
				//报听,被人胡
				|| (player.isSignTing() && !table.getHuConfirmMap().containsKey(player.getSeat()) && table.getDisCardSeat() == player.getSeat());
	}

	/**
     *@description 特殊逻辑,当天胡与硬庄共存时,检测王是否拆了,拆了不算硬庄,依然算天胡
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/12/25
     */
    private static void checkRemoveYingZhuang(MjiangHu hu, boolean canTianHu) {
    	//天天胡+硬庄,没变牌时(能组成硬庄),不检测是否拆牌,算作天胡+硬庄  20200727.16:30
		if(hu.isTiantianhu() && hu.isYingZhuang()) {
			//天胡降级
			hu.setTiantianhu(false);
			hu.setTianhu(true);
		} else if ((hu.isTianhu() || hu.isTiantianhu()) && hu.isYingZhuang() && !canTianHu) {
            hu.setYingZhuang(false);
        }
    }

    private static void checkRemoveDiHu(MjiangHu hu, boolean canDiHu) {
        //1.拆除地牌后不能胡牌,移除地胡
        //2.只有地胡能胡牌,这里不移除
        //3.存在7小对,特殊处理,不移除
		//**&& MjiangHu.canHuMenZi(hu.buildDahuList()) > 1*/
        if ((!canDiHu/* && (hu.isDihu() || hu.isDidihu()))  && (!hu.isXiaodui() && !hu.isHao7xiaodui() && !hu.isShuang7xiaodui()*/)) {
            hu.setDihu(false);
            hu.setDidihu(false);
        }
    }

    private static void checkTianHu(TjMjTable table, MjiangHu hu, int kingNum) {
        //天胡,只有王牌没有用出去算天胡
        //①天胡：手中有3张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带平2+1，天胡+胡牌番型（炮胡不算天胡）
        //②天天胡：手中有4张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天胡带平4+1，天天胡+胡牌番型（炮胡不算天天胡）
		//天天天胡：手中有 6 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天 天天胡带平 6+1，天胡+胡牌番型（炮胡不算天胡）
		if (table.getGameModel().isEightKing() && (hu.isHu() || hu.getDahuCount() > 0) && table.getGameModel().getSpecialPlay().isSkyHu() && kingNum >= table.getGameModel().getSpecialPlay().getKingHuNum() + 2) {
			hu.setTiantiantianhu(true);
		} else if ((hu.isHu() || hu.getDahuCount() > 0) && table.getGameModel().getSpecialPlay().isSkyHu() && kingNum >= table.getGameModel().getSpecialPlay().getKingHuNum() + 1) {
            hu.setTiantianhu(true);
        } else if ((hu.isHu() || hu.getDahuCount() > 0) && table.getGameModel().getSpecialPlay().isSkyHu() && kingNum >= table.getGameModel().getSpecialPlay().getKingHuNum()) {
            hu.setTianhu(true);
        }
    }

    private static void checkDihu(TjMjTable table, MjiangHu hu, int floorNum, TjMjPlayer player) {
		//7对地胡一定需要拆, 所以这里不给地胡
		if(hu.isSevenDui()){
			return;
		}

        //地胡没有牌型>=3张可直接倒牌,同时两张地牌, 有人杠出地牌来可以胡牌
        //天胡需要满足胡牌牌型,光王牌无法接炮和自摸
        // 手上三个王牌或地牌，开杠被抢或杠出的牌被别人胡或自己报听放炮，别人按自己牌型算天胡或地胡
        // 手上两个王牌或地牌，杠出一张王或者地牌，自己不能胡，别人胡了按自己牌型算分，不能算天胡地胡

        //地胡自己胡, 只能自摸, 或者杠上花, 不能接炮
        //③地胡：手中有3张“地牌”即可胡牌，若有其他牌型算地胡+胡牌牌型（三个地牌不能拆开），（炮胡不算地胡）
        //④地地胡：手中有4张“地牌”即可胡牌，若有其他牌型算地胡+胡牌牌型，（炮胡不算地胡）
		//地胡一定要有能够胡牌的门子才能和报听叠加
        if (table.getGameModel().getSpecialPlay().isFloorHu() && floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum() + 1 && (!player.isSignTing() || (hu.isHu() && player.isSignTing())) && hu.isSevenDui()) {
            hu.setDidihu(true);
            hu.setHu(true);
        } else if (table.getGameModel().getSpecialPlay().isFloorHu() && floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum() && (!player.isSignTing() || (hu.isHu() && player.isSignTing()))) {
            hu.setDihu(true);
            hu.setHu(true);
        }
    }

    private static boolean isCanDiHu(MjiangHu hu2, List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, TjMjTable table, TjMjPlayer player, int floorNum, boolean canYingZhuang,int subKingNum) {
        boolean canDiHu = false;
        //有地胡,校验地胡是否能拆
        if (table.getGameModel().getSpecialPlay().isFloorHu() && table.getFloorCard() != null && floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum()) {
            List<Mj> handCards = new ArrayList<>(handCardIds);
            //移除手里的底牌
            handCards.removeIf(v -> v.getVal() == table.getFloorCard().getVal());
            //移除手里的地牌之后,如果依旧能胡,则可以组成地胡牌型
            isHuMain(handCards, gang, peng, chi , buzhang, jiang258, table, player, hu2, canYingZhuang,true, subKingNum);
            canDiHu = hu2.isHu();
        }
        return canDiHu;
    }

    private static boolean isCanTianHu(MjiangHu hu2, List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, TjMjTable table, TjMjPlayer player, int kingNum, boolean canYingZhuang, int subKingNum) {
        boolean canTianHu = false;
        //有天胡,校验天胡是否能拆,拆了一样能胡,算硬庄
        if (table.getGameModel().getSpecialPlay().isSkyHu() && table.getKingCard() != null && kingNum >= table.getGameModel().getSpecialPlay().getKingHuNum()) {
            List<Mj> handCards = new ArrayList<>(handCardIds);
            //移除手里的底牌
            handCards.removeIf(v -> v.getVal() == table.getKingCard().getVal());
            //移除手里的地牌之后,如果依旧能胡,则可以组成地胡牌型
            isHuMain(handCards, gang, peng, chi , buzhang, jiang258, table, player, hu2, canYingZhuang,true, subKingNum);
            canTianHu = hu2.isHu();
        }
        return canTianHu;
    }

    private static void isHuMain(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, TjMjTable table, TjMjPlayer player, MjiangHu hu, boolean canYingZhuang, int subKingNum) {
		isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu, canYingZhuang, false, subKingNum);
    }

        /**
         *@description 胡牌校验
         *@param
         *@return
         *@author Guang.OuYang
         *@date 2019/10/22
         */
    private static void isHuMain(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, TjMjTable table, TjMjPlayer player, MjiangHu hu, boolean canYingZhuang, boolean isTianDiHuCheck, int subKingNum) {
        if (isPingHu(handCardIds, jiang258, subKingNum, canYingZhuang ? null : table.getKingCard(), canYingZhuang ? null : table.getKingCard2())) {
            hu.setPingHu(true);
            hu.setHu(true);
        }

        LogUtil.printDebug(player.getName() + "可以触发硬庄:{}, 硬庄;{}, 王牌数:{}, 地牌数{}", canYingZhuang, hu.isYingZhuang(), player.getKingCardNumHuFlag(), player.getFloorCardNumHuFlag());

        // 大胡检测
        MjRule.checkDahu(hu, handCardIds, gang, peng, chi, buzhang, table, canYingZhuang, player, isTianDiHuCheck, subKingNum);

        // 小胡检测
//		MjRule.checkXiaoHu2(hu, handCardIds, isBegin,table);

        //        if (hu.isQuanqiuren() && table.getGameModel().getSpecialPlay().isQuanQiuRenJiang() && hu.getDahuCount() == 1) {
//            if (huMj != null && !huMj.isJiang()) {
//                hu.setHu(false);
//            }
//        }
    }


    /***
     *  听哪些牌, 所有麻将和当前手牌做匹配, 匹配成功能胡则能听
     * @param majiangIds
     * @param gang
     * @param peng
     * @param chi
     * @param buzhang
     * @param jiang258
     * @param preserveAllTingInfo 保留所有听牌信息, false仅保留场上还存在的牌的信息
     *
     * @return
     */
    public static List<Mj> getTingMjs(List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, boolean dahu, int rule, TjMjTable table, TjMjPlayer player, boolean preserveAllTingInfo) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return null;
        }
		int subKingNum = 0;

        List<Mj> res = new LinkedList<>();

//        List<Integer> lists = new ArrayList<Integer>();
//        table.getSeatMap().values().stream().filter(v -> v.getSeat() != player.getSeat()).forEach(v -> lists.addAll(v.getHandPais()));
//        lists.addAll(table.getLeftMajiangs().stream().map(v -> v.getId()).collect(Collectors.toList()));

        HashMap<Integer, Void> repeatedChecked = new HashMap<>();

        for (Integer id : MjConstants.zhuanzhuan_mjList) {
//        for (Integer id : lists) {

            int idx = getOtherId(majiangIds, id);
            if (idx == 0) {
                continue;
            }

            Mj mj = Mj.getMajang(idx);

            if (repeatedChecked.containsKey(mj.getVal())) {
              continue;
            }

            repeatedChecked.put(mj.getVal(), null);

            MjiangHu hu = new MjiangHu();
            majiangIds.add(mj);

            if (isPingHu(majiangIds, jiang258, subKingNum, table.getKingCard(), table.getKingCard2())) {
                hu.setPingHu(true);
                hu.setHu(true);
            } else {
                MjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang, table, false, player, subKingNum);

                if (table.getGameModel().getSpecialPlay().isQuanQiuRenJiang() && hu.isQuanqiuren() && rule == 1 && hu.getDahuCount() == 1) {
                    if (!mj.isJiang()) {
                        hu.setHu(false);
                    }
                }
            }

//            LogUtil.printDebug("听牌胡牌检测, 胡:{}, 大胡:{}", hu.isHu(), dahuListToString(hu.getDahuList()));

            //听牌校验, 黑天胡庄家不能听
            //黑天胡不能听
            if (hu.isHu() && !hu.isHeitianhu() /* && !(player.noNeedMoCard() && hu.isHeitianhu())*/) {
                res.add(mj);
            }
            majiangIds.remove(mj);
        }

//        if (!preserveAllTingInfo) {
//            res.removeIf(v -> !lists.stream().anyMatch(l -> Mj.getMajang(l).getVal() == v.getVal()));
//        }

        if (!res.isEmpty() && res.size() > 1)
            res.sort((v1, v2) -> Integer.valueOf(v1.getVal()).compareTo(v2.getVal()));
        return res;

    }


    public static int getOtherId(List<Mj> majiangIds, int id) {

        List<Integer> list = new ArrayList<>();
        Mj omj = Mj.getMajang(id);
        for (Integer idx : MjConstants.zhuanzhuan_mjList) {
            Mj cm = Mj.getMajang(idx);
            if (omj.getVal() == cm.getVal()) {
                list.add(idx);
            }
        }

        List<Integer> list2 = new ArrayList<>();
        for (Mj mj : majiangIds) {
            if (omj.getVal() == mj.getVal()) {
                list2.add(mj.getId());
            }
        }

        list.removeAll(list2);
        if (list.size() > 0) {
            return list.get(0);
        }
        return 0;
    }

    /**
     *@description 平胡检测
     *@param majiangIds 手牌
     *@param needJiang258 是否需要258做将
     *@param kingCard 王牌,癞子牌
     *@param subKingNum 不能变化的王牌
     *@return
     *@author Guang.OuYang
     *@date 2019/10/18
     */
	public static boolean isPingHu(List<Mj> majiangIds, boolean needJiang258, int subKingNum, Mj... kingCard) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }

        if (majiangIds.size() % 3 != 2) {
            return false;
        }

        // 先去掉红中
        List<Mj> copy = new ArrayList<>(majiangIds);
        List<Mj> kingCards = dropKingCard(copy, subKingNum, kingCard);

        MjIndexArr card_index = new MjIndexArr();
        MjQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, kingCards.size(), needJiang258)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 红中麻将没有7小对，所以不用红中补
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<Mj> majiangIds, MjIndexArr card_index, int hongzhongNum) {
        if (majiangIds.size() == 14) {
            // 7小对
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } else if (majiangIds.size() + hongzhongNum == 14) {
            if (hongzhongNum == 0) {
                return false;
            }

            MjIndex index0 = card_index.getMajiangIndex(0);
            MjIndex index2 = card_index.getMajiangIndex(2);
            int lackNum = index0 != null ? index0.getLength() : 0;
            lackNum += index2 != null ? index2.getLength() : 0;

            if (lackNum <= hongzhongNum) {
                return true;
            }

            if (lackNum == 0) {
                lackNum = 14 - majiangIds.size();
                if (lackNum == hongzhongNum) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     *@description 拆牌, 先拆将,
     *@param card_index 牌分组的容器 4个3个2个1个集合
     *@param hasPais 手牌
     *@param kingCardSize 王牌数量
     *@param needJiang258 需要258做将牌
     *@return
     *@author Guang.OuYang
     *@date 2019/10/18
     */
    public static boolean chaijiang(MjIndexArr card_index, List<Mj> hasPais, int kingCardSize, boolean needJiang258) {
        Map<Integer, List<Mj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<Mj>> valEntry : jiangMap.entrySet()) {
            List<Mj> copy = new ArrayList<>(hasPais);
            MjHuLack lack = new MjHuLack(kingCardSize);
            List<Mj> list = valEntry.getValue();
            int i = 0;
            for (Mj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
//                LogUtil.printDebug("拆牌后, 胡牌检测通过:{}"+JacksonUtil.writeValueAsString(lack));
                return hu;
            }
        }

        if (kingCardSize > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (Mj majiang : hasPais) {
                List<Mj> copy = new ArrayList<>(hasPais);
                MjHuLack lack = new MjHuLack(kingCardSize);
                boolean isJiang = false;
                if (!needJiang258) {
                    // 不需要将
                    isJiang = true;

                } else {
                    // 需要258做将
                    if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                        isJiang = true;
                    }

                }
                if (isJiang) {
                    lack.setHasJiang(true);
                    lack.changeHongzhong(-1);
                    lack.addLack(majiang.getVal());
                    copy.remove(majiang);
                }

                boolean hu = chaipai(lack, copy, needJiang258);
                if (lack.isHasJiang() && hu) {
                    LogUtil.printDebug("胡牌检测通过,有将:{}",JacksonUtil.writeValueAsString(lack));
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // 红中做将
                        LogUtil.printDebug("胡牌检测通过,红中做将:{}", JacksonUtil.writeValueAsString(lack));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 拆牌
    public static boolean chaipai(MjHuLack lack, List<Mj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<Mj> hasPais) {
        Collections.sort(hasPais, new Comparator<Mj>() {

            @Override
            public int compare(Mj o1, Mj o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                }
                if (o1.getPai() > o2.getPai()) {
                    return 1;
                }
                return 0;
            }

        });
    }

    /**
     * 拆顺
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(MjHuLack lack, List<Mj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        Mj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<Mj> minList = MjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // 做顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 10 == 9) {
            pai1 = pai1 - 2;

        } else if (pai1 % 10 == 8) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;

        List<Integer> lackList = new ArrayList<>();
        List<Mj> num1 = MjQipaiTool.getVal(hasPais, pai1);
        List<Mj> num2 = MjQipaiTool.getVal(hasPais, pai2);
        List<Mj> num3 = MjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<Mj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // 一句话缺少的麻将
        if (num1.isEmpty()) {
            lackList.add(pai1);
        }
        if (num2.isEmpty()) {
            lackList.add(pai2);
        }
        if (num3.isEmpty()) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            if (lack.getHongzhongNum() <= 0) {
                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<Mj> count = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<Mj> count = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<Mj> count1 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<Mj> count2 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<Mj> count3 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<Mj> copy = new ArrayList<>(hasPais);
                    copy.removeAll(count1);
                    MjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }

            hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static boolean isCanAsJiang(Mj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<Mj> checkChi(List<Mj> majiangs, Mj dismajiang , TjMjTable table) {
        return checkChiCheck(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<Mj> checkChiCheck(List<Mj> majiangs, Mj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = MjHelper.toMajiangVals(majiangs);
        if (wangValList == null || !checkWang(chi1, wangValList)) {
            if (majiangIds.containsAll(chi1)) {
                return findMajiangByVals(majiangs, chi1);
            }
        }
        if (wangValList == null || !checkWang(chi2, wangValList)) {
            if (majiangIds.containsAll(chi2)) {
                return findMajiangByVals(majiangs, chi2);
            }
        }
        if (wangValList == null || !checkWang(chi3, wangValList)) {
            if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }
        return new ArrayList<Mj>();
    }

    public static List<Mj> findMajiangByVals(List<Mj> majiangs, List<Integer> vals) {
        List<Mj> result = new ArrayList<>();
        for (int val : vals) {
            for (Mj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 去掉王牌
     *
     * @param copy
     * @return
     */
    public static List<Mj> dropKingCard(List<Mj> copy,int subKingNum,  Mj...king) {
		subKingNum = -subKingNum;

        List<Mj> kingCards = new ArrayList<>();

		for (int i = copy.size() - 1; i >= 0; i--) {
			if (king != null && king.length > 0 && MjRule.isKingCard(copy.get(i), king) && ++subKingNum>0) {
				kingCards.add(copy.remove(i));
			}
		}

//        Iterator<Mj> iterator = copy.iterator();
//        while (iterator.hasNext()) {
//            Mj majiang = iterator.next();
//			if (king != null && king.length > 0 && MjRule.isKingCard(majiang, king)) {
//                kingCards.add(majiang);
//                iterator.remove();
//            }
//        }
        return kingCards;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof Mj) {
                    val = ((Mj) majiang).getVal();
                } else {
                    val = (int) majiang;
                }
                if (wangValList.contains(val)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * 相同的麻将
     *
     * @param majiangs 麻将牌
     * @param majiang  麻将
     * @param num      想要的数量
     * @return
     */
    public static List<Mj> getSameMajiang(List<Mj> majiangs, Mj majiang, int num) {
        List<Mj> hongzhong = new ArrayList<>();
        int i = 0;
        for (Mj maji : majiangs) {
            if (maji.getVal() == majiang.getVal()) {
                hongzhong.add(maji);
                i++;
            }
            if (i >= num) {
                break;
            }
        }
        return hongzhong;

    }

    /**
     * 先去某个值
     *
     * @param copy
     * @return
     */
    public static List<Mj> dropMjId(List<Mj> copy, int id) {
        List<Mj> hongzhong = new ArrayList<>();
        Iterator<Mj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Mj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static void sortMinPoint(List<Mj> handPais) {
        Collections.sort(handPais, new Comparator<Mj>() {

            @Override
            public int compare(Mj o1, Mj o2) {
                return o1.getVal() - o2.getVal();
            }

        });
    }

//    public static void main(String[] args) {
////        testHuPai();
////		List<Integer> moTailPai = new ArrayList<>(Arrays.asList(0,1,2,4,5));
////		System.out.println(moTailPai);
////		moTailPai = addMoTailPai(moTailPai,15);
////		System.out.println(moTailPai);
//
//        testHuPai();
//    }

    public static void testHuPai() {
        String
                pais = "11,11,12,12,13,13,14,14,21,21,31,31,22,22";
//				pais = "12,12,12,12,13,13,14,14,21,21,31,31,22,22";
//		pais = "12,12,12,12,13,13,13,14,14,22,22,32,32,33";
//		pais = "11,11,11,33,33,33,37,37,37,39,39,39,24,24";
//		pais = "27,27,27,27,14,14,14,14,16,16,11,11,19,201";
//		pais = "11,12,13,17,18,19,31,32,33,35,36,37,28,28";
//		pais = "11,12,13,15,16,17,18,18,24,25,23";
//		pais = "32,32,33,33,34,15,15,21,21,22,22,24,24,36";
		pais = "11,11,11,12,12,12,13,13,13,14,14,14,15,36";

        List<Mj> handPais = getPais(pais);
        System.out.println(toString(handPais));
        List<Mj> gangList = new ArrayList<>();
        List<Mj> pengList = new ArrayList<>();
        List<Mj> chiList = new ArrayList<>();
        List<Mj> buZhangList = new ArrayList<>();
        boolean isBegin = false;
        List<Mj> copy = new ArrayList<>(handPais);
        boolean jiang258 = false;
        TjMjTable tjMjTable = new TjMjTable();
        tjMjTable.setGameModel(GameModel.builder().build());
        tjMjTable.setKingCard(Mj.getMajang(24));
        tjMjTable.setFloorCard(Mj.getMajang(23));
        tjMjTable.getGameModel().setSpecialPlay(new GameModel.SpecialPlay());
        tjMjTable.getGameModel().getSpecialPlay().setFloorHuNum(3);
        tjMjTable.getGameModel().getSpecialPlay().setSevenPairs(true);
        MjiangHu hu = isHu(copy, gangList, pengList, chiList, buZhangList, isBegin, jiang258, tjMjTable, null, null, false, -1);
        StringBuilder sb = new StringBuilder("");
        sb.append("hu:").append(hu.isHu());
        sb.append("--daHuList:").append(hu.getDahuList());
        sb.append("--daHuL:").append(dahuListToString(hu.getDahuList()));

        sb.append("--xiaoHuList:").append(hu.getXiaohuList());
        sb.append("--xiaoHu:").append(actListToString(hu.getXiaohuList()));
        System.out.println(sb.toString());
    }

    public static String toString(List<Mj> handPais) {
        sortMinPoint(handPais);
        String paiStr = "";
        for (Mj mj : handPais) {
            paiStr += mj + ",";
        }
        return paiStr;
    }

    public static List<Mj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<Mj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (Mj mj : Mj.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    public static String dahuListToString(List<Integer> actList) {
        String[] str = new String[]{"1.碰碰胡","2.将将胡", "3.七小对","4.清一色" ,"5.豪华七小对4"," 6.超豪华七小对","7.杠上开花" ,"8.杠上炮" ,"9.报听" ,"10.抢杠胡"," 11.黑天胡","12.地胡","13.倒底胡","14.天胡","15天天胡4","16地地胡4","17平胡1","18硬庄","19天天天胡"};
        StringBuilder sb = new StringBuilder();
        if (actList != null && actList.size() > 0) {
            sb.append("[");
            for (int i = 0; i < actList.size(); i++) {
                if (sb.length() > 1) {
                    sb.append(",");
                }

                sb.append(str[actList.get(i)]);
            }
            sb.append("]");
        }
        return sb.toString();
    }


    public static String actListToString(List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        if (actList != null && actList.size() > 0) {
            sb.append("[");
            for (int i = 0; i < actList.size(); i++) {
                if (actList.get(i) == 1) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    if (i == MjAction.HU) {
                        sb.append("hu");
                    } else if (i == MjAction.PENG) {
                        sb.append("peng");
                    } else if (i == MjAction.MINGGANG) {
                        sb.append("mingGang");
                    } else if (i == MjAction.ANGANG) {
                        sb.append("anGang");
                    } else if (i == MjAction.CHI) {
                        sb.append("chi");
                    } else if (i == MjAction.BUZHANG) {
                        sb.append("buZhang");
                    } else if (i == MjAction.QUEYISE) {
                        sb.append("queYiSe");
                    } else if (i == MjAction.BANBANHU) {
                        sb.append("banBanHu");
                    } else if (i == MjAction.YIZHIHUA) {
                        sb.append("yiZhiHua");
                    } else if (i == MjAction.LIULIUSHUN) {
                        sb.append("liuLiuShun");
                    } else if (i == MjAction.DASIXI) {
                        sb.append("daSiXi");
                    } else if (i == MjAction.JINGTONGYUNU) {
                        sb.append("jinTongYuNv");
                    } else if (i == MjAction.JIEJIEGAO) {
                        sb.append("jieJieGao");
                    } else if (i == MjAction.SANTONG) {
                        sb.append("sanTong");
                    } else if (i == MjAction.ZHONGTUSIXI) {
                        sb.append("zhongTuSiXi");
                    } else if (i == MjAction.ZHONGTULIULIUSHUN) {
                        sb.append("zhongTuLiuLiuShun");
                    }
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public static List<Integer> addMoTailPai(List<Integer> moTailPai, int gangDice) {
        int leftMjCount = 5;
        int startIndex = 0;
        if (moTailPai.contains(0)) {
            int lastIndex = moTailPai.get(0);
            for (int i = 1; i < moTailPai.size(); i++) {
                if (moTailPai.get(i) == lastIndex + 1) {
                    lastIndex++;
                } else {
                    break;
                }
            }
            startIndex = lastIndex + 1;
        }
        if (gangDice == -1) {
            //补张，取一张
            for (int i = 0, size = leftMjCount; i < size; i++) {
                int nowIndex = i + startIndex;
                if (!moTailPai.contains(nowIndex)) {
                    moTailPai.add(nowIndex);
                    break;
                }
            }

        } else {
            int duo = gangDice / 10 + gangDice % 10;
            //开杠打色子，取两张
            for (int i = 0, j = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (nowIndex % 2 == 1) {
                    j++; //取到第几剁
                }
                if (moTailPai.contains(nowIndex)) {
                    if (nowIndex % 2 == 1) {
                        duo++;
                        leftMjCount = leftMjCount + 2;
                    }
                } else {
                    if (j == duo) {
                        moTailPai.add(nowIndex);
                        moTailPai.add(nowIndex - 1);
                        break;
                    }

                }
            }

        }
        Collections.sort(moTailPai);
        return moTailPai;
    }



    private static Integer[][] defaultArrays;

    static {
        defaultArrays = comboAllSerial(null);
    }

    /**
     *@description 所有顺子组合 123,234,345,...
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    public static Integer[][] comboAllSerial(GameModel gameModel) {
        if (defaultArrays != null) {
            return defaultArrays.clone();
        }


        int index = -1;
        int maxCombo = 7;
        //找出所有能组成顺子的组合123,234,345...
        //最大组合(8+2)*2
        int maxSerial = 9;
        //最大顺子组合7,最大吃组合为9
        Integer[][] arrays = new Integer[3 * maxCombo][3];

        int basic = 10;
        for (int i = 1; i <= maxSerial; i++) {
            if (i <= maxCombo) {
                arrays[++index] = new Integer[]{i + basic, i + basic + 1, i + basic + 2};
//                arrays[++index] = new Integer[]{i + 100, i + 1 + 100, i + 2 + 100};
            }
            if (i == maxSerial && basic < 30) {
                i = 0;
                basic += 10;
            }
        }

        return arrays;
    }

    public static FindAnyComboResult findSerial(List<Mj> handCards, GameModel gameModel) {
        Integer[][] allCombo = comboAllSerial(gameModel);
        return new FindAnyCombo(allCombo).anyComboMathSizeNonEquals(handCards.stream().map(v -> v.getVal()));
    }

    /**
     * @param
     * @param removeRepeatedMath 移除重复数字后做匹配
     * @return
     * @description 判断数组内的数字是否连续, 时间复杂度O(n) 测试数据10w测试次数100,数组大小100,总平均160ms
     * @author Guang.OuYang
     * @date 2019/9/20
     */
    public static boolean isSerialNumber(int[] array, boolean removeRepeatedMath) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        HashSet hashSet = new HashSet<>(array.length);

        int arraySum = 0;

        //剔除重复数字
        for (int i = 0; i < array.length; i++) {
            //不计入重复数字
            if (!hashSet.contains(array[i]))
                arraySum += array[i];

            hashSet.add(array[i]);

            if (!(hashSet.size() > i)) {
                if (!removeRepeatedMath) {
//                    System.out.println("找到重复的数字, 不做匹配");
                    return false;
                }
            }

            //找出最大最小数
            if (array[i] < min) {
                min = array[i];
            }

            if (array[i] > max) {
                max = array[i];
            }
        }

        if (max == min) {
//            System.out.println("组合内数量为1,顺序匹配不够");
            return false;
        }

        int arraySize = max - min;

        //10-1=9 10-0=11
        int arrayReSize = max + 1 - min;//min == 0 || min == 1 ? arraySize + 1 : arraySize;

        //最大数大于整体数组, 意味着这一串数字并不连续
        if (hashSet.size() != arrayReSize) {
//            System.out.println(min + "~" + max + "长度应为:" + arrayReSize + ",实际"+(removeRepeatedMath?"剔除重复后":"")+"长度:" + arrRveRepeated.length);
            return false;
        }

        //算出最小与最大数正确的和
        long sum = 0;
        for (int i = min; i <= max; i++) {
            sum += i;
        }

//        System.out.println("正确的和:" + sum + ",实际:" + arraySum);
        return arraySum == sum;
    }


    /**
     * @param
     * @author Guang.OuYang
     * @description 特定数字组合寻找[时间复杂度O(n - m)], 数据源200, 循环10w次, 结果:全部false,平均1ms/100次,结果:全部true,50ms/10w
     * @return
     * @date 2019/9/19
     */
    public static class FindAnyCombo {
        private List<FindAny> dataSrc = new ArrayList<>();

        /**
         * @param
         * @author Guang.OuYang
         * @description 搜索容器, 给出指定数字
         * @return
         * @date 2019/9/19
         */
        @Data
        public class FindAny {
            //结果匹配值,不重复, 匹配值->结果
            private HashMap<Integer, Boolean> src = new HashMap<>();
            //匹配值,不重复,  匹配值->出现次数
            private HashMap<Integer, Integer> srcCount = new HashMap<>();
            //初始匹配值,不重复,  匹配值->出现次数
            private HashMap<Integer, Integer> initSrcCount = new HashMap<>();
            //匹配值的重复值
            private List<Integer> repeatedSrcKey = new ArrayList<>();

            public FindAny(Integer... val) {
                for (int i = 0; i < val.length; i++) {
                    src.put(val[i], false);
                    if (srcCount.get(val[i]) == null) {
                        srcCount.put(val[i], 1);
                    } else {
                        srcCount.put(val[i], srcCount.get(val[i]) + 1);
                    }

                    initSrcCount.putAll(srcCount);
                    repeatedSrcKey.add(val[i]);
                }

            }

            public boolean in(Integer number) {
                if (src.containsKey(number)) {
                    int v = srcCount.get(number) - 1;
                    srcCount.put(number, v);
                    if (v <= 0) {
                        src.put(number, true);
                    }
                    return true;
                }
                return false;
            }

            public boolean checkAllIn() {
                Iterator<Boolean> iterator = src.values().iterator();
                while (iterator.hasNext()) {
                    Boolean next = iterator.next();
                    if (!next) {
                        return false;
                    }
                }
                return true;
//                return src.values().stream().noneMatch(v -> !v.booleanValue());
            }

            public boolean clearMark() {
                srcCount.putAll(initSrcCount);
                srcCount.keySet().forEach(k -> src.put(k, false));
                return true;
            }
        }

        public FindAnyCombo(Integer[]... val) {
            for (int i = 0; i < val.length; i++) {
                if (val[i] == null) continue;
                dataSrc.add(new FindAny(val[i]));
            }
        }

        /**
         * @param
         * @return
         * @description 多个组合中匹配任意1个
         * @author Guang.OuYang
         * @date 2019/9/20
         */
        public FindAnyComboResult anyComboMath(Stream<Integer> stream) {
            return assignComboMath(stream, 1, true);
        }

        public FindAnyComboResult anyComboMathSizeNonEquals(Stream<Integer> stream) {return assignComboMath(stream, 1, false);}

        /**
         * @param
         * @return
         * @description 多个组合中匹配全部组合
         * @author Guang.OuYang
         * @date 2019/9/20
         */
        public FindAnyComboResult allComboMath(Stream<Integer> stream) {return assignComboMath(stream, dataSrc.size(), false); }
        public FindAnyComboResult allComboMathSizeEquals(Stream<Integer> stream) {return assignComboMath(stream, dataSrc.size(), true);}

        /**
         * @param stream         需要做筛选的数据集
         * @param matchComboSize 匹配组合数量为1时, 匹配给定的组合内任意一组
         * @param jump 如果所有组合的数量一致时开启它
         * @return
         * @description 特定数字组合寻找, 数据源200, 循环10w次, 结果:全部false,平均1ms/100次,结果:全部true,50ms/10w
         * @author Guang.OuYang
         * @date 2019/9/19
         */
        private FindAnyComboResult assignComboMath(Stream<Integer> stream, int matchComboSize, boolean jump) {
            FindAnyComboResult findAnyComboResult = new FindAnyComboResult();
            int i = 0;
            int iniSize = matchComboSize;//dataSrc.size();
            Iterator<Integer> iterator = stream.iterator();

            boolean find = false;
            int findCount = 0;
            co:
            while (iterator.hasNext()) {
                i++;
                Integer next = iterator.next();
                Iterator<FindAny> iterator1 = dataSrc.iterator();
                while (iterator1.hasNext()) {
                    FindAny findAny = iterator1.next();//2710
                    findAny.in(next);
                    //这里循环校验避免过多的check
                    find = (!jump || i % findAny.src.size() == 0) && findAny.checkAllIn();

                    //命中的组合
                    if (find) {
                        findAnyComboResult.getValues().addAll(findAny.getRepeatedSrcKey());
                        findAnyComboResult.getFindCombos().add(findAny);
                        ++findCount;
                        iterator1.remove();
                    }

                    //数量足够了
                    if (find = findCount == iniSize) {
                        break co;
                    }
                }
            }
            findAnyComboResult.setFind(find);
            return findAnyComboResult;
        }
    }

    @Data
    public static class FindAnyComboResult{
        private boolean find;
        private List<FindAnyCombo.FindAny> findCombos = new ArrayList<>();
        private List<Integer> values = new ArrayList<>();
    }

}
