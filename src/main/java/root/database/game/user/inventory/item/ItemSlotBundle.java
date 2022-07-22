package root.database.game.user.inventory.item;

import database.Config;
import database.snapshot.DBSnapshotList;
import root.database.game.user.Character;

import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import static database.Config.*;
import static database.Config.aFlaggedEquipItems;

public class ItemSlotBundle extends DBSnapshotList<ItemSlotBundle> {

    public long liSN, liCashItemSN, ftDateExpire;
    public short nPosition, nNumber, nAttribute;
    public byte nBagIndex, nTI;
    public int dwAccountID, dwCharacterID, nItemID;
    public String sTitle;
    public List<Integer> lCharacterID;
    public LinkedList<ItemSlotBundle> lItemSlotBundle;

    public ItemSlotBundle(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        if (bAutoLoad) {
            this.lItemSlotBundle = GetLoadedList();
        }
    }

    public ItemSlotBundle(Object dwAccountID, LinkedList<Integer> lCharacterID) {
        super(dwAccountID, false);
        this.lCharacterID = lCharacterID;
        this.bLoaded = LoadFromDB();
        this.lItemSlotBundle = GetLoadedList();
    }

    @Override
    public String DB_GetSelectQuery() {
        return String.format("SELECT %s FROM `%s`.`%s` WHERE %s", GetColumnNames(), DB_GetSchema(), DB_GetTable(), GetAdditionalArguments());
    }

    @Override
    public String DB_GetSchema() {
        return Config.DB_GAME_SCHEMA;
    }

    @Override
    public String GetAdditionalArguments() {
        int nIndex = 0;
        StringBuilder s = new StringBuilder("");
        if (lCharacterID == null || lCharacterID.isEmpty()) {
            Character pUser = new Character(dwAccountID, true); //todo:: store this guy
            lCharacterID = pUser.GetAccountCharacterIDs();
        }
        if (!lCharacterID.isEmpty()) {
            for (int nCharacterID : lCharacterID) {
                if (nIndex++ == 0) {
                    s.append("(");
                } else {
                    s.append(" OR ");
                }
                s.append("`dwCharacterID` = " + nCharacterID);
            }
            s.append(")");
            s.append(" AND (");
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
        }
        return s.toString();
    }

    @Override
    public String DB_GetKey() {
        return "dwCharacterID";
    }

    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwCharacterID",
                "liSN",
                "liCashItemSN",
                "nItemID",
                "nPosition",
                "nNumber",
                "nBagIndex",
                "nAttribute",
                "sTitle",
                "ftDateExpire",
                "nTI"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Character ID", "dwCharacterID"),
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
