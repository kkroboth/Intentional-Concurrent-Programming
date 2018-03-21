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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Internal redirect request.
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.1.0
 */
class HttpRequestBuilderRedirect extends HttpRequest {
    private int redirect = 0;

    public HttpRequestBuilderRedirect(HttpRequest builder, URL url) {
        super(builder, builder.method, url);
        if (builder instanceof HttpRequestBuilderRedirect) {
            redirect = ((HttpRequestBuilderRedirect) builder).redirect;
        }
    }

    @Override
    public HttpResponse execute(HttpHelper httpHelper) throws IOException {
        if (++redirect > httpHelper.maxRedirects) throw new IOException(
                "Request was redirected too many times: " + redirect);
        return super.execute(httpHelper);
    }

    static class RedirectHandler implements RequestHandler {

        @Override
        public HttpRequest getRequest(int statuscode, HttpRequest builder,
                                      HttpURLConnection connection) throws IOException {
            // respect follow redirects option
            if (statuscode == 302 && !builder.followRedirects) return null;

            builder.log.debug("Internally handled redirect");
            String location = connection.getHeaderField("Location");
            URL url = new URL(builder.url(), location);
            connection.disconnect();

            // only use one instance of redirect builder
            HttpRequest newBuilder;
            if (builder instanceof HttpRequestBuilderRedirect) {
                newBuilder = builder;
                newBuilder.url(url);
            } else {
                newBuilder = new HttpRequestBuilderRedirect(builder, url);
                // 302 and 301 requests can't be POST, PUT, or DELETE
                switch (builder.method) {
                    case POST:
                    case PUT:
                    case DELETE:
                        newBuilder.method = Method.GET;
                        break;
                    default: // nothing
                }
            }

            return newBuilder;
        }

    }

}
