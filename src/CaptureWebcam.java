import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class CaptureWebcam extends JPanel {

	static HashMap<String, Mat> boardMap;
	static HashMap<String, Mat> cellMap;
	static BufferedImage orginal;
	static BufferedImage cropImg;
	static BufferedImage cropLineImg;
	static BufferedImage machineViewImg;
	static BufferedImage devViewInvert;
	static BufferedImage devView;
	static BufferedImage TESTVIEW;
	static BufferedImage screenImg;



	static Mat frame;
	private VideoCapture camera;
	static Object accessImage;
	static boolean runWebcam;

	public void display(){

		if(!camera.isOpened()){
			System.err.println("Error: No camera found!");
		}
		else {  
			BoardScanner boardScan = new BoardScanner();
			JFrame frame0 = window(orginal, "Images", 0, 0);
			new Thread(boardScan).start();
			//imageBoard = boardScan.getImages(frame, .5, .95, 3, false);
			frame0.getContentPane().add(this);

			/*TESTING COUNT*/
			while(true){        
				if (camera.read(frame)){
					frame0.repaint();
					frame0.revalidate();
					try{
						boardMap = boardScan.getImages(frame, .5, .95, 3, true);
						screenImg = ToBufferedImage((boardMap.containsKey("screen"))? boardMap.get("screen"): frame);
						cropLineImg = ToBufferedImage(boardMap.get("cropOutline"));

						cellMap = boardScan.getImages(boardMap.get("box0"), .2, .5, 6, false);
						cropImg = ToBufferedImage(cellMap.get("cropOutline"));
						machineViewImg = ToBufferedImage(cellMap.get("machineView"));
					}catch(NullPointerException e){
						screenImg = ToBufferedImage(frame);
						continue;
					}finally{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}


				}
			}
		}
		camera.release();
	}

	@Override
	public void paint(Graphics g) {
		try{
			g.drawImage(screenImg, 0, 0, this);
			g.drawImage(cropImg , 0, screenImg.getHeight(), this);

			g.drawImage(cropLineImg ,cropLineImg.getWidth(), 0, this);
			//DEV_VIEW OBJECT
			g.drawImage(machineViewImg ,cropImg.getWidth(), screenImg.getHeight(), this);
			/*************************************************************************/

			//g.drawImage(devView ,cellOutline.getWidth()+crop.getWidth(), cameraFrame.getHeight(), this);
			int count = 0;
			BufferedImage image = null;
			int position = machineViewImg.getWidth()+cropImg.getWidth();//+devView.getWidth();
			for(int i = 0; i < 3; i++){
				for(int j = 0; j < 3; j++){
					image = ToBufferedImage(cellMap.get("boxInvert"+(count)));
					g.drawImage(image, position+(image.getWidth()*j), screenImg.getHeight()+(image.getHeight()*i), this);
					count++;
				}
			}



		}catch(NullPointerException e){

		}

		//g.drawImage(image2, image.getWidth(), 0, this);
		//g.drawImage(image3, image.getWidth(), image.getHeight(), this);

		//g.drawImage(image4, 0, image.getHeight(), this);



	}

	@SuppressWarnings("static-access")
	public CaptureWebcam() {
		this.camera = new VideoCapture(0);
		this.frame = new Mat();
		this.camera.read(frame);
		this.orginal = ToBufferedImage(frame);
		this.runWebcam = true;
	}

	public CaptureWebcam(BufferedImage img) {
		orginal = img;
	}   

	public BufferedImage getWebcamImage(){
		return orginal;
	}

	//Show image on window
	public JFrame window(BufferedImage img, String text, int x, int y) {
		JFrame frame0 = new JFrame();
		frame0.getContentPane().add(new CaptureWebcam(img));
		frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame0.setTitle(text);

		frame0.setSize(img.getWidth()*2, img.getHeight()*2 + 30);
		frame0.setLocation(x, y);
		frame0.setVisible(true);
		return frame0;
	}


	public static BufferedImage ToBufferedImage(Mat frame) throws NullPointerException {
		//Mat() to BufferedImage
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

	public void run() {
		while(runWebcam){
			try {
				this.orginal = ToBufferedImage(frame);
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
