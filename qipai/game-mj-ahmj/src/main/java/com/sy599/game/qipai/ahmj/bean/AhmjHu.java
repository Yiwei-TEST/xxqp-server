package com.sy599.game.qipai.ahmj.bean;

import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.util.DataMapUtil;

import java.util.Collections;
import java.util.List;

public class AhmjHu {
	private List<Integer> wangValList;
	private List<Ahmj> wangMajiangList;
	private int wangDahuNum;
	/*** 3(111) 4(1111) 6(111222) 7(1112222) */
	private int wangType;
	/*** 起手抓到王胡牌 */
	private boolean isStartHu;
	// 0平胡 1 碰碰胡 2将将胡 3清一色 4双豪华7小对 5豪华小对 6:7小对 7全求人 8大四喜 9板板胡 10缺一色 11六六顺
	private boolean pingHu;
	private boolean qingyiseHu;
	private boolean xiaodui;
	private boolean isHu;
	private boolean isDahu;
	private boolean isQGangHu;
	private boolean isGangShangPao;
	private boolean isGangShangHua;
	private boolean zimo;
	/**
	 * 0:硬庄 1:清一色 2:小对 3:杠上花 4:抢杠胡 5:杠上炮 6:3王 7:4王 8:6王 9:7王
	 */
	private List<Integer> dahuList;
	private List<Ahmj> showMajiangs;
	private List<Ahmj> withoutWangMajiangs;
	private int dahuPoint;
	// 硬庄王
	private boolean isYzWang;

	/**
	 * @return 0硬庄 1清一色 2小对 3杠上花 4抢杠胡 5:杠上炮 63王 74王 86王 97王
	 */
	public List<Integer> buildDahuList() {
		int[] arr = new int[12];
		if (getWangNum() == 0) {
			// 硬庄
			arr[0] = 1;
		} else if (isYzWang) {
			arr[0] = 1;
		}
		if (qingyiseHu) {
			arr[1] = 1;
		}

		if (xiaodui) {
			arr[2] = 1;
		}

		if (isGangShangHua) {
			arr[3] = 1;
		}
		if (isQGangHu) {
			arr[4] = 1;
		}
		if (isGangShangPao) {
			arr[5] = 1;
		}
		if (!isStartHu) {
			if (wangType == 3) {
				arr[6] = 1;
			}
			if (wangType == 4) {
				arr[7] = 1;
			}
			if (wangType == 6) {
				arr[8] = 1;
			}
			if (wangType == 7) {
				arr[9] = 1;
			}
		}

		List<Integer> dahu = DataMapUtil.indexToValList(arr);
		if (!dahu.isEmpty()) {
			return dahu;
		}
		return Collections.EMPTY_LIST;
	}

	public int initDahuPoint(int maxPlayerCount) {
		int point = 0;
		if (!isStartHu) {// 起手胡 是乱胡 不会存在大胡
			// 不是起手抓王才能胡的牌 算上王的数量分
			point += wangDahuNum;
		}

		point += 1;
		if (dahuList != null) {
			for (int dahu : dahuList) {
				// 6789代表王
				if (dahu >= 6 && dahu <= 9) {
					continue;
				}

				point += 1;
			}
			// point += dahuList.size();
		}
		// 大胡数量
		if (point != 0) {
			point = (int) Math.pow(2, point);
			setDahuPoint(point);
		}
		return point;
	}

	public boolean isPingHu() {
		return pingHu;
	}

	public void setPingHu(boolean pingHu) {
		this.pingHu = pingHu;
	}

	public boolean isQingyiseHu() {
		return qingyiseHu;
	}

	public void setQingyiseHu(boolean qingyiseHu) {
		this.qingyiseHu = qingyiseHu;
	}

	public boolean is7Xiaodui() {
		return xiaodui;
	}

	public void set7Xiaodui(boolean xiaodui) {
		this.xiaodui = xiaodui;
	}

	public boolean isHu() {
		return isHu;
	}

	public void setHu(boolean isHu) {
		this.isHu = isHu;
	}

	public boolean isDahu() {
		return isDahu;
	}

	public void setDahu(boolean isDahu) {
		this.isDahu = isDahu;
	}

	public List<Integer> getDahuList() {
		return dahuList;
	}

	public void setDahuList(List<Integer> dahuList) {
		this.dahuList = dahuList;
	}

	/**
	 * 比较有没有清一色和7小对
	 * 
	 * @param dahuList
	 * @return
	 */
	public boolean compare(List<Integer> dahuList) {
		for (int dahu : this.dahuList) {
			if (dahu > 0 && dahu < 3) {
				if (!dahuList.contains(dahu)) {
					return false;
				}
			}
		}
		return true;
	}

	public void initDahuList(int maxPlayerCount) {
		if (this.dahuList == null || dahuList.isEmpty()) {
			List<Integer> build = buildDahuList();
			this.dahuList = build;
			if (!this.dahuList.isEmpty()) {
				isDahu = true;
			}
		}

		initDahuPoint(maxPlayerCount);

	}

	public void calcDahuList(int maxPlayerCount) {
		List<Integer> build = buildDahuList();
		this.dahuList = build;
		if (!this.dahuList.isEmpty()) {
			isDahu = true;
		}

		initDahuPoint(maxPlayerCount);

	}

	public void setShowMajiangs(List<Ahmj> showMajiangs) {
		this.showMajiangs = showMajiangs;
	}

	public List<Ahmj> getShowMajiangs() {
		return showMajiangs;
	}

	public int getDahuPoint() {
		return dahuPoint;
	}

	public void setDahuPoint(int dahuPoint) {
		this.dahuPoint = dahuPoint;
	}

	public void addToDahu(List<Integer> dahuList, int maxPlayerCount) {
		if (this.dahuList == null) {
			this.initDahuList(maxPlayerCount);
		}
		for (int dahu : dahuList) {
			if (dahu == 3 || dahu == 5) {
				// 杠上花和杠上炮可以算两次
				this.dahuList.add(dahu);
				continue;
			}
			if (!this.dahuList.contains(dahu)) {
				this.dahuList.add(dahu);
			}

		}

		initDahuPoint(maxPlayerCount);
	}

	public boolean isQGangHu() {
		return isQGangHu;
	}

	public void setQGangHu(boolean isQGangHu) {
		this.isQGangHu = isQGangHu;
	}

	public boolean isGangShangPao() {
		return isGangShangPao;
	}

	public void setGangShangPao(boolean isGangShangPao) {
		this.isGangShangPao = isGangShangPao;
	}

	public boolean isGangShangHua() {
		return isGangShangHua;
	}

	public void setGangShangHua(boolean isGangShangHua) {
		this.isGangShangHua = isGangShangHua;
	}

	public List<Integer> getWangValList() {
		return wangValList;
	}

	public void setWangValList(List<Integer> wangValList) {
		this.wangValList = wangValList;
	}

	public List<Ahmj> getWangMajiangList() {
		return wangMajiangList;
	}

	public void setWangMajiangList(List<Ahmj> wangMajiangList) {
		this.wangMajiangList = wangMajiangList;
	}

	public int getWangNum() {
		if (wangMajiangList == null) {
			return 0;
		}
		return wangMajiangList.size();
	}

	public int getWangDahuNum() {
		return wangDahuNum;
	}

	public void setWangDahuNum(int wangDahuNum) {
		this.wangDahuNum = wangDahuNum;
	}

	public boolean isStartHu() {
		return isStartHu;
	}

	public void setStartHu(boolean isStartHu) {
		this.isStartHu = isStartHu;
	}

	public boolean isZimo() {
		return zimo;
	}

	public void setZimo(boolean zimo) {
		this.zimo = zimo;
	}

	public List<Ahmj> getWithoutWangMajiangs() {
		return withoutWangMajiangs;
	}

	public void setWithoutWangMajiangs(List<Ahmj> withoutWangMajiangs) {
		this.withoutWangMajiangs = withoutWangMajiangs;
	}

	public int getWangType() {
		return wangType;
	}

	public void setWangType(int wangType) {
		this.wangType = wangType;
	}

	public boolean isYzWang() {
		return isYzWang;
	}

	public void setYzWang(boolean isYzWang) {
		this.isYzWang = isYzWang;
	}
}
