package com.sy599.game.qipai.bsmj.bean;

import java.util.Collections;
import java.util.List;

import com.sy599.game.qipai.bsmj.rule.BsMj;
import com.sy599.game.util.DataMapUtil;

public class BsMjiangHu {
	/**
	 * 是否平胡
	 */
	private boolean pingHu;
	//------------------------------大胡----------------------------
	/**
	 * 碰碰胡
	 */
	private boolean pengpengHu;
	/**
	 * 字一色
	 */
	private boolean ziyise;
	/**
	 * 清一色
	 */
	private boolean qingyiseHu;
	/**
	 * 四归一
	 */
	private boolean siguiyi;
	/**
	 * 豪华7小对
	 */
	private boolean longzhuabei;
	/**
	 * 7小对
	 */
	private boolean xiaodui;
	/**
	 * 全求人
	 */
	private boolean quanqiuren;
	/**
	 * 抢杠胡
	 */
	private boolean isQGangHu;
	/**
	 * 杠上炮
	 */
	private boolean isGangShangPao;
	/**
	 * 杠上花
	 */
	private boolean isGangShangHua;
	/**
	 * 门清
	 */
	private boolean menqing;
	/**
	 * 卡绝张
	 */
	private boolean kajuezhang;
	/**
	 * 天胡
	 */
	private boolean tianhu;
	/**
	 * 地胡
	 */
	private boolean dihu;
	
	
	
	/**
	 * 十老头
	 */
	private boolean shilaotou;
	
	/**
	 * 双杠上花
	 */
	private boolean shuangGSH;
	
	/**
	 * 报听
	 */
	private boolean baoting;
	

	/**
	 * 一条龙
	 */
	private boolean yitiaolong;
	
	/**
	 * 是否胡
	 */
	private boolean isHu;
	/**
	 * 是否小胡
	 */
	private boolean isXiaohu;
	/**
	 * 是否大胡
	 */
	private boolean isDahu;
	
	
	/**
	 * 双杠上花
	 */
	private boolean sanGSH;
	/**
	 * 小胡列表
	 */
	private List<Integer> xiaohuList;
	/**
	 * 大胡列表：0碰碰胡 1将将胡 2清一色 3海底月 4海底炮 5:7小对 6 豪华7小对 7杠上花 8抢杠胡9 杠上炮 10全求人 11双豪华7小对
	 */
	private List<Integer> dahuList;
	/**
	 *
	 */
	private List<BsMj> showMajiangs;
	/**
	 * 胡的分数
	 */
	private int dahuFan;

	/**
	 * @return 0：碰碰胡
	 * 		   1：字一色
	 * 		   2：清一色
	 * 		   3：门清
	 * 		   4：卡绝张
	 * 		   5：7小对
	 * 		   6：豪华7小对（龙爪背）
	 * 		   7：杠上花
	 * 		   8：抢杠胡
	 * 		   9：杠上炮
	 * 		   10：全求人
	 *         11：四归一
	 *         12：天胡
	 *         13：地胡
	 *         14：十老头
	 *         15：双杠上花
	 *         16：报听
	 *         17：一条龙
	 *         18：三杠上花
	 */
	public List<Integer> buildDahuList() {
		int[] arr = new int[19];
		if (pengpengHu) {
			arr[0] = 1;
		}
		if (ziyise) {
			arr[1] = 1;
		}
		if (qingyiseHu) {
			arr[2] = 1;
		}
		if (menqing) {
			arr[3] = 1;
		}
		if (kajuezhang) {
			arr[4] = 1;
		}
		if (xiaodui) {
			arr[5] = 1;
		}
		if (longzhuabei) {
			arr[6] = 1;
		}
		if (isGangShangHua) {
			arr[7] = 1;
		}
		if (isQGangHu) {
			arr[8] = 1;
		}
		if (isGangShangPao) {
			arr[9] = 1;
		}
		if (quanqiuren) {
			arr[10] = 1;
		}
		if (siguiyi) {
			arr[11] = 1;
		}
		if (tianhu) {
			arr[12] = 1;
		}
		if (dihu) {
			arr[13] = 1;
		}
		if (shilaotou) {
			arr[14] = 1;
		}
		if (shuangGSH) {
			arr[15] = 1;
		}
		if (baoting) {
			arr[16] = 1;
		}
		if (yitiaolong) {
			arr[17] = 1;
		}
		if(sanGSH) {
			arr[18] = 1;
		}
		

		List<Integer> dahu = DataMapUtil.indexToValList(arr);
		if (!dahu.isEmpty()) {
			return dahu;
		}
		return Collections.EMPTY_LIST;
	}

	public int getDahuPointByList() {
		int point = 0;
		
		
		return point;
	}

	public boolean isPingHu() {
		return pingHu;
	}

	public void setPingHu(boolean pingHu) {
		this.pingHu = pingHu;
	}

	public boolean isPengpengHu() {
		return pengpengHu;
	}

	public void setPengpengHu(boolean pengpengHu) {
		this.pengpengHu = pengpengHu;
		addFan(1);
	}

	public boolean isZiyiseHu() {
		return ziyise;
	}

	public void setZiYiSe(boolean jiangjiangHu) {
		this.ziyise = jiangjiangHu;
		addFan(1);
	}
	
	

	public boolean isQingyiseHu() {
		return qingyiseHu;
	}

	public void setQingyiseHu(boolean qingyiseHu) {
		this.qingyiseHu = qingyiseHu;
		addFan(1);
	}

	public boolean isShiGuiYiHu() {
		return siguiyi;
	}

	public void setShiGuiYi(boolean siguiyi) {
		this.siguiyi = siguiyi;
		if(!siguiyi){
			addFan(-2);
		}else{
			addFan(2);
		}
	}

	public boolean isLongZhuaBeiHu() {
		return longzhuabei;
	}

	public void setLongZhuaBeiHu(boolean longzhuabei) {
		this.longzhuabei = longzhuabei;
		addFan(2);
	}
	
	public void addFan(int point){
		dahuFan +=point;
		if(dahuFan<0){
			dahuFan = 0;
		}
	}

	public boolean is7Xiaodui() {
		return xiaodui;
	}

	public void set7Xiaodui(boolean xiaodui) {
		this.xiaodui = xiaodui;
		addFan(1);
	}

	public boolean isQuanqiuren() {
		return quanqiuren;
	}

	public void setQuanqiuren(boolean quanqiuren) {
		this.quanqiuren = quanqiuren;
		addFan(1);
	}

	


	public boolean isHu() {
		return isHu;
	}

	public void setHu(boolean isHu) {
		this.isHu = isHu;
	}

	public boolean isXiaohu() {
		return isXiaohu;
	}

	public void setXiaohu(boolean isXiaohu) {
		this.isXiaohu = isXiaohu;
	}

	public boolean isDahu() {
		return isDahu;
	}

	public void setDahu(boolean isDahu) {
		this.isDahu = isDahu;
	}

	public List<Integer> getXiaohuList() {
		return xiaohuList;
	}

	public void setXiaohuList(List<Integer> xiaohuList) {
		this.xiaohuList = xiaohuList;
	}

	public List<Integer> getDahuList() {
		return dahuList;
	}

	public void setDahuList(List<Integer> dahuList) {
		this.dahuList = dahuList;
	}

	public void initDahuList() {
		this.dahuList = buildDahuList();
		if (!this.dahuList.isEmpty()) {
			isDahu = true;
		}
	}

	public void setShowMajiangs(List<BsMj> showMajiangs) {
		this.showMajiangs = showMajiangs;
	}

	public List<BsMj> getShowMajiangs() {
		return showMajiangs;
	}

	public int getDahuFan() {
		return dahuFan;
	}

	public void setDahuFan(int dahuFan) {
		this.dahuFan = dahuFan;
	}

	public void addToDahu(List<Integer> dahuList) {
		if (this.dahuList == null) {
			this.initDahuList();
		}
		for (int dahu : dahuList) {
			if (dahu == 7 || dahu == 9) {
				// 杠上花和杠上炮可以算两次
				this.dahuList.add(dahu);
				continue;
			}
			if (!this.dahuList.contains(dahu)) {
				this.dahuList.add(dahu);
			}

		}

		//setDahuPoint(getDahuPointByList());
	}

	public boolean isQGangHu() {
		return isQGangHu;
	}

	public void setQGangHu(boolean isQGangHu) {
		this.isQGangHu = isQGangHu;
		//dahuPoint += 6;
	}

	public boolean isGangShangPao() {
		return isGangShangPao;
	}

	public void setGangShangPao(boolean isGangShangPao) {
		this.isGangShangPao = isGangShangPao;
		addFan(1);
	}

	public void setMenQing(boolean haidilaoyue) {
		this.menqing = haidilaoyue;
		addFan(1);
	}

	public void setKaJueZhang(boolean haidipao) {
		this.kajuezhang = haidipao;
		addFan(1);
	}

	public boolean isGangShangHua() {
		return isGangShangHua;
	}

	public void setGangShangHua(boolean isGangShangHua) {
		this.isGangShangHua = isGangShangHua;
		addFan(1);
	}
	
	
	public void setShilaotou(boolean shilaotou) {
		this.shilaotou = shilaotou;
		addFan(1);
	}
	
	
	

	public void setYitiaolong(boolean yitiaolong) {
		this.yitiaolong = yitiaolong;
		addFan(1);
	}


	public void setShuangGSH(boolean shuangGSH) {
		this.shuangGSH = shuangGSH;
		addFan(2);
	}
	
	public void setSanGSH(boolean sanGSH) {
		this.sanGSH = sanGSH;
		addFan(3);
	}

	public void setBaoting(boolean baoting) {
		this.baoting = baoting;
		addFan(1);
	}



	public boolean isTianhu() {
		return tianhu;
	}

	public void setTianhu(boolean tianhu) {
		this.tianhu = tianhu;
//		dahuPoint += 6;
	}

	public boolean isDihu() {
		return dihu;
	}

	public void setDihu(boolean dihu) {
		this.dihu = dihu;
//		dahuPoint += 6;
	}

	public static int getDaHuPointCount(List<Integer> daHuList){
		if(daHuList == null || daHuList.size()==0){
			return 0;
		}
		int count = 0;
//		for (int dahu : daHuList) {
//			if (dahu == 11) {
//				count += 3;
//			} else if (dahu == 6) {
//				count += 2;
//			} else if(dahu != 12 && dahu != 13){
//				//天胡地胡不算大胡20180803
//				count += 1;
//			}
//		}
		return 0;
	}

}
