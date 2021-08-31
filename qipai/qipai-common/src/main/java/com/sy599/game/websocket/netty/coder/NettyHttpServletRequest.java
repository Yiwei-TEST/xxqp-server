package com.sy599.game.websocket.netty.coder;

import com.sy599.game.websocket.netty.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.*;

/**
 * Created by lz
 */
public class NettyHttpServletRequest implements HttpServletRequest {
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final Map<String, String> params = new HashMap<>();

    public NettyHttpServletRequest(ChannelHandlerContext ctx,FullHttpRequest request) {
        this.ctx = ctx;
        this.request = request;
        HttpMethod method = this.request.method();
        if (method == HttpMethod.GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> param = decoder.parameters();
            if (param != null && param.size() > 0) {
                for (Map.Entry<String, List<String>> kv : param.entrySet()) {
                    if (kv.getValue() != null && kv.getValue().size() > 0) {
                        params.put(kv.getKey(), kv.getValue().get(0));
                    }
                }
            }
        } else if (method == HttpMethod.POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                    new DefaultHttpDataFactory(false), request);
            List<InterfaceHttpData> postDatas = decoder.getBodyHttpDatas();
            if (postDatas != null && postDatas.size() > 0) {
                for (InterfaceHttpData postData : postDatas) {
                    if (postData != null && postData.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) postData;
                        try {
                            params.put(attribute.getName(), attribute.getValue());
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        return request.headers().getTimeMillis(s);
    }

    @Override
    public String getHeader(String s) {
        return request.headers().get(s);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return Collections.enumeration(request.headers().getAll(s));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.headers().names());
    }

    @Override
    public int getIntHeader(String s) {
        return request.headers().getInt(s);
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getPathInfo() {
        return request.uri();
    }

    @Override
    public String getPathTranslated() {
        return request.uri();
    }

    @Override
    public String getContextPath() {
        return request.uri();
    }

    @Override
    public String getQueryString() {
        return request.uri();
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return request.uri();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(request.uri());
    }

    @Override
    public String getServletPath() {
        return request.uri();
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return params.get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(params.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return HttpUtil.getCharset(request).name();
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return request.headers().getInt(HttpHeaderNames.CONTENT_LENGTH);
    }

    @Override
    public long getContentLengthLong() {
        return HttpUtil.getContentLength(request);
    }

    @Override
    public String getContentType() {
        return request.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String s) {
        return params.get(s);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(params.keySet());
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[]{params.get(s)};
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>();
        for (Map.Entry<String, String> kv : params.entrySet()) {
            map.put(kv.getKey(), new String[]{kv.getValue()});
        }
        return map;
    }

    @Override
    public String getProtocol() {
        return request.protocolVersion().protocolName();
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return NettyUtil.getRemoteAddr(ctx);
    }

    @Override
    public String getRemoteHost() {
        String host = (((InetSocketAddress)ctx.channel().remoteAddress())).getAddress().getHostName();
        return host;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
