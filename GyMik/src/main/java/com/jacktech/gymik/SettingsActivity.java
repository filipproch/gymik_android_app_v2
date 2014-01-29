package com.jacktech.gymik;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class SettingsActivity extends PreferenceActivity implements UpdateClass.OnCompletitionListener{

	private Handler h;
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		addPreferencesFromResource(R.layout.settings_layout);
		
		DataWorker dw = DataWorker.getInstance();
		UpdateClass.getInstance().init(this);
		UpdateClass.getInstance().setOnCompletitionListener(this);
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		h = new Handler();
		
		Preference aboutPreference = (Preference) findPreference("aboutApp");
		aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference p) {
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				PackageInfo pInfo;
				String version;
				try {
					pInfo = SettingsActivity.this.getPackageManager().getPackageInfo(SettingsActivity.this.getPackageName(), 0);
					version = pInfo.versionName;
				} catch (NameNotFoundException e) {
					pInfo = null;
					version = "";
				}
				
				Builder builder = new Builder(SettingsActivity.this);
		        builder.setMessage(SettingsActivity.this.getString(R.string.app_name)+" v"+version+" ©"+year+"\nvytvořil Filip Procházka")
		               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                       dialog.dismiss();
		                   }
		               });
		        AlertDialog dialog = builder.show();
		        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
		        messageText.setGravity(Gravity.CENTER);
		        dialog.show();
				return false;
			}
			
		});
		
		Preference authorWeb = (Preference) findPreference("authorWeb");
		authorWeb.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url = "http://gymik.jacktech.cz/";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return true;
			}
		});
		
		Preference changeClass = (Preference) findPreference("changeClass");
		changeClass.setTitle("Změnit třídu ("+Config.getInstance().getConfigString(Config.KEY_CLASS)+")");
		changeClass.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Config.getInstance().setConfigString(Config.KEY_CLASS, "-");
				SettingsActivity.this.finish();
				Intent i = new Intent(SettingsActivity.this,InstallActivity.class);
				i.putExtra("install", false);
				startActivity(i);
				return true;
			}
		});
		
		Preference updateSuplov = (Preference) findPreference("updateSuplov");
		updateSuplov.setTitle("Aktualizovat suplování (naposledy "+UtilityClass.getDateReadable(Config.getInstance().getConfigLong(Config.KEY_LAST_SUPLOVANI_DOWNLOAD))+")");
		updateSuplov.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				progressDialog.setMessage("Stahuji suplování...");
				progressDialog.show();
				UpdateClass.getInstance().downloadSuplov();
				return true;
			}
		});
		
		Preference updateJidlo = (Preference) findPreference("updateJidlo");
		updateJidlo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				progressDialog.setMessage("Stahuji jídelníček...");
				progressDialog.show();
				UpdateClass.getInstance().downloadJidlo();
				return true;
			}
		});
		
		Preference updateRozvrh = (Preference) findPreference("updateRozvrh");
		updateRozvrh.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				progressDialog.setMessage("Stahuji známky/rozvrh...");
				progressDialog.show();
				UpdateClass.getInstance().downloadBakalari();
				return true;
			}
		});
		
		Preference setupBakalari = (Preference) findPreference("setupBakalari");
		setupBakalari.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				h.post(new Runnable() {
					
					@Override
					public void run() {
						Builder builder = new Builder(SettingsActivity.this);
						builder.setTitle("Nastavit přihlašování");
						View v = getLayoutInflater().inflate(R.layout.login_layout, null);
						final EditText newBakUser = (EditText) v.findViewById(R.id.setupBakUser);
						newBakUser.setText(Config.getInstance().getConfigString(Config.KEY_BAKALARI_USER));
						final EditText newBakPsw = (EditText) v.findViewById(R.id.setupBakPsw);
						newBakPsw.setText(Config.getInstance().getConfigString(Config.KEY_BAKALARI_PASSWORD));
						builder.setView(v);
						builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Config.getInstance().setConfigString(Config.KEY_BAKALARI_USER, newBakUser.getText().toString());
								Config.getInstance().setConfigString(Config.KEY_BAKALARI_PASSWORD, newBakPsw.getText().toString());
								dialog.dismiss();
							}
						});
						builder.setNegativeButton(R.string.dialogClose, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						builder.show();
					}
				});
				return true;
			}
		});

        Preference setupJidelna = (Preference) findPreference("setupJidelna");
        setupJidelna.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                h.post(new Runnable() {

                    @Override
                    public void run() {
                        Builder builder = new Builder(SettingsActivity.this);
                        builder.setTitle("Nastavit přihlašování");
                        View v = getLayoutInflater().inflate(R.layout.login_layout, null);
                        final EditText newBakUser = (EditText) v.findViewById(R.id.setupBakUser);
                        newBakUser.setText(Config.getInstance().getConfigString(Config.KEY_JIDELNA_USERNAME));
                        final EditText newBakPsw = (EditText) v.findViewById(R.id.setupBakPsw);
                        newBakPsw.setText(Config.getInstance().getConfigString(Config.KEY_JIDELNA_PASSWORD));
                        builder.setView(v);
                        builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Config.getInstance().setConfigString(Config.KEY_JIDELNA_USERNAME, newBakUser.getText().toString());
                                Config.getInstance().setConfigString(Config.KEY_JIDELNA_PASSWORD, newBakPsw.getText().toString());
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.dialogClose, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
                return true;
            }
        });
		
		Preference updateMap = (Preference) findPreference("updateMap");
		updateMap.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				progressDialog.setMessage("Stahuji mapu...");
				progressDialog.show();
				UpdateClass.getInstance().downloadMap();
				return true;
			}
		});
		
		CheckBoxPreference updateSuplovAuto = (CheckBoxPreference) findPreference("updateSuplovAuto");
		updateSuplovAuto.setChecked(Config.getInstance().getConfigBoolean(Config.KEY_SUPLOVANI_AUTO_DOWNLOAD));
		updateSuplovAuto.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Config.getInstance().setConfigBool(Config.KEY_SUPLOVANI_AUTO_DOWNLOAD, newValue.equals(true));
				return true;
			}
		});
		
		final ListPreference updateSuplovTime = (ListPreference) findPreference("updateSuplovTime");
		updateSuplovTime.setTitle("Doba stahování ("+getDayTimeName(Config.getInstance().getConfigString(Config.KEY_SUPLOVANI_DOWNLOAD_TIME))+")");
		updateSuplovTime.setDefaultValue(Config.getInstance().getConfigString(Config.KEY_SUPLOVANI_DOWNLOAD_TIME));
		updateSuplovTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateSuplovTime.setTitle("Doba stahování ("+getDayTimeName((String) newValue)+")");
				Config.getInstance().setConfigString(Config.KEY_SUPLOVANI_DOWNLOAD_TIME, newValue.toString());
				return true;
			}
		});
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
	
	private String getDayTimeName(String value){
		if(value.equals("midnight"))
			return "Půlnoc";
		if(value.equals("school"))
			return "Kolem 11";
		if(value.equals("morning"))
			return "Ráno";
		if(value.equals("afternoon"))
			return "Odpoledne";
		return "";
	}

	@Override
	public void onComplete(UpdateClass.Action action, boolean success, Bundle bundle) {
		progressDialog.dismiss();
		Builder builder = new Builder(this);
		builder.setTitle("Stahování dokončeno");
		if(success){
			builder.setMessage("Stahování bylo úspěšně dokončeno");
		}else{
			builder.setMessage("Stahování bylo neúspěšné");
		}
		builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
}
