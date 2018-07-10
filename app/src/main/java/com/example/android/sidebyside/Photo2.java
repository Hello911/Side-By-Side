package com.example.android.sidebyside;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.goodiebag.carouselpicker.CarouselPicker;

/**
 * Created by Gongwei (David) Chen on 6/8/2018.
 */

public class Photo2 extends AppCompatActivity implements View.OnTouchListener {

    private final int PICK_PHOTO = 1;
    ImageView photo;
    Uri uri;
    CarouselPicker carouselPicker;
    String[] dataArray;

    //DRAGGING & ZOOMING
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    //set of touch parameters for photo1
    private int mode = NONE;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();//PointF holds 2 coordinates
    private PointF mid = new PointF();
    private Bitmap bmap;
    private float oldDist = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo2);

        //If these 2 lines are not added, the toolbar will appear without icon
        android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        photo = (ImageView) findViewById(R.id.photo);
        photo.setOnTouchListener(this);

        carouselPicker = (CarouselPicker) findViewById(R.id.carousel);
        List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
        //20 here represents the textSize in dp, change it to the value you want.
        textItems.add(new CarouselPicker.TextItem("days", 20));
        textItems.add(new CarouselPicker.TextItem("pounds", 20));
        textItems.add(new CarouselPicker.TextItem("inches", 20));
        textItems.add(new CarouselPicker.TextItem("BMIs", 20));
        CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
        textAdapter.setTextColor(Color.MAGENTA);
        carouselPicker.setAdapter(textAdapter);

    }//onCreate()

    /**
     * To make the toolbar appear
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo, menu);
        return true;
    }

    /**
     * What happen when button in toolbar is clicked
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectPhoto:
                Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto1, PICK_PHOTO);
                break;

            case R.id.seeComment:
                if(uri!=null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    String message = "Note: ";
                    for (int j = 3; j < dataArray.length; j++) {
                        message += dataArray[j] + " ";
                    }
                    alertDialogBuilder.setMessage(message);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else
                    Toast.makeText(this,"Select a photo first to see note.",Toast.LENGTH_SHORT).show();
        }//ID switch
        return true;
    }//onOptionsItemSelected(MenuItem item) ends

    /**
     * Handle after user picks the photo
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(24)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //display the 1st select photo, and its data
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();
            //Display photo
            try {
                Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                photo.setImageBitmap(bitmap1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //read photo exif data
            InputStream in;
            String dataString = "Not found";
            try {
                in = getContentResolver().openInputStream(uri);
                ExifInterface exifInterface = new ExifInterface(in);
                dataString = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //this if else structure is to prevent empty String[] for photos w/o data
            if (dataString != null) {
                dataArray = dataString.split("\\s+");
            } else {
                dataString = "Nothing is here";
                dataArray = dataString.split("\\s+");
            }
            try {
                List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                textItems.add(new CarouselPicker.TextItem((String) getDate(uri), 15));
                textItems.add(new CarouselPicker.TextItem(dataArray[0] + "lbs", 15));
                textItems.add(new CarouselPicker.TextItem(dataArray[1] + "ins", 15));
                textItems.add(new CarouselPicker.TextItem(dataArray[2] + "BMIs", 15));
                CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                textAdapter.setTextColor(Color.MAGENTA);
                carouselPicker.setAdapter(textAdapter);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }//OnActivityResult() ends

    /**
     * For getting the date taken of photo with that uri
     *
     * @param photoUri photo uri passed from imageview button handler
     * @return string MM/dd/yyyy
     */
    public CharSequence getDate(Uri photoUri) {
        Long longDate = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATE_TAKEN};
        Cursor cur = managedQuery(photoUri, projection, null, null, null);
        if (cur.moveToFirst()) {//when cursor is empty
            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            longDate = cur.getLong(dateColumn);
        }
        Date d = new Date(longDate);
        java.text.DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(d);
    }

    /**
     * find the distance between two fingers
     *
     * @param event
     * @return
     */
    private float findDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getX(1);
        float d = x * x + y * y;
        return (float) Math.sqrt(d);
    }

    /**
     * find the midpt between two fingers, used for Matrix.postScale()
     *
     * @param point
     * @param event
     */
    private void findMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Dragging and Zooming using imageview matrix scaleType
     *
     * @param v     which imageview
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://first pointer down
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = findDistance(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    findMidPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                    break;
                } else if (mode == ZOOM) {
                    float newDist = findDistance(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
        }//switch
        photo.setImageMatrix(matrix);
        bmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas1 = new Canvas(bmap);
        photo.draw(canvas1);

        return true;
    }//onTouch() ends


}
