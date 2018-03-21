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

import java.io.Serializable;
import java.util.*;

/**
 * HashMap holding Cookies with set of domains.
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.0
 */
public class CookieMap extends AbstractMap<String, CookieList> implements
        Map<String, CookieList>, Cloneable, Serializable {
    private static final long serialVersionUID = 5833376510852965354L;
    private final HashMap<String, CookieList> delegate;
    private final Set<String> domainSet = new HashSet<String>();

    /**
     * {@link HashMap#HashMap(int, float)}.
     *
     * @param initialCapacity
     * @param loadFactor
     * @since COMMONS 1.0
     */
    public CookieMap(int initialCapacity, float loadFactor) {
        delegate = new HashMap<String, CookieList>(initialCapacity, loadFactor);
    }

    /**
     * {@link HashMap#HashMap(int)}.
     *
     * @param initialCapacity
     * @since COMMONS 1.0
     */
    public CookieMap(int initialCapacity) {
        delegate = new HashMap<String, CookieList>(initialCapacity);
    }

    /**
     * {@link HashMap#HashMap()}.
     *
     * @since COMMONS 1.0
     */
    public CookieMap() {
        delegate = new HashMap<String, CookieList>();
    }

    /**
     * {@link HashMap#HashMap(Map)}.
     *
     * @param m
     * @since COMMONS 1.0
     */
    public CookieMap(Map<? extends String, ? extends CookieList> m) {
        delegate = new HashMap<String, CookieList>(m);
    }

    /**
     * Gets set of domains of every cookie.
     *
     * @return unmodifiable set of domains
     * @since COMMONS 1.0
     */
    public Set<String> getDomains() {
        return Collections.unmodifiableSet(domainSet);
    }

    /**
     * Removes all expired cookies using current time.
     *
     * @param session removes session cookies if true
     * @return if one of the list changed
     * @since COMMONS 1.0
     */
    public boolean purgeExpired(boolean session) {
        return purgeExpired(System.currentTimeMillis() / 1000, session);
    }

    /**
     * Removes all expired cookies giving <code>date</code> in seconds.
     *
     * @param date    time in seconds
     * @param session remove session cookies
     * @return if one of the list changed
     * @since COMMONS 1.1.0
     */
    public boolean purgeExpired(long date, boolean session) {
        boolean modified = false;
        for (String domain : domainSet) {
            if (!delegate.get(domain).purgeExpired(date, session)) modified = true;
        }

        return modified;
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public CookieList get(Object key) {
        return delegate.get(key);
    }

    /**
     * Gets cookie from domain and name.
     *
     * @param domain domain of cookie
     * @param name   name of cookie
     * @return found cookie, or null
     */
    public Cookie getCookie(String domain, String name) {
        CookieList list = get(domain);
        if (list != null) return list.get(name);
        return null;
    }

    /**
     * Adds to delegate and domain set
     *
     * @since COMMONS 1.0
     */
    @Override
    public CookieList put(String key, CookieList value) {
        domainSet.add(key);
        return delegate.put(key, value);
    }

    /**
     * Puts Cookie correct domain.
     *
     * @param cookie    to add
     * @param overwrite remove old cookie if present
     * @since COMMONS 1.0
     */
    public void putCookie(Cookie cookie, boolean overwrite) {
        String domain = cookie.getDomain();
        CookieList list = get(domain);

        if (list == null) {
            list = new CookieList();
            put(domain, list);
        }

        if (overwrite) list.put(cookie);
        else
            list.add(cookie);
    }

    /**
     * Puts list of cookies their corresponding domains.
     *
     * @param cookies   to add
     * @param overwrite remove old cookie if present
     * @since COMMONS 1.0
     */
    public void putCookieList(List<Cookie> cookies, boolean overwrite) {
        for (Cookie cookie : cookies) {
            putCookie(cookie, overwrite);
        }
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public CookieList remove(Object key) {
        return delegate.remove(key);
    }

    /**
     * Removes cookie from list of domain.
     *
     * @param domain domain of cookie
     * @param name   name of cookie
     * @return true, if deleted
     * @since COMMONS 1.0
     */
    public boolean removeCookie(String domain, String name) {
        CookieList list = get(domain);
        return list != null && list.remove(name);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public void putAll(Map<? extends String, ? extends CookieList> m) {
        delegate.putAll(m);
    }

    /**
     * Clears delegate and domain set.
     *
     * @since COMMONS 1.0
     */
    @Override
    public void clear() {
        domainSet.clear();
        delegate.clear();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Collection<CookieList> values() {
        return delegate.values();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Set<java.util.Map.Entry<String, CookieList>> entrySet() {
        return delegate.entrySet();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return delegate.clone();
    }

}
