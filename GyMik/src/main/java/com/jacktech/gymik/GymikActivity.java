package com.jacktech.gymik;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.jacktech.gymik.Adapters.NavigationAdapter;
import com.jacktech.gymik.fragments.ExtraFragment;
import com.jacktech.gymik.fragments.JidloFragment;
import com.jacktech.gymik.fragments.MapFragment;
import com.jacktech.gymik.fragments.MoodleFragment;
import com.jacktech.gymik.fragments.NewsFragment;
import com.jacktech.gymik.fragments.OverviewFragment;
import com.jacktech.gymik.fragments.RozvrhFragment;
import com.jacktech.gymik.fragments.SuplovFragment;
import com.jacktech.gymik.fragments.ZnamkyFragment;
import com.jacktech.gymik.meals.MealDay;
import com.jacktech.gymik.meals.MealManager;
import com.jacktech.gymik.server.BackgroundService;

public class GymikActivity extends AbstractActivity implements UpdateClass.OnCompletitionListener, ExtraFragment.FragmentCallback, MealManager.MealManagerCallback{

    private static final String TAG = "GymikActivity";
    public static final String ACTION_BAR_TAB_POSITION = "ab_tab_position";
    private boolean settingsOpen = false;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private List<NavigationItem> mNavigationArray;
	private Tracker analyticsTracker;
	private int beforeSelected = 0;
	private ExtraFragment fragment;
	public static String MENU_POSITION = "menuPosition";
	private Handler h;
	private ActionBarDrawerToggle abDrawerToggle;
	private ProgressDialog progressDialog;
	private UpdateClass updateClass;
    private String schoolName;
    private Boolean hasShownMenuBeforeExit = false;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		this.setContentView(R.layout.navigation_layout);

        schoolName = getResources().getString(R.string.school_name);

		EasyTracker.getInstance().setContext(this);
		analyticsTracker = EasyTracker.getTracker();
		h = new Handler();
        MealManager.getInstance().setCallback(this);
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mNavigationArray = loadNavigation();
        mDrawerList.setAdapter(new NavigationAdapter(this,mNavigationArray));
        mDrawerList.setSelector(android.R.color.transparent);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				updateScreen(pos);
			}
		});
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        abDrawerToggle = new ActionBarDrawerToggle(this,
        		mDrawerLayout, 
        		R.drawable.ic_navigation_drawer,
        		R.string.drawer_open, R.string.drawer_close){
        	public void onDrawerClosed(View view) {
                //invalidateOptionsMenu();
        		super.onDrawerClosed(view);
                Log.v(TAG, "onDrawerClosed");
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            	//invalidateOptionsMenu();
                Log.v(TAG, "onDrawerOpened");
            }
        };
        mDrawerLayout.setDrawerListener(abDrawerToggle);
		updateClass = UpdateClass.getInstance();
        updateClass.init(this);
		updateClass.setOnCompletitionListener(this);
		
		if(savedInstanceState != null && savedInstanceState.containsKey(MENU_POSITION))
			beforeSelected = savedInstanceState.getInt(MENU_POSITION);

		updateScreen(beforeSelected);
		
		if(!backgroundServiceRunning() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			startService(new Intent(this, BackgroundService.class));

        PackageInfo pInfo = null;
        try {
            pInfo = GymikActivity.this.getPackageManager().getPackageInfo(GymikActivity.this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Package info loading failed: "+e.getLocalizedMessage());
        }
        if(pInfo != null && Config.getInstance().getConfigInt(Config.KEY_SHOW_UPDATES) != pInfo.versionCode){
            Config.getInstance().setConfigInt(Config.KEY_SHOW_UPDATES, pInfo.versionCode);
            showVersionDetails();
        }
	}

    private void showVersionDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GymikActivity.this);
        builder.setTitle(R.string.versionInfo);
        builder.setMessage(R.string.versionNodes);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.dialogClose, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    abDrawerToggle.syncState();
	}
	
	private boolean backgroundServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (BackgroundService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	private List<NavigationItem> loadNavigation() {
		ArrayList<NavigationItem> retList = new ArrayList<NavigationItem>();
        retList.add(new NavigationItem(0, "Přehled", "Menu"));
		retList.add(new NavigationItem(0, "Suplování", null));
		retList.add(new NavigationItem(0, "Mapa školy", null));
		retList.add(new NavigationItem(0, "Novinky", null));
		retList.add(new NavigationItem(0, "Moodle", null));
		retList.add(new NavigationItem(R.drawable.ic_znamky, "Moje známky", "Bakaláři"));
		retList.add(new NavigationItem(R.drawable.ic_timetable,"Rozvrh", null));
		retList.add(new NavigationItem(0,"Jídelníček", "Jídelna"));
		return retList;
	}

	public void updateScreen(final int screen){
        hasShownMenuBeforeExit = false;
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        UpdateClass.getInstance().setOnCompletitionListener(this);
        if(InfoHolder.getInstance().getBooleanInfo(InfoHolder.JIDELNICEK_SHOWING)){
            showJidelnicekAlert();
            return;
        }
		FragmentManager fragmentManager = getFragmentManager();
		
		Log.i("GymikActivity", "updateScreen, screen: "+screen);
		
		if(fragment != null)
			fragmentManager.beginTransaction().remove(fragment).commit();

		switch(screen){
            case 0:
                fragment = new OverviewFragment();
                break;
            case 1:
                fragment = new SuplovFragment();
                break;
            case 2:
                fragment = new MapFragment();
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                getActionBar().removeAllTabs();
                addActionBarTab(getString(R.string.prizemi));
                addActionBarTab(getString(R.string.p1patro));
                addActionBarTab(getString(R.string.p2patro));
                addActionBarTab(getString(R.string.vestavba));
                break;
            case 3:
                fragment = new NewsFragment();
                break;
            case 4:
                fragment = new MoodleFragment();
                break;
            case 5:
                fragment = new ZnamkyFragment();
                break;
            case 6:
                fragment = new RozvrhFragment();
                break;
            case 7:
                fragment = new JidloFragment();
                break;
			default:
                fragment = null;
				return;
		}
        fragment.setFragmentCallback(this);
	    fragmentManager.beginTransaction()
	                   .replace(R.id.content_frame, fragment)
	                   .commit();
	    Log.i("GymikActivity", "fragment_frame replaced with fragment");
	    mDrawerList.setItemChecked(screen, true);
        if(getResources().getBoolean(R.bool.showFullTitle))
            getActionBar().setTitle(schoolName + " - " + mNavigationArray.get(screen).text);
        else
            getActionBar().setTitle(mNavigationArray.get(screen).text);
        mDrawerLayout.closeDrawer(mDrawerList);
        beforeSelected = screen;
        Log.v(TAG, "screen updated");
	}

    private void addActionBarTab(String title) {
        ActionBar.Tab newTab = getActionBar().newTab();
        newTab.setText(title);
        newTab.setTabListener(tabListener);
        getActionBar().addTab(newTab);
    }

    private void showJidelnicekAlert() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        MealManager.getInstance().finishOrdering();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        InfoHolder.getInstance().setBooleanInfo(InfoHolder.JIDELNICEK_SHOWING, false);
                        GymikActivity.this.invalidateOptionsMenu();
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.jidelnicek_save_dialog).setPositiveButton("Objednat", dialogClickListener)
                .setNegativeButton("Zrušit", dialogClickListener)
                .setNeutralButton("Zpět", dialogClickListener).show();
    }

	/*private void startMoodleApp(){
		boolean isInstalled = false;

		PackageManager pm = getPackageManager();
		try{
			pm.getPackageInfo("com.moodle.moodlemobile", PackageManager.GET_ACTIVITIES);
			isInstalled = true;
		}catch(Exception e){
			isInstalled = false;
		}

		if(isInstalled){
			Intent i = pm.getLaunchIntentForPackage("com.moodle.moodlemobile");
			startActivity(i);
		}else{
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.moodle.moodlemobile"));
			startActivity(i);
		}
	}*/

	private void showFunctionPreparing() {
		h.post(new Runnable() {
			
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(GymikActivity.this);
				builder.setTitle("Funkce není dostupná");
				builder.setMessage("Tato funkce se připravuje a nyní není dostupná. Vyčkejte na uvolnění funkce v některé z následujících aktualizací.");
				builder.setPositiveButton(getText(R.string.dialogClose), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
			}
		});
	}

	@Override
	public void onResume(){
		super.onResume();
		Log.i("GymikActivity", "onResume");
		if(settingsOpen){
			settingsOpen = false;
		}
		updateScreen(beforeSelected);
	}
	
	public void openSettings(Activity a){
		settingsOpen = true;
		a.startActivity(new Intent(a, SettingsActivity.class));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case android.R.id.home:
				if(mDrawerLayout.isDrawerOpen(mDrawerList))
					mDrawerLayout.closeDrawer(mDrawerList);
				else
					mDrawerLayout.openDrawer(mDrawerList);
				break;
			case R.id.action_settings:
				openSettings(this);
				return true;
			case R.id.action_reload:
				int currentOrientation = getResources().getConfiguration().orientation;
				if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
				   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
				}
				else {
				   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
				}
                updateClass.setOnCompletitionListener(this);
				switch(beforeSelected){
					case 0:
						progressDialog.setMessage("Stahuji suplování...");
						progressDialog.show();
						updateClass.downloadSuplov();
						break;
					case 1:
						progressDialog.setMessage("Stahuji mapu...");
						progressDialog.show();
						updateClass.downloadMap();
						break;
					case 2:
						progressDialog.setMessage("Stahuji novinky...");
						progressDialog.show();
						updateClass.downloadNews();
						break;
					case 5:
					case 6:
						progressDialog.setMessage("Stahuji známky/rozvrh...");
						progressDialog.show();
						updateClass.downloadBakalari();
						break;
					case 8:
						progressDialog.setMessage("Stahuji jídelníček...");
						progressDialog.show();
						updateClass.downloadJidlo();
						break;
				}
				return true;
		}
		return false;
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	    savedInstanceState.putInt(MENU_POSITION, mDrawerList.getSelectedItemPosition());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        if(InfoHolder.getInstance().getBooleanInfo(InfoHolder.JIDELNICEK_SHOWING))
            getMenuInflater().inflate(R.menu.jidelnicek, menu);
        else
		    getMenuInflater().inflate(R.menu.gymik, menu);
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
	    EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
	    EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onComplete(UpdateClass.Action action, boolean success, Bundle data) {
        if(action == UpdateClass.Action.BAKALARI_LOGIN || action == UpdateClass.Action.JIDLO_LOGIN)
            return;
		progressDialog.dismiss();

        if(action == UpdateClass.Action.JIDLO_ORDER){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Objednávání jídla");
            StringBuilder builder1 = new StringBuilder();
            builder1.append("Bylo dokončeno objednávání jídel, stav :\n");
            for(MealDay mealDay : MealManager.getInstance().getMeals()){
                if(data.containsKey(mealDay.getDayString())){
                    if(data.getBoolean(mealDay.getDayString()))
                        builder1.append(MealDay.getDateFromTimestamp(mealDay.getDay())+" - objednáno\n");
                    else
                        builder1.append(MealDay.getDateFromTimestamp(mealDay.getDay())+" - selhalo\n");
                }
            }
            builder.setMessage(builder1.toString());
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    updateScreen(beforeSelected);
                }
            });
            builder.show();
            MealManager.getInstance().writeJidlo();
            InfoHolder.getInstance().setBooleanInfo(InfoHolder.JIDELNICEK_SHOWING, false);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            updateScreen(beforeSelected);
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        updateScreen(beforeSelected);
	}

    public void showJidelnicekMenu() {
        InfoHolder.getInstance().setBooleanInfo(InfoHolder.JIDELNICEK_SHOWING, true);
        invalidateOptionsMenu();
    }

    @Override
    public void callback(Bundle bundle) {
        if(bundle != null){
            String className = bundle.getString(ExtraFragment.FRAGMENT_NAME);
            if(className.equals(ZnamkyFragment.TAG)){
                FragmentManager fragmentManager = getFragmentManager();
                if(fragment != null)
                    fragmentManager.beginTransaction().remove(fragment).commit();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, new ZnamkyFragment())
                        .commit();
            }else if(className.equals(JidloFragment.TAG)){
                showJidelnicekMenu();
            }
        }
    }

    @Override
    public void mealManagerAction(MealManager.Action action) {
        switch (action){
            case MEAL_ORDERED:
                InfoHolder.getInstance().setBooleanInfo(InfoHolder.JIDELNICEK_SHOWING, true);
                break;
            case MEAL_ORDERING:
                progressDialog = ProgressDialog.show(this, "Odesílám objednávky...", "Probíhá autorizace");
                break;
        }
    }

    @Override
    public void onBackPressed(){
        if(InfoHolder.getInstance().getBooleanInfo(InfoHolder.JIDELNICEK_SHOWING)){
            showJidelnicekAlert();
        }else{
            if(!hasShownMenuBeforeExit){
                mDrawerLayout.openDrawer(mDrawerList);
                hasShownMenuBeforeExit = true;
                Log.v(TAG, "onBackPressed - pre app closed");
            }else{
                promptExit();
            }
        }
    }

    private void promptExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ukončit aplikaci?");
        builder.setMessage("Opravdu chcete aplikaci ukončit?");
        builder.setPositiveButton("Ano", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Bundle bundle = new Bundle();
            bundle.putInt(GymikActivity.ACTION_BAR_TAB_POSITION, tab.getPosition());
            fragment.action(ExtraFragment.ActionType.ACTION_BAR_TAB_SELECTED, bundle);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Bundle bundle = new Bundle();
            bundle.putInt(GymikActivity.ACTION_BAR_TAB_POSITION, tab.getPosition());
            fragment.action(ExtraFragment.ActionType.ACTION_BAR_TAB_UNSELECTED, bundle);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
    };

}
