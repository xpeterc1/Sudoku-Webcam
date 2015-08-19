# Sudoku-Webcam

Libraries: [OpenCV 3.0](http://opencv.org/downloads.html) and [Java Tesseract OCR](https://github.com/tesseract-ocr/tesseract).

Personal project for solving Sudoku puzzles using a webcam or camera. 
An image is scanned for a puzzle board, to determine its position in the image.
Image is scanned for values using optical character recognition, and processed to find amount of clues given by the puzzle.
Solve puzzle and redisplay the solution back to the user as an overlay on the video display.  

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


