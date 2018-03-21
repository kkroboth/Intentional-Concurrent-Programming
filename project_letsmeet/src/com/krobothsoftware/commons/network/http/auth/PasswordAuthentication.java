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
 * Created by Kyle on 8/26/2014.
 */
public abstract class PasswordAuthentication extends Authentication {
    /**
     * Username.
     *
     * @since COMMONS 1.0.1
     */
    protected String username;

    /**
     * Password.
     *
     * @since COMMONS 1.0.1
     */
    protected char[] password;

    /**
     * Instantiates a new authorization with username and password.
     *
     * @param username
     * @param password in char array
     * @since COMMONS 1.0.1
     */
    public PasswordAuthentication(String schemeName, String username, char[] password) {
        super(schemeName);
        this.username = username;
        this.password = password;
    }

}
