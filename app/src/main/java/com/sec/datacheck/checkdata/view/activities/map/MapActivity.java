package com.sec.datacheck.checkdata.view.activities.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sec.datacheck.R;
import com.sec.datacheck.checkdata.model.models.BookMark;
import com.sec.datacheck.checkdata.model.models.Columns;
import com.sec.datacheck.checkdata.model.models.DataCollectionApplication;
import com.sec.datacheck.checkdata.model.models.OnlineQueryResult;
import com.sec.datacheck.checkdata.view.activities.MapSingleTapListener;
import com.sec.datacheck.checkdata.view.adapter.BookMarkAdapter;
import com.sec.datacheck.checkdata.view.callbacks.mapCallbacks.SingleTapListener;
import com.sec.datacheck.checkdata.view.fragments.AutoReCloser.AutoReCloserFragment;
import com.sec.datacheck.checkdata.view.fragments.EditFeatureFragment;
import com.sec.datacheck.checkdata.view.fragments.FuseCutOutFragment.FuseCutOutFragment;
import com.sec.datacheck.checkdata.view.fragments.LinkBox.LinkBoxFragment;
import com.sec.datacheck.checkdata.view.fragments.Meter.MeterFragment;
import com.sec.datacheck.checkdata.view.fragments.MvMetering.MvMeteringFragment;
import com.sec.datacheck.checkdata.view.fragments.OHLine.OHLineFragment;
import com.sec.datacheck.checkdata.view.fragments.Pole.PoleFragment;
import com.sec.datacheck.checkdata.view.fragments.StationFragment.StationFragment;
import com.sec.datacheck.checkdata.view.fragments.SubstationFragment.SubStationFragment;
import com.sec.datacheck.checkdata.view.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements SingleTapListener, View.OnClickListener, MapPresenterListener {


    public static final String DOWNLOAD_GEO_DATABASE = "DOWNLOAD_GEO_DATABASE";
    public static final String POLYLINE = "PolyLine";
    public static final String POLYGON = "Polygon";
    private static final String TAG = "MapActivity";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 2;
    final int MY_LOCATION_REQUEST_CODE = 2;
    @BindView(R.id.mapView)
    public MapView mapView;
    @BindView(R.id.rlFragment)
    public RelativeLayout rlFragment;
    @BindView(R.id.fab_general)
    public FloatingActionMenu fabGeneral;
    @BindView(R.id.fab_add_distribution_box)
    public com.github.clans.fab.FloatingActionButton fabDistributionBox;
    @BindView(R.id.fab_add_poles)
    public com.github.clans.fab.FloatingActionButton fabPoles;
    @BindView(R.id.fab_add_rmu)
    public com.github.clans.fab.FloatingActionButton fabRMU;

    //    @BindView(R.id.fabMeasure)
//    public FloatingActionButton fabMeasure;
    @BindView(R.id.fab_add_sub_station)
    public com.github.clans.fab.FloatingActionButton fabSubStation;
    @BindView(R.id.fab_add_ocl_meter)
    public com.github.clans.fab.FloatingActionButton fabOCLMeter;
    @BindView(R.id.fab_add_service_point)
    public com.github.clans.fab.FloatingActionButton fabServicePoint;
    @BindView(R.id.fabLocation)
    public FloatingActionButton fabLocation;
    @BindView(R.id.fabFullScreen)
    public FloatingActionButton fabFullScreen;
    @BindView(R.id.fab_measure_distance)
    public com.github.clans.fab.FloatingActionButton fabMeasureDistance;
    @BindView(R.id.fab_measure_area)
    public com.github.clans.fab.FloatingActionButton fabMeasureArea;
    @BindView(R.id.compass)
    public ImageView mCompass;
    public MenuItem menuItemOnline;
    public MenuItem menuItemLoad;
    public MenuItem menuItemSync;
    public MenuItem menuItemOffline;
    public MenuItem item_load_previous_offline;
    public MenuItem menuItemGoOfflineMode;
    public MenuItem menuItemGoOnlineMode;
    public MenuItem menuItemOverflow;
    public Matrix mMatrix;
    public Bitmap mBitmap;
    public boolean drawShape = false;
    public boolean onlineData = true;
    public int currentOfflineVersion;
    public String currentOfflineVersionTitle;
    public String shapeType;
    @BindView(R.id.fab_measure)
    FloatingActionMenu mFabMeasureMenu;
    @BindView(R.id.linear_layers_info)
    LinearLayout mapLegend;
    @BindView(R.id.tvLatLong)
    TextView tvLatLong;
    @BindView(R.id.tv_more_layer_info)
    TextView tvMoreLayerInfo;
    @BindView(R.id.linear_layers_details)
    LinearLayout mapLegendContainer;
    @BindView(R.id.map_layout)
    ConstraintLayout mConstraintLayout;
    @BindView(R.id.bottom_sheet)
    LinearLayout mBottomSheet;
    @BindView(R.id.update_btn)
    Button mUpdateBtn;
    @BindView(R.id.cancel_btn)
    Button mCancelBtn;
    @BindView(R.id.add_btn)
    Button mCreatePointBtn;
    @BindView(R.id.cancel_button)
    Button mCancelCreatePointBtn;
    @BindView(R.id.edit_point_bottom_sheet_container)
    LinearLayout mEditPointLayout;
    @BindView(R.id.add_point_bottom_sheet_container)
    LinearLayout mAddPointLayout;
    @BindView(R.id.measure_info)
    LinearLayout mMeasureLayerInfo;
    @BindView(R.id.measure_function_in_meter_lbl)
    TextView mMeasureInMeterLbl;
    @BindView(R.id.measure_function_value_in_meter_lbl)
    TextView mMeasureValueInMeterLbl;
    @BindView(R.id.measure_function_in_km_lbl)
    TextView mMeasureInKMLbl;
    @BindView(R.id.measure_function_value_in_km_lbl)
    TextView mMeasureValueInKMLbl;
    Point pointToAdd;
    BottomSheetBehavior sheetBehavior;
    //    EditInFeatureFragment editInFeatureFragment;
    View dialogView;
    Polygon poly;
    ActionMode drawToolsActionMode;
    LocationManager manager;
    Geometry workingAreaGeometry;
    MapActivity mCurrent;
    MapSingleTapListener mapSingleTapListener;
//    FeatureLayer FCL_DistributionBoxLayer, FCL_POLES_Layer, FCL_RMU_Layer, FCL_Substation_Layer, OCL_METER_Layer, ServicePoint_Layer;
//    ServiceFeatureTable FCL_DistributionBox_ServiceTable, FCL_POLES_ServiceTable, FCL_RMU_ServiceTable, FCL_Substation_ServiceTable, OCL_METER_ServiceTable, ServicePoint_ServiceTable;
//    GeodatabaseFeatureTable FCL_DistributionBoxTableOffline, FCL_POLESTableOffline, FCL_RMUTableOffline, FCL_SubstationTableOffline, OCL_METERTableOffline, ServicePointTableOffline;


    GraphicsOverlay graphicsOverlay, drawGraphicLayer;
    PictureMarkerSymbol pictureMarkerSymbol;
    ArcGISMap baseMap;
    MapPresenter presenter;
    OnlineQueryResult selectedResult;
    Point mCurrentLocation;


    ServiceFeatureTable substationTable, stationTable, autoReCloserTable, fuseCutOutTable, linkBoxTable, LoadBreakerSwitchTable, LVDistributionBoxTable, LVDistributionPanelTable, MCCB_LVP_Circuits_RecordTable, MeterTable,
            MiniPillarTable, MV_MeteringTable, OH_LinesTable, PoleTable, SectionlizerTable, SVC_StaticVarCompensatorTable, SwitchGearTable, TransformerTable, VoltageRegulatorTable;
    GeodatabaseFeatureTable substationOfflineTable, stationOfflineTable, autoReCloserOfflineTable, fuseCutOutOfflineTable, linkBoxOfflineTable, mvMeteringOfflineTable,poleOfflineTable,OHLineOfflineTable,MeterOfflineTable;
    FeatureLayer substationLayer, stationLayer, autoReCloserLayer, fuseCutOutLayer, linkBoxLayer, LoadBreakerSwitchLayer, LVDistributionBoxLayer, LVDistributionPanelLayer, MCCB_LVP_Circuits_RecordLayer, MeterLayer,
            MiniPillarLayer, MV_MeteringLayer, OH_LinesLayer, PoleLayer, SectionlizerLayer, SVC_StaticVarCompensatorLayer, SwitchGearLayer, TransformerLayer, VoltageRegulatorLayer;


    PointCollection pointCollection;
    Point lastPointStep;
    SubStationFragment substationFragment;
    StationFragment stationFragment;
    AutoReCloserFragment autoReCloserFragment;
    FuseCutOutFragment fuseCutOutFragment;
    LinkBoxFragment linkBoxFragment;
    MvMeteringFragment mvMeteringFragment;
    PoleFragment poleFragment;
    OHLineFragment ohLineFragment;
    MeterFragment meterFragment;
    private boolean isShowingLayerInfo;
    private boolean queryStatus = false;
    private boolean isFragmentShown = false;
    private FragmentManager fragmentManager;
    private boolean isFullScreenMode = false;
    private boolean isInDrawMood;
    private String localDatabaseTitle;
    private boolean syncAndGoOnline;
    private Point startPoint, endPoint;
    private boolean drawMeasure = false;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mapView != null) {
                mapView.resume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_home_check_data);

            init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            mCurrent = MapActivity.this;

            ButterKnife.bind(mCurrent);

            presenter = new MapPresenter(this, mCurrent);

            fragmentManager = getSupportFragmentManager();

            sheetBehavior = BottomSheetBehavior.from(mBottomSheet);
            sheetBehavior.setPeekHeight(150, true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            mBottomSheet.setVisibility(View.GONE);

            mCompass.setScaleType(ImageView.ScaleType.MATRIX);
            mMatrix = new Matrix();
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_compass);

            initMapFabMenu();

            initSheetBehavior();

            initMap();

            intFab();

            tvMoreLayerInfo.setOnClickListener(this);
            fabFullScreen.setOnClickListener(this);
            //Compass rotation
            mCompass.setOnClickListener(this);
            //AddButton
            mCreatePointBtn.setOnClickListener(this);
            mCancelCreatePointBtn.setOnClickListener(this);

            if (Utilities.isNetworkAvailable(mCurrent)) {
                onlineData = true;
            } else {
                onlineData = false;
                showOfflineMapsList(mCurrent, mapView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMapFabMenu() {
        try {

            fabGeneral.setClosedOnTouchOutside(true);

            fabDistributionBox.setOnClickListener(this);
            fabPoles.setOnClickListener(this);
            fabRMU.setOnClickListener(this);
            fabSubStation.setOnClickListener(this);
            fabOCLMeter.setOnClickListener(this);
            fabServicePoint.setOnClickListener(this);

            mFabMeasureMenu.setClosedOnTouchOutside(true);

            fabMeasureArea.setOnClickListener(this);
            fabMeasureDistance.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSheetBehavior() {
        try {
            mCancelBtn.setOnClickListener(this);
            mUpdateBtn.setOnClickListener(this);
            /**
             * bottom sheet state change listener
             * we are changing button text when sheet changed state
             * */
            sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        graphicsOverlay.getGraphics().clear();
                        selectedResult = null;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intFab() {
        try {

            fabLocation.setOnClickListener(this);
//            fabMeasure.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMap() {
        try {

            //Declaring baseMap
            baseMap = new ArcGISMap(Basemap.createOpenStreetMap());

            mapView.setMap(baseMap);

            pointCollection = new PointCollection(mapView.getSpatialReference());

            // wraparound is enabled if layers within map support it
            mapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);

            graphicsOverlay = new GraphicsOverlay();
            pictureMarkerSymbol = new PictureMarkerSymbol((BitmapDrawable) getResources().getDrawable(R.drawable.ic_marker_64));
            mapView.getGraphicsOverlays().add(graphicsOverlay);

            if (checkLocationPermissions()) {

                // displaying user location on map
                showDeviceLocation();
            }


            initOnlineLayers(baseMap, mapView);
            // init map rotation
//            initMapRotation();

            //initSingleTap
            initSingleTap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initOnlineLayers(ArcGISMap baseMap, MapView mapView) {
        try {

            // create feature layer with its service feature table
            // create the service feature table
//
//            FCL_DistributionBox_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_DISTRIBUTIONBOX));
//            FCL_POLES_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_POLES));
//            FCL_RMU_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_RMU));
//            FCL_Substation_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_Substation));
//            OCL_METER_ServiceTable = new ServiceFeatureTable(getString(R.string.OCL_METER));
//            ServicePoint_ServiceTable = new ServiceFeatureTable(getString(R.string.Service_Point));

            autoReCloserTable = new ServiceFeatureTable(getString(R.string.auto_recloser));
            fuseCutOutTable = new ServiceFeatureTable(getString(R.string.fuse_cut_out));
            substationTable = new ServiceFeatureTable(getString(R.string.substations));
            stationTable = new ServiceFeatureTable(getString(R.string.stations));

            linkBoxTable = new ServiceFeatureTable(getString(R.string.link_box));
            LoadBreakerSwitchTable = new ServiceFeatureTable(getString(R.string.load_Breaker_Switch_LBS_33_13_KV));
            LVDistributionBoxTable = new ServiceFeatureTable(getString(R.string.l_V_distribution_Box));
            LVDistributionPanelTable = new ServiceFeatureTable(getString(R.string.l_V_distribution_Panel));
            MCCB_LVP_Circuits_RecordTable = new ServiceFeatureTable(getString(R.string.MCCB_LVP_Circuits_Record));
            MeterTable = new ServiceFeatureTable(getString(R.string.Meter));
            MiniPillarTable = new ServiceFeatureTable(getString(R.string.Mini_pillar));
            MV_MeteringTable = new ServiceFeatureTable(getString(R.string.MV_Metering));
            OH_LinesTable = new ServiceFeatureTable(getString(R.string.OH_Lines));
            PoleTable = new ServiceFeatureTable(getString(R.string.Pole));
            SectionlizerTable = new ServiceFeatureTable(getString(R.string.Sectionlizer));
            SVC_StaticVarCompensatorTable = new ServiceFeatureTable(getString(R.string.SVC_Static_VAR_Compensator));
            SwitchGearTable = new ServiceFeatureTable(getString(R.string.Switchgear));
            TransformerTable = new ServiceFeatureTable(getString(R.string.Transformer));
            VoltageRegulatorTable = new ServiceFeatureTable(getString(R.string.Voltage_regulator));

            // create the feature layer using the service feature table
//          FCL_DistributionBoxLayer = new FeatureLayer(FCL_DistributionBox_ServiceTable);
//            FCL_POLES_Layer = new FeatureLayer(FCL_POLES_ServiceTable);
//            FCL_RMU_Layer = new FeatureLayer(FCL_RMU_ServiceTable);
//            FCL_Substation_Layer = new FeatureLayer(FCL_Substation_ServiceTable);
//            OCL_METER_Layer = new FeatureLayer(OCL_METER_ServiceTable);
//            ServicePoint_Layer = new FeatureLayer(ServicePoint_ServiceTable);

            autoReCloserLayer = new FeatureLayer(autoReCloserTable);
            substationLayer = new FeatureLayer(substationTable);
            stationLayer = new FeatureLayer(stationTable);
            fuseCutOutLayer = new FeatureLayer(fuseCutOutTable);

            linkBoxLayer = new FeatureLayer(linkBoxTable);
            LoadBreakerSwitchLayer = new FeatureLayer(LoadBreakerSwitchTable);
            LVDistributionBoxLayer = new FeatureLayer(LVDistributionBoxTable);
            LVDistributionPanelLayer = new FeatureLayer(LVDistributionPanelTable);
            MCCB_LVP_Circuits_RecordLayer = new FeatureLayer(MCCB_LVP_Circuits_RecordTable);
            MeterLayer = new FeatureLayer(MeterTable);
            MiniPillarLayer = new FeatureLayer(MiniPillarTable);
            MV_MeteringLayer = new FeatureLayer(MV_MeteringTable);
            OH_LinesLayer = new FeatureLayer(OH_LinesTable);
            PoleLayer = new FeatureLayer(PoleTable);
            SectionlizerLayer = new FeatureLayer(SectionlizerTable);
            SVC_StaticVarCompensatorLayer = new FeatureLayer(SVC_StaticVarCompensatorTable);
            SwitchGearLayer = new FeatureLayer(SwitchGearTable);
            TransformerLayer = new FeatureLayer(TransformerTable);
            VoltageRegulatorLayer = new FeatureLayer(VoltageRegulatorTable);

            // add the layer to the map

            baseMap.getOperationalLayers().add(autoReCloserLayer);
            baseMap.getOperationalLayers().add(fuseCutOutLayer);
            baseMap.getOperationalLayers().add(substationLayer);
            baseMap.getOperationalLayers().add(stationLayer);

            baseMap.getOperationalLayers().add(linkBoxLayer);
            baseMap.getOperationalLayers().add(LoadBreakerSwitchLayer);
            baseMap.getOperationalLayers().add(LVDistributionBoxLayer);
            baseMap.getOperationalLayers().add(LVDistributionPanelLayer);
            baseMap.getOperationalLayers().add(MCCB_LVP_Circuits_RecordLayer);
            baseMap.getOperationalLayers().add(MeterLayer);
            baseMap.getOperationalLayers().add(MiniPillarLayer);
            baseMap.getOperationalLayers().add(MV_MeteringLayer);
            baseMap.getOperationalLayers().add(OH_LinesLayer);
            baseMap.getOperationalLayers().add(PoleLayer);
            baseMap.getOperationalLayers().add(SVC_StaticVarCompensatorLayer);
            baseMap.getOperationalLayers().add(SwitchGearLayer);
            baseMap.getOperationalLayers().add(TransformerLayer);
            baseMap.getOperationalLayers().add(VoltageRegulatorLayer);


            // set the map to be displayed in the mapView
            mapView.setMap(baseMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoomToCurrentLocation() {
        try {
            mapView.setViewpoint(new Viewpoint(mCurrentLocation, 16.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkLocationPermissions() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(mCurrent,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSingleTap() {
        try {
            mapSingleTapListener = new MapSingleTapListener(mCurrent, mapView, this);
            mapView.setOnTouchListener(mapSingleTapListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDeviceLocation() {
        try {
            Log.i(TAG, "showDeviceLocation(): is called");

            LocationDisplay locationDisplay = mapView.getLocationDisplay();
            locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);

            locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
                @Override
                public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                    //reading location changing
                    mCurrentLocation = locationChangedEvent.getLocation().getPosition();

                    if (mCurrentLocation != null) {
                        double accuracy = locationChangedEvent.getLocation().getHorizontalAccuracy();
                        int mAccuracy = (int) (accuracy * 100);
                        accuracy = (double) mAccuracy / 100;
                        String latLang = "Lat: " + Utilities.round(mCurrentLocation.getX(), 5) + " Lan: " + Utilities.round(mCurrentLocation.getY(), 5) + " Accuracy = " + accuracy;
                        tvLatLong.setText(latLang);
                    }

                    mMatrix.reset();
                    mMatrix.postRotate(-(float) mapView.getRotation(), mBitmap.getHeight() / 2, mBitmap.getWidth() / 2);
                    mCompass.setImageMatrix(mMatrix);
                }
            });

            locationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {

                if (dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError() == null) {
                    Log.i(TAG, "Location Display Started=" + dataSourceStatusChangedEvent.isStarted());
                } else {
                    dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().printStackTrace();
                }
            });
            locationDisplay.startAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMapRotation() {
        try {
            Log.i(TAG, "initMapRotation(): is called");

            mMatrix.reset();
            final ListenableFuture<Boolean> viewpointSetFuture = mapView.setViewpointRotationAsync(0.0);
            viewpointSetFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean completed = viewpointSetFuture.get();
                        if (completed) {
                            Log.i(TAG, "Rotation completed successfully");
                            mMatrix.postRotate(-(float) mapView.getRotation(), mBitmap.getHeight() / 2, mBitmap.getWidth() / 2);
                            mCompass.setImageMatrix(mMatrix);
                        }
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Rotation interrupted");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        // Deal with exception during animation...
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mapView != null) {
                mapView.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mapView != null) {
                mapView.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            try {
                if ((permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) || (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showDeviceLocation();
                } /*else {
                    Utilities.showToast(mCurrent, getString(R.string.please_open_gps_location));
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {


        if (v.equals(fabLocation)) {
            if (mCurrentLocation != null) {
                zoomToCurrentLocation();
            }
        } else if (v.equals(tvMoreLayerInfo)) {
            if (mapLegendContainer.getVisibility() == View.GONE) {
                mapLegendContainer.setVisibility(View.VISIBLE);
            } else {
                mapLegendContainer.setVisibility(View.GONE);
            }
        } else if (v.equals(fabFullScreen)) {
            if (isFullScreenMode) {
                exitFullScreenMode();
            } else {
                fullScreenMode();
            }
        } else if (v.equals(mUpdateBtn)) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            if (graphicsOverlay != null && graphicsOverlay.getGraphics() != null) {
//                graphicsOverlay.getGraphics().clear();
//
//                showEditFragment(selectedResult);
//            }
            showEditReviewFragment();
        } else if (v.equals(mCancelBtn)) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (graphicsOverlay != null && graphicsOverlay.getGraphics() != null) {
                graphicsOverlay.getGraphics().clear();
            }
        } else if (v.equals(mCompass)) {

            initMapRotation();

        }
//        else if (v.equals(fabDistributionBox)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(FCL_DistributionBoxTableOffline);
//            selectedResult.setServiceFeatureTable(FCL_DistributionBox_ServiceTable);
//            selectedResult.setFeatureLayer(FCL_DistributionBoxLayer);
//        } else if (v.equals(fabPoles)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(FCL_POLESTableOffline);
//            selectedResult.setServiceFeatureTable(FCL_POLES_ServiceTable);
//            selectedResult.setFeatureLayer(FCL_POLES_Layer);
//
//        } else if (v.equals(fabRMU)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(FCL_RMUTableOffline);
//            selectedResult.setServiceFeatureTable(FCL_RMU_ServiceTable);
//            selectedResult.setFeatureLayer(FCL_RMU_Layer);
//
//        } else if (v.equals(fabSubStation)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(FCL_SubstationTableOffline);
//            selectedResult.setServiceFeatureTable(FCL_Substation_ServiceTable);
//            selectedResult.setFeatureLayer(FCL_Substation_Layer);
//
//        } else if (v.equals(fabOCLMeter)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(OCL_METERTableOffline);
//            selectedResult.setServiceFeatureTable(OCL_METER_ServiceTable);
//            selectedResult.setFeatureLayer(OCL_METER_Layer);
//
//        } else if (v.equals(fabServicePoint)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(ServicePointTableOffline);
//            selectedResult.setServiceFeatureTable(ServicePoint_ServiceTable);
//            selectedResult.setFeatureLayer(ServicePoint_Layer);
//
//        } else if (v.equals(mCreatePointBtn)) {
//            createPoint();
//        } else if (v.equals(mCancelCreatePointBtn)) {
//            cancelCreatePoint();
//        }
//
        else if (v.equals(fabMeasureDistance)) {
            handleFabMeasureAction();
            mFabMeasureMenu.close(true);
            drawMeasure = true;
            shapeType = POLYLINE;
        }
    }

    private void startDrawShape(Point point) {
        try {

//            graphicsOverlay.getGraphics().clear();
//            graphicsOverlay.getGraphics().add(new Graphic(point, pictureMarkerSymbol));
//            pointToAdd = point;
//
//            if (startPoint == null) {
//                startPoint = point;
//            } else {
//                endPoint = point;
//                drawLine(pointCollection);
//                calculateDistanceBetweenTwoPoints(startPoint, endPoint, mapView.getSpatialReference());
//            }

            mAddPointLayout.setVisibility(View.VISIBLE);
            mEditPointLayout.setVisibility(View.GONE);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            graphicsOverlay.getGraphics().clear();
            graphicsOverlay.getGraphics().add(new Graphic(point, pictureMarkerSymbol));
            pointToAdd = point;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditReviewFragment() {
        try {

            if (selectedResult.getFeatureLayer().getName().matches(substationLayer.getName())) {
                substationFragment = SubStationFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(substationFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(stationLayer.getName())) {
                stationFragment = StationFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(stationFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(autoReCloserLayer.getName())) {
                autoReCloserFragment = AutoReCloserFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(autoReCloserFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(fuseCutOutLayer.getName())) {
                fuseCutOutFragment = FuseCutOutFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(fuseCutOutFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(linkBoxLayer.getName())) {
                linkBoxFragment = LinkBoxFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(linkBoxFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(MV_MeteringLayer.getName())) {
                mvMeteringFragment = MvMeteringFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(mvMeteringFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(PoleLayer.getName())) {
                poleFragment = PoleFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(poleFragment);
            } else if (selectedResult.getFeatureLayer().getName().matches(OH_LinesLayer.getName())) {
                ohLineFragment = OHLineFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(ohLineFragment);
            }
            else if (selectedResult.getFeatureLayer().getName().matches(MeterLayer.getName())) {
                meterFragment = MeterFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
                showCheckDataEditFragment(meterFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelCreatePoint() {
        try {
            graphicsOverlay.getGraphics().clear();
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mAddPointLayout.setVisibility(View.GONE);
            mEditPointLayout.setVisibility(View.GONE);
            drawShape = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPoint() {
        try {
            Utilities.showLoadingDialog(mCurrent);

            graphicsOverlay.getGraphics().clear();
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mAddPointLayout.setVisibility(View.GONE);
            mEditPointLayout.setVisibility(View.GONE);

            drawShape = false;

            if (onlineData) {
                drawOnlineMode();
            } else {
                drawOfflineMode();
            }


        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
            Utilities.dismissLoadingDialog();
        }
    }

    private void drawOnlineMode() {
        try {
            Map<String, Object> attributes = new HashMap<>();
            selectedResult.getServiceFeatureTable().loadAsync();
            selectedResult.getServiceFeatureTable().addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "applyEdits(): add Done listener is called");
                        Log.i(TAG, "createPoint(): loading service feature table is finished");
                        attributes.put(Columns.Device_No, "0");
                        attributes.put(Columns.Code, 0);

                        // creates a new feature using default attributes and point
                        Feature feature = selectedResult.getServiceFeatureTable().createFeature(attributes, pointToAdd);

                        // check if feature can be added to feature table
                        if (selectedResult.getServiceFeatureTable().canAdd()) {
                            Log.i(TAG, "createPoint(): can add feature");

                            // add the new feature to the feature table and to server
                            selectedResult.getServiceFeatureTable().addFeatureAsync(feature).addDoneListener(() -> applyEditsOnline(selectedResult.getServiceFeatureTable(), selectedResult));
                        } else {
                            Log.i(TAG, "createPoint(): cannot add feature");

                            runOnUiThread(() -> {
                                Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                                Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawOfflineMode() {
        try {
            Map<String, Object> attributes = new HashMap<>();
            selectedResult.getGeodatabaseFeatureTable().loadAsync();
            selectedResult.getGeodatabaseFeatureTable().addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "applyEdits(): add Done listener is called");
                        Log.i(TAG, "createPoint(): loading service feature table is finished");
                        attributes.put(Columns.Device_No, "0");
                        attributes.put(Columns.Code, 0);

                        // creates a new feature using default attributes and point
                        Feature feature = selectedResult.getGeodatabaseFeatureTable().createFeature(attributes, pointToAdd);

                        // check if feature can be added to feature table
                        if (selectedResult.getGeodatabaseFeatureTable().canAdd()) {
                            Log.i(TAG, "createPoint(): can add feature");

                            // add the new feature to the feature table and to server
                            selectedResult.getGeodatabaseFeatureTable().addFeatureAsync(feature).addDoneListener(() -> applyEditsOffline(selectedResult.getGeodatabaseFeatureTable(), selectedResult));
                        } else {
                            Log.i(TAG, "createPoint(): cannot add feature");

                            runOnUiThread(() -> {
                                Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                                Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
                                Utilities.dismissLoadingDialog();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
                        Utilities.dismissLoadingDialog();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
            Utilities.dismissLoadingDialog();
        }
    }

    private void applyEditsOnline(ServiceFeatureTable featureTable, OnlineQueryResult selectedResult) {
        Log.i(TAG, "applyEdits(): is called");

        // apply the changes to the server
        final ListenableFuture<List<FeatureEditResult>> editResult = featureTable.applyEditsAsync();
        editResult.addDoneListener(() -> {
            try {
                Log.i(TAG, "applyEdits(): add Done listener is called");

                List<FeatureEditResult> editResults = editResult.get();
                // check if the server edit was successful
                if (editResults != null && !editResults.isEmpty()) {
                    if (!editResults.get(0).hasCompletedWithErrors()) {
                        Log.i(TAG, getString(R.string.feature_added));
//                            selectedResult.setObjectID(String.valueOf(editResults.get(0).getObjectId()));
                        queryOnAddedFeatureOnline(featureTable, selectedResult);

                    } else {
                        throw editResults.get(0).getError();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                runOnUiThread(() -> {
                    Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                    Utilities.showToast(mCurrent, e.getMessage());
                    Utilities.dismissLoadingDialog();
                });
            }
        });
    }

    private void applyEditsOffline(GeodatabaseFeatureTable featureTable, OnlineQueryResult selectedResult) {
        Log.i(TAG, "applyEdits(): is called");


        try {
            queryOnAddedFeatureOffline(featureTable, selectedResult);


        } catch (Exception e) {
            runOnUiThread(() -> {
                Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                Utilities.showToast(mCurrent, e.getMessage());
                Utilities.dismissLoadingDialog();
            });
        }

    }

    private void queryOnAddedFeatureOnline(ServiceFeatureTable featureTable, OnlineQueryResult selectedResult) {
        try {

            QueryParameters queryParameters = new QueryParameters();
            queryParameters.setWhereClause("1 = 1");
            queryParameters.setGeometry(pointToAdd);
            queryParameters.setReturnGeometry(false);

            ListenableFuture<FeatureQueryResult> queryResult = featureTable.queryFeaturesAsync(queryParameters);
            queryResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        FeatureQueryResult result = queryResult.get();
                        Iterator<Feature> resultIterator = result.iterator();
                        if (resultIterator.hasNext()) {
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to

                                ArcGISFeature feature = (ArcGISFeature) resultIterator.next();
                                feature.loadAsync();

                                if (feature == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): feature result = null");
                                }

                                if (selectedResult == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): selected Query Online = null");
                                }
                                selectedResult.setFeature(feature);
                                selectedResult.setObjectID(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));

                                Log.i(TAG, "queryOnAddedFeature(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utilities.dismissLoadingDialog();
                                        showEditFragment(selectedResult);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.showToast(mCurrent, e.getMessage());
                        Utilities.dismissLoadingDialog();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
            Utilities.dismissLoadingDialog();
        }
    }

    private void queryOnAddedFeatureOffline(GeodatabaseFeatureTable featureTable, OnlineQueryResult selectedResult) {
        try {

            QueryParameters queryParameters = new QueryParameters();
            queryParameters.setWhereClause("1 = 1");
            queryParameters.setGeometry(pointToAdd);
            queryParameters.setReturnGeometry(false);

            ListenableFuture<FeatureQueryResult> queryResult = featureTable.queryFeaturesAsync(queryParameters);
            queryResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        FeatureQueryResult result = queryResult.get();
                        Iterator<Feature> resultIterator = result.iterator();
                        if (resultIterator.hasNext()) {
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to

                                Feature feature = resultIterator.next();

                                if (feature == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): feature result = null");
                                }

                                if (selectedResult == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): selected Query Online = null");
                                }
                                selectedResult.setGeodatabaseFeatureTable(featureTable);
                                selectedResult.setFeatureOffline(feature);
                                selectedResult.setObjectID(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));

                                Log.i(TAG, "queryOnAddedFeature(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utilities.dismissLoadingDialog();
                                        showEditFragment(selectedResult);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.showToast(mCurrent, e.getMessage());
                        Utilities.dismissLoadingDialog();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
            Utilities.dismissLoadingDialog();
        }
    }

    private void exitFullScreenMode() {
        try {
            isFullScreenMode = false;
            fabFullScreen.setImageResource(R.drawable.ic_fullscreen_white_24dp);
            getSupportActionBar().show();
            fabLocation.setVisibility(View.VISIBLE);
//        fabGeneral.setVisibility(View.VISIBLE);
            tvLatLong.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fullScreenMode() {
        try {
            isFullScreenMode = true;
            fabFullScreen.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fabLocation.setVisibility(View.GONE);
        fabGeneral.setVisibility(View.GONE);
        tvLatLong.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);

            menuItemOffline = menu.findItem(R.id.item_go_offline);
            item_load_previous_offline = menu.findItem(R.id.item_load_previous_offline);
            menuItemOnline = menu.findItem(R.id.item_go_online);
            menuItemSync = menu.findItem(R.id.item_sync);
            menuItemLoad = menu.findItem(R.id.item_load_previous);
            menuItemGoOfflineMode = menu.findItem(R.id.item_go_offline_mode);
            menuItemGoOnlineMode = menu.findItem(R.id.item_go_online_mode);
            menuItemOverflow = menu.findItem(R.id.overflow);


            if (presenter.isLocalGeoDatabase() && Utilities.isNetworkAvailable(this)) {
                menuItemLoad.setVisible(false);
            } else {
                menuItemLoad.setVisible(true);
            }

            if (!onlineData) {
                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
                menuItemSync.setVisible(true);
                menuItemOnline.setVisible(true);
                menuItemGoOfflineMode.setVisible(false);
                menuItemGoOnlineMode.setVisible(true);
            } else {
                item_load_previous_offline.setVisible(true);
                menuItemGoOfflineMode.setVisible(true);
                menuItemGoOnlineMode.setVisible(false);
            }
            Log.d("Test", "In OnCreate Options Menu");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_draw_polygon:


                break;
            case R.id.item_current_extent:
                // define permission to request
                String[] reqPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

                if (ContextCompat.checkSelfPermission(mCurrent, reqPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                    // request permission
                    ActivityCompat.requestPermissions(mCurrent, reqPermission, WRITE_EXTERNAL_STORAGE_REQUEST);
                } else {
                    goOffline();
                }
                try {
//                    menuItemGoOfflineMode.setVisible(false);
//                    menuItemGoOnlineMode.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.item_load_previous:
                showOfflineMapsList(mCurrent, mapView);
                return true;
            case R.id.item_load_previous_offline:
                showOfflineMapsList(mCurrent, mapView);
                return true;
            case R.id.item_go_online:
                syncAndGoOnline();
                return true;
            case R.id.item_sync:
                syncData();
                return true;
            case R.id.item_Add_Bookmark:
                showAddNewBookmarkDialog();
                break;
            case R.id.item_Show_Bookmarks:
                showBookmarksDialog();
                break;
            /**------------------------------Ali Ussama Update------------------------------------*/
            case R.id.item_go_offline_mode:
                try {

                    goOffline();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.item_go_online_mode:
                try {
//                    menuItemGoOfflineMode.setVisible(true);
//                    menuItemGoOnlineMode.setVisible(false);
                    if (Utilities.isNetworkAvailable(mCurrent)) {
                        goOnline();
                    } else {
                        Utilities.showInfoDialog(mCurrent, getString(R.string.network_connection_failed), getString(R.string.please_your_network_connect));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            /**------------------------------Ali Ussama Update------------------------------------*/

        }
        return super.onOptionsItemSelected(item);
    }

    void showBookmarksDialog() {
        ArrayList<BookMark> bookMarks = DataCollectionApplication.getAllBookMarks();

        if (bookMarks.size() > 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_show_bookmarks_title));
            dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_show_bookmarks, null, false);

            ListView listView = (ListView) dialogView.findViewById(R.id.lvBookmarks);


            builder.setView(dialogView);
            final android.app.AlertDialog alertDialog = builder.create();

            final BookMarkAdapter bookMarkAdapter = new BookMarkAdapter(this, bookMarks, alertDialog);
            listView.setAdapter(bookMarkAdapter);

            alertDialog.show();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                try {

                    BookMark bookMark = (BookMark) bookMarkAdapter.getItem(position);

                    Viewpoint viewpoint = Viewpoint.fromJson(bookMark.getJson());
                    mapView.setViewpoint(viewpoint);
                    alertDialog.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

        } else {
            Utilities.showToast(this, getString(R.string.no_bookmarks));
        }
    }

    private void showAddNewBookmarkDialog() {

        MaterialDialog mBookMarkDlg = Utilities.showAlertDialogWithCustomView(mCurrent, R.layout.book_mark_layout, getString(R.string.dialog_bookmark_cancel));

        EditText bookmarkET = mBookMarkDlg.getView().findViewById(R.id.book_mark_name_edit_text);
        Button bookmarkBtn = mBookMarkDlg.getView().findViewById(R.id.book_mark_save_btn);
        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmarkET.getText() == null || bookmarkET.getText().toString().isEmpty()) {
                    bookmarkET.setError(getString(R.string.required));
                } else {
                    String title = bookmarkET.getText().toString();
                    saveBookMark(title, mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY));
                    mBookMarkDlg.dismiss();
                }
            }
        });
    }

    private void saveBookMark(String title, Viewpoint currentViewpoint) {
        try {

            DataCollectionApplication.addBookMark(currentViewpoint.toJson(), title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncAndGoOnline() {
        try {
            syncData();
            syncAndGoOnline = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goOnline() {
        if (Utilities.isNetworkAvailable(mCurrent)) {
            initMap();
            onlineData = true;
            showViews();
        } else {
            Utilities.showInfoDialog(mCurrent, getString(R.string.network_connection_failed), getString(R.string.please_your_network_connect));
        }
    }

    private void syncData() {
        try {
            if (Utilities.isNetworkAvailable(mCurrent)) {
                presenter.syncData(currentOfflineVersionTitle);
            } else {
                Utilities.showInfoDialog(mCurrent, getString(R.string.network_connection_failed), getString(R.string.please_your_network_connect));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showOfflineMapsList(final MapActivity context, final MapView mapView) {
        //Declaring List to hold offline database titles
        ArrayList<String> databaseTitles = DataCollectionApplication.getOfflineDatabasesTitle();

        //Declaring List to hold NonNull offline database titles
        ArrayList<String> databaseTitlesWithoutNull = new ArrayList<>();
        //Filtering NonNull database titles
        for (String title : databaseTitles) {
            if (title != null) {
                Log.i(TAG, "showOfflineMapsList(): title " + title + " is not null");
                databaseTitlesWithoutNull.add(title);
            } else {
                Log.i(TAG, "showOfflineMapsList(): title  is null");
            }
        }
        //Handle database's titles not null
        if (databaseTitlesWithoutNull.size() > 0) {
            Log.i(TAG, "showOfflineMapsList() : database titles without null size = " + databaseTitlesWithoutNull.size());
            //declaring array to hold database titles
            String[] titles = new String[databaseTitlesWithoutNull.size()];
            //Converting ArrayList to array
            titles = databaseTitlesWithoutNull.toArray(titles);
            //calling method to display dialog with available offline database titles
            displayTitlesOfflineMapDialog(mCurrent, mapView, titles, databaseTitles);
        } else {
            Log.i(TAG, "showOfflineMapsList() : displaying No Offline Map Dialog");

            mCurrent.showNoOfflineMapDialog(context);
        }
    }

    private void showNoOfflineMapDialog(MapActivity context) {
        try {
            Utilities.showAlertDialog(context, getString(R.string.no_offline_version), "Ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayTitlesOfflineMapDialog(final MapActivity context, final MapView mapView, final String[] titles, final ArrayList<String> databaseTitles) {
        new AlertDialog.Builder(context)
                .setItems(titles, (dialog, which) -> {
                    try {
                        String selectedTitle = titles[which];
                        int selectedVersion = 0;
                        for (int i = 0; i < databaseTitles.size(); i++) {
                            if (selectedTitle.equals(databaseTitles.get(i))) {
                                selectedVersion = i + 1;
                                break;
                            }
                        }

                        Log.i(TAG, "Selected Version: " + selectedVersion);
                        onlineData = false;
                        currentOfflineVersion = selectedVersion; // TODO un comment
                        currentOfflineVersionTitle = selectedTitle;
                        presenter.addLocalLayers(mapView, baseMap, selectedVersion, selectedTitle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void goOffline() {
        try {
            MaterialDialog materialDialog = Utilities.showAlertDialogWithCustomView(mCurrent, R.layout.dialog_local_db_name, getString(R.string.cancel));

            EditText databaseName = (EditText) materialDialog.findViewById(R.id.local_db_name_edit_text);
            Button dialogCloseButton = (Button) materialDialog.findViewById(R.id.local_db_download_btn);

            dialogCloseButton.setOnClickListener(v -> {

                localDatabaseTitle = databaseName.getText().toString();
                if (localDatabaseTitle.equals("")) {
                    databaseName.setError(getString(R.string.name_validation));
                } else {
                    try {

                        menuItemGoOfflineMode.setVisible(false);
                        menuItemGoOnlineMode.setVisible(true);

                        materialDialog.dismiss();
                        Log.d(TAG, "Going offline ....");
                        //Async task
                        baseMap.getOperationalLayers().clear();
                        graphicsOverlay.getGraphics().clear();

                        Utilities.showLoadingDialog(mCurrent);
                        presenter.downloadAndSaveDatabase(DOWNLOAD_GEO_DATABASE, localDatabaseTitle, mapView.getVisibleArea().getExtent());

                    } catch (Exception e) {
                        Log.d(TAG, "Error in Going offline");
                        e.printStackTrace();
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (rlFragment.getVisibility() == View.VISIBLE) {

                Utilities.showConfirmDialog(mCurrent, "", "هل انت متأكد من الرجوع ؟", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == -1) {
                            resetAfterUpdate();
                            hideFragment();
                            showViews();
                        }
                        dialog.dismiss();
                    }
                });
            } else {
                Utilities.showConfirmDialog(mCurrent, "", "هل تريد الخروج ؟", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == -1) {
                            dialog.dismiss();
                            finish();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * --------------------------------------callbacks-------------------------------------------
     */

    @Override
    public void onSingleTap(Point point) {
        try {
            if (drawMeasure) {
                if (shapeType.matches(POLYLINE)) {
                    if (pointCollection.size() < 2) {
                        pointCollection.add(point);
                        drawLine(pointCollection);
                    } else {
                        Utilities.showToast(mCurrent, "Please undo, or press Done and remeasure again");
                    }
                }

            } else if (drawShape) {
                startDrawShape(point);
            } else if (!queryStatus) {

                graphicsOverlay.getGraphics().clear();
                selectedResult = null;
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                queryStatus = true;

                Log.i(TAG, "onSingleTap(): callback is Called ");
                Log.i(TAG, "onSingleTap(): taped point lat = " + point.getX() + " Lang = " + point.getY());

                presenter.prepareQueryResult();
                Utilities.showLoadingDialog(mCurrent);

                if (onlineData) {
//                    presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_DistributionBox_ServiceTable, FCL_DistributionBoxLayer);
                    presenter.queryCheckDataOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), autoReCloserTable, autoReCloserLayer);
                } else {
                    presenter.queryCheckDataOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), autoReCloserOfflineTable, autoReCloserLayer);

                }
            } else {
                Utilities.showToast(mCurrent, getString(R.string.please_wait));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onQueryOnline(ArrayList<OnlineQueryResult> results, FeatureLayer featureLayer, Point point) {
        try {
            /*if (featureLayer.equals(FCL_DistributionBoxLayer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_POLES_ServiceTable, FCL_POLES_Layer);
            } else if (featureLayer.equals(FCL_POLES_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_RMU_ServiceTable, FCL_RMU_Layer);
            } else if (featureLayer.equals(FCL_RMU_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_Substation_ServiceTable, FCL_Substation_Layer);
            } else if (featureLayer.equals(FCL_Substation_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), OCL_METER_ServiceTable, OCL_METER_Layer);
            } else if (featureLayer.equals(OCL_METER_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), ServicePoint_ServiceTable, ServicePoint_Layer);
            } else if (featureLayer.equals(ServicePoint_Layer)) {
                showQueryResult(results, point);
            } else */
            if (featureLayer.equals(autoReCloserLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is sub station layer");
                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), substationTable, substationLayer);
            } else if (featureLayer.equals(substationLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is sub station layer");
                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), stationTable, stationLayer);
            } else if (featureLayer.equals(stationLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is station layer");
                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), linkBoxTable, linkBoxLayer);

            } else if (featureLayer.equals(linkBoxLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is linkBoxLayer layer");
                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), fuseCutOutTable, fuseCutOutLayer);

            } else if (featureLayer.equals(fuseCutOutLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is fuseCutOutLayer layer");

                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), MV_MeteringTable, MV_MeteringLayer);

            } else if (featureLayer.equals(MV_MeteringLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is fuseCutOutLayer layer");
                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), PoleTable, PoleLayer);

            } else if (featureLayer.equals(PoleLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is PoleLayer layer");

                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), OH_LinesTable, OH_LinesLayer);

            } else if (featureLayer.equals(OH_LinesLayer)) {
                Log.i(TAG, "onQueryOnline(): featureLayer is OH_LinesLayer layer");
                presenter.queryCheckDataOnline(results, point, mapView.getSpatialReference(), MeterTable, MeterLayer);
            }
            else if (featureLayer.equals(MeterLayer)){
                Log.i(TAG, "onQueryOnline(): featureLayer is MeterLayer layer");
                showQueryResult(results, point);


            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onQueryOffline(ArrayList<OnlineQueryResult> results, FeatureLayer featureLayer, Point point) {
//        if (featureLayer.equals(FCL_DistributionBoxLayer)) {
//            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_POLESTableOffline, FCL_POLES_Layer);
//        } else if (featureLayer.equals(FCL_POLES_Layer)) {
//            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_RMUTableOffline, FCL_RMU_Layer);
//        } else if (featureLayer.equals(FCL_RMU_Layer)) {
//            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_SubstationTableOffline, FCL_Substation_Layer);
//        } else if (featureLayer.equals(FCL_Substation_Layer)) {
//            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), OCL_METERTableOffline, OCL_METER_Layer);
//        } else if (featureLayer.equals(OCL_METER_Layer)) {
//            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), ServicePointTableOffline, ServicePoint_Layer);
//        } else if (featureLayer.equals(ServicePoint_Layer)) {
//            showOfflineQueryResult(results, point);
//        }

        if (featureLayer.equals(autoReCloserLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is sub station layer");
            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), substationOfflineTable, substationLayer);
        } else if (featureLayer.equals(substationLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is sub station layer");
            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), stationOfflineTable, stationLayer);
        } else if (featureLayer.equals(stationLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is station layer");
            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), linkBoxOfflineTable, linkBoxLayer);

        } else if (featureLayer.equals(linkBoxLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is linkBoxLayer layer");
            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), fuseCutOutOfflineTable, fuseCutOutLayer);

        } else if (featureLayer.equals(fuseCutOutLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is fuseCutOutLayer layer");
            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), mvMeteringOfflineTable, MV_MeteringLayer);

        } else if (featureLayer.equals(MV_MeteringLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is MV_MeteringLayer layer");
            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(),poleOfflineTable, PoleLayer);
        }
        else if (featureLayer.equals(PoleLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is PoleLayer layer");

            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), OHLineOfflineTable, OH_LinesLayer);
        } else if (featureLayer.equals(OH_LinesLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is OH_LinesLayer layer");

            presenter.queryCheckDataOffline(results, point, mapView.getSpatialReference(), MeterOfflineTable, MeterLayer);
        }
        else if (featureLayer.equals(MeterLayer)) {
            Log.i(TAG, "onQueryOffline(): featureLayer is OH_LinesLayer layer");

            showOfflineQueryResult(results, point);
        }
    }

    @Override
    public void onSyncSuccess(boolean status) {
        if (status) {
            Utilities.showToast(mCurrent, "Sync complete");
            if (syncAndGoOnline) {
                goOnline();
            }
        } else {
            Utilities.showToast(mCurrent, "Database did not sync correctly!");
        }
    }

    @Override
    public void onShowOfflineViews(boolean status) {
        try {
            if (status) {
                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
                menuItemSync.setVisible(true);
                menuItemOnline.setVisible(true);
                if (presenter.isLocalGeoDatabase()) {
                    menuItemLoad.setVisible(false);
                } else {
                    menuItemLoad.setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteFeature(boolean status) {
        try {
            hideFragment();
            showViews();

            if (status) {
                Utilities.showToast(mCurrent, getString(R.string.deleted_successfully));
            } else {
                Utilities.showToast(mCurrent, getString(R.string.failed_to_deleted));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateFeature(boolean status, Throwable t) {
        Log.i(TAG, "onUpdateFeature(): is called");

        try {

            if (status) {
                Log.i(TAG, "onUpdateFeature(): updated successfully");
                Utilities.showToast(mCurrent, getString(R.string.updated_successfuly));
            } else {
                Utilities.showToast(mCurrent, getString(R.string.failed_to_update));
                Log.i(TAG, "onUpdateFeature(): failed to update");
                if (t != null) {
                    t.printStackTrace();
                }
            }
            resetAfterUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideFragmentFromActivity() {
        try {
            Log.i(TAG, "hideFragmentFromActivity(): is called");
            runOnUiThread(() -> {
                try {
                    hideFragment();
                    showViews();
                    Utilities.dismissLoadingDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetAfterUpdate() {
        try {
            Log.i(TAG, "resetAfterUpdate(): is called");
            selectedResult = null;
            presenter.resetOnlineQueryResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDownloadGeoDatabaseSuccess(boolean status, String folderPath, String geoDatabasePath) {
        try {
            if (status) {
                onlineData = false;
                presenter.loadMap(folderPath, geoDatabasePath, mapView, baseMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOfflineQueryResult(ArrayList<OnlineQueryResult> results, Point pointOnMap) {
        try {
            queryStatus = false;
            if (results != null && !results.isEmpty()) {
                Log.i(TAG, "showOnlineQueryResult(): there is feature");
                Log.i(TAG, "showOnlineQueryResult(): listQueryResults size = " + results.size());


                runOnUiThread((Runnable) () -> {
                    try {
                        double shortestDistance = 1000000000;
                        for (OnlineQueryResult result : results) {

                            Point endPoint = (Point) result.getFeatureOffline().getGeometry();

                            if (endPoint != null) {
                                Log.i(TAG, "showOnlineQueryResult(): endPoint != null");
                                Point edinburghGeographic = new Point(pointOnMap.getX(), pointOnMap.getY(), SpatialReferences.getWgs84());
                                Point darEsSalaamGeographic = new Point(endPoint.getX(), endPoint.getY(), SpatialReferences.getWgs84());

                                // Create a world equidistant cylindrical spatial reference for measuring planar distance.
                                SpatialReference equidistantSpatialRef = SpatialReference.create(54002);

                                // Project the points from geographic to the projected coordinate system.
                                Point startP = (Point) GeometryEngine.project(edinburghGeographic, equidistantSpatialRef);
                                Point endP = (Point) GeometryEngine.project(darEsSalaamGeographic, equidistantSpatialRef);

                                // Get the planar distance between the points in the spatial reference unit (meters).
                                double planarDistanceMeters = GeometryEngine.distanceBetween(startP, endP);
                                // Result = 7,372,671.29511302 (around 7,372.67 kilometers)

                                Log.i(TAG, "showOnlineQueryResult(): Start Point X = " + startP.getX() + " Start Point Y = " + startP.getY());
                                Log.i(TAG, "showOnlineQueryResult(): End Point X = " + endP.getX() + " Enf Point Y = " + endP.getY());
                                Log.i(TAG, "showOnlineQueryResult(): point id = " + result.getObjectID() + " distance = " + planarDistanceMeters);

                                if (planarDistanceMeters <= shortestDistance) {
                                    shortestDistance = planarDistanceMeters;
                                    selectedResult = result;
                                }
                            } else {
                                Log.i(TAG, "showOnlineQueryResult(): endPoint = null");

                            }
                        }
                        if (selectedResult != null) {

                            graphicsOverlay.getGraphics().add(new Graphic((Point) selectedResult.getFeatureOffline().getGeometry(), pictureMarkerSymbol));

                            Utilities.dismissLoadingDialog();
                            mAddPointLayout.setVisibility(View.GONE);
                            mEditPointLayout.setVisibility(View.VISIBLE);
                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


                        } else {
                            Utilities.dismissLoadingDialog();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.dismissLoadingDialog();
                    }
                });

            } else {
                runOnUiThread(() -> {
                    Utilities.dismissLoadingDialog();
                    Toast.makeText(mCurrent, getString(R.string.zoom_more), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (
                Exception e) {
            e.printStackTrace();
            Utilities.dismissLoadingDialog();
        }
    }

    private void showQueryResult(ArrayList<OnlineQueryResult> results, Point pointOnMap) {
        try {
            Log.i(TAG, "showQueryResult():is called");
            queryStatus = false;
            if (results != null && !results.isEmpty()) {
                Log.i(TAG, "processSelectQueryResultOnline(): there is feature");
                Log.i(TAG, "processSelectQueryResultOnline(): listQueryResults size = " + results.size());


                runOnUiThread((Runnable) () -> {
                    try {
                        double shortestDistance = 1000000000;
                        for (OnlineQueryResult result : results) {

                            Point endPoint = (Point) result.getFeature().getGeometry();

                            if (endPoint != null) {
                                Point edinburghGeographic = new Point(pointOnMap.getX(), pointOnMap.getY(), SpatialReferences.getWgs84());
                                Point darEsSalaamGeographic = new Point(endPoint.getX(), endPoint.getY(), SpatialReferences.getWgs84());

                                // Create a world equidistant cylindrical spatial reference for measuring planar distance.
                                SpatialReference equidistantSpatialRef = SpatialReference.create(54002);

                                // Project the points from geographic to the projected coordinate system.
                                Point startP = (Point) GeometryEngine.project(edinburghGeographic, equidistantSpatialRef);
                                Point endP = (Point) GeometryEngine.project(darEsSalaamGeographic, equidistantSpatialRef);

                                // Get the planar distance between the points in the spatial reference unit (meters).
                                double planarDistanceMeters = GeometryEngine.distanceBetween(startP, endP);
                                // Result = 7,372,671.29511302 (around 7,372.67 kilometers)

                                Log.i(TAG, "showOnlineQueryResult(): Start Point X = " + startP.getX() + " Start Point Y = " + startP.getY());
                                Log.i(TAG, "showOnlineQueryResult(): End Point X = " + endP.getX() + " Enf Point Y = " + endP.getY());
                                Log.i(TAG, "showOnlineQueryResult(): point id = " + result.getObjectID() + " distance = " + planarDistanceMeters);

                                if (planarDistanceMeters <= shortestDistance) {
                                    shortestDistance = planarDistanceMeters;
                                    selectedResult = result;
                                }
                            }
                        }
                        if (selectedResult != null) {
//                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pin);
//                                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

                            graphicsOverlay.getGraphics().add(new Graphic((Point) selectedResult.getFeature().getGeometry(), pictureMarkerSymbol));
                            selectedResult.getFeature().loadAsync();
                            selectedResult.getFeature().addDoneLoadingListener(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.i(TAG, "showQueryResult(): feature is loaded");
                                        Map<String, Object> attr = selectedResult.getFeature().getAttributes();
                                        for (String key : attr.keySet()) {
                                            try {
                                                Log.i(TAG, "showQueryResult(): " + key + " = " + attr.get(key));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            Utilities.dismissLoadingDialog();
                            mAddPointLayout.setVisibility(View.GONE);
                            mEditPointLayout.setVisibility(View.VISIBLE);
                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        } else {
                            Utilities.dismissLoadingDialog();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } else {
                runOnUiThread(() -> {
                    Utilities.dismissLoadingDialog();
                    Toast.makeText(mCurrent, getString(R.string.zoom_more), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideFragment() {
        try {
            Log.i(TAG, "hideFragment(): is called");
            fragmentManager.beginTransaction().remove(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.rlFragment))).commit();
            rlFragment.setVisibility(View.INVISIBLE);
            isFragmentShown = false;
            ActionBar actionBar = mCurrent.getSupportActionBar();

            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setTitle(getString(R.string.home));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditFragment(OnlineQueryResult selectedResult) {
        try {
            Log.i(TAG, "showEditingFragment(): is called");
            exitFullScreenMode();

            rlFragment.setVisibility(View.VISIBLE);
            EditFeatureFragment editInFeatureFragment = EditFeatureFragment.newInstance(mCurrent, presenter, selectedResult, onlineData);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                    .replace(R.id.rlFragment, editInFeatureFragment)
                    .addToBackStack(null)
                    .commit();

            isFragmentShown = true;

            hideActivityViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCheckDataEditFragment(Fragment fragment) {
        try {
            Log.i(TAG, "showEditingFragment(): is called");
            exitFullScreenMode();

            rlFragment.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                    .replace(R.id.rlFragment, fragment)
                    .addToBackStack(null)
                    .commit();

            isFragmentShown = true;

            hideActivityViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideActivityViews() {
        try {
            Log.i(TAG, "hideActivityViews(): is called");

            mCompass.setVisibility(View.GONE);
            fabFullScreen.setVisibility(View.GONE);
            mFabMeasureMenu.setVisibility(View.GONE);
            fabGeneral.setVisibility(View.GONE);
            fabLocation.setVisibility(View.GONE);
            mapLegend.setVisibility(View.GONE);
            tvLatLong.setVisibility(View.GONE);

            item_load_previous_offline.setVisible(false);
            menuItemOffline.setVisible(false);
            menuItemSync.setVisible(false);
            menuItemOnline.setVisible(false);
            menuItemGoOfflineMode.setVisible(false);
            menuItemGoOnlineMode.setVisible(false);
            menuItemOverflow.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showViews() {
        try {
            Log.i(TAG, "showViews(): is called");
            mCompass.setVisibility(View.VISIBLE);
            fabFullScreen.setVisibility(View.VISIBLE);
            mFabMeasureMenu.setVisibility(View.VISIBLE);
            fabLocation.setVisibility(View.VISIBLE);
            tvLatLong.setVisibility(View.VISIBLE);
            mapLegend.setVisibility(View.VISIBLE);

            if (onlineData) {
                Log.i(TAG, "showViews(): working online");
                item_load_previous_offline.setVisible(true);
                menuItemGoOfflineMode.setVisible(true);
                menuItemGoOnlineMode.setVisible(false);
                menuItemSync.setVisible(false);
                menuItemOnline.setVisible(false);
                menuItemOffline.setVisible(true);
            } else {
                Log.i(TAG, "showViews(): working offline");


                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
                menuItemSync.setVisible(true);
                menuItemOnline.setVisible(true);
                menuItemGoOfflineMode.setVisible(false);
                menuItemGoOnlineMode.setVisible(true);

            }

//            if (!onlineData) {
//                item_load_previous_offline.setVisible(true);
//                menuItemOffline.setVisible(false);
//                menuItemSync.setVisible(true);
//                menuItemOnline.setVisible(true);
//                menuItemGoOfflineMode.setVisible(false);
//                menuItemGoOnlineMode.setVisible(true);
//            } else {
//                item_load_previous_offline.setVisible(true);
//                menuItemGoOfflineMode.setVisible(true);
//                menuItemGoOnlineMode.setVisible(false);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ---------------------------------------Draw------------------------------------------------
     */

    private void handleFabMeasureAction() {
        try {
            Log.i(TAG, "handleFabMeasureAction(): is called");
            startDrawMode();
            startSupportActionMode(new androidx.appcompat.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                    MenuInflater inflater = getMenuInflater();
                    inflater.inflate(R.menu.menu_action_add_shape, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                    try {
                        switch (item.getItemId()) {
                            case R.id.item_Done:
                                Done();
                                mode.finish();
                                return true;
                            case R.id.item_undo:
                                undo();
                                return true;
                            case R.id.item_redo:
                                redo();
                                return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                    Done();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redo() {
        try {
            Log.i(TAG, "redo(): is called");
            if (pointCollection != null && lastPointStep != null) {
                if (pointCollection.size() < 2) {
                    pointCollection.add(lastPointStep);
                    drawLine(pointCollection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void undo() {
        try {
            Log.i(TAG, "undo(): is called");
            if (pointCollection.size() > 0) {
                Log.i(TAG, "undo(): pointCollection size = " + pointCollection.size());
                int lastIndex = pointCollection.size() - 1;
                lastPointStep = pointCollection.get(lastIndex);
                pointCollection.remove(lastIndex);
                if (pointCollection.size() >= 0) {
                    Log.i(TAG, "undo(): pointCollection size = " + pointCollection.size());
                    if (shapeType.matches(POLYLINE)) {
                        Log.i(TAG, "undo(): shapeType = POLYLINE");

                        drawLine(pointCollection);

                    } else if (shapeType.matches(POLYGON)) {

                    }
                    //                    drawShape();
                } else
                    graphicsOverlay.getGraphics().clear();
            } else {
                Toast.makeText(mCurrent, "No Steps", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Done() {
        try {
            Log.i(TAG, "Done(): is called");
            endDrawMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawLine(PointCollection points) {
        try {
            Log.i(TAG, "drawLine(): is called");
            drawGraphicLayer.getGraphics().clear();

            // create a new point collection for polyline
            Polyline polyline = new Polyline(points);

            //define a line symbol
            SimpleLineSymbol lineSymbol =
                    new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.argb(255, 255, 40, 0), 4.0f);
            Graphic line = new Graphic(polyline, lineSymbol);
            drawGraphicLayer.getGraphics().add(line);

            for (Point point : pointCollection) {
                drawGraphicLayer.getGraphics().add(new Graphic((Point) point, pictureMarkerSymbol));
            }

            if (pointCollection.size() == 2) {
                show(mMeasureLayerInfo);
                show(mMeasureInMeterLbl);
                show(mMeasureValueInMeterLbl);
                show(mMeasureInKMLbl);
                show(mMeasureValueInKMLbl);
                calculateDistanceBetweenTwoPoints(pointCollection.get(0), pointCollection.get(1), mapView.getSpatialReference());
            } else {
                hide(mMeasureLayerInfo);
                hide(mMeasureInMeterLbl);
                hide(mMeasureValueInMeterLbl);
                hide(mMeasureInKMLbl);
                hide(mMeasureValueInKMLbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Polygon createPolygon() {
        //[DocRef: Name=Create Polygon, Category=Fundamentals, Topic=Geometries]
        // create a Polygon from a PointCollection
        PointCollection coloradoCorners = new PointCollection(SpatialReferences.getWgs84());

        coloradoCorners.add(-109.048, 40.998);
        coloradoCorners.add(-102.047, 40.998);
        coloradoCorners.add(-102.037, 36.989);
        coloradoCorners.add(-109.048, 36.998);

        Polygon polygon = new Polygon(coloradoCorners);

        //[DocRef: END]
        Polygon projectedPolygon = (Polygon) GeometryEngine.project(polygon, mapView.getSpatialReference());

        double area = GeometryEngine.area(projectedPolygon);

        return polygon;
    }

    private void startDrawMode() {
        try {
            Log.i(TAG, "startDrawMode(): is called");
            drawShape = true;

            drawGraphicLayer = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(drawGraphicLayer);

            hide(mFabMeasureMenu);
            hide(fabLocation);
            hide(fabGeneral);
            hide(fabFullScreen);
            hide(mCompass);
            hide(mapLegend);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endDrawMode() {
        try {
            Log.i(TAG, "endDrawMode(): is called");
            drawGraphicLayer.getGraphics().clear();
            pointCollection.clear();
            drawMeasure = false;

            hide(mMeasureLayerInfo);
            show(mFabMeasureMenu);
            show(fabLocation);
            show(fabFullScreen);
            show(mCompass);
            show(mapLegend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hide(View v) {
        try {
            Log.i(TAG, "hide(): is called");
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show(View v) {
        try {
            Log.i(TAG, "show(): is called");
            v.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateDistanceBetweenTwoPoints(Point edinburghGeographic, Point darEsSalaamGeographic, SpatialReference equidistantSpatialRef) {
        try {
            Log.i(TAG, "calculateDistanceBetweenTwoPoints(): is called");

            // Project the points from geographic to the projected coordinate system.
            Point edinburghProjected = (Point) GeometryEngine.project(edinburghGeographic, equidistantSpatialRef);
            Point darEsSalaamProjected = (Point) GeometryEngine.project(darEsSalaamGeographic, equidistantSpatialRef);

            // Get the planar distance between the points in the spatial reference unit (meters).
            double planarDistanceMeters = GeometryEngine.distanceBetween(edinburghProjected, darEsSalaamProjected);

            // Result = 7,372,671.29511302 (around 7,372.67 kilometers)

            Log.i(TAG, "calculateDistanceBetweenTwoPoints(): distance in Meter = " + planarDistanceMeters);
            Log.i(TAG, "calculateDistanceBetweenTwoPoints(): distance in KM = " + (planarDistanceMeters / 1000));

            mMeasureValueInMeterLbl.setText(String.valueOf(Utilities.round(planarDistanceMeters, 2)));

            mMeasureValueInKMLbl.setText(String.valueOf(Utilities.round((planarDistanceMeters / 1000), 2)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
