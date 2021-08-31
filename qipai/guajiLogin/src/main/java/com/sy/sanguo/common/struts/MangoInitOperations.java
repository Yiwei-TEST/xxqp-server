package com.sy.sanguo.common.struts;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.HostConfig;
import org.apache.struts2.dispatcher.InitOperations;

/**
 * 自定义struts配置信息
 * @author taohuiliang
 * @date Jul 19, 2012
 * @version v1.0
 */
public class MangoInitOperations extends InitOperations {
	private final String relativePath="config/action";
	
	@Override
    public Dispatcher initDispatcher( HostConfig filterConfig ) {
        Dispatcher dispatcher = createDispatcher(filterConfig);
        dispatcher.init();
        return dispatcher;
    }

	@Override
	protected Dispatcher createDispatcher(HostConfig filterConfig) {
        Map<String, String> params = new HashMap<String, String>();  
        for ( Iterator<String> e = filterConfig.getInitParameterNames(); e.hasNext(); ) {  
            String name = e.next();  
            String value = filterConfig.getInitParameter(name);  
            params.put(name, value);   
        }
        
        ServletContext ctx = filterConfig.getServletContext();
  	  
        //获得自定义的配置表信息
  	    String strutsPath = getStrutsConfig(ctx);
		
		params.put("config", strutsPath);
  	  
        
        return new Dispatcher(filterConfig.getServletContext(), params);  
    }

	/**
	 * 根据自定义路径读取拼装配置路径字符串
	 * @param ctx
	 * @return String
	 * @throws
	 */
	private String getStrutsConfig(ServletContext ctx) {
		String home = ctx.getRealPath("/WEB-INF/classes/"+relativePath);
  	    home = home.replace('\\', '/');
  	    if (!home.endsWith("/")) {
  	      home = home + "/";
  	     }
  	    
  	    File f = new File(home);
		File[] ff = f.listFiles();
		String strutsPath = new String("struts-default.xml,struts-plugin.xml,config/struts.xml");
		if (ff != null && ff.length > 0) {
			for (int i = 0; i < ff.length; i++) {
				String fname = ff[i].getName();
			
				if (fname.endsWith(".xml")) {
				   strutsPath+=","+relativePath+"/"+fname;
				}
			}
		}
		return strutsPath;
	}  
}
