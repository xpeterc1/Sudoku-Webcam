import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import EnumValues.Box;
import EnumValues.Invert_Box;
import EnumValues.Views;
import EnumValues.EnumValues;

public class BoxScan {
	/*Author: Peter Chow
	 * 
	 * Read in a cropped image of the board to determine the 9 boxes of the puzzle
	 * sort based on their midpoints and relation to one another and return all 9 image for OCR use.
	 * */

	private final static int BOARD_DIMENSION = 9;
	private final static int ERODE_THRESHOLD = 7;
	private final static int THICKNESS = 3;


	public static Map<EnumValues<Integer>, Mat> findBoxImg(Mat sourceImg, double lowBound, double upBound, int erodeLvl)
	{ 
		Map<EnumValues<Integer>, Mat> returnImg = new HashMap<EnumValues<Integer>, Mat>();
		List<Rect> boxList = getBoxList(sourceImg, lowBound, upBound, erodeLvl);
		if(boxList.isEmpty())
			return null;
		if(boxList.size() == BOARD_DIMENSION)
			boxList = positionalSort2D(boxList);

		Mat outline = sourceImg.clone();
		for(int i = 0; i < boxList.size(); i++)
		{
			Rect squRect = boxList.get(i);
			Point startPoint = new Point(squRect.x, squRect.y);
			Point endPoint = new Point(squRect.x + squRect.width, squRect.y + squRect.height);
			Imgproc.rectangle(outline, startPoint, endPoint, ImgUtil.getColor(i), THICKNESS);

			Mat crop = new Mat(sourceImg.clone(), squRect);
			returnImg.put(Box.getEnum(i), crop);

			Mat invertCrop = ImgUtil.getInvert(crop, ERODE_THRESHOLD);
			returnImg.put(Invert_Box.getEnum(i), invertCrop);
		}

		returnImg.put(Views.BOX_OUTLINE_VIEW, outline);
		returnImg.put(Views.MACHINE_VIEW, ImgUtil.getMachineView(sourceImg));

		return returnImg;
	}

	//returns a list a boxes that meet the conditions of our lower and upper bounds from the image.
	private static List<Rect> getBoxList(Mat sourceImg, double lowerBound, double upperBound, int erodeLevel)
	{
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat usableImage = getUsableImg(sourceImg, erodeLevel);
		Imgproc.findContours(usableImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		List<Rect> box = new ArrayList<Rect>();

		for (int i=0; i<contours.size(); i++)
		{
			Rect rectangle = convertPointToRect(contours.get(i).toArray());
			int h = usableImage.height();
			int w = usableImage.width();
			if((rectangle.height > (h * lowerBound)) && (rectangle.width > (w * lowerBound))) 
				if((rectangle.height < (h * upperBound)) && (rectangle.width < (w * upperBound)))
					box.add(new Rect(rectangle.x+1, rectangle.y+1, rectangle.width, rectangle.height));
		}

		return box; 
	}

	//Clean up Mat image for easier optical character recognition processing
	private static Mat getUsableImg(Mat sourceImg, int erosion_size)
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

	//Convert points[] into Rect
	private static Rect convertPointToRect(Point[] rectContours)
	{
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
	private static List<Rect> positionalSort2D(List<Rect> list)
	{
		Collections.sort(list, new Comparator<Rect>() {
			public int compare(Rect r1, Rect r2)
			{
				double atan = Math.atan2((r2.y-r1.y), (r2.x-r1.x));
				double angleDegree = Math.abs(Math.toDegrees(atan));

				if((angleDegree < 165) && (angleDegree > 30))
				{
					Integer yMidPoint1 = r1.y+(r1.height/2);
					Integer yMidPoint2 = r2.y+(r2.height/2);
					return Integer.compare(yMidPoint1, yMidPoint2);
				}
				else
				{
					Integer xMidPoint1 = r1.x+(r1.width/2);
					Integer xMidPoint2 = r2.x+(r2.width/2);
					return Integer.compare(xMidPoint1, xMidPoint2);
				}
			}
		});
		return list;
	}
}
