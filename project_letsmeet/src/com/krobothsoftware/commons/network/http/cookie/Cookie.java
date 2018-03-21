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

package com.krobothsoftware.commons.network.http.cookie;

import com.krobothsoftware.commons.util.StringUtils;
import com.krobothsoftware.commons.util.ThreadSafeDateUtil;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Object holding information on HTTP Cookie and methods to build them.
 * <p/>
 * <p>
 * <p/>
 * </p>
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.cookie.CookieList
 * @see com.krobothsoftware.commons.network.http.cookie.CookieMap
 * @since COMMONS 1.0
 */
public class Cookie implements Serializable {
    private static final long serialVersionUID = -7856076968023905033L;

    private static final String[] DATE_FORMATS = {
            "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
            "EEE',' dd MMM yyyy HH:mm:ss 'GMT'"};
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /**
     * Locale.US week data. First day of week and minimum days in week.
     */
    private static final int[] WEEK_DATA = {1, 1};

    private final String name;
    private String value;
    private String domain;
    private String path;
    private long maxage;
    private final long created;
    private boolean secure;
    private boolean http;

    /**
     * Instantiates a new cookie with name and value. Max age will have the
     * value of -1.
     *
     * @param name  name of cookie
     * @param value value of cookie
     * @since COMMONS 1.0
     */
    public Cookie(String name, String value) {
        this(null, name, value);
    }

    /**
     * Instantiates a new cookie with domain, name, and value. Max age will have
     * the value of -1.
     *
     * @param domain cookie domain
     * @param name   name of cookie
     * @param value  value of cookie
     * @since COMMONS 1.0
     */
    public Cookie(String domain, String name, String value) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        path = "/";
        maxage = -1;
        created = System.currentTimeMillis() / 1000;
        secure = false;
        http = false;
    }

    Cookie(Builder builder) {
        name = builder.name;
        value = builder.value;
        domain = builder.domain;
        path = builder.path;
        maxage = builder.maxage;
        created = builder.created;
        secure = builder.secure;
        http = builder.http;
    }

    /**
     * Gets cookie name.
     *
     * @return name
     * @since COMMONS 1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Gets cookie value.
     *
     * @return value
     * @since COMMONS 1.0
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets cookie value.
     *
     * @param value
     * @since COMMONS 1.0
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets cookie domain.
     *
     * @return domain
     * @since COMMONS 1.0
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets cookie domain.
     *
     * @param domain
     * @since COMMONS 1.0
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Gets cookie path.
     *
     * @return path
     * @since COMMONS 1.0
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets cookie path.
     *
     * @param path
     * @since COMMONS 1.0
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Checks if cookie has expired using current time. If max age is 0 or
     * greater, cookie has expired.
     *
     * @return true, if cookie has expired
     * @see #isExpired(long)
     * @since COMMONS 1.0
     */
    public boolean isExpired() {
        return isExpired(System.currentTimeMillis() / 1000);
    }

    /**
     * Checks if cookie has expired from giving <code>date</code> in seconds. If
     * max age is 0 or greater, cookie has expired.
     *
     * @param date time in seconds
     * @return true, if cookie has expired
     * @since COMMONS 1.1.0
     */
    public boolean isExpired(long date) {
        return (date - created) > maxage;
    }

    /**
     * Gets creation time of cookie
     *
     * @return milliseconds of creation time
     * @since COMMONS 1.1.0
     */
    public long whenCreated() {
        return created * 1000;
    }

    /**
     * Gets cookie max age.
     *
     * @return expires
     * @since COMMONS 1.0
     */
    public long getMaxage() {
        return maxage;
    }

    /**
     * Sets cookie max age.
     *
     * @param expire max age for cookie
     * @since COMMONS 1.0
     */
    public void setMaxage(long expire) {
        this.maxage = expire;
    }

    /**
     * Checks if cookie is session only
     *
     * @return true, if cookies session only
     * @since COMMONS 1.0
     */
    public boolean isSession() {
        return maxage == -1;
    }

    /**
     * Gets cookie secure.
     *
     * @return is secure
     * @since COMMONS 1.0
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets cookie secure.
     *
     * @param secure
     * @since COMMONS 1.0
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Gets cookie http only.
     *
     * @return is http only
     * @since COMMONS 1.0
     */
    public boolean isHttpOnly() {
        return http;
    }

    /**
     * Sets http only.
     *
     * @param httpOnly
     * @since COMMONS 1.0
     */
    public void setHttpOnly(boolean httpOnly) {
        this.http = httpOnly;
    }

    /**
     * Gets cookie string in format <code>Name=Value</code>.
     *
     * @return cookie header format
     * @since COMMONS 1.0
     */
    public String getCookieString() {
        return name + "=" + value;
    }

    /**
     * Returns string in format "[name] [domain]".
     *
     * @since COMMONS 1.0
     */
    @Override
    public final String toString() {
        return String.format("%s %s", name, domain);
    }

    /**
     * Computes hash code from domain, name, and path.
     *
     * @since COMMONS 1.0
     */
    @Override
    public final int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (domain == null ? 0 : domain.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (path == null ? 0 : path.hashCode());
        return result;
    }

    /**
     * Checks domain, name, value, and regular check statements.
     *
     * @since COMMONS 1.0
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Cookie)) return false;
        Cookie other = (Cookie) obj;
        if (domain == null) {
            if (other.domain != null) return false;
        } else if (!domain.equals(other.domain)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (path == null) {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        return true;
    }

    /**
     * Checks if paths match. They match if they both equal each other, are not
     * null, or <code>urlPath</code> start with <code>cookiePath</code>.
     *
     * @param urlPath    path for url
     * @param cookiePath path for cookie
     * @return if they match
     * @since COMMONS 1.1.0
     */
    public static boolean matchesPath(String urlPath, String cookiePath) {
        if (urlPath.equals(cookiePath)) return true;
        if (cookiePath == null) return false;
        return urlPath.startsWith(cookiePath);

    }

    /**
     * Build and parse cookie.
     *
     * @param cookieString cookie header string
     * @return built cookie
     * @since COMMONS 1.0
     */
    public static Cookie parseCookie(URL url, String cookieString) {
        Cookie.Builder builder = new Cookie.Builder().setCreated(
                System.currentTimeMillis() / 1000).setMaxAge(-1);

        StringTokenizer tokenizer = new StringTokenizer(cookieString, ";");

        // get name and value
        String token = StringUtils.trim(tokenizer.nextToken());
        int index = token.indexOf('=');
        if (index == -1) throw new IllegalArgumentException(
                "Invalid name value pair in cookie");
        builder.name = token.substring(0, index);
        builder.value = token.substring(index + 1);

        while (tokenizer.hasMoreTokens()) {
            token = StringUtils.trim(tokenizer.nextToken());

            // check if it's a boolean
            index = token.indexOf('=');
            String name;
            String value = null;
            if (index != -1) {
                name = token.substring(0, index);
                value = token.substring(index + 1);
                if (value.length() == 0) value = null;
            } else {
                name = token;
            }
            setValue(builder, name, value);

        }

        if (builder.domain == null) builder.domain = url.getHost();
        if (builder.path == null) {
            // use url's path
            String path = url.getPath();
            if (path.isEmpty()) path = "/";
            else {
                int slashIndex = path.lastIndexOf('/');
                if (slashIndex > 0) path = path.substring(0, slashIndex);
                else
                    path = "/";
            }
            builder.path = path;
        }

        return builder.build();
    }

    private static void setValue(Builder builder, String name, String value) {
        name = name.toLowerCase(Locale.ENGLISH);
        if (name.equals("domain")) {
            builder.domain = value;
        } else if (name.equals("path")) {
            builder.path = value;
        } else if (name.equals("secure")) {
            builder.secure = true;
        } else if (name.equals("httponly")) {
            builder.http = true;
        } else if (name.equals("expires")) {
            // max-age has precedence over expires
            if (builder.maxage != -1) return;

            for (String format : DATE_FORMATS) {
                try {
                    Date date = ThreadSafeDateUtil.parse(format, value, GMT,
                            WEEK_DATA);
                    builder.maxage = (date.getTime() / 1000) - builder.created;
                    return;
                } catch (ParseException e) {
                    // ignore. Will set max age to 0 if both formats don't
                    // succeed.
                    builder.maxage = 0;
                }
            }
        } else if (name.equals("max-age")) {
            builder.maxage = Long.parseLong(value);
        }
    }

    /**
     * Builder for cookies.
     *
     * @author Kyle Kroboth
     * @since COMMONS 1.0
     */
    public static class Builder {
        String name;
        String value;
        String domain;
        String path;
        long created;
        long maxage;
        boolean secure;
        boolean http;

        public Builder(String data) {
            name = data.substring(0, data.indexOf("="));
            value = data.substring(data.indexOf("=") + 1, data.length() - 1);
        }

        public Builder() {

        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setCreated(long created) {
            this.created = created;
            return this;
        }

        public Builder setMaxAge(long expire) {
            this.maxage = expire;
            return this;
        }

        public Builder setSecure(boolean isSecure) {
            this.secure = isSecure;
            return this;
        }

        public Builder setHttpOnly(boolean isHttpOnly) {
            this.http = isHttpOnly;
            return this;
        }

        public Cookie build() {
            if (domain == null) throw new IllegalStateException(
                    "'domain' may not be null");
            return new Cookie(this);
        }

    }

}
