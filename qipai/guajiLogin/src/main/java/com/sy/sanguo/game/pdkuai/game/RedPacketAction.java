package com.sy.sanguo.game.pdkuai.game;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.pdkuai.constants.ActivityConstant;
import com.sy.sanguo.game.pdkuai.db.bean.RedPacketMoneyInfo;
import com.sy.sanguo.game.pdkuai.db.bean.RedPacketRecord;
import com.sy.sanguo.game.pdkuai.db.dao.RedPacketDao;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.ActivityBean;

/**
 * 活动红包
 * 
 * @author Ysy
 * 
 */
public class RedPacketAction extends BaseAction {

	@Override
	public String execute() throws Exception {
		switch (this.getInt("funcType")) {
		case 1:
			getMeony();
			break;
		case 2:
			exchangeRedPacket();
			break;
		case 3:
			ReceiveRecord();
			break;
		case 4:
			hbRanking();
			break;
		case 5:
			HbRankReward();
			break;
		case 6:
			getActivityCsvData();
			break;
		default:
			break;
		}
		return result;
	}

	private void getActivityCsvData() {
		Map<String, Object> result = new HashMap<String, Object>();
		
		if (StaticDataManager.getActivityList().size() > 0) {
			result.put("info", StaticDataManager.getActivityList());
			this.writeMsg(0, result);
		} else {
			result.put("info", "error");
			this.writeMsg(-1, result);
		}
		
	}

	/**
	 * 获得红包已兑换，未兑换金额
	 */
	public void getMeony() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		DecimalFormat  format=new DecimalFormat("######0.00");
		
		ActivityBean activity = StaticDataManager.getSingleActivityBaseInfo(ActivityConstant.activity_6);
		// 判断当前时间是否为活动可领取奖励时间
		if (activity == null) {
			result.put("msg", "不在活动时间内");
			this.writeMsg(-1, result);
			return;
		}

		Long userId = this.getLong("userId", 0);// 用户ID
		if (userId == 0) {
			result.put("msg", "用户ID错误");
			this.writeMsg(-1, result);
			return;
		}

//		String endTime = TimeUtil.formatTime(activity.getEndDateTime());

		double notMoney = 0.0;// 未兑换金额
		double endMoney = 0.0;// 已兑换金额
		RedPacketMoneyInfo redPacketMoneyInfo = RedPacketDao.getInstance().userMoeny(userId);// 用户红包金额信息
		// 红包金额表有当前用户的记录时候返回真实金额
		if (redPacketMoneyInfo != null) {
			endMoney = redPacketMoneyInfo.getShengMoney();// 获得已兑换金额
			notMoney = redPacketMoneyInfo.getTotalMoney() - endMoney;
		}

		result.put("endTime", activity.getShowContent());
		result.put("notMoney", format.format(notMoney));
		result.put("endMoney", format.format(endMoney));
		this.writeMsg(0, result);
	}

	/**
	 * 兑换红包，返回未兑换金额
	 */
	public void exchangeRedPacket() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		ActivityBean activity = StaticDataManager.getActivityBean(ActivityConstant.activity_6);
		// 判断当前时间是否为活动可领取奖励时间
		if (activity == null) {
			result.put("msg", "未到活动时间");
			this.writeMsg(-1, result);
			return;
		}
		
		Long userId = this.getLong("userId", 0);// 用户ID
		double money = this.getDouble("money", 0.0);// 需兑换金额
		String phone = this.getString("phone");// 手机号码
		String wxname = this.getString("wxname");// 用户微信号
		double notMoney = 0;// 未兑换金额
		double endMoney = 0;// 已兑换金额
		if (userId == 0) {
			result.put("msg", "用户ID错误");
			this.writeMsg(-1, result);
			return;
		}

		if (phone == null || phone.length() != 11) {
			result.put("msg", "手机号码错误");
			this.writeMsg(-1, result);
			return;
		}

		if (wxname == null || wxname.length() < 1) {
			result.put("msg", "请输入正确的微信号");
			this.writeMsg(-1, result);
			return;
		}

		// 判断红包对应的数值赋值红包金额
		if (money > 0 && money < activity.getExchangeMinMoney()) {
			result.put("msg", "没达到兑换金额");
			this.writeMsg(-1, result);
			return;
		}

		
		RedPacketMoneyInfo redPacketMoneyInfo = RedPacketDao.getInstance().userMoeny(userId);// 用户红包金额信息
		
		if (redPacketMoneyInfo == null) {
			result.put("msg", "兑换金额不够");
			this.writeMsg(-1, result);
			return;
		}
		
		if(redPacketMoneyInfo.getTotalMoney() < redPacketMoneyInfo.getShengMoney()){
			result.put("msg", "兑换金额大于总金额");
			this.writeMsg(-1, result);
			return;
		}
		
		// 根据用户红包金额信息判断用户是否有可兑换金额
		notMoney=redPacketMoneyInfo.getTotalMoney()-redPacketMoneyInfo.getShengMoney();
		if (notMoney >= money) {
			RedPacketDao.getInstance().updateExchange(userId, money);// 进行兑换
			notMoney =redPacketMoneyInfo.getTotalMoney() -RedPacketDao.getInstance().notMoney(userId);// 更新用户未兑换金额
			Date date = new Date();// 获得当前时间
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 时间格式化
			String time = formatter.format(date);
			RedPacketDao.getInstance().addExchange(userId, money, wxname, phone, time);// 添加兑换记录
			endMoney = RedPacketDao.getInstance().notMoney(userId);// 更新用户已兑换金额
			result.put("endMoney", endMoney);
			result.put("notMoney", notMoney);
			result.put("msg", "兑换成功");
			this.writeMsg(0, result);
		} else {
			result.put("msg", "兑换金额不够");
			this.writeMsg(-1, result);
			return;
		}
	}

	/**
	 * 领取红包记录
	 */
	public void ReceiveRecord() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Long userId = this.getLong("userId", 0);// 用户ID
		int hbType = this.getInt("hbType", 0);// 红包类型
		if (userId == 0) {
			result.put("msg", "用户ID错误");
			this.writeMsg(-1, result);
			return;
		}

		if (hbType != 1 && hbType != 2) {
			result.put("msg", "红包类型错误");
			this.writeMsg(-1, result);
			return;
		}
		Map<Object, Object> hbmap = new LinkedHashMap<Object, Object>();// 红包map
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// 格式化时间
	  //牌桌红包
	    if (hbType == 1) {
	      List<RedPacketRecord> userMoneyInfo = RedPacketDao.getInstance().allMoneyInfo(userId,1);
	      for (RedPacketRecord redPacketRecord : userMoneyInfo) {
	        List<RedPacketRecord> list = new ArrayList<>();// 红包记录集合
	        for (RedPacketRecord allInfo : userMoneyInfo) {
	          if (redPacketRecord.getTableId() == allInfo.getTableId()) {
	            list.add(allInfo);
	            hbmap.put(allInfo.getTableId() + "/" + dateFormat.format(allInfo.getCreateTime()), list);// key为牌桌ID+红包创建时间
//	            hbmap.put(allInfo.getTableId() + "/" + TimeUtil.formatTime(allInfo.getCreateTime()), list);// key为牌桌ID+红包创建时间

	          }
	        }
	      }
	      result.put("ReceiveRecord", hbmap);
	      this.writeMsg(0, result);
	    }
	    //幸运红包
	    if(hbType==2){
	      List<RedPacketRecord> userMoneyInfo = RedPacketDao.getInstance().userMoneyInfo(userId, 2);// 获得用户红包记录
	      for (RedPacketRecord redPacketRecord : userMoneyInfo) {
	        List<RedPacketRecord> list = new ArrayList<>();// 红包记录集合
	        list.add(redPacketRecord);
	        hbmap.put(redPacketRecord.getTableId() + "/" + dateFormat.format(redPacketRecord.getCreateTime()), list);// key为牌桌ID+红包创建时间
	      }
	      result.put("ReceiveRecord", hbmap);
	      this.writeMsg(0, result);
	    }

	}

	/**
	 * 红包排行榜
	 */

	public void hbRanking() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		ActivityBean rankingBaseInfo = StaticDataManager.getSingleActivityBaseInfo(ActivityConstant.activity_hbaward);
		if (rankingBaseInfo == null) {
			result.put("msg", "rankingBaseInfo is null, activity type = " + ActivityConstant.activity_hbaward);
			this.writeMsg(-2, result);
			return;
		}
		
		Long userId = this.getLong("userId", 0);// 用户ID
		if (userId == 0) {
			result.put("msg", "用户ID错误");
			this.writeMsg(-1, result);
			return;
		}
		List<HashMap<String, Object>> list = RedPacketDao.getInstance().RedRanking();// 取前50名用户
		RedPacketMoneyInfo redPacketMoneyInfo = RedPacketDao.getInstance().userMoeny(userId);// 个人红包金额
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMDD");// 格式化时间
		double totalMoney = 0;// 用户总金额
		int rank = 0;// 排行榜名次
		int myRank = -1;// 自己排行榜名次
		int load = 0;// 奖励
		int prizeFlag = 0;// 领奖状态
		
		

		if (list != null) {
			for (HashMap<String, Object> m : list) {
				Long uid = (Long) m.get("userId");// 获得用户ID
				rank = rank + 1;
				m.put("rank", rank);
				m.put("load", rankingBaseInfo.getAward(rank));
				m.put("num", m.get("totalMoney"));
				m.remove("totalMoney");
				m.remove("prizeFlag");
				// 获得自己排名
				if (uid.equals(userId)) {
					myRank = (int) m.get("rank");
				}
				m.remove("userId");
			}
			// 判断是否有用户数据
			if (redPacketMoneyInfo != null) {
				Date date = new Date();// 获得当前时间
				 int dqTime = Integer.parseInt(dateFormat.format(date));// 格式化当前时间
				totalMoney = redPacketMoneyInfo.getTotalMoney();// 获得用户总金额
				if (redPacketMoneyInfo.getPrizeFlag() != 2) {
					if(myRank>0&&myRank<=50){
						// 判断是否到达领取时间
						if (Integer.parseInt(dateFormat.format(rankingBaseInfo.getStartTime())) <= dqTime && Integer.parseInt(dateFormat.format(rankingBaseInfo.getEndDateTime())) >= dqTime) {
							RedPacketDao.getInstance().updatePrizeFlag(userId, 1);
						}else{
							RedPacketDao.getInstance().updatePrizeFlag(userId, 0);
						}
					}
				}
				prizeFlag = RedPacketDao.getInstance().userMoeny(userId).getPrizeFlag();// 获得领奖状态
			}
			
			result.put("prizeFlag", prizeFlag);
			result.put("load", rankingBaseInfo.getAward(myRank));
			result.put("num", totalMoney);
			result.put("myRank", myRank);
			result.put("time", rankingBaseInfo.getShowContent());
			result.put("list", list);
			this.writeMsg(0, result);
		} else {
			result.put("msg", "暂时没有排名");
			this.writeMsg(-1, result);
		}
	}

	/**
	 * 领取排行榜奖励
	 */
	public void HbRankReward() throws Exception {
		int rank = 0;// 排行榜名次
		int myRank = -1;// 自己排行榜名次
		int load = 0;// 奖励
		int prizeFlag = 0;// 领奖状态
		Map<String, Object> result = new HashMap<String, Object>();
		// 名次对应的奖品信息
		ActivityBean activity = StaticDataManager.getActivityBean(ActivityConstant.activity_hbaward);
		// 判断当前时间是否为活动可领取奖励时间
		if (activity == null) {
			result.put("msg", "未到领取时间");
			this.writeMsg(-1, result);
			return;
		}

		Long userId = this.getLong("userId", 0);// 用户ID
		if (userId == 0) {
			result.put("msg", "用户ID错误");
			this.writeMsg(-1, result);
			return;
		}

		List<HashMap<String, Object>> list = RedPacketDao.getInstance().RedRanking();// 取前50名用户
		if (list != null) {
			for (HashMap<String, Object> m : list) {
				Long uid = (Long) m.get("userId");// 获得用户ID
				rank = rank + 1;
				m.put("rank", rank);
				// 获得自己排名
				if (uid.equals(userId)) {
					myRank = (int) m.get("rank");
					prizeFlag = (int) m.get("prizeFlag");
				}
			}
			load = activity.getAward(myRank);// 对应的礼物
			// 判断排名进前50
			if (myRank > 0 && myRank <= 50) {
				// 判断状态是否为可领取
				if (prizeFlag == 1) {
					int update = RedPacketDao.getInstance().updatePrizeFlag(userId, 2);// 修改状态
					if (update == 1) {
						prizeFlag = RedPacketDao.getInstance().userMoeny(userId).getPrizeFlag();// 刷新状态
						// 判断礼物数是否大于0 大于则更新
						if (load > 0) {
							RegInfo user = userDao.getUser(userId);
							userDao.addUserCards(user, 0, load, 0, CardSourceType.receive_rank_reward);
						}
					} else {
						result.put("msg", "状态修改失败");
						this.writeMsg(-1, result);
						return;
					}
				} else {
					result.put("msg", "已领取或没到领取时间");
					this.writeMsg(-1, result);
					return;
				}
			} else {
				result.put("msg", "没有奖励");
				this.writeMsg(-1, result);
				return;
			}
		}

		result.put("load", load);
		result.put("prizeFlag", prizeFlag);
		this.writeMsg(0, result);
	}
}
