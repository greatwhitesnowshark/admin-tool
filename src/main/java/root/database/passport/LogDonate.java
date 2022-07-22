package root.database.passport;

import database.snapshot.DBSnapshotList;
import root.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class LogDonate extends DBSnapshotList<LogDonate> {

    public int dwAccountID, nStatus, nQuantity, nRisk, nCouponID, nWebhookType;
    public String sID, sAccountClubID, sAccountEmail, sPaymentID, sProductID, sEmail, sAddr, sCountry, sProduct, sValue, sUSDValue,
            sCurrency, sGateway, sCreateDate, sUpdateDate, sAccountName;
    public byte bInfoMatch, bCredited;
    public List<String> lAddr = new LinkedList<>();
    public List<LogDonate> lLogDonate;

    public LogDonate(int dwAccountID) {
        super(dwAccountID, true, true);
        this.dwAccountID = dwAccountID;
        this.lLogDonate = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public LogDonate(int dwAccountID, String sAddr) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.sAddr = sAddr;
        this.lAddr.add(sAddr);
        this.bLoaded = LoadFromDB();
        this.lLogDonate = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public LogDonate(int dwAccountID, String sAddr, String sID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.sAddr = sAddr;
        this.sID = sID;
        this.bLoaded = LoadFromDB();
        this.lLogDonate = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public LogDonate(int dwAccountID, List<String> lAddr, String sID) {
        super(dwAccountID, false, true);
        this.dwAccountID = dwAccountID;
        this.lAddr = lAddr;
        this.sID = sID;
        this.bLoaded = LoadFromDB();
        this.lLogDonate = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public LogDonate(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad, true);
        this.dwAccountID = (Integer) dwAccountID;
        this.lLogDonate = GetLoadedList();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    @Override
    public String GetAdditionalArguments() {
        StringBuilder sArgs = new StringBuilder("");
        if (!lAddr.isEmpty()) {
            for (int i = 0; i < lAddr.size(); i++) {
                String sAddr = lAddr.get(i);
                if (sAddr != null && !sAddr.isBlank()) {
                    sArgs.append(" OR `sAddr` = \"").append(sAddr).append("\"");
                }
            }
        }
        if (sID != null && !sID.isBlank()) {
            sArgs.append(" OR `sEmail` = \"").append(sID).append("\"");
            sArgs.append(" OR `sAccountEmail` = \"").append(sID).append("\"");
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
                "sAccountClubID",
                "sAccountEmail",
                "bInfoMatch",
                "bCredited",
                "nStatus",
                "sPaymentID",
                "sProductID",
                "sEmail",
                "sAddr",
                "sCountry",
                "sProduct",
                "sValue",
                "sUSDValue",
                "nQuantity",
                "sCurrency",
                "sGateway",
                "nRisk",
                "nCouponID",
                "sCreateDate",
                "sUpdateDate",
                "nWebhookType"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Account Name", "sAccountClubID"),
                new SimpleEntry<>("Account Email", "sAccountEmail"),
                new SimpleEntry<>("Information Matched", "bInfoMatch"),
                new SimpleEntry<>("Credited", "bCredited"),
                new SimpleEntry<>("Status", "nStatus"),
                new SimpleEntry<>("Payment ID", "sPaymentID"),
                new SimpleEntry<>("USD Value", "sUSDValue"),
                new SimpleEntry<>("Quantity", "nQuantity"),
                new SimpleEntry<>("IP Address", "sAddr"),
                new SimpleEntry<>("Gateway", "sGateway"),
                new SimpleEntry<>("Risk Level", "nRisk"),
                new SimpleEntry<>("Purchase Date", "sCreateDate"),
                //new SimpleEntry<>("Last Updated Date", "sUpdateDate"),
        };
    }

    public DefaultTableModel GetHistoryTableData() {
        return ToDefaultTableModel(false);
    }
}
