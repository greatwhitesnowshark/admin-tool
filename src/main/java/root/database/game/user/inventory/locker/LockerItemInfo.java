package root.database.game.user.inventory.locker;

import database.Config;
import database.snapshot.DBSnapshotList;

import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import static database.Config.*;

public class LockerItemInfo extends DBSnapshotList<LockerItemInfo> {

    public long liSN, ftDateExpire;
    public double dDiscountRate;
    public byte bRefundable, nSourceFlag, nStoreBank;
    public int dwAccountID, dwCharacterID, nItemID, nCommodityID, nNumber, nPaybackRate, dwOrderNo, dwProductNo;
    public String sBuyCharacterID;
    public LinkedList<LockerItemInfo> lLockerItemInfo;

    public LockerItemInfo(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        this.lLockerItemInfo = GetLoadedList();
    }

    @Override
    public String DB_GetSchema() {
        return Config.DB_GAME_SCHEMA;
    }

    @Override
    public String GetAdditionalArguments() {
        StringBuilder s = new StringBuilder(" AND (");
        for (int i = 0; i < aFlaggedBundleItems.length; i++) {
            int nItemID = aFlaggedBundleItems[i];
            if (i > 0) {
                s.append(" OR ");
            }
            s.append("`nItemID` = " + nItemID);
        }
        if (LOG_EQUIPS) {
            for (int i = 0; i < aFlaggedEquipItems.length; i++) {
                int nItemID = aFlaggedEquipItems[i];
                s.append(" OR `nItemID` = " + nItemID);
            }
        }
        s.append(")");
        return s.toString();
    }

    @Override
    public String DB_GetKey() {
        return "dwAccountID";
    }
    
    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwAccountID",
                "dwCharacterID",
                "nItemID",
                "liSN",
                "nCommodityID",
                "nNumber",
                "sBuyCharacterID",
                "ftDateExpire",
                "nPaybackRate",
                "dDiscountRate",
                "dwOrderNo",
                "dwProductNo",
                "bRefundable",
                "nSourceFlag",
                "nStoreBank"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Character ID", "dwCharacterID"),
                new SimpleEntry<>("Item ID", "nItemID"),
                new SimpleEntry<>("Item Unique ID", "liSN"),
                new SimpleEntry<>("Item Commodity ID", "nCommodityID"),
                new SimpleEntry<>("Quantity", "nNumber"),
                new SimpleEntry<>("Buy-Character ID", "sBuyCharacterID"),
                new SimpleEntry<>("Payback Rate", "nPaybackRate"),
                new SimpleEntry<>("Discount Rate", "dDiscountRate"),
                new SimpleEntry<>("Order Number", "dwOrderNo"),
                new SimpleEntry<>("Product Number", "dwProductNo"),
                new SimpleEntry<>("Refundable", "bRefundable"),
                new SimpleEntry<>("Temporary Expiration Date", "ftDateExpire"),
                new SimpleEntry<>("Source Flag", "nSourceFlag"),
                new SimpleEntry<>("Store Bank", "nStoreBank")
        };
    }
}
