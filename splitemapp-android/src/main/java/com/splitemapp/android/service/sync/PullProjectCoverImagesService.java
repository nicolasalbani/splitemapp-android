package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.dto.ProjectCoverImageDTO;
import com.splitemapp.commons.domain.dto.response.PullProjectCoverImageResponse;

public class PullProjectCoverImagesService extends PullService<ProjectCoverImageDTO, PullProjectCoverImageResponse> {

	private static final String TAG = PullProjectCoverImagesService.class.getSimpleName();

	public PullProjectCoverImagesService() {
		super(TAG);
	}

	@Override
	protected String getTableName() {
		return TableName.PROJECT_COVER_IMAGE;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_PROJECT_COVER_IMAGES_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullProjectCoverImageResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(ProjectCoverImage.class, response.getSuccess(), response.getPulledAt());

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
