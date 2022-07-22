package root.database.game.user.inventory.locker;

import database.Config;
import database.snapshot.DBSnapshotList;

import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import static database.Config.*;

public class LockerEquip extends DBSnapshotList<LockerEquip> {

    public long liSN, liCashItemSN, ftEquipped, ftDateExpire, nExGradeOption;
    public int dwAccountID, nItemID, nPosition, nRUC, nCUC, niSTR, nfiSTR, niDEX, nfiDEX, niINT, nfiINT,
            niLUK, nfiLUK, niMaxHP, nfiMaxHP, niMaxMP, nfiMaxMP, niPAD, nfiPAD, niMAD, nfiMAD, niPDD,
            nfiPDD, niMDD, niACC, niEVA, niCraft, niSpeed, nfiSpeed, niJump, nfiJump, nPrevBonusExpRate,
            nLevelUpType, nLevel, nEXP, nDurability, nIUC, nDurabilityMax, niReduceReq, nfiReduceReq,
            niIncReq, nGrowthEnchant, nPSEnchant, nBDR, nfiBDR, nIMDR, nDamR, nfiDamR, nStatR, nfiStatR,
            nCuttable, nAttribute, niPVPDamage, nSpecialAttribute, nItemState, nGrade, nCHUC, nSocketGrade,
            nOption1, nOption2, nOption3, nOption4, nOption5, nOption6, nOption7, nSocket1, nSocket2,
            nSocket3, nSoulOptionID, nSoulSocketID, nSoulOption, nArcaneForce, nArcaneExp, nArcaneLevel;
    public String sTitle;
    public LinkedList<LockerEquip> lLockerEquip;

    public LockerEquip(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        this.lLockerEquip = GetLoadedList();
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
        return new Entry[]{
                new SimpleEntry<>("Account ID", "dwAccountID"),
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
