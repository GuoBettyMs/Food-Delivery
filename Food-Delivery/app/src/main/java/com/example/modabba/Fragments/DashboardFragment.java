package com.example.modabba.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.modabba.Adapter;
import com.example.modabba.CheckoutActivity;
import com.example.modabba.History;
import com.example.modabba.MainActivity;
import com.example.modabba.MapActivity;
import com.example.modabba.Model;
import com.example.modabba.OrderSatusModel;
import com.example.modabba.OrderStatusAdapter;
import com.example.modabba.R;
import com.example.modabba.SessionManagement.SessionManagement;
import com.example.modabba.SlidePagerAdapter;
import com.example.modabba.SliderAdapterExample;
import com.example.modabba.ViewPagerAdapter;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.recyclerview.widget.LinearLayoutManager.*;

public class DashboardFragment extends Fragment {

    private Context context;
    private FirebaseFirestore db;
    private SliderView sliderView;
    private SessionManagement sessionManagement;
    private TextView dashBoardCredit;
    private String[] descriptionData = {"Preparing", "On Way", "Delivered"};
    private TextView dashboardUsername, dashboardLunch, dashBoardDinner, loc;
    private ViewPager viewPager;
    private ViewPager pager;
    private TabLayout tabLayout;
    private ImageButton getmap,dash_wallet;
    private List<Address> addressList;
    Adapter adapter;
    List<Model> models;
    List<OrderSatusModel> orderSatusModels;
    RecyclerView timelineView;
    private static final String TAG = DashboardFragment.class.getSimpleName();

    public DashboardFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        init();

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //scrollable card view
        models = new ArrayList<>();
        models.add(new Model(R.drawable.photo4, "LUNCH", "Brochure is an informative paper document (often also used for advertising) that can be folded into a template", 0,0));
        models.add(new Model(R.drawable.photo2, "DINNER", "Sticker is a type of label: a piece of printed paper, plastic, vinyl, or other material with pressure sensitive adhesive on one side", 0,0));
        models.add(new Model(R.drawable.photo1, "LUNCH", "Poster is any piece of printed paper designed to be attached to a wall or vertical surface.", 1,0));
        models.add(new Model(R.drawable.photo3, "DINNER", "Business cards are cards bearing business information about a company or individual.", 1,0));
        models.add(new Model(R.drawable.photo3, "COMBO", "Business cards are cards bearing business information about a company or individual.", 0,1));
        models.add(new Model(R.drawable.photo3, "COMBO", "Business cards are cards bearing business information about a company or individual.", 1,1));
        adapter = new Adapter(models, this, getContext());
        ViewPager horiscroll = view.findViewById(R.id.horiscroll);
        horiscroll.setAdapter(adapter);
        horiscroll.setPadding(130, 0, 130, 0);

        //top flipper
        sliderView = view.findViewById(R.id.imageSlider);
        final SliderAdapterExample adapter = new SliderAdapterExample(getContext());
        adapter.setCount(5);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.SLIDE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(2);
        sliderView.startAutoCycle();
        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
            @Override
            public void onIndicatorClicked(int position) {
                sliderView.setCurrentPagePosition(position);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        //刷新地址和余额，从 <users> 集合中找到对应文档， 获取属性 <address> 的属性值
        setLoccredits();

        //menu
        tabLayout.addTab(tabLayout.newTab().setText("Veg")); //添加导航标题
        tabLayout.addTab(tabLayout.newTab().setText("Non Veg"));
        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 2);
        viewPager.setAdapter(viewPagerAdapter);

        //today's menu
        List<Fragment> list = new ArrayList<>();
        list.add(new TVegDashboard()); //素食菜谱板
        list.add(new TNonVegDashboard());
        PagerAdapter pageadapter = new SlidePagerAdapter(getChildFragmentManager(), list);
        pager.setAdapter(pageadapter);

        //更改地址
        getmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, MapActivity.class).putExtra("callingActivity",002)
                        .putExtra("Sessionid",sessionManagement.getUserDocumentId()));
                    setLoccredits();
            }
        });

        //"钱包"按钮点击时间，获取历史记录
        dash_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,History.class).putExtra("Sessionid",sessionManagement.getUserDocumentId()));
            }
        });

       // orderState();

    }

    /**
    * 设置地址，从 <users> 集合中找到对应文档， 获取属性 <address> 的属性值
    **/
    private void setLoccredits() {
        db.collection("users").document(sessionManagement.getUserDocumentId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Map<String, Object>> address = (Map<String, Map<String, Object>>) documentSnapshot.get("address");
                        long credit = (long) documentSnapshot.get("wallet");
                        dashBoardCredit.setText(String.valueOf(credit));

                        assert address != null;
                        for (Map.Entry<String, Map<String, Object>> data : address.entrySet()) {

                            Map<String, Object> address_type_data = data.getValue();

                            String c = (String) address_type_data.get("city");

                            loc.setText(c);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        sessionManagement = new SessionManagement(context);
    }

    private void initView(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        pager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        dash_wallet=view.findViewById(R.id.dash_wallet);
        dashBoardCredit = view.findViewById(R.id.dashboard_credits);
        getmap = view.findViewById(R.id.getmap);
        loc = view.findViewById(R.id.loc);
        dashboardLunch = view.findViewById(R.id.dashboard_lunch);
        dashBoardDinner = view.findViewById(R.id.dashboard_dinner);

        StateProgressBar stateProgressBar = view.findViewById(R.id.progress_bar);
        stateProgressBar.setStateDescriptionData(descriptionData);

    }

    private void orderState() {
        final CollectionReference ref = db.collection("users").document(sessionManagement.getUserDocumentId()).collection("MyOrders");
        final String[] date = {""};
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots) {
                    Map<String,Object>sublist=documentSnapshot.getData();
                    date[0]=String.valueOf(sublist.get("date"));
                    String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
                }
            }
        });

    }

    /**
     * 删除 Cloud Firestore <users>集合下其子集合的文档，当文档全部清空时，集合也会清空
     * 仅删除主集合文档时，其子集合及其子集合文档不会随之删除！
     **/
    public void deleteFirestoreDocData(String collectionName,String docName){
        db.collection("users").document(sessionManagement.getUserDocumentId())
                .collection(collectionName).document(docName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG," => Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG," => Error");
                    }
                });
    }

    /**
     * 查询 Cloud Firestore 不同集合下的每一个文档所有数据
     **/
    public void checkFirestoreCollectionData(){
        //查询 Cloud Firestore <users>集合下的每一个文档所有数据
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
//                            Log.d(TAG, "users 集合文档数量 => " + task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG,  "users 集合 "+document.getId() + " => " + document.getData());
                                Log.d(TAG,  "users 集合 "+document.getId() + " => wallet属性值： " + document.get("wallet"));
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <Subscriptions> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Subscriptions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Subscriptions 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <MyOrders> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("MyOrders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "MyOrders 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <Wallet> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Wallet")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Wallet 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

}
