package com.sy.sanguo.game.action.competition;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreTotalDB;
import com.sy.sanguo.game.competition.model.enums.CompetitionScoreDetailStatusEnum;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.competition.service.CompetitionRankService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Guang.OuYang
 * @date 2020/5/26-17:01
 */
@Component
@Scope("request")
public class CompetitionRankAction extends GameStrutsAction {

	@Autowired
	private CompetitionPlayingService competitionPlayingService;
	@Autowired
	private CompetitionRankService competitionRankService;

	/**
	 *@description 满员测试
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void getRank() {
		try {
			Map<String, String> params = checkAndGetParams();

			CompetitionScoreTotalDB build = convertBuildParam(params);

			//当前赛场全部榜单
			LinkedList<CompetitionScoreTotalDB> ranks = new LinkedList<>(competitionRankService.getRank(
					CompetitionScoreTotalDB.builder()
							.playingId(build.getPlayingId())
							.status(CompetitionScoreDetailStatusEnum.RISE_IN_RANK)
							.limit(30)
							.build()));

			//头部加入自己的榜单
			CompetitionScoreTotalDB rankForSelf = competitionRankService.getRankForSelf(
					CompetitionScoreTotalDB.builder()
							.playingId(build.getPlayingId())
							.userId(build.getUserId())
							.build());

			//这里的榜单已经不正确了,因为在游戏内一定会有排名,或者玩家不在该场比赛
			if (rankForSelf == null) {
				rankForSelf = CompetitionScoreTotalDB.builder().rank(99).userId(build.getUserId()).build();
			}

			ranks.addFirst(rankForSelf);

			OutputUtil.output(0, ranks, getRequest(), getResponse(), false);
		} catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionRankAction|getRank|error|",e);
		}
	}

	private CompetitionScoreTotalDB convertBuildParam(Map<String, String> params) throws IllegalAccessException, InvocationTargetException {
		if (!params.containsKey("playingId") || !params.containsKey("userId")) {
			OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
		}

		CompetitionScoreTotalDB build = CompetitionScoreTotalDB.builder().build();
		BeanUtils.copyProperties(build, params);

		return build;
	}

	/**
	 *@description 满员测试
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void getRankSimple() {
		try {
			Map<String, String> params = checkAndGetParams();

			CompetitionScoreTotalDB build = convertBuildParam(params);
			CompetitionScoreTotalDB rankSimple = competitionRankService.getRankSimple(build);

			OutputUtil.output(0, rankSimple, getRequest(), getResponse(), false);
		} catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionRankAction|getRankSimple|error|",e);
		}
	}

	private Map<String, String> checkAndGetParams() throws UnsupportedEncodingException {
		Map<String, String> params = UrlParamUtil.getParameters(getRequest());

		if(!competitionPlayingService.isDebug()){
			if (!checkSign(params)) {
				throw new MessageException(-1, LangMsg.getMsg(LangMsg.code_1));
			}
		}

		return params;
	}
}
