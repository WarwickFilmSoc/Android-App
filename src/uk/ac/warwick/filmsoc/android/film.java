package uk.ac.warwick.filmsoc.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class film extends Activity {
	
	private int filmId = -1;
	private String filmName = "";
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.film);
	     Button backButton = (Button) findViewById(R.id.btnBack);
	     backButton.setOnClickListener(returnToMain);
	     
	     Bundle extras = getIntent().getExtras();
	     filmName = extras.getString("filmName");
	     ((TextView)findViewById(R.id.txtFilmName)).setText(filmName);
	     ((TextView)findViewById(R.id.txtFilmScreenings)).setText(extras.getString("filmScreenings"));
	     
	     filmId = extras.getInt("filmId");

	     String reviewAndTag = downloadString("http://www.filmsoc.warwick.ac.uk/upages/csujba/othercontent/HEAD/www/html/content/android/review_plaintext.php?filmid=" + filmId);
	     int firstNL = reviewAndTag.indexOf("\n");
	     int secondNL = reviewAndTag.indexOf("\n", firstNL+1);
	     
	     ((TextView)findViewById(R.id.txtTag)).setText(reviewAndTag.substring(0,firstNL));
	     String reviewImage = reviewAndTag.substring(firstNL+1,secondNL);
	     ((TextView)findViewById(R.id.txtReview)).setText(reviewAndTag.substring(secondNL));
	     
	 
	     try{
		     updateFilmImage("http://www.filmsoc.warwick.ac.uk/include/image.php?rt=wi&maxwidth=400&maxheight=150&id=" + Integer.parseInt(reviewImage));
	     } catch (NumberFormatException e) {
	    	 // Cry.
	    	 ((ImageView)findViewById(R.id.imgFilm)).setImageBitmap(null);
	     }
	     
	 }	 
	 
	 public String downloadString(String url) {
	    	HttpClient client = new DefaultHttpClient();
			HttpGet getMethod = new HttpGet(url);
			String responseBody = "Error Downloading.";
			try {
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				responseBody=client.execute(getMethod, responseHandler);
				
			}
			catch (Throwable t) {
				Toast
					.makeText(this, "Request failed: "+t.toString(), 25000)
					.show();
			}
			return responseBody;
	    }
	 
	Button.OnClickListener returnToMain = new View.OnClickListener() {
   	  public void onClick(View v) {
   		  finish();
   	  }
   	};
   	
   
   	
   	// Download Image
   	Bitmap bmImg;
   	public void updateFilmImage(String fileUrl){
	   	URL myFileUrl = null; 
	   	try {
	   		myFileUrl= new URL(fileUrl);
	   	} catch (MalformedURLException e) {
		   	// TODO Auto-generated catch block
		   	e.printStackTrace();
	   	}
	   	try {
		   	HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
		   	conn.setDoInput(true);
		   	conn.connect();
		   	InputStream is = conn.getInputStream();		
		   	bmImg = BitmapFactory.decodeStream(is);
		   	((ImageView)findViewById(R.id.imgFilm)).setImageBitmap(bmImg);
		   
	   	} catch (IOException e) {
		   	// TODO Auto-generated catch block
		   	e.printStackTrace();
		} catch (Throwable t) {
		   	// Some other exception
			((ImageView)findViewById(R.id.imgFilm)).setImageBitmap(null);
	   	}

   	}
}
