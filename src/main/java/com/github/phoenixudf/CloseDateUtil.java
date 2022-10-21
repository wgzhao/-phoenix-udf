package com.github.phoenixudf;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CloseDateUtil
{
    public static final String closePath = "/closedate.dat";

    private static final List<String> closeDateList = new ArrayList<>();

    static {
        InputStream in = null;
        try {
            in = CloseDateUtil.class.getResourceAsStream(closePath);
            closeDateList.addAll(IOUtils.readLines(in, StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                IOUtils.closeQuietly(in);
            }
        }
    }

    public static boolean isCloseDate(final String date)
    {
        return closeDateList.contains(date);
    }

    public static String getNextExchangeDay(final String date)
    {
        String nextDate = nextDay(date);
        while (!isCloseDate(nextDate)) {
            nextDate = nextDay(nextDate);
            if (nextDate.compareTo(getMaxExchangeDay()) > 0) {
                return null;
            }
        }
        return nextDate;
    }

    public static String getLastExchangeDay(final String date)
    {
        // 如果传递的日期恰好是交易日列表的第一天，则需要直接返回null，否则陷入死循环
        if (date.equals(closeDateList.get(0))) {
            return null;
        }
        String lastDate = lastDay(date);
        if (lastDate == null) {
            return null;
        }
        while (!isCloseDate(lastDate)) {
            if (lastDate.compareTo(closeDateList.get(0)) < 0) {
                return null;
            }
            lastDate = lastDay(lastDate);
            if (lastDate == null) {
                return null;
            }
        }
        return lastDate;
    }

    public static String getPeriodExchangeDay(final String date, final int day)
    {
        String lastDate = date;
        while (!closeDateList.contains(lastDate)) {
            lastDate = lastDay(lastDate);
            if (lastDate == null) {
                return null;
            }
            if (lastDate.compareTo(getMinExchangeDay()) < 0 || lastDate.compareTo(getMaxExchangeDay()) > 0) {
                return null;
            }
        }
        final int i = closeDateList.indexOf(lastDate);
        try {
            return closeDateList.get(i + day);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static String getFirstPeriodExchangeDay(final String date, final int day)
    {
        String lastDate = addDate(date, day);
        if (lastDate == null) {
            return null;
        }
        while (!closeDateList.contains(lastDate)) {
            if (day < 0) {
                if (closeDateList.isEmpty() || closeDateList.get(closeDateList.size() - 1).compareTo(lastDate) < 0) {
                    return null;
                }
                lastDate = nextDay(lastDate);
            }
            else {
                if (closeDateList.isEmpty() || closeDateList.get(0).compareTo(lastDate) > 0) {
                    return null;
                }
                lastDate = lastDay(lastDate);
            }
        }
        return lastDate;
    }

    /**
     * type: 1 = week 2 = month 3 = year
     *
     * @param date date
     * @param type the type will be extract, type: 1 = week 2 = month 3 = year
     * @return the extracted date part
     * @throws IOException if occurred
     */
    public static String getFirstCalendarExchangeDay(final String date, final int type)
            throws IOException
    {
        String lastDate = getFirstCalendarDay(date, type);
        while (!closeDateList.contains(lastDate)) {
            lastDate = nextDay(lastDate);
            if (lastDate == null) {
                return null;
            }
            if (lastDate.compareTo(closeDateList.get(closeDateList.size() - 1)) > 0) {
                throw new IOException("index out of closeDateList[" + lastDate + "]");
            }
        }
        return lastDate;
    }

    public static String getFirstCalendarDay(final String date, final int type)
    {
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            c.setTime(sdf.parse(date));
        }
        catch (final ParseException e) {
            e.printStackTrace();
            return null;
        }
        if (type == 1) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        else if (type == 2) {
            c.set(Calendar.DAY_OF_MONTH, 1);
        }
        else if (type == 3) {
            c.set(Calendar.DAY_OF_YEAR, 1);
        }
        return sdf.format(c.getTime());
    }

    /*
     * type: 1 = week 2 = month 3 = year
     */
    public static String getLastCalendarExchangeDay(final String date, final int type)
            throws IOException
    {
        String lastDate = getLastCalendarDay(date, type);
        if (lastDate == null) {
            return null;
        }
        while (!closeDateList.contains(lastDate)) {
            lastDate = lastDay(lastDate);
            if (lastDate == null) {
                return null;
            }
            if (lastDate.compareTo(closeDateList.get(0)) < 0) {
                throw new IOException("index out of closeDateList[" + lastDate + "]");
            }
        }
        return lastDate;
    }

    /*
     * type: 1 = week 2 = month 3 = year
     */
    public static String getLastCalendarDay(final String date, final int type)
    {
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            c.setTime(sdf.parse(date));
        }
        catch (final ParseException e) {
            e.printStackTrace();
            return null;
        }
        if (type == 1) {
            c.set(Calendar.DAY_OF_WEEK, c.getActualMaximum(Calendar.DAY_OF_WEEK));
        }
        else if (type == 2) {
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        else if (type == 3) {
            c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
        }
        return sdf.format(c.getTime());
    }

    public static long abs(final long l1, final long l2)
    {
        return l1 > l2 ? l1 - l2 : l2 - l1;
    }

    public static long hsDateDiff(final String date1, final String date2)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            final Date d1 = sdf.parse(date1);
            final Date d2 = sdf.parse(date2);

            return TimeUnit.MILLISECONDS.toDays(abs(d1.getTime(), d2.getTime()));
        }
        catch (final ParseException e) {
            return -1;
        }
    }

    public static String toHsDate(final String date)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        try {
            return sdf2.format(sdf.parse(date));
        }
        catch (final ParseException e) {
            return null;
        }
    }

    public static String fromHsDate(final String date)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf2.format(sdf.parse(date));
        }
        catch (final ParseException e) {
            return null;
        }
    }

    public static String addDate(final String date, final int days)
    {
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            c.setTime(sdf.parse(date));
        }
        catch (final ParseException e) {
            e.printStackTrace();
            return null;
        }
        c.add(Calendar.DAY_OF_MONTH, days);
        return sdf.format(c.getTime());
    }

    public static String nextDay(final String date)
    {
        return addDate(date, 1);
    }

    public static String lastDay(final String date)
    {
        return addDate(date, -1);
    }

    public static String getPrevCloseDay(final String date, final String type)
    {
        String newDate;
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            c.setTime(sdf.parse(date));
        }
        catch (final ParseException e) {
            e.printStackTrace();
            return null;
        }

        switch (type) {
            case "month":
                c.add(Calendar.MONTH, -1);
                break;
            case "quoter":
                c.add(Calendar.MONTH, -3);
                break;
            case "halfyear":
                c.add(Calendar.MONTH, -6);
                break;
            case "year":
                c.add(Calendar.YEAR, -1);
                break;
            default:
                c.add(Calendar.MONTH, -1);
        }
        newDate = sdf.format(c.getTime());
        return CloseDateUtil.getNextExchangeDay(newDate);
    }

    public static int getCountExchangeDay(final String date1, final String date2)
    {
        if (date1 == null || date2 == null) {
            return 0;
        }
        String lastDate = date1;
        int count = 0;
        while (lastDate.compareTo(date2) <= 0) {
            if (closeDateList.contains(lastDate)) {
                count++;
            }
            lastDate = nextDay(lastDate);
            if (lastDate == null) {
                return 0;
            }
        }

        return count;
    }

    /**
     * 获取当前交易日文件的最大日期
     *
     * @return the max trade date
     */
    public static String getMaxExchangeDay()
    {
        return closeDateList.get(closeDateList.size() - 1);
    }

    /**
     * 获取当前交易文件的最小交易日
     *
     * @return the min trade date
     */
    public static String getMinExchangeDay()
    {
        return closeDateList.get(0);
    }
}
