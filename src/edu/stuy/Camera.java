package edu.stuy;

import java.net.URL;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import edu.wpi.first.wpijavacv.WPICamera;
import edu.wpi.first.wpijavacv.WPIColorImage;

/*
 * This class has the responsibility of talking to cameras.
 * A dignified position.
 */
public class Camera {
    public static final String cameraIP = "10.6.94.12";
    private WPICamera _cam;
    
    public Camera () {
        _cam = new WPICamera(cameraIP);
    }
    

    public WPIColorImage getFrame () {
        try {
            return new WPIColorImage(_cam.getImage().getBufferedImage());
        } catch (Exception e) {
            return null;
        }
    }
    
}
