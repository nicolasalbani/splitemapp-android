package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.response.PullProjectResponse;

public class PullProjectsService extends PullService<ProjectDTO, PullProjectResponse> {

	private static final String TAG = PullProjectsService.class.getSimpleName();

	public PullProjectsService() {
		super(TAG);
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
