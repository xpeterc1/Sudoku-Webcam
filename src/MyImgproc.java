import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class MyImgproc {
	//A image processing helper class that has simple uses and out of the way.
	
	private static int BOARD_DIMENSIONS = 9;
	private static int BOX_DIMENSIONS = 3;
	private static int LOW_THRESHOLD = 100;
	private static int HIGH_THRESHOLD = 255;
	
	public static Mat toGreyscale(Mat source){
		Mat greyscale = new Mat();
		Imgproc.cvtColor(source, greyscale, Imgproc.COLOR_BGR2GRAY);
		return greyscale;
	}
	public static Mat toCanny(Mat source){
		Mat canny = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.Canny(source, canny, LOW_THRESHOLD, HIGH_THRESHOLD, 3, false);
		return canny;
	}
	public static Mat toThreshBinary(Mat source){
		Mat threshold = new Mat();
		Imgproc.threshold(source, threshold, LOW_THRESHOLD, HIGH_THRESHOLD, Imgproc.THRESH_BINARY_INV);
		return threshold;
	}
	
	public static Mat toDilate(Mat source, int erosion_size){
		
		Size kSize = new Size(2 * erosion_size + 1, 2 * erosion_size + 1);
		Point anchor = new Point(erosion_size, erosion_size);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kSize, anchor);
		Mat dilateDes = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.dilate(source, dilateDes, element);
		return dilateDes;
	}

	//Provide some colors for display depending on the value given
	public static Scalar getColor(int i){
		Color c;
		switch(i){
		case 0: c = Color.red; break;
		case 1: c = Color.orange; break;
		case 2: c = Color.blue; break;
		case 3: c = Color.black; break;
		case 4: c = Color.cyan; break;
		case 5: c = Color.white; break;
		case 6: c = Color.yellow; break;
		case 7: c = Color.magenta; break;
		case 8: c = Color.green; break;
		default: c = Color.gray; break;
		}
		return new Scalar(c.getBlue(), c.getGreen(), c.getRed());
	}

	//Print board to console for Debugging int arrays
	public static void printBoard(int[][] board)
	{
		for (int i = 0; i < BOARD_DIMENSIONS; i++) {
			if (i%BOX_DIMENSIONS == 0)
				System.out.println(" -----------------------");
			for (int j = 0; j < BOARD_DIMENSIONS; j++) {
				if (j%BOX_DIMENSIONS == 0)
					System.out.print("| ");
				System.out.print(((board[i][j] == 0))? " " : Integer.toString(board[i][j]));
				System.out.print(' ');
			}
			System.out.println("|");
		}
		System.out.println(" -----------------------");
		System.out.println();
	} 
	
	//Print board to console for Debugging for CellObjects
	public static void printTable(CellObject[][] table)
	{
		for (int i = 0; i < BOARD_DIMENSIONS; i++) {
			if (i%BOX_DIMENSIONS == 0)
				System.out.println(" -----------------------");
			for (int j = 0; j < BOARD_DIMENSIONS; j++) {
				if (j%BOX_DIMENSIONS == 0)
					System.out.print("| ");
				System.out.print( (table[i][j].getValue() == 0) ? " " :
					Integer.toString(table[i][j].getValue()));
				System.out.print(' ');
			}
			System.out.println("|");
		}
		System.out.println(" -----------------------");
		System.out.println();
	}
}
