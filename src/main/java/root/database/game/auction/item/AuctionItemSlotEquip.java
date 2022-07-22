package root.database.game.auction.item;

import database.Config;
import database.snapshot.DBSnapshotList;
import root.database.game.user.Character;

import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import static database.Config.*;
import static database.Config.aFlaggedEquipItems;

public class AuctionItemSlotEquip extends DBSnapshotList<AuctionItemSlotEquip> {

    public long liSN, liCashItemSN, ftEquipped, ftDateExpire, nExGradeOption;
    public int dwAuctionID, dwAccountID, dwCharacterID, nItemID, nRUC, nCUC, niSTR, nfiSTR, niDEX, nfiDEX, niINT,
            nfiINT, niLUK, nfiLUK, niMaxHP, nfiMaxHP, niMaxMP, nfiMaxMP, niPAD, nfiPAD, niMAD, nfiMAD, niPDD,
            nfiPDD, niMDD, niACC, niEVA, niCraft, niSpeed, nfiSpeed, niJump, nfiJump, nPrevBonusExpRate,
            nLevelUpType, nLevel, nEXP, nDurability, nIUC, nDurabilityMax, niReduceReq, nfiReduceReq,
            niIncReq, nGrowthEnchant, nPSEnchant, nBDR, nfiBDR, nIMDR, nDamR, nfiDamR, nStatR, nfiStatR,
            nCuttable, nAttribute, niPVPDamage, nSpecialAttribute, nItemState, nGrade, nCHUC, nSocketGrade,
            nOption1, nOption2, nOption3, nOption4, nOption5, nOption6, nOption7, nSocket1, nSocket2,
            nSocket3, nSoulOptionID, nSoulSocketID, nSoulOption, nArcaneForce, nArcaneExp, nArcaneLevel;
    public String sTitle;
    public List<Integer> lCharacterID;
    public LinkedList<AuctionItemSlotEquip> lAuctionItemSlotEquip;

    public AuctionItemSlotEquip(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        if (bAutoLoad) {
            this.lAuctionItemSlotEquip = GetLoadedList();
        }
    }

    public AuctionItemSlotEquip(Object dwAccountID, LinkedList<Integer> lCharacterID) {
        super(dwAccountID, false);
        this.lCharacterID = lCharacterID;
        this.bLoaded = LoadFromDB();
        this.lAuctionItemSlotEquip = GetLoadedList();
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
            if (Config.LOG_EQUIPS) {
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
                "dwAuctionID",
                "dwCharacterID",
                "liSN",
                "liCashItemSN",
                "nItemID",
                "nRUC",
                "nCUC",
                "niSTR",
                "nfiSTR",
                "niDEX",
                "nfiDEX",
                "niINT",
                "nfiINT",
                "niLUK",
                "nfiLUK",
                "niMaxHP",
                "nfiMaxHP",
                "niMaxMP",
                "nfiMaxMP",
                "niPAD",
                "nfiPAD",
                "niMAD",
                "nfiMAD",
                "niPDD",
                "nfiPDD",
                "niMDD",
                "niACC",
                "niEVA",
                "niCraft",
                "niSpeed",
                "nfiSpeed",
                "niJump",
                "nfiJump",
                "sTitle",
                "ftEquipped",
                "ftDateExpire",
                "nPrevBonusExpRate",
                "nLevelUpType",
                "nLevel",
                "nEXP",
                "nDurability",
                "nIUC",
                "nDurabilityMax",
                "niReduceReq",
                "nfiReduceReq",
                "niIncReq",
                "nGrowthEnchant",
                "nPSEnchant",
                "nBDR",
                "nfiBDR",
                "nIMDR",
                "nDamR",
                "nfiDamR",
                "nStatR",
                "nfiStatR",
                "nCuttable",
                "nAttribute",
                "niPVPDamage",
                "nSpecialAttribute",
                "nItemState",
                "nExGradeOption",
                "nGrade",
                "nCHUC",
                "nSocketGrade",
                "nOption1",
                "nOption2",
                "nOption3",
                "nOption4",
                "nOption5",
                "nOption6",
                "nOption7",
                "nSocket1",
                "nSocket2",
                "nSocket3",
                "nSoulOptionID",
                "nSoulSocketID",
                "nSoulOption",
                "nArcaneForce",
                "nArcaneExp",
                "nArcaneLevel"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Auction ID", "dwAuctionID"),
                new SimpleEntry<>("Character ID", "dwCharacterID"),
                new SimpleEntry<>("Item ID", "nItemID"),
                new SimpleEntry<>("STR (sc)", "niSTR"),
                new SimpleEntry<>("STR (fl)", "nfiSTR"),
                new SimpleEntry<>("DEX (sc)", "niDEX"),
                new SimpleEntry<>("DEX (fl)", "nfiDEX"),
                new SimpleEntry<>("INT (sc)", "niINT"),
                new SimpleEntry<>("INT (fl)", "nfiINT"),
                new SimpleEntry<>("LUK (sc)", "niLUK"),
                new SimpleEntry<>("LUK (fl)", "nfiLUK"),
                new SimpleEntry<>("Max HP (sc)", "niMaxHP"),
                new SimpleEntry<>("Max HP (fl)", "nfiMaxHP"),
                new SimpleEntry<>("Max HP (sc)", "niMaxMP"),
                new SimpleEntry<>("Max MP (fl)", "nfiMaxMP"),
                new SimpleEntry<>("Title", "sTitle"),
        };
    }
}
