package root.database.game.auction;

import root.User;
import root.database.game.auction.item.AuctionItem;
import root.database.game.auction.item.AuctionItemSlotBundle;
import root.database.game.auction.item.AuctionItemSlotEquip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Auction {

    public User pUser;
    public AuctionHistory pAuctionHistory;
    public AuctionItem pAuctionItem;
    public AuctionItemSlotBundle pAuctionItemSlotBundle;
    public AuctionItemSlotEquip pAuctionItemSlotEquip;

    public Auction(User pUser) {
        this.pUser = pUser;
        this.pAuctionHistory = new AuctionHistory(pUser.GetAccount().dwAccountID, true);
        this.pAuctionItem = new AuctionItem(pUser.GetAccount().dwAccountID, true);
        this.pAuctionItemSlotBundle = new AuctionItemSlotBundle(pUser.GetAccount().dwAccountID, pUser.pCharacter.GetAccountCharacterIDs());
        this.pAuctionItemSlotEquip = new AuctionItemSlotEquip(pUser.GetAccount().dwAccountID, pUser.pCharacter.GetAccountCharacterIDs());
    }

    public String ToString() {
        String sDump = "";

        sDump += pAuctionHistory.ToFlattenedString();
        sDump += pAuctionItem.ToFlattenedString();
        sDump += pAuctionItemSlotBundle.ToFlattenedString();
        sDump += pAuctionItemSlotEquip.ToFlattenedString();

        return sDump;
    }

    public DefaultTableModel GetItemTableData() {
        return pAuctionItemSlotBundle.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetHistoryTableData() {
        return pAuctionHistory.ToDefaultTableModel(false);
    }
}
