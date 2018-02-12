package com.example.shrikanthravi.gobumprincomingcall;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import java.util.Date;

/**
 * Created by Shrikanth Ravi on 01-06-2017.
 */

public class PhoneStateListener extends BroadcastReceiver {
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }

                onCallStateChanged(context, state, number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void onIncomingCallStarted(Context ctx, String number, Date start,String name){
        final Context context;
        context =   ctx;

        final Intent intent = new Intent(context, MyCustomDialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("phone_no",number);
        intent.putExtra("contact_name",name);

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
        String[] proj = new String[]
                {
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts._ID,
                };
        String id=null;
        String sortOrder1 = ContactsContract.CommonDataKinds.StructuredPostal.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor crsr = context.getContentResolver().query(lookupUri,proj, null, null, sortOrder1);
        while(crsr.moveToNext())
        {
            String name1=crsr.getString(crsr.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            id = crsr.getString(crsr.getColumnIndex(ContactsContract.Contacts._ID));
            System.out.println(name1);
            System.out.println(id);
        }
        if(id==null){
            System.out.println("not found");
        }
        crsr.close();
        intent.putExtra("id",id);
        context.startActivity(intent);
        /*new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

            }
        },1000);*/


    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){
        MyCustomDialog.mActivity.finish();
    }

    public void onCallStateChanged(Context context, int state, String number)
    {
        String cname=getContactName(context,number);
        if(lastState == state)
        {
            return;
        }
        switch (state)
        {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime,cname);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (isIncoming)
                {
                    onIncomingCallEnded(context,savedNumber,callStartTime,new Date());
                }

            case TelephonyManager.CALL_STATE_IDLE:
                if(isIncoming)
                {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
        }
        lastState = state;
    }
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;

        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));


        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }


}
