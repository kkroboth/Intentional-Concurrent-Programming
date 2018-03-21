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

import com.krobothsoftware.commons.network.http.HttpHelper;
import com.krobothsoftware.commons.util.StringUtils;

import java.io.Serializable;
import java.net.HttpCookie;
import java.net.URLConnection;
import java.util.*;

/**
 * Stores and sets up HTTP Cookies in {@link java.net.HttpURLConnection}. Uses
 * {@link #CookieManager()} for holding cookies in manager.
 * <p/>
 * <p>
 * Store cookies with {@link #putCookie(Cookie, boolean)} or
 * {@link #putCookieList(List, boolean)}. Boolean option to override cookie.
 * Call {@link #purgeExpired(boolean)} to remove expired and session cookies,
 * only if boolean value is true.
 * </p>
 * <p/>
 * <p>
 * Use {@link #getCookies(HttpURLConnection)} to retrieve all Cookies from
 * <code>Set-Cookie</code> headers.
 * <p/>
 * <p/>
 * <p>
 * {@link #setupCookies(HttpURLConnection)} and
 * {@link #setupCookies(HttpURLConnection, CookieMap)} will setup
 * <code>HttpURLConnection</code> cookie header from <code>CookieMap</code> for
 * URL's domain. Both methods will check if cookie has expired and make sure it
 * can be set for connection's properties.
 * </p>
 * <p/>
 * <p>
 * {@link #setCookies(HttpURLConnection, List)} setups the cookie header like
 * the above methods, but doesn't check if they are expired, or matches
 * connection's properties. Will create a new Cookie header.
 * </p>
 *
 * @author Kyle Kroboth
 * @see com.krobothsoftware.commons.network.http.cookie.CookieList
 * @since COMMONS 1.1.0
 */
public class CookieManager implements Serializable {
    private static final long serialVersionUID = -8278470310322638902L;

    /**
     * Sorts Cookies from largest path to smallest <b>only</b> if two cookies
     * have the same name.
     *
     * @since COMMONS 1.1.0
     */
    public static final Comparator<Cookie> COOKIE_PATH_ORDER = new CookieSortComparator();

    private final CookieMap cookieMap;

    /**
     * Instantiates a new cookie manager.
     *
     * @since COMMONS 1.1.0
     */
    public CookieManager() {
        cookieMap = new CookieMap();
    }

    /**
     * Gets manager cookie map.
     *
     * @return unmodifiable map of cookies
     * @since COMMONS 1.1.0
     */
    public Map<String, CookieList> getCookieMap() {
        return Collections.unmodifiableMap(cookieMap);
    }

    /**
     * Helper method for {@link CookieMap#getDomains()}.
     *
     * @return unmodifiable set of domains
     * @since COMMONS 1.1.0
     */
    public Set<String> getDomains() {
        return cookieMap.getDomains();
    }

    /**
     * Gets the {@link CookieList} for given domain.
     *
     * @param domain domain for list
     * @return an immutable or empty list of cookies
     * @see com.krobothsoftware.commons.network.http.cookie.CookieList
     * @since COMMONS 1.1.0
     */
    public List<Cookie> getCookieList(String domain) {
        List<Cookie> list = cookieMap.get(domain);
        return (list != null) ? Collections.unmodifiableList(list)
                : Collections.<Cookie>emptyList();
    }

    /**
     * Retrieves accepted cookie domains for giving host. Uses
     * <code>CookieMap</code> instance domains.
     *
     * @param host to check with domains
     * @see #getDomainsForHost(String, List)
     * @since COMMONS 1.1.0
     */
    public List<String> getDomainsForHost(String host) {
        return getDomainsForHost(host, cookieMap.getDomains());
    }

    /**
     * Get the cookie for domain and name.
     *
     * @param domain domain of cookie
     * @param name   name of cookie
     * @return found cookie, or null
     * @since COMMONS 1.1.0
     */
    public Cookie getCookie(String domain, String name) {
        return cookieMap.getCookie(domain, name);
    }

    /**
     * Removes cookie in <code>domain</code> for given <code>name</code>.
     *
     * @param name the name
     * @return true, if successful
     * @since COMMONS 1.1.0
     */
    public boolean removeCookie(String domain, String name) {
        return cookieMap.removeCookie(domain, name);
    }

    /**
     * Clear cookie map.
     *
     * @since COMMONS 1.1.0
     */
    public void clear() {
        cookieMap.clear();
    }

    /**
     * Removes expired cookies using current time.
     *
     * @param session removes session cookies if true
     * @return if cookie map was changed
     * @since COMMONS 1.1.0
     */
    public boolean purgeExpired(boolean session) {
        return cookieMap.purgeExpired(System.currentTimeMillis() / 1000,
                session);
    }

    /**
     * Removes expired cookies giving <code>date</code> in seconds.
     *
     * @param session removes session cookies if true
     * @return COMMONS 1.1.0
     */
    public boolean purgeExpired(long date, boolean session) {
        return cookieMap.purgeExpired(date, session);
    }

    /**
     * Stores given cookies in list.
     *
     * @param cookieList {@link Cookie} list
     * @param overwrite  remove old cookie if present
     * @since COMMONS 1.1.0
     */
    public void putCookieList(List<Cookie> cookieList, boolean overwrite) {
        cookieMap.putCookieList(cookieList, overwrite);
    }

    /**
     * Stores <code>cookie</code> in list for given domain.
     *
     * @param cookie    {@link Cookie}
     * @param overwrite remove old cookie if present
     * @since COMMONS 1.1.0
     */
    public void putCookie(Cookie cookie, boolean overwrite) {
        cookieMap.putCookie(cookie, overwrite);
    }

    /**
     * Sets up cookies for <code>connection</code> giving host. Will preserve
     * header if is set.
     *
     * @param connection connection to setup
     * @since COMMONS 1.1.0
     */
    public void setupCookies(URLConnection connection) {
        setupCookies(connection, cookieMap);
    }

    private static void setupCookies(URLConnection connection, CookieMap map,
                                     List<String> domains) {
        if (domains.isEmpty()) return;

        // list holding cookies to be sent.
        // Needs to be sorted.
        List<Cookie> sendCookies = new LinkedList<Cookie>();
        long startTime = System.currentTimeMillis() / 1000;
        String protocol = connection.getURL().getProtocol()
                .toLowerCase(Locale.ENGLISH);
        String path = connection.getURL().getPath();
        if (path.isEmpty()) path = "/";

        for (String domain : domains) {
            List<Cookie> list = map.get(domain);
            Iterator<Cookie> itr = list.iterator();

            // needed when deleting expired cookies
            synchronized (list) {
                while (itr.hasNext()) {
                    Cookie cookie = itr.next();

                    // if is HttpOnly, ensure it's over HTTP protocol
                    if (cookie.isHttpOnly() && !(protocol.equals("http"))
                            || protocol.equals("https")) continue;

                    // don't send insecure cookie if has Secure attribute
                    if (cookie.isSecure() && !protocol.equals("https")) continue;

                    // don't send expired cookies
                    if (cookie.isExpired(startTime)) {
                        itr.remove();
                        continue;
                    }

                    // check paths
                    if (!Cookie.matchesPath(path, cookie.getPath())) continue;

                    sendCookies.add(cookie);

                }
            }
        }

        setRequest(connection, sendCookies);
    }

    /**
     * Gets List of cookies from {@link java.net.HttpURLConnection} response headers.
     * <p/>
     * <blockquote>
     * <p/>
     * <pre>
     * <b>Header</b> Set-Cookie:
     * ...
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param connection url connection
     * @return cookies built from connection, or empty list
     * @since COMMONS 1.1.0
     */
    public static List<Cookie> getCookies(URLConnection connection) {
        ArrayList<Cookie> cookieList = new ArrayList<Cookie>();

        int i = 1;
        String header;
        while ((header = connection.getHeaderFieldKey(i)) != null) {
            if (header.equalsIgnoreCase(("Set-Cookie"))) {
                cookieList.add(Cookie.parseCookie(connection.getURL(),
                        connection.getHeaderField(i)));

            }
            i++;
        }

        return cookieList;
    }

    /**
     * Sets Cookie request header for <code>connection</code> on list of
     * cookies. Will preserve header if is set. Does not perform any checks on
     * cookies, all of them will be set for connection.
     *
     * @param connection being set up
     * @param cookies    to set up
     * @since COMMONS 1.1.0
     */
    public static void setCookies(URLConnection connection, List<Cookie> cookies) {
        setRequest(connection, cookies);
    }

    /**
     * Sets Cookie request header for <code>connection</code> on map of cookies.
     * Only sets cookies with appropriate domain of connection.
     *
     * @param connection being set up
     * @param cookies    to set up
     * @since COMMONS 1.1.0
     */
    public static void setupCookies(URLConnection connection, CookieMap cookies) {
        setupCookies(
                connection,
                cookies,
                getDomainsForHost(connection.getURL().getHost(),
                        cookies.getDomains()));
    }

    /**
     * Retrieves accepted cookie domains for giving host. Uses
     * {@link HttpCookie#domainMatches(String, String)} to check match.
     *
     * @param host          to check with domains
     * @param cookieDomains list of cookie domains
     * @return list of domains that match with the host
     * @since COMMONS 1.1.0
     */
    public static List<String> getDomainsForHost(String host,
                                                 Collection<String> cookieDomains) {
        // create new list for retaining
        List<String> cookieDomainList = new ArrayList<String>(cookieDomains);
        List<String> hostDomains = HttpHelper.getDomains(host);

        // retain host list
        Iterator<String> e = cookieDomainList.iterator();
        while (e.hasNext()) {
            boolean matches = false;
            String cd = e.next();
            for (String domain : hostDomains) {
                if (HttpCookie.domainMatches(cd, domain)) {
                    matches = true;
                    break;
                }
            }

            if (!matches) e.remove();
        }

        return cookieDomainList;

    }

    private static void setRequest(URLConnection connection,
                                   List<Cookie> cookies) {
        if (cookies.isEmpty()) return;
        Collections.sort(cookies, COOKIE_PATH_ORDER);

        StringBuilder builder;
        boolean append = false; // if header isn't null

        // preserve header
        if (connection.getRequestProperty("Cookie") != null) {
            builder = new StringBuilder(connection.getRequestProperty("Cookie"));
            append = true;
        } else {
            builder = new StringBuilder();
        }

        for (Cookie cookie : cookies) {
            builder.append(';').append(' ').append(cookie.getCookieString());
        }

        String cookieString;
        if (append) cookieString = builder.substring(1);
        else
            cookieString = builder.toString();

        connection.setRequestProperty("Cookie", StringUtils.trim(cookieString));

    }

    static class CookieSortComparator implements Comparator<Cookie> {

        @Override
        public int compare(Cookie o1, Cookie o2) {
            if (o1 == o2) return 0;
            if (!o1.getName().equals(o2.getName())) return 0;

            int len1 = o1.getPath().length();
            int len2 = o2.getPath().length();
            if (len1 > len2) return -1;
            else if (len2 > len1) return 1;
            else
                return 0;
        }

    }
}
