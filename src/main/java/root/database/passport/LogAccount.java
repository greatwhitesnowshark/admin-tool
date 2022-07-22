package root.database.passport;

import database.snapshot.DBSnapshotList;
import root.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class LogAccount extends DBSnapshotList<LogAccount> {

    public int dwAccountID;
    public String sAddr, sHWID, sAccountName;
    public List<String> lAddr = new LinkedList<>(), lHWID = new LinkedList<>();
    public LinkedList<LogAccount> lLogAccount;

    public LogAccount(int dwAccountID) {
        super(dwAccountID, true, true);
        this.dwAccountID = dwAccountID;
        this.lLogAccount = GetLoadedList();
        this.sAccountName = GetAccountName();
    }

    public LogAccount(int dwAccountID, String sHWID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.sHWID = sHWID;
        this.lHWID.add(sHWID);
        this.bLoaded = LoadFromDB();
        this.lLogAccount = GetLoadedList();
        this.sAccountName = GetAccountName();
    }

    public LogAccount(int dwAccountID, List<String> lAddr, List<String> lHWID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.pKeyValue = this.dwAccountID;
        this.lAddr = lAddr;
        this.lHWID = lHWID;
        this.bLoaded = LoadFromDB();
        this.lLogAccount = GetLoadedList();
        this.sAccountName = GetAccountName();
    }

    public LogAccount(int dwAccountID, String sAddr, String sHWID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.pKeyValue = this.dwAccountID;
        this.sAddr = sAddr;
        this.sHWID = sHWID;
        this.lAddr.add(sAddr);
        this.lHWID.add(sHWID);
        this.bLoaded = LoadFromDB();
        this.lLogAccount = GetLoadedList();
        this.sAccountName = GetAccountName();
    }

    public LogAccount(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad, true);
        this.dwAccountID = (Integer) dwAccountID;
        this.lLogAccount = GetLoadedList();
        this.sAccountName = GetAccountName();
    }

    public String GetAccountName() {
        return User.GetAccountNameFromAccountID(dwAccountID);
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
                "sAddr",
                "sHWID"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Map.Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("IP Address", "sAddr"),
                new SimpleEntry<>("HardwareID", "sHWID")
        };
    }

    public DefaultTableModel GetFullTableData() {
        return ToDefaultTableModel(false);
    }

    public DefaultTableModel GetFlattenedTableData() {
        return ToDefaultTableModel(true);
    }
}
