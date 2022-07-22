package root.database.game.user.inventory.locker;

import database.Config;
import database.snapshot.DBSnapshotList;

import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import static database.Config.*;
import static database.Config.aFlaggedEquipItems;

public class LockerBundle extends DBSnapshotList<LockerBundle> {

    public long liSN, liCashItemSN, ftDateExpire;
    public short nPosition, nNumber, nAttribute;
    public byte nBagIndex;
    public int dwAccountID, nItemID;
    public String sTitle;
    public LinkedList<LockerBundle> lLockerBundle;

    public LockerBundle(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        this.lLockerBundle = GetLoadedList();
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
                "liSN",
                "liCashItemSN",
                "nItemID",
                "nPosition",
                "nNumber",
                "nBagIndex",
                "nAttribute",
                "sTitle",
                "ftDateExpire"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Item Unique ID", "liSN"),
                new SimpleEntry<>("Cash Item Unqiue ID", "liCashItemSN"),
                new SimpleEntry<>("Item ID", "nItemID"),
                new SimpleEntry<>("Position", "nPosition"),
                new SimpleEntry<>("Quantity", "nNumber"),
                new SimpleEntry<>("Title", "sTitle"),
                new SimpleEntry<>("Temporary Expiration Date", "ftDateExpire")
        };
    }
}
