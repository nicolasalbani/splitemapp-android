package com.splitemapp.android.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.splitemapp.android.R;
import com.splitemapp.domainmodel.domain.ExpenseCategory;
import com.splitemapp.domainmodel.domain.Group;
import com.splitemapp.domainmodel.domain.GroupStatus;
import com.splitemapp.domainmodel.domain.InviteStatus;
import com.splitemapp.domainmodel.domain.Project;
import com.splitemapp.domainmodel.domain.ProjectStatus;
import com.splitemapp.domainmodel.domain.ProjectType;
import com.splitemapp.domainmodel.domain.User;
import com.splitemapp.domainmodel.domain.UserContactData;
import com.splitemapp.domainmodel.domain.UserExpense;
import com.splitemapp.domainmodel.domain.UserInvite;
import com.splitemapp.domainmodel.domain.UserStatus;
import com.splitemapp.domainmodel.domain.UserToGroup;
import com.splitemapp.domainmodel.domain.UserToGroupStatus;
import com.splitemapp.domainmodel.domain.UserToProject;
import com.splitemapp.domainmodel.domain.UserToProjectStatus;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// We need the context to access resources
	private Context context = null;
	// Name of the tag that is going to be used for the logging
	private static final String DATABASE_HELPER_TAG = "DatabaseHelper";
	// Name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "splitemapp.db";
	// Name of the sql file containing the initial data
	private static final String INITIAL_DATA_RESOURCE = "/initial_data.sql";
	// Any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// The list of DAO object we need to access different tables in the database
	private Dao<UserStatus, Integer> userStatusDao = null;
	private Dao<ProjectStatus, Integer> projectStatusDao = null;
	private Dao<ProjectType, Integer> projectTypeDao = null;
	private Dao<GroupStatus, Integer> groupStatusDao = null;
	private Dao<UserToGroupStatus, Integer> userToGroupStatusDao = null;
	private Dao<UserToProjectStatus, Integer> userToProjectStatusDao = null;
	private Dao<InviteStatus, Integer> inviteStatusDao = null;
	private Dao<ExpenseCategory, Integer> expenseCategoryDao = null;
	private Dao<Project, Integer> projectDao = null;
	private Dao<User, Integer> userDao = null;
	private Dao<Group, Integer> groupDao = null;
	private Dao<UserContactData, Integer> userContactDataDao = null;
	private Dao<UserExpense, Integer> userExpensesDao = null;
	private Dao<UserToGroup, Integer> userToGroupDao = null;
	private Dao<UserToProject, Integer> userToProjectDao = null;
	private Dao<UserInvite, Integer> userInviteDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DATABASE_HELPER_TAG, "onCreate");

			// We create all the required tables in the domain
			TableUtils.createTable(connectionSource, UserStatus.class);
			TableUtils.createTable(connectionSource, ProjectStatus.class);
			TableUtils.createTable(connectionSource, ProjectType.class);
			TableUtils.createTable(connectionSource, GroupStatus.class);
			TableUtils.createTable(connectionSource, UserToGroupStatus.class);
			TableUtils.createTable(connectionSource, UserToProjectStatus.class);
			TableUtils.createTable(connectionSource, InviteStatus.class);
			TableUtils.createTable(connectionSource, ExpenseCategory.class);
			TableUtils.createTable(connectionSource, Project.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, Group.class);
			TableUtils.createTable(connectionSource, UserContactData.class);
			TableUtils.createTable(connectionSource, UserExpense.class);
			TableUtils.createTable(connectionSource, UserToGroup.class);
			TableUtils.createTable(connectionSource, UserToProject.class);
			TableUtils.createTable(connectionSource, UserInvite.class);

			// We insert initial data
			insertInitialData(db);

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

			// We drop all the tables
			TableUtils.dropTable(connectionSource, UserStatus.class, true);
			TableUtils.dropTable(connectionSource, ProjectStatus.class, true);
			TableUtils.dropTable(connectionSource, ProjectType.class, true);
			TableUtils.dropTable(connectionSource, GroupStatus.class, true);
			TableUtils.dropTable(connectionSource, UserToGroupStatus.class, true);
			TableUtils.dropTable(connectionSource, UserToProjectStatus.class, true);
			TableUtils.dropTable(connectionSource, InviteStatus.class, true);
			TableUtils.dropTable(connectionSource, ExpenseCategory.class, true);
			TableUtils.dropTable(connectionSource, Project.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, Group.class, true);
			TableUtils.dropTable(connectionSource, UserContactData.class, true);
			TableUtils.dropTable(connectionSource, UserExpense.class, true);
			TableUtils.dropTable(connectionSource, UserToGroup.class, true);
			TableUtils.dropTable(connectionSource, UserToProject.class, true);
			TableUtils.dropTable(connectionSource, UserInvite.class, true);

			// We create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DATABASE_HELPER_TAG, "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	private void insertInitialData(SQLiteDatabase db){
		InputStream inputStream = context.getResources().openRawResource(R.raw.initial_data);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		// We execute the initial data queries one by one
		String sqlStatement = null;
		try {
			while((sqlStatement = bufferedReader.readLine()) != null){
				// We check whether this line is not empty nor a comment
				if(!sqlStatement.startsWith("--") && !sqlStatement.equals("")){
					Log.i(DATABASE_HELPER_TAG, "Executing: " +sqlStatement);
					db.execSQL(sqlStatement);
				}
			}

		} catch (IOException e) {
			Log.e(DATABASE_HELPER_TAG, "Can't read from initial data file: " + INITIAL_DATA_RESOURCE, e);
		}
	}


	/**
	 * Returns the Database Access Object (DAO) for the UserStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserStatus, Integer> getUserStatusDao() throws SQLException {
		if (userStatusDao == null) {
			userStatusDao = getDao(UserStatus.class);
		}
		return userStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ProjectStatus, Integer> getProjectStatusDao() throws SQLException {
		if (projectStatusDao == null) {
			projectStatusDao = getDao(ProjectStatus.class);
		}
		return projectStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectType class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ProjectType, Integer> getProjectTypeDao() throws SQLException {
		if (projectTypeDao == null) {
			projectTypeDao = getDao(ProjectType.class);
		}
		return projectTypeDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the GroupStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<GroupStatus, Integer> getGroupStatusDao() throws SQLException {
		if (groupStatusDao == null) {
			groupStatusDao = getDao(GroupStatus.class);
		}
		return groupStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToGroupStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToGroupStatus, Integer> getUserToGroupStatusDao() throws SQLException {
		if (userToGroupStatusDao == null) {
			userToGroupStatusDao = getDao(UserToGroupStatus.class);
		}
		return userToGroupStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToProjectStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToProjectStatus, Integer> getUserToProjectStatusDao() throws SQLException {
		if (userToProjectStatusDao == null) {
			userToProjectStatusDao = getDao(UserToProjectStatus.class);
		}
		return userToProjectStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the InviteStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<InviteStatus, Integer> getInviteStatusDao() throws SQLException {
		if (inviteStatusDao == null) {
			inviteStatusDao = getDao(InviteStatus.class);
		}
		return inviteStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ExpenseCategory class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ExpenseCategory, Integer> getExpenseCategoryDao() throws SQLException {
		if (expenseCategoryDao == null) {
			expenseCategoryDao = getDao(ExpenseCategory.class);
		}
		return expenseCategoryDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the Project class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Project, Integer> getProjectDao() throws SQLException {
		if (projectDao == null) {
			projectDao = getDao(Project.class);
		}
		return projectDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the User class. It will create it or just give the cached
	 * value.
	 */
	public Dao<User, Integer> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}
		return userDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the Group class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Group, Integer> getGroupDao() throws SQLException {
		if (groupDao == null) {
			groupDao = getDao(Group.class);
		}
		return groupDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserContactData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserContactData, Integer> getUserContactDataDao() throws SQLException {
		if (userContactDataDao == null) {
			userContactDataDao = getDao(UserContactData.class);
		}
		return userContactDataDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserExpenses class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserExpense, Integer> getUserExpensesDao() throws SQLException {
		if (userExpensesDao == null) {
			userExpensesDao = getDao(UserExpense.class);
		}
		return userExpensesDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToGroup class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToGroup, Integer> getUserToGroupDao() throws SQLException {
		if (userToGroupDao == null) {
			userToGroupDao = getDao(UserToGroup.class);
		}
		return userToGroupDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToProject class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToProject, Integer> getUserToProjectDao() throws SQLException {
		if (userToProjectDao == null) {
			userToProjectDao = getDao(UserToProject.class);
		}
		return userToProjectDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserInvite class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserInvite, Integer> getUserInviteDao() throws SQLException {
		if (userInviteDao == null) {
			userInviteDao = getDao(UserInvite.class);
		}
		return userInviteDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		userStatusDao = null;
		projectStatusDao = null;
		projectTypeDao = null;
		groupStatusDao = null;
		userToGroupStatusDao = null;
		userToProjectStatusDao = null;
		inviteStatusDao = null;
		expenseCategoryDao = null;
		projectDao = null;
		userDao = null;
		groupDao = null;
		userContactDataDao = null;
		userExpensesDao = null;
		userToGroupDao = null;
		userToProjectDao = null;
		userInviteDao = null;
	}
}