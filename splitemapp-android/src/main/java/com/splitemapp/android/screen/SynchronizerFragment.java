package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Group;
import com.splitemapp.commons.domain.GroupStatus;
import com.splitemapp.commons.domain.InviteStatus;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.UserToGroup;
import com.splitemapp.commons.domain.UserToGroupStatus;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.dto.GroupDTO;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.UserExpenseDTO;
import com.splitemapp.commons.domain.dto.UserInviteDTO;
import com.splitemapp.commons.domain.dto.UserToGroupDTO;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.request.PullRequest;
import com.splitemapp.commons.domain.dto.request.PushRequest;
import com.splitemapp.commons.domain.dto.response.PullGroupResponse;
import com.splitemapp.commons.domain.dto.response.PullProjectResponse;
import com.splitemapp.commons.domain.dto.response.PullResponse;
import com.splitemapp.commons.domain.dto.response.PullUserContactDataResponse;
import com.splitemapp.commons.domain.dto.response.PullUserExpenseResponse;
import com.splitemapp.commons.domain.dto.response.PullUserInviteResponse;
import com.splitemapp.commons.domain.dto.response.PullUserResponse;
import com.splitemapp.commons.domain.dto.response.PullUserToGroupResponse;
import com.splitemapp.commons.domain.dto.response.PullUserToProjectResponse;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.dto.response.PushResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public abstract class SynchronizerFragment extends RestfulFragment{

	/**
	 * Creates an asynchronous user table pull request
	 */
	protected void pullUsers(){
		new PullUsersTask().execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table pull request
	 */
	protected void pullUserContactDatas(){
		new PullUserContactDatasTask().execute();
	}

	/**
	 * Creates an asynchronous project table pull request
	 */
	protected void pullProjects(){
		new PullProjectsTask().execute();
	}

	/**
	 * Creates an asynchronous user_to_project table pull request
	 */
	protected void pullUserToProjects(){
		new PullUserToProjectsTask().execute();
	}

	/**
	 * Creates an asynchronous group table pull request
	 */
	protected void pullGroups(){
		new PullGroupsTask().execute();
	}

	/**
	 * Creates an asynchronous user_to_group table pull request
	 */
	protected void pullUserToGroups(){
		new PullUserToGroupsTask().execute();
	}

	/**
	 * Creates an asynchronous user_invite table pull request
	 */
	protected void pullUserInvites(){
		new PullUserInvitesTask().execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pullUserExpenses(){
		new PullUserExpensesTask().execute();
	}
	
	/**
	 * Creates an asynchronous project table push request
	 */
	protected void pushProjects(){
		new PushProjectsTask().execute();
	}

	/**
	 * Sync Task to pull user table data from the remote DB
	 * @author nicolas
	 */
	private class PullUsersTask extends PullTask<UserDTO, PullUserResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USERS_PATH;
		}

		@Override
		protected void processResult(PullUserResponse response) throws SQLException {
			Set<UserDTO> userDTOs = response.getItemSet();
			for(UserDTO userDTO:userDTOs){
				// We obtain the required parameters for the object creation from the local database
				UserStatus userStatus = getHelper().getUserStatusDao().queryForId(userDTO.getUserStatusId().shortValue());

				// We create the new entity and store it into the local database
				User user = new User(userStatus, userDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getUserDao().createOrUpdate(user);
				getHelper().updateSyncStatusPullAt(User.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullUserResponse> getResponseType() {
			return PullUserResponse.class;
		}
	}

	/**
	 * Sync Task to pull user_contact_data table data from the remote DB
	 * @author nicolas
	 */
	private class PullUserContactDatasTask extends PullTask<UserContactDataDTO, PullUserContactDataResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_CONTACT_DATA;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USER_CONTACT_DATAS_PATH;
		}

		@Override
		protected void processResult(PullUserContactDataResponse response) throws SQLException {
			Set<UserContactDataDTO> userContactDataDTOs = response.getItemSet();
			for(UserContactDataDTO userContactDataDTO:userContactDataDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUserById(userContactDataDTO.getUserId().longValue());

				// We create the new entity and store it into the local database
				UserContactData userContactData = new UserContactData(user, userContactDataDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getUserContactDataDao().createOrUpdate(userContactData);
				getHelper().updateSyncStatusPullAt(UserContactData.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullUserContactDataResponse> getResponseType() {
			return PullUserContactDataResponse.class;
		}
	}

	/**
	 * Sync Task to pull project table data from the remote DB
	 * @author nicolas
	 */
	private class PullProjectsTask extends PullTask<ProjectDTO, PullProjectResponse> {
		@Override
		protected String getTableName(){
			return TableName.PROJECT;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_PROJECTS_PATH;
		}

		@Override
		protected void processResult(PullProjectResponse response) throws SQLException {
			Set<ProjectDTO> projectDTOs = response.getItemSet();
			for(ProjectDTO projectDTO:projectDTOs){
				// We obtain the required parameters for the object creation from the local database
				ProjectStatus projectStatus = getHelper().getProjectStatusDao().queryForId(projectDTO.getProjectStatusId().shortValue());
				ProjectType projectType = getHelper().getProjectTypeDao().queryForId(projectDTO.getProjectTypeId().shortValue());

				// We create the new entity and store it into the local database
				Project project = new Project(projectType, projectStatus, projectDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getProjectDao().createOrUpdate(project);
				getHelper().updateSyncStatusPullAt(Project.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullProjectResponse> getResponseType() {
			return PullProjectResponse.class;
		}
	}

	/**
	 * Sync Task to pull user_to_project table data from the remote DB
	 * @author nicolas
	 */
	private class PullUserToProjectsTask extends PullTask<UserToProjectDTO, PullUserToProjectResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_TO_PROJECT;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USER_TO_PROJECTS_PATH;
		}

		@Override
		protected void processResult(PullUserToProjectResponse response) throws SQLException {
			Set<UserToProjectDTO> userToProjectDTOs = response.getItemSet();
			for(UserToProjectDTO userToProjectDTO:userToProjectDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUserDao().queryForId(userToProjectDTO.getUserId().longValue());
				Project project = getHelper().getProjectDao().queryForId(userToProjectDTO.getProjectId().longValue());
				UserToProjectStatus userToProjectStatus = getHelper().getUserToProjectStatusDao().queryForId(userToProjectDTO.getUserToProjectStatusId().shortValue());

				// We create the new entity and store it into the local database
				UserToProject userToProject = new UserToProject(user, project, userToProjectStatus, userToProjectDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getUserToProjectDao().createOrUpdate(userToProject);
				getHelper().updateSyncStatusPullAt(UserToProject.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullUserToProjectResponse> getResponseType() {
			return PullUserToProjectResponse.class;
		}
	}

	/**
	 * Sync Task to pull group table data from the remote DB
	 * @author nicolas
	 */
	private class PullGroupsTask extends PullTask<GroupDTO, PullGroupResponse> {
		@Override
		protected String getTableName(){
			return TableName.GROUP;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_GROUPS_PATH;
		}

		@Override
		protected void processResult(PullGroupResponse response) throws SQLException {
			Set<GroupDTO> groupDTOs = response.getItemSet();
			for(GroupDTO groupDTO:groupDTOs){
				// We obtain the required parameters for the object creation from the local database
				GroupStatus groupStatus = getHelper().getGroupStatusDao().queryForId(groupDTO.getGroupStatusId().shortValue());

				// We create the new entity and store it into the local database
				Group group = new Group(groupStatus, groupDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getGroupDao().createOrUpdate(group);
				getHelper().updateSyncStatusPullAt(Group.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullGroupResponse> getResponseType() {
			return PullGroupResponse.class;
		}
	}


	/**
	 * Sync Task to pull user_to_group table data from the remote DB
	 * @author nicolas
	 */
	private class PullUserToGroupsTask extends PullTask<UserToGroupDTO, PullUserToGroupResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_TO_GROUP;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USER_TO_GROUPS_PATH;
		}

		@Override
		protected void processResult(PullUserToGroupResponse response) throws SQLException {
			Set<UserToGroupDTO> userToGroupDTOs = response.getItemSet();
			for(UserToGroupDTO userToGroupDTO:userToGroupDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUserDao().queryForId(userToGroupDTO.getUserId().longValue());
				Group group = getHelper().getGroupDao().queryForId(userToGroupDTO.getGroupId().longValue());
				UserToGroupStatus userToGroupStatus = getHelper().getUserToGroupStatusDao().queryForId(userToGroupDTO.getUserToGroupStatusId().shortValue());

				// We create the new entity and store it into the local database
				UserToGroup userToGroup = new UserToGroup(user, group, userToGroupStatus, userToGroupDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getUserToGroupDao().createOrUpdate(userToGroup);
				getHelper().updateSyncStatusPullAt(UserToGroup.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullUserToGroupResponse> getResponseType() {
			return PullUserToGroupResponse.class;
		}
	}


	/**
	 * Sync Task to pull user_invite table data from the remote DB
	 * @author nicolas
	 */
	private class PullUserInvitesTask extends PullTask<UserInviteDTO, PullUserInviteResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_INVITE;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USER_INVITES_PATH;
		}

		@Override
		protected void processResult(PullUserInviteResponse response) throws SQLException {
			Set<UserInviteDTO> userInviteDTOs = response.getItemSet();
			for(UserInviteDTO userInviteDTO:userInviteDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUserDao().queryForId(userInviteDTO.getUserId().longValue());
				Project project = getHelper().getProjectById(userInviteDTO.getProjectId().longValue());
				InviteStatus inviteStatus = getHelper().getInviteStatusDao().queryForId(userInviteDTO.getInviteStatusId().shortValue());

				// We create the new entity and store it into the local database
				UserInvite userInvite = new UserInvite(user, project, inviteStatus, userInviteDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getUserInviteDao().createOrUpdate(userInvite);
				getHelper().updateSyncStatusPullAt(UserInvite.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullUserInviteResponse> getResponseType() {
			return PullUserInviteResponse.class;
		}
	}


	/**
	 * Sync Task to pull user_expense table data from the remote DB
	 * @author nicolas
	 */
	private class PullUserExpensesTask extends PullTask<UserExpenseDTO, PullUserExpenseResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_EXPENSE;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USER_EXPENSES_PATH;
		}

		@Override
		protected void processResult(PullUserExpenseResponse response) throws SQLException {
			Set<UserExpenseDTO> userExpenseDTOs = response.getItemSet();
			for(UserExpenseDTO userExpenseDTO:userExpenseDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUserDao().queryForId(userExpenseDTO.getUserId().longValue());
				Project project = getHelper().getProjectById(userExpenseDTO.getProjectId().longValue());
				ExpenseCategory expenseCategory = getHelper().getExpenseCategoryDao().queryForId(userExpenseDTO.getExpenseCategoryId().shortValue());

				// We create the new entity and store it into the local database
				UserExpense userExpense = new UserExpense(user, project, expenseCategory, userExpenseDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getUserExpenseDao().createOrUpdate(userExpense);
				getHelper().updateSyncStatusPullAt(UserExpense.class, createOrUpdate);
			}
		}

		@Override
		protected Class<PullUserExpenseResponse> getResponseType() {
			return PullUserExpenseResponse.class;
		}
	}


	/**
	 * Base Pull task
	 * @author nicolas
	 *
	 * @param <E>
	 */
	private abstract class PullTask <E, R extends PullResponse<E>> extends AsyncTask<Void, Void, R> {

		/**
		 * Gets the name of the table to pull the data for
		 * @return
		 */
		protected abstract String getTableName();

		/**
		 * Gets the path for the service to call
		 * @return
		 */
		protected abstract String getServicePath();

		/**
		 * Gets the response type
		 * @return
		 */
		protected abstract Class<R> getResponseType();

		/**
		 * Processes the results coming from the service. This will typically contain DB inserts or updates
		 * @param response ServiceResponse that contains the list returned by the server
		 * @throws SQLException
		 */
		protected abstract void processResult(R response) throws SQLException;

		@Override
		protected R doInBackground(Void... params) {
			try {
				// We get the date in which this table was last successfully pulled
				Date lastPullSuccessAt = getHelper().getSyncStatusDao().queryForEq(TableField.SYNC_STATUS_TABLE_NAME, getTableName()).get(0).getLastPullSuccessAt();

				// We get the session token
				String sessionToken = getHelper().getSessionToken();

				// We create the pull request
				PullRequest pullRequest = new PullRequest();
				pullRequest.setLastPullSuccessAt(lastPullSuccessAt);
				pullRequest.setToken(sessionToken);

				// We call the rest service and send back the pull response
				return callRestService(getServicePath(), pullRequest, getResponseType());
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(R response) {
			boolean pullSuccess = false;

			// We validate the response
			if(response != null){
				pullSuccess = response.getSuccess();
			}

			// We show the status toast
			String pullMessage = "Pull " +getTableName();
			showToast(pullSuccess ? pullMessage+ " Successful!" : pullMessage+ " Failed!");

			// We save the user and session information returned by the backend
			if(pullSuccess){
				try {
					// We process the service response
					processResult(response);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing " +pullMessage+ " response", e);
				}

				// We refresh the fragment we called the sync service from
				refreshFragment();
			}
		}

	}
	
	
	/**
	 * Sync Task to push project table data to the remote DB
	 * @author nicolas
	 */
	private class PushProjectsTask extends PushTask<ProjectDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.PROJECT;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_PROJECTS_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<ProjectDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			//TODO make one raw query that will return only the items updated after lastPushSuccessAt and add that in the DatabaseHelper 
			// We get all the project in the database
			List<Project> projectList = getHelper().getProjectDao().queryForAll();
			
			// We add to the project DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<ProjectDTO> projectDTOList = new ArrayList<ProjectDTO>();
			for(Project project:projectList){
				projectDTOList.add(new ProjectDTO(project));
			}
			return projectDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();
			
			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.PROJECT, TableField.PROJECT_ID));
			idReferenceList.add(new IdReference(TableName.USER_TO_PROJECT, TableField.USER_TO_PROJECT_PROJECT_ID));
			idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_PROJECT_ID));
			
			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(getHelper().getProjectDao(), idUpdate, idReferenceList);
			}
		}
	}
	
	/**
	 * Base Push task
	 * @author nicolas
	 *
	 * @param <E>
	 */
	private abstract class PushTask <F, E extends Number, R extends PushResponse<E>> extends AsyncTask<Void, Void, R> {

		/**
		 * Gets the name of the table to pull the data for
		 * @return
		 */
		protected abstract String getTableName();

		/**
		 * Gets the path for the service to call
		 * @return
		 */
		protected abstract String getServicePath();
		
		/**
		 * Gets the response type
		 * @return
		 */
		protected abstract Class<R> getResponseType();
		
		/**
		 * Returns the request item list to be sent to the push service
		 * @param lastPushSuccessAt Date containing the last push success date
		 * @return List<F> containing the list of F objects to be sent to the push service
		 * @throws SQLException
		 */
		protected abstract List<F> getRequestItemList(Date lastPushSuccessAt) throws SQLException;

		/**
		 * Processes the results coming from the service. This will typically contain DB inserts or updates
		 * @param response ServiceResponse that contains the list returned by the server
		 * @throws SQLException
		 */
		protected abstract void processResult(R response) throws SQLException;

		@Override
		protected R doInBackground(Void... params) {
			try {
				// We get the date in which this table was last successfully pulled
				Date lastPushSuccessAt = getHelper().getSyncStatusDao().queryForEq(TableField.SYNC_STATUS_TABLE_NAME, getTableName()).get(0).getLastPushSuccessAt();
				
				// We get the session token
				String sessionToken = getHelper().getSessionToken();
				
				// We create the push request
				PushRequest<F> pushRequest = new PushRequest<F>();
				pushRequest.setToken(sessionToken);
				pushRequest.setLastPushSuccessAt(lastPushSuccessAt);
				pushRequest.setItemList(getRequestItemList(lastPushSuccessAt));

				// We call the rest service and send back the login response
				return callRestService(getServicePath(), pushRequest, getResponseType());
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(R response) {
			boolean pushSuccess = false;

			// We validate the response
			if(response != null){
				pushSuccess = response.getSuccess();
			}

			// We show the status toast
			String pushMessage = "Push " +getTableName();
			showToast(pushSuccess ? pushMessage+ " Successful!" : pushMessage+ " Failed!");

			// We save the user and session information returned by the backend
			if(pushSuccess){
				try {
					// We process the service response
					processResult(response);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing " +pushMessage+ " response", e);
				}

				// We refresh the fragment we called the sync service from
				refreshFragment();
			}
		}

	}
}
