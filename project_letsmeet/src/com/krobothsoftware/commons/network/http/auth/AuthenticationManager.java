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

import com.krobothsoftware.commons.network.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;

/**
 * Manager for authenticating connections with {@link AuthScope}.
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.auth.Authentication
 * @since COMMONS 1.0
 */
public class AuthenticationManager {
    protected final HashMap<AuthScope, Authentication> authMap;
    protected final Logger log;

    /**
     * Instantiates a new auth manager.
     *
     * @since COMMONS 1.0
     */
    public AuthenticationManager() {
        log = LoggerFactory.getLogger(AuthenticationManager.class);
        authMap = new HashMap<AuthScope, Authentication>();
    }

    public void add(AuthScope scope, Authentication authentication) {
        authMap.put(scope, authentication);
    }

    public Authentication remove(AuthScope scope) {
        return authMap.remove(scope);
    }

    /**
     * Gets Authentication by checking {@link com.krobothsoftware.commons.network.http.auth.AuthScope} with
     * {@link HttpRequest} URL.
     *
     * @param request url to check with scopes
     * @return found Authentication or null
     * @since COMMONS 1.0
     */
    public Authentication getAuthentication(URL url, String realm, String scheme) {
        String host = url.getHost();
        int port = url.getPort();
        for (AuthScope scope : authMap.keySet()) {
            if (scope.matches(host, realm, scheme, port))
                return authMap.get(scope);
        }

        return null;
    }

    /**
     * Clears map holding authentications.
     *
     * @since COMMONS 1.0
     */
    public void clear() {
        authMap.clear();
    }
}
