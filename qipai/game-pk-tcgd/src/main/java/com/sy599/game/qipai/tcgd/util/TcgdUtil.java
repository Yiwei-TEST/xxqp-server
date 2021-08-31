package com.sy599.game.qipai.tcgd.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TcgdUtil {
	/**
	 * 获取下一个说话玩家的位置
	 * @param
	 * @return
	 */
//	public static String getNextUps(String ups) {
//		if(ups.equals("4")) {
//			return "1";
//		} else {
//			return String.valueOf(Integer.parseInt(ups)+1);
//		}
//	}
	public static List<Integer> getScoreCardsList(List<Integer> cardIds) {
		List<Integer> scoreCards = new ArrayList<Integer>();

		for (Integer id : cardIds) {
			int val = loadCardValue(id);
			if (val == 5 || val == 10) {
				scoreCards.add(id);
			} else if (val == 13) {
				scoreCards.add(id);
			}
		}
		return scoreCards;
	}
	/**
	 * 计算所有牌的得分值
	 *
	 * @param cards
	 * @return
	 */
	public static int loadCardScore(List<Integer> cards) {
		int total = 0;
		for (int card : cards) {
			int val = loadCardValue(card);
			if (val == 5 || val == 10) {
				total += val;
			} else if (val == 13) {
				total += 10;
			}
		}
		return total;
	}
	public static String getDuiJiaUps(String ups) {
		String djups = "";
		if(ups.equals("1")) {
			djups = "3";
		} else if(ups.equals("2")) {
			djups = "4";
		} else if(ups.equals("3")) {
			djups = "1";
		} else if(ups.equals("4")) {
			djups = "2";
		}
		return djups;
	}
	
	/**
	 * 获取一组牌里的分
	 * @param pai
	 * @return
	 */
	public static int getCpfen(List<String> pai) {
		if(null==pai || pai.size()==0){
			return 0;
		}
		int fen = 0;
		int[] ary = TcgdSf.paiToShortAry(pai);
		fen = ary[2]*5 + ary[7]*10 + ary[10]*10;
		return fen;
	}
	/**
	 * 获取一组牌里的分
	 * @param pai2
	 * @return
	 */
	public static int getScoreCards(List<Integer> pai2) {
		List<String> pai = TcgdSfNew.intCardToStringCard(pai2);
		if(null==pai || pai.size()==0){
			return 0;
		}
		int fen = 0;
		int[] ary = TcgdSf.paiToShortAry(pai);
		fen = ary[2]*5 + ary[7]*10 + ary[10]*10;
		return fen;
	}
	/**
	 * 计算牌值(A_14,2_15,3_3...,K_13)
	 *
	 * @param card
	 * @return
	 */
	public static int loadCardValue(int card) {
		int value = card % 100;
		return value;
	}
	public static int loadCardColor(int card) {
		int value = card / 100;
		return value;
	}
	public static List<Integer> getScoreCards2(List<Integer> cardIds) {
		List<Integer> scoreCards = new ArrayList<Integer>();

		for (Integer id : cardIds) {
			int val = loadCardValue(id);
			if (val == 5 || val == 10) {
				scoreCards.add(id);
			} else if (val == 13) {
				scoreCards.add(id);
			}
		}
		return scoreCards;
	}
	/**
	 * 获取喜钱分数
	 * @param pai
	 * @return
	 */
	public static int getXiqian(List<String> pai) {
		if(TcgdSf.is8zha(pai)) {
			return 4*4;
		} else if(TcgdSf.is7zha(pai)) {
			return 2*4;
		} else if(TcgdSf.is6zha(pai)) {
			return 1*4;
		} else if(TcgdSf.is4wang(pai)) {
			return 1*4;
		} else {
			return 0;
		}
	}

	public static List<String> getInitList() {
		if(initList==null || initList.size()==0){
			initList.clear();
			for (int i = 3; i <15; i++) {
				initList.add("R" + i);
				initList.add("B" + i);
				initList.add("M" + i);
				initList.add("F" + i);
			}
		}
		return initList;
	}

	public static void setInitList(List<String> initList) {
		TcgdUtil.initList = initList;
	}

	private static  List<String> initList= new ArrayList<>();


	public static List<String> CheckR3OutCard (String handpai ,List<String> result){
		String r =	TcgdSfNew.getCpType2(TcgdSfNew.paiToList(handpai.split(",")), 1, 1);
		if(!"".equals(r )){
			result.add(r);
		}
		String copypai = handpai;
		initList = getInitList();
		if(copypai.contains("R15")){
			for(int k=0;k<initList.size();k++){
				String copy2 = copypai;
				copy2 = copy2.replaceFirst("R15",initList.get(k));
				CheckR3OutCard(copy2,result);
			}
		}else{
			 r =	TcgdSfNew.getCpType2(TcgdSfNew.paiToList(copypai.split(",")), 1, 1);
			if(!"".equals(r )){
				result.add(r);
			}
		}
		return result;
	}

	public static List<List<String>>  getAllTs (String handpai ,List<List<String>>  result,String cptype,List<String> sjp){
//		System.out.println("handpai:="+handpai);
		if(!result.isEmpty()){
			return result;//有提示了 能打得起就直接返回 不需要所有提示
		}
		List<List<String>>  rel2=TcgdSfNew.getAllTsNew1(sjp,TcgdSfNew.paiToList(handpai.split(",")), cptype);
		if(!rel2.isEmpty()){
			result.addAll(rel2);
			return result;
		}
		String copypai = handpai;
		initList = getInitList();
		if(copypai.contains("R15")){
			for(int k=0;k<initList.size();k++){
				String copy2 = copypai;
				copy2 = copy2.replaceFirst("R15",initList.get(k));
				getAllTs(copy2,result,cptype,sjp);
			}
		}else{
			List<List<String>>  rel =	TcgdSfNew.getAllTsNew1(sjp,TcgdSfNew.paiToList(copypai.split(",")), cptype);
			if(!rel.isEmpty()){
				result.addAll(rel);
				return result;//有提示了 能打得起就直接返回 不需要所有提示
			}
		}
		return result;
	}

	public static List<Integer> haveRed2CardsTurn(List<Integer> c,int px){
		System.out.println("=====================================================");
		System.out.println(c);
		System.out.println("=====================================================");

		//不含红桃2
		List<Integer> cards = new ArrayList<>(c);
		int card315num=0;
		if(!cards.contains(315)){
			return c;
		}else{
			card315num++;
			cards.remove(cards.lastIndexOf(315));
			if(cards.contains(315)){
				card315num++;
				cards.remove(cards.lastIndexOf(315));
			}
		}

//		11单 12=对子 13=顺 14=3连对 15=三张 16=三带对 17=飞机 20=炸 23同花顺 30=天炸
			if(px==11){
				 if(c.get(0)==315){
				 	List<Integer> return_card = new ArrayList<>();
				 	return_card.add(415);
				 	return return_card;
				 }
				 return c;
			}
			if(px==12){
				if(card315num==1){
					int card0 = cards.get(0);
					List<Integer> return_cards = new ArrayList<>(cards);
					return_cards.add(card0);
					return return_cards;
				}else if(card315num==2){
					return c;
				}
			}
			else if(13== px){
				//顺
				List<Integer>  cardval = new ArrayList<>();
				List<Integer>  cardcol = new ArrayList<>();
				for(int c0 :cards){
					cardval.add(loadCardValue(c0));
					cardcol.add(loadCardColor(c0));
				}
				Collections.sort(cardval);
				int card0 =  cardval.get(0);
				int col =  cardcol.get(0);
				List<Integer> return_cards = new ArrayList<>(cards);
				do {
					if(cardval.contains(card0)){
						card0++;
						continue;
					}else{
						return_cards.add(card0+100*4);
						card0++;
					}
			 	}while (return_cards.size()!=c.size());//为false则跳出do-
				return return_cards;
			}
			else if(14== px){
			//3连对
				List<Integer>  cardval = new ArrayList<>();
				List<Integer>  cardcol = new ArrayList<>();
				for(int c0 :cards){
					cardval.add(loadCardValue(c0));
					cardcol.add(loadCardColor(c0));
				}
				List<Integer> return_cards = new ArrayList<>(cards);
				// 34455 445522 334522
			 	int[] intAry = paiToIntAry(cardval);
			 	if(card315num==1){
			 		//3*4455 334*55 33445*
					for (int i:cards){
						int num =getNumByVal(cards,loadCardValue(i));
						if(num==1){
							return_cards.add(i);
						}
					}
					return return_cards;
				}else if(card315num==2){
			 		int card0 = 0;
					//3*4*55
					for (int i:cards){
						int num =getNumByVal(cards,loadCardValue(i));
						if(num==1){
							return_cards.add(i);
						}
					}
					if(return_cards.size()==4){
						//33**55
						Collections.sort(cardval);
						List<Integer> lsAA33= new ArrayList<>();
						lsAA33.add(14);	lsAA33.add(14);	lsAA33.add(3);	lsAA33.add(3);
						if(cardval.containsAll(lsAA33)){
							return_cards.add(415);return_cards.add(415);return return_cards;
						}
						List<Integer> lsAA22= new ArrayList<>();
						lsAA22.add(14);	lsAA22.add(14);	lsAA22.add(15);	lsAA22.add(15);
						if(cardval.containsAll(lsAA22)){
							return_cards.add(403);return_cards.add(403);return return_cards;
						}
						List<Integer> ls2233= new ArrayList<>();
						ls2233.add(3);	ls2233.add(3);	ls2233.add(15);	ls2233.add(15);
						if(cardval.containsAll(ls2233)){
							return_cards.add(414);return_cards.add(414);
							return return_cards;
						}
						int re =cardval.get(3)-cardval.get(0);
						if(re==1 && cardval.get(3)<=13){
							return_cards.add(400+cardval.get(3)+1);
							return_cards.add(400+cardval.get(3)+1);
							return return_cards;
						}
						if(re==2 && cardval.get(3)<=13){
							return_cards.add(400+cardval.get(0)+1);
							return_cards.add(400+cardval.get(0)+1);
							return return_cards;
						}
					}
//					for (int i=0;i<intAry.length;i++){
//						if(return_cards.size()==c.size()){
//							return return_cards;
//						}
//						if(intAry[i]==0){
//							if(card0==0){
//								continue;
//							}else{
//								//33**55  card0=3  /3344** card0=4
//								card0 = card0+1;
//								int color = 4*100;
//								int c3 = card0+color;
//								return_cards.add(c3);
//								return_cards.add(c3);
//							}
//						}else if(intAry[i]==1){//  3*4*55
//							if(card0==0){
//								//3*4*55 card=0
//								card0 = i+3;
//								int color =4*100;
//								int c3 = card0+color;
//								return_cards.add(c3);
//								return_cards.add(c3);
//							}else{
//								//3*4*55 card=3
//								card0=i+3;
//								int card3 =i+3+1;
//								int color = 4*100;
//								int c3 = card3+color;
//								return_cards.add(c3);
//								return_cards.add(c3);
//							}
//						}else  if(intAry[i]==2){
//								card0 = i+3;
//								int color = 4*100;
//								int c3 = card0+color;
//								return_cards.add(c3);
//								return_cards.add(c3);
//						}
//						if(return_cards.size()==c.size()){
//							return return_cards;
//						}
//					}
				}
				return return_cards;
			}
			else if(15== px){
				List<Integer> return_cards = new ArrayList<>();
				int card0 = cards.get(0);
				return_cards.add(card0);
				return_cards.add(card0);
				return_cards.add(card0);
				return return_cards;
			}
			else if(16== px){
				//三带对
				List<Integer>  cardval = new ArrayList<>();
				List<Integer>  cardcol = new ArrayList<>();
				for(int c0 :cards){
					cardval.add(loadCardValue(c0));
					cardcol.add(loadCardColor(c0));
				}
				Collections.sort(cardval);
				if(card315num==1){
					int card0=  cardval.get(0);
					int card0um =0;
					for (int c0:cardval) {
						if(c0==card0){
							card0um++;
						}
					}
					List<Integer> return_cards = new ArrayList<>(cards);
					if(card0um==1){
						//3444*  card0=3
						int c3 = cardval.get(0)+100*cardcol.get(0);
						return_cards.add(c3);
						if(return_cards.size()==5){
							return return_cards;
						}
					}else if(card0um==2){
						// 3344* card0=3
						int c3 = cardval.get(3)+100*cardcol.get(3);
						return_cards.add(c3);
						if(return_cards.size()==5){
							return return_cards;
						}
					}else if(card0um==3){
						// 3334* card0=3
						int c3 = cardval.get(3)+100*cardcol.get(3);
						return_cards.add(c3);
						if(return_cards.size()==5){
							return return_cards;
						}
					}
				}else if(card315num==2){
					//344**  333**  cardval=333  // 334**
					if(cardval.get(0)==cardval.get(1) && cardval.get(1) ==cardval.get(2)){
						return c;
					}else{
						//344**  cardval=344
						if(cardval.get(1) ==cardval.get(2)&& cardval.get(0)<cardval.get(1)){
							List<Integer> return_cards = new ArrayList<>(cards);
							int card3 = cardval.get(2);
							int card0 = cardval.get(0);
							int color = 400;
							return_cards.add(card3+color);
							return_cards.add(card0+color);
							if(return_cards.size()==5){
								return return_cards;
							}
						}
						//334**
						if(cardval.get(0) ==cardval.get(1) && cardval.get(2)>cardval.get(1)){
							List<Integer> return_cards = new ArrayList<>(cards);
							return_cards.add(400+cardval.get(2));
							return_cards.add(400+cardval.get(2));
							if(return_cards.size()==5){
								return return_cards;
							}
						}

					}
				}
			}
			else if(17== px){
				//飞机二连三同张  一个癞子 哪个不够3就补  2个癞子 哪个不够3就补
				List<Integer>  cardval = new ArrayList<>();
				List<Integer>  cardcol = new ArrayList<>();
				for(int c0 :cards){
					cardval.add(loadCardValue(c0));
					cardcol.add(loadCardColor(c0));
				}
				Collections.sort(cardval);
				List<Integer> return_cards = new ArrayList<>(cards);
				if(card315num==1){
					//33444* card0=3
					int card0=  cardval.get(0);
					int color = cardcol.get(0);
					int card0um =0;
					for (int c0:cardval) {
						if(c0==card0){
							card0um++;
						}
					}
					if(card0um!=3){
						int c3= card0+color*100;
						return_cards.add(c3);
					}else{
						card0 =cardval.get(4);
						color = cardcol.get(4);
						int c3= card0+color*100;
						return_cards.add(c3);
					}
					if(return_cards.size()==6){
						return return_cards;
					}
				}else if(card315num==2){
					//3344** 3444**
					int card0=  cardval.get(0);
					int card2=  cardval.get(3);
					int color = cardcol.get(0);
					int color2 = cardcol.get(3);
					int card0um =0;
					int card2um =0;
					for (int c0:cardval) {
						if(c0==card0){
							card0um++;
						}
						if(c0==card2){
							card2um++;
						}
					}
					if(card0um==1){
						// 3444**  card0 =3;
						int c3 =card0+color*100;
						return_cards.add(c3);
						return_cards.add(c3);
					}else if(card0um==2){
						//3344**
						int c3 =card0+color*100;
						return_cards.add(c3);
					}else if(card0um ==3){
						//3334**
						int c3 =card2+color2*100;
						return_cards.add(c3);
						return_cards.add(c3);
					}
				}
				if(return_cards.size()==6){
					return return_cards;
				}
			}
			else if(20== px){
				//炸弹
				List<Integer> return_cards = new ArrayList<>(cards);
				int card0 = cards.get(0);
				if(card315num==1){
					return_cards.add(card0);
				}else if(card315num==2){
					return_cards.add(card0);
					return_cards.add(card0);
				}
				return return_cards;
			}
			else if(23== px){
				//顺
				List<Integer>  cardval = new ArrayList<>();
				List<Integer>  cardcol = new ArrayList<>();
				for(int c0 :cards){
					cardval.add(loadCardValue(c0));
					cardcol.add(loadCardColor(c0));
				}
				Collections.sort(cardval);
				int card0 =  cardval.get(0);
				int col =  cardcol.get(0);
				List<Integer> return_cards = new ArrayList<>(cards);
				do {
					if(cardval.contains(card0)){
						card0++;
						continue;
					}else{
						return_cards.add(card0+col*100);
						card0++;
					}
				}while (return_cards.size()!=c.size());//为false则跳出do-
				return return_cards;
			}
		return null;
	}
	public static int[] paiToIntAry(List<Integer> pai) {
		int[] iary = {0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};//  "M3","M4","M5","M6","M7","M8","M9","M10","M11","M12","M13","M14","M15"33-43
		// 大王W(6)小王w(5)黑B(4)红R(3)梅M(2)方F(1)
		for (int i = 0; i < pai.size(); i++) {
			iary[pai.get(i) - 3] += 1;
		}
		return iary;
	}

	/**
	 *  比较值，获取某一张牌的个数
	 * @param handPais
	 * @param val
	 * @return
	 */
	public static int getNumByVal(List<Integer> handPais, int val){
		int c2num =0;
		for (int c2:handPais) {
			if(val==loadCardValue(c2)){
				c2num++;
			}
		}
		return c2num;
	}
	/**
	 *  比较值和花色，获取某一张牌的个数
	 * @param handPais
	 * @param card
	 * @return
	 */
	public static int getNumByColVal(List<Integer> handPais, int card){
		int c2num =0;
		for (int c2:handPais) {
			if(card==c2){
				c2num++;
			}
		}
		return c2num;
	}
	public static void main(String[] args) {
		List<Integer> sjp = new ArrayList<Integer>();
		//1133 ok  1122 2233
		sjp.add(309);
		sjp.add(409);
		sjp.add(315);
		Collections.sort(sjp);
		System.out.println(sjp);
//		11单 12=对子 13=顺 14=3连对 15=三张 16=三带对 17=飞机二连三同张 20=炸 23同花顺 30=天炸
		System.out.println(	haveRed2CardsTurn(sjp,15));

	}
	public static void test14(){
		List<Integer> sjp = new ArrayList<Integer>();
		sjp.add(315);
		sjp.add(315);
		sjp.add(304);
		sjp.add(304);
		sjp.add(303);
		sjp.add(303);
		//3344**
		System.out.println("连对 2个癞子 3344** "+sjp);
		System.out.println(haveRed2CardsTurn(sjp,14));
		System.out.println("");

		List<Integer> sjp2 = new ArrayList<Integer>();
		sjp2.add(315);
		sjp2.add(315);
		sjp2.add(305);
		sjp2.add(305);
		sjp2.add(303);
		sjp2.add(303);
		//3456**
		System.out.println("连对 2个癞子 33**55"+sjp2);
		System.out.println(haveRed2CardsTurn(sjp2,14));
		System.out.println("");

		List<Integer> sjp3 = new ArrayList<Integer>();
		sjp3.add(315);
		sjp3.add(305);
		sjp3.add(305);
		sjp3.add(304);
		sjp3.add(303);
		sjp3.add(303);
		//3*5*78
		System.out.println("连对 1个癞子 334*55"+sjp3);
		System.out.println(haveRed2CardsTurn(sjp3,14));
		System.out.println("");

		List<Integer> sjp4 = new ArrayList<Integer>();
		sjp4.add(315);
		sjp4.add(305);
		sjp4.add(305);
		sjp4.add(304);
		sjp4.add(304);
		sjp4.add(303);
		//3*5*78
		System.out.println("连对 1个癞子 3*4455"+sjp4);
		System.out.println(haveRed2CardsTurn(sjp3,14));
		System.out.println("");
	}
	public static void test23(){
		List<Integer> sjp = new ArrayList<Integer>();
		sjp.add(308);
		sjp.add(315);
		sjp.add(306);
		sjp.add(305);
		sjp.add(304);
		sjp.add(303);
		//3456*8
		System.out.println("同花顺子 1个癞子 "+sjp);
		System.out.println(haveRed2CardsTurn(sjp,23));
		System.out.println("");

		List<Integer> sjp2 = new ArrayList<Integer>();
		sjp2.add(315);
		sjp2.add(315);
		sjp2.add(306);
		sjp2.add(305);
		sjp2.add(304);
		sjp2.add(303);
		//3456**
		System.out.println("同花顺子 2个癞子尾巴"+sjp2);
		System.out.println(haveRed2CardsTurn(sjp2,23));
		System.out.println("");

		List<Integer> sjp3 = new ArrayList<Integer>();
		sjp3.add(315);
		sjp3.add(315);
		sjp3.add(308);
		sjp3.add(307);
		sjp3.add(305);
		sjp3.add(303);
		//3*5*78
		System.out.println("同花顺子 2个癞子补中间"+sjp3);
		System.out.println(haveRed2CardsTurn(sjp3,23));
		System.out.println("");

	}
	public static void test13(){
		List<Integer> sjp = new ArrayList<Integer>();
		sjp.add(108);
		sjp.add(315);
		sjp.add(106);
		sjp.add(105);
		sjp.add(304);
		sjp.add(303);
		//
		System.out.println("顺子 1个癞子 "+sjp);
		System.out.println(haveRed2CardsTurn(sjp,13));
		System.out.println("");

		List<Integer> sjp2 = new ArrayList<Integer>();
		sjp2.add(315);
		sjp2.add(315);
		sjp2.add(106);
		sjp2.add(105);
		sjp2.add(304);
		sjp2.add(303);
		//16=三带对 3334* 3344*
		System.out.println("顺子 2个癞子尾巴"+sjp2);
		System.out.println(haveRed2CardsTurn(sjp2,13));
		System.out.println("");

		List<Integer> sjp3 = new ArrayList<Integer>();
		sjp3.add(315);
		sjp3.add(315);
		sjp3.add(108);
		sjp3.add(107);
		sjp3.add(305);
		sjp3.add(303);
		//16=三带对 3334* 3344*
		System.out.println("顺子 2个癞子补中间"+sjp3);
		System.out.println(haveRed2CardsTurn(sjp3,13));
		System.out.println("");

	}
	public static void test17(){
		List<Integer> sjp = new ArrayList<Integer>();
		sjp.add(315);
		sjp.add(104);
		sjp.add(104);
		sjp.add(103);
		sjp.add(303);
		sjp.add(303);
		//
		System.out.println("2连三同张 1个癞子 "+sjp);
		System.out.println(haveRed2CardsTurn(sjp,17));
		System.out.println("");

		List<Integer> sjp2 = new ArrayList<Integer>();
		sjp2.add(315);
		sjp2.add(315);
		sjp2.add(104);
		sjp2.add(103);
		sjp2.add(303);
		sjp2.add(303);
		//16=三带对 3334* 3344*
		System.out.println("2连三同张 2个癞子"+sjp2);
		System.out.println(haveRed2CardsTurn(sjp2,17));
		System.out.println("");

		List<Integer> sjp3 = new ArrayList<Integer>();
		sjp3.add(315);
		sjp3.add(315);
		sjp3.add(104);
		sjp3.add(103);
		sjp3.add(104);
		sjp3.add(104);
		//16=三带对 3334* 3344*
		System.out.println("2连三同张 2个癞子"+sjp3);
		System.out.println(haveRed2CardsTurn(sjp3,17));
		System.out.println("");

	}
	public static void test16(){
		List<Integer> sjp = new ArrayList<Integer>();
		sjp.add(111);
		sjp.add(111);
		sjp.add(307);
		sjp.add(307);
		sjp.add(315);
		//16=三带对 3334* 3344*
		System.out.println("三带一对 1个癞子");
		System.out.println(haveRed2CardsTurn(sjp,16));
		System.out.println("");

//		List<Integer> sjp2 = new ArrayList<Integer>();
//		sjp2.add(315);
//		sjp2.add(315);
//		sjp2.add(105);
//		sjp2.add(305);
//		sjp2.add(305);
//		//16=三带对 3334* 3344*
//		System.out.println("三带一对 2个癞子");
//		System.out.println(haveRed2CardsTurn(sjp2,16));
//		System.out.println("");
	};
}
