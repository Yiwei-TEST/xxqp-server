package com.sy.sanguo.game.action.competition;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingConfigDB;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingConfigExtModel;
import com.sy.sanguo.game.competition.service.CompetitionGeneratePlayingService;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.competition.util.NetworkUtils;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

/**
 * @author Guang.OuYang
 * @date 2020/5/26-17:01
 */
@Component
@Scope("prototype")
public class CompetitionDeployAction extends GameStrutsAction {
	@Autowired
	private CompetitionGeneratePlayingService competitionGeneratePlayingService;
	@Autowired
	private CompetitionPlayingService competitionPlayingService;

	public void addConfig() {
		try {
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|deploy|addConfig|"+params);

			CompetitionPlayingConfigDB build = CompetitionPlayingConfigDB.builder().build();
			BeanUtils.copyProperties(build, params);

			build.setTitleCode(build.getTitleCode()
					.replace("%2B","+")
					.replace("%20"," ")
					.replace("%2F","/")
					.replace("%3F","?")
					.replace("%23","%")
					.replace("%26","&")
					.replace("%3D","="));
			build.setLoginCallBackUrl(NetworkUtils.getServletBaseUrl(getRequest()));


			CompetitionPlayingConfigExtModel build1 = CompetitionPlayingConfigExtModel.builder().build();
			BeanUtils.copyProperties(build1, params);

			build.setExtModel(JSONObject.toJSONString(build1));

			competitionGeneratePlayingService.addNewConfig(build);

			OutputUtil.output(0, "success", getRequest(), getResponse(), false);
		} catch (MessageException m){
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionDeployAction|addConfig|error|",e);
		}
	}

	public void closeConfig() {
		try {
			Map<String, String> params = checkAndGetParams();
			LogUtil.i("competition|deploy|closeConfig|" + params);

			CompetitionPlayingConfigDB build = CompetitionPlayingConfigDB.builder().build();
			BeanUtils.copyProperties(build, params);

			if (params.containsKey("open")) {
				competitionGeneratePlayingService.openConfig(build);
			} else {
				competitionGeneratePlayingService.closeConfig(build);
			}
			competitionGeneratePlayingService.clearConfigCache();

			OutputUtil.output(0, "success", getRequest(), getResponse(), false);
		}
		catch (MessageException m) {
			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
		}
		catch (Exception e) {
			GameBackLogger.SYS_LOG.error("CompetitionDeployAction|addConfig|error|", e);
		}
	}

//	public void delConfig() {
//		try {
//			Map<String, String> params = checkAndGetParams();
//			LogUtil.i("competition|deploy|closeConfig|"+params);
//
//			CompetitionPlayingConfigDB build = CompetitionPlayingConfigDB.builder().build();
//			BeanUtils.copyProperties(build, params);
//
//			competitionGeneratePlayingService.delConfig(build);
//
//			OutputUtil.output(0, "success", getRequest(), getResponse(), false);
//		} catch (MessageException m){
//			OutputUtil.output(m.getCode(), m.getMessage(), getRequest(), getResponse(), false);
//		} catch (Exception e) {
//			GameBackLogger.SYS_LOG.error("CompetitionDeployAction|addConfig|error|",e);
//		}
//	}

	public void clearConfigCache() {
		LogUtil.i("competition|deploy|clearConfigCache");

		competitionGeneratePlayingService.clearConfigCache();
		OutputUtil.output(0, "success", getRequest(), getResponse(), false);
	}

	public void ipPortTest() {
		OutputUtil.output(0, NetworkUtils.getHttpPort(), getRequest(), getResponse(), false);
	}

	private Map<String, String> checkAndGetParams() throws UnsupportedEncodingException {
		Map<String, String> params = UrlParamUtil.getParameters(getRequest());
//		LOGGER.info("params:{}", params);

		if(!competitionPlayingService.isDebug()){
			if (/*!checkSign(params)*/ !params.containsKey("signKey") || !params.get("signKey").equalsIgnoreCase(TimeUtil.formatTimeMin(new Date()))) {
				throw new MessageException(-1, LangMsg.getMsg(LangMsg.code_1));
			}
		}

		return params;
	}
}
