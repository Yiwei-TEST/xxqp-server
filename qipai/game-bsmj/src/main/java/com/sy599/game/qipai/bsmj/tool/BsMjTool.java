package com.sy599.game.qipai.bsmj.tool;

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
import com.sy599.game.qipai.bsmj.bean.BsMjHuLack;
import com.sy599.game.qipai.bsmj.bean.BsMjPlayer;
import com.sy599.game.qipai.bsmj.bean.BsMjTable;
import com.sy599.game.qipai.bsmj.bean.BsMjiangHu;
import com.sy599.game.qipai.bsmj.constant.BsMjAction;
import com.sy599.game.qipai.bsmj.constant.BsMjConstants;
import com.sy599.game.qipai.bsmj.rule.BsMj;
import com.sy599.game.qipai.bsmj.rule.BsMjHelper;
import com.sy599.game.qipai.bsmj.rule.BsMjIndex;
import com.sy599.game.qipai.bsmj.rule.BsMjIndexArr;
import com.sy599.game.qipai.bsmj.rule.BsMjRule;
import com.sy599.game.qipai.bsmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author liuping
 * 
 */
public class BsMjTool {


	public static synchronized List<List<BsMj>> fapai(List<Integer> copy, int playerCount) {
		List<List<BsMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<BsMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(BsMj.getMajang(id));
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
	public  static  List<BsMj> getTingMjs(List<BsMj> majiangIds,BsMjPlayer player,BsMjTable table2,List<Integer> allMjs){
		if (majiangIds == null || majiangIds.isEmpty()) {
			return null;
		}
		List<BsMj> res = new LinkedList<>();
		
		
		for(BsMj mjx :BsMj.fullMj) {
			int id = mjx.getId();
			int idx =getOtherId(majiangIds, id,allMjs);
			if(idx ==0) {
				continue;
			}
			
			BsMjiangHu hu = new BsMjiangHu();
			BsMj mj = BsMj.getMajang(idx);
			majiangIds.add(mj);
			hu = BsMjTool.isHuBaoShan(majiangIds,table2,player,mj,false);
			
			if (hu.isHu()) {
				res.add(mj);
			}
			majiangIds.remove(mj);
		}
		return res;
		
	}
	
	
	public static int getOtherId(List<BsMj> majiangIds,int id,List<Integer> allMjs) {
		
		List<Integer> list = new ArrayList<>();
		BsMj omj = BsMj.getMajang(id);
		
		for(Integer idx: allMjs) {
			BsMj cm =  BsMj.getMajang(idx);
			if(omj.getVal()==cm.getVal()) {
				list.add(idx);
			}
		}
		
		List<Integer> list2 = new ArrayList<>();
		for(BsMj mj: majiangIds) {
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
	
	
	
	public static synchronized List<List<BsMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
		List<List<BsMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<List<BsMj>> zpList = new ArrayList<>();
		if (GameServerConfig.isDebug() && t != null && !t.isEmpty()){
			for (List<Integer> zp : t){
				zpList.add(BsMjHelper.find(copy, zp));
			}
		}
		List<BsMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(BsMj.getMajang(id));
		}
		int count = 0;
		for (int i = 0; i < playerCount; i++) {
			if (i == 0) {
				if(zpList.size() > 0){
					List<BsMj> pai = zpList.get(0);
					int len = 14-pai.size();
					pai.addAll(allMjs.subList(count, len));
					count += len;
					list.add(new ArrayList<>(pai));
				}else{
					list.add(new ArrayList<>(allMjs.subList(0, 14)));
				}
			} else {
				if(zpList.size() > i){
					List<BsMj> pai = zpList.get(i);
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
					List<BsMj> pai = zpList.get(i+1);
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
//	public static boolean isHu(List<BsMj> majiangIds, int playType) {
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
//	public static BsMjiangHu isHuBaoShan(List<BsMj> majiangIds, List<BsMj> gang, List<BsMj> peng, List<BsMj> chi, List<BsMj> buzhang, boolean isbegin,BsMj huCard) {
//		BsMjiangHu hu = new BsMjiangHu();
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
//		BsMjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang,huCard,hu.isHu());
//		/*// 小胡检测
//		BsMjRule.checkXiaoHu(hu, majiangIds, isbegin);*/
//		if (hu.isHu()) {
//			hu.setShowMajiangs(majiangIds);
//		}
//		if (hu.isDahu()) {
//			hu.initDahuList();
//		}
//		return hu;
//	}
	
	
	
	public static BsMjiangHu  isHuBaoShan(List<BsMj> majiangIds,BsMjTable table, BsMjPlayer player ,BsMj huCard,boolean zimo) {
		
		
		BsMjiangHu hu = new BsMjiangHu();
		if (majiangIds == null || majiangIds.isEmpty()) {
			return hu;
		}

		if (isPingHu(majiangIds)) {
			hu.setPingHu(true);
			hu.setHu(true);
			
		}
		// 麻将大胡检测
		BsMjRule.checkDahu(hu, majiangIds, player.getGang(), player.getPeng(),huCard,hu.isHu(),table);
	
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

	public static void checkDahuMax(BsMjiangHu hu,BsMjTable table, BsMjPlayer player ,BsMj huCard,boolean zimo) {
		
		if(!hu.isHu()) {
			return;
		}
		if(table.getBaoting() ==1 && player.getBaoTingS()==1) {
			hu.setBaoting(true);
		}
		if(zimo && !player.isChiPengGang()) {
			hu.setMenQing(true);
		}
		boolean isKa= false;
		for (BsMjPlayer player2 :table.getSeatMap2().values()) {
			if(player2.getSeat() != player.getSeat()) {
				List<BsMj> cards = BsMjQipaiTool.getVal(player2.getPeng(), huCard.getVal());
				if(cards.size() >0) {
					
					
					isKa = true;
					break;
				}
			}
        }
		
		if(isKa&&table.getBuyPoint()>2){
			//hu.setKaJueZhang(true);
		}
		
	}

	public static boolean isPingHu(List<BsMj> majiangIds) {
		return isPingHu(majiangIds, false);

	}

	public static boolean isPingHu(List<BsMj> majiangIds, boolean needJiang258) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}
		if (majiangIds.size() % 3 != 2) {
			return false;

		}

		// 先去掉红中
		List<BsMj> copy = new ArrayList<>(majiangIds);
		//List<BsMj> hongzhongList = dropHongzhong(copy);

		BsMjIndexArr card_index = new BsMjIndexArr();
		BsMjQipaiTool.getMax(card_index, copy);
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
	public static boolean isHuZhuanzhuan(List<BsMj> majiangIds) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}

		List<BsMj> copy = new ArrayList<>(majiangIds);
		// 先去掉红中
		List<BsMj> hongzhongList = dropHongzhong(copy);
		if (hongzhongList.size() == 4) {
			// 4张红中直接胡
			return true;
		}
		if (majiangIds.size() % 3 != 2) {
			System.out.println("%3！=2");
			return false;

		}

		BsMjIndexArr card_index = new BsMjIndexArr();
		BsMjQipaiTool.getMax(card_index, copy);
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
	public static boolean check7duizi(List<BsMj> majiangIds, BsMjIndexArr card_index, int hongzhongNum) {
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

			BsMjIndex index0 = card_index.getMajiangIndex(0);
			BsMjIndex index2 = card_index.getMajiangIndex(2);
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
	public static boolean chaijiang(BsMjIndexArr card_index, List<BsMj> hasPais, int hongzhongnum, boolean needJiang258) {
		Map<Integer, List<BsMj>> jiangMap = card_index.getJiang(needJiang258);
		for (Entry<Integer, List<BsMj>> valEntry : jiangMap.entrySet()) {
			List<BsMj> copy = new ArrayList<>(hasPais);
			BsMjHuLack lack = new BsMjHuLack(hongzhongnum);
			List<BsMj> list = valEntry.getValue();
			int i = 0;
			for (BsMj majiang : list) {
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
			for (BsMj majiang : hasPais) {
				List<BsMj> copy = new ArrayList<>(hasPais);
				BsMjHuLack lack = new BsMjHuLack(hongzhongnum);
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
	public static boolean chaipai(BsMjHuLack lack, List<BsMj> hasPais, boolean isNeedJiang258) {
		if (hasPais.isEmpty()) {
			return true;

		}
		boolean hu = chaishun(lack, hasPais, isNeedJiang258);
		if (hu)
			return true;
		return false;
	}

	public static void sortMin(List<BsMj> hasPais) {
		Collections.sort(hasPais, new Comparator<BsMj>() {

			@Override
			public int compare(BsMj o1, BsMj o2) {
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
	public static boolean chaishun(BsMjHuLack lack, List<BsMj> hasPais, boolean needJiang258) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		BsMj minMajiang = hasPais.get(0);
		int minVal = minMajiang.getVal();
		List<BsMj> minList = BsMjQipaiTool.getVal(hasPais, minVal);
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
		List<BsMj> num1 = BsMjQipaiTool.getVal(hasPais, pai1);
		List<BsMj> num2 = BsMjQipaiTool.getVal(hasPais, pai2);
		List<BsMj> num3 = BsMjQipaiTool.getVal(hasPais, pai3);

		// 找到一句话的麻将
		List<BsMj> hasMajiangList = new ArrayList<>();
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
				List<BsMj> count = BsMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

				List<BsMj> count = BsMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
				List<BsMj> count1 = BsMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				List<BsMj> count2 = BsMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
				List<BsMj> count3 = BsMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
				if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
					List<BsMj> copy = new ArrayList<>(hasPais);
					copy.removeAll(count1);
					BsMjHuLack copyLack = lack.copy();
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

	public static boolean isCanAsJiang(BsMj majiang, boolean isNeed258) {
		if (isNeed258) {
			if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
				return true;
			}
			return false;
		} else {
			return true;
		}

	}

	public static List<BsMj> checkChi(List<BsMj> majiangs, BsMj dismajiang) {
		return new ArrayList<BsMj>();
		//return checkChi(majiangs, dismajiang, null);
	}

	/**
	 * 是否能吃
	 * 
	 * @param majiangs
	 * @param dismajiang
	 * @return
	 */
	public static List<BsMj> checkChi(List<BsMj> majiangs, BsMj dismajiang, List<Integer> wangValList) {
		int disMajiangVal = dismajiang.getVal();
		List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
		List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
		List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

		List<Integer> majiangIds = BsMjHelper.toMajiangVals(majiangs);
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
		return new ArrayList<BsMj>();
	}

	public static List<BsMj> findMajiangByVals(List<BsMj> majiangs, List<Integer> vals) {
		List<BsMj> result = new ArrayList<>();
		for (int val : vals) {
			for (BsMj majiang : majiangs) {
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
	public static List<BsMj> dropHongzhong(List<BsMj> copy) {
		List<BsMj> hongzhong = new ArrayList<>();
		Iterator<BsMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			BsMj majiang = iterator.next();
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
				if (majiang instanceof BsMj) {
					val = ((BsMj) majiang).getVal();
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
	public static List<BsMj> getSameMajiang(List<BsMj> majiangs, BsMj majiang, int num) {
		List<BsMj> hongzhong = new ArrayList<>();
		int i = 0;
		for (BsMj maji : majiangs) {
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
	public static List<BsMj> dropMjId(List<BsMj> copy, int id) {
		List<BsMj> hongzhong = new ArrayList<>();
		Iterator<BsMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			BsMj majiang = iterator.next();
			if (majiang.getId() == id) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static void sortMinPoint(List<BsMj> handPais) {
		Collections.sort(handPais, new Comparator<BsMj>() {

			@Override
			public int compare(BsMj o1, BsMj o2) {
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
		List<BsMj> handPais = getPais(pais);
		System.out.println(toString(handPais));
		List<BsMj> gangList = new ArrayList<>();
		List<BsMj> pengList = new ArrayList<>();
		List<BsMj> chiList = new ArrayList<>();
		List<BsMj> buZhangList = new ArrayList<>();
		boolean isBegin = false;
		List<BsMj> copy = new ArrayList<>(handPais);
//		
//		BsMjiangHu hu = isHuBaoShan(copy, gangList, pengList,isBegin,copy.get(0));
//		StringBuilder sb = new StringBuilder("");
//		sb.append("hu:").append(hu.isHu());
//		sb.append("--xiaoHuList:").append(hu.getXiaohuList());
//		sb.append("--xiaoHu:").append(actListToString(hu.getXiaohuList()));
//		System.out.println(sb.toString());
	}

	public static String toString(List<BsMj> handPais) {
		sortMinPoint(handPais);
		String paiStr = "";
		for (BsMj mj : handPais) {
			paiStr += mj + ",";
		}
		return paiStr;
	}

	public static List<BsMj> getPais(String paisStr){
		String [] pais = paisStr.split(",");
		List<BsMj> handPais = new ArrayList<>();
		for (String pai : pais) {
			for(BsMj mj : BsMj.values()){
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
    public static List<BsMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<BsMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (BsMj mj : BsMj.fullMj) {
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
					if (i == BsMjAction.HU) {
						sb.append("hu");
					} else if (i == BsMjAction.PENG) {
						sb.append("peng");
					} else if (i == BsMjAction.MINGGANG) {
						sb.append("mingGang");
					} else if (i == BsMjAction.ANGANG) {
						sb.append("anGang");
					} else if (i == BsMjAction.CHI) {
						sb.append("chi");
					} else if (i == BsMjAction.BUZHANG) {
						sb.append("buZhang");
					} else if (i == BsMjAction.QUEYISE) {
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
