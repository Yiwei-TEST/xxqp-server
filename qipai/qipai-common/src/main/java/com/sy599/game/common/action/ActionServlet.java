
package com.sy599.game.common.action;

import com.sy599.game.common.constant.KeyConstants;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.MD5Util;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final List<String> excludeTypes = Arrays.asList("2", "15");

    public ActionServlet() {
    }

    public void init() throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            String type = request.getParameter("type");
            if (!StringUtils.isBlank(type) && !excludeTypes.contains(type)) {
                String syTime = request.getParameter("sytime");
                String sySign = request.getParameter("sysign");
                if (StringUtils.isBlank(syTime) || StringUtils.isBlank(sySign)) {
                    this.sendErrMsg(response);
                    return;
                }

                String md5 = MD5Util.getMD5String(syTime + KeyConstants.md5_http);
                if (!md5.equals(sySign)) {
                    this.sendErrMsg(response);
                    return;
                }
            }

            if (StringUtils.isBlank(type)) {
                this.sendErrMsg(response);
            } else {
                ActionProcessor.getInstance().process(request, response);
            }
    }

    private void sendErrMsg(HttpServletResponse response) throws IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> temp = new HashMap(4);
        temp.put("code", -3);
        response.getWriter().write(JacksonUtil.writeValueAsString(temp));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
