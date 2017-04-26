package com.splitemapp.android.screen;

import java.io.IOException;
import java.sql.SQLException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

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
	public void openImageSelector(Integer imageWidth, Integer imageHeight, boolean isCircular){
		// Setting image size
		this.imageHeight = imageHeight;
		this.imageWidth = imageWidth;

		// Calling intent
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Opening image selector
        int requestCode;
        if(isCircular){
            requestCode = Constants.SELECT_CIRCULAR_PICTURE_REQUEST_CODE;
        } else {
            requestCode = Constants.SELECT_RECTANGULAR_PICTURE_REQUEST_CODE;
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
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
	 * @param userAvatar
	 * @param user
	 * @param imageQuality
	 */
	public void setUsetAvatarToImageView(ImageView userAvatar, TextView userInitials, User user, int imageQuality){
		//Getting the user avatar
		byte[] avatar = null;
		try {
			avatar = getHelper().getUserAvatarByUserId(user.getId()).getAvatarData();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		// Setting the avatar
		if(avatar != null){
			userAvatar.setImageBitmap(ImageUtils.getCroppedBitmap(ImageUtils.byteArrayToBitmap(avatar,imageQuality)));
		} else {
			// Setting blue background by default
			userAvatar.setImageResource(R.drawable.shape_circle_blue);
			// Getting initials from user
			String[] tokens = user.getFullName().toUpperCase().split(" ");
			String initials = tokens[0].substring(0,1);
			if(tokens.length >= 2){
				initials = initials + tokens[1].substring(0,1);
			}
			userInitials.setText(initials);
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
	 * @param projectId
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
	 * @param project
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
	 * Executes custom code upon picking an image
	 */
	public void executeOnImageSelection(Bitmap selectedBitmap){}

	/**
	 * Acts upon image selection
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.SELECT_CIRCULAR_PICTURE_REQUEST_CODE  && null != data) {
			Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(imageWidth, imageHeight)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(getContext(), this);
		} else if (requestCode == Constants.SELECT_RECTANGULAR_PICTURE_REQUEST_CODE  && null != data) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(imageWidth, imageHeight)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(getContext(), this);
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                    executeOnImageSelection(bitmap);
                } catch (IOException e) {
                    Log.e(getLoggingTag(), "Error when getting bitmap from URI", e);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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
