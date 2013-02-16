package edu.stuy;

import java.net.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Iterator;
import java.io.StringWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.*;

import javax.imageio.*;
import javax.imageio.stream.*;

import edu.wpi.first.wpijavacv.WPICamera;
import edu.wpi.first.wpijavacv.WPIColorImage;

/*
 * This class has the responsibility of talking to cameras.
 * A dignified position.
 */
public class Camera {
    public static final String cameraIP = "10.6.94.12";
    public static final String imageURL = "/axis-cgi/jpg/image.cgi";
    private InputStream cam;
    private StringWriter stringWriter;
    //private Thread camThread;
    //private MjpegRunner cam;
    //private WPICamera _cam;
    
    public Camera () {
        try {
            URL asset = new URL("http://" + cameraIP + imageURL);
            cam = asset.openStream();
            stringWriter = new StringWriter(128);
            //cam = new MjpegRunner(asset);
            //camThread = new Thread(cam);
            //camThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public WPIColorImage getFrame () {
	long startTime = System.currentTimeMillis();
        try {
            URL asset = new URL("http://" + cameraIP + imageURL);
            System.out.println(asset);
            /*cam = asset.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(cam));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    String line = "";
	    while ((line = reader.readLine()) != null) {
                baos.write(line.getBytes(), 0, line.getBytes().length);
            }*/
	    WPIColorImage wpici = new WPIColorImage(ImageIO.read(asset));//new ByteArrayInputStream(baos.toByteArray())));
	    long endTime = System.currentTimeMillis();
	    System.out.println(endTime-startTime);
            return wpici;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


 
}
