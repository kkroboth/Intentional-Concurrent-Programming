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

import com.krobothsoftware.commons.network.http.*;

import java.io.IOException;
import java.net.URL;

/**
 * Builder for authenticating HTTP connections.
 * <p/>
 * <p>
 * There are two ways of requesting an authorization on connection. By setting
 * Authentication,
 * {@link #HttpRequestAuthenticate(com.krobothsoftware.commons.network.http.Method, URL, Authentication)}, or check
 * with <code>NetworkHelper</code> AuthenticationManager,
 * {@link #HttpRequestAuthenticate(com.krobothsoftware.commons.network.http.Method, URL)}.
 * </p>
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.HttpRequest
 * @since COMMONS 1.0
 */
public class HttpRequestAuthenticate extends HttpRequest {
    private final Authentication auth;
    private final String realm;
    private final String scheme;
    private int retry = 1;

    /**
     * Instantiates a new builder with Authorization.
     *
     * @param method HTTP method
     * @param url    URL to request
     * @param auth   to use for connection
     * @since COMMONS 1.0
     */
    public HttpRequestAuthenticate(Method method, URL url, Authentication auth) {
        super(method, url);
        this.auth = auth;
        this.realm = null;
        this.scheme = null;
    }

    /**
     * Instantiates a new builder and uses <code>NetworkHelper</code>
     * {@link AuthenticationManager}.
     *
     * @param method HTTP method
     * @param url    URL to request
     * @param realm  to check with scopes
     * @since COMMONS 1.0
     */
    public HttpRequestAuthenticate(Method method, URL url, String realm, String scheme) {
        super(method, url);
        auth = null;
        this.realm = realm;
        this.scheme = scheme;
    }

    /**
     * Instantiates a new builder and uses <code>NetworkHelper</code>
     * {@link AuthenticationManager}
     *
     * @param method
     * @param url
     * @since COMMONS 1.0
     */
    public HttpRequestAuthenticate(Method method, URL url) {
        this(method, url, null, null);
    }

    /**
     * Instantiates a new request builder from another.
     *
     * @param builder
     * @since COMMONS 1.0
     */
    public HttpRequestAuthenticate(HttpRequestAuthenticate builder) {
        super(builder);
        auth = builder.auth;
        realm = builder.realm;
        scheme = builder.scheme;
    }

    /**
     * Limit for retrying connection if 401 status code is returned and
     * Authentication supports handshake. Resets Authentication if needed.
     * Default retry limit is 1.
     *
     * @param count number of retries
     * @return request builder
     * @throws IllegalArgumentException if count is less than zero
     * @see com.krobothsoftware.commons.network.http.auth.Authentication#reset()
     * @see com.krobothsoftware.commons.network.http.auth.Authentication#handshakeSupported()
     * @since COMMONS 1.0
     */
    public HttpRequestAuthenticate retryLimit(int count) {
        if (count < 0) throw new IllegalArgumentException(
                "Count must be above zero");
        this.retry = count;
        return this;
    }

    /**
     * Sends HTTP request based on request builder with authorization.
     * Connection is not closed.
     *
     * @see com.krobothsoftware.commons.network.http.HttpRequest#execute(com.krobothsoftware.commons.network.http.HttpHelper)
     * @since COMMONS 1.0
     */
    @Override
    public HttpResponse execute(HttpHelper httpHelper) throws IOException {
        Authentication auth = this.auth;
        if (auth == null) auth = httpHelper.getAuthenticationManager()
                .getAuthentication(url(), realm, scheme);
        if (auth == null) {
            log.warn("[Authenticate] no authentication found for {}. Calling super.execute()", this);
            return super.execute(httpHelper);
        }

        // setup authentication
        auth.setup(this);
        HttpResponse response = super.execute(httpHelper);
        if (response instanceof HttpResponseAuthenticate
                && auth.handshakeSupported()) {
            // handshake connection
            // index starts at negative one for normal connection
            for (int i = -1; i < retry; i++) {
                auth.handshake(this,
                        (HttpResponseAuthenticate) response, httpHelper);
                response = super.execute(httpHelper);
                if (!(response instanceof HttpResponseAuthenticate)) break;
                auth.reset();
            }
        }

        return response;


    }
}
