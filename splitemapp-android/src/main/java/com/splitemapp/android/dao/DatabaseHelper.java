package com.splitemapp.android.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.splitemapp.android.R;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Group;
import com.splitemapp.commons.domain.GroupStatus;
import com.splitemapp.commons.domain.InviteStatus;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.SyncStatus;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.UserToGroup;
import com.splitemapp.commons.domain.UserToGroupStatus;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;
import com.splitemapp.commons.utils.Utils;

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
	private Dao<UserStatus, Short> userStatusDao = null;
	private Dao<ProjectStatus, Short> projectStatusDao = null;
	private Dao<ProjectType, Short> projectTypeDao = null;
	private Dao<GroupStatus, Short> groupStatusDao = null;
	private Dao<UserToGroupStatus, Short> userToGroupStatusDao = null;
	private Dao<UserToProjectStatus, Short> userToProjectStatusDao = null;
	private Dao<InviteStatus, Short> inviteStatusDao = null;
	private Dao<ExpenseCategory, Short> expenseCategoryDao = null;
	private Dao<Project, Long> projectDao = null;
	private Dao<User, Long> userDao = null;
	private Dao<Group, Long> groupDao = null;
	private Dao<UserContactData, Long> userContactDataDao = null;
	private Dao<UserExpense, Long> userExpensesDao = null;
	private Dao<UserToGroup, Long> userToGroupDao = null;
	private Dao<UserToProject, Long> userToProjectDao = null;
	private Dao<UserInvite, Long> userInviteDao = null;
	private Dao<UserSession, Long> userSessionDao = null;
	private Dao<SyncStatus, Short> syncStatusDao = null;

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
			TableUtils.createTable(connectionSource, UserSession.class);
			TableUtils.createTable(connectionSource, SyncStatus.class);

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
			TableUtils.dropTable(connectionSource, UserSession.class, true);
			TableUtils.dropTable(connectionSource, SyncStatus.class, true);

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
	public Dao<UserStatus, Short> getUserStatusDao() throws SQLException {
		if (userStatusDao == null) {
			userStatusDao = getDao(UserStatus.class);
		}
		return userStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ProjectStatus, Short> getProjectStatusDao() throws SQLException {
		if (projectStatusDao == null) {
			projectStatusDao = getDao(ProjectStatus.class);
		}
		return projectStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectType class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ProjectType, Short> getProjectTypeDao() throws SQLException {
		if (projectTypeDao == null) {
			projectTypeDao = getDao(ProjectType.class);
		}
		return projectTypeDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the GroupStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<GroupStatus, Short> getGroupStatusDao() throws SQLException {
		if (groupStatusDao == null) {
			groupStatusDao = getDao(GroupStatus.class);
		}
		return groupStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToGroupStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToGroupStatus, Short> getUserToGroupStatusDao() throws SQLException {
		if (userToGroupStatusDao == null) {
			userToGroupStatusDao = getDao(UserToGroupStatus.class);
		}
		return userToGroupStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToProjectStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToProjectStatus, Short> getUserToProjectStatusDao() throws SQLException {
		if (userToProjectStatusDao == null) {
			userToProjectStatusDao = getDao(UserToProjectStatus.class);
		}
		return userToProjectStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the InviteStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<InviteStatus, Short> getInviteStatusDao() throws SQLException {
		if (inviteStatusDao == null) {
			inviteStatusDao = getDao(InviteStatus.class);
		}
		return inviteStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ExpenseCategory class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ExpenseCategory, Short> getExpenseCategoryDao() throws SQLException {
		if (expenseCategoryDao == null) {
			expenseCategoryDao = getDao(ExpenseCategory.class);
		}
		return expenseCategoryDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the Project class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Project, Long> getProjectDao() throws SQLException {
		if (projectDao == null) {
			projectDao = getDao(Project.class);
		}
		return projectDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the User class. It will create it or just give the cached
	 * value.
	 */
	public Dao<User, Long> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}
		return userDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the Group class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Group, Long> getGroupDao() throws SQLException {
		if (groupDao == null) {
			groupDao = getDao(Group.class);
		}
		return groupDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserContactData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserContactData, Long> getUserContactDataDao() throws SQLException {
		if (userContactDataDao == null) {
			userContactDataDao = getDao(UserContactData.class);
		}
		return userContactDataDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserExpenses class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserExpense, Long> getUserExpenseDao() throws SQLException {
		if (userExpensesDao == null) {
			userExpensesDao = getDao(UserExpense.class);
		}
		return userExpensesDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToGroup class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToGroup, Long> getUserToGroupDao() throws SQLException {
		if (userToGroupDao == null) {
			userToGroupDao = getDao(UserToGroup.class);
		}
		return userToGroupDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToProject class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserToProject, Long> getUserToProjectDao() throws SQLException {
		if (userToProjectDao == null) {
			userToProjectDao = getDao(UserToProject.class);
		}
		return userToProjectDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserInvite class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserInvite, Long> getUserInviteDao() throws SQLException {
		if (userInviteDao == null) {
			userInviteDao = getDao(UserInvite.class);
		}
		return userInviteDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserSession class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserSession, Long> getUserSessionDao() throws SQLException {
		if (userSessionDao == null) {
			userSessionDao = getDao(UserSession.class);
		}
		return userSessionDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the SyncStatus class. It will create it or just give the cached
	 * value.
	 */
	public Dao<SyncStatus, Short> getSyncStatusDao() throws SQLException {
		if (syncStatusDao == null) {
			syncStatusDao = getDao(SyncStatus.class);
		}
		return syncStatusDao;
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
		userSessionDao = null;
		syncStatusDao = null;
	}




	/**********************************************************
	 * CONVENIENCE METHODS
	 *********************************************************/

	/**
	 * Convenience method to update the Sync PULL at timestamp
	 * @param entity
	 * @param createOrUpdate
	 * @throws SQLException
	 */
	public <T extends java.io.Serializable> void updateSyncStatusPullAt(Class<T> entity, CreateOrUpdateStatus createOrUpdate) throws SQLException{
		// If a record was created or updated we update the sync_status table
		if(createOrUpdate.isCreated() || createOrUpdate.isUpdated()){
			// We get the proper record from the sync_status table
			Dao<SyncStatus,Short> syncStatusDao = getSyncStatusDao();
			List<SyncStatus> queryResult = syncStatusDao.queryForEq("table_name", Utils.getTableName(entity.getSimpleName()));

			// We update the "last_pull_at" field in the sync_status table
			SyncStatus syncStatus = queryResult.get(0);
			syncStatus.setLastPullAt(new Date());
			syncStatus.setLastPullSuccessAt(new Date());
			syncStatusDao.update(syncStatus);
		}
	}

	/**
	 * Convenience method to update the Sync PUSH at timestamp
	 * @param entity
	 * @param createOrUpdate
	 * @throws SQLException
	 */
	public <T extends java.io.Serializable> void updateSyncStatusPushAt(Class<T> entity, CreateOrUpdateStatus createOrUpdate) throws SQLException{
		// If a record was created or updated we update the sync_status table
		if(createOrUpdate.isCreated() || createOrUpdate.isUpdated()){
			// We get the proper record from the sync_status table
			Dao<SyncStatus,Short> syncStatusDao = getSyncStatusDao();
			List<SyncStatus> queryResult = syncStatusDao.queryForEq("table_name", Utils.getTableName(entity.getSimpleName()));

			// We update the "last_push_at" field in the sync_status table
			SyncStatus syncStatus = queryResult.get(0);
			syncStatus.setLastPushAt(new Date());
			syncStatus.setLastPushSuccessAt(new Date());
			syncStatusDao.update(syncStatus);
		}
	}

	/**
	 * Gets the user contact data from a particular user id
	 * @param userId Long containing the user id in the DB 
	 * @return UserContactData instance
	 * @throws SQLException 
	 */
	public UserContactData getUserContactData(Long userId) throws SQLException{
		UserContactData userContactData = null;

		Dao<UserContactData, Long> userContactDataDao = getUserContactDataDao();
		for(UserContactData ucd:userContactDataDao){
			if(ucd.getUser().getId().equals(userId)){
				userContactData = ucd;
			}
		}

		return userContactData;
	}

	/**
	 * Gets the UserExpense instance with its ExpenseCategory instance already loaded
	 * @param userExpenseId Long containing the user expense id from the DB
	 * @return UserExpense instance
	 * @throws SQLException 
	 */
	public UserExpense getUserExpenseById(Long userExpenseId) throws SQLException{
		UserExpense userExpense = null;
		// We get the user expense
		Dao<UserExpense,Long> userExpensesDao = getUserExpenseDao();
		userExpense = userExpensesDao.queryForId(userExpenseId.longValue());

		// We get the expense category
		Dao<ExpenseCategory,Short> expenseCategoryDao = getExpenseCategoryDao();
		ExpenseCategory expenseCategory = expenseCategoryDao.queryForId(userExpense.getExpenseCategory().getId().shortValue());

		userExpense.setExpenseCategory(expenseCategory);
		return userExpense;
	}

	/**
	 * Gets the User object for the userId
	 * @param userId Long containing the user id in the DB
	 * @return User instance
	 * @throws SQLException 
	 */
	public User getUserById(Long userId) throws SQLException{
		return getUserDao().queryForId(userId.longValue());
	}

	/**
	 * Gets the Project object for the projectId
	 * @param projectId Long containing the project id in the DB
	 * @return Project instance
	 * @throws SQLException 
	 */
	public Project getProjectById(Long projectId) throws SQLException{
		return getProjectDao().queryForId(projectId.longValue());
	}
	
	/**
	 * Gets the list of Projects associated with the currently logged user
	 * @return
	 * @throws SQLException
	 */
	public List<Project> getAllProjectsForLoggedUser() throws SQLException{
		ArrayList<Project> result = new ArrayList<Project>();

		User loggedUser = getLoggedUser();
		List<UserToProject> userToProjectList = getUserToProjectDao().queryForEq(TableField.USER_TO_PROJECT_USER_ID, loggedUser.getId());
		for(UserToProject userToProject:userToProjectList){
			result.add(getProjectDao().queryForId(userToProject.getProject().getId()));
		}

		return result;
	}

	/**
	 * Gets the logged user, if any
	 * @return User instance if logged, null otherwise
	 * @throws SQLException 
	 */
	public User getLoggedUser() throws SQLException{
		User user = null;

		Long loggedUserId = getLoggedUserId();
		if(loggedUserId != null){
			user = getUserById(getLoggedUserId());
		}

		return user;
	}
	
	/**
	 * Gets the logged user contact data, if any
	 * @return User instance if logged, null otherwise
	 * @throws SQLException 
	 */
	public UserContactData getLoggedUserContactData() throws SQLException{
		UserContactData userContactData = null;

		Long loggedUserId = getLoggedUserId();
		if(loggedUserId != null){
			userContactData = getUserContactData(getLoggedUserId());
		}

		return userContactData;
	}

	/**
	 * Gets the logged user id, if any
	 * @return User instance if logged, null otherwise
	 * @throws SQLException 
	 */
	public Long getLoggedUserId() throws SQLException{
		Long userId = null;

		List<UserSession> userSessionList = getUserSessionDao().queryForAll();
		if(userSessionList.size() > 0){
			UserSession userSession = userSessionList.get(userSessionList.size()-1);
			userId = userSession.getUser().getId();
		}
		return userId;
	}

	/**
	 * Gets the active session token
	 * @return String containing the active session token
	 * @throws SQLException
	 */
	public String getSessionToken() throws SQLException{
		return getUserSessionDao().queryForAll().get(0).getToken();
	} 

	/**
	 * Deletes all existing user sessions in the DB
	 * @throws SQLException 
	 */
	public void deleteAllUserSessions() throws SQLException{
		Dao<UserSession, Long> userSessionDao = getUserSessionDao();
		List<UserSession> userSessionList = userSessionDao.queryForAll();
		for(UserSession us:userSessionList){
			userSessionDao.deleteById(us.getId().longValue());
		}
	}
	
	public <F extends Serializable,E extends Number> void updateIdReferences(Dao<F,E> dao, IdUpdate<E> idUpdate, List<IdReference> idReferenceList) throws SQLException{
		for(IdReference idReference:idReferenceList){
			String statement = "UPDATE " +idReference.getTableName()+ " SET " +idReference.getFieldName()+ " = " +idUpdate.getNewId()+ " WHERE " +idReference.getFieldName()+ " = " +idUpdate.getOldId();
			Log.i(DATABASE_HELPER_TAG, "Executing ID update: " +statement);
			dao.updateRaw(statement);
		}
	}
}