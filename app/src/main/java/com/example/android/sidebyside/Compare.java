package com.example.android.sidebyside;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

/**
 * Created by Gongwei (David) Chen on 6/8/2018.
 */

public class Compare extends AppCompatActivity implements View.OnClickListener{
    Uri uri1;
    Uri uri2;
    ImageView photo1;
    ImageView photo2;
    TextView date1;
    TextView date2;
    TextView dateDifference;
    private final int PICK_PHOTO1 = 1;
    private final int PICK_PHOTO2 = 2;
    // The ‘active pointer’ is the one currently moving our object.
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare);

        photo1 = (ImageView) findViewById(R.id.photo1);
        photo2 = (ImageView) findViewById(R.id.photo2);
        photo1.setOnClickListener(this);
        photo2.setOnClickListener(this);

        date1=(TextView)findViewById(R.id.date1);
        date2=(TextView)findViewById(R.id.date2);
        dateDifference=(TextView) findViewById(R.id.dateDifference);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compare, menu);
        return true;
    }

    /**ImageView BUTTON Onclick
     * triggered when either imageview is clicked. Differentiated by imageview.ID
     * @param v which imageview is click
     */
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.photo1:
                Intent pickPhoto1= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto1, PICK_PHOTO1);
                break;
            case R.id.photo2:
                Intent pickPhoto2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto2, PICK_PHOTO2);
                break;
        }
    }

    /**ImageView BUTTON HANDLER
     * Do different things based on which imageview is clicked, differentiated by requestCode
     * @param requestCode customized constants
     * @param resultCode  system constant RESULT_OK
     * @param data i dunno. Its an intent.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri1 = data.getData();
            try {
                date1.setText(getDate(uri1));
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
            try {
                Bitmap bitmap1= MediaStore.Images.Media.getBitmap(getContentResolver(), uri1);
                photo1.setImageBitmap(bitmap1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == PICK_PHOTO2 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri2 = data.getData();
            try {
                date2.setText(getDate(uri2));
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
            try {
                Bitmap bitmap2= MediaStore.Images.Media.getBitmap(getContentResolver(), uri2);
                photo2.setImageBitmap(bitmap2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }//OnActivityResult() ends

    /**For TOOLBAR
     * Do different tasks based on which button is clicked.
     * @param item buttons in the top toolbar
     * @return whichever button is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_photo1:
                photo1.setRotation(photo1.getRotation() + 90);
                return true;
            case R.id.action_photo2:
                photo2.setRotation(photo2.getRotation() + 90);
                return true;
            case R.id.action_done:
                if(uri1!=null&&uri2!=null) {
                    try {
                        dateDifference.setText(getDateDifference(uri1, uri2));
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this,"Make sure you have picked both photos",Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * For getting the date taken of photo with that uri
     * @param photoUri photo uri passed from imageview button handler
     * @return string MM/dd/yyyy
     */
    public CharSequence getDate(Uri photoUri){
        Long longDate=null;
        String[] projection=new String[] {MediaStore.Images.Media.DATE_TAKEN};
        Cursor cur=managedQuery(photoUri,projection,null,null,null);
        if(cur.moveToFirst()){//when cursor is empty
            int dateColumn=cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            do {
                longDate = cur.getLong(dateColumn);
            }while(cur.moveToNext());
        }
        Date d=new Date(longDate);
        java.text.DateFormat formatter=new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(d);
    }

    /**
     *
     * @param uri1
     * @param uri2
     * @return String difference in days (Date of uri2-Date of uri1)
     */
    public String getDateDifference(Uri uri1, Uri uri2){
        Long longDate1=null;//UNIX time in millisec
        Long longDate2=null;
        Long differenceInDays=null;

        String[] projection=new String[] {MediaStore.Images.Media.DATE_TAKEN};
        Cursor cur1=managedQuery(uri1,projection,null,null,null);

        //get UNIX time for first photo
        if(cur1.moveToFirst()){
            int dateColumn=cur1.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            do{
                longDate1=cur1.getLong(dateColumn);
            }while(cur1.moveToNext());
        }

        //get UNIX time for second photo
        Cursor cur2=managedQuery(uri2,projection,null,null,null);
        if(cur2.moveToFirst()){
            int dateColumn=cur2.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            do{
                longDate2=cur2.getLong(dateColumn);
            }while(cur2.moveToNext());
        }

        differenceInDays=(longDate2-longDate1)/1000/60/60/24;
        return differenceInDays.toString();
    }
}