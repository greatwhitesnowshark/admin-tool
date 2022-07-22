package database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
public class DBHelper {

    private static int bind(PreparedStatement propSet, Object... command) {
        for (int i = 1; i <= command.length; i++) {
            Object cmd = command[i - 1];
            if (cmd != null) {
                try {
                    if (cmd instanceof Number) {
                        // Specific to only setByte calls, default Integer
                        if (cmd instanceof Byte) {
                            propSet.setByte(i, (Byte) cmd);
                        } else if (cmd instanceof Short) {
                            propSet.setShort(i, (Short) cmd);
                            // Specific to only setLong calls, default Integer
                        } else if (cmd instanceof Long) {
                            propSet.setLong(i, (Long) cmd);
                        } else if (cmd instanceof Double) {
                            propSet.setDouble(i, (Double) cmd);
                            // Almost all types are INT(11), so default to this
                        } else {
                            propSet.setInt(i, (Integer) cmd);
                        }
                        // If it is otherwise a String, we only require setString
                    } else if (cmd instanceof String) {
                        propSet.setString(i, (String) cmd);
                    } else if (cmd instanceof Boolean) {
                        propSet.setBoolean(i, (Boolean) cmd);
                    } else if (cmd instanceof Timestamp) {
                        propSet.setTimestamp(i, (Timestamp) cmd);
                    } else if (cmd instanceof Serializable) {
                        propSet.setObject(i, cmd);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                return DBResult.INVALID_CMD_ARG;
            }
        }
        return DBResult.SUCCESS_BIND;
    }

    public static int softExecute(PreparedStatement propSet, Object... command) throws SQLException {
        if (propSet != null) {
            int result = DBResult.SUCCESS_BIND;
            if (command != null)
                result = bind(propSet, command);

            if (result > 0) {
                int rowsAffected = propSet.executeUpdate();
                if (rowsAffected == 0) {
                    return DBResult.SUCCESS_NOCHANGE;
                }
                return DBResult.SUCCESS_BIND;
            }
            return result;
        }
        return DBResult.INVALID_COMMAND;
    }

    public static int execute(Connection con, PreparedStatement propSet, Object... command) throws SQLException {
        if (propSet != null) {
            int result = bind(propSet, command);

            if (result > 0) {
                int rowsAffected = propSet.executeUpdate();
                if (rowsAffected == 0) {
                    String query = propSet.toString();
                    // The only valid DML statement for re-insertion is UPDATE.
                    if (!query.contains("DELETE FROM") && !query.contains("INSERT INTO")) {
                        // Substring based on if the query contains '?' IN params or not
                        if (query.contains("', parameters"))
                            query = query.substring(query.indexOf("UPDATE"), query.indexOf("', parameters"));
                        else
                            query = query.substring(query.indexOf("UPDATE"));

                        // Begin the new query, starting by converting an update to an insert
                        String newQuery = query.replaceAll("UPDATE", "INSERT INTO");

                        // Substring the FRONT rows (prior to WHERE condition)
                        String rows;
                        if (newQuery.contains("WHERE"))
                            rows = newQuery.substring(newQuery.indexOf("SET ") + "SET ".length(), newQuery.indexOf("WHERE "));
                        else
                            rows = newQuery.substring(newQuery.indexOf("SET ") + "SET ".length());
                        // Construct an array of every front row
                        String[] frontRows = rows.replaceAll(" = \\?, ", ", ").replaceAll(" = \\? ", ", ").split(", ");
                        // Not all queries perform an UPDATE with a WHERE condition, allocate empty back rows
                        String[] backRows = { };
                        // If the query does contain a WHERE condition, parse the back rows (everything after WHERE)
                        if (newQuery.contains("WHERE")) {
                            rows = newQuery.substring(newQuery.indexOf("WHERE ") + "WHERE ".length());
                            backRows = rows.replaceAll(" = \\? AND ", ", ").replaceAll(" = \\?", ", ").split(", ");
                        }
                        // Merge the front and back rows into one table, these are all columns being inserted
                        String[] allRows = new String[frontRows.length + backRows.length];
                        System.arraycopy(frontRows, 0, allRows, 0, frontRows.length);
                        System.arraycopy(backRows, 0, allRows, frontRows.length, backRows.length);

                        // Begin transforming the query - clear the rest of the string, transform to (Col1, Col2, Col3)
                        newQuery = newQuery.substring(0, newQuery.indexOf("SET "));
                        newQuery += "(";
                        for (String key : allRows) {
                            newQuery += key + ", ";
                        }
                        // Trim the remaining , added at the end of the last column
                        newQuery = newQuery.substring(0, newQuery.length() - ", ".length());

                        // Begin appending the VALUES(?, ?) for the total size there is rows
                        newQuery += ") VALUES(";
                        for (String notUsed : allRows) {
                            newQuery += "?, ";
                        }
                        // Trim the remaining , added at the end of the last column
                        newQuery = newQuery.substring(0, newQuery.length() - ", ".length());
                        newQuery += ")";

                        return execute(con, con.prepareStatement(newQuery), command);
                    }
                }
                return rowsAffected;
            }
            return result;
        }
        return DBResult.INVALID_COMMAND;
    }
}