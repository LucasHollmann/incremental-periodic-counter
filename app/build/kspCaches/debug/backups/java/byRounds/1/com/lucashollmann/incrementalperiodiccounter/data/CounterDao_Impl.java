package com.lucashollmann.incrementalperiodiccounter.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CounterDao_Impl implements CounterDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Counter> __insertionAdapterOfCounter;

  private final EntityInsertionAdapter<CounterHistoryEntry> __insertionAdapterOfCounterHistoryEntry;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCounter;

  private final SharedSQLiteStatement __preparedStmtOfUpdateCounterValue;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCounterHistory;

  private final SharedSQLiteStatement __preparedStmtOfUpdateCounterSettings;

  public CounterDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCounter = new EntityInsertionAdapter<Counter>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `counters` (`id`,`name`,`value`,`normal_increment`,`secondary_increment`,`allow_negative`,`notification_times`,`notification_days`,`notification_schedules`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Counter entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getValue());
        statement.bindLong(4, entity.getNormalIncrement());
        if (entity.getSecondaryIncrement() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getSecondaryIncrement());
        }
        final int _tmp = entity.getAllowNegative() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindString(7, entity.getNotificationTimes());
        statement.bindString(8, entity.getNotificationDays());
        statement.bindString(9, entity.getNotificationSchedules());
      }
    };
    this.__insertionAdapterOfCounterHistoryEntry = new EntityInsertionAdapter<CounterHistoryEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `counter_history` (`id`,`counter_id`,`timestamp`,`new_value`,`step`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CounterHistoryEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCounterId());
        statement.bindLong(3, entity.getTimestamp());
        statement.bindLong(4, entity.getNewValue());
        statement.bindLong(5, entity.getStep());
      }
    };
    this.__preparedStmtOfDeleteCounter = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM counters WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateCounterValue = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE counters SET value = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCounterHistory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM counter_history WHERE counter_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateCounterSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE counters\n"
                + "        SET name = ?,\n"
                + "            normal_increment = ?,\n"
                + "            secondary_increment = ?,\n"
                + "            notification_times = '',\n"
                + "            notification_days = '1,2,3,4,5,6,7',\n"
                + "            notification_schedules = ?,\n"
                + "            value = CASE\n"
                + "                WHEN ? = 0 AND value < 0 THEN 0\n"
                + "                ELSE value\n"
                + "            END,\n"
                + "            allow_negative = ?\n"
                + "        WHERE id = ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insertCounter(final Counter counter, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCounter.insert(counter);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertHistoryEntry(final CounterHistoryEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCounterHistoryEntry.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object changeCounter(final long counterId, final long step,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> CounterDao.DefaultImpls.changeCounter(CounterDao_Impl.this, counterId, step, __cont), $completion);
  }

  @Override
  public Object setCounterValue(final long counterId, final int value,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> CounterDao.DefaultImpls.setCounterValue(CounterDao_Impl.this, counterId, value, __cont), $completion);
  }

  @Override
  public Object resetCounter(final long counterId, final boolean clearHistory,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> CounterDao.DefaultImpls.resetCounter(CounterDao_Impl.this, counterId, clearHistory, __cont), $completion);
  }

  @Override
  public Object deleteCounter(final long counterId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCounter.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, counterId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCounter.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCounterValue(final long counterId, final int value,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCounterValue.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, value);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, counterId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateCounterValue.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCounterHistory(final long counterId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCounterHistory.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, counterId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCounterHistory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCounterSettings(final long counterId, final String name,
      final int normalIncrement, final Integer secondaryIncrement, final boolean allowNegative,
      final String notificationSchedules, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCounterSettings.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, normalIncrement);
        _argIndex = 3;
        if (secondaryIncrement == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, secondaryIncrement);
        }
        _argIndex = 4;
        _stmt.bindString(_argIndex, notificationSchedules);
        _argIndex = 5;
        final int _tmp = allowNegative ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 6;
        final int _tmp_1 = allowNegative ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 7;
        _stmt.bindLong(_argIndex, counterId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateCounterSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Counter>> observeCounters() {
    final String _sql = "SELECT * FROM counters ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"counters"}, new Callable<List<Counter>>() {
      @Override
      @NonNull
      public List<Counter> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfNormalIncrement = CursorUtil.getColumnIndexOrThrow(_cursor, "normal_increment");
          final int _cursorIndexOfSecondaryIncrement = CursorUtil.getColumnIndexOrThrow(_cursor, "secondary_increment");
          final int _cursorIndexOfAllowNegative = CursorUtil.getColumnIndexOrThrow(_cursor, "allow_negative");
          final int _cursorIndexOfNotificationTimes = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_times");
          final int _cursorIndexOfNotificationDays = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_days");
          final int _cursorIndexOfNotificationSchedules = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_schedules");
          final List<Counter> _result = new ArrayList<Counter>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Counter _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final int _tmpNormalIncrement;
            _tmpNormalIncrement = _cursor.getInt(_cursorIndexOfNormalIncrement);
            final Integer _tmpSecondaryIncrement;
            if (_cursor.isNull(_cursorIndexOfSecondaryIncrement)) {
              _tmpSecondaryIncrement = null;
            } else {
              _tmpSecondaryIncrement = _cursor.getInt(_cursorIndexOfSecondaryIncrement);
            }
            final boolean _tmpAllowNegative;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAllowNegative);
            _tmpAllowNegative = _tmp != 0;
            final String _tmpNotificationTimes;
            _tmpNotificationTimes = _cursor.getString(_cursorIndexOfNotificationTimes);
            final String _tmpNotificationDays;
            _tmpNotificationDays = _cursor.getString(_cursorIndexOfNotificationDays);
            final String _tmpNotificationSchedules;
            _tmpNotificationSchedules = _cursor.getString(_cursorIndexOfNotificationSchedules);
            _item = new Counter(_tmpId,_tmpName,_tmpValue,_tmpNormalIncrement,_tmpSecondaryIncrement,_tmpAllowNegative,_tmpNotificationTimes,_tmpNotificationDays,_tmpNotificationSchedules);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCounterCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM counters";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCounter(final long counterId, final Continuation<? super Counter> $completion) {
    final String _sql = "SELECT * FROM counters WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, counterId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Counter>() {
      @Override
      @Nullable
      public Counter call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfNormalIncrement = CursorUtil.getColumnIndexOrThrow(_cursor, "normal_increment");
          final int _cursorIndexOfSecondaryIncrement = CursorUtil.getColumnIndexOrThrow(_cursor, "secondary_increment");
          final int _cursorIndexOfAllowNegative = CursorUtil.getColumnIndexOrThrow(_cursor, "allow_negative");
          final int _cursorIndexOfNotificationTimes = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_times");
          final int _cursorIndexOfNotificationDays = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_days");
          final int _cursorIndexOfNotificationSchedules = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_schedules");
          final Counter _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final int _tmpNormalIncrement;
            _tmpNormalIncrement = _cursor.getInt(_cursorIndexOfNormalIncrement);
            final Integer _tmpSecondaryIncrement;
            if (_cursor.isNull(_cursorIndexOfSecondaryIncrement)) {
              _tmpSecondaryIncrement = null;
            } else {
              _tmpSecondaryIncrement = _cursor.getInt(_cursorIndexOfSecondaryIncrement);
            }
            final boolean _tmpAllowNegative;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAllowNegative);
            _tmpAllowNegative = _tmp != 0;
            final String _tmpNotificationTimes;
            _tmpNotificationTimes = _cursor.getString(_cursorIndexOfNotificationTimes);
            final String _tmpNotificationDays;
            _tmpNotificationDays = _cursor.getString(_cursorIndexOfNotificationDays);
            final String _tmpNotificationSchedules;
            _tmpNotificationSchedules = _cursor.getString(_cursorIndexOfNotificationSchedules);
            _result = new Counter(_tmpId,_tmpName,_tmpValue,_tmpNormalIncrement,_tmpSecondaryIncrement,_tmpAllowNegative,_tmpNotificationTimes,_tmpNotificationDays,_tmpNotificationSchedules);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllCounters(final Continuation<? super List<Counter>> $completion) {
    final String _sql = "SELECT * FROM counters";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Counter>>() {
      @Override
      @NonNull
      public List<Counter> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfNormalIncrement = CursorUtil.getColumnIndexOrThrow(_cursor, "normal_increment");
          final int _cursorIndexOfSecondaryIncrement = CursorUtil.getColumnIndexOrThrow(_cursor, "secondary_increment");
          final int _cursorIndexOfAllowNegative = CursorUtil.getColumnIndexOrThrow(_cursor, "allow_negative");
          final int _cursorIndexOfNotificationTimes = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_times");
          final int _cursorIndexOfNotificationDays = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_days");
          final int _cursorIndexOfNotificationSchedules = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_schedules");
          final List<Counter> _result = new ArrayList<Counter>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Counter _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final int _tmpNormalIncrement;
            _tmpNormalIncrement = _cursor.getInt(_cursorIndexOfNormalIncrement);
            final Integer _tmpSecondaryIncrement;
            if (_cursor.isNull(_cursorIndexOfSecondaryIncrement)) {
              _tmpSecondaryIncrement = null;
            } else {
              _tmpSecondaryIncrement = _cursor.getInt(_cursorIndexOfSecondaryIncrement);
            }
            final boolean _tmpAllowNegative;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAllowNegative);
            _tmpAllowNegative = _tmp != 0;
            final String _tmpNotificationTimes;
            _tmpNotificationTimes = _cursor.getString(_cursorIndexOfNotificationTimes);
            final String _tmpNotificationDays;
            _tmpNotificationDays = _cursor.getString(_cursorIndexOfNotificationDays);
            final String _tmpNotificationSchedules;
            _tmpNotificationSchedules = _cursor.getString(_cursorIndexOfNotificationSchedules);
            _item = new Counter(_tmpId,_tmpName,_tmpValue,_tmpNormalIncrement,_tmpSecondaryIncrement,_tmpAllowNegative,_tmpNotificationTimes,_tmpNotificationDays,_tmpNotificationSchedules);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CounterHistoryEntry>> observeHistory(final long counterId) {
    final String _sql = "SELECT * FROM counter_history WHERE counter_id = ? ORDER BY timestamp DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, counterId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"counter_history"}, new Callable<List<CounterHistoryEntry>>() {
      @Override
      @NonNull
      public List<CounterHistoryEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCounterId = CursorUtil.getColumnIndexOrThrow(_cursor, "counter_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNewValue = CursorUtil.getColumnIndexOrThrow(_cursor, "new_value");
          final int _cursorIndexOfStep = CursorUtil.getColumnIndexOrThrow(_cursor, "step");
          final List<CounterHistoryEntry> _result = new ArrayList<CounterHistoryEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CounterHistoryEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCounterId;
            _tmpCounterId = _cursor.getLong(_cursorIndexOfCounterId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpNewValue;
            _tmpNewValue = _cursor.getInt(_cursorIndexOfNewValue);
            final long _tmpStep;
            _tmpStep = _cursor.getLong(_cursorIndexOfStep);
            _item = new CounterHistoryEntry(_tmpId,_tmpCounterId,_tmpTimestamp,_tmpNewValue,_tmpStep);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
