package name.dudkov.mqtt_kiosk.service;

/**
 * Created by Konstantin Dudkov on 23.10.15.
 */
public interface MqttWatcher {
    void messageReceived(String topic, String payload);
    void stateChanged(MqttSender.State state);
}
