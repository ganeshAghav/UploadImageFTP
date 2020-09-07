package com.example.uploadimageftp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static Bitmap scaledphoto = null;
    private String filePath = null;
    private String FileName="DemoImage.jpg";
    private Uri u = null;
    private Boolean picTaken = false;
    public static int REQUEST_CODE = 1;
    Button btnCaptureImg, btnUploadImg;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCaptureImg = (Button) findViewById(R.id.capture_photo);
        btnUploadImg = (Button) findViewById(R.id.uploadImage);
        imgView = (ImageView) findViewById(R.id.imgView);


        btnCaptureImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                filePath = Environment.getExternalStorageDirectory() + "/" +FileName;
                File file = new File(filePath);
                Uri outputFileUri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnUploadImg  .setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                UploadImage uploadImage=new UploadImage();
                uploadImage.execute("ftp.somewhere.net","username","Passwod",filePath,FileName);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        try {

            getContentResolver().notifyChange(u, null);
            ContentResolver cr = getContentResolver();
            Bitmap bm = android.provider.MediaStore.Images.Media.getBitmap(cr, u);
            imgView.setImageBitmap(bm);
            picTaken=true; //to ensure picture is taken

        }catch(Exception e) {

            e.printStackTrace();

        }
    }

   public class UploadImage  extends AsyncTask<String, Void, String>{


       ProgressDialog  progressBar=new ProgressDialog (MainActivity.this);
       String result="";

       @Override
       protected void onPreExecute() {
           progressBar.setIndeterminate(true);
           progressBar.setCancelable(false);
           progressBar.setMessage("Please wait...");
           progressBar.show();
       }

       @Override
        protected String doInBackground(String... strings) {
           try
           {
               SimpleFTP ftp = new SimpleFTP();
               String ServerName=strings[0];
               String UserName=strings[1];
               String PasswordName=strings[2];
               String FilePath=strings[3];
               String FileName=strings[4];

               ftp.connect(ServerName, 21, UserName, PasswordName);

               // Set binary mode.
               ftp.bin();

               // Change to a new working directory on the FTP server.
               ftp.cwd("web");

               // You can also upload from an InputStream, e.g.
               ftp.stor(new FileInputStream(new File(FilePath)), FileName);

               // Quit from the FTP server.
               ftp.disconnect();

               result="Success";
           }
           catch (IOException e)
           {
               result="Fail";
               e.printStackTrace();
               progressBar.dismiss();
           }
           return result;
        }

       @Override
       protected void onPostExecute(String result) {
          if(result=="")
          {
              progressBar.dismiss();
              Toast.makeText(getApplicationContext(),"File Uploaded Successfully.",Toast.LENGTH_LONG).show();
          }
          else {
              progressBar.dismiss();
              Toast.makeText(getApplicationContext(),"File Not uploaded try again.",Toast.LENGTH_LONG).show();
          }
       }
   }

}