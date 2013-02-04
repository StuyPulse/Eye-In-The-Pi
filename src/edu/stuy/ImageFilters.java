package edu.stuy;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.CvSize;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ImageFilters {
    public static IplImage logFilter (IplImage input) {
        WritableRaster r = (WritableRaster) input.getBufferedImage().getData(); //We need this to write to/from an image

        for (int i = 0; i < r.getWidth(); i++) {
            for (int j = 0; j < r.getHeight(); j++) {
                int[] pixel = new int[3];
                pixel = r.getPixel(i, j, pixel); //get our pixel
                float[] hsb = new float[3];
                Color.RGBtoHSB(pixel[0], pixel[1], pixel[2], hsb); //Convert to HSB
                hsb[2] *= 255;
                hsb[2] -= (float) Math.log(hsb[2] - 255); // put the image through a funky logarithmic filter, to make brights brighter and darks darker.
                hsb[2] /= 255;
                float[] rgb = Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGBColorComponents(null); 
                rgb[0] *= 255;
                rgb[1] *= 255;
                rgb[2] *= 225;
                r.setPixel(i, j, rgb);
            }
        }

        BufferedImage bufferedImage = new BufferedImage(input.getBufferedImage().getWidth(), input.getBufferedImage().getHeight(), BufferedImage.TYPE_INT_RGB);
        bufferedImage.setData(r);

        IplImage logFiltered = IplImage.createFrom(bufferedImage);
        return logFiltered.nChannels(1);
        
    }
    
    public static IplImage hueFilter (IplImage input) {
        //We need a target hue to look at, and a range to look around it
        int targetValue = 30;
        int variance = 2;

        CvSize size = opencv_core.cvSize(input.width(),input.width());
        
        IplImage upper = IplImage.create(size, 8, 1);
        IplImage lower = IplImage.create(size, 8, 1);
        IplImage combined = IplImage.create(size, 8, 1);

        opencv_imgproc.cvThreshold(input, upper, targetValue-variance, 255, opencv_imgproc.CV_THRESH_BINARY);
        opencv_imgproc.cvThreshold(input, lower, targetValue+variance, 255, opencv_imgproc.CV_THRESH_BINARY_INV);

        opencv_core.cvAnd(upper, lower, combined, null);
        return combined;
    }
}
