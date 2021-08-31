package com.sy599.game.activity;

import com.alibaba.fastjson.TypeReference;
import com.sy599.game.common.action.BaseAction;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.staticdata.bean.ActivityCsvInfo;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ActivityAction extends BaseAction {

	@Override
	public void execute() throws Exception {
		switch (this.getInt("funcType")) {
		case 1:
			loadActivityData();
			// receiveFuDai(info);
			break;
		case 2:
			// receiveNewYearHb(info);
			break;
		case 3:
			// receiveGoodLuckHb(info);
			break;
		case 4:
			// test();
			break;
		case 5:
			// test();
			break;
		default:
			break;
		}

	}

	private void loadActivityData() {
		String info = this.getString("info");
		if (StringUtils.isBlank(info)) {
			return;
		}
		List<ActivityCsvInfo> activityCsvList = JacksonUtil.readValue(info, new TypeReference<List<ActivityCsvInfo>>() {
		});
		StaticDataManager.setActivityCsvList(activityCsvList);
		LogUtil.monitor_i("loadActivityData size-->" + StaticDataManager.activityCsvList.size());

	}

	// private void receiveFuDai(String info) {
	// List<FuDai> fudaiList = JacksonUtil.readValue(info, new
	// TypeReference<List<FuDai>>() {});
	// System.out.println(fudaiList);
	// }
	//
	// private void receiveNewYearHb(String info) {
	// List<NewYearHb> newYearHbList = JacksonUtil.readValue(info, new
	// TypeReference<List<NewYearHb>>() {});
	// System.out.println(newYearHbList);
	// }
	//
	// private void receiveGoodLuckHb(String info) {
	// List<GoodLuckHb> goodLuckHbList = JacksonUtil.readValue(info, new
	// TypeReference<List<GoodLuckHb>>() {});
	// System.out.println(goodLuckHbList);
	// }

}
