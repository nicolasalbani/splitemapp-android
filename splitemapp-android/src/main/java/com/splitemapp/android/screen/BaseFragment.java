package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.R;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.User;

public abstract class BaseFragment extends Fragment {

	protected DatabaseHelper databaseHelper = null;

	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
	}

	/**
	 * This method returns the desired TAG to be used for logging purposes
	 * @return
	 */
	public abstract String getLoggingTag();

	/**
	 * This method is called when the fragment is destroyed, releasing the database helper object
	 */
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	/**
	 * This method calls the OpenHelperManager getHelper static method with the proper DatabaseHelper class reference 
	 * @return DatabaseHelper object which offers DAO for every domain entity
	 */
	protected DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		}
		return databaseHelper;
	}

	/**
	 * Convenience method to show a Toast with a particular message
	 * @param message String to be shown in the Toast
	 */
	protected void showToast(String message){
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Gets the full path from an image URI
	 * @param uri URI from the image
	 * @return String containing the full path to the image
	 */
	protected String getImagePath(Uri uri) {
		// just some safety built in 
		if( uri == null ) {
			return null;
		}

		// try to retrieve the image from the media store first
		// this will only work for images selected from gallery
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().getContentResolver().query(uri,filePathColumn, null, null, null);
		if( cursor != null ){
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String imagePath = cursor.getString(column_index);
			cursor.close();
			return imagePath;
		}
		// this is our fallback here
		return uri.getPath();
	}

	/**
	 * Refreshes the fragment you are on
	 */
	protected void refreshFragment(){
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.detach(this);
		transaction.attach(this);
		transaction.commit();
	}

	/**
	 * Starts the Home activity
	 * @param userId Long containing the user id from the local DB
	 */
	protected void startHomeActivity(Long userId){
		Intent intent = new Intent(getActivity(), HomeActivity.class);
		startActivity(intent);
	}

	/**
	 * Returns a list with all the email addresses from the device contacts 
	 * @return
	 */
	public List<String> getContactsEmailAddressList(){
		List<String> contactsEmailAddressList = new ArrayList<String>();

		ContentResolver cr = getActivity().getContentResolver();
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
	
	public void setUsetAvatar(ImageView userAvatarResource, User user){
		//Getting the user avatar
		byte[] avatar = null;
		try {
			avatar = getHelper().getUserAvatarDao().queryForEq(TableField.USER_AVATAR_USER_ID, user.getId()).get(0).getAvatarData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Setting the avatar
		if(avatar != null){
			userAvatarResource.setImageBitmap(ImageUtils.getCroppedBitmap(ImageUtils.byteArrayToBitmap(avatar,10)));
		} else {
			userAvatarResource.setImageResource(R.drawable.avatar_placeholder);
		}
	}
}
