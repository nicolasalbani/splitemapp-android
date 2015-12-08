package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.response.PullUserContactDataResponse;

/**
 * Sync Task to pull user_contact_data table data from the remote DB
 * @author nicolas
 */
public abstract class PullUserContactDatasTask extends PullTask<UserContactDataDTO, PullUserContactDataResponse> {
	
	public PullUserContactDatasTask(DatabaseHelper databaseHelper) {
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
		return ServiceConstants.PULL_USER_CONTACT_DATAS_PATH;
	}

	@Override
	protected void processResult(PullUserContactDataResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPullAt(UserContactData.class, response.getSuccess());

		Set<UserContactDataDTO> userContactDataDTOs = response.getItemSet();
		for(UserContactDataDTO userContactDataDTO:userContactDataDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = databaseHelper.getUser(userContactDataDTO.getUserId().longValue());

			// We create the new entity and store it into the local database
			UserContactData userContactData = new UserContactData(user, userContactDataDTO);
			databaseHelper.createOrUpdateUserContactData(userContactData);
		}
	}

	@Override
	protected Class<PullUserContactDataResponse> getResponseType() {
		return PullUserContactDataResponse.class;
	}
}