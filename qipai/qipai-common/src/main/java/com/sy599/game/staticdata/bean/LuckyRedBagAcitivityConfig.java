/**
 * 
 */
package com.sy599.game.staticdata.bean;

import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;

import java.security.SecureRandom;
import java.util.*;

/**
 * @author liuping
 * 转盘抽奖活动配置
 */
public class LuckyRedBagAcitivityConfig extends ActivityConfigInfo {


	/**
	 * 活动截止日期
	 */
	private Date endDate;

	private int maxGrade;  //最大等级

	private int playBureau;  //需要游戏大局数
	/**
	 * 转盘档次
	 */
	private Map<Integer, float[]> grades;

	private int tatalRatio;  //总比率 1000


	@Override
	public void configParamsAndRewards() {
		try {
			endDate = new Date(TimeUtil.parseTimeInMillis(params));
			playBureau = 5;
			String[] strs = rewards.split(";");
			grades = new HashMap<>();//new float[strs.length][5];
			maxGrade = 0;
			for (String str : strs) {
				String[] temps = str.split("_");
				if (temps.length == 4) {
					float[] arr = new float[5];
					arr[0] = Float.parseFloat(temps[0]);// 奖励档次ID
					arr[1] = Float.parseFloat(temps[1]);// 奖励类型  1钻石 2现金红包
					arr[2] = Float.parseFloat(temps[2]);// 金额
					float ratio = Float.parseFloat(temps[3]); // 概率
					arr[3] = ratio;
					tatalRatio += ratio;
					arr[4] = tatalRatio;
					int grade = (int) arr[0];
					grades.put(grade, arr);
					if (grade > maxGrade)
						maxGrade = grade;
				}
			}
		} catch (Exception e) {
			LogUtil.errorLog.info("开房送钻活动配置异常:", e);
		}
	}

	public int randomGrade(List<Integer> filterGrade) {
		Random random = new SecureRandom();
		int value = random.nextInt(tatalRatio) + 1;
		for (int grade = 1; grade <= maxGrade; grade++) {
			float[] curGrades = grades.get(grade);  //获取随机等级对应的 描述
			if(filterGrade.contains(new Integer(grade)))
				continue;
			if(curGrades[3] == 0)// 概率为0的过滤掉
				continue;
			if (value <= curGrades[4] && grade != 6) {  //总比率
				return grade;
			}
		}
		return 1;
	}

	public int randomGrade() {
		Random random = new SecureRandom();
		int value = random.nextInt(tatalRatio) + 1;
		for (int grade = 1; grade <= maxGrade; grade++) {
			float[] curGrades = grades.get(grade);
			if(curGrades[3] == 0)// 概率为0的过滤掉
				continue;
			if (value <= curGrades[4]) {
				return grade;
			}
		}
		return 1;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getMaxGrade() {
		return maxGrade;
	}

	public void setMaxGrade(int maxGrade) {
		this.maxGrade = maxGrade;
	}

	public Map<Integer, float[]> getGrades() {
		return grades;
	}

	public void setGrades(Map<Integer, float[]> grades) {
		this.grades = grades;
	}

	public int getTatalRatio() {
		return tatalRatio;
	}

	public void setTatalRatio(int tatalRatio) {
		this.tatalRatio = tatalRatio;
	}

	public int getPlayBureau() {
		return playBureau;
	}

	public void setPlayBureau(int playBureau) {
		this.playBureau = playBureau;
	}
}
