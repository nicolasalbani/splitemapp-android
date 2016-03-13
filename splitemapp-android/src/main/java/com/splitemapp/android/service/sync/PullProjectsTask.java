package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.response.PullProjectResponse;

public class PullProjectsTask extends PullTask<ProjectDTO, PullProjectResponse> {

	private static final String TAG = PullProjectsTask.class.getSimpleName();

	public PullProjectsTask(Context context) {
		super(context);
	}

	@Override
	protected String getTableName() {
		return TableName.PROJECT;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_PROJECTS_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullProjectResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(Project.class, response.getSuccess(), response.getPulledAt());

		Set<ProjectDTO> projectDTOs = response.getItemSet();
		for(ProjectDTO projectDTO:projectDTOs){
			// We obtain the required parameters for the object creation from the local database
			ProjectStatus projectStatus = getHelper().getProjectStatus(projectDTO.getProjectStatusId().shortValue());
			ProjectType projectType = getHelper().getProjectType(projectDTO.getProjectTypeId().shortValue());

			// Obtaining updatedBy and pushedBy fields
			User updatedBy = getHelper().getUser(projectDTO.getUpdatedBy());
			User pushedBy = getHelper().getUser(projectDTO.getPushedBy());

			// We create the new entity and store it into the local database
			Project project = new Project(projectType, projectStatus, updatedBy, pushedBy, projectDTO);
			getHelper().createOrUpdateProject(project);
		}
	}

	@Override
	protected Class<PullProjectResponse> getResponseType() {
		return PullProjectResponse.class;
	}

}
