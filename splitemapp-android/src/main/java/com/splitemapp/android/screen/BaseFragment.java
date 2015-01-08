package com.splitemapp.android.screen;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.home.HomeActivity;

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
		intent.putExtra(Constants.EXTRA_USER_ID, userId);
		startActivity(intent);
	}

}
