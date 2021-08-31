package com.sy599.game.qipai.axphz254.rule;

import com.sy599.game.qipai.axphz254.bean.AxPaohuziPlayer;
import com.sy599.game.qipai.axphz254.bean.AxPaohuziTable;
import com.sy599.game.qipai.axphz254.bean.CardTypeHuxi;
import com.sy599.game.qipai.axphz254.bean.GameModel;
import com.sy599.game.qipai.axphz254.bean.PaohzDisAction;
import com.sy599.game.qipai.axphz254.constant.HuType;
import com.sy599.game.qipai.axphz254.constant.PaohzCard;
import com.sy599.game.qipai.axphz254.tool.PaohuziTool;
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

    public static final int DUIZI_HU = 1;//对子胡：七句全由对子组成
    public static final int WU_DUI_HU = 2;// 乌对：七句全由黑字对子组成
    public static final int WU_HU = 3;//乌胡：胡牌时没有红字
    public static final int DIAN_HU = 4;//点胡：胡牌时只有一个红字
    public static final int YIN_HU = 5;//印胡：任意红字四个团圆，每多一个团圆多一番
    public static final int CHUN_YIN_HU = 6;//纯印胡：任意红字四个团圆,且没有零散红字息
    public static final int HONG_HU = 7;//红胡：胡牌后红字的个数有10个
    
    public static final int DUO_HONG_HU = 8;//多红：在红胡的基础上 每多一个红

    public static final int MAN_YUAN_HUA_HU = 9;//满园花：七句中每句必须有红字
    
    public static final int DA_ZI_HU = 10;//大字胡:胡牌都是大字
    public static final int XIAO_ZI_HU = 11; // 小字胡：胡牌都是小字
    public static final int ZHUO_HU = 12; //卓胡：两个四字团圆
    public static final int JIE_MEI_ZHUO_HU = 13; //姐妹卓胡：1和2、2和3等以此类推的两个四字团圆
    public static final int SAN_LUAN_ZHUO_HU = 14; //三乱卓：任意三个字,四字团圆

    public static final int JIE_MEI_ZHUO_DAI_TUO = 15; //姐妹卓带拖：姊妹卓再加一个字团圆

    public static final int DIE_SUN_ZHUO = 16; //爹孙卓:123、234等以此类推的四字团圆

    public static final int SI_LUAN_ZHUO = 17; //四乱卓：任意四个字,四字团圆

    public static final int DIE_SUN_ZHUO_DAI_TUO = 18;//嗲孙卓帯拖:嗲孙卓再加一个字团圆

    public static final int HAI_DI_HU = 19;//海底胡, 最后一个字胡

    public static final int DAN_DIAO = 20;//单调

    public static final int ZHEN_BA_PENG_TOU = 21;//真八碰头:大捌/小八团圆


    public static final int JIA_BA_PENG_TOU = 22;//假八碰头：除大捌/小八团圆外的大小字团圆

    public static final int BEI_KAO_BEI = 23;//背靠背：听牌后只要手里有两对牌，且这两对牌点数相同，胡了其中一张
    
    public static final int SHOU_QIAN_SHOU = 24;//手牵手：12、23、34以此类推的两个对子胡牌

    public static final int LONG_BAI_WEI = 25;//龙摆尾：大抬大壹团圆或小ー小十团圆

    public static final int KA_WEI = 26;//卡煨：一对字胡牌,必须偎好10息
    public static final int TIAN_HU = 27;//天胡
	public static final int QUAN_QIU_REN = 28;//全求人：打的最后手上只剩一张牌单丁胡牌
	public static final int DING_DUI = 29;//顶对：胡牌每组都有息,比如对子胡胡小七小八,出小八胡牌就是顶对
	public static final int PIAO_DUI = 30;//飘对：对子胡胡牌手里只有一对红字并且碰下来

	public static final int S_X_W_QIAN_NIAN = 31;//上下五干年：桌面上5硬息,手上5硬息
	public static final int QUAN_HEI = 32;//全黑：庄家或闲家摸完牌后全是黑字,算天胡的一种
	public static final int WU_XI_HU = 33;//无息胡：庄家或闲家摸完牌后，手牌随便凑，永远打不出10硬息，算天胡的一种
	public static final int LIU_DUI_HONG = 34;//六对红：庄家或闲家摸完牌后，手上有6对红字，算天胡的一种
	public static final int JIU_DUI = 35;//九对：庄家或闲家摸完牌后，手上有6对红字，算天胡的一种
	public static final int JI_DING = 36;//鸡丁：1223丁小2胡牌叫鸡丁

	public static final int LIANG_ZHA_DAN = 37;//两炸弹：起手有2组或4组相同的牌，算天胡的一种
	public static final int SI_BIAN_DUI = 38;//四边对：一十壹拾各一对，算天胡的一种
	public static final int BIAN_KAN = 39;//边坎：只能胡一张牌叫边(1、2胡3或9、10胡8)，胡中间的牌叫坎
	
	public static final int ZHEN_BEI_KAO_BEI = 40;//真背靠背：八捌两对胡牌
	public static final int FENG_BAI_WEI = 41;//凤摆尾：一(壹)十(拾)两对胡牌
	
	public static final int KA_HU = 42;//卡胡：刚好10胡息胡牌

	public static final int ZI_MO_HU = 43;//自摸胡：自摸胡牌

	public static final int XIANG_XIANG_XI = 44;//项项息：每组牌型都有息

	public static final int DUI_DAO_HU = 45;//对到胡：中间隔一数字的两对对子胡牌，比如小二、小四对倒

	public static final int ZHUO_XIAO_SAN = 46;//捉小三：自己摸的胡的字是小三

	public static final int ER_XI_MAN_YUAN_HUA = 47;//二息满园花：红字做麻雀但不能转弯，其全部为带红字的话句

	public static final int JIA_FENG_BAI_WEI = 48;//假凤摆尾：小ー大十对倒胡或小十大一对倒胡

	public static final int JIA_LONG_BAI_WEI = 49;//假龙摆尾：小ー大十团圆或小十大ー团圆

	public static final int YUAN_YUAN_DING = 50;//圆圆丁：一坎拆或吃成两句话后第三张牌单丁第四张，如原本手上有一坎大贰后拆成大壹贰叁，叁肆伍，贰丁贰

	public static final int XIN_LIAN_XIN = 51;//心连心：两句话以中间相同字穿插往两边辐射，如 123 + 345 
	
	public static final int ER_LONG_XI_ZHU = 52;//二龙戏珠：胡牌后有小字一大字壹贰叁，两句话后再无其他红字

	public static final int MEI_NV_CAI_DAN_CHE = 53;//美女踩单车：全求人后只能剩下红字单丁

	public static final int HONG_ZHA_DAN = 54;//红炸弹：天胡，四张相同点数的红字且没有黑字可和红字组成一句话的牌组

	public static final int HEI_ZHA_DAN = 55;//黑炸弹：天胡，四张相同点数的黑字且没有其他黑字或红字可和点数相同的黑字组成一句话的牌组

	public static final int YOU_XI_TIAN_HU = 56;//有息天胡：起手就是胡牌牌型

	public static final int YI_TIAO_LONG = 57;//一条龙：结算时有大小同字，比如123,456,789,三句话且满足10硬息后单丁小十或大拾胡牌

	public static final int ZU_SUN_ZHUO = 58;//祖孙卓：四连续的四个点数相同的牌组成的牌型，有祖孙卓不算三乱卓不算两乱卓
	
	public static final int SHI_SHI_HU = 59;//啫啫胡：出胡字不胡，等下一张相同字在胡
	
	public static final int BIAN_DING = 60;//边丁：有两头字胡牌且一头有偎的可能字
	public static final int KAN_HU = 62;//坎胡：一句话胡中间的字
	
	public static final int DUI_DAO_HU_2 = 63;//对到胡：
	
    public static  HashMap<Integer, HashMap<Integer, int[]>> SCORE_CALC = new HashMap<>();
    
    static{
    		 
    		 addMingtangData(DUIZI_HU, 2, 3, 4, 8, 10, 0, 0);
         	//* -> 0 ;+ ->1
    		 addMingtangData(WU_DUI_HU, 2, 3, 6, 10, 15, 0, 0);
    		 addMingtangData(WU_HU, 2, 3, 3, 6, 6, 0, 0);
    		 addMingtangData(DIAN_HU, 2, 3, 2, 4, 4, 0, 0);
    		 addMingtangData(YIN_HU, 2, 3, 10, 30, 30, 1, 0);
    		 addMingtangData(DUI_DAO_HU_2, 2, 3, 30, 30, 30, 1, 0);
    		 
    		 addMingtangData(CHUN_YIN_HU, 2, 3, 150, 150, 150, 1, 2);
    		 addMingtangData(HONG_HU, 2, 3, 10, 20, 30, 1, 0);
    		 addMingtangData(DUO_HONG_HU, 2, 3, 10, 20, 30, 1, 0);
    		 addMingtangData(MAN_YUAN_HUA_HU, 2, 3, 50, 200, 200, 1, 0);
    		 addMingtangData(DA_ZI_HU, 2, 3, 50, 200, 200, 1, 0);
    		 addMingtangData(XIAO_ZI_HU, 2, 3, 50, 200, 200, 1, 0);
    		 addMingtangData(ZHUO_HU, 2, 3, 20, 40, 40, 1, 1);
    		 addMingtangData(JIE_MEI_ZHUO_HU, 2, 3,40, 40, 40, 1, 1);
    		 addMingtangData(SAN_LUAN_ZHUO_HU, 2, 3,60, 100,100, 1, 1);
    		 addMingtangData(JIE_MEI_ZHUO_DAI_TUO, 2, 3,80, 120,150, 1, 1);
    		 addMingtangData(DIE_SUN_ZHUO, 2, 3,150, 300,300, 1, 1);
    		 addMingtangData(SI_LUAN_ZHUO, 2, 3,100, 300,300, 1, 1);
    		 addMingtangData(DIE_SUN_ZHUO_DAI_TUO, 2, 3,200, 400,400, 1, 1);
    		 addMingtangData(HAI_DI_HU, 2, 3,20, 30,50, 1, 1);
    		 addMingtangData(DAN_DIAO, 2, 3,20, 30,30, 1, 1);
    		 addMingtangData(ZHEN_BA_PENG_TOU, 2, 3,200, 300,300, 1, 1);
    		 addMingtangData(JIA_BA_PENG_TOU, 2, 3,100, 150,150, 1, 1);
    		 addMingtangData(BEI_KAO_BEI, 2, 3,20, 50,50, 1, 1);
    		 addMingtangData(SHOU_QIAN_SHOU, 2, 3,20, 50,50, 1, 1);
    		 addMingtangData(LONG_BAI_WEI, 2, 3,100, 150,150, 1, 1);
    		 addMingtangData(KA_WEI, 2, 3,20, 50,50, 1, 1);
    		 addMingtangData(TIAN_HU, 2, 3,50, 100,150, 1, 1);
    		 addMingtangData(QUAN_QIU_REN, 2, 3,100, 150,150, 1, 1);
    		 addMingtangData(DING_DUI, 2, 3,20, 50,50, 1, 1);
    		 addMingtangData(PIAO_DUI, 2, 3,20, 50,50, 1, 1);
    		 addMingtangData(JI_DING, 2, 3,50, 100,100, 1, 1);
    		 addMingtangData(S_X_W_QIAN_NIAN, 2, 3,20, 50,50, 1, 1);
    		 addMingtangData(QUAN_HEI, 2, 3,150, 150,150, 1, 3);
    		 addMingtangData(WU_XI_HU, 2, 3,150, 150,150, 1, 3);
    		 addMingtangData(LIU_DUI_HONG, 2, 3,150, 150,150, 1, 3);
    		 addMingtangData(JIU_DUI, 2, 3,150, 150,150, 1, 3);
    		 addMingtangData(LIANG_ZHA_DAN, 2, 3,150, 150,150, 1, 4);
    		 addMingtangData(SI_BIAN_DUI, 2, 3,150, 150,150, 1, 3);
    		 addMingtangData(BIAN_KAN, 2, 3,30, 30,30, 1, 3);
    		 
    		 addMingtangData(KAN_HU, 2, 3,30, 30,30, 1, 3);
    		 addMingtangData(ZHEN_BEI_KAO_BEI, 2, 3,100, 100,100, 1, 3);
    		 addMingtangData(FENG_BAI_WEI, 2, 3,50, 50,50, 1, 3);
    		 addMingtangData(KA_HU, 2, 3,50, 50,50, 1, 3);
    		 addMingtangData(ZI_MO_HU, 2, 3,20, 40,40, 1, 1);
    		 removeCORE_CALCData(ZI_MO_HU, 4);
    		 addMingtangData(XIANG_XIANG_XI, 2, 3,0, 50,50, 1, 4);
    		 addMingtangData(XIANG_XIANG_XI, 2, 3,0, 0,30, 1, 4);
    		 addMingtangData(ZHUO_XIAO_SAN, 2, 3,20, 50,30, 1, 1);
    		 addMingtangData(ZHUO_XIAO_SAN, 2, 3,20, 50,30, 1, 1);
    		 
    		 addMingtangData(DUI_DAO_HU, 2, 3,0, 0,30, 1, 4);
    		 
    		 
    		 removeCORE_CALCData(ZHUO_XIAO_SAN, 4);
    		 addMingtangData(ER_XI_MAN_YUAN_HUA, 2, 3,0, 150,150, 1, 3);
    		 addMingtangData(JIA_FENG_BAI_WEI, 2, 3,0, 0,30, 1, 4);
    		 addMingtangData(JIA_LONG_BAI_WEI, 2, 3,0, 0,100, 1, 4);
    		 addMingtangData(YUAN_YUAN_DING, 2, 3,0, 0,50, 1, 4);
    		 addMingtangData(XIN_LIAN_XIN, 2, 3,0, 0,30, 1, 4);
    		 addMingtangData(ER_LONG_XI_ZHU, 2, 3,0, 0,50, 1, 4);
    		 addMingtangData(MEI_NV_CAI_DAN_CHE, 2, 3,0, 0,50, 1, 4);
    		 addMingtangData(HONG_ZHA_DAN, 2, 3,0, 0,200, 1, 3);
    		 removeCORE_CALCData(HONG_ZHA_DAN, 5);
    		 addMingtangData(HEI_ZHA_DAN, 2, 3,0, 0,100, 1, 3);
    		 removeCORE_CALCData(HEI_ZHA_DAN, 5);
    		 addMingtangData(YOU_XI_TIAN_HU, 2, 3,0, 80,80, 1, 2);
    		 addMingtangData(YI_TIAO_LONG, 2, 3,0, 150,150, 1, 2);
    		 addMingtangData(ZU_SUN_ZHUO, 2, 3,0, 500,500, 1, 2);
    		 addMingtangData(SHI_SHI_HU, 2, 3,0, 20,20, 1, 2);
    		 addMingtangData(BIAN_DING, 2, 3,20, 50,50, 1, 1);
    	
    }
    


    /**
     * 名堂
     *
     * @param player 胡牌玩家
     * @return  map k->名堂 v->额外加分这个分数的运算取决于名堂内的分数是增加还是翻番
     */
    public static HashMap<Integer,Integer> calcMingTang(AxPaohuziPlayer player) {

    	HashMap<Integer,Integer> mtList = new HashMap<Integer,Integer>();
        List<PaohzCard> allCards = new ArrayList<>();
        boolean hhx= true;
        boolean jiang =false;
        boolean manyuanHua =true;
        AxPaohuziTable table = player.getPlayingTable(AxPaohuziTable.class);
        PaohzCard huCard = null;
        if(player.getHu() != null){
        	huCard = player.getHu().getCheckCard();
        	if(huCard==null){
        		huCard = table.getNowDisCardIds().get(0);
        	}
        }
        List<PaohzCard> hucardVals = new ArrayList<>();
      //  HashMap<Integer,Integer> cardMap = new HashMap<>();
        boolean kapaFlag =true;
        boolean kawei= false;
        int redDui = 0;
        int jiangVal =0;
        int fengBaiWei = 0;
        List<Integer> yjhTypes = new ArrayList<>();
        List<Integer> yjhTypes2 = new ArrayList<>();
        List<Integer> redPWK = new ArrayList<>();
        for (CardTypeHuxi type : player.getCardTypes()) {
			List<PaohzCard> huTypeCards = PaohuziTool.toPhzCards(type.getCardIds());
			
			//putCardToMap(cardMap, huTypeCards);
			List<PaohzCard> redCards = PaohuziTool.findRedPhzs(huTypeCards);
			if (redCards.isEmpty()) {
				manyuanHua = false;
			}else{
//				if(type.getHux()>0){
					redPWK.add(redCards.get(0).getVal());
//				}
				redDui++;
			}
			yjhTypes.add(getYiJuHuaType(huTypeCards));
            allCards.addAll(huTypeCards);
            if(type.getHux()==0){
            	hhx =false;
            }
            if(huCard!=null&&(type.getAction()==PaohzDisAction.action_zai||type.getAction()==PaohzDisAction.action_chouzai)){
            	if(huCard.getVal()==PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal()&&type.getCardIds().contains(huCard.getId())){
            		kawei = true;
            		hucardVals.addAll(PaohuziTool.getHuCardVals(type.getCardIds(), huCard));
            		fengBaiWei = 1;
            	}
            }
        }
        
        
        boolean  bianHu = false;
        boolean  kanHu = false;
        boolean decPengXi = false;
        if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
            for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
            	List<PaohzCard> huTypeCards = PaohuziTool.toPhzCards(type.getCardIds());
            //	putCardToMap(cardMap, huTypeCards);
    			List<PaohzCard> redCards = PaohuziTool.findRedPhzs(huTypeCards);
    			if (redCards.isEmpty()) {
    				manyuanHua = false;
    			}else{
//    				if(type.getHux()>0){
    					redPWK.add(redCards.get(0).getVal());
//    				}
    				redDui++;
    			}
    			int yjtype = getYiJuHuaType(huTypeCards);
    			yjhTypes.add(yjtype);
    			yjhTypes2.add(yjtype);
    			if(!bianHu){
    				bianHu = isBianHu(yjtype, huCard) ;   			
    			}
    			if(!kanHu&&type.getAction()==0){
    				kanHu = isKanHu(huTypeCards, huCard) ;   			
    			}
    			
                allCards.addAll(huTypeCards);
                if(type.getHux()==0){
                	hhx =false;
                	
                }
                if(type.getCardIds().size()==2){
                	int val = PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal();
                	if(huCard!=null&&huCard.getVal()==val){
                		jiang = true;
                	}
                	jiangVal= val;
            	}
                
                if(huCard!=null){
                    if((type.getAction()==PaohzDisAction.action_zai||type.getAction()==PaohzDisAction.action_chouzai||type.getAction()==PaohzDisAction.action_peng)){
                    	if(huCard.getVal()==PaohzCard.getPaohzCard(type.getCardIds().get(0)).getVal()){
                    		fengBaiWei = 1;
                    		hucardVals.addAll(PaohuziTool.getHuCardVals(type.getCardIds(), huCard));
                    	}
                    	
                    	if(type.getAction()==PaohzDisAction.action_peng){
                    		decPengXi = true;
                    	}
                    }
                	   //背靠背的牌
                    kapaFlag = checkBeiKaoBei(huCard, hucardVals, type);
                    
                }
            }
        }
        

        HuType huType = table.calcHuType();

        //红牌数量
        int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(allCards)).orElse(Collections.emptyList()).size();
        //小牌数量
        int smallCardsSize = Optional.ofNullable(PaohuziTool.findSmallPhzs(allCards)).orElse(Collections.emptyList()).size();
        
        PaohzCardIndexArr valArr = PaohuziTool.getMax(allCards);
        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
        List<Integer> tuanYuans =  null;
        if(index3!=null){
        	tuanYuans = index3.getValList();
        }
        
        List<PaohzCard> jiangs =  PaohuziTool.findPhzByVal(allCards, jiangVal);
        int zhuanWangXi = 0;
        if(jiangs.size()==3){
        	if(jiangs.get(0).isRed()){
        		zhuanWangXi=1;
        	}else{
        		zhuanWangXi=2;
        	}
        	
        	player.changeTuanYuanXi(zhuanWangXi);
        }
        
        
        
        if(tuanYuans!=null){
        	int[] yins = getYing(tuanYuans, jiangVal, redPWK);
         	if(yins[0]>0){
        		mtList.put(YIN_HU,yins[0]-1);
        	}
        	
        	if(yins[11]>0){
        		mtList.put(CHUN_YIN_HU,yins[11]-1);
        	}
        	if(table.getGameModel().getSpecialPlay().isLongBaiWei()&&tuanYuans.contains(110)&&tuanYuans.contains(101)||(tuanYuans.contains(10)&&tuanYuans.contains(1))){
        		mtList.put(LONG_BAI_WEI,0);
        	}
        	if(tuanYuans.contains(110)&&tuanYuans.contains(1)||(tuanYuans.contains(10)&&tuanYuans.contains(101))){
        		mtList.put(JIA_LONG_BAI_WEI,0);
        	}
        	
        	int zhuos[]  = getZhuoType(tuanYuans);
        	if(yins[8]==2){
        		mtList.put(ZHEN_BA_PENG_TOU,0);
        	}
        	if(zhuos[1]>=3){
        		mtList.put(SI_LUAN_ZHUO,0);
        	}else if(zhuos[1]>=2){
        		mtList.put(SAN_LUAN_ZHUO_HU,0);
        	}else if(zhuos[1]>=1){
        		mtList.put(ZHUO_HU,0);
        	}
        	
        	if(zhuos[0]>=3){
        		mtList.put(ZU_SUN_ZHUO,0);
        	}else if(zhuos[0]>=2){
        		if(zhuos[1]>=1){
        			mtList.put(DIE_SUN_ZHUO_DAI_TUO,0);
        			
        		}else{
        			mtList.put(DIE_SUN_ZHUO,0);
        		}	
        	
        	}else if(zhuos[0]>=1){
        		if(zhuos[1]>=1){
        			mtList.put(JIE_MEI_ZHUO_DAI_TUO,0);
        		}else{
        			mtList.put(JIE_MEI_ZHUO_HU,0);
        		}
        	}
        	
        	if(isJiaBaPengTou(yins)){
        		mtList.put(JIA_BA_PENG_TOU,0);
        	}
        	
        }
        
        
        
        if(fengBaiWei==1&&huCard!=null){
        	if(huCard.getVal()==1&&jiangVal==110||huCard.getVal()==110&&jiangVal==1||huCard.getVal()==101&&jiangVal==10||huCard.getVal()==10&&jiangVal==101){
        		mtList.put(JIA_FENG_BAI_WEI,0);
        	}else if(Math.abs(huCard.getVal()-jiangVal)==9){
        		mtList.put(FENG_BAI_WEI,0);
        	}
        	if(Math.abs(huCard.getVal()-jiangVal)==2){
        		mtList.put(DUI_DAO_HU,0);
        	}
        	
        	mtList.put(DUI_DAO_HU_2,0);
        	
        }
        
        if(kawei){
        	mtList.put(DUI_DAO_HU_2,0);
        }
        
        if(kanHu){
        	mtList.put(KAN_HU,0);
        }else if(bianHu){
        	mtList.put(BIAN_KAN,0);
        }
        
        
      //碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
        boolean duiziHu = isPPHu(player,huCard);
      	
      
      	if(redCardCount == 0){
        	mtList.put(WU_HU,0);
        }else if(redCardCount == 1){
        	mtList.put(DIAN_HU,0);
        }else if(redCardCount>=10){
        	mtList.put(redCardCount>10?DUO_HONG_HU:HONG_HU,redCardCount-10);
        }
      	
      	
    	if(redCardCount == 0&&duiziHu){
      		mtList.put(WU_DUI_HU,0);
      		mtList.remove(WU_HU);
      	}else if (duiziHu) {
      		mtList.put(DUIZI_HU,0);
        }
      	
      	
      	//findCountGeTwo
      	
      	if(jiang){
      		mtList.put(DAN_DIAO, 0);
      		if(huCard!=null){
      			boolean addDing = false;
      			//边丁
      			if(yjhTypes2.contains(1)&&huCard.getVal()==3||yjhTypes2.contains(11)&&huCard.getVal()==103||yjhTypes2.contains(17)&&huCard.getVal()==109||yjhTypes2.contains(7)&&huCard.getVal()==9){
          			mtList.put(BIAN_DING, 0);
          			mtList.remove(ZHUO_XIAO_SAN);
          			addDing = true;
          		}
      			//一条龙
          		if(huCard.getPai()==10){
          			if(yjhTypes.contains(1)&&yjhTypes.contains(4)&&yjhTypes.contains(7)||(yjhTypes.contains(11)&&yjhTypes.contains(14)&&yjhTypes.contains(17))){
          				mtList.put(YI_TIAO_LONG, 0);
              		}
          		}
          		boolean jiding = isJiDing(yjhTypes2, huCard);
          		if(jiding){
          			mtList.put(JI_DING, 0);
          			addDing = true;
          		}
          		
          		if(player.getChaiKanOrLCardVals().contains(huCard.getVal())){
          			mtList.put(YUAN_YUAN_DING, 0);
          			addDing = true;
          		}
          		
          		if(addDing){
          			mtList.remove(DAN_DIAO);
          		}
      		}
      	
      	}
      	
        if(manyuanHua){
       	 mtList.put(MAN_YUAN_HUA_HU,0);
       }
      	
      	
      	//2息满园花，2湖息 2红字 一定是二息满园花
      	if(player.getTotalHu()==2&&manyuanHua){
      		mtList.put(ER_XI_MAN_YUAN_HUA, 0);
      		mtList.remove(MAN_YUAN_HUA_HU);
      	}
      	
      	if(table.getGameModel().getSpecialPlay().isPiaoDui()&&duiziHu&&redDui==1&&!isRead(jiangVal%100)){
      			mtList.put(PIAO_DUI, 0);
      	}
      	
//      	if(yjhTypes.contains(1)&&yjhTypes.contains(11)&&redCardCount==2){
      	
      	if(isErLongXiZhu(yjhTypes, redCardCount)){
      		mtList.put(ER_LONG_XI_ZHU, 0);
      	}
      	
      	if(player.getPassHuCount()>0){
      		mtList.put(SHI_SHI_HU, player.getPassHuCount()-1);
      		
      	}
      	
       if ( smallCardsSize > 18) {
    	   mtList.put(XIAO_ZI_HU, 0);
       }else if(smallCardsSize==0){
    	   mtList.put(DA_ZI_HU, 0);
       }
      	
         if (kawei && player.getTotalHu() == 10) {
        	 mtList.put(KA_WEI , 0);
        }else if(player.getHandPais().size()==1&&table.getGameModel().getSpecialPlay().isQuanQiuR()){
        	 mtList.put(QUAN_QIU_REN , 0);
        }
         if(player.getHandPais().size()==1&& player.getHandPhzs().get(0).isRed()){
        	 mtList.put(MEI_NV_CAI_DAN_CHE , 0);
        }
         
         int sxwqn = player.getOutHuxi()+player.getZaiHuxi();
         if(decPengXi){
        	 sxwqn +=3;
         }else if(jiang&&isRead(jiangVal%100)){
        	 sxwqn +=2;
         }
         
         
         if(player.getTuanYuanXi()>0){
        	 sxwqn+=(player.getTuanYuanXi()-player.getFirstTuanYuanXi());
         }
         
         
     
         
         
         if(table.getGameModel().getSpecialPlay().isSxWuQianNian()&&player.getTotalHu()==10&&sxwqn==5){
        	 mtList.put(S_X_W_QIAN_NIAN , 0);
         }
         if(player.getTotalHu() == 10){
        	 mtList.put(KA_HU , 0);
         }
         
//         if(player.getHandPais().size()==2||player.getHandPais().size()==4){
        	 if(huCard!=null &&Math.abs(huCard.getVal()-jiangVal)==1){
        		 mtList.put(SHOU_QIAN_SHOU , 0);
             }
             
//         }
         
         if(isXinLianXin(yjhTypes)){
        	 mtList.put(XIN_LIAN_XIN , 0);
         }
         
 		//行行息：胡牌时每一个门子都有息8番。
			if ( hhx) {
				//&&huCard!=null &&!huCard.isRed()
				if(isRead(jiangVal%100)&&table.getGameModel().getSpecialPlay().isDingDui()){
					mtList.put(DING_DUI, 0);
				}else{
					mtList.put(XIANG_XIANG_XI , 0);
				}
			}
         
			if(huCard!=null &&huCard.getVal()==3&&table.getGameModel().getSpecialPlay().isHuoZxiaoSan()){
				 mtList.put(ZHUO_XIAO_SAN , 0);
			}
			
	        if (huType == HuType.ZI_MO) {
	            mtList.put(ZI_MO_HU,0);
	        }
	        
	        
	      

			//海胡
			if( table.getLeftCards().size()==0){
				mtList.put(HAI_DI_HU,0);
			}

			//背靠背：听牌后只要手里有两对牌，且这两对牌点数相同，胡了其中一张，6番。
			if ( hucardVals.size()==5&&kapaFlag) {
				if (huCard!=null&&huCard.getPai()==8) {
					mtList.put(ZHEN_BEI_KAO_BEI,0);
				}else{
					mtList.put(BEI_KAO_BEI,0);
				}
				
			}
			


        return  mtList ;
    }

	private static boolean checkBeiKaoBei(PaohzCard huCard, List<PaohzCard> hucardVals,
			CardTypeHuxi type) {
		boolean kapaFlag = true;
		if(type.getAction()!=PaohzDisAction.action_chi&&type.getAction()!=PaohzDisAction.action_kan&&type.getAction()!=PaohzDisAction.action_zai){
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
		return kapaFlag;
	}
    
    public static boolean isRead(int pai){
    	if(pai==2||pai==7||pai==10){
    		return true;
    	}
    	return false;
    }
    private static boolean isBianHu(int type, PaohzCard card){
    	
    	if(card==null){
    		return false;
    	}
    	
    	if(type==1&&card.getVal()==3||type==11&&card.getVal()==103||type==7&&card.getVal()==9||type==17&&card.getVal()==109){
    		return true;
    	}
    	return false;
    	
    }
    
    private static boolean isErLongXiZhu( List<Integer> yjhTypes,int redCount){
    	if(redCount!=2){
    		return false;
    	}
    	for(Integer type: yjhTypes){
    		if(type>10){
    			int type2 = type-10;
    			if(yjhTypes.contains(type2)){
    				return true;
    			}
    		}
    	}
    	return false;
    	
    }
    private static boolean isJiaBaPengTou(int[] tuanyuan){
    	for(int i=0;i<tuanyuan.length;i++){
    		if(i>0&i!=8&&i<=10){
    			if(tuanyuan[i]>=2)
    				return true;
    		}
    	}
    	return false;
    }
    
    private static boolean isKanHu(List<PaohzCard> cards, PaohzCard huCard){
    	if(huCard==null||cards.size()!=3){
    		return false;
    	}
    	List<Integer> vals = new ArrayList<Integer>();
    	for(PaohzCard card: cards){
    		vals.add(card.getVal());
    	}
    	Collections.sort(vals);
    	
    	if(huCard.getVal()==vals.get(1)){
    		return true;
    	}
    	
    	return false;
    	
    }
    
    
    private static int[] getYing(List<Integer> valCount ,int mqVal,List<Integer> redPWK){
    	int[] ying = new int[12];
    	//将碰 煨过 有印的全部去掉
    	redPWK.removeAll(valCount);
    	for(Integer val: valCount){
    		int pai = val%100;
    		if(pai==2||pai==7||pai==10){
    			if(mqVal ==val){
    				//印胡
//    				ying[0] += 1;
    				continue;
    			}
    			if(redPWK.isEmpty()){
    				ying[11] += 1;
    			}
    			ying[0] += 1;
    		}
    		ying[pai]+=1;
    		
    	}
    	return ying;
    }
    
    
    
    
    
    private static int[] getZhuoType(List<Integer> valCount){
    	int[] res = new int[2];
    	Collections.sort(valCount);
    	for(int i=0;i<valCount.size()-1;i++){
    		if(valCount.get(i+1)-valCount.get(i)==1){
    			res[0] +=1;
    			//lianxuCount++;
    		}else{
    			res[1] +=1;
    			//buLianx++;
    		}
    	}
    	
    	
    	
    	return res;
    	
    	
    }
    
    
    
    /**
     * 鸡丁
     * @param types
     * @param card
     * @return
     */
    private static boolean isJiDing(List<Integer> types, PaohzCard card){
    	if(card.getPai()==10){
    		return false;
    	}
    	for(Integer type: types){
    		if(card.isBig()){
    			if((card.getPai()-(type-10))==1){
    				return true;
    			}
    		}else{
    			if((card.getPai()-type)==1){
    				return true;
    			}
    		}
    		
    	}
    	
    	return false;
    }
    
    private static boolean isXinLianXin(List<Integer> types){
    	Collections.sort(types);
    	for(int i=0;i<types.size()-1;i++){
    		if(types.get(i)==9||types.get(i)==0||types.get(i+1)==9||types.get(i+1)==19||types.get(i)==19){
    			continue;
    		}
    		if(types.get(i+1)>0&& (Math.abs(types.get(i)-types.get(i+1))==2||(i>0&&Math.abs(types.get(i+1)-types.get(i-1))==2))){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    private static int getYiJuHuaType(List<PaohzCard> cards){
    	List<Integer> vals = new ArrayList<Integer>();
    	for(PaohzCard card: cards){
    		vals.add(card.getPai());
    	}
    	Collections.sort(vals);
    	StringBuffer sb = new StringBuffer();
    	for(Integer val : vals){
    		sb.append(val);
    	}
    	String str = sb.toString();
    	
    	int res = 0;
    	switch (str) {
		case "123":
			res = 1;
			break;
		case "234":
			res = 2;
			break;
		case "345":
			res = 3;
			break;
		case "456":
			res = 4;
			break;
		case "567":
			res = 5;
			break;
		case "678":
			res = 6;
			break;
		case "789":
			res = 7;
			break;
		case "8910":
			res = 8;
		default:
			break;
		}
    	
    	//红麻雀
    	if(cards.size()==2&&(vals.contains(2)||vals.contains(7)||vals.contains(10))){
    		res = 9;
    	}
    	
    	if(res>0&&cards.get(0).isBig()){
    		res+=10;
    	}
    	return res;
    }
    
    
    
    private static void putCardToMap(HashMap<Integer,Integer>map,List<PaohzCard> cards){
    	 for (PaohzCard card : cards) {
    		 Integer count = map.get(card.getVal());
    		 if(count==null){
    			 map.put(card.getVal(), 1);
    		 }else{
    			 map.put(card.getVal(), count+1);
    		 }
    		 
    	 }
    	
    	
    }

    private static boolean isPPHu(AxPaohuziPlayer player,PaohzCard huCard) {
        
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

    public static int calcMingTangFen(int totalTun, AxPaohuziTable table, Map<Integer,Integer> mt) {
        //总囤
        int yingXi = 0;
		int addXi = 0;  //总番数
		Iterator<Entry<Integer, Integer>> iterator = mt.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> kv = iterator.next();
			int[] fans = getMingTangFan(kv.getKey(), table.getGameModel().getMingTangPlayT());
			if(fans==null){
				continue;
			}
			int addType = fans[0];
			if(addType==0){
				yingXi += totalTun*fans[1];
			}else{
				if(kv.getKey()==DAN_DIAO&&(mt.containsKey(DUIZI_HU)||mt.containsKey(WU_DUI_HU))){
					addXi +=50;
				}else{
					addXi+=(fans[1]+fans[1]* kv.getValue());
				}
			}
			
        }
		if(yingXi==0){
			yingXi = totalTun;
		}

		int res = yingXi + addXi;


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
     * 
     * @param mingtang
     * @param index
     * @param index2
     * @param index3
     * @param val1
     * @param val2
     * @param val3
     * @param addType
     * @param removeIndex  所选版本没有的名堂
     */
    private static void addMingtangData(int mingtang,int index,int index2,int val1,int val2,int val3,int addType,int removeIndex){
        for(int i=1;i<=5;i++){
        	if(i<=removeIndex){
        		continue;
        	}
        	
        	int val;
        	if(i<=index){
        		val = val1;
        	}else if(i==index2){
        		val = val2;
        	}else{
        		val = val3;
        	}
        	putCORE_CALCData(mingtang, i, new int[]{addType,val});
        }
    	
    }
    
    
    public static int[] getMingTangFan(int key1,int key2){
    	HashMap<Integer, int[]> vals = SCORE_CALC.get(key1);
    	if(vals==null){
    		return null;
    	}
    	return vals.get(key2);
    	
    }
    
    private static void putCORE_CALCData(int key1,int key2,int[] mval){
    	HashMap<Integer, int[]> vals = SCORE_CALC.get(key1);
    	if(vals ==null){
    		vals = new HashMap<>();
    		SCORE_CALC.put(key1, vals);
    	}
    	vals.put(key2, mval);
    }
    private static void removeCORE_CALCData(int key1,int key2){
    	HashMap<Integer, int[]> vals = SCORE_CALC.get(key1);
    	if(vals!=null){
    		vals.remove((Integer)key2);
    	}

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
