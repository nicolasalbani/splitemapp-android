package com.splitemapp.android.utils;

import java.io.ByteArrayOutputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class ImageUtils {
	
	private static final int COMPRESSION_QUALITY = 10;
	
	/**
	 * Decodes a Bitmap based on the specified parameters
	 * @param filePath String containing the full path to the image file
	 * @param reqWidth int specifying the required width
	 * @param reqHeight int specifying the required height
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
	 * @param resources
	 * @param imageView
	 * @param resource
	 * @return
	 */
	public static boolean imageViewEqualsResource(Resources resources, ImageView imageView, int resource){
		return imageView.getDrawable().getConstantState().equals(resources.getDrawable(resource).getConstantState());
	}
	
	/**
	 * Return a byte array matching the Bitmap object provided
	 * @param image
	 * @return
	 */
	public static byte[] bitmapToByteArray(Bitmap image){
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
		byte[] img = byteArrayOutputStream.toByteArray();
		
		return img;
	}
	
	/**
	 * Return a byte array matching the ImageView object provided
	 * @param imageView
	 * @return
	 */
	public static byte[] imageViewToByteArray(ImageView imageView){
		Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
		return bitmapToByteArray(bitmap);
	}
	
	/**
	 * Return a bitmap matching the provided byte array
	 * @param image byte array containing the image information
	 * @param scale from 1 to 100 percent
	 * @return
	 */
	public static Bitmap byteArrayToBitmap(byte[] image, int scale){
		// Adjusting scale input
		if(scale < 1){
			scale = 1;
		} else if (scale > 100){
			scale = 100;
		}
		
		// Obtaining scaled image
		Bitmap bitmap = BitmapFactory.decodeByteArray(image , 0, image.length);
		bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * scale / 100 , bitmap.getHeight() * scale / 100, true);
		
		return bitmap;
	}
}
