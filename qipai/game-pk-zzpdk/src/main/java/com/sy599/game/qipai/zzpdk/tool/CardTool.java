package com.sy599.game.qipai.zzpdk.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang.math.RandomUtils;

import com.sy599.game.GameServerConfig;
import com.sy599.game.db.bean.PdkRateConfig;
import com.sy599.game.qipai.zzpdk.constant.PdkConstants;
import com.sy599.game.qipai.zzpdk.util.CardUtils;
import com.sy599.game.util.GameUtil;

/**
 * @author lc
 * 
 */
public final class CardTool {
	public static List<List<Integer>> zuopai(int playerCount, int cardNum,List<List<Integer>> zps){
		List<List<Integer>> list = new ArrayList<>();
		List<Integer> copy;
		if((cardNum == GameUtil.play_type_16)){
			copy = new ArrayList<>(PdkConstants.cardList_16);
		}else {
			copy = new ArrayList<>(PdkConstants.cardList_15);
		}
		int maxCount = copy.size() / 3;
		if (zps != null && !zps.isEmpty()) {
			for (List<Integer> zp : zps) {
				list.add(findCardIIds(copy, zp, 0));
			}
		}
		Collections.shuffle(copy);
		int size = zps.size();
		for (int i=size;i<playerCount;i++){
			List<Integer> pai = new ArrayList<>();
			for (int j = 0; j < copy.size(); j++) {
				int card = copy.get(j);
				if (pai.size() < maxCount) {
					pai.add(card);
				}else {
					list.add(pai);
					break;
				}
			}
		}
		return list;
	}

	/**
	 * @param playerCount
	 *            人数
	 *            玩法 15和16张
	 * @param zps
	 * @param checkBoom
	 * @return
	 */
	public static List<List<Integer>> fapai(int playerCount, int cardNum, List<List<Integer>> zps, boolean checkBoom) {
		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy;
		if((cardNum == 16)){
			copy = new ArrayList<>(PdkConstants.cardList_16);
		}else {
			copy = new ArrayList<>(PdkConstants.cardList_15);
		}

		Collections.shuffle(copy);

		int maxCount = copy.size() / 3;
		List<Integer> pai = new ArrayList<>();
		if (GameServerConfig.isDebug()) {
			if (zps != null && !zps.isEmpty()) {
				for (List<Integer> zp : zps) {
					list.add(findCardIIds(copy, zp, 0));
				}
			}

			if (list.size() == 3) {
				return list;
			}
		}

		int j = 0;
		int flag = 0;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}

			if (card == 403)
				flag = j;
		}
		list.add(pai);
		// 若黑桃三在抽牌中，则改变位置
		if (flag == 2 && playerCount == 2) {
			List<List<Integer>> list1 = new ArrayList<>();
			int seat = new Random().nextInt(2);
			if (seat == 0) {
				list1.add(list.get(2));
				list1.add(list.get(0));
				list1.add(list.get(1));
			} else {
				list1.add(list.get(0));
				list1.add(list.get(2));
				list1.add(list.get(1));
			}
			list = list1;
		}

		// 决定发牌里面的炸弹
		if (checkBoom && (zps == null || zps.size() == 0)) {
			int boomCount = checkBoom(list);
			int temp;
			if (boomCount == 0) {
				temp = -1;
			} else if (boomCount == 1) {
				temp = 53;
			} else if (boomCount == 2) {
				temp = 80;
			} else if (boomCount >= 3) {
				temp = 95;
			} else {
				temp = 0;
			}
			if (temp >= 0 && new SecureRandom().nextInt(100) <= temp) {
				return fapai(playerCount, cardNum, zps, true);
			}
		}
		return list;
	}
	
	


	public static List<List<Integer>> fapaiPingHeng(int playerCount, int cardNum, List<PdkRateConfig> configs,
			boolean checkBoom, List<List<Integer>> zps) {

		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy;
		if (cardNum == GameUtil.play_type_16){
			copy = new ArrayList<>(PdkConstants.cardList_16);
		}else{
			copy = new ArrayList<>(PdkConstants.cardList_15);
		}

		Collections.shuffle(copy);

		int maxCount = copy.size() / 3;
		List<Integer> pai = new ArrayList<>();

		// 配好牌 几个人需要？
		int type = getPdkPeiPaiScheme(configs, 4);
		if (type > 0) {
			List<Integer> phCards = loadPingHengCards(copy, cardNum, checkBoom, maxCount, type);
			list.add(phCards);
			
		}

		if (GameServerConfig.isDebug()) {
			// if(zps != null && !zps.isEmpty()) {
			// for (List<Integer> zp : zps) {
			// list.add(findCardIIds(copy, zp, 0));
			// break;
			// }
			// }

		}

		int j = 0;
		int flag = 0;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}
			if (card == 403)
				flag = j;
		}

		list.add(pai);

		int type2 = getPdkPeiPaiScheme(configs, 1);
		List<Integer> winPais = null;
		if (type2 > 0) {
			 winPais = list.get(1);
			if(type==0){
				winPais = list.get(0);
			}
//			System.out.println("拆好牌 类型--------------------"+type2 +"---------------winCards =-"+winPais);
			pingHengCards(winPais, list.get(2), type2);
			
		}
		// 决定发牌里面的炸弹
		if (checkBoom) {
			int boomCount = checkBoom(list);
			if (boomCount > 0) {
				return fapaiPingHeng(playerCount, cardNum, configs, checkBoom, zps);
			}
		}else if(winPais!=null){
			int boomCount = 0;
			Map<Integer, Integer> map = CardUtils.countValue(CardUtils.loadCards(winPais));
			for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
				if (kv.getValue().intValue() >= 4) {
					boomCount++;
				}
			}
			int temp;
			if (boomCount == 0) {
				temp = -1;
			} else if (boomCount == 1) {
				temp = 53;
			} else if (boomCount == 2) {
				temp = 80;
			} else if (boomCount >= 3) {
				temp = 95;
			} else {
				temp = 0;
			}
			if (temp >= 0 && new SecureRandom().nextInt(100) <= temp) {
				return fapaiPingHeng(playerCount, cardNum, configs, checkBoom, zps);
			}
		}
		
		return list;
	}
	
	
	
	private static  int getPdkPeiPaiScheme(List<PdkRateConfig> configs ,int type){
		if(configs==null){
			return 0;
		}
		PdkRateConfig config = getXuanZCongfig(configs, type);
		if(config==null){
			return 0;
		}
		if(config.getRate()==null ||config.getRate().length()==0){
			return 0;
		}
		List<Integer> rates = new ArrayList<>();
		String[] str =  config.getRate().split(";");
		for(String s : str){
			rates.add(Integer.valueOf(s));
		}
		
		int rate =  RandomUtils.nextInt(100);
		for(int i=0;i<rates.size();i++){
			int val =  rates.get(i);
			if(val>=rate){
				return i+1;
			}
			rate -=val;
		}
		
		return 0;
		
	}

	public static PdkRateConfig getXuanZCongfig(List<PdkRateConfig> configs, int type) {
		PdkRateConfig config = null;
		for(PdkRateConfig c : configs){
			if((type<3&&c.getType()<=3)||c.getType()>=type){
				config =c ;
				break;
			}
		}
		return config;
	}

	public static List<List<Integer>> fapaiNoBoom(int playerCount, int playType, List<List<Integer>> zps) {
		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy;
		if (playType == GameUtil.play_type_15) {
			copy = new ArrayList<>(PdkConstants.cardList_15);
		} else if(playType == GameUtil.play_type_16){
			copy = new ArrayList<>(PdkConstants.cardList_16);
		}else{
			copy = new ArrayList<>(PdkConstants.cardList_11);
		}

		Collections.shuffle(copy);

		int maxCount = copy.size() / 3;
		List<Integer> pai = new ArrayList<>();
		if (GameServerConfig.isDebug()) {
			if (zps != null && !zps.isEmpty()) {
				for (List<Integer> zp : zps) {
					list.add(findCardIIds(copy, zp, 0));
				}
			}

			if (list.size() == 3) {
				return list;
			}
		}

		int j = 0;
		int flag = 0;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}
			if (card == 403)
				flag = j;
		}

		list.add(pai);
		// 若黑桃三在抽牌中，则改变位置
		if (flag == 2 && playerCount == 2) {
			List<List<Integer>> list1 = new ArrayList<>();
			int seat = new Random().nextInt(2);
			if (seat == 0) {
				list1.add(list.get(2));
				list1.add(list.get(0));
				list1.add(list.get(1));
			} else {
				list1.add(list.get(0));
				list1.add(list.get(2));
				list1.add(list.get(1));
			}
			list = list1;
		}
		int boomCount = checkBoom(list);
		if (boomCount > 0) {
			return fapaiNoBoom(playerCount, playType, zps);
		}
		return list;
	}

	public static List<List<Integer>> fapai(int playerCount, List<List<Integer>> zps,int cardNum) {
		return fapai(playerCount, cardNum, zps, true);
	}

	private static int checkBoom(List<List<Integer>> lists) {
		int count = 0;
		for (List<Integer> list : lists) {
			Map<Integer, Integer> map = CardUtils.countValue(CardUtils.loadCards(list));
			for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
				if (kv.getValue().intValue() >= 4) {
					count++;
				}
			}
		}
		return count;

	}

	public static int loadCardValue(int id) {
		return id % 100;
	}

	public static List<Integer> loadCards(List<Integer> list, int val) {
		List<Integer> ret = new ArrayList<>(4);
		for (Integer integer : list) {
			if (val == loadCardValue(integer.intValue())) {
				ret.add(integer);
			}
		}
		return ret;
	}

	public static List<Integer> loadPingHengCards(List<Integer> copy, int playType,boolean boom,int maxCount,int type) {
		
		List<Integer> zp =getLosePHcards(type, playType,boom);
		//配的牌
		HashSet<Integer> cardVals = new HashSet<Integer>();
		if (type == 2 || type == 3) {
			for (Integer id : zp) {
				int val = id % 100;
				cardVals.add(val);
			}
		}
		
		List<Integer> pai = findCardIIds(copy, zp, 0);
		List<Integer> add = new ArrayList<>();
		add.addAll(copy.subList(0, maxCount-pai.size()));
		pai.addAll(add);
		
		copy.removeAll(add);
		
		
		if (!cardVals.isEmpty()) {
			HashSet<Integer> boomCardVals = new HashSet<Integer>();
			Map<Integer, Integer> map = CardUtils.countValue(CardUtils.loadCards(pai));
			for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
				if (kv.getValue().intValue() >= 4) {
					boomCardVals.add(kv.getKey());
				}
			}

			for (Integer boomVal : boomCardVals) {
				if (cardVals.contains(boomVal)) {// 配牌 配到炸弹需要删掉
					Integer cardId = 200 + boomVal;
					pai.remove(cardId);
					pai.add(copy.remove(0));
					copy.add(cardId);
				}

			}
		}
		return pai;
	}

	private static  List<Integer> getLosePHcards(int type, int playType,boolean boom) {
		 List<Integer> zp = new ArrayList<>();
		if (type == 1) {
			if (playType == GameUtil.play_type_15) {
				zp.add(15);
			} else {
				zp.add(15);
				zp.add(14);
			}

		} else if (type == 2) {
			zp.addAll(loadJQKCards());

		} else if (type == 3) {
			zp.addAll(loadFeijiCards());
		} else if (type == 4&&!boom) {
			zp.addAll(loadBoomCards());
		}
		
		return zp;
	}

	/***
	 * 拆好牌
	 * @param winCards
	 * @param loseCards
	 * @param type
	 */
	public static void pingHengCards(List<Integer> winCards, List<Integer> loseCards,int type) {
		
		List<Integer> boomCardVals = new ArrayList<Integer>();
		Map<Integer, Integer> map = CardUtils.countValue(CardUtils.loadCards(winCards));
		int feij = CardUtils.isFeiJi(winCards);
		int shun = CardUtils.isShunzi(winCards);
		
		if(feij>0&&type==3){
			boomCardVals.add(getWinCard(winCards, feij));
		}else if(shun>0&&type==3){
			boomCardVals.addAll(getWinCard2(winCards, shun));
		}
		
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			int value = getWinCard(winCards, kv.getKey());
			if((kv.getKey()==14&&type==2)||(kv.getKey()==15&&type==1)){
				boomCardVals.add(value);
				break;
			}else if(kv.getValue().intValue() ==4&&type==3){
				if(!boomCardVals.contains(value))
				boomCardVals.add(getWinCard(winCards, kv.getKey()));
				break;
			}
		}
		if(boomCardVals.isEmpty()){
			return;
		}
		
		winCards.removeAll(boomCardVals);
		List<Integer> removeKeys = new ArrayList<>();
		CardUtils.sortCards(loseCards);
		
//		Integer removeKey = 0;
		for(Integer id: loseCards){
			int val = id%100;
			if(val >10){
				continue;
			}
			
			if(boomCardVals.size()==removeKeys.size()){
				break;
			}
			int chaiPai =  boomCardVals.get(0);
			int val2=chaiPai%100;
			if(val ==val2){
				continue;
			}
			Integer num = map.get(val);
			if(num !=null&& num==3){
				continue;
			}
			removeKeys.add(id);
		}
		//zp.addAll(loseCards.subList(0, boomCardVals.size()));
		loseCards.removeAll(removeKeys);
		loseCards.addAll(boomCardVals);
		winCards.addAll(removeKeys);
		
	}
	
	
	private static  int getWinCard(List<Integer> winCards,int val){
		for(Integer id:winCards){
			int val2 = id%100;
			if(val==val2){
				return id;
			}
		}
		return 0;
	}
	
	private static  List<Integer> getWinCard2(List<Integer> winCards,int val){
		List<Integer> zp = new ArrayList<>(3);
		for(Integer id:winCards){
			int val2 = id%100;
			if(val==val2){
				zp.add(id);
			}
		}
		return zp;
	}
	

	public static List<Integer> loadJQKCards() {
		List<Integer> zp = new ArrayList<>(3);
		int val = RandomUtils.nextInt(3) + 11;
		zp.add(val);
		zp.add(val);
		zp.add(val);
		return zp;

	}

	public static List<Integer> loadFeijiCards() {
		List<Integer> zp = new ArrayList<>();
		int val = RandomUtils.nextInt(7) + 7;
		// if()
		int val2 = 0;

		if (val == 13) {
			val2 = 12;
		} else {
			val2 = val + 1;
		}

		zp.add(val);
		zp.add(val);
		zp.add(val);

		zp.add(val2);
		zp.add(val2);
		zp.add(val2);
		return zp;

	}

	public static List<Integer> loadBoomCards() {
		List<Integer> zp = new ArrayList<>(3);
		int val = RandomUtils.nextInt(8) + 3;

		zp.add(val);
		zp.add(val);
		zp.add(val);
		zp.add(val);
		return zp;

	}

	public static Map<Integer, Integer> loadCards(List<Integer> list) {
		Map<Integer, Integer> map = new TreeMap<>();
		for (Integer integer : list) {
			int val = loadCardValue(integer.intValue());
			int count = map.getOrDefault(val, 0);
			count++;
			map.put(val, count);
		}
		return map;
	}

	public static List<Integer> findCardIIds(List<Integer> copy, List<Integer> vals, int cardNum) {
		List<Integer> pai = new ArrayList<>();
		if (!vals.isEmpty()) {
			int i = 1;
			for (int zpId : vals) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					int paiVal = card % 100;
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

	public static void main(String args[]) {
		
		String st = "10;3;15;5";
		String[] strA = st.split(";");
		for(String s: strA){
			System.out.println(s);
		}
//		for (int i = 0; i < 50; i++) {
//			// List<List<Integer>> list = fapai(2, GameUtil.play_type_15, null);
//			// if(check3(list)){
//			// System.out.println("没得黑桃三");
//			// break;
//			// }
//			System.out.println("运行了" + 412 % 100);
//		}

	}

	public static boolean check3(List<List<Integer>> list) {

		List<Integer> l = list.get(2);
		for (int a : l) {
			if (a == 403)
				return true;
		}
		return false;
	}

	public static void calcBookRate(int count, int playerCount, boolean checkBoom) {
		Map<Integer, Integer> map = new HashMap<>();
		List<List<Integer>> list;
		for (int i = 0; i < count; i++) {
			list = fapai(playerCount, GameUtil.play_type_15, null, checkBoom);
			int boomCount = checkBoom(list);
			if (map.containsKey(boomCount)) {
				map.put(boomCount, map.get(boomCount) + 1);
			} else {
				map.put(boomCount, 1);
			}
		}
		System.out.println("统计总次数：" + count + ", " + playerCount + " 人玩法," + (checkBoom ? "炸弹限制开关：开启" : "炸弹限制：关闭"));
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if (entry.getKey() > 0) {
				System.out.println("炸蛋数："
						+ entry.getKey() + ",概率：" + new BigDecimal(entry.getValue() * 1d / count)
								.multiply(new BigDecimal(100)).setScale(4, RoundingMode.FLOOR)
						+ " ,次数：" + entry.getValue());
			}
		}
	}

}
