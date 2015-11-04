package name.dudkov.mqtt_kiosk.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.TimeUnit;

import name.dudkov.mqtt_kiosk.data.StateHolder;

public class MainService extends Service implements MqttWatcher {
    private final static String TAG = MainService.class.getSimpleName();
    public static final long RECONNECT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30);
    public static final long CHECK_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    private SharedPreferences sp;
    private MqttSender mqttSender;

    private StateHolder stateHolder = StateHolder.getInstance();

    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {
            if (message.what == 0) {
                try {
                    mqttSender.checkNetwork();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(0, RECONNECT_INTERVAL_MS);
            } else if (message.what == 1) {
                try {
                    checkData();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
                handler.removeMessages(1);
                handler.sendEmptyMessageDelayed(1, CHECK_INTERVAL_MS);
            }
            return true;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mqttSender = new MqttSender(this, this);
        try {
            mqttSender.init();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        scheduleReconnect(100);
        scheduleDataCheck(CHECK_INTERVAL_MS);
    }

    @Override
    public void messageReceived(String topic, String payload) {
        stateHolder.setLastTopic(topic);
        Object[] data = stateHolder.getData();
        Long[] time = stateHolder.getTime();
        for (int i=0; i<StateHolder.SLOTS; i++) {
            String pattern = sp.getString("topic" + (i+1), "");
            if (! "".equals(pattern) && Utils.checkTopic(pattern, topic)) {
                try {
                    float val = Float.parseFloat(payload);
                    data[i] = val;
                    time[i] = System.currentTimeMillis();
                } catch (Exception ignored) {
                    data[i] = payload;
                    time[i] = System.currentTimeMillis();
                }
                break;
            }
        }
        updateScreen();
    }

    @Override
    public void stateChanged(MqttSender.State state) {
        stateHolder.setState(state);
        updateScreen();
        switch (state) {
            case DISCONNECTED:
                scheduleReconnect(100);
        }
    }

    private void checkData() {
        Object[] data = stateHolder.getData();
        Long[] time = stateHolder.getTime();
        boolean update = false;
        for (int i=0; i<StateHolder.SLOTS; i++) {
            if (time[i] != null && (System.currentTimeMillis() - time[i] > TimeUnit.MINUTES.toMillis(10))) {
                data[i] = null;
                update = true;
            }
        }
        if (update) {
            updateScreen();
        }
    }

    private void updateScreen() {
        Handler uiHandler = stateHolder.getUiHandler();
        if (uiHandler != null) {
            uiHandler.sendEmptyMessage(0);
        }
    }

    private void scheduleReconnect(long i) {
        handler.sendEmptyMessageDelayed(0, i);
    }

    private void scheduleDataCheck(long i) {
        handler.sendEmptyMessageDelayed(1, i);
    }
}
