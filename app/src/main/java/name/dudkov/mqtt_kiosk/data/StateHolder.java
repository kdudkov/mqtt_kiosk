package name.dudkov.mqtt_kiosk.data;


import android.os.Handler;

import name.dudkov.mqtt_kiosk.service.MqttSender;

/**
 * Created by madrider on 15.10.15.
 */
public class StateHolder {
    public static final int SLOTS = 7;
    private static StateHolder ourInstance = new StateHolder();

    public static StateHolder getInstance() {
        return ourInstance;
    }

    private String server = "192.168.0.1";
    private String user = "1";
    private String password;
    private int port = 0;
    private boolean ssl = false;
    private int keepalive = 60;

    private MqttSender.State state = MqttSender.State.DISCONNECTED;

    private Object[] data = new Object[SLOTS];
    private Long[] time = new Long[SLOTS];
    private String lastTopic;

    private Handler uiHandler;

    private StateHolder() {
    }

    public synchronized String getServer() {
        return server;
    }

    public synchronized void setServer(String server) {
        this.server = server;
    }

    public synchronized String getUser() {
        return user;
    }

    public synchronized void setUser(String user) {
        this.user = user;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized void setPort(int port) {
        this.port = port;
    }

    public synchronized boolean isSsl() {
        return ssl;
    }

    public synchronized void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public synchronized int getKeepalive() {
        return keepalive;
    }

    public synchronized void setKeepalive(int keepalive) {
        this.keepalive = keepalive;
    }

    public synchronized MqttSender.State getState() {
        return state;
    }

    public synchronized void setState(MqttSender.State state) {
        this.state = state;
    }

    public synchronized Object[] getData() {
        return data;
    }

    public synchronized void setData(Object[] data) {
        this.data = data;
    }

    public synchronized Long[] getTime() {
        return time;
    }

    public synchronized void setTime(Long[] time) {
        this.time = time;
    }

    public synchronized String getLastTopic() {
        return lastTopic;
    }

    public synchronized void setLastTopic(String lastTopic) {
        this.lastTopic = lastTopic;
    }

    public synchronized Handler getUiHandler() {
        return uiHandler;
    }

    public synchronized void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }
}
