package com.budgetwise.campusexpensemanager.database;

import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

@Dao
public interface RawQueryDao {
    @RawQuery
    int performCheckpoint(SupportSQLiteQuery query);
}
