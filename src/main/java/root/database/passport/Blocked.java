package root.database.passport;

import database.snapshot.DBSnapshotList;
import root.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

public class Blocked extends DBSnapshotList<Blocked> {

    public int dwAccountID, nBlockReason;
    public byte bExpired;
    public String sAddr, sHWID, sAccountName;
    public List<String> lAddr = new LinkedList<>(), lHWID = new LinkedList<>();
    public List<Blocked> lBlocked;

    public Blocked(int dwAccountID) {
        super(dwAccountID, true, true);
        this.dwAccountID = dwAccountID;
        this.lBlocked = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public Blocked(int dwAccountID, List<String> lAddr, List<String> lHWID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.lAddr = lAddr;
        this.lHWID = lHWID;
        this.bLoaded = LoadFromDB();
        this.lBlocked = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public Blocked(int dwAccountID, String sAddr, String sHWID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.sAddr = sAddr;
        this.sHWID = sHWID;
        this.lAddr.add(sAddr);
        this.lHWID.add(sHWID);
        this.bLoaded = LoadFromDB();
        this.lBlocked = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public Blocked(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad, true);
        this.dwAccountID = (Integer) dwAccountID;
        this.lBlocked = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    @Override
    public String GetAdditionalArguments() {
        StringBuilder sArgs = new StringBuilder("");
        if (lAddr == null) {
            lAddr = new LinkedList<>();
        }
        if (!lAddr.isEmpty()) {
            for (int i = 0; i < lAddr.size(); i++) {
                String sAddr = lAddr.get(i);
                if (sAddr != null && !sAddr.isBlank()) {
                    sArgs.append(" OR `sAddr` = \"").append(sAddr).append("\"");
                }
            }
        }
        if (lHWID == null) {
            lHWID = new LinkedList<>();
        }
        if (!lHWID.isEmpty()) {
            for (int i = 0; i < lHWID.size(); i++) {
                String sHWID = lHWID.get(i);
                if (sHWID != null && !sHWID.isBlank()) {
                    sArgs.append(" OR `sHWID` = \"").append(sHWID).append("\"");
                }
            }
        }
        return sArgs.toString();
    }

    @Override
    public String DB_GetKey() {
        return "dwAccountID";
    }

    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwAccountID",
                "nBlockReason",
                "bExpired",
                "sAddr",
                "sHWID"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Map.Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Ban Reason", "nBlockReason"),
                new SimpleEntry<>("Expired", "bExpired"),
                new SimpleEntry<>("IP Address", "sAddr"),
                new SimpleEntry<>("HWID", "sHWID")
        };
    }

    public DefaultTableModel GetHistoryTableData() {
        return ToDefaultTableModel(false);
    }
}
