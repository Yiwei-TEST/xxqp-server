package com.sy599.game.websocket.netty.handshaker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.FastThreadLocal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lz
 */
public class WebSocketUtil {
    private static final FastThreadLocal<MessageDigest> MD5 = new FastThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() throws Exception {
            try {
                //Try to get a MessageDigest that uses MD5
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                //This shouldn't happen! How old is the computer?
                throw new InternalError("MD5 not supported on this platform - Outdated?");
            }
        }
    };

    private static final FastThreadLocal<MessageDigest> SHA1 = new FastThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() throws Exception {
            try {
                //Try to get a MessageDigest that uses SHA1
                return MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e) {
                //This shouldn't happen! How old is the computer?
                throw new InternalError("SHA-1 not supported on this platform - Outdated?");
            }
        }
    };

    /**
     * Performs a MD5 hash on the specified data
     *
     * @param data The data to hash
     * @return The hashed data
     */
    static byte[] md5(byte[] data) {
        // TODO(normanmaurer): Create md5 method that not need MessageDigest.
        return digest(MD5, data);
    }

    /**
     * Performs a SHA-1 hash on the specified data
     *
     * @param data The data to hash
     * @return The hashed data
     */
    static byte[] sha1(byte[] data) {
        // TODO(normanmaurer): Create sha1 method that not need MessageDigest.
        return digest(SHA1, data);
    }

    private static byte[] digest(FastThreadLocal<MessageDigest> digestFastThreadLocal, byte[] data) {
        MessageDigest digest = digestFastThreadLocal.get();
        digest.reset();
        return digest.digest(data);
    }

    /**
     * Performs base64 encoding on the specified data
     *
     * @param data The data to encode
     * @return An encoded string containing the data
     */
    static String base64(byte[] data) {
        ByteBuf encodedData = Unpooled.wrappedBuffer(data);
        ByteBuf encoded = Base64.encode(encodedData);
        String encodedString = encoded.toString(CharsetUtil.UTF_8);
        encoded.release();
        return encodedString;
    }

    /**
     * Creates an arbitrary number of random bytes
     *
     * @param size the number of random bytes to create
     * @return An array of random bytes
     */
    static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];

        for (int index = 0; index < size; index++) {
            bytes[index] = (byte) randomNumber(0, 255);
        }

        return bytes;
    }

    /**
     * Generates a pseudo-random number
     *
     * @param minimum The minimum allowable value
     * @param maximum The maximum allowable value
     * @return A pseudo-random number
     */
    static int randomNumber(int minimum, int maximum) {
        return (int) (Math.random() * maximum + minimum);
    }

    /**
     * A private constructor to ensure that instances of this class cannot be made
     */
    private WebSocketUtil() {
        // Unused
    }
}
