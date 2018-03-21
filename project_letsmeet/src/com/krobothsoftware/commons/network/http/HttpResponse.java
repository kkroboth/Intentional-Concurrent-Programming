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

package com.krobothsoftware.commons.network.http;

import com.krobothsoftware.commons.util.StringUtils;
import com.krobothsoftware.commons.util.UnclosableInputStream;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Response holder from {@link HttpRequest#execute(NetworkHelper)}. Make sure to
 * call {@link #close()}.
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.HttpRequest#execute(NetworkHelper)
 * @since COMMONS 1.1.0
 */
public class HttpResponse implements AutoCloseable {
    protected final HttpURLConnection conn;
    protected final UnclosableInputStream stream;
    protected final int status;
    protected final String charset;

    /**
     * Instantiates a new response with results from connection.
     *
     * @param connection
     * @param input      non-null stream
     * @param status     response code
     * @param charset    charset of connection
     * @since COMMONS 1.1.0
     */
    public HttpResponse(HttpURLConnection connection,
                        UnclosableInputStream input, int status, String charset) {
        this.conn = connection;
        this.stream = input;
        this.status = status;
        this.charset = charset;
    }

    /**
     * Get response connection.
     *
     * @return connection
     * @since COMMONS 1.1.0
     */
    public HttpURLConnection getConnection() {
        return conn;
    }

    /**
     * Get response stream. May be InputStream or ErrorStream. Will never be
     * null.
     *
     * @return stream from connection
     * @see UnclosableInputStream#forceClose()
     * @since COMMONS 1.1.0
     */
    public UnclosableInputStream getStream() {
        return stream;
    }

    /**
     * Get response status code.
     *
     * @return response code
     * @since COMMONS 1.1.0
     */
    public int getStatusCode() {
        return status;
    }

    /**
     * Checks if status code is successful.
     * <p/>
     * <p>
     * <code>status >= 200 and status < 300</code>
     * </p>
     *
     * @return true, if status code is considered successful
     * @since COMMONS 1.1.0
     */
    public boolean isSuccessful() {
        return status / 100 == 2;
    }

    /**
     * Checks if status code is a redirection.
     * <p/>
     * <p>
     * <code>status >= 300 and status < 400</code>
     * </p>
     *
     * @return true, if status code is considered a redirection
     * @since COMMONS 1.1.0
     */
    public boolean isRedirection() {
        return status / 100 == 3;
    }

    /**
     * Checks if status code is a client error.
     * <p/>
     * <p>
     * <code>status >= 400 and status < 500</code>
     * </p>
     *
     * @return true, if status code is considered a client error
     * @since COMMONS 1.1.0
     */
    public boolean isClientError() {
        return status / 100 == 4;
    }

    /**
     * Checks if status code is a server error.
     * <p/>
     * <p>
     * <code>status >= 500 and status < 600</code>
     * </p>
     *
     * @return true, if status code is considered a server error
     * @since COMMONS 1.1.0
     */
    public boolean isServerError() {
        return status / 100 == 5;
    }

    /**
     * Get response charset.
     *
     * @return charset from connection
     * @since COMMONS 1.1.0
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Get connection content Length. Same as calling
     * {@link HttpURLConnection#getContentLength()}.
     * <p/>
     * <p>
     * Use {@link #getContentLengthLong()} to ensure length doesn't return
     * <i>-1</i>.
     * </p>
     *
     * @return connection content length
     * @see #getContentLengthLong()
     * @since COMMONS 1.1.0
     */
    public int getContentLength() {
        return conn.getContentLength();
    }

    /**
     * Parses <code>long</i> from <code>Content-Length</code> header.
     *
     * @return connection content length
     * @since COMMONS 1.1.0
     */
    public long getContentLengthLong() {
        return Long.parseLong(conn.getHeaderField("Content-Length"));
    }

    /**
     * Helper method for getting {@link HttpURLConnection} headers.
     *
     * @param name name of header
     * @return header value, or null
     * @since COMMONS 1.1.0
     */
    public String getHeader(String name) {
        return conn.getHeaderField(name);
    }

    /**
     * Disconnects connection and force closes inputstream in that order.
     * {@link UnclosableInputStream#forceClose()}.
     *
     * @throws IOException
     * @since COMMONS 1.1.0
     */
    @Override
    public void close() throws IOException {
        conn.disconnect();
        stream.forceClose();
    }

    /**
     * Returns string in format "Response [url] : [status-code]".
     *
     * @since COMMONS 1.1.0
     */
    @Override
    public String toString() {
        return String.format("Response %s : %s", conn.getURL().toString(),
                String.valueOf(status));
    }

    /**
     * Helper method for getting string from Response.
     *
     * @param response
     * @return String from stream
     * @see com.krobothsoftware.commons.util.StringUtils#toString(java.io.InputStream, String)
     * @since COMMONS 1.1.0
     */
    public static String toString(HttpResponse response) {
        return StringUtils.toString(response.stream, response.charset);
    }
}
