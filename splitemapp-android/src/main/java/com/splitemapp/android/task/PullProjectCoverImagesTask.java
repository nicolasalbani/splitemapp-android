package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.dto.ProjectCoverImageDTO;
import com.splitemapp.commons.domain.dto.response.PullProjectCoverImageResponse;

/**
 * Sync Task to pull project_cover_image table data from the remote DB
 * @author nicolas
 */
public abstract class PullProjectCoverImagesTask extends PullTask<ProjectCoverImageDTO, PullProjectCoverImageResponse> {
	
	public PullProjectCoverImagesTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

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
		databaseHelper.updateSyncStatusPullAt(ProjectCoverImage.class, response.getSuccess());

		Set<ProjectCoverImageDTO> projectCoverImageDTOs = response.getItemSet();
		for(ProjectCoverImageDTO projectCoverImageDTO:projectCoverImageDTOs){
			// We obtain the required parameters for the object creation from the local database
			Project project = databaseHelper.getProject(projectCoverImageDTO.getProjectId());

			// We create the new entity and store it into the local database
			ProjectCoverImage projectCoverImage = new ProjectCoverImage(project, projectCoverImageDTO);
			databaseHelper.createOrUpdateProjectCoverImage(projectCoverImage);
		}
	}

	@Override
	protected Class<PullProjectCoverImageResponse> getResponseType() {
		return PullProjectCoverImageResponse.class;
	}
}
