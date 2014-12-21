package com.splitemapp.android.screen;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.request.PullAllSyncRequest;
import com.splitemapp.commons.domain.dto.response.PullAllSyncResponse;

public abstract class BaseFragment extends Fragment {

	protected DatabaseHelper databaseHelper = null;

	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
		
		// We initialize logging
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
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
	 * 
	 * @param servicePath String containing the rest service name
	 * @param request <E> The request object used in the rest service call
	 * @param responseType <T> The response class that the rest service call is supposed to return
	 * @return	<T> An instance of the response type specified as a parameter
	 */
	protected <E,T> T callRestService(String servicePath, E request, Class<T> responseType){
		// We create the url based on the provider serviceName
		String url = "http://"+Constants.BACKEND_HOST+":"+Constants.BACKEND_PORT+"/"+Constants.BACKEND_PATH+servicePath;

		// We get an instance of the spring framework RestTemplate and configure wrapping the root XML element
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

		// We use old version of request factory that uses HTTPClient instead of HttpURLConnection to avoid bugs
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());  

		// We make the POST rest service call
		T response = restTemplate.postForObject(url, request, responseType);
		return response;
	}

	protected void showToast(String message){
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	protected String getIpAddress() throws SocketException {
		Enumeration<NetworkInterface> networkInterfaceList = NetworkInterface.getNetworkInterfaces();

		while(networkInterfaceList.hasMoreElements()){
			Enumeration<InetAddress> inetAddresses = networkInterfaceList.nextElement().getInetAddresses();
			while(inetAddresses.hasMoreElements()){
				InetAddress inetAddress = inetAddresses.nextElement();
				if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
					return inetAddress.getHostAddress();
				}
			}
		}

		return Constants.LOOPBACK_ADDRESS;
	}

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
	
	protected Bitmap decodeScaledBitmap(String filePath, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filePath, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath, options);
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}
	
	protected User getCurrentUser(Long userId){
		User user = null;
		try {
			Dao<User,Integer> userDao = getHelper().getUserDao();
			for(User u:userDao){
				if(userId.equals(u.getId())){
					user = u;
				}
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return user;
	}
	
	protected Bitmap getCroppedBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	    return output;
	}
	
	public class PullAllSyncTask extends AsyncTask<Void, Void, PullAllSyncResponse> {
		@Override
		protected PullAllSyncResponse doInBackground(Void... params) {
			try {
				// We create the login request
				PullAllSyncRequest pullAllSyncRequest = new PullAllSyncRequest();
				pullAllSyncRequest.setLastPullSuccessAt(new Date(100));
				UserSession userSession = getHelper().getUserSessionDao().queryForAll().get(0);
				pullAllSyncRequest.setToken(userSession.getToken());

				// We call the rest service and send back the login response
				return callRestService(ServiceConstants.PULL_ALL_SYNC_PATH, pullAllSyncRequest, PullAllSyncResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(PullAllSyncResponse pullAllSyncResponse) {
			boolean loginSuccess = false;

			// We validate the response
			if(pullAllSyncResponse != null){
				loginSuccess = pullAllSyncResponse.getSuccess();
			}

			// We show the status toast
			showToast(loginSuccess ? "PullAllSync Successful!" : "PullAllSync Failed!");

			// We save the user and session information returned by the backend
			if(loginSuccess){
				try {
					// We save all project instances received
					Set<ProjectDTO> projectDTOs = pullAllSyncResponse.getProjectDTOs();
					for(ProjectDTO projectDTO:projectDTOs){
						ProjectStatus projectStatus = getHelper().getProjectStatusDao().queryForId(projectDTO.getProjectStatusId().intValue());
						ProjectType projectType = getHelper().getProjectTypeDao().queryForId(projectDTO.getProjectTypeId().intValue());
						Project project = new Project(projectType, projectStatus, projectDTO);
						getHelper().getProjectDao().createOrUpdate(project);
					}
					
					// We save all user_to_project instances received
					Set<UserToProjectDTO> userToProjectDTOs = pullAllSyncResponse.getUserToProjectDTOs();
					for(UserToProjectDTO userToProjectDTO:userToProjectDTOs){
						User user = getHelper().getUserDao().queryForId(userToProjectDTO.getUserId().intValue());
						Project project = getHelper().getProjectDao().queryForId(userToProjectDTO.getProjectId().intValue());
						UserToProjectStatus userToProjectStatus = getHelper().getUserToProjectStatusDao().queryForId(userToProjectDTO.getUserToProjectStatusId().intValue());
						UserToProject userToProject = new UserToProject(user, project, userToProjectStatus, userToProjectDTO);
						getHelper().getUserToProjectDao().createOrUpdate(userToProject);
					}
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing PullAllSync response", e);
				}
			}
		}
	}

}
