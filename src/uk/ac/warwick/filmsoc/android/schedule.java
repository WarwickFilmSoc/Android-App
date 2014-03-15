package uk.ac.warwick.filmsoc.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.Button;


public class schedule extends Activity {
    /** Called when the activity is first created. */
	
	private LinearLayout filmContainer;
	
	private String[] weekFeeds;
	private int currentWeek;
	private int displayingWeek;
	
	private static final int SWIPE_MIN_DISTANCE = 70;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
	    if (gestureDetector.onTouchEvent(event))
	    	return true;
	    else
	    	return false;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weekFeeds = new String[11];
        setContentView(R.layout.main);
        filmContainer = (LinearLayout) findViewById(R.id.filmContainer);
        filmContainer.setOnTouchListener(gestureListener);
        
        updateWeek(0);
        
        Button prevButton = (Button) findViewById(R.id.btnPrev);
        prevButton.setOnClickListener(prevWeeknavHandler);
        Button nxtButton = (Button) findViewById(R.id.btnNext);
        nxtButton.setOnClickListener(nextWeeknavHandler);
        
        ImageView title = (ImageView) findViewById(R.id.title);
        title.setOnClickListener(titleWebsiteHandler);
        
        
     // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        
    }
    
    private void updateWeek(int week){
    	filmContainer.removeAllViews();
    	boolean successCode = processFeed("http://www.filmsoc.warwick.ac.uk/upages/csujba/othercontent/HEAD/www/html/content/android/feed.php?week=",week);
    	
    	Button prevButton = (Button) findViewById(R.id.btnPrev);
    	Button nxtButton = (Button) findViewById(R.id.btnNext);
    	TextView txtWeek = (TextView)findViewById(R.id.txtWeek);
    	
    	if(!successCode){
    		// Failed to Download
    		prevButton.setEnabled(false);
    		nxtButton.setEnabled(false);
    		txtWeek.setText("Error");
    		displayingWeek = -1;
    		createTV("Error: Could not download the screenings listing.\n\nPlease check you are connected to the internet.\n\n",filmContainer);
    		addRefreshButton();
    		return;
    	}
    	
    	if(displayingWeek == currentWeek)
    		prevButton.setEnabled(false);
    	else
    		prevButton.setEnabled(true);
    	
    	if(displayingWeek >= 10)
    		nxtButton.setEnabled(false);
    	else
    		nxtButton.setEnabled(true);    		
    		
        txtWeek.setText("Week " + displayingWeek);
    }
    
    private void nextWeek() {
    	updateWeek(++displayingWeek);    
    }
    
    private void prevWeek() {
    	updateWeek(--displayingWeek);
    }
    
    public String downloadString(String url) {
    	HttpClient client = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(url);
		String responseBody = "-1";
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody=client.execute(getMethod, responseHandler);
			
		}
		catch (Throwable t) {
			/*Toast
				.makeText(this, "Request failed: "+t.toString(), 25000)
				.show();*/
		}
		return responseBody;
    }
    
    public boolean processFeed(String url, int week){
    	String content = weekFeeds[week];
    	if(content == null)
    		content = downloadString(url + week);
    	
    	if(content == "-1") {
    		// There was an error downloading the listing.
    		return false;    	
    	}
    	 	
    	String[] films = content.split("\n");
    	
    	// Save which week we are displaying!!
    	displayingWeek = Integer.parseInt(films[0]);
    	
    	// Cache the week download for later reuse
    	weekFeeds[displayingWeek] = content;    	
    	
    	// Week loads as -1 on first load
    	// So save this as our current week
    	if(week == 0)
    		currentWeek = displayingWeek;
    	
    	ListView lstFilms = (ListView)findViewById(R.id.lstFilms);
    	lstFilms.setAdapter(new lstAdapter());
        
        lstFilms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
        		Intent i = new Intent(schedule.this,film.class);
        		i.putExtra("filmName",validFor.get(position).getId());
        		startActivity(i);				
			}        	
        	
        });
        
        
    	return true;
    }
    
    class lstAdapter extends ArrayAdapter {
    	lstAdapter() {
    		super(schedule.this,R.layout.lstindividualfilm,validFor);
    	}
    	
    	public View getView(int position,View convertView, ViewGroup parent){
    		LayoutInflater inflater = getLayoutInflater();
    		View row=inflater.inflate(R.layout.lstindividualfilm,parent,false);
    		
    		TextView filmtitle = (TextView)row.findViewById(R.id.lblFilmName);
    		filmtitle.setText(validFor.get(position).title());
    		  
    		TextView filmtime = (TextView)row.findViewById(R.id.lblFilmTime);
    		filmtime.setText(validFor.get(position).tagline());  
    		
    		
    		return(row);
    	}
    }
    
    
    public void addRefreshButton(){
    	
    	Button btn = new Button(this);
    	btn.setText("Refresh");
    	btn.setOnClickListener(refreshWeeks);
    	btn.setGravity(0x01);
    	
    	filmContainer.addView(btn);
    	
    }
    
    View.OnClickListener filmTapHandler = new View.OnClickListener() {
    	  public void onClick(View v) {
    		  Intent specificFilm = new Intent(schedule.this,film.class);
    		  specificFilm.putExtra("filmId",((FilmLinearLayout)v).filmId());    		  
    		  specificFilm.putExtra("filmName",((FilmLinearLayout)v).filmName());
    		  specificFilm.putExtra("filmScreenings",((FilmLinearLayout)v).filmScreenings());
    		  startActivity(specificFilm);
    	  }
    	};
    	
    Button.OnClickListener nextWeeknavHandler = new View.OnClickListener() {
       	  public void onClick(View v) {
       			  nextWeek();
       	  }
       	};
   	
   	Button.OnClickListener prevWeeknavHandler = new View.OnClickListener() {
   			public void onClick(View v) {
   				prevWeek();
   			}
   	};
   	
   	Button.OnClickListener refreshWeeks = new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(schedule.this, "Refreshing...", Toast.LENGTH_SHORT).show();
				updateWeek(0);
			}
	};
   	
   	Button.OnClickListener titleWebsiteHandler = new View.OnClickListener() {
			public void onClick(View v) {
			   	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.filmsoc.warwick.ac.uk"));
			   	startActivity(browserIntent);
			}
   	};
   	
   	GestureDetector.SimpleOnGestureListener flingHandler = new SimpleOnGestureListener() {
   		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
   			nextWeek();
   			return true;
   		}
   		
   	};
   	
   	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.layout.menu, menu);
        return true;
   	}
   	
   	@Override
   	public boolean onOptionsItemSelected(MenuItem item) {
   	        switch (item.getItemId()) {
   	        case R.id.refresh: 
   	        		Toast.makeText(schedule.this, "Refreshing...", Toast.LENGTH_SHORT).show();
   	                updateWeek(0);
   	        case R.id.currentweek: 
   	        		updateWeek(currentWeek);
   	        } 
   	        return false; //should never happen
   	}
    
   	private class FilmLinearLayout extends LinearLayout {
   		
   			protected int filmId;
   			protected String filmScreenings;
   			protected String filmName;
   			
   			public int filmId() { return filmId; }
   			public void filmId(int filmId) { this.filmId = filmId; }
   			
   			public String filmScreenings() { return filmScreenings; }
   			public void filmScreenings(String filmScreenings) { this.filmScreenings = filmScreenings; }
   			
   			public String filmName() { return filmName; }
   			public void filmName(String filmName) { this.filmName = filmName; }
   		
   			public FilmLinearLayout(Context context) { super(context); }
   			public FilmLinearLayout(Context context, AttributeSet attrs) { super(context, attrs); }
   	}
   	
   	private void leftSwipe(){
   		if(displayingWeek == currentWeek || displayingWeek == -1){
   			Toast.makeText(schedule.this, "Sorry, can't go back!", Toast.LENGTH_SHORT).show();
   			return;
   		}
   		prevWeek();   			
   	}
   	
   	private void rightSwipe(){   		
   		if(displayingWeek >= 10 || displayingWeek == -1){
   			Toast.makeText(schedule.this, "Sorry, can't go forward!", Toast.LENGTH_SHORT).show();
   			return;
   		}
   		nextWeek();
   	}
   	
   	private class MyGestureDetector extends SimpleOnGestureListener {
   	    @Override
   	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
   	        try {
   	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
   	                return false;
   	            // right to left swipe
   	            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
   	                rightSwipe();
   	            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
   	                leftSwipe();
   	            }
   	        } catch (Exception e) {
   	            // nothing
   	        }
   	        return false;
   	    }
   	    
   	}
}



