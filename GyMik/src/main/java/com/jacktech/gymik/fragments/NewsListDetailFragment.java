package com.jacktech.gymik.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jacktech.gymik.Adapters.NewsAdapter;
import com.jacktech.gymik.DataWorker;
import com.jacktech.gymik.GymikActivity;
import com.jacktech.gymik.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NewsListDetailFragment extends ExtraListFragment{

	private View rootView;
	private JSONArray newsList;
	private boolean isTablet = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        try{
            if(DataWorker.getInstance().getNews() != null){
                newsList = DataWorker.getInstance().getNews().getJSONArray("news");
                NewsAdapter newsAdapter = new NewsAdapter(getActivity(), newsList);
                setListAdapter(newsAdapter);
                getListView().setDivider(getResources().getDrawable(R.drawable.transperent_color));
                getListView().setSelector(getResources().getDrawable(R.drawable.transperent_color));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int pos, long id){
        try{
            final JSONObject item = newsList.getJSONObject(pos);
            View newsDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.news_dialog, null);
            TextView text = (TextView) newsDialogView.findViewById(R.id.newsDialogText);
            text.setText(Html.fromHtml((String)item.get("description")));
            text.setMovementMethod(LinkMovementMethod.getInstance());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle((String)item.get("title"));
            builder.setView(newsDialogView);
            builder.setPositiveButton(R.string.dialogClose, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dialogLink, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse((String)item.get("link")));
                        startActivity(i);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
            builder.create().show();
        }catch (JSONException e){
            e.printStackTrace();
        }
	}
	
}
