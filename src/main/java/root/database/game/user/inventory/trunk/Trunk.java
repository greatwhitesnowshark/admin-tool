package root.database.game.user.inventory.trunk;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import database.Config;
import database.snapshot.DBSnapshot;

public class Trunk extends DBSnapshot {

    public long nMoney;
    public int dwAccountID;
    public short nSlotCount;

    public Trunk(Object dwAccountID, boolean bAutoLoad) {
        super(dwAccountID, bAutoLoad);
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
    public String DB_GetKey() {
        return "dwAccountID";
    }

    @Override
    public String[] DB_GetColumnNames() {
        return new String[] {
                "dwAccountID",
                "nSlotCount",
                "nMoney"
        };
    }

    @Override
    public Entry<String, String>[] GetLoggedColumnNames() {
        return new Entry[] {
                new SimpleEntry<>("Account ID", "dwAccountID"),
                new SimpleEntry<>("Slot Count", "nSlotCount"),
                new SimpleEntry<>("Money", "nMoney")
        };
    }
}
