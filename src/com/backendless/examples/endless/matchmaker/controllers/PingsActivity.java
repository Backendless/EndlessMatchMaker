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
import android.widget.LinearLayout;
import com.backendless.examples.endless.matchmaker.R;
import com.backendless.examples.endless.matchmaker.controllers.shared.Lifecycle;
import com.backendless.examples.endless.matchmaker.controllers.shared.ResponseAsyncCallback;
import com.backendless.examples.endless.matchmaker.utils.Defaults;
import com.backendless.examples.endless.matchmaker.views.UIFactory;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.SearchMatchesResult;

import java.util.HashMap;
import java.util.Map;

public class PingsActivity extends Activity
{
  private LinearLayout pingedContainer, werePingedContainer, callbackPingsContainer;
  private ProgressDialog progressDialog;
  private GeoPoint currentUserGeoPoint;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.pings );

    progressDialog = UIFactory.getDefaultProgressDialog( this );

    pingedContainer = (LinearLayout) findViewById( R.id.pingsContainer );
    werePingedContainer = (LinearLayout) findViewById( R.id.pingsContainer2 );
    callbackPingsContainer = (LinearLayout) findViewById( R.id.pingsContainer3 );

    currentUserGeoPoint = (GeoPoint) getIntent().getSerializableExtra( Defaults.CURRENT_USER_GEO_POINT_BUNDLE_TAG );

    Map<String, String> metaDataForSearch = new HashMap<String, String>();
    metaDataForSearch.put( "Asian", "Food" );
    metaDataForSearch.put( "Caribean", "Food" );
    metaDataForSearch.put( "Bar food", "Food" );
    metaDataForSearch.put( "French", "Food" );
    metaDataForSearch.put( "Mediterranean", "Food" );
    metaDataForSearch.put( "Greek", "Food" );
    metaDataForSearch.put( "Spanish", "Food" );
    metaDataForSearch.put( "Mexican", "Food" );
    metaDataForSearch.put( "Thai", "Food" );
    //Music
    metaDataForSearch.put( "Classical", "Music" );
    metaDataForSearch.put( "Jazz", "Music" );
    metaDataForSearch.put( "Hip-hop", "Music" );
    metaDataForSearch.put( "Reggae", "Music" );
    metaDataForSearch.put( "Blues", "Music" );
    metaDataForSearch.put( "Trance", "Music" );
    metaDataForSearch.put( "House", "Music" );
    metaDataForSearch.put( "Rock", "Music" );
    metaDataForSearch.put( "Folk", "Music" );
    //  Hobbies
    metaDataForSearch.put( "Fishing", "Hobbies" );
    metaDataForSearch.put( "Diving", "Hobbies" );
    metaDataForSearch.put( "Rock climbing", "Hobbies" );
    metaDataForSearch.put( "Hiking", "Hobbies" );
    metaDataForSearch.put( "Reading", "Hobbies" );
    metaDataForSearch.put( "Dancing", "Hobbies" );
    metaDataForSearch.put( "Cooking", "Hobbies" );
    metaDataForSearch.put( "Surfing", "Hobbies" );
    metaDataForSearch.put( "Photography", "Hobbies" );
    //Travel
    metaDataForSearch.put( "Cruise", "Travel" );
    metaDataForSearch.put( "B&B", "Travel" );
    metaDataForSearch.put( "Europe", "Travel" );
    metaDataForSearch.put( "Asia", "Travel" );
    metaDataForSearch.put( "Caribean", "Travel" );
    metaDataForSearch.put( "Mountains", "Travel" );
    metaDataForSearch.put( "Whale watching", "Travel" );
    metaDataForSearch.put( "Active travel", "Travel" );

    int maxPoints = 10;
    BackendlessGeoQuery backendlessGeoQuery = new BackendlessGeoQuery( metaDataForSearch, maxPoints );
    backendlessGeoQuery.setPageSize( 50 );
    backendlessGeoQuery.setIncludeMeta( true );

    Backendless.Geo.relativeFind( backendlessGeoQuery, gotPingsCallback );
  }

  //Listeners section

  public View.OnClickListener getPingDetailListener( final GeoPoint targetUserGeoPoint )
  {
    return new View.OnClickListener()
    {
      @Override
      public void onClick( View view )
      {
        Lifecycle.runMatchViewActivityFromPings( PingsActivity.this, currentUserGeoPoint, targetUserGeoPoint, "1" );
      }
    };
  }

  @Override
  public void onBackPressed()
  {
    Lifecycle.runFindMatchesActivity( PingsActivity.this );
    finish();
  }

  //Callbacks section
  private AsyncCallback<BackendlessCollection<SearchMatchesResult>> gotPingsCallback = new ResponseAsyncCallback<BackendlessCollection<SearchMatchesResult>>( PingsActivity.this )
  {
    @Override
    public void handleResponse( BackendlessCollection<SearchMatchesResult> response )
    {
      for( SearchMatchesResult targetUserGeoPoint : response.getCurrentPage() )
      {
        String targetUserEmail = targetUserGeoPoint.getGeoPoint().getMetadata( BackendlessUser.EMAIL_KEY );
        String targetUserName = targetUserGeoPoint.getGeoPoint().getMetadata( Defaults.NAME_PROPERTY );
        double matchCount = targetUserGeoPoint.getMatches();
        if( matchCount <= 1 )
          matchCount = matchCount * 100;
        //Callback pings
        if( currentUserGeoPoint.getMetadata().containsKey( targetUserEmail ) && targetUserGeoPoint.getGeoPoint().getMetadata().containsKey( currentUserGeoPoint.getMetadata( BackendlessUser.EMAIL_KEY ) ) )
        {
          callbackPingsContainer.addView( UIFactory.getPingsListElement( PingsActivity.this, targetUserName + "  -  " + targetUserEmail, round( matchCount, 2 ), getPingDetailListener( targetUserGeoPoint.getGeoPoint() ), targetUserGeoPoint.getGeoPoint().getMetadata( Defaults.GENDER_PROPERTY ) ) );
        }
        //You pinged
        if( targetUserGeoPoint.getGeoPoint().getMetadata().containsKey( currentUserGeoPoint.getMetadata( BackendlessUser.EMAIL_KEY ) ) && !currentUserGeoPoint.getMetadata().containsKey( targetUserEmail ) )
        {
          pingedContainer.addView( UIFactory.getPingsListElement( PingsActivity.this, targetUserName, round( matchCount, 2 ), getPingDetailListener( targetUserGeoPoint.getGeoPoint() ), targetUserGeoPoint.getGeoPoint().getMetadata( Defaults.GENDER_PROPERTY ) ) );
        }
        //You were pinged
        if( currentUserGeoPoint.getMetadata().containsKey( targetUserEmail ) && !targetUserGeoPoint.getGeoPoint().getMetadata().containsKey( currentUserGeoPoint.getMetadata( BackendlessUser.EMAIL_KEY ) ) )
        {
          werePingedContainer.addView( UIFactory.getPingsListElement( PingsActivity.this, targetUserName, round( matchCount, 2 ), getPingDetailListener( targetUserGeoPoint.getGeoPoint() ), targetUserGeoPoint.getGeoPoint().getMetadata( Defaults.GENDER_PROPERTY ) ) );
        }
      }
      progressDialog.cancel();
    }
  };

  public static double round( double value, int scale )
  {
    return Math.round( value * Math.pow( 10, scale ) ) / Math.pow( 10, scale );
  }
}
