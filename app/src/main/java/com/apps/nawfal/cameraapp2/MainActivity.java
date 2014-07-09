package com.apps.nawfal.cameraapp2;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.orleonsoft.android.simplefilechooser.Constants;
import com.orleonsoft.android.simplefilechooser.ui.FileChooserActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class MainActivity extends Activity {

    final int FILE_CHOOSER = 41;
    ImageView imgFavorite;
    Bitmap bitmap = null;
    String bitmappath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgFavorite = (ImageView) findViewById(R.id.imageView1);
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText sign1 = (EditText) findViewById(R.id.signtext);
                String textsign = sign1.getText().toString();

                if (textsign.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter your Signature ", Toast.LENGTH_LONG).show();
                } else if (bitmap == null) {
                    Toast.makeText(MainActivity.this, "Please Select your Photo ", Toast.LENGTH_LONG).show();
                } else
                    try {

                        imgFavorite.setImageBitmap(writeTextOnDrawable(R.id.imageView1, textsign));
                        imgFavorite.refreshDrawableState();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
            }
        });

        Button btn2 = (Button) findViewById(R.id.savenutton);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                savebitmap();
            }
        });
        Button btnloadpic = (Button) findViewById(R.id.loadpicbutton);
        btnloadpic.setOnClickListener(new OnClickListener() {
            public static final int REQUEST_CHOOSER = 1234;

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);
                startActivityForResult(intent, FILE_CHOOSER);

/*                Intent intent = new Intent();
                intent.setType("image/jpeg");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, (CharSequence) "nawfal"), 2);
  */
            }
        });


        final Button startcamera = (Button) findViewById(R.id.startcamera);
        startcamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                open();
            }
        });

    }


    private String getPath(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(MainActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public void loadpicfromsd(String filePath) {
        File imgFile = new File(filePath);
        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView myImage = (ImageView) findViewById(R.id.imageView1);
            myImage.setImageBitmap(myBitmap);
            bitmap = myBitmap;

        }
    }


    private Bitmap writeTextOnDrawable(int drawableId, String text) {
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(11));
        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas;
        canvas = new Canvas(mutableBitmap);
        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        // int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        int yPos = (int) ((canvas.getHeight() - 10));

        canvas.drawText(text, xPos, yPos, paint);
        //   return new BitmapDrawable(getResources(), bm);
        bitmap = mutableBitmap;
        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_LONG).show();
        return mutableBitmap;
    }


    public int convertToPixels(int nDP) {
        final float conversionScale = getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f);

    }

    public boolean savebitmap() {
        try {
            File sdCardDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "NawfalAppImages");
            sdCardDirectory.mkdirs();
            Random random = new Random();
            String imageNameForSDCard = "image_" + String.valueOf(random.nextInt(1000)) + System.currentTimeMillis() + ".jpg";


            File image = new File(sdCardDirectory, imageNameForSDCard);
            FileOutputStream outStream;
            outStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
    /* 100 to keep full quality of the image */
            outStream.flush();
            outStream.close();
            //Refreshing SD card
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            return true;
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;


    }

    public void open() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case FILE_CHOOSER: {
                String fileSelected = data.getStringExtra(Constants.KEY_FILE_SELECTED);
                bitmappath = fileSelected;
                loadpicfromsd(bitmappath);
                Toast.makeText(this, "file selected " + fileSelected, Toast.LENGTH_LONG).show();
                break;
            }
            case 0: {
                super.onActivityResult(requestCode, resultCode, data);
                try {
                    Bitmap bp;
                    if (data.hasExtra("data")) {
                        bp = (Bitmap) data.getExtras().get("data");

                        bitmap = bp;
                        // savebitmap(bp);
                        imgFavorite.setImageBitmap(bp);
                    } else {
                        Toast.makeText(this, "Please Take a Photo", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                }
            }
/*            case PICKFILE_RESULT_CODE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();



                    String filename=getRealPathFromURI(uri);
                 //   loadpicfromsd(filename);
                    Toast.makeText(MainActivity.this, filename, Toast.LENGTH_LONG).show();
                    //textFile.setText(FilePath);

                }
                break;
            }*/

        }
    }

    /*   public String getRealPathFromURI(Uri contentUri) {
           String result;
       Cursor cursor = getContentResolver().query(contentUri, null, null,
       null, null);
       if (cursor == null) {
           result = contentUri.getPath();
       } else {
           cursor.moveToFirst();
           int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
           result = cursor.getString(idx);
       }*/
/*
        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
String result=cursor.getString(column_index);

        return result;

    }
    /*
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
*/

}