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

/**
 * Scope for {@link Authentication} in {@link AuthenticationManager}.
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.0
 */
public class AuthScope {
    public static final String ANY_REALM = null;
    public static final int ANY_PORT = -1;
    public static final String ANY_SCHEME = null;

    final String host;
    final String realm;
    final int port;
    final String scheme;

    public AuthScope(String host, String realm, String scheme,
                     int port) {
        this.host = host;
        this.realm = realm;
        this.scheme = scheme;
        this.port = AuthScope.ANY_PORT;
    }


    public AuthScope(String host, String realm, String scheme) {
        this(host, realm, scheme, ANY_PORT);
    }

    /**
     * Creates new scope with realm and host.
     *
     * @param host  connection host to check
     * @param realm auth realm to check
     * @since COMMONS 1.1.0
     */
    public AuthScope(String host, String realm) {
        this(host, realm, ANY_SCHEME, ANY_PORT);
    }

    /**
     * Creates new scope with host only.
     *
     * @param host connection host to check
     * @since COMMONS 1.0
     */
    public AuthScope(String host) {
        this(host, ANY_REALM, ANY_SCHEME, ANY_PORT);
    }

    /**
     * Gets host of Authentication.
     *
     * @return host
     * @since COMMONS 1.0
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets realm of Authentication.
     *
     * @return realm
     * @since COMMONS 1.0
     */
    public String getRealm() {
        return realm;
    }

    public String getScheme() {
        return scheme;
    }

    public int getPort() {
        return port;
    }

    public boolean matches(String host, String realm, String scheme, int port) {
        if (!this.host.equalsIgnoreCase(host)) return false;
        if (!this.realm.equals(ANY_REALM) && !this.realm.equals(realm)) return false;
        if (!this.scheme.equals(ANY_SCHEME) && !this.scheme.equals(scheme)) return false;
        return !(this.port != ANY_PORT && this.port != port);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthScope)) return false;

        AuthScope authScope = (AuthScope) o;

        if (port != authScope.port) return false;
        if (host != null ? !host.equals(authScope.host) : authScope.host != null) return false;
        if (realm != null ? !realm.equals(authScope.realm) : authScope.realm != null) return false;
        return !(scheme != null ? !scheme.equals(authScope.scheme) : authScope.scheme != null);

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (realm != null ? realm.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
        return result;
    }
}
