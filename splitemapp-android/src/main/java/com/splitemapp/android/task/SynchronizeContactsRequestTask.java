package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.request.SynchronizeContactsRequest;
import com.splitemapp.commons.domain.dto.response.SynchronizeContactsResponse;

/**
 * Synchronize contacts task which queries the remote server for user information
 * @author nicolas
 *
 */
public abstract class SynchronizeContactsRequestTask extends BaseAsyncTask<Void, Void, SynchronizeContactsResponse> {
	private List<String> contactsEmailAddressList;

	public SynchronizeContactsRequestTask(DatabaseHelper databaseHelper, List<String> contactsEmailAddressList) {
		super(databaseHelper);
		
		this.contactsEmailAddressList = contactsEmailAddressList;
	}

	String getLoggingTag(){
		return getClass().getSimpleName();
	}
	
	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	@Override
	public SynchronizeContactsResponse doInBackground(Void... params) {
		try {
			// We create the login request
			SynchronizeContactsRequest synchronizeContactsRequest = new SynchronizeContactsRequest();
			synchronizeContactsRequest.setContactsEmailAddressList(contactsEmailAddressList);

			// We call the rest service and send back the synchronize contacts
			return NetworkUtils.callRestService(ServiceConstants.SYNCHRONIZE_CONTACTS_PATH, synchronizeContactsRequest, SynchronizeContactsResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void onPostExecute(SynchronizeContactsResponse synchronizeContactsResponse) {
		boolean success = false;

		// Validating the response
		if(synchronizeContactsResponse != null){
			success = synchronizeContactsResponse.getSuccess();
		}

		// We show the status toast if it failed
		if(!success){
			executeOnFail();
		} else{
			// Saving the information returned by the back-end
			try {
				databaseHelper.updateSyncStatusPullAt(User.class, success);
				databaseHelper.updateSyncStatusPullAt(UserContactData.class, success);
				databaseHelper.updateSyncStatusPullAt(UserAvatar.class, success);
				if(success){
					for(UserDTO userDTO:synchronizeContactsResponse.getUserDTOList()){
						// Reconstructing the user status object
						UserStatus userStatus = databaseHelper.getUserStatus(userDTO.getUserStatusId().shortValue());

						// Reconstructing the user object
						User user = new User(userStatus, userDTO);
						databaseHelper.createOrUpdateUser(user);

						// Reconstructing the user contact data object
						for(UserContactDataDTO userContactDataDTO:synchronizeContactsResponse.getUserContactDataDTOList()){
							// Matching the appropriate user contact data
							if(userDTO.getId() == userContactDataDTO.getUserId()){
								UserContactData userContactData = new UserContactData(user,userContactDataDTO);
								databaseHelper.createOrUpdateUserContactData(userContactData);
							}
						}

						// Reconstructing the user avatar data object
						for(UserAvatarDTO userAvatarDTO:synchronizeContactsResponse.getUserAvatarDTOList()){
							// Matching the appropriate user avatar
							if(userDTO.getId() == userAvatarDTO.getUserId()){
								UserAvatar userAvatar = new UserAvatar(user, userAvatarDTO);
								databaseHelper.createOrUpdateUserAvatar(userAvatar);
							}
						}
					}
				}
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while synchronizing contacts", e);
			}
		} 

	}
}