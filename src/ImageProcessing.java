import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class ImageProcessing {

	
	public static Mat toGreyscale(Mat source){
		Mat greyscale = new Mat();//(source.size(), IPL_DEPTH_8U);
		Imgproc.cvtColor(source, greyscale, Imgproc.COLOR_BGR2GRAY);
		return greyscale;
	}
	public static Mat toCanny(Mat source){
		Mat canny = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.Canny(source, canny, 100, 255, 3, false);
		return canny;
	}
	public static Mat toThreshBinary(Mat source){
		Mat threshold = new Mat();//(source.size(), IPL_DEPTH_8U);
		Imgproc.threshold(source, threshold, 100, 255, Imgproc.THRESH_BINARY_INV);
		return threshold;
	}
	
	public static Mat toDilate(Mat source, int erosion_size){
		//int erosion_size = 3;
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1),	new 

Point(erosion_size, erosion_size));
		Mat dilateDes = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.dilate(source, dilateDes, element);
		return dilateDes;
	}
	
	public static Mat toThreshBinaryTest(Mat source){
		Mat threshold = new Mat();//(source.size(), IPL_DEPTH_8U);
		Imgproc.threshold(source, threshold, 100, 255, Imgproc.THRESH_BINARY_INV);
		return threshold;
	}
	
	public static Mat toCannyTest(Mat source){
		Mat canny = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.Canny(source, canny, 100, 255, 3, false);
		return canny;
	}
	

	
