import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class ImgHelper {
	//A image processing helper class that has simple uses and out of the way.
	
	private static int LOW_THRESHOLD = 100;
	private static int HIGH_THRESHOLD = 255;
	private final static int BOARD_DIMENSION = 9;
	private final static int BOX_DIMENSION = 3;
	
	//remove color data from the image
	public static Mat toGreyscale(Mat source)
	{
		Mat greyscale = new Mat();
		Imgproc.cvtColor(source, greyscale, Imgproc.COLOR_BGR2GRAY);
		return greyscale;
	}
	
	//Remove range of grey to be within the low and high threshold and create an bone structure like image
	public static Mat toCanny(Mat source)
	{
		Mat canny = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.Canny(source, canny, LOW_THRESHOLD, HIGH_THRESHOLD, 3, false);
		return canny;
	}
	
	//Invert black and white images
	public static Mat toThreshBinary(Mat source){
		Mat threshold = new Mat();
		Imgproc.threshold(source, threshold, LOW_THRESHOLD, HIGH_THRESHOLD, Imgproc.THRESH_BINARY_INV);
		return threshold;
	}
	
	//Reduce noise from images
	public static Mat toDilate(Mat source, int erosion_size)
	{	
		Size kSize = new Size(2 * erosion_size + 1, 2 * erosion_size + 1);
		Point anchor = new Point(erosion_size, erosion_size);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kSize, anchor);
		Mat dilateDes = new Mat(source.size(), IPL_DEPTH_8U);
		Imgproc.dilate(source, dilateDes, element);
		return dilateDes;
	}
	

	//Mat to BufferedImage
	public static BufferedImage ToBufferedImage(Mat frame) 
	{
		int type = 0;
		if (frame.channels() == 1) 
		{
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) 
		{
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);
		return image;
	}

	//Provide some colors for display depending on the value given
	public static Scalar getColor(int i)
	{
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
	
	//Print board to console for Debugging for CellObjects
	public static void printBoard(int[][] table)
	{
		for (int i = 0; i < BOARD_DIMENSION; i++) {
			if (i%BOX_DIMENSION == 0)
				System.out.println(" -----------------------");
			for (int j = 0; j < BOARD_DIMENSION; j++) {
				if (j%BOX_DIMENSION == 0)
					System.out.print("| ");
				System.out.print( (table[i][j] == 0) ? " " :
					Integer.toString(table[i][j]));
				System.out.print(' ');
			}
			System.out.println("|");
		}
		System.out.println(" -----------------------");
		System.out.println();
	}

}
