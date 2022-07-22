package root.database.passport;

import database.Database;
import database.snapshot.DBSnapshot;
import root.BanReason;
import util.FileTime;

import javax.swing.table.TableModel;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CompletableFuture;

public class Account extends DBSnapshot {

    public boolean bIndexByAcctID;
    public byte bBlocked, bAdminClient, bEmailVerified, nBuyCharCount;
    public long ftRegisterDate, ftUnblockDate;
    public int dwAccountID, nMigrateState, nBlockReason, nChatBlockReason, nGradeCode, nAccountCredit, nMaplePoint,
            nRewardPoint, nAccountCash, nDonationPoint, nVotePoint;
    public String sAccountClubID, sPassword, sSPW, nBirthDate, sID, sAddr, sHardwareID, sMacAddress, sResetKey;
    public Map<String, Integer> mHWID = new LinkedHashMap<>();
    public Map<String, Integer> mIP = new LinkedHashMap<>();
    public volatile boolean bIsLogAccountLoaded = false;

    public Account(String sAccountClubID) {
        super(sAccountClubID, true, false);
        LoadLogAccountCounts();
    }

    public Account(int dwAccountID) {
        super(dwAccountID, false, false);
        if (dwAccountID > 0) {
            this.dwAccountID = dwAccountID;
            this.bIndexByAcctID = true;
            this.bLoaded = LoadFromDB();
        } else {
            this.bLoaded = LoadFromDB();
        }
        LoadLogAccountCounts();
    }

    public void LoadLogAccountCounts() {
        CompletableFuture.runAsync(() -> {
            try {
                List<String> lAddr = new ArrayList<>();
                List<String> lHWID = new ArrayList<>();
                try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement("SELECT sAddr, sHWID from `schema`.`logaccount` WHERE `dwAccountID` = ?", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, dwAccountID);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String sIP = rs.getString(1);
                            String sHWID = rs.getString(2);
                            lAddr.add(sIP);
                            lHWID.add(sHWID);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                CompletableFuture.allOf(
                        CompletableFuture.runAsync(() -> {
                            for (String sAddr : lAddr) {
                                if (sAddr != null && !sAddr.isBlank()) {
                                    if (!mIP.containsKey(sAddr)) {
                                        mIP.put(sAddr, 1);
                                    } else {
                                        int nCount = mIP.get(sAddr);
                                        nCount += 1;
                                        mIP.put(sAddr, nCount);
                                    }
                                }
                            }
                        }),
                        CompletableFuture.runAsync(() -> {
                            for (String sHWID : lHWID) {
                                if (sHWID != null && !sHWID.isBlank()) {
                                    if (!mHWID.containsKey(sHWID) && !sHWID.isBlank()) {
                                        mHWID.put(sHWID, 1);
                                    } else {
                                        int nCount = mHWID.get(sHWID);
                                        nCount += 1;
                                        mHWID.put(sHWID, nCount);
                                    }
                                }
                            }
                        })
                ).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.bIsLogAccountLoaded = true;
        }).join();
    }

    @Override
    public String DB_GetKey() {
        return bIndexByAcctID ? "dwAccountID" : "sAccountClubID";
    }

    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwAccountID",
                "sAccountClubID",
                "sPassword",
                "sSPW",
                "nMigrateState",
                "ftRegisterDate",
                "nBirthDate",
                "bBlocked",
                "nBlockReason",
                "nChatBlockReason",
                "ftUnblockDate",
                "bAdminClient",
                "nGradeCode",
                "sID",
                "bEmailVerified",
                "nAccountCredit",
                "nMaplePoint",
                "nRewardPoint",
                "nAccountCash",
                "nDonationPoint",
                "nVotePoint",
                "sAddr",
                "sHardwareID",
                "sMacAddress",
                "nBuyCharCount",
                "sResetKey"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Username", "sAccountClubID"),
                new SimpleEntry<>("Birthday", "nBirthDate"),
                new SimpleEntry<>("Blocked/Banned", "bBlocked"),
                new SimpleEntry<>("Ban Reason", "nBlockReason"),
                new SimpleEntry<>("Ban Expiration", "ftUnblockDate"),
                new SimpleEntry<>("Email Address", "sID"),
                new SimpleEntry<>("IP", "sAddr"),
                new SimpleEntry<>("HWID", "sHardwareID"),
                new SimpleEntry<>("Account Cash", "nAccountCash"),
                new SimpleEntry<>("Donation Points", "nDonationPoint"),
                new SimpleEntry<>("Vote Points", "nVotePoint"),
                new SimpleEntry<>("Registered Date", "ftRegisterDate")
        };
    }

    public String ToStringSuspectedAccount(String sHeader1, String sHeader2) {
        StringBuilder sDump = new StringBuilder();
        sDump.append("\n    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        sDump.append(("\n      Suspected Account"));
        sDump.append((String.format("\n      %s (ID: %d)", sAccountClubID, dwAccountID)));
        sDump.append("\n    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        for (Entry<String, String> pEntry : GetLoggedColumnNames()) {
            String sColumn = pEntry.getValue();
            Object pValue = mSnapshot.get(sColumn);
            if (pValue != null) {
                String sValue;
                if (pEntry.getKey().equals("Ban Reason")) {
                    BanReason pBanReason = BanReason.GetReason((Integer) pValue);
                    sValue = pBanReason.name();
                } else if (pEntry.getKey().equals("Registered Date")) {
                    FileTime ft = new FileTime((Long) pValue);
                    sValue = ft.toString();
                } else {
                    if (pValue.getClass() == String.class) {
                        sValue = "\"" + pValue + "\"";
                    } else {
                        sValue = "" + pValue;
                    }
                }
                sDump.append(String.format("\n           %s\n                   %s", (pEntry.getKey()), sValue));
            }
        }
        sDump.append("\n\n           Reasons/flags for assuming shared account ownership between `" + sHeader1 + "` and `" + sHeader2 + "` -\n");
        return sDump.toString();
    }

    public TableModel GetDetailsTableData() {
        return ToDefaultTableModel(false);
    }
}
