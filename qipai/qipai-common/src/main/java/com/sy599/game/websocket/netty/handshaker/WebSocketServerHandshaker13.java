package com.sy599.game.websocket.netty.handshaker;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by lz
 */
public class WebSocketServerHandshaker13 extends io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker13 {

    public WebSocketServerHandshaker13(
            String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength) {
        this(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength, false);
    }

    /**
     * Constructor specifying the destination web socket location
     *
     * @param webSocketURL
     *        URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web
     *        socket frames will be sent to this URL.
     * @param subprotocols
     *        CSV of supported protocols
     * @param allowExtensions
     *        Allow extensions to be used in the reserved bits of the web socket frame
     * @param maxFramePayloadLength
     *        Maximum allowable frame payload length. Setting this value to your application's
     *        requirement may reduce denial of service attacks using long data frames.
     * @param allowMaskMismatch
     *            When set to true, frames which are not masked properly according to the standard will still be
     *            accepted.
     */
    public WebSocketServerHandshaker13(
            String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength,
            boolean allowMaskMismatch) {
        super(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength,allowMaskMismatch);
    }

    @Override
    protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers) {
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS);
        if (headers != null) {
            res.headers().add(headers);
        }

        CharSequence key = req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_KEY);
        if (key == null) {
            throw new WebSocketHandshakeException("not a WebSocket request: missing key");
        }
        String acceptSeed = key + WEBSOCKET_13_ACCEPT_GUID;
        byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        String accept = WebSocketUtil.base64(sha1);

        if (logger.isDebugEnabled()) {
            logger.debug("WebSocket version 13 server handshake key: {}, response: {}", key, accept);
        }

        /**
         * 必须包含Upgrade,Connection,Sec-WebSocket-Accept（区分大小写）
         */
        res.headers().add("Upgrade", "websocket");
        res.headers().add("Connection", "Upgrade");
        res.headers().add("Sec-WebSocket-Accept", accept);

        String subprotocols = req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        if (subprotocols != null) {
            String selectedSubprotocol = selectSubprotocol(subprotocols);
            if (selectedSubprotocol == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Requested subprotocol(s) not supported: {}", subprotocols);
                }
            } else {
                res.headers().add(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, selectedSubprotocol);
            }
        }
        return res;
    }

}
