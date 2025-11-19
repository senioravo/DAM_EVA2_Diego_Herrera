package cl.duoc.app.model.data.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.duoc.app.model.data.entities.PlantelPlantEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class PlantelPlantDao_Impl implements PlantelPlantDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlantelPlantEntity> __insertionAdapterOfPlantelPlantEntity;

  private final EntityDeletionOrUpdateAdapter<PlantelPlantEntity> __deletionAdapterOfPlantelPlantEntity;

  private final EntityDeletionOrUpdateAdapter<PlantelPlantEntity> __updateAdapterOfPlantelPlantEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUserPlants;

  public PlantelPlantDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlantelPlantEntity = new EntityInsertionAdapter<PlantelPlantEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `plantel_plants` (`id`,`productId`,`userId`,`addedDate`,`lastWateredDate`,`assistanceStarted`,`customTitle`,`wateringHistory`,`notificationsEnabled`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlantelPlantEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProductId());
        statement.bindLong(3, entity.getUserId());
        statement.bindLong(4, entity.getAddedDate());
        if (entity.getLastWateredDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getLastWateredDate());
        }
        final int _tmp = entity.getAssistanceStarted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getCustomTitle() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCustomTitle());
        }
        statement.bindString(8, entity.getWateringHistory());
        final int _tmp_1 = entity.getNotificationsEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
      }
    };
    this.__deletionAdapterOfPlantelPlantEntity = new EntityDeletionOrUpdateAdapter<PlantelPlantEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `plantel_plants` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlantelPlantEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPlantelPlantEntity = new EntityDeletionOrUpdateAdapter<PlantelPlantEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `plantel_plants` SET `id` = ?,`productId` = ?,`userId` = ?,`addedDate` = ?,`lastWateredDate` = ?,`assistanceStarted` = ?,`customTitle` = ?,`wateringHistory` = ?,`notificationsEnabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlantelPlantEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProductId());
        statement.bindLong(3, entity.getUserId());
        statement.bindLong(4, entity.getAddedDate());
        if (entity.getLastWateredDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getLastWateredDate());
        }
        final int _tmp = entity.getAssistanceStarted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getCustomTitle() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCustomTitle());
        }
        statement.bindString(8, entity.getWateringHistory());
        final int _tmp_1 = entity.getNotificationsEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteUserPlants = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM plantel_plants WHERE userId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPlant(final PlantelPlantEntity plant,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPlantelPlantEntity.insertAndReturnId(plant);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlant(final PlantelPlantEntity plant,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPlantelPlantEntity.handle(plant);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePlant(final PlantelPlantEntity plant,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPlantelPlantEntity.handle(plant);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUserPlants(final int userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUserPlants.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, userId);
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
          __preparedStmtOfDeleteUserPlants.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlantelPlantEntity>> getUserPlants(final int userId) {
    final String _sql = "SELECT * FROM plantel_plants WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"plantel_plants"}, new Callable<List<PlantelPlantEntity>>() {
      @Override
      @NonNull
      public List<PlantelPlantEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAddedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "addedDate");
          final int _cursorIndexOfLastWateredDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastWateredDate");
          final int _cursorIndexOfAssistanceStarted = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceStarted");
          final int _cursorIndexOfCustomTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "customTitle");
          final int _cursorIndexOfWateringHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "wateringHistory");
          final int _cursorIndexOfNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationsEnabled");
          final List<PlantelPlantEntity> _result = new ArrayList<PlantelPlantEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlantelPlantEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpProductId;
            _tmpProductId = _cursor.getInt(_cursorIndexOfProductId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final long _tmpAddedDate;
            _tmpAddedDate = _cursor.getLong(_cursorIndexOfAddedDate);
            final Long _tmpLastWateredDate;
            if (_cursor.isNull(_cursorIndexOfLastWateredDate)) {
              _tmpLastWateredDate = null;
            } else {
              _tmpLastWateredDate = _cursor.getLong(_cursorIndexOfLastWateredDate);
            }
            final boolean _tmpAssistanceStarted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAssistanceStarted);
            _tmpAssistanceStarted = _tmp != 0;
            final String _tmpCustomTitle;
            if (_cursor.isNull(_cursorIndexOfCustomTitle)) {
              _tmpCustomTitle = null;
            } else {
              _tmpCustomTitle = _cursor.getString(_cursorIndexOfCustomTitle);
            }
            final String _tmpWateringHistory;
            _tmpWateringHistory = _cursor.getString(_cursorIndexOfWateringHistory);
            final boolean _tmpNotificationsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationsEnabled);
            _tmpNotificationsEnabled = _tmp_1 != 0;
            _item = new PlantelPlantEntity(_tmpId,_tmpProductId,_tmpUserId,_tmpAddedDate,_tmpLastWateredDate,_tmpAssistanceStarted,_tmpCustomTitle,_tmpWateringHistory,_tmpNotificationsEnabled);
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
  public Object getPlantById(final int plantId,
      final Continuation<? super PlantelPlantEntity> $completion) {
    final String _sql = "SELECT * FROM plantel_plants WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, plantId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlantelPlantEntity>() {
      @Override
      @Nullable
      public PlantelPlantEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAddedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "addedDate");
          final int _cursorIndexOfLastWateredDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastWateredDate");
          final int _cursorIndexOfAssistanceStarted = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceStarted");
          final int _cursorIndexOfCustomTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "customTitle");
          final int _cursorIndexOfWateringHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "wateringHistory");
          final int _cursorIndexOfNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationsEnabled");
          final PlantelPlantEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpProductId;
            _tmpProductId = _cursor.getInt(_cursorIndexOfProductId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final long _tmpAddedDate;
            _tmpAddedDate = _cursor.getLong(_cursorIndexOfAddedDate);
            final Long _tmpLastWateredDate;
            if (_cursor.isNull(_cursorIndexOfLastWateredDate)) {
              _tmpLastWateredDate = null;
            } else {
              _tmpLastWateredDate = _cursor.getLong(_cursorIndexOfLastWateredDate);
            }
            final boolean _tmpAssistanceStarted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAssistanceStarted);
            _tmpAssistanceStarted = _tmp != 0;
            final String _tmpCustomTitle;
            if (_cursor.isNull(_cursorIndexOfCustomTitle)) {
              _tmpCustomTitle = null;
            } else {
              _tmpCustomTitle = _cursor.getString(_cursorIndexOfCustomTitle);
            }
            final String _tmpWateringHistory;
            _tmpWateringHistory = _cursor.getString(_cursorIndexOfWateringHistory);
            final boolean _tmpNotificationsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationsEnabled);
            _tmpNotificationsEnabled = _tmp_1 != 0;
            _result = new PlantelPlantEntity(_tmpId,_tmpProductId,_tmpUserId,_tmpAddedDate,_tmpLastWateredDate,_tmpAssistanceStarted,_tmpCustomTitle,_tmpWateringHistory,_tmpNotificationsEnabled);
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
  public Object getPlantByUserAndProduct(final int userId, final int productId,
      final Continuation<? super PlantelPlantEntity> $completion) {
    final String _sql = "SELECT * FROM plantel_plants WHERE userId = ? AND productId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlantelPlantEntity>() {
      @Override
      @Nullable
      public PlantelPlantEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAddedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "addedDate");
          final int _cursorIndexOfLastWateredDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastWateredDate");
          final int _cursorIndexOfAssistanceStarted = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceStarted");
          final int _cursorIndexOfCustomTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "customTitle");
          final int _cursorIndexOfWateringHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "wateringHistory");
          final int _cursorIndexOfNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationsEnabled");
          final PlantelPlantEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpProductId;
            _tmpProductId = _cursor.getInt(_cursorIndexOfProductId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final long _tmpAddedDate;
            _tmpAddedDate = _cursor.getLong(_cursorIndexOfAddedDate);
            final Long _tmpLastWateredDate;
            if (_cursor.isNull(_cursorIndexOfLastWateredDate)) {
              _tmpLastWateredDate = null;
            } else {
              _tmpLastWateredDate = _cursor.getLong(_cursorIndexOfLastWateredDate);
            }
            final boolean _tmpAssistanceStarted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAssistanceStarted);
            _tmpAssistanceStarted = _tmp != 0;
            final String _tmpCustomTitle;
            if (_cursor.isNull(_cursorIndexOfCustomTitle)) {
              _tmpCustomTitle = null;
            } else {
              _tmpCustomTitle = _cursor.getString(_cursorIndexOfCustomTitle);
            }
            final String _tmpWateringHistory;
            _tmpWateringHistory = _cursor.getString(_cursorIndexOfWateringHistory);
            final boolean _tmpNotificationsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationsEnabled);
            _tmpNotificationsEnabled = _tmp_1 != 0;
            _result = new PlantelPlantEntity(_tmpId,_tmpProductId,_tmpUserId,_tmpAddedDate,_tmpLastWateredDate,_tmpAssistanceStarted,_tmpCustomTitle,_tmpWateringHistory,_tmpNotificationsEnabled);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
