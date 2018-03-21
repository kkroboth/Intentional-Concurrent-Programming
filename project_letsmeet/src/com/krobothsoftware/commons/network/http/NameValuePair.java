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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Class for holding Name-Value pairs.
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.HttpHelper#getPairs(String...)
 * @since COMMONS 1.0
 */
public class NameValuePair implements Serializable {
    private static final long serialVersionUID = -1049649389075000405L;
    private final String name;
    private final String value;

    /**
     * Creates new Pair with name and value.
     *
     * @param name
     * @param value
     * @since COMMONS 1.0
     */
    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets pair in format <code>Name=Value</code>.
     *
     * @return pair in format
     * @since COMMONS 1.0
     */
    public String getPair() {
        return getPair(name, value);
    }

    /**
     * Gets pair in format <code>Name=Value</code>.
     *
     * @param name
     * @param value
     * @return pair
     * @since COMMONS 1.1.0
     */
    public static String getPair(String name, String value) {
        return name + "=" + value;
    }

    /**
     * Gets the encoded pair from specified charset. <code>Name=Value</code>
     * Only Value is encoded.
     *
     * @param charset for encoding
     * @return pair with value encoded
     * @throws UnsupportedEncodingException {@inheritDoc}
     * @since COMMONS 1.0
     */
    public String getEncodedPair(String charset)
            throws UnsupportedEncodingException {
        return getEncodedPair(name, value, charset);
    }

    /**
     * Gets the encoded pair from specified charset. <code>Name=Value</code>
     * Only Value is encoded.
     *
     * @param name
     * @param value
     * @param charset for encoding
     * @return pair with value encoded
     * @throws UnsupportedEncodingException {@inheritDoc}
     * @since 1.0.2
     */
    public static String getEncodedPair(String name, String value,
                                        String charset) throws UnsupportedEncodingException {
        return name + "=" + URLEncoder.encode(value, charset);
    }

    /**
     * Gets pair where value is in quotes. <code>Name="Value"</code>
     *
     * @return pair with value in quotes
     * @since COMMONS 1.0
     */
    public String getQuotedPair() {
        return getQuotedPair(name, value);
    }

    /**
     * Gets pair where value is in quotes. <code>Name="Value"</code>.
     *
     * @param name
     * @param value
     * @return pair with value in quotes
     * @since COMMONS 1.1.0
     */
    public static String getQuotedPair(String name, String value) {
        return name + "=\"" + value + "\"";
    }

    /**
     * Gets pair name.
     *
     * @return name
     * @since COMMONS 1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Gets pair value.
     *
     * @return value
     * @since COMMONS 1.0
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns string in format "NameValuePair [name='name'], value='value']".
     *
     * @since COMMONS 1.0
     */
    @Override
    public final String toString() {
        return "NameValuePair [name=" + name + ", value=" + value + "]";
    }

    /**
     * Computes hash from name and value.
     *
     * @since COMMONS 1.0
     */
    @Override
    public final int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    /**
     * Checks name, value, and regular check statements.
     *
     * @since COMMONS 1.0
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof NameValuePair)) return false;
        NameValuePair other = (NameValuePair) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (value == null) {
            if (other.value != null) return false;
        } else if (!value.equals(other.value)) return false;
        return true;
    }

}
