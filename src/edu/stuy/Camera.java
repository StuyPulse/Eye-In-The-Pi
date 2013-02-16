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
import javax.swing.*;
import java.awt.*;

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
            ImageIcon image0 = new ImageIcon(asset);
            BufferedImage bi = new BufferedImage(image0.getIconWidth(), image0.getIconHeight(), BufferedImage.TYPE_INT_RGB);
            System.out.println(bi);
            Graphics g = bi.createGraphics();
            image0.paintIcon(null, g, 0, 0);
            g.dispose();
            //File outputFile = new File("saved.png");
            //ImageIO.write(bi, "png", outputFile);
            //System.exit(0);
            /*cam = asset.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(cam));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    String line = "";
	    while ((line = reader.readLine()) != null) {
                baos.write(line.getBytes(), 0, line.getBytes().length);
            }*/
        //WPIColorImage wpici = new WPIColorImage(ImageIO.read(asset));//new ByteArrayInputStream(baos.toByteArray())));
	    long endTime = System.currentTimeMillis();
	    System.out.println(endTime-startTime);
        return new WPIColorImage(bi);//wpici;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


 
}
