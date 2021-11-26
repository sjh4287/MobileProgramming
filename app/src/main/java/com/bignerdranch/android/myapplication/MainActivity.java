package com.bignerdranch.android.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    ViewFlipper vFlipper; //메인화면과 즐겨찾기화면 전환할 뷰플리퍼
    int FlipperCount = 0;//뷰플리퍼가 순환되지 않게 하기 위한 변수
    EditText ETStart, ETFinish; //출발지와 도착지 입력받을 EditText
    Button btnStart, btnFinish; //출발지와 도착지 입력후 이벤트 처리할 버튼
    SupportMapFragment mapFragment; //지도보여줄 프래그먼트
    GoogleMap mMap;// 구글지도
    TextView fav;
    private Marker currentMarker = null;


    ////////////////////////Json 파싱///////////////////////////////

    /*JSONArray jsonArray;
    String url = "http://api.nubija.com:1577/"; //누비자 api url
    Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();

    Terminal term = retrofit.create(Terminal.class);
    Call<TerminalInfo> call = term.getPosts("1");

    call.enqueue(new Callback<TerminalInfo>)*/


    ///////////////////////////////////////////////////////////////

    String[] PERMISSIONS = { //위치권한 배열
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    int DEFAULT_ZOOM = 16; //디폴트 줌
    LatLng CITY_HALL = new LatLng(35.245595, 128.6897643); //디폴트 위치값 (창원대학교)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("메인");

        ///////////////////Json 파싱 2/////////////////////////////////////////////
        /*
        try {
            InputStream is = new URL("http://api.nubija.com:1577/ubike/nubijaInfoApi.do?apikey=kTKnDZYpryizkfmPsCyu")
                                                                                                            .openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str ="";
            StringBuffer buffer = new StringBuffer();
            while((str = rd.readLine()) != null) {
                buffer.append(str);
            }
            fav.setText(str);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }

        fav = (TextView) findViewById(R.id.fav); */

        ///////////////////////////////////////////////////////////////////////

        ETStart = (EditText) findViewById(R.id.ETStart);
        ETFinish = (EditText) findViewById(R.id.ETFinish);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnFinish = (Button) findViewById(R.id.btnFinish);
        vFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);


        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mapView, mapFragment)
                .commit();

        if (checkPermission()) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PackageManager.PERMISSION_GRANTED);
        }

        btnStart.setOnClickListener(new View.OnClickListener() { //출발지 입력 버튼
            @Override
            public void onClick(View view) {
                if(ETStart.getText().toString().length() > 0) {
                    Location location = getLocationFromAddress(getApplicationContext(), ETStart.getText().toString());
                    LatLng start = new LatLng(location.getLatitude(),location.getLongitude());
                    AddMarker(start,"출발지","사용자가 입력한 출발지"); // 위치 입력한 곳에 마커찍힘 (삭제예정)
                }
            }
        });
    }


    private Location getLocationFromAddress(Context context, String address) { //검색한 장소 위치값 가져오기
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        Location resLocation = new Location("");
        try {
            addresses = geocoder.getFromLocationName(address, 5);
            if((addresses == null) || (addresses.size() == 0)) {
                return null;
            }
            Address addressLoc = addresses.get(0);

            resLocation.setLatitude(addressLoc.getLatitude());
            resLocation.setLongitude(addressLoc.getLongitude());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resLocation;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu1, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){ //메뉴 선택되었을 때

        switch(item.getItemId()) {
            case R.id.main:
                setTitle("메인");
                if(FlipperCount != 0) {
                    vFlipper.showPrevious();
                    FlipperCount = 0;
                }
                return true;
            case R.id.favorite:
                setTitle("즐겨찾기");
                if(FlipperCount != 1) {
                    vFlipper.showNext();
                    FlipperCount = 1;
                }
                return true;
        }
        return false;
    }

    private boolean checkPermission() {

        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if(checkPermission()) {
            googleMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(),15));
        }
        else
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM));
        //googleMap.setOnInfoWindowClickListener((GoogleMap.OnInfoWindowClickListener) this);
    }

    public void AddMarker(LatLng latLng, String title, String snippet) { //누비자정류장 마커 추가(위치, 정류장 이름, snippet)
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.snippet(snippet);
        mMap.addMarker(markerOptions);
    }

    ////////////////////다이얼로그 추가///////////////////////

    ///////////////////////////////////////////////////////


    public LatLng getMyLocation() { //나의 위치 찾기

        String locationProvider = LocationManager.GPS_PROVIDER;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new LatLng(35.245595,128.6897643);
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        return new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}