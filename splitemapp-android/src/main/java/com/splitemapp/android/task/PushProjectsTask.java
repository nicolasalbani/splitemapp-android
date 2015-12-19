package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

/**
 * Sync Task to push project table data to the remote DB
 * @author nicolas
 */
public abstract class PushProjectsTask extends PushTask<ProjectDTO, Long, PushLongResponse> {
	
	private List<Project> projectList = null;
	
	public PushProjectsTask(DatabaseHelper databaseHelper) {
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
		return ServiceConstants.PUSH_PROJECTS_PATH;
	}

	@Override
	protected Class<PushLongResponse> getResponseType() {
		return PushLongResponse.class;
	}

	@Override
	protected List<ProjectDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		projectList = databaseHelper.getProjectList();

		// We add to the project DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<ProjectDTO> projectDTOList = new ArrayList<ProjectDTO>();
		for(Project project:projectList){
			if(project.getUpdatedAt().after(lastPushSuccessAt)){
				// Adding item to the list
				projectDTOList.add(new ProjectDTO(project));
			}
		}
		return projectDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPushAt(Project.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(Project entity:projectList){
			databaseHelper.updatePushedAt(entity, response.getPushedAt());
		}

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
			databaseHelper.updateIdReferences(idUpdate, idReferenceList);
		}
	}
}