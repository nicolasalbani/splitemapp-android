package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

/**
 * Sync Task to push user_contact_data table data to the remote DB
 * @author nicolas
 */
public abstract class PushUserContactDatasTask extends PushTask<UserContactDataDTO, Long, PushLongResponse> {
	
	List<UserContactData> userContactDataList = null;

	public PushUserContactDatasTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

	@Override
	protected String getTableName(){
		return TableName.USER_CONTACT_DATA;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_CONTACT_DATAS_PATH;
	}

	@Override
	protected Class<PushLongResponse> getResponseType() {
		return PushLongResponse.class;
	}

	@Override
	protected List<UserContactDataDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userContactDataList = databaseHelper.getUserContactDataList();

		// We add to the user_contact_data DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<UserContactDataDTO> userContactDataDTOList = new ArrayList<UserContactDataDTO>();
		for(UserContactData userContactData:userContactDataList){
			if(userContactData.getUpdatedAt().after(lastPushSuccessAt)){
				// Adding item to the list
				userContactDataDTOList.add(new UserContactDataDTO(userContactData));
			}
		}
		return userContactDataDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPushAt(UserContactData.class, response.getSuccess());
		
		// Updating pushedAt
		for(UserContactData entity:userContactDataList){
			databaseHelper.updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			databaseHelper.updateIdReferences(idUpdate, idReferenceList);
		}
	}
}