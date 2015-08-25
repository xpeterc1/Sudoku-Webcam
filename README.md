# Sudoku-Webcam

Libraries: [OpenCV 3.0](http://opencv.org/downloads.html) and [Java Tesseract OCR](https://github.com/tesseract-ocr/tesseract).

Personal project for learning tracking and augmented reality, by creating an application to solve Sudoku puzzles using a webcam or camera. 
An image is scanned to determine if a Sudoku puzzle board is present, is so locate its position.
Using optical character recognition, the application will decipher values from the image and solve the puzzle once all hints are found.
The solution is redisplayed back to the user as an overlay on top of the board image.
  

Image below shows the application running.  

<img src="http://i.imgur.com/IHukXHn.png" alt="Sudoku-Webcam" width="625" heignt="650"/>

<b>Top left</b>: Video feed from webcam, solution is overlayed on top once found   
<b>Top right</b>: App will locate the board's position, and crop image to red outline  
<b>Bottom left</b>: Locate the 9 boxes of the puzzle from cropped image  
<b>Bottom middle</b>: Reduced noise from shadows for optical character recognition  
<b>Bottom right</b>: Count black blobs for correct number of hints given by puzzle



Tested using a Logitech QuickCam Pro 9000 (webcam)  
Tested mainly on printed out sudoku puzzles from google images, 
and from http://www.websudoku.com/.  


