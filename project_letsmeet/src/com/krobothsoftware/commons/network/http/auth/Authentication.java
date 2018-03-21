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
import com.krobothsoftware.commons.network.http.HttpResponseAuthenticate;

import java.io.IOException;

/**
 * Authentication is the base class for authorizations.
 * <p>
 * There are two steps to an Authentication. Prior to executing the connection,
 * {@link #setup(HttpRequestAuthenticate)} is called to set up credentials.
 * After the connection has sent and if the status code is 401(Unauthorized),
 * {@link #handshake(HttpRequestAuthenticate,
 * com.krobothsoftware.commons.network.http.HttpResponseAuthenticate,
 * com.krobothsoftware.commons.network.http.HttpHelper)} is
 * then called.
 * </p>
 * <p>
 * Authenticating after 401 response may only be called if
 * {@link #handshakeSupported()} is true.
 * </p>
 *
 * @author Kyle Kroboth
 * @see AuthenticationManager
 * @since COMMONS 1.0
 */
public abstract class Authentication {

    /**
     * Constant header for Authorization
     *
     * @since COMMONS 1.0.1
     */
    static final String HEADER_AUTHORIZATION = "Authorization";

    private final String schemeName;

    /*
     * Empty constructor
     * @since COMMONS 1.0
     */
    protected Authentication(String schemeName) {
        this.schemeName = schemeName;
    }

    /**
     * Gets scheme name of authentication. Used to match in
     * {@link com.krobothsoftware.commons.network.http.auth.AuthScope}
     *
     * @return
     * @since 1.0.1
     */
    public String getSchemeName() {
        return schemeName;
    }

    /**
     * Called when connection needs to be set up for authentication.
     *
     * @param request for setup
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     * @since COMMONS 1.0
     */
    public abstract void setup(HttpRequestAuthenticate request)
            throws IOException;

    /**
     * Called after request was executed and response is 401.
     *
     * @param request  for authenticating.
     * @param response for authenticating. Must close.
     * @return response after authenticating
     * @throws IOException Signals that an I/O exception has occurred.
     * @see #handshakeSupported()
     * @since COMMONS 1.0
     */
    public abstract void handshake(HttpRequestAuthenticate request,
                                   HttpResponseAuthenticate response, HttpHelper httpHelper) throws IOException;

    /**
     * If handshake is supported, {@link #handshake(HttpRequestAuthenticate,
     * com.krobothsoftware.commons.network.http.HttpResponseAuthenticate,
     * com.krobothsoftware.commons.network.http.HttpHelper)}
     * will be called after the request was executed. Further authentication occurs there.
     *
     * @return true, if supported
     * @see #handshake(HttpRequestAuthenticate,
     * com.krobothsoftware.commons.network.http.HttpResponseAuthenticate,
     * com.krobothsoftware.commons.network.http.HttpHelper)
     * @since COMMONS 1.0
     */
    public boolean handshakeSupported() {
        return false;
    }

    /**
     * Resets any info in Authentication object except credentials.
     *
     * @since COMMONS 1.0
     */
    public abstract void reset();

}
