package com.sy.sanguo.game.action.competition;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.param.CompetitionApplyParam;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelReq;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelRes;
import com.sy.sanguo.game.competition.service.CompetitionApplyService;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService.CallBackInvoke;
import com.sy.sanguo.game.competition.util.ParamCheck;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;

/**
 * @author Guang.OuYang
 * @date 2020/5/26-17:01
 */
@Component("competitionPlaying")
@Scope("prototype")
public class CompetitionPlayingAction extends GameStrutsAction {

	@Autowired
	private CompetitionPlayingService competitionPlayingService;
	@Autowired
	private CompetitionApplyService competitionApplyService;


	/**
	 *@description 小结更新榜单
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/8
	 */
	public void onlyRefreshRank() {
		try {
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|playing|action|onlyRefreshRank| " + params);

			if (params.get("arg") == null) {
				LogUtil.i("competition|playing|action|onlyRefreshRank|paramError| " + params);
				//异步清算
				OutputUtil.output(-1, "paramError", getRequest(), getResponse(), false);
				return;
			}

			CompetitionClearingModelReq arg = JSONObject.parseObject(params.get("arg"), CompetitionClearingModelReq.class);


			competitionPlayingService.batchUpdateRank(arg);

			//推送总榜单变化
			competitionPlayingService.pushRankChange(competitionPlayingService.getPlaying(arg.getPlayingId()).get(), true);
		} catch (Exception e) {
			LogUtil.e("competition|");
		}
	}

	/**
	 *@description 结算
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	public void clearing() {
		try {
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|playing|action|clearing| " + params);

			if(params.get("arg") == null){
				LogUtil.i("competition|playing|action|clearing|paramError| " + params);
				//异步清算
				OutputUtil.output(-1, "paramError", getRequest(), getResponse(), false);
				return;
			}

			CompetitionClearingModelReq arg = JSONObject.parseObject(params.get("arg"), CompetitionClearingModelReq.class);

			long t = System.currentTimeMillis();
			CallBackInvoke<CompetitionClearingModelRes> handle = competitionPlayingService.createHandle(arg);//batchUpdateRank(arg);
			handle.setMiddle(() -> competitionPlayingService.clearingCallBack(handle, arg));

//			handle.beforeInvoke();
			handle.middleInvoke();

			CompetitionClearingModelRes result = handle.getResult();

			LogUtil.i("competition|playing|action|clearing|invoke|"+(System.currentTimeMillis()-t)+"ms"+"|"+result);
			if ((System.currentTimeMillis() - t) >= 1000) {
				GameBackLogger.SYS_LOG.warn("competition|playing|action|clearing|invoke|>1000|" + (System.currentTimeMillis() - t) + "ms" + "|" + params + "|" + result);
			}
			if (result == null) {
				LogUtil.e("competition|playing|action|clearing|return null error|" + arg);
				OutputUtil.output(-1, "clearingException", getRequest(), getResponse(), false);
				return;
			}
			//异步清算
			OutputUtil.output(0, result, getRequest(), getResponse(), false);

			if (handle.getResult() != null) {
				//回调
				handle.afterInvoke();
			}
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			LogUtil.e("competition|playing|action|clearing|action|error|", e);
		}
	}

	/**
	 *@description 处理在其他牌局中的玩家退赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/6
	 */
	public void cancel() {
		try{
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|playing|action|cancel|" + params);
			competitionApplyService.cancel(CompetitionApplyParam.builder()
					.playingId(ParamCheck.check(params.get("playingId"), NumberUtils::toLong))
					.userId(ParamCheck.check(params.get("userId"), NumberUtils::toLong))
					.build(), true);
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionPlayingAction|cancel|error|",e);
		}
	}

	/**
	 *@description 拉回报名界面
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/6
	 */
	public void backSign() {
		try{
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|playing|action|backSign|" + params);
			Optional<CompetitionPlayingDB> playingDBOptional = competitionPlayingService.serachPlayerSignPlaying(NumberUtils.toLong(params.get("userId")));
			if (playingDBOptional.isPresent()) {
				OutputUtil.output(1, playingDBOptional.get(), getRequest(), getResponse(), false);
			} else {
				OutputUtil.output(0, "", getRequest(), getResponse(), false);
			}
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionPlayingAction|cancel|error|",e);
		}
	}

	/**
	 *@description 拉回比赛等待晋级界面
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/6
	 */
	public void playCheck() {
		try{
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|playing|action|backSign|" + params);

//			competitionPlayingService.playingClearingAwardSend(514,0);
			CompetitionClearingModelRes clearingModelRes = competitionPlayingService.playCheck(Long.valueOf(params.get("userId")));
			if(clearingModelRes == null){
				OutputUtil.output(0, "", getRequest(), getResponse(), false);
			} else {
				OutputUtil.output(1, clearingModelRes, getRequest(), getResponse(), false);
			}
		}  catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionPlayingAction|cancel|error|",e);
		}
	}

	private Map<String, String> getParams() throws UnsupportedEncodingException {
		return UrlParamUtil.getParameters(getRequest());
	}

	private Map<String, String> checkAndGetParams() throws UnsupportedEncodingException {
		Map<String, String> params = getParams();

		if(!competitionPlayingService.isDebug()){
			if (!checkSign(params)) {
				throw new MessageException(-1, LangMsg.getMsg(LangMsg.code_1));
			}
		}

		return params;
	}

}
