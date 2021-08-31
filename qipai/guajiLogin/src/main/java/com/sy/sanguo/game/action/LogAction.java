package com.sy.sanguo.game.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sy.sanguo.common.struts.StringResultType;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.JacksonUtil;

public class LogAction extends GameStrutsAction {

	private static final long serialVersionUID = 5762297438266220836L;
	private String result = "";

	@SuppressWarnings("unchecked")
	public String ioslog() {
		Map<String,String> paramMap = new HashMap<String,String>();
		Set<String> set = getRequest().getParameterMap().keySet();
		for (String o : set) {
			paramMap.put(o, getRequest().getParameter(o));
			//GameBackLogger.MONITOR_LOG.info("ios log " + o + "  " + getRequest().getParameter(o));
		}
		GameBackLogger.MONITOR_LOG.info("ios log::" + JacksonUtil.writeValueAsString(paramMap));
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", 0);
		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	
	// 记录客户端日志
	public String clientLog() {
		if (!checkPdkSign()) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		
		String log = getRequest().getParameter("log");
		if (StringUtils.isBlank(log)) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		
		GameBackLogger.MONITOR_LOG.info(log);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", 0);
		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
