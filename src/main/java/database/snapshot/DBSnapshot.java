package database.snapshot;

import database.Config;
import database.Database;
import root.BanReason;
import util.FileTime;
import util.Utilities;

import javax.swing.table.DefaultTableModel;

import static util.Utilities.AddStringPadding;

import java.lang.reflect.Array;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public abstract class DBSnapshot implements DBObject {

    public boolean bLoaded; //todo:: protected
    protected boolean bSaveReady, bAutoFlush;
    protected Object pKeyValue;
    protected Map<String, Object> mSnapshot; //stores all of the object's fields with field-name as key for a "snapshot" history reference
    protected Map<String, Object> mCachedUpdate; //stores all of the object's will-be-done updates; these objects are never deleted, only modified

    public DBSnapshot(Object pKeyValue) {
        this.mSnapshot = new LinkedHashMap<>(DB_GetColumnNames().length);
        this.mCachedUpdate = new LinkedHashMap<>(0);
        this.pKeyValue = pKeyValue;
        this.bLoaded = LoadFromDB();
        this.bAutoFlush = false;
    }

    public DBSnapshot(Object pKeyValue, boolean bAutoLoad) {
        this.mSnapshot = new LinkedHashMap<>(DB_GetColumnNames().length);
        this.mCachedUpdate = new LinkedHashMap<>(0);
        this.pKeyValue = pKeyValue;
        this.bLoaded = bAutoLoad && LoadFromDB();
        this.bAutoFlush = false;
    }

    public DBSnapshot(Object pKeyValue, boolean bAutoLoad, boolean bAutoFlush) {
        this.mSnapshot = new LinkedHashMap<>(DB_GetColumnNames().length);
        this.mCachedUpdate = new LinkedHashMap<>(0);
        this.pKeyValue = pKeyValue;
        if (bAutoLoad) {
            this.bLoaded = LoadFromDB();
        }
        this.bAutoFlush = bAutoFlush;
    }

    public void Update() {
        for (String sFieldName : DB_GetColumnNames()) try {
            Object pLastValue = mCachedUpdate.get(sFieldName);
            if (pLastValue == null) {
                pLastValue = mSnapshot.get(sFieldName);
            }
            Object pNewValue = getClass().getDeclaredField(sFieldName).get(this);
            if (pNewValue == null) {
                pNewValue = pLastValue;
            }
            if (!Objects.deepEquals(pLastValue, pNewValue)) {
                mCachedUpdate.put(sFieldName, pNewValue);
                if (!bSaveReady) {
                    bSaveReady = true;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (bSaveReady && bAutoFlush) {
            SaveToDB();
        }
    }

    public synchronized void Flush() {
        Update(); //failsafe
        SaveToDB();
    }

    protected boolean SaveToDB() {
        if (!mCachedUpdate.isEmpty()) {
            try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(DB_GetUpdateQuery(), Statement.RETURN_GENERATED_KEYS)) {
                int i = 1;
                for (Entry<String, Object> o : mCachedUpdate.entrySet()) {
                    ps.setObject(i++, o.getValue());
                }
                Database.execute(con, ps, mCachedUpdate.values());
                for (Entry<String, Object> pUpdated : mCachedUpdate.entrySet()) {
                    mSnapshot.put(pUpdated.getKey(), pUpdated.getValue());
                }
                mCachedUpdate.clear();
                if (!bLoaded) {
                    bLoaded = true;
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected void LoadFromSelf() {
        mSnapshot.clear();
        mCachedUpdate.clear();
        for (int i = 0; i < DB_GetColumnNames().length; i++) {
            try {
                Object pValue = getClass().getDeclaredField(DB_GetColumnNames()[i]).get(this);
                mSnapshot.put(DB_GetColumnNames()[i], pValue);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean LoadFromDB() {
        if (!bLoaded) {
            String[] aColumnNames = DB_GetColumnNames();
            Class<?>[] aColumnTypes = DB_GetColumnTypes();
            String sSelectQuery = DB_GetSelectQuery();
            if (!sSelectQuery.contains(" = ") && !sSelectQuery.contains("like")) {
                return false;
            }
            try (Connection con = Database.connection(); PreparedStatement ps = con.prepareStatement(sSelectQuery, Statement.RETURN_GENERATED_KEYS)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        for (int i = 0; i < aColumnNames.length; i++) {
                            Object pValue = rs.getObject((i + 1), aColumnTypes[i]);
                            try {
                                getClass().getDeclaredField(aColumnNames[i]).set(this, pValue);
                            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            mSnapshot.put(aColumnNames[i], pValue);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return !mSnapshot.isEmpty();
    }

    public String GetAdditionalArguments() { //override for adding multiple location arguments in the select query
        return "";
    }

    private String GetColumnNames() {
        return Utilities.JoinString(DB_GetColumnNames(), 0, ", ");
    }

    private String GetValues() {
        StringBuilder sValues = new StringBuilder();
        for (int i = 0; i < mSnapshot.keySet().size(); i++) {
            if (i != 0) {
                sValues.append(", ?");
            } else {
                sValues.append("?");
            }
        }
        return sValues.toString();
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
        return String.format("INSERT INTO `%s`.`%s` (%s) VALUES (%s)", DB_GetSchema(), DB_GetTable(), GetColumnNames(), GetValues());
    }

    @Override
    public String DB_GetUpdateQuery() {
        if (bLoaded) {
            int i = 0;
            StringBuilder sBuilder = new StringBuilder();
            for (String sKey : mCachedUpdate.keySet()) {
                if (i++ != 0) {
                    sBuilder.append(", ");
                }
                sBuilder.append(String.format("`%s` = ?", sKey));
            }
            if (i > 0) {
                return String.format("INSERT INTO `%s`.`%s` SET %s WHERE %s", DB_GetSchema(), DB_GetTable(), sBuilder.toString(), GetLocationPart());
            }
        }
        return DB_GetInsertQuery();
    }

    private String GetLocationPart() {
        String sKeyValue = pKeyValue instanceof String ? String.format("\"%s\"", (""+pKeyValue)) : (""+pKeyValue);
        return String.format("`%s` = %s%s", DB_GetKey(), sKeyValue, GetAdditionalArguments()).trim();
    }

    @Override
    public String DB_GetSelectQuery() {
        String s = String.format("SELECT %s FROM `%s`.`%s` WHERE %s", GetColumnNames(), DB_GetSchema(), DB_GetTable(), GetLocationPart()).trim();
        return s;
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

    @Override
    public String ToString() {
        LoadFromSelf();
        String sHeader = "\n------------------------------------------------" +
                         "\n::  " + (getClass().getSimpleName()) + " ::" +
                         "\n------------------------------------------------\n";
        StringBuilder s = new StringBuilder(sHeader);
        for (String sColumn : DB_GetColumnNames()) {
            Object pValue = mSnapshot.get(sColumn);
            if (pValue != null) {
                String sValue;
                if (pValue.getClass() == String.class) {
                    sValue = "\"" + pValue + "\"";
                } else {
                    sValue = "" + pValue;
                }
                s.append(String.format("\t`%s` - %s\n", sColumn, sValue));
            }
        }
        s.append("\n");
        return s.toString();
    }

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
        String[][] aRowValues = new String[1][GetLoggedColumnNames().length];
        int nIndex = 0;
        for (Map.Entry<String, String> pEntry : GetLoggedColumnNames()) try {
            String sColumn = pEntry.getValue() == null ? "" : pEntry.getValue();
            Object pValue = pEntry.getValue() == null ? "" : getClass().getDeclaredField(sColumn).get(this);
            String sValue;
            if (pEntry.getKey().equals("Ban Reason")) {
                BanReason pBanReason = BanReason.GetReason((Integer) pValue);
                sValue = pBanReason.name();
            } else if (pEntry.getKey().equals("Registered Date")) {
                FileTime ft = new FileTime((Long) pValue);
                sValue = ft.toString();
            } else {
                //if (pValue.getClass() == String.class) {
                //sValue = "\"" + pValue + "\"";
                //} else {
                sValue = "" + pValue;
                //}
            }
            aRowValues[0][nIndex++] = sValue;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
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
        StringBuilder sb = new StringBuilder();
        sb.append(
                "\n------------------------------------------------" +
                "\n::  User Dump - <" + getClass().getSimpleName() + ">" +
                "\n------------------------------------------------");
        for (Map.Entry<String, String> pEntry : GetLoggedColumnNames()) try {
            String sLabel = pEntry.getKey();
            String sColumn = pEntry.getValue();
            Object pValue = getClass().getDeclaredField(sColumn).get(this);
            if (pValue != null) {
                String sValue;
                if (sLabel.equals("Ban Reason")) {
                    BanReason pBanReason = BanReason.GetReason((Integer) pValue);
                    sValue = pBanReason.name();
                } else if (pEntry.getValue().startsWith("ft")) {
                    FileTime ft = new FileTime((Long) pValue);
                    sValue = ft.toString();
                } else {
                    if (pValue.getClass() == String.class) {
                        sValue = "\"" + pValue + "\"";
                    } else {
                        sValue = "" + pValue;
                    }
                }
                sb.append(String.format("\n\t\t\t%s%s", AddStringPadding(sLabel, 32), sValue));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        sb.append("\n");
        return sb.toString();
    }
}
