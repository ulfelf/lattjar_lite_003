package com.example.lattjar_lite_003;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String    TAG = "openCV::Activity";

    private Mat mInFrame;
    private Mat mOutFrame;
    private Mat mGray;
    private Mat mBlurred;
    private Size size = new Size(3,3);
    private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    private ArrayList arr = new ArrayList();
    private Mat mHierarchy;

    private CameraBridgeViewBase mOpenCvCameraView;


    //finner cosinus for vinklar mellan vektorer, pt0->pt1 och pt0->pt2
    static double angle(Point pt1, Point pt2, Point pt0)
    {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;

        return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }

    private void clearStatusbar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //Skalar om bilden
        mOpenCvCameraView.setMaxFrameSize(970, 540);
        clearStatusbar();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        clearStatusbar();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mInFrame    = new Mat(height, width, CvType.CV_8UC4);
        mOutFrame   = new Mat(height, width, CvType.CV_8UC4);
        mGray       = new Mat(height, width, CvType.CV_8UC4);
        mBlurred = new Mat(height, width, CvType.CV_8UC4);
        mHierarchy = new Mat();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mInFrame = inputFrame.rgba();
        Imgproc.cvtColor(mInFrame, mGray, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.Canny(mGray, mBlurred, 60, 80);

        Imgproc.findContours(mBlurred, contours, mHierarchy ,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Imgproc.drawContours(mOutFrame, contours, 1,new Scalar(120,56,255));
//        Imgproc.cvtColor(mBlurred, mOutFrame, Imgproc.COLOR_GRAY2BGRA);

        // negativt värde, rita alla vektorer, random färger. Samma färg.
        Imgproc.drawContours( mOutFrame, contours, -1, new Scalar( Math.random() * 255 + 1, Math.random() * 255 + 1,Math.random() * 255 + 1));;

        return mOutFrame;
    }
}
