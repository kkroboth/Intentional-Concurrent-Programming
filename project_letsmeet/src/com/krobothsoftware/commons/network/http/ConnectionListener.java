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

import java.net.HttpURLConnection;

/**
 * Listens for opening and finishing connection when
 * {@link com.krobothsoftware.commons.network.http.HttpRequest#execute(com.krobothsoftware.commons.network.http.HttpHelper)}
 * is called.
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.HttpHelper#setConnectionListener(ConnectionListener)
 * @since COMMONS 1.0
 */
public interface ConnectionListener {

    /**
     * Called before the connection is sent.
     *
     * @param connection url connection
     * @param builder    builder of connection
     * @since COMMONS 1.0
     */
    void onRequest(HttpURLConnection connection, HttpRequest builder);

    /**
     * Called after connection is sent and handled.
     *
     * @param connection before connection is put into Response
     * @since COMMONS 1.0
     */
    void onFinish(HttpURLConnection connection);

}
