import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opencv.utils.Converters;

import EnumValues.Box;
import EnumValues.Invert_Box;
import EnumValues.Views;
import EnumValues.EnumValues;
/*Author: Peter Chow
 * 
 * Reads in given images using the Core method getImages().
 * Approximates all squares from the images within the upper and lower bounds and crop that image.
 * The thread will attempt to find the smaller 9 boxes using the cropped image.
 * Once it has locate all 9 boxes, it will count the number of hints given already and try to determine what value 
 * the cell has by using Optical Character Recognition.
 * Populate a board with the values and have it be solved.
 * finally creating an overlay to be displayed back to the user to show the answer within the real-time video
 * */



public class BoardScan implements Runnable{
	private Tesseract instance;
	private Mat currentSnapshot;

	private final int BOARD_DIMENSION = 9;
	private final int BOX_DIMENSION = 3;
	private final double PIXEL_THRESHOLD = .6;

	private Object getNextSnapshot = new Object();
	private boolean threadWaiting = false;
	private boolean foundBoard = false;
	private boolean threadBusy = false;
	private volatile boolean keepRunning = true;
	private String solvedBoard[];

	//Reads in a source image and locates approximate where squares are within the upper and lower bounds.
	public Map<EnumValues<Integer>, Mat> findBoardImg(Mat sourceImg, double lowBound, double upBound, int erodeLvl)
	{ 
		Map<EnumValues<Integer>, Mat> returnImg = new HashMap<EnumValues<Integer>, Mat>();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		//Mat usableImage = getUsableImg(sourceImg, erodeLvl);
		Mat usableImage = ImgUtil.getMachineView(sourceImg);
		Imgproc.findContours(usableImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		if(contours.size() == 0)
			return null;

		double maxArea = -1;
		MatOfPoint2f approxCurveCopy = new MatOfPoint2f();
		MatOfPoint contour = null;

		for (MatOfPoint matPoint: contours)
		{
			MatOfPoint currentMatPoint = matPoint;
			double contourArea = Imgproc.contourArea(currentMatPoint);
			if(contourArea > maxArea)
			{
				MatOfPoint2f rectangle =new MatOfPoint2f(currentMatPoint.toArray());
				long contourSize = currentMatPoint.total();
				MatOfPoint2f approxCurve = new MatOfPoint2f();
				Imgproc.approxPolyDP(rectangle, approxCurve, contourSize*0.05, true);
				if(approxCurve.total() == 4)
				{
					maxArea = contourArea;
					approxCurveCopy = approxCurve;
					contour = currentMatPoint;
				}
			}
		}

		List<Point> source = new ArrayList<Point>();
		for(int i = 0; i < 4; i++)
		{
			double[] toArray = approxCurveCopy.get(i,0);       
			Point point = new Point(toArray[0], toArray[1]);
			source.add(point);
		}

		//Sort points in a counter clockwise order based on the top-right point position 
		counterClockWiseSort(source);

		List<MatOfPoint> boardList = new ArrayList<MatOfPoint>();
		boardList.add(contour);

		if(boardList.isEmpty())
			return null;

		if(foundBoard)
		{
			//Solution Overlay
			Mat solvedScreen = sourceImg.clone();
			Point p = source.get(0);
			double height = source.get(2).y - p.y;
			for(int j = 0; j < BOARD_DIMENSION; j++)
			{
				Point point = new Point(p.x, p.y + ((height / 9) * (j + 1)));
				Scalar green = new Scalar(0, 255, 10);
				int font = Core.FONT_HERSHEY_SIMPLEX;
				Imgproc.putText(solvedScreen, solvedBoard[j], point, font, 1.2, green, 2, Core.LINE_AA, false);
			}
			returnImg.put(Views.SCREEN_VIEW, solvedScreen);
		}

		Mat outline = sourceImg.clone();

		Imgproc.drawContours(outline, boardList, -1, new Scalar(0, 255, 0), 3);

		for(int i = 0; i < source.size(); i++)
			Imgproc.circle(outline, source.get(i), 9, ImgUtil.getColor(i), 4);

		//TODO
		Point mid1 = getMidpoint(source.get(0), source.get(1));
		Point mid2 = getMidpoint(source.get(2), source.get(3));
		Point midPoint = getMidpoint(mid1, mid2);

		Imgproc.circle(outline, midPoint, 9, ImgUtil.getColor(8), 4);
		//TODO

		Mat crop = warp(sourceImg, Converters.vector_Point2f_to_Mat(source));
		returnImg.put(Views.CROP_VIEW, crop);

		returnImg.put(Views.BOARD_OUTLINE_VIEW, outline);

		synchronized (getNextSnapshot) 
		{
			if(!threadBusy)
			{
				threadBusy = true;
				currentSnapshot = crop;
				if(threadWaiting)
					getNextSnapshot.notify();
			}//If end
		}//synchronized end

		return returnImg;
	}

	//return the midpoint between two points
	private Point getMidpoint(Point a, Point b)
	{
		double x = (a.x + b.x)/2;
		double y = (a.y + b.y)/2;
		return new Point(x, y);
	}


	//Find the midPoint between all 4 points to our origin and determine which point is at the top left
	//we then rotate the list so this point is now first in the list.
	private void counterClockWiseSort(List<Point> source)
	{
		Point mid1 = getMidpoint(source.get(0), source.get(1));
		Point mid2 = getMidpoint(source.get(2), source.get(3));
		Point center = getMidpoint(mid1, mid2);

		int topLeftIndex = 0;
		for(int i = 0; i < 4; i++)
		{
			Point point = source.get(i);
			if((point.x < center.x) && (point.y < center.y))
			{
				topLeftIndex = i;
			}
		}

		List<Point> sourceCopy = new ArrayList<Point>();
		for(int i = 0; i < 4; i++)
		{
			Point point = source.get((i + topLeftIndex) % 4);
			sourceCopy.add(point);
		}
		source.clear();
		source.addAll(sourceCopy);
		//source = new ArrayList<Point>(sourceCopy);
		//return sourceCopy;

	}

	//Clean up Mat image for easier optical character recognition processing
	private Mat getUsableImg(Mat sourceImg, int erosion_size)
	{

		Mat usableImg = new Mat(sourceImg.size(), IPL_DEPTH_8U);
		Imgproc.cvtColor(sourceImg.clone(), usableImg, Imgproc.COLOR_BGR2GRAY);
		Imgproc.Canny(usableImg, usableImg, 150, 255, 3, false);

		Point erodePoint = new Point(erosion_size, erosion_size);
		Size erodeSize = new Size(2 * erosion_size + 1, 2 * erosion_size + 1);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, erodeSize, erodePoint);
		Imgproc.dilate(usableImg, usableImg, element);

		return usableImg;
	}


	//Checks if there is anything in the board's cell and try to OCR a value for that index, returns a populated board array
	private int[][] populateBoard(Mat image)
	{
		Map<EnumValues<Integer>, Mat> box = BoxScan.findBoxImg(image.clone(), .2, .5, 7);
		if(box == null){
			System.out.println("EMPTY BOX");
			return null;
		}

		int[][] board = new int[9][9];
		for(Integer boxIndex = 0; boxIndex < BOARD_DIMENSION; boxIndex++)
		{	
			//Reversed search for boxInvert to know we don't have enough boxes earlier
			if(!box.containsKey(Box.getEnum(8-boxIndex))) 
				return null; 

			Mat currentBoxImg = box.get(Box.getEnum(boxIndex));

			Mat cellInvert = box.get(Invert_Box.getEnum(boxIndex));
			double subWidth = cellInvert.width()/BOX_DIMENSION;
			double subHeight = cellInvert.height()/BOX_DIMENSION;
			int pixelCountThreshold = (int) (subWidth*subHeight*PIXEL_THRESHOLD);

			int row = (boxIndex/BOX_DIMENSION) * BOX_DIMENSION;
			int col = (boxIndex%BOX_DIMENSION) * BOX_DIMENSION;

			for(int subCol = 0; subCol < BOX_DIMENSION; subCol++)
			{
				for(int subRow = 0; subRow < BOX_DIMENSION; subRow++)
				{
					double startX = subWidth*subCol;
					double startY = subHeight*subRow;
					Point start = new Point(startX, startY);
					Point end =  new Point((startX + subWidth), (startY + subHeight));
					Rect subCellDimensions = new Rect(start, end);

					Mat subCell = new Mat(cellInvert.clone(), subCellDimensions);
					Core.extractChannel(subCell, subCell, 0);
					int whitePixelCount = Core.countNonZero(subCell);

					if(whitePixelCount < pixelCountThreshold)
					{
						String subCellValue = ocrScanner(new Mat(currentBoxImg.clone(), subCellDimensions));
						if(subCellValue.length() == 1)
							board[row + subRow][col + subCol] = Integer.parseInt(subCellValue); 
						else 
							return null;
						//If no values can be found for this hint, we return to get a new image to try and grab the hint again.
					}//end IF

				}//Inner For loop

			}//Mid For loop

		}//Outer For loop
		return board;

	}

	//Scan image for numbers in the given Mat image
	private String ocrScanner(Mat image)
	{
		String ocrValues = "";
		try 
		{
			ocrValues = instance.doOCR(ImgUtil.ToBufferedImage(image));
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

	//http://stackoverflow.com/questions/21084098/how-to-find-the-corners-of-a-rect-object-in-opencv
	public Mat warp(Mat inputMat, Mat startM) {
		int resultWidth = 300;
		int resultHeight = 300;

		Mat outputMat = new Mat(resultWidth, resultHeight, IPL_DEPTH_8U);

		List<Point> dest = new ArrayList<Point>();
		dest.add(new Point(0, 0));
		dest.add(new Point(0, resultHeight));
		dest.add(new Point(resultWidth, resultHeight));
		dest.add(new Point(resultWidth, 0));
		Mat endM = Converters.vector_Point2f_to_Mat(dest);      

		Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

		Imgproc.warpPerspective(inputMat.clone(), outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

		return outputMat;
	}

	public void stop(){
		this.keepRunning = false;
		synchronized (getNextSnapshot){ 
			if(threadWaiting)
				getNextSnapshot.notify();
		}
	}
	//Thread will find all hint number given, 
	//populate board array, and solve the puzzle.
	@SuppressWarnings("deprecation")
	public void run() 
	{
		this.instance = Tesseract.getInstance();
		Mat snapshotCopy = null;
		while(keepRunning){
			try {
				threadWaiting = true;
				synchronized (getNextSnapshot) 
				{
					getNextSnapshot.wait();
					snapshotCopy = currentSnapshot;
					threadBusy = true;	
				}

				if(!keepRunning)
					break;

				int[][] board = populateBoard(snapshotCopy);
				if(board == null)
				{
					//System.err.println("Not enough hints found, retrieving new board image");
					continue;
				}

				ocrScanner(snapshotCopy);
				System.out.println("All hints found");
				ImgUtil.printBoard(board);
				System.out.println("Starting Board Solver");
				if(!MySolver.getSolution(board))
					continue;

				ImgUtil.printBoard(board);				

				solvedBoard = new String[BOARD_DIMENSION];
				for(int i = 0; i < BOARD_DIMENSION; i++)
				{
					solvedBoard[i] = "";
					for(int j = 0; j < BOARD_DIMENSION; j++)
					{
						solvedBoard[i] += Integer.toString(board[i][j]) + " ";
					}
				}

				System.out.println("We have found a Solution");
				System.out.println("Exiting Thread");
				foundBoard = true;

				break;

			}
			catch (InterruptedException e1) 
			{
				System.err.println("THREAD ERROR");
			}
			finally
			{
				//image failed to be recognized, grab a new image from main thread
				synchronized (getNextSnapshot) 
				{
					threadBusy = false;
				}
			}

		}
		System.out.println("Thread stop");
	}
}
