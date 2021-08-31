package com.sy.sanguo.game.action.competition;

import com.alibaba.fastjson.JSON;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.param.CompetitionApplyParam;
import com.sy.sanguo.game.competition.model.param.CompetitionPlayingParam;
import com.sy.sanguo.game.competition.service.CompetitionApplyService;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Guang.OuYang
 * @date 2020/5/26-17:01
 */
@Component
@Scope("prototype")
public class CompetitionToolAction extends GameStrutsAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompetitionToolAction.class);

	@Autowired
	private CompetitionPlayingService competitionPlayingService;
	@Autowired
	private CompetitionApplyService competitionApplyService;

	/**
	 *@description 满员测试
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void testAllInSign() {
		try {
			Map<String, String> params = getParams();
			LOGGER.info("params:{}", params);
//			//找到最近一场开赛的比赛
			CompetitionPlayingParam build = CompetitionPlayingParam.builder().build();
			BeanUtils.copyProperties(build, params);

			List<CompetitionPlayingDB> competitionApplyPlayingList = competitionPlayingService.applyPlayingList(build);

			if (CollectionUtils.isEmpty(competitionApplyPlayingList)) {
				OutputUtil.output(-1, "当前参数没有找到合适的比赛"+ JSON.toJSONString(build), getRequest(), getResponse(), false);
				return;
			}

			CompetitionPlayingDB competitionPlayingDB = competitionApplyPlayingList.get(0);

			int m;
			if(params.containsKey("robotCount")) {
				m = Integer.valueOf(params.get("robotCount"));
			}else {
				m = Math.min(ThreadLocalRandom.current().nextInt(competitionPlayingDB.getMaxHuman()) + competitionPlayingDB.getBeginHuman(), competitionPlayingDB.getMaxHuman()) - competitionPlayingDB.getCurHuman();
			}

			long start = -Long.valueOf(competitionPlayingDB.getId() + 100000 + ThreadLocalRandom.current().nextInt(10000));

//			List<Long> userIds = Arrays.stream(params.get("userIds").split(",")).map(NumberUtils::toLong).collect(Collectors.toList());

			for (int i = 0 ; i < m;i++){
				try {
					competitionApplyService.sign(CompetitionApplyParam.builder()
							.userId(start++)
							.titleType(build.getTitleType())
							.playingType(build.getPlayingType())
							.category(build.getCategory())
							.entrance(build.getEntrance())
							.build());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			OutputUtil.output(0, competitionApplyPlayingList.get(0).getId(), getRequest(), getResponse(), false);
		} catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionAction|testAllInSign|error|",e);
		}
	}

	/**
	 *@description 手动加入房间
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void joinTable() {
		try {
			Map<String, String> params = getParams();
			LOGGER.info("params:{}", params);
//			//找到最近一场开赛的比赛
			CompetitionPlayingParam build = CompetitionPlayingParam.builder().build();
			BeanUtils.copyProperties(build, params);
			String res = competitionPlayingService.competitionJoinTable(build.getPlayingId());
			OutputUtil.output(0, res, getRequest(), getResponse(), false);
		} catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionAction|joinTable|error|",e);
		}
	}

	/**
	 *@description 手动清理房间
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	public void clearInvalidRoom() {
		try{
			Map<String, String> params = getParams();
			LogUtil.i("competition|action|clearInvalidRoom|"+params);
			competitionPlayingService.playingBatchChangeClearingEndToEndStatus(NumberUtils.toLong(params.get("playingId")), params.containsKey("force"));
		}catch (Exception e){
			GameBackLogger.SYS_LOG.error("CompetitionAction|clearInvalidRoom|error|",e);
		}
	}

	private Map<String,String> getParams() throws UnsupportedEncodingException{
		return UrlParamUtil.getParameters(getRequest());
	}

	private Map<String, String> checkAndGetParams() throws UnsupportedEncodingException {
		Map<String, String> params = getParams();
//		LOGGER.info("params:{}", params);

		if(!competitionPlayingService.isDebug()){
			if (!checkSign(params)) {
				throw new MessageException(-1, LangMsg.getMsg(LangMsg.code_1));
			}
		}

		return params;
	}
}
