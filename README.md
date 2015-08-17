# Sudoku-Webcam

Libraries: [OpenCV 3.0](http://opencv.org/downloads.html) and [Java Tesseract OCR](https://github.com/tesseract-ocr/tesseract).

Personal project for solving Sudoku puzzles using a webcam feed for image capture
and redisplaying the solution back to the user as an overlay on the video display. 

<img src="http://i.imgur.com/IHukXHn.png" alt="Sudoku-Webcam" width="625" heignt="650"/>

<b>Top left</b>: Video feed from webcam, solution is overlayed ontop once found  
<b>Top right</b>: Crop to red outline  
<b>Bottom left</b>: Locate the 9 boxes of the puzzle from cropped image  
<b>Bottom middle</b>: Reduced noise from shaodows for OCR  
<b>Bottom right</b>: Count black blobs for correct amount of hints found in image.  


Tested using a Logitech QuickCam Pro 9000 (webcam)  
Tested mainly on printed out sudoku puzzles from google images, 
and from http://www.websudoku.com/.


