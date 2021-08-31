package com.sy599.game.qipai.ahmj.rule;

import com.sy599.game.qipai.ahmj.bean.AhmjHu;
import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.tool.AhMajiangTool;
import com.sy599.game.qipai.ahmj.tool.AhmjTool;
import com.sy599.game.qipai.ahmj.tool.QipaiTool;

import java.util.ArrayList;
import java.util.List;

public class AhmjRule {
	/**
	 * 算王能有几个大胡 三个一样的王 1个大胡 有两种 三个一个的王 2个大胡 7个王算3个大胡
	 * 
	 * @param hu
	 * @return 3个大胡
	 */
	public static int checkWangDaHu(AhmjHu hu) {
		if (hu.getWangMajiangList() == null) {
			return 0;
		}
		if (hu.getWangNum() == 7) {
			hu.setWangDahuNum(3);
			hu.setWangType(7);// 7王
			return 3;
		}

		MajiangIndexArr all_card_index = new MajiangIndexArr();
		QipaiTool.getMax(all_card_index, hu.getWangMajiangList());
		int num = 0;
		// 三张牌
		MajiangIndex index3 = all_card_index.getMajiangIndex(2);
		if (index3 != null) {
			num += index3.getLength();
			if (index3.getLength() == 2) {
				hu.setWangType(6);// 6王
			} else if (index3.getLength() == 1) {
				hu.setWangType(3);// 3王
			}
		}

		// 四张牌
		MajiangIndex index4 = all_card_index.getMajiangIndex(3);
		if (index4 != null) {
			num += index4.getLength() * 2;
			hu.setWangType(4);// 4王
		}
		hu.setWangDahuNum(num);
		return num;
	}

	/**
	 * 2清一色5:7小对
	 * 
	 * @param majiangIds
	 * @param gang
	 * @param peng
	 * @param chi
	 * @param buzhang
	 * @return
	 */
	public static int[] checkDahu(AhmjHu hu, List<Ahmj> majiangIds, List<Ahmj> gang, List<Ahmj> peng, List<Ahmj> chi, List<Ahmj> buzhang) {
		List<Ahmj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		allMajiangs.addAll(chi);
		allMajiangs.addAll(buzhang);
		int arr[] = new int[7];
		if (majiangIds.size() % 3 != 2) {
			return arr;
		}

//		MajiangIndexArr all_card_index = new MajiangIndexArr();
//		MajiangTool.getMax(all_card_index, allMajiangs);


		// 3清一色
		if (isqingyiseHu(allMajiangs, majiangIds, hu)) {
			arr[2] = 1;
			hu.setQingyiseHu(true);
			hu.setHu(true);
		}
		
		MajiangIndexArr card_index = new MajiangIndexArr();
		QipaiTool.getMax(card_index, hu.getWithoutWangMajiangs());
		// 4 7小对
		if (AhmjTool.check7duizi(hu.getWithoutWangMajiangs(), card_index, hu.getWangNum())) {
			// 是否有豪华7小对
			// 普通7小对
			arr[5] = 1;
			hu.set7Xiaodui(true);
			hu.setHu(true);
		}
		
		return arr;
	}

	/**
	 * 是否胡清一色
	 * 
	 * @param allMajiangs
	 * @param majiangIds
	 * @param huBean
	 * @return
	 */
	private static boolean isqingyiseHu(List<Ahmj> allMajiangs, List<Ahmj> majiangIds, AhmjHu huBean) {
		boolean qingyise = false;
		int se = 0;
		for (Ahmj mjiang : allMajiangs) {
			if (huBean.getWangValList() != null && huBean.getWangValList().contains(mjiang.getVal())) {
				continue;
			}
			if (se == 0) {
				qingyise = true;
				se = mjiang.getHuase();
				continue;
			}

			if (mjiang.getHuase() != se) {
				if (huBean.getWangValList() != null && huBean.getWangValList().contains(mjiang.getVal())) {
					continue;
				}
				qingyise = false;
				break;
			}
		}
		if (qingyise) {
			return AhMajiangTool.isHuWangMajiang(majiangIds, huBean, false);
		}

		return false;
	}
}
