package com.sy599.game.qipai.dtz.tool;

import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author lc
 * 
 */
public class CardTool {

	/**
	 * 打筒子算分取整百
	 * @param score
	 * @return
	 */
    public static final int calcScore(int score , boolean isGoldRoom) {
        if(isGoldRoom){
            return score;
        }
        if (score > 0) {
            return ((score / 100) + ((score % 100) >= 50 ? 1 : 0)) * 100;
        } else if (score < 0) {
            return ((score / 100) + ((score % 100) <= -50 ? -1 : 0)) * 100;
        } else {
            return 0;
        }
    }
	
	/**
	 * 
	 * 一个小王，/大王  单出，  对出必须相同， 三个 大王/小王 是筒子， 
	 * 测试代码  打筒子
	 * @param playerCount
	 * @return
	 */
	public static synchronized List<List<Integer>> fapaiDtz(DtzTable table,int playerCount, int playtype, int haveJacker) {
		List<List<Integer>> list = new ArrayList<>();
		
		List<Integer> copyCard = null;
		switch(playtype){
	    	case DtzzConstants.play_type_3POK:
	    	case DtzzConstants.play_type_3PERSON_3POK:	
	    	case DtzzConstants.play_type_2PERSON_3POK:	
	    		if(table.getWangTongZi() == 1){
	    			copyCard = new ArrayList<Integer>(DtzzConstants.cardList_wang_Dtz);
	    		}else{
	    			copyCard = new ArrayList<Integer>(DtzzConstants.cardList_Dtz);
	    		}
	    		break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                copyCard = new ArrayList<>(DtzzConstants.cardList_Dtz_4_buDaiWang);
                break;
            default:
	    		copyCard = new ArrayList<Integer>(DtzzConstants.cardList_Dtz_4);
	    		break;
		}
		List<Integer> copy = DtzzConstants.getPokCardList(table, copyCard, table.getKouType() , table.getOut67Type(), playtype);
		Collections.shuffle(copy);  //随机一下
		ArrayList<Integer> pai = new ArrayList<>();
		int maxCount = copy.size() / playerCount;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
			} else {
				if (haveJacker == 1) {
					pai.add(16); //jacker_samll
					pai.add(17); //jacker_red
				}
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
			}
		}
		//默认是没有加大小王的后面需要的话可以加上
		list.add(pai);
		return list;
	}
	
	/**
	 * 打筒子测试代码
	 * @param playerCount
	 * @param playType
	 * @param zps
	 * @return
	 */
	public static synchronized List<List<Integer>> fapaiDtz(DtzTable table,int playerCount, int playType, int  haveJacker, List<List<Integer>> zps) {
		return fapaiDtz(table,playerCount, playType, haveJacker);
	}

	public static List<Integer> findCardIIds(List<Integer> copy, List<Integer> vals, int cardNum) {
		List<Integer> pai = new ArrayList<>();
		if (!vals.isEmpty()) {
			int i = 1;
			for (int zpId : vals) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					int paiVal=card % 100;
					if (paiVal == zpId) {
						pai.add(card);
						iterator.remove();
						break;
					}
				}
				if (cardNum != 0) {
					if (i >= cardNum) {
						break;
					}
					i++;
				}
			}
		}
		return pai;
	}

	public static void main(String[] args) {
	}

	public static List<Integer> getValue(List<Integer> copy) {
		if(copy == null || copy.isEmpty()) {
			return new ArrayList<Integer>();
		}
		List<Integer> list = new ArrayList<>();
		for(int card : copy) {
			list.add(card % 100);
		}
		return list;
	}
}
