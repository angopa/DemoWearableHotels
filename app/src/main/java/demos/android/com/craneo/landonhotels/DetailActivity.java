package demos.android.com.craneo.landonhotels;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int ERROR_DIALOG_REQUEST = 901;
    private static final int ZOOM_VALUE = 15;
    private Hotel hotel;

    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_detail);

        if(servicesOk()){
            setContentView(R.layout.activity_detail);
            initMap();
            Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
        }else{
            setContentView(R.layout.activity_detail);
            Toast.makeText(this, "Map no connected", Toast.LENGTH_SHORT).show();
        }

        String city = getIntent().getStringExtra("city");

        if(city == null){
            Bundle remoteInput = RemoteInput.getResultsFromIntent(getIntent());
            if (remoteInput != null){
                city = remoteInput.getCharSequence(MainActivity.EXTRA_VOICE_REPLY).toString();
            }
        }

        setTitle("Landon Hotel"+", "+city);
        hotel = DataProvider.hotelMap.get(city);

        if(hotel == null){
            Toast.makeText(this, "Can't find hotel: "+city, Toast.LENGTH_SHORT).show();
        }else{
            displayHotelDetails();
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean servicesOk(){
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS){
            return true;
        }else if (googleAPI.isUserResolvableError(isAvailable)){
            googleAPI.getErrorDialog(this, isAvailable,  ERROR_DIALOG_REQUEST).show();
        }else{
            Toast.makeText(this, "Can't connect to mapping service", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void displayHotelDetails(){
        TextView cityText = (TextView)findViewById(R.id.cityText);
        cityText.setText(hotel.getCity());

        TextView neighborhoodText = (TextView)findViewById(R.id.neighborhood);
        neighborhoodText.setText(hotel.getNeighborhood());

        TextView descriptionText = (TextView)findViewById(R.id.description);
        descriptionText.setText(hotel.getDescription()+"\n");
    }

    public void sendNotification(View view){
        String text = "Visit Landon Hotel in "+ hotel.getCity()+"!\n\n"+hotel.getDescription();
        NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(text);

        int backgroundId = getResources().getIdentifier(
                hotel.getImage(), "drawable", getPackageName());
        Bitmap background = BitmapFactory.decodeResource(
                getResources(),backgroundId);

        NotificationCompat.BigTextStyle secondPageStyle =
                new NotificationCompat.BigTextStyle();
        secondPageStyle.bigText(getText(R.string.lorem_ipsum));

        Notification secondPage =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Page 2")
                        .setStyle(secondPageStyle)
                        .build();

        NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .addPage(secondPage)
                        .setBackground(background);

        Uri uri = Uri.parse("geo:0,0?q="+hotel.getCity());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(uri);

        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(this, 0,mapIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_notify)
                .addAction(R.drawable.ic_action_map, "Map", mapPendingIntent)
                .extend(extender);

        int notificationId = 1;
        NotificationManagerCompat nmc =
                NotificationManagerCompat.from(this);
        nmc.notify(notificationId, builder.build());


    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        try {
            geoLocale();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void geoLocale() throws IOException{
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = geocoder.getFromLocationName(hotel.getAddress().toString(),1);

        if (list.size()>0){
            Address add = list.get(0);
            double lat = add.getLatitude();
            double lng = add.getLongitude();

            gotoLocation(lat, lng, ZOOM_VALUE);
        }

    }

    private void gotoLocation(double lat, double lng, int zoomValue) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoomValue);
        mMap.moveCamera(update);
    }
}
