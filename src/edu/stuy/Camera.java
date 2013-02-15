package edu.stuy;

import java.net.URL;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import edu.wpi.first.wpijavacv.WPICamera;
import edu.wpi.first.wpijavacv.WPIColorImage;

import org.magnos.asset.Assets;

/*
 * This class has the responsibility of talking to cameras.
 * A dignified position.
 */
public class Camera {
    public static final String cameraIP = "10.6.94.12";
    public static final String imageURL = "/mjpg/video.mjpg";
    //private WPICamera _cam;
    
    public Camera () {
        //_cam = new WPICamera(cameraIP);
    }
    

    public WPIColorImage getFrame () {
        try {
            //return new WPIColorImage(_cam.getImage().getBufferedImage());
            return new WPIColorImage((BufferedImage) Assets.load("http://" + cameraIP + imageURL));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
