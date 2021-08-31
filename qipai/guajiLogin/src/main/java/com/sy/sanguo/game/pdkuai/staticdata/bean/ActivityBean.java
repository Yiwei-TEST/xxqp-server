package com.sy.sanguo.game.pdkuai.staticdata.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.MathUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.msg.ActivityMsg;
import com.sy.sanguo.game.pdkuai.staticdata.KeyValuePair;
import com.sy.sanguo.game.pdkuai.util.DataMapUtil;

public class ActivityBean {
	private ActivityCsvInfo csvInfo;
	private int luckMinBureau;
	private float luckRate;
	private int fudaiNeedConsumeCard;
	private KeyValuePair<Float, Float> xnMoneyRates;
	private Map<Integer, Long> luckWeights;
	// 数量
	private int luckTotalMoney;
	private Map<Integer, Long> fudaiWeights;
	private Map<Integer, Integer> awardMap;
	private String showContent;

	public void load(ActivityCsvInfo info) {
		csvInfo = info;
		if (!StringUtils.isBlank(info.getGoodLuckHb())) {
			// 10_0.01_18,30,30;68,10,10;88,5,5;118,5,5
			String[] values = info.getGoodLuckHb().split("_");
			luckMinBureau = StringUtil.getIntValue(values, 0);
			luckRate = StringUtil.getFloatValue(values, 1);

			// 金额,权重,数量
			luckWeights = new HashMap<>();
			// 18,30,30;68,10,10;88,5,5;118,5,5
			String luck = StringUtil.getValue(values, 2);
			String[] luckValues = luck.split(";");
			for (String lvalue : luckValues) {
				// 18,30,30
				String[] detailValues = lvalue.split(",");
				int luckMoney = StringUtil.getIntValue(detailValues, 0);
				long luckWeight = StringUtil.getLongValue(detailValues, 1);
				luckWeights.put(luckMoney, luckWeight);
			}
			luckTotalMoney = StringUtil.getIntValue(values, 3);

		}

		if (!StringUtils.isBlank(info.getNewYearHb())) {
			// 0.4_0.25
			xnMoneyRates = new KeyValuePair<>();
			String[] values = info.getNewYearHb().split("_");
			xnMoneyRates.setId(Float.parseFloat(values[0]));
			xnMoneyRates.setValue(Float.parseFloat(values[1]));
		}
		if (!StringUtils.isBlank(info.getFuDai())) {
			// 1_5_1,30;2,20;3,10
			String[] values = info.getGoodLuckHb().split("_");
			fudaiNeedConsumeCard = StringUtil.getIntValue(values, 1);
			fudaiWeights = DataMapUtil.implodeT(info.getNewYearHb(), Integer.class, Long.class, ",");
		}

		if (!StringUtils.isBlank(info.getRankingList())) {
			awardMap = DataMapUtil.implode(info.getRankingList());
		}

		if (!StringUtils.isBlank(info.getShowContent())) {
			showContent = info.getShowContent();
		}

	}

	public ActivityMsg buildActivityMsg() {
		ActivityMsg msg = new ActivityMsg();
		msg.setId(csvInfo.getType());
		msg.setStartTime(getStartTime());
		msg.setEndTime(getEndTime());
		return msg;
	}

	/**
	 * 如果抽中了新年红包 返回人均金额
	 * 
	 * @return
	 */
	public float shakeXnHbMoney() {
		if (xnMoneyRates == null) {
			return 0;
		}

		boolean isShake = MathUtil.shake(xnMoneyRates.getId());
		if (isShake) {
			return xnMoneyRates.getValue();

		} else {
			return 0;
		}
	}

	public String getShowContent() {
		return showContent;
	}

	/**
	 * 最低触发幸运红包局数
	 * 
	 * @return
	 */
	public int getLuckMinBureau() {
		return luckMinBureau;
	}

	/**
	 * 兑换现金最低额度
	 * 
	 * @return
	 */
	public int getExchangeMinMoney() {
		return csvInfo.getMinExchange();
	}

	/**
	 * 活动开始时间
	 * 
	 * @return
	 */
	public long getStartTime() {
		return csvInfo.getStartTime().getTime();
	}

	/**
	 * 活动开始时间
	 * 
	 * @return
	 */
	public Date getStartDateTime() {
		return csvInfo.getStartTime();
	}

	/**
	 * 活动结束时间
	 * 
	 * @return
	 */
	public long getEndTime() {
		return csvInfo.getEndTime().getTime();
	}

	/**
	 * 活动结束时间
	 * 
	 * @return
	 */
	public Date getEndDateTime() {
		return csvInfo.getEndTime();
	}

	/**
	 * 类型
	 * 
	 * @return
	 */
	public int getType() {
		return csvInfo.getType();
	}

	/**
	 * 
	 * 新用户每累积消耗房卡的数量老用户可获得福袋
	 * 
	 * @return
	 */
	public int getFudaiNeedConsumeCard() {
		return fudaiNeedConsumeCard;
	}

	/**
	 * 奖励
	 * 
	 * @param rank
	 * @return
	 */
	public int getAward(int rank) {
		if (awardMap == null) {
			return 0;
		}
		for (Entry<Integer, Integer> entry : awardMap.entrySet()) {
			if (rank > 0 && rank <= entry.getKey()) {
				return entry.getValue();
			}
		}
		return 0;
	}
}
