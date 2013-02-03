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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("asdf");
        System.out.println("bazinga");
        System.out.println("zimbabwe");
    }
}
