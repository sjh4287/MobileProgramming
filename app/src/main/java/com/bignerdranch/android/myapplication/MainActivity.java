package com.bignerdranch.android.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    ViewFlipper vFlipper; //메인화면과 즐겨찾기화면 전환할 뷰플리퍼
    int FlipperCount = 0;//뷰플리퍼가 순환되지 않게 하기 위한 변수
    EditText ETStart, ETFinish; //출발지와 도착지 입력받을 EditText
    Button btnStart, btnFinish, w; //출발지와 도착지 입력후 이벤트 처리할 버튼
    SupportMapFragment mapFragment; //지도보여줄 프래그먼트
    GoogleMap mMap;// 구글지도
    TextView fav;
    Marker []markers = new Marker[300];
    int MarkerCount = 0;
    int StartCount = 0, FinishCount = 0;
    int Count = 0;//Start 먼저하면 0 Finish 먼저하면 1
    private Marker currentMarker = null;
    
    
    ///여기에 전역변수 생성, 받아온 JSON의 데이터를 처리할거 
    TerminalInfo[] terminalList;


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

        String url = "http://api.nubija.com:1577/"; //누비자 api url
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();

        TermInterface term = retrofit.create(TermInterface.class);
        Call<Terminal> call = term.getPosts(); //java.lang.IllegalArgumentException

           call.enqueue(new Callback<Terminal>() {
            @Override
            public void onResponse(Call<Terminal> call, Response<Terminal> response) {
                if (response.body() != null) {
                    //터미널 리스트라는 배열에다가 받아온 터미널의 터미널정보 배열을 저장 
                    //애초에 터미널 객체가 잘못되어있음 JSON 자체의 형식에 안맞아서 못가져옴 
                    terminalList = response.body().getTerminalInfo();
                    for (int i = 0; i < terminalList.length; i ++) {

                        Log.d("받아온 정거장 번호", terminalList[i].getVno());  //각 배열 요소에서 Vno만 뽑아서 출력 
                    }
                    FirstAddMarkers();

                }
                else {

                }
            }
            public void onFailure(Call<Terminal> call, Throwable t) {
                   Log.d("왜 실패함", String.valueOf(t));
               }



        });

        ETStart = (EditText) findViewById(R.id.ETStart);
        ETFinish = (EditText) findViewById(R.id.ETFinish);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnFinish = (Button) findViewById(R.id.btnFinish);
        vFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
        fav = (TextView) findViewById(R.id.fav);
        //w=(Button)findViewById(R.id.btn);

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
                    if (location == null) {
                        Toast.makeText(getApplicationContext(), "위치를 다시 입력하세요", Toast.LENGTH_LONG).show();
                    } else {
                        if (MarkerCount > 2) { //모든 정류장 마커들이 찍혀있을 때
                            RemoveAllMarkers();
                        } else if (StartCount == 1) { //이미 StartMarker가 찍혀있을 때 해당 마커 삭제
                            markers[0].remove();
                            MarkerCount--;
                            StartCount = 0;
                        }
                        LatLng start = MinLocation(location); //가장 가까운 터미널의 위치
                        //AddMarker(start,"출발지","사용자가 입력한 출발지");

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(start);
                        markerOptions.title("출발지");
                        markerOptions.snippet("사용자가 입력한 출발지");

                        markers[0] = mMap.addMarker(markerOptions);

                        MarkerCount++;
                        StartCount = 1;
                    }
                }
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(ETFinish.getText().toString().length() > 0) {
                    Location location = getLocationFromAddress(getApplicationContext(), ETFinish.getText().toString());
                    if (location == null) {
                        Toast.makeText(getApplicationContext(),"위치를 다시 입력하세요",Toast.LENGTH_LONG).show();
                    } else {
                        if (MarkerCount > 2) { //모든 정류장 마커들이 찍혀있을 때
                            RemoveAllMarkers();
                        } else if (FinishCount == 1) { //이미 StartMarker가 찍혀있을 때 해당 마커 삭제
                            markers[1].remove();
                            MarkerCount--;
                            FinishCount = 0;
                        }
                        LatLng finish = MinLocation(location); //가장 가까운 터미널의 위치
                        //AddMarker(finish,"도착지","사용자가 입력한 도착지");

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(finish);
                        markerOptions.title("도착지");
                        markerOptions.snippet("사용자가 입력한 도착지");

                        markers[0] = mMap.addMarker(markerOptions);

                        MarkerCount++;
                        FinishCount = 1;
                    }
                }
            }
        });

       /*
        w.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });*/

    }

    public void FirstAddMarkers(){
        for(int i = 0; i<terminalList.length; i++){
            AddMarker(makeLatLng(terminalList[i].getLatitude(),terminalList[i].getLongitude()),terminalList[i].getTmname(),"정류장");
            markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            //markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.H));
        }
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

    public LatLng makeLatLng(String Lat, String Lng){ //String의 위도 경도를 double로 형변환하여 LatLng 객체 생성
        double dLat = Double.parseDouble(Lat);
        double dLng = Double.parseDouble(Lng);
        return new LatLng(dLat,dLng);
    }

    public void AddMarker(LatLng latLng, String title, String snippet) { //누비자정류장 마커 추가(위치, 정류장 이름, snippet)
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.snippet(snippet);

        markers[MarkerCount] = mMap.addMarker(markerOptions);
        MarkerCount++;
    }


    public void RemoveAllMarkers(){
        for(int i = 0;i < MarkerCount; i++){
            markers[i].remove();
        }
        MarkerCount = 0;
    }

    public void RemoveMarker(LatLng latLng){

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

    public LatLng MinLocation(Location loc){
        double locLat = loc.getLatitude();
        double locLng = loc.getLongitude();
        double temp;
        int result = 0;
        double min = 0;
        for(int i = 0; i<terminalList.length; i++){
            double dLat = Double.parseDouble(terminalList[i].getLatitude());
            double dLng = Double.parseDouble(terminalList[i].getLongitude());
            temp = Math.sqrt(Math.abs(locLat - dLat)*Math.abs(locLat - dLat) + Math.abs(locLng - dLng)*Math.abs(locLng - dLng));
            if(min == 0){
                min = temp;
                result = i;
            } else if(min > temp){
                min = temp;
                result = i;
            }
        }
        return new LatLng(Double.parseDouble(terminalList[result].getLatitude()),Double.parseDouble(terminalList[result].getLongitude()));
    }

}
