/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stuy;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_imgproc.*;
import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.smartdashboard.gui.DashboardFrame;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpijavacv.DaisyExtensions;
import edu.wpi.first.wpijavacv.WPIBinaryImage;
import edu.wpi.first.wpijavacv.WPIColor;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIContour;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIPoint;
import edu.wpi.first.wpijavacv.WPIPolygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import javax.imageio.ImageIO;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.Color;

/* Much of this code is based on Team 341's DaisyCV code.
 * This is an implementation of their vision code using OpenCV on a Rasberry Pi.
 * This runs as a standalone command-line utility that will run when the Pi on the robot starts.
 */
/**
 *
 * @author yulli
 */
public class EyeInThePi {
    
    public static final String NAME = "DaisyCV Target Tracker";
    private WPIColor targetColor = new WPIColor(0, 255, 0);

    // Constants that need to be tuned
    private static final double kNearlyHorizontalSlope = Math.tan(Math.toRadians(14));
    private static final double kNearlyVerticalSlope = Math.tan(Math.toRadians(90-15));
    private static final int kMinWidth = 20;
    private static final int kMaxWidth = 200;
    private static final double kRangeOffset = 0.0;
    private static final int kHoleClosingIterations = 10;

    private static final double kShooterOffsetDeg = -1.55;
    private static final double kHorizontalFOVDeg = 47.0;

    private static final double kVerticalFOVDeg = 480.0/640.0*kHorizontalFOVDeg;
    private static final double kCameraHeightIn = 54.0;
    private static final double kCameraPitchDeg = 21.0;
    private static final double kTopTargetHeightIn = 98.0 + 2.0 + 9.0; // 98 to rim, +2 to bottom of target, +9 to center of target

    private TreeMap<Double, Double> rangeTable;

    private static boolean m_debugMode = true;

    // Store JavaCV temporaries as members to reduce memory management during processing
    private CvSize size = null;
    private WPIContour[] contours;
    private ArrayList<WPIPolygon> polygons;
    private IplConvKernel morphKernel;
    private IplImage bin;
    private IplImage hsv;
    private IplImage hue;
    private IplImage sat;
    private IplImage upper;
    private IplImage lower;
    private IplImage combined;
    private IplImage lightness;
    private IplImage logFiltered;
    private WPIPoint linePt1;
    private WPIPoint linePt2;
    private int horizontalOffsetPixels;
    
    public EyeInThePi()
    {
        this(false);
    }
    
    public EyeInThePi(boolean debug)
    {
        m_debugMode = debug;
        morphKernel = IplConvKernel.create(3, 3, 1, 1, opencv_imgproc.CV_SHAPE_RECT, null);

        // Put the mode String in the SmartDashboard.
        if (!m_debugMode) {
            Robot.getTable().putString("mode", "result");
            Robot.getTable().putInt("target", 100);
            Robot.getTable().putInt("variance", 10);

        }

        rangeTable = new TreeMap<Double,Double>();
        rangeTable.put(110.0, 3800.0+kRangeOffset);
        rangeTable.put(120.0, 3900.0+kRangeOffset);
        rangeTable.put(130.0, 4000.0+kRangeOffset);
        rangeTable.put(140.0, 3434.0+kRangeOffset);
        rangeTable.put(150.0, 3499.0+kRangeOffset);
        rangeTable.put(160.0, 3544.0+kRangeOffset);
        rangeTable.put(170.0, 3574.0+kRangeOffset);
        rangeTable.put(180.0, 3609.0+kRangeOffset);
        rangeTable.put(190.0, 3664.0+kRangeOffset);
        rangeTable.put(200.0, 3854.0+kRangeOffset);
        rangeTable.put(210.0, 4034.0+kRangeOffset);
        rangeTable.put(220.0, 4284.0+kRangeOffset);
        rangeTable.put(230.0, 4434.0+kRangeOffset);
        rangeTable.put(240.0, 4584.0+kRangeOffset);
        rangeTable.put(250.0, 4794.0+kRangeOffset);
        rangeTable.put(260.0, 5034.0+kRangeOffset);
        rangeTable.put(270.0, 5234.0+kRangeOffset);

        DaisyExtensions.init();
    }
    
    public double getRPMsForRange(double range)
    {
        double lowKey = -1.0;
        double lowVal = -1.0;
        for( double key : rangeTable.keySet() )
        {
            if( range < key )
            {
                double highVal = rangeTable.get(key);
                if( lowKey > 0.0 )
                {
                    double m = (range-lowKey)/(key-lowKey);
                    return lowVal+m*(highVal-lowVal);
                }
                else
                    return highVal;
            }
            lowKey = key;
            lowVal = rangeTable.get(key);
        }

        return 5234.0+kRangeOffset;
    }
    
    @Override
    public WPIImage processImage(WPIColorImage rawImage)
    {
        double heading = 0.0;
        
        // Get the current heading of the robot first
        if( !m_debugMode )
        {
            try
            {
                heading = Robot.getTable().getDouble("Heading");
            }
            catch( NoSuchElementException e)
            {
            }
            catch( IllegalArgumentException e )
            {
            }
        }

        if( size == null || size.width() != rawImage.getWidth() || size.height() != rawImage.getHeight() )
        {
            size = opencv_core.cvSize(rawImage.getWidth(),rawImage.getHeight());
            bin = IplImage.create(size, 8, 1);
            hsv = IplImage.create(size, 8, 3);
            hue = IplImage.create(size, 8, 1);
            upper = IplImage.create(size, 8, 1);
            lower = IplImage.create(size, 8, 1);
            combined = IplImage.create(size, 8, 1);
            sat = IplImage.create(size, 8, 1);
            lightness = IplImage.create(size, 8, 1);
            logFiltered = IplImage.create(size, 8, 1);
            horizontalOffsetPixels =  (int)Math.round(kShooterOffsetDeg*(size.width()/kHorizontalFOVDeg));
            linePt1 = new WPIPoint(size.width()/2+horizontalOffsetPixels,size.height()-1);
            linePt2 = new WPIPoint(size.width()/2+horizontalOffsetPixels,0);
        }

        WritableRaster r = (WritableRaster) rawImage.getBufferedImage().getData(); //We need this to write to/from an image

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
        //System.out.println(r);

        BufferedImage bufferedImage = new BufferedImage(rawImage.getBufferedImage().getWidth(), rawImage.getBufferedImage().getHeight(), BufferedImage.TYPE_INT_RGB);
        bufferedImage.setData(r);

        logFiltered = IplImage.createFrom(bufferedImage);
        logFiltered = logFiltered.nChannels(1);
        PulseImage logImage = new PulseImage(logFiltered);

        if (m_debugMode) {
            CanvasFrame logImageFrame = new CanvasFrame("Log Image");
            logImageFrame.showImage(logImage.getBufferedImage());
        }

        // Get the raw IplImages for OpenCV
        IplImage input = DaisyExtensions.getIplImage(rawImage);


        // Convert to HSV color space
        opencv_imgproc.cvCvtColor(input, hsv, opencv_imgproc.CV_BGR2HLS);
        opencv_core.cvSplit(hsv, hue, lightness, sat, null);

        // Threshold each component separately
        // Hue
        // NOTE: Red is at the end of the color space, so you need to OR together
        // a thresh and inverted thresh in order to get points that are red
        int targetValue = 30;
        int variance = 2;
        if (!m_debugMode) {
            targetValue = Robot.getTable().getInt("target");
            variance = Robot.getTable().getInt("variance");
        }
        opencv_imgproc.cvThreshold(hue, upper, targetValue-variance, 255, opencv_imgproc.CV_THRESH_BINARY);
        opencv_imgproc.cvThreshold(hue, lower, targetValue+variance, 255, opencv_imgproc.CV_THRESH_BINARY_INV);

        opencv_core.cvAnd(upper, lower, combined, null);

        //opencv_core.cvNot(combined, combined);

        //opencv_core.cvNot(bin, bin);
        //opencv_core.cvNot(hue, hue);

        if (m_debugMode) {
            CanvasFrame postNotBin = new CanvasFrame("PostNotBin");
            postNotBin.showImage(bin.getBufferedImage());

            CanvasFrame postNotHue = new CanvasFrame("PostNotHue");
            postNotHue.showImage(hue.getBufferedImage());
        }

        // Saturation
        opencv_imgproc.cvThreshold(sat, sat, 250, 255, opencv_imgproc.CV_THRESH_BINARY);

        // Value
        opencv_imgproc.cvThreshold(lightness, lightness, 150, 255, opencv_imgproc.CV_THRESH_BINARY);

        // Combine the results to obtain our binary image which should for the most
        // part only contain pixels that we care about
        //opencv_core.cvAnd(upper, lower, bin, null);
        
        opencv_core.cvCopy(combined, bin);

        if (m_debugMode) {
            CanvasFrame asdf = new CanvasFrame("prepostbinthing");
            asdf.showImage(bin.getBufferedImage());
        }

        opencv_core.cvOr(logFiltered, bin, bin, null);
        opencv_core.cvOr(bin, sat, bin, null);
        opencv_core.cvAnd(bin, lightness, bin, null);

        if (m_debugMode) {
            CanvasFrame hsvas = new CanvasFrame("hsv");
            hsvas.showImage(hsv.getBufferedImage());

            CanvasFrame hueas = new CanvasFrame("hue");
            hueas.showImage(hue.getBufferedImage());

            CanvasFrame satas = new CanvasFrame("sat");
            satas.showImage(sat.getBufferedImage());

            CanvasFrame valas = new CanvasFrame("lightness");
            valas.showImage(lightness.getBufferedImage());

            CanvasFrame result = new CanvasFrame("binary");
            result.showImage(bin.getBufferedImage());

        }

        // Fill in any gaps using binary morphology
        opencv_imgproc.cvMorphologyEx(bin, bin, null, morphKernel, opencv_imgproc.CV_MOP_CLOSE, kHoleClosingIterations);

        // Uncomment the next two lines to see the image post-morphology
        if (m_debugMode) {
            CanvasFrame result2 = new CanvasFrame("morph");
            result2.showImage(bin.getBufferedImage());
        }

        // Find contours
        WPIBinaryImage binWpi = DaisyExtensions.makeWPIBinaryImage(bin);
        contours = DaisyExtensions.findConvexContours(binWpi);

        polygons = new ArrayList<WPIPolygon>();
        for (WPIContour c : contours)
        {
            //System.out.println("Contour: X: " + c.getX() + " Y: " + c.getY());
            rawImage.drawPoint(new WPIPoint(c.getX(), c.getY()), WPIColor.BLUE, 5);
            double ratio = ((double) c.getHeight()) / ((double) c.getWidth());
            // TODO: change magic numbers to match new targets sizes in 2013
            if (ratio < 10.0 && ratio > 0.0 && c.getWidth() > kMinWidth && c.getWidth() < kMaxWidth)
            {
                WPIPolygon p = c.approxPolygon(20);
                if (p.isConvex() && p.getNumVertices() == 4)
                {
                    System.out.println("Ratio: " + ratio);
                }
                polygons.add(c.approxPolygon(20));
            }
        }

        WPIPolygon square = null;
        int highest = Integer.MAX_VALUE;

        for (WPIPolygon p : polygons)
        {
            //System.out.println("Convex: " + p.isConvex() + " Verts: " + p.getNumVertices()); 
            if (p.isConvex() && p.getNumVertices() == 4)
            {

                // We passed the first test...we fit a rectangle to the polygon
                // Now do some more tests

                WPIPoint[] points = p.getPoints();
                // We expect to see a top line that is nearly horizontal, and two side lines that are nearly vertical
                int numNearlyHorizontal = 0;
                int numNearlyVertical = 0;
                for( int i = 0; i < 4; i++ )
                {
                    double dy = points[i].getY() - points[(i+1) % 4].getY();
                    double dx = points[i].getX() - points[(i+1) % 4].getX();
                    double slope = Double.MAX_VALUE;
                    if( dx != 0 )
                        slope = Math.abs(dy/dx);

                    if( slope < kNearlyHorizontalSlope )
                        ++numNearlyHorizontal;
                    else if( slope > kNearlyVerticalSlope )
                        ++numNearlyVertical;
                }

                if(numNearlyHorizontal >= 1 && numNearlyVertical == 2)
                {
                    rawImage.drawPolygon(p, WPIColor.BLUE, 2);

                    int pCenterX = (p.getX() + (p.getWidth() / 2));
                    int pCenterY = (p.getY() + (p.getHeight() / 2));

                    rawImage.drawPoint(new WPIPoint(pCenterX, pCenterY), targetColor, 5);
                    if (pCenterY < highest) // Because coord system is funny
                    {
                        square = p;
                        highest = pCenterY;
                    }
                }
            }
            else
            {
                rawImage.drawPolygon(p, WPIColor.YELLOW, 1);
            }
        }

        if (square != null)
        {
            double x = square.getX() + (square.getWidth() / 2);
            x = (2 * (x / size.width())) - 1;
            double y = square.getY() + (square.getHeight() / 2);
            y = -((2 * (y / size.height())) - 1);

            double azimuth = this.boundAngle0to360Degrees(x*kHorizontalFOVDeg/2.0 + heading - kShooterOffsetDeg);
            double range = (kTopTargetHeightIn-kCameraHeightIn)/Math.tan((y*kVerticalFOVDeg/2.0 + kCameraPitchDeg)*Math.PI/180.0);
            double rpms = getRPMsForRange(range);

            if (!m_debugMode)
            {
                Robot.getTable().beginTransaction();
                Robot.getTable().putBoolean("found", true);
                Robot.getTable().putDouble("azimuth", azimuth);
                Robot.getTable().putDouble("rpms", rpms);
                Robot.getTable().getString("mode");
                Robot.getTable().endTransaction();
            } else
            {
                System.out.println("Target found");
                System.out.println("x: " + x);
                System.out.println("y: " + y);
                System.out.println("azimuth: " + azimuth);
                System.out.println("range: " + range);
                System.out.println("rpms: " + rpms);
                System.out.println("height: " + square.getHeight());
                System.out.println("width: " + square.getWidth());
            }
            rawImage.drawPolygon(square, targetColor, 7);
        } else
        {

            if (!m_debugMode)
            {
                Robot.getTable().putBoolean("found", false);
            } else
            {
                System.out.println("Target not found");
            }
        }

        // Draw a crosshair
        rawImage.drawLine(linePt1, linePt2, targetColor, 2);

        DaisyExtensions.releaseMemory();

        //System.gc();

        //Choose a frame to show in the SmartDashboard
        if (!m_debugMode) {
            String modeChoice = Robot.getTable().getString("mode");
                if (modeChoice.equals("result"))
                    return rawImage;
                if (modeChoice.equals( "bin"))
                    return new PulseColorImage(bin.getBufferedImage());
                if (modeChoice.equals( "input"))
                    return new PulseColorImage(input.getBufferedImage());
                if (modeChoice.equals( "log"))
                    return new PulseColorImage(logFiltered.getBufferedImage());
                if (modeChoice.equals( "hsv"))
                    return new PulseColorImage(hsv.getBufferedImage());
                if (modeChoice.equals( "hue"))
                    return new PulseColorImage(hue.getBufferedImage());
                if (modeChoice.equals( "combined"))
                    return new PulseColorImage(combined.getBufferedImage());
                if (modeChoice.equals( "upper"))
                    return new PulseColorImage(upper.getBufferedImage());
                if (modeChoice.equals( "lower"))
                    return new PulseColorImage(lower.getBufferedImage());
                if (modeChoice.equals( "sat"))
                    return new PulseColorImage(sat.getBufferedImage());
                if (modeChoice.equals( "lightness"))
                    return new PulseColorImage(lightness.getBufferedImage());
        }
        return rawImage;
    }
    
    private double boundAngle0to360Degrees(double angle)
    {
        // Naive algorithm
        while(angle >= 360.0)
        {
            angle -= 360.0;
        }
        while(angle < 0.0)
        {
            angle += 360.0;
        }
        return angle;
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Usage: Arguments are paths to image files to test the program on");
            return;
        }

        
        //new DashboardFrame(!m_debugMode); //Call the constructor for DashboardFrame, because FIRST is stupid.
        // Create the PiEye
        EyeInThePi pieye = new EyeInThePi(true);

        long totalTime = 0;
        for (int i = 0; i < args.length; i++)
        {
            // Load the image
            WPIColorImage rawImage = null;
            try
            {
                rawImage = new WPIColorImage(ImageIO.read(new File(args[i%args.length])));
            } catch (IOException e)
            {
                System.err.println("Could not find file!");
                return;
            }
            
            //shows the raw image before processing to eliminate the possibility
            //that both may be the modified image.
            CanvasFrame original = new CanvasFrame("Raw");
            original.showImage(rawImage.getBufferedImage());

            WPIImage resultImage = null;

            // Process image
            long startTime, endTime;
            startTime = System.nanoTime();
            resultImage = pieye.processImage(rawImage);
            endTime = System.nanoTime();

            // Display results
            totalTime += (endTime - startTime);
            double milliseconds = (double) (endTime - startTime) / 1000000.0;
            System.out.format("Processing took %.2f milliseconds%n", milliseconds);
            System.out.format("(%.2f frames per second)%n", 1000.0 / milliseconds);
            
            CanvasFrame result = new CanvasFrame("Result");
            result.showImage(resultImage.getBufferedImage());

            System.out.println("Waiting for ENTER to continue to next image or exit...");
            Scanner console = new Scanner(System.in);
            console.nextLine();

            if (original.isVisible())
            {
                original.setVisible(false);
                original.dispose();
            }
            if (result.isVisible())
            {
                result.setVisible(false);
                result.dispose();
            }
        }

        double milliseconds = (double) (totalTime) / 1000000.0 / (args.length);
        System.out.format("AVERAGE:%.2f milliseconds%n", milliseconds);
        System.out.format("(%.2f frames per second)%n", 1000.0 / milliseconds);
        System.exit(0);
    }
}
