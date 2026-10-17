package com.example.zzzznotch.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zzzznotch.data.models.RuleConfig;

import java.util.List;

@Dao
public interface RuleDao {

    @Query("SELECT * FROM rules ORDER BY id ASC")
    List<RuleConfig> getAllRules();

    @Query("SELECT * FROM rules WHERE enabled = 1")
    List<RuleConfig> getEnabledRules();

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    RuleConfig getRuleById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRule(RuleConfig rule);

    @Update
    int updateRule(RuleConfig rule);

    @Delete
    int deleteRule(RuleConfig rule);

    @Query("DELETE FROM rules WHERE id = :id")
    void deleteRuleById(long id);
}
