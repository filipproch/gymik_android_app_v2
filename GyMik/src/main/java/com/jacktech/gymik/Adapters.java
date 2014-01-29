package com.jacktech.gymik;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jacktech.gymik.bakalari.Predmet;
import com.jacktech.gymik.bakalari.Znamka;
import com.jacktech.gymik.meals.Meal;
import com.jacktech.gymik.meals.MealDay;
import com.jacktech.gymik.meals.MealManager;
import com.jacktech.gymik.meals.MealView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Adapters {

	public static class NavigationAdapter extends BaseAdapter{

		private List<NavigationItem> items;
		private Context context;
		private LayoutInflater layoutInflater;
		
		public NavigationAdapter(Context context, List<NavigationItem> list) {
			this.items = list;
			this.context = context;
			layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
        public int getItemViewType(int position) {
            return items.get(position).delimiter ? 1 : 0;
        }
		
		@Override
        public int getViewTypeCount() {
            return 2;
        }
		
		@Override
        public int getCount() {
            return items.size();
        }
 
        @Override
        public NavigationItem getItem(int position) {
            return items.get(position);
        }
 
        @Override
        public long getItemId(int position) {
            return position;
        }
		
		@Override
		public View getView(int position, View convertedView, ViewGroup group){
			View conv = convertedView;
			if(conv == null){
                conv = layoutInflater.inflate(R.layout.drawer_list_item, null);
                RelativeLayout headerLayout = (RelativeLayout) conv.findViewById(R.id.drawer_item_header);
                TextView headerText = (TextView) conv.findViewById(R.id.drawer_item_header_text);
                TextView itemText = (TextView) conv.findViewById(R.id.drawer_item_text);
                conv.setTag(new ViewHolder(headerLayout, headerText, itemText));
			}
			
			NavigationItem listElement = items.get(position);
			if(listElement != null && conv.getTag() != null){
                ViewHolder holder = (ViewHolder) conv.getTag();
                if(listElement.delimiter){
                    holder.headerLayout.setVisibility(View.VISIBLE);
                    holder.headerText.setText(listElement.header);
                }else{
                    holder.headerLayout.setVisibility(View.GONE);
                }
                holder.itemText.setText(listElement.text);
                //holder.itemText.setCompoundDrawablesWithIntrinsicBounds(listElement.drawable, 0, 0, 0);
				//t.setText(listElement.text);
				//t.setCompoundDrawablesWithIntrinsicBounds(listElement.drawable, 0, 0, 0);
			}
			return conv;	
		}

        private class ViewHolder{
            public RelativeLayout headerLayout;
            public TextView headerText;
            public TextView itemText;
            public ViewHolder(RelativeLayout headerLayout, TextView headerText, TextView itemText){
                this.headerLayout = headerLayout;
                this.headerText = headerText;
                this.itemText = itemText;
            }
        }
	}

    public static class ZnamkyAdapter extends BaseAdapter{

        private List<Znamka> items;
        private Context context;
        private LayoutInflater layoutInflater;

        public class DescendingComparator implements Comparator<Znamka> {
            public int compare(Znamka z1, Znamka z2) {
                return -1*z1.compareTo(z2);
            }
        }

        public ZnamkyAdapter(Context context, List<Znamka> list) {
            this.items = list;
            this.context = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Collections.sort(items, new DescendingComparator());
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View conv = view;
            if(conv == null){
                conv = layoutInflater.inflate(R.layout.znamky_znamka_menu_item, null);
            }

            Znamka listElement = items.get(i);
            if(listElement != null){
                TextView hodnota = (TextView) conv.findViewById(R.id.znamky_znamka_hodnota);
                TextView vaha = (TextView) conv.findViewById(R.id.znamky_znamka_vaha);
                TextView datum = (TextView) conv.findViewById(R.id.znamky_znamka_datum);
                TextView popis = (TextView) conv.findViewById(R.id.znamky_znamka_popis);
                if(hodnota != null && vaha != null && datum != null && popis != null){
                    String znamka = listElement.getHodnota().toString();
                    hodnota.setText(znamka);
                    hodnota.setBackgroundColor(R.drawable.ab_solid_example);
                    vaha.setText(listElement.getVaha()+"x");
                    datum.setText(listElement.getDateDisplay());
                    popis.setText(listElement.getPopis());
                }
            }else{
                TextView hodnota = (TextView) conv.findViewById(R.id.znamky_znamka_hodnota);
                TextView vaha = (TextView) conv.findViewById(R.id.znamky_znamka_vaha);
                TextView datum = (TextView) conv.findViewById(R.id.znamky_znamka_datum);
                TextView popis = (TextView) conv.findViewById(R.id.znamky_znamka_popis);
                if(hodnota != null && vaha != null && datum != null && popis != null){
                    hodnota.setText("");
                    hodnota.setBackgroundColor(Color.TRANSPARENT);
                    vaha.setText("");
                    datum.setText("");
                    popis.setText("popis");
                }
            }
            return conv;
        }
    }

	public static class PredmetyAdapter extends BaseAdapter {

		public static class PredmetyListItem{
			public Predmet p;
			public int type;
			public PredmetyListItem(Predmet p, int type){
				this.p = p;
				this.type = type;
			}
		}
		
		private List<PredmetyListItem> items;
		private Context context;
		private LayoutInflater layoutInflater;
		
		public PredmetyAdapter(Context context, List<PredmetyListItem> list) {
			this.items = list;
			this.context = context;
			layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/*@Override
		public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
				ViewGroup arg4) {
			View childView = arg3;
			if(childView == null){
				childView = layoutInflater.inflate(R.layout.znamky_predmet_menu_item_child, null);
			}
			
			Znamka z = items.get(arg0).p.znamky.get(arg1);
			TextView tx = (TextView) childView.findViewById(R.id.znamky_hodnota);
			if(tx != null && z != null){
				String znamka = z.hodnota+"";
				if(z.minus)
					znamka += "-";
				tx.setText(Html.fromHtml("<b>"+znamka+"</b> - "+z.vahaInfo+" ("+z.date+")"));
			}
			
			return childView;
		}*/

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
		public boolean hasStableIds() {
			return true;
		}

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View conv = view;
            if(conv == null){
                switch(items.get(i).type){
                    case 0:
                        conv = layoutInflater.inflate(R.layout.znamky_predmet_menu_item, null);
                        break;
                    case 1:
                        conv = layoutInflater.inflate(R.layout.znamky_predmet_item_header, null);
                        break;
                }
            }

            PredmetyListItem listElement = items.get(i);
            if(listElement != null && listElement.p != null){
                TextView tx = (TextView) conv.findViewById(R.id.znamky_predmet_name);
                if(tx != null){
                    //if(context.getResources().getBoolean(R.bool.showFullTitle) || listElement.p.name.length() <= 12)
                        tx.setText(listElement.p.getName());
                    /*else
                        tx.setText(listElement.p.name.substring(0, 12)+"...");*/
                }
                TextView tx2 = (TextView) conv.findViewById(R.id.znamky_predmet_average);
                double rounded = listElement.p.getPrumer();
                if(tx2 != null){
                    tx2.setText(rounded+"");
                    if(rounded > 4.49)
                        tx2.setTextColor(Color.parseColor("#e60000"));
                    else if(rounded > 2.49)
                        tx2.setTextColor(Color.parseColor("#ffd500"));
                    else if(rounded < 1.50)
                        tx2.setTextColor(Color.parseColor("#00d000"));
                    else
                        tx2.setTextColor(Color.parseColor("#ffffff"));
                }
                //t.setText(listElement.text);
                //t.setCompoundDrawablesWithIntrinsicBounds(listElement.drawable, 0, 0, 0);
            }
            return conv;
        }
    }
	
	public static class NewsAdapter extends BaseAdapter{

		private JSONArray items;
        private Context context;
		private ImageGetter imgGetter = new ImageGetter(){

			@Override
			public Drawable getDrawable(String source) {
				/*try {
					return new ImageLoader(getContext()).execute(source).get();
				} catch (InterruptedException e) {
					Log.e("Adapters.ImageGetter", "InterruptedException, image loader failed");
				} catch (ExecutionException e) {
					Log.e("Adapters.ImageGetter", "ExecutionException, image loader failed");
				}*/
				return null;
			}
			
		};
		
		public NewsAdapter(Context context, JSONArray list) {
			this.items = list;
            this.context = context;
		}

        @Override
        public int getCount() {
            return items.length();
        }

        @Override
        public JSONObject getItem(int i) {
            try {
                return items.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
		public View getView(int position, View convertedView, ViewGroup group){
			View conv = convertedView;
			if(conv == null){
				LayoutInflater inflater = LayoutInflater.from(context);
				conv = inflater.inflate(R.layout.news_item, null);
			}

            JSONObject newsElement = null;
            try {
                newsElement = items.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(newsElement != null){
                try{
                    TextView title = (TextView) conv.findViewById(R.id.news_item_title);
                    TextView text = (TextView) conv.findViewById(R.id.news_item_text);
                    title.setText((String)newsElement.get("title"));
                    String textData = (String)newsElement.get("description");
                    textData.replace("<div class=\"feed-description\">", "");
                    textData.replace("</div>", "");
                    if(textData.length() > 160){
                        int i = 160;
                        while(i < textData.length()-1){
                            if(textData.charAt(i) == ' ')
                                break;
                            i++;
                        }
                        textData = textData.substring(0,i);
                        }
                    Spanned data = Html.fromHtml(textData, imgGetter, null);
                    text.setText(data);
                }catch (JSONException e){
                    e.printStackTrace();
                }
			}
			return conv;	
		}
	}
	
	public static class JidloAdapter extends BaseAdapter{

        private static final String TAG = "JidloAdapter";
        private ArrayList<MealDay> mealDays;
        private Context context;

		public JidloAdapter(Context context, ArrayList<MealDay> mealDays) {
            this.mealDays = mealDays;
            this.context = context;
		}

        @Override
        public int getCount() {
            return mealDays.size();
        }

        @Override
        public MealDay getItem(int i) {
            return mealDays.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
		public View getView(int position, View convertedView, ViewGroup group){
			View conv = convertedView;
            LayoutInflater inflater = LayoutInflater.from(context);
			if(conv == null){
				conv = inflater.inflate(R.layout.jidlo_item, null);
                MealViewHolder holder = new MealViewHolder();
                holder.textViewsParent = (LinearLayout) conv.findViewById(R.id.jidlo_item_meals);
                holder.dayView = (TextView) conv.findViewById(R.id.jidlo_item_day);
                for(int i = 0;i < MealManager.getInstance().getMaxMeals();i++){
                    MealView tx = (MealView) inflater.inflate(R.layout.jidlo_item_meal, null);
                    holder.textViews.add(tx);
                    holder.textViewsParent.addView(tx);
                }
                conv.setTag(holder);
			}
			
			if(mealDays != null){
                MealViewHolder holder = (MealViewHolder) conv.getTag();
                MealDay menu = mealDays.get(position);
                holder.mealId = position;
                holder.orderedMeal = menu.getSelectedMeal();
                holder.dayView.setText(MealDay.getDateFromTimestamp(menu.getDay()));
                Log.v(TAG, "menuDay: "+holder.dayView.getText()+", selected="+menu.getSelectedMeal());
                for(int i = 0;i < holder.textViews.size();i++){
                    MealView mealView = holder.textViews.get(i);
                    if(menu.getMeals().size() > i){
                        Meal meal = menu.getMeals().get(i);
                        mealView.setText(meal.getType().toString()+" : "+meal.getName());
                        mealView.setVisibility(View.VISIBLE);
                        if(menu.getSelectedMeal() == i)
                            mealView.setBackgroundColor(Color.parseColor("#cccccc"));
                        else if(menu.getOrdered() == i)
                            mealView.setBackgroundColor(Color.parseColor("#e9725e"));
                        else
                            mealView.setBackgroundColor(Color.TRANSPARENT);
                        if(meal.getType() == Meal.Type.MAIN_DISH){
                            mealView.setTag(holder);
                            mealView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MealViewHolder holder1 = (MealViewHolder)view.getTag();
                                    Calendar c = Calendar.getInstance();
                                    if(!Config.getInstance().getConfigString(Config.KEY_JIDELNA_USERNAME).equals("-") && MealManager.getInstance().getMeals().get(holder1.mealId).getDay() > c.getTimeInMillis()){
                                        for(int i = 0;i < holder1.textViews.size();i++){
                                            MealView mealView = holder1.textViews.get(i);
                                            if(mealView.isOrdered()){
                                                if(i == MealManager.getInstance().getMeals().get(holder1.mealId).getSelectedMeal()){
                                                    mealView.setBackgroundColor(Color.parseColor("#cccccc"));
                                                }else{
                                                    mealView.setBackgroundColor(Color.TRANSPARENT);
                                                }
                                                mealView.setOrdered(false);
                                            }
                                        }

                                        MealView mView = (MealView) view;
                                        if(holder1.textViews.indexOf(mView) != MealManager.getInstance().getMeals().get(holder1.mealId).getSelectedMeal()){
                                            holder1.orderedMeal = holder1.textViews.indexOf(mView);
                                            mView.setOrdered(true);
                                            mView.setBackgroundColor(Color.parseColor("#e9725e"));
                                            MealManager.getInstance().orderMeal(holder1.mealId, holder1.orderedMeal);
                                        }else{
                                            MealManager.getInstance().orderMeal(holder1.mealId, -1);
                                        }
                                    }
                                }
                            });
                        }
                    }else{
                        mealView.setVisibility(View.GONE);
                    }
                }
			}
			return conv;	
		}

        private class MealViewHolder{
            public LinearLayout textViewsParent;
            public TextView dayView;
            public ArrayList<MealView> textViews = new ArrayList<MealView>();
            public int mealId = 0;
            public int orderedMeal = 0;
        }

	}
	
	public static class SuplovAdapter extends ArrayAdapter<JSONObject>{

		private List<JSONObject> data;
		private HashMap<Integer, String> days;
		
		public SuplovAdapter(Context context, int textViewResourceId,
				List<JSONObject> objects,HashMap<Integer,String> days) {
			super(context, textViewResourceId, objects);
			this.data = objects;
			this.days = days;
			String day = "";
			for(int i = 0;i<data.size();i++){
				if(days.get(i) != null)
					day = days.get(i);
				else
					days.put(i, day);
			}
		}
		
		@Override
		public View getView(int pos,View v, ViewGroup group){
			boolean createdView = false;
			if(v == null){
				v = LayoutInflater.from(getContext()).inflate(R.layout.suplov_item, null);
				createdView = true;
			}
			if(data.get(pos)!=null){
				String dayId = getDayFromDate(days.get(pos));
				JSONObject item = data.get(pos);
				TextView day = (TextView) v.findViewById(R.id.suplov_item_day);
				TextView detail = (TextView) v.findViewById(R.id.suplov_item_detail);
				TextView skupina = (TextView) v.findViewById(R.id.suplov_item_skupina);
				TextView mistnost = (TextView) v.findViewById(R.id.suplov_item_mistnost);
				TextView nahrazuje = (TextView) v.findViewById(R.id.suplov_item_nahrazuje);
				day.setText(dayId);
				if(item != null){
                    try {
                        String predmet = "";
                        if(item.has("predmet") && item.getString("predmet").length() > 0){
                            predmet = "("+item.get("predmet")+")";
                        }
                        String text = (String)item.get("hodina")+"h"+predmet+" - "+getStatusString(item.get("status"));
                        detail.setText(text);
                        if(skupina != null && item.has("skupina") && ((String)item.get("skupina")).length() > 0){
                            skupina.setText("Skupina: "+item.get("skupina"));
                        }else
                            if(createdView)
                                ((LinearLayout)skupina.getParent()).removeView(skupina);

                        if(mistnost != null && item.has("mistnost") && ((String)item.get("mistnost")).length() > 0)
                            mistnost.setText("Mistnost: "+item.get("mistnost"));
                        else
                            if(createdView)
                                ((LinearLayout)mistnost.getParent()).removeView(mistnost);

                        if(nahrazuje != null && item.has("nahrazuje") && ((String)item.get("nahrazuje")).length() > 0)
                            nahrazuje.setText("Učitel: "+item.get("nahrazuje"));
                        else
                            if(createdView)
                                ((LinearLayout)nahrazuje.getParent()).removeView(nahrazuje);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
				}else{
					detail.setText("CHYBA");
				}
			}
			return v;
		}


		private String getDayFromDate(String object) {
			Calendar c = Calendar.getInstance();
			for(int i = -1;i<2;i++){
				String date = (c.get(Calendar.DAY_OF_MONTH)+i)+"."+(c.get(Calendar.MONTH+1)+"."+c.get(Calendar.YEAR));
				if(date.equals(object)){
					switch(i){
						case -1: return "VČERA";
						case 0: return "DNES";
						case 1: return "ZÍTRA";
					}
				}
			}
			String[] dateSplit = object.split("\\.");
			return dateSplit[0]+"."+dateSplit[1]+".";
		}
		
	}
	
	private static class ImageLoader extends AsyncTask<String, Void, Drawable>{

		private Context context;
		
		public ImageLoader(Context c){
			this.context = c;
		}
		
		@Override
		protected Drawable doInBackground(String... params) {
			try {
				URL aURL = new URL(params[0]);
				URLConnection conn = aURL.openConnection();
				conn.connect();
		        InputStream is = conn.getInputStream();
		        BufferedInputStream bis = new BufferedInputStream(is);
		        Bitmap bm = BitmapFactory.decodeStream(bis);
		        bis.close();
		        is.close();

		        Drawable d =new BitmapDrawable(context.getResources(),bm);
		        return d;
	        } catch (IOException e) {
		        Log.e("DEBUGTAG", "Remote Image Exception", e);
	        }
			return null;
		}
		
	}
	
	public static String getStatusString(Object object) {
		if(object instanceof Long){
			switch(((Long)object).intValue()){
				case 0: return "suplovaná";
				case 1: return "odpadá";
				case 2: return "změna";
				case 3: return "zrušena";
				case 4: return "exkurze";
				case 5: return "jiná akce";
				case 6: return "spojí";
				case 7: return "výlet";
				case 8: return "přesun";
			}
		}
		return null;
	}
}
