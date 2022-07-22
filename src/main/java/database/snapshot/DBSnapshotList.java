package database.snapshot;

import database.Config;
import database.Database;
import root.BanReason;
import root.User;
import root.database.game.user.Character;
import util.Blowfish;
import util.FileTime;
import util.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import static util.Utilities.AddStringPadding;

import java.awt.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.List;

public abstract class DBSnapshotList<T extends DBSnapshotList> implements DBObject {

    public boolean bLoaded; //todo:: protected
    protected String sSalt = Blowfish.CreateSalt();
    protected boolean bNotSaved;
    protected Object pKeyValue;
    protected List<Map<String, Object>> lSnapshot; //stores all of the object's fields with field-name as key for a "snapshot" history reference
    protected LinkedList<T> lLoadedList;

    public DBSnapshotList(Object pKeyValue) {
        this.lLoadedList = new LinkedList<>();
        this.lSnapshot = new LinkedList<>();
        this.pKeyValue = pKeyValue;
        this.bNotSaved = false;
        this.bLoaded = false;
    }

    public DBSnapshotList(Object pKeyValue, boolean bAutoLoad) {
        this.lLoadedList = new LinkedList<>();
        this.lSnapshot = new LinkedList<>();
        this.pKeyValue = pKeyValue;
        this.bNotSaved = false;
        if (bAutoLoad) {
            this.bLoaded = LoadFromDB();
        }
    }

    public DBSnapshotList(Object pKeyValue, boolean bAutoLoad, boolean bNotSaved) {
        this.lLoadedList = new LinkedList<>();
        this.lSnapshot = new LinkedList<>();
        this.pKeyValue = pKeyValue;
        this.bNotSaved = bNotSaved;
        if (bAutoLoad) {
            this.bLoaded = LoadFromDB();
        }
    }

    public LinkedList<T> GetLoadedList() {
        return this.lLoadedList;
    }

    protected void DeleteFromDB() {
        if (!bNotSaved) {
            try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(DB_GetDeleteQuery())) {
                Database.execute(con, ps);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //todo:: consider re-doing this haxfix method that deletes and re-inserts fresh data instead of iterating the list and caching updates per item
    public void Update(LinkedList<T> lUpdate) {
        /*lSnapshot.clear();
        for (int i = 0; i < lUpdate.size(); i++) {
            Object pUpdate = lUpdate.get(i);
            Map<String, Object> mSnapshot = new LinkedHashMap<>();
            for (String sFieldName : DB_GetColumnNames()) try {
                Object pValue = getClass().getDeclaredField(sFieldName).get(pUpdate);
                mSnapshot.put(sFieldName, pValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            lSnapshot.add(mSnapshot);
        }*/
    }

    public synchronized void Flush(LinkedList<T> lUpdate) {
        //Update(lUpdate); //failsafe
        //SaveToDB(lUpdate);
    }

    protected boolean SaveToDB(LinkedList<T> lUpdate) {
        /*if (!bNotSaved) {
            DeleteFromDB();
            try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(DB_GetUpdateQuery(), Statement.RETURN_GENERATED_KEYS)) {
                for (Map<String, Object> mUpdate : lSnapshot) {
                    int i = 1;
                    for (Entry<String, Object> pUpdate : mUpdate.entrySet()) {
                        ps.setObject(i++, pUpdate.getValue());
                    }
                    Database.execute(con, ps, mUpdate.values());
                    //ps.clearParameters();
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;*/
        return true;
    }

    protected boolean LoadFromDB() {
        //if (!bLoaded) {
            String[] aColumnNames = DB_GetColumnNames();
            Class<?>[] aColumnTypes = DB_GetColumnTypes();
            String sSelectQuery = DB_GetSelectQuery();
            try (Connection con = Database.connection()) {
                assert con != null;
                try (PreparedStatement ps = con.prepareStatement(sSelectQuery, Statement.RETURN_GENERATED_KEYS)) {
                    ResultSet rs = ps.executeQuery();
                    try {
                        int a = 0;
                        while (rs.next()) try {
                            T pLoadedObject = (T) getClass().getConstructor(Object.class, boolean.class).newInstance(pKeyValue, false);
                            Map<String, Object> mSnapshot = new LinkedHashMap<>();
                            for (int i = 0; i < aColumnNames.length; i++) try {
                                int nIndex = /*DB_IncrementKey() ? i + 2 : */i + 1;
                                Object pValue = rs.getObject(nIndex, aColumnTypes[i]);
                                pLoadedObject.getClass().getDeclaredField(aColumnNames[i]).set(pLoadedObject, pValue);
                                mSnapshot.put(aColumnNames[i], pValue);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            lSnapshot.add(mSnapshot);
                            lLoadedList.add(pLoadedObject);
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        rs.close();
                        ps.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        //}
        return !lSnapshot.isEmpty();
    }

    protected String GetColumnNames() {
        return Utilities.JoinString(DB_GetColumnNames(), 0, ", ");
    }

    protected String GetValuesPart() {
        StringBuilder sValues = new StringBuilder();
        int nSize = DB_GetColumnNames().length;
        for (int i = 0; i < nSize; i++) {
            if (i != 0) {
                sValues.append(", ?");
            } else {
                sValues.append("?");
            }
        }
        return sValues.toString();
    }

    public String DB_GetDeleteQuery() {
        return "";//String.format("DELETE FROM `%s`.`%s` WHERE %s", DB_GetSchema(), DB_GetTable(), GetLocationPart());
    }

    @Override
    public boolean DB_IncrementKey() {
        return true;
    }

    @Override
    public String DB_GetSchema() {
        return Config.DB_SCHEMA;
    }

    @Override
    public String DB_GetTable() {
        return getClass().getSimpleName().toLowerCase();
    }

    @Override
    public String DB_GetInsertQuery() {
        return "";//String.format("INSERT INTO `%s`.`%s` (%s) VALUES (%s)", DB_GetSchema(), DB_GetTable(), GetColumnNames(), GetValuesPart());
    }

    @Override
    public String DB_GetUpdateQuery() {
        return DB_GetInsertQuery();
    }

    private String GetLocationPart() {
        return String.format("`%s` = %s%s", DB_GetKey(), pKeyValue, GetAdditionalArguments()).trim();
    }

    @Override
    public String DB_GetSelectQuery() {
        return String.format("SELECT %s FROM `%s`.`%s` WHERE %s", GetColumnNames(), DB_GetSchema(), DB_GetTable(), GetLocationPart()).trim();
    }

    @Override
    public Class<?>[] DB_GetColumnTypes() {
        String[] aColumnNames = DB_GetColumnNames();
        Class<?>[] aTypes = new Class<?>[aColumnNames.length];
        int i = 0;
        for (String sColumn : aColumnNames) try {
            Class<?> pColumnType;
            Class<?> pType = getClass().getDeclaredField(sColumn).getType();
            if (pType.isPrimitive()) switch (pType.getSimpleName().toLowerCase()) {
                case "int":
                    pColumnType = Integer.class;
                    break;
                case "long":
                    pColumnType = Long.class;
                    break;
                case "byte":
                    pColumnType = Byte.class;
                    break;
                case "short":
                    pColumnType = Short.class;
                    break;
                case "boolean":
                    pColumnType = Boolean.class;
                    break;
                case "char":
                    pColumnType = Character.class;
                    break;
                case "float":
                    pColumnType = Float.class;
                    break;
                case "double":
                    pColumnType = Double.class;
                    break;
                default:
                    pColumnType = pType;
                    break;
            } else {
                if (pType.isArray()) {
                    pColumnType = Array.class;
                } else {
                    pColumnType = pType;
                }
            }
            aTypes[i++] = pColumnType;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return aTypes;
    }

    public boolean ListOfContains(String s, String[] a) {
        for (String sa : a) {
            if (sa.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String ToString() {
        String sSimpleName = getClass().getSimpleName();
        String sHeader = "\n------------------------------------------------" +
                         "\n::  " + (sSimpleName + " History [" + lLoadedList.size() + "]") + " ::" +
                         "\n------------------------------------------------\n";
        StringBuilder s = new StringBuilder(sHeader);
        int i = 1;
        for (T pLoadedObject : lLoadedList) try {
            String sAccountName = "";
            if (ListOfContains("dwAccountID", DB_GetColumnNames())) {
                //if (List.of(DB_GetColumnNames()).contains("dwAccountID")) {
                int dwID = (Integer) getClass().getDeclaredField("dwAccountID").get(pLoadedObject);
                sAccountName = User.GetAccountNameFromAccountID(dwID);
            } else if (ListOfContains("dwCharacterID", DB_GetColumnNames())) {
            //} else if (List.of(DB_GetColumnNames()).contains("dwCharacterID")) {
                int dwID = (Integer) getClass().getDeclaredField("dwCharacterID").get(pLoadedObject);
                int dwAccountID = User.GetAccountIDFromCharID(dwID);
                sAccountName = AddStringPadding(User.GetAccountNameFromAccountID(dwAccountID), 14) + " (Username)";
                sAccountName += "\n  " + AddStringPadding(User.GetCharNameFromCharID(dwID), 14) + " (IGN)";
            }
            s.append(String.format("\n  #%d - \n  %s  [%s]", i++, sAccountName == null || sAccountName.isBlank() ? "" : sAccountName + "\n", pLoadedObject.getClass().getSimpleName()));
            int a = 0;
            for (String sColumn : DB_GetColumnNames()) {
                String sName = sColumn;
                Object pValue = getClass().getDeclaredField(sColumn).get(pLoadedObject);
                for (Map.Entry<String, String> pEntry : pLoadedObject.GetLoggedColumnNames()) {
                    if (pEntry.getValue().equalsIgnoreCase(sColumn)) {
                        sName = pEntry.getKey();
                        break;
                    }
                }
                s.append(String.format("\n\t\t%s\n\t\t\t\t\t%s\n\t\t---------------------------------------------------------------------------------------", sName, pValue.getClass() == String.class ? "\"" + pValue + "\"" : ""+pValue));
            }
            s.append("\n");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return s.toString();
    }

    public abstract String GetAdditionalArguments();

    public abstract Map.Entry<String, String>[] GetLoggedColumnNames();

    protected String[] GetDataTableColumnNames(boolean bFlattened) {
        int nLen = bFlattened ? 1 : 0;
        String[] aColumnNames = new String[GetLoggedColumnNames().length + nLen];
        for (int i = 0; i < GetLoggedColumnNames().length; i++) {
            Map.Entry<String, String> pEntry = GetLoggedColumnNames()[i];
            if (pEntry != null) {
                aColumnNames[i] = pEntry.getKey();
            }
        }
        if (bFlattened) aColumnNames[GetLoggedColumnNames().length] = "COUNT(S)";
        return aColumnNames;
    }

    protected String[][] GetDataTableValues(boolean bFlattened) {
        return bFlattened ? GetFlatTableData() : GetTableData();
    }

    protected String[][] GetTableData() {
        int nFirstIndex = 0, nSecondIndex = 0;
        String[][] aRowValues = new String[GetLoadedList().size()][GetLoggedColumnNames().length];
        for (T pLoadedObject : this.GetLoadedList()) {
            nSecondIndex = 0;
            for (Map.Entry<String, String> pEntry : GetLoggedColumnNames()) try {
                String sColumn = pEntry.getValue() == null ? "" : pEntry.getValue();
                Object pValue = pEntry.getValue() == null ? "" : getClass().getDeclaredField(sColumn).get(pLoadedObject);
                String sValue;
                if (pEntry.getKey().equals("Ban Reason")) {
                    BanReason pBanReason = BanReason.GetReason((Integer) pValue);
                    sValue = pBanReason.name();
                } else if (pEntry.getKey().equals("Registered Date")) {
                    FileTime ft = new FileTime((Long) pValue);
                    sValue = ft.toString();
                } else if (pEntry.getKey().equals("IP Address")) {
                    sValue = Blowfish.Encrypt(pEntry.getValue(), sSalt);
                } else {
                    //if (pValue.getClass() == String.class) {
                        //sValue = "\"" + pValue + "\"";
                    //} else {
                        sValue = "" + pValue;
                    //}
                }
                aRowValues[nFirstIndex][nSecondIndex++] = sValue;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            nFirstIndex++;
        }
        return aRowValues;
    }

    protected String[][] GetFlatTableData() {
        Map<String, Integer> mCounts = new LinkedHashMap<>();
        String[][] aRowValues = GetTableData();
        for (String[] aRow : aRowValues) {
            if (!mCounts.isEmpty()) {
                String sKey = Utilities.JoinString(aRow, 0, ",");
                if (!mCounts.containsKey(sKey)) {
                    mCounts.put(sKey, 1);
                } else {
                    int nCount = mCounts.get(sKey);
                    nCount += 1;
                    mCounts.put(sKey, nCount);
                }
            } else {
                mCounts.put(Utilities.JoinString(aRow, 0, ","), 1);
            }
        }
        int nIndex = 0;
        String[][] aFlatRowValues = new String[mCounts.keySet().size()][GetLoggedColumnNames().length + 1];
        for (String sFlatValues : mCounts.keySet()) {
            String[] aFlatValues = sFlatValues.split(",");
            String[] a = new String[aFlatValues.length + 1];
            System.arraycopy(aFlatValues, 0, a, 0, aFlatValues.length);
            a[aFlatValues.length] = (""+mCounts.get(sFlatValues));
            aFlatRowValues[nIndex++] = a;
        }
        return aFlatRowValues;
    }

    public DefaultTableModel ToDefaultTableModel(boolean bFlattened) {
        return new DefaultTableModel(GetDataTableValues(bFlattened), GetDataTableColumnNames(bFlattened));
    }

    public String ToFlattenedString() {
        int i = 1;
        StringBuilder sb = new StringBuilder("");
        if (!lLoadedList.isEmpty()) {
            sb.append(
                    "\n------------------------------------------------" +
                    "\n::  User Dump - <" + getClass().getSimpleName() + ">" +
                    "\n------------------------------------------------");
            for (T pLoadedObject : lLoadedList)
                try {
                    String sAccountName = "";
                    if (ListOfContains("dwAccountID", DB_GetColumnNames())) {
                        //if (List.of(DB_GetColumnNames()).contains("dwAccountID")) {
                        int dwID = (Integer) getClass().getDeclaredField("dwAccountID").get(pLoadedObject);
                        sAccountName = User.GetAccountNameFromAccountID(dwID);
                    } else if (ListOfContains("dwCharacterID", DB_GetColumnNames())) {
                    //} else if (List.of(DB_GetColumnNames()).contains("dwCharacterID")) {
                        int dwID = (Integer) getClass().getDeclaredField("dwCharacterID").get(pLoadedObject);
                        int dwAccountID = User.GetAccountIDFromCharID(dwID);
                        sAccountName = User.GetAccountNameFromAccountID(dwAccountID);
                        sAccountName += "\n  " + AddStringPadding(User.GetCharNameFromCharID(dwID), 16) + " (IGN)";
                    }
                    sb.append(String.format("\n  #%d - \n  %s  [%s]", i++, sAccountName == null || sAccountName.isBlank() ? "" : sAccountName + "\n", pLoadedObject.getClass().getSimpleName()));
                    for (Map.Entry<String, String> pEntry : GetLoggedColumnNames()) {
                        String sColumn = pEntry.getValue() == null ? "" : pEntry.getValue();
                        Object pValue = pEntry.getValue() == null ? "" : getClass().getDeclaredField(sColumn).get(pLoadedObject);
                        if (pValue != null) {
                            String sValue;
                            if (pEntry.getKey().equals("Ban Reason")) {
                                BanReason pBanReason = BanReason.GetReason((Integer) pValue);
                                sValue = pBanReason.name();
                            } else if (pEntry.getKey().equals("Registered Date")) {
                                FileTime ft = new FileTime((Long) pValue);
                                sValue = ft.toString();
                            } else {
                                if (pValue.getClass() == String.class) {
                                    sValue = "\"" + pValue + "\"";
                                } else {
                                    sValue = "" + pValue;
                                }
                            }
                            sb.append(String.format("\n\t\t\t%s%s", AddStringPadding(pEntry.getKey(), 32), sValue));
                        }
                    }
                    sb.append("\n");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            sb.append("\n");
        }
        return sb.toString();
    }
}
