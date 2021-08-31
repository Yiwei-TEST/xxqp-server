package com.sy.sanguo.game.action.competition;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.model.param.CompetitionApplyParam;
import com.sy.sanguo.game.competition.model.param.CompetitionPlayingParam;
import com.sy.sanguo.game.competition.model.param.HistoryParam;
import com.sy.sanguo.game.competition.service.CompetitionApplyService;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.competition.service.CompetitionRunningHorseLightService;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.beanutils.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Guang.OuYang
 * @date 2020/5/26-17:01
 */
@Component
@Scope("prototype")
public class CompetitionAction extends GameStrutsAction {
	@Autowired
	private CompetitionPlayingService competitionPlayingService;
	@Autowired
	private CompetitionApplyService competitionApplyService;
	@Autowired
	private CompetitionRunningHorseLightService competitionRunningHorseLightService;

	/**
	 *@description 报名
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void sign() {
		try {
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|action|sign|"+params);
			//找到最近一场开赛的比赛
			CompetitionApplyParam build = CompetitionApplyParam.builder().build();
			BeanUtils.copyProperties(build, params);
			String sign = competitionApplyService.sign(build);
			OutputUtil.output(0, sign == null ? "" : sign, getRequest(), getResponse(), false);
		} catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionAction|sign|error|",e);
		}
	}

	/**
	 *@description 退赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void cancel() {
		try {
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|action|cancel|"+params);
			//找到最近一场开赛的比赛
			CompetitionApplyParam build = CompetitionApplyParam.builder().build();
			BeanUtils.copyProperties(build, params);
			competitionApplyService.cancel(build, false);
			OutputUtil.output("0", "success", getRequest(), getResponse(), false);

		} catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionAction|cancel|error|",e);
		}
	}

	/**
	 *@description 获得最近一场可用的比赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void getPlayingList() {
		Map<String, String> params = null;
		try{
			params = checkAndGetParams();

			CompetitionPlayingParam build = CompetitionPlayingParam.builder().build();
			BeanUtils.copyProperties(build, params);
			build.setStatus(null);

			OutputUtil.output(0, competitionPlayingService.playingList(build), getRequest(), getResponse(), false);
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionAction|getPlayingList|error|" + getResponse().isCommitted() + " params:" + params, e);
		}
	}

	public void signShowRefresh() {
		Map<String, String> params = null;
		try{
			params = checkAndGetParams();
			OutputUtil.output(0, competitionPlayingService.signShowRefresh(Long.valueOf(params.get("userId")), Long.valueOf(params.get("playingId")), Integer.valueOf(params.get("signShow"))), getRequest(), getResponse(), false);
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionAction|getPlayingList|error|" + getResponse().isCommitted() + " params:" + params, e);
		}
	}

	public void runningHorseLight() {
		try{
			OutputUtil.output(0, competitionRunningHorseLightService.findLastLight(), getRequest(), getResponse(), false);
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionPlayingAction|cancel|error|",e);
		}
	}

	public void history() {
		Map<String, String> params = null;
		try{
			params = checkAndGetParams();
			if(!params.containsKey("userId")){
				return;
			}

			HistoryParam history = competitionPlayingService.getHistory(Long.valueOf(params.get("userId")));
			OutputUtil.output(0, Arrays.asList(history.getTotal(),history.getOne(),history.getTwo(),history.getThree()), getRequest(), getResponse(), false);
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionPlayingAction|cancel|error|",e);
		}
	}

	private Map<String,String> getParams() throws UnsupportedEncodingException{
		return UrlParamUtil.getParameters(getRequest());
	}

	private Map<String, String> checkAndGetParams() throws UnsupportedEncodingException {
		Map<String, String> params = getParams();
//		LOGGER.info("params:{}", params);

//		params.entrySet().removeIf(v-> StringUtils.isBlank(v.getValue()) || v.getValue().equalsIgnoreCase("null"));

		if(!competitionPlayingService.isDebug()){
			if (!checkSign(params)) {
				throw new MessageException(-1, LangMsg.getMsg(LangMsg.code_1));
			}
		}

		return params;
	}
}
