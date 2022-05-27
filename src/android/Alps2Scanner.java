package com.intelliacc.alps2Scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.pda.serialport.Tools2;

public class Alps2Scanner extends CordovaPlugin {
  private final String TAG = "Alps2Scanner";

  private boolean startFlag = false;

  private CallbackContext CallbackContext; // The callback context from which we were invoked.
  // private JSONArray executeArgs;

  private Lf134KManager manager;
  public boolean shouldKeepCallback = false;

  private boolean isUHFLoop = false;
  private ReadThread mReadThread;

  private ScanThread scanThread;
  private boolean mIsContinuous = false;
  private Timer scanTimer = null;

  PluginResult resultLoop;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext CallbackContext) throws JSONException {
    this.CallbackContext = CallbackContext;
    // this.executeArgs = args;

    Context context = this.cordova.getActivity().getApplicationContext();

    IntentFilter filter = new IntentFilter();
    filter.addAction("android.rfid.FUN_KEY");
    filter.addAction("android.intent.action.FUN_KEY");
    this.cordova.getActivity()
        .getApplicationContext()
        .registerReceiver(keyReceiver, filter);

    if (action.equals("getMServiceInit")) {
      CallbackContext.sendPluginResult(
          new PluginResult(PluginResult.Status.OK, manager != null));
      return true;
    }

    if (action.equals("uninitMService")) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (manager != null) {
        this.startFlag = false;
        this.runFlag = false;
        this.manager.Close();
        manager = null;
      }
      if (this.cordova.getActivity() != null) {
        this.cordova.getActivity()
            .getApplicationContext()
            .unregisterReceiver(keyReceiver);
      }
      CallbackContext.sendPluginResult(
          new PluginResult(PluginResult.Status.OK, true));
      return true;
    }

    if (action.equals("initMService")) {
      try {
        manager = new Lf134KManager();
        // Util.initSoundPool(context);

        CallbackContext.sendPluginResult(
            new PluginResult(PluginResult.Status.OK, true));
      } catch (Exception e) {
        CallbackContext.sendPluginResult(
            new PluginResult(PluginResult.Status.OK, false));
        // Toast.makeText(App.this, "dasd", Toast.LENGTH_SHORT).show();
      }
    }

    if (action.equals("triggerUHFContinuous")) {
      cordova
          .getThreadPool()
          .execute(
              new Runnable() {
                public void run() {
                  shouldKeepCallback = true;
                  triggerUHFContinuous();
                }
              });
      return true;
    }

    if (action.equals("triggerUHFSingle")) {
      cordova
          .getThreadPool()
          .execute(
              new Runnable() {
                public void run() {
                  shouldKeepCallback = false;
                  triggerUHFContinuous();
                }
              });
      return true;
    }

    if (action.equals("triggerUHFEnd")) {
      triggerUHFEnd();
      return true;
    }

    if (action.equals("getUHFLoop")) {
      CallbackContext.sendPluginResult(
          new PluginResult(PluginResult.Status.OK, false));

      return true;
    }

    if (action.equals("scanBarcode")) {
      scanBarcode();
      return true;
    }

    return false;
  }

  public void triggerUHFContinuous() {
    if (manager == null) {
      Log.e(TAG, "failure mUhfrManager = null");
      return;
    }
    if (!startFlag) {
      startFlag = true;
      runFlag = true;
      mReadThread = new ReadThread();
      mReadThread.start();
    }
  }

  public void triggerUHFEnd() {
    // RFID
    this.startFlag = false;
    this.runFlag = false;
    // this.manager.Close();

    // BARCODE
    if (scanThread != null) {
      scanThread.stopScan();
      scanThread.interrupt();
      scanThread.close();
      scanThread = null;
    }

  }

  public void scanBarcode() {
    try {
      scanThread = new ScanThread(mHandler);
    } catch (Exception e) {
      Log.d(TAG, "scanBarcode: " + e);
      return;
      // e.printStackTrace();
    }
    scanThread.start();
    // init sound
    // Util.initSoundPool(this.cordova.getActivity().getApplicationContext());
    scanThread.scan();
    // 注册按键广播接收者
    // keyReceiver = new KeyReceiver();
    // IntentFilter filter = new IntentFilter();
    // filter.addAction("android.rfid.FUN_KEY");
    // filter.addAction("android.intent.action.FUN_KEY");
    // registerReceiver(keyReceiver, filter);
    // mRegisterFlag = true;
  }

  // key receiver
  private long startTime = 0;
  private boolean keyUpFalg = true;
  private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int keyCode = intent.getIntExtra("keyCode", 0);
      if (keyCode == 0) { // H941
        keyCode = intent.getIntExtra("keycode", 0);
      }
      boolean keyDown = intent.getBooleanExtra("keydown", false);
      if (keyUpFalg && keyDown && System.currentTimeMillis() - startTime > 500) {
        keyUpFalg = false;
        startTime = System.currentTimeMillis();
        if (( // keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2
        keyCode == KeyEvent.KEYCODE_F3 ||
        // keyCode == KeyEvent.KEYCODE_F4 ||
            keyCode == KeyEvent.KEYCODE_F5)) {
          // Log.e("key ","inventory.... " ) ;
          // onClick(btnStart);
          isRead();
        }
        return;
      } else if (keyDown) {
        startTime = System.currentTimeMillis();
      } else {
        keyUpFalg = true;
      }
    }
  };

  public void isRead() {
    if (manager == null) {
      // showToast(getActivity().getString(R.string.connection_failed));
      return;
    }
    Context context = this.cordova.getActivity().getApplicationContext();
    if (!startFlag) {
      final Intent intent = new Intent("hardwareTriggerOn");
      context.sendOrderedBroadcast(intent, null);
      // triggerUHFContinuous();
    } else {
      final Intent intent = new Intent("hardwareTriggerOff");
      context.sendOrderedBroadcast(intent, null);
      // triggerUHFEnd();
    }
  }

  private boolean runFlag = true;

  private class ReadThread extends Thread {

    @Override
    public void run() {
      super.run();
      while (runFlag) {
        if (startFlag) {
          Lf134kDataModel model = manager.GetData(1000);
          if (model != null) {
            sendMSG(
                Tools2.BytesToLong(model.ID) + "",
                Tools2.BytesToLong(model.Country) + "",
                model.Type);
          }
        }
      }
    }

    private void sendMSG(String id, String nation, String type) {
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          int position;
          JSONObject res = new JSONObject();
          try {
            // EpcDataModel epcTag = new EpcDataModel();
            // res.setepc(epc);
            res.put("data", id);
            res.put("Count", 1);
            res.put("nation", nation);
            res.put("type", type);

            resultLoop = new PluginResult(PluginResult.Status.OK, res.toString(2));
            resultLoop.setKeepCallback(shouldKeepCallback);
            CallbackContext.sendPluginResult(resultLoop);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }, 200);
    }
  }

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      if (msg.what == ScanThread.SCAN) {
        // String data = msg.getData().getString("data");
        byte[] dataBytes = msg.getData().getByteArray("dataBytes");
        if (dataBytes == null || dataBytes.length == 0) {
          if (mIsContinuous) {
            scanThread.scan();
          }
          return;
        }
        JSONObject res = new JSONObject();

        String dataStr = new String(dataBytes, 0, dataBytes.length);
        try {
          res.put("data", dataStr);
          res.put("dataBytes", dataBytes);
          resultLoop = new PluginResult(PluginResult.Status.OK, res.toString(2));
          resultLoop.setKeepCallback(shouldKeepCallback);
          CallbackContext.sendPluginResult(resultLoop);
        } catch (Exception e) {
          e.printStackTrace();
        }

        // Util.play(1, 0);
        if (mIsContinuous) {
          scanThread.scan();
        }
      }
    };
  };
}
