package root.database.game.auction.item;

import database.Config;
import database.snapshot.DBSnapshotList;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

public class AuctionItem extends DBSnapshotList<AuctionItem> {

    public int dwAuctionID, nAuctionType, dwAccountID, dwCharacterID, dwBidUserID, nBidWorld, nAccountOID, nSSType;
    public byte nState, nWorldID;
    public long nPrice, nSecondPrice, nDirectPrice, nUnitPrice, ftEndDate, ftRegDate, nDeposit;
    public String sCharacterName, sBidUserName;
    public LinkedList<AuctionItem> lAuctionItem;

    public AuctionItem(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        this.lAuctionItem = GetLoadedList();
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
    public String GetAdditionalArguments() {
        return "";
    }
    
    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwAuctionID",
                "nAuctionType",
                "dwAccountID",
                "dwCharacterID",
                "nState",
                "nWorldID",
                "sCharacterName",
                "nPrice",
                "nSecondPrice",
                "nDirectPrice",
                "nUnitPrice",
                "ftEndDate",
                "dwBidUserID",
                "sBidUserName",
                "nBidWorld",
                "nAccountOID",
                "ftRegDate",
                "nDeposit",
                "nSSType"
        };
    }

    @Override
    public Map.Entry<String, String>[] GetLoggedColumnNames() {
        return new Map.Entry[] {
                new AbstractMap.SimpleEntry<>("Account ID", "dwAccountID"),
                new AbstractMap.SimpleEntry<>("Auction ID", "dwAuctionID"),
                new AbstractMap.SimpleEntry<>("Auction Type", "nAuctionType"),
                new AbstractMap.SimpleEntry<>("Character ID", "dwCharacterID"),
                new AbstractMap.SimpleEntry<>("Character Name", "sCharacterName"),
                new AbstractMap.SimpleEntry<>("Price", "nPrice"),
                new AbstractMap.SimpleEntry<>("Second Price", "nSecondPrice"),
                new AbstractMap.SimpleEntry<>("Direct Price", "nDirectPrice"),
                new AbstractMap.SimpleEntry<>("Unit Price", "nUnitPrice"),
                new AbstractMap.SimpleEntry<>("End Date", "ftEndDate"),
                new AbstractMap.SimpleEntry<>("Bidder ID", "dwBidUserID"),
                new AbstractMap.SimpleEntry<>("Bidder Name", "sBidUserName"),
                new AbstractMap.SimpleEntry<>("Registered Date", "ftRegDate"),
                new AbstractMap.SimpleEntry<>("Deposit", "nDeposit"),
        };
    }
}
