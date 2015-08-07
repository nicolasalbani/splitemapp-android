package com.splitemapp.android.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.splitemapp.android.R;
import com.splitemapp.android.utils.EconomicUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableFieldCod;
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
	 * Returns the Database Access Object (DAO) for the ProjectCoverImage class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ProjectCoverImage, Long> getProjectCoverImageDao() throws SQLException {
		if (projectCoverImageDao == null) {
			projectCoverImageDao = getDao(ProjectCoverImage.class);
		}
		return projectCoverImageDao;
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
	 * Returns the Database Access Object (DAO) for the UserAvatar class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UserAvatar, Long> getUserAvatarDao() throws SQLException {
		if (userAvatarDao == null) {
			userAvatarDao = getDao(UserAvatar.class);
		}
		return userAvatarDao;
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
			List<SyncStatus> queryResult = syncStatusDao.queryForEq(TableField.SYNC_STATUS_TABLE_NAME, Utils.getTableName(entity.getSimpleName()));

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
			List<SyncStatus> queryResult = syncStatusDao.queryForEq(TableField.SYNC_STATUS_TABLE_NAME, Utils.getTableName(entity.getSimpleName()));

			// We update the "last_push_at" field in the sync_status table
			SyncStatus syncStatus = queryResult.get(0);
			syncStatus.setLastPushAt(new Date());
			syncStatus.setLastPushSuccessAt(new Date());
			syncStatusDao.update(syncStatus);
		}
	}

	/**
	 * Creates or updates the user in the DB based on the username
	 * @return CreateOrUpdateStatus
	 * @throws SQLException 
	 */
	public CreateOrUpdateStatus createOrUpdateUser(User user) throws SQLException{
		List<User> userList = getUserDao().queryForEq(TableField.USER_USERNAME, user.getUsername());

		// Setting return values
		boolean created = false;
		boolean updated = false;
		int linesChanged = 0;

		// If it doesn't exists we create it, otherwise we update it
		if(userList.size() == 0){
			getUserDao().create(user);
			created = true;
		} else {
			// If this record already exists we make the id match and the update the record
			user.setId(userList.get(0).getId());
			linesChanged = getUserDao().update(user);
			updated = true;
		}

		return new CreateOrUpdateStatus(created, updated, linesChanged);
	}

	/**
	 * Creates or updates the user contact data in the DB based on the userId
	 * @param userContactData
	 * @return
	 * @throws SQLException
	 */
	public CreateOrUpdateStatus createOrUpdateUserContactData(UserContactData userContactData) throws SQLException{
		List<UserContactData> userContactDataList = getUserContactDataDao().queryForEq(TableField.USER_CONTACT_DATA_USER_ID, userContactData.getUser().getId());

		// Setting return values
		boolean created = false;
		boolean updated = false;
		int linesChanged = 0;

		// If it doesn't exists we create it, otherwise we update it
		if(userContactDataList.size() == 0){
			getUserContactDataDao().create(userContactData);
			created = true;
		} else {
			// If this record already exists we make the id match and the update the record
			userContactData.setId(userContactDataList.get(0).getId());
			linesChanged = getUserContactDataDao().update(userContactData);
			updated = true;
		}

		return new CreateOrUpdateStatus(created, updated, linesChanged);
	}
	
	/**
	 * Creates or updates the user avatar in the DB based on the userId
	 * @param userAvatar
	 * @return
	 * @throws SQLException
	 */
	public CreateOrUpdateStatus createOrUpdateUserAvatar(UserAvatar userAvatar) throws SQLException{
		List<UserAvatar> userAvatarList = getUserAvatarDao().queryForEq(TableField.USER_AVATAR_USER_ID, userAvatar.getUser().getId());

		// Setting return values
		boolean created = false;
		boolean updated = false;
		int linesChanged = 0;

		// If it doesn't exists we create it, otherwise we update it
		if(userAvatarList.size() == 0){
			getUserAvatarDao().create(userAvatar);
			created = true;
		} else {
			// If this record already exists we make the id match and the update the record
			userAvatar.setId(userAvatarList.get(0).getId());
			linesChanged = getUserAvatarDao().update(userAvatar);
			updated = true;
		}

		return new CreateOrUpdateStatus(created, updated, linesChanged);
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
	 * Returns all users in the database with their corresponding UserContactData
	 * @return
	 * @throws SQLException 
	 */
	public List<User> getAllUsers() throws SQLException{
		// Getting all users
		List<User> userList = getUserDao().queryForAll();

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
		ProjectCoverImage projectCoverImage = getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, projectId).get(0);
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
				activeUsersList.add(getUserById(userToProject.getUser().getId()));
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
		// Getting project
		Project project = getProjectDao().queryForId(projectId.longValue());
		
		// Getting project type
		ProjectType projectType = getProjectTypeDao().queryForId(project.getProjectType().getId());
		project.setProjectType(projectType);
		
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
		Dao<UserToProject, Long> userToProjectDao = getUserToProjectDao();

		List<UserToProject> userToProjectList = getAllUserToProjectsForLoggedUser();

		UserToProjectStatus archivedUserToProjectStatus = getUserToProjectStatusDao().queryForEq(TableField.USER_TO_PROJECT_COD, TableFieldCod.USER_TO_PROJECT_STATUS_ARCHIVED).get(0);
		for(UserToProject userToProject:userToProjectList){
			if(userToProject.getProject().getId().equals(projectId)){
				userToProject.setUserToProjectStatus(archivedUserToProjectStatus);
				userToProjectDao.update(userToProject);
			}
		}
	}

	/**
	 * Gets the list of Projects actively associated with the currently logged user
	 * @return
	 * @throws SQLException
	 */
	public List<Project> getActiveProjectsForLoggedUser() throws SQLException{
		ArrayList<Project> result = new ArrayList<Project>();

		List<UserToProject> userToProjectList = getAllUserToProjectsForLoggedUser();
		for(UserToProject userToProject:userToProjectList){
			UserToProjectStatus userToProjectStatus = getUserToProjectStatusDao().queryForId(userToProject.getUserToProjectStatus().getId());
			if(userToProjectStatus.getCod().equals(TableFieldCod.USER_TO_PROJECT_STATUS_ACTIVE)){
				result.add(getProjectDao().queryForId(userToProject.getProject().getId()));
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
			user = getUserById(getLoggedUserId());

			// Setting UserContactData information
			Set<UserContactData> userContactDatas = new HashSet<UserContactData>();
			userContactDatas.add(getLoggedUserContactData());
			user.setUserContactDatas(userContactDatas);
		}

		return user;
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
		BigDecimal expenseShare = EconomicUtils.calulateShare(userToProjectList.size());

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
			getUserToProjectDao().update(userToProject);
		}

		// Now we only have new users in the user list, we add the records for those
		for(User user:userList){
			UserToProject userToProject = new UserToProject();
			userToProject.setUserToProjectStatus(userToProjectActive);
			userToProject.setProject(project);
			userToProject.setUser(user);
			userToProject.setExpensesShare(expenseShare);
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
	 * Gets the sum of all UserExpense items for a particular project
	 * @param projectId
	 * @return
	 * @throws SQLException
	 */
	public List<UserExpense> getAllUserExpenseForProject(Long projectId) throws SQLException{
		List<UserExpense> userExpenseList = getUserExpenseDao().queryForAll();

		List<UserExpense> filteredUserExpenseList = new ArrayList<UserExpense>(); 
		for(UserExpense userExpense:userExpenseList){
			if(userExpense.getProject().getId() == projectId){
				filteredUserExpenseList.add(userExpense);
			}
		}

		return filteredUserExpenseList;
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

	/**
	 * Updates the specified ID reference with a new ID matching the one in the remote server
	 * @param dao
	 * @param idUpdate
	 * @param idReferenceList
	 * @throws SQLException
	 */
	public <F extends Serializable,E extends Number> void updateIdReferences(Dao<F,E> dao, IdUpdate<E> idUpdate, List<IdReference> idReferenceList) throws SQLException{
		for(IdReference idReference:idReferenceList){
			String statement = "UPDATE " +idReference.getTableName()+ " SET " +idReference.getFieldName()+ " = " +idUpdate.getNewId()+ " WHERE " +idReference.getFieldName()+ " = " +idUpdate.getOldId();
			Log.i(DATABASE_HELPER_TAG, "Executing ID update: " +statement);
			dao.updateRaw(statement);
		}
	}

}