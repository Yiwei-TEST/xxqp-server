package com.sy599.game.websocket.netty.coder;

import com.sy599.game.websocket.netty.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * Created by lz
 */
public class NettyHttpServletResponse implements HttpServletResponse {
    private final ChannelHandlerContext ctx;
    private final FullHttpResponse response;
    private final ServletOutputStream outputStream;
    private final PrintWriter printWriter;
    private volatile boolean flush = false;

    public NettyHttpServletResponse(ChannelHandlerContext ctx0, FullHttpResponse response) {
        this.ctx = ctx0;
        this.response = response;

        this.response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        this.response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        outputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                response.content().writeByte(b);
            }

            @Override
            public void flush() throws IOException {
                if (!flush){
                    flush = true;
                    HttpUtil.setContentLength(response, response.content().readableBytes());
                    NettyUtil.sendHttpResponse(ctx, response);
                }
            }
        };

        printWriter = new PrintWriter(outputStream);
    }

    public boolean isFlush() {
        return flush;
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void setHeader(String s, String s1) {
        response.headers().set(s, s1);
    }

    @Override
    public void addHeader(String s, String s1) {
        response.headers().add(s, s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        response.headers().setInt(s, i);
    }

    @Override
    public void addIntHeader(String s, int i) {
        response.headers().addInt(s, i);
    }

    @Override
    public void setStatus(int i) {
        response.setStatus(HttpResponseStatus.valueOf(i));
    }

    @Override
    public void setStatus(int i, String s) {
        response.setStatus(HttpResponseStatus.valueOf(i, s));
    }

    @Override
    public int getStatus() {
        return response.status().code();
    }

    @Override
    public String getHeader(String s) {
        return response.headers().get(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return response.headers().getAll(s);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.headers().names();
    }

    @Override
    public String getCharacterEncoding() {
        return HttpUtil.getCharset(response).name();
    }

    @Override
    public String getContentType() {
        return response.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {
        HttpUtil.setContentLength(response, i);
    }

    @Override
    public void setContentLengthLong(long l) {
        HttpUtil.setContentLength(response, l);
    }

    @Override
    public void setContentType(String s) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, s);
    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
