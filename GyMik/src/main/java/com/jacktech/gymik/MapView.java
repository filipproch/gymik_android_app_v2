package com.jacktech.gymik;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapView extends View {

private static final int INVALID_POINTER_ID = -1;

    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;
    private boolean showColors = false;
    
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    
    private int currentLevel = 0;
    private JSONObject map;
    private Paint paint;
    private int coordsScaleFactor = 20;
    private boolean showRoomNames = false;
    
    private DataWorker dataWorker;
    
    private ArrayList<Line> drawLines;
    private ArrayList<Text> drawTexts;
    private ArrayList<Room> drawRooms;
    
    public MapView(Context context) {
        this(context, null, 0);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        dataWorker = DataWorker.getInstance();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        readMap();
        loadMap();
    }
    
    public void updateLevel(int level){
    	Log.i("MapView.updateLevel", "changing map floor : "+level);
    	this.currentLevel = level;
    	readMap();
    	loadMap();
    	super.invalidate();
    }
    
    public void setShowColors(boolean show){
    	this.showColors = show;
    }
    
    public void setShowRoomNames(boolean show){
    	this.showRoomNames = show;
    }
    
    private void readMap(){
    	map = dataWorker.getMap(currentLevel);
		Log.i("MapView.readMap", "Map loaded");
    }
    
    private void loadMap(){
    	if(map != null){
            try{
                drawLines = new ArrayList<Line>();
                drawRooms = new ArrayList<Room>();
                drawTexts = new ArrayList<Text>();
                JSONArray lines = map.getJSONArray("lines");
                for(int a = 0;a<lines.length();a++){
                    JSONArray line = lines.getJSONArray(a);
                    drawLines.add(new Line(line.getDouble(0), line.getDouble(1), line.getDouble(2),line.getDouble(3)));
                }

                JSONArray rooms = (JSONArray) map.get("rooms");
                if(rooms != null){
                    for(int a = 0;a<rooms.length();a++){
                        JSONArray line = rooms.getJSONArray(a);
                        drawRooms.add(new Room((float)line.getDouble(0), (float)line.getDouble(1), (float)line.getDouble(2), (float)line.getDouble(3), Integer.toHexString(line.getInt(4)), Integer.toHexString(line.getInt(5)), Integer.toHexString(line.getInt(6))));
                    }
                }

                JSONArray texts = (JSONArray) map.get("names");
                if(texts != null){
                    for(int a = 0;a<texts.length();a++){
                        JSONObject room  = texts.getJSONObject(a);
                        JSONArray pos = (JSONArray) room.get("position");
                        if(showRoomNames || room.get("room") == null)
                            drawTexts.add(new Text(pos.getDouble(0), pos.getDouble(1), room.getString("text")));
                        else
                            drawTexts.add(new Text(pos.getDouble(0), pos.getDouble(1), room.getString("room")));
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
    	}else{
    		Log.i("MapView.loadMap","unable to read map data");
    	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //ScaleGestureDetector kontroluje udalosti
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            final float x = ev.getX();
            final float y = ev.getY();

            mLastTouchX = x;
            mLastTouchY = y;
            mActivePointerId = ev.getPointerId(0);
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);

            // pohybujeme s mapou pouze pokud GestureDetector nedetekuje nejake gesto
            if (!mScaleDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                //if(mPosX+dx >= 60 && mPosX+dx <= -50*coordsScaleFactor)
                	mPosX += dx;
                //if(mPosY+dy >= 60 && mPosY+dy <= -40*coordsScaleFactor)
                	mPosY += dy;

                Log.i("MapView.move", "mPosX:"+mPosX+", mPosY:"+mPosY);
                	
                invalidate();
            }

            mLastTouchX = x;
            mLastTouchY = y;

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
            }
            break;
        }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        
        paint.setTextSize(12/10*coordsScaleFactor);
        paint.setFlags(Paint.LINEAR_TEXT_FLAG);
        
        for(Line l : drawLines){
        	canvas.drawLine(l.a.x*coordsScaleFactor, l.a.y*coordsScaleFactor, l.b.x*coordsScaleFactor, l.b.y*coordsScaleFactor, paint);
        }
        
        if(showColors){
	        for(Room r : drawRooms){
	        	paint.setColor(Color.parseColor("#ff"+r.r+r.g+r.b));
	        	paint.setStyle(Style.FILL);
	        	canvas.drawRect(r.p.x*coordsScaleFactor,r.p.y*coordsScaleFactor,r.p.x*coordsScaleFactor+r.w*coordsScaleFactor,r.p.y*coordsScaleFactor+r.h*coordsScaleFactor, paint);
	        }
        }
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.BLACK);
        
        for(Text t : drawTexts){
        	canvas.drawText(t.name, t.p.x*coordsScaleFactor, t.p.y*coordsScaleFactor, paint);
        }
        
        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            //Omezeni na priblizeni/oddaleni
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            invalidate();
            return true;
        }
    }
    
    class Position2D {
    	public float x,y;
    	public Position2D(double x,double y){
    		this.x = ((Double)x).floatValue();
    		this.y = ((Double)y).floatValue();
    	}
    }
    
    class Line{
    	public Position2D a;
    	public Position2D b;
    	public Line(double x1, double y1,double x2, double y2){
    		a = new Position2D(x1, y1);
    		b = new Position2D(x2,y2);
    	}
    }
    
    class Room{
    	public Position2D p;
    	public float w,h;
    	public String r,g,b;
    	public Room(float x1, float y1,float w,float h,String r,String g,String b){
    		p = new Position2D(x1,y1);
    		this.w = w;
    		this.h = h;
    		this.r = r;
    		this.g = g;
    		this.b = b;
    		repairColors();
    	}
    	
    	private void repairColors(){
    		if(r.length() == 1)
    			r = "0"+r;
    		if(g.length() == 1)
    			g = "0"+g;
    		if(b.length() == 1)
    			b = "0"+b;
    	}
    }
    
    class Text{
    	public Position2D p;
    	public String name;
    	public Text(double x, double y,String name){
    		p = new Position2D(x,y);
    		this.name = name;
    	}
    }

}