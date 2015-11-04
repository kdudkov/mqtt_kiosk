package name.dudkov.mqtt_kiosk;

import junit.framework.TestCase;
import junit.framework.Test;

import name.dudkov.mqtt_kiosk.service.Utils;


/**
 * Created by Konstantin Dudkov on 03.11.15.
 */
public class UtilsTest extends TestCase {

    public void test1() {
        assertTrue(Utils.checkTopic("aa/bb", "aa/bb"));
        assertFalse(Utils.checkTopic("aa/bb", "aa/bb/cc"));

    }
}