
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;

public class CaptureWebcam extends JPanel {
	private static final long serialVersionUID = 4152259007829979107L;

	static HashMap<String, Mat> boardMap;
	static HashMap<String, Mat> cellMap;
	static BufferedImage orginal;
	static BufferedImage screenImg;

	static Mat frame;
	private VideoCapture camera;
	static boolean runWebcam;

	public void display(){

		if(!camera.isOpened()){
			System.err.println("Error: No camera found!");
		}
		else {  
			BoardScanner boardScan = new BoardScanner();
			JFrame frame0 = window(orginal, "Images", 0, 0);
			new Thread(boardScan).start();
			frame0.getContentPane().add(this);

			while(true){        
				if (camera.read(frame)){
					frame0.repaint();
					frame0.revalidate();
					try{
						boardMap = boardScan.getImages(frame, .5, .95, 3, true);
						screenImg = ToBufferedImage((boardMap.containsKey("screen"))? boardMap.get("screen"): frame);
					}catch(NullPointerException e){
						continue;
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
					}
				}
			}
		}
		camera.release();
	}

	//Display images to the user by redrawing new images to the created window
	@Override
	public void paint(Graphics g) {
		try{
			g.drawImage(screenImg, 0, 0, this);

			BufferedImage cropImg = ToBufferedImage(cellMap.get("cropOutline"));
			g.drawImage(cropImg , 0, screenImg.getHeight(), this);

			BufferedImage cropLineImg = ToBufferedImage(boardMap.get("cropOutline"));
			g.drawImage(cropLineImg ,cropLineImg.getWidth(), 0, this);

			BufferedImage machineViewImg = ToBufferedImage(cellMap.get("machineView"));
			g.drawImage(machineViewImg ,cropImg.getWidth(), screenImg.getHeight(), this);
			/*************************************************************************/

			int count = 0;
			BufferedImage image = null;
			int position = machineViewImg.getWidth()+cropImg.getWidth();
			for(int i = 0; i < 3; i++){
				for(int j = 0; j < 3; j++){
					image = ToBufferedImage(cellMap.get("boxInvert"+(count)));
					g.drawImage(image, position+(image.getWidth()*j), screenImg.getHeight()+(image.getHeight()*i), this);
					count++;
				}
			}
		}catch(NullPointerException e){
			screenImg = ToBufferedImage(frame);
			g.drawImage(screenImg, 0, 0, this);
		}
	}

	//Constructor to set orginal images
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


	//Create window and display content to screen
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


	//Mat to BufferedImage
	public static BufferedImage ToBufferedImage(Mat frame) throws NullPointerException {
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

}
