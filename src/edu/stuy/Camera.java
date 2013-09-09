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
import java.awt.image.*;

import java.util.Hashtable;

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
    private BufferedImage cachedImage;
    //private Thread camThread;
    //private MjpegRunner cam;
    //private WPICamera _cam;
    
    public Camera () {
        try {
            //URL asset = new URL("http://" + cameraIP + imageURL);
            //cam = asset.openStream();
            //stringWriter = new StringWriter(128);
            //cachedImage = ImageIO.read(new URL("http://" + cameraIP + imageURL));
            ImageIO.setUseCache(false);
            //cam = new MjpegRunner(asset);
            //camThread = new Thread(cam);
            //camThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public WPIColorImage getFrame () {
        try {
            URL asset = new URL("http://i.dailymail.co.uk/i/pix/2013/02/25/article-2284287-184499FD000005DC-943_964x641.jpg");//("http://" + cameraIP + imageURL);
            BufferedImage imagiobi = ImageIO.read(asset);
            WPIColorImage wpici = new WPIColorImage(imagiobi);//new ByteArrayInputStream(baos.toByteArray())));
            return wpici;
            //return new WPIColorImage(bi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


 
}
