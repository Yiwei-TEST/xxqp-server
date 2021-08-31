package com.sy599.game.qipai.hsphz.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.NullArgumentException;

import com.sy599.game.qipai.hsphz.bean.CardTypeHuxi;
import com.sy599.game.qipai.hsphz.bean.GameModel;
import com.sy599.game.qipai.hsphz.bean.HsphzPlayer;
import com.sy599.game.qipai.hsphz.bean.HsphzTable;
import com.sy599.game.qipai.hsphz.bean.PaohzDisAction;
import com.sy599.game.qipai.hsphz.constant.HuType;
import com.sy599.game.qipai.hsphz.constant.PaohzCard;
import com.sy599.game.qipai.hsphz.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_HONGWU = 1;//即胡牌时手中的红牌大于等于10张，小于13张，加红加小时每多一张红牌加一倍（2倍起）
	public static final int LOUDI_MINGTANG_PIAOHU = 2;//飘胡：即胡牌时手中的牌只有两坎或两提或一坎一提红牌（2倍） 
	public static final int LOUDI_MINGTANG_BIANHU = 3;//扁胡：即胡牌时手中只有一对，一坎或一提红牌时称为二扁三扁四扁胡（二扁2倍，三扁3倍，四扁4倍）  
	public static final int LOUDI_MINGTANG_YIDIANZHU = 4;//点胡：即胡牌时手中只有一个红牌，其余全为黑牌（4倍） 
	public static final int LOUDI_MINGTANG_DAHONGHU = 5;//即胡牌时手中的红牌大于等于13张，加红加小时每多一张红牌加一倍（5倍起）
	public static final int LOUDI_MINGTANG_WUHU = 6;//黑胡：即胡牌时手中没有红牌（5倍） 
	public static final int LOUDI_MINGTANG_PENGPENGHU = 7;// 碰碰胡：即胡牌时手中的牌全部为跑起，提起，碰起，偎起，没有1绞牌和1句话（5倍）
	public static final int LOUDI_MINGTANG_XIAOZIHU = 8;//十六小：即胡牌时手中的牌小字牌大于等于16张，加红加小时每多一个小字牌加一倍（5倍起） 
	public static final int LOUDI_MINGTANG_DAZIHU = 9;//十八大：即胡牌时手中的牌大字牌等于18张，每多一个大字牌加一倍（5倍起） 
	public static final int LOUDI_MINGTANG_TIANHU = 10;//天胡：庄家起手21张牌构成胡牌（5倍） 
    public static final int LOUDI_MINGTANG_DIHU = 11; // 地胡：闲家在牌墩上揭开的第一张牌即构成胡牌为地胡，庄家不能胡地胡（5倍） 
    public static final int LOUDI_MINGTANG_HAIDIHU = 12; //海底胡：摸墩上最后一张牌为海底胡（5倍） 
    public static final int LOUDI_MINGTANG_ZIMO = 13;// 自摸：即胡牌的牌为自己亲手在墩上所摸。（在分数上+1分） 

    
    /**
     * @description
     * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public static final BigMap<Integer, String, Integer> SCORE_CALC = new BigMap<Integer, String, Integer>(15) {
        {
            put(LOUDI_MINGTANG_ZIMO, "+", 0);        //加分直接都计入胡息
            put(LOUDI_MINGTANG_DAHONGHU, "*", 5);
            put(LOUDI_MINGTANG_PIAOHU, "*", 2);
            put(LOUDI_MINGTANG_BIANHU, "*", 0);
            put(LOUDI_MINGTANG_TIANHU, "*", 5);
            put(LOUDI_MINGTANG_DIHU, "*", 5);
            put(LOUDI_MINGTANG_WUHU, "*", 5);
            put(LOUDI_MINGTANG_YIDIANZHU, "*", 4);
            put(LOUDI_MINGTANG_DAZIHU, "*", 5);
            put(LOUDI_MINGTANG_XIAOZIHU, "*", 5);
            put(LOUDI_MINGTANG_PENGPENGHU, "*", 5);
            put(LOUDI_MINGTANG_HAIDIHU, "*", 5);
            put(LOUDI_MINGTANG_HONGWU, "*", 2);
//            put(LOUDI_MINGTANG_SANSHIHUXI_FANBEI, "*", 2);
//            put(LOUDI_MINGTANG_DIAOPAO, "+", 0);
//            put(LOUDI_MINGTANG_SANSHIHUXI, "+", 0);
//            put(LOUDI_MINGTANG_JIEPAO, "+", 0);
//            put(LOUDI_MINGTANG_DIHU_HONG, "+", 0);
        }
    };

    /**
     * @param
     * @author Guang.OuYang
     * @description
     * @return
     * @date 2019/9/4
     */
    @AllArgsConstructor
    @Getter
    public static class BigMap<K, E, V> {
        private BigMap[] bigMap;
        private E e;
        private K k;
        private V v;

        BigMap() {
        }

        BigMap(int size) {
            bigMap = new BigMap[size];
        }

        BigMap putAll(K k, E e, V v) {
            this.e = e;
            this.k = k;
            this.v = v;
            return this;
        }

        void put(K k, E e, V v) {
            if (bigMap == null) {
                throw new NullArgumentException("BigMap don't init..");
            }
            int hash = k.hashCode() % bigMap.length;
            int reHash = hash == 0 ? (k.hashCode() + hash - 1) : hash - 1;
            bigMap[reHash] = new BigMap<K, E, V>().putAll(k, e, v);
        }

        public BigMap<K, E, V> get(K k) {
            int hash = k.hashCode() % bigMap.length;
            int reHash = hash == 0 ? (k.hashCode() + hash - 1) : hash - 1;
            return hash < bigMap.length ? bigMap[reHash] : null;
        }
    }

    /**
     * 名堂
     *
     * @param player 胡牌玩家
     * @return
     */
    public static Map<Integer, Integer> calcMingTang(HsphzPlayer player) {

        /*  
            4.1 红胡：即胡牌时手中的红牌大于等于10张，小于13张，从10张开始加红加小时每多一张红牌加一倍（2倍起） 
			4.2 飘胡：即胡牌时手中的红牌只有两坎或两提或一坎一提的牌（2倍） 
			4.3 扁胡：即胡牌时手中的红牌只有一对，一坎或一提红牌时称为二扁三扁四扁胡（二扁2倍，三扁3倍，四扁4倍） 
			4.4 点胡：即胡牌时手中只有一个红牌，其余全为黑牌（4倍） 
			4.5 十三红：即胡牌时手中的红牌大于等于13张，加红加小时每多一张红牌加一倍（5倍起） 
			4.6 黑胡：即胡牌时手中没有红牌（5倍） 
			4.7 碰碰胡：即胡牌时手中的牌全部为跑起，提起，碰起，偎起，没有1绞牌和1句话（5倍） 
			4.8 十六小：即胡牌时手中的牌小字牌大于等于16张，加红加小时每多一个小字牌加一倍（5倍起） 
			4.9 十八大：即胡牌时手中的牌大字牌等于18张，每多一个大字牌加一倍（5倍起） 
			4.10 天胡：庄家起手21张牌构成胡牌（5倍） 
			4.11 地胡：闲家在牌墩上揭开的第一张牌即构成胡牌为地胡，庄家不能胡地胡（5倍） 
			4.12 海底胡：摸墩上最后一张牌为海底胡（5倍） 
			4.13 自摸：即胡牌的牌为自己亲手在墩上所摸。（在分数上+1分）
            
            */

    	Map<Integer, Integer> mtMap = new HashMap<>();
        List<PaohzCard> allCards = new ArrayList<>();
        for (CardTypeHuxi type : player.getCardTypes()) {
            allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
        }

        if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
            for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
                allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
            }
        }

        
        HsphzTable table = player.getPlayingTable(HsphzTable.class);
        boolean isJhJdJx = table.getGameModel().getSpecialPlay().isHxd();

        //红牌数量
        int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(allCards)).orElse(Collections.emptyList()).size();
        //小牌数量
        int smallCardsSize = Optional.ofNullable(PaohuziTool.findSmallPhzs(allCards)).orElse(Collections.emptyList()).size();
        //大牌数量
        int largerCardsSize = allCards.size() - smallCardsSize;

        //天胡
//        if (table.isFirstCard() && table.getLastWinSeat() == player.getSeat()) {
//            mtMap.put(LOUDI_MINGTANG_TIANHU, 5);
//            LogUtil.printDebug("4.10 天胡: 庄家起手发牌结束后直接胡牌;计分翻倍。");
//        } else if (table.getLastWinSeat() != player.getSeat() && player.isFirstDisSingleCard()) {
//        	mtMap.put(LOUDI_MINGTANG_DIHU, 5);
//        	LogUtil.printDebug("4.11 地胡: 手牌未变动，胡牌胡息翻倍；");
//        }

        HuType huType = ((HsphzTable) player.getPlayingTable()).calcHuType();
        //自摸,庄家天胡也算自摸
//        if ( huType == HuType.ZI_MO && table.getGameModel().getSpecialPlay().getZimoAdd() != 0) {
//        	if(table.getGameModel().getSpecialPlay().getZimoAdd()<0){
//        		mtMap.put(LOUDI_MINGTANG_ZIMO, 2);
//        	}else{
//        		mtMap.put(LOUDI_MINGTANG_ZIMO, table.getGameModel().getSpecialPlay().getZimoAdd());
//        	}
//            LogUtil.printDebug("4.13 自摸：自摸加分或翻倍");
//        }
        
        //飘胡  互斥
//        if(isPiaoHu(player)){
//        	mtMap.put(LOUDI_MINGTANG_PIAOHU, 2);
//            LogUtil.printDebug("4.2飘胡：即胡牌时手中的牌只有两坎或两提或一坎一提红牌（2倍） ");
//        }else{
//        	//扁胡
//        	int bianCount = getBianHuNew(player);
//        	if(bianCount != 0){
//        		mtMap.put(LOUDI_MINGTANG_BIANHU, bianCount);
//        		LogUtil.printDebug("4.3扁胡：即胡牌时手中的红牌只有一对，一坎或一提红牌时称为二扁三扁四扁胡（二扁2倍，三扁3倍，四扁4倍） ");
//        	}
//        }
        //碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
        if (isPPHuNew(player)) {
        	mtMap.put(LOUDI_MINGTANG_PENGPENGHU, 4);
            LogUtil.printDebug("4.7胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。");

        }

        //互斥名堂
        if (redCardCount >= 10 && redCardCount < 13) {
            //红胡
//            if(isJhJdJx){
//            	mtMap.put(LOUDI_MINGTANG_HONGWU, 2+redCardCount-10);
//            }else{
            	mtMap.put(LOUDI_MINGTANG_HONGWU, 2);
//            }
            LogUtil.printDebug("4.1 红胡：即胡牌时手中的红牌大于等于10张，小于13张，2倍 ");
        } else if (redCardCount == 1) {
            //一点红
        	mtMap.put(LOUDI_MINGTANG_YIDIANZHU,3);
        	LogUtil.printDebug("4.4点胡：即胡牌时手中只有一个红牌，其余全为黑牌（3倍） ");
        } else if (redCardCount == 0) {
            //黑胡
            mtMap.put(LOUDI_MINGTANG_WUHU, 4);
            LogUtil.printDebug("4.6黑胡：即胡牌时手中没有红牌（4倍） ");
        }else if(redCardCount >= 13){
        	//十三红
//            if(isJhJdJx){
//            	mtMap.put(LOUDI_MINGTANG_DAHONGHU, 5+redCardCount-13);
//            }else{
            	mtMap.put(LOUDI_MINGTANG_DAHONGHU, 4);
//            }
            LogUtil.printDebug("4.5即胡牌时手中的红牌大于等于13张，4倍");
        }

        //互斥名堂, 大小字
//        if (table.getGameModel().getSpecialPlay().isSlx()&& smallCardsSize >= 16) {
//            //小字胡
//            if(isJhJdJx){
//            	mtMap.put(LOUDI_MINGTANG_XIAOZIHU, 5+smallCardsSize-16);
//            }else{
//            	mtMap.put(LOUDI_MINGTANG_XIAOZIHU, 5);
//            }
//            LogUtil.printDebug("4.8十六小：即胡牌时手中的牌小字牌大于等于16张，加红加小时每多一个小字牌加一倍（5倍起） ");
//        }else if(table.getGameModel().getSpecialPlay().isSbd()&& largerCardsSize >= 18) {
//            //大字胡
//        	 if(isJhJdJx){
//        		 mtMap.put(LOUDI_MINGTANG_DAZIHU, 5+largerCardsSize-18);
//             }else{
//            	 mtMap.put(LOUDI_MINGTANG_DAZIHU, 5);
//             }
//        	LogUtil.printDebug("4.9十八大：即胡牌时手中的牌大字牌等于18张，每多一个大字牌加一倍（5倍起） ");
//        }

//        if (table.getGameModel().getSpecialPlay().isHaiduHu() && table.getLeftCards().size()==0) {
//            //海底胡
//        	mtMap.put(LOUDI_MINGTANG_HAIDIHU, 5);
//            LogUtil.printDebug("4.12海底胡：摸墩上最后一张牌为海底胡（5倍）  ");
//        }
        
        return mtMap;
    }

    /**
     * @param
     * @return
     * @description 扁胡：即胡牌时手中只有一对，一坎或一提红牌时称为二扁三扁四扁胡（二扁2倍，三扁3倍，四扁4倍）  
     * @author Guang.OuYang
     * @date 2019/9/5
     */
    private static int getBianHu(HsphzPlayer player) {
    	long t = System.currentTimeMillis();
    	//检测手牌中是否有顺子
//    	if (player.getHandPais().size() > 3) {
    		List<PaohzCard> handCards = new ArrayList<>();
    		handCards.addAll(player.getHandPhzs());
    		handCards.addAll(player.getTi());
    		handCards.addAll(player.getZai());
    		handCards.addAll(player.getChi());
    		handCards.addAll(player.getPeng());
    		handCards.addAll(player.getPao());
    		
    		//除非天胡否则把最后摸出来的牌算入
//    		if (!player.isFirstDisSingleCard() && !handCards.contains(player.getPlayingTable(NxphzTable.class).getNowDisCardIds().get(0))) {
    		if (!player.isFirstDisSingleCard() && (!handCards.contains(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0)) 
            		&& !handCards.contains(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0)))) {
    			//胡牌时把最后张底牌算进去,这张牌的组合不确定所以把玩家所有牌都算入
    			handCards.add(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0));
    		}
    		//找到所有牌组中3个以上红牌的组合值val
    		Map<Integer, Integer> hongCards = findHongCountGeTwo(handCards);
    		if(hongCards.size()!= 1){
    			return 0;
    		}
    		int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(handCards)).orElse(Collections.emptyList()).size();
    		int sumCount = 0;
    		for (Integer count : hongCards.values()) {
				if(count < 2){
					return 0;
				}
				sumCount += count; 
			}
    		if(redCardCount == sumCount){
    			return sumCount;
    		}
    		
//    	}
    	
    	LogUtil.printDebug("扁胡匹配:{}ms", (System.currentTimeMillis() - t));
    	return 0;
    }
    private static int getBianHuNew(HsphzPlayer player) {
    	long t = System.currentTimeMillis();
    	
    	 List<CardTypeHuxi> huxiList = player.getHuCards();
    	 if(huxiList == null || huxiList.size() <= 0){
    		 return 0;
    	 }
    	 int hongJuNum = 0;//红色一句话的数量
    	 int hongNum = 0;//红色一句话的数量
    	 for (CardTypeHuxi type : huxiList) {
    		 List<PaohzCard> cards = type.getCardIdsToCard();
    		 if(cards == null) continue;
    		 if(cards.size() < 2){
    			 return 0;
    		 }
    		 boolean isHua = false;
       		 int val = cards.get(0).getVal();
    		 for (PaohzCard card : cards) {
    			if(val != card.getVal()){
    				isHua = true;
    				break;
    			}
    		 }
    		 if(isHua){//一句话
    			 //一句话里面只要有一个是红牌则扁胡不成立
    			 for (PaohzCard card:cards) {
					if(card.isHongpai()){
						return 0;
					}
				}
    		 }else{
    			 if(cards.get(0).isHongpai()){
    				 if(hongJuNum > 0){
    					 return 0;
    				 }
    				 hongJuNum++;
    				 hongNum = cards.size();
    			 }
    		 }
		 }
    		 
    	
    	LogUtil.printDebug("扁胡匹配:{}ms", (System.currentTimeMillis() - t));
    	return hongNum;
    }
    /**
     * @param
     * @return
     * @description 飘胡: 即胡牌时手中的红牌只有两坎或两提或一坎一提红牌（2倍）
     * @author Guang.OuYang
     * @date 2019/9/5
     */
    private static boolean isPiaoHu(HsphzPlayer player) {
    	long t = System.currentTimeMillis();
    	
    	
    	 List<CardTypeHuxi> huxiList = player.getHuCards();
	   	 if(huxiList == null || huxiList.size() <= 0){
	   		 return false;
	   	 }
	   	 for (CardTypeHuxi type : huxiList) {
   		 List<PaohzCard> cards = type.getCardIdsToCard();
   		 if(cards == null || cards.size() < 2) continue;
   		 boolean isHua = false;
   		 int val = cards.get(0).getVal();
		 for (PaohzCard card : cards) {
			if(val != card.getVal()){
				isHua = true;
				break;
			}
		 }
   		 if(isHua){//一句话
   			 //一句话里面只要有一个是红牌则扁胡不成立
   			 for (PaohzCard card:cards) {
					if(card.isHongpai()){
						return false;
					}
				}
   		 }
		 }
    	
    	//检测手牌中是否有顺子
//    	if (player.getHandPais().size() > 3) {
    		
    		List<PaohzCard> handCards = new ArrayList<>();
    		handCards.addAll(player.getHandPhzs());
    		handCards.addAll(player.getTi());
    		handCards.addAll(player.getZai());
    		handCards.addAll(player.getChi());
    		handCards.addAll(player.getPeng());
    		handCards.addAll(player.getPao());
    		
    		//除非天胡否则把最后摸出来的牌算入
//    		if (!player.isFirstDisSingleCard() && !handCards.contains(player.getPlayingTable(NxphzTable.class).getNowDisCardIds().get(0))) {
    		if (!player.isFirstDisSingleCard() && (!handCards.contains(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0)) 
            		&& !handCards.contains(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0)))) {
    			//胡牌时把最后张底牌算进去,这张牌的组合不确定所以把玩家所有牌都算入
    			handCards.add(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0));
    		}
    		//找到所有牌组中3个以上红牌的组合值val
    		Map<Integer, Integer> hongCards = findHongCountGeTwo(handCards);
    		if(hongCards.size()!= 2){
    			return false;
    		}
    		int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(handCards)).orElse(Collections.emptyList()).size();
    		int sumCount = 0;
    		for (Integer count : hongCards.values()) {
    			if(count < 3){
    				return false;
    			}
    			sumCount += count; 
    		}
    		if(redCardCount == sumCount){
    			return true;
    		}
    		
//    	}
    	
    	LogUtil.printDebug("飘胡匹配:{}ms", (System.currentTimeMillis() - t));
    	return false;
    }
    /**
     * @param
     * @return
     * @description 碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍
     * @author Guang.OuYang
     * @date 2019/9/5
     */
    private static boolean isPPHu(HsphzPlayer player) {
        long t = System.currentTimeMillis();
        //不能有吃
        boolean ppHu = !player.getCardTypes().stream().anyMatch(v -> v.getAction() == PaohzDisAction.action_chi);

        //检测手牌中是否有顺子
//        if (ppHu && player.getHandPais().size() > 3) {
    	if (ppHu ) {
            List<PaohzCard> handCards = new ArrayList<>();
            List<PaohzCard> outCards = new ArrayList<>();
            handCards.addAll(player.getHandPhzs());
            outCards.addAll(player.getTi());
            outCards.addAll(player.getZai());
            outCards.addAll(player.getChi());
            outCards.addAll(player.getPeng());
            outCards.addAll(player.getPao());

            //除非天胡否则把最后摸出来的牌算入
            if (!player.isFirstDisSingleCard() && (!handCards.contains(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0)) 
            		&& !outCards.contains(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0)))) {
                //胡牌时把最后张底牌算进去,这张牌的组合不确定所以把玩家所有牌都算入
                handCards.add(player.getPlayingTable(HsphzTable.class).getNowDisCardIds().get(0));
            }
            //找到所有牌组中3个以上的组合值val
            List<Integer> threeOrFourCountCards = findCountGeTwo(handCards);

            //除掉手牌中的3个和4个
            handCards.removeIf(v -> threeOrFourCountCards.contains(v.getVal()));

//            //匹配找到所有组合
//            Integer[][] allCombo = comboAllSerial(player.getPlayingTable(XtPaohuziTable.class).getGameModel());

            //匹配所有顺子组合, 匹配到了则不能组成碰碰胡
//            ppHu = !new FindAnyCombo(allCombo).anyComboMath(handCards.stream().map(v -> v.getVal()));
            if(handCards.size() == 0){
            	ppHu = true;
            }else if(handCards.size() == 2){
            	ppHu= handCards.get(0).getVal() == handCards.get(1).getVal();
            }else{
            	ppHu = false;
            }
        }

        LogUtil.printDebug("碰碰胡匹配:{}ms", (System.currentTimeMillis() - t));
        return ppHu;
    }
    private static boolean isPPHuNew(HsphzPlayer player) {
    	long t = System.currentTimeMillis();
    	
    	 List<CardTypeHuxi> huxiList = player.getHuCards();
    	 if(huxiList == null || huxiList.size() <= 0){
    		 return false;
    	 }
    	 for (CardTypeHuxi type : huxiList) {
    		 List<PaohzCard> cards = type.getCardIdsToCard();
    		 if(cards == null) continue;
    		 if(cards.size() < 2){
    			 return false;
    		 }
    		 int val = cards.get(0).getVal();
    		 for (PaohzCard card : cards) {
				if(val != card.getVal()){
					return false;
				}
			 }
		 }
    		 
        LogUtil.printDebug("碰碰胡匹配:{}ms", (System.currentTimeMillis() - t));
        return true;
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
            Integer[][] clone = defaultArrays.clone();
//            if (!(gameModel != null && gameModel.getSpecialPlay().isOneFiveTen())) {
//                clone[clone.length - 1] = null;
//                clone[clone.length - 2] = null;
//            }

            return clone;
        }


        int index = -1;
        //找出所有能组成顺子的组合123,234,345...
        //最大组合(8+2)*2
        int maxSerial = 10;
        //最大顺子组合8,最大吃组合为10,额外组合2710+1510+大于8的吃牌如9,109,109但是不能组成9,10,11
        Integer[][] arrays = new Integer[((maxSerial - 2) * 4) + 2 * 4][3];
        for (int i = 1; i <= maxSerial; i++) {
            if (i <= 8) {
                arrays[++index] = new Integer[]{i, i + 1, i + 2};
                arrays[++index] = new Integer[]{i + 100, i + 1 + 100, i + 2 + 100};
            }
            arrays[++index] = new Integer[]{i, i + 100, i + 100};
            arrays[++index] = new Integer[]{i, i, i + 100};
        }

        //特殊组合2710
        arrays[++index] = new Integer[]{2, 7, 10};
        arrays[++index] = new Integer[]{102, 107, 110};
        return arrays;
    }

    /**
     *@description 找到牌组中所有大于等于3张的牌组（红牌）
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    private static Map<Integer, Integer> findHongCountGeTwo(List<PaohzCard> cards) {
        cards.sort((v1, v2) -> Integer.valueOf(v1.getVal()).compareTo(Integer.valueOf(v2.getVal())));

        //最低匹配数量
        int prePai=0;   //上一个牌
        int repeated=0; //当前计算的数量
        Map<Integer, Integer> paohzCards = new HashMap<>();

        Iterator<PaohzCard> iterator = cards.iterator();
        while (iterator.hasNext()) {
            PaohzCard next = iterator.next();
            if(!next.isHongpai()){
            	continue;
            }
            if (prePai > 0 && prePai > 0 && prePai != next.getVal()) {
            	paohzCards.put(prePai, repeated);
                repeated = 0;
                prePai = 0;
            }
            if (prePai == 0 || prePai == next.getVal()) {
                prePai = next.getVal();
                repeated += 1;
                paohzCards.put(prePai, repeated);
            }
//            if (repeated == minMathCount) {
//            	
//            }
        }
        return paohzCards;
    }
    
    /**
     *@description 找到牌组中所有大于3张的牌组
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    private static List<Integer> findCountGeTwo(List<PaohzCard> cards) {
        cards.sort((v1, v2) -> Integer.valueOf(v1.getVal()).compareTo(Integer.valueOf(v2.getVal())));

        //最低匹配数量
        int minMathCount = 3;
        int prePai=0;   //上一个牌
        int repeated=0; //当前计算的数量
        ArrayList<Integer> paohzCards = new ArrayList<>();

        Iterator<PaohzCard> iterator = cards.iterator();
        while (iterator.hasNext()) {
            PaohzCard next = iterator.next();
            if (prePai > 0 && prePai > 0 && prePai != next.getVal()) {
                repeated = 0;
                prePai = 0;
            }
            if (prePai == 0 || prePai == next.getVal()) {
                prePai = next.getVal();
                repeated += 1;
            }
            if (repeated == minMathCount) {
                paohzCards.add(next.getVal());
            }
        }
        return paohzCards;
    }

    /**
     * @param
     * @return
     * @description 计算名堂分, 番数, 分数=囤数*番数(名堂)。 囤数=胡息/3 , 这里直接计算胡息
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static int calcMingTangFen(int totalTun, HsphzTable table, Map<Integer, Integer> mt,boolean isSelfMo) {
        //总囤
        int totalTunNumber = 0;
        for (Entry<Integer, Integer> entry: mt.entrySet()) {
			if(entry.getKey() != LOUDI_MINGTANG_ZIMO){
				totalTunNumber+=entry.getValue();
			}
		}
        if(totalTunNumber == 0){
        	totalTunNumber =1;
        }
//        int zimo = table.getGameModel().getSpecialPlay().getZimoAdd();
//        if(totalTunNumber <= 0){
//        	if(zimo < 0 && isSelfMo){
//        		totalTunNumber = 0;
//        	}else{
//        		totalTunNumber = 1;
//        	}
//        }
    	/*底分2分 
    	囤数=（胡息-9或15）÷3 余数+2 
    	分数=囤数*名堂番数 
    	如果勾选自摸加分：分数=（囤数+自摸加分）*名堂番数 
    	如果勾选自摸翻倍：分数=囤数*自摸2*名堂番数*/
        
//        if(zimo > 0 && isSelfMo){
//        	return (totalTun+zimo) * totalTunNumber+table.getGameModel().getSpecialPlay().getZhaniao()*2;
//        }else if(zimo < 0 && isSelfMo){
//        	return totalTun * (2 + totalTunNumber)+table.getGameModel().getSpecialPlay().getZhaniao()*2;
//        }else{
        	return  totalTun * totalTunNumber+table.getGameModel().getSpecialPlay().getZhaniao()*2;
//        }
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


//        /**
//         * @param stream         需要做筛选的数据集
//         * @param matchComboSize 匹配组合数量为1时, 匹配给定的组合内任意一组
//         * @return
//         * @description 字典查找,当多次查找在同个大量数据堆搜索大量数据可以减少约10倍搜索时间
//         * @author Guang.OuYang
//         * @date 2019/9/19
//         */
//        private boolean groupAssignComboMath(Stream<Integer> stream, int matchComboSize) {
//            int i = 0;
//            int iniSize = matchComboSize;//dataSrc.size();
//            List<List<Integer>> res = new ArrayList<>();
//            Iterator<Integer> iterator = stream.iterator();
//            while (iterator.hasNext()) {
//                Integer val = iterator.next();
//
//                int groupId = val / 10 + 1;
//
//                //寻找组
//                List<Integer> group = res.get(groupId);
//
//                if (group == null) {
//                    group = new ArrayList<>(10);
//                    res.add(group);
//                }
//                group.add(val);
//            }
//
//            return false;
//        }
    }

    @Data
    public static class FindAnyComboResult{
        private boolean find;
        private List<FindAnyCombo.FindAny> findCombos = new ArrayList<>();
        private List<Integer> values=new ArrayList<>();
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
}
