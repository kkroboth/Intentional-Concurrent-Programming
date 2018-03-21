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

import java.util.Random;

/**
 * Common Utility methods.
 *
 * @author Kyle Kroboth
 * @since COMMONS 1.0
 */
public final class CommonUtils {

    private CommonUtils() {
        // no op
    }

    /**
     * Get random through range.
     *
     * @param rand
     * @param min
     * @param max
     * @return random in range
     * @since COMMONS 1.0
     */
    public static int randomRange(Random rand, int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    /**
     * Quietly closes <code>Closeable</code> instances and ignores exceptions.
     * In Java-7, closes <code>AutoCloseable</code>.
     *
     * @param closeable to close
     * @since COMMONS 1.0
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (Exception e) {
            // ignore exception
        }
    }

}
