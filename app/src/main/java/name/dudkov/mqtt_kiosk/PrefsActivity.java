package name.dudkov.mqtt_kiosk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Konstantin Dudkov on 03.11.15.
 */
public class PrefsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
