package com.sy599.game.qipai.xx2710.bean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaohuziCheckCardBean {
	private int seat;
	// 碰
	private boolean peng;
	// 吃
	private boolean chi;
	// 胡
	private boolean hu;

	private List<Integer> actionList;

	public List<Integer> getActionList() {
		return actionList;
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


	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	/**
	 * 0胡，1碰，2栽，3提，4吃，5跑，6臭栽
	 * @return
	 */
	public List<Integer> buildActionList() {
		int[] arr = new int[7];
		// 0胡，1碰，2栽，3提，4吃，5跑，6臭栽
		if (hu) {
			arr[0] = 1;
		}
		if (peng) {
			arr[1] = 1;
		}
		if (chi) {
			arr[4] = 1;
		}
		List<Integer> list = new ArrayList<>();
		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			actionList = list;
		} else {
			actionList = Collections.EMPTY_LIST;
		}
		return actionList;
	}

	public String toString(){
		StringBuilder s=new StringBuilder();
		if (peng)
			s.append("碰,");
		if (chi)
			s.append("吃,");
		if (hu)
			s.append("胡,");
		return s.toString();
	}


	public static String actionListToString(List<Integer> actionList) {
		if (actionList == null || actionList.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < actionList.size(); i++) {
			if (actionList.get(i) == 1) {
				switch (i) {
					case 0:
						sb.append("hu").append(",");
						break;
					case 1:
						sb.append("peng").append(",");
						break;
					case 2:
						sb.append("zai").append(",");
						break;
					case 3:
						sb.append("ti").append(",");
						break;
					case 4:
						sb.append("chi").append(",");
						break;
					case 5:
						sb.append("pao").append(",");
						break;
					case 6:
						sb.append("chouZai").append(",");
						break;
					default:
						sb.append("未知").append(i).append(",");
				}
			}

		}
		sb.append("]");
		return sb.toString();
	}
}
