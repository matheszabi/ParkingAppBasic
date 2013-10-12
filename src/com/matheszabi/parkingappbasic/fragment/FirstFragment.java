package com.matheszabi.parkingappbasic.fragment;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class FirstFragment extends Fragment {

	private RelativeLayout rootView;
	private TextView tvGpsState, tvSatelitesInView, tvSpeed, tvSatelitesInUse, tvAccuracy, tvTime, tvLatitude, tvLongitude, tvAngle, tvListenGps;

	private Button btSave;
	private ToggleButton tbtGps;

	private LocationManager mLocationManager;
	private GpsLocationListener mGpsLocationListener;
	private GpsStatusListener mGpsStatusListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_first, container, false);
		tvGpsState = (TextView) rootView.findViewById(R.id.tvGpsState);
		tvSatelitesInView = (TextView) rootView.findViewById(R.id.tvSatelitesInView);
		tvSpeed = (TextView) rootView.findViewById(R.id.tvSpeed);
		tvSatelitesInUse = (TextView) rootView.findViewById(R.id.tvSatelitesInUse);
		tvAccuracy = (TextView) rootView.findViewById(R.id.tvAccuracy);
		tvTime = (TextView) rootView.findViewById(R.id.tvTime);
		tvLatitude = (TextView) rootView.findViewById(R.id.tvLatitude);
		tvLongitude = (TextView) rootView.findViewById(R.id.tvLongitude);
		tvAngle = (TextView) rootView.findViewById(R.id.tvAngle);
		tvListenGps = (TextView) rootView.findViewById(R.id.tvListenGps);
		btSave = (Button) rootView.findViewById(R.id.btSave);
		tbtGps = (ToggleButton) rootView.findViewById(R.id.tbtGps);

		tbtGps.setChecked(true);
		btSave.setOnClickListener(new SaveListener());

		tbtGps.setOnCheckedChangeListener(new GpsListener());

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

	private Location mLocation;

	private class GpsLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			mLocation = location;// store

			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			float angle = location.getBearing();// degree
			long time = location.getTime();
			float speed = location.getSpeed();// m/s
			float accuracy = location.getAccuracy();// m

			if (getActivity() != null) {// display:
				tvAccuracy.setText(getActivity().getString(R.string.tvAccuracyDefault) + " " + accuracy + " m");
				tvSpeed.setText(getActivity().getString(R.string.tvSpeedDefault) + " " + speed + " m/s");
				tvTime.setText(getActivity().getString(R.string.tvTimeDefault) + " " + new Date(time));
				tvLatitude.setText(getActivity().getString(R.string.tvLatitudeDefault) + latitude);
				tvLongitude.setText(getActivity().getString(R.string.tvLongitudeDefault) + longitude);
				tvAngle.setText(getActivity().getString(R.string.tvAngleDefault) + angle);
			}

			btSave.setTextColor(ColorStateList.valueOf(Color.rgb(0, 127, 0)));
		}

		private void refreshGpsStatus() {
			boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (getActivity() != null) {
				tvGpsState.setText(getActivity().getString(R.string.tvGpsStateDefault) + (gpsEnabled ? " ON" : " OFF"));
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

	private class SaveListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (mLocation == null) {
				return;
			}

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			Editor editor = prefs.edit();

			editor.putFloat("Latitude", (float) mLocation.getLatitude());
			editor.putFloat("Longitude", (float) mLocation.getLongitude());
			editor.putFloat("Accuracy", (float) mLocation.getAccuracy());
			editor.putFloat("Altitude", (float) mLocation.getAltitude());
			editor.putLong("Time", mLocation.getTime());

			editor.commit();
		}
	}

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
