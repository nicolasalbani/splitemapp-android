package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.InviteStatus;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.dto.ProjectCoverImageDTO;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.UserExpenseDTO;
import com.splitemapp.commons.domain.dto.UserInviteDTO;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.request.PullRequest;
import com.splitemapp.commons.domain.dto.request.PushRequest;
import com.splitemapp.commons.domain.dto.response.PullProjectCoverImageResponse;
import com.splitemapp.commons.domain.dto.response.PullProjectResponse;
import com.splitemapp.commons.domain.dto.response.PullResponse;
import com.splitemapp.commons.domain.dto.response.PullUserAvatarResponse;
import com.splitemapp.commons.domain.dto.response.PullUserContactDataResponse;
import com.splitemapp.commons.domain.dto.response.PullUserExpenseResponse;
import com.splitemapp.commons.domain.dto.response.PullUserInviteResponse;
import com.splitemapp.commons.domain.dto.response.PullUserResponse;
import com.splitemapp.commons.domain.dto.response.PullUserToProjectResponse;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.dto.response.PushResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public abstract class SynchronizerFragment extends RestfulFragment{

	/**
	 * Creates a linked list of asynchronous pull requests
	 */
	protected void syncAllTables(){
		// Showing progress indicator
		showProgressIndicator();
		
		// Calling all push services
		final PushUserExpensesTask pushUserExpensesTask = new PushUserExpensesTask(){protected void executeOnSuccess() {hideProgressIndicator();}};
		final PushUserInvitesTask pushUserInvitesTask = new PushUserInvitesTask(){protected void executeOnSuccess() {pushUserExpensesTask.execute();}};
		final PushUserToProjectsTask pushUserToProjectsTask = new PushUserToProjectsTask(){protected void executeOnSuccess() {pushUserInvitesTask.execute();}};
		final PushProjectCoverImagesTask pushProjectCoverImagesTask = new PushProjectCoverImagesTask(){protected void executeOnSuccess() {pushUserToProjectsTask.execute();}};
		final PushProjectsTask pushProjectsTask = new PushProjectsTask(){protected void executeOnSuccess() {pushProjectCoverImagesTask.execute();}};
		final PushUserContactDatasTask pushUserContactDatasTask = new PushUserContactDatasTask(){protected void executeOnSuccess() {pushProjectsTask.execute();}};
		final PushUserAvatarsTask pushUserAvatarsTask = new PushUserAvatarsTask(){protected void executeOnSuccess() {pushUserContactDatasTask.execute();}};
		final PushUsersTask pushUsersTask = new PushUsersTask(){protected void executeOnSuccess() {pushUserAvatarsTask.execute();}};
		
		// Calling all pull services
		final PullUserExpensesTask pullUserExpensesTask = new PullUserExpensesTask(){protected void executeOnSuccess() {pushUsersTask.execute();}};
		final PullUserInvitesTask pullUserInvitesTask = new PullUserInvitesTask(){protected void executeOnSuccess() {pullUserExpensesTask.execute();}};
		final PullUserToProjectsTask pullUserToProjectsTask = new PullUserToProjectsTask(){protected void executeOnSuccess() {pullUserInvitesTask.execute();}};
		final PullProjectCoverImagesTask pullProjectCoverImagesTask = new PullProjectCoverImagesTask(){protected void executeOnSuccess() {pullUserToProjectsTask.execute();}};
		final PullProjectsTask pullProjectsTask = new PullProjectsTask(){protected void executeOnSuccess() {pullProjectCoverImagesTask.execute();}};
		final PullUserContactDatasTask pullUserContactDatasTask = new PullUserContactDatasTask(){protected void executeOnSuccess() {pullProjectsTask.execute();}};
		final PullUserAvatarsTask pullUserAvatarsTask = new PullUserAvatarsTask(){protected void executeOnSuccess() {pullUserContactDatasTask.execute();}};
		PullUsersTask pullUsersTask = new PullUsersTask(){protected void executeOnSuccess() {pullUserAvatarsTask.execute();}};
		pullUsersTask.execute();
	}
	
	/**
	 * Creates an asynchronous user table pull request
	 */
	protected void pullUsers(){
		PullUsersTask task = new PullUsersTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table pull request
	 */
	protected void pullUserContactDatas(){
		PullUserContactDatasTask task = new PullUserContactDatasTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_avatar table pull request
	 */
	protected void pullUserAvatars(){
		PullUserAvatarsTask task = new PullUserAvatarsTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous project table pull request
	 */
	protected void pullProjects(){
		PullProjectsTask task = new PullProjectsTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous project_cover_image table pull request
	 */
	protected void pullProjectCoverImages(){
		PullProjectCoverImagesTask task = new PullProjectCoverImagesTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_to_project table pull request
	 */
	protected void pullUserToProjects(){
		PullUserToProjectsTask task = new PullUserToProjectsTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_invite table pull request
	 */
	protected void pullUserInvites(){
		PullUserInvitesTask task = new PullUserInvitesTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pullUserExpenses(){
		PullUserExpensesTask task = new PullUserExpensesTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user table push request
	 */
	protected void pushUsers(){
		PushUsersTask task = new PushUsersTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table push request
	 */
	protected void pushUserContactDatas(){
		PushUserContactDatasTask task = new PushUserContactDatasTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_avatar table push request
	 */
	protected void pushUserAvatars(){
		PushUserAvatarsTask task = new PushUserAvatarsTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous project table push request
	 */
	protected void pushProjects(){
		PushProjectsTask task = new PushProjectsTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous project_cover_image table push request
	 */
	protected void pushProjectCoverImages(){
		PushProjectCoverImagesTask task = new PushProjectCoverImagesTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_to_project table push request
	 */
	protected void pushUserToProjects(){
		PushUserToProjectsTask task = new PushUserToProjectsTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_invite table push request
	 */
	protected void pushUserInvites(){
		PushUserInvitesTask task = new PushUserInvitesTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pushUserExpenses(){
		PushUserExpensesTask task = new PushUserExpensesTask(){protected void executeOnSuccess() {}};
		task.execute();
	}

	/**
	 * Sync Task to pull user table data from the remote DB
	 * @author nicolas
	 */
	private abstract class PullUsersTask extends PullTask<UserDTO, PullUserResponse> {
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
			// Updating sync status
			getHelper().updateSyncStatusPullAt(User.class, response.getSuccess());

			Set<UserDTO> userDTOs = response.getItemSet();
			for(UserDTO userDTO:userDTOs){
				// We obtain the required parameters for the object creation from the local database
				UserStatus userStatus = getHelper().getUserStatus(userDTO.getUserStatusId().shortValue());

				// We create the new entity and store it into the local database
				User user = new User(userStatus, userDTO);
				getHelper().createOrUpdateUser(user);
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
	private abstract class PullUserContactDatasTask extends PullTask<UserContactDataDTO, PullUserContactDataResponse> {
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
			// Updating sync status
			getHelper().updateSyncStatusPullAt(UserContactData.class, response.getSuccess());

			Set<UserContactDataDTO> userContactDataDTOs = response.getItemSet();
			for(UserContactDataDTO userContactDataDTO:userContactDataDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUser(userContactDataDTO.getUserId().longValue());

				// We create the new entity and store it into the local database
				UserContactData userContactData = new UserContactData(user, userContactDataDTO);
				getHelper().createOrUpdateUserContactData(userContactData);
			}
		}

		@Override
		protected Class<PullUserContactDataResponse> getResponseType() {
			return PullUserContactDataResponse.class;
		}
	}

	/**
	 * Sync Task to pull user_contact_data table data from the remote DB
	 * @author nicolas
	 */
	private abstract class PullUserAvatarsTask extends PullTask<UserAvatarDTO, PullUserAvatarResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_AVATAR;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_USER_AVATARS_PATH;
		}

		@Override
		protected void processResult(PullUserAvatarResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPullAt(UserAvatar.class, response.getSuccess());

			Set<UserAvatarDTO> userAvatarDTOs = response.getItemSet();
			for(UserAvatarDTO userAvatarDTO:userAvatarDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUser(userAvatarDTO.getUserId().longValue());

				// We create the new entity and store it into the local database
				UserAvatar userAvatar = new UserAvatar(user, userAvatarDTO);
				getHelper().createOrUpdateUserAvatar(userAvatar);
			}
		}

		@Override
		protected Class<PullUserAvatarResponse> getResponseType() {
			return PullUserAvatarResponse.class;
		}
	}

	/**
	 * Sync Task to pull project table data from the remote DB
	 * @author nicolas
	 */
	private abstract class PullProjectsTask extends PullTask<ProjectDTO, PullProjectResponse> {
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
			// Updating sync status
			getHelper().updateSyncStatusPullAt(Project.class, response.getSuccess());

			Set<ProjectDTO> projectDTOs = response.getItemSet();
			for(ProjectDTO projectDTO:projectDTOs){
				// We obtain the required parameters for the object creation from the local database
				ProjectStatus projectStatus = getHelper().getProjectStatus(projectDTO.getProjectStatusId().shortValue());
				ProjectType projectType = getHelper().getProjectType(projectDTO.getProjectTypeId().shortValue());

				// We create the new entity and store it into the local database
				Project project = new Project(projectType, projectStatus, projectDTO);
				getHelper().createOrUpdateProject(project);
			}
		}

		@Override
		protected Class<PullProjectResponse> getResponseType() {
			return PullProjectResponse.class;
		}
	}

	/**
	 * Sync Task to pull project_cover_image table data from the remote DB
	 * @author nicolas
	 */
	private abstract class PullProjectCoverImagesTask extends PullTask<ProjectCoverImageDTO, PullProjectCoverImageResponse> {
		@Override
		protected String getTableName(){
			return TableName.PROJECT_COVER_IMAGE;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_PROJECT_COVER_IMAGES_PATH;
		}

		@Override
		protected void processResult(PullProjectCoverImageResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPullAt(ProjectCoverImage.class, response.getSuccess());

			Set<ProjectCoverImageDTO> projectCoverImageDTOs = response.getItemSet();
			for(ProjectCoverImageDTO projectCoverImageDTO:projectCoverImageDTOs){
				// We obtain the required parameters for the object creation from the local database
				Project project = getHelper().getProject(projectCoverImageDTO.getProjectId());

				// We create the new entity and store it into the local database
				ProjectCoverImage projectCoverImage = new ProjectCoverImage(project, projectCoverImageDTO);
				getHelper().createOrUpdateProjectCoverImage(projectCoverImage);
			}
		}

		@Override
		protected Class<PullProjectCoverImageResponse> getResponseType() {
			return PullProjectCoverImageResponse.class;
		}
	}

	/**
	 * Sync Task to pull user_to_project table data from the remote DB
	 * @author nicolas
	 */
	private abstract class PullUserToProjectsTask extends PullTask<UserToProjectDTO, PullUserToProjectResponse> {
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
			// Updating sync status
			getHelper().updateSyncStatusPullAt(UserToProject.class, response.getSuccess());

			Set<UserToProjectDTO> userToProjectDTOs = response.getItemSet();
			for(UserToProjectDTO userToProjectDTO:userToProjectDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUser(userToProjectDTO.getUserId().longValue());
				Project project = getHelper().getProject(userToProjectDTO.getProjectId().longValue());
				UserToProjectStatus userToProjectStatus = getHelper().getUserToProjectStatus(userToProjectDTO.getUserToProjectStatusId().shortValue());

				// We create the new entity and store it into the local database
				UserToProject userToProject = new UserToProject(user, project, userToProjectStatus, userToProjectDTO);
				getHelper().createOrUpdateUserToProject(userToProject);
			}
		}

		@Override
		protected Class<PullUserToProjectResponse> getResponseType() {
			return PullUserToProjectResponse.class;
		}
	}



	/**
	 * Sync Task to pull user_invite table data from the remote DB
	 * @author nicolas
	 */
	private abstract class PullUserInvitesTask extends PullTask<UserInviteDTO, PullUserInviteResponse> {
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
			// Updating sync status
			getHelper().updateSyncStatusPullAt(UserInvite.class, response.getSuccess());

			Set<UserInviteDTO> userInviteDTOs = response.getItemSet();
			for(UserInviteDTO userInviteDTO:userInviteDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUser(userInviteDTO.getUserId().longValue());
				Project project = getHelper().getProject(userInviteDTO.getProjectId().longValue());
				InviteStatus inviteStatus = getHelper().getInviteStatus(userInviteDTO.getInviteStatusId().shortValue());

				// We create the new entity and store it into the local database
				UserInvite userInvite = new UserInvite(user, project, inviteStatus, userInviteDTO);
				getHelper().createOrUpdateUserInvite(userInvite);
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
	private abstract class PullUserExpensesTask extends PullTask<UserExpenseDTO, PullUserExpenseResponse> {
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
			// Updating sync status
			getHelper().updateSyncStatusPullAt(UserExpense.class, response.getSuccess());

			Set<UserExpenseDTO> userExpenseDTOs = response.getItemSet();
			for(UserExpenseDTO userExpenseDTO:userExpenseDTOs){
				// We obtain the required parameters for the object creation from the local database
				User user = getHelper().getUser(userExpenseDTO.getUserId().longValue());
				Project project = getHelper().getProject(userExpenseDTO.getProjectId().longValue());
				ExpenseCategory expenseCategory = getHelper().getExpenseCategory(userExpenseDTO.getExpenseCategoryId().shortValue());

				// We create the new entity and store it into the local database
				UserExpense userExpense = new UserExpense(user, project, expenseCategory, userExpenseDTO);
				getHelper().createOrUpdateUserExpense(userExpense);
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
		
		/**
		 * Executes a required action on success. This code executes after the processResult method.
		 */
		protected abstract void executeOnSuccess();

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
			boolean success = false;

			// We validate the response
			if(response != null){
				success = response.getSuccess();
			}

			// We show the status toast if it failed
			String pullMessage = "Pull " +getTableName();
			if(!success){
				showToast(pullMessage+ " Failed!");
			}

			// We save the user and session information returned by the backend
			if(success){
				try {
					// We process the service response
					processResult(response);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing " +pullMessage+ " response", e);
				}

				// We refresh the fragment we called the sync service from
				refreshFragment();
				
				// Executing next synchronized action
				executeOnSuccess();
			}
		}

	}

	/**
	 * Sync Task to push user table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushUsersTask extends PushTask<UserDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_USERS_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<UserDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<User> userList = getHelper().getUserList();

			// We add to the project DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<UserDTO> userDTOList = new ArrayList<UserDTO>();
			for(User user:userList){
				if(user.getUpdatedAt().after(lastPushSuccessAt)){
					userDTOList.add(new UserDTO(user));
				}
			}
			return userDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(User.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.USER, TableField.USER_ID));
			idReferenceList.add(new IdReference(TableName.USER_TO_PROJECT, TableField.USER_TO_PROJECT_USER_ID));
			idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_USER_ID));
			idReferenceList.add(new IdReference(TableName.USER_INVITE, TableField.USER_INVITE_USER_ID));
			idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_USER_ID));
			idReferenceList.add(new IdReference(TableName.USER_AVATAR, TableField.USER_AVATAR_USER_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push user_contact_data table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushUserContactDatasTask extends PushTask<UserContactDataDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_CONTACT_DATA;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_USER_CONTACT_DATAS_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<UserContactDataDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<UserContactData> userContactDataList = getHelper().getUserContactDataList();

			// We add to the user_contact_data DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<UserContactDataDTO> userContactDataDTOList = new ArrayList<UserContactDataDTO>();
			for(UserContactData userContactData:userContactDataList){
				if(userContactData.getUpdatedAt().after(lastPushSuccessAt)){
					userContactDataDTOList.add(new UserContactDataDTO(userContactData));
				}
			}
			return userContactDataDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(UserContactData.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push user_avatar table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushUserAvatarsTask extends PushTask<UserAvatarDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_AVATAR;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_USER_AVATARS_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<UserAvatarDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<UserAvatar> userAvatarList = getHelper().getUserAvatarList();

			// We add to the user_contact_data DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<UserAvatarDTO> userAvatarDTOList = new ArrayList<UserAvatarDTO>();
			for(UserAvatar userAvatar:userAvatarList){
				if(userAvatar.getUpdatedAt().after(lastPushSuccessAt)){
					userAvatarDTOList.add(new UserAvatarDTO(userAvatar));
				}
			}
			return userAvatarDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(UserAvatar.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push project table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushProjectsTask extends PushTask<ProjectDTO, Long, PushLongResponse> {
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
			// We get all the project in the database
			List<Project> projectList = getHelper().getProjectList();

			// We add to the project DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<ProjectDTO> projectDTOList = new ArrayList<ProjectDTO>();
			for(Project project:projectList){
				if(project.getUpdatedAt().after(lastPushSuccessAt)){
					projectDTOList.add(new ProjectDTO(project));
				}
			}
			return projectDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(Project.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.PROJECT, TableField.PROJECT_ID));
			idReferenceList.add(new IdReference(TableName.USER_TO_PROJECT, TableField.USER_TO_PROJECT_PROJECT_ID));
			idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_PROJECT_ID));
			idReferenceList.add(new IdReference(TableName.USER_INVITE, TableField.USER_INVITE_PROJECT_ID));
			idReferenceList.add(new IdReference(TableName.PROJECT_COVER_IMAGE, TableField.PROJECT_COVER_IMAGE_PROJECT_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push user_to_project table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushUserToProjectsTask extends PushTask<UserToProjectDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_TO_PROJECT;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_USER_TO_PROJECTS_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<UserToProjectDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<UserToProject> projectCoverImageList = getHelper().getUserToProjectList();

			// We add to the project_cover_image DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<UserToProjectDTO> userToProjectDTOList = new ArrayList<UserToProjectDTO>();
			for(UserToProject userToProject:projectCoverImageList){
				if(userToProject.getUpdatedAt().after(lastPushSuccessAt)){
					userToProjectDTOList.add(new UserToProjectDTO(userToProject));
				}
			}
			return userToProjectDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(UserToProject.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.USER_TO_PROJECT, TableField.USER_TO_PROJECT_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push user_invite table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushUserInvitesTask extends PushTask<UserInviteDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_INVITE;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_USER_INVITES_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<UserInviteDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<UserInvite> userInviteList = getHelper().getUserInviteList();

			// We add to the user_invite DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<UserInviteDTO> userInviteDTOList = new ArrayList<UserInviteDTO>();
			for(UserInvite userInvite:userInviteList){
				if(userInvite.getUpdatedAt().after(lastPushSuccessAt)){
					userInviteDTOList.add(new UserInviteDTO(userInvite));
				}
			}
			return userInviteDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(UserInvite.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.USER_INVITE, TableField.USER_INVITE_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push user_expense table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushUserExpensesTask extends PushTask<UserExpenseDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.USER_EXPENSE;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_USER_EXPENSES_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<UserExpenseDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<UserExpense> userExpenseList = getHelper().getUserExpenseList();

			// We add to the DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<UserExpenseDTO> userExpenseDTOList = new ArrayList<UserExpenseDTO>();
			for(UserExpense userExpense:userExpenseList){
				if(userExpense.getUpdatedAt().after(lastPushSuccessAt)){
					userExpenseDTOList.add(new UserExpenseDTO(userExpense));
				}
			}
			return userExpenseDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(UserExpense.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
			}
		}
	}

	/**
	 * Sync Task to push project_cover_image table data to the remote DB
	 * @author nicolas
	 */
	private abstract class PushProjectCoverImagesTask extends PushTask<ProjectCoverImageDTO, Long, PushLongResponse> {
		@Override
		protected String getTableName(){
			return TableName.PROJECT_COVER_IMAGE;
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PUSH_PROJECT_COVER_IMAGES_PATH;
		}

		@Override
		protected Class<PushLongResponse> getResponseType() {
			return PushLongResponse.class;
		}

		@Override
		protected List<ProjectCoverImageDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
			// We get all the project in the database
			List<ProjectCoverImage> projectCoverImageList = getHelper().getProjectCoverImageList();

			// We add to the project_cover_image DTO list the ones which were updated after the lastPushSuccessAt date 
			ArrayList<ProjectCoverImageDTO> projectCoverImageDTOList = new ArrayList<ProjectCoverImageDTO>();
			for(ProjectCoverImage projectCoverImage:projectCoverImageList){
				if(projectCoverImage.getUpdatedAt().after(lastPushSuccessAt)){
					projectCoverImageDTOList.add(new ProjectCoverImageDTO(projectCoverImage));
				}
			}
			return projectCoverImageDTOList;
		}

		@Override
		protected void processResult(PushLongResponse response) throws SQLException {
			// Updating sync status
			getHelper().updateSyncStatusPushAt(ProjectCoverImage.class, response.getSuccess());

			List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

			// We create the ID reference list to be updated
			List<IdReference> idReferenceList = new ArrayList<IdReference>();
			idReferenceList.add(new IdReference(TableName.PROJECT_COVER_IMAGE, TableField.PROJECT_COVER_IMAGE_ID));

			//We update all references to this ID
			for(IdUpdate<Long> idUpdate:idUpdateList){
				getHelper().updateIdReferences(idUpdate, idReferenceList);
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
		
		/**
		 * Executes a required action on success. This code executes after the processResult method.
		 */
		protected abstract void executeOnSuccess();

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
			boolean success = false;

			// We validate the response
			if(response != null){
				success = response.getSuccess();
			}

			// We show the status toast if it failed
			String pushMessage = "Push " +getTableName();
			if(!success){
				showToast(pushMessage+ " Failed!");
			}

			// We save the user and session information returned by the backend
			if(success){
				try {
					// We process the service response
					processResult(response);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing " +pushMessage+ " response", e);
				}

				// We refresh the fragment we called the sync service from
				refreshFragment();
				
				// Executing next synchronized action
				executeOnSuccess();
			}
		}

	}
}
