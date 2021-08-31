package com.sy599.game.qipai.dtz.constant;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.util.JacksonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DtzzConstants {
	public static boolean isTest = false;

	public static final int state_player_ready = 1;
	public static final int state_player_offline = 2;
	public static final int state_player_diss = 3;

	/** 牌局离线 **/
	public static final int table_offline = 1;
	/** 牌局在线 **/
	public static final int table_online = 2;
	/** 牌局暂离 **/
	public static final int table_afk = 3;
	/** 牌局暂离回来 **/
	public static final int table_afkback = 4;

    /** 快乐四喜：2人玩法 **/
    public static final int play_type_2PERSON_4Xi = 210;
    /** 快乐四喜：3人玩法 **/
    public static final int play_type_3PERSON_4Xi = 211;
    /** 快乐四喜：4人玩法 **/
    public static final int play_type_4PERSON_4Xi = 212;
	/** 3副牌玩法 **/
	public static final int play_type_3POK = 113;
	/** 4副牌玩法 **/
	public static final int play_type_4POK = 114;
	/** 三人3副牌玩法 **/
	public static final int play_type_3PERSON_3POK = 115;
	/** 三人4副牌玩法 **/
	public static final int play_type_3PERSON_4POK = 116; 
	/** 两人3副牌玩法 **/
	public static final int play_type_2PERSON_3POK = 117;
	/** 两人4副牌玩法 **/
	public static final int play_type_2PERSON_4POK = 118; 
	
	
    /**托管**/
    public static final int action_tuoguan = 100;

	public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";

	/** 3副牌的dtz*/
	public static List<Integer> cardList_Dtz = new ArrayList<>();
	/** 3副牌有王的dtz*/
	public static List<Integer> cardList_wang_Dtz = new ArrayList<>();
	/** 4副牌的dtz*/
	public static List<Integer> cardList_Dtz_4 = new ArrayList<>();
    /** 4副牌的不带王*/
    public static List<Integer> cardList_Dtz_4_buDaiWang = new ArrayList<>();
	
//	public static final int AUTO_MAX_TIME = 20000;//进入托管最大等待时间
//	public static final int AUTO_PLAY_TIME = 3000;
//	public static final int AUTO_START_NEXT = 8000;//自动下一局

	static {
		if (GameServerConfig.isDeveloper()) {
			 isTest = false;
		}
		// 方片 1 梅花2 洪涛3 黑桃4
        // 5~13：5到k, 14：A ,15：2,16：小鬼，17：大鬼

		/*打筒子玩法
		 * 三副牌
		 */
		for (int count = 0; count <= 2; count ++) {
			for (int i = 1; i <= 4; i ++) {
				for (int j = 3; j <= 15; j++) {
					if (j != 3 && j != 4) {
						int card = i * 100 + j;
						cardList_Dtz.add(card);
						cardList_wang_Dtz.add(card);
					}
				}
			}
			cardList_wang_Dtz.add(16); //小鬼
			cardList_wang_Dtz.add(17); //大鬼
		}

		/*打筒子玩法
		 * 四副牌
		 */
		for (int count = 0; count <= 3; count ++) {
			for (int i = 1; i <= 4; i ++) {
				for (int j = 3; j <= 15; j++) {
					if (j != 3 && j != 4) {
						int card = i * 100 + j;
						cardList_Dtz_4.add(card);
                        cardList_Dtz_4_buDaiWang.add(card);
					}
				}
			}
			cardList_Dtz_4.add(16); //小鬼
			cardList_Dtz_4.add(17); //大鬼
		}
	}


//	public enum table_state {
//		ready(1), play(2), over(3);
//		private int id;
//
//		private table_state(int id) {
//			this.id = id;
//		}
//
//		public int getId() {
//			return id;
//		}
//	}
//
//	public enum player_state {
//		entry(1), ready(2), play(3), over(4);
//		private int id;
//
//		private player_state(int id) {
//			this.id = id;
//		}
//
//		public int getId() {
//			return id;
//		}
//	}


	public static boolean isPlayDtz(int playType) {
		switch(playType){
			case play_type_3POK:
			case play_type_4POK:
			case play_type_3PERSON_3POK:
			case play_type_3PERSON_4POK:
			case play_type_2PERSON_3POK:
			case play_type_2PERSON_4POK:
				return true;
			default:
				return false;
		}
	}


	/**
	 * 按模式获取3副牌打筒子整副牌
	 * @param table 牌桌
	 * @param kouType 是否扣牌，0不扣、1扣牌
	 * @param out67Type 是否去掉67,0不去掉、1去掉
	 * @return
	 */
	public static List<Integer> getPokCardList(DtzTable table,List<Integer> copyCard,int kouType,int out67Type, int playtype){
		//判断去掉67情况
		if(out67Type == 1){
			for(int i=0;i<copyCard.size();i++){
				if((copyCard.get(i).intValue() % 100) == 6 || (copyCard.get(i).intValue() % 100) == 7){
					copyCard.remove(i);
					i--;
				}
			}
		}
        //判断扣牌情况
        if(kouType == 1 || DtzzConstants.isKlSiXi(playtype)){
            Collections.shuffle(copyCard);
            //清除原有的8张
            table.getKou8CardList().clear();
            int kouPai = 0;
            switch(playtype){
                case DtzzConstants.play_type_3POK:
                case DtzzConstants.play_type_4POK:
                    kouPai = 8;
                    break;
                case DtzzConstants.play_type_3PERSON_3POK:
                    if(table.getWangTongZi() == 1){
                        kouPai = 15;
                    }else{
                        kouPai = 9;
                    }
                    break;
                case DtzzConstants.play_type_3PERSON_4POK:
                    kouPai = 52;
                    break;
                case DtzzConstants.play_type_2PERSON_3POK:
                    if(table.getWangTongZi() == 1){
                        kouPai = 72;
                    }else{
                        kouPai = 66;
                    }
                    break;
                case DtzzConstants.play_type_2PERSON_4POK:
                    kouPai = 96;
                    break;
                case DtzzConstants.play_type_2PERSON_4Xi:
                    kouPai = 88;
                    break;
                case DtzzConstants.play_type_3PERSON_4Xi:
                    kouPai = 44;
                    break;
                case DtzzConstants.play_type_4PERSON_4Xi:
                    kouPai = 0;
                    break;
            }
            for(int i=0;i<kouPai;i++){
                table.getKou8CardList().add(copyCard.get(0));
                copyCard.remove(0);
            }
        }else{
            //清除原有的8张
            table.getKou8CardList().clear();
        }
		return copyCard;
	}

	public static void main(String[] args) {
		System.out.println(cardList_Dtz.size());
		Collections.sort(cardList_Dtz);
		System.out.println(JacksonUtil.writeValueAsString(cardList_Dtz));
		System.out.println(cardList_Dtz_4.size());
		Collections.sort(cardList_Dtz_4);
		System.out.println(JacksonUtil.writeValueAsString(cardList_Dtz_4));
	}

	public static boolean isKlSiXi(int playType){
	    return playType == play_type_2PERSON_4Xi || playType == play_type_3PERSON_4Xi || playType == play_type_4PERSON_4Xi;
    }
}
