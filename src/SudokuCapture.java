/*Author: Peter Chow
 * 
 * 
 * SudokuCapture will capture images from a webcam, 
 * using still image capture from the video, another thread will approximate the bounds of the board
 * and try to scan in the values of the board for the program to use.
 * After a successful scan, the program will solve the board and display the answer using an overlay over the camera view
 * 
 * */

public class SudokuCapture {
	
	public static void main (String args[]){
		init();
		CaptureWebcam webcam = new CaptureWebcam();
		webcam.display();
	}
	
	//Load up OpenCV lib by checking if the machine is 64-bit or 32-bit
	private static void init() {
		System.out.println("starting webcam capture");
		String answer = System.getProperty("sun.arch.data.model");
		if(answer.matches("64"))
		{
			System.loadLibrary("./build/x64/opencv_java300");
		}
		else if(answer.matches("32"))
		{
			System.loadLibrary("./build/x86/opencv_java300");
		}
		else
		{
			System.err.println("Your platform is not comptable");
			System.exit(0);
		}		
	}

}
