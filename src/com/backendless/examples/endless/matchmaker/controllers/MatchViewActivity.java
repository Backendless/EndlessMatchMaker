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

package com.backendless.examples.endless.matchmaker.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.endless.matchmaker.R;
import com.backendless.examples.endless.matchmaker.controllers.shared.Lifecycle;
import com.backendless.examples.endless.matchmaker.controllers.shared.ResponseAsyncCallback;
import com.backendless.examples.endless.matchmaker.models.local.Gender;
import com.backendless.examples.endless.matchmaker.utils.Defaults;
import com.backendless.examples.endless.matchmaker.utils.Log;
import com.backendless.examples.endless.matchmaker.utils.SimpleMath;
import com.backendless.examples.endless.matchmaker.views.UIFactory;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.SearchMatchesResult;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PushBroadcastMask;

import java.text.ParseException;
import java.util.*;

public class MatchViewActivity extends Activity
{
  private TextView foodMatchValue;
  private ProgressBar foodProgressBar;
  private TextView musicMatchValue;
  private TextView matchPercentField;
  private ProgressBar musicProgressBar;
  private TextView hobbiesMatchValue;
  private ProgressBar hobbiesProgressBar;
  private TextView travelMatchValue;
  private ProgressBar travelProgressBar;
  private ProgressDialog progressDialog;
  private String targetUserName;
  private String triger;
  private String targetUserDeviceRegistrationId;
  private GeoPoint targetUserGeoPoint;
  private GeoPoint currentUserGeoPoint;
  private double foodMatchCount, musicMatchCount, travelMatchCount, hobbiesMatchCount, summMatch;
  private boolean food, music, hobbies, travel;
  private String foodName = "Food";
  private String musicName = "Music";
  private String hobbiesName = "Hobbies";
  private String travelName = "Travel";

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.matchview );

    progressDialog = UIFactory.getDefaultProgressDialog( this );

    TextView nameField = (TextView) findViewById( R.id.nameField );
    TextView genderField = (TextView) findViewById( R.id.genderField );
    TextView ageField = (TextView) findViewById( R.id.ageField );
    ImageView avatarImage = (ImageView) findViewById( R.id.avatarImage );
    ImageView genderImage = (ImageView) findViewById( R.id.genderImage );
    matchPercentField = (TextView) findViewById( R.id.matchPercents );
    foodMatchValue = (TextView) findViewById( R.id.foodMatchValue );
    foodProgressBar = (ProgressBar) findViewById( R.id.foodProgressBar );
    musicMatchValue = (TextView) findViewById( R.id.musicMatchValue );
    musicProgressBar = (ProgressBar) findViewById( R.id.musicProgressBar );
    hobbiesMatchValue = (TextView) findViewById( R.id.hobbiesMatchValue );
    hobbiesProgressBar = (ProgressBar) findViewById( R.id.hobbiesProgressBar );
    travelMatchValue = (TextView) findViewById( R.id.travelMatchValue );
    travelProgressBar = (ProgressBar) findViewById( R.id.travelProgressBar );

    currentUserGeoPoint = (GeoPoint) getIntent().getSerializableExtra( Defaults.CURRENT_USER_GEO_POINT_BUNDLE_TAG );
    targetUserGeoPoint = (GeoPoint) getIntent().getSerializableExtra( Defaults.TARGET_USER_GEO_POINT_BUNDLE_TAG );
    triger = getIntent().getStringExtra( Defaults.TRIGER );

    String targetUserEmail = targetUserGeoPoint.getMetadata( BackendlessUser.EMAIL_KEY );
    targetUserName = targetUserGeoPoint.getMetadata( Defaults.NAME_PROPERTY );
    Gender targetUserGender = Gender.valueOf( targetUserGeoPoint.getMetadata( Defaults.GENDER_PROPERTY ) );
    targetUserDeviceRegistrationId = targetUserGeoPoint.getMetadata( Defaults.DEVICE_REGISTRATION_ID_PROPERTY );

    Date userBirthDate;
    try
    {
      userBirthDate = Defaults.DEFAULT_DATE_FORMATTER.parse( targetUserGeoPoint.getMetadata( Defaults.BIRTH_DATE_PROPERTY ) );
    }
    catch( ParseException e )
    {
      progressDialog.cancel();
      Log.logLine( e );

      return;
    }

    Button actionButton = (Button) findViewById( R.id.actionButton );

    if( !targetUserGeoPoint.getMetadata().containsKey( Backendless.UserService.CurrentUser().getEmail() ) || !currentUserGeoPoint.getMetadata().containsKey( targetUserEmail ) )
    {
      actionButton.setText( getResources().getText( R.string.button_match_ping ) );
      actionButton.setOnClickListener( pingUserListener );
    }
    else
    {
      actionButton.setText( getResources().getText( R.string.button_sendmessage ) );
      actionButton.setOnClickListener( sendMessageListener );
    }

    nameField.setText( targetUserName );
    genderField.setText( targetUserGender.name() );

    if( targetUserGender == Gender.male )
    {
      avatarImage.setImageDrawable( getResources().getDrawable( R.drawable.avatar_default_male ) );
      genderImage.setImageDrawable( getResources().getDrawable( R.drawable.icon_male ) );
    }
    else
    {
      avatarImage.setImageDrawable( getResources().getDrawable( R.drawable.avatar_default_female ) );
      genderImage.setImageDrawable( getResources().getDrawable( R.drawable.icon_female ) );
    }

    ageField.setText( String.valueOf( SimpleMath.getAgeFromDate( userBirthDate ) ) );

    //Food
    Map<String, String> metaDataFood = new HashMap<String, String>();
    metaDataFood.put( "Asian", foodName );
    metaDataFood.put( "Caribean", foodName );
    metaDataFood.put( "Bar food", foodName );
    metaDataFood.put( "French", foodName );
    metaDataFood.put( "Mediterranean", foodName );
    metaDataFood.put( "Greek", foodName );
    metaDataFood.put( "Spanish", foodName );
    metaDataFood.put( "Mexican", foodName );
    metaDataFood.put( "Thai", foodName );
    int maxPoints = 10;
    BackendlessGeoQuery backendlessGeoQuery = new BackendlessGeoQuery( metaDataFood, maxPoints );
    backendlessGeoQuery.setPageSize( 50 );
    backendlessGeoQuery.setIncludeMeta( true );
    food = false;
    Backendless.Geo.relativeFind( backendlessGeoQuery, gotFoodCallback );
    // Music
    Map<String, String> metaDataMusic = new HashMap<String, String>();
    metaDataMusic.put( "Classical", musicName );
    metaDataMusic.put( "Jazz", musicName );
    metaDataMusic.put( "Hip-hop", musicName );
    metaDataMusic.put( "Reggae", musicName );
    metaDataMusic.put( "Blues", musicName );
    metaDataMusic.put( "Trance", musicName );
    metaDataMusic.put( "House", musicName );
    metaDataMusic.put( "Rock", musicName );
    metaDataMusic.put( "Folk", musicName );
    backendlessGeoQuery = new BackendlessGeoQuery( metaDataMusic, maxPoints );
    backendlessGeoQuery.setPageSize( 50 );
    backendlessGeoQuery.setIncludeMeta( true );
    music = false;
    Backendless.Geo.relativeFind( backendlessGeoQuery, gotMusicCallback );
    //Hobbies
    Map<String, String> metaDataHobbies = new HashMap<String, String>();
    metaDataHobbies.put( "Fishing", hobbiesName );
    metaDataHobbies.put( "Diving", hobbiesName );
    metaDataHobbies.put( "Rock climbing", hobbiesName );
    metaDataHobbies.put( "Hiking", hobbiesName );
    metaDataHobbies.put( "Reading", hobbiesName );
    metaDataHobbies.put( "Dancing", hobbiesName );
    metaDataHobbies.put( "Cooking", hobbiesName );
    metaDataHobbies.put( "Surfing", hobbiesName );
    metaDataHobbies.put( "Photography", hobbiesName );
    backendlessGeoQuery = new BackendlessGeoQuery( metaDataHobbies, maxPoints );
    backendlessGeoQuery.setPageSize( 50 );
    backendlessGeoQuery.setIncludeMeta( true );
    hobbies = false;
    Backendless.Geo.relativeFind( backendlessGeoQuery, gotHobbiesCallback );
    //Travel
    Map<String, String> metaDataTravel = new HashMap<String, String>();
    metaDataTravel.put( "Cruise", travelName );
    metaDataTravel.put( "B&B", travelName );
    metaDataTravel.put( "Europe", travelName );
    metaDataTravel.put( "Asia", travelName );
    metaDataTravel.put( "Caribean", travelName );
    metaDataTravel.put( "Mountains", travelName );
    metaDataTravel.put( "Whale watching", travelName );
    metaDataTravel.put( "Active travel", travelName );
    metaDataTravel.put( "Passive travel", travelName );
    backendlessGeoQuery = new BackendlessGeoQuery( metaDataTravel, maxPoints );
    backendlessGeoQuery.setPageSize( 50 );
    backendlessGeoQuery.setIncludeMeta( true );
    travel = false;
    Backendless.Geo.relativeFind( backendlessGeoQuery, gotTravelCallback );
  }

  //Callbacks section
  private AsyncCallback<BackendlessCollection<SearchMatchesResult>> gotFoodCallback = new ResponseAsyncCallback<BackendlessCollection<SearchMatchesResult>>( MatchViewActivity.this )
  {
    @Override
    public void handleResponse( BackendlessCollection<SearchMatchesResult> response )
    {
      for( SearchMatchesResult geoPoint : response.getCurrentPage() )
      {

        if( geoPoint.getGeoPoint().equals( targetUserGeoPoint ) )
        {
          foodMatchCount = geoPoint.getMatches();
          if( foodMatchCount <= 1 )
            foodMatchCount = foodMatchCount * 100;
          foodMatchValue.setText( String.valueOf( round( foodMatchCount, 2 ) ) );
          foodProgressBar.setProgress( (int) round( foodMatchCount, 2 ) );
          food = true;
        }
      }

      if( music == true && food == true && hobbies == true && travel == true )
      {
        summMatch = (foodMatchCount + musicMatchCount + hobbiesMatchCount + travelMatchCount) / 4;
        matchPercentField.setText( String.valueOf( round( summMatch, 2 ) ) );
        progressDialog.cancel();
      }
    }
  };
  private AsyncCallback<BackendlessCollection<SearchMatchesResult>> gotMusicCallback = new ResponseAsyncCallback<BackendlessCollection<SearchMatchesResult>>( MatchViewActivity.this )
  {
    @Override
    public void handleResponse( BackendlessCollection<SearchMatchesResult> response )
    {
      for( SearchMatchesResult geoPoint : response.getCurrentPage() )
      {

        if( geoPoint.getGeoPoint().equals( targetUserGeoPoint ) )
        {
          musicMatchCount = geoPoint.getMatches();
          if( musicMatchCount <= 1 )
            musicMatchCount = musicMatchCount * 100;
          musicMatchValue.setText( String.valueOf( round( musicMatchCount, 2 ) ) );
          musicProgressBar.setProgress( (int) round( musicMatchCount, 2 ) );
          music = true;
        }
      }

      if( music == true && food == true && hobbies == true && travel == true )
      {
        summMatch = (foodMatchCount + musicMatchCount + hobbiesMatchCount + travelMatchCount) / 4;
        matchPercentField.setText( String.valueOf( round( summMatch, 2 ) ) );
        progressDialog.cancel();
      }
    }
  };
  private AsyncCallback<BackendlessCollection<SearchMatchesResult>> gotHobbiesCallback = new ResponseAsyncCallback<BackendlessCollection<SearchMatchesResult>>( MatchViewActivity.this )
  {
    @Override
    public void handleResponse( BackendlessCollection<SearchMatchesResult> response )
    {
      for( SearchMatchesResult geoPoint : response.getCurrentPage() )
      {

        if( geoPoint.getGeoPoint().equals( targetUserGeoPoint ) )
        {
          hobbiesMatchCount = geoPoint.getMatches();
          if( hobbiesMatchCount <= 1 )
            hobbiesMatchCount = hobbiesMatchCount * 100;
          hobbiesMatchValue.setText( String.valueOf( round( hobbiesMatchCount, 2 ) ) );
          hobbiesProgressBar.setProgress( (int) round( hobbiesMatchCount, 2 ) );
          hobbies = true;
        }
      }

      if( music == true && food == true && hobbies == true && travel == true )
      {
        summMatch = (foodMatchCount + musicMatchCount + hobbiesMatchCount + travelMatchCount) / 4;
        matchPercentField.setText( String.valueOf( round( summMatch, 2 ) ) );
        progressDialog.cancel();
      }
    }
  };

  private AsyncCallback<BackendlessCollection<SearchMatchesResult>> gotTravelCallback = new ResponseAsyncCallback<BackendlessCollection<SearchMatchesResult>>( MatchViewActivity.this )
  {
    @Override
    public void handleResponse( BackendlessCollection<SearchMatchesResult> response )
    {
      for( SearchMatchesResult geoPoint : response.getCurrentPage() )
      {

        if( geoPoint.getGeoPoint().equals( targetUserGeoPoint ) )
        {
          travelMatchCount = geoPoint.getMatches();
          if( travelMatchCount <= 1 )
            travelMatchCount = travelMatchCount * 100;
          travelMatchValue.setText( String.valueOf( round( travelMatchCount, 2 ) ) );
          travelProgressBar.setProgress( (int) round( travelMatchCount, 2 ) );
          travel = true;
        }
      }

      if( music == true && food == true && hobbies == true && travel == true )
      {
        summMatch = (foodMatchCount + musicMatchCount + hobbiesMatchCount + travelMatchCount) / 4;
        matchPercentField.setText( String.valueOf( round( summMatch, 2 ) ) );
        progressDialog.cancel();
      }
    }
  };

  //Listeners section
  private View.OnClickListener pingUserListener = new View.OnClickListener()
  {
    @Override
    public void onClick( View view )
    {
      progressDialog = UIFactory.getDefaultProgressDialog( MatchViewActivity.this );

      targetUserGeoPoint.putMetadata( Backendless.UserService.CurrentUser().getEmail(), Defaults.PING_TAG );
      Backendless.Geo.savePoint( targetUserGeoPoint, new ResponseAsyncCallback<GeoPoint>( MatchViewActivity.this )
      {
        @Override
        public void handleResponse( GeoPoint response )
        {
          if( targetUserDeviceRegistrationId == null || targetUserDeviceRegistrationId.equals( "" ) )
            return;

          DeliveryOptions deliveryOptions = new DeliveryOptions();
          deliveryOptions.setPushBroadcast( PushBroadcastMask.ALL );
          deliveryOptions.addPushSinglecast( targetUserDeviceRegistrationId );
          Backendless.Messaging.publish( Defaults.MESSAGING_CHANNEL, targetUserName, null, deliveryOptions, new ResponseAsyncCallback<MessageStatus>( MatchViewActivity.this )
          {
            @Override
            public void handleResponse( MessageStatus response )
            {
              progressDialog.cancel();
              String targetUserEmail = targetUserGeoPoint.getMetadata( BackendlessUser.EMAIL_KEY );

              if( currentUserGeoPoint.getMetadata().containsKey( targetUserEmail ) )
                Lifecycle.runPingsActivity( MatchViewActivity.this, currentUserGeoPoint );
              else
                Toast.makeText( MatchViewActivity.this, "Your ping was successfully sent.", Toast.LENGTH_LONG ).show();

              Lifecycle.runFindMatchesActivity( MatchViewActivity.this );
            }
          } );
        }
      } );
    }
  };

  public static double round( double value, int scale )
  {
    return Math.round( value * Math.pow( 10, scale ) ) / Math.pow( 10, scale );
  }

  private View.OnClickListener sendMessageListener = new View.OnClickListener()
  {
    @Override
    public void onClick( View view )
    {
      //Start send message activity
      Lifecycle.runSendMessageActivity( MatchViewActivity.this, currentUserGeoPoint, targetUserGeoPoint );
    }
  };

  @Override
  public void onBackPressed()
  {
    if( triger == null )
    {
      Lifecycle.runFindMatchesActivity( MatchViewActivity.this );
      finish();
    }
    else
      Lifecycle.runPingsActivity( MatchViewActivity.this, currentUserGeoPoint );
  }
}