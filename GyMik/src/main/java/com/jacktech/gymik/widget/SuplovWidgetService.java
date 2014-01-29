package com.jacktech.gymik.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class SuplovWidgetService extends RemoteViewsService{

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return(new SuplovViewsFactory(this.getApplicationContext(),intent));
	}

}
