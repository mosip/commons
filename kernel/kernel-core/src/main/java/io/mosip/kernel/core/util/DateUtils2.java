/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mosip.kernel.core.util;

import io.mosip.kernel.core.exception.IllegalArgumentException;
import io.mosip.kernel.core.util.constant.CalendarUtilConstants;
import io.mosip.kernel.core.util.constant.DateUtilConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for Date Time operations.
 *
 * Provide Date and Time utility for usage across the application to manipulate
 * dates or calendars
 *
 * @author Ravi C Balaji
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
public final class DateUtils2 {

    /**
     * Default UTC TimeZone.
     */
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
    /**
     * Default UTC ZoneId.
     */
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    /**
     * Default UTC pattern.
     */
    private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /** Cached thread-local formatters for high performance */
    private static final ThreadLocal<SimpleDateFormat> DEFAULT_UTC_FORMATTER =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat(UTC_DATETIME_PATTERN);
                sdf.setTimeZone(UTC_TIME_ZONE);
                return sdf;
            });

    private static final ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> FORMATTER_CACHE = new ConcurrentHashMap<>();

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN);

    private static final ConcurrentHashMap<String, DateTimeFormatter> FORMATTER_CACHE_01 = new ConcurrentHashMap<>();

    private DateUtils2() {

    }

    private static SimpleDateFormat getFormatter(String pattern, TimeZone tz, Locale locale) {
        return FORMATTER_CACHE.computeIfAbsent(pattern + tz.getID() + locale.toLanguageTag(),
                k -> ThreadLocal.withInitial(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
                    sdf.setTimeZone(tz);
                    return sdf;
                })).get();
    }

    /**
     * <p>
     * Adds a number of days to a date returning a new Date object.
     * </p>
     *
     * @param date the date, not null
     * @param days the number of days to add, may be negative
     * @return the new Date with the number of days added
     * @throws IllegalArgumentException if the date
     *                                                                 is null
     */
    public static Date addDays(final Date date, final int days) {
        try {
            return org.apache.commons.lang3.time.DateUtils.addDays(date, days);
        } catch (java.lang.IllegalArgumentException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException(CalendarUtilConstants.ILLEGAL_ARGUMENT_CODE.getErrorCode(),
                    CalendarUtilConstants.ILLEGAL_ARGUMENT_MESSAGE.getErrorCode(), exception.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Adds a number of hours to a date returning a new Date object.
     * </p>
     *
     * @param date  the date, not null
     * @param hours the hours to add, may be negative
     * @return the new Date with the hours added
     * @throws IllegalArgumentException if the date
     *                                                                 is null
     */
    public static Date addHours(final Date date, final int hours) {
        try {
            return org.apache.commons.lang3.time.DateUtils.addHours(date, hours);
        } catch (java.lang.IllegalArgumentException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException(CalendarUtilConstants.ILLEGAL_ARGUMENT_CODE.getErrorCode(),
                    CalendarUtilConstants.ILLEGAL_ARGUMENT_MESSAGE.getErrorCode(), exception.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Adds a number of minutes to a date returning a new Date object.
     * </p>
     *
     * @param date    the date, not null
     * @param minutes the minutes to add, may be negative
     * @return the new Date with the minutes added
     * @throws IllegalArgumentException if the date
     *                                                                 is null
     */
    public static Date addMinutes(final Date date, final int minutes) {
        try {
            return org.apache.commons.lang3.time.DateUtils.addMinutes(date, minutes);
        } catch (java.lang.IllegalArgumentException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException(CalendarUtilConstants.ILLEGAL_ARGUMENT_CODE.getErrorCode(),
                    CalendarUtilConstants.ILLEGAL_ARGUMENT_MESSAGE.getErrorCode(), exception.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Adds a number of seconds to a date returning a new Date object.
     * </p>
     *
     * @param date    the date, not null
     * @param seconds the seconds to add, may be negative
     * @return the new Date with the seconds added
     * @throws IllegalArgumentException if the date
     *                                                                 is null
     */
    public static Date addSeconds(final Date date, final int seconds) {
        try {
            return org.apache.commons.lang3.time.DateUtils.addSeconds(date, seconds);
        } catch (java.lang.IllegalArgumentException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException(CalendarUtilConstants.ILLEGAL_ARGUMENT_CODE.getErrorCode(),
                    CalendarUtilConstants.ILLEGAL_ARGUMENT_MESSAGE.getErrorCode(), exception.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Formats a date/time into a specific pattern.
     * </p>
     *
     * @param date    the date to format, not null
     * @param pattern the pattern to use to format the date, not null
     * @return the formatted date
     * @throws IllegalArgumentException if the
     *                                                                 date/pattern
     *                                                                 is null
     */
    public static String formatDate(final Date date, final String pattern) {
        try {
            return getFormatter(pattern, TimeZone.getDefault(), Locale.getDefault()).format(date);
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Formats a date/time into a specific pattern in a time zone.
     * </p>
     *
     * @param date     the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @param timeZone the time zone to use, may be null
     * @return the formatted date
     * @throws IllegalArgumentException if the
     *                                                                 date/pattern/timeZone
     *                                                                 is null
     */
    public static String formatDate(final Date date, final String pattern, final TimeZone timeZone) {
        try {
            return getFormatter(pattern, timeZone, Locale.getDefault()).format(date);
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Formats a date/time into a specific pattern in a time zone and locale.
     * </p>
     *
     * @param date,     the date to format, not null
     * @param pattern,  the pattern to use to format the date, not null
     * @param timeZone, the time zone to use, may be null
     * @param locale,   the locale to use, may be null
     * @return the formatted date
     * @throws IllegalArgumentException if the
     *                                                                 date/pattern/timeZone
     *                                                                 is null
     */
    public static String formatDate(final Date date, final String pattern, final TimeZone timeZone,
                                    final Locale locale) {
        try {
            return getFormatter(pattern, timeZone, locale).format(date);
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * Formats a calendar into a specific pattern.
     *
     * @param calendar, the calendar to format, not null
     * @param pattern,  the pattern to use to format the calendar, not null
     * @return the formatted calendar
     * @throws IllegalArgumentException if the
     *                                                                 calendar/pattern
     *                                                                 is null
     */
    public static String formatCalendar(final Calendar calendar, final String pattern) {
        try {
            return getFormatter(pattern, TimeZone.getDefault(), Locale.getDefault()).format(calendar.getTime());
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * Formats a calendar into a specific pattern in a time zone.
     *
     * @param calendar, the calendar to format, not null
     * @param pattern,  the pattern to use to format the calendar, not null
     * @param timeZone, the time zone to use, may be null
     * @return the formatted calendar
     * @throws IllegalArgumentException if the
     *                                                                 calendar/pattern/timeZone
     *                                                                 is null
     */
    public static String formatCalendar(final Calendar calendar, final String pattern, final TimeZone timeZone) {
        try {
            return getFormatter(pattern, timeZone, Locale.getDefault()).format(calendar.getTime());
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * Formats a calendar into a specific pattern in a time zone and locale.
     *
     * @param calendar, the calendar to format, not null
     * @param pattern,  the pattern to use to format the calendar, not null
     * @param locale,   the locale to use, may be null
     * @return the formatted calendar
     * @throws IllegalArgumentException if the
     *                                                                 calendar/pattern/locale
     *                                                                 is null
     */
    public static String formatCalendar(final Calendar calendar, final String pattern, final Locale locale) {
        try {
            return getFormatter(pattern, TimeZone.getDefault(), locale).format(calendar.getTime());
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // -----------------------------------------------------------------------
    /**
     * Formats a calendar into a specific pattern in a time zone and locale.
     *
     * @param calendar, the calendar to format, not null
     * @param pattern,  the pattern to use to format the calendar, not null
     * @param timeZone, the time zone to use, may be null
     * @param locale,   the locale to use, may be null
     * @return the formatted calendar
     * @throws IllegalArgumentException if the
     *                                                                 calendar/pattern/timeZone
     *                                                                 is null
     */
    public static String formatCalendar(final Calendar calendar, final String pattern, final TimeZone timeZone,
                                        final Locale locale) {
        try {
            return getFormatter(pattern, timeZone, locale).format(calendar.getTime());
        } catch (java.lang.IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    /**
     * Tests if this date is after the specified date.
     *
     * @param d1 a Date by which we will compare
     *
     * @param d2 a Date with which we want to compare
     *
     * @return <b>true</b> if and only if the instant represented by d1
     *         <tt>Date</tt> object is strictly later than the instant represented
     *         by <tt>d2</tt>; <b>false</b> otherwise.
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see Date
     */
    public static boolean after(Date d1, Date d2) {
        try {
            return d1.after(d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * Tests if this date is before the specified date.
     *
     * @param d1 a Date by which we will compare
     * @param d2 a Date with which we want to compare
     *
     * @return <b>true</b> if and only if the instant of time represented by d1
     *         <tt>Date</tt> object is strictly earlier than the instant represented
     *         by <tt>d2</tt>; <b>false</b> otherwise.
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see Date
     */
    public static boolean before(Date d1, Date d2) {
        try {
            return d1.before(d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * Checks if two date objects are on the same day ignoring time. <br>
     * 28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.<br>
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     *
     * @param d1 a Date by which we will compare
     *
     * @param d2 a Date with which we want to compare
     *
     * @return <b>true</b> if they represent the same day
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see Date
     *
     */
    public static boolean isSameDay(Date d1, Date d2) {
        try {
            return org.apache.commons.lang3.time.DateUtils.isSameDay(d1, d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * Checks if two date objects represent the same instant in time. <br>
     * This method compares the long millisecond time of the two objects.
     *
     * @param d1 a Date by which we will compare
     *
     * @param d2 a Date with which we want compare
     *
     * @return <code>true</code> if they represent the same millisecond instant
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see Date
     */
    public static boolean isSameInstant(Date d1, Date d2) {
        try {
            return org.apache.commons.lang3.time.DateUtils.isSameInstant(d1, d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    // ---------------------------------------------------------------------------------------------
    /**
     * Tests if this java.time.LocalDateTime is after the specified
     * java.time.LocalDateTime.
     *
     * @param d1 a LocalDateTime which is going to be compared
     *
     * @param d2 a LocalDateTime by which we will compare
     *
     * @return <b>true</b> if and only if the instant represented by d1
     *         <tt>LocalDateTime</tt> object is strictly later than the instant
     *         represented by <tt>d2</tt>; <b>false</b> otherwise.
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see LocalDateTime
     */
    public static boolean after(LocalDateTime d1, LocalDateTime d2) {
        try {
            return d1.isAfter(d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * Tests if this LocalDateTime is before the specified LocalDateTime.
     *
     * @param d1 a LocalDateTime which is going to be compared
     *
     * @param d2 a LocalDateTime by which we will compare
     *
     * @return <b>true</b> if and only if the instant of time represented by d1
     *         <tt>LocalDateTime</tt> object is strictly earlier than the instant
     *         represented by <tt>d2</tt>; <b>false</b> otherwise.
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see LocalDateTime
     */
    public static boolean before(LocalDateTime d1, LocalDateTime d2) {
        try {
            return d1.isBefore(d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * Checks if two java.time.LocalDateTime objects are on the same day ignoring
     * time. <br>
     * 28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.<br>
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     *
     * @param d1 a LocalDateTime which is going to be compared
     * @param d2 a LocalDateTime by which we will compare
     *
     * @return <b>true</b> if they represent the same day
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see LocalDateTime
     */
    public static boolean isSameDay(LocalDateTime d1, LocalDateTime d2) {
        try {
            return d1.toLocalDate().isEqual(d2.toLocalDate());
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * Checks if two java.time.LocalDateTime objects represent the same instant in
     * time. <br>
     * This method compares the long millisecond time of the two objects.
     *
     * @param d1 a LocalDateTime which is going to be compared
     *
     * @param d2 a LocalDateTime by which we will compare
     *
     * @return <b>true</b> if they represent the same millisecond instant
     *
     * @throws IllegalArgumentException if the
     *                                                                 <code>d1</code>
     *                                                                 ,
     *                                                                 <code>d2</code>
     *                                                                 is null
     *
     * @see IllegalArgumentException
     *
     * @see LocalDateTime
     */
    public static boolean isSameInstant(LocalDateTime d1, LocalDateTime d2) {
        try {
            return d1.isEqual(d2);
        } catch (Exception e) {
            throw new IllegalArgumentException(DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }
    // ---------------------------------------------------------------------------------------------------------------------------

    /**
     * Converts java.time.LocalDateTime to UTC string in default ISO pattern -
     * <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b>.
     *
     * @param localDateTime java.time.LocalDateTime
     *
     * @return a date String
     */

    public static String toISOString(LocalDateTime localDateTime) {
        ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.UTC);
        return converted.toString();
    }

    /**
     * Converts java.util.Date to UTC string in default ISO pattern -
     * <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b>.
     *
     * @param date java.util.Date
     *
     * @return a date String
     */
    public static String toISOString(Date date) {
        return DEFAULT_UTC_FORMATTER.get().format(date);
    }

    /**
     * Formats java.time.LocalDateTime to UTC string in default ISO pattern -
     * <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b> ignoring zone offset.
     *
     * @param localDateTime java.time.LocalDateTime
     *
     * @return a date String
     */

    public static String formatToISOString(LocalDateTime localDateTime) {
        return localDateTime.format(ISO_FORMATTER);
    }

    /**
     * Provides current UTC java.time.LocalDateTime.
     *
     * @return LocalDateTime
     *
     * @see LocalDateTime
     */
    public static LocalDateTime getUTCCurrentDateTime() {
        return ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Returns the current UTC time as a {@link LocalDateTime}, with precision
     * truncated to the smallest time unit implied by the supplied date-time
     * pattern.
     *
     * <p>The truncation is determined by scanning the pattern for time fields:
     * <ul>
     *   <li>Contains {@code 'S'} → truncate to <b>milliseconds</b></li>
     *   <li>Else contains {@code 's'} → truncate to <b>seconds</b></li>
     *   <li>Else contains {@code 'm'} → truncate to <b>minutes</b></li>
     *   <li>Else contains any of {@code 'H','h','k','K'} → truncate to <b>hours</b></li>
     *   <li>Else (date-only pattern) → <b>no time truncation</b></li>
     * </ul>
     *
     * <p>This method does not format or parse the value; it works directly on
     * {@link Instant} and converts to UTC {@link LocalDateTime}, minimizing
     * allocations and avoiding formatter overhead.
     *
     * <p><b>Examples</b>
     * <pre>
     * getUTCCurrentDateTime("yyyy-MM-dd")                 // no time truncation
     * getUTCCurrentDateTime("yyyy-MM-dd HH")              // truncated to hours
     * getUTCCurrentDateTime("yyyy-MM-dd HH:mm")           // truncated to minutes
     * getUTCCurrentDateTime("yyyy-MM-dd HH:mm:ss")        // truncated to seconds
     * getUTCCurrentDateTime("yyyy-MM-dd HH:mm:ss.SSS")    // truncated to milliseconds
     * </pre>
     *
     * @param pattern a {@link DateTimeFormatter}-compatible pattern
     *                used only to infer the desired precision; if {@code null} or empty,
     *                the current UTC time is returned without truncation.
     * @return current UTC time as {@link LocalDateTime}, truncated to the resolution
     *         implied by {@code pattern}
     */
    public static LocalDateTime getUTCCurrentDateTime(String pattern) {
        Instant now = Instant.now();
        Instant truncated = truncateToPattern(now, pattern);
        return LocalDateTime.ofInstant(truncated, ZoneOffset.UTC);
    }

    /**
     * Truncates the given {@link Instant} to the smallest time unit implied by the
     * provided date-time pattern.
     *
     * <p>Resolution detection rules:
     * <ul>
     *   <li>If the pattern contains {@code 'S'} (fraction), truncate to milliseconds.</li>
     *   <li>Else if it contains {@code 's'} (seconds), truncate to seconds.</li>
     *   <li>Else if it contains {@code 'm'} (minutes), truncate to minutes.</li>
     *   <li>Else if it contains any of {@code 'H','h','k','K'} (hours), truncate to hours.</li>
     *   <li>Else return the {@code instant} unchanged (date-only pattern).</li>
     * </ul>
     *
     * <p>This method is purely computational and does not allocate formatters.
     *
     * @param instant the instant to truncate (must not be {@code null})
     * @param pattern the date-time pattern from which to infer resolution; if {@code null}
     *                or empty, the {@code instant} is returned unmodified
     * @return a truncated {@link Instant} according to the resolution implied by {@code pattern}
     */
    public static Instant truncateToPattern(Instant instant, String pattern) {
        if (pattern == null || pattern.isEmpty()) return instant;

        boolean hasMillis  = pattern.indexOf('S') >= 0;                         // fraction of second
        boolean hasSecond  = pattern.indexOf('s') >= 0;
        boolean hasMinute  = pattern.indexOf('m') >= 0;
        boolean hasHour    = pattern.indexOf('H') >= 0 || pattern.indexOf('h') >= 0
                || pattern.indexOf('k') >= 0 || pattern.indexOf('K') >= 0;

        if (hasMillis)      return instant.truncatedTo(ChronoUnit.MILLIS);
        else if (hasSecond) return instant.truncatedTo(ChronoUnit.SECONDS);
        else if (hasMinute) return instant.truncatedTo(ChronoUnit.MINUTES);
        else if (hasHour)   return instant.truncatedTo(ChronoUnit.HOURS);
        else                return instant; // date-only patterns: no time truncation needed
    }

    /**
     * Provides the current UTC date-time as a {@link String} in ISO-8601 format.
     * <p>
     * This method obtains the current {@link OffsetDateTime} from the system clock
     * in the default time-zone, converts it to an {@link Instant}, and
     * then returns its ISO-8601 string representation. The output will always be
     * in UTC (denoted by the trailing {@code 'Z'}).
     * </p>
     * <p>
     * Note: {@link Instant#toString()} may include up to nanosecond
     * precision (up to 9 fractional digits). If only millisecond precision
     * is required (3 fractional digits), consider truncating the {@code Instant}
     * before calling {@code toString()}.
     * </p>
     *
     * <pre>
     * Example output:
     *  2025-08-26T06:30:41.055195136Z
     * </pre>
     *
     * @return the current UTC date-time as an ISO-8601 string
     *
     * @see OffsetDateTime#now()
     * @see OffsetDateTime#toInstant()
     * @see Instant#toString()
     */
    public static String getUTCCurrentDateTimeString() {
        return getUTCCurrentDateTimeString(UTC_DATETIME_PATTERN);
    }

    /**
     * Provides the current UTC date-time as a {@link String} in the default ISO pattern
     * <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b>.
     * <p>
     * This method internally invokes {@link #getUTCCurrentDateTimeString(String)} with
     * the default UTC pattern {@code UTC_DATETIME_PATTERN}.
     * </p>
     * <p>
     * The output is always in UTC, truncated to milliseconds, and ends with a literal
     * {@code 'Z'} to indicate UTC time.
     * </p>
     *
     * <pre>
     * Example output:
     *  2025-08-26T07:25:41.123Z
     * </pre>
     *
     * @return the current UTC date-time as a formatted string in pattern
     *         {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}
     */
    public static String getUTCCurrentDateTimeWithZString() {
        return getUTCCurrentDateTimeString(UTC_DATETIME_PATTERN);
    }

    /**
     * Provides UTC Current DateTime string in given pattern.
     *
     * @param pattern is of type String
     *
     * @return date String
     */
    public static String getUTCCurrentDateTimeString(String pattern) {
        DateTimeFormatter formatter = FORMATTER_CACHE_01.computeIfAbsent(pattern,
                p -> DateTimeFormatter.ofPattern(p).withZone(ZoneOffset.UTC));

        // Use cached formatter for high-speed conversion
        return formatter.format(Instant.now());
    }

    /**
     * Provides current DateTime string with system zone offset and in default ISO
     * pattern - <b>yyyy-MM-dd'T'HH:mm:ss.SSSXXX</b>.
     *
     * @return a date String
     */
    public static String getCurrentDateTimeString() {
        return OffsetDateTime.now().toString();
    }

    /**
     * Converts UTC string to java.time.LocalDateTime ignoring zone offset.
     *
     * @param utcDateTime is of type String
     *
     * @return a LocalDateTime
     *
     * @throws java.time.format.DateTimeParseException if not able to parse the
     *                                                 utcDateTime string for the
     *                                                 pattern.
     *
     *
     * @see LocalDateTime
     */
    public static LocalDateTime convertUTCToLocalDateTime(String utcDateTime) {
        return ZonedDateTime.parse(utcDateTime).toLocalDateTime();
    }

    /**
     * Parses UTC string to java.time.LocalDateTime adjusted for system time zone.
     *
     * @param utcDateTime is of type String
     *
     * @return a LocalDateTime
     *
     *
     * @see LocalDateTime
     */
    public static LocalDateTime parseUTCToLocalDateTime(String utcDateTime) {
        OffsetDateTime odt = OffsetDateTime.parse(utcDateTime);
        return odt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Parses UTC string of pattern <b>yyyy-MM-dd'T'HH:mm:ss.SSS</b> or
     * <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b> to java.time.LocalDateTime.
     *
     * @param dateTime is of type String
     *
     * @return a LocalDateTime
     *
     * @throws java.time.format.DateTimeParseException if not able to parse the
     *                                                 utcDateTime string for the
     *                                                 pattern
     *
     * @see LocalDateTime
     */
    public static LocalDateTime parseToLocalDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, ISO_FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.parse(dateTime);
        }
    }

    /**
     * Parses UTC string of given pattern to java.time.LocalDateTime.
     *
     * @param utcDateTime is of type String
     *
     * @param pattern     is of type String
     *
     * @return LocalDateTime
     *
     * @throws io.mosip.kernel.core.exception.ParseException if not able to parse
     *                                                       the utcDateTime string
     *                                                       for the pattern.
     *
     * @see io.mosip.kernel.core.exception.ParseException
     *
     * @see LocalDateTime
     */
    public static LocalDateTime parseUTCToLocalDateTime(String utcDateTime, String pattern) {
        try {
            SimpleDateFormat formatter = getFormatter(pattern, UTC_TIME_ZONE, Locale.getDefault());
            return formatter.parse(utcDateTime)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (ParseException e) {
            throw new io.mosip.kernel.core.exception.ParseException(
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getEexceptionMessage(), e);
        }
    }

    /**
     * Parses Date to java.time.LocalDateTime adjusted for system time zone.
     *
     * @param date is of type String
     *
     * @return a LocalDateTime
     *
     *
     * @see LocalDateTime
     */
    public static LocalDateTime parseDateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Parses given UTC string of ISO pattern <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b> to
     * java.util.Date.
     *
     * @param utcDateTime is of type String
     *
     * @return a Date
     *
     * @throws io.mosip.kernel.core.exception.ParseException if not able to parse
     *                                                       the
     *                                                       <code>utcDateTime</code>
     *                                                       string in given Default
     *                                                       utcDateTime pattern -
     *                                                       <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b>.
     *
     * @see io.mosip.kernel.core.exception.ParseException
     */
    public static Date parseUTCToDate(String utcDateTime) {
        try {
            return DEFAULT_UTC_FORMATTER.get().parse(utcDateTime);
        } catch (ParseException e) {
            throw new io.mosip.kernel.core.exception.ParseException(
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getEexceptionMessage(), e);
        }
    }

    /**
     * Parses UTC string of given pattern to java.util.Date.
     *
     * @param utcDateTime is of type String
     *
     * @param pattern     is of type String
     *
     * @return a Date
     *
     * @throws io.mosip.kernel.core.exception.ParseException if not able to parse
     *                                                       the dateTime string in
     *                                                       given string pattern.
     *
     * @see io.mosip.kernel.core.exception.ParseException
     */
    public static Date parseUTCToDate(String utcDateTime, String pattern) {
        try {
            SimpleDateFormat sdf = getFormatter(pattern, UTC_TIME_ZONE, Locale.getDefault());
            return sdf.parse(utcDateTime);
        } catch (ParseException e) {
            throw new io.mosip.kernel.core.exception.ParseException(
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getEexceptionMessage(), e);
        }
    }

    /**
     * Parses date string of given pattern and TimeZone to java.util.Date.
     *
     * @param dateTime is of type String
     *
     * @param pattern  is of type String
     *
     * @param timeZone is of type java.util.TimeZone
     *
     * @return a Date
     *
     * @throws io.mosip.kernel.core.exception.ParseException if not able to parse
     *                                                       the dateTime string in
     *                                                       given string pattern.
     *
     * @see io.mosip.kernel.core.exception.ParseException
     *
     * @see TimeZone
     */
    public static Date parseToDate(String dateTime, String pattern, TimeZone timeZone) {
        try {
            SimpleDateFormat sdf = getFormatter(pattern, timeZone, Locale.getDefault());
            return sdf.parse(dateTime);
        } catch (ParseException e) {
            throw new io.mosip.kernel.core.exception.ParseException(
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getEexceptionMessage(), e);
        }
    }

    /**
     * Parses date string of given pattern to java.util.Date.
     *
     * @param dateString The date string.
     * @param pattern    The date format pattern which should respect the
     *                   SimpleDateFormat rules.
     * @return The parsed date object.
     * @throws io.mosip.kernel.core.exception.ParseException       If the given date
     *                                                             string or its
     *                                                             actual date is
     *                                                             invalid based on
     *                                                             the given date
     *                                                             format pattern.
     * @throws io.mosip.kernel.core.exception.NullPointerException If
     *                                                             <code>dateString</code>
     *                                                             or
     *                                                             <code>dateFormat</code>
     *                                                             is null.
     * @see SimpleDateFormat
     */
    public static Date parseToDate(String dateString, String pattern) {
        if (Objects.isNull(dateString) || Objects.isNull(pattern)) {
            throw new io.mosip.kernel.core.exception.NullPointerException(
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.ILLEGALARGUMENT_ERROR_CODE.getEexceptionMessage(),
                    new NullPointerException("dateString or dateFormat is null"));
        }
        try {

            SimpleDateFormat sdf = getFormatter(pattern, TimeZone.getDefault(), Locale.getDefault());
            sdf.setLenient(false); // Prevent auto-conversion of invalid dates
            return sdf.parse(dateString);
        } catch (Exception e) {
            throw new io.mosip.kernel.core.exception.ParseException(
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getErrorCode(),
                    DateUtilConstants.PARSE_EXCEPTION_ERROR_CODE.getEexceptionMessage(), e.getCause());
        }
    }

    /**
     * This method to convert “java.util.Date” time stamp to UTC date string
     *
     * @param date The java.util.Date.
     * @return return UTC DateTime format string.
     *
     */
    public static String getUTCTimeFromDate(Date date) {
        return DEFAULT_UTC_FORMATTER.get().format(date);
    }
}