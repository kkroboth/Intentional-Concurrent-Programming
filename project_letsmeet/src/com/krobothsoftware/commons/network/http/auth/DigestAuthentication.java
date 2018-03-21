/*
 * Copyright 2018 Kroboth Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.krobothsoftware.commons.network.http.auth;

import com.krobothsoftware.commons.network.http.HttpHelper;
import com.krobothsoftware.commons.network.http.HttpRequest;
import com.krobothsoftware.commons.network.http.HttpResponseAuthenticate;
import com.krobothsoftware.commons.network.http.NameValuePair;
import com.krobothsoftware.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;
import java.util.Locale;

/**
 * Authentication with the digest scheme.
 *
 * @author Kyle Kroboth
 * @version 1.0
 */
public class DigestAuthentication extends PasswordAuthentication {
    public static final String SCHEME_NAME = "Digest";

    private static final String DEFAULT_ELEMENT_CHARSET = "US-ASCII";
    private static final Logger log = LoggerFactory.getLogger(DigestAuthentication.class);

    /**
     * Used to clone for thread safety.
     */
    private static MessageDigest md5;

    private final SecureRandom rnd = new SecureRandom();
    private String nonce;
    private String realm;
    private String algorithm;
    private String qop;
    private String charset;
    private String nc;
    private int nonceCount;

    /**
     * Hexa values used when creating 32 character long digest in HTTP
     * DigestScheme in case of auth. Credit goes to Apache Commons
     * HttpClient.
     *
     * @see #encode(byte[])
     */
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Instantiates a new digest auth with username and password.
     *
     * @param username
     * @param password in char array
     * @since COMMONS 1.0.1
     */
    public DigestAuthentication(String username, char[] password) {
        super(SCHEME_NAME, username, password);
    }

    /**
     * Sets up Digest header only if nonce is valid.
     *
     * @since COMMONS 1.0
     */
    @Override
    public void setup(HttpRequestAuthenticate request) throws IOException {
        String header = nonceUsable(request);

        if (header != null) {
            request.header(HEADER_AUTHORIZATION, header);
        }
    }

    /**
     * Authenticate using digest algorithm.
     *
     * @since COMMONS 1.0
     */
    @Override
    public void handshake(HttpRequestAuthenticate request,
                          HttpResponseAuthenticate response, HttpHelper httpHelper) throws IOException {

        String headerField = response.getAuthentication();
        response.close();
        nonceCount = 1;

        realm = getHeaderValueByType("realm", headerField);
        nonce = getHeaderValueByType("nonce", headerField);
        algorithm = getHeaderValueByType("algorithm", headerField);
        qop = getHeaderValueByType("qop", headerField);
        charset = getHeaderValueByType("charset", headerField);
        request.header(HEADER_AUTHORIZATION, createHeader(request));

        //return new HttpRequest(request).execute(httpHelper);

    }

    /**
     * Resets all values stored including nonce.
     *
     * @since COMMONS 1.0
     */
    @Override
    public void reset() {
        nonce = null;
        realm = null;
        algorithm = null;
        qop = null;
        charset = null;
        nc = null;
        nonceCount = 0;

    }

    /**
     * Has support, returns true.
     *
     * @since COMMONS 1.0
     */
    @Override
    public boolean handshakeSupported() {
        return true;
    }

    private String nonceUsable(HttpRequest request) throws IOException {

        if (nonce == null) return null;
        nonceCount++;

        return createHeader(request);

    }

    @SuppressWarnings("boxing")
    private String createHeader(HttpRequest request) throws IOException {
        MessageDigest messageDigest;
        StringBuffer buffer = null;

        try {
            // better than a new instance
            messageDigest = (MessageDigest) md5.clone();

            StringBuilder sb = new StringBuilder(256);
            Formatter formatter = new Formatter(sb, Locale.US);
            try {
                formatter.format("%08x", nonceCount);
            } finally {
                formatter.close();
            }
            nc = sb.toString();

            if (charset == null) {
                charset = DEFAULT_ELEMENT_CHARSET;
            }

            // generate client nonce
            String cnonce = createCnonce(rnd);

            // make MD5 hash of username, realm, and password
            buffer = new StringBuffer();
            buffer.append(username).append(':').append(realm).append(':')
                    .append(password);
            String hash1 = buffer.toString();
            hash1 = encode(messageDigest.digest(hash1.getBytes(charset)));

            // made MD5 hash of method name and url path
            buffer = new StringBuffer();
            buffer.append(request.method()).append(':')
                    .append(request.url().getPath());
            String hash2 = buffer.toString();
            hash2 = encode(messageDigest.digest(hash2.getBytes(charset)));

            // make MD5 hash of hash1, nonce, nc, cnonce, qop, and hash2
            buffer = new StringBuffer();
            buffer.append(hash1).append(':').append(nonce).append(':')
                    .append(nc).append(':').append(cnonce).append(':')
                    .append(qop).append(':').append(hash2);
            String response = buffer.toString();
            response = encode(messageDigest.digest(response.getBytes(charset)));

            // setup header

            String[] params = {"username", username, "realm", realm, "nonce",
                    nonce, "uri", request.url().getPath(), "algorithm",
                    algorithm, "response", response, "qop", qop, "nc", nc,
                    "cnonce", cnonce};

            buffer = new StringBuffer();
            buffer.append("Digest ");

            for (int i = 0; i < params.length; i += 2) {
                if (params[i].equals("algorithm") && params[i + 1] == null)
                    continue;

                if (!params[i].equalsIgnoreCase("username")) buffer
                        .append(", ");
                // check if need parentheses
                if (params[i].equalsIgnoreCase("nc") || params[i].equals("qop")) {
                    buffer.append(NameValuePair.getPair(params[i],
                            params[i + 1]));
                } else {
                    buffer.append(NameValuePair.getQuotedPair(params[i],
                            params[i + 1]));
                }
            }

        } catch (CloneNotSupportedException e) {
            // MD5 clone is supported
        }

        log.info("Authorizing Digest[{}] {}", String.valueOf(nonceCount),
                request.url());

        return buffer.toString();
    }

    private static String getHeaderValueByType(String type, String headerText) {
        String header = headerText.replaceFirst("Digest ", "");
        header = header.replaceFirst("Basic ", "");
        String[] values = header.split(",");

        for (String value : values) {
            if (type.equalsIgnoreCase(value.substring(0, value.indexOf("="))
                    .trim())) {
                return StringUtils.trim(
                        value.substring(value.indexOf("=") + 1), '"');
            }
        }

        return null;
    }

    /**
     * Encodes the 128 bit (16 bytes) MD5 digest into a 32 characters long
     * <CODE>String</CODE> according to RFC 2617. Credit goes to Apache Commons
     * HttpClient
     *
     * @param binaryData array containing the digest
     * @return encoded MD5, or <CODE>null</CODE> if encoding failed
     */
    private static String encode(byte[] binaryData) {
        int n = binaryData.length;
        char[] buffer = new char[n * 2];
        for (int i = 0; i < n; i++) {
            int low = binaryData[i] & 0x0f;
            int high = (binaryData[i] & 0xf0) >> 4;
            buffer[i * 2] = HEXADECIMAL[high];
            buffer[i * 2 + 1] = HEXADECIMAL[low];
        }

        return new String(buffer);
    }

    /**
     * Creates a random cnonce value based on the current time. Credit goes to
     * Apache Commons HttpClient
     *
     * @return The cnonce value as String.
     */
    private static String createCnonce(SecureRandom rnd) {
        byte[] tmp = new byte[8];
        rnd.nextBytes(tmp);
        return encode(tmp);
    }

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
