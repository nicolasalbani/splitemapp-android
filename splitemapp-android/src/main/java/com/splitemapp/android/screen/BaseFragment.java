package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public abstract class BaseFragment extends Fragment {

	public DatabaseHelper databaseHelper = null;

	private int imageWidth;
	private int imageHeight;

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
	public DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		}
		return databaseHelper;
	}

	/**
	 * Convenience method to show a Toast with a particular message
	 * @param message String to be shown in the Toast
	 */
	public void showToast(String message){
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Opens the image 
	 */
	public void openImageSelector(Integer imageWidth, Integer imageHeight){
		// Setting image size
		this.imageHeight = imageHeight;
		this.imageWidth = imageWidth;

		// Calling intent
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		startActivityForResult(Intent.createChooser(intent,"Complete action using"), Constants.SELECT_PICTURE_REQUEST_CODE);
	}

	/**
	 * Gets the full path from an image URI
	 * @param uri URI from the image
	 * @return String containing the full path to the image
	 */
	public String getImagePath(Uri uri) {
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
	public void refreshFragment(){
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.detach(this);
		transaction.attach(this);
		transaction.commit();
	}

	/**
	 * Starts the Home activity
	 * @param userId Long containing the user id from the local DB
	 */
	public void startHomeActivity(Long userId){
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

	/**
	 * Sets the user avatar image to the provided image view and the specified image quality
	 * @param userAvatarResource
	 * @param user
	 * @param imageQuality
	 */
	public void setUsetAvatar(ImageView userAvatarResource, User user, int imageQuality){
		//Getting the user avatar
		byte[] avatar = null;
		try {
			avatar = getHelper().getUserAvatarDao().queryForEq(TableField.USER_AVATAR_USER_ID, user.getId()).get(0).getAvatarData();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		// Setting the avatar
		if(avatar != null){
			userAvatarResource.setImageBitmap(ImageUtils.getCroppedBitmap(ImageUtils.byteArrayToBitmap(avatar,imageQuality)));
		}
	}

	/**
	 * Sets the project avatar image to the provided image view and the specified image quality
	 * @param projectAvatarResource
	 * @param project
	 * @param imageQuality
	 */
	public void setProjectAvatar(ImageView projectAvatarResource, Project project, int imageQuality){
		//Getting the project cover
		byte[] projectAvatar = null;
		try {
			projectAvatar = getHelper().getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, project.getId()).get(0).getAvatarData();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		// Setting the project cover
		if(projectAvatar != null){
			projectAvatarResource.setImageBitmap(ImageUtils.getCroppedBitmap(ImageUtils.byteArrayToBitmap(projectAvatar,imageQuality)));
		}
	}

	/**
	 * Sets the project cover image to the provided image view and the specified image quality
	 * @param projectAvatarResource
	 * @param user
	 * @param imageQuality
	 */
	public void setProjectCoverImage(ImageView projectAvatarResource, Project project, int imageQuality){
		//Getting the project image cover
		byte[] coverImage = null;
		try {
			coverImage = getHelper().getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, project.getId()).get(0).getAvatarData();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		// Setting the image cover
		if(coverImage != null){
			projectAvatarResource.setImageBitmap(ImageUtils.byteArrayToBitmap(coverImage,imageQuality));
		}
	}

	/**
	 * Returns whether or not the image should be cropped to a circle. False if not overriden.
	 * @return
	 */
	public boolean getCropImage(){
		return false;
	}

	/**
	 * Executes custom code upon picking an image
	 */
	public void executeOnImageSelection(Bitmap selectedBitmap){}

	/**
	 * Acts upon image selection
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.SELECT_PICTURE_REQUEST_CODE  && null != data) {
			// Getting the image bitmap
			Bitmap bitmap = data.getExtras().getParcelable("data");

			// Cropping the image if required, otherwise scaling image
			if(getCropImage()){
				bitmap = ImageUtils.getCroppedBitmap(bitmap);
			} else {
				bitmap = Bitmap.createScaledBitmap(bitmap, this.imageWidth, this.imageHeight, true);
			}

			// Executing custom code upon picking an image
			executeOnImageSelection(bitmap);
		}
	}

	/**
	 * Returns the total sum of user expenses for a particular project
	 * @param projectId
	 * @return
	 */
	public float getTotalExpenseForProject(Long projectId){
		float total = 0;
		try {
			for(UserExpense userExpense:getHelper().getAllUserExpenseForProject(projectId)){
				if(userExpense.getProject().getId() == projectId){
					total += userExpense.getExpense().floatValue();
				}
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		return total;
	}

	/**
	 * Returns the screen width in dp
	 * @return
	 */
	public float getScreenWidth(){
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		return displayMetrics.widthPixels / displayMetrics.density;
	}

	/**
	 * Returns the screen heigth in dp
	 * @return
	 */
	public float getScreenHeight(){
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		return displayMetrics.heightPixels / displayMetrics.density;
	}

	/**
	 * Returns the project cover image height in dp
	 * @return
	 */
	public int getProjectCoverImageHeight(){
		return (int) (getResources().getDimension(R.dimen.project_cover_image_height) / getResources().getDisplayMetrics().density);
	}

	/**
	 * Returns the project cover image width in dp
	 * @return
	 */
	public int getProjectCoverImageWidth(){
		return (int)getScreenWidth();
	}

}
