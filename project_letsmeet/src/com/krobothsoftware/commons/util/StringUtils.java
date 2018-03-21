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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;

/**
 * String Utilities methods.
 *
 * @author Kyle Kroboth
 */
public final class StringUtils {

    /**
     * Logger isn't used a lot, so kept null until needed. {@link #getLogger()}.
     */
    private static Logger log;

    private StringUtils() {
        // no op
    }


    /**
     * Gets string from InputStream. Uses Scanner with delimiter
     * <code>\\A</code>. <a
     * href="http://stackoverflow.com/a/5445161/702568">More Info</a>.
     *
     * @param inputStream
     * @param charset
     * @return String from stream, or an empty string
     * @since COMMONS 1.0
     */
    public static String toString(InputStream inputStream, String charset) {
        Scanner s = null;
        try {
            s = new Scanner(inputStream, charset);
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } finally {
            // SE 6 doesn't implement Closeable
            if (s != null) {
                s.close();
            }
        }
    }

    /**
     * Trims all whitespace off including special characters and non breaking
     * space. Sometimes {@link String#trim()} doesn't do the job when working
     * with Web content.
     * <p/>
     * <p>
     * Characters considered whitespace: <code>Non breaking space</code>,
     * <code>Narrow no-break</code>, <code>Figure space</code>, and lastly
     * {@link Character#isWhitespace(char)}.
     * </p>
     *
     * @param str text to trim
     * @return trimmed string
     * @since COMMONS 1.0
     */
    public static String trim(String str) {
        int count = str.length();
        int len = str.length();
        int st = 0;
        int off = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (isWhiteSpace(val[off + st])))
            st++;
        while ((st < len) && (isWhiteSpace(val[off + len - 1])))
            len--;

        return ((st > 0) || (len < count)) ? str.substring(st, len) : str;
    }

    private static boolean isWhiteSpace(char ch) {
        if ('\u00A0' == ch) return true;
        else if ('\u2007' == ch) return true;
        else if ('\u202F' == ch) return true;
        else return Character.isWhitespace(ch);
    }

    /**
     * Trims string for given character.
     *
     * @param str text to trim
     * @param ch  trim character
     * @return trimmed string
     * @since COMMONS 1.0
     */
    public static String trim(String str, char ch) {
        int count = str.length();
        int len = str.length();
        int st = 0;
        int off = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (val[off + st] <= ch)) {
            st++;
        }
        while ((st < len) && (val[off + len - 1] <= ch)) {
            len--;
        }

        return ((st > 0) || (len < count)) ? str.substring(st, len) : str;
    }

    /**
     * Trims string with first and last characters.
     *
     * @param str text to trim
     * @param chF first trim character
     * @param chL last trim character
     * @return trimmed string
     * @since COMMONS 1.0
     */
    public static String trim(String str, char chF, char chL) {
        int count = str.length();
        int len = str.length();
        int st = 0;
        int off = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (val[off + st] <= chF)) {
            st++;
        }
        while ((st < len) && (val[off + len - 1] <= chL)) {
            len--;
        }

        return ((st > 0) || (len < count)) ? str.substring(st, len) : str;
    }

    /**
     * Checks if inputstream has the <code>contains</code> string.
     *
     * @param input    inputstream to be checked
     * @param charset  encoding charset
     * @param contains contains string
     * @return true, if found
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     * @since COMMONS 1.0
     */
    public static boolean streamingContains(InputStream input, String charset,
                                            String contains) throws IOException {
        BufferedReader in = null;
        int count = 0;
        char[] content = contains.toCharArray();
        try {
            in = new BufferedReader(new InputStreamReader(input, charset));

            int ic;
            while ((ic = in.read()) != -1) {
                if ((char) ic == content[count]) {
                    count++;
                    if (count == contains.length()) return true;
                } else
                    count = 0;
            }

        } catch (UnsupportedEncodingException e) {
            getLogger().error(
                    "streamingContains UnsupportedEncodingException {}",
                    e.getMessage());
            return false;
        } finally {
            CommonUtils.closeQuietly(in);
        }

        return false;
    }

    /**
     * Checks if inputstream contains any of the <code>contains</code> string
     * elements. Only the first found index will be returned.
     *
     * @param input    inputstream to be checked
     * @param charset  encoding charset
     * @param contains array of strings to check if inputstream contains
     * @return index of found contains, or -1 if none
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     * @since COMMONS 1.0
     */
    public static int streamingContains(InputStream input, String charset,
                                        String... contains) throws IOException {
        BufferedReader in = null;
        int len = contains.length;
        int[] conLen = new int[len];
        int[] countArray = new int[len];
        char[][] contentArray = new char[len][];
        for (int i = 0; i < len; i++) {
            contentArray[i] = contains[i].toCharArray();
            conLen[i] = contains[i].length();
        }
        try {
            in = new BufferedReader(new InputStreamReader(input, charset));

            int ic;
            while ((ic = in.read()) != -1) {
                for (int i = 0; i < len; i++) {
                    if ((char) ic == contentArray[i][countArray[i]]) {
                        countArray[i]++;
                        if (countArray[i] == conLen[i]) return i;
                    } else
                        countArray[i] = 0;
                }
            }

        } catch (UnsupportedEncodingException e) {
            getLogger().error(
                    "streamingContains UnsupportedEncodingException {}",
                    e.getMessage());
            return -1;
        } finally {
            CommonUtils.closeQuietly(in);
        }

        return -1;
    }

    /**
     * Gets sub-string between <code>beginIndex</code> and <code>endIndex</code>
     * from inputstream. Does not include beginIndex from returned result.
     *
     * @param input      inputstream to be checked
     * @param charset    encoding charset
     * @param beginIndex the beginning index
     * @param endIndex   the ending index
     * @return string between begin and end index, or null
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     * @since COMMONS 1.1.0
     */
    public static String streamingSubString(InputStream input, String charset,
                                            String beginIndex, String endIndex) throws IOException {
        StringBuilder sb;
        BufferedReader in = null;
        int count = 0;
        int len = beginIndex.length();
        char[] content = beginIndex.toCharArray();
        boolean found = false;

        try {
            in = new BufferedReader(new InputStreamReader(input, charset));

            int ic;
            // find beginIndex
            while ((ic = in.read()) != -1) {
                if ((char) ic == content[count]) {
                    count++;
                    if (count == len) {
                        found = true;
                        break;
                    }
                } else
                    count = 0;
            }
            if (found) {
                sb = new StringBuilder();
                count = 0;
                len = endIndex.length();
                content = endIndex.toCharArray();
                // find endIndex
                while ((ic = in.read()) != -1) {
                    char c = (char) ic;
                    sb.append(c);
                    if (c == content[count]) {
                        count++;
                        if (count == len) {
                            return sb.substring(0, sb.length() - len);
                        }
                    } else
                        count = 0;
                }
            }

        } catch (UnsupportedEncodingException e) {
            getLogger().error(
                    "streamingContains UnsupportedEncodingException {}",
                    e.getMessage());
        } finally {
            CommonUtils.closeQuietly(in);
        }

        return null;

    }

    private static Logger getLogger() {
        return (log != null) ? log : (log = LoggerFactory
                .getLogger(CommonUtils.class));
    }
}
