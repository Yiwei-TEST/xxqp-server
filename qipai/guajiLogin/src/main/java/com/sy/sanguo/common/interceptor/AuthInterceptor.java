package com.sy.sanguo.common.interceptor;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
/***
 * 权限过拦截器
 * @date 2013-1-23
 * @version v1.0
 */
public class AuthInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = 1L;
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
//		Object session = invocation.getInvocationContext().getSession().get("userAuth");
//		if(session instanceof UserAuthInfo){
//			if(!StringUtils.isBlank(((UserAuthInfo) session).getUserName())){
//				return invocation.invoke();
//			}
//		}
//		((BaseAction) invocation.getAction()).resultJson = "string";
		return Action.SUCCESS;
	}

}
