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

import com.krobothsoftware.commons.util.UnclosableInputStream;

import java.net.HttpURLConnection;

/**
 * Handle <code>Responses</code> with every
 * {@link HttpRequest#execute(HttpHelper)}.
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.0
 */
public interface ResponseHandler {

    /**
     * Get correct {@link HttpResponse} for given results after
     * {@link HttpRequest} executes.
     *
     * @param connection connection for response
     * @param input      stream for response
     * @param status     response code for response
     * @param charset    charset for response
     * @return custom response or null
     * @see com.krobothsoftware.commons.network.http.HttpRequest#execute(HttpHelper)
     * @since COMMONS 1.0
     */
    HttpResponse getResponse(HttpURLConnection connection,
                             UnclosableInputStream input, int status, String charset);

}
