package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.response.PullUserToProjectResponse;

/**
 * Sync Task to pull user_to_project table data from the remote DB
 * @author nicolas
 */
public class PullUserToProjectsTask extends PullTask<UserToProjectDTO, PullUserToProjectResponse> {
	
	public PullUserToProjectsTask(DatabaseHelper databaseHelper, RestfulFragment restfulFragment) {
		super(databaseHelper, restfulFragment);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

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
		databaseHelper.updateSyncStatusPullAt(UserToProject.class, response.getSuccess());

		Set<UserToProjectDTO> userToProjectDTOs = response.getItemSet();
		for(UserToProjectDTO userToProjectDTO:userToProjectDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = databaseHelper.getUser(userToProjectDTO.getUserId().longValue());
			Project project = databaseHelper.getProject(userToProjectDTO.getProjectId().longValue());
			UserToProjectStatus userToProjectStatus = databaseHelper.getUserToProjectStatus(userToProjectDTO.getUserToProjectStatusId().shortValue());

			// We create the new entity and store it into the local database
			UserToProject userToProject = new UserToProject(user, project, userToProjectStatus, userToProjectDTO);
			databaseHelper.createOrUpdateUserToProject(userToProject);
		}
	}

	@Override
	protected Class<PullUserToProjectResponse> getResponseType() {
		return PullUserToProjectResponse.class;
	}
}
