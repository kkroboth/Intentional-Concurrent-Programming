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

import com.krobothsoftware.commons.network.http.HttpRequestBuilderRedirect.RedirectHandler;
import com.krobothsoftware.commons.network.http.cookie.Cookie;
import com.krobothsoftware.commons.network.http.cookie.CookieManager;
import com.krobothsoftware.commons.network.http.cookie.CookieMap;
import com.krobothsoftware.commons.util.UnclosableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Builder for requesting HTTP connections. Call {@link #execute(HttpHelper)} to
 * send connection and get {@link HttpResponse}.
 * <p/>
 * <p>
 * Internally handled responses for HTTP response code </br>
 * <table border="1">
 * <tr>
 * <td>301</td>
 * <td>Resends request with new Location</td>
 * </tr>
 * <tr>
 * <td>302</td>
 * <td>Resends request with new Location. Only if <i>followRedirects</i> is true
 * </td>
 * </tr>
 * </table>
 * </br> Use {@link #setInternalHandler(int, RequestHandler)} to set an internal
 * handler. </br> To ignore handling, use {@link #ignoreCode(int)}.
 * </p>
 * <p/>
 * <pre>
 * <b>GET Request</b>
 * <p>
 * Uses custom CookieMap which will send and store cookies from the token.
 * Unless {@link #requestCookies(boolean)} is false, cookies from
 * <code>HttpHelper</code> CookieManager will be sent too.
 * </p>
 * <code>
 * CookieMap token = new CookieMap();
 *
 * HttpResponse response = null;
 * HttpRequest request = new HttpRequest(Method.GET, new URL(
 * 	"http://example.com"))
 *  .header("name", "value")
 *  .use(token);
 * try {
 * 	response = request.execute(networkHelper);
 * } catch (IOException e) {
 * 	// handle it
 * } finally {
 * 	CommonUtils.closeQuietly(response);
 * }
 * </code>
 *
 * <b>POST Request</b>
 * <p>
 * Sends POST request with payload as <i>application/x-www-form-urlencoded</i>
 * params.
 * </p>
 * <code>
 * List<NameValuePair> params = HttpHelper.getPairs("type", "login",
 * 		"username", "kyle", "password", "blackcoffee");
 *
 * HttpRequest request = new HttpRequest(Method.POST, new URL(
 * 		"http://example.com"))
 * 		.payload(params, "UTF-8");
 * </code>
 *
 * <b>POST Request</b>
 * <p>
 * Sends POST request with payload as raw bytes.
 * </p>
 * <code>
 * String xmlPost = "&lt;?xml version="1.0"?&gt; &lt;top&gt;&lt;/top&gt;";
 *
 * HttpRequest request = new HttpRequest(Method.POST, new URL(
 * 		"http://example.com"))
 * 		.header("Content-Type", "text/xml; charset=UTF-8")
 * 		.payload(xmlPost.getBytes("UTF-8"));
 * </code>
 * </pre>
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.HttpHelper
 * @since COMMONS 1.1.0
 */
public class HttpRequest {
    private static final Map<Integer, RequestHandler> internalCodes;

    /**
     * Logger for request.
     *
     * @since COMMONS 1.1.0
     */
    protected Logger log;

    /**
     * URL for connection.
     *
     * @since COMMONS 1.1.0
     */
    protected URL url;

    /**
     * Method for connection.
     *
     * @since COMMONS 1.1.0
     */
    protected Method method;

    /**
     * Response handler for returning custom response.
     *
     * @since COMMONS 1.1.0
     */
    protected ResponseHandler responseHandler;

    /**
     * May be null if not set.
     *
     * @since COMMONS 1.1.0
     */
    protected Proxy proxy;

    /**
     * Status codes to ignore while executing response.
     *
     * @since COMMONS 1.1.0
     */
    protected final Set<Integer> ignoreCodes;

    /**
     * Uses timeout if greater than 0. Default is -1.
     *
     * @since COMMONS 1.1.0
     */
    protected int connectTimeout = -1;

    /**
     * Uses timeout if greater than 0. Default is -1.
     *
     * @since COMMONS 1.1.0
     */
    protected int readTimeout = -1;

    /**
     * Checks whether to use internal redirect builders.
     *
     * @since COMMONS 1.1.0
     */
    protected boolean followRedirects = false;

    /**
     * Once response is found, close it if true.
     *
     * @since COMMONS 1.1.0
     */
    protected boolean close;

    /**
     * See {@link URLConnection#setUseCaches(boolean)}. Default is true.
     *
     * @since COMMONS 1.1.0
     */
    protected boolean cache = true;

    /**
     * Headers for connection.
     *
     * @since COMMONS 1.1.0
     */
    protected final Map<String, String> headerMap;

    /**
     * Cookies to set to connection.
     *
     * @since COMMONS 1.1.0
     */
    protected List<Cookie> cookies;

    /**
     * Cookies to setup for connection and process after is sent.
     *
     * @since COMMONS 1.1.0
     */
    protected CookieMap useCookies;

    /**
     * Store cookies in <code>NetworkHelper</code> CookieManager. If
     * {@link #useCookies} is not null, connection will not store.
     *
     * @since COMMONS 1.1.0
     */
    protected boolean storeCookies = true;

    /**
     * Request cookies in <code>NetworkHelper</code> CookieManager.
     *
     * @since COMMONS 1.1.0
     */
    protected boolean reqCookies = true;

    /**
     * Payload for outputStream of connection.
     *
     * @since COMMONS 1.1.0
     */
    protected byte[] payload;

    /**
     * Sets internal request handler for specific response code. To remove, set
     * <code>handler</code> null.
     *
     * @param responseCode status code to check
     * @param handler      handler to process if status code matched connection's
     * @see RequestHandler
     * @since COMMONS 1.1.0
     */
    public static void setInternalHandler(int responseCode,
                                          RequestHandler handler) {
        Integer integer = responseCode;
        if (handler == null) {
            internalCodes.remove(integer);
            LoggerFactory.getLogger(HttpRequest.class).trace(
                    "RequestHandler unregistered for {}",
                    String.valueOf(responseCode));
        } else {
            internalCodes.put(integer, handler);
            LoggerFactory.getLogger(HttpRequest.class).trace(
                    "RequestHandler {} registered for {}", handler,
                    String.valueOf(responseCode));
        }
    }

    /**
     * Instantiates a new HTTP request with method and url.
     *
     * @param method HTTP method
     * @param url    URL to request
     * @see com.krobothsoftware.commons.network.http.Method
     * @since COMMONS 1.1.0
     */
    public HttpRequest(Method method, URL url) {
        this.method = method;
        this.url = url;
        headerMap = new HashMap<String, String>();
        ignoreCodes = new HashSet<Integer>();
        log = LoggerFactory.getLogger(HttpRequest.class);
    }

    /**
     * Instantiates a new HTTP request from another.
     *
     * @param builder
     * @since COMMONS 1.1.0
     */
    public HttpRequest(HttpRequest builder) {
        this(builder, builder.method, builder.url);
    }

    /**
     * Instantiates a new HTTP request from another.
     *
     * @param builder
     * @param method  new method
     * @param url     new url
     * @since COMMONS 1.1.0
     */
    public HttpRequest(HttpRequest builder, Method method, URL url) {
        this.method = method;
        this.url = url;
        log = builder.log;
        proxy = builder.proxy;
        ignoreCodes = builder.ignoreCodes;
        connectTimeout = builder.connectTimeout;
        readTimeout = builder.readTimeout;
        followRedirects = builder.followRedirects;
        headerMap = builder.headerMap;
        cookies = builder.cookies;
        useCookies = builder.useCookies;
        payload = builder.payload;
    }

    /**
     * Resets all values to default with the exception of <code>URL</code> and
     * <code>Method</code>.
     *
     * @since COMMONS 1.1.0
     */
    public void reset() {
        responseHandler = null;
        proxy = null;
        ignoreCodes.clear();
        connectTimeout = -1;
        readTimeout = -1;
        followRedirects = false;
        close = false;
        cache = true;
        headerMap.clear();
        if (cookies != null) cookies.clear();
        useCookies = null;
        storeCookies = true;
        reqCookies = true;
        payload = null;
    }

    /**
     * Gets the url.
     *
     * @return url
     * @since COMMONS 1.1.0
     */
    public URL url() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url new url
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest url(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Gets the method.
     *
     * @return method
     * @since COMMONS 1.1.0
     */
    public Method method() {
        return method;
    }

    /**
     * Sets the method
     *
     * @param method new method
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest method(Method method) {
        this.method = method;
        return this;
    }

    /**
     * Sets response handler for custom responses when
     * {@link #execute(HttpHelper)} is called.
     *
     * @param responseHandler handler for custom responses
     * @returns request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest responseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    /**
     * Sets proxy for request.
     *
     * @param proxy the proxy
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets response code to ignore when connecting. Internally, HttpRequest
     * may handle status codes.
     *
     * @param responseCode status code to ignore
     * @return request instance
     * @see HttpRequest
     * @since COMMONS 1.1.0
     */
    public HttpRequest ignoreCode(int responseCode) {
        ignoreCodes.add(responseCode);
        return this;
    }

    /**
     * Follow redirects on 302 responses internally. Will use same request over
     * and re-send with new URL.
     *
     * @param followRedirects the follow redirects
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * Cache connection. Sets {@link URLConnection#setUseCaches(boolean)}.
     * Default value is true.
     *
     * @param useCache to use cache.
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest cache(boolean useCache) {
        this.cache = useCache;
        return this;
    }

    /**
     * After request is executed, close connection. Default is false.
     *
     * @param close close connection
     * @return request instance
     * @see com.krobothsoftware.commons.network.http.HttpResponse#close()
     * @since COMMONS 1.1.0
     */
    public HttpRequest close(boolean close) {
        this.close = close;
        return this;
    }

    /**
     * Sets connection timeout. Will override <code>HttpHelper</code> default
     * timeout.
     *
     * @param connectionTimeout connection timeout
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest connectTimeout(int connectionTimeout) {
        connectTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets read timeout. Will override <code>HttpHelper</code> default
     * timeout.
     *
     * @param readTimeout read timeout
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Adds cookie for request.
     *
     * @param cookie to request
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest put(Cookie cookie) {
        if (cookies == null) cookies = new ArrayList<Cookie>();
        cookies.add(cookie);
        return this;
    }

    /**
     * Adds list of cookies for request.
     *
     * @param cookies to request
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest put(List<Cookie> cookies) {
        if (this.cookies == null) this.cookies = new ArrayList<Cookie>();
        this.cookies.addAll(cookies);
        return this;
    }

    /**
     * Uses the cookie map for setting and storing cookies in connection. Will
     * not store cookies in <code>HttpHelper</code> CookieManager.
     *
     * @param cookies to and after request
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest use(CookieMap cookies) {
        this.useCookies = cookies;
        return this;
    }

    /**
     * Store cookies in <code>HttpHelper</code> CookieManager after
     * connection is made. True by default.
     *
     * @param store
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest storeCookies(boolean store) {
        this.storeCookies = store;
        return this;
    }

    /**
     * Request cookies in <code>HttpHelper</code> CookieManager when setting
     * up connection. True by default.
     *
     * @param request
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest requestCookies(boolean request) {
        this.reqCookies = request;
        return this;
    }

    /**
     * Sets the payload for POST and PUT {@link Method}. Content type
     * <i>application/x-www-form-urlencoded</i>.
     *
     * @param params  post params
     * @param charset the charset
     * @return request instance
     * @throws UnsupportedEncodingException {@inheritDoc}
     * @since COMMONS 1.1.0
     */
    public HttpRequest payload(List<NameValuePair> params, String charset)
            throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (NameValuePair pair : params) {
            builder.append('&').append(pair.getEncodedPair(charset));
        }
        payload = builder.substring(1).getBytes(charset);
        return this;
    }

    /**
     * Sets the payload for POST and PUT {@link Method}.
     *
     * @param payload raw bytes
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Sets header for request. Will override <code>HttpHelper</code> default
     * header.
     *
     * @param name  the name
     * @param value the value
     * @return request instance
     * @since COMMONS 1.1.0
     */
    public HttpRequest header(String name, String value) {
        headerMap.put(name, value);
        return this;
    }

    /**
     * Sends HTTP request based on request instance. <b>Connection is not
     * closed</b>.
     * <p>
     * Execute process
     * <ul>
     * <li>Call
     * {@link ConnectionListener#onRequest(HttpURLConnection, HttpRequest)}</li>
     * <li>Open connection with proxy</li>
     * <li>Set HTTP Method, connection and read timeouts, and follow redirects</li>
     * <li>Setup Cookies, Headers, and OutputStream</li>
     * <li>Open connection and get InputStream</li>
     * <li>Check ignored response codes, handle {@link RequestHandler}'s</li>
     * <li>Store Cookies</li>
     * <li>Call {@link ConnectionListener#onFinish(HttpURLConnection)}</li>
     * </ul>
     * </p
     * <p>
     * <p>
     * Responses in current package based on status code </br>First checks
     * {@link ResponseHandler#getResponse(HttpURLConnection, UnclosableInputStream, int, String)}
     * <table border="1">
     * <tr>
     * <td>3xx</td>
     * <td>ResponseRedirect.class</td>
     * </tr>
     * <tr>
     * <td>401</td>
     * <td>ResponseAuthenticate.class</td>
     * </tr>
     * <tr>
     * <td>OTHER</td>
     * <td>Response.class</td>
     * </tr>
     * </table>
     *
     * @param httpHelper http helper for connection
     * @return response depends on status code of connection
     * @throws IOException Signals that an I/O exception has occurred.
     * @since COMMONS 1.1.0
     */
    public HttpResponse execute(HttpHelper httpHelper) throws IOException {
        HttpURLConnection connection;

        log.debug("Request {} {}", method, url);

        if (proxy != null) connection = HttpHelper.openConnection(url, proxy);
        else
            connection = httpHelper.openConnection(url);

        connection.setRequestMethod(method.name());
        connection.setConnectTimeout(connectTimeout > -1 ? connectTimeout
                : httpHelper.connectTimeout);
        connection.setReadTimeout(readTimeout > -1 ? readTimeout
                : httpHelper.readTimeout);

        // Always false because redirects are handled internally.
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(cache);

        if (cookies != null) CookieManager.setCookies(connection, cookies);
        if (useCookies != null) CookieManager.setupCookies(connection,
                useCookies);
        if (reqCookies) httpHelper.cookieManager.setupCookies(connection);

        // Adds requests headers to override default ones
        Map<String, String> headers = new HashMap<String, String>(
                httpHelper.headerMap);
        headers.putAll(headerMap);
        setupHeaders(connection, headers);

        // must send onRequest before outputstream
        if (httpHelper.connListener != null) httpHelper.connListener
                .onRequest(connection, this);
        switch (method) {
            case POST:
            case PUT:
                if (payload == null) break;
                if (connection.getRequestProperty("Content-Type") == null) {
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                }
                log.debug("Writing payload: {} bytes",
                        String.valueOf(payload.length));
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(payload.length);
                OutputStream output = connection.getOutputStream();
                output.write(payload);
                output.close();
                break;
            default:
                break;
        }

        InputStream inputStream;
        int statuscode;
        try {
            connection.connect();
            inputStream = HttpHelper.getInputStream(connection);
            statuscode = connection.getResponseCode();
        } catch (IOException e) {
            statuscode = connection.getResponseCode();

			/*
             * Get error stream only if status code is 400 or greater, AND
			 * request's ignore codes do not match it.
			 */
            if (!(ignoreCodes.contains(Integer.valueOf(statuscode)))
                    && statuscode >= 400) {
                inputStream = HttpHelper.getErrorStream(connection);
            } else
                throw e;
        }

        log.debug("Response {}", connection.getResponseMessage());

		/*
         * Check internally requests handlers and process them if can.
		 */
        if (internalCodes.containsKey(statuscode)
                && !ignoreCodes.contains(Integer.valueOf(statuscode))) {
            HttpRequest newBuilder = internalCodes.get(
                    statuscode).getRequest(statuscode, this,
                    connection);
            if (newBuilder != null) return newBuilder.execute(httpHelper);
        }

        if (useCookies != null) useCookies.putCookieList(
                CookieManager.getCookies(connection), true);
        else if (storeCookies) httpHelper.cookieManager.putCookieList(
                CookieManager.getCookies(connection), true);

        if (httpHelper.connListener != null) httpHelper.connListener
                .onFinish(connection);

        HttpResponse response = getResponse(responseHandler,
                connection, inputStream, statuscode);
        if (close) response.close();
        return response;

    }

    /**
     * Returns string in format "[method] : [url]".
     *
     * @since COMMONS 1.1.0
     */
    @Override
    public String toString() {
        return method + " : " + url.toString();
    }

    private void setupHeaders(HttpURLConnection connection, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            log.trace("Header {}: {}", entry.getKey(), entry.getValue());
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private static HttpResponse getResponse(ResponseHandler handler,
                                            HttpURLConnection connection, InputStream inputStream,
                                            int statuscode) {
        UnclosableInputStream stream = new UnclosableInputStream(inputStream);
        String charset = HttpHelper.getCharset(connection);
        HttpResponse found = null;
        if (handler != null) found = handler.getResponse(connection, stream,
                statuscode, charset);
        if (found != null) return found;

        if (statuscode / 100 == 3) return new HttpResponseRedirect(connection,
                stream, statuscode, charset);
        switch (statuscode) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return new HttpResponseAuthenticate(connection, stream,
                        statuscode, charset);
            default:
                return new HttpResponse(connection, stream, statuscode, charset);
        }
    }

    static {
        internalCodes = new HashMap<Integer, RequestHandler>(2);
        RedirectHandler redirectHandler = new RedirectHandler();
        internalCodes.put(301, redirectHandler);
        internalCodes.put(302, redirectHandler);
    }
}
