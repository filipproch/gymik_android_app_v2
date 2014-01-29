package com.jacktech.gymik;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.Calendar;

public class InstallActivity extends Activity {

	private Config config;
	private DataWorker dw;
	private int stage = 0;
	private ProgressBar downloadingClassesBar;
    private TextView downloadingInfoText;
	private boolean firstInstall = true;
	private String className =null;
	private Handler h;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

        getActionBar().hide();

		if(getIntent().hasExtra("install"))
			firstInstall = false;
		setContentView(R.layout.install_layout);
        dw = DataWorker.getInstance();
		config = Config.getInstance();
        UpdateClass.getInstance().init(this);
        UpdateClass.getInstance().setOnCompletitionListener(new UpdateClass.OnCompletitionListener() {
            @Override
            public void onComplete(UpdateClass.Action action, boolean success, Bundle data) {
                switch (action){
                    case SUPLOV_DOWNLOAD:
                        downloadingInfoText.setText("stahuji mapu školy");
                        UpdateClass.getInstance().downloadMap();
                        break;
                    case MAP_DOWNLOAD:
                        downloadingInfoText.setText("stahuji jídelníček");
                        UpdateClass.getInstance().downloadJidlo();
                        break;
                    case JIDLO_DOWNLOAD:
                        downloadingInfoText.setText("stahuji novinky");
                        UpdateClass.getInstance().downloadNews();
                        break;
                    case NEWS_DOWNLOAD:
                        downloadingInfoText.setText("dokončeno");
                        config.getConfigLong("lastSuplov", System.currentTimeMillis());
                        setContentView(R.layout.install_login);
                        break;
                }
            }
        });
		h = new Handler();
		stage = 1;
		ArrayList<String> classes = new ArrayList<String>();
		for(int i = 1;i<=8;i++){
			for(int j = 0;j<5;j++){
				classes.add(i+"."+getChar(j));
			}
		}
		final Spinner sp = (Spinner) findViewById(R.id.pickClassSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(InstallActivity.this,android.R.layout.simple_spinner_item,classes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);
        if(!Config.getInstance().getConfigString(Config.KEY_CLASS).equals("-"))
            sp.setSelection(classes.indexOf(Config.getInstance().getConfigString(Config.KEY_CLASS)));
		Button customClass = (Button) findViewById(R.id.customClassName);
		customClass.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				h.post(new Runnable() {
					
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(InstallActivity.this);
						final EditText input = new EditText(InstallActivity.this);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						        LinearLayout.LayoutParams.MATCH_PARENT,
						        LinearLayout.LayoutParams.MATCH_PARENT);
						input.setLayoutParams(lp);
						builder.setView(input);
						builder.setTitle("Zadejte název třídy");
						builder.setMessage("Název musí odpovídat označení na suplování, jinak aplikace nebude fungovat správně");
						builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								className = input.getText().toString().toUpperCase();
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
			}
		});
		Button cont = (Button) findViewById(R.id.installButton);
		cont.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				String schoolYear;
				if(c.get(Calendar.MONTH)<9){
					schoolYear = String.valueOf(c.get(Calendar.YEAR)-1).substring(2)+"/"+String.valueOf(c.get(Calendar.YEAR)).substring(2);
				}else
					schoolYear = String.valueOf(c.get(Calendar.YEAR)).substring(2)+"/"+String.valueOf(c.get(Calendar.YEAR)+1).substring(2);
				config.setConfigString(Config.KEY_SCHOOL_YEAR, schoolYear);
				if(className == null)
					config.setConfigString(Config.KEY_CLASS, sp.getSelectedItem().toString());
				else
					config.setConfigString(Config.KEY_CLASS, className);
				downloadSuplovAndRozvrh();
			}
		});
	}

    private void finishInstall(){
        if(firstInstall)
            startActivity(new Intent(InstallActivity.this, GymikActivity.class));
        else
            finish();
    }
	
	private char getChar(int j) {
		switch (j) {
			case 0:
				return 'A';
			case 1:
				return 'B';
			case 2:
				return 'C';
			case 3:
				return 'D';
			case 4:
				return 'E';
		}
		return '-';
	}

	protected void downloadSuplovAndRozvrh() {
		stage = 2;
		setContentView(R.layout.install_download_layout);
        downloadingInfoText = (TextView) findViewById(R.id.download_info_text);
        downloadingInfoText.setText("stahuji suplování");
		UpdateClass.getInstance().downloadSuplov();
	}

	@Override
	public void onBackPressed(){
		if(stage < 2){
			AlertDialog.Builder builder = new AlertDialog.Builder(InstallActivity.this);
			builder.setTitle(R.string.warning);
			builder.setCancelable(false);
			builder.setMessage(R.string.exitDialog1);
			builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.dialogExit, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					InstallActivity.this.finish();
				}
			});
			builder.create().show();
		}else{
			AlertDialog.Builder builder = new AlertDialog.Builder(InstallActivity.this);
			builder.setTitle(R.string.warning);
			builder.setCancelable(false);
			builder.setMessage(R.string.exitDialog2);
			builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
	}

	public void showDownloadErrorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(InstallActivity.this);
		builder.setTitle(R.string.warning);
		builder.setCancelable(false);
		builder.setMessage(R.string.noInternetDataDownloadFailed);
		builder.setPositiveButton(R.string.dialogExit, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				InstallActivity.this.finish();
			}
		});
		builder.create().show();
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
	
}
