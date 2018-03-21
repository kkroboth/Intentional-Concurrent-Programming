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
import com.krobothsoftware.commons.util.Base64;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by Kyle on 8/26/2014.
 */
public class BasicAuthentication extends PasswordAuthentication {
    public static final String SCHEME_NAME = "Basic";

    public BasicAuthentication(String username, char[] password) {
        super(SCHEME_NAME, username, password);
    }

    @Override
    public void setup(HttpRequestAuthenticate request) throws IOException {
        byte[] value = new byte[username.length() + password.length + 1];
        System.arraycopy(username.getBytes("UTF-8"), 0, value, 0, username.length());
        value[username.length()] = ':';
        System.arraycopy(Charset.forName("UTF-8").encode(CharBuffer.wrap(password)).array()
                , 0, value, username.length() + 1, password.length);
        request.header("Authorization", "Basic " + Base64.encodeToString(value, false));
    }

    @Override
    public void handshake(HttpRequestAuthenticate request, HttpResponseAuthenticate response, HttpHelper httpHelper) throws IOException {
        // no op
    }

    @Override
    public void reset() {
        // no op
    }

    @Override
    public boolean handshakeSupported() {
        return false;
    }
}
