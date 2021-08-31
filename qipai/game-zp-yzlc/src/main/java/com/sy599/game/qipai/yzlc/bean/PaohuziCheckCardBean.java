package com.sy599.game.qipai.yzlc.bean;

import com.sy599.game.qipai.yzlc.constant.PaohzCard;
import com.sy599.game.qipai.yzlc.tool.PaohuziHuLack;
import com.sy599.game.qipai.yzlc.tool.PaohuziTool;
import com.sy599.game.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class PaohuziCheckCardBean {
	private int seat;
	// 胡
	private boolean hu;
	//上一次被动操作
	private int autoAction;
	//上一次被动出牌, 碰吃等等操作组成的卡组
	private List<PaohzCard> autoDisList;
	//
	private PaohzCard disCard;
	//操作列表
	private List<Integer> actionList;
	//
	private boolean isMoPaiIng;
	//
	private PaohuziHuLack lack;
	//过掉胡
	private boolean isPassHu;
	/** 单牌 **/
	public boolean action_single_card;
	/** 对牌 **/
	public boolean action_double_card;
	/** 大面 **/
	public boolean action_big_face;
	/** pass **/
	public boolean action_pass;
	/** 小面 **/
	public boolean action_small_face;
	/** 食盒 **/
	public boolean action_eat_box ;
	/** 坎 **/
	public boolean action_kan;
	/** 龙 **/
	public boolean action_dragon;
	/** 顺子 **/
	public boolean action_straight;

	// 2单牌 3对牌 4大面 6小面 7食盒 8坎 10龙 11顺子
	public void setDisActionToAction(int disAction) {
		switch (disAction){
			case 2:
				setAction_single_card(true);
				break;
			case 3:
				setAction_double_card(true);
				break;
			case 4:
				setAction_big_face(true);
				break;
			case 6:
				setAction_small_face(true);
				break;
			case 7:
				setAction_eat_box(true);
				break;
			case 8:
				setAction_kan(true);
				break;
			case 10:
				setAction_dragon(true);
				break;
			case 11:
				setAction_straight(true);
				break;

		}
	}

	public void setAuto(int autoAction, List<PaohzCard> autoDisList) {
		if (this.autoAction == 0) {
			this.autoAction = autoAction;
			this.autoDisList = autoDisList;
		}
	}

	public boolean isHu() {
		return hu;
	}

	public void setHu(boolean hu) {
		this.hu = hu;
	}

	public PaohzCard getDisCard() {
		return disCard;
	}

	public void setDisCard(PaohzCard disCard) {
		this.disCard = disCard;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	/**
	 * // 0胡,1单牌,2对子,3大面,4小面,5食盒,6坎,7龙,8顺子
	 * @return
	 */
	public List<Integer> buildActionList() {
		int[] arr = new int[10];
		// 0胡,1单牌,2对子,3大面,4小面,5食盒,6坎,7龙,8顺子
		if (hu) {
			arr[0] = 1;
		}
		if (action_single_card) {
			arr[1] = 1;
		}
		if (action_double_card) {
			arr[2] = 1;
		}
		if (action_big_face) {
			arr[3] = 1;
		}
		if (action_small_face) {
			arr[4] = 1;
		}
		if (action_eat_box) {
			arr[5] = 1;
		}
		if (action_kan) {
			arr[6] = 1;
		}
		if (action_dragon) {
			arr[7] = 1;
		}
		if (action_straight) {
			arr[8] = 1;
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

	public void setAutoAction(int autoAction) {
		this.autoAction = autoAction;
	}

	public int getAutoAction() {
		return autoAction;
	}

	public boolean isMoPaiIng() {
		return isMoPaiIng;
	}

	public void setMoPaiIng(boolean isMoPaiIng) {
		this.isMoPaiIng = isMoPaiIng;
	}

	public void initAutoDisData(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(";");
			seat = StringUtil.getIntValue(values, i++);
			autoAction = StringUtil.getIntValue(values, i++);
			String autoDisStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(autoDisStr)) {
				this.autoDisList = PaohuziTool.explodePhz(autoDisStr, ",");
			}
			this.isMoPaiIng = StringUtil.getIntValue(values, i++) == 1;
			this.isPassHu = StringUtil.getIntValue(values, i++) == 1;
		}
	}

	public String buildAutoDisStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(seat).append(";");
		sb.append(autoAction).append(";");
		sb.append(PaohuziTool.implodePhz(autoDisList, ",")).append(";");
		sb.append(isMoPaiIng ? 1 : 0).append(";");
		sb.append(isPassHu ? 1 : 0).append(";");
		return sb.toString();
	}

	public PaohuziHuLack getLack() {
		return lack;
	}

	public void setLack(PaohuziHuLack lack) {
		this.lack = lack;
	}

	public boolean isPassHu() {
		return isPassHu;
	}

	public void setPassHu(boolean isPassHu) {
		this.isPassHu = isPassHu;
	}

    public static String actionListToString(List<Integer> actionList) {
        if (actionList == null || actionList.size() == 0) {
            return "";
        }
		//0胡,1单牌,2对子,3大面,4小面,5食盒,6坎,7龙,8顺子
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < actionList.size(); i++) {
            if (actionList.get(i) == 1) {
                switch (i) {
                    case 0:
                        sb.append("hu").append(",");
                        break;
                    case 1:
                        sb.append("DanPai").append(",");
                        break;
                    case 2:
                        sb.append("DuiZi").append(",");
                        break;
                    case 3:
                        sb.append("DaMian").append(",");
                        break;
                    case 4:
                        sb.append("XiaoMian").append(",");
                        break;
                    case 5:
                        sb.append("ShiHe").append(",");
                        break;
                    case 6:
                        sb.append("Kan").append(",");
                        break;
					case 7:
                        sb.append("Long").append(",");
                        break;
					case 8:
                        sb.append("ShunZi").append(",");
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
