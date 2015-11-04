package name.dudkov.mqtt_kiosk.service;

/**
 * Created by Konstantin Dudkov on 03.11.15.
 */
public class Utils {

    public static boolean checkTopic(String pattern, String topic) {
        String[] sp = pattern.split("/");
        String[] st = topic.split("/");

        int i = 0;
        int j = 0;
        boolean wild = false;
        while(i<sp.length && j<st.length) {
            if (sp[i].equals("#")) {
                wild = true;
                i++;
                j++;
                continue;
            }
            if (wild) {
                if (st[j].equals(sp[i])) {
                    wild = false;
                    i++;
                    j++;
                } else {
                    j++;
                }
                continue;
            }
            if (sp[i].equals("+")) {
                i++;
                j++;
                continue;
            }
            if (!sp[i].equals(st[j])) {
                return false;
            }
            i++;
            j++;
        }
        return (i==sp.length && j==st.length) || (i==sp.length && wild);
    }
}
