package com.sy599.game.qipai.nxmj.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.nxmj.bean.NxMjHuLack;
import com.sy599.game.qipai.nxmj.bean.NxMjTable;
import com.sy599.game.qipai.nxmj.bean.NxMjiangHu;
import com.sy599.game.qipai.nxmj.constant.NxMjAction;
import com.sy599.game.qipai.nxmj.constant.NxMjConstants;
import com.sy599.game.qipai.nxmj.rule.NxMj;
import com.sy599.game.qipai.nxmj.rule.NxMjHelper;
import com.sy599.game.qipai.nxmj.rule.NxMjIndex;
import com.sy599.game.qipai.nxmj.rule.NxMjIndexArr;
import com.sy599.game.qipai.nxmj.rule.NxMjRule;
import com.sy599.game.util.JacksonUtil;

/**
 * @author liuping
 * 
 */
public class NxMjTool {


	public static synchronized List<List<NxMj>> fapai(List<Integer> copy, int playerCount) {
		List<List<NxMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<NxMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(NxMj.getMajang(id));
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
	public static synchronized List<List<NxMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
		List<List<NxMj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<List<NxMj>> zpList = new ArrayList<>();
		if (GameServerConfig.isDebug() && t != null && !t.isEmpty()){
			for (List<Integer> zp : t){
				zpList.add(NxMjHelper.find(copy, zp));
			}
		}
		List<NxMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(NxMj.getMajang(id));
		}
		int count = 0;
		for (int i = 0; i < playerCount; i++) {
			if (i == 0) {
				if(zpList.size() > 0){
					List<NxMj> pai = zpList.get(0);
					int len = 14-pai.size();
					pai.addAll(allMjs.subList(count, len));
					count += len;
					list.add(new ArrayList<>(pai));
				}else{
					list.add(new ArrayList<>(allMjs.subList(0, 14)));
				}
			} else {
				if(zpList.size() > i){
					List<NxMj> pai = zpList.get(i);
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
					List<NxMj> pai = zpList.get(i+1);
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
//	 * ????????????
//	 *
//	 * @param majiangIds
//	 * @param playType
//	 * @return
//	 */
//	public static boolean isHu(List<CsMj> majiangIds, int playType) {
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
	 * ??????????????????
	 * 
	 * @param majiangIds
	 * @return 0?????? 1 ????????? 2????????? 3????????? 4?????????7?????? 5???????????? 6:7?????? 7????????? 8????????? 9????????? 10????????? 11?????????
	 */
	public static NxMjiangHu isHuChangsha(List<NxMj> majiangIds, List<NxMj> gang, List<NxMj> peng, List<NxMj> chi, List<NxMj> buzhang, boolean isbegin,boolean jiang258,NxMj huMj) {
		return isChangshaHu2(majiangIds, gang, peng, chi, buzhang, isbegin, jiang258,null,huMj);
	}
	public static NxMjiangHu isChangshaHu2(List<NxMj> majiangIds, List<NxMj> gang, List<NxMj> peng, List<NxMj> chi,
			List<NxMj> buzhang, boolean isbegin, boolean jiang258,NxMjTable table,NxMj huMj) {
		NxMjiangHu hu = new NxMjiangHu();
		if (majiangIds == null || majiangIds.isEmpty()) {
			return hu;
		}

		if (isPingHu(majiangIds,jiang258)) {
			hu.setPingHu(true);
			hu.setHu(true);
		}
		// ????????????????????????
		NxMjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);
		// ????????????????????????
		NxMjRule.checkXiaoHu2(hu, majiangIds, isbegin,table);
		if (hu.isHu()) {
			hu.setShowMajiangs(majiangIds);
		}
		
		if(hu.isQuanqiuren()&&table.getQuanqiurJiang()==1&&hu.getDahuCount()==1) {
			if(huMj!=null&&!huMj.isJiang()) {
				hu.setHu(false);
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
	public  static  List<NxMj> getTingMjs(List<NxMj> majiangIds, List<NxMj> gang, List<NxMj> peng, List<NxMj> chi, List<NxMj> buzhang,boolean jiang258,boolean dahu,int rule){
		if (majiangIds == null || majiangIds.isEmpty()) {
			return null;
		}
		List<NxMj> res = new LinkedList<>();
		
		
		for(Integer id :NxMjConstants.fullMj) {
			
			int idx =getOtherId(majiangIds, id);
			if(idx ==0) {
				continue;
			}
			
			NxMjiangHu hu = new NxMjiangHu();
			NxMj mj = NxMj.getMajang(idx);
			majiangIds.add(mj);
			
			if (isPingHu(majiangIds,jiang258)) {
				hu.setPingHu(true);
				hu.setHu(true);
			}
			NxMjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);

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
	
	
	public static int getOtherId(List<NxMj> majiangIds,int id) {
		
		List<Integer> list = new ArrayList<>();
		NxMj omj = NxMj.getMajang(id);
		for(Integer idx: NxMjConstants.zhuanzhuan_mjList) {
			NxMj cm =  NxMj.getMajang(idx);
			if(omj.getVal()==cm.getVal()) {
				list.add(idx);
			}
		}
		
		List<Integer> list2 = new ArrayList<>();
		for(NxMj mj: majiangIds) {
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
	

	public static boolean isPingHu(List<NxMj> majiangIds) {
		return isPingHu(majiangIds, true);

	}

	public static boolean isPingHu(List<NxMj> majiangIds, boolean needJiang258) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}
		if (majiangIds.size() % 3 != 2) {
			return false;

		}

		// ???????????????
		List<NxMj> copy = new ArrayList<>(majiangIds);
		List<NxMj> hongzhongList = dropHongzhong(copy);

		NxMjIndexArr card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(card_index, copy);
		// ??????
		if (chaijiang(card_index, copy, hongzhongList.size(), needJiang258)) {
			System.out.println("??????");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static boolean isHuZhuanzhuan(List<NxMj> majiangIds) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}

		List<NxMj> copy = new ArrayList<>(majiangIds);
		// ???????????????
		List<NxMj> hongzhongList = dropHongzhong(copy);
		if (hongzhongList.size() == 4) {
			// 4??????????????????
			return true;
		}
		if (majiangIds.size() % 3 != 2) {
			System.out.println("%3???=2");
			return false;

		}

		NxMjIndexArr card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(card_index, copy);
		if (check7duizi(copy, card_index, hongzhongList.size())) {
			System.out.println("??????7???");
			return true;
		}
		// ??????
		if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
			System.out.println("??????");
			return true;
		} else {
			return false;
		}

	}

	/**
	 * ??????????????????7??????????????????????????????
	 * 
	 * @param majiangIds
	 * @param card_index
	 */
	public static boolean check7duizi(List<NxMj> majiangIds, NxMjIndexArr card_index, int hongzhongNum) {
		if (majiangIds.size() == 14) {
			// 7??????
			int duizi = card_index.getDuiziNum();
			if (duizi == 7) {
				return true;
			}

		} else if (majiangIds.size() + hongzhongNum == 14) {
			if (hongzhongNum == 0) {
				return false;
			}

			NxMjIndex index0 = card_index.getMajiangIndex(0);
			NxMjIndex index2 = card_index.getMajiangIndex(2);
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

	// ??????
	public static boolean chaijiang(NxMjIndexArr card_index, List<NxMj> hasPais, int hongzhongnum, boolean needJiang258) {
		Map<Integer, List<NxMj>> jiangMap = card_index.getJiang(needJiang258);
		for (Entry<Integer, List<NxMj>> valEntry : jiangMap.entrySet()) {
			List<NxMj> copy = new ArrayList<>(hasPais);
			NxMjHuLack lack = new NxMjHuLack(hongzhongnum);
			List<NxMj> list = valEntry.getValue();
			int i = 0;
			for (NxMj majiang : list) {
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
			// ???????????????
			if (hasPais.isEmpty()) {
				return true;
			}
			// ?????????
			for (NxMj majiang : hasPais) {
				List<NxMj> copy = new ArrayList<>(hasPais);
				NxMjHuLack lack = new NxMjHuLack(hongzhongnum);
				boolean isJiang = false;
				if (!needJiang258) {
					// ????????????
					isJiang = true;

				} else {
					// ??????258??????
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
						// ????????????
						System.out.println(JacksonUtil.writeValueAsString(lack));
						return true;
					}
				}
			}
		}

		return false;
	}

	// ??????
	public static boolean chaipai(NxMjHuLack lack, List<NxMj> hasPais, boolean isNeedJiang258) {
		if (hasPais.isEmpty()) {
			return true;

		}
		boolean hu = chaishun(lack, hasPais, isNeedJiang258);
		if (hu)
			return true;
		return false;
	}

	public static void sortMin(List<NxMj> hasPais) {
		Collections.sort(hasPais, new Comparator<NxMj>() {

			@Override
			public int compare(NxMj o1, NxMj o2) {
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
	 * ??????
	 * 
	 * @param hasPais
	 * @return
	 */
	public static boolean chaishun(NxMjHuLack lack, List<NxMj> hasPais, boolean needJiang258) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		NxMj minMajiang = hasPais.get(0);
		int minVal = minMajiang.getVal();
		List<NxMj> minList = NxMjQipaiTool.getVal(hasPais, minVal);
		if (minList.size() >= 3) {
			// ????????????
			hasPais.removeAll(minList.subList(0, 3));
			return chaipai(lack, hasPais, needJiang258);
		}

		// ?????????
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
		List<NxMj> num1 = NxMjQipaiTool.getVal(hasPais, pai1);
		List<NxMj> num2 = NxMjQipaiTool.getVal(hasPais, pai2);
		List<NxMj> num3 = NxMjQipaiTool.getVal(hasPais, pai3);

		// ????????????????????????
		List<NxMj> hasMajiangList = new ArrayList<>();
		if (!num1.isEmpty()) {
			hasMajiangList.add(num1.get(0));
		}
		if (!num2.isEmpty()) {
			hasMajiangList.add(num2.get(0));
		}
		if (!num3.isEmpty()) {
			hasMajiangList.add(num3.get(0));
		}

		// ????????????????????????
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

			// ?????????????????????2????????????????????????????????????
			if (lackNum >= 2) {
				// ?????????
				List<NxMj> count = NxMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				if (count.size() >= 3) {
					hasPais.removeAll(count);
					return chaipai(lack, hasPais, needJiang258);

				} else if (count.size() == 2) {
					if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
						// ???????????????
						lack.setHasJiang(true);
						hasPais.removeAll(count);
						return chaipai(lack, hasPais, needJiang258);
					}

					// ????????????????????????
					lack.changeHongzhong(-1);
					lack.addLack(count.get(0).getVal());
					hasPais.removeAll(count);
					return chaipai(lack, hasPais, needJiang258);
				}

				// ??????
				if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
					lack.changeHongzhong(-1);
					lack.setHasJiang(true);
					hasPais.removeAll(count);
					lack.addLack(count.get(0).getVal());
					return chaipai(lack, hasPais, needJiang258);
				}
			} else if (lackNum == 1) {
				// ??????
				if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
					lack.changeHongzhong(-1);
					lack.setHasJiang(true);
					hasPais.remove(minMajiang);
					lack.addLack(minMajiang.getVal());
					return chaipai(lack, hasPais, needJiang258);
				}

				List<NxMj> count = NxMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				if (count.size() == 2 && lack.getHongzhongNum() > 0) {
					lack.changeHongzhong(-1);
					lack.addLack(count.get(0).getVal());
					hasPais.removeAll(count);
					return chaipai(lack, hasPais, needJiang258);
				}
			}

			// ????????????????????????
			if (lack.getHongzhongNum() >= lackNum) {
				lack.changeHongzhong(-lackNum);
				hasPais.removeAll(hasMajiangList);
				lack.addAllLack(lackList);

			} else {
				return false;
			}
		} else {
			// ???????????????
			if (lack.getHongzhongNum() > 0) {
				List<NxMj> count1 = NxMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				List<NxMj> count2 = NxMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
				List<NxMj> count3 = NxMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
				if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
					List<NxMj> copy = new ArrayList<>(hasPais);
					copy.removeAll(count1);
					NxMjHuLack copyLack = lack.copy();
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

	public static boolean isCanAsJiang(NxMj majiang, boolean isNeed258) {
		if (isNeed258) {
			if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
				return true;
			}
			return false;
		} else {
			return true;
		}

	}

	public static List<NxMj> checkChi(List<NxMj> majiangs, NxMj dismajiang) {
		return checkChi(majiangs, dismajiang, null);
	}

	/**
	 * ????????????
	 * 
	 * @param majiangs
	 * @param dismajiang
	 * @return
	 */
	public static List<NxMj> checkChi(List<NxMj> majiangs, NxMj dismajiang, List<Integer> wangValList) {
		int disMajiangVal = dismajiang.getVal();
		List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
		List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
		List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

		List<Integer> majiangIds = NxMjHelper.toMajiangVals(majiangs);
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
		return new ArrayList<NxMj>();
	}

	public static List<NxMj> findMajiangByVals(List<NxMj> majiangs, List<Integer> vals) {
		List<NxMj> result = new ArrayList<>();
		for (int val : vals) {
			for (NxMj majiang : majiangs) {
				if (majiang.getVal() == val) {
					result.add(majiang);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * ????????????
	 * 
	 * @param copy
	 * @return
	 */
	public static List<NxMj> dropHongzhong(List<NxMj> copy) {
		List<NxMj> hongzhong = new ArrayList<>();
		Iterator<NxMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			NxMj majiang = iterator.next();
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
				if (majiang instanceof NxMj) {
					val = ((NxMj) majiang).getVal();
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
	 * ???????????????
	 * 
	 * @param majiangs
	 *            ?????????
	 * @param majiang
	 *            ??????
	 * @param num
	 *            ???????????????
	 * @return
	 */
	public static List<NxMj> getSameMajiang(List<NxMj> majiangs, NxMj majiang, int num) {
		List<NxMj> hongzhong = new ArrayList<>();
		int i = 0;
		for (NxMj maji : majiangs) {
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
	 * ???????????????
	 * 
	 * @param copy
	 * @return
	 */
	public static List<NxMj> dropMjId(List<NxMj> copy, int id) {
		List<NxMj> hongzhong = new ArrayList<>();
		Iterator<NxMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			NxMj majiang = iterator.next();
			if (majiang.getId() == id) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static void sortMinPoint(List<NxMj> handPais) {
		Collections.sort(handPais, new Comparator<NxMj>() {

			@Override
			public int compare(NxMj o1, NxMj o2) {
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
		List<NxMj> handPais = getPais(pais);
		System.out.println(toString(handPais));
		List<NxMj> gangList = new ArrayList<>();
		List<NxMj> pengList = new ArrayList<>();
		List<NxMj> chiList = new ArrayList<>();
		List<NxMj> buZhangList = new ArrayList<>();
		boolean isBegin = false;
		List<NxMj> copy = new ArrayList<>(handPais);
		boolean jiang258 = true;
		NxMjiangHu hu = isHuChangsha(copy, gangList, pengList,chiList,buZhangList, isBegin,jiang258,null);
		StringBuilder sb = new StringBuilder("");
		sb.append("hu:").append(hu.isHu());
		sb.append("--xiaoHuList:").append(hu.getXiaohuList());
		sb.append("--xiaoHu:").append(actListToString(hu.getXiaohuList()));
		System.out.println(sb.toString());
	}

	public static String toString(List<NxMj> handPais) {
		sortMinPoint(handPais);
		String paiStr = "";
		for (NxMj mj : handPais) {
			paiStr += mj + ",";
		}
		return paiStr;
	}

	public static List<NxMj> getPais(String paisStr){
		String [] pais = paisStr.split(",");
		List<NxMj> handPais = new ArrayList<>();
		for (String pai : pais) {
			for(NxMj mj : NxMj.values()){
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
					if (i == NxMjAction.HU) {
						sb.append("hu");
					} else if (i == NxMjAction.PENG) {
						sb.append("peng");
					} else if (i == NxMjAction.MINGGANG) {
						sb.append("mingGang");
					} else if (i == NxMjAction.ANGANG) {
						sb.append("anGang");
					} else if (i == NxMjAction.CHI) {
						sb.append("chi");
					} else if (i == NxMjAction.BUZHANG) {
						sb.append("buZhang");
					} else if (i == NxMjAction.QUEYISE) {
						sb.append("queYiSe");
					}else if (i == NxMjAction.BANBANHU) {
						sb.append("banBanHu");
					}else if (i == NxMjAction.YIZHIHUA) {
						sb.append("yiZhiHua");
					}else if (i == NxMjAction.LIULIUSHUN) {
						sb.append("liuLiuShun");
					}else if (i == NxMjAction.DASIXI) {
						sb.append("daSiXi");
					}else if (i == NxMjAction.JINGTONGYUNU) {
						sb.append("jinTongYuNu");
					}else if (i == NxMjAction.JIEJIEGAO) {
						sb.append("jieJieGao");
					}else if (i == NxMjAction.SANTONG) {
						sb.append("sanTong");
					}else if (i == NxMjAction.ZHONGTUSIXI) {
						sb.append("zhongTuSiXi");
					}else if (i == NxMjAction.ZHONGTULIULIUSHUN) {
						sb.append("zhongTuLiuLiuShun");
					}else if (i == NxMjAction.BAOTING) {
						sb.append("baoting");
					}else if (i == NxMjAction.YIDIANHONG) {
						sb.append("yidianhong");
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
			//??????????????????
			for (int i = 0, size = leftMjCount; i < size; i++) {
				int nowIndex = i + startIndex;
				if (!moTailPai.contains(nowIndex)) {
					moTailPai.add(nowIndex);
					break;
				}
			}

		} else {
			int duo = gangDice / 10 + gangDice % 10;
			//???????????????????????????
			for (int i = 0, j = 0; i < leftMjCount; i++) {
				int nowIndex = i + startIndex;
				if (nowIndex % 2 == 1) {
					j++; //???????????????
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
