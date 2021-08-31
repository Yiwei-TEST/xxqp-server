package com.sy.sanguo.common.struts;


import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.result.StrutsResultSupport;


/**
 * 重新struts2 Result 
 * @author lc
 *
 */
public class StringResultType extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private String contentTypeName;
	private String stringName = "";
	/**
	 * 返回结果属性名称
	 */
	public static final String RETURN_ATTRIBUTE_NAME="result";

	public StringResultType() {
		super();
	}

	public StringResultType(String location) {
		super(location);
	}

	@Override
	protected void doExecute(String finalLocation, ActionInvocation invocation)
			throws Exception {
		HttpServletResponse response = (HttpServletResponse) invocation
				.getInvocationContext().get(HTTP_RESPONSE);
		HttpServletRequest request = (HttpServletRequest) ActionContext
		.getContext().get(StrutsStatics.HTTP_REQUEST);
		String contentType = conditionalParse(contentTypeName, invocation);
		if (contentType == null) {
			contentType = "text/html;charset=UTF-8";
		}
		response.setContentType(contentType);
		response.addHeader("Access-Control-Allow-Origin", "*");
		String result;
		
		
		PrintWriter out = response.getWriter();
		// String result = conditionalParse(stringName, invocation);
		result = (String) invocation.getStack().findValue(stringName);
		request.setAttribute(RETURN_ATTRIBUTE_NAME, result);
		out.print(result);
		out.flush();
		out.close();
	}

	public String getContentTypeName() {
		return contentTypeName;
	}

	public void setContentTypeName(String contentTypeName) {
		this.contentTypeName = contentTypeName;
	}

	public String getStringName() {
		return stringName;
	}

	public void setStringName(String stringName) {
		this.stringName = stringName;
	}

}
