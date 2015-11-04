package name.dudkov.mqtt_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import name.dudkov.mqtt_kiosk.data.StateHolder;
import name.dudkov.mqtt_kiosk.service.MainService;
import name.dudkov.mqtt_kiosk.service.MqttSender;

public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences sp;

    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {
            if (message.what == 1) {
//                toast(message.getData().getString("msg"));
            } else {
                updateScreen();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StateHolder.getInstance().setUiHandler(handler);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        changeBrightness(.01f);

        startService(new Intent(MainActivity.this, MainService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi;
        mi = menu.add(0, 1, 0, "Preferences");
        mi.setIntent(new Intent(this, PrefsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    private void changeBrightness(float level) {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.screenBrightness = level;
        this.getWindow().setAttributes(lp);
    }

    private void updateScreen() {
        StateHolder stateHolder = StateHolder.getInstance();
        TextView[] captionViews = {
                (TextView) findViewById(R.id.mainTextCaption),
                (TextView) findViewById(R.id.textCaption2),
                (TextView) findViewById(R.id.textCaption3),
                (TextView) findViewById(R.id.textCaption4),
                (TextView) findViewById(R.id.textCaption5),
                (TextView) findViewById(R.id.textCaption6),
                (TextView) findViewById(R.id.textCaption7),
        };
        TextView[] textViews = {
                (TextView) findViewById(R.id.mainText),
                (TextView) findViewById(R.id.text2),
                (TextView) findViewById(R.id.text3),
                (TextView) findViewById(R.id.text4),
                (TextView) findViewById(R.id.text5),
                (TextView) findViewById(R.id.text6),
                (TextView) findViewById(R.id.text7),
        };
        Object[] data = stateHolder.getData();
        for (int i=0;i<StateHolder.SLOTS;i++) {
            TextView text = textViews[i];
            TextView caption = captionViews[i];
            String title = sp.getString("caption" + (i+1), "");
            if (! "".equals(title)) {
                caption.setText(title);

                if (data[i] == null) {
                    text.setText("-");
                    text.setBackgroundColor(getResources().getColor(R.color.no_data));
                } else {
                    String format = sp.getString("format" + (i+1), "%s");
                    String val = String.format(format, data[i]);
                    text.setText(val);
                    int color = getResources().getColor(R.color.default_data);
                    if (i == 0 && data[i] instanceof Float) {
                        Float n = (Float) data[i];
                        color = getTempColor(n);
                    }
                    if (val.equals("on")) {
                        color = getResources().getColor(R.color.switch_on);
                    }
                    if (val.equals("off")) {
                        color = getResources().getColor(R.color.switch_off);
                    }
                    text.setBackgroundColor(color);
                }

            } else {
                caption.setText("");
                text.setText("-");
            }

        }

        TextView text;
        text = (TextView) findViewById(R.id.lastTopicText);
        text.setText(stateHolder.getLastTopic());

        text = (TextView) findViewById(R.id.statusText);
        if (stateHolder.getState() == MqttSender.State.CONNECTED) {
            text.setText(stateHolder.getState().toString());
            text.setTextColor(Color.WHITE);
        } else {
            text.setText(stateHolder.getState().toString());
            text.setTextColor(Color.RED);
        }

    }

    private int getTempColor(Float f) {
        if (f >= 21 && f < 25) {
            return Color.parseColor("#4c9900");
        }
        if (f >=25 && f < 28) {
            return Color.parseColor("#ff8000");
        }
        if (f >= 28) {
            return Color.parseColor("#cc0000");
        }
        if (f > 18 && f < 21) {
            return Color.parseColor("#0099aa");
        }
        return Color.DKGRAY;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.text6) {
            int i = 6;

        }
    }

    public String getTitle(int i) {
        return sp.getString("title" + i, "");
    }

    public int getSwitchColor(String val) {
        int color = R.color.default_data;

        if (val.equals("on")) {
            color = getResources().getColor(R.color.switch_on);
        }
        if (val.equals("off")) {
            color = getResources().getColor(R.color.switch_off);
        }
        return color;
    }
}
