import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * https://github.com/hylun/AutoPlayWechatJumpGame
 */
public class AndroidUtil {
    private static final Logger logger = LogManager.getLogger(AndroidUtil.class);
    private static double rate = 1.5;

    public static void exec(String cmd) throws IOException {
        logger.info("cmd:" + cmd);
        Runtime rt = Runtime.getRuntime();
        Process exec = rt.exec(cmd);
    }


    //截屏将图片保存到相应位置
    public static void getScreehot(String filepath) {
        try {
            exec("adb shell /system/bin/screencap -p /sdcard/screenshot.png");
            Thread.sleep(1000);
            exec("adb pull /sdcard/screenshot.png " + filepath);
            Thread.sleep(2500);
            logger.info("screenshot successed...");
        } catch (IOException e) {
            logger.info("screenshot error...");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //长按屏幕
    public static void swipeScree(int interval){
        try {
            exec("adb shell input swipe 500 500 500 500 " + interval);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int scanPic(File pic) throws Exception {
        BufferedImage bi = ImageIO.read(pic);
        //获取图像的宽度和高度
        int width = bi.getWidth();
        int height = bi.getHeight();
        int x1 = 0, y1 = 0, x2 = 0, y2 = 0,r = width/1080;
        //扫描获取黑棋位置
        for (int i = 50; i < width; i++) {
            for (int flag = 0, j = height * 3 / 4; j > height / 3; j -= 5) {
                if (!colorDiff(bi.getRGB(i,j),55<<16|58<<8|100)) flag++;
                if (flag > 3) {
                    x1 = i + 13*r;
                    y1 = j + 2*r;
                    break;
                }
            }
            if (x1 > 0) break;
        }
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3f));
        //扫描目标点
        for (int i = height / 3; i < y1; i++) {
            int p1 = bi.getRGB(99, i);
            for (int j = 100; j < width; j++) {
                if (colorDiff(bi.getRGB(j,i),p1)) {
                    if(Math.abs(j-x1)<50*r) {//黑棋比图高
                        j = j + 50*r;
                    }else {
                        x2 = j;
                        y2 = i;
                        break;
                    }
                }
            }
            if (x2 > 0) {//找到了目标块顶点
                int p2 = bi.getRGB(x2, y2 - 10),j,max = -1;
                for (; i < y1-50*r;i += 5 ) {
                    for (j = x2; colorDiff(bi.getRGB(j,i),p2) && j<x2+200*r;) j++;
                    if(max < 0 && j-x2>0) x2 = x2 + (j-x2)/2;//修正顶点横坐标
                    if(max < j - x2) max = j - x2;//找到目标块最长宽度
                    else break;
                }
                g2d.drawLine(x2,y2,x2,i);
                y2 = i - 5;
                g2d.drawLine(x2-max,y2,x2+max,y2);
                break;
            }
        }
        g2d.drawLine(x1,y1,x2,y2);
        ImageIO.write(bi, "png", new FileOutputStream(pic));//保存成图片
        double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        if (x1 < 50 || y1 < 50 || x2 < 50 || y2 < 50 || distance < 100) {
//            if (!restart) throw new Exception("scan error:" + x1 + "|" + y1 + "|" + x2 + "|" + y2);
            int x = width / 2, y = height * 3 / 4, z = 9 * height / 10, i = y;//获取开始按钮位置，自动重新开始
            while( (i+=20) < z) if (bi.getRGB(x, i) == -1 && bi.getRGB(x + 20, i + 20) == -1) break;
            if (i == y-20 || i == z) throw new Exception("scan error:game not start");
            return 100;
        }
        if (distance < 150) distance = 150;
        return (int) (distance * rate);
    }

    private static boolean colorDiff(int c1, int c2){
        int c11 = c1 >> 16 & 0xFF,c12 = c1 >> 8 & 0xFF,c13 = c1 & 0xFF;
        int c21 = c2 >> 16 & 0xFF,c22 = c2 >> 8 & 0xFF,c23 = c2 & 0xFF;
        return Math.abs(c11 - c21) > 5 || Math.abs(c12 - c22) > 5 || Math.abs(c13 - c23) > 5;
    }


    public static void main(String[] args) throws Exception {
        logger.info("start game.....");
        while (true) {
            getScreehot("D:/screenshot");
            int interval = scanPic(new File("D:/screenshot/screenshot.png"));
            swipeScree(interval);
            Thread.sleep(2000);
        }
    }

}
