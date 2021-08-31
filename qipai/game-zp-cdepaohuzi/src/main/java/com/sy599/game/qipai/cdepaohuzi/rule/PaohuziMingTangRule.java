package com.sy599.game.qipai.cdepaohuzi.rule;

import com.sy599.game.qipai.cdepaohuzi.bean.CardTypeHuxi;
import com.sy599.game.qipai.cdepaohuzi.bean.GameModel;
import com.sy599.game.qipai.cdepaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.cdepaohuzi.bean.CdePaohuziPlayer;
import com.sy599.game.qipai.cdepaohuzi.bean.CdePaohuziTable;
import com.sy599.game.qipai.cdepaohuzi.constant.HuType;
import com.sy599.game.qipai.cdepaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.cdepaohuzi.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.NullArgumentException;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class PaohuziMingTangRule {

    public static final int LOUDI_MINGTANG_TIANHU = 1;//4.2 天胡: 庄家起手发牌结束后直接胡牌;计分翻倍。
    public static final int LOUDI_MINGTANG_DIHU_HONG = 2;// 4.3 地胡：手牌未变动，胡牌算地胡;计分翻倍。
    public static final int LOUDI_MINGTANG_ZIMO = 3;//自摸: 即胡牌的牌为自己亲手在墩上所摸(2番)。选项
    public static final int LOUDI_MINGTANG_YIDIANZHU = 4;//4.7 一点红: 胡牌时红牌张数=1;计分翻倍。
    public static final int LOUDI_MINGTANG_DAHONGHU = 5;//红胡：胡牌时大于或等于十张红字，每多一个红字多一番，大六八番起始3番、小六八番起始2番；
    public static final int LOUDI_MINGTANG_HONGWU = 6;//红乌：胡牌时大于等于13张红字，4番；
    public static final int LOUDI_MINGTANG_WUHU = 7;//4.5 黑胡：胡牌牌型红字牌张数=0;计分翻倍。
    public static final int LOUDI_MINGTANG_DAZIHU = 8;//4.8 大字胡：大胡：胡牌时大字大于等于十八张，每多一个多一番，大六八番8番、小六八番6番；
    public static final int LOUDI_MINGTANG_XIAOZIHU = 9;//十八小: 小胡：胡牌时小字大于等于十六张，每多一个多一番，大六八番10番、小六八番8番；
    public static final int LOUDI_MINGTANG_PENGPENGHU = 10;//4.6 碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
    public static final int LOUDI_MINGTANG_DIAOPAO = 11; // 放炮：所胡那张牌是别人打出来的，胡息加10。
    public static final int LOUDI_MINGTANG_SANSHIHUXI = 12; //30胡息（十红）：30胡息以上，且红字数量>=10,算100胡。
    public static final int LOUDI_MINGTANG_JIEPAO = 13; //4.3 放炮：所胡那张牌是别人打出来的，胡息加10。
    public static final int LOUDI_MINGTANG_SANSHIHUXI_FANBEI = 14; //4.9 30胡息：胡牌时胡息≥30，胡息翻倍(先翻倍再加自摸放炮10分)。
    public static final int LOUDI_MINGTANG_DIHU = 15; //4.3 地胡：手牌未变动，胡牌算地胡;计分翻倍。
//    public static final int LOUDI_MINGTANG_EIGHTEEN_MIN = 16; //十八小: 即胡牌时小字牌数≥18张(6番)。
    public static final int LOUDI_MINGTANG_THREE_TI_FIRE_KAN = 17; //三提五坎：任何玩家起手3提或5坎可直接胡牌 (6番)。选项
    public static final int LOUDI_MINGTANG_ZIMO_ADD = 18;//自摸+1囤
    public static final int LOUDI_MINGTANG_UPHILL = 19;//黄番, 特殊名堂  黄番：胡牌之前，有过黄庄，黄一次则总囤数乘以2，黄两次则乘以3，黄三次则乘以4，依此类推；
    public static final int LOUDI_MINGTANG_ZIMO_JIAFAN_JIATUN = 20;//自摸加番加囤,特殊名堂

    public static final int LOUDI_MINGTANG_HAIHU = 21;//海胡：胡牌为墩上最后一张，大六八番6番、小六八番4番；
    public static final int LOUDI_MINGTANG_TINGHU = 22;//听胡：没有进字就胡牌（天胡和地胡都是听胡），大六八番6番、小六八番4番；
    public static final int LOUDI_MINGTANG_DUIZIHU = 23;//对子胡：胡牌时全部七方门子中的每一方子都是同一个字，即七方门子或都是对子、碰牌、偎牌、跑牌或提牌
    public static final int LOUDI_MINGTANG_SHUAHOU = 24;//耍猴：胡牌时只剩下一张字单钓，大六八番8番、小六八番6番；
    public static final int LOUDI_MINGTANG_TUANHU = 25;//团胡：同字重招（对应大小字都开跑为大团圆），如：壹和一都跑起或提。出现N对也只是算8番，不需要累积计算；

    public static final int LOUDI_MINGTANG_HONGHU_TUPAO = 26;//土炮红胡：胡牌时大于等于10张红字，小于13张红字，2番；
    public static final int LOUDI_MINGTANG_YIDIANZHU_TUPAO = 27;//红黑点点胡：胡牌时有且只有一个红字，3番；
	public static final int LOUDI_MINGTANG_WUHU_TUPAO = 28;//黑胡：胡牌时全是黑字，6番。
	
	public static final int LOUDI_MINGTANG_HANGHANGX = 29;//行行息：胡牌时每一个门子都有息8番。
	public static final int LOUDI_MINGTANG_JIAHh = 30;//假行行：6方门子有息，加一对将，4番。
	public static final int LOUDI_MINGTANG_SIQI_HONG = 31;//四七红：四个红和七个红都算红胡，2番。
	public static final int LOUDI_MINGTANG_KAPPA = 32;//背靠背：听牌后只要手里有两对牌，且这两对牌点数相同，胡了其中一张，6番。
	
	public static final int LOUDI_MINGTANG_HHD_PENGPENGHU = 33;//红黑点碰碰胡：胡牌时全部七方门子中的每一方子都是同一个字，即七方门子或都是对子、碰牌、偎牌、跑牌或提牌4番

	
	


    /**
     * @description
     * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    //大六八
    public static final BigMap<Integer, String, int[]> SCORE_CALC = new BigMap<Integer, String, int[]>(30) {
        {
//            put(LOUDI_MINGTANG_ZIMO, "*", 2);
			//全名堂,,
			//大六八,小六八
			put(LOUDI_MINGTANG_ZIMO_ADD, "+", new int[] { 1 });        //加分直接都计入囤
			put(LOUDI_MINGTANG_TIANHU, "*", new int[] { 6, 4 });
            put(LOUDI_MINGTANG_DIHU, "*", new int[] { 6, 4 });
            put(LOUDI_MINGTANG_HAIHU, "*", new int[] { 6, 4 });
            put(LOUDI_MINGTANG_TINGHU, "*", new int[] { 6, 4 });
            put(LOUDI_MINGTANG_DAHONGHU, "*", new int[] { 3, 2 });
            put(LOUDI_MINGTANG_WUHU, "*", new int[] { 8, 6 });
            put(LOUDI_MINGTANG_YIDIANZHU, "*", new int[] { 6, 4 });
            put(LOUDI_MINGTANG_DAZIHU, "*", new int[] { 8, 6 });
            put(LOUDI_MINGTANG_XIAOZIHU, "*", new int[] { 10, 8 });
            put(LOUDI_MINGTANG_PENGPENGHU, "*", new int[] { 8, 6 });
            put(LOUDI_MINGTANG_SHUAHOU, "*", new int[] { 8, 6 });
            put(LOUDI_MINGTANG_UPHILL, "*", new int[] { 0 });
            put(LOUDI_MINGTANG_TUANHU, "*", new int[] { 8 });
           
            
           // put(LOUDI_MINGTANG_DUIZIHU, "*", new int[] { 8 });
            
        //    put(LOUDI_MINGTANG_HONGHD_DUIZIHU, "*", new int[] { 4 });
            
            
            
            put(LOUDI_MINGTANG_HONGHU_TUPAO, "*", new int[] { 2 });
            put(LOUDI_MINGTANG_YIDIANZHU_TUPAO, "*", new int[] { 3 });
            put(LOUDI_MINGTANG_HONGWU, "*", new int[] { 4 });
            put(LOUDI_MINGTANG_WUHU_TUPAO, "*", new int[] { 6 });
            
            put(LOUDI_MINGTANG_HANGHANGX, "*", new int[] { 8 });
            put(LOUDI_MINGTANG_JIAHh, "*", new int[] { 6 });
            put(LOUDI_MINGTANG_SIQI_HONG, "*", new int[] { 2 });
            put(LOUDI_MINGTANG_KAPPA, "*", new int[] { 8 });
            put(LOUDI_MINGTANG_HHD_PENGPENGHU, "*", new int[] { 4 });
            

//            put(LOUDI_MINGTANG_EIGHTEEN_MIN, "*", 6);
//            put(LOUDI_MINGTANG_THREE_TI_FIRE_KAN, "*", 6);

//            put(LOUDI_MINGTANG_PENGPENGHU, "+", 0);
//            put(LOUDI_MINGTANG_SANSHIHUXI_FANBEI, "+", 0);
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
		private final int DEFAULT_CAPACITY = 16;

		private HashMap<K , BigMapEntry<K, E, V>> bigMap ;

		BigMap() {
			bigMap = new HashMap<>(DEFAULT_CAPACITY);
		}

		BigMap(int capacity) {
			bigMap = new HashMap<>(capacity);
		}

		public void put(K k, E e, V v) {
			if (bigMap == null) {
				throw new NullArgumentException("BigMap don't init..");
			}
			this.bigMap.put(k, new BigMapEntry<>(k, e, v));
		}

		public void merge(K k, E e, V v, BiFunction<? super BigMapEntry<K, E, V>, ? super BigMapEntry<K, E, V>, ? extends BigMapEntry<K, E, V>> remappingFunction) {
			bigMap.merge(k, new BigMapEntry<>(k,e,v), remappingFunction);
		}

		public BigMapEntry<K, E, V> get(K k) {
			return bigMap.get(k);
		}

		public Iterator<BigMapEntry<K, E, V>> iterable(){
			return this.bigMap.values().iterator();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			Iterator<BigMapEntry<K, E, V>> iterable = iterable();
			while (iterable.hasNext()) {
				BigMapEntry<K, E, V> next = iterable.next();
				sb.append("," + next.toString());
			}
			return sb.length() > 0 ? sb.deleteCharAt(0).toString() : sb.toString();
		}
//        final int reHash(Object key) {
//            int hash = key.hashCode() % bigMap.length;
//            int reHash = hash == 0 ? key.hashCode() == 0 ? 0 : (key.hashCode() + hash - 1) : hash - 1;
//            return reHash;
//        }
	}

	@Data
	@AllArgsConstructor
	public static class BigMapEntry<K, E, V> {
		private K k;
		private E e;
		private V v;

		BigMapEntry put(K k, E e, V v) {
			this.e = e;
			this.k = k;
			this.v = v;
			return this;
		}

		public K getK(){return this.k;}
		public E getE(){return this.e;}
		public V getV(){return this.v;}

		@Override
		public String toString() {
			return "[K=" + k + " , E=" + e + " , V=" + v + "]";
		}
	}
    /**
     * 名堂
     *
     * @param player 胡牌玩家
     * @return  map k->名堂 v->额外加分这个分数的运算取决于名堂内的分数是增加还是翻番
     */
    public static Map<Integer,Integer> calcMingTang(CdePaohuziPlayer player) {

        /* 四、名堂
				全名堂玩法：
				1：天胡：庄家起手胡牌，大六八番6番、小六八番4番；
				2：地胡：庄家亮张的字或第一张打的字闲家胡，大六八番6番、小六八番4番；
				3：海胡：胡牌为墩上最后一张，大六八番6番、小六八番4番；
				4：听胡：没有进字就胡牌（天胡和地胡都是听胡），大六八番6番、小六八番4番；
				5：红胡：胡牌时大于或等于十张红字，每多一个红字多一番，大六八番起始3番、小六八番起始2番；
				6：黑胡：胡牌时没有一张红字，大六八番8番、小六八番6番；
				7：点胡：胡牌时只有一张红字，大六八番6番、小六八番4番；
				8：大胡：胡牌时大字大于等于十八张，每多一个多一番，大六八番8番、小六八番6番；
				9：小胡：胡牌时小字大于等于十六张，每多一个多一番，大六八番10番、小六八番8番；
				10：对子胡：胡牌时全部七方门子中的每一方子都是同一个字，即七方门子或都是对子、碰牌、偎牌、跑牌或提牌，大六八番8番、小六八番6番；
				11：耍猴：胡牌时只剩下一张字单钓，大六八番8番、小六八番6番；
				12：黄番：胡牌之前，有过黄庄，黄一次则总囤数乘以2，黄两次则乘以3，黄三次则乘以4，依此类推；
				13：团胡：同字重招（对应大小字都开跑为大团圆），如：壹和一都跑起或提。出现N对也只是算8番，不需要累积计算；
				14：自摸：胡牌时，所胡的那张牌是玩家自己从墩上摸出来的。在原有的基础上加1囤（不加番）。
				计番原则：如果有两个或以上名堂的，总番数按各名堂的番数累加。

				土炮胡玩法：
				1：红胡：胡牌时大于等于10张红字，小于13张红字，2番；
				2：点胡：胡牌时有且只有一个红字，3番；
				3：红乌：胡牌时大于等于13张红字，4番；
				4：黑胡：胡牌时全是黑字，6番。
				5：自摸+1囤
            */

        Map<Integer,Integer> mtList = new HashMap<>();
        List<PaohzCard> allCards = new ArrayList<>();
        
        
        boolean hhx= true;
        boolean jiang =false;
        boolean duizi = true;
       
        CdePaohuziTable table = player.getPlayingTable(CdePaohuziTable.class);
        PaohzCard huCard = null;
        if(player.getHu() != null){
        	huCard = player.getHu().getCheckCard();
        	if(huCard==null){
        		huCard = table.getNowDisCardIds().get(0);
        		huCard = table.getNowDisCardIds().get(0);
        	}
        }
        List<PaohzCard> hucardVals = new ArrayList<>();
        boolean kapaFlag =true;
        for (CardTypeHuxi type : player.getCardTypes()) {
            allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
            if(type.getHux()==0){
            	hhx =false;
            }
            
            if(type.getAction()==PaohzDisAction.action_chi){
            	duizi = false;
            }
            
            if(huCard!=null&&(type.getAction()==PaohzDisAction.action_zai||type.getAction()==PaohzDisAction.action_chouzai)){
            	if(huCard.getVal()==PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal()){
            		hucardVals.addAll(PaohuziTool.getHuCardVals(type.getCardIds(), huCard));
            	}
            }
        }
        
        if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
            for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
                allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
                if(type.getHux()==0){
                	if(type.getCardIds().size()==2){
                		jiang = true;
                	}else{
                		hhx =false;
                	}
                }
                if(type.getAction()==0&&type.getCardIds().size()>2){
                	duizi=false;
                }
                
                if(huCard!=null&&(type.getAction()==PaohzDisAction.action_zai||type.getAction()==PaohzDisAction.action_chouzai||type.getAction()==PaohzDisAction.action_peng)){
                	if(huCard.getVal()==PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal()){
                		hucardVals.addAll(PaohuziTool.getHuCardVals(type.getCardIds(), huCard));
                	}
                }
                //背靠背的牌
                if(huCard!=null&&type.getAction()!=PaohzDisAction.action_chi&&type.getAction()!=PaohzDisAction.action_kan&&type.getAction()!=PaohzDisAction.action_zai){
                	if(type.getAction()==0&&huCard.getVal()==PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal()){
                		 if(type.getCardIds().size()==3){
             				//在顺子里有胡的牌一定不是背靠背
             				List<PaohzCard> shun = PaohuziTool.getHuCardVals(type.getCardIds(), huCard);
             				if(!shun.isEmpty()){
             					kapaFlag = false;
             				}
             			}
                	}else{
                		if(type.getAction()==0){
                			//3+2
                			if(type.getCardIds().size()==2){
                				hucardVals.addAll(PaohuziTool.getHuCardVals(type.getCardIds(), huCard));
                			}else if(type.getCardIds().size()==3){
                				//在顺子里有胡的牌一定不是背靠背
                				List<PaohzCard> shun = PaohuziTool.getHuCardVals(type.getCardIds(), huCard);
                				if(!shun.isEmpty()){
                					kapaFlag = false;
                				}
                			}
                		}
                	}
                }
            }
        }


        HuType huType = ((CdePaohuziTable) player.getPlayingTable()).calcHuType();

        //仅自摸名堂叠加
        //自摸
        if (table.getGameModel().getSpecialPlay().isSinceTouchFanBei() && huType == HuType.ZI_MO) {
            mtList.put(LOUDI_MINGTANG_ZIMO,0);
            LogUtil.printDebug("自摸: 即胡牌的牌为自己亲手在墩上所摸(2番)。选项");
        }

        if (table.getGameModel().getSpecialPlay().isSinceTouchAdd3Score() && huType == HuType.ZI_MO) {
            mtList.put(LOUDI_MINGTANG_ZIMO_ADD,0);
            LogUtil.printDebug("自摸: 加1囤");
        }

        //红牌数量
        int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(allCards)).orElse(Collections.emptyList()).size();
        //小牌数量
        int smallCardsSize = Optional.ofNullable(PaohuziTool.findSmallPhzs(allCards)).orElse(Collections.emptyList()).size();

        //三提五坎
        if (table.getGameModel().getSpecialPlay().isThreeTiFiveKan() && table.isThreeTiFiveKan(player)) {
            mtList.put(LOUDI_MINGTANG_THREE_TI_FIRE_KAN,0);
            LogUtil.printDebug("三提五坎：任何玩家起手3提或5坎可直接胡牌(6番)。选项");
        }

        //十八小
//        else if (table.getGameModel().getSpecialPlay().isEighteenMin() && smallCardsSize >= 18) {
//            mtList.put(LOUDI_MINGTANG_XIAOZIHU , 0);
//            LogUtil.printDebug("十八小:即胡牌时小字牌数≥18张(6番)。");
//        }
        else if (table.getGameModel().getSpecialPlay().isRedBlackHu() && redCardCount == 0) {
            //黑胡
            mtList.put(table.getGameModel().getMingTangPlayT()!=2 ? LOUDI_MINGTANG_WUHU : LOUDI_MINGTANG_WUHU_TUPAO , 0);
            LogUtil.printDebug("黑胡:即胡牌时红牌数=0张(5番)。");
        }
		else if (table.getGameModel().getSpecialPlay().isRedWu() && redCardCount >= 13&&table.getGameModel().getMingTangPlayT()==2) {
			//红乌
				mtList.put(LOUDI_MINGTANG_HONGWU , 0);
			LogUtil.printDebug("红乌：胡牌时大于等于13张红字，4番；");
		}
		//互斥名堂
        else if (table.getGameModel().getSpecialPlay().isRedBlackHu() && redCardCount >= 10) {
            //红胡
			mtList.put(table.getGameModel().getMingTangPlayT()!=2 ? LOUDI_MINGTANG_DAHONGHU :LOUDI_MINGTANG_HONGHU_TUPAO , table.getGameModel().getMingTangPlayT()!=2 ? redCardCount - 10 : 0);
            LogUtil.printDebug("红胡：胡牌时大于或等于十张红字，紅黑点玩法2番。其他每多一个红字多一番，");
        }else if (table.getGameModel().getSpecialPlay().isASmallRed() && redCardCount == 1) {
            //一点红
            mtList.put(table.getGameModel().getMingTangPlayT()!=2 ? LOUDI_MINGTANG_YIDIANZHU : LOUDI_MINGTANG_YIDIANZHU_TUPAO, 0);
            LogUtil.printDebug("点胡:即胡牌时手中的红牌数=1张(3番)。");
        }

        //4.9 30胡息：胡牌时胡息≥30，胡息翻倍(先翻倍再加自摸放炮10分)。
        else if (table.getGameModel().getSpecialPlay().isThirtyHuXiDoubleScore() && player.getTotalHu() >= 30) {
            mtList.put(LOUDI_MINGTANG_SANSHIHUXI_FANBEI , 0);
            LogUtil.printDebug("30胡息：胡牌时胡息≥30，胡息翻倍(先翻倍再加自摸放炮10分)。");
        }

        //天胡
        if (table.getGameModel().getSpecialPlay().isSkyFloorHu() && table.isFirstCard() && table.getLastWinSeat() == player.getSeat()&& player.isFirstDisSingleCard()) {
            if (table.getGameModel().getSpecialPlay().isSkyFloorHu()) {
                mtList.put(LOUDI_MINGTANG_TIANHU , 0);
                LogUtil.printDebug("4.2 天胡: 庄家起手发牌结束后直接胡牌;计分翻倍。");
            }
        } else if (table.getGameModel().getSpecialPlay().isSkyFloorHu() && table.getLastWinSeat() != player.getSeat() && player.isFirstDisSingleCard()) {
            mtList.put(LOUDI_MINGTANG_DIHU , 0);
            LogUtil.printDebug("4.7 地胡: 手牌未变动，胡牌胡息翻倍；");
        }
        
      //听胡：没有进字就胡牌（天胡和地胡都是听胡），大六八番6番、小六八番4番；
      		if (table.getGameModel().getSpecialPlay().isTingHu() && player.getTingFlag()==1) {
      			mtList.put(LOUDI_MINGTANG_TINGHU, 0);
      		}

        //碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
      	if (table.getGameModel().getSpecialPlay().isPpHu() && isPPHu(player,huCard)) {
            mtList.put(table.getGameModel().getMingTangPlayT()!=2 ?LOUDI_MINGTANG_PENGPENGHU:LOUDI_MINGTANG_HHD_PENGPENGHU , 0);
            LogUtil.printDebug("胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。");
        }
      	

        //互斥名堂, 大小字
         if (table.getGameModel().getSpecialPlay().isMaxMinChar()) {
            if (smallCardsSize >= 16) {
                //小字胡
                mtList.put(LOUDI_MINGTANG_XIAOZIHU , smallCardsSize - 16);
                LogUtil.printDebug("小胡：胡牌时小字大于等于十六张，每多一个多一番，大六八番10番、小六八番8番；");
            } else if (allCards.size() - smallCardsSize >= 18) {
                //大字胡
				mtList.put(LOUDI_MINGTANG_DAZIHU, (allCards.size() - smallCardsSize) - 18);
                LogUtil.printDebug("大胡：胡牌时大字大于等于十八张，每多一个多一番，大六八番8番、小六八番6番；");
            }
        } else if (table.getGameModel().getSpecialPlay().isIgnite() && table.getHuType() == HuType.DIAN_PAO) {
            mtList.put(LOUDI_MINGTANG_JIEPAO , 0);
            LogUtil.printDebug("放炮：所胡那张牌是别人打出来的，胡息加10。");
        }

		

		//海胡：胡牌为墩上最后一张，大六八番6番、小六八番4番；
		if(table.getGameModel().getSpecialPlay().isSeaHu() && table.getLeftCards().size()==0){
			mtList.put(LOUDI_MINGTANG_HAIHU, 0);
		}

		//耍猴：胡牌时只剩下一张字单钓，大六八番8番、小六八番6番；
		if (table.getGameModel().getSpecialPlay().isMonkeyShowHu() && player.getHandPais().size() == 1) {
			mtList.put(LOUDI_MINGTANG_SHUAHOU, 0);
		}
		
		// 对子胡：胡牌时全部七方门子中的每一方子都是同一个字，即七方门子或都是对子、碰牌、偎牌、跑牌或提牌    碰碰胡
//		if (table.getGameModel().getSpecialPlay().isDdHu() && duizi) {
//			mtList.put(table.getGameModel().getMingTangPlayT() != 2 ? LOUDI_MINGTANG_DUIZIHU
//					: LOUDI_MINGTANG_HONGHD_DUIZIHU, 0);
//		}
		
		
		//-------------------------------------------------------
		
		//行行息：胡牌时每一个门子都有息8番。
				if (table.getGameModel().getSpecialPlay().isHanghangX() && hhx&&!jiang) {
					mtList.put(LOUDI_MINGTANG_HANGHANGX, 0);
				}
				////假行行：6方门子有息，加一对将，4番。
				if (table.getGameModel().getSpecialPlay().isJiaHangHang()  && hhx&&jiang) {
					mtList.put(LOUDI_MINGTANG_JIAHh, 0);
				}
				//四七红：四个红和七个红都算红胡，2番。
				if (table.getGameModel().getSpecialPlay().isSiQiHong() && (redCardCount==4||redCardCount==7)) {
					mtList.put(LOUDI_MINGTANG_SIQI_HONG, 0);
				}
				
				//背靠背：听牌后只要手里有两对牌，且这两对牌点数相同，胡了其中一张，6番。
				if (table.getGameModel().getSpecialPlay().isKappa() && hucardVals.size()==5&&kapaFlag) {
					mtList.put(LOUDI_MINGTANG_KAPPA, 0);
				}
		
		
		
		

		//团胡：同字重招（对应大小字都开跑为大团圆），如：壹和一都跑起或提。出现N对也只是算8番，不需要累积计算；
		if (table.getGameModel().getSpecialPlay().isGroupHu()) {
			HashMap<Integer, Void> repeatedBig = new HashMap<Integer, Void>();
			HashMap<Integer, Void> repeatedSmall = new HashMap<Integer, Void>();
			player.getPao().iterator().forEachRemaining(v -> {
				if (v.getVal() > 100) {
					repeatedBig.put(v.getVal(), null);
				}
				else {
					repeatedSmall.put(v.getVal(), null);
				}
			});

			player.getTi().iterator().forEachRemaining(v -> {
				if (v.getVal() > 100) {
					repeatedBig.put(v.getVal(), null);
				}
				else {
					repeatedSmall.put(v.getVal(), null);
				}
			});

			boolean isGroupHu = repeatedSmall.keySet().stream().anyMatch(v -> repeatedBig.containsKey(v + 100));

			if (isGroupHu) {
				mtList.put(LOUDI_MINGTANG_TUANHU, 0);
			}
		}

        Map<Integer,Integer> mtListRes = new HashMap<>();

        //特殊：三提五坎不与自摸叠加。
        if ((table.getGameModel().getSpecialPlay().isSinceTouchAdd3Score() || table.getGameModel().getSpecialPlay().isSinceTouchFanBei())
                && table.getGameModel().getSpecialPlay().isThreeTiFiveKan()
//                && table.getLastWinSeat() == player.getSeat()
                && mtList.containsKey(LOUDI_MINGTANG_THREE_TI_FIRE_KAN)) {
            boolean isAdd = true;

			Iterator<Entry<Integer, Integer>> iterator = mtList.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Integer> next = iterator.next();
				if ((next.getKey()== LOUDI_MINGTANG_ZIMO || next.getKey() == LOUDI_MINGTANG_ZIMO_ADD)) {
                    isAdd = false;
                    LogUtil.printDebug("特殊：庄家三提五坎不与自摸叠加, 移除三提五坎");
                }
                if (isAdd) {
                    mtListRes.put(next.getKey(),next.getValue());
                }
            }
        }

//        boolean removeRedHu = false;
//        if (mtList.contains(LOUDI_MINGTANG_DAHONGHU)) {
//            Iterator<Integer> iterator = mtList.iterator();
//            while (iterator.hasNext()) {
//                Integer mt = iterator.next();
//                boolean isAdd = false;
//                //地胡+红胡 合并名堂
//                if (mt == LOUDI_MINGTANG_DIHU) {
//                    mtListRes.add(LOUDI_MINGTANG_DIHU_HONG);
//                    isAdd = removeRedHu = true;
//                } else if (mt == LOUDI_MINGTANG_SANSHIHUXI_FANBEI) { //三十胡息+红胡 合并名堂
//                    mtListRes.add(LOUDI_MINGTANG_SANSHIHUXI);
//                    isAdd = removeRedHu = true;
//                }
//
//                if (!isAdd)
//                    mtListRes.add(mt);
//            }
//        }

//        if (removeRedHu) {
//            mtListRes.removeIf(v -> v == LOUDI_MINGTANG_DAHONGHU);
//        }

        return MapUtils.isEmpty(mtListRes) ? mtList : mtListRes;
    }

    private static boolean isPPHu(CdePaohuziPlayer player,PaohzCard huCard) {
        
        boolean duizi = true;
        for (CardTypeHuxi type : player.getCardTypes()) {
            if(type.getAction()==PaohzDisAction.action_chi){
            	duizi = false;
            }
        }
        if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
            for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
//                if(type.getHux()==0){
//                }
                if(type.getAction()==0){
                	if(type.getCardIds().size()>2){
                		duizi=false;
                	}
                	else if(type.getCardIds().size()==2){
                    	int val1 = PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal();
                    	int val2= PaohzCard.getPaohzCard(type.getCardIds().get(1)).getVal();
                    	if(val1!=val2){
                    		duizi=false;
                    	}
                	}
//                	int val1 = PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal();
//                	int val2= PaohzCard.getPaohzCard(type.getCardIds().get(1)).getVal();
//                	if(val1!=val2){
                		//duizi=false;
//                	}
                }
            }
        }
        
        
        
        
        
        return duizi;
    }

    /**
     *@description 找到牌组中所有大于3张的牌组
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    public static Map<Integer, Integer> findCountGeTwo(List<PaohzCard> cards) {
        Map<Integer, Integer> res = new HashMap<Integer, Integer>();
        cards.sort((v1, v2) -> Integer.valueOf(v1.getPai()).compareTo(Integer.valueOf(v2.getPai())));

        //最低匹配数量
        int minMathCount = 2;
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

            res.merge(next.getVal(), repeated, (oV, nV) -> nV);
        }
        return res;
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
            if (!(gameModel != null && gameModel.getSpecialPlay().isOneFiveTen())) {
                clone[clone.length - 1] = null;
                clone[clone.length - 2] = null;
            }

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
        //特殊组合1510
        arrays[++index] = new Integer[]{1, 5, 10};
        arrays[++index] = new Integer[]{101, 105, 110};
        return arrays;
    }

    /**
     * @param
     * @return
     * @description 计算名堂分, 番数, 分数=囤数*番数(名堂)。 囤数=胡息/3 , 这里直接计算胡息
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static int calcMingTangFen(int totalTun, CdePaohuziTable table, Map<Integer,Integer> mt) {
        //总囤
        int totalTunNumber = totalTun;
		int totalFan = 0;  //总番数
        //额外增加的胡息
        int addHuXi = 0;
        //默认没有大六八小六八区分
		int bigSexEightIndex = table.getGameModel().bigSexEightMinSexEight();

        //不叠加翻倍的番数仅生效一次, 叠加可重复算
        int repeatedIndex = (table).getGameModel().getSpecialPlay().isRepeatedEffect() ? 99999 : 1;

		Iterator<Entry<Integer, Integer>> iterator = mt.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> kv = iterator.next();
			BigMapEntry<Integer, String, int[]> currentCalc = SCORE_CALC.get(kv.getKey());
            if (currentCalc != null) {
                //名堂分不叠加
				if (currentCalc.getE().equals("*") && --repeatedIndex >= 0) {           //*代表乘积
//					totalTunNumber *= (currentCalc.getV()[currentCalc.getV().length > bigSexEightIndex ? bigSexEightIndex : 0] + kv.getValue());
					totalFan += (currentCalc.getV()[currentCalc.getV().length > bigSexEightIndex ? bigSexEightIndex : 0] + kv.getValue());
					//多红多番 -2番
					if(table.getGameModel().getMingTangPlayT()==3&&kv.getKey()!=LOUDI_MINGTANG_DAHONGHU){
						totalFan -=2;
					}
					
					LogUtil.printDebug("名堂分翻倍 : {}, {} ,{}", currentCalc.getV(), currentCalc.getK(), totalFan);
				}else if (currentCalc.getE().equals("+")) {    //+代表直接增加胡息
					addHuXi += (currentCalc.getV()[currentCalc.getV().length > bigSexEightIndex ? bigSexEightIndex : 0] + kv.getValue());
					LogUtil.printDebug("名堂分增加 :{},  {}, {}", currentCalc.getV(), currentCalc.getK(), addHuXi);
				}
            }
        }

		int res = totalTunNumber + addHuXi;

		res *= Math.max(totalFan, 1);

//		if (table.getGameModel().getTopScore() > 0 && table.getGameModel().getTopScore() < res) {
//			res = table.getGameModel().getTopScore();
//		}

		return res;
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

}
