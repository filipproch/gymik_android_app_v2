package com.jacktech.gymik.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jacktech.gymik.Adapters;
import com.jacktech.gymik.Config;
import com.jacktech.gymik.R;
import com.jacktech.gymik.UpdateClass;
import com.jacktech.gymik.bakalari.Predmet;
import com.jacktech.gymik.bakalari.ZnamkyManager;

import org.json.JSONException;

import java.util.ArrayList;

public class ZnamkyFragment extends ExtraFragment implements UpdateClass.OnCompletitionListener{

    public static final String TAG = "ZnamkyFragment";
    private View rootView;
	private ArrayList<Adapters.PredmetyAdapter.PredmetyListItem> predmety;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle, this.getClass().getName());
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		if (rootView != null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
        try{
            ZnamkyManager.getInstance().loadMarksDb();
        }catch (JSONException e){
            e.printStackTrace();
        }
        if(Config.getInstance().getConfigBoolean(Config.KEY_BAKALARI_DOWNLOADED) && ZnamkyManager.getInstance().areThereMarks()){
            try {
                rootView = inflater.inflate(R.layout.znamky_layout,container, false);
            } catch (Exception e) {
            }
            predmety = ZnamkyManager.getInstance().getPredmety();
            TextView prospechTextView = (TextView) rootView.findViewById(R.id.znamkyCelkovyProspech);
            switch (ZnamkyManager.getInstance().getProspech()){
                case -1:
                    prospechTextView.setText(R.string.znamky_neprospel);
                    prospechTextView.setTextColor(Color.parseColor("#FF0000"));
                    break;
                case 0:
                    prospechTextView.setText(R.string.znamky_prospel);
                    prospechTextView.setTextColor(Color.parseColor("#000000"));
                    break;
                case 1:
                    prospechTextView.setText(R.string.znamky_prospel_vyznamenani);
                    prospechTextView.setTextColor(Color.parseColor("#00FF00"));
                    break;
                case 2:
                    prospechTextView.setText(getActivity().getString(R.string.znamky_unavailable));
                    prospechTextView.setTextColor(Color.parseColor("#000000"));
                    break;
            }
            TextView celkovyPrumer = (TextView) rootView.findViewById(R.id.znamkyCelkovyPrumer);
            double rounded = ZnamkyManager.getInstance().getPrumer();
            celkovyPrumer.setText(""+rounded);
            if(rounded > 4.49)
                celkovyPrumer.setTextColor(Color.parseColor("#e60000"));
            else if(rounded > 2.49)
                celkovyPrumer.setTextColor(Color.parseColor("#ffd500"));
            else if(rounded < 1.50)
                celkovyPrumer.setTextColor(Color.parseColor("#009d00"));
            else
                celkovyPrumer.setTextColor(Color.parseColor("#000000"));

            Adapters.PredmetyAdapter adapter = new Adapters.PredmetyAdapter(getActivity(), predmety);
            ListView view = (ListView) rootView.findViewById(R.id.znamky_list);
            if(view != null){
                view.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        showPredmetInfo(predmety.get(i).p);
                    }
                });
                view.setAdapter(adapter);
            }
        }else{
            try {
                rootView = inflater.inflate(R.layout.znamky_unavailable_layout,container, false);
            } catch (Exception e) {
            }
            UpdateClass.getInstance().setOnCompletitionListener(this);
            Log.v(TAG, Config.getInstance().getConfigString(Config.KEY_BAKALARI_USER));
            if(Config.getInstance().getConfigString(Config.KEY_BAKALARI_USER).equals("-")){
                final EditText usernameField = (EditText) rootView.findViewById(R.id.znamky_username);
                final EditText passwordField = (EditText) rootView.findViewById(R.id.znamky_password);
                Button znamkyDownload = (Button) rootView.findViewById(R.id.znamky_login);
                znamkyDownload.setVisibility(View.VISIBLE);
                znamkyDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(usernameField.length() > 0 && passwordField.length() > 0){
                            progressDialog = ProgressDialog.show(getActivity(), "Stahuji známky a rozvrh", "Autorizace...");
                            Config.getInstance().setConfigString(Config.KEY_BAKALARI_USER, usernameField.getText().toString());
                            Config.getInstance().setConfigString(Config.KEY_BAKALARI_PASSWORD, passwordField.getText().toString());
                            UpdateClass.getInstance().downloadBakalari();
                        }
                    }
                });
            }else{
                rootView.findViewById(R.id.znamky_login_form).setVisibility(View.GONE);
                Button znamkyLogin = (Button) rootView.findViewById(R.id.znamky_download_znamky);
                znamkyLogin.setVisibility(View.VISIBLE);
                znamkyLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog = ProgressDialog.show(getActivity(), "Stahuji známky a rozvrh", "Autorizace...");
                        UpdateClass.getInstance().downloadBakalari();
                    }
                });
            }
        }
		return rootView;
	}

    private void showPredmetInfo(Predmet predmet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.znamky_detail_dialog, null);
        builder.setTitle(predmet.getName());
        builder.setView(v);
        ListView list = (ListView) v.findViewById(R.id.znamky_detail_list);
        list.setAdapter(new Adapters.ZnamkyAdapter(getActivity(), ZnamkyManager.getInstance().getZnamky(predmet.getName())));
        builder.setPositiveButton(R.string.dialogOk, new OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /*protected void showZnamkaInfo(String predmet, Znamka z) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View v = getActivity().getLayoutInflater().inflate(R.layout.znamky_detail_dialog, null);
		builder.setTitle("Známka - "+predmet);
		builder.setView(v);
		String znamka = z.hodnota+"";
		if(z.minus)
			znamka += "-";
		((TextView)v.findViewById(R.id.znamky_detail_hodnota)).setText("Hodnota : "+znamka);
		((TextView)v.findViewById(R.id.znamky_detail_vaha)).setText("Váha : "+z.vaha+" ("+z.vahaInfo+")");
		((TextView)v.findViewById(R.id.znamky_detail_info)).setText("Popis : "+z.popis);
		((TextView)v.findViewById(R.id.znamky_detail_date)).setText("Ze dne : "+z.date);
		builder.setPositiveButton(R.string.dialogOk, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}*/

	private void displayError(String string) {
		if(rootView != null){
			/*TextView tx = (TextView) rootView.findViewById(R.id.errorText);
			if(tx != null){
				tx.setText(string);
				tx.setVisibility(View.VISIBLE);
			}*/
		}
	}

    @Override
    public void onComplete(UpdateClass.Action action, boolean success, Bundle data) {
        if(action == UpdateClass.Action.BAKALARI_LOGIN){
            if(success)
                if(progressDialog != null)
                    progressDialog.setMessage("Stahuji data...");
            else
                if(progressDialog != null)
                    progressDialog.setMessage("Neplatné údaje");
        }else if(action == UpdateClass.Action.BAKALARI_DOWNLOAD){
            if(success){
                if(progressDialog != null)
                    progressDialog.dismiss();
                callback(new Bundle());
            }else{
                if(progressDialog != null)
                    progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Stahování selhalo");
                builder.setMessage("Stažení známek a rozvrhu se nezdařilo");
                builder.setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        }
    }
}
