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

package com.krobothsoftware.commons.util;

import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread safe {@link SimpleDateFormat} methods for parsing and formatting.
 * {@link ThreadLocal} holds a map of <code>String</code>(Date pattern) to
 * {@link SimpleDateFormat} instance.
 * <p/>
 * <p>
 * Methods with <code>TimeZone</code> and <code>Locale</code> do not set values
 * permanently, but for each call. Only way to use <code>Locale</code> is
 * setting {@link Calendar#setFirstDayOfWeek(int)} and
 * {@link Calendar#setMinimalDaysInFirstWeek(int)}. Those values are retrieved
 * internally and need reflection or access to non-public classes.
 * {@link #getWeekData(Locale)} <i>should</i> get them.
 * </p>
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.1.0
 */
public final class ThreadSafeDateUtil {

    /**
     * Thread Local HashMap.
     */
    private static final Cache cache = new Cache();

    /**
     * Static field in Calendar class.
     */
    private static ConcurrentMap<Locale, int[]> cachedLocaleData;

    /**
     * If reflection fails, don't want to keep on trying.
     */
    private static volatile boolean tried = false;

    private ThreadSafeDateUtil() {
        // no op
    }

    /**
     * Thread safe {@link SimpleDateFormat#parse(String)}.
     *
     * @param pattern <code>SimpleDateFormat</code> pattern
     * @param source  <code>String</code> date to parse
     * @return A Date parsed from the string
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     * @since COMMONS 1.1.0
     */
    public static Date parse(String pattern, String source)
            throws ParseException {
        return cache.get(pattern).parse(source);
    }

    /**
     * Thread safe {@link SimpleDateFormat#parse(String)}. Uses Locale in
     * formatter. Use {@link #getWeekData(Locale)} to get data array.
     *
     * @param pattern    <code>SimpleDateFormat</code> pattern
     * @param source     <code>String</code> date to parse
     * @param localeData array of first day of week and minimal days in first week in
     *                   <code>Locale</code>
     * @return A Date parsed from the string
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     * @since COMMONS 1.1.0
     */
    public static Date parse(String pattern, String source, int[] localeData)
            throws ParseException {
        SimpleDateFormat df = cache.get(pattern);
        Calendar calendar = df.getCalendar();
        // hold old values
        int fd = calendar.getFirstDayOfWeek();
        int md = calendar.getMinimalDaysInFirstWeek();
        calendar.setFirstDayOfWeek(localeData[0]);
        calendar.setMinimalDaysInFirstWeek(localeData[1]);
        try {
            return df.parse(source);
        } finally {
            calendar.setFirstDayOfWeek(fd);
            calendar.setMinimalDaysInFirstWeek(md);
        }
    }

    /**
     * Thread safe {@link SimpleDateFormat#parse(String)}. Uses TimeZone in
     * formatter.
     *
     * @param pattern <code>SimpleDateFormat</code> pattern
     * @param source  <code>String</code> date to parse
     * @param zone    <code>TimeZone</code> to use
     * @return A Date parsed from the string
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     * @since COMMONS 1.1.0
     */
    public static Date parse(String pattern, String source, TimeZone zone)
            throws ParseException {
        SimpleDateFormat df = cache.get(pattern);
        // hold old value
        TimeZone oldZone = df.getTimeZone();
        df.setTimeZone(zone);
        try {
            return df.parse(source);
        } finally {
            df.setTimeZone(oldZone);
        }

    }

    /**
     * Thread safe {@link SimpleDateFormat#parse(String)}. Uses TimeZone and
     * Locale in formatter.
     *
     * @param pattern    <code>SimpleDateFormat</code> pattern
     * @param source     <code>String</code> date to parse
     * @param zone       <code>TimeZone</code> to use
     * @param localeData array of first day of week and minimal days in first week in
     *                   <code>Locale</code>
     * @return A Date parsed from the string
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     * @since COMMONS 1.1.0
     */
    public static Date parse(String pattern, String source, TimeZone zone,
                             int[] localeData) throws ParseException {
        SimpleDateFormat df = cache.get(pattern);
        Calendar calendar = df.getCalendar();
        // hold old values
        TimeZone oldZone = df.getTimeZone();
        int fd = calendar.getFirstDayOfWeek();
        int md = calendar.getMinimalDaysInFirstWeek();
        calendar.setFirstDayOfWeek(localeData[0]);
        calendar.setMinimalDaysInFirstWeek(localeData[1]);
        calendar.setTimeZone(zone);
        try {
            return df.parse(source);
        } finally {
            calendar.setFirstDayOfWeek(fd);
            calendar.setMinimalDaysInFirstWeek(md);
            calendar.setTimeZone(oldZone);
        }
    }

    /**
     * Thread safe {@link SimpleDateFormat#format(Date)}.
     *
     * @param pattern <code>SimpleDateFormat</code> pattern
     * @param date    the time value to be formatted into a time string
     * @return the time value to be formatted into a time string.
     * @since COMMONS 1.1.0
     */
    public static String format(String pattern, Date date) {
        return cache.get(pattern).format(date);
    }

    /**
     * Thread safe {@link SimpleDateFormat#format(Date)}. Uses Locale in
     * formatter. Use {@link #getWeekData(Locale)} to get data array.
     *
     * @param pattern    <code>SimpleDateFormat</code> pattern
     * @param date       the time value to be formatted into a time string
     * @param localeData array of first day of week and minimal days in first week in
     *                   <code>Locale</code>
     * @return the time value to be formatted into a time string.
     * @since COMMONS 1.1.0
     */
    public static String format(String pattern, Date date, int[] localeData) {
        SimpleDateFormat df = cache.get(pattern);
        Calendar calendar = df.getCalendar();
        // hold old values
        int fd = calendar.getFirstDayOfWeek();
        int md = calendar.getMinimalDaysInFirstWeek();
        calendar.setFirstDayOfWeek(localeData[0]);
        calendar.setMinimalDaysInFirstWeek(localeData[1]);
        try {
            return df.format(date);
        } finally {
            calendar.setFirstDayOfWeek(fd);
            calendar.setMinimalDaysInFirstWeek(md);
        }

    }

    /**
     * Thread safe {@link SimpleDateFormat#format(Date)}. Uses TimeZone and
     * Locale in formatter.
     *
     * @param pattern <code>SimpleDateFormat</code> pattern
     * @param date    the time value to be formatted into a time string
     * @param zone    <code>TimeZone</code> to use
     * @return the time value to be formatted into a time string.
     * @since COMMONS 1.1.0
     */
    public static String format(String pattern, Date date, TimeZone zone) {
        SimpleDateFormat df = cache.get(pattern);
        // hold old value
        TimeZone oldZone = df.getTimeZone();
        df.setTimeZone(zone);
        try {
            return df.format(date);
        } finally {
            df.setTimeZone(oldZone);
        }

    }

    /**
     * Thread safe {@link SimpleDateFormat#format(Date)}. Uses TimeZone and
     * Locale in formatter.
     *
     * @param pattern    <code>SimpleDateFormat</code> pattern
     * @param date       the time value to be formatted into a time string
     * @param zone       <code>TimeZone</code> to use
     * @param localeData array of first day of week and minimal days in first week in
     *                   <code>Locale</code>
     * @return the time value to be formatted into a time string.
     * @since COMMONS 1.1.0
     */
    public static String format(String pattern, Date date, TimeZone zone,
                                int[] localeData) {
        SimpleDateFormat df = cache.get(pattern);
        Calendar calendar = df.getCalendar();
        // hold old values
        TimeZone oldZone = df.getTimeZone();
        int fd = calendar.getFirstDayOfWeek();
        int md = calendar.getMinimalDaysInFirstWeek();
        calendar.setFirstDayOfWeek(localeData[0]);
        calendar.setMinimalDaysInFirstWeek(localeData[1]);
        calendar.setTimeZone(zone);
        try {
            return df.format(date);
        } finally {
            calendar.setFirstDayOfWeek(fd);
            calendar.setMinimalDaysInFirstWeek(md);
            calendar.setTimeZone(oldZone);
        }
    }

    /**
     * Retrieves Locale data regarding first day of week and minimal days in a
     * week.
     * <p/>
     * <p>
     * <b>Java SE:</b> Uses reflection to get cached Map inside
     * <i>Calendar.class</i>. If not found, will get <code>ResourceBundle</code>
     * for Locale. Should not be a problem calling this method many times since
     * getting the data is <i>O(1)</i>.
     * </p>
     * <p/>
     * <p>
     * <b>Android:</b> Calls <i>libcore.icu.LocaleData.get(Locale)</i> through
     * reflection and gets values. If performance is an issue, call this method
     * once and store value or use <i>Android-Frameworks</i>.
     * </p>
     * <p/>
     * <p>
     * A good idea is to retrieve the values and store them in an int array.
     * That way there is no need to call this method every time, or ever.
     * </p>
     *
     * @param locale to get calendar values
     * @return array of week data for calendar. Will return {1, 1} if any errors
     * occur.
     * @since COMMONS 1.1.0
     * @deprecated Do not use! Used to use reflection to access week data.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static int[] getWeekData(Locale locale) {
        throw new UnsupportedOperationException("Deprecated: No alternative");
    }

    static class Cache extends ThreadLocal<Map<String, SoftReference<SimpleDateFormat>>> {

        @Override
        protected Map<String, SoftReference<SimpleDateFormat>> initialValue() {
            return new HashMap<String, SoftReference<SimpleDateFormat>>();
        }

        public SimpleDateFormat get(String pattern) {
            Map<String, SoftReference<SimpleDateFormat>> map = get();
            SimpleDateFormat df;
            if (!map.containsKey(pattern)) {
                df = new SimpleDateFormat(pattern);
                map.put(pattern, new SoftReference<SimpleDateFormat>(df));
                return df;
            } else if (map.get(pattern).get() == null) {
                df = new SimpleDateFormat(pattern);
                map.put(pattern, new SoftReference<SimpleDateFormat>(df));
                return df;
            } else {
                return map.get(pattern).get();
            }
        }
    }

}
