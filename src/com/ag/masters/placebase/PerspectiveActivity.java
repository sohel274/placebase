/**
 * Save the perspective and assemble the last of the story and media objects 
 * to store in the database during confirmation screen to follow
 * 
 * https://code.google.com/p/apics/source/browse/trunk/EE546-Project3/src/android/com/app/aPics.java?r=109
 */
package com.ag.masters.placebase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.ag.masters.placebase.handlers.DateHandler;
import com.ag.masters.placebase.model.CameraView;
import com.ag.masters.placebase.model.Global;
import com.ag.masters.placebase.sqlite.Story;
import com.ag.masters.placebase.sqlite.StoryAudio;
import com.ag.masters.placebase.sqlite.StoryImage;
import com.ag.masters.placebase.sqlite.StoryVideo;

public class PerspectiveActivity extends Activity implements 
	OnClickListener, Camera.PictureCallback, Camera.ShutterCallback, LocationListener, SensorEventListener{

	private CameraView cameraView;
    private LocationManager myLocationManager;

	// device sensor (accelerometer and magnetic field)
	private SensorManager mySensorManager;
	float[] inR = new float[16];
	float[] I = new float[16];
	float[] gravity = new float[3];
	float[] geomag = new float[3];
	float[] orientVals = new float[3];
	double azimuth = 0;
	
    private Story story;
	// only one of these will not be null
	private StoryAudio audio;
	private StoryImage image;
	private StoryVideo video;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perspective);
		
		// get Story and media parcels
		Bundle data = getIntent().getExtras();
		if (data != null) {
			// get the story object
			Story tempStory = data.getParcelable("story");
			if (tempStory != null) {
				// and save into a global var
				story = tempStory;
			}else {
				throw new RuntimeException("SenseActivity: story passed was null");
			}
		}

		if(story != null) {
			// get the correct media object from parcel 
			// based on Story's media parameter
			switch (story.getMedia()) {
			case Global.IMAGE_CAPTURE:
				StoryImage tempImage = data.getParcelable("media");
				if(tempImage != null) {
					image = tempImage;
				} else {
					throw new RuntimeException("SenseActivity: image passed was null");
				}
				break;
			case Global.AUDIO_CAPTURE:
				StoryAudio tempAudio = data.getParcelable("media");
				if(tempAudio != null) {
					audio = tempAudio;
				} else {
					throw new RuntimeException("SenseActivity: audio passed was null");
				}
				break;
			case Global.VIDEO_CAPTURE:
				StoryVideo tempVideo = data.getParcelable("media");
				if(tempVideo != null) {
					video = tempVideo;
				} else {
					throw new RuntimeException("SenseActivity: video passed was null");
				}
				break;
			}
		}
		

		ImageButton pictureButton = (ImageButton) this.findViewById(R.id.btn_takePhoto);
		cameraView = (CameraView) this.findViewById(R.id.view_preview);
		
		pictureButton.setOnClickListener(this);
		
		Criteria myCriteria = new Criteria();
		myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		 //call GPS location manager to get the data from the GPS
        myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		//Register for location updates using a Criteria, and a callback on the specified looper thread.
		myLocationManager.requestLocationUpdates(
				400L, 		// minTime
				1.0f,		// minDistance
				myCriteria,	// criteria
				(LocationListener) this, 		// listener
				null);		// intent
		

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// register sensor listeners
				mySensorManager.registerListener(this, 
						mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
						SensorManager
						.SENSOR_DELAY_NORMAL);
				mySensorManager.registerListener(this, 
						mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
						SensorManager
						.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		myLocationManager.removeUpdates((LocationListener) this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_perspective, menu);
		return true;
	}

	/** 
	 * shutter button clicked
	 */
	@Override
	public void onClick(View v) {
		cameraView.takePicture(this, null, this);
	}

	
	// From the Camera.PictureCallback
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		
		// save the current time to the story object
		DateHandler myDateHandler = new DateHandler();
		story.setTimestamp(myDateHandler.getCurrentTimeAsString());
		
		// save the azimuth to the story
		story.setBearing((float) azimuth);
		
		// Set up content Values to save with the Image on the SD card		
		ContentValues values = new ContentValues();
		
		//GPS 
		Location location = myLocationManager.getLastKnownLocation("gps");
		if(location != null) {
			values.put(Images.Media.LATITUDE, location.getLatitude());
			// save latitude data to Story
			story.setLat(location.getLatitude());
			values.put(Images.Media.LONGITUDE, location.getLongitude());
			// save longitude data to Story
			story.setLng(location.getLongitude());
		} else {
			Log.v("PERSPECTIVE", "GPS NULL");
		}
	    
		
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		// TODO resize this image and save it to the database instead of the raw data.
		
		
		
		// set up the target to save the image
	    Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
	    OutputStream imageFileOS;
	    try {
	     imageFileOS = getContentResolver().openOutputStream(uri);
	     // save the image
	     imageFileOS.write(data);
	     imageFileOS.flush();
	     imageFileOS.close();
	     Log.v(getClass().getSimpleName(), "Image saved: " + uri.toString());
	    } catch (FileNotFoundException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	    } catch (IOException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	    }
	    
	    // grab the filepath of this new file to associate with the story file
	    String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String capturedImageFilePath = cursor.getString(index);
		cursor.close();
		Log.i("IMAGE-CAPTURE", capturedImageFilePath);
		// Store the filepath in StoryImage object
		image.setUri(capturedImageFilePath);
		
		// start new Activity to confirm capture
		Intent intent = new Intent(this, ConfirmTrace.class);
		intent.putExtra("story", story);
		switch(story.getMedia()) {
		case Global.IMAGE_CAPTURE:
			intent.putExtra("media", image);
			break;
		case Global.VIDEO_CAPTURE:
			intent.putExtra("media", video);
			break;
		case Global.AUDIO_CAPTURE:
			intent.putExtra("media", audio);
			break;
		}
		startActivity(intent);
	}
	
	// From Camera.ShutterCallback
	@Override
	public void onShutter() {
		// play sound
		// store bearing
		Log.i(getClass().getSimpleName(), "SHUTTER CALLBACK");
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// http://stackoverflow.com/questions/4819626/android-phone-orientation-overview-including-compass 
				// http://stackoverflow.com/questions/4020048/finding-orientation-using-getrotationmatrix-and-getorientation?rq=1
				// If the sensor data is unreliable return
				//if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
				//return;

				// Gets the value of the sensor that has been changed
				switch (event.sensor.getType()) {  
				case Sensor.TYPE_ACCELEROMETER:
					gravity = event.values.clone();
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					geomag = event.values.clone();
					break;
				}
				// If gravity and geomag have values then find rotation matrix
				if (gravity != null && geomag != null) {

					// checks that the rotation matrix is found
					boolean success = SensorManager.getRotationMatrix(inR, I, gravity, geomag);
					if (success) {
						SensorManager.getOrientation(inR, orientVals);
						azimuth = Math.toDegrees(orientVals[0]);
						
						// compensate for different screen orientations
						Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
						int compensation = display.getRotation() * 90;                          
						azimuth = azimuth+compensation;

						//TODO: you might need this to get readings that align correctly with the users
						//setDeviceBearing(azimuth);

					}
				}
		
	}


}