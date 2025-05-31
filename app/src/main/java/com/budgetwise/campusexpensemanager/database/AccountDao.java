package com.budgetwise.campusexpensemanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

import com.budgetwise.campusexpensemanager.models.Account;

@Dao
public interface AccountDao {
    @Insert
    void insert(Account account);

    @Query("SELECT * FROM accounts WHERE username = :username LIMIT 1")
    Account findByUsername(String username);

    @Query("SELECT * FROM accounts")
    List<Account> getAllAccounts();
}
