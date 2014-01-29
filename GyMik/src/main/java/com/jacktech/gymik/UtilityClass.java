package com.jacktech.gymik;

import java.util.Calendar;

import android.R.color;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

public class UtilityClass {

	public static String getDateReadable(Long config) {
		String date = "nikdy";
		if(config != null){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(config);
			Calendar now = Calendar.getInstance();
			if(c.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) && c.get(Calendar.MONTH) == now.get(Calendar.MONTH))
				date = c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE);
			else
				date = c.get(Calendar.DAY_OF_MONTH)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR);
		}
		return date;
	}

}
