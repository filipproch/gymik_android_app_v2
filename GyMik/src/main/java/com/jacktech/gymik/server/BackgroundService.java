package com.jacktech.gymik.server;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.jacktech.gymik.Config;
import com.jacktech.gymik.DataWorker;
import com.jacktech.gymik.InitActivity;
import com.jacktech.gymik.R;
import com.jacktech.gymik.UpdateClass;
import com.jacktech.gymik.bakalari.Predmet;
import com.jacktech.gymik.bakalari.Znamka;
import com.jacktech.gymik.bakalari.ZnamkyManager;

import org.json.JSONException;

import java.util.Calendar;

public class BackgroundService extends Service implements UpdateClass.OnCompletitionListener, ZnamkyManager.OnZnamkyActionListener{

	private boolean serviceRunning = true;
	private DataWorker dw;
	private Config config;
	private UpdateClass updateClass;
	private Handler h;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		dw = DataWorker.getInstance();
        dw.init(this);
		config = Config.getInstance();
        config.init(this);
        updateClass = UpdateClass.getInstance();
        updateClass.init(this);
		updateClass.setOnCompletitionListener(this);
        try {
            ZnamkyManager.getInstance().init(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        h = new Handler();
		new Thread(downloadThread).start();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		serviceRunning = false;
	}
	
	private Runnable downloadThread = new Runnable() {
		
		@Override
		public void run() {
			Calendar c = Calendar.getInstance();
			while(serviceRunning){
				if(config.getConfigBoolean(Config.KEY_SUPLOVANI_AUTO_DOWNLOAD)){
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(config.getConfigLong(Config.KEY_LAST_SUPLOVANI_DOWNLOAD));
					String timeDownload = config.getConfigString(Config.KEY_SUPLOVANI_DOWNLOAD_TIME);
					if(cal.get(Calendar.DAY_OF_YEAR) != c.get(Calendar.DAY_OF_YEAR)){
						if(timeDownload.equals("midnight") && c.get(Calendar.HOUR_OF_DAY) == 0){
							h.post(new Runnable() {
								
								@Override
								public void run() {
									updateClass.downloadSuplov();
									updateClass.downloadBakalari();
								}
							});
							
						}
						if(timeDownload.equals("morning") && c.get(Calendar.HOUR_OF_DAY) == 6){
							h.post(new Runnable() {
								
								@Override
								public void run() {
									updateClass.downloadSuplov();
									updateClass.downloadBakalari();
								}
							});
						}
						if(timeDownload.equals("school") && c.get(Calendar.HOUR_OF_DAY) == 11){
							h.post(new Runnable() {
								
								@Override
								public void run() {
									updateClass.downloadSuplov();
									updateClass.downloadBakalari();
								}
							});
						}
						if(timeDownload.equals("afternoon") && c.get(Calendar.HOUR_OF_DAY) == 16){
							h.post(new Runnable() {
								
								@Override
								public void run() {
									updateClass.downloadSuplov();
									updateClass.downloadBakalari();
								}
							});
						}
					}
				}
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(config.getConfigLong(Config.KEY_LAST_WEEK_UPDATE));
				if(cal.get(Calendar.WEEK_OF_YEAR) != c.get(Calendar.WEEK_OF_YEAR)){
					h.post(new Runnable() {
						
						@Override
						public void run() {
							updateClass.downloadJidlo();
							updateClass.downloadMap();
							updateClass.downloadNews();
						}
					});
					
					config.setConfigLong(Config.KEY_LAST_WEEK_UPDATE, System.currentTimeMillis());
				}
				
				try {
					Thread.sleep(1000*60*5);
				} catch (InterruptedException e) {
				}
			}
		}
	};

	@Override
	public void onComplete(UpdateClass.Action action,boolean success,Bundle bundle) {
		switch (action){
            case SUPLOV_DOWNLOAD:
                if(success)
                    showSuplovDownload();
                break;
        }
	}

    @Override
    public void onAction(ZnamkyManager.Action action, Bundle actionInfo) {

        switch (action){
            case NEW_MARK:
                showNewMark(actionInfo);
                break;
        }

    }

    private void showNewMark(final Bundle actionInfo) {
        h.post(new Runnable() {

            @Override
            public void run() {
                Znamka z = Znamka.getZnamkaFromBundle(actionInfo);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(BackgroundService.this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Nová známka - "+actionInfo.get(Predmet.PREDMET))
                                .setContentText(z.getHodnota()+" ("+z.getVaha()+") - "+z.getPopis());
                Intent resultIntent = new Intent(BackgroundService.this, InitActivity.class);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, mBuilder.build());
            }
        });
    }

    private void showSuplovDownload() {
        h.post(new Runnable() {

            @Override
            public void run() {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(BackgroundService.this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Staženo aktuální suplování")
                                .setContentText("Bylo staženo aktuální suplování");
                Intent resultIntent = new Intent(BackgroundService.this, InitActivity.class);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, mBuilder.build());
            }
        });
    }
}
