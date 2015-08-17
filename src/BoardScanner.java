import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Image;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
/*Author: Peter Chow
 * 
 * Reads in given images from the Core method getImages().
 * Approximates all squares from the images within the upper and lower bounds and crop that image.
 * The thread will attempt to find the smaller 9 boxes using the cropped image.
 * Once it has locate all 9 boxes, it will count the number of hints given already and try to determine what value 
 * the cell has by using Optical Character Recognition.
 * Populate a board with the values and have it be solved.
 * finally creating an overlay to be displayed back to the user to show the answer within the real-time video
 *  
 * */

public class BoardScanner implements Runnable{
	Tesseract instance;
	private Mat currentSnapshot;
	static ArrayList<Mat> SudokuCells = new ArrayList<Mat>();
	private static boolean threadBusy = false;
	private boolean threadWaiting = false;
	private Object getNextSnapshot = new Object();
	private String solvedBoard[];
	private boolean foundBoard = false;

	private final double PIXEL_THRESHOLD = .6;
	private final int ERODE_THRESHOLD = 7;
	private final int THICKNESS = 3;
	private final int BOARD_DIMENSION = 9;
	private final int BOARD_SIZE = 9*9;
	private final int BOX_DIMENSION = 3;
	private final int BOX_SIZE = 3*3;

	/*	Board = 9X9
	 * 	Box = 3X3
	 * 	Cell = 1X1
	 * */

	//Public method that reads in a source image and locates approximate ares where squares are found within the upper and lower bounds given.
	public HashMap<String, Mat> getImages(Mat sourceImg, double lowerBound, double upperBound, int erodeLevel, boolean runThread){ 
		HashMap<String, Mat> returnImg = new HashMap<String, Mat>();

		ArrayList<Rect> boxList = getBoxes(sourceImg, lowerBound, upperBound, erodeLevel);
		if(boxList.isEmpty())
			return null;
		if(boxList.size() == BOX_SIZE)
			boxList = positionalSort2D(boxList);


		Mat outline = sourceImg.clone();
		for(int i = 0; i < boxList.size(); i++){
			Rect squRect = boxList.get(i);

			if(foundBoard){
				if(runThread){
					Mat solvedScreen = sourceImg.clone();
					for(int j = 0; j < BOARD_DIMENSION; j++)
						Imgproc.putText(solvedScreen, solvedBoard[j], new Point(squRect.x,squRect.y+((squRect.height/9)*(j+1))), Core.FONT_HERSHEY_SIMPLEX, 1.2, new Scalar(0, 255, 10), 2, Core.LINE_AA, false);
					returnImg.put("screen", solvedScreen);
				}
			}
			Imgproc.rectangle(outline, new Point(squRect.x,squRect.y), new Point(squRect.x+squRect.width,squRect.y+squRect.height), MyImgproc.getColor(i), THICKNESS);

			Mat crop = new Mat(sourceImg.clone(), squRect);
			returnImg.put("box"+i, crop);

			Mat invertCrop = new Mat();
			Imgproc.threshold(getUsableImg(crop, ERODE_THRESHOLD), invertCrop, 240, 255, Imgproc.THRESH_BINARY_INV);
			returnImg.put("boxInvert"+i, invertCrop);
		}

		returnImg.put("cropOutline", outline);
		returnImg.put("machineView", (MyImgproc.toDilate((MyImgproc.toCanny(MyImgproc.toGreyscale(sourceImg.clone())).clone()),2)));
		/**************************/

		if(runThread && !threadBusy){
			synchronized (getNextSnapshot) {
				if(!threadBusy){
					threadBusy = true;
					currentSnapshot = returnImg.get("box0").clone();
					if(threadWaiting)
						getNextSnapshot.notify();
				}
			}
		}
		return returnImg;
	}


	//returns a list a boxes that meet the conditions of our lower and upper bounds.
	private ArrayList<Rect> getBoxes(Mat sourceImg, double lowerBound, double upperBound, int erodeLevel){
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat usableImage = getUsableImg(sourceImg, erodeLevel);
		Imgproc.findContours(usableImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		ArrayList<Rect> box = new ArrayList<Rect>();

		for (int i=0; i<contours.size(); i++){
			Rect rectangle = convertPointToRect(contours.get(i).toArray());
			int h = usableImage.height();
			int w = usableImage.width();
			if((rectangle.height > (h * lowerBound)) && (rectangle.width > (w * lowerBound))) 
				if((rectangle.height < (h * upperBound)) && (rectangle.width < (w * upperBound)))
					box.add(new Rect(rectangle.x+1, rectangle.y+1, rectangle.width, rectangle.height));
		}

		return box; 
	}

	//Convert points[] into Rect
	private Rect convertPointToRect(Point[] rectContours){
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		MatOfPoint2f contour2f = new MatOfPoint2f(rectContours);
		double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
		Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
		MatOfPoint points = new MatOfPoint( approxCurve.toArray() );
		return Imgproc.boundingRect(points);
	}


	/*	sort ArrayList based on their 2D position from Top-Bottom, Left-Right 
	 *	by checking if both mid-points are on the same horizontal plane using angles
	 *	else check the vertical plane.
	 */
	private ArrayList<Rect> positionalSort2D(ArrayList<Rect> list){
		Collections.sort(list, new Comparator<Rect>() {
			public int compare(Rect r1, Rect r2) {
				double angleDegree = Math.abs(Math.toDegrees(Math.atan2((r2.y-r1.y), (r2.x-r1.x))));
				if((angleDegree < 165) && (angleDegree > 30)){
					Integer yMidPoint1 = r1.y+(r1.height/2);
					Integer yMidPoint2 = r2.y+(r2.height/2);
					return Integer.compare(yMidPoint1, yMidPoint2);
				}else{
					Integer xMidPoint1 = r1.x+(r1.width/2);
					Integer xMidPoint2 = r2.x+(r2.width/2);
					return Integer.compare(xMidPoint1, xMidPoint2);
				}
			}
		});
		return list;
	}


	//Clean up Mat image for easier optical character recognition processing
	private Mat getUsableImg(Mat sourceImg, int erosion_size){

		Mat usableImg = new Mat(sourceImg.size(), IPL_DEPTH_8U);
		Imgproc.cvtColor(sourceImg.clone(), usableImg, Imgproc.COLOR_BGR2GRAY);
		Imgproc.Canny(usableImg, usableImg, 150, 255, 3, false);

		Point erodePoint = new Point(erosion_size, erosion_size);
		Size erodeSize = new Size(2 * erosion_size + 1, 2 * erosion_size + 1);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, erodeSize, erodePoint);
		Imgproc.dilate(usableImg, usableImg, element);

		return usableImg;
	}

	//Convert Mat to BufferedImage for tesseract optical character recognition use
	private static BufferedImage MatToBufferedImage(Mat frame){
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);

		return image;
	}

	//Checks if there is anything in the board's cell and try to OCR a value for that index, returns a populated board array
	private int[][] populateBoard(Mat image){

		HashMap<String, Mat> box = getImages(image.clone(), .2, .5, 7, false);
		if(box == null) return null;

		int hintsFound = 0;
		int[][] board = new int[9][9];

		for(int boxIndex = 0; boxIndex < 9; boxIndex++){
			if(!box.containsKey("boxInvert"+boxIndex)) return null;

			Mat currentBoxImg = box.get("box"+boxIndex);

			Mat cellInvert = box.get("boxInvert"+boxIndex);
			double subWidth = cellInvert.width()/3;
			double subHeight = cellInvert.height()/3;
			int pixelCountThreshold = (int) (subWidth*subHeight*PIXEL_THRESHOLD);
			int row = (boxIndex/3)*3;
			int col = (boxIndex%3)*3;

			for(int subCol = 0; subCol < 3; subCol++){
				for(int subRow = 0; subRow < 3; subRow++){
					Point start = new Point((subWidth*subCol), (subHeight*subRow));
					Point end =  new Point(((subWidth*subCol)+subWidth), ((subHeight*subRow)+subHeight));
					Rect subCellDimensions = new Rect(start, end);

					Mat subCell = new Mat(cellInvert.clone(), subCellDimensions);
					Core.extractChannel(subCell, subCell, 0);
					int whitePixelCount = Core.countNonZero(subCell);

					if(whitePixelCount < pixelCountThreshold){
						String subCellValue = ocrScanner(new Mat(currentBoxImg.clone(), subCellDimensions));
						if(subCellValue.length() == 1)
							board[row + subRow][col + subCol] = Integer.parseInt(subCellValue); 
						else return null;
						//If not values can be found for this hint, we return to get a new image to try and grab the hint again.

						hintsFound++;
					}
				}
			}

		}
		return (hintsFound < 17)? null: board;
		//Mathematicians prove that there is no puzzle less than 16 hints given
	}

	//Scan image for numbers in the given Mat image
	private String ocrScanner(Mat image){
		String ocrValues = "";
		try {
			ocrValues = instance.doOCR(MatToBufferedImage(image));
		} catch (TesseractException e) {
			System.err.println("Tesseract Exception");
		}
		String getOCRVal = "";
		char[] charArray = ocrValues.trim().toCharArray();
		for(char c: charArray){
			if(Character.isDigit(c)){
				getOCRVal += c;
			}else{
				String cStr = Character.toString(c);
				if(cStr.matches("[ilI]")){
					getOCRVal += "1";
				}else if(cStr.matches("[sz]")){
					getOCRVal += "5";
				}else if(cStr.matches("[d]")){
					getOCRVal += "6";
				}
			}
		}
		return getOCRVal;
	}


	//Thread will find all hint number given, 
	//populate board array, and solve the puzzle.
	@SuppressWarnings("deprecation")
	public void run() {
		this.instance = Tesseract.getInstance();
		Mat snapshotCopy = null;
		while(true){
			try {
				threadWaiting = true;
				synchronized (getNextSnapshot) {
					getNextSnapshot.wait();
					snapshotCopy = currentSnapshot;
					threadBusy = true;	
				}

				int[][] board = populateBoard(snapshotCopy);
				if(board == null){
					continue;
				}

				MyImgproc.printBoard(board);
				System.out.println("Starting Board Solver");
				if(!MySolver.isSolvable(board)){
					continue;
				}
				solvedBoard = new String[9];
				for(int i = 0; i < BOARD_DIMENSION; i++){
					solvedBoard[i] = "";
					for(int j = 0; j < BOARD_DIMENSION; j++){
						solvedBoard[i] += Integer.toString(board[i][j]) + " ";
					}
				}

				System.out.println("We have found a usable Board");
				System.out.println("Exiting Thread");
				foundBoard = true;

				break;

			} catch (InterruptedException e1) {
				System.err.println("THREAD ERROR");
			}finally{
				//image failed to be recognized, grab a new image from main thread
				synchronized (getNextSnapshot) {
					threadBusy = false;
				}
			}

		}
	}





}
