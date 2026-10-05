package com.lucashollmann.incrementalperiodiccounter.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CounterDatabase_Impl extends CounterDatabase {
  private volatile CounterDao _counterDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(8) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `counters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `value` INTEGER NOT NULL, `normal_increment` INTEGER NOT NULL, `secondary_increment` INTEGER, `allow_negative` INTEGER NOT NULL DEFAULT 0, `notification_times` TEXT NOT NULL DEFAULT '', `notification_days` TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7', `notification_schedules` TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE IF NOT EXISTS `counter_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `counter_id` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `new_value` INTEGER NOT NULL, `step` INTEGER NOT NULL, FOREIGN KEY(`counter_id`) REFERENCES `counters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_counter_history_counter_id_timestamp` ON `counter_history` (`counter_id`, `timestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6e38c66a8f82ff5ef9bc68b67a979c89')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `counters`");
        db.execSQL("DROP TABLE IF EXISTS `counter_history`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCounters = new HashMap<String, TableInfo.Column>(9);
        _columnsCounters.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("value", new TableInfo.Column("value", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("normal_increment", new TableInfo.Column("normal_increment", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("secondary_increment", new TableInfo.Column("secondary_increment", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("allow_negative", new TableInfo.Column("allow_negative", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("notification_times", new TableInfo.Column("notification_times", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("notification_days", new TableInfo.Column("notification_days", "TEXT", true, 0, "'1,2,3,4,5,6,7'", TableInfo.CREATED_FROM_ENTITY));
        _columnsCounters.put("notification_schedules", new TableInfo.Column("notification_schedules", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCounters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCounters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCounters = new TableInfo("counters", _columnsCounters, _foreignKeysCounters, _indicesCounters);
        final TableInfo _existingCounters = TableInfo.read(db, "counters");
        if (!_infoCounters.equals(_existingCounters)) {
          return new RoomOpenHelper.ValidationResult(false, "counters(com.lucashollmann.incrementalperiodiccounter.data.Counter).\n"
                  + " Expected:\n" + _infoCounters + "\n"
                  + " Found:\n" + _existingCounters);
        }
        final HashMap<String, TableInfo.Column> _columnsCounterHistory = new HashMap<String, TableInfo.Column>(5);
        _columnsCounterHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounterHistory.put("counter_id", new TableInfo.Column("counter_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounterHistory.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounterHistory.put("new_value", new TableInfo.Column("new_value", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCounterHistory.put("step", new TableInfo.Column("step", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCounterHistory = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCounterHistory.add(new TableInfo.ForeignKey("counters", "CASCADE", "NO ACTION", Arrays.asList("counter_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesCounterHistory = new HashSet<TableInfo.Index>(1);
        _indicesCounterHistory.add(new TableInfo.Index("index_counter_history_counter_id_timestamp", false, Arrays.asList("counter_id", "timestamp"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoCounterHistory = new TableInfo("counter_history", _columnsCounterHistory, _foreignKeysCounterHistory, _indicesCounterHistory);
        final TableInfo _existingCounterHistory = TableInfo.read(db, "counter_history");
        if (!_infoCounterHistory.equals(_existingCounterHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "counter_history(com.lucashollmann.incrementalperiodiccounter.data.CounterHistoryEntry).\n"
                  + " Expected:\n" + _infoCounterHistory + "\n"
                  + " Found:\n" + _existingCounterHistory);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6e38c66a8f82ff5ef9bc68b67a979c89", "6439a161f310b8592b1e91eb138d9a43");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "counters","counter_history");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `counters`");
      _db.execSQL("DELETE FROM `counter_history`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CounterDao.class, CounterDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CounterDao counterDao() {
    if (_counterDao != null) {
      return _counterDao;
    } else {
      synchronized(this) {
        if(_counterDao == null) {
          _counterDao = new CounterDao_Impl(this);
        }
        return _counterDao;
      }
    }
  }
}
