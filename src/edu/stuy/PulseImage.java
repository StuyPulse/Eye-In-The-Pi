package edu.stuy;

import edu.wpi.first.wpijavacv.WPIBinaryImage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class PulseImage extends WPIBinaryImage {
    public PulseImage (IplImage image) {
        super(image);
    }
}
