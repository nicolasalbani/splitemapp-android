package com.splitemapp.android.utils;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class ImageUtils {

	public static final int IMAGE_QUALITY_MIN = 10;
	public static final int IMAGE_QUALITY_MED = 50;
	public static final int IMAGE_QUALITY_MAX = 100;
	public static final float IMAGE_QUALITY_FACTOR = 3;

	/**
	 * Decodes a Bitmap based on the specified parameters
	 * @param filePath String containing the full path to the image file
	 * @param imageQuality int containing the image quality, it goes from 1 to 100
	 * @return Bitmap with the decoded and scaled image
	 */
	public static Bitmap decodeScaledBitmap(String filePath, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * Calculates the sample size to be used in the Bitmap decoding from the file system
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;

		// Calculate sample size based on ratio between raw and requested sizes
		int inSampleSize = Math.round((float) (height*width) / (float) (reqHeight*reqWidth) / IMAGE_QUALITY_FACTOR);

		return inSampleSize;
	}

	/**
	 * Returned a Bitmap containing the circle cropped version of the bitmap parameter
	 * @param bitmap Bitmap to be circle cropped
	 * @return Bitmap with the circle cropped image
	 */
	public static Bitmap getCroppedBitmap(Bitmap bitmap) {
		// Making the bitmap a square image
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		if(width < height){
			height = width;
		} else {
			width = height;
		}

		// Creating the output bitmap
		Bitmap output = Bitmap.createBitmap(width,height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, width, height);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawCircle(width/2, height/2, width/2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * Returns a boolean indicating whether the image view contains the specified resource
	 * @param context
	 * @param imageView
	 * @param resource
	 * @return
	 */
	public static boolean imageViewEqualsResource(Context context, ImageView imageView, int resource){
		return imageView.getDrawable().getConstantState().equals(ContextCompat.getDrawable(context, resource).getConstantState());
	}

	/**
	 * Return a byte array matching the Bitmap object provided
	 * @param image
	 * @param quality
	 * @return
	 */
	public static byte[] bitmapToByteArray(Bitmap image, int imageQuality){
		// Adjusting image quality
		imageQuality = getValidImageQuality(imageQuality);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, imageQuality, byteArrayOutputStream);
		byte[] img = byteArrayOutputStream.toByteArray();

		return img;
	}

	/**
	 * Return a bitmap matching the provided byte array
	 * @param image byte array containing the image information
	 * @param imageQuality from 1 to 100 percent
	 * @return
	 */
	public static Bitmap byteArrayToBitmap(byte[] image, int imageQuality){
		// Adjusting imageQuality input
		imageQuality = getValidImageQuality(imageQuality);

		// Obtaining scaled image
		Bitmap bitmap = BitmapFactory.decodeByteArray(image , 0, image.length);
		bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * imageQuality / 100 , bitmap.getHeight() * imageQuality / 100, true);

		return bitmap;
	}

	/**
	 * Returns a valid image quality based on the provided parameter
	 * @param imageQuality
	 * @return
	 */
	private static int getValidImageQuality(int imageQuality){
		if(imageQuality < IMAGE_QUALITY_MIN){
			imageQuality = IMAGE_QUALITY_MIN;
		} else if (imageQuality > IMAGE_QUALITY_MAX){
			imageQuality = IMAGE_QUALITY_MAX;
		}

		return imageQuality;
	}

	/**
	 * Return a byte array matching the ImageView object provided
	 * @param imageView
	 * @param imageQuality
	 * @return
	 */
	public static byte[] imageViewToByteArray(ImageView imageView, int imageQuality){
		Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
		return bitmapToByteArray(bitmap, imageQuality);
	}
	
	/**
	 * Calculates the provided number into DP units
	 * @param context
	 * @param dp
	 * @return
	 */
	public static int calculateDpUnits(Context context, float dp){
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float fpixels = metrics.density * dp;
		return (int) (fpixels + 0.5f);
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
}
