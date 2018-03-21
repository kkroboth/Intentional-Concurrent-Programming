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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * ArrayList of Cookies with <i>put</i> methods. Methods {@link #put(Cookie)}
 * and {@link #putAll(Collection)} will remove duplicate and add new one.
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.0
 */
public class CookieList extends AbstractList<Cookie> implements RandomAccess,
        Cloneable, java.io.Serializable {
    private static final long serialVersionUID = -1600884932769213656L;

    private transient Logger log = LoggerFactory.getLogger(CookieList.class);
    private final ArrayList<Cookie> delegate;
    private final String domain;

    /**
     * {@link ArrayList#ArrayList(Collection)}.
     *
     * @param collection to add
     * @param domain     of cookies or null
     * @since COMMONS 1.0
     */
    public CookieList(Collection<? extends Cookie> collection, String domain) {
        this.delegate = new ArrayList<Cookie>(collection);
        this.domain = domain;
    }

    /**
     * {@link ArrayList#ArrayList()}.
     *
     * @param domain of cookies or null
     * @since COMMONS 1.0
     */
    public CookieList(String domain) {
        this.delegate = new ArrayList<Cookie>();
        this.domain = domain;
    }

    /**
     * Creates new Cookie list with null domain.
     *
     * @since COMMONS 1.0
     */
    public CookieList() {
        this(null);
    }

    /**
     * Removes all expired cookies using current time.
     *
     * @param session remove session cookies
     * @return if list has changed
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
     * @return if list has changed
     * @since COMMONS 1.1.0
     */
    public boolean purgeExpired(long date, boolean session) {
        boolean modified = false;
        Iterator<Cookie> e = iterator();
        while (e.hasNext()) {
            Cookie cookie = e.next();
            if ((session && cookie.isSession()) || cookie.isExpired(date)) {
                modified = true;
                e.remove();
                log.debug("Removed Expired [{}]", cookie.toString());
            }
        }

        return modified;
    }

    /**
     * Gets domain of cookies.
     *
     * @return domain of list, or null
     * @since COMMONS 1.0
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Puts cookie into list and removes duplicate if found.
     *
     * @param cookie
     * @since COMMONS 1.0
     */
    public void put(Cookie cookie) {
        if (contains(cookie)) remove(cookie);
        add(cookie);
        log.debug("Stored [{}]", cookie);
    }

    /**
     * Puts all cookies in collection.
     *
     * @param collection
     * @see #put(Cookie)
     * @since COMMONS 1.0
     */
    public void putAll(Collection<? extends Cookie> collection) {
        for (Cookie cookie : collection) {
            put(cookie);
        }
    }

    /**
     * Removes cookie from list by name.
     *
     * @param name
     * @return true, if found
     * @since COMMONS 1.0
     */
    public boolean remove(String name) {
        for (Cookie cookie : this) {
            if (cookie.getName().equals(name)) return remove(cookie);
        }

        return false;
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Cookie get(int index) {
        return delegate.get(index);
    }

    /**
     * Gets cookie by name.
     *
     * @param name
     * @return cookie, or null if not found
     * @since COMMONS 1.0
     */
    public Cookie get(String name) {
        for (Cookie cookie : this) {
            if (cookie.getName().equals(name)) return cookie;
        }

        return null;
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
    public boolean add(Cookie cookie) {
        return delegate.add(cookie);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean addAll(Collection<? extends Cookie> collection) {
        return delegate.addAll(collection);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean remove(Object cookie) {
        return delegate.remove(cookie);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Cookie set(int index, Cookie cookie) {
        return delegate.set(index, cookie);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public List<Cookie> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public void add(int index, Cookie cookie) {
        delegate.add(index, cookie);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Cookie remove(int index) {
        return delegate.remove(index);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public int indexOf(Object index) {
        return delegate.indexOf(index);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public int lastIndexOf(Object lastIndex) {
        return delegate.lastIndexOf(lastIndex);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean addAll(int index, Collection<? extends Cookie> collection) {
        return delegate.addAll(index, collection);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Iterator<Cookie> iterator() {
        return delegate.iterator();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public ListIterator<Cookie> listIterator() {
        return delegate.listIterator();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public ListIterator<Cookie> listIterator(int index) {
        return delegate.listIterator(index);
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
    public boolean contains(Object cookie) {
        return delegate.contains(cookie);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public <T> T[] toArray(T[] type) {
        return delegate.toArray(type);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        return delegate.removeAll(collection);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        return delegate.retainAll(collection);
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
    public boolean containsAll(Collection<?> collection) {
        return delegate.containsAll(collection);
    }

    /**
     * @since COMMONS 1.0
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return delegate.clone();
    }

    private Object readResolve() {
        log = LoggerFactory.getLogger(CookieList.class);
        return this;
    }

}
