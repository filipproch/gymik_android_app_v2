package com.jacktech.gymik;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.jacktech.gymik.bakalari.Predmet;
import com.jacktech.gymik.bakalari.Znamka;
import com.jacktech.gymik.bakalari.ZnamkyManager;
import com.jacktech.gymik.meals.Meal;
import com.jacktech.gymik.meals.MealDay;
import com.jacktech.gymik.meals.MealManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class UpdateClass {

	private Context context;
	private OnCompletitionListener onComplete = null;
    private static UpdateClass instance;
    private Handler handler;

    private UpdateClass(){}

    public static UpdateClass getInstance(){
        if(instance == null)
            instance = new UpdateClass();
        instance.handler = new Handler();
        return instance;
    }

    public enum Action{
        SUPLOV_DOWNLOAD, MAP_DOWNLOAD, JIDLO_DOWNLOAD, BAKALARI_DOWNLOAD, NEWS_DOWNLOAD, BAKALARI_LOGIN, JIDLO_LOGIN, JIDLO_ORDER;
    }

	public interface OnCompletitionListener{
		public void onComplete(UpdateClass.Action action, boolean success, Bundle data);
	}
	
	public void setOnCompletitionListener(OnCompletitionListener listener){
		this.onComplete = listener;
	}
	
	public void init(Context context){
		this.context = context;
	}
	
	public void downloadSuplov(){
		new SuplovDownloader().execute();
	}
	
	public void downloadMap() {
		new MapDownloader().execute();
	}
	
	public void downloadJidlo() {
		new JidloDownloader().execute();	
	}
	
	public void downloadBakalari(){
		new ZnamkyDownloader().execute();
	}
	
	public void downloadNews() {
		new NewsDownloader().execute();
	}

    public void orderJidlo() {
        new MealsOrderer().execute();
    }
	
	private class SuplovDownloader extends AsyncTask<Void, Void, JSONObject>{

		@Override
		protected JSONObject doInBackground(Void... params) {
            Config config = Config.getInstance();
            JSONObject data;
			try{
				//stahovani suplovani
				URL suplovUrl = new URL("http://gymik.jacktech.cz/suplov_parser.php?class="+config.getConfigString(Config.KEY_CLASS));
				URLConnection con1 = suplovUrl.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con1.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    builder.append(line);
				data = new JSONObject(builder.toString());
				reader.close();
				return data;
			}catch(IOException e){
				Log.i("DEBUG", e.getLocalizedMessage());
                e.printStackTrace();
				return null;
			} catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
		
		@Override
		protected void onPostExecute(JSONObject data){
			if(data != null){
				Config.getInstance().setConfigLong(Config.KEY_LAST_SUPLOVANI_DOWNLOAD, System.currentTimeMillis());
				DataWorker.getInstance().writeSuplovani(data);
				if(onComplete != null)
					onComplete.onComplete(Action.SUPLOV_DOWNLOAD, true, null);
			}else{
				if(onComplete != null)
					onComplete.onComplete(Action.SUPLOV_DOWNLOAD, false, null);
			}
		}
		
	}
	
	private class NewsDownloader extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
                Config config = Config.getInstance();
				//stahovani novinek
				URL suplovUrl = new URL("http://gymik.jacktech.cz/suplov_parser.php?class="+config.getConfigString(Config.KEY_CLASS));
				URLConnection con1 = suplovUrl.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con1.getInputStream()));
				reader.close();
				return true;
			}catch(IOException e){
				Log.i("NewsDownloader.doInBackground", "IOException: "+e.getLocalizedMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean data){
			if(onComplete != null)
				onComplete.onComplete(Action.NEWS_DOWNLOAD, data, null);
		}
		
	}
	
	private class JidloDownloader extends AsyncTask<Void, Void, JSONObject>{

        private static final String TAG = "JidloDownloader";

        @Override
		protected JSONObject doInBackground(Void... params) {
            try{
                Log.i(TAG,"starting");
                Config config = Config.getInstance();
                Calendar c = Calendar.getInstance();
                //stahovani suplovani
                PageLoader pageLoader = new PageLoader();
                ArrayList<NameValuePair> vars = new ArrayList<NameValuePair>();
                vars.add(new BasicNameValuePair("login_name", config.getConfigString(Config.KEY_JIDELNA_USERNAME)));
                vars.add(new BasicNameValuePair("login_password", config.getConfigString(Config.KEY_JIDELNA_PASSWORD)));
                vars.add(new BasicNameValuePair("canteenId","88"));
                vars.add(new BasicNameValuePair("submitLogin","Přihlásit"));
                String lPage = pageLoader.getPageNormal("http://nutricnihodnoty.cz/index/login/canteenId/88", vars);
                String webpage;
                if(lPage.contains("Špatné jméno nebo heslo")){
                    webpage = pageLoader.getPageNormal("http://nutricnihodnoty.cz/index/loadMenu/date/"+c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+"/canteenId/88",null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(onComplete != null)
                                onComplete.onComplete(Action.JIDLO_LOGIN, false, null);
                        }
                    });
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(onComplete != null)
                                onComplete.onComplete(Action.JIDLO_LOGIN, true, null);
                        }
                    });
                    webpage = pageLoader.getPageNormal("http://nutricnihodnoty.cz/visitor/loadMenu?date="+c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+"&canteenId=88",null);
                }
                JSONObject jsonData = new JSONObject();
                JSONArray daysData = new JSONArray();
                jsonData.put("data", daysData);

                String[] data1 = webpage.split("<div class=\"dayHead\">");
                for(int i = 1;i<data1.length-1;i++){
                    String line_data = data1[i].substring(data1[i].indexOf("<div class=\"colorHint\">"));
                    String day = line_data.substring(line_data.indexOf("</div>")+6);
                    day = day.substring(0, day.indexOf("</div>"));
                    day = day.trim().replaceAll("\n","");
                    day = day.substring(day.indexOf("-")+1);
                    JSONObject dayJSON = new JSONObject();
                    dayJSON.put("day",MealDay.getTimestampFromDate(day));
                    JSONArray jidla = new JSONArray();
                    dayJSON.put("data",jidla);
                    line_data = line_data.substring(line_data.indexOf("<table")+6);
                    line_data = line_data.substring(line_data.indexOf("<table"));
                    line_data = line_data.substring(0,line_data.indexOf("</table>"));
                    String[] data2 = line_data.split("<tr");
                    daysData.put(dayJSON);
                    for(int j = 1;j<data2.length;j++){
                        JSONObject jidlo = new JSONObject();
                        String dietId = data2[j].substring(data2[j].indexOf("dietId=\"")+8);
                        dietId = dietId.substring(0, dietId.indexOf("\""));
                        String mealId = data2[j].substring(data2[j].indexOf("menuId=\"")+8);
                        mealId = mealId.substring(0, mealId.indexOf("\""));
                        String date = data2[j].substring(data2[j].indexOf("date=\"")+6);
                        date = date.substring(0, date.indexOf("\""));
                        jidlo.put(Meal.MEAL_DIET_ID, Integer.parseInt(dietId));
                        jidlo.put(Meal.MEAL_MEAL_ID,Integer.parseInt(mealId));
                        boolean selected = (data2[j].contains("class=\"ordered\""));
                        jidlo.put(Meal.MEAL_SELECTED,selected);
                        String s1 = data2[j].substring(data2[j].indexOf("<b>")+3);
                        jidlo.put(Meal.MEAL_TYPE, Meal.Type.getFromString(s1.substring(0,s1.indexOf("</b>"))).value());
                        s1 = s1.substring(s1.indexOf("<td>")+4);
                        String name = s1.substring(0, s1.indexOf("</td>")).trim();
                        jidlo.put(Meal.MEAL_NAME,name);
                        jidla.put(jidlo);
                    }
                }
                return jsonData;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
		
		@Override
		protected void onPostExecute(JSONObject data){
			if(data != null){
				MealManager.getInstance().updateJidlo(data);
				if(onComplete != null)
					onComplete.onComplete(Action.JIDLO_DOWNLOAD, true, null);
			}else{
				if(onComplete != null)
					onComplete.onComplete(Action.JIDLO_DOWNLOAD, false, null);
			}
		}
		
	}

    private class MealsOrderer extends AsyncTask<Void, Void, Bundle>{

        private static final String TAG = "MealsOrderer";

        @Override
        protected Bundle doInBackground(Void... voids) {
            PageLoader pageLoader = new PageLoader();
            ArrayList<NameValuePair> vars = new ArrayList<NameValuePair>();
            vars.add(new BasicNameValuePair("login_name", Config.getInstance().getConfigString(Config.KEY_JIDELNA_USERNAME)));
            vars.add(new BasicNameValuePair("login_password", Config.getInstance().getConfigString(Config.KEY_JIDELNA_PASSWORD)));
            vars.add(new BasicNameValuePair("canteenId","88"));
            vars.add(new BasicNameValuePair("submitLogin","Přihlásit"));
            String lPage = pageLoader.getPageNormal("http://nutricnihodnoty.cz/index/login/canteenId/88", vars);
            String webpage;
            if(lPage.contains("Špatné jméno nebo heslo")){
                onComplete.onComplete(Action.JIDLO_LOGIN, false, null);
                Log.w(TAG, "wrong credentials - cannot order meals");
            }else{
                Log.v(TAG, "logged in, ordering");
                Bundle bundle = new Bundle();
                ArrayList<MealDay> mealDays = MealManager.getInstance().getMeals();
                for(MealDay day : mealDays){
                    if(day.getOrdered() == -1)
                        continue;
                    Meal meal = day.getMeal(day.getOrdered());
                    Log.v(TAG, "sending order - "+meal.getName());
                    ArrayList<NameValuePair> vars2 = new ArrayList<NameValuePair>();
                    vars2.add(new BasicNameValuePair("orderType", "order"));
                    vars2.add(new BasicNameValuePair("date",day.getDayString()));
                    vars2.add(new BasicNameValuePair("dietId",meal.getDietId()+""));
                    vars2.add(new BasicNameValuePair("menuId",meal.getMealId()+""));

                    String data = pageLoader.getPageNormal("http://nutricnihodnoty.cz/visitor/sendOrder", vars2);
                    if(data.contains("error")){
                        Log.v(TAG, "order failed - "+data);
                        day.setOrdered(-1);
                        bundle.putBoolean(day.getDayString(), false);
                    }else{
                        Log.v(TAG, "order success - "+data);
                        day.setSelectedMeal(day.getOrdered());
                        day.setOrdered(-1);
                        bundle.putBoolean(day.getDayString(), true);
                    }
                }
                Log.v(TAG, "ordering finished");
                return bundle;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bundle bool){
            onComplete.onComplete(Action.JIDLO_ORDER, true, bool);
        }
    }

	private class ZnamkyDownloader extends AsyncTask<Void, Void, Boolean>{

        private static final String TAG = "ZnamkyDownloader";
        private PageLoader pageLoader;
		
		private String getRozvrhPage(int week) throws IOException{
			Calendar c = Calendar.getInstance();
			int day = c.get(Calendar.DAY_OF_WEEK);
			String blankZnamky = pageLoader.getPage("https://bakalari.mikulasske.cz/prehled.aspx?s=6");
		    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    if(week == 1)
		    	nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$radiorozvrh", "rozvrh na příští týden"));
		    else if(week == 2)
		    	nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$radiorozvrh", "stálý rozvrh"));
		    nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$Flyrozvrh$checkucitel", "on"));
		    nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$Flyrozvrh$checkskupina", "on"));
		    nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$Flyrozvrh$Checkmistnost", "on" ));
			nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", PageLoader.getViewState(blankZnamky)));
			nameValuePairs.add(new BasicNameValuePair("__EVENTVALIDATION", PageLoader.getEventValidation(blankZnamky)));
	        
			return pageLoader.getPage("https://bakalari.mikulasske.cz/prehled.aspx?s=6", nameValuePairs);
		}
		
		private String getZnamkyPage() throws IOException{
			String blankZnamky = pageLoader.getPage("https://bakalari.mikulasske.cz/prehled.aspx?s=2");
		    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$Flyout2$Checktypy", "on"));
		    nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$Flyout2$Checkdatumy", "on"));
			nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$Checkdetail", "on"));
			nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", PageLoader.getViewState(blankZnamky)));
			nameValuePairs.add(new BasicNameValuePair("__EVENTVALIDATION", PageLoader.getEventValidation(blankZnamky)));
			return pageLoader.getPage("https://bakalari.mikulasske.cz/prehled.aspx?s=2", nameValuePairs);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			JSONObject data = new JSONObject();
            Config config = Config.getInstance();
			try{
				String bakUser = config.getConfigString(Config.KEY_BAKALARI_USER);
				String bakPsw = config.getConfigString(Config.KEY_BAKALARI_PASSWORD);
				if(!bakUser.equals("-") && !bakPsw.equals("-")){
					pageLoader = new PageLoader();
					String blankPage = pageLoader.getPage("https://bakalari.mikulasske.cz/login.aspx");
				    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			        nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$TextBoxjmeno", bakUser));
			        nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$TextBoxHeslo", bakPsw));
			        nameValuePairs.add(new BasicNameValuePair("ctl00$cphmain$ButtonPrihlas", ""));
			        nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",PageLoader.getViewState(blankPage)));
			        nameValuePairs.add(new BasicNameValuePair("__EVENTVALIDATION",PageLoader.getEventValidation(blankPage)));
			        
			        String page = pageLoader.getPage("https://bakalari.mikulasske.cz/login.aspx", nameValuePairs);
			        
			        data = new JSONObject();
			        
			        if(page.length() == 0){
                        Log.w(TAG, "Page length = 0");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(onComplete != null)
                                    onComplete.onComplete(Action.BAKALARI_DOWNLOAD, false, null);
                            }
                        });
			        	return false;
			        }
			        
			        if(page.contains("Přihlášení neproběhlo v pořádku.")){
                        Log.w(TAG, "Wrong credentials");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(onComplete != null)
                                    onComplete.onComplete(Action.BAKALARI_LOGIN, false, null);
                            }
                        });
                        Thread.sleep(3000);
			        	return false;
			        }else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(onComplete != null)
                                    onComplete.onComplete(Action.BAKALARI_LOGIN, true, null);
                            }
                        });
                        Log.v(TAG, "Login succesfull");
			        	String znamkyPage = getZnamkyPage();
			        	String rozvrhPage = getRozvrhPage(0);
			        	String rozvrhPageN = getRozvrhPage(1);
			        	String rozvrhPageS = getRozvrhPage(2);
                        try {
                            DataWorker.getInstance().writeRozvrh(parseRozvrh(parseRozvrh(rozvrhPage) + "\n#####\n" + parseRozvrh(rozvrhPageN) + "\n#####\n" + parseRozvrh(rozvrhPageS)));
                            parseZnamky(znamkyPage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return true;
			        }
				}
                Log.w(TAG, "Undefined login credentials");
				return false;
			}catch(IOException e){
				Log.i(TAG, e.getLocalizedMessage());
                e.printStackTrace();
				return false;
			} catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
		
		private String parseRozvrh(String page){
			if(page != null){
				Elements tableRozvrh = Jsoup.parse(page).getElementsByClass("rozbunka");
				if(tableRozvrh.size() > 0)
					return "<table cellspacing=\"0\" cellpadding=\"0\" class=\"rozbunka\">"+tableRozvrh.get(0).html()+"</table>";
				else
					return "error/noTable";
			}else{
				return "error/getPage";
			}
		}
		
		private void parseZnamky(String page) throws JSONException {
            if(page != null){
                if(page.contains("V zadaném období žádné známky.")){
                    ZnamkyManager.getInstance().setNoMarks();
                }
                //parsovani znamek
                HashMap<String,Predmet> predmety = new HashMap<String,Predmet>();
                Document doc = Jsoup.parse(page);
                Elements tableZnamky = doc.getElementsByClass("dettable");
                if(tableZnamky.size() < 1){
                    ZnamkyManager.getInstance().setNoMarks();
                }
                ZnamkyManager.getInstance().startMarksReload();
                String predmet = null;
                Elements trs = tableZnamky.get(0).getElementsByTag("tr");
                for(Element tr : trs){
                    Element td;
                    td = tr.child(0);

                    if(!td.text().equals("")){
                        predmet = td.text();
                        if(!predmety.containsKey(predmet)){
                            predmety.put(predmet, null);
                        }
                    }
                    td = tr.child(1);		// znamka
                    String zn = td.child(0).text(); //getElementValue(getFirstChild(td));

                    boolean minus = false;
                    if(zn.contains("-")) minus = true;

                    zn = zn.replaceAll("[^\\d]", "");
                    if(zn.equals("") || zn.equalsIgnoreCase("n")) zn = "0";
                    double znamka = Integer.parseInt(zn);
                    if(minus)
                        znamka += 0.5;

                    td = tr.child(2);		// popis
                    String popis = td.text();

                    td = tr.child(4);		// vaha info
                    String vahaInfo = td.text();

                    td = tr.child(5);		// vaha
                    String va = td.text();
                    va = va.replaceAll("[^\\d]", "");
                    if(va.equals("")) zn = "0";
                    int vaha = Integer.parseInt(va);

                    td = tr.child(6);		// datum
                    String date = td.text();
                    ZnamkyManager.getInstance().loadMark(predmet, new Znamka(znamka, vaha, vahaInfo, date, popis));
                }
                ZnamkyManager.getInstance().finishMarksReload();
                predmety.clear();
                //uklada se v ZnamkyManageru
            }else{
                //TODO: nejak zapsat chybu, informovat o ni uzivatele
            }
        }
		
		@Override
		protected void onPostExecute(Boolean data){
			if(onComplete != null)
				onComplete.onComplete(Action.BAKALARI_DOWNLOAD, data, null);
		}
		
	}
	
	private class MapDownloader extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
				for(int i = 0;i<4;i++){
					URL mapUrl = new URL("http://gymik.jacktech.cz/genmap.php?floor="+i+"&output=json");
					URLConnection con1 = mapUrl.openConnection();
					BufferedReader reader = new BufferedReader(new InputStreamReader(con1.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null)
                        builder.append(line).append("\n");
					DataWorker.getInstance().writeMap(new JSONObject(builder.toString()), i);
					reader.close();
				}
				return true;
			}catch(IOException e){
				Log.i("DEBUG", e.getLocalizedMessage());
				return false;
			} catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
		
		@Override
		protected void onPostExecute(Boolean data){
			if(onComplete != null)
				onComplete.onComplete(Action.MAP_DOWNLOAD, data, null);
		}
		
	}
	
	public String getPage(String url) throws IOException{
		URL suplovUrl = new URL("http://gymik.jacktech.cz/jidlo_parser.php");
		URLConnection con1 = suplovUrl.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(con1.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String s;
		while((s = reader.readLine()) != null)
			sb.append(s);
		return sb.toString();
	}

}
