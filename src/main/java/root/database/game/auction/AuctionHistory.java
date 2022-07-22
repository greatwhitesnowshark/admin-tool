package root.database.game.auction;

import database.Config;
import database.snapshot.DBSnapshotList;
import root.User;

import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import static database.Config.aFlaggedBundleItems;
import static database.Config.aFlaggedEquipItems;

public class AuctionHistory extends DBSnapshotList<AuctionHistory> {

    public long liSN, nPrice, ftDate, nDeposit;
    public int dwAccountID, dwAuctionID, dwCharacterID, nItemID, nState, nCount, nWorldID;
    public String sAccountName;
    public LinkedList<AuctionHistory> lAuctionHistory;

    public AuctionHistory(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, false, true);
        this.dwAccountID = (Integer) dwAccountID;
        if (bAutoLoad) {
            this.bLoaded = LoadFromDB();
            this.lAuctionHistory = GetLoadedList();
        } else {
            this.lAuctionHistory = new LinkedList<>();
        }
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
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
        if (Config.LOG_EQUIPS) {
            for (int i = 0; i < aFlaggedEquipItems.length; i++) {
                int nItemID = aFlaggedEquipItems[i];
                s.append(" OR `nItemID` = " + nItemID);
            }
        }
        s.append(")");
        return s.toString();
    }

    @Override
    public String DB_GetSchema() {
        return Config.DB_GAME_SCHEMA;
    }

    @Override
    public String DB_GetKey() {
        return "dwAccountID";
    }

    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "liSN",
                "dwAuctionID",
                "dwAccountID",
                "dwCharacterID",
                "nItemID",
                "nState",
                "nPrice",
                "ftDate",
                "nDeposit",
                "nCount",
                "nWorldID"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Character ID", "dwCharacterID"),
                new SimpleEntry<>("Auction ID", "dwAuctionID"),
                new SimpleEntry<>("Item Unique ID", "liSN"),
                new SimpleEntry<>("Item ID", "nItemID"),
                new SimpleEntry<>("State", "nState"),
                new SimpleEntry<>("Price", "nPrice"),
                new SimpleEntry<>("Time/Date", "ftDate"),
                new SimpleEntry<>("Deposit", "nDeposit"),
                new SimpleEntry<>("Quantity", "nCount")
        };
    }
}
