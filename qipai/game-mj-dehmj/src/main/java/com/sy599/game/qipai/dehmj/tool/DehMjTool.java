package com.sy599.game.qipai.dehmj.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.dehmj.bean.DehMjHuLack;
import com.sy599.game.qipai.dehmj.bean.DehMjPlayer;
import com.sy599.game.qipai.dehmj.bean.DehMjTable;
import com.sy599.game.qipai.dehmj.bean.DehMjiangHu;
import com.sy599.game.qipai.dehmj.constant.DehMjAction;
import com.sy599.game.qipai.dehmj.constant.DehMjConstants;
import com.sy599.game.qipai.dehmj.rule.DehMj;
import com.sy599.game.qipai.dehmj.rule.DehMjHelper;
import com.sy599.game.qipai.dehmj.rule.DehMjIndex;
import com.sy599.game.qipai.dehmj.rule.DehMjIndexArr;
import com.sy599.game.qipai.dehmj.rule.DehMjRule;
import com.sy599.game.qipai.dehmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author liuping
 * 
 */
public class DehMjTool {


	public static synchronized List<List<DehMj>> fapai(List<Integer> copy, int playerCount) {
		List<List<DehMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<DehMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(DehMj.getMajang(id));
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
	
	
	/***
	 * 
	 * @param majiangIds
	 * @param gang
	 * @param peng
	 * @param chi
	 * @param buzhang
	 * @param jiang258
	 * @return
	 */
	public  static  List<DehMj> getTingMjs(List<DehMj> majiangIds,DehMjPlayer player,DehMjTable table2,List<Integer> allMjs){
		if (majiangIds == null || majiangIds.isEmpty()) {
			return null;
		}
		List<DehMj> res = new LinkedList<>();
		
		
		for(DehMj mjx :DehMj.fullMj) {
			int id = mjx.getId();
			int idx =getOtherId(majiangIds, id,allMjs);
			if(idx ==0) {
				continue;
			}
			
			DehMjiangHu hu = new DehMjiangHu();
			DehMj mj = DehMj.getMajang(idx);
			majiangIds.add(mj);
			hu = DehMjTool.isHuBaoShan(majiangIds,table2,player,mj,false);
			
			if (hu.isHu()) {
				res.add(mj);
			}
			majiangIds.remove(mj);
		}
		return res;
		
	}
	
	
	public static int getOtherId(List<DehMj> majiangIds,int id,List<Integer> allMjs) {
		
		List<Integer> list = new ArrayList<>();
		DehMj omj = DehMj.getMajang(id);
		
		for(Integer idx: allMjs) {
			DehMj cm =  DehMj.getMajang(idx);
			if(omj.getVal()==cm.getVal()) {
				list.add(idx);
			}
		}
		
		List<Integer> list2 = new ArrayList<>();
		for(DehMj mj: majiangIds) {
			if(omj.getVal()==mj.getVal()) {
				list2.add(mj.getId());
			}
		}
		
		list.removeAll(list2);
		if(list.size()>0) {
			return list.get(0);
		}
		return 0;
	}
	
	
	
	public static synchronized List<List<DehMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
		List<List<DehMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<List<DehMj>> zpList = new ArrayList<>();
		if (GameServerConfig.isDebug() && t != null && !t.isEmpty()){
			for (List<Integer> zp : t){
				zpList.add(DehMjHelper.find(copy, zp));
			}
		}
		List<DehMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(DehMj.getMajang(id));
		}
		int count = 0;
		for (int i = 0; i < playerCount; i++) {
			if (i == 0) {
				if(zpList.size() > 0){
					List<DehMj> pai = zpList.get(0);
					int len = 14-pai.size();
					pai.addAll(allMjs.subList(count, len));
					count += len;
					list.add(new ArrayList<>(pai));
				}else{
					list.add(new ArrayList<>(allMjs.subList(0, 14)));
				}
			} else {
				if(zpList.size() > i){
					List<DehMj> pai = zpList.get(i);
					int len = 13-pai.size();
					pai.addAll(allMjs.subList(count, count+len));
					count += len;
					list.add(new ArrayList<>(pai));
				}else{
					list.add(new ArrayList<>(allMjs.subList(count, count+13)));
					count += 13;
				}
			}
			if (i == playerCount - 1) {
				if(zpList.size() > i+1){
					List<DehMj> pai = zpList.get(i+1);
					pai.addAll(allMjs.subList(count, allMjs.size()));
					list.add(new ArrayList<>(pai));
				}else{
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
//	public static boolean isHu(List<DehMj> majiangIds, int playType) {
//		if (playType == ZZMajiangConstants.play_type_zhuanzhuan) {
//			return isHuZhuanzhuan(majiangIds);
//
//		} else if (playType == ZZMajiangConstants.play_type_changesha) {
//			// return isHuChangsha(majiangIds);
//		} else if (playType == ZZMajiangConstants.play_type_hongzhong) {
//			return isHuZhuanzhuan(majiangIds);
//		}
//
//		return false;
//	}

	/**
	 * 保山麻将胡牌
	 * 
	 * @param majiangIds
	 * @return 0平胡 1 碰碰胡 2将将胡 3清一色 4双豪华7小对 5豪华小对 6:7小对 7全求人 8大四喜 9板板胡 10缺一色 11六六顺
	 */
//	public static DehMjiangHu isHuBaoShan(List<DehMj> majiangIds, List<DehMj> gang, List<DehMj> peng, List<DehMj> chi, List<DehMj> buzhang, boolean isbegin,DehMj huCard) {
//		DehMjiangHu hu = new DehMjiangHu();
//		if (majiangIds == null || majiangIds.isEmpty()) {
//			return hu;
//		}
//
//		if (isPingHu(majiangIds)) {
//			hu.setPingHu(true);
//			hu.setHu(true);
//			
//		}
//		// 麻将大胡检测
//		DehMjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang,huCard,hu.isHu());
//		/*// 小胡检测
//		DehMjRule.checkXiaoHu(hu, majiangIds, isbegin);*/
//		if (hu.isHu()) {
//			hu.setShowMajiangs(majiangIds);
//		}
//		if (hu.isDahu()) {
//			hu.initDahuList();
//		}
//		return hu;
//	}
	
	
	
	public static DehMjiangHu  isHuBaoShan(List<DehMj> majiangIds,DehMjTable table, DehMjPlayer player ,DehMj huCard,boolean zimo) {
		
		
		DehMjiangHu hu = new DehMjiangHu();
		if (majiangIds == null || majiangIds.isEmpty()) {
			return hu;
		}

		if (isPingHu(majiangIds)) {
			hu.setPingHu(true);
			hu.setHu(true);
			
		}
		// 麻将大胡检测
		DehMjRule.checkDahu(hu, majiangIds, player.getGang(), player.getPeng(),huCard,hu.isHu(),table);
	
		if (hu.isHu()) {
			hu.setShowMajiangs(majiangIds);
			if(hu.isShiGuiYiHu()){
				if (table.getMoMajiangSeat()!=player.getSeat()) {
					hu.setShiGuiYi(false);
				}
			}
		}
		if (hu.isDahu()) {
			hu.initDahuList();
		}
		return hu;
		
	}
	
	
	
	
	
	
	/**
	 * 
	 * 进这个接口都是能胡 
	 * */

	public static void checkDahuMax(DehMjiangHu hu,DehMjTable table, DehMjPlayer player ,DehMj huCard,boolean zimo) {
		
		if(!hu.isHu()) {
			return;
		}
		if(table.getBaoting() ==1 && player.getBaoTingS()==1) {
			hu.setBaoting(true);
		}
		if(zimo && !player.isChiPengGang()) {
			if(table.getDaiGen()!=1){
				hu.setMenQing(true);
			}
		}
		boolean isKa= false;
		for (DehMjPlayer player2 :table.getSeatMap2().values()) {
			if(player2.getSeat() != player.getSeat()) {
				List<DehMj> cards = DehMjQipaiTool.getVal(player2.getPeng(), huCard.getVal());
				if(cards.size() >0) {
					
					
					isKa = true;
					break;
				}
			}
        }
		//&&table.getBuyPoint()>2
		if(isKa){
			hu.setKaJueZhang(true);
		}
		
	}

	public static boolean isPingHu(List<DehMj> majiangIds) {
		return isPingHu(majiangIds, false);

	}

	public static boolean isPingHu(List<DehMj> majiangIds, boolean needJiang258) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}
		if (majiangIds.size() % 3 != 2) {
			return false;

		}

		// 先去掉红中
		List<DehMj> copy = new ArrayList<>(majiangIds);
		//List<DehMj> hongzhongList = dropHongzhong(copy);

		DehMjIndexArr card_index = new DehMjIndexArr();
		DehMjQipaiTool.getMax(card_index, copy);
		// 拆将
		if (chaijiang(card_index, copy, 0, needJiang258)) {
			System.out.println("胡牌");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 转转麻将胡牌
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static boolean isHuZhuanzhuan(List<DehMj> majiangIds) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}

		List<DehMj> copy = new ArrayList<>(majiangIds);
		// 先去掉红中
		List<DehMj> hongzhongList = dropHongzhong(copy);
		if (hongzhongList.size() == 4) {
			// 4张红中直接胡
			return true;
		}
		if (majiangIds.size() % 3 != 2) {
			System.out.println("%3！=2");
			return false;

		}

		DehMjIndexArr card_index = new DehMjIndexArr();
		DehMjQipaiTool.getMax(card_index, copy);
		if (check7duizi(copy, card_index, hongzhongList.size())) {
			System.out.println("胡牌7对");
			return true;
		}
		// 拆将
		if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
			System.out.println("胡牌");
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
	public static boolean check7duizi(List<DehMj> majiangIds, DehMjIndexArr card_index, int hongzhongNum) {
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

			DehMjIndex index0 = card_index.getMajiangIndex(0);
			DehMjIndex index2 = card_index.getMajiangIndex(2);
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

	// 拆将
	public static boolean chaijiang(DehMjIndexArr card_index, List<DehMj> hasPais, int hongzhongnum, boolean needJiang258) {
		Map<Integer, List<DehMj>> jiangMap = card_index.getJiang(needJiang258);
		for (Entry<Integer, List<DehMj>> valEntry : jiangMap.entrySet()) {
			List<DehMj> copy = new ArrayList<>(hasPais);
			DehMjHuLack lack = new DehMjHuLack(hongzhongnum);
			List<DehMj> list = valEntry.getValue();
			int i = 0;
			for (DehMj majiang : list) {
				i++;
				copy.remove(majiang);
				if (i >= 2) {
					break;
				}
			}
			lack.setHasJiang(true);
			boolean hu = chaipai(lack, copy, needJiang258);
			if (hu) {
				System.out.println(JacksonUtil.writeValueAsString(lack));
				return hu;
			}
		}

		if (hongzhongnum > 0) {
			// 只剩下红中
			if (hasPais.isEmpty()) {
				return true;
			}
			// 没有将
			for (DehMj majiang : hasPais) {
				List<DehMj> copy = new ArrayList<>(hasPais);
				DehMjHuLack lack = new DehMjHuLack(hongzhongnum);
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
					System.out.println(JacksonUtil.writeValueAsString(lack));
					return true;
				}
				if (!lack.isHasJiang() && hu) {
					if (lack.getHongzhongNum() == 2) {
						// 红中做将
						System.out.println(JacksonUtil.writeValueAsString(lack));
						return true;
					}
				}
			}
		}

		return false;
	}

	// 拆牌
	public static boolean chaipai(DehMjHuLack lack, List<DehMj> hasPais, boolean isNeedJiang258) {
		if (hasPais.isEmpty()) {
			return true;

		}
		boolean hu = chaishun(lack, hasPais, isNeedJiang258);
		if (hu)
			return true;
		return false;
	}

	public static void sortMin(List<DehMj> hasPais) {
		Collections.sort(hasPais, new Comparator<DehMj>() {

			@Override
			public int compare(DehMj o1, DehMj o2) {
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
	public static boolean chaishun(DehMjHuLack lack, List<DehMj> hasPais, boolean needJiang258) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		DehMj minMajiang = hasPais.get(0);
		int minVal = minMajiang.getVal();
		List<DehMj> minList = DehMjQipaiTool.getVal(hasPais, minVal);
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
		List<DehMj> num1 = DehMjQipaiTool.getVal(hasPais, pai1);
		List<DehMj> num2 = DehMjQipaiTool.getVal(hasPais, pai2);
		List<DehMj> num3 = DehMjQipaiTool.getVal(hasPais, pai3);

		// 找到一句话的麻将
		List<DehMj> hasMajiangList = new ArrayList<>();
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
				List<DehMj> count = DehMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

				List<DehMj> count = DehMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
				List<DehMj> count1 = DehMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				List<DehMj> count2 = DehMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
				List<DehMj> count3 = DehMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
				if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
					List<DehMj> copy = new ArrayList<>(hasPais);
					copy.removeAll(count1);
					DehMjHuLack copyLack = lack.copy();
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

	public static boolean isCanAsJiang(DehMj majiang, boolean isNeed258) {
		if (isNeed258) {
			if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
				return true;
			}
			return false;
		} else {
			return true;
		}

	}

	public static List<DehMj> checkChi(List<DehMj> majiangs, DehMj dismajiang) {
		return new ArrayList<DehMj>();
		//return checkChi(majiangs, dismajiang, null);
	}

	/**
	 * 是否能吃
	 * 
	 * @param majiangs
	 * @param dismajiang
	 * @return
	 */
	public static List<DehMj> checkChi(List<DehMj> majiangs, DehMj dismajiang, List<Integer> wangValList) {
		int disMajiangVal = dismajiang.getVal();
		List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
		List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
		List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

		List<Integer> majiangIds = DehMjHelper.toMajiangVals(majiangs);
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
		return new ArrayList<DehMj>();
	}

	public static List<DehMj> findMajiangByVals(List<DehMj> majiangs, List<Integer> vals) {
		List<DehMj> result = new ArrayList<>();
		for (int val : vals) {
			for (DehMj majiang : majiangs) {
				if (majiang.getVal() == val) {
					result.add(majiang);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 去掉红中
	 * 
	 * @param copy
	 * @return
	 */
	public static List<DehMj> dropHongzhong(List<DehMj> copy) {
		List<DehMj> hongzhong = new ArrayList<>();
		Iterator<DehMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			DehMj majiang = iterator.next();
			if (majiang.getVal() > 200) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
		if (majiangs instanceof List) {
			List list = (List) majiangs;
			for (Object majiang : list) {
				int val = 0;
				if (majiang instanceof DehMj) {
					val = ((DehMj) majiang).getVal();
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
	 * @param majiangs
	 *            麻将牌
	 * @param majiang
	 *            麻将
	 * @param num
	 *            想要的数量
	 * @return
	 */
	public static List<DehMj> getSameMajiang(List<DehMj> majiangs, DehMj majiang, int num) {
		List<DehMj> hongzhong = new ArrayList<>();
		int i = 0;
		for (DehMj maji : majiangs) {
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
	public static List<DehMj> dropMjId(List<DehMj> copy, int id) {
		List<DehMj> hongzhong = new ArrayList<>();
		Iterator<DehMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			DehMj majiang = iterator.next();
			if (majiang.getId() == id) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static void sortMinPoint(List<DehMj> handPais) {
		Collections.sort(handPais, new Comparator<DehMj>() {

			@Override
			public int compare(DehMj o1, DehMj o2) {
				return o1.getVal() - o2.getVal();
			}

		});
	}

	public static void main(String[] args) {
//        testHuPai();
		List<Integer> moTailPai = new ArrayList<>(Arrays.asList(0,1,2,4,5));
		System.out.println(moTailPai);
		moTailPai = addMoTailPai(moTailPai,15);
		System.out.println(moTailPai);

//		testHuPai();
	}

	public static void testHuPai() {
		String pais = "11,11,12,12,13,13,14,14,21,21,31,31,22,22";
		pais = "12,12,12,12,13,13,13,14,14,22,22,32,32,33";
		pais = "11,11,11,33,33,33,37,37,37,39,39,39,24,24";
		pais = "27,27,27,27,14,14,14,14,16,16,11,11,19,201";
		pais = "11,12,13,17,18,19,31,32,33,35,36,37,28,28";
		pais = "11,12,13,15,16,17,18,18,24,25,23";
		List<DehMj> handPais = getPais(pais);
		System.out.println(toString(handPais));
		List<DehMj> gangList = new ArrayList<>();
		List<DehMj> pengList = new ArrayList<>();
		List<DehMj> chiList = new ArrayList<>();
		List<DehMj> buZhangList = new ArrayList<>();
		boolean isBegin = false;
		List<DehMj> copy = new ArrayList<>(handPais);
//		
//		DehMjiangHu hu = isHuBaoShan(copy, gangList, pengList,isBegin,copy.get(0));
//		StringBuilder sb = new StringBuilder("");
//		sb.append("hu:").append(hu.isHu());
//		sb.append("--xiaoHuList:").append(hu.getXiaohuList());
//		sb.append("--xiaoHu:").append(actListToString(hu.getXiaohuList()));
//		System.out.println(sb.toString());
	}

	public static String toString(List<DehMj> handPais) {
		sortMinPoint(handPais);
		String paiStr = "";
		for (DehMj mj : handPais) {
			paiStr += mj + ",";
		}
		return paiStr;
	}

	public static List<DehMj> getPais(String paisStr){
		String [] pais = paisStr.split(",");
		List<DehMj> handPais = new ArrayList<>();
		for (String pai : pais) {
			for(DehMj mj : DehMj.values()){
				if(mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)){
					handPais.add(mj);
					break;
				}
			}
		}
		return handPais;
	}

	
    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<DehMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<DehMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (DehMj mj : DehMj.fullMj) {
            if (mj.isHongzhong() || have.contains(mj.getVal())) {
                continue;
            }
            int cardIndex = HuUtil.getMjIndex(mj);
            cardArr[cardIndex] = cardArr[cardIndex] + 1;
            if (hu7dui && HuUtil.isCanHu7Dui(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            if (HuUtil.isCanHu(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            cardArr[cardIndex] = cardArr[cardIndex] - 1;
        }
        if (lackPaiList.size() == 27) {
            lackPaiList.clear();
            lackPaiList.add(null);
        }
        return lackPaiList;
    }

	
	
	public static String actListToString(List<Integer> actList){
		StringBuilder sb = new StringBuilder();
		if(actList != null && actList.size() >0){
			sb.append("[");
			for (int i = 0; i < actList.size(); i++) {
				if (actList.get(i) == 1) {
					if (sb.length() > 1) {
						sb.append(",");
					}
					if (i == DehMjAction.HU) {
						sb.append("hu");
					} else if (i == DehMjAction.PENG) {
						sb.append("peng");
					} else if (i == DehMjAction.MINGGANG) {
						sb.append("mingGang");
					} else if (i == DehMjAction.ANGANG) {
						sb.append("anGang");
					} else if (i == DehMjAction.CHI) {
						sb.append("chi");
					} else if (i == DehMjAction.BUZHANG) {
						sb.append("buZhang");
					} else if (i == DehMjAction.QUEYISE) {
						sb.append("queYiSe");
					}
				}
			}
			sb.append("]");
		}
		return sb.toString();
	}

	public static List<Integer> addMoTailPai(List<Integer> moTailPai,int gangDice) {
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

}
