package root.database.game.user.inventory;

import database.Config;
import root.User;
import root.database.passport.Account;
import root.database.game.user.inventory.item.ItemBagData;
import root.database.game.user.inventory.item.ItemSlotBundle;
import root.database.game.user.inventory.item.ItemSlotEquip;
import root.database.game.user.inventory.locker.LockerBundle;
import root.database.game.user.inventory.locker.LockerEquip;
import root.database.game.user.inventory.locker.LockerItemInfo;
import root.database.game.user.inventory.trunk.Trunk;
import root.database.game.user.inventory.trunk.TrunkBundle;
import root.database.game.user.inventory.trunk.TrunkEquip;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;

public class Inventory {

    public boolean bNoEquips;
    public User pUser;
    public ItemBagData pItemBagData;
    public ItemSlotBundle pItemSlotBundle;
    public ItemSlotEquip pItemSlotEquip;
    public LockerItemInfo pLockerItemInfo;
    public LockerBundle pLockerBundle;
    public LockerEquip pLockerEquip;
    public TrunkBundle pTrunkBundle;
    public TrunkEquip pTrunkEquip;
    public Trunk pTrunk;

    public Inventory(User pUser) {
        this.pUser = pUser;
        this.bNoEquips = Config.LOG_EQUIPS;

        Account pAccount = pUser.GetAccount();
        LinkedList<Integer> lCharacterID = pUser.pCharacter.GetAccountCharacterIDs();

        this.pItemBagData = new ItemBagData(pAccount.dwAccountID, lCharacterID);
        this.pItemSlotBundle = new ItemSlotBundle(pAccount.dwAccountID, lCharacterID);
        this.pItemSlotEquip = new ItemSlotEquip(pAccount.dwAccountID, lCharacterID);

        this.pLockerItemInfo = new LockerItemInfo(pAccount.dwAccountID, true);
        this.pLockerBundle = new LockerBundle(pAccount.dwAccountID, true);
        this.pLockerEquip = new LockerEquip(pAccount.dwAccountID, true);

        this.pTrunkBundle = new TrunkBundle(pAccount.dwAccountID, true);
        this.pTrunkEquip = new TrunkEquip(pAccount.dwAccountID, true);
        this.pTrunk = new Trunk(pAccount.dwAccountID, true);

    }

    public String ToString() {
        StringBuilder sb = new StringBuilder();

        sb.append(pItemBagData.ToFlattenedString());
        sb.append(pItemSlotBundle.ToFlattenedString());
        sb.append(pItemSlotEquip.ToFlattenedString());

        sb.append(pLockerItemInfo.ToFlattenedString());
        sb.append(pLockerBundle.ToFlattenedString());
        sb.append(pLockerEquip.ToFlattenedString());

        sb.append(pTrunkBundle.ToFlattenedString());
        sb.append(pTrunkEquip.ToFlattenedString());
        sb.append(pTrunk.ToFlattenedString());

        return sb.toString();
    }

    public DefaultTableModel GetItemTableData() {
        return pItemSlotBundle.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetItemBagTableData() {
        return pItemBagData.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetItemEquipTableData() {
        return pItemSlotEquip.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetLockerItemTableData() {
        return pLockerBundle.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetLockerItemInfoTableData() {
        return pLockerItemInfo.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetLockerEquipTableData() {
        return pLockerEquip.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetTrunkTableData() {
        return pTrunk.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetTrunkItemData() {
        return pTrunkBundle.ToDefaultTableModel(false);
    }

    public DefaultTableModel GetTrunkEquipData() {
        return pTrunkEquip.ToDefaultTableModel(false);
    }
}
