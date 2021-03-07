package org.firstinspires.ftc.teamcode.Utility;

import android.os.Environment;

import com.acmerobotics.dashboard.config.Config;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

//import org.opencv.core.*;
//import org.opencv.imgproc.*;

/**
 * GripPipeline class.
 *
 * <p>An OpenCV pipeline generated by GRIP.
 *
 * @author GRIP
 */

@Config
public class UltimateGoalImageProcessor {

    private static UltimateGoalImageProcessor processor = new UltimateGoalImageProcessor();
    public static UltimateGoalImageProcessor getInstance(){
        return processor;
    }

    private int sizeThresh = 70000;
    private int lrTolerance = 10;
    public static double blurAmount = 10;


    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public UltimateGoalImageProcessor() {
    }

    public ImageResult process(Mat input) {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filename = "Orig.jpg";
        File file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), input);

        //Rotate the Image

        Core.rotate(input, input, Core.ROTATE_90_CLOCKWISE);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Rotate.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), input);

        List<Mat> bgr = new ArrayList<>();
        Core.split(input, bgr);
        Mat gray = bgr.get(2);
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "gray.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), gray);

        //Crop the image
        Mat cropInput = gray;
        Mat cropOutput = new Mat();
        int widthLeft = (int) Math.round(input.width() * 0.36);
        int widthRight = (int) Math.round(input.width() * 0.77);
        int heightDown = (int) Math.round(input.height() * 0.62);
        int heightUp = (int) Math.round(input.height() * 0.75);
        cropOutput = cropInput.submat(heightDown,heightUp, widthLeft, widthRight);
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Crop.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), cropOutput);

        //Convert to black and white
        Mat bwInput = cropOutput;
        Mat bwOutput = new Mat();
        Imgproc.threshold(bwInput, bwOutput, 200, 255, Imgproc.THRESH_BINARY);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Bw.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), bwOutput);

        ImageResult imageResult = new ImageResult();
        imageResult.numberOfRings = -1;

        int topWhiteCount = 0;
        int bottomWhiteCount = 0;

        for (int i = 0; i < bwOutput.height(); i++) {
            for (int j = 0; j < bwOutput.width(); j++) {
                double pixelValue = bwOutput.get(i, j)[0];
                if(pixelValue == 255) {
                    if(i < bwOutput.height()/2) {
                        topWhiteCount ++;
                    }
                    else {
                        bottomWhiteCount ++;
                    }
                }
            }
        }

        if(topWhiteCount < 500 && bottomWhiteCount < 500) {
            imageResult.numberOfRings = 0;
        }
        else if(topWhiteCount > bottomWhiteCount + 500) {
            imageResult.numberOfRings = 4;
        }
        else {
            imageResult.numberOfRings = 1;
        }

        return imageResult;

    }

}


