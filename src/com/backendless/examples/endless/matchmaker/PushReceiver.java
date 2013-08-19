/*
 * ********************************************************************************************************************
 *  <p/>
 *  BACKENDLESS.COM CONFIDENTIAL
 *  <p/>
 *  ********************************************************************************************************************
 *  <p/>
 *  Copyright 2012 BACKENDLESS.COM. All Rights Reserved.
 *  <p/>
 *  NOTICE: All information contained herein is, and remains the property of Backendless.com and its suppliers,
 *  if any. The intellectual and technical concepts contained herein are proprietary to Backendless.com and its
 *  suppliers and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret
 *  or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden
 *  unless prior written permission is obtained from Backendless.com.
 *  <p/>
 *  ********************************************************************************************************************
 */

package com.backendless.examples.endless.matchmaker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.backendless.examples.endless.matchmaker.R;
import com.backendless.examples.endless.matchmaker.controllers.PushActivity;
import com.backendless.examples.endless.matchmaker.utils.Defaults;
import com.backendless.push.BackendlessBroadcastReceiver;

public class PushReceiver extends BackendlessBroadcastReceiver
{
  public PushReceiver()
  {
    super( Defaults.GCM_SENDER_ID );
  }

  @Override
  public void onRegistered( Context context, String registrationId )
  {
    SharedPreferences.Editor editor = context.getSharedPreferences( context.getResources().getText( R.string.app_name ).toString(), Context.MODE_PRIVATE ).edit();
    editor.putString( Defaults.DEVICE_REGISTRATION_ID_PROPERTY, registrationId );
    editor.commit();
  }

  @Override
  public void onUnregistered( Context context, Boolean unregistered )
  {
    SharedPreferences.Editor editor = context.getSharedPreferences( context.getResources().getText( R.string.app_name ).toString(), Context.MODE_PRIVATE ).edit();
    editor.remove( Defaults.DEVICE_REGISTRATION_ID_PROPERTY );
    editor.commit();
  }

  @Override
  public void onMessage( Context context, Intent intent )
  {
    String title = context.getResources().getString( R.string.app_name );
    String message = "You have new ping!";
    String userName = intent.getStringExtra( "message" );

    Intent targetIntent = new Intent( context, PushActivity.class );
    targetIntent.putExtra( Defaults.NAME_PROPERTY, userName );
    Notification notification = new Notification( R.drawable.logo_small, message, 0 );
    NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
    targetIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
    PendingIntent pendingIntent = PendingIntent.getActivity( context, 0, targetIntent, 0 );
    notification.setLatestEventInfo( context, title, message, pendingIntent );
    notification.flags |= Notification.FLAG_AUTO_CANCEL;

    notificationManager.notify( 0, notification );
  }
}
