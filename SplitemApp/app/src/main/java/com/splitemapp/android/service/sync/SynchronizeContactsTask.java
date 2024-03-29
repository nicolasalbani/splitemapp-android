package com.splitemapp.android.service.sync;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.splitemapp.android.service.BaseTask;
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

public class SynchronizeContactsTask extends BaseTask {

	private static final String TAG = SynchronizeContactsTask.class.getSimpleName();

	private Context context;

	public SynchronizeContactsTask(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void executeService(Intent intent) throws Exception{
		Log.i(TAG, "Synchronize Contacts START");
		try {
			// We get the session token
			String sessionToken = getHelper().getSessionToken();

			// We create the login request
			SynchronizeContactsRequest synchronizeContactsRequest = new SynchronizeContactsRequest();
			synchronizeContactsRequest.setContactsEmailAddressList(getContactsEmailAddressList());
			synchronizeContactsRequest.setToken(sessionToken);

			// We call the rest service and send back the synchronize contacts
			SynchronizeContactsResponse response = NetworkUtils.callRestService(ServiceConstants.SYNCHRONIZE_CONTACTS_PATH, synchronizeContactsRequest, SynchronizeContactsResponse.class);

			boolean success = false;

			// Validating the response
			if(response != null){
				success = response.getSuccess();
			}

			// We show the status toast if it failed
			if(!success){
				executeOnFail();
			} else{
				// Saving the information returned by the back-end
				getHelper().updateSyncStatusPullAt(User.class, success, response.getPulledAt());
				getHelper().updateSyncStatusPullAt(UserContactData.class, success, response.getPulledAt());
				getHelper().updateSyncStatusPullAt(UserAvatar.class, success, response.getPulledAt());
				for(UserDTO userDTO:response.getUserDTOList()){
					// Reconstructing the user status object
					UserStatus userStatus = getHelper().getUserStatus(userDTO.getUserStatusId().shortValue());

					// Reconstructing the user object
					User user = new User(userStatus, userDTO);
					getHelper().createOrUpdateUser(user);

					// Reconstructing the user contact data object
					for(UserContactDataDTO userContactDataDTO:response.getUserContactDataDTOList()){
						// Matching the appropriate user contact data
						if(userDTO.getId() == userContactDataDTO.getUserId()){
							User updatedBy = getHelper().getUser(userContactDataDTO.getUpdatedBy().longValue());
							User pushedBy = getHelper().getUser(userContactDataDTO.getPushedBy().longValue());
							UserContactData userContactData = new UserContactData(user,updatedBy,pushedBy,userContactDataDTO);
							getHelper().createOrUpdateUserContactData(userContactData);
						}
					}

					// Reconstructing the user avatar data object
					for(UserAvatarDTO userAvatarDTO:response.getUserAvatarDTOList()){
						// Matching the appropriate user avatar
						if(userDTO.getId() == userAvatarDTO.getUserId()){
							User updatedBy = getHelper().getUser(userAvatarDTO.getUpdatedBy().longValue());
							User pushedBy = getHelper().getUser(userAvatarDTO.getPushedBy().longValue());
							UserAvatar userAvatar = new UserAvatar(user,updatedBy,pushedBy,userAvatarDTO);
							getHelper().createOrUpdateUserAvatar(userAvatar);
						}
					}
				}

				// Executing on success action
				executeOnSuccess();
			} 


		} finally {
			Log.i(TAG, "Synchronize Contacts END");
		}
	}

	/**
	 * Returns a list with all the email addresses from the device contacts 
	 * @return
	 */
	private List<String> getContactsEmailAddressList(){
		List<String> contactsEmailAddressList = new ArrayList<String>();

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				Cursor cur1 = cr.query( ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
						null, 
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
						new String[]{id}, null); 
				while (cur1.moveToNext()) { 
					String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					if(email!=null){
						contactsEmailAddressList.add(email);
					}
				} 
				cur1.close();
			}
		}

		return contactsEmailAddressList;
	}
}
