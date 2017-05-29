package com.example.gps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/*

http://android-coding.blogspot.com/2011/10/set-latitude-and-longitude-in-exif.html
https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android
http://www.androidauthority.com/get-use-location-data-android-app-625012/
https://github.com/obaro/SimpleLocationApp/blob/master/app/src/main/java/com/sample/foo/simplelocationapp/MainActivity.java
https://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(java.lang.String,%20long,%20float,%20android.location.LocationListener)
https://stackoverflow.com/questions/5280479/how-to-save-gps-coordinates-in-exif-data-on-android
 */


public class MyActivity extends Activity {

    private String filepath = null;
    LocationManager locationManager;
    double longitudeNetwork, latitudeNetwork;
    double longitudeGPS, latitudeGPS;
    Location location;

    TextView textView;
    TextView textView2;

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
            MyActivity.this.location = location;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //longitudeValueGPS.setText(longitudeGPS + "");
                    //latitudeValueGPS.setText(latitudeGPS + "");
                    Toast.makeText(MyActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();
            MyActivity.this.location = location;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //longitudeValueNetwork.setText(longitudeNetwork + "");
                    //latitudeValueNetwork.setText(latitudeNetwork + "");
                    Toast.makeText(MyActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 100, locationListenerGPS);

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 60 * 1000, 100, locationListenerNetwork);

        /*Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d("LOC", "START");
        if (loc != null)
            Log.d("LOC", String.format("lat=%d\nlong=%d", loc.getLatitude(), loc.getLongitude()));
        */
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_coord: {
                if(location == null) {
                    textView.setText("Координаты не доступны");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 100, locationListenerGPS);

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 60 * 1000, 100, locationListenerNetwork);
                }
                else
                    textView.setText(String.format("lat=%f\nlong=%f", location.getLatitude(), location.getLongitude()));
                break;
            }
            case R.id.button_img_choose: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;
            }
            case R.id.button_save_coord: {
                if (filepath != null) {
                    try {

                        String lat_str = ((EditText)findViewById(R.id.editTextLat)).getText().toString();
                        String lng_str = ((EditText)findViewById(R.id.editTextLng)).getText().toString();

                        double lat = 0; //48.3005;
                        try {
                            lat = Double.parseDouble(lat_str);
                        } catch (Exception e) {

                        }
                        double lng = 0; //38.0233;
                        try {
                            Double.parseDouble(lng_str);
                        } catch (Exception e) {

                        }

                        if(location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }

                        String str_latitude = GPS.convert(lat);
                        String str_latitude_ref = GPS.latitudeRef(lat);
                        String str_longitude = GPS.convert(lng);
                        String str_longitude_ref = GPS.longitudeRef(lng);
                        String currentDate = new SimpleDateFormat("yyyy:MM:dd").format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

                        ExifInterface exifInterface = new ExifInterface(filepath);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, currentDate);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, currentTime);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, str_latitude);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, str_latitude_ref);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, str_longitude);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, str_longitude_ref);
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, "NETWORK");
                        exifInterface.saveAttributes();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }

            try {
                String file = data.getData().getPath();//getFileNameByUri(getApplicationContext(), data.getData());
                filepath = file;
                ExifInterface exifInterface = new ExifInterface(file);
                String exif="Exif: "  + file + " \n";
                float[] latlong = new float[2];
                exifInterface.getLatLong(latlong);
                exif += String.format("Coord: lat=%f; lng=%f\n", latlong[0], latlong[1]);
                exif += "\nIMAGE_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                exif += "\nIMAGE_WIDTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                exif += "\n DATETIME: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                exif += "\n TAG_MAKE: " + exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                exif += "\n TAG_MODEL: " + exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                exif += "\n TAG_ORIENTATION: " + exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
                exif += "\n TAG_WHITE_BALANCE: " + exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
                exif += "\n TAG_FOCAL_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                exif += "\n TAG_FLASH: " + exifInterface.getAttribute(ExifInterface.TAG_FLASH);
                exif += "\nGPS related:";
                exif += "\n TAG_GPS_DATESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                exif += "\n TAG_GPS_TIMESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                exif += "\n TAG_GPS_LATITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                exif += "\n TAG_GPS_LATITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                exif += "\n TAG_GPS_LONGITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                exif += "\n TAG_GPS_LONGITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                exif += "\n TAG_GPS_PROCESSING_METHOD: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
                textView2.setText(exif);
                Log.d("EXIF", exif);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                Log.d("DATA", data.getData().toString());
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    public static String getFileNameByUri(Context context, Uri uri)
    {
        // https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
        String fileName="unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content")==0)
        {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                fileName = filePathUri.getLastPathSegment().toString();
            }
        }
        else if (uri.getScheme().compareTo("file")==0)
        {
            fileName = filePathUri.getLastPathSegment().toString();
        }
        else
        {
            fileName = fileName+"_"+filePathUri.getLastPathSegment();
        }
        return fileName;
    }
}

class GPS {
    // https://stackoverflow.com/questions/5280479/how-to-save-gps-coordinates-in-exif-data-on-android
    private static StringBuilder sb = new StringBuilder(20);

    /**
     * returns ref for latitude which is S or N.
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(double latitude) {
        return latitude<0.0d?"S":"N";
    }

    /**
     * returns ref for latitude which is S or N.
     * @param longitude
     * @return S or N
     */
    public static String longitudeRef(double longitude) {
        return longitude<0.0d?"W":"E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     *  79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     * @param latitude could be longitude.
     * @return
     */
    synchronized public static final String convert(double latitude) {
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }
}
