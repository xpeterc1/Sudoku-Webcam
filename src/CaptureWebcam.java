
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
/*Author: Peter Chow
 * 
 * This is a personal project for learning how to scan and search through an image.
 * Using this information combined with an interest with the puzzle game Sudoku, 
 * this code was created to try and understand how to solve such a Sudoku puzzle using 
 * image manipulation in OpenCV and OCR for reading in characters from an image.
 * 
 * This class is for displaying and getting all the image to the user
 * 
 * This code will show an application views of different process used to try and solve the puzzle
 * */
public class CaptureWebcam extends JPanel {
	private static final long serialVersionUID = 4152259007829979107L;

	private static HashMap<String, Mat> boardMap;
	private static HashMap<String, Mat> cellMap;
	
	private static BufferedImage screenImg;

	private Mat frame;
	private VideoCapture camera;
	private BufferedImage orginal;
	
	public void display()
	{	//Gets the webcam's images and pass it to the boardScan to get other views, finally shows the soltuion as an overlay of the camera feed.
		if(!camera.isOpened())
		{
			System.err.println("Error: No camera found!");
		}
		else 
		{  
			System.out.println("Creating image display");
			BoardScanner boardScan = new BoardScanner();
			JFrame frame0 = window(orginal, "Images", 0, 0);
			new Thread(boardScan).start();
			frame0.getContentPane().add(this);

			while(true)
			{        
				if (camera.read(frame))
				{
					frame0.repaint();
					frame0.revalidate();
					try{
						boardMap = boardScan.getImages(frame, .5, .95, 3, true);
						screenImg = ImgHelper.ToBufferedImage((boardMap.containsKey("screen"))? boardMap.get("screen"): frame);
					}catch(NullPointerException e){
						screenImg = ImgHelper.ToBufferedImage(frame);
					}

					try{
						cellMap = boardScan.getImages(boardMap.get("box0"), .2, .5, 6, false);
					}catch(NullPointerException e){
						continue;
					}finally{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}//Finally end
				}//If end
			}//While end	
		}//else end
		camera.release();
	}

	
	@Override
	public void paint(Graphics g) 
	{	//Display images to the user by redrawing new images to the created window
		try{
			//Top Left (Webcam feed and solution overlay)
			g.drawImage(screenImg, 0, 0, this);
		}catch(NullPointerException e){
			g.drawImage(ImgHelper.ToBufferedImage(frame), 0, 0, this);
		}
		
		try{
			//Top Right (Locate Board)
			BufferedImage cropLineImg = ImgHelper.ToBufferedImage(boardMap.get("cropOutline"));
			g.drawImage(cropLineImg ,cropLineImg.getWidth(), 0, this);
			
			//Bottom Left (Locate the 3x3 boxes)
			BufferedImage cropImg = ImgHelper.ToBufferedImage(cellMap.get("cropOutline"));
			g.drawImage(cropImg , 0, screenImg.getHeight(), this);

			//Bottom Middle (Clean image for OCR)
			BufferedImage machineViewImg = ImgHelper.ToBufferedImage(cellMap.get("machineView"));
			g.drawImage(machineViewImg ,cropImg.getWidth(), screenImg.getHeight(), this);
			/*************************************************************************/

			//Blob counter images, Bottom Right
			BufferedImage image = null;
			int position = machineViewImg.getWidth()+cropImg.getWidth();
			for(int i = 0; i < 3; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					image = ImgHelper.ToBufferedImage(cellMap.get("boxInvert"+((i*3)+j)));
					g.drawImage(image, position+(image.getWidth()*j), screenImg.getHeight()+(image.getHeight()*i), this);
				}
			}
		}catch(NullPointerException e){
			
		}
	}

	public CaptureWebcam() 
	{	//Constructor
		this.camera = new VideoCapture(0);
		this.frame = new Mat();
		this.camera.read(frame);
		this.orginal = ImgHelper.ToBufferedImage(frame);
	}

	public CaptureWebcam(BufferedImage img) 
	{
		orginal = img;
	}   

	public JFrame window(BufferedImage img, String text, int x, int y) 
	{	//Create window and display content to screen
		JFrame frame0 = new JFrame();
		frame0.getContentPane().add(new CaptureWebcam(img));
		frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame0.setTitle(text);

		frame0.setSize(img.getWidth()*2, img.getHeight()*2 + 30);
		frame0.setLocation(x, y);
		frame0.setVisible(true);
		return frame0;
	}


}
