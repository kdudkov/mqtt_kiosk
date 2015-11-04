package name.dudkov.mqtt_kiosk.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.atomic.AtomicBoolean;

import name.dudkov.mqtt_kiosk.data.StateHolder;

/**
 * Created by Konstantin Dudkov on 23.10.15.
 */
public class MqttSender {
    private final static String TAG = MqttSender.class.getSimpleName();

    private final Context context;
    private final StateHolder stateHolder = StateHolder.getInstance();

    private String clientId;

    private final MqttWatcher watcher;
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    public MqttSender(Context context, MqttWatcher watcher) {
        this.context = context;
        this.watcher = watcher;
        this.clientId = Settings.Secure.ANDROID_ID;
    }

    public enum State {CONNECTED, DISCONNECTED, NO_NETWORK}

    private MqttAsyncClient mqttClient;
    private MqttConnectOptions connOpts;

    public void init() throws MqttException {
        if (mqttClient != null) {
            mqttClient.disconnectForcibly();
        }
        Log.i(TAG, "trying to connect to " + getServerUri());
        mqttClient = new MqttAsyncClient(getServerUri(), clientId, new MemoryPersistence());
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setConnectionTimeout(3000);
        connOpts.setKeepAliveInterval(stateHolder.getKeepalive());
        connOpts.setUserName(stateHolder.getUser());
        if (stateHolder.getPassword() != null && !"".equals(stateHolder.getPassword())) {
            connOpts.setPassword(stateHolder.getPassword().toCharArray());
        }

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Log.w(TAG, "connection to broker is lost", throwable);
                watcher.stateChanged(State.DISCONNECTED);
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                watcher.messageReceived(s, new String(mqttMessage.getPayload(), "UTF-8"));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    private String getServerUri() {
        StringBuilder sb = new StringBuilder();
        sb.append(stateHolder.isSsl() ? "ssl://" : "tcp://");
        sb.append(stateHolder.getServer());
        if (stateHolder.getPort() > 0) {
            sb.append(":").append(stateHolder.getPort());
        }
        return sb.toString();
    }

    public synchronized boolean checkNetwork() {
        if (isOnline()) {
            if (!mqttClient.isConnected()) {
                connect();
            }
            return true;

        } else {
            Log.i(TAG, "no network");
            try {
                mqttClient.disconnectForcibly();
            } catch (MqttException ignored) {
            }
            watcher.stateChanged(State.NO_NETWORK);
        }
        return false;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isAvailable() &&
                cm.getActiveNetworkInfo().isConnected();
    }

    private synchronized void connect() {
        if (!mqttClient.isConnected()) {
            if (connecting.compareAndSet(false, true)) {
                watcher.stateChanged(State.DISCONNECTED);
                Log.i(TAG, "trying to connect");
                try {
                    mqttClient.connect(connOpts, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken iMqttToken) {
                            try {
                                mqttClient.subscribe("#", 0);
                                mqttClient.subscribe("#", 1);
                            } catch (MqttException e) {
                                Log.e(TAG, "error", e);
                            }
                            Log.i(TAG, "connected");
                            watcher.stateChanged(State.CONNECTED);
                            connecting.set(false);
                        }

                        @Override
                        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                            Log.i(TAG, "can't connect", throwable);
                            watcher.stateChanged(State.DISCONNECTED);
                            connecting.set(false);
                        }
                    });

                } catch (Exception e) {
                    watcher.stateChanged(State.DISCONNECTED);
                    Log.e(TAG, "can't connect", e);
                    connecting.set(false);
                }
            }
        }

    }

}
