/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrftoolbox.uart;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.colorpicker.MainActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
//import no.nordicsemi.android.nrftoolbox.dfu.adapter.FileBrowserAppsAdapter;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.template.TemplateActivity;
import no.nordicsemi.android.nrftoolbox.uart.database.DatabaseHelper;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;
import no.nordicsemi.android.nrftoolbox.uart.wearable.UARTConfigurationSynchronizer;
import no.nordicsemi.android.nrftoolbox.utility.FileHelper;
import no.nordicsemi.android.nrftoolbox.widget.ClosableSpinner;
import no.nordicsemi.android.nrftoolbox.colorpicker.MainActivity;

public class UARTActivity extends BleProfileServiceReadyActivity<UARTService.UARTBinder> implements Serializable, UARTInterface,
		 UARTConfigurationsAdapter.ActionListener, AdapterView.OnItemSelectedListener,
		GoogleApiClient.ConnectionCallbacks {
	private final static String TAG = "UARTActivity";

	private final static String PREFS_BUTTON_ENABLED = "prefs_uart_enabled_";
	private final static String PREFS_BUTTON_COMMAND = "prefs_uart_command_";
	private final static String PREFS_BUTTON_ICON = "prefs_uart_icon_";
	/**
	 * This preference keeps the ID of the selected configuration.
	 */
	private final static String PREFS_CONFIGURATION = "configuration_id";
	/**
	 * This preference is set to true when initial data synchronization for wearables has been completed.
	 */
	private final static String PREFS_WEAR_SYNCED = "prefs_uart_synced";
	private final static String SIS_EDIT_MODE = "sis_edit_mode";

	private final static int SELECT_FILE_REQ = 2678; // random
	private final static int PERMISSION_REQ = 24; // random, 8-bit
	private final static int LEDUSERSETTING = 2;

	UARTConfigurationSynchronizer mWearableSynchronizer;

	/**
	 * The current configuration.
	 */
	private UartConfiguration mConfiguration;
	private DatabaseHelper mDatabaseHelper;
	private SharedPreferences mPreferences;
	//private UARTConfigurationsAdapter mConfigurationsAdapter;
	private ClosableSpinner mConfigurationSpinner;
	private SlidingPaneLayout mSlider;
	private UARTService.UARTBinder mServiceBinder;
	private boolean mEditMode;

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return UARTService.class;
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.uart_feature_title;
	}

	@Override
	protected Uri getLocalAuthorityLogger() {
		return UARTLocalLogContentProvider.AUTHORITY_URI;
	}

	@Override
	protected void setDefaultUI() {
		// empty
	}

	@Override
	protected void onServiceBinded(final UARTService.UARTBinder binder) {
		mServiceBinder = binder;
	}

	@Override
	protected void onServiceUnbinded() {
		//mServiceBinder = null;
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mDatabaseHelper = new DatabaseHelper(this);
		//mConfigurationsAdapter = new UARTConfigurationsAdapter(this, this, mDatabaseHelper.getConfigurationsNames());

		// Initialize Wearable synchronizer
		mWearableSynchronizer = UARTConfigurationSynchronizer.from(this, this);
	}

	/**
	 * Method called when Google API Client connects to Wearable.API.
	 */
	@Override
	public void onConnected(final Bundle bundle) {
		if (!mPreferences.getBoolean(PREFS_WEAR_SYNCED, false)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					final Cursor cursor = mDatabaseHelper.getConfigurations();
					try {
						while (cursor.moveToNext()) {
							final long id = cursor.getLong(0 /* _ID */);
							try {
								final String xml = cursor.getString(2 /* XML */);
								final Format format = new Format(new HyphenStyle());
								final Serializer serializer = new Persister(format);
								final UartConfiguration configuration = serializer.read(UartConfiguration.class, xml);
								mWearableSynchronizer.onConfigurationAddedOrEdited(id, configuration).await();
							} catch (final Exception e) {
								Log.w(TAG, "Deserializing configuration with id " + id + " failed", e);
							}
						}
						mPreferences.edit().putBoolean(PREFS_WEAR_SYNCED, true).apply();
					} finally {
						cursor.close();
					}
				}
			}).start();
		}
	}

	/**
	 * Method called then Google API client connection was suspended.
	 *
	 * @param cause the cause of suspension
	 */
	@Override
	public void onConnectionSuspended(final int cause) {
		// dp nothing
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWearableSynchronizer.close();
	}

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_uart);

		// Setup the sliding pane if it exists
		final SlidingPaneLayout slidingPane = mSlider = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
		if (slidingPane != null) {
			slidingPane.setSliderFadeColor(Color.TRANSPARENT);
			slidingPane.setShadowResourceLeft(R.drawable.shadow_r);
			slidingPane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
				@Override
				public void onPanelClosed(final View panel) {
					// Close the keyboard
					final UARTLogFragment logFragment = (UARTLogFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_log);
					logFragment.onFragmentHidden();
				}
			});
		}
	/*	String device=getDeviceName();
		if(device!=null) {
			String RGBsetting = getIntent().getStringExtra("RGB setting");
			send(RGBsetting);
		}*/
	}

	@Override
	protected void onViewCreated(final Bundle savedInstanceState) {
//		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final ClosableSpinner configurationSpinner = mConfigurationSpinner = (ClosableSpinner) findViewById(R.id.toolbar_spinner);
		//configurationSpinner.setOnItemSelectedListener(this);
		//configurationSpinner.setAdapter(mConfigurationsAdapter);
		//configurationSpinner.setSelection(mConfigurationsAdapter.getItemPosition(mPreferences.getLong(PREFS_CONFIGURATION, 0)));
	}

	@Override
	protected void onRestoreInstanceState(final @NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mEditMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SIS_EDIT_MODE, mEditMode);
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		// do nothing
	}

	@Override
	public void onDeviceSelected(final BluetoothDevice device, final String name) {
		// The super method starts the service
		super.onDeviceSelected(device, name);

		// Notify the log fragment about it
		final UARTLogFragment logFragment = (UARTLogFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_log);
		logFragment.onServiceStarted();
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.uart_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.uart_about_text;
	}

	@Override
	protected UUID getFilterUUID() {
		return null; // not used
	}

	@Override
	public void send(final String text) {
		if (mServiceBinder != null)
			mServiceBinder.send(text);
	}

	@Override
	public void onBackPressed() {
		if (mSlider != null && mSlider.isOpen()) {
			mSlider.closePane();
			return;
		}
		if (mEditMode) {
			final UARTControlFragment fragment = (UARTControlFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_control);
			//fragment.setEditMode(false);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.uart_menu_configurations, menu);
		getMenuInflater().inflate(mEditMode ? R.menu.uart_menu_config : R.menu.uart_menu, menu);

		final int configurationsCount = mDatabaseHelper.getConfigurationsCount();
		menu.findItem(R.id.action_remove).setVisible(configurationsCount > 1);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected boolean onOptionsItemSelected(int itemId) {
		final String name = mConfiguration.getName();
		switch (itemId) {
			case R.id.action_show_log:
				mSlider.openPane();
				return true;
			case R.id.action_share: {
				final String xml = mDatabaseHelper.getConfiguration(mConfigurationSpinner.getSelectedItemId());

				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setType("text/xml");
				intent.putExtra(Intent.EXTRA_TEXT, xml);
				intent.putExtra(Intent.EXTRA_SUBJECT, mConfiguration.getName());
				try {
					startActivity(intent);
				} catch (final ActivityNotFoundException e) {
					Toast.makeText(this, R.string.no_uri_application, Toast.LENGTH_SHORT).show();
				}
				return true;
			}
			case R.id.action_rename: {
				final DialogFragment fragment = UARTNewConfigurationDialogFragment.getInstance(name, false);
				fragment.show(getSupportFragmentManager(), null);
				// onNewConfiguration(name, false) will be called when user press OK
				return true;
			}
			case R.id.action_duplicate: {
				final DialogFragment fragment = UARTNewConfigurationDialogFragment.getInstance(name, true);
				fragment.show(getSupportFragmentManager(), null);
				// onNewConfiguration(name, true) will be called when user press OK
				return true;
			}
					}
		return false;
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case PERMISSION_REQ: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// We have been granted the Manifest.permission.WRITE_EXTERNAL_STORAGE permission. Now we may proceed with exporting.
					//exportConfiguration();
				} else {
					Toast.makeText(this, R.string.no_required_permission, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	}

	@Override
	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (position > 0) { // FIXME this is called twice after rotation.
			try {
				final String xml = mDatabaseHelper.getConfiguration(id);
				final Format format = new Format(new HyphenStyle());
				final Serializer serializer = new Persister(format);
				mConfiguration = serializer.read(UartConfiguration.class, xml);
				//mConfigurationListener.onConfigurationChanged(mConfiguration);
			} catch (final Exception e) {
				Log.e(TAG, "Selecting configuration failed", e);

				String message;
				if (e.getLocalizedMessage() != null)
					message = e.getLocalizedMessage();
				else if (e.getCause() != null && e.getCause().getLocalizedMessage() != null)
					message = e.getCause().getLocalizedMessage();
				else
					message = "Unknown error";
				final String msg = message;
				Snackbar.make(mSlider, R.string.uart_configuration_loading_failed, Snackbar.LENGTH_INDEFINITE).setAction(R.string.uart_action_details, new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						new AlertDialog.Builder(UARTActivity.this).setMessage(msg).setTitle(R.string.uart_action_details).setPositiveButton(R.string.ok, null).show();
					}
				}).show();
				return;
			}

			mPreferences.edit().putLong(PREFS_CONFIGURATION, id).apply();
		}
	}

	@Override
	public void onNothingSelected(final AdapterView<?> parent) {
		// do nothing
	}

	public void onEditPatternClick(final View view)
	{
		final Intent intent = new Intent(UARTActivity.this, MainActivity.class);//huy
		startActivityForResult(intent, LEDUSERSETTING);// Activity is started with requestCode 2
		//send("Start editing pattern");
	}

	@Override
	public void onNewConfigurationClick() {
		// No item has been selected. We must close the spinner manually.
		mConfigurationSpinner.close();

		// Open the dialog
		final DialogFragment fragment = UARTNewConfigurationDialogFragment.getInstance(null, false);
		fragment.show(getSupportFragmentManager(), null);

		// onNewConfiguration(null, false) will be called when user press OK
	}

	@Override
	public void onImportClick() {
		// No item has been selected. We must close the spinner manually.
		mConfigurationSpinner.close();

		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/xml");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		if (intent.resolveActivity(getPackageManager()) != null) {
			// file browser has been found on the device
			startActivityForResult(intent, SELECT_FILE_REQ);

		} else {
			// there is no any file browser app, let's try to download one
			final View customView = getLayoutInflater().inflate(R.layout.app_file_browser, null);
			final ListView appsList = (ListView) customView.findViewById(android.R.id.list);
			//appsList.setAdapter(new FileBrowserAppsAdapter(this));
			appsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			appsList.setItemChecked(0, true);
			new AlertDialog.Builder(this).setTitle(R.string.dfu_alert_no_filebrowser_title).setView(customView).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					dialog.dismiss();
				}
			}).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					final int pos = appsList.getCheckedItemPosition();
					if (pos >= 0) {
						final String query = getResources().getStringArray(R.array.dfu_app_file_browser_action)[pos];
						final Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
						startActivity(storeIntent);
					}
				}
			}).show();
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_CANCELED)
			return;

		switch (requestCode) {
			case SELECT_FILE_REQ: {
				// clear previous data
				final Uri uri = data.getData();
				/*
				 * The URI returned from application may be in 'file' or 'content' schema.
				 * 'File' schema allows us to create a File object and read details from if directly.
				 * Data from 'Content' schema must be read with use of a Content Provider. To do that we are using a Loader.
				 */
				if (uri.getScheme().equals("file")) {
					// The direct path to the file has been returned
					final String path = uri.getPath();
					try {
						final FileInputStream fis = new FileInputStream(path);
						//loadConfiguration(fis);
					} catch (final FileNotFoundException e) {
						Toast.makeText(this, R.string.uart_configuration_load_error, Toast.LENGTH_LONG).show();
					}
				} else if (uri.getScheme().equals("content")) {
					// An Uri has been returned
					Uri u = uri;

					// If application returned Uri for streaming, let's us it. Does it works?
					final Bundle extras = data.getExtras();
					if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
						u = extras.getParcelable(Intent.EXTRA_STREAM);

					try {
						final InputStream is = getContentResolver().openInputStream(u);
						//loadConfiguration(is);
					} catch (final FileNotFoundException e) {
						Toast.makeText(this, R.string.uart_configuration_load_error, Toast.LENGTH_LONG).show();
					}
				}
				break;

			}
			case LEDUSERSETTING: {
				byte[] LedSetting = data.getByteArrayExtra("LED SETTING");
				//int[] ArrayInt = new int[LedSetting.length];
				String test1="";
				try{
				test1=new String(LedSetting,"US-ASCII");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				String LedString1=test1.substring(0,2);
				LedString1=LedString1+"abcdefgh";
				//this.send("abcdefghik");

				this.send(LedString1);
				//String LedString2=test1.substring(10);
				while(UARTService.returnSendData()==0);
				String LedString2="123456789";
				this.send(LedString2);
				Toast.makeText(getApplicationContext(), test1, Toast.LENGTH_LONG).show();
				//int[] LedSetting = data.getIntArrayExtra("LED SETTING");
				//String LedString1=Arrays.toString(ArrayInt).substring(1,10);
				//this.send(LedString1);
				//String LedString2=LedSetting.toString().substring(10,LedSetting.length+1);
				//this.send(LedString2);
				String DoneSetting="Pattern đã được sync với Device";
				Toast.makeText(getApplicationContext(), DoneSetting, Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	/*	protected void onResume()
		{
        String device=getDeviceName();
            if(device!=null)
            {
                String RGBsetting = getIntent().getStringExtra("RGB setting");
                send(RGBsetting);
    }
        }
*/

}
