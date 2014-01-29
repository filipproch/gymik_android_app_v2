package com.jacktech.gymik.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.jacktech.gymik.Config;
import com.jacktech.gymik.DataWorker;
import com.jacktech.gymik.GymikActivity;
import com.jacktech.gymik.R;
import com.jacktech.gymik.UpdateClass;

public class RozvrhFragment extends ExtraFragment implements UpdateClass.OnCompletitionListener {

    private static final String TAG = "RozvrhFragment";
    private View rootView;
	private int rType = 0;
	private String[] rozvrh;
    private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if(Config.getInstance().getConfigBoolean(Config.KEY_BAKALARI_DOWNLOADED)){
            rootView = inflater.inflate(R.layout.rozvrh_layout,container, false);
            WebView view = (WebView) rootView.findViewById(R.id.rozvrh_web_view);
            rozvrh = DataWorker.getInstance().getRozvrh().split("#####");
            if(rozvrh != null)
                view.loadDataWithBaseURL(null, "<style>"+getString(R.string.rozvrhStyle)+"</style>"+rozvrh[rType], "text/html", "utf-8", null);
            else
                view.loadDataWithBaseURL(null,getString(R.string.missingRozvrh), "text/html", "utf-8", null);
            Spinner viewTypeSpinner = (Spinner) rootView.findViewById(R.id.rozvrh_pick_type);
            String[] spinnerElements = new String[]{"Tento týden", "Příští týden", "Stálý rozvrh"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinnerElements);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewTypeSpinner.setAdapter(adapter);
            viewTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                        long arg3) {
                    rType = arg2;
                    switchRozvrh();
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }else{
            rootView = inflater.inflate(R.layout.znamky_unavailable_layout,container, false);
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

	protected void switchRozvrh() {
		WebView view = (WebView) rootView.findViewById(R.id.rozvrh_web_view);
		if(rozvrh != null && rozvrh.length > rType)
			view.loadDataWithBaseURL(null, "<style>"+getString(R.string.rozvrhStyle)+"</style>"+rozvrh[rType], "text/html", "utf-8", null);
		else
			view.loadDataWithBaseURL(null,getString(R.string.missingRozvrh), "text/html", "utf-8", null);
	}

    @Override
    public void onComplete(UpdateClass.Action action, boolean success, Bundle data) {

    }
}
