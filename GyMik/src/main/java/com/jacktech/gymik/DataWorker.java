package com.jacktech.gymik;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataWorker {
	private static String NEWS_FILE_NAME = "news.data";
	private static String ROZVRH_FILE_NAME = "rozvrh.data";
	private static String SUPLOV_FILE_NAME = "suplov.data";
	private static String JIDLO_FILE_NAME = "jidlo.data";
	private static String ZNAMKY_FILE_NAME = "znamky.data";
	private static String MAP_FOLDER = "map/";
	private Context activity;

	public boolean mExternalStorageAvailable = false;
	public boolean mExternalStorageWriteable = false;

    private static DataWorker instance;

    private DataWorker(){}

    public static DataWorker getInstance(){
        if(instance == null)
            instance = new DataWorker();
        return instance;
    }

	public void init(Context activity){
		this.activity = activity;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}
	
	public void writeMap(JSONObject object,int floor){
		if(mExternalStorageWriteable){
			File dataDir = activity.getExternalFilesDir(null);
			new File(dataDir.getAbsolutePath()+"/map/").mkdir();
			try {
				FileWriter writer = new FileWriter(dataDir.getAbsolutePath()+"/"+MAP_FOLDER+"map_"+floor+".dat");
				writer.write(object.toString());
                writer.flush();
				writer.close();
			} catch (IOException e) {
			}
		}
	}
	
	public void writeJidlo(JSONObject jidlo){
		if(mExternalStorageWriteable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				FileWriter writer = new FileWriter(dataDir.getAbsolutePath()+"/"+JIDLO_FILE_NAME);
                writer.write(jidlo.toString());
                writer.flush();
				writer.close();
			} catch (IOException e) {
			}
		}
	}
	
	public JSONObject getJidlo(){
		if(mExternalStorageAvailable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath()+"/"+JIDLO_FILE_NAME));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null)
                    builder.append(line).append("\n");
				JSONObject data = new JSONObject(builder.toString());
				reader.close();
				return data;
			} catch (IOException e) {
                e.printStackTrace();
				return null;
			} catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }else{
			return null;
		}
	}
	
	public JSONObject getMap(int floor){
		if(mExternalStorageAvailable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath()+"/"+MAP_FOLDER+"map_"+floor+".dat"));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null)
                    builder.append(line).append("\n");
                JSONObject data = new JSONObject(builder.toString());
				reader.close();
				return data;
			} catch (IOException e) {
                e.printStackTrace();
				return null;
			} catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }else{
			return null;
		}
	}
	
	public void writeNews(List<RSSFeed> data){
		if(mExternalStorageWriteable){
            try{
                JSONObject datax = new JSONObject();
                JSONArray newsList = new JSONArray();
                for(RSSFeed feed : data){
                    JSONObject news = new JSONObject();
                    news.put("title", feed.getTitle());
                    news.put("description", feed.getDescription());
                    news.put("link", feed.getLink());
                    news.put("date", feed.getPubDate());
                    newsList.put(news);
                }
                datax.put("news", newsList);
                File dataDir = activity.getExternalFilesDir(null);
                try {
                    FileWriter writer = new FileWriter(dataDir.getAbsolutePath()+"/"+NEWS_FILE_NAME);
                    writer.write(datax.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
		}
	}
	
	public void writeZnamky(JSONObject znamky){
		if(mExternalStorageWriteable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				FileWriter writer = new FileWriter(dataDir.getAbsolutePath()+"/"+ZNAMKY_FILE_NAME);
				writer.write(znamky.toString());
                writer.flush();
				writer.close();
			} catch (IOException e) {
                e.printStackTrace();
			}
		}
	}
	
	public JSONObject getZnamky(){
		if(mExternalStorageAvailable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath()+"/"+ZNAMKY_FILE_NAME));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null)
                    builder.append(line).append("\n");
                JSONObject data = new JSONObject(builder.toString());
				reader.close();
				return data;
			} catch (IOException e) {
                e.printStackTrace();
				return null;
			} catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }else{
			return null;
		}
	}
	
	public void writeRozvrh(String string){
		if(mExternalStorageWriteable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				FileWriter writer = new FileWriter(dataDir.getAbsolutePath()+"/"+ROZVRH_FILE_NAME);
				writer.append(string);
				writer.flush();
				writer.close();
			} catch (IOException e) {
			}
		}
	}
	
	public void writeSuplovani(JSONObject suplov){
		if(mExternalStorageWriteable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				FileWriter writer = new FileWriter(dataDir.getAbsolutePath()+"/"+SUPLOV_FILE_NAME);
				writer.write(suplov.toString());
                writer.flush();
				writer.close();
			} catch (IOException e) {
                e.printStackTrace();
			}
		}
	}
	
	public JSONObject getNews(){
		if(mExternalStorageAvailable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath()+"/"+NEWS_FILE_NAME));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null)
                    builder.append(line).append("\n");
                JSONObject data = new JSONObject(builder.toString());
				reader.close();
				return data;
			} catch (IOException e) {
                e.printStackTrace();
				return null;
			} catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }else{
			return null;
		}
	}
	
	public String getRozvrh(){
		if(mExternalStorageAvailable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath()+"/"+ROZVRH_FILE_NAME));
				StringBuilder sb = new StringBuilder();
				String s;
				while((s = reader.readLine())!=null)
					sb.append(s+"\n");
				reader.close();
				return sb.toString();
			} catch (IOException e) {
				return null;
			}
			
		}else{
			return null;
		}
	}
	
	public JSONObject getSuplovani(){
		if(mExternalStorageAvailable){
			File dataDir = activity.getExternalFilesDir(null);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath()+"/"+SUPLOV_FILE_NAME));
                StringBuilder builder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                    builder.append(line).append("\n");
                JSONObject data = new JSONObject(builder.toString());
				reader.close();
				return data;
			} catch (IOException e) {
                e.printStackTrace();
				return null;
			} catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }else{
			return null;
		}
	}

    public Context getContext() {
        return activity;
    }

    public Locale getLocale() {
        for(Locale l : Locale.getAvailableLocales()){
            if(l.getISO3Language().equals("cs"))
                return l;
        }
        return Locale.getDefault();
    }
}
