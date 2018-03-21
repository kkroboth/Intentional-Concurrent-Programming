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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Response holder from {@link HttpRequest#execute(HttpHelper)} if
 * response code is a redirection(3xx).
 * <p/>
 * <p>
 * Use {@link #getLocation()} to get redirection URL.
 * </p>
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.0
 */
public class HttpResponseRedirect extends HttpResponse {
    private final String location;
    private final URL base;

    /**
     * Instantiates a new response with results from connection and retrieves
     * redirect URL through header <code>Location</code>.
     *
     * @param connection
     * @param input      non-null stream
     * @param status     response code
     * @param charset    charset of connection
     * @since COMMONS 1.0
     */
    public HttpResponseRedirect(HttpURLConnection connection,
                                UnclosableInputStream input, int status, String charset) {
        super(connection, input, status, charset);
        location = connection.getHeaderField("Location");
        base = connection.getURL();
    }

    /**
     * Gets header <code>Location</code>.
     *
     * @return location header, or null if not found
     * @since COMMONS 1.0
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets built redirection URL.
     *
     * @return redirection url appended with location header
     * @throws MalformedURLException
     * @since COMMONS 1.0.1
     */
    public URL getRedirectionUrl() throws MalformedURLException {
        return new URL(base, location);
    }

    /**
     * Returns string in format
     * "ResponseRedirect [url] : [status-code] : redirect[url]".
     *
     * @since COMMONS 1.1.0
     */
    @Override
    public String toString() {
        return String.format("ResponseRedirect %s : %s : redirect[%s]", conn
                .getURL().toString(), String.valueOf(status), location);
    }

}

