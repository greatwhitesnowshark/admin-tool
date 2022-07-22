/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
public class SystemTime {

    public LocalDateTime pLocalDT = null;
    public int wYear, wMonth, wDay, wDayOfWeek, wHour, wMinute, wSecond;
    public static final int Sunday = 0, Monday = 1, Tuesday = 2, Wednesday = 3, Thursday = 4, Friday = 5, Saturday = 6;
    public static final SystemTime Default = GetLocalTime();

    public SystemTime(long nMilliseconds) {
        this.pLocalDT = LocalDateTime.ofInstant(Instant.ofEpochMilli(nMilliseconds), ZoneId.systemDefault());
        this.wYear = this.pLocalDT.getYear();
        this.wMonth = this.pLocalDT.getMonthValue();
        this.wDayOfWeek = this.pLocalDT.getDayOfWeek().getValue();
        this.wDay = this.pLocalDT.getDayOfMonth();
        this.wHour = this.pLocalDT.getHour();
        this.wMinute = this.pLocalDT.getMinute();
        this.wSecond = this.pLocalDT.getSecond();
    }

    public SystemTime(long nMilliseconds, boolean bUTCTime) {
        this.pLocalDT = LocalDateTime.ofInstant(Instant.ofEpochMilli(nMilliseconds), bUTCTime ? ZoneOffset.UTC.normalized() : ZoneId.systemDefault());
        this.wYear = this.pLocalDT.getYear();
        this.wMonth = this.pLocalDT.getMonthValue();
        this.wDayOfWeek = this.pLocalDT.getDayOfWeek().getValue();
        this.wDay = this.pLocalDT.getDayOfMonth();
        this.wHour = this.pLocalDT.getHour();
        this.wMinute = this.pLocalDT.getMinute();
        this.wSecond = this.pLocalDT.getSecond();
    }

    public SystemTime(int nYear, int nMonth, int nDay, int nHour, int nMin, int nSec) {
        Set(nYear, nMonth, nDay, nHour, nMin, nSec);
        this.wYear = nYear;
        this.wMonth = nMonth;
        this.wDay = nDay;
        this.wDayOfWeek = pLocalDT.getDayOfWeek().getValue();
        this.wHour = nHour;
        this.wMinute = nMin;
        this.wSecond = nSec;
    }

    public final void Set(int nYear, int nMonth, int nDay, int nHour, int nMin, int nSec) {
        pLocalDT = LocalDateTime.of(nYear, nMonth, nDay, nHour, nMin, nSec);
    }

    public void SetYear(int nYear) {
        this.pLocalDT = pLocalDT.withYear(nYear);
        this.wYear = nYear;
    }

    public void SetMonth(int nMonth) {
        this.pLocalDT = pLocalDT.withMonth(nMonth);
        this.wMonth = nMonth;
    }

    public void SetDay(int nDay) {
        this.pLocalDT = pLocalDT.withDayOfMonth(nDay);
        this.wDay = nDay;
    }

    public void SetHour(int nHour) {
        this.pLocalDT = pLocalDT.withHour(nHour);
        this.wHour = nHour;
    }

    public void SetMinute(int nMin) {
        this.pLocalDT = pLocalDT.withMinute(nMin);
        this.wMinute = nMin;
    }

    public void SetSecond(int nSec) {
        this.pLocalDT = pLocalDT.withSecond(nSec);
        this.wSecond = nSec;
    }

    public static int CompareSystemTime(SystemTime st1, SystemTime st2) {
        if (st1.GetTime() < st2.GetTime()) {
            return -1;
        } else if (st1.GetTime() == st2.GetTime()) {
            return 0;
        } else if (st1.GetTime() > st2.GetTime()) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        pLocalDT.format(formatter);
        return pLocalDT.toString();
    }

    public String toString(String sFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(sFormat);
        pLocalDT.format(formatter);
        return pLocalDT.toString();
    }

    public static SystemTime FileTimeToSystemTime(FileTime ftCur) {
        return new SystemTime(ftCur.GetTime());
    }

    public static SystemTime GetLocalTime() {
        return new SystemTime(System.currentTimeMillis());
    }

    public long GetTime() {
        return pLocalDT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
