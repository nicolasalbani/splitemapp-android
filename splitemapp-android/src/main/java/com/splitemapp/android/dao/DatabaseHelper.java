package com.splitemapp.android.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.splitemapp.android.R;
import com.splitemapp.android.utils.EconomicUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableFieldCod;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.InviteStatus;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.SyncStatus;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
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
	private Dao<UserToProjectStatus, Short> userToProjectStatusDao = null;
	private Dao<InviteStatus, Short> inviteStatusDao = null;
	private Dao<ExpenseCategory, Short> expenseCategoryDao = null;
	private Dao<Project, Long> projectDao = null;
	private Dao<ProjectCoverImage, Long> projectCoverImageDao = null;
	private Dao<User, Long> userDao = null;
	private Dao<UserAvatar, Long> userAvatarDao = null;
	private Dao<UserContactData, Long> userContactDataDao = null;
	private Dao<UserExpense, Long> userExpensesDao = null;
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
			TableUtils.createTable(connectionSource, UserToProjectStatus.class);
			TableUtils.createTable(connectionSource, InviteStatus.class);
			TableUtils.createTable(connectionSource, ExpenseCategory.class);
			TableUtils.createTable(connectionSource, Project.class);
			TableUtils.createTable(connectionSource, ProjectCoverImage.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, UserAvatar.class);
			TableUtils.createTable(connectionSource, UserContactData.class);
			TableUtils.createTable(connectionSource, UserExpense.class);
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
			TableUtils.dropTable(connectionSource, UserToProjectStatus.class, true);
			TableUtils.dropTable(connectionSource, InviteStatus.class, true);
			TableUtils.dropTable(connectionSource, ExpenseCategory.class, true);
			TableUtils.dropTable(connectionSource, Project.class, true);
			TableUtils.dropTable(connectionSource, ProjectCoverImage.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, UserContactData.class, true);
			TableUtils.dropTable(connectionSource, UserExpense.class, true);
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
	private Dao<UserStatus, Short> getUserStatusDao() throws SQLException {
		if (userStatusDao == null) {
			userStatusDao = getDao(UserStatus.class);
		}
		return userStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectStatus class. It will create it or just give the cached
	 * value.
	 */
	private Dao<ProjectStatus, Short> getProjectStatusDao() throws SQLException {
		if (projectStatusDao == null) {
			projectStatusDao = getDao(ProjectStatus.class);
		}
		return projectStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectType class. It will create it or just give the cached
	 * value.
	 */
	private Dao<ProjectType, Short> getProjectTypeDao() throws SQLException {
		if (projectTypeDao == null) {
			projectTypeDao = getDao(ProjectType.class);
		}
		return projectTypeDao;
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
	private Dao<InviteStatus, Short> getInviteStatusDao() throws SQLException {
		if (inviteStatusDao == null) {
			inviteStatusDao = getDao(InviteStatus.class);
		}
		return inviteStatusDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ExpenseCategory class. It will create it or just give the cached
	 * value.
	 */
	private Dao<ExpenseCategory, Short> getExpenseCategoryDao() throws SQLException {
		if (expenseCategoryDao == null) {
			expenseCategoryDao = getDao(ExpenseCategory.class);
		}
		return expenseCategoryDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the Project class. It will create it or just give the cached
	 * value.
	 */
	private Dao<Project, Long> getProjectDao() throws SQLException {
		if (projectDao == null) {
			projectDao = getDao(Project.class);
		}
		return projectDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the ProjectCoverImage class. It will create it or just give the cached
	 * value.
	 */
	private Dao<ProjectCoverImage, Long> getProjectCoverImageDao() throws SQLException {
		if (projectCoverImageDao == null) {
			projectCoverImageDao = getDao(ProjectCoverImage.class);
		}
		return projectCoverImageDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the User class. It will create it or just give the cached
	 * value.
	 */
	private Dao<User, Long> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}
		return userDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserAvatar class. It will create it or just give the cached
	 * value.
	 */
	private Dao<UserAvatar, Long> getUserAvatarDao() throws SQLException {
		if (userAvatarDao == null) {
			userAvatarDao = getDao(UserAvatar.class);
		}
		return userAvatarDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserContactData class. It will create it or just give the cached
	 * value.
	 */
	private Dao<UserContactData, Long> getUserContactDataDao() throws SQLException {
		if (userContactDataDao == null) {
			userContactDataDao = getDao(UserContactData.class);
		}
		return userContactDataDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserExpenses class. It will create it or just give the cached
	 * value.
	 */
	private Dao<UserExpense, Long> getUserExpenseDao() throws SQLException {
		if (userExpensesDao == null) {
			userExpensesDao = getDao(UserExpense.class);
		}
		return userExpensesDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserToProject class. It will create it or just give the cached
	 * value.
	 */
	private Dao<UserToProject, Long> getUserToProjectDao() throws SQLException {
		if (userToProjectDao == null) {
			userToProjectDao = getDao(UserToProject.class);
		}
		return userToProjectDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserInvite class. It will create it or just give the cached
	 * value.
	 */
	private Dao<UserInvite, Long> getUserInviteDao() throws SQLException {
		if (userInviteDao == null) {
			userInviteDao = getDao(UserInvite.class);
		}
		return userInviteDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the UserSession class. It will create it or just give the cached
	 * value.
	 */
	private Dao<UserSession, Long> getUserSessionDao() throws SQLException {
		if (userSessionDao == null) {
			userSessionDao = getDao(UserSession.class);
		}
		return userSessionDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for the SyncStatus class. It will create it or just give the cached
	 * value.
	 */
	private Dao<SyncStatus, Short> getSyncStatusDao() throws SQLException {
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
		userToProjectStatusDao = null;
		inviteStatusDao = null;
		expenseCategoryDao = null;
		projectDao = null;
		projectCoverImageDao = null;
		userDao = null;
		userAvatarDao = null;
		userContactDataDao = null;
		userExpensesDao = null;
		userToProjectDao = null;
		userInviteDao = null;
		userSessionDao = null;
		syncStatusDao = null;
	}




	/**********************************************************
	 * CONVENIENCE METHODS
	 *********************************************************/

	/**
	 * Gets the user status by id
	 * @param userStatusId
	 * @return
	 * @throws SQLException 
	 */
	public UserStatus getUserStatus(short userStatusId) throws SQLException{
		return getUserStatusDao().queryForId(userStatusId);
	}

	/**
	 * Gets the project status by id
	 * @param projectStatusId
	 * @return
	 * @throws SQLException 
	 */
	public ProjectStatus getProjectStatus(short projectStatusId) throws SQLException{
		return getProjectStatusDao().queryForId(projectStatusId);
	}

	/**
	 * Gets the project status by cod
	 * @param projectStatusCod
	 * @return
	 * @throws SQLException
	 */
	public ProjectStatus getProjectStatus(String projectStatusCod) throws SQLException{
		return getProjectStatusDao().queryForEq(TableField.ALTER_TABLE_COD, projectStatusCod).get(0);
	}

	/**
	 * Gets the project type by id
	 * @param projectTypeId
	 * @return
	 * @throws SQLException
	 */
	public ProjectType getProjectType(short projectTypeId) throws SQLException{
		return getProjectTypeDao().queryForId(projectTypeId);
	}

	/**
	 * Gets the project type by cod
	 * @param projectTypeCod
	 * @return
	 * @throws SQLException
	 */
	public ProjectType getProjectType(String projectTypeCod) throws SQLException{
		return getProjectTypeDao().queryForEq(TableField.PROJECT_TYPE_COD, projectTypeCod).get(0);
	}

	/**
	 * Gets the user to project status by id
	 * @param userToProjectStatusId
	 * @return
	 * @throws SQLException
	 */
	public UserToProjectStatus getUserToProjectStatus(short userToProjectStatusId) throws SQLException{
		return getUserToProjectStatusDao().queryForId(userToProjectStatusId);
	}

	/**
	 * Gets the invite status by id
	 * @param inviteStatusId
	 * @return
	 * @throws SQLException
	 */
	public InviteStatus getInviteStatus(short inviteStatusId) throws SQLException{
		return getInviteStatusDao().queryForId(inviteStatusId);
	}

	/**
	 * Gets the expense category by id
	 * @param expenseCategoryId
	 * @return
	 * @throws SQLException
	 */
	public ExpenseCategory getExpenseCategory(short expenseCategoryId) throws SQLException{
		return getExpenseCategoryDao().queryForId(expenseCategoryId);
	}

	/**
	 * Gets all the expense category items in the database
	 * @return
	 * @throws SQLException
	 */
	public List<ExpenseCategory> getExpenseCategoryList() throws SQLException{
		return getExpenseCategoryDao().queryForAll();
	}

	/**
	 * Gets all the UserToProject items in the database
	 * @return
	 * @throws SQLException
	 */
	public List<UserToProject> getUserToProjectList() throws SQLException{
		return getUserToProjectDao().queryForAll();
	}

	/**
	 * Gets all the project items in the database
	 * @return
	 * @throws SQLException
	 */
	public List<Project> getProjectList() throws SQLException{
		return getProjectDao().queryForAll();
	}

	/**
	 * Gets all the UserSession items in the database
	 * @return
	 * @throws SQLException
	 */
	public List<UserSession> getUserSessionList() throws SQLException{
		return getUserSessionDao().queryForAll();
	}

	/**
	 * Gets all the project cover image items in the database
	 * @return
	 * @throws SQLException
	 */
	public List<ProjectCoverImage> getProjectCoverImageList() throws SQLException{
		return getProjectCoverImageDao().queryForAll();
	}

	/**
	 * Persists the Project in the database
	 * @param project
	 * @throws SQLException
	 */
	public void persistProject(Project project) throws SQLException{
		project.setCreatedAt(new Date());
		project.setUpdatedAt(new Date());
		project.setUpdatedBy(getLoggedUser());
		getProjectDao().create(project);
	}

	/**
	 * Persists the user expense in the database
	 * @param userExpense
	 * @throws SQLException
	 */
	public void persistUserExpense(UserExpense userExpense) throws SQLException{
		userExpense.setCreatedAt(new Date());
		userExpense.setUpdatedAt(new Date());
		userExpense.setUpdatedBy(getLoggedUser());
		getUserExpenseDao().create(userExpense);
	}

	/**
	 * Persists the project cover image in the database
	 * @param projectCoverImage
	 * @throws SQLException
	 */
	public void persistProjectCoverImage(ProjectCoverImage projectCoverImage) throws SQLException{
		projectCoverImage.setCreatedAt(new Date());
		projectCoverImage.setUpdatedAt(new Date());
		projectCoverImage.setUpdatedBy(getLoggedUser());
		getProjectCoverImageDao().create(projectCoverImage);
	}

	/**
	 * Updates the Project in the database
	 * @param project
	 * @throws SQLException
	 */
	public void updateProject(Project project) throws SQLException{
		project.setUpdatedAt(new Date());
		project.setUpdatedBy(getLoggedUser());
		getProjectDao().update(project);
	}

	/**
	 * Updates the User in the database
	 * @param project
	 * @throws SQLException
	 */
	public void updateUser(User user) throws SQLException{
		user.setUpdatedAt(new Date());
		getUserDao().update(user);
	}

	/**
	 * Updates the UserAvatar in the database
	 * @param project
	 * @throws SQLException
	 */
	public void updateUserAvatar(UserAvatar userAvatar) throws SQLException{
		userAvatar.setUpdatedAt(new Date());
		getUserAvatarDao().update(userAvatar);
	}

	/**
	 * Updates the UserToProject in the database
	 * @param userToProject
	 * @throws SQLException
	 */
	public void updateUserToProject(UserToProject userToProject) throws SQLException{
		userToProject.setUpdatedAt(new Date());
		userToProject.setUpdatedBy(getLoggedUser());
		getUserToProjectDao().update(userToProject);
	}

	/**
	 * Updates the project in the database
	 * @param projectCoverImage
	 * @throws SQLException
	 */
	public void updateProjectCoverImage(ProjectCoverImage projectCoverImage) throws SQLException{
		projectCoverImage.setUpdatedAt(new Date());
		projectCoverImage.setUpdatedBy(getLoggedUser());
		getProjectCoverImageDao().update(projectCoverImage);
	}

	/**
	 * Updates the user expense in the database
	 * @param userExpense
	 * @throws SQLException
	 */
	public void updateUserExpense(UserExpense userExpense) throws SQLException{
		userExpense.setUpdatedAt(new Date());
		userExpense.setUpdatedBy(getLoggedUser());
		getUserExpenseDao().update(userExpense);
	}

	/**
	 * Gets the UserAvatar object by user id
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public UserAvatar getUserAvatarByUserId(Long userId) throws SQLException{
		UserAvatar userAvatar = null;
		List<UserAvatar> userAvatarList = getUserAvatarDao().queryForEq(TableField.USER_AVATAR_USER_ID, userId);
		if(!userAvatarList.isEmpty()){
			userAvatar = userAvatarList.get(0);
		}
		return userAvatar;
	}

	/**
	 * Gets the complete list of user avatars in the database
	 * @return
	 * @throws SQLException
	 */
	public List<UserAvatar> getUserAvatarList() throws SQLException{
		return getUserAvatarDao().queryForAll();
	}

	/**
	 * Gets the complete list of UserInvite in the database
	 * @return
	 * @throws SQLException
	 */
	public List<UserInvite> getUserInviteList() throws SQLException{
		return getUserInviteDao().queryForAll();
	}

	/**
	 * Gets the complete list of user expense in the database
	 * @return
	 * @throws SQLException
	 */
	public List<UserExpense> getUserExpenseList() throws SQLException{
		return getUserExpenseDao().queryForAll();
	}

	/**
	 * Gets the list of user expense associated to a project id
	 * @return
	 * @throws SQLException
	 */
	public List<UserExpense> getUserExpensesByProjectId(Long projectId) throws SQLException{
		return getUserExpensesByProjectId(projectId, null);
	}

	/**
	 * Gets the list of user expense associated to a project id for the month specified in Calendar 
	 * @param projectId
	 * @param calendar
	 * @return
	 * @throws SQLException
	 */
	public List<UserExpense> getUserExpensesByProjectId(Long projectId, Calendar calendar) throws SQLException{
		List<UserExpense> userExpenseList = new ArrayList<UserExpense>();

		List<UserExpense> fullList =  getUserExpenseDao().queryForEq(TableField.USER_EXPENSE_PROJECT_ID, projectId);

		if(calendar != null){
			userExpenseList = new ArrayList<UserExpense>();

			int calendarMonth = calendar.get(Calendar.MONTH);
			int calendarYear = calendar.get(Calendar.YEAR);

			for(UserExpense userExpense:fullList){
				Calendar expenseCal = Calendar.getInstance();
				expenseCal.setTime(userExpense.getExpenseDate());

				int expenseMonth = expenseCal.get(Calendar.MONTH);
				int expenseYear = expenseCal.get(Calendar.YEAR);

				if(expenseMonth == calendarMonth && expenseYear == calendarYear){
					userExpenseList.add(userExpense);
				}
			}

		} else {
			userExpenseList = fullList;
		}

		return userExpenseList;
	}

	/**
	 * Gets the total expense value associated to a project id
	 * @param projectId
	 * @return
	 * @throws SQLException
	 */
	public BigDecimal getTotalExpenseValueByProjectId(Long projectId, Calendar calendar) throws SQLException{
		BigDecimal totalExpenseValue = new BigDecimal(0);

		List<UserExpense> userExpenses = getUserExpensesByProjectId(projectId, calendar);

		for(UserExpense userExpense:userExpenses){
			totalExpenseValue = totalExpenseValue.add(userExpense.getExpense());
		}

		return totalExpenseValue;
	}

	/**
	 * Deletes all existing user sessions in the DB
	 * @throws SQLException
	 */
	public void deleteAllUserSessions() throws SQLException{
		for(UserSession userSession:getUserSessionDao().queryForAll()){
			getUserSessionDao().delete(userSession);
		}
	}

	/**
	 * Deletes all existing information in the database
	 * @throws SQLException
	 */
	public void clearDatabase() throws SQLException{
		// Removing all information for all tables
		TableUtils.clearTable(connectionSource, UserSession.class);
		TableUtils.clearTable(connectionSource, UserInvite.class);
		TableUtils.clearTable(connectionSource, UserAvatar.class);
		TableUtils.clearTable(connectionSource, UserContactData.class);
		TableUtils.clearTable(connectionSource, UserExpense.class);
		TableUtils.clearTable(connectionSource, UserToProject.class);
		TableUtils.clearTable(connectionSource, User.class);
		TableUtils.clearTable(connectionSource, ProjectCoverImage.class);
		TableUtils.clearTable(connectionSource, Project.class);
	}

	/**
	 * Convenience method to update the Sync PULL at timestamp
	 * @param entity
	 * @param boolean
	 * @throws SQLException
	 */
	public <T extends java.io.Serializable> void updateSyncStatusPullAt(Class<T> entity, boolean success, Date pulledAt) throws SQLException{
		// We get the proper record from the sync_status table
		Dao<SyncStatus,Short> syncStatusDao = getSyncStatusDao();
		List<SyncStatus> queryResult = syncStatusDao.queryForEq(TableField.SYNC_STATUS_TABLE_NAME, Utils.getTableName(entity.getSimpleName()));

		// We update the "last_pull_at" field in the sync_status table
		SyncStatus syncStatus = queryResult.get(0);
		syncStatus.setLastPullAt(pulledAt);
		if(success){
			syncStatus.setLastPullSuccessAt(pulledAt);
		}
		syncStatusDao.update(syncStatus);
	}

	/**
	 * Convenience method to update the Sync PUSH at timestamp
	 * @param entity
	 * @param createOrUpdate
	 * @throws SQLException
	 */
	public <T extends java.io.Serializable> void updateSyncStatusPushAt(Class<T> entity, boolean success, Date pushedAt) throws SQLException{
		// We get the proper record from the sync_status table
		Dao<SyncStatus,Short> syncStatusDao = getSyncStatusDao();
		List<SyncStatus> queryResult = syncStatusDao.queryForEq(TableField.SYNC_STATUS_TABLE_NAME, Utils.getTableName(entity.getSimpleName()));

		// We update the "last_push_at" field in the sync_status table
		SyncStatus syncStatus = queryResult.get(0);
		syncStatus.setLastPushAt(pushedAt);
		if(success){
			syncStatus.setLastPushSuccessAt(pushedAt);
		}
		syncStatusDao.update(syncStatus);
	}

	/**
	 * Creates or updates a project
	 * @param project
	 * @throws SQLException
	 */
	public void createOrUpdateProject(Project project) throws SQLException{
		Dao<Project, Long> dao = getProjectDao();
		if(dao.idExists(project.getId())){
			dao.update(project);
		} else {
			dao.create(project);
		}
	}

	/**
	 * Returns the UserToProject object for the specified projedt and user ids
	 * @param projectId
	 * @param userId
	 * @return
	 * @throws SQLException 
	 */
	public UserToProject getUserToProject(Long projectId, Long userId) throws SQLException{
		// Creating query
		QueryBuilder<UserToProject, Long> qb = getUserToProjectDao().queryBuilder();

		// Setting where conditions
		Where<UserToProject, Long> where = qb.where();
		where.eq(TableField.USER_TO_PROJECT_PROJECT_ID, projectId);
		where.and();
		where.eq(TableField.USER_TO_PROJECT_USER_ID, userId);
		qb.setWhere(where);

		// Executing query
		return qb.queryForFirst();
	}

	/**
	 * Creates or updates a UserInvite
	 * @param userInvite
	 * @throws SQLException
	 */
	public void createOrUpdateUserInvite(UserInvite userInvite) throws SQLException{
		Dao<UserInvite, Long> dao = getUserInviteDao();
		if(dao.idExists(userInvite.getId())){
			dao.update(userInvite);
		} else {
			dao.create(userInvite);
		}
	}

	/**
	 * Creates or updates a UserSession
	 * @param userInvite
	 * @throws SQLException
	 */
	public void createOrUpdateUserSession(UserSession userSession) throws SQLException{
		getUserSessionDao().createOrUpdate(userSession);
	}

	/**
	 * Creates or updates a user to project
	 * @param userToProject
	 * @throws SQLException
	 */
	public void createOrUpdateUserToProject(UserToProject userToProject) throws SQLException{
		Dao<UserToProject, Long> dao = getUserToProjectDao();
		if(dao.idExists(userToProject.getId())){
			dao.update(userToProject);
		} else {
			dao.create(userToProject);
		}
	}

	/**
	 * Creates or updates a user expense
	 * @param userExpense
	 * @throws SQLException
	 */
	public void createOrUpdateUserExpense(UserExpense userExpense) throws SQLException{
		Dao<UserExpense, Long> dao = getUserExpenseDao();
		if(dao.idExists(userExpense.getId())){
			dao.update(userExpense);
		} else {
			dao.create(userExpense);
		}
	}

	/**
	 * Creates or updates a project cover image
	 * @param projectCoverImage
	 * @throws SQLException
	 */
	public void createOrUpdateProjectCoverImage(ProjectCoverImage projectCoverImage) throws SQLException{
		Dao<ProjectCoverImage, Long> dao = getProjectCoverImageDao();
		if(dao.idExists(projectCoverImage.getId())){
			dao.update(projectCoverImage);
		} else {
			dao.create(projectCoverImage);
		}
	}

	/**
	 * Creates or updates a user
	 * @param user
	 * @throws SQLException 
	 */
	public void createOrUpdateUser(User user) throws SQLException{
		Dao<User, Long> dao = getUserDao();
		if(dao.idExists(user.getId())){
			dao.update(user);
		} else {
			dao.create(user);
		}
	}

	/**
	 * Creates or updates the user contact data in the DB based on the userId
	 * @param userContactData
	 * @throws SQLException
	 */
	public void createOrUpdateUserContactData(UserContactData userContactData) throws SQLException{
		Dao<UserContactData, Long> dao = getUserContactDataDao();
		if(dao.idExists(userContactData.getId())){
			dao.update(userContactData);
		} else {
			dao.create(userContactData);
		}
	}

	/**
	 * Creates or updates the user avatar in the DB based on the userId
	 * @param userAvatar
	 * @throws SQLException
	 */
	public void createOrUpdateUserAvatar(UserAvatar userAvatar) throws SQLException{
		Dao<UserAvatar, Long> dao = getUserAvatarDao();
		if(dao.idExists(userAvatar.getId())){
			dao.update(userAvatar);
		} else {
			dao.create(userAvatar);
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
	 * Gets the UserContactData object by contact data
	 * @param contactData
	 * @return
	 * @throws SQLException
	 */
	public UserContactData getUserContactData(String contactData) throws SQLException{
		UserContactData userContactData = null;
		List<UserContactData> userContactDataList = getUserContactDataDao().queryForEq(TableField.USER_CONTACT_DATA_CONTACT_DATA, contactData);
		if(!userContactDataList.isEmpty()){
			userContactData = userContactDataList.get(0);
		}
		return userContactData;
	}

	/**
	 * Returns all users in the database with their corresponding UserContactData
	 * @return
	 * @throws SQLException 
	 */
	public List<User> getAllUsers() throws SQLException{
		// Getting all users
		List<User> userList = getUserList();

		// Setting contact data for all users
		for(User user:userList){
			Set<UserContactData> userContactDatas = new HashSet<UserContactData>(); 
			userContactDatas.add(getUserContactData(user.getId()));
			user.setUserContactDatas(userContactDatas);
		}

		return userList;
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
	 * Returns the project cover image for the specified project ID
	 * @param projectCoverImageId
	 * @return
	 * @throws SQLException
	 */
	public ProjectCoverImage getProjectCoverImageByProjectId(Long projectId) throws SQLException{
		ProjectCoverImage projectCoverImage = null;
		List<ProjectCoverImage> projectCoverImageList = getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, projectId);
		if(!projectCoverImageList.isEmpty()){
			projectCoverImage = projectCoverImageList.get(0);
		}
		return projectCoverImage;
	}

	/**
	 * Returns a list of all available project types
	 * @return
	 * @throws SQLException 
	 */
	public List<ProjectType> getAllProjectTypes() throws SQLException{
		return getProjectTypeDao().queryForAll();
	}

	/**
	 * Initializes the SyncStatus table fields
	 * @throws SQLException
	 */
	public void initializeSyncStatus() throws SQLException{
		// We initialize all entries in the table
		List<SyncStatus> syncStatusList = getSyncStatusDao().queryForAll();
		for(SyncStatus syncStatus:syncStatusList){
			// Setting pull entries to the oldest time
			syncStatus.setLastPullAt(new Date(0));
			syncStatus.setLastPullSuccessAt(new Date(0));

			// Setting push entries to the current time
			syncStatus.setLastPushAt(new Date());
			syncStatus.setLastPushSuccessAt(new Date());

			// Persisting changes
			getSyncStatusDao().update(syncStatus);
		}
	}

	/**
	 * Returns the list of actively associated users to a particular project
	 * @param projectId
	 * @return
	 * @throws SQLException
	 */
	public List<User> getActiveUsersByProjectId(Long projectId) throws SQLException{
		List<UserToProject> allUserToProjectsForProjectId = getAllUserToProjectsForProjectId(projectId);

		List<User> activeUsersList = new ArrayList<User>();
		for(UserToProject userToProject:allUserToProjectsForProjectId){
			UserToProjectStatus userToProjectStatus = getUserToProjectStatusDao().queryForId(userToProject.getUserToProjectStatus().getId());
			if(userToProjectStatus.getCod().equals(TableFieldCod.USER_TO_PROJECT_STATUS_ACTIVE)){
				activeUsersList.add(getUser(userToProject.getUser().getId()));
			}
		}

		return activeUsersList;
	}


	/**
	 * Gets the list of UserToProjects associated with the currently logged user
	 * @return
	 * @throws SQLException
	 */
	public List<UserToProject> getAllUserToProjectsForProjectId(Long projectId) throws SQLException{
		List<UserToProject> userToProjectList = getUserToProjectDao().queryForEq(TableField.USER_TO_PROJECT_PROJECT_ID, projectId);
		return userToProjectList;
	}

	/**
	 * Gets the User object for the userId
	 * @param userId Long containing the user id in the DB
	 * @return User instance
	 * @throws SQLException 
	 */
	public User getUser(Long userId) throws SQLException{
		return getUserDao().queryForId(userId.longValue());
	}

	/**
	 * Gets the complete list of users in the database
	 * @return
	 * @throws SQLException
	 */
	public List<User> getUserList() throws SQLException{
		return getUserDao().queryForAll();
	}

	/**
	 * Gets the complete list of user contact data in the database
	 * @return
	 * @throws SQLException
	 */
	public List<UserContactData> getUserContactDataList() throws SQLException{
		return getUserContactDataDao().queryForAll();
	}

	/**
	 * Gets the User object for the userName
	 * @param userName String containing the user name in the DB
	 * @return User instance
	 * @throws SQLException 
	 */
	public User getUser(String userName) throws SQLException{
		User user = null;
		List<User> userList = getUserDao().queryForEq(TableField.USER_USERNAME, userName);
		if(!userList.isEmpty()){
			user = userList.get(0);
		}
		return user;
	}

	/**
	 * Gets the project cover image by project id
	 * @param projectId
	 * @return
	 * @throws SQLException
	 */
	public ProjectCoverImage getProjectCoverImageByProject(Long projectId) throws SQLException{
		return getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, projectId).get(0);
	}

	/**
	 * Gets the Project object for the projectId
	 * @param projectId Long containing the project id in the DB
	 * @return Project instance
	 * @throws SQLException 
	 */
	public Project getProject(Long projectId) throws SQLException{
		// Getting project
		Project project = getProjectDao().queryForId(projectId.longValue());

		// Getting project type
		ProjectType projectType = getProjectTypeDao().queryForId(project.getProjectType().getId());
		project.setProjectType(projectType);
		
		// Getting updatedBy and pushedBy
		project.setUpdatedBy(getUserDao().queryForId(project.getUpdatedBy().getId()));
		if(project.getPushedBy() != null){
			project.setPushedBy(getUserDao().queryForId(project.getPushedBy().getId()));
		}

		// Getting project avatar
		Set<ProjectCoverImage> projectCoverImageSet = new HashSet<ProjectCoverImage>();
		projectCoverImageSet.add(getProjectCoverImageByProjectId(projectId));
		project.setProjectCoverImages(projectCoverImageSet);

		return project;
	}

	/**
	 * Gets the list of UserToProjects associated with the currently logged user
	 * @return
	 * @throws SQLException
	 */
	public List<UserToProject> getAllUserToProjectsForLoggedUser() throws SQLException{
		User loggedUser = getLoggedUser();
		List<UserToProject> userToProjectList = getUserToProjectDao().queryForEq(TableField.USER_TO_PROJECT_USER_ID, loggedUser.getId());
		return userToProjectList;
	}

	/**
	 * Gets the list of Projects associated with the currently logged user
	 * @return
	 * @throws SQLException
	 */
	public List<Project> getAllProjectsForLoggedUser() throws SQLException{
		ArrayList<Project> result = new ArrayList<Project>();

		List<UserToProject> userToProjectList = getAllUserToProjectsForLoggedUser();
		for(UserToProject userToProject:userToProjectList){
			result.add(getProjectDao().queryForId(userToProject.getProject().getId()));
		}

		return result;
	}

	/**
	 * Archive the user to project relationship for the provided project ID and the current user
	 * @param projectId
	 * @throws SQLException
	 */
	public void archiveCurrentUserToProject(Long projectId) throws SQLException{
		List<UserToProject> userToProjectList = getAllUserToProjectsForLoggedUser();

		UserToProjectStatus archivedUserToProjectStatus = getUserToProjectStatusDao().queryForEq(TableField.USER_TO_PROJECT_COD, TableFieldCod.USER_TO_PROJECT_STATUS_ARCHIVED).get(0);
		for(UserToProject userToProject:userToProjectList){
			if(userToProject.getProject().getId().equals(projectId)){
				userToProject.setUserToProjectStatus(archivedUserToProjectStatus);
				updateUserToProject(userToProject);
			}
		}
	}

	/**
	 * Gets the list of Projects actively associated with the currently logged user
	 * @return
	 * @throws SQLException
	 */
	public List<Project> getProjectsForLoggedUser(boolean showActiveProjects, 
			boolean showArchivedProjects, 
			boolean showMonthlyProjects, 
			boolean showOneTimeProjects) throws SQLException{

		ArrayList<Project> result = new ArrayList<Project>();

		List<UserToProject> userToProjectList = getAllUserToProjectsForLoggedUser();
		for(UserToProject userToProject:userToProjectList){
			UserToProjectStatus userToProjectStatus = getUserToProjectStatusDao().queryForId(userToProject.getUserToProjectStatus().getId());
			boolean isActive = userToProjectStatus.getCod().equals(TableFieldCod.USER_TO_PROJECT_STATUS_ACTIVE);
			boolean isArchived = userToProjectStatus.getCod().equals(TableFieldCod.USER_TO_PROJECT_STATUS_ARCHIVED);

			if((showActiveProjects && isActive) || (showArchivedProjects && isArchived)){
				Project project = getProjectDao().queryForId(userToProject.getProject().getId());
				ProjectType projectType = getProjectTypeDao().queryForId(project.getProjectType().getId());
				boolean isMonthly = projectType.getCod().equals(TableFieldCod.PROJECT_TYPE_MONTHLY);
				boolean isOneTime = projectType.getCod().equals(TableFieldCod.PROJECT_TYPE_ONE_TIME);

				if((showMonthlyProjects && isMonthly) || (showOneTimeProjects && isOneTime)){
					result.add(project);
				}
			}
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
			// Setting User information
			user = getUser(getLoggedUserId());

			// Setting UserContactData information
			Set<UserContactData> userContactDatas = new HashSet<UserContactData>();
			userContactDatas.add(getLoggedUserContactData());
			user.setUserContactDatas(userContactDatas);
		}

		return user;
	}

	/**
	 * Indicates whether this particular userExpense was pushed to the server
	 * @param userExpense
	 * @return
	 * @throws SQLException 
	 */
	public boolean isExpensePushed(UserExpense userExpense) throws SQLException{
		Date lastSuccessPushAt = getLastSuccessPushAt(TableName.USER_EXPENSE);
		return lastSuccessPushAt.after(userExpense.getUpdatedAt());
	}

	/**
	 * Updates the user list associated with the provided project object
	 * @param projectId
	 * @param userList
	 * @throws SQLException 
	 */
	public void updateProjectContacts(Project project, List<User> userList) throws SQLException{
		// Getting user to project statuses
		UserToProjectStatus userToProjectActive = getUserToProjectStatusDao().queryForEq(TableField.ALTER_TABLE_COD, TableFieldCod.USER_TO_PROJECT_STATUS_ACTIVE).get(0);
		UserToProjectStatus userToProjectLeft = getUserToProjectStatusDao().queryForEq(TableField.ALTER_TABLE_COD, TableFieldCod.USER_TO_PROJECT_STATUS_LEFT).get(0);

		// Getting pre-existing users for this project
		List<UserToProject> userToProjectList = getUserToProjectDao().queryForEq(TableField.USER_TO_PROJECT_PROJECT_ID, project.getId());

		// Calculating expense share by default (all members in the group have same %)
		Float expenseShare = EconomicUtils.calulateShare(userToProjectList.size());

		// For each of the existing users in the list, we update their status
		for(UserToProject userToProject:userToProjectList){
			// By default, we set the status to "left"
			userToProject.setUserToProjectStatus(userToProjectLeft);

			// If it's in the users list, we update status to active and remove it from the users list
			for(int i=0; i<userList.size();i++){
				Long userId = userList.get(i).getId();
				if (userToProject.getUser().getId().equals(userId)){
					userToProject.setUserToProjectStatus(userToProjectActive);
					userToProject.setExpensesShare(expenseShare);
					userList.remove(i);
					break;
				}
			}

			// Updating userToProject
			updateUserToProject(userToProject);
		}

		// Now we only have new users in the user list, we add the records for those
		for(User user:userList){
			UserToProject userToProject = new UserToProject();
			userToProject.setUserToProjectStatus(userToProjectActive);
			userToProject.setProject(project);
			userToProject.setUser(user);
			userToProject.setExpensesShare(expenseShare);
			userToProject.setUpdatedBy(getLoggedUser());
			getUserToProjectDao().create(userToProject);
		}
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
		Long id = null;

		UserSession currentUserSession = getCurrentUserSession();
		if(currentUserSession != null){
			id = currentUserSession.getUser().getId();
		}

		return id;
	}

	/**
	 * Gets the active session token
	 * @return String containing the active session token
	 * @throws SQLException
	 */
	public String getSessionToken() throws SQLException{
		String sessionToken = null;

		UserSession currentUserSession = getCurrentUserSession();
		if(currentUserSession != null){
			sessionToken = currentUserSession.getToken();
		}

		return sessionToken;
	}

	/**
	 * Gets the active GCM token
	 * @return String containing the active GCM token
	 * @throws SQLException
	 */
	public String getGcmToken() throws SQLException{
		String gcmToken = null;

		UserSession currentUserSession = getCurrentUserSession();
		if(currentUserSession != null){
			gcmToken = currentUserSession.getGcmToken();
		}

		return gcmToken;
	}

	/**
	 * Sets the active GCM token
	 * @throws SQLException
	 */
	public void setGcmToken(String gcmToken) throws SQLException{
		UserSession currentUserSession = getCurrentUserSession();

		// Updating the GCM token in the current user session
		currentUserSession.setGcmToken(gcmToken);

		getUserSessionDao().update(currentUserSession);
	}

	/**
	 * Gets the UserSession instance for the current user
	 * @return
	 * @throws SQLException
	 */
	public UserSession getCurrentUserSession() throws SQLException{
		UserSession userSession = null;

		List<UserSession> userSessionList = getUserSessionList();
		if(userSessionList.size() > 0){
			userSession = userSessionList.get(userSessionList.size()-1);
		}

		return userSession;
	}

	/**
	 * Gets the sum of all UserExpense items for a particular project
	 * @param projectId
	 * @return
	 * @throws SQLException
	 */
	public List<UserExpense> getAllUserExpenseForProject(Long projectId) throws SQLException{
		List<UserExpense> userExpenseList = getUserExpenseList();

		List<UserExpense> filteredUserExpenseList = new ArrayList<UserExpense>(); 
		for(UserExpense userExpense:userExpenseList){
			if(userExpense.getProject().getId() == projectId){
				filteredUserExpenseList.add(userExpense);
			}
		}

		return filteredUserExpenseList;
	}

	/**
	 * Updates the pushedAt field for the provided entity in the database
	 * @param entity
	 * @throws SQLException 
	 */
	public <E> void updatePushedAt(E entity, Date pushedAt) throws SQLException{
		// This is an anti-pattern but required since otherwise we need to change the domain to inherit pushedAt property
		if(entity instanceof Project){
			Project project = (Project)entity;
			project.setPushedAt(pushedAt);
			project.setPushedBy(getLoggedUser());
			getProjectDao().update(project);
		} else if (entity instanceof ProjectCoverImage){
			ProjectCoverImage projectCoverImage = (ProjectCoverImage)entity;
			projectCoverImage.setPushedAt(pushedAt);
			projectCoverImage.setPushedBy(getLoggedUser());
			getProjectCoverImageDao().update(projectCoverImage);
		} else if (entity instanceof User){
			User user = (User)entity;
			user.setPushedAt(pushedAt);
			getUserDao().update(user);
		} else if (entity instanceof UserAvatar){
			UserAvatar userAvatar = (UserAvatar)entity;
			userAvatar.setPushedAt(pushedAt);
			getUserAvatarDao().update(userAvatar);
		} else if (entity instanceof UserContactData){
			UserContactData userContactData = (UserContactData)entity;
			userContactData.setPushedAt(pushedAt);
			getUserContactDataDao().update(userContactData);
		} else if (entity instanceof UserExpense){
			UserExpense userExpense = (UserExpense)entity;
			userExpense.setPushedAt(pushedAt);
			userExpense.setPushedBy(getLoggedUser());
			getUserExpenseDao().update(userExpense);
		} else if (entity instanceof UserInvite){
			UserInvite userInvite = (UserInvite)entity;
			userInvite.setPushedAt(pushedAt);
			userInvite.setPushedBy(getLoggedUser());
			getUserInviteDao().update(userInvite);
		} else if (entity instanceof UserToProject){
			UserToProject userToProject = (UserToProject)entity;
			userToProject.setPushedAt(pushedAt);
			userToProject.setPushedBy(getLoggedUser());
			getUserToProjectDao().update(userToProject);
		} else {
			throw new SQLException("Entity provided is not a valid database instance!");
		}
	}

	/**
	 * Returns the last date in which the table was successfully pulled 
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public Date getLastSuccessPullAt(String tableName) throws SQLException{
		return getSyncStatusDao().queryForEq(TableField.SYNC_STATUS_TABLE_NAME, tableName).get(0).getLastPullSuccessAt();
	}

	/**
	 * Returns the last date in which the table was successfully pushed 
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public Date getLastSuccessPushAt(String tableName) throws SQLException{
		return getSyncStatusDao().queryForEq(TableField.SYNC_STATUS_TABLE_NAME, tableName).get(0).getLastPushSuccessAt();
	}

	/**
	 * Updates the specified ID reference with a new ID matching the one in the remote server
	 * @param idUpdate
	 * @param idReferenceList
	 * @throws SQLException
	 */
	public void updateIdReferences(IdUpdate<Long> idUpdate, List<IdReference> idReferenceList) throws SQLException{
		updateIdReferences(getProjectDao(), idUpdate, idReferenceList);
	}

	/**
	 * Updates the specified ID reference with a new ID matching the one in the remote server
	 * @param dao
	 * @param idUpdate
	 * @param idReferenceList
	 * @throws SQLException
	 */
	private <F extends Serializable,E extends Number> void updateIdReferences(Dao<F,E> dao, IdUpdate<E> idUpdate, List<IdReference> idReferenceList) throws SQLException{
		for(IdReference idReference:idReferenceList){
			String statement = "UPDATE " +idReference.getTableName()+ " SET " +idReference.getFieldName()+ " = " +idUpdate.getNewId()+ " WHERE " +idReference.getFieldName()+ " = " +idUpdate.getOldId();
			Log.i(DATABASE_HELPER_TAG, "Executing ID update: " +statement);
			dao.updateRaw(statement);
		}
	}

}