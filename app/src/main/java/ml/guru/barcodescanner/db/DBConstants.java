package ml.guru.barcodescanner.db;

public class DBConstants {
    public static final String STOCK_TABLE = "stock";
    public static final String ITEM_ID = "item_id";
    public static final String ITEM_PRESENT = "item_present";
    public static final String ITEM_COUNT = "item_count";

    public static final String CREATE_STOCK_TABLE = "CREATE TABLE IF NOT EXISTS " + STOCK_TABLE + " ("
            + ITEM_ID + " INTEGER PRIMARY KEY,"
            + ITEM_PRESENT + " BOOLEAN DEFAULT 0,"
            + ITEM_COUNT + " INTEGER DEFAULT 0)";

    public static final String SELECT_QUERY = "SELECT * FROM " + STOCK_TABLE;
}
