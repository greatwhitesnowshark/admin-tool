package database;

public class DBResult {
    public static final int
            /* If all GET operations completed successfully */
            SUCCESS             = 0,
    /* If all POST operations completed successfully */
    SUCCESS_BIND        = 1,
    /* If all POST operations completed successfully, but no rows were affected */
    SUCCESS_NOCHANGE    = 2,
    /* If all POST operations completed successfully, and are batch statements */
    SUCCESS_BATCH       = 3,
    /* If the SQL Query/Command is EMPTY or NULL */
    INVALID_COMMAND     = -1,
    /* If the UnifiedDB is NULL or an exception occurred while pooling the DB */
    INVALID_SESSION     = -2,
    /* If the PreparedStatement is NULL or an exception occurred while preparing it */
    INVALID_PROPSET     = -3,
    /* If a command argument is NULL or an exception occurred while setting it */
    INVALID_CMD_ARG     = -4,
    /* If the required parameter is a returned reference and it is NULL */
    INVALID_PARAMS      = -5,
    /* If all other operations have succeeded, but an SQL exception has occurred */
    UNKNOWN_ERROR       = -6;
}