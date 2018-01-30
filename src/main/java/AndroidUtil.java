import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

public class AndroidUtil {
    private static final Logger logger = LogManager.getLogger(AndroidUtil.class);

    public static void exec(String cmd) throws IOException {
        logger.info("cmd:" + cmd);
        Runtime rt = Runtime.getRuntime();
        Process exec = rt.exec(cmd);
    }


    public static void getScreehot(String filepath) {
        try {
            exec("adb shell /system/bin/screencap -p /sdcard/screenshot.png");
            exec("adb pull /sdcard/screenshot.png " + filepath);
            Thread.sleep(2000);
            logger.info("screenshot successed...");
        } catch (IOException e) {
            logger.info("screenshot error...");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        getScreehot("D:/screenshot");
    }

}
