package com.sy599.game.qipai.jzmj.bean;

import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuping
 * 长沙麻将胡记录
 */
public class JzMjHuRecord {
	/**
	 * 胡的麻将
	 */
	private int majiangId;
	/**
	 * 胡牌人的座位号
	 */
	private int seat;
	/**
	 * 放炮人的座位号 (0)表示自摸
	 */
	private int paoSeat;
	/**
	 * 胡的分数
	 */
	private int score;
	/**
	 * 第几个胡牌的
	 */
	private int huTimes;
	/**
	 * 大胡列表
	 */
	private List<Integer> dahus = new ArrayList<>();
	
	/**
	 * 要扣分的座位号(自摸时 因为已经胡过的人不再扣分了)
	 */
	private List<Integer> subScoreSeat = new ArrayList<>();
	
	public boolean isZiMo() {
		return (paoSeat == 0);
	}
	
	public int getMajiangId() {
		return majiangId;
	}
	
	public void setMajiangId(int majiangId) {
		this.majiangId = majiangId;
	}
	
	public int getSeat() {
		return seat;
	}
	
	public void setSeat(int seat) {
		this.seat = seat;
	}
	
	public int getPaoSeat() {
		return paoSeat;
	}
	
	public void setPaoSeat(int paoSeat) {
		this.paoSeat = paoSeat;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getHuTimes() {
		return huTimes;
	}
	
	public void setHuTimes(int huTimes) {
		this.huTimes = huTimes;
	}

	public List<Integer> getDahus() {
		return dahus;
	}
	
	public void setDahus(List<Integer> dahus) {
		this.dahus = dahus;
	}

	public List<Integer> getSubScoreSeat() {
		return subScoreSeat;
	}
	
	public void setSubScoreSeat(List<Integer> subScoreSeat) {
		this.subScoreSeat = subScoreSeat;
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
        sb.append(seat).append(",");
        sb.append(paoSeat).append(",");
        sb.append(majiangId).append(",");
        sb.append(score).append(",");
        sb.append(huTimes).append(",");
		sb.append(StringUtil.implode(dahus, "_")).append(",");
		sb.append(StringUtil.implode(subScoreSeat, "_")).append(",");
        return sb.toString();
    }
	
	/**
     * 初始化扩展信息
     *
     * @param info
     */
    public void initExtend(String info) {
    	if (!StringUtils.isBlank(info)) {
            int i = 0;
            String[] values = info.split(",");
            this.seat = StringUtil.getIntValue(values, i++);
            this.paoSeat = StringUtil.getIntValue(values, i++);
            this.majiangId = StringUtil.getIntValue(values, i++);
            this.score = StringUtil.getIntValue(values, i++);
            this.huTimes = StringUtil.getIntValue(values, i++);
        	String dahusStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(dahusStr)) {
				this.dahus = StringUtil.explodeToIntList(dahusStr, "_");
			}
			String subScoreSeatStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(subScoreSeatStr)) {
				this.subScoreSeat = StringUtil.explodeToIntList(subScoreSeatStr, "_");
			}
        }
    }
}
