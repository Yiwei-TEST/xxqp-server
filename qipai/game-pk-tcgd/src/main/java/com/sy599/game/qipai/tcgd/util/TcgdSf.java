package com.sy599.game.qipai.tcgd.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcgdSf {
	/**
	 * 获取初始化牌局的所有牌
	 * 
	 * @param num
	 * @return
	 */
	public static List<String> getInitArr() {
		List<String> initList = new ArrayList<String>();
		// 14表示A 15表示2 16表示王
		for(int j=0; j<1; j++) {
			for (int i = 3; i <= 15; i++) {
				initList.add("B" + i);
				initList.add("R" + i);
				initList.add("M" + i);
				initList.add("F" + i);
			}
			initList.add("W16");
			initList.add("w16");
		}
		List<String> ranList = new ArrayList<String>();
		while (initList.size() > 0) {
			Random random = new Random();
			int ranNum = random.nextInt(initList.size());
			ranList.add(initList.get(ranNum));
			initList.remove(ranNum);
		}
		return ranList;
	}
	
	public static List<String> getInitFen() {
		List<String> initList = new ArrayList<String>();
		//B R M F
		initList.add("B13");initList.add("B13");
		initList.add("B10");initList.add("B10");
		initList.add("B5");initList.add("B5");
		
		initList.add("R13");initList.add("R13");
		initList.add("R10");initList.add("R10");
		initList.add("R5");initList.add("R5");
		
		initList.add("M13");initList.add("M13");
		initList.add("M10");initList.add("M10");
		initList.add("M5");initList.add("M5");
		
		initList.add("F13");initList.add("F13");
		initList.add("F10");initList.add("F10");
		initList.add("F5");initList.add("F5");
		
		return initList;
	}

	/**
	 * 初始化手牌
	 * 
	 * @param list
	 * @param num
	 *            起手发num张牌
	 * @return
	 */
	public static List<String> initShouPai(List<String> list, int num) {
		List<String> ranList = new ArrayList<String>();
		while (list.size() > 0 && ranList.size() < num) {
			Random random = new Random();
			int ranNum = random.nextInt(list.size());
			ranList.add(list.get(ranNum));
			list.remove(ranNum);
		}
		return ranList;
	}
	
	/**
	 * 牌转短int数组
	 * @param pai
	 * @return
	 */
	public static int[] paiToShortAry(List<String> pai){
		int[] ary = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		for (int i = 0; i < pai.size(); i++) {
			int p = getNumbers(pai.get(i));
			ary[p-3]++;
		}
		return ary;
	}

	/**
	 * 截取数值
	 * 
	 * @param content
	 * @return
	 */
	public static Integer getNumbers(String content) {
		Integer i = 0;
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			i = Integer.parseInt(matcher.group(0));
		}
		return i;
	}

	/**
	 * 截取花色 返回对应花色的值，方便排序
	 * 
	 * @param args
	 *            返回大王W(6)小王w(5)黑B(4)红R(3)梅M(2)方F(1)
	 */
	public static Integer getColor(String content) {
		String color = String.valueOf(content.charAt(0));
		if (color.equals("W")) {
			return 6;
		} else if (color.equals("w")) {
			return 5;
		} else if (color.equals("B")) {
			return 4;
		} else if (color.equals("R")) {
			return 3;
		} else if (color.equals("M")) {
			return 2;
		} else {
			return 1;
		}
	}

	public static List<String> sortList(List<String> pai, String type) {
		for (int i = 0; i < pai.size() - 1; i++) {
			for (int j = 0; j < pai.size() - i - 1; j++) {
				if ("asc".equals(type)) {
					if (comparePai(pai.get(j), pai.get(j + 1)) > 0) { // 把小的值交换到后面
						String temp = pai.get(j);
						pai.set(j, pai.get(j + 1));
						pai.set(j + 1, temp);
					}
				} else if ("desc".equals(type)) {
					if (comparePai(pai.get(j), pai.get(j + 1)) < 0) { // 把小的值交换到后面
						String temp = pai.get(j);
						pai.set(j, pai.get(j + 1));
						pai.set(j + 1, temp);
					}
				}
			}
		}
		return pai;
	}

	/**
	 * 自定义牌比较大小
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static int comparePai(String arg0, String arg1) {
		if (arg0.equals(arg1)) {
			return 0;
		} else {
			if (getNumbers(arg0) > getNumbers(arg1)) {
				return 1;
			} else if (getNumbers(arg0) < getNumbers(arg1)) {
				return -1;
			} else {// 两者数值相等，判断花色
				if (getColor(arg0) > getColor(arg1)) {
					return 1;
				} else if (getColor(arg0) < getColor(arg1)) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
	
	public static boolean is8zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==8) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is7zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==7) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is6zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==6) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is5zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==5) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is4zhang(List<String> pai) {
//		boolean bl = false;
//		if(pai.size()==4) {
//			int temp = 0;
//			int flag = 0;
//			for(String p : pai) {
//				int ip = getNumbers(p);
//				if(temp != ip && ip != 16) {
//					temp = ip;
//					flag++;
//				}
//			}
//			if(flag==1) {
//				bl = true;
//			}
//		}
//		return bl;
		boolean bl = false;
		if(pai.size()==4) {
			if(getNumbers(pai.get(0)) == getNumbers(pai.get(1)) && getNumbers(pai.get(0)) == getNumbers(pai.get(2)) && getNumbers(pai.get(0)) == getNumbers(pai.get(3))) {
				bl = true;
			}
			for(String p : pai) {
				int ip = getNumbers(p);
				if(ip == 16) {
					bl = false;
					break;
				}
			}
		}
		return bl;
	}
	
	public static boolean is3zhang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getNumbers(pai.get(0)) == getNumbers(pai.get(1))  && getNumbers(pai.get(0)) == getNumbers(pai.get(2))) {
				bl = true;
			}
			for(String p : pai) {
				int ip = getNumbers(p);
				if(ip == 16) {
					bl = false;
					break;
				}
			}
		}
		return bl;
	}
	
	public static boolean is510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			pai = sortList(pai, "asc");
			if(getNumbers(pai.get(0)) == 5 && getNumbers(pai.get(1)) == 10 && getNumbers(pai.get(2)) == 13) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isHt510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 4 && getColor(pai.get(1)) == 4 && getColor(pai.get(2)) == 4 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isHx510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 3 && getColor(pai.get(1)) == 3 && getColor(pai.get(2)) == 3 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isMh510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 2 && getColor(pai.get(1)) == 2 && getColor(pai.get(2)) == 2 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isFk510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 1 && getColor(pai.get(1)) == 1 && getColor(pai.get(2)) == 1 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isMin510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) != getColor(pai.get(1)) || getColor(pai.get(0)) != getColor(pai.get(2)) || getColor(pai.get(1)) != getColor(pai.get(2))) {
				if(is510k(pai)) {
					bl = true;
				}
			}
		}
		return bl;
	}
	
	public static boolean is4wang(List<String> pai) {
		boolean bl = true;
		if(pai.size()==4) {
			for(String p : pai) {
				if(getNumbers(p) != 16) {
					bl = false;
					break;
				}
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean is3wang(List<String> pai) {
		boolean bl = true;
		if(pai.size()==3) {
			for(String p : pai) {
				if(getNumbers(p) != 16) {
					bl = false;
					break;
				}
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean is2dawang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==2) {
			if(getColor(pai.get(0)) == 6 && getColor(pai.get(1)) == 6) {
				bl = true;
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean is2xiaowang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==2) {
			if(getColor(pai.get(0)) == 5 && getColor(pai.get(1)) == 5) {
				bl = true;
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean is1dw1xw(List<String> pai) {
		boolean bl = false;
		if(pai.size()==2) {
			if((getColor(pai.get(0)) == 6 && getColor(pai.get(1)) == 5) || (getColor(pai.get(0)) == 5 && getColor(pai.get(1)) == 6)) {
				bl = true;
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean isDanzhang(List<String> pai) {
		if(pai.size()==1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isDuizi(List<String> pai) {
		if(pai.size()==2) {
			if(getNumbers(pai.get(0)) == getNumbers(pai.get(1)) && getNumbers(pai.get(0)) != 16) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * 是否连对
	 * @param pai
	 * @return
	 */
	public static boolean isLiandui(List<String> pai) {
		boolean bl = true;
		if(pai.size()%2 == 0 && pai.size() >=4) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = paiToShortAry(pai);
			if(ary[13] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 2 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 2) {
						xblist.add(i);
					}
				}
				
				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}
				
			}
		} else {
			bl = false;
		}
		return bl;
	}
	/**
	 * 是否连三张
	 * @param pai
	 * @return
	 */
	public static boolean isLian3zhang(List<String> pai) {
		boolean bl = true;
		if(pai.size()%3 == 0 && pai.size() >=6) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = paiToShortAry(pai);
			if(ary[13] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 3 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 3) {
						xblist.add(i);
					}
				}
				
				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}
				
			}
		} else {
			bl = false;
		}
		return bl;
	}
	/**
	 * 是否连四张
	 * @param pai
	 * @return
	 */
	public static boolean isLian4zhang(List<String> pai) {
		boolean bl = true;
		if(pai.size()%4 == 0 && pai.size() >=8) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = paiToShortAry(pai);
			if(ary[13] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 4 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 4) {
						xblist.add(i);
					}
				}
				
				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	/**
	 * 获取单张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsDanzhang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		for(String p : zjp) {
			if(getNumbers(p) > getNumbers(sjp.get(0))) {
				List<String> ts = new ArrayList<String>();
				ts.add(p);
				all.add(ts);
			}
			//单张小王要被大王打起
			if(getNumbers(sjp.get(0)) == 16 && getColor(sjp.get(0)) == 5) {
				if(getNumbers(p) == 16 && getColor(p) == 6) {
					List<String> ts = new ArrayList<String>();
					ts.add(p);
					all.add(ts);
				}
			}
		}
		
		return all;
	}
	
	/**
	 * 获取对子提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsDuizi(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		String[] zjpary = new String[zjp.size()];
		zjp.toArray(zjpary);
		combinationDz(zjpary, 2, list, 2);
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	
	/**
	 * 获取三张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs3zhang(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 3) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination3zhang(zjpary, 3, list, 3);
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	
	/**
	 * 获取4张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs4zhang(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 4) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination4zhang(zjpary, 4, list, 4);
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	
	/**
	 * 获取连对提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsLiandui(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<List<String>> ldlist = new ArrayList<List<String>>();
		List<Integer> xblist = new ArrayList<Integer>();
		sjp = sortList(sjp, "asc");
		List<String> zjp3 = new ArrayList<String>();
		zjp3.addAll(zjp);
		for(String p : zjp3) {
			if(getNumbers(p) < getNumbers(sjp.get(0))) {
				zjp.remove(p);
			}
		}
		
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 2) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		zjpary = TcgdSfNew.sortAry(zjpary, "asc");
		if(isLiandui(sjp)) {
			TcgdSfNew.combinationDz1(zjpary, 2, list, 2);
			int slen = sjp.size();
			int num = slen/2;
			if(slen <= zjp.size()) {
				TcgdSfNew.combinationLd(list, num, ldlist, num);
			}
		}
		
		for (int i = 0; i < ldlist.size(); i++) {
			List<String> pl = ldlist.get(i);
			String[] pary = new String[pl.size()];
			for (int j = 0; j < pl.size(); j++) {
				pary[j] = pl.get(j);
			}
			pary = TcgdSfNew.sortAry(pary, "asc");
			if(getNumbers(pary[0]) > getNumbers(sjp.get(0))) {
				all.add(pl);
			}
		}
		return all;
	}

	public static List<List<String>> getTsLiandui1(List<String> sjp, List<String> zjp) {
	
	List<List<String>> all = new ArrayList<List<String>>();
	List<List<String>> list = new ArrayList<List<String>>();
	String[] zjpary = new String[zjp.size()];
	zjp.toArray(zjpary);
	combinationLiandui(zjpary, sjp.size(), list, sjp.size());
	int snum = getNumbers(sjp.get(0));
	for (int i = 0; i < list.size(); i++) {
		List<String> l = list.get(i);
		if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
			all.add(l);
		}
	}
	return all;
}
	
	/**
	 * 获取连3张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsLian3zhang(List<String> sjp, List<String> zjp) {
//		List<List<String>> all = new ArrayList<List<String>>();
//		List<List<String>> list = new ArrayList<List<String>>();
//		String[] zjpary = new String[zjp.size()];
//		zjp.toArray(zjpary);
//		combinationLian3zhang(zjpary, sjp.size(), list, sjp.size());
//		int snum = getNumbers(sjp.get(0));
//		for (int i = 0; i < list.size(); i++) {
//			List<String> l = list.get(i);
//			if(getNumbers(l.get(0)) > snum) {
//				all.add(l);
//			}
//		}
//		return all;
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<List<String>> l3zlist = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 3) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		int num = sjp.size()/3;
		combination3zhang(zjpary, 3, list, 3);
		combinationl3z(list, num, l3zlist, num);
		
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < l3zlist.size(); i++) {
			List<String> l = l3zlist.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	
	/**
	 * 获取连4张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsLian4zhang(List<String> sjp, List<String> zjp) {
//		List<List<String>> all = new ArrayList<List<String>>();
//		List<List<String>> list = new ArrayList<List<String>>();
//		String[] zjpary = new String[zjp.size()];
//		zjp.toArray(zjpary);
//		combinationLian4zhang(zjpary, sjp.size(), list, sjp.size());
//		int snum = getNumbers(sjp.get(0));
//		for (int i = 0; i < list.size(); i++) {
//			List<String> l = list.get(i);
//			if(getNumbers(l.get(0)) > snum) {
//				all.add(l);
//			}
//		}
//		return all;
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<List<String>> l4zlist = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 4) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		int num = sjp.size()/4;
		combination4zhang(zjpary, 4, list, 4);
		combinationl4z(list, num, l4zlist, num);
		
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < l4zlist.size(); i++) {
			List<String> l = l4zlist.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	
	/**
	 * 获取副50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsMin50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
//		int[] ary = paiToShortAry(zjp);
//		for (int i = 0; i < ary.length; i++) {
//			if(ary[i] != 2 && ary[i] != 7 && ary[i] != 10) {
//				xblist.add(i);
//			}
//		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationMin50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取副50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs50k(List<String> sjp, List<String> zjp) {
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取方块50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsFk50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationFk50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取梅花50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsMh50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationMh50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取红心50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsHx50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationHx50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取黑桃50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsHt50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationHt50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取2w提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs2XiaoWang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(ts.size() == 2) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取1dw1xw提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs1dw1xw(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(ts.size() == 2) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取2W提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs2DaWang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(ts.size() == 2) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取3W提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs3Wang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(13);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination3Wang(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取4W提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs4Wang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(ts.size() == 4) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取5炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs5zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 5) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination5zha(zjpary, 5, list, 5);
		if(is5zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum  && !isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		} else {
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(!isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
			//return list;
		}
	}
	
	/**
	 * 获取6炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs6zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 6) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination6zha(zjpary, 6, list, 6);
		
		if(is6zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum  && !isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		} else {
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(!isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		}
	}
	
	/**
	 * 获取7炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs7zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 7) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination7zha(zjpary, 7, list, 7);
		if(is7zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum  && !isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		} else {
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(!isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		}
	}
	
	/**
	 * 获取8炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs8zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 8) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination8zha(zjpary, 8, list, 8);
		if(is8zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum) {
					all.add(l);
				}
			}
			return all;
		} else {
			return list;
		}
	}
	
	/** 
	 * 获取所有提示2
	 * @param sjp
	 * @param zjp
	 * @return 
	 */
	public static List<List<String>> getAllTs2(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		sjp = sortList(sjp, "asc");
		zjp = sortList(zjp, "asc");
		
		
		
		
		return all;
	}
	
	/** 
	 * 获取所有提示1
	 * @param sjp
	 * @param zjp
	 * @return 
	 */
	public static List<List<String>> getAllTs1(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		sjp = sortList(sjp, "asc");
		zjp = sortList(zjp, "asc");
		if(getCpType(sjp).equals("danz")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTsDanzhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "danz");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("duiz")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTsDuizi(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "duiz");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("3z")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTs3zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "3z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("4z")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTs4zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "4z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("ld")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTsLiandui(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "ld");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("l3z")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTsLian3zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "l3z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("l4z")) {
			List<String> zjps = new ArrayList<String>();
			zjps.addAll(zjp);
			List<List<String>> list = getTsLian4zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan1(sjp, zjps, "l4z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("min50k")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "min50k"));
		} else if(getCpType(sjp).equals("f50k")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "f50k"));
		} else if(getCpType(sjp).equals("m50k")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "m50k"));
		} else if(getCpType(sjp).equals("r50k")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "r50k"));
		} else if(getCpType(sjp).equals("b50k")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "b50k"));
		} else if(getCpType(sjp).equals("ww")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "ww"));
		} else if(getCpType(sjp).equals("Ww")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "Ww"));
		} else if(getCpType(sjp).equals("WW")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "WW"));
		} else if(getCpType(sjp).equals("5zha")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "5zha"));
		} else if(getCpType(sjp).equals("3w")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "3w"));
		} else if(getCpType(sjp).equals("6zha")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "6zha"));
		} else if(getCpType(sjp).equals("7zha")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "7zha"));
		} else if(getCpType(sjp).equals("8zha")) {
			all.addAll(getTsZhaDan1(sjp, zjp, "8zha"));
		}
		
		return all;
	}
	
	/** 
	 * 获取所有提示
	 * @param sjp
	 * @param zjp
	 * @return 
	 */
	public static List<List<String>> getAllTs(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		sjp = sortList(sjp, "asc");
		zjp = sortList(zjp, "asc");
		if(getCpType(sjp).equals("danz")) {
			List<List<String>> list = getTsDanzhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "danz");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("duiz")) {
			List<List<String>> list = getTsDuizi(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "duiz");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
//			all.addAll(getTsDuizi(sjp, zjp));
//			all.addAll(getTsZhaDan(sjp, zjp, "duiz"));//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("3z")) {
			List<List<String>> list = getTs3zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "3z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
//			all.addAll(getTs3zhang(sjp, zjp));
//			all.addAll(getTsZhaDan(sjp, zjp, "3z"));//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("4z")) {
			List<List<String>> list = getTs4zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "4z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
//			all.addAll(getTs4zhang(sjp, zjp));
//			all.addAll(getTsZhaDan(sjp, zjp, "4z"));//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("ld")) {
			List<List<String>> list = getTsLiandui(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "ld");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
//			all.addAll(getTsLiandui(sjp, zjp));
//			all.addAll(getTsZhaDan(sjp, zjp, "ld"));//额外获取50k，各种炸弹
		} else if(getCpType(sjp).equals("l3z")) {
			List<List<String>> list = getTsLian3zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "l3z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
//			all.addAll(getTsLian3zhang(sjp, zjp));
//			all.addAll(getTsZhaDan(sjp, zjp, "l3z"));
		} else if(getCpType(sjp).equals("l4z")) {
			List<List<String>> list = getTsLian4zhang(sjp, zjp);
			List<List<String>> zdlist = getTsZhaDan(sjp, zjp, "l4z");
			all.addAll(list);
			all.addAll(zdlist);//额外获取50k，各种炸弹
//			all.addAll(getTsLian4zhang(sjp, zjp));
//			all.addAll(getTsZhaDan(sjp, zjp, "l4z"));
		} else if(getCpType(sjp).equals("min50k")) {
			all.addAll(getTsZhaDan(sjp, zjp, "min50k"));
		} else if(getCpType(sjp).equals("f50k")) {
			all.addAll(getTsZhaDan(sjp, zjp, "f50k"));
		} else if(getCpType(sjp).equals("m50k")) {
			all.addAll(getTsZhaDan(sjp, zjp, "m50k"));
		} else if(getCpType(sjp).equals("r50k")) {
			all.addAll(getTsZhaDan(sjp, zjp, "r50k"));
		} else if(getCpType(sjp).equals("b50k")) {
			all.addAll(getTsZhaDan(sjp, zjp, "b50k"));
		} else if(getCpType(sjp).equals("ww")) {
			all.addAll(getTsZhaDan(sjp, zjp, "ww"));
		} else if(getCpType(sjp).equals("Ww")) {
			all.addAll(getTsZhaDan(sjp, zjp, "Ww"));
		} else if(getCpType(sjp).equals("WW")) {
			all.addAll(getTsZhaDan(sjp, zjp, "WW"));
		} else if(getCpType(sjp).equals("5zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "5zha"));
		} else if(getCpType(sjp).equals("3w")) {
			all.addAll(getTsZhaDan(sjp, zjp, "3w"));
		} else if(getCpType(sjp).equals("6zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "6zha"));
		} else if(getCpType(sjp).equals("7zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "7zha"));
		} else if(getCpType(sjp).equals("8zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "8zha"));
		}
		List<List<String>> all1 = new ArrayList<List<String>>();
		all1.addAll(all);
		List<List<String>> all2 = new ArrayList<List<String>>();
		all2.addAll(all);
		List<List<String>> all3 = new ArrayList<List<String>>();
		for(List<String> l : all) {
			if(!TcgdSf.isZhadan(l)) {
				for(String p : l) {
					for(List<String> l1 : all1) {
						if(l1.contains(p) && l1.size() != l.size()) {
							if(!all3.contains(l)) {
								all3.add(0, l);
							}
							all2.remove(l);
						}
					}
				}
			}
		}
		all2.addAll(all3);
		
		return all2;
	}
	
	public static List<List<String>> getTsZhaDan1(List<String> sjp, List<String> zjp, String type) {
		List<List<String>> all = new ArrayList<List<String>>();
		if(type.equals("danz") || type.equals("duiz") || type.equals("3z") || type.equals("4z")
				 || type.equals("ld") || type.equals("l3z") || type.equals("l4z") || type.equals("noBoom")) {
			all.addAll(getTsMin50k(sjp, zjp));
			all.addAll(getTsFk50k(sjp, zjp));
			all.addAll(getTsMh50k(sjp, zjp));
			all.addAll(getTsHx50k(sjp, zjp));
			all.addAll(getTsHt50k(sjp, zjp));
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2XiaoWang(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("min50k")) {
			all.addAll(getTsFk50k(sjp, zjp));
			all.addAll(getTsMh50k(sjp, zjp));
			all.addAll(getTsHx50k(sjp, zjp));
			all.addAll(getTsHt50k(sjp, zjp));
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2XiaoWang(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("f50k")) {
			all.addAll(getTsMh50k(sjp, zjp));
			all.addAll(getTsHx50k(sjp, zjp));
			all.addAll(getTsHt50k(sjp, zjp));
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2XiaoWang(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("m50k")) {
			all.addAll(getTsHx50k(sjp, zjp));
			all.addAll(getTsHt50k(sjp, zjp));
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2XiaoWang(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("r50k")) {
			all.addAll(getTsHt50k(sjp, zjp));
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2XiaoWang(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("b50k")) {
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2XiaoWang(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("ww")) {
			all.addAll(getTs1dw1xw(sjp, zjp));
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("Ww")) {
			all.addAll(getTs2DaWang(sjp, zjp));
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("WW")) {
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("5zha")) {
			all.addAll(getTs5zha(sjp, zjp));
			all.addAll(getTs3Wang(sjp, zjp));
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("3w")) {
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("6zha")) {
			all.addAll(getTs6zha(sjp, zjp));
			all.addAll(getTs4Wang(sjp, zjp));
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		}  else if(type.equals("4w")) {
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("7zha")) {
			all.addAll(getTs7zha(sjp, zjp));
			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("8zha")) {
			all.addAll(getTs8zha(sjp, zjp));
		}
		
		return all;
	}
	
	public static List<List<String>> getTsZhaDan(List<String> sjp, List<String> zjp, String type) {
		List<List<String>> all = new ArrayList<List<String>>();
		if(type.equals("danz") || type.equals("duiz") || type.equals("3z") || type.equals("4z")
				 || type.equals("ld") || type.equals("l3z") || type.equals("l4z")) {
			List<List<String>> min50klist = getTsMin50k(sjp, zjp);
			List<List<String>> fk50klist = getTsFk50k(sjp, zjp);
			List<List<String>> mh50klist = getTsMh50k(sjp, zjp);
			List<List<String>> hx50klist = getTsHx50k(sjp, zjp);
			List<List<String>> ht50klist = getTsHt50k(sjp, zjp);
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2xwlist = getTs2XiaoWang(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断副50K是不是拆了其他正50K，拆了的话放到houlist里，没有拆则直接放到all
			List<List<String>> zheng50klist = new ArrayList<List<String>>();
			zheng50klist.addAll(ht50klist);
			zheng50klist.addAll(hx50klist);
			zheng50klist.addAll(mh50klist);
			zheng50klist.addAll(fk50klist);
			for(List<String> list: min50klist) {
				if(is50kFuchaiZheng(list, zheng50klist, zjp)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(zheng50klist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _2xwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			} else {
				all.addAll(_1dw1xwlist);
				all.addAll(_2xwlist);
				all.addAll(_2dwlist);
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
//			all.addAll(getTsMin50k(sjp, zjp));
//			all.addAll(getTsFk50k(sjp, zjp));
//			all.addAll(getTsMh50k(sjp, zjp));
//			all.addAll(getTsHx50k(sjp, zjp));
//			all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
//			all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
//			all.addAll(getTs6zha(sjp, zjp));
//			all.addAll(getTs7zha(sjp, zjp));
//			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("min50k")) {
			List<List<String>> fk50klist = getTsFk50k(sjp, zjp);
			List<List<String>> mh50klist = getTsMh50k(sjp, zjp);
			List<List<String>> hx50klist = getTsHx50k(sjp, zjp);
			List<List<String>> ht50klist = getTsHt50k(sjp, zjp);
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2xwlist = getTs2XiaoWang(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断副50K是不是拆了其他正50K，拆了的话放到houlist里，没有拆则直接放到all
			List<List<String>> zheng50klist = new ArrayList<List<String>>();
			zheng50klist.addAll(ht50klist);
			zheng50klist.addAll(hx50klist);
			zheng50klist.addAll(mh50klist);
			zheng50klist.addAll(fk50klist);
			all.addAll(zheng50klist);
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _2xwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
//			all.addAll(getTsFk50k(sjp, zjp));
//			all.addAll(getTsMh50k(sjp, zjp));
//			all.addAll(getTsHx50k(sjp, zjp));
//			all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
//			all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
//			all.addAll(getTs6zha(sjp, zjp));
//			all.addAll(getTs4Wang(sjp, zjp));
//			all.addAll(getTs7zha(sjp, zjp));
//			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("f50k")) {
			List<List<String>> mh50klist = getTsMh50k(sjp, zjp);
			List<List<String>> hx50klist = getTsHx50k(sjp, zjp);
			List<List<String>> ht50klist = getTsHt50k(sjp, zjp);
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2xwlist = getTs2XiaoWang(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断副50K是不是拆了其他正50K，拆了的话放到houlist里，没有拆则直接放到all
			List<List<String>> zheng50klist = new ArrayList<List<String>>();
			zheng50klist.addAll(ht50klist);
			zheng50klist.addAll(hx50klist);
			zheng50klist.addAll(mh50klist);
			all.addAll(zheng50klist);
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _2xwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
//			all.addAll(getTsMh50k(sjp, zjp));
//			all.addAll(getTsHx50k(sjp, zjp));
//			all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
//			all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
//			all.addAll(getTs6zha(sjp, zjp));
//			all.addAll(getTs4Wang(sjp, zjp));
//			all.addAll(getTs7zha(sjp, zjp));
//			all.addAll(getTs8zha(sjp, zjp));
		} else if(type.equals("m50k")) {
			List<List<String>> hx50klist = getTsHx50k(sjp, zjp);
			List<List<String>> ht50klist = getTsHt50k(sjp, zjp);
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2xwlist = getTs2XiaoWang(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断副50K是不是拆了其他正50K，拆了的话放到houlist里，没有拆则直接放到all
			List<List<String>> zheng50klist = new ArrayList<List<String>>();
			zheng50klist.addAll(ht50klist);
			zheng50klist.addAll(hx50klist);
			all.addAll(zheng50klist);
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _2xwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("r50k")) {
			List<List<String>> ht50klist = getTsHt50k(sjp, zjp);
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2xwlist = getTs2XiaoWang(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断副50K是不是拆了其他正50K，拆了的话放到houlist里，没有拆则直接放到all
			List<List<String>> zheng50klist = new ArrayList<List<String>>();
			zheng50klist.addAll(ht50klist);
			all.addAll(zheng50klist);
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _2xwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("b50k")) {
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2xwlist = getTs2XiaoWang(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _2xwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("ww")) {
			List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, zjp);
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
				for(List<String> list : _1dw1xwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("Ww")) {
			List<List<String>> _2dwlist = getTs2DaWang(sjp, zjp);
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_3wlist.size() > 0) {
				for(List<String> list : _2dwlist) {
					houlist.add(0, list);
				}
			}
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("WW")) {
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("5zha")) {
			List<List<String>> _5zhalist = getTs5zha(sjp, zjp);
			List<List<String>> _3wlist = getTs3Wang(sjp, zjp);
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			//判断2个王的牌是不是拆了3W，4W 是的话放到houlist里，没有拆则直接放到all
			if(_4wlist.size() > 0) {
				for(List<String> list : _3wlist) {
					houlist.add(0, list);
				}
			}
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断5炸是不是拆了678炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _5zhalist) {
				if(isXiaochaiDa(list, _6zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("3w")) {
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("6zha")) {
			List<List<String>> _6zhalist = getTs6zha(sjp, zjp);
			List<List<String>> _4wlist = getTs4Wang(sjp, zjp);
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			all.addAll(_4wlist);
			all.addAll(houlist);
			houlist = new ArrayList<List<String>>();
			//判断6炸是不是拆了78炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _6zhalist) {
				if(isXiaochaiDa(list, _7zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		}  else if(type.equals("4w")) {
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			houlist = new ArrayList<List<String>>();
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("7zha")) {
			List<List<String>> _7zhalist = getTs7zha(sjp, zjp);
			List<List<String>> _8zhalist = getTs8zha(sjp, zjp);
			
			List<List<String>> houlist = new ArrayList<List<String>>();
			houlist = new ArrayList<List<String>>();
			//判断7炸是不是拆了8炸，是的话放到houlist里，没有拆则直接放到all
			for(List<String> list : _7zhalist) {
				if(isXiaochaiDa(list, _8zhalist)) {
					houlist.add(0, list);
				} else {
					all.add(list);
				}
			}
			all.addAll(_8zhalist);
			all.addAll(houlist);
		} else if(type.equals("8zha")) {
			all.addAll(getTs8zha(sjp, zjp));
		}
		
		return all;
	}
	
	/**
	 * 获取出牌类型
	 * @param pai
	 * @return
	 */
	public static String getCpType(List<String> pai) {
		String type = "";
		if(isDanzhang(pai)) {
			type = "danz";
		} else if(isDuizi(pai)) {
			type = "duiz";
		} else if(is3zhang(pai)) {
			type = "3z";
		} else if(is4zhang(pai)) {
			type = "4z";
		} else if(isLiandui(pai)) {
			type = "ld";
		} else if(isLian3zhang(pai)) {
			type = "l3z";
		} else if(isLian4zhang(pai)) {
			type = "l4z";
		} else if(isMin510k(pai)) {
			type = "min50k";
		} else if(isFk510k(pai)) {
			type = "f50k";
		} else if(isMh510k(pai)) {
			type = "m50k";
		} else if(isHx510k(pai)) {
			type = "r50k";
		} else if(isHt510k(pai)) {
			type = "b50k";
		} else if(is2xiaowang(pai)) {
			type = "ww";
		}  else if(is1dw1xw(pai)) {
			type = "Ww";
		} else if(is2dawang(pai)) {
			type = "WW";
		} else if(is5zha(pai)) {
			type = "5zha";
		} else if(is3wang(pai)) {
			type = "3w";
		} else if(is6zha(pai)) {
			type = "6zha";
		} else if(is4wang(pai)) {
			type = "4w";
		} else if(is7zha(pai)) {
			type = "7zha";
		} else if(is8zha(pai)) {
			type = "8zha";
		} 
		
		return type;
	}
	
	
	
	/////////////
	public static boolean isZhadan(List<String> pai) {
		boolean bl = false;
		if(isMin510k(pai) || isFk510k(pai) || isMh510k(pai) || isHx510k(pai) || isHt510k(pai)
				|| is2dawang(pai) || is2xiaowang(pai) || is1dw1xw(pai) || is5zha(pai) || is3wang(pai)
				|| is4wang(pai) || is6zha(pai) || is7zha(pai) || is8zha(pai)) {
			bl = true;
		}
		return bl;
	}
	
	/**
	 * 判断该组合在所有组合中是否已经存在，存在返回true，不存在返回false
	 * @param list
	 * @param all
	 * @return
	 */
	public static boolean isHave(List<String> list, List<List<String>> all) {
		for (int i = 0; i < all.size(); i++) {
			List<String> a1 = all.get(i);
			if(a1.size()==list.size() && getListSum(a1) == getListSum(list)) {
				return true;
			}
		}
		return false;
	}
	
	public static int getListSum(List<String> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += getNumbers(list.get(i)) + getColor(list.get(i)) * 100;
		}
		return sum;
	}
	
	public static boolean isHave1(List<String> list, List<List<String>> all) {
//		for (int i = 0; i < all.size(); i++) {
//			List<String> a1 = all.get(i);
//			if(a1.size()==list.size() && getListSum1(a1) == getListSum1(list)) {
//				return true;
//			}
//		}
//		return false;
		return false;
	}
	
	public static int getListSum1(List<String> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += getNumbers(list.get(i));
		}
		return sum;
	}
	
	public static void combinationDz(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationDz("", ia, n, list, dpnum);
    }

    public static void combinationDz(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isDuizi(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationDz(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination3zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination3zhang("", ia, n, list, dpnum);
    }

    public static void combination3zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is3zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination3zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination4zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination4zhang("", ia, n, list, dpnum);
    }

    public static void combination4zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is4zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination4zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationLiandui(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationLiandui("", ia, n, list, dpnum);
    }

    public static void combinationLiandui(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isLiandui(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLiandui(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationLian3zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationLian3zhang("", ia, n, list, dpnum);
    }

    public static void combinationLian3zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isLian3zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	               // }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLian3zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationLian4zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationLian4zhang("", ia, n, list, dpnum);
    }

    public static void combinationLian4zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isLian4zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	               // if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLian4zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationMin50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationMin50k("", ia, n, list, dpnum);
    }

    public static void combinationMin50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isMin510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationMin50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination50k("", ia, n, list, dpnum);
    }

    public static void combination50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	               // if(!isHave(list1, list)) {
	                	list.add(list1);
	               // }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationFk50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationFk50k("", ia, n, list, dpnum);
    }

    public static void combinationFk50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isFk510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationFk50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationMh50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationMh50k("", ia, n, list, dpnum);
    }

    public static void combinationMh50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isMh510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationMh50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationHx50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationHx50k("", ia, n, list, dpnum);
    }

    public static void combinationHx50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isHx510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationHx50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationHt50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationHt50k("", ia, n, list, dpnum);
    }

    public static void combinationHt50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isHt510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationHt50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination3Wang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination3Wang("", ia, n, list, dpnum);
    }

    public static void combination3Wang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is3wang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	               // }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination3Wang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    
    public static void combination6zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination6zha("", ia, n, list, dpnum);
    }

    public static void combination6zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is6zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	               // }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination6zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination7zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination7zha("", ia, n, list, dpnum);
    }

    public static void combination7zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is7zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination7zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination8zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination8zha("", ia, n, list, dpnum);
    }

    public static void combination8zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is8zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	               // }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination8zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination5zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination5zha("", ia, n, list, dpnum);
    }

    public static void combination5zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is5zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                //if(!isHave(list1, list)) {
	                	list.add(list1);
	                //}
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination5zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    
    public static void combinationl3z(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
    	List<String> l = new ArrayList<String>();
    	combinationl3z(l, ia, n, list, dpnum);
    }

    public static void combinationl3z(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s); totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                	iary[j] = totalStr.get(j);
				}
                List<String> pai = Arrays.asList(iary);
                if(isLian3zhang(pai) && iary.length==dpnum*3) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum*3; j++) {
	                	list1.add(iary[j]);
					}
	                list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
            	List<String> ss = new ArrayList<String>();
            	ss.addAll(s);
            	ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                	ii.add(ia.get(i + j + 1));
                }
                combinationl3z(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationl4z(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
    	List<String> l = new ArrayList<String>();
    	combinationl4z(l, ia, n, list, dpnum);
    }

    public static void combinationl4z(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s); totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                	iary[j] = totalStr.get(j);
				}
                List<String> pai = Arrays.asList(iary);
                if(isLian4zhang(pai) && iary.length==dpnum*3) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum*3; j++) {
	                	list1.add(iary[j]);
					}
	                list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
            	List<String> ss = new ArrayList<String>();
            	ss.addAll(s);
            	ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                	ii.add(ia.get(i + j + 1));
                }
                combinationl4z(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static Map<String, Object> isHaveXiqian(List<String> pai, String bbx) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	List<String> sjp = new ArrayList<String>();
    	List<List<String>> _8list = getTs8zha(sjp, pai);
    	List<List<String>> _7list = getTs7zha(sjp, pai);
    	List<List<String>> _6list = getTs6zha(sjp, pai);
    	List<List<String>> _4wlist = getTs4Wang(sjp, pai);
    	List<List<String>> all = new ArrayList<List<String>>();
    	all.addAll(_8list);
    	all.addAll(_4wlist);
    	for(List<String> list : _7list) {
			int pnum = getNumbers(list.get(0));
			if(!isHaveInLists(pnum, _8list) && !isHaveInLists(pnum, all)) {
				all.add(list);
			}
		}
    	for(List<String> list : _6list) {
			int pnum = getNumbers(list.get(0));
			if(!isHaveInLists(pnum, _7list) && !isHaveInLists(pnum, all)) {
				all.add(list);
			}
		}
    	String have = "0";
    	if(_8list.size() + _7list.size() + _6list.size() + _4wlist.size() > 0) {
    		if(bbx.equals("true")) {
        		have = "0";
        	} else {
        		have = "1";
        	}
    		map.put("have", have);
    		map.put("pai", all);
    	} else {
    		map.put("have", "0");
    		map.put("pai", all);
    	}
    	
    	return map;
    }
    
    public static boolean isHaveInLists(int pnum, List<List<String>> pai) {
    	boolean bl = false;
    	for(List<String> list : pai) {
    		int plnum = getNumbers(list.get(0));
    		if(pnum == plnum) {
    			bl = true;
    			break;
    		}
    	}
    	return bl;
    }
    
    /**
     * 排序
     * @param pai
     * @return
     */
    public static List<String> sortPai(List<String> pai, String type) {
    	List<String> slist = new ArrayList<String>();
    	List<String> sjp = new ArrayList<String>();
    	sjp.add("B0");
    	//pai = sortList(pai, "desc");
    	//获取所有炸弹，往左边排，从大到小获取一个remove一个
    	List<List<String>> _8zhalist = getTs8zha(sjp, pai);
    	for (int i = 0; i < _8zhalist.size(); i++) {
    		List<String> l = _8zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _7zhalist = getTs7zha(sjp, pai);
    	for (int i = 0; i < _7zhalist.size(); i++) {
    		List<String> l = _7zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _4wlist = getTs4Wang(sjp, pai);
    	for (int i = 0; i < _4wlist.size(); i++) {
    		List<String> l = _4wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _6zhalist = getTs6zha(sjp, pai);
    	for (int i = 0; i < _6zhalist.size(); i++) {
    		List<String> l = _6zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _3wlist = getTs3Wang(sjp, pai);
    	for (int i = 0; i < _3wlist.size(); i++) {
    		List<String> l = _3wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _5zhalist = getTs5zha(sjp, pai);
    	for (int i = 0; i < _5zhalist.size(); i++) {
    		List<String> l = _5zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _2Wlist = getTs2DaWang(sjp, pai);
    	for (int i = 0; i < _2Wlist.size(); i++) {
    		List<String> l = _2Wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
			slist.addAll(l);
		}
    	List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, pai);
    	for (int i = 0; i < _1dw1xwlist.size(); i++) {
    		List<String> l = _1dw1xwlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _2wlist = getTs2XiaoWang(sjp, pai);
    	for (int i = 0; i < _2wlist.size(); i++) {
    		List<String> l = _2wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
			slist.addAll(l);
		}
//    	List<List<String>> _ht50k = getTsHt50k(sjp, pai);
//    	for (int i = 0; i < _ht50k.size(); i++) {
//    		List<String> l = _ht50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_ht50k = getTsHt50k(sjp, pai);
//		}
//    	List<List<String>> _hx50k = getTsHx50k(sjp, pai);
//    	for (int i = 0; i < _hx50k.size(); i++) {
//    		List<String> l = _hx50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_hx50k = getTsHx50k(sjp, pai);
//		}
//    	List<List<String>> _mh50k = getTsMh50k(sjp, pai);
//    	for (int i = 0; i < _mh50k.size(); i++) {
//    		List<String> l = _mh50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_ht50k = getTsHt50k(sjp, pai);
//		}
    	List<List<String>> _ht50k = getTsHt50k(sjp, pai);
    	List<String> _ht50kslist = new ArrayList<String>();
    	while(_ht50k.size() > 0) {
    		List<String> l = _ht50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_ht50kslist.addAll(l);
    		_ht50k = getTsHt50k(sjp, pai);
    	}
		slist.addAll(_ht50kslist);
		
		List<List<String>> _hx50k = getTsHx50k(sjp, pai);
    	List<String> _hx50kslist = new ArrayList<String>();
    	while(_hx50k.size() > 0) {
    		List<String> l = _hx50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_hx50kslist.addAll(l);
    		_hx50k = getTsHx50k(sjp, pai);
    	}
		slist.addAll(_hx50kslist);
		
    	List<List<String>> _mh50k = getTsMh50k(sjp, pai);
    	List<String> _mh50kslist = new ArrayList<String>();
    	while(_mh50k.size() > 0) {
    		List<String> l = _mh50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_mh50kslist.addAll(l);
    		_mh50k = getTsMh50k(sjp, pai);
    	}
		slist.addAll(_mh50kslist);
    	
//    	List<List<String>> _fk50k = getTsFk50k(sjp, pai);
//    	for (int i = 0; i < _fk50k.size(); i++) {
//    		List<String> l = _fk50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_fk50k = getTsFk50k(sjp, pai);
//		}
    	
    	List<List<String>> _fk50k = getTsFk50k(sjp, pai);
    	List<String> _fk50kslist = new ArrayList<String>();
    	while(_fk50k.size() > 0) {
    		List<String> l = _fk50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_fk50kslist.addAll(l);
    		_fk50k = getTsFk50k(sjp, pai);
    	}
		slist.addAll(_fk50kslist);
//    	List<List<String>> _min50k = getTsMin50k(sjp, pai);
//    	List<String> _min50kslist = new ArrayList<String>();
//    	for (int i = 0; i < _min50k.size(); i++) {
//    		List<String> l = _min50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//    		_min50kslist.addAll(l);
//			_min50k = getTsMin50k(sjp, pai);
//		}
//    	List<String> _min50kslist1 = new ArrayList<String>();
//    	_min50kslist1.addAll(_min50kslist);
//    	//如果该副50K已经在正套里了，则删除
//    	for(int i=0; i<_min50kslist1.size(); i++) {
//    		String p = _min50kslist1.get(i);
//    		if(_ht50k.contains(p) || _hx50k.contains(p) || _mh50k.contains(p) || _fk50k.contains(p)) {
//    			_min50kslist.remove(i);
//    		}
//    	}
//    	slist.addAll(_min50kslist);
    	
    	
//    	List<List<String>> _min50k = getTsMin50k(sjp, pai);
//    	List<String> _min50kslist = new ArrayList<String>();
//    	for (int i = 0; i < _min50k.size(); i++) {
//    		List<String> l = _min50k.get(i);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//    		_min50kslist.addAll(l);
//			_min50k = getTsMin50k(sjp, pai);
//			i=0;
//		}
//    	slist.addAll(_min50kslist);
    	
    	List<List<String>> _min50k = getTsMin50k(sjp, pai);
    	List<String> _min50kslist = new ArrayList<String>();
    	while(_min50k.size() > 0) {
    		List<String> l = _min50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_min50kslist.addAll(l);
    		_min50k = getTsMin50k(sjp, pai);
    	}
		slist.addAll(_min50kslist);
    	
    	
		if(type.equals("1")) {
			pai = sortList(pai, "desc");
			slist.addAll(pai);
    	} else {
    		List<List<String>> _4zhang = getTs4zhang(sjp, pai);
    		List<String> _4slist = new ArrayList<String>();
    		while(_4zhang.size() > 0) {
        		List<String> l = _4zhang.get(0);
        		pai = myRemoveAll(pai, l);
        		l = sortList(l, "desc");
        		_4slist.addAll(l);
    			_4zhang = getTs4zhang(sjp, pai);
        	}
    		_4slist = sortList(_4slist, "desc");
    		slist.addAll(_4slist);

    		List<List<String>> _3zhang = getTs3zhang(sjp, pai);
        	List<String> _3slist = new ArrayList<String>();
        	while(_3zhang.size() > 0) {
        		List<String> l = _3zhang.get(0);
        		pai = myRemoveAll(pai, l);
        		l = sortList(l, "desc");
        		_3slist.addAll(l);
    			_3zhang = getTs3zhang(sjp, pai);
        	}
        	_3slist = sortList(_3slist, "desc");
    		slist.addAll(_3slist);

        	List<List<String>> _duizi = getTsDuizi(sjp, pai);
        	List<String> _dzslist = new ArrayList<String>();
        	while(_duizi.size() > 0) {
        		List<String> l = _duizi.get(0);
        		pai = myRemoveAll(pai, l);
        		l = sortList(l, "desc");
        		_dzslist.addAll(l);
    			_duizi = getTsDuizi(sjp, pai);
        	}
        	_dzslist = sortList(_dzslist, "desc");
    		slist.addAll(_dzslist);

        	pai = sortList(pai, "desc");
			slist.addAll(pai);
    	}
    	return slist;
    }
    
    public static List<String> myRemoveAll(List<String> pai, List<String> l) {
    	for (String p : l) {
			if(pai.contains(p)) {
				pai.remove(p);
			}
		}
    	return pai;
    }
    
    public static boolean isXiaochaiDa(List<String> zha, List<List<String>> zhalist) {
    	boolean bl = false;
    	int num = getNumbers(zha.get(0));
    	for(List<String> list : zhalist) {
    		int num1 = getNumbers(list.get(0));
    		if(num == num1) {
    			bl = true;
    		}
    	}
    	return bl;
    }
    
    public static boolean is50kFuchaiZheng(List<String> fu, List<List<String>> zheng, List<String> pai) {
    	boolean bl = false;
    	for(String p : fu) {
    		for(List<String> list : zheng) {
    			if(getStringInListCount(p, pai) == 1 && list.contains(p)) {
    				bl = true;
    				break;
    			}
    		}
    	}
    	return bl;
    }
    
    public static int getStringInListCount(String p, List<String> pai) {
    	int num = 0;
    	for(String pp : pai) {
    		if(p.equals(pp)) {
    			num++;
    		}
    	}
    	return num;
    }
}
