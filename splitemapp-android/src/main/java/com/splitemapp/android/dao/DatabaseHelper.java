package com.splitemapp.android.dao;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.splitemapp.android.domain.UserStatus;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the tag that is going to be used for the logging
	private static final String DATABASE_HELPER_TAG = "DatabaseHelper";
	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "splitemapp.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the SimpleData table
	private Dao<UserStatus, Integer> userStatusDao = null;
	private RuntimeExceptionDao<UserStatus, Integer> userStatusRuntimeDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DATABASE_HELPER_TAG, "onCreate");
			TableUtils.createTable(connectionSource, UserStatus.class);
		} catch (SQLException e) {
			Log.e(DATABASE_HELPER_TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DATABASE_HELPER_TAG, "onUpgrade");
			TableUtils.dropTable(connectionSource, UserStatus.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DATABASE_HELPER_TAG, "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Returns the Database Access Object (DAO) for our UserStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserStatus, Integer> getUserStatusDao() throws SQLException {
		if (userStatusDao == null) {
			userStatusDao = getDao(UserStatus.class);
		}
		return userStatusDao;
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our UserStatus class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<UserStatus, Integer> getUserStatusRuntimeExceptionDao() {
		if (userStatusRuntimeDao == null) {
			userStatusRuntimeDao = getRuntimeExceptionDao(UserStatus.class);
		}
		return userStatusRuntimeDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		userStatusDao = null;
		userStatusRuntimeDao = null;
	}
}