package com.example.shrikanthravi.gobumprincomingcall;

import android.app.Activity;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by Shrikanth Ravi on 02-06-2017.
 */


public class MyCustomDialog extends Activity
{
    float x1, x2;
    static final int MIN_DISTANCE = 150;
    TextView Cname,Cnumber;
    String ContactName,Contactnumber,id;
    long cid;
    ImageView Cphoto;
    public static Activity mActivity;
    RelativeLayout card;
    Animation rslide,lslide;
    private Window wind;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.setFinishOnTouchOutside(true);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog);
            this.mActivity = this;
            initializeContent();
            lslide = AnimationUtils.loadAnimation(this, R.anim.lslide);
            rslide = AnimationUtils.loadAnimation(this, R.anim.rslide);
            ContactName = getIntent().getExtras().getString("contact_name");
            Contactnumber = getIntent().getExtras().getString("phone_no");
            id = getIntent().getExtras().getString("id");
            try{
                if(id!=null) {
                    cid = Long.parseLong(id);
                    retrieveContactPhoto(cid);
                }

            }catch (NumberFormatException e){
                System.out.println("nfe");
            }
            if(ContactName==null){
                Cname.setText("Unknown number");
                Cnumber.setText(Contactnumber);
            }
            else{
                Cname.setText(""+ContactName +" is calling you");
                Cnumber.setText(Contactnumber);
            }





        }
        catch (Exception e)
        {
            Log.d("Exception", e.toString());
            e.printStackTrace();
        }
        card = (RelativeLayout)findViewById(R.id.card);

    }

    private void initializeContent()
    {
        Cname  = (TextView) findViewById(R.id.Cname);
        Cnumber  = (TextView) findViewById(R.id.Cnumber);
        Cphoto = (ImageView) findViewById(R.id.contactPhoto);
    }
    private void retrieveContactPhoto(long contactID) {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                Cphoto.setImageBitmap(photo);
            }

            if(inputStream != null)
                inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE)
                {

                    if (x2 > x1)
                    {
                        card.startAnimation(rslide);
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {

                                MyCustomDialog.this.finish();
                                System.exit(0);
                                ExitActivity.exitApplication(getApplicationContext());
                            }
                        },500);


                    }


                    else
                    {
                        card.startAnimation(lslide);
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try {
                                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

                                    Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
                                    m1.setAccessible(true);
                                    Object iTelephony = m1.invoke(tm);

                                    Method m2 = iTelephony.getClass().getDeclaredMethod("silenceRinger");
                                    Method m3 = iTelephony.getClass().getDeclaredMethod("endCall");

                                    m2.invoke(iTelephony);
                                    m3.invoke(iTelephony);
                                }catch (Exception e){

                                }
                                MyCustomDialog.this.finish();
                                System.exit(0);
                                ExitActivity.exitApplication(getApplicationContext());
                            }
                        },500);
                    }

                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        wind.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        wind.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);

    }


}
