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


/**
 * Supported HTTP methods.
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.HttpRequest#RequestBuilder(Method,
 *      java.net.URL)
 * @since COMMONS 1.0
 */
public enum Method {

    /**
     * HTTP GET.
     *
     * @since COMMONS 1.0
     */
    GET,

    /**
     * HTTP POST.
     *
     * @since COMMONS 1.0
     */
    POST,

    /**
     * HTTP HEAD.
     *
     * @since COMMONS 1.0
     */
    HEAD,

    /**
     * HTTP PUT.
     *
     * @since COMMONS 1.0
     */
    PUT,

    /**
     * HTTP OPTIONS.
     *
     * @since COMMONS 1.0
     */
    OPTIONS,

    /**
     * HTTP DELETE.
     *
     * @since COMMONS 1.0
     */
    DELETE,

    /**
     * HTTP TRACE.
     *
     * @since COMMONS 1.0
     */
    TRACE
}
