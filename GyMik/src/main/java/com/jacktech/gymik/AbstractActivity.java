package com.jacktech.gymik;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class AbstractActivity extends FragmentActivity {
	public DataWorker dw;
	public Config config;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dw = DataWorker.getInstance();
        dw.init(this);
		config = Config.getInstance();
        config.init(this);
	}
}
