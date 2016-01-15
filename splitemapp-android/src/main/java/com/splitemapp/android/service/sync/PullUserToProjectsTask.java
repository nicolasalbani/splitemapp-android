package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.response.PullUserToProjectResponse;

public class PullUserToProjectsTask extends PullTask<UserToProjectDTO, PullUserToProjectResponse> {

	private static final String TAG = PullUserToProjectsTask.class.getSimpleName();

	public PullUserToProjectsTask(Context context) {
		super(context);
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
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullUserToProjectResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(UserToProject.class, response.getSuccess(), response.getPulledAt());

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
