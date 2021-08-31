package com.sy.sanguo.common.struts;

import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.filter.FilterHostConfig;
import org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


public class MangoFilterDispatcher extends StrutsPrepareAndExecuteFilter {

    protected static final Logger loggerRoot = LoggerFactory.getLogger(MangoFilterDispatcher.class);
    protected static final Logger loggerService = LoggerFactory.getLogger("service");


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        //自定义的@link｛InitOperations｝
        MangoInitOperations init = new MangoInitOperations();
        Dispatcher dispatcher = null;

        try {
            FilterHostConfig config = new FilterHostConfig(filterConfig);
            init.initLogging(config);
            dispatcher = init.initDispatcher(config);
            init.initStaticContentLoader(config, dispatcher);
            this.prepare = this.createPrepareOperations(dispatcher);
            this.execute = this.createExecuteOperations(dispatcher);
            this.excludedPatterns = init.buildExcludedPatternsList(dispatcher);
            this.postInit(dispatcher, filterConfig);
        } finally {
            if (dispatcher != null) {
                dispatcher.cleanUpAfterInit();
            }

            init.cleanup();
        }
    }


    //	/**监控所有请求，如果请求消耗的时间过长，则记录到log*/
    @Override
    public void doFilter(javax.servlet.ServletRequest req,
                         javax.servlet.ServletResponse res, javax.servlet.FilterChain chain)
            throws java.io.IOException {
		/*HttpServletRequest request = (HttpServletRequest)req;
        long beforeTime = System.currentTimeMillis();
        InputStream in = request.getInputStream();
		JSONObject js = null;
		String param = "";*/
        req.setCharacterEncoding("utf-8");
        String str = PropertiesCacheUtil.getValue("performance_service", Constants.GAME_FILE);
        if (StringUtils.isBlank(str)){
            str="1";
        }
        boolean saveLog = "1".equals(str);
//
        long time = System.currentTimeMillis();
        try {
            super.doFilter(req, res, chain);
        } catch (Exception e) {
            loggerRoot.error("Exception:" + e.getMessage(), e);
        } finally {
            if (saveLog) {
                loggerService.info((new StringBuilder(256)).append("currentThread:").append(Thread.currentThread().toString()).append(" uri:").append(((HttpServletRequest) req).getRequestURI()).append(" time(ms):").append(System.currentTimeMillis() - time).append(" ip:").append(IpUtil.getIpAddr((HttpServletRequest)req)).toString());
            }
        }
    }


}
