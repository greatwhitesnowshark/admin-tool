package app;

import database.Config;
import database.Database;
import root.BanReason;
import root.User;
import root.database.passport.Account;
import root.database.passport.LogAccount;
import util.FileTime;
import util.Utilities;

import javax.swing.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UserStorage {

    public static Map<String, String> mStringReplace = new LinkedHashMap<>();
    static {
        mStringReplace.put("bBlocked", "Blocked");
        mStringReplace.put("nBlockReason", "Block Reason");
        mStringReplace.put("bEmailVerified", "Email Verified");
        mStringReplace.put("ftRegisterDate", "Registered Date");
        mStringReplace.put("dwAccountID", "Account ID");
        mStringReplace.put("nAccountCash", "Account Cash");
        mStringReplace.put("nDonationPoint", "Donation Point");
        mStringReplace.put("sAccountClubID", "Account Name");
        mStringReplace.put("nBirthDate", "Birthday");
        mStringReplace.put("sID", "Account Email");
        mStringReplace.put("sAddr", "Last Used IP Address");
        mStringReplace.put("sHardwareID", "Last Used Device ID");
        mStringReplace.put("mHWID", "All Device ID Logins / Counts");
        mStringReplace.put("mIP", "All IP Address Logins / Counts");
        mStringReplace = Utilities.SortedMapByValue(mStringReplace, true);
    }

    public static abstract class LoginReferenceCount {
        private Map<Integer, Integer> mLogAccount = Collections.synchronizedMap(new LinkedHashMap<>());
        private String sHWID, sIP;
        private int dwFirstLoggedIn, dwLastLoggedIn;

        public BiFunction<Integer, Integer, Integer> GetLoggedInFirstBFunc = (dwAccountID1, dwAccountID2) -> {
            int nSize = mLogAccount.keySet().size();
            Integer[] aOrderedID = mLogAccount.keySet().toArray(new Integer[nSize]);
            for (int i = 0; i < nSize; i++) {
                if (aOrderedID[i] == dwAccountID1) {
                    return dwAccountID1;
                }
                if (aOrderedID[i] == dwAccountID2) {
                    return dwAccountID2;
                }
            }
            return -1;
        };

        public int GetLoggedInOrderNumber(int dwAccountID) {
            int nSize = mLogAccount.keySet().size();
            Integer[] aOrderedID = mLogAccount.keySet().toArray(new Integer[nSize]);
            for (int i = 1; i <= nSize; i++) {
                if (aOrderedID[i-1] == dwAccountID) {
                    return i;
                }
            }
            return -1;
        }

        public int GetLoggedInFirst(int dwAccountID1, int dwAccountID2) {
            int nSize = mLogAccount.keySet().size();
            Integer[] aOrderedID = mLogAccount.keySet().toArray(new Integer[nSize]);
            for (int i = 0; i < nSize; i++) {
                if (aOrderedID[i] == dwAccountID1) {
                    return dwAccountID1;
                }
                if (aOrderedID[i] == dwAccountID2) {
                    return dwAccountID2;
                }
            }
            return -1;
        }

        LoginReferenceCount(String sHWID, String sIP) {
            this.sHWID = sHWID;
            this.sIP = sIP;
        }

        protected String GetRefString() {
            return !sHWID.isBlank() ? sHWID : sIP;
        }

        public void AddLogin(int dwAccountID) {
            if (!mLogAccount.containsKey(dwAccountID)) {
                mLogAccount.put(dwAccountID, 1);
            } else {
                int nCount = mLogAccount.get(dwAccountID);
                nCount += 1;
                mLogAccount.put(dwAccountID, nCount);
            }
        }

        public int GetNumberOfLogins(int dwAccountID) {
            Integer n = mLogAccount.get(dwAccountID);
            return n != null ? n : 0;
        }

        public Integer[] GetAccountIDs() {
            return mLogAccount.keySet().toArray(new Integer[0]);
        }

        public String[] GetAccountNames() {
            Integer[] aID = GetAccountIDs();
            String[] aAccountName = new String[aID.length];
            for (int i = 0; i < aID.length; i++) {
                String sAccountName = User.GetAccountNameFromAccountID(aID[i]);
                aAccountName[i] = sAccountName + "(" + aID[i] + ")";
            }
            return aAccountName;
        }
    }

    public static class HWIDCount extends LoginReferenceCount {

        HWIDCount(String sHWID) {
            super(sHWID, "");
        }
    }

    public static class IPCount extends LoginReferenceCount {

        IPCount(String sIP) {
            super("", sIP);
        }
    }

    public static Map<Integer, Account> mAccount = new ConcurrentHashMap<>() {
        @Override
        public Account get(Object key) {
            Account a = super.get(key);
            if (a != null) {
                if (bWithQuery) {
                    if (a.nGradeCode > 1) {
                        System.err.println("Returning NULL on Account");
                        return null;
                    }
                }
            }
            return a;
        }
    };
    public static Map<Integer, LogAccount> mLogAccount = new ConcurrentHashMap<>();
    public static Map<String, HWIDCount> mHWIDCount = new ConcurrentHashMap<>();
    public static Map<String, IPCount> mIPCount = new ConcurrentHashMap<>();
    public static Map<String, LinkedList<Integer>> mHWIDFull = new HashMap<>();
    public static Map<String, LinkedList<Integer>> mIPFull = new HashMap<>();

    public static void AddLoginCount(int dwAccountID, String sAddr, String sHWID) {
        if (!sAddr.isBlank()) {
            IPCount pIPCount;
            if (!mIPCount.containsKey(sAddr)) {
                pIPCount = new IPCount(sAddr);
            } else {
                pIPCount = mIPCount.get(sAddr);
            }
            if (pIPCount != null) {
                pIPCount.AddLogin(dwAccountID);
                mIPCount.put(sAddr, pIPCount);
            }
            if (!mIPFull.containsKey(sAddr)) {
                mIPFull.put(sAddr, new LinkedList<>());
            }
            LinkedList<Integer> l = mIPFull.get(sAddr);
            l.add(dwAccountID);
            mIPFull.put(sAddr, l);
        }
        if (!sHWID.isBlank()) {
            HWIDCount pHWIDCount;
            if (!mHWIDCount.containsKey(sHWID)) {
                pHWIDCount = new HWIDCount(sHWID);
            } else {
                pHWIDCount = mHWIDCount.get(sHWID);
            }
            if (pHWIDCount != null) {
                pHWIDCount.AddLogin(dwAccountID);
                mHWIDCount.put(sHWID, pHWIDCount);
            }
            if (!mHWIDFull.containsKey(sHWID)) {
                mHWIDFull.put(sHWID, new LinkedList<>());
            }
            LinkedList<Integer> l = mHWIDFull.get(sHWID);
            l.add(dwAccountID);
            mHWIDFull.put(sHWID, l);
        }
    }

    public static int GetHWIDCount(String sHWID, int dwAccountID) {
        HWIDCount pHWIDCount = mHWIDCount.get(sHWID);
        if (pHWIDCount != null) {
            return pHWIDCount.GetNumberOfLogins(dwAccountID);
        }
        return 0;
    }

    public static int GetIPCount(String sAddr, int dwAccountID) {
        IPCount pIPCount = mIPCount.get(sAddr);
        if (pIPCount != null) {
            return pIPCount.GetNumberOfLogins(dwAccountID);
        }
        return 0;
    }

    public static int GetLastKnownUserHWID(String sHWID) {
        String sQuery = "SELECT `dwAccountID` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `sHWID` = \"" + sHWID + "\" order by `nIncrementKey` desc LIMIT 1";
        try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int GetLastKnownUserIP(String sIP) {
        String sQuery = "SELECT `dwAccountID` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `sAddr` = \"" + sIP + "\" order by `nIncrementKey` desc LIMIT 1";
        try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String GetCharactersList(int dwAccountID) {
        int nCharacterCount = 0;
        StringBuilder sb = new StringBuilder();
        String sQuery = "SELECT dwCharacterID, sCharacterName, nLevel, nJob FROM `" + Config.DB_GAME_SCHEMA + "`.`character` WHERE `dwAccountID` = " + dwAccountID;
        try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nCharacterCount++;
                    int dwCharacterID = rs.getInt(1);
                    String sCharacterName = rs.getString(2);
                    int nLevel = rs.getInt(3);
                    int nJob = rs.getInt(4);
                    sb.append("\r\n\t\t" + Utilities.AddStringPadding(""+dwCharacterID, 24) + Utilities.AddStringPadding(sCharacterName, 24) + Utilities.AddStringPadding("" + nLevel, 24) + Utilities.AddStringPadding(""+nJob, 24));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String s = "";
        if (nCharacterCount > 0) {
            s = "\r\n\t\t" + Utilities.AddStringPadding("------------------------", 24) + Utilities.AddStringPadding("------------------------", 24) + Utilities.AddStringPadding("------------------------", 24) + Utilities.AddStringPadding("------------------------", 24);
            s += "\r\n\t\t" + Utilities.AddStringPadding("Character ID", 24) + Utilities.AddStringPadding("Character Name", 24) + Utilities.AddStringPadding("Level", 24) + Utilities.AddStringPadding("Job ID", 24);
            s += "\r\n\t\t" + Utilities.AddStringPadding("------------------------", 24) + Utilities.AddStringPadding("------------------------", 24) + Utilities.AddStringPadding("------------------------", 24) + Utilities.AddStringPadding("------------------------", 24);
        }
        return "\r\n\tTotal Characters on Account - " + nCharacterCount + "\r\n" + s + "\r\n" + sb.toString();
    }

    public static boolean bTerminate = false;

    public static void DumpSuspectedAccounts(String... aAccountClubID) {
        List<Integer> lAccountID = new LinkedList<>();
        for (String sAccountClubID : aAccountClubID) {
            int dwAccountID = User.GetAccountIDFromAccountName(sAccountClubID);
            if (dwAccountID > 0) {
                lAccountID.add(dwAccountID);
            }
        }
        DumpSuspectedAccounts(lAccountID.toArray(new Integer[0]));
    }

    public static void DumpSuspectedAccounts(Integer[] aAccountID) {
        final StringBuilder sb = new StringBuilder();
        String sBuffer = "*******************************************************************************************************************************************************************";
        List<Integer> lManuallyLoadedAcc = new LinkedList<>();
        int nAccountIdx = 0;
        for (int dwAccountID : aAccountID) {
            nAccountIdx++;
            final int nAccountIndex = nAccountIdx;
            //CompletableFuture.runAsync(() -> {
                try {
                    final List<String> lAccountsList = new LinkedList<>();
                    Account pAccount = mAccount.get(dwAccountID);
                    if (pAccount != null) {
                        if (!pAccount.bIsLogAccountLoaded) {
                            System.err.println("\r\n\r\nLOG ACCOUNT NOT YET LOADED FOR ACCOUNT - " + pAccount.sAccountClubID + " / " + pAccount.dwAccountID);
                        }
                        long tTimestamp = System.currentTimeMillis();
                        String sBanStatus = pAccount.bBlocked > 0 && pAccount.nBlockReason > 0 ?
                                (" (" + BanReason.GetReason(pAccount.nBlockReason).name() + (pAccount.ftUnblockDate == 3439756800000L ? " - Permanent)" : " - Temporary)")) : "";
                        String sText = "#" + nAccountIndex + " of " + aAccountID.length;
                        sb.append(
                                "\r\n" + sBuffer + "" +
                                        "\r\n" + Utilities.AddStringPadding("*  Suspected/Shared Accounts Trace", sBuffer.length() - sText.length() - 3) + sText + "  *" +
                                        "\r\n" + Utilities.AddStringPadding("*  Username:  `" + pAccount.sAccountClubID + "`" + sBanStatus, sBuffer.length() - 1) + "*" +
                                        "\r\n" + Utilities.AddStringPadding("*  Account ID: " + pAccount.dwAccountID, sBuffer.length() - 1) + "*" +
                                        "\r\n" + sBuffer + "");
                        int nIndex = 0;
                        String[] aHWIDList = pAccount.mHWID.keySet().toArray(new String[0]);
                        for (String sHWID : aHWIDList) {
                            nIndex++;
                            HWIDCount pHWIDCount = mHWIDCount.get(sHWID);
                            Integer[] aHWIDCount = pHWIDCount.GetAccountIDs();
                            int dwFirstKnownUser = aHWIDCount[0];
                            int dwLastKnownUser = GetLastKnownUserHWID(sHWID);
                            String sHackFixFirstUserRef = User.GetAccountNameFromAccountID(dwFirstKnownUser);
                            String sHackFixLastKnownUserRef = User.GetAccountNameFromAccountID(dwLastKnownUser);
                            String sFirstKnownUserRef = sHackFixFirstUserRef/*mAccount.get(dwFirstKnownUser).sAccountClubID*/ + " (" + dwFirstKnownUser + ")";
                            String sLastKnownUserRef = sHackFixLastKnownUserRef/*mAccount.get(dwLastKnownUser).sAccountClubID*/ + " (" + dwLastKnownUser + ")";
                            sText = "#" + nIndex + " of " + aHWIDList.length;
                            sb.append("\r\n\r\n\t\t\t" + sBuffer);
                            sb.append("\r\n\t\t\t" + Utilities.AddStringPadding("* Device ID:           " + sHWID, sBuffer.length() - sText.length() - 3) + sText + "  *");
                            sb.append("\r\n\t\t\t" + Utilities.AddStringPadding("* First-Known User:    " + sFirstKnownUserRef, sBuffer.length() - 1) + "*");
                            sb.append("\r\n\t\t\t" + Utilities.AddStringPadding("* Most-Recent User:    " + sLastKnownUserRef, sBuffer.length() - 1) + "*");
                            sb.append("\r\n\t\t\t" + sBuffer);
                            sb.append("\r\n\r\n\t\t\t  (Number of Accounts -  " + aHWIDCount.length + ") (Full List) ->");
                            List<String> sOut = new LinkedList<>();
                            String[] aAccountNames = pHWIDCount.GetAccountNames();
                            for (String s : aAccountNames) {
                                if (!lAccountsList.contains(s)) {
                                    lAccountsList.add(s);
                                }
                            }
                            String sUserList = Utilities.JoinString(aAccountNames, 0, ", ");
                            while (sUserList.length() >= sBuffer.length()) {
                                String sUsrList = sUserList.substring(0, sBuffer.length());
                                sUsrList = sUsrList.substring(0, sUsrList.lastIndexOf(",") + 1);
                                sOut.add(sUsrList);
                                int nLen = sUsrList.length() + 2 > sUserList.length() ? sUserList.length() : sUsrList.length() + 2;
                                sUserList = sUserList.substring(0, nLen);
                            }
                            if (sUserList.startsWith(",")) sUserList = sUserList.substring(1);
                            sOut.add(sUserList.trim());
                            for (String sUsers : sOut) {
                                sb.append("\r\n\t\t\t  " + sUsers);
                            }
                            sb.append("\r\n");
                            int nLoginCount = 0;
                            int nAccIndex = 0;
                            String sAccountQuery = "SELECT * FROM `account` WHERE ";
                            String sLogDonateQuery = "SELECT * FROM `logdonate` WHERE ";
                            String sCharacterQuery = "SELECT * FROM `schema0`.`character` c INNER JOIN (SELECT `dwAccountID`, `sAccountClubID`, from_unixtime(`ftRegisterDate`/1000) as CreateDate, `bBlocked`, `nBlockReason`, from_unixtime(`ftUnblockDate`/1000) as UnblockDate FROM account) a ON c.dwaccountid = a.dwaccountid WHERE ";
                            for (int nAccountID : aHWIDCount) {
                                nLoginCount++;
                                Function<Integer, String> GetSuffix = (n) ->
                                        n % 10 == 1 ? "st" :
                                                n % 10 == 2 ? "nd" :
                                                        n % 10 == 3 ? "rd" : "th";
                                Account pAcc = mAccount.get(nAccountID);
                                if (pAcc == null) { //only load the first 100 so this is null sometimes
                                    pAcc = new Account(nAccountID);
                                    do try {
                                        Thread.sleep(100);
                                    } catch (Exception ignore) {
                                    } while (!pAcc.bIsLogAccountLoaded);
                                    lManuallyLoadedAcc.add(nAccountID);
                                    mAccount.put(nAccountID, pAcc);
                                }
                                if (nAccIndex++ > 0) {
                                    sAccountQuery += " OR ";
                                    sLogDonateQuery += " OR ";
                                    sCharacterQuery += " OR ";
                                }
                                sAccountQuery += "`sAccountClubID` = \"" + pAcc.sAccountClubID + "\"";
                                sLogDonateQuery += "`dwAccountID` = " + pAcc.dwAccountID;
                                sCharacterQuery += "`sAccountClubID` = \"" + pAcc.sAccountClubID + "\"";
                                sBanStatus = pAcc.bBlocked > 0 ?
                                        (" (" + BanReason.GetReason(pAcc.nBlockReason).name() + (pAcc.ftUnblockDate == 3439756800000L ? " - Permanent)" : " - Temporary)")) : "";
                                sb.append("\r\n\t\t\t  " +
                                        Utilities.AddStringPadding((pAcc.dwAccountID == pAccount.dwAccountID ? ("*** " + pAcc.sAccountClubID + sBanStatus + " ***") : pAcc.sAccountClubID + sBanStatus), 48) +
                                        Utilities.AddStringPadding(pHWIDCount.GetNumberOfLogins(nAccountID) + " login(s)", 33) +
                                        (nLoginCount + GetSuffix.apply(nLoginCount) + " user (historically)") + (nLoginCount == 1 ? " -> \"First-Login/Owner\"" : "") + (dwLastKnownUser == nAccountID ? " -> \"Most-Recent-Login\"" : ""));
                            }
                            if (bWithQuery) {
                                sb.append("\r\n\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                sb.append("\r\n\r\n\t\t\tAccount Query: \r\n" +
                                        sAccountQuery + " \r\n" +
                                        "ORDER BY `dwAccountID` asc");
                                sb.append("\r\n\r\n\t\t\tLogDonate Query: \r\n" +
                                        sLogDonateQuery + " \r\n" +
                                        "ORDER BY `nIncrementKey` asc");
                                sb.append("\r\n\r\n\t\t\tCharacter List Query: \r\n" +
                                        sCharacterQuery + " \r\n" +
                                        "ORDER BY `dwAccountID` asc");
                                sb.append("\r\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\r\n");
                            }
                        }
                        nIndex = 0;
                        String[] aIPList = pAccount.mIP.keySet().toArray(new String[0]);
                        for (String sIP : aIPList) {
                            nIndex++;
                            IPCount pIPCount = mIPCount.get(sIP);
                            Integer[] aIPCount = pIPCount.GetAccountIDs();
                            int dwFirstKnownUser = aIPCount[0];
                            int dwLastKnownUser = GetLastKnownUserIP(sIP);
                            String sHackFixFirstUserRef = User.GetAccountNameFromAccountID(dwFirstKnownUser);
                            String sHackFixLastKnownUserRef = User.GetAccountNameFromAccountID(dwLastKnownUser);
                            String sFirstKnownUserRef = sHackFixFirstUserRef/*mAccount.get(dwFirstKnownUser).sAccountClubID*/ + " (" + dwFirstKnownUser + ")" + User.GetBanReason(dwFirstKnownUser);
                            String sLastKnownUserRef = sHackFixLastKnownUserRef/*mAccount.get(dwLastKnownUser).sAccountClubID*/ + " (" + dwLastKnownUser + ")" + User.GetBanReason(dwLastKnownUser);
                            sText = "#" + nIndex + " of " + aIPList.length;
                            sb.append("\r\n\r\n\t\t\t" + sBuffer);
                            sb.append("\r\n\t\t\t" + Utilities.AddStringPadding("* IP Address:          " + sIP, sBuffer.length() - sText.length() - 3) + sText + "  *");
                            sb.append("\r\n\t\t\t" + Utilities.AddStringPadding("* First-Known User:    " + sFirstKnownUserRef, sBuffer.length() - 1) + "*");
                            sb.append("\r\n\t\t\t" + Utilities.AddStringPadding("* Most-Recent User:    " + sLastKnownUserRef, sBuffer.length() - 1) + "*");
                            sb.append("\r\n\t\t\t" + sBuffer);
                            sb.append("\r\n\r\n\t\t\t" + aIPCount.length + " accounts");
                            String[] aAccountNames = pIPCount.GetAccountNames();
                            List<String> sOut = new LinkedList<>();
                            for (String s : aAccountNames) {
                                if (!lAccountsList.contains(s)) {
                                    lAccountsList.add(s);
                                }
                            }
                            String sUserList = Utilities.JoinString(aAccountNames, 0, ", ");
                            String sFUserList = sUserList;
                            while (sUserList.length() > sBuffer.length()) {
                                String sUsrList = sUserList.substring(0, sBuffer.length());
                                sUsrList = sUsrList.substring(0, sUsrList.lastIndexOf(",") + 1);
                                sOut.add(sUsrList);
                                int nLen = sUsrList.length() + 2 > sUserList.length() ? sUserList.length() : sUsrList.length() + 2;
                                sUserList = sUserList.substring(0, nLen);
                            }
                            if (sUserList.startsWith(",")) sUserList = sUserList.substring(1);
                            sOut.add(sUserList.trim());
                            for (String sUsers : sOut) {
                                sb.append("\r\n\t\t\t  " + sUsers);
                            }
                            sb.append("\r\n");
                            int nLoginCount = 0;
                            int nAccIndex = 0;
                            String sAccountQuery = "SELECT * FROM `account` WHERE ";
                            String sLogDonateQuery = "SELECT * FROM `logdonate` WHERE ";
                            String sCharacterQuery = "SELECT * FROM `schema0`.`character` c INNER JOIN (SELECT `dwAccountID`, `sAccountClubID`, from_unixtime(`ftRegisterDate`/1000) as CreateDate, `bBlocked`, `nBlockReason` FROM account) a ON c.dwaccountid = a.dwaccountid WHERE ";
                            for (int nAccountID : aIPCount) {
                                nLoginCount++;
                                Function<Integer, String> GetSuffix = (n) ->
                                        n % 10 == 1 ? "st" :
                                                n % 10 == 2 ? "nd" :
                                                        n % 10 == 3 ? "rd" : "th";
                                Account pAcc = mAccount.get(nAccountID);
                                if (pAcc == null) { //only load the first 100 so this is null sometimes
                                    pAcc = new Account(nAccountID);
                                    lManuallyLoadedAcc.add(nAccountID);
                                    mAccount.put(nAccountID, pAcc);
                                }
                                if (nAccIndex++ > 0) {
                                    sAccountQuery += " OR ";
                                    sLogDonateQuery += " OR ";
                                    sCharacterQuery += " OR ";
                                }
                                sAccountQuery += "`sAccountClubID` = \"" + pAcc.sAccountClubID + "\"";
                                sLogDonateQuery += "`dwAccountID` = " + pAcc.dwAccountID;
                                sCharacterQuery += "`sAccountClubID` = \"" + pAcc.sAccountClubID + "\"";
                                sBanStatus = pAcc.bBlocked > 0 && pAccount.nBlockReason > 0 ?
                                        (" (" + BanReason.GetReason(pAcc.nBlockReason).name() + (pAcc.ftUnblockDate == 3439756800000L ? " - Permanent)" : " - Temporary)")) : "";
                                sb.append("\r\n\t\t\t  " +
                                        Utilities.AddStringPadding((pAcc.dwAccountID == pAccount.dwAccountID ? ("*** " + pAcc.sAccountClubID + sBanStatus + " ***") : pAcc.sAccountClubID + sBanStatus), 48) +
                                        Utilities.AddStringPadding(pIPCount.GetNumberOfLogins(nAccountID) + " login(s)", 33) +
                                        (nLoginCount + GetSuffix.apply(nLoginCount) + " user (historically)") + (nLoginCount == 1 ? " -> \"First-Login/Owner\"" : "") + (dwLastKnownUser == nAccountID ? " -> \"Most-Recent-Login\"" : ""));
                            }
                            if (bWithQuery) {
                                sb.append("\r\n\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                sb.append("\r\n\r\n\t\t\tAccount Query: \r\n" +
                                        sAccountQuery + " \r\n" +
                                        "ORDER BY `dwAccountID` asc");
                                sb.append("\r\n\r\n\t\t\tLogDonate Query: \r\n" +
                                        sLogDonateQuery + " \r\n" +
                                        "ORDER BY `nIncrementKey` asc");
                                sb.append("\r\n\r\n\t\t\tCharacter List Query: \r\n" +
                                        sCharacterQuery + " \r\n" +
                                        "ORDER BY `c.dwAccountID` asc");
                                sb.append("\r\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\r\n");
                            }
                        }

                        if (bWithQuery) {
                            String sr = "INSERT INTO `centerrequest` (`dwRequest`, `sResult`, `nWorldID`) VALUES ";
                            sb.append("\r\n\r\n\tAll associated accounts - ");
                            int i = 0;
                            for (String s : lAccountsList) {
                                sb.append("\r\n\t\t" + s);
                                if (i++ > 0) {
                                    sr += ", ";
                                }
                                sr += "(4, \"Account=" + s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                                sr += ";Reason=4;Duration=-1\", -1)";
                            }
                            sb.append("\r\n\r\n\t" + sr);
                        }

                        sb.append("\r\n\r\n\r\n\t\t\tCompleted for account in:  " + String.format("%.2f", ((((double) (System.currentTimeMillis() - tTimestamp)) / 1000))) + " seconds");
                        sb.append("\r\n\t\t\tNumber of other accounts loaded in the process that affected the compilation time: " + lManuallyLoadedAcc.size() + "\r\n");
                    } else {
                        System.err.println("[ERROR] PRESENTING A NULL ACCOUNT FOR ID " + dwAccountID);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(sb.toString());
            //});
        }
        bTerminate = true;
    }

    public static AtomicLong nThreadCounter = new AtomicLong(1);
    public static AtomicLong nCounter = new AtomicLong(0);

    public static boolean bWithQuery = false;

    public static void main(String[] args) {
        ExecutorService pMainExec = Executors.newSingleThreadExecutor();
        long tTimeStamp = System.currentTimeMillis();
        String[] aUsername = {"kcl123"};
        LinkedList<Integer> lAccountID = new LinkedList<>();
        for (String sUsername : aUsername) {
            int dwAccountID = User.GetAccountIDFromAccountName(sUsername);
            if (dwAccountID > 0) {
                lAccountID.add(dwAccountID);
            }
        }
        int nDumpCount = lAccountID.size();
        for (int dwAccountID : lAccountID) try {
            CompletableFuture.runAsync(() -> {
                Thread.currentThread().setName("Thread #" + nThreadCounter.incrementAndGet());
                Account pAccount = new Account(dwAccountID); //should get a print after this is loaded
                mAccount.put(dwAccountID, pAccount);
                System.out.println("\r\n=====================================\r\nAccount Details  -  " + pAccount.sAccountClubID + "\r\n=====================================\r\n");
                for (String sFieldName : mStringReplace.keySet().toArray(new String[0])) try {
                    java.lang.reflect.Field f = pAccount.getClass().getDeclaredField(sFieldName);
                    if (f.getName().contains("sSPW") || f.getName().contains("sPassword")) {
                        continue;
                    }
                    try {
                        Object o = f.get(pAccount);
                        if (o != null) {
                            if (o instanceof Number || f.getType().isPrimitive()) {
                                Class<?> type = f.getType();
                                long nVal = 0;
                                if (type == int.class) {
                                    nVal = (Integer) o;
                                } else if (type == long.class) {
                                    nVal = (Long) o;
                                    if (nVal == 3439756800000L) {
                                        continue;
                                    }
                                    if (f.getName().contains("ftRegisterDate") || f.getName().contains("ftExpire")) {
                                        String sOut = "\t\t" + Utilities.AddStringPadding((mStringReplace.get(f.getName()) != null ? mStringReplace.get(f.getName()) : f.getName()), 36) + " -> " + new FileTime(nVal).toString();
                                        System.out.println(sOut);
                                        continue;
                                    }
                                } else if (type == short.class) {
                                    nVal = (Short) o;
                                } else if (type == byte.class) {
                                    nVal = (Byte) o;
                                }
                                if (nVal <= 0) {
                                    continue;
                                }
                            } else if (o instanceof String) {
                                String sVal = (String) o;
                                if (sVal.isBlank() || sVal.equalsIgnoreCase("null")) {
                                    continue;
                                }
                            }

                            String sOut = "\t\t" + Utilities.AddStringPadding(mStringReplace.get(sFieldName), 36) + " -> " + o;
                            System.out.println(sOut);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String s = GetCharactersList(dwAccountID);
                System.out.println(s);
                LogAccount pLogAccount = new LogAccount(pAccount.dwAccountID, new ArrayList<>(pAccount.mIP.keySet()), new ArrayList<>(pAccount.mHWID.keySet()));
                mLogAccount.put(pAccount.dwAccountID, pLogAccount);
                for (LogAccount la : pLogAccount.lLogAccount) {
                    AddLoginCount(la.dwAccountID, la.sAddr, la.sHWID);
                }
                if (nCounter.incrementAndGet() == nDumpCount) {
                    bTerminate = true;
                }
            }, pMainExec)
            .thenRunAsync(() -> {
                Integer[] aAccountID = mAccount.keySet().toArray(new Integer[0]);
                DumpSuspectedAccounts(aAccountID);
                mAccount.clear();
            }, pMainExec).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Press any key to terminate program ...\r\n");
            System.in.read();
        } catch (Exception ignore) {} finally {
            pMainExec.shutdown();
        }
    }

    public static void DumpFirst100Accounts() {
        int nDumpCount = 100;
        long tTimeStamp = System.currentTimeMillis();
        List<Integer> lAccountID = new ArrayList<>();
        try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement("SELECT `dwAccountID` from `schema`.`account` LIMIT " + nDumpCount, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwAccountID = rs.getInt(1);
                    if (!lAccountID.contains(dwAccountID)) {
                        lAccountID.add(dwAccountID);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int dwAccountID : lAccountID) {
            CompletableFuture.runAsync(() -> {
                Thread.currentThread().setName("Thread #" + nThreadCounter.incrementAndGet());
                Account pAccount = new Account(dwAccountID); //should get a print after this is loaded
                mAccount.put(dwAccountID, pAccount);
                LogAccount pLogAccount = new LogAccount(pAccount.dwAccountID, new ArrayList<>(pAccount.mIP.keySet()), new ArrayList<>(pAccount.mHWID.keySet()));
                mLogAccount.put(pAccount.dwAccountID, pLogAccount);
                for (LogAccount la : pLogAccount.lLogAccount) {
                    AddLoginCount(la.dwAccountID, la.sAddr, la.sHWID);
                }
                if (nCounter.incrementAndGet() == nDumpCount) {
                    bTerminate = true;
                }
            });
        }
        do try {
            Thread.sleep(100);
        } catch (Exception ignored) {} while (!bTerminate);
        bTerminate = false;
        Integer[] aAccountID = mAccount.keySet().toArray(new Integer[0]);
        Integer[] aSubset = new Integer[nDumpCount];
        System.arraycopy(aAccountID, 0, aSubset, 0, nDumpCount);

        DumpSuspectedAccounts(aSubset);
        do try {
            Thread.sleep(100);
        } catch (Exception ignored) {} while (!bTerminate);
        System.out.println(Thread.currentThread().getName() + " - Waiting to terminate any time you are ready.");
        try {
            System.in.read();
        } catch (Exception ignore) {}
    }
}
