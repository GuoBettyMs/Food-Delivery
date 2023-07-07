package com.example.modabba;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.modabba.SessionManagement.SessionManagement;
import com.example.modabba.Utils.PassingData;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ProgressDialog progressDialog;
    private GoogleMap mGoogleMap;
    private Geocoder geocoder;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private View mapView;
    private static final String TAG = MapActivity.class.getSimpleName();
    private final float DEFAULT_ZOOM = 19;

    private FirebaseAuth firebaseAuth;
    private SessionManagement sessionManagement;
    private FirebaseFirestore db;
    private ImageView moveMarker;
    private int callingActivity;
    private TextInputEditText location, landmark;
    private ChipGroup chipGroup;
    private String sessionId;
    private List<Address> addressList;
    private LatLng finalLatLng;
    private MaterialButton save;
    private String addressType;
    private PassingData data;
    private ImageView iv_lines;
    private AnimatedVectorDrawable avd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        callingActivity = getIntent().getIntExtra("callingActivity", 0000);
        sessionId = getIntent().getStringExtra("Sessionid");

        init();

        //动画绘制地图
        avd.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                iv_lines.post(new Runnable() {
                    @Override
                    public void run() {
                        avd.start();
                    }
                });
            }
        });
        avd.start();

        //选择地址类别
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
            switch (i) {
               case R.id.home:
                    addressType = "Home";
                    break;
               case R.id.work:
                    addressType = "Work";
                    break;
               case R.id.other:
                    addressType = "Other";
                    break;
                }
                save.setBackgroundColor(getResources().getColor(R.color.buttonPressed));
                save.setTextColor(Color.WHITE);
                save.setEnabled(true);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            switch (callingActivity) {
                case ActivityConstants.SignUpActivity:
                    signUpUser();
                    break;
                case 002:
                     updateAddress(); //DashboardFragment 更新地址
                     break;
                }
            }
        });

    }

    /**
    * 注册用户
     *  结构如下
     * users
     *     文档名称（系统ID）
     *          username（属性）：data.getName()（属性值）
     *          email：data.getEmail()
     *          ...
     *          address：
     *                  home：
     *                     city：addressList.get(0).getLocality()
     *                     pincode：addressList.get(0).getPostalCode()
     *                     ...
     *                  work:
     *                     city：addressList.get(0).getLocality()
     *                     pincode：addressList.get(0).getPostalCode()
     *                     ...
     *                  other:
     *                     city：addressList.get(0).getLocality()
     *                     pincode：addressList.get(0).getPostalCode()
     *                     ...
     *          registration_Date: currentDate + " " + currentTime
     *          ...
     **/
    public void signUpUser() {

        String city = addressList.get(0).getLocality();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        /** 定义 <details> 属性和属性值，嵌套到 addressType 的属性值
         * users
         *     文档名称（系统ID）
         *          username（属性）：data.getName()（属性值）
         *          address：
         *              home（addressType）：
         *                  city：addressList.get(0).getLocality()
         *                  ...
         *              ...
         *          ...
         **/
        Map<String, Object> details = new HashMap<>();
        details.put("city", addressList.get(0).getLocality());
        details.put("pincode", addressList.get(0).getPostalCode());
        details.put("geopoint", new GeoPoint(finalLatLng.latitude, finalLatLng.longitude));
        details.put("completeAddress", addressList.get(0).getAddressLine(0));
        details.put("landmark", Objects.requireNonNull(landmark.getText()).toString());


        /** 定义 <userAddress> 属性和属性值，嵌套到 address 的属性值
         * users
         *     文档名称（系统ID）
         *          username（属性）：data.getName()（属性值）
         *          address：
         *              home：<details>（嵌套对象）
         *              ...
         *          ...
         **/
        Map<String, Map<String, Object>> userAddress = new HashMap<>();
        userAddress.put(addressType, details);

        data = getIntent().getParcelableExtra("object");

        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Registering User");
        progressDialog.show();

        /** 定义文档的属性和属性值
         * users
         *     文档名称（系统ID）
         *          username（属性）：data.getName()（属性值）
         *          address： <userAddress> （嵌套对象）
         *          ...
        **/
        Map<String, Object> user = new HashMap<>();
        user.put("username", data.getName());
        user.put("email", data.getEmail());
        user.put("primaryNumber", data.getPhone()); //注册号码
        user.put("alternateNumber", data.getAltphone()); //联系号码
        user.put("password", data.getPassword());
        user.put("address", userAddress); //嵌套对象，1.home_details(5个地址数据) 2.work_details(5个地址数据) 3.other_details(5个地址数据)
        user.put("registration_Date", currentDate + " " + currentTime);
        user.put("wallet", 10000);

        //check for serviceability
        if (city.equals("Bhubaneswar")) {
            user.put("servicable", "Servicable");
        } else {
            user.put("servicable", "Not Servicable");
        }

        //<users> 集合添加新文档，系统自动给文档命名，可由document.getId()获取文档命
        db.collection("users")
          .add(user)
          .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
              @Override
              public void onSuccess(DocumentReference documentReference) {
                  Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                  progressDialog.dismiss();
                  sessionManagement.createLoginSession(data.getPhone(), data.getEmail(), data.getName(), documentReference.getId());
                  startActivity(new Intent(MapActivity.this, MainActivity.class)
                      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                }
          })
          .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Please try after some time", Toast.LENGTH_SHORT).show();
                }
          });

        //查询 Cloud Firestore <users>集合下的每一个文档所有数据,并跳转到 MainActivity
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "users 集合文档数量 => " + task.getResult().size());
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "Map :"+document.getId() + " => " + document.getData());
//                            }
                            //找到<Subscriptions> 集合的最新文档ID，添加订单信息和钱包流水信息
                            for(int i=0;i<task.getResult().size();i++){
                                if (i == (task.getResult().size() - 1)){
                                    String id = task.getResult().getDocuments().get(i).getId();
                                    Log.d(TAG, "文档("+task.getResult().size()+") :"+id + " => " + task.getResult().getDocuments().get(i).getData());
                                    progressDialog.dismiss();
                                    sessionManagement.createLoginSession(data.getPhone(), data.getEmail(), data.getName(), id);
                                    startActivity(new Intent(MapActivity.this, MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    /**
    * DashboardFragment 更新地址
    **/
    public void updateAddress() {
        String city = addressList.get(0).getLocality();
        Map<String, Object> details = new HashMap<>();
        details.put("city", addressList.get(0).getLocality());
        details.put("pincode", addressList.get(0).getPostalCode());
        details.put("geopoint", new GeoPoint(finalLatLng.latitude, finalLatLng.longitude));
        details.put("completeAddress", addressList.get(0).getAddressLine(0));
        details.put("landmark", Objects.requireNonNull(landmark.getText()).toString());

        Map<String, Map<String, Object>> userAddress = new HashMap<>();
        userAddress.put(addressType, details);

        DocumentReference docref = db.collection("users").document(sessionId);
        docref.update("address", userAddress).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MapActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapActivity.this, "not updated", Toast.LENGTH_SHORT).show();
            }
        });
        if (city.equals("Bhubaneswar")) {
            docref.update("servicable", "Servicable");
        } else {
            docref.update("servicable", "Not Servicable");
        }

        checkFirestoreCollectionData();
        onBackPressed();
    }

    /**
     * 将得到的数据转为字符串地址
     **/
    private void getLocationString(LatLng finalLatLng) throws IOException {

        geocoder = new Geocoder(this, Locale.getDefault());
        addressList = geocoder.getFromLocation(finalLatLng.latitude,finalLatLng.longitude,1);
        Log.i(TAG,"geocoder: "+geocoder+"; finalLatLng: "+finalLatLng + "\n" +addressList);
        if(!addressList.isEmpty()) {

            Address ad = addressList.get(0);
            String city = ad.getLocality();
            String state = ad.getAdminArea();
            String a = ad.getAddressLine(0);
            location.setText(city);
            landmark.setText(state);
            avd.stop();

//            if(!state.equals("Odisha")){
//                save.setText("Notify Availability");
//            }
//            else if(!city.equals("Bhubaneswar")){
//                save.setText("Notify Availability");
//            }
//            else
        }
    }

    /**
     * 定位
     **/
    @SuppressLint("MissingPermission")
    private void getDeviceLocation(){

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation != null){
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));

                                finalLatLng= mGoogleMap.getCameraPosition().target;
                                Log.i(TAG,"finalLatLng "+finalLatLng);
                                try {
                                    getLocationString(finalLatLng);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                                locationCallback = new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if(locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(MapActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    /**
    * google 地图定位请求
    **/
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if(mapView!=null && mapView.findViewById(Integer.parseInt("1"))!=null){

            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 45);

        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //检查设备的定位设置，检查结果为 Success
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();//定位
            }
        });
        //检查设备的定位设置，检查结果为 Failure
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        //重新定位
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                moveMarker.setVisibility(View.INVISIBLE);
            }
        });
        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                moveMarker.setVisibility(View.VISIBLE);
                finalLatLng= mGoogleMap.getCameraPosition().target;
                try {
                    getLocationString(finalLatLng);//将得到的数据转为字符串地址
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
    * 检查设备的定位设置，检查结果为 Failure，重新定位
    **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 51) {
            if(resultCode == RESULT_OK) {
                getDeviceLocation(); //定位
            }
        }
    }

    /**
     * 初始化
     **/
    private void init() {

        db = FirebaseFirestore.getInstance(); // Cloud Firestore 的初始化实例
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManagement = new SessionManagement(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        moveMarker = findViewById(R.id.text3);
        location = findViewById(R.id.maps_details_address);
        landmark = findViewById(R.id.landmark);
        chipGroup = findViewById(R.id.chipGroup);
        save = findViewById(R.id.save);
        iv_lines = findViewById(R.id.iv_line);
        avd = (AnimatedVectorDrawable) iv_lines.getBackground();
        progressDialog = new ProgressDialog(this);

    }

    /**
     * 查询 Cloud Firestore <users>集合下的每一个文档所有数据
     **/
    public void checkFirestoreCollectionData(){
        //快速验证是否已将数据添加到 Cloud Firestore
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Map :"+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    /**
     * 添加 Cloud Firestore <users>集合下的新文档数据
     **/
    public void addFirestoreDocData(){
        Map<String, Object> data = new HashMap<>();

        //方法一,手动给文档命名<user>，并添加数据
        //在新文档后面可继续内嵌新的集合+新的文档，但是删除文档时，其子集合不会随之删除！
        db.collection("users").document("user").set(data);
        db.collection("users").document("user")
                .collection("集合2").document("文档2").set(data);

        //方法二，系统会自动分配ID作为文档名称，可由document.getId()获取ID，并添加数据
        db.collection("users").add(data);


        //方法三，记录文档的refenrence，并添加数据
        DocumentReference ref = db.collection("users").document();
        ref.set(data);
    }

}
