package com.splitemapp.android.screen;

import java.sql.SQLException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.utils.PreferencesManager;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserExpense;

public abstract class BaseFragment extends Fragment {

	public DatabaseHelper databaseHelper = null;
	private PreferencesManager preferencesManager = null; 

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
	 * This method returns an instance of the PreferencesManager which contains access to app settings
	 * @return
	 */
	public PreferencesManager getPrefsManager(){
		if(preferencesManager == null){
			preferencesManager = new PreferencesManager(getContext());
		}
		return preferencesManager;
	}
	
	/**
	 * Shows a particular toast based on the received error message
	 * @param message
	 */
	public void showToastForMessage(String message){
		if(message.equals(ServiceConstants.ERROR_MESSAGE_ACCOUNT_EXISTS)){
			showToast(getResources().getString(R.string.account_exists));
		} else if (message.equals(ServiceConstants.ERROR_MESSAGE_LOGIN_FAILED)){
			showToast(getResources().getString(R.string.login_failed));
		} else if (message.equals(ServiceConstants.ERROR_MESSAGE_SERVER_ERROR)){
			showToast(getResources().getString(R.string.server_error));
		} else if (message.equals(ServiceConstants.ERROR_MESSAGE_NETWORK_ERROR)){
			showToast(getResources().getString(R.string.network_error));
		}
	}

	/**
	 * Convenience method to show a Toast with a particular message
	 * @param message String to be shown in the Toast
	 */
	public void showToast(String message){
		// Inflating the toast layout
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layouttoast = inflater.inflate(R.layout.toast, (ViewGroup)getActivity().findViewById(R.id.custom_toast));
		((TextView) layouttoast.findViewById(R.id.toast_textView)).setText(message);
		
		// Creating and showing the toast
		Toast mytoast = new Toast(getActivity().getBaseContext());
        mytoast.setView(layouttoast);
        mytoast.setDuration(Toast.LENGTH_LONG);
        mytoast.setGravity(Gravity.CENTER, 0, 100);
        mytoast.show();
		
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
		Log.i(getLoggingTag(), "Refreshing fragment");
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.detach(this);
		transaction.attach(this);
		transaction.commit();
	}

	/**
	 * Starts the Home activity
	 */
	public void startHomeActivity(){
		Intent intent = new Intent(getActivity(), HomeActivity.class);
		startActivity(intent);
	}

	/**
	 * Sets the user avatar image to the provided image view and the specified image quality
	 * @param userAvatarResource
	 * @param user
	 * @param imageQuality
	 */
	public void setUsetAvatarToImageView(ImageView userAvatarResource, User user, int imageQuality){
		//Getting the user avatar
		byte[] avatar = null;
		try {
			avatar = getHelper().getUserAvatarByUserId(user.getId()).getAvatarData();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		// Setting the avatar
		if(avatar != null){
			userAvatarResource.setImageBitmap(ImageUtils.getCroppedBitmap(ImageUtils.byteArrayToBitmap(avatar,imageQuality)));
		}
	}

	/**
	 * Checks whether the user has an avatar assigned or not
	 * @param user
	 * @return
	 */
	public boolean isUserHasAvatar(User user){
		try {
			UserAvatar userAvatar = getHelper().getUserAvatarByUserId(user.getId());
			if(userAvatar != null && userAvatar.getAvatarData() != null){
				return true;
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		return false;
	}
	
	/**
	 * Returns the project title in the database for a particular projectId
	 * @return
	 */
	public String getProjectTitle(Long projectId){
		String title = null;
		
		try {
			title = getHelper().getProject(projectId).getTitle();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught while getting project title", e);
		}
		
		return title;
	}

	/**
	 * Sets the project avatar image to the provided image view and the specified image quality
	 * @param projectAvatarResource
	 * @param project
	 * @param imageQuality
	 */
	public void setProjectAvatar(ImageView projectAvatarResource, Long projectId, int imageQuality){
		//Getting the project cover
		byte[] projectAvatar = null;
		try {
			projectAvatar = getHelper().getProjectCoverImageByProject(projectId).getAvatarData();
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
			coverImage = getHelper().getProjectCoverImageByProject(project.getId()).getAvatarData();
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
			for(UserExpense userExpense:getHelper().getActiveUserExpensesByProjectId(projectId, null)){
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

	/**
	 * Rotates the provided image view 90 degrees anti-clockwise
	 * @param imageView
	 */
	public void rotateImageViewAntiClockwise(ImageView imageView){
		// Creating anti-clockwise rotation animation
		rotateImageView(imageView, -90f, 100);
	}

	/**
	 * Rotates the provided image view 90 degrees clockwise
	 * @param imageView
	 */
	public void rotateImageViewClockwise(ImageView imageView){
		// Creating anti-clockwise rotation animation
		rotateImageView(imageView, 0f, 100);
	}

	/**
	 * Rotates the provided image view in the specified duration the specified amount of rotation degrees
	 * @param imageView
	 * @param rotationDegree
	 * @param duration
	 */
	private void rotateImageView(final ImageView imageView, final float rotationDegree, int duration){
		// Getting image center
		final float hCenter = imageView.getWidth()/2;
		final float vCenter = imageView.getHeight()/2;

		// Creating anti-clockwise rotation animation
		RotateAnimation anim = new RotateAnimation(0f, rotationDegree,  hCenter, vCenter);
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(duration);
		anim.setFillAfter(true);
		anim.setFillEnabled(true);

		// Start animating the image
		imageView.startAnimation(anim);
	}

}
