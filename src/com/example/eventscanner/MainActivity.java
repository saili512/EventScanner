package com.example.eventscanner;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity.java";
	public static final String lang = "eng";
	protected EditText _field;

	int TAKE_PHOTO_CODE = 0;
	public static int count = 0;
	private static int RESULT_LOAD_IMAGE = 1;

	LocationParser lp = new LocationParser();
	DateParser dp = new DateParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// here,we are making a folder named picFolder to store pics taken by
		// the camera using this application
		final String dir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				+ "/picFolder/";
		File newdir = new File(dir);
		newdir.mkdirs();

		ImageButton capture = (ImageButton) findViewById(R.id.btnCapture);
		ImageButton openf = (ImageButton) findViewById((R.id.button));
		openf.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_IMAGE);

			}
		});

		capture.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// here,counter will be incremented each time,and the picture
				// taken by camera will be stored as 1.jpg,2.jpg and likewise.
				count++;
				String file = dir + count + ".jpg";
				File newfile = new File(file);
				try {
					newfile.createNewFile();
				} catch (IOException e) {
					Log.v(TAG, "File creation failed!");
				}

				Uri outputFileUri = Uri.fromFile(newfile);

				Intent cameraIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

				startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
			}
		});
		// _field = (EditText) findViewById(R.id.field);
		// readImage();
	}

	private String readImage(String _path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		Log.v(TAG, "Before baseApi");

		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init("/storage/emulated/0/", "eng");
		baseApi.setImage(bitmap);

		String recognizedText = baseApi.getUTF8Text();

		baseApi.end();

		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if (lang.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9:/]+", " ");
		}

		recognizedText = recognizedText.trim();

		Log.v(TAG, "Edited OCRED TEXT: " + recognizedText);

		if (recognizedText.length() != 0) {
			/*_field.setText(_field.getText().toString().length() == 0 ? recognizedText
					: _field.getText() + " " + recognizedText);
			_field.setSelection(_field.getText().toString().length());*/
		}

		return recognizedText;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
			Log.v(TAG, "Pic saved");
		} else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			//_field = (EditText) findViewById(R.id.editText1);
			String recognizedText = readImage(picturePath);

			lp.initialize();
			String location = lp.returnLocation(recognizedText); // get location
			Log.v(TAG, "Location:" + location);
			dp.initialize();
			Date[] dates = dp.parseDate(recognizedText);
			addCalendarEvent(location, dates[0], dates[1]);
			Log.v(TAG, "Dates:" + dates[0] + " " + dates[1]);
			
			 BitmapFactory.Options options = new BitmapFactory.Options();
			 options.inSampleSize = 4; 
			 ImageView imageView = (ImageView) findViewById(R.id.imgView);
			 imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath,options));
			 

		} else {
			Log.v(TAG, "User cancelled");
		}
	}

	public void addCalendarEvent(String location, Date startDate, Date endDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("beginTime", cal.getTimeInMillis());
		intent.putExtra("allDay", false);
		//intent.putExtra("rrule", "FREQ=YEARLY");
		cal.setTime(endDate);
		intent.putExtra("endTime", cal.getTimeInMillis());
		intent.putExtra("title", "Title");
		intent.putExtra("description", "Description");
		intent.putExtra("eventLocation", location);
		startActivity(intent);
	}
}
