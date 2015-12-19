package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.response.PullProjectResponse;

/**
 * Sync Task to pull project table data from the remote DB
 * @author nicolas
 */
public abstract class PullProjectsTask extends PullTask<ProjectDTO, PullProjectResponse> {
	
	public PullProjectsTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

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
		databaseHelper.updateSyncStatusPullAt(Project.class, response.getSuccess(), response.getPulledAt());

		Set<ProjectDTO> projectDTOs = response.getItemSet();
		for(ProjectDTO projectDTO:projectDTOs){
			// We obtain the required parameters for the object creation from the local database
			ProjectStatus projectStatus = databaseHelper.getProjectStatus(projectDTO.getProjectStatusId().shortValue());
			ProjectType projectType = databaseHelper.getProjectType(projectDTO.getProjectTypeId().shortValue());

			// We create the new entity and store it into the local database
			Project project = new Project(projectType, projectStatus, projectDTO);
			databaseHelper.createOrUpdateProject(project);
		}
	}

	@Override
	protected Class<PullProjectResponse> getResponseType() {
		return PullProjectResponse.class;
	}
}