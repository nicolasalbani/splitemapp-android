package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.dto.ProjectCoverImageDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

/**
 * Sync Task to push project_cover_image table data to the remote DB
 * @author nicolas
 */
public abstract class PushProjectCoverImagesTask extends PushTask<ProjectCoverImageDTO, Long, PushLongResponse> {
	
	private List<ProjectCoverImage> projectCoverImageList = null;
	
	public PushProjectCoverImagesTask(DatabaseHelper databaseHelper) {
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
		return ServiceConstants.PUSH_PROJECT_COVER_IMAGES_PATH;
	}

	@Override
	protected List<ProjectCoverImageDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		projectCoverImageList = databaseHelper.getProjectCoverImageList();

		// We add to the project_cover_image DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<ProjectCoverImageDTO> projectCoverImageDTOList = new ArrayList<ProjectCoverImageDTO>();
		for(ProjectCoverImage projectCoverImage:projectCoverImageList){
			if(projectCoverImage.getUpdatedAt().after(lastPushSuccessAt)){
				// Adding item to the list
				projectCoverImageDTOList.add(new ProjectCoverImageDTO(projectCoverImage));
			}
		}
		return projectCoverImageDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPushAt(ProjectCoverImage.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(ProjectCoverImage entity:projectCoverImageList){
			databaseHelper.updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.PROJECT_COVER_IMAGE, TableField.PROJECT_COVER_IMAGE_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			databaseHelper.updateIdReferences(idUpdate, idReferenceList);
		}
	}
}
