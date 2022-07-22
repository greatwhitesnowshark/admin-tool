/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Date;

public class FileTime {

    public int dwHighDateTime, dwLowDateTime;
    public static final long // Time Intervals
            Millisecond = 10000,
            Second = 10000000,
            Minute = 600000000L,
            Hour = 36000000000L,
            Day = 864000000000L,
            Month = 24192000000000L,
            Year = 290304000000000L,
            EpochBias = 11644473600000L;

    public static final FileTime // Dates
            Date1900 = new FileTime(4259332096L, 21968699L), //Sun Dec 31 16:00:00 PST 1899 | -2208988800000
            Date2079 = new FileTime(3137699840L, 35120710L), // | 3439756800000
            Date2078 = new FileTime(-1157267456L, 35120710L), // Permanent
            Start = Date1900,
            Default = Date2079,
            End = Date2079,
            None = new FileTime(0, 0);

    public FileTime() {
        this.dwLowDateTime = 0;
        this.dwHighDateTime = 0;
    }

    public FileTime(long nMilliseconds) {
        nMilliseconds += EpochBias;
        nMilliseconds *= Millisecond;
        this.dwLowDateTime = (int) (nMilliseconds & 0xFFFFFFFFL);
        this.dwHighDateTime = (int) (nMilliseconds >> 32 & 0xFFFFFFFFL);
    }

    public FileTime(long dwLowDateTime, long dwHighDateTime) {
        this.dwLowDateTime = (int) dwLowDateTime;
        this.dwHighDateTime = (int) dwHighDateTime;
    }

    public FileTime(String sDate, boolean bStart) {
        if (sDate != null && !sDate.isBlank()) {
            int nYear = Integer.valueOf(sDate.substring(0, 4));
            int nMonth = Integer.valueOf(sDate.substring(4, 6));// - 1; // Calendar is 0-11
            int nDay = Integer.valueOf(sDate.substring(6, 8));
            int nHour = Integer.valueOf(sDate.substring(8, 10));
            int nMinute = 0;
            if (sDate.length() > 10) {
                nMinute = Integer.valueOf(sDate.substring(10, 12));
            }
            SystemTime st = new SystemTime(nYear, nMonth, nDay, nHour, nMinute, 0);
            FileTime ft = SystemTimeToFileTime(st);
            this.dwLowDateTime = ft.dwLowDateTime;
            this.dwHighDateTime = ft.dwHighDateTime;
        } else {
            if (bStart) {
                FileTime ft = Start;
                this.dwHighDateTime = ft.dwHighDateTime;
                this.dwLowDateTime = ft.dwLowDateTime;
            } else {
                FileTime ft = End;
                this.dwHighDateTime = ft.dwHighDateTime;
                this.dwLowDateTime = ft.dwLowDateTime;
            }
        }
    }

    public FileTime(int nDate) {
        String sDate = Integer.toString(nDate);
        if(sDate.length() == 8) { // YYYYMMDD
            int nYear = Integer.valueOf(sDate.substring(0, 4));
            int nMonth = Integer.valueOf(sDate.substring(4, 6));// - 1; // Calendar is 0-11
            int nDay = Integer.valueOf(sDate.substring(6, 8));
            SystemTime st = new SystemTime(nYear, nMonth, nDay, 0, 0, 0);
            FileTime ft = SystemTimeToFileTime(st);
            this.dwLowDateTime = ft.dwLowDateTime;
            this.dwHighDateTime = ft.dwHighDateTime;
        }
    }

    public static int CompareFileTime(FileTime ft1, FileTime ft2) {
        if (ft1.GetTime() < ft2.GetTime()) {
            return -1;
        } else if (ft1.GetTime() == ft2.GetTime()) {
            return 0;
        } else if (ft1.GetTime() > ft2.GetTime()) {
            return 1;
        }
        return 0;
    }

    public long GetFileTime() {
        return (long) dwHighDateTime << 32 | dwLowDateTime & 0xFFFFFFFFL;
    }

    public long GetTime() {
        return (GetFileTime() / Millisecond) - EpochBias;
    }

    public static FileTime GetSystemTime() {
        return new FileTime(System.currentTimeMillis());
    }

    public static FileTime FromCurrentTime(long nMilliseconds) {
        return new FileTime(System.currentTimeMillis() + nMilliseconds);
    }

    public static FileTime SystemTimeToFileTime(SystemTime stTime) {
        return new FileTime(stTime.GetTime());
    }

    public SystemTime FileTimeToSystemTime(FileTime ftTime) {
        return new SystemTime(ftTime.GetTime());
    }

    public void Add(long nType, long nValue) {
        long tTime = nType * nValue;
        tTime += GetFileTime();
        dwHighDateTime = (int) (tTime >> 32 & 0xFFFFFFFFL);
        dwLowDateTime = (int) (tTime & 0xFFFFFFFFL);
    }

    public static void Subtract(FileTime ftFrom, FileTime ftTo, Pointer<Integer> pnDay, Pointer<Integer> pnHour, Pointer<Integer> pnMin, Pointer<Integer> pnSec) {
        long tTime = ((ftFrom.dwHighDateTime - (long)ftTo.dwHighDateTime) << 32) - ftTo.dwLowDateTime + ftFrom.dwLowDateTime;
        if (pnDay != null) {
            pnDay.Set((int) (tTime / FileTime.Day));
        }
        if (pnHour != null) {
            pnHour.Set((int) ((tTime / FileTime.Hour) % 24));
        }
        if (pnMin != null) {
            pnMin.Set((int) ((tTime / FileTime.Minute) % 60));
        }
        if (pnSec != null) {
            pnSec.Set((int) ((tTime / FileTime.Second) % 60));
        }
    }

    @Override
    public String toString() {
        return new Date(FileTimeToSystemTime(this).GetTime()).toString();
    }
}