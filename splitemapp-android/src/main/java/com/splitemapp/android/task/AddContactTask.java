package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
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
 * Questions tasks which enables the user to send a question to the remote server
 * @author nicolas
 *
 */
public abstract class AddContactTask extends BaseAsyncTask<Void, Void, SynchronizeContactsResponse> {

	String email;

	public AddContactTask(DatabaseHelper databaseHelper, String email){
		super(databaseHelper);
		this.email = email;
	}

	private String getLoggingTag(){
		return getClass().getSimpleName();
	}

	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	/**
	 * Method to be called when there was no user found for that email address
	 */
	abstract protected void executeOnUserNotFound();

	@Override
	public SynchronizeContactsResponse doInBackground(Void... params) {
		SynchronizeContactsResponse synchronizeContactsResponse = null;
		
		try {
			Log.i(getLoggingTag(), "Search contact START");

			// We add the email address to the contacts email address list
			List<String> contactsEmailAddressList = new ArrayList<String>();
			contactsEmailAddressList.add(email);

			// We get the session token
			String sessionToken = databaseHelper.getSessionToken();

			// We create the request
			SynchronizeContactsRequest synchronizeContactsRequest = new SynchronizeContactsRequest();
			synchronizeContactsRequest.setContactsEmailAddressList(contactsEmailAddressList);
			synchronizeContactsRequest.setToken(sessionToken);

			// We call the rest service and send back the synchronize contacts
			synchronizeContactsResponse = NetworkUtils.callRestService(ServiceConstants.SYNCHRONIZE_CONTACTS_PATH, synchronizeContactsRequest, SynchronizeContactsResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return synchronizeContactsResponse;
	}

	@Override
	public void onPostExecute(SynchronizeContactsResponse response) {
		boolean success = false;

		// Validating the response
		if(response != null){
			success = response.getSuccess();
		} else {
			executeOnFail(ServiceConstants.ERROR_MESSAGE_NETWORK_ERROR);
			return;
		}

		// We show the status toast if it failed
		if(!success){
			executeOnFail(response.getMessage());
		} else {
			if(response.getUserDTOList().isEmpty()){
				executeOnUserNotFound();
			} else {
				try {
					// Adding user to users table
					for(UserDTO userDTO:response.getUserDTOList()){
						// Reconstructing the user status object
						UserStatus userStatus = databaseHelper.getUserStatus(userDTO.getUserStatusId().shortValue());

						// Reconstructing the user object
						User user = new User(userStatus, userDTO);
						databaseHelper.createOrUpdateUser(user);

						// Reconstructing the user contact data object
						for(UserContactDataDTO userContactDataDTO:response.getUserContactDataDTOList()){
							// Matching the appropriate user contact data
							if(userDTO.getId() == userContactDataDTO.getUserId()){
								User updatedBy = databaseHelper.getUser(userContactDataDTO.getUpdatedBy().longValue());
								User pushedBy = databaseHelper.getUser(userContactDataDTO.getPushedBy().longValue());
								UserContactData userContactData = new UserContactData(user,updatedBy,pushedBy,userContactDataDTO);
								databaseHelper.createOrUpdateUserContactData(userContactData);
							}
						}

						// Reconstructing the user avatar data object
						for(UserAvatarDTO userAvatarDTO:response.getUserAvatarDTOList()){
							// Matching the appropriate user avatar
							if(userDTO.getId() == userAvatarDTO.getUserId()){
								User updatedBy = databaseHelper.getUser(userAvatarDTO.getUpdatedBy().longValue());
								User pushedBy = databaseHelper.getUser(userAvatarDTO.getPushedBy().longValue());
								UserAvatar userAvatar = new UserAvatar(user,updatedBy,pushedBy,userAvatarDTO);
								databaseHelper.createOrUpdateUserAvatar(userAvatar);
							}
						}
					}

					// Executing code on success
					executeOnSuccess();
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught!", e);
				}
			}
		}
		
		Log.i(getLoggingTag(), "Search contact END");
	}
}
