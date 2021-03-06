package com.bignerdranch.android.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Criteria;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    ViewFlipper vFlipper; //메인화면과 즐겨찾기화면 전환할 뷰플리퍼
    EditText ETStart, ETFinish; //출발지와 도착지 입력받을 EditText
    Button btnStart, btnFinish, btnAll; //출발지와 도착지 입력후 이벤트 처리할 버튼
    SupportMapFragment mapFragment; //지도보여줄 프래그먼트
    GoogleMap mMap;// 구글지도
    ListView listView; //즐겨찾기 리스트뷰

    myDBHelper myHelper;
    SQLiteDatabase sqlDB; //SQLite 데이터베이스

    Marker []markers = new Marker[300]; //마커 객체배열
    int MarkerCount = 0; //마커의 개수 변수
    int StartCount = 0, FinishCount = 0; //출발지 마커와 도착지 마커의 개수 변수
    int FlipperCount = 0;//뷰플리퍼가 순환되지 않게 하기 위한 변수

    TerminalInfo[] terminalList; //받아온 터미널의 정보들이 담긴 배열

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

        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl(url)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build();

        TermInterface term = retrofit.create(TermInterface.class);
        Call<Terminal> call = term.getPosts();

           call.enqueue(new Callback<Terminal>() {
            @Override
            public void onResponse(Call<Terminal> call, Response<Terminal> response) {
                if (response.body() != null) {
                    //터미널 리스트라는 배열에다가 받아온 터미널의 터미널정보 배열을 저장
                    terminalList = response.body().getTerminalInfo();
                    for (int i = 0; i < terminalList.length; i ++) {
                        Log.d("받아온 정거장 번호 :", terminalList[i].getVno());  //각 배열 요소에서 Vno만 뽑아서 출력
                    }
                }
            }
            public void onFailure(Call<Terminal> call, Throwable t) {
                   Log.d("실패 : ", String.valueOf(t));
               }
        });

        ETStart = (EditText) findViewById(R.id.ETStart); // 출발지 입력 EditText
        ETFinish = (EditText) findViewById(R.id.ETFinish); // 도착지 입력 EditText
        btnStart = (Button) findViewById(R.id.btnStart); // 출발지 입력 버튼
        btnFinish = (Button) findViewById(R.id.btnFinish); // 도착지 입력 버튼
        btnAll = (Button) findViewById(R.id.btnAll); // 모든 정류장 출력 버튼
        vFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1); // 뷰플리퍼
        listView = (ListView) findViewById(R.id.listView); // 즐겨찾기 리스트뷰

        myHelper = new myDBHelper(this);

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

                        }
                        LatLng start = MinLocation(location); //가장 가까운 터미널의 위치
                        //AddMarker(start,"출발지","사용자가 입력한 출발지");
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(start);

                        int index = 0;
                        for(int i = 0; i < terminalList.length; i++){
                            if(start.latitude == Double.parseDouble(terminalList[i].getLatitude()) && start.longitude == Double.parseDouble(terminalList[i].getLongitude())){
                                markerOptions.title(terminalList[i].getVno());
                                index = i;
                            }
                        }

                        markers[0] = mMap.addMarker(markerOptions);

                        if((SetMarkerColor(terminalList[index].getEmptycnt(), terminalList[index].getParkcnt()) == 0)){ // 노란색
                            markers[0].setIcon(BitmapDescriptorFactory.defaultMarker(60));
                        } else if((SetMarkerColor(terminalList[index].getEmptycnt(), terminalList[index].getParkcnt()) == 1)){ //빨간색
                            markers[0].setIcon(BitmapDescriptorFactory.defaultMarker());
                        } else if((SetMarkerColor(terminalList[index].getEmptycnt(), terminalList[index].getParkcnt()) == 2)) { //남색
                            markers[0].setIcon(BitmapDescriptorFactory.defaultMarker(240)); //hue 색상표보고 2가지 추가하기
                        }

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
                        } else if (FinishCount == 1) { //이미 FinishMarker가 찍혀있을 때 해당 마커 삭제
                            markers[1].remove();
                            MarkerCount--;

                        }
                        LatLng finish = MinLocation(location); //가장 가까운 터미널의 위치
                        int index = 0;
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(finish);
                        for(int i = 0; i < terminalList.length; i++){
                            if(finish.latitude == Double.parseDouble(terminalList[i].getLatitude()) && finish.longitude == Double.parseDouble(terminalList[i].getLongitude())){
                                markerOptions.title(terminalList[i].getVno());
                                index = i;
                            }
                        }

                        markers[1] = mMap.addMarker(markerOptions);

                        if((SetMarkerColor(terminalList[index].getEmptycnt(), terminalList[index].getParkcnt()) == 0)){ // 노란색
                            markers[1].setIcon(BitmapDescriptorFactory.defaultMarker(60));
                        } else if((SetMarkerColor(terminalList[index].getEmptycnt(), terminalList[index].getParkcnt()) == 1)){ //빨간색
                            markers[1].setIcon(BitmapDescriptorFactory.defaultMarker());
                        } else if((SetMarkerColor(terminalList[index].getEmptycnt(), terminalList[index].getParkcnt()) == 2)) { //남색
                            markers[1].setIcon(BitmapDescriptorFactory.defaultMarker(240)); //hue 색상표보고 2가지 추가하기
                        }

                        MarkerCount++;
                        FinishCount = 1;
                    }
                }
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() { //모든 마커 출력 버튼을 눌렀을 때
            @Override
            public void onClick(View v) { //현재 지도위의 모든 마커를 삭제하고 새로 마커들을 출력한다.
                RemoveAllMarkers();
                FirstAddMarkers();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mapFragment.getMapAsync(this);  //지도 실행
    }

    public void FirstAddMarkers(){ //지도에 모든 정류장의 위치에 마커들을 표시한다.
        for(int i = 0; i<terminalList.length; i++){ //terminalList의 길이(터미널의 개수)만큼 반복
            AddMarker(makeLatLng(terminalList[i].getLatitude(),terminalList[i].getLongitude()),terminalList[i].getVno());

        } for(int i = 0; i < terminalList.length; i++){

            if((SetMarkerColor(terminalList[i].getEmptycnt(), terminalList[i].getParkcnt()) == 0)){ // 노란색
                markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(60));
            } else if((SetMarkerColor(terminalList[i].getEmptycnt(), terminalList[i].getParkcnt()) == 1)){ //빨간색
                markers[i].setIcon(BitmapDescriptorFactory.defaultMarker());
            } else if((SetMarkerColor(terminalList[i].getEmptycnt(), terminalList[i].getParkcnt()) == 2)) { //남색
                markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(240)); //hue 색상표보고 2가지 추가하기
            }

        }
    }

    private Location getLocationFromAddress(Context context, String address) { //검색한 장소 위치값 가져오기
        Geocoder geocoder = new Geocoder(context); //지오코더 객체 생성
        List<Address> addresses; //Address 리스트 생성
        Location resLocation = new Location("");
        try {
            addresses = geocoder.getFromLocationName(address, 5); //매개변수 address의 검색된 결과 최대 5개까지 addresses 리스트에 삽입
            if((addresses == null) || (addresses.size() == 0)) { // 검색한 장소가 없을 때
                return null;
            }
            Address addressLoc = addresses.get(0); //리스트의 첫 인덱스

            resLocation.setLatitude(addressLoc.getLatitude()); //위도설정
            resLocation.setLongitude(addressLoc.getLongitude()); //경도설정

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resLocation; //Location 반환
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
                if(FlipperCount != 0) { //메인일때 메인을 선택하면 즐겨찾기로 넘어가는것 방지
                    vFlipper.showPrevious();
                    FlipperCount = 0;
                }
                return true;
            case R.id.favorite:
                setTitle("즐겨찾기");
                if(FlipperCount != 1) { //즐겨찾기일때 즐겨찾기를 선택하면 메인으로 넘어가는것 방지
                    vFlipper.showNext();
                    FlipperCount = 1;
                }
                String text = "", Vno = "", tName ="", tPark ="", tEmpty ="";
                sqlDB = myHelper.getReadableDatabase();
                Cursor cursor;
                cursor = sqlDB.rawQuery("SELECT * FROM termTBL;", null);

                List<String> list = new ArrayList<>();
                while(cursor.moveToNext()){
                    Vno = cursor.getString(0);

                    for(int i = 0; i < terminalList.length; i++){
                        if(Vno.equals(terminalList[i].getVno())){
                            tName = terminalList[i].getTmname();
                            tPark = terminalList[i].getParkcnt();
                            tEmpty = terminalList[i].getEmptycnt();
                            break;
                        }
                    }
                    list.add(tName +"\n" + "\n대여 가능한 자전거수: " + tPark + "\n반납 가능한 자전거수: " + tEmpty);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);

                cursor.close();
                sqlDB.close();
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
    public void onMapReady(@NonNull GoogleMap googleMap) { //지도 준비되면 호출됨
        mMap = googleMap;

        if(checkPermission()) {
            googleMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(),15));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM));
        }
        mMap.setOnMarkerClickListener(this);
    }

    public LatLng makeLatLng(String Lat, String Lng){ //String의 위도 경도를 double로 형변환하여 LatLng 객체 생성
        double dLat = Double.parseDouble(Lat);
        double dLng = Double.parseDouble(Lng);
        return new LatLng(dLat,dLng);
    }

    public void AddMarker(LatLng latLng, String title) { //누비자정류장 마커 추가(위치, 정류장 이름)
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);

        markers[MarkerCount] = mMap.addMarker(markerOptions);

        MarkerCount++;
    }


    public int SetMarkerColor(String empty, String park){ //자전거 개수에 따른 마커색상 설정 0 : 노란색, 1 : 빨간색, 2 : 남색
        int emptyCnt = Integer.parseInt(empty);
        int parkCnt = Integer.parseInt(park);

        if(emptyCnt == 0){ // 자전거가 꽉차 있을 때
            return 1;
        }else if(parkCnt == 0){ // 자전거가 텅 비어 있을 때
            return 2;
        }else{ // 그외 모든 경우
            return 0;
        }
    }

    public void RemoveAllMarkers(){
        for(int i = 0;i < MarkerCount; i++){
            markers[i].remove();
        }
        MarkerCount = 0;
    }


    public LatLng getMyLocation() { //나의 위치 찾기

        String locationProvider = LocationManager.GPS_PROVIDER;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new LatLng(35.245595,128.6897643);
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        if (lastKnownLocation == null) { //공기계일때 최근 위치값이 null인경우 발생 예외 처리
            final double[] loc = new double[2];
            LocationListener locationListener = location -> {
                loc[0] = location.getLatitude();
                loc[1] = location.getLongitude();
            };

            locationManager.requestLocationUpdates(String.valueOf(locationManager.getBestProvider(new Criteria(), true)), 1000, 0, locationListener);
            return new LatLng(loc[0], loc[1]);
        }

        return new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    public LatLng MinLocation(Location loc){ //최소 거리 구하기
        double locLat = loc.getLatitude();
        double locLng = loc.getLongitude();
        double temp;
        int result = 0;
        double min = 0;
        for(int i = 0; i<terminalList.length; i++){
            double dLat = Double.parseDouble(terminalList[i].getLatitude());
            double dLng = Double.parseDouble(terminalList[i].getLongitude());
            temp = Math.sqrt(Math.abs(locLat - dLat)*Math.abs(locLat - dLat) + Math.abs(locLng - dLng)*Math.abs(locLng - dLng)); //피타고라스 이용해서 두 점사이의 거리 구하기
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


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) { //마커 클릭했을 때 호출

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle(getName(marker));
        dlg.setMessage("대여 가능한 자전거 수: " + getPark(marker)+ "\n" + "반납 가능한 자전거 수: " + getEmpty(marker));
        dlg.setPositiveButton("확인",null);

        if(isValid(marker.getTitle())){ //즐겨찾기 된 마커를 클릭했을 때
            dlg.setNegativeButton("즐겨찾기 삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    sqlDB = myHelper.getWritableDatabase();
                    sqlDB.execSQL("DELETE FROM termTBL WHERE Vno = '"
                    + marker.getTitle()+"';");
                    Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다.", Toast.LENGTH_LONG).show();
                    sqlDB.close();

                }
            });
        } else { //즐겨찾기가 되지 않은 마커를 클릭했을 때
            dlg.setNegativeButton("즐겨찾기 추가", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    sqlDB = myHelper.getWritableDatabase();
                    sqlDB.execSQL("INSERT INTO termTBL VALUES ( '"
                    + marker.getTitle() + "');");
                    sqlDB.close();
                    Toast.makeText(getApplicationContext(), "즐겨찾기가 추가되었습니다.", Toast.LENGTH_LONG).show();

                }
            });

        }
        dlg.show();
        return true;
    }

    public String getName(Marker marker){ // 마커의 Title은 Vno
        int Vno = Integer.parseInt(marker.getTitle());
        String result = "";

        for(int i = 0; i < terminalList.length; i++){
            if(Vno == Integer.parseInt(terminalList[i].getVno())){
                result = terminalList[i].getTmname();
                break;
            }
        }
        return result;
    }

    public int getEmpty(Marker marker){// 마커의 Title은 Vno
        int Vno = Integer.parseInt(marker.getTitle());
        int result = 0;

        for(int i = 0; i < terminalList.length; i++){
            if(Vno == Integer.parseInt(terminalList[i].getVno())){
                result =Integer.parseInt(terminalList[i].getEmptycnt());
                break;
            }
        }
        return result;
    }

    public int getPark(Marker marker){// 마커의 Title은 Vno
        int Vno = Integer.parseInt(marker.getTitle());
        int result = 0;

        for(int i = 0; i < terminalList.length; i++){
            if(Vno == Integer.parseInt(terminalList[i].getVno())){
                result =Integer.parseInt(terminalList[i].getParkcnt());
                break;
            }
        }
        return result;
    }

    public boolean isValid(String Vno){ //해당 정류장이 데이터베이스에 들어있는가 판별
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM termTBL;", null);

        while(cursor.moveToNext()){
            if(cursor.getString(0).equals(Vno)){
                cursor.close();
                sqlDB.close();
                return true;
            }
        }
        cursor.close();
        sqlDB.close();
        return false;
    }
    public class myDBHelper extends SQLiteOpenHelper{
        public myDBHelper(Context context) {
            super(context, "terminalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE TermTBL (Vno CHAR(20) PRIMARY KEY);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS TermTBL");
            onCreate(db);
        }
    }
}
