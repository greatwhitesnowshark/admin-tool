package database.snapshot;

public interface DBObject {
    String ToString();
    String DB_GetKey();
    String DB_GetTable();
    String DB_GetSchema();
    String DB_GetInsertQuery();
    String DB_GetUpdateQuery();
    String DB_GetSelectQuery();
    String[] DB_GetColumnNames();
    Class<?>[] DB_GetColumnTypes();
    boolean DB_IncrementKey();
}
