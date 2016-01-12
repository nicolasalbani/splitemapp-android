package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUserToProjectsService extends PushService<UserToProjectDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserToProjectsService.class.getSimpleName();

	private List<UserToProject> userToProjectList = null;

	public PushUserToProjectsService() {
		super(TAG);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.USER_TO_PROJECT;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_TO_PROJECTS_PATH;
	}

	@Override
	protected List<UserToProjectDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userToProjectList = getHelper().getUserToProjectList();

		// We add to the project_cover_image DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<UserToProjectDTO> userToProjectDTOList = new ArrayList<UserToProjectDTO>();
		for(UserToProject userToProject:userToProjectList){
			if(userToProject.getUpdatedAt().after(lastPushSuccessAt)){
				// Adding item to the list
				userToProjectDTOList.add(new UserToProjectDTO(userToProject));
			}
		}
		return userToProjectDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(UserToProject.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(UserToProject entity:userToProjectList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_TO_PROJECT, TableField.USER_TO_PROJECT_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			getHelper().updateIdReferences(idUpdate, idReferenceList);
		}
	}
}
