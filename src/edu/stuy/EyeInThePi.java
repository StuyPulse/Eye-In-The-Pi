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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("asdf");
        System.out.println("bazinga");
        System.out.println("zimbabwe");
    }
}
