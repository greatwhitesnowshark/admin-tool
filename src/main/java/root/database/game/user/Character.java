package root.database.game.user;

import database.Config;
import database.snapshot.DBSnapshotList;
import root.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

public class Character extends DBSnapshotList<Character> {

    public byte bBurning, nEarType, bHideTail;
    public long nEXP, nMoney, ftLastUpdateCharmByCashPR, ftLastLogout;
    public int dwCharacterID, dwAccountID, dwWorldID, nGender, nSkin, nFace, nHair, nMixBaseHairColor, nMixAddHairColor, nMixHairBaseProb, nLevel,
            nJob, nSubJob, nSTR, nDEX, nINT, nLUK, nHP, nMP, nMHP, nMMP, nAP, nPOP, nWP, dwPosMap, nPortal, nDefenseFaceAcc, nFatigue, nLastFatigueUpdateTime,
            nCharisma, nCharismaEXP, nInsight, nInsightEXP, nWill, nWillEXP, nCraft, nCraftEXP, nSense, nSenseEXP, nCharm, nCharmEXP, nCharmByCashPR,
            nPvPExp, nPvPGrade, nPvPPoint, nPvPModeLevel, nPvPModeType, nEventPoint, nLocation;
    public String sAccountName, sCharacterName, nSP, nUnionGridX, nUnionGridY;
    public LinkedList<Character> lUser;
    public LinkedList<Integer> lCharacterIDs;

    public Character(int dwCharacterID) {
        super(0, false);
        this.dwAccountID = User.GetAccountIDFromCharID(dwCharacterID);
        if (dwAccountID > 0) {
            this.pKeyValue = dwAccountID;
            this.bLoaded = LoadFromDB();
            this.lUser = GetLoadedList();
            this.lCharacterIDs = new LinkedList<>();
            this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
        }
    }

    public Character(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
        if (bAutoLoad) {
            this.lUser = GetLoadedList();
        } else {
            this.lUser = new LinkedList<>();
        }
        this.lCharacterIDs = new LinkedList<>();
        this.sAccountName = User.GetAccountNameFromAccountID(this.dwAccountID);
    }

    public LinkedList<Integer> GetAccountCharacterIDs() {
        lCharacterIDs = new LinkedList<>();
        for (Character pUser : lUser) {
            lCharacterIDs.add(pUser.dwCharacterID);
        }
        return lCharacterIDs;
    }

    @Override
    public boolean DB_IncrementKey() {
        return false;
    }

    @Override
    public String DB_GetSchema() {
        return Config.DB_GAME_SCHEMA;
    }

    @Override
    public String GetAdditionalArguments() {
        return "";
    }

    @Override
    public String DB_GetKey() {
        return "dwAccountID";
    }

    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwCharacterID",
                "dwAccountID",
                "dwWorldID",
                "sCharacterName",
                "nGender",
                "nSkin",
                "nFace",
                "nHair",
                "nMixBaseHairColor",
                "nMixAddHairColor",
                "nMixHairBaseProb",
                "nLevel",
                "nJob",
                "nSubJob",
                "nEXP",
                "nSTR",
                "nDEX",
                "nINT",
                "nLUK",
                "nHP",
                "nMP",
                "nMHP",
                "nMMP",
                "nAP",
                "nSP",
                "nMoney",
                "nPOP",
                "nWP",
                "dwPosMap",
                "nPortal",
                "nDefenseFaceAcc",
                "nFatigue",
                "nLastFatigueUpdateTime",
                "nCharisma",
                "nCharismaEXP",
                "nInsight",
                "nInsightEXP",
                "nWill",
                "nWillEXP",
                "nCraft",
                "nCraftEXP",
                "nSense",
                "nSenseEXP",
                "nCharm",
                "nCharmEXP",
                "nCharmByCashPR",
                "ftLastUpdateCharmByCashPR",
                "nPvPExp",
                "nPvPGrade",
                "nPvPPoint",
                "nPvPModeLevel",
                "nPvPModeType",
                "nEventPoint",
                "ftLastLogout",
                "bBurning",
                "nLocation",
                "nUnionGridX",
                "nUnionGridY",
                "nEarType",
                "bHideTail"
        };
    }

    @Override
    public Map.Entry<String, String>[] GetLoggedColumnNames() {
        return new Map.Entry[]{
                new AbstractMap.SimpleEntry<>("Account ID", "dwAccountID"),
                new AbstractMap.SimpleEntry<>("Character ID", "dwCharacterID"),
                new AbstractMap.SimpleEntry<>("Name", "sCharacterName"),
                new AbstractMap.SimpleEntry<>("Gender", "nGender"),
                new AbstractMap.SimpleEntry<>("Level", "nLevel"),
                new AbstractMap.SimpleEntry<>("Job", "nJob"),
                new AbstractMap.SimpleEntry<>("SubJob", "nSubJob")
        };
    }

    public DefaultTableModel GetCharacterTableData() {
        return ToDefaultTableModel(false);
    }
}