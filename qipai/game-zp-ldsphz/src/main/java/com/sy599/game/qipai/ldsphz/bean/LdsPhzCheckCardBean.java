package com.sy599.game.qipai.ldsphz.bean;

import com.sy599.game.qipai.ldsphz.util.LdsPhzCardResult;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LdsPhzCheckCardBean {
	private int seat;
	// 提
	private boolean ti;
	// 碰
	private boolean peng;
	// 吃
	private boolean chi;
	// 胡
	private boolean hu;
	// 栽
	private boolean zai;
	// 栽
	private boolean chouZai;
	// 跑
	private boolean pao;
	// 王钓
	private boolean wangDiao;
	// 王闯
	private boolean wangChuang;

	//王炸
	private boolean wangZha;

	private int autoAction;
	private Integer disCard;
	private List<Integer> actionList;
	private List<Integer> autoDisList;
	private boolean isMoPaiIng;
	private LdsPhzCardResult cardResult;

	public LdsPhzCardResult getCardResult() {
		return cardResult;
	}

	public void setCardResult(LdsPhzCardResult cardResult) {
		this.cardResult = cardResult;
	}

	//过掉胡
	private boolean isPassHu;

	public boolean isWangZha() {
		return wangZha;
	}

	public void setWangZha(boolean wangZha) {
		this.wangZha = wangZha;
	}

	public List<Integer> getActionList() {
		if(actionList == null ||actionList.size()==0) {
			 buildActionList();
		}
		return actionList;
	}

	public void setActionList(List<Integer> actionList) {
		this.actionList = actionList;
	}

	public List<Integer> getAutoDisList() {
		return autoDisList;
	}

	public void setAuto(int autoAction, List<Integer> autoDisList) {
		if (this.autoAction == 0) {
			this.autoAction = autoAction;
			this.autoDisList = autoDisList;
		}

	}

	public void forceSetAuto(int autoAction, List<Integer> autoDisList) {
			this.autoAction = autoAction;
			this.autoDisList = autoDisList;
	}

	public boolean isTi() {
		return ti;
	}

	public void setTi(boolean ti) {
		this.ti = ti;
	}

	public boolean isPeng() {
		return peng;
	}

	public void setPeng(boolean peng) {
		this.peng = peng;
	}

	public boolean isChi() {
		return chi;
	}

	public void setChi(boolean chi) {
		this.chi = chi;
	}

	public boolean isHu() {
		return hu;
	}

	public void setHu(boolean hu) {
		this.hu = hu;
	}

	public boolean isZai() {
		return zai;
	}

	public void setZai(boolean zai) {
		this.zai = zai;
	}

	public boolean isPao() {
		return pao;
	}

	public void setPao(boolean pao) {
		this.pao = pao;
	}

	public Integer getDisCard() {
		return disCard;
	}

	public void setDisCard(Integer disCard) {
		this.disCard = disCard;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}


	public static List<Integer> uniqueHasActionList(int idx){
		int[] arr = new int[13];
		if (idx>=0&&idx<=12){
			arr[idx]=1;
		}
		List<Integer> list = new ArrayList<>(arr.length);
		for (int val : arr) {
			list.add(val);
		}
		return list;
	}

	public static List<Integer> hasActionList(int... idxs){
		int[] arr = new int[13];

		for(int idx:idxs){
			if (idx>=0&&idx<=12){
				arr[idx]=1;
			}
		}

		List<Integer> list = new ArrayList<>(arr.length);
		for (int val : arr) {
			list.add(val);
		}
		return list;
	}

	/**
	 * 0胡，1碰，2栽，3提，4吃，5跑，6臭栽
	 * @return
	 */
	public List<Integer> buildActionList() {
	    int count=0;
		int[] arr = new int[13];
		// 0胡，1碰，2栽，3提，4吃，5跑，6臭栽，7王钓，8王闯，9王炸，10王炸王，11王闯王，12王钓王
		if (hu) {
			arr[0] = 1;
            count++;
		}
		if (peng) {
			arr[1] = 1;
            count++;
		}
		if (zai) {
			arr[2] = 1;
            count++;
		}
		if (ti) {
			arr[3] = 1;
            count++;
		}
		if (chi) {
			arr[4] = 1;
            count++;
		}
		if (pao) {
			arr[5] = 1;
            count++;
		}
		if (chouZai) {
			arr[6] = 1;
            count++;
		}
		if (wangDiao) {
			arr[7] = 1;
            count++;
		}
		if (wangChuang) {
			arr[8] = 1;
            count++;
		}
		if (wangZha) {
			arr[9] = 1;
            count++;
		}
		List<Integer> list = new ArrayList<>(arr.length);
		for (int val : arr) {
			list.add(val);
		}
		if (count>0) {
			actionList = list;
		} else {
			actionList = Collections.EMPTY_LIST;
		}
		return actionList;
	}

	public void setAutoAction(int autoAction) {
		this.autoAction = autoAction;
	}

	public int getAutoAction() {
		return autoAction;
	}

	public void initAutoDisData(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(";");
			seat = StringUtil.getIntValue(values, i++);
			autoAction = StringUtil.getIntValue(values, i++);
			String autoDisStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(autoDisStr)) {
				this.autoDisList = str2IntList(autoDisStr);
			}
			this.isMoPaiIng = StringUtil.getIntValue(values, i++) == 1;
			this.isPassHu = StringUtil.getIntValue(values, i++) == 1;
		}
	}

	List<Integer> str2IntList(String str){
		List<Integer> list=new ArrayList<>();
		String[] strs=str.split(",");
		for (String temp:strs){
			if (StringUtils.isNotBlank(temp)){
				list.add(Integer.valueOf(temp));
			}
		}
		return list;
	}

	String intList2Str(List<Integer> list){
		StringBuilder stringBuilder=new StringBuilder();
		for (Integer temp:list){
			stringBuilder.append(",").append(temp);
		}
		return stringBuilder.length()>0?stringBuilder.substring(1):"";
	}

	public String buildAutoDisStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(seat).append(";");
		sb.append(autoAction).append(";");
		sb.append(intList2Str(autoDisList)).append(";");
		sb.append(isMoPaiIng ? 1 : 0).append(";");
		sb.append(isPassHu ? 1 : 0).append(";");
		return sb.toString();
	}

	public boolean isChouZai() {
		return chouZai;
	}

	public void setChouZai(boolean chouZai) {
		this.chouZai = chouZai;
	}
	
	public boolean isPassHu() {
		return isPassHu;
	}

	public void setPassHu(boolean isPassHu) {
		this.isPassHu = isPassHu;
	}

	public boolean isWangDiao() {
		return wangDiao;
	}

	public void setWangDiao(boolean wangDiao) {
		this.wangDiao = wangDiao;
	}

	public boolean isWangChuang() {
		return wangChuang;
	}

	public void setWangChuang(boolean wangChuang) {
		this.wangChuang = wangChuang;
	}


}
