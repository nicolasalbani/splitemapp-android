package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUserToProjectsTask extends PushTask<UserToProject, UserToProjectDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserToProjectsTask.class.getSimpleName();

	private List<UserToProject> userToProjectList = null;

	public PushUserToProjectsTask(Context context) {
		super(context);
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
	protected List<UserToProjectDTO> getRequestItemList() throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userToProjectList = getHelper().getUserToProjectList();

		// Removing items that should not be pushed
		removeNotPushable(userToProjectList);

		// We add to the project_cover_image DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<UserToProjectDTO> userToProjectDTOList = new ArrayList<UserToProjectDTO>();
		for(UserToProject userToProject:userToProjectList){
			// Setting the user that pushes the record
			userToProject.setPushedBy(getHelper().getLoggedUser());
			// Adding item to the list
			userToProjectDTOList.add(new UserToProjectDTO(userToProject));
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
		updateIdReferences(idUpdateList, idReferenceList);
	}
}
