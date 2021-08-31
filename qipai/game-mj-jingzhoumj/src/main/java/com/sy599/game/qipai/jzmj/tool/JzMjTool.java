package com.sy599.game.qipai.jzmj.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.jzmj.bean.JzMjHuLack;
import com.sy599.game.qipai.jzmj.bean.JzMjTable;
import com.sy599.game.qipai.jzmj.bean.JzMjiangHu;
import com.sy599.game.qipai.jzmj.constant.JzMjAction;
import com.sy599.game.qipai.jzmj.constant.JzMjConstants;
import com.sy599.game.qipai.jzmj.rule.*;
import com.sy599.game.util.JacksonUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author liuping
 * 
 */
public class JzMjTool {


	public static synchronized List<List<JzMj>> fapai(List<Integer> copy, int playerCount) {
		List<List<JzMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<JzMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(JzMj.getMajang(id));
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
	public static synchronized List<List<JzMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
		List<List<JzMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<List<JzMj>> zpList = new ArrayList<>();
		if (GameServerConfig.isDebug() && t != null && !t.isEmpty()){
			for (List<Integer> zp : t){
				zpList.add(JzMjHelper.find(copy, zp));
			}
		}
		List<JzMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(JzMj.getMajang(id));
		}
		int count = 0;
		for (int i = 0; i < playerCount; i++) {
			if (i == 0) {
				if(zpList.size() > 0){
					List<JzMj> pai = zpList.get(0);
					int len = 14-pai.size();
					pai.addAll(allMjs.subList(count, len));
					count += len;
					list.add(new ArrayList<>(pai));
				}else{
					list.add(new ArrayList<>(allMjs.subList(0, 14)));
				}
			} else {
				if(zpList.size() > i){
					List<JzMj> pai = zpList.get(i);
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
					List<JzMj> pai = zpList.get(i+1);
					pai.addAll(allMjs.subList(count, allMjs.size()));
					list.add(new ArrayList<>(pai));
				}else{
					list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
				}
			}

		}
		System.out.println(list);
		return list;
	}


//	/**
//	 * 麻将胡牌
//	 *
//	 * @param majiangIds
//	 * @param playType
//	 * @return
//	 */
//	public static boolean isHu(List<JzMj> majiangIds, int playType) {
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
	 * 长沙麻将胡牌
	 * 
	 * @param majiangIds
	 * @return 0平胡 1 碰碰胡 2将将胡 3清一色 4双豪华7小对 5豪华小对 6:7小对 7全求人 8大四喜 9板板胡 10缺一色 11六六顺
	 */
	public static JzMjiangHu isHuChangsha(List<JzMj> majiangIds, List<JzMj> gang, List<JzMj> peng, List<JzMj> chi, List<JzMj> buzhang, boolean isbegin,boolean jiang258,JzMj huMj) {
		return isChangshaHu2(majiangIds, gang, peng, chi, buzhang, isbegin, jiang258,null,huMj);
	}
	public static JzMjiangHu isChangshaHu2(List<JzMj> majiangIds, List<JzMj> gang, List<JzMj> peng, List<JzMj> chi,
			List<JzMj> buzhang, boolean isbegin, boolean jiang258,JzMjTable table,JzMj huMj) {
		JzMjiangHu hu = new JzMjiangHu();
		if (majiangIds == null || majiangIds.isEmpty()) {
			return hu;
		}

		if (isPingHu(majiangIds,jiang258)) {
			hu.setPingHu(true);
			hu.setHu(true);
		}
		// 长沙麻将大胡检测
		JzMjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);
		// 长沙麻将小胡检测
		JzMjRule.checkXiaoHu2(hu, majiangIds, isbegin,table);
		if (hu.isHu()) {
			hu.setShowMajiangs(majiangIds);
		}
		//全求人吊将
		if(hu.isQuanqiuren()&&table.getQuanqiurJiang()==1&&hu.getDahuCount()==1) {
			if(huMj!=null&&!huMj.isJiang()) {
				hu.setHu(false);
				//hu.setQuanqiuren(false);
			}
		}
		if (hu.isDahu()) {
			hu.initDahuList();
		}

		return hu;
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
	public  static  List<JzMj> getTingMjs(List<JzMj> majiangIds, List<JzMj> gang, List<JzMj> peng, List<JzMj> chi, List<JzMj> buzhang,boolean jiang258,boolean dahu,int rule){
		if (majiangIds == null || majiangIds.isEmpty()) {
			return null;
		}
		List<JzMj> res = new LinkedList<>();
		
		
		for(Integer id :JzMjConstants.fullMj) {
			
			int idx =getOtherId(majiangIds, id);
			if(idx ==0) {
				continue;
			}
			
			JzMjiangHu hu = new JzMjiangHu();
			JzMj mj = JzMj.getMajang(idx);
			majiangIds.add(mj);
			
			if (isPingHu(majiangIds,jiang258)) {
				hu.setPingHu(true);
				hu.setHu(true);
			}
			JzMjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);

			if(hu.isQuanqiuren()&&rule==1&&hu.getDahuCount()==1) {
				if(!mj.isJiang()) {
					hu.setHu(false);
				}
			}
			
			if (hu.isHu()) {
				res.add(mj);
			}
			majiangIds.remove(mj);
		}
		return res;
		
	}
	
	
	public static int getOtherId(List<JzMj> majiangIds,int id) {
		
		List<Integer> list = new ArrayList<>();
		JzMj omj = JzMj.getMajang(id);
		for(Integer idx: JzMjConstants.zhuanzhuan_mjList) {
			JzMj cm =  JzMj.getMajang(idx);
			if(omj.getVal()==cm.getVal()) {
				list.add(idx);
			}
		}
		
		List<Integer> list2 = new ArrayList<>();
		for(JzMj mj: majiangIds) {
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
	

	public static boolean isPingHu(List<JzMj> majiangIds) {
		return isPingHu(majiangIds, true);

	}

	public static boolean isPingHu(List<JzMj> majiangIds, boolean needJiang258) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}
		if (majiangIds.size() % 3 != 2) {
			return false;

		}

		// 先去掉红中
		List<JzMj> copy = new ArrayList<>(majiangIds);
		List<JzMj> hongzhongList = dropHongzhong(copy);

		JzMjIndexArr card_index = new JzMjIndexArr();
		JzMjQipaiTool.getMax(card_index, copy);
		// 拆将
		if (chaijiang(card_index, copy, hongzhongList.size(), needJiang258)) {
			System.out.println("===1胡牌");
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
	public static boolean isHuZhuanzhuan(List<JzMj> majiangIds) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}

		List<JzMj> copy = new ArrayList<>(majiangIds);
		// 先去掉红中
		List<JzMj> hongzhongList = dropHongzhong(copy);
		if (hongzhongList.size() == 4) {
			// 4张红中直接胡
			return true;
		}
		if (majiangIds.size() % 3 != 2) {
			System.out.println("%3！=2");
			return false;

		}

		JzMjIndexArr card_index = new JzMjIndexArr();
		JzMjQipaiTool.getMax(card_index, copy);
		if (check7duizi(copy, card_index, hongzhongList.size())) {
			System.out.println("胡牌7对");
			return true;
		}
		// 拆将
		if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
			System.out.println("==2胡牌");
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
	public static boolean check7duizi(List<JzMj> majiangIds, JzMjIndexArr card_index, int hongzhongNum) {
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

			JzMjIndex index0 = card_index.getMajiangIndex(0);
			JzMjIndex index2 = card_index.getMajiangIndex(2);
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
	public static boolean chaijiang(JzMjIndexArr card_index, List<JzMj> hasPais, int hongzhongnum, boolean needJiang258) {
		Map<Integer, List<JzMj>> jiangMap = card_index.getJiang(needJiang258);
		for (Entry<Integer, List<JzMj>> valEntry : jiangMap.entrySet()) {
			List<JzMj> copy = new ArrayList<>(hasPais);
			JzMjHuLack lack = new JzMjHuLack(hongzhongnum);
			List<JzMj> list = valEntry.getValue();
			int i = 0;
			for (JzMj majiang : list) {
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
			for (JzMj majiang : hasPais) {
				List<JzMj> copy = new ArrayList<>(hasPais);
				JzMjHuLack lack = new JzMjHuLack(hongzhongnum);
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
	public static boolean chaipai(JzMjHuLack lack, List<JzMj> hasPais, boolean isNeedJiang258) {
		if (hasPais.isEmpty()) {
			return true;

		}
		boolean hu = chaishun(lack, hasPais, isNeedJiang258);
		if (hu)
			return true;
		return false;
	}

	public static void sortMin(List<JzMj> hasPais) {
		Collections.sort(hasPais, new Comparator<JzMj>() {

			@Override
			public int compare(JzMj o1, JzMj o2) {
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
	public static boolean chaishun(JzMjHuLack lack, List<JzMj> hasPais, boolean needJiang258) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		JzMj minMajiang = hasPais.get(0);
		int minVal = minMajiang.getVal();
		List<JzMj> minList = JzMjQipaiTool.getVal(hasPais, minVal);
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
		List<JzMj> num1 = JzMjQipaiTool.getVal(hasPais, pai1);
		List<JzMj> num2 = JzMjQipaiTool.getVal(hasPais, pai2);
		List<JzMj> num3 = JzMjQipaiTool.getVal(hasPais, pai3);

		// 找到一句话的麻将
		List<JzMj> hasMajiangList = new ArrayList<>();
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
				List<JzMj> count = JzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

				List<JzMj> count = JzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
				List<JzMj> count1 = JzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				List<JzMj> count2 = JzMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
				List<JzMj> count3 = JzMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
				if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
					List<JzMj> copy = new ArrayList<>(hasPais);
					copy.removeAll(count1);
					JzMjHuLack copyLack = lack.copy();
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

	public static boolean isCanAsJiang(JzMj majiang, boolean isNeed258) {
		if (isNeed258) {
			if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
				return true;
			}
			return false;
		} else {
			return true;
		}

	}

	public static List<JzMj> checkChi(List<JzMj> majiangs, JzMj dismajiang) {
		return checkChi(majiangs, dismajiang, null);
	}

	/**
	 * 是否能吃
	 * 
	 * @param majiangs
	 * @param dismajiang
	 * @return
	 */
	public static List<JzMj> checkChi(List<JzMj> majiangs, JzMj dismajiang, List<Integer> wangValList) {
		int disMajiangVal = dismajiang.getVal();
		List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
		List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
		List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

		List<Integer> majiangIds = JzMjHelper.toMajiangVals(majiangs);
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
		return new ArrayList<JzMj>();
	}

	public static List<JzMj> findMajiangByVals(List<JzMj> majiangs, List<Integer> vals) {
		List<JzMj> result = new ArrayList<>();
		for (int val : vals) {
			for (JzMj majiang : majiangs) {
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
	public static List<JzMj> dropHongzhong(List<JzMj> copy) {
		List<JzMj> hongzhong = new ArrayList<>();
		Iterator<JzMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			JzMj majiang = iterator.next();
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
				if (majiang instanceof JzMj) {
					val = ((JzMj) majiang).getVal();
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
	public static List<JzMj> getSameMajiang(List<JzMj> majiangs, JzMj majiang, int num) {
		List<JzMj> hongzhong = new ArrayList<>();
		int i = 0;
		for (JzMj maji : majiangs) {
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
	public static List<JzMj> dropMjId(List<JzMj> copy, int id) {
		List<JzMj> hongzhong = new ArrayList<>();
		Iterator<JzMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			JzMj majiang = iterator.next();
			if (majiang.getId() == id) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static void sortMinPoint(List<JzMj> handPais) {
		Collections.sort(handPais, new Comparator<JzMj>() {

			@Override
			public int compare(JzMj o1, JzMj o2) {
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
		List<JzMj> handPais = getPais(pais);
		System.out.println(toString(handPais));
		List<JzMj> gangList = new ArrayList<>();
		List<JzMj> pengList = new ArrayList<>();
		List<JzMj> chiList = new ArrayList<>();
		List<JzMj> buZhangList = new ArrayList<>();
		boolean isBegin = false;
		List<JzMj> copy = new ArrayList<>(handPais);
		boolean jiang258 = true;
		JzMjiangHu hu = isHuChangsha(copy, gangList, pengList,chiList,buZhangList, isBegin,jiang258,null);
		StringBuilder sb = new StringBuilder("");
		sb.append("hu:").append(hu.isHu());
		sb.append("--xiaoHuList:").append(hu.getXiaohuList());
		sb.append("--xiaoHu:").append(actListToString(hu.getXiaohuList()));
		System.out.println(sb.toString());
	}

	public static String toString(List<JzMj> handPais) {
		sortMinPoint(handPais);
		String paiStr = "";
		for (JzMj mj : handPais) {
			paiStr += mj + ",";
		}
		return paiStr;
	}

	public static List<JzMj> getPais(String paisStr){
		String [] pais = paisStr.split(",");
		List<JzMj> handPais = new ArrayList<>();
		for (String pai : pais) {
			for(JzMj mj : JzMj.values()){
				if(mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)){
					handPais.add(mj);
					break;
				}
			}
		}
		return handPais;
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
					if (i == JzMjAction.HU) {
						sb.append("hu");
					} else if (i == JzMjAction.PENG) {
						sb.append("peng");
					} else if (i == JzMjAction.MINGGANG) {
						sb.append("mingGang");
					} else if (i == JzMjAction.ANGANG) {
						sb.append("anGang");
					} else if (i == JzMjAction.CHI) {
						sb.append("chi");
					} else if (i == JzMjAction.BUZHANG) {
						sb.append("buZhang");
					} else if (i == JzMjAction.QUEYISE) {
						sb.append("queYiSe");
					}else if (i == JzMjAction.BANBANHU) {
						sb.append("banBanHu");
					}else if (i == JzMjAction.YIZHIHUA) {
						sb.append("yiZhiHua");
					}else if (i == JzMjAction.LIULIUSHUN) {
						sb.append("liuLiuShun");
					}else if (i == JzMjAction.DASIXI) {
						sb.append("daSiXi");
					}else if (i == JzMjAction.JINGTONGYUNU) {
						sb.append("jinTongYuNu");
					}else if (i == JzMjAction.JIEJIEGAO) {
						sb.append("jieJieGao");
					}else if (i == JzMjAction.SANTONG) {
						sb.append("sanTong");
					}else if (i == JzMjAction.ZHONGTUSIXI) {
						sb.append("zhongTuSiXi");
					}else if (i == JzMjAction.ZHONGTULIULIUSHUN) {
						sb.append("zhongTuLiuLiuShun");
					}else if (i == JzMjAction.BAOTING) {
						sb.append("baoting");
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
