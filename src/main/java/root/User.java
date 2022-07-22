package root;

import app.AdminTool;
import app.UserStorage;
import database.Config;
import database.Database;
import root.database.game.auction.Auction;
import root.database.game.user.inventory.Inventory;
import root.database.game.user.Character;
import root.database.passport.Account;
import root.database.passport.Blocked;
import root.database.passport.LogAccount;
import root.database.passport.LogDonate;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class User {

    public static Map<Integer, String> mAccountIDToName = new HashMap<>();
    public static Map<String, Integer> mAccountNameToID = new HashMap<>();
    public static Map<Integer, User> mAccountIDToUser = new HashMap<>();
    public static Map<Integer, Integer> mCharIDToAccountID = new HashMap<>();
    public static Map<Integer, String> mCharIDToCharName = new HashMap<>();
    public static Map<String, Integer> mCharNameToCharID = new HashMap<>();

    public static String GetBanReason(int dwAccountID) {
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT `nBlockReason` FROM `" + Config.DB_SCHEMA + "`.`account` WHERE `dwAccountID` = \"" + dwAccountID + "\"", Statement.RETURN_GENERATED_KEYS))
        {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int nReason = rs.getInt(1);
                    return nReason == 0 ? "" : " (" + BanReason.GetReason(nReason).name() + ")";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String GetAccountNameFromAccountID(int dwAccountID) {
        if (mAccountIDToName.containsKey(dwAccountID)) {
            return mAccountIDToName.get(dwAccountID);
        }
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT `sAccountClubID` FROM `" + Config.DB_SCHEMA + "`.`account` WHERE `dwAccountID` = \"" + dwAccountID + "\"", Statement.RETURN_GENERATED_KEYS))
        {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sName = rs.getString(1);
                    mAccountNameToID.put(sName, dwAccountID);
                    mAccountIDToName.put(dwAccountID, sName);
                    return sName;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int GetAccountIDFromAccountName(String sAccountClubID) {
        if (mAccountNameToID.containsKey(sAccountClubID)) {
            return mAccountNameToID.get(sAccountClubID);
        }
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT `dwAccountID`, `sAccountClubID` FROM `" + Config.DB_SCHEMA + "`.`account` WHERE `sAccountClubID` = \"" + sAccountClubID + "\"", Statement.RETURN_GENERATED_KEYS))
        {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwAccountID = rs.getInt(1);
                    String sName = rs.getString(2);
                    mAccountIDToName.put(dwAccountID, sName);
                    mAccountNameToID.put(sName, dwAccountID);
                    return dwAccountID;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int GetAccountIDFromCharID(int dwCharacterID) {
        if (mCharIDToAccountID.containsKey(dwCharacterID)) {
            return mCharIDToAccountID.get(dwCharacterID);
        }
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT `dwAccountID`, `sCharacterName` FROM `" + Config.DB_GAME_SCHEMA + "`.`character` WHERE `dwCharacterID` = " + dwCharacterID, Statement.RETURN_GENERATED_KEYS))
        {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwAccountID = rs.getInt(1);
                    String sName = rs.getString(2);
                    mCharIDToAccountID.put(dwCharacterID, dwAccountID);
                    mCharIDToCharName.put(dwCharacterID, sName);
                    mCharNameToCharID.put(sName, dwCharacterID);
                    return dwAccountID;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String GetCharNameFromCharID(int dwCharacterID) {
        if (mCharIDToCharName.containsKey(dwCharacterID)) {
            return mCharIDToCharName.get(dwCharacterID);
        }
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT `dwAccountID`, `sCharacterName` FROM `" + Config.DB_GAME_SCHEMA + "`.`character` WHERE `dwCharacterID` = " + dwCharacterID, Statement.RETURN_GENERATED_KEYS))
        {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwAccountID = rs.getInt(1);
                    String sName = rs.getString(2);
                    mCharIDToAccountID.put(dwCharacterID, dwAccountID);
                    mCharIDToCharName.put(dwCharacterID, sName);
                    mCharNameToCharID.put(sName, dwCharacterID);
                    return sName;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int GetCharIDFromCharName(String sCharacterName) {
        if (mCharNameToCharID.containsKey(sCharacterName)) {
            return mCharNameToCharID.get(sCharacterName);
        }
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT `dwCharacterID`, `dwAccountID`, `sCharacterName` FROM `" + Config.DB_GAME_SCHEMA + "`.`character` WHERE `sCharacterName` = \"" + sCharacterName + "\"", Statement.RETURN_GENERATED_KEYS))
        {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwCharacterID = rs.getInt(1);
                    int dwAccountID = rs.getInt(2);
                    String sName = rs.getString(3);
                    mCharIDToAccountID.put(dwCharacterID, dwAccountID);
                    mCharIDToCharName.put(dwCharacterID, sName);
                    mCharNameToCharID.put(sName, dwCharacterID);
                    return dwCharacterID;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static User GetUser(int dwAccountID) {
        if (!mAccountIDToUser.containsKey(dwAccountID)) {
            String sAccountClubID = GetAccountNameFromAccountID(dwAccountID);
            if (!sAccountClubID.isBlank()) {
                User pUser = new User(sAccountClubID);
                mAccountIDToUser.put(dwAccountID, pUser);
            }
        }
        return mAccountIDToUser.get(dwAccountID);
    }

    public static User GetUser(String sAccountClubID) {
        int dwAccountID = GetAccountIDFromAccountName(sAccountClubID);
        if (dwAccountID > 0) {
            User pUser = GetUser(dwAccountID);
            return pUser;
        }
        return null;
    }

    public int dwAccountID;
    public long tLoadTimeMeasured;
    public boolean bLoaded;
    public String sAccountName, sAccountEmail;
    public Account pAccount;
    public Auction pAuction;
    public Blocked pBlocked;
    public Inventory pInventory;
    public LogAccount pLogAccount;
    public LogDonate pLogDonate;
    public Character pCharacter;
    public List<String> lAddr = new LinkedList<>();
    public List<String> lHWID = new LinkedList<>();
    public Map<AdminTool.ListTypes, DefaultTableModel> mTableData = new ConcurrentHashMap<>(new EnumMap(AdminTool.ListTypes.class));
    public List<Integer> lSuspectedAccounts = new LinkedList<>();
    public Map<String, Integer> mLoginCounts = new ConcurrentHashMap<>();
    public Map<String, Map<Integer, Integer>> mSuspectedAccountsByHWID = new ConcurrentHashMap<>();
    public Map<String, Map<Integer, Integer>> mSuspectedAccountsByIP = new ConcurrentHashMap<>();

    private User(String sAccountClubID) {
        long tTimestamp = System.currentTimeMillis();
        this.dwAccountID = GetAccountIDFromAccountName(sAccountClubID);
        this.sAccountName = GetAccountNameFromAccountID(dwAccountID);
        try { LoadUser(this).get(); } catch (Exception ignore) {} finally {
            this.tLoadTimeMeasured = System.currentTimeMillis() - tTimestamp;
            AdminTool.pInstance.UpdateList();
        }
    }

    public Auction GetAuction() {
       return this.pAuction;
    }

    public Account GetAccount() {
       if (this.pAccount == null) {
           this.pAccount = new Account(dwAccountID);
       }
       return this.pAccount;
    }

    public Blocked GetBlocked() {
       return this.pBlocked;
    }

    public Inventory GetInventory() {
       return this.pInventory;
    }

    public LogAccount GetLogAccount() {
       return this.pLogAccount;
    }

    public LogDonate GetLogDonate() {
       return this.pLogDonate;
    }

    public Character GetCharacter() {
       return this.pCharacter;
    }

    public void DumpToConsole() {
        if (!bLoaded) {
            CompletableFuture.runAsync(() -> {
                while (!bLoaded) try {
                    System.out.println("Not loaded yet... waiting...");
                    Thread.sleep(250);
                    if (bLoaded) break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PrintDump();
            });
        } else {
            PrintDump();
        }
    }

    private void PrintDump() {
        System.out.println(
                "\n-----------------------------------------------------------------------" +
                "\n::  Full User Dump - Account ID: <" + dwAccountID + "> - " + sAccountName +
                "\n-----------------------------------------------------------------------");
        System.out.println(pAccount.ToFlattenedString());
        System.out.println(pBlocked.ToFlattenedString());
        System.out.println(pLogAccount.ToFlattenedString());
        System.out.println(pLogDonate.ToFlattenedString());
        System.out.println(pCharacter.ToFlattenedString());
    }

    public static CompletableFuture<Void> LoadAccountBase(User pUser) {
        return CompletableFuture.runAsync(() -> pUser.pAccount = new Account(pUser.dwAccountID))
                .thenRunAsync(() -> pUser.pLogAccount = new LogAccount(pUser.dwAccountID))
                .thenRunAsync(() -> pUser.pLogAccount = new LogAccount(pUser.dwAccountID, pUser.lAddr, pUser.lHWID))
                .thenRunAsync(() -> pUser.pBlocked = new Blocked(pUser.dwAccountID, pUser.lAddr, pUser.lHWID));
    }

    public static List<Integer> lAlreadyLoaded = new ArrayList<>();

    public static CompletableFuture<Void> LoadUser(final User pUser) {
        if (AdminTool.dwSelectedUser <= 0) {
            AdminTool.dwSelectedUser = pUser.dwAccountID;
        }
        if (lAlreadyLoaded.contains(pUser.dwAccountID)) {
            System.out.println("Trying to load again.");
            return null;
        }
        lAlreadyLoaded.add(pUser.dwAccountID);
        System.out.println("Loading user - " + pUser.sAccountName + " (" + pUser.dwAccountID + ")    [expected dwSelectedUser = " + AdminTool.dwSelectedUser + "]");
        return LoadAccountBase(pUser).thenRunAsync(() -> {
            //try {
                CompletableFuture.allOf(
                    CompletableFuture.runAsync(() -> pUser.mTableData.put(AdminTool.ListTypes.AccountDetails, pUser.GetAccount().ToDefaultTableModel(false))),
                    CompletableFuture.runAsync(() -> pUser.mTableData.put(AdminTool.ListTypes.LogAccountFull, pUser.GetLogAccount().GetFullTableData()))
                            .thenRunAsync(() -> pUser.mTableData.put(AdminTool.ListTypes.LogAccountFlattened, pUser.GetLogAccount().GetFlattenedTableData())),
                    CompletableFuture.runAsync(() -> pUser.mTableData.put(AdminTool.ListTypes.LogBlocked, pUser.GetBlocked().ToDefaultTableModel(false))),
                    CompletableFuture.runAsync(() -> {
                        pUser.pLogDonate = new LogDonate(pUser.dwAccountID, pUser.lAddr, pUser.sAccountEmail);
                        pUser.mTableData.put(AdminTool.ListTypes.LogDonate, pUser.pLogDonate.ToDefaultTableModel(false));
                    })
                ).thenRunAsync(() -> {
                    pUser.pCharacter = new Character(pUser.dwAccountID, true);
                    pUser.mTableData.put(AdminTool.ListTypes.LogCharacter, pUser.GetCharacter().ToDefaultTableModel(false));
                }).thenRunAsync(() -> {
                    pUser.pAuction = new Auction(pUser);
                    pUser.mTableData.put(AdminTool.ListTypes.LogAuction, pUser.pAuction.GetItemTableData());
                    pUser.mTableData.put(AdminTool.ListTypes.LogAuctionHistory, pUser.pAuction.GetHistoryTableData());
                }).thenRunAsync(() -> {
                    pUser.pInventory = new Inventory(pUser);
                    pUser.mTableData.put(AdminTool.ListTypes.LogInventory, pUser.GetInventory().GetItemTableData());
                }).thenRunAsync(() -> {
                    //try {
                        CompletableFuture.allOf(
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogItemBagData, pUser.GetInventory().GetItemBagTableData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogEquips, pUser.GetInventory().GetItemEquipTableData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogLockerItemInfo, pUser.GetInventory().GetLockerItemInfoTableData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogLocker, pUser.GetInventory().GetLockerItemTableData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogLockerEquips, pUser.GetInventory().GetLockerEquipTableData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogTrunk, pUser.GetInventory().GetTrunkTableData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogTrunkItems, pUser.GetInventory().GetTrunkItemData())),
                                CompletableFuture.runAsync(() ->
                                        pUser.mTableData.put(AdminTool.ListTypes.LogTrunkEquips, pUser.GetInventory().GetTrunkEquipData())),
                                CompletableFuture.runAsync(() -> {
                                    pUser.mTableData.put(AdminTool.ListTypes.LogSuspectedAccounts, pUser.GetSuspectedAccountsTableData());
                                    AdminTool.pInstance.AddToUserComboBox(pUser.dwAccountID, pUser.sAccountName, false);
                                }));
                    //} catch (InterruptedException | ExecutionException e) {
                        //e.printStackTrace();
                    //}
                }).whenCompleteAsync((result, throwable) -> {
                    AdminTool.pInstance.UpdateList();
                });
            //} catch (InterruptedException | ExecutionException e) {
                //e.printStackTrace();
            //}
        });
    }

    public int GetNumberOfLogins(String sKey, boolean bHWID, int dwAccID) {
       String sHeaderKey = bHWID ? "sHWID" : "sAddr";
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `" + sHeaderKey + "` = \"" + sKey + "\" AND `dwAccountID` = " + dwAccID, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<String> GetAllHWIDs(int dwAccID) {
       List<String> lHWIDList = new LinkedList<>();
       String sQuery = "SELECT `sHWID` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `dwAccountID` = " + dwAccID;
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sHWID = rs.getString(1);
                    if (!lHWIDList.contains(sHWID)) lHWIDList.add(sHWID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lHWIDList;
    }

    public static List<String> GetAllIPs(int dwAccID) {
        List<String> lIPList = new LinkedList<>();
        String sQuery = "SELECT `sAddr` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `dwAccountID` = " + dwAccID;
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sIP = rs.getString(1);
                    if (!lIPList.contains(sIP)) lIPList.add(sIP);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lIPList;
    }

    public static List<Integer> GetAllPossibleLogins(int dwAccID, boolean bNoIP) {
       List<Integer> lUsers = new LinkedList<>();
        String sAdditionalArgs = "";
        int i = 0;
        for (String sHWID : GetAllHWIDs(dwAccID)) {
            if (i++ > 0) sAdditionalArgs += " OR ";
            sAdditionalArgs += "`sHWID` = \"" + sHWID + "\"";
        }
        if (!bNoIP) {
            for (String sAddr : GetAllIPs(dwAccID)) {
                sAdditionalArgs += " OR `sAddr` = \"" + sAddr + "\"";
            }
        }
        String sQuery = "SELECT `dwAccountID` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE " + sAdditionalArgs;
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwAccountID = rs.getInt(1);
                    if (!lUsers.contains(dwAccountID)) lUsers.add(dwAccountID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lUsers;
    }

    public String GetFirstLogin(String sKey, boolean bHWID) {
       String sHeaderKey = bHWID ? "sHWID" : "sAddr";
       String sQuery = "SELECT `dwAccountID` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `" + sHeaderKey + "` = \"" + sKey + "\"";
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
         sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dwAccountID = rs.getInt(1);
                    return User.GetAccountNameFromAccountID(dwAccountID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GetLastLogin(String sKey, boolean bHWID) {
        String sHeaderKey = bHWID ? "sHWID" : "sAddr";
        String sQuery = "SELECT `dwAccountID` FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `" + sHeaderKey + "` = \"" + sKey + "\"";
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                int dwAccountID = -1;
                while (rs.next()) {
                    dwAccountID = rs.getInt(1);
                }
                return User.GetAccountNameFromAccountID(dwAccountID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GetMoreRecentLogin(String sKey, boolean bHWID, int dwAccountID, int dwCompareID) {
        String sHeaderKey = bHWID ? "sHWID" : "sAddr";
        String sQuery = "SELECT * FROM `" + Config.DB_SCHEMA + "`.`logaccount` WHERE `" + sHeaderKey + "` = \"" + sKey + "\"";
        try (Connection con = Database.connection();
             PreparedStatement ps = con.prepareStatement(
                     sQuery, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                int dwRecentID = -1;
                while (rs.next()) {
                    int dwID = rs.getInt(1);
                    if (dwID == dwAccountID || dwID == dwCompareID) {
                        dwRecentID = dwID;
                    }
                }
                return User.GetAccountNameFromAccountID(dwRecentID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public DefaultTableModel GetSuspectedAccountsTableData() {
         String[] aColumns = new String[] {"Username", "Account ID", "HWID/IP Match", "No. of Matches"};
         List<Integer> lSuspects = GetAllPossibleLogins(dwAccountID, false);
         String[][] aRowValues = new String[256][aColumns.length];
         int nRowIndex = 0;
         int nHWIDIndex = 1;
         for (String sHWID : GetAllHWIDs(dwAccountID)) {
             aRowValues[nRowIndex++] = new String[]{};
             aRowValues[nRowIndex++] = new String[]{"[Match #" + nHWIDIndex++ + " - HWID]", "First Known Account:  " + GetFirstLogin(sHWID, true), sHWID, "-"};
             for (int dwSuspectID : lSuspects) {
                 int nSuspectLoginCounts = GetNumberOfLogins(sHWID, true, dwSuspectID);
                 if (nSuspectLoginCounts > 0) {
                     User pSuspect = User.GetUser(dwSuspectID);
                     if (pSuspect != null && (!UserStorage.bWithQuery || pSuspect.GetAccount().nGradeCode <= 1)) {
                         String sAccountName = pSuspect.sAccountName;
                         if (pSuspect.GetAccount().nBlockReason > 0) {
                             sAccountName += " (Ban:  " + BanReason.GetReason(pSuspect.GetAccount().nBlockReason).name() + ")";
                         }
                         aRowValues[nRowIndex++] = new String[]{
                                 sAccountName,
                                 "" + dwSuspectID,
                                 sHWID,
                                 nSuspectLoginCounts + " login(s)",
                         };
                     }
                 }
             }
         }
         aRowValues[nRowIndex++] = new String[]{};
         int nIPIndex = 1;
         List<String> aIPList = GetAllIPs(dwAccountID);
         for (String sIP : aIPList) {
             if (nIPIndex >= 30) {
                 aRowValues[nRowIndex++] = new String[]{};
                 aRowValues[nRowIndex++] = new String[]{};
                 aRowValues[nRowIndex++] = new String[]{"Not logging any more matches...", (""+(aIPList.size() - nIPIndex)+" IPs not logged"), "-", "RIP VPN Users"};
                 aRowValues[nRowIndex++] = new String[]{};
                 aRowValues[nRowIndex++] = new String[]{};
                 break;
             }
             aRowValues[nRowIndex++] = new String[]{};
             aRowValues[nRowIndex++] = new String[]{"[Match #" + nIPIndex++ + " - IP Address]", "First Known Account:  " + GetFirstLogin(sIP, false), sIP, "-"};
             aRowValues[nRowIndex++] = new String[]{};
             for (int dwSuspectID : lSuspects) {
                 int nSuspectLoginCount = GetNumberOfLogins(sIP, false, dwSuspectID);
                 if (nSuspectLoginCount > 0) {
                     User pSuspect = User.GetUser(dwSuspectID);
                     if (pSuspect != null && (!UserStorage.bWithQuery || pSuspect.GetAccount().nGradeCode <= 1)) {
                         String sAccountName = pSuspect.sAccountName;
                         if (pSuspect.GetAccount().nBlockReason > 0) {
                             sAccountName += " (Ban:  " + BanReason.GetReason(pSuspect.GetAccount().nBlockReason).name() + ")";
                         }
                         aRowValues[nRowIndex++] = new String[]{
                                 sAccountName,
                                 "" + dwSuspectID,
                                 //Blowfish.Encrypt(sIP, Blowfish.CreateSalt()),
                                 sIP,
                                 nSuspectLoginCount + " login(s)",
                         };
                     }
                 }
             }
         }
         DefaultTableModel model = new DefaultTableModel(aRowValues, aColumns);
         return model;
    }
}
