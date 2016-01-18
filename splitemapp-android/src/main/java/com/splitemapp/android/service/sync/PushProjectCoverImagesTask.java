package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.dto.ProjectCoverImageDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushProjectCoverImagesTask extends PushTask<ProjectCoverImageDTO, Long, PushLongResponse> {

	private static final String TAG = PushProjectCoverImagesTask.class.getSimpleName();

	private List<ProjectCoverImage> projectCoverImageList = null;

	public PushProjectCoverImagesTask(Context context) {
		super(context);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.PROJECT_COVER_IMAGE;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_PROJECT_COVER_IMAGES_PATH;
	}

	@Override
	protected List<ProjectCoverImageDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		projectCoverImageList = getHelper().getProjectCoverImageList();

		// We add to the project_cover_image DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<ProjectCoverImageDTO> projectCoverImageDTOList = new ArrayList<ProjectCoverImageDTO>();
		for(ProjectCoverImage projectCoverImage:projectCoverImageList){
			if(projectCoverImage.getUpdatedAt().after(lastPushSuccessAt)  && (projectCoverImage.getPushedAt() == null || projectCoverImage.getPushedAt().before(lastPushSuccessAt))){
				// Adding item to the list
				projectCoverImageDTOList.add(new ProjectCoverImageDTO(projectCoverImage));
			}
		}
		return projectCoverImageDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(ProjectCoverImage.class, response.getSuccess(), response.getPushedAt());

		// Updating pushedAt
		for(ProjectCoverImage entity:projectCoverImageList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

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
