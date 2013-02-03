package edu.stuy;

import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIColorImage;
import java.awt.image.BufferedImage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

// We need this because WPIColorImages can be stupid sometimes and it lets us change stuff if we need to.
public class PulseColorImage extends WPIColorImage {
    //public PulseColorImage (IplImage image) {
        //super(image.nChannels(1));
    //}
    public PulseColorImage (BufferedImage image) {
        super(image);
    }
}
