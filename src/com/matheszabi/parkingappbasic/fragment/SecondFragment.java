package com.matheszabi.parkingappbasic.fragment;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.matheszabi.parkingappbasic.R;

public class SecondFragment extends Fragment {

	private RelativeLayout rootView;

	private TextView tvLatitude, tvAccuracy, tvTime, tvLongitude, tvListenGps, tvGpsState, tvBearingTo, tvDistanceTo, tvSatelitesInView, tvSatelitesInUse;

	private Button btLoad;

	private ToggleButton tbtGps;

	private LocationManager mLocationManager;
	private GpsLocationListener mGpsLocationListener;
	private GpsStatusListener mGpsStatusListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_second, container, false);

		tvLatitude = (TextView) rootView.findViewById(R.id.tvLatitude);
		tvAccuracy = (TextView) rootView.findViewById(R.id.tvAccuracy);
		tvTime = (TextView) rootView.findViewById(R.id.tvTime);
		tvLongitude = (TextView) rootView.findViewById(R.id.tvLongitude);
		tvListenGps = (TextView) rootView.findViewById(R.id.tvListenGps);
		tvGpsState = (TextView) rootView.findViewById(R.id.tvGpsState);
		tvBearingTo = (TextView) rootView.findViewById(R.id.tvBearingTo);
		tvDistanceTo = (TextView) rootView.findViewById(R.id.tvDistanceTo);
		tvSatelitesInView = (TextView) rootView.findViewById(R.id.tvSatelitesInView);
		tvSatelitesInUse = (TextView) rootView.findViewById(R.id.tvSatelitesInUse);
		btLoad = (Button) rootView.findViewById(R.id.btLoad);
		tbtGps = (ToggleButton) rootView.findViewById(R.id.tbtGps);

		tbtGps.setChecked(true);
		tbtGps.setOnCheckedChangeListener(new GpsListener());
		btLoad.setOnClickListener(new LoadListener());

		mGpsStatusListener = new GpsStatusListener();
		mGpsLocationListener = new GpsLocationListener();

		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		}
		// start monitoring:
		addGpsListener();

		boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		tvGpsState.setText(getActivity().getString(R.string.tvGpsStateDefault) + (gpsEnabled ? " ON" : " OFF"));
	}


	private void addGpsListener() {
		mLocationManager.addGpsStatusListener(mGpsStatusListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mGpsLocationListener);
	}

	private void removeGpsListener() {
		mLocationManager.removeGpsStatusListener(mGpsStatusListener);
		mLocationManager.removeUpdates(mGpsLocationListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();

		if (!tbtGps.isChecked()) {
			removeGpsListener();
		}
	}

	private void refreshGpsStatus() {
		boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (getActivity() != null) {
			tvGpsState.setText(getActivity().getString(R.string.tvGpsStateDefault) + (gpsEnabled ? " ON" : " OFF"));
		}
	}

	private Location mCurrentLocation;
	private float bearingTo;
	private float distanceTo;

	private class GpsLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			mCurrentLocation = location;// store

			// double latitude = location.getLatitude();
			// double longitude = location.getLongitude();
			// float angle = location.getBearing();// degree
			// long time = location.getTime();
			// float speed = location.getSpeed();// m/s
			// float accuracy = location.getAccuracy();// m

			if (getActivity() != null) {// display:
				if (mLocationLoaded != null) {
					bearingTo = mCurrentLocation.bearingTo(mLocationLoaded);
					distanceTo = mCurrentLocation.distanceTo(mLocationLoaded);

					tvBearingTo.setText(getActivity().getString(R.string.tvBearingToDefault) + "" + bearingTo);
					tvDistanceTo.setText(getActivity().getString(R.string.tvDistanceToDefault) + "" + distanceTo);
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			refreshGpsStatus();
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				refreshGpsStatus();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				refreshGpsStatus();
			}
		}
	}

	private Location mLocationLoaded;

	private class LoadListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

			float latitude = prefs.getFloat("Latitude", Float.NaN);
			float longitude = prefs.getFloat("Longitude", Float.NaN);
			float accuracy = prefs.getFloat("Accuracy", Float.NaN);
			float altitude = prefs.getFloat("Altitude", Float.NaN);
			long time = prefs.getLong("Time", 0);

			if (getActivity() != null) {
				if (latitude != Float.NaN && longitude != Float.NaN && accuracy != Float.NaN && altitude != Float.NaN && time != 0) {

					mLocationLoaded = new Location(LocationManager.GPS_PROVIDER);
					mLocationLoaded.setLongitude(longitude);
					mLocationLoaded.setLatitude(latitude);
					mLocationLoaded.setAccuracy(accuracy);
					mLocationLoaded.setTime(time);

					tvAccuracy.setText(getActivity().getString(R.string.tvAccuracyDefault) + " " + accuracy + " m");
					tvTime.setText(getActivity().getString(R.string.tvTimeDefault) + " " + new Date(time));
					tvLatitude.setText(getActivity().getString(R.string.tvLatitudeDefault) + latitude);
					tvLongitude.setText(getActivity().getString(R.string.tvLongitudeDefault) + longitude);
				}
			}
		}
	}

	private class GpsStatusListener implements Listener {

		private GpsStatus mGpsStatus;

		@Override
		public void onGpsStatusChanged(int event) {
			String msg;
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				msg = "GPS_EVENT_STARTED";
				break;

			case GpsStatus.GPS_EVENT_STOPPED:
				msg = "GPS_EVENT_STOPPED";
				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				msg = "GPS_EVENT_FIRST_FIX";
				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				msg = "GPS_EVENT_SATELLITE_STATUS";
				break;

			default:
				msg = "Unknown";
				break;
			}// switch

			mGpsStatus = mLocationManager.getGpsStatus(mGpsStatus);

			if ("GPS_EVENT_SATELLITE_STATUS".equals(msg)) {

				int countInView = 0;
				int countInUse = 0;
				Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();

				if (satellites != null) {
					for (GpsSatellite gpsSatellite : satellites) {
						countInView++;
						// which one it says it is use?
						if (gpsSatellite.usedInFix()) {
							countInUse++;
						}
					}
				}
				if (getActivity() != null) {
					tvSatelitesInView.setText(getActivity().getString(R.string.tvSatelitesInViewDefault) + " " + countInView);
					tvSatelitesInUse.setText(getActivity().getString(R.string.tvSatelitesInUseDefault) + " " + countInUse);
				}

			} else {
				// Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
			}

		}// onGpsStatusChanged

	}// GpsStatusListener

	private class GpsListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				addGpsListener();
			} else {
				removeGpsListener();
			}
		}
	}
}
