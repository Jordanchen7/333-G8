package com.whack.a.mole;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.whack.a.mole.bridge.MoleCallback;
import com.whack.a.mole.bridge.SystemCapability;
import com.whack.a.mole.net.MoleClient;
import com.whack.a.mole.net.MoleServer;
import com.whack.a.mole.utils.ConstUtils;
import com.whack.a.mole.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AndroidLauncher extends AndroidApplication {
	private static final String TAG = "AndroidLauncher";

	private static final String SP_FILE = "mole";

	private Handler handler;

	private boolean dialogShowing = false;

	private MoleCallback moleCallback;

	private MoleServer moleServer;

	private MoleClient moleClient;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler(Looper.getMainLooper());

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		initialize(new WhackAMoleGame(new SystemCapability() {
			@Override
			public void quit() {
				finish();
			}

			@Override
			public void vibrate(int duration) {
				vibrator.vibrate(duration);
			}

			@Override
			public void writeValue(String key, String value) {
				SharedPreferences s = getApplicationContext().getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = s.edit();
				editor.putString(key, value);
				editor.apply();

				Log.w(TAG, "writeValue:" + key + "=" + value);
			}

			@Override
			public String readValue(String key) {
				SharedPreferences s = getApplicationContext().getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
				Log.w(TAG, "readValue:" + key + "=" + s.getString(key, null));
				return s.getString(key, null);
			}

			@Override
			public void removeValue(String key) {
				SharedPreferences s = getApplicationContext().getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = s.edit();
				editor.remove(key);
				editor.apply();
			}

			@Override
			public void log(String tag, String msg) {
				Log.i(tag, msg);
			}

			@Override
			public void requestPermission() {
				List<String> permissions = new ArrayList<>();
				if (!checkPermissions(Manifest.permission.ACCESS_NETWORK_STATE)) {
					permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
				}
				if (!checkPermissions(Manifest.permission.ACCESS_WIFI_STATE)) {
					permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
				}

				if (!permissions.isEmpty()) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						requestPermissions(permissions.toArray(new String[0]), 1);
					}
				}
			}

			@Override
			public String getUsrInput(String prompt, String key, boolean mustShowDialog) {
				String value = readValue(key);
				if (!TextUtils.isEmpty(value) && !mustShowDialog) {
					return value;
				}

				if (dialogShowing) {
					return value;
				}

				dialogShowing = true;
				handler.post(() -> {

					// input by user
					AlertDialog.Builder builder = new AlertDialog.Builder(AndroidLauncher.this);
					builder.setTitle(prompt);
					final EditText input = new EditText(AndroidLauncher.this);
					input.setText(value);
					builder.setView(input);

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String text = input.getText().toString();
							// save username
							writeValue(key, text);
							dialog.dismiss();

							if (!text.equals(value)) {
								if (ConstUtils.KEY_USERNAME.equals(key)) {
									moleCallback.onUserChanged(text);
								} else if (ConstUtils.KEY_SERVER_IP.equals(key)) {
									moleCallback.onServerChanged(text);
								}
							}

							dialogShowing = false;
						}
					});

					AlertDialog dialog = builder.create();
					dialog.show();
				});

				return value;
			}

			@Override
			public void showToast(String msg) {
				handler.post(() -> {
					Toast.makeText(AndroidLauncher.this, msg, Toast.LENGTH_SHORT).show();
				});
			}

			@Override
			public void setMoleCallback(MoleCallback callback) {
				AndroidLauncher.this.moleCallback = callback;
			}

			@Override
			public void startMoleServer() {
				if (moleServer != null) {
					return;
				}

				moleServer = new MoleServer(ConstUtils.WS_PORT, moleCallback);
				moleServer.start();
			}

			@Override
			public void stopMoleServer() {
				if (moleServer != null) {
					moleServer.stop();
				}
			}

			@Override
			public void startMoleClient(String ip) {
				if (moleClient != null) {
					moleClient.stop();
				}

				moleClient = new MoleClient(ip, ConstUtils.WS_PORT, moleCallback);
				moleClient.start();
			}

			@Override
			public void sendMsgToClient(String msg) {
				if (moleServer != null) {
					Logger.info(TAG, "sendMsgToClient:" + msg);
					moleServer.sendMessage(msg);
				}
			}

			@Override
			public void sendMsgToServer(String msg) {
				if (moleClient != null) {
					Logger.info(TAG, "sendMsgToServer:" + msg);
					moleClient.sendMessage(msg);
				}
			}

		}), config);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	private boolean checkPermissions(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
		}

		return true;
	}

	@Override
	public void onBackPressed() {
		// terminal current Activity
		Toast.makeText(AndroidLauncher.this, "go to background", Toast.LENGTH_SHORT).show();
//		finish();

		moveTaskToBack(true);
	}
}
