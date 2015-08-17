import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
//Custom Sudoku Solver
/*Author: Peter Chow
 * 
 * Code is based on the backtracking code by Andrew Davison
 * Andrew's site : http://fivedots.coe.psu.ac.th/~ad/jg
 * 
 * Code creates a copy from the int array and 
 * attempts to find all possible solutions for a given cell using LogicSolve() method first.
 * Code attempt backtracking in the backtrackSolve() method
 * using only possible solutions per cell found earlier in LogicSolve(),
 * reducing the number of isLegal checks required.
 *  
 * 
 * 
 * */

public class MySolver {


	private static final int BOARD_DIMENSIONS = 9;
	private static final int BOX_SIZE = 3;
	private static final boolean TESTING = false;
	private static final ArrayList<Integer> VALUE_LIST = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));
	private static int count;



	//Core method of this class
	public static boolean isSolvable(int[][] board){		
		CellObject[][] myBoard = copyFromArray(board);

		System.out.println("UNSOLVED BOARD");
		MyImgproc.printTable(myBoard);

		long startTime = System.currentTimeMillis();

		//find possible values per each cell.
		if(!logicSolve(myBoard)){
			System.out.println("INVALID TABLE: Duplicate found");
			return false;
		}
		System.out.println("Table valid in: " + (System.currentTimeMillis() - startTime) + "ms");
		//Try brute force using backtracking
		if(!backtrackSolve(0, 0, myBoard)){
			System.out.println("INVALID TABLE: No Possible Solution ");
			return false;
		}

		System.out.println();
		MyImgproc.printTable(myBoard);
		//System.out.println("Found a solution in " + (System.currentTimeMillis() - startTime) + "ms, count: " + count);

		for (int i=0; i < BOARD_DIMENSIONS; i++)    // copy result back into original array
			for(int j=0; j < BOARD_DIMENSIONS; j++){
				board[i][j] = myBoard[i][j].getValue();
			}
		return true;
	}

	//Copy int array and save to a copy using CellObjects
	private static CellObject[][] copyFromArray(int[][] board){
		CellObject[][] myBoard = new CellObject[BOARD_DIMENSIONS][BOARD_DIMENSIONS];
		for (int i=0; i < BOARD_DIMENSIONS; i++)
			for(int j=0; j < BOARD_DIMENSIONS; j++)
				myBoard[i][j] = new CellObject(board[i][j]);
		return myBoard;
	}

	//Creates a reference of all Rows, Columns, and Boxes which we do an intersection based on the cell's location for that cell's possible value list. 
	private static boolean logicSolve(CellObject[][] myBoard){
		ArrayList<ArrayList<Integer>> rowRef = new ArrayList<ArrayList<Integer>>(BOARD_DIMENSIONS);
		ArrayList<ArrayList<Integer>> colRef = new ArrayList<ArrayList<Integer>>(BOARD_DIMENSIONS);
		ArrayList<ArrayList<ArrayList<Integer>>> boxRef = new ArrayList<ArrayList<ArrayList<Integer>>>(BOX_SIZE);

		//gets all reference list for all ROW, COL, and BOX
		if(!getAllReferences(myBoard, rowRef, colRef, boxRef))
			return false;

		//for each cell index, we do the intersection with the references and save that to the cellObject's possible value list.
		for (int i=0; i < BOARD_DIMENSIONS; i++)
			for(int j=0; j < BOARD_DIMENSIONS; j++)
				if(!myBoard[i][j].hasValue())
					if(!myBoard[i][j].setValList(getValList(rowRef.get(i), colRef.get(j), boxRef.get(i/3).get(j/3), j, i)))
						return false; //If no possible values are found for no value cell, board is invalid					
		return true;
	}

	//Code based on Andrew Davison and Bob Carpenter code
	//
	private static boolean backtrackSolve(int row, int col, CellObject[][] board)
	{
		if (row == BOARD_DIMENSIONS) {
			row = 0;  col++;    // move to top of next column
			if (col == BOARD_DIMENSIONS)
				return true;
		}
		if (board[row][col].getValue() != 0)   // skip table element that is filled
			return backtrackSolve(row+1, col, board);

		ArrayList<Integer> copyValues = board[row][col].getPossiblIntegers();
		for (int val = 0; val < copyValues.size(); val++) {     // try all data from the intersected reference list
			Integer value = copyValues.get(val);
			if(isLegal(board, row, col, value)){
				board[row][col].setValue(value);
				if (backtrackSolve(row+1, col, board))
					return true;
			}
		}
		board[row][col].resetValue(); // reset value when backtracking
		return false;
	} 

	//finds all the values for each Rows, Columns, and Boxes for the board and save to their respected ArrayList
	//We also check for duplicates within here 
	private static boolean getAllReferences(CellObject[][] board, ArrayList<ArrayList<Integer>> rowRef, ArrayList<ArrayList<Integer>> colRef, ArrayList<ArrayList<ArrayList<Integer>>> boxRef){
		for(int i = 0; i < BOARD_DIMENSIONS; i++){
			ArrayList<Integer> list = rowCheck(i, board);
			if(list == null)
				return false;	//Duplicate found within the same row
			rowRef.add(list);
		}	
		for(int i = 0; i < BOARD_DIMENSIONS; i++){
			ArrayList<Integer> list = colCheck(i, board);
			if(list == null)
				return false; //Duplicate found within the same column
			colRef.add(list);
		}	
		for(int i = 0; i < BOARD_DIMENSIONS; i+=BOX_SIZE){
			ArrayList<ArrayList<Integer>> rowList = new ArrayList<ArrayList<Integer>>(); 
			for(int j = 0; j < BOARD_DIMENSIONS; j+=BOX_SIZE){
				ArrayList<Integer> list = boxCheck(j,i, board);
				if(list == null)
					return false;	//Duplicate found within the same box
				rowList.add(list);
			}
			boxRef.add(rowList);
		}
		return true;
	}


	//Does the intersection with all 3 reference list based on the cell's index and check if the list is of size 1,
	private static ArrayList<Integer> getValList(ArrayList<Integer> rowRef, ArrayList<Integer> colRef, ArrayList<Integer> boxRef, int col, int row){
		ArrayList<Integer> list = new ArrayList<Integer>(rowRef);
		list.retainAll(colRef);
		list.retainAll(boxRef);
		
		//if list is size one, there is only one possible value for that cell, and we remove the value from the reference list 
		if(list.size() == 1){
			rowRef.remove(list.get(0));
			colRef.remove(list.get(0));
			boxRef.remove(list.get(0));
		}
		return list;
	}

	//Legal checks if the given value is possible to be used at the given index of i (row), j (column) within the board 
	private static boolean isLegal(CellObject[][] board, int i, int j, Integer val)
	{
		count++;
		for (int row = 0; row < i; row++)  // check elements in jth column
			if (val == board[row][j].getValue())
				return false;

		for (int col = 0; col < j; col++)  // check elements in ith row
			if (val == board[i][col].getValue())
				return false;

		// check box containing (i,j) element
		int boxRowOffset = (i/3)*3;
		int boxColOffset = (j/3)*3;
		for (int x = 0; x < BOX_SIZE; x++)    // check elements in the box
			for (int y = 0; y < BOX_SIZE; y++) 
				if (val == board[boxRowOffset + y][boxColOffset + x].getValue())
					return false;

		return true; // no violations, so it's allowed
	}


	//find all the values of a given row
	private static ArrayList<Integer> rowCheck(int colIndex, CellObject[][] board){
		//check row of given colIndex;
		ArrayList<Integer> rowValues = new ArrayList<Integer>(VALUE_LIST);
		for (int row = 0; row < BOARD_DIMENSIONS; row++){
			Integer value = board[colIndex][row].getValue();
			if(value != 0)
				if(!rowValues.remove(value))
					return null;
	

		}
		return rowValues;
	}
	
	//finds all the values of a given column
	private static ArrayList<Integer> colCheck(int rowIndex, CellObject[][] board){
		ArrayList<Integer> colValues = new ArrayList<Integer>(VALUE_LIST);
		for (int col = 0; col < BOARD_DIMENSIONS; col++){
			Integer value = board[col][rowIndex].getValue();
			if(value != 0)
				if(!colValues.remove(value))
					return null;
		}
		return colValues;
	}
	
	//find all values in a 3x3 box
	private static ArrayList<Integer> boxCheck(int rowIndex, int colIndex, CellObject[][] board){
		ArrayList<Integer> boxValues = new ArrayList<Integer>(VALUE_LIST);
		int boxRow = (rowIndex/3)*3;
		int boxCol = (colIndex/3)*3;
		for(int i = 0; i < BOX_SIZE; i++)
			for(int j = 0; j < BOX_SIZE; j++){
				Integer value = board[i + boxCol][j + boxRow].getValue();
				if(value != 0)
					if(!boxValues.remove(value))
						return null;
			}
		return boxValues;
	}


	//Test cases provided for use as a simple sudoku solver 
	public static void main(String[] args){
		int[][] intBoard = new int[][]{	
				{0,8,0,4,0,2,0,6,0},
				{0,3,4,0,0,0,9,1,0},
				{9,6,0,0,0,0,0,8,4},
				{0,0,0,2,1,6,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,3,5,7,0,0,0},
				{8,4,0,0,0,0,0,7,5},
				{0,2,6,0,0,0,1,3,0},
				{0,9,0,7,0,1,0,4,0}};

		int[][] intBoardEASY = new int[][]{	
				{5,6,0,0,2,9,0,1,3},
				{0,8,9,0,0,1,0,0,2},
				{4,0,0,0,0,0,9,0,0},
				{6,0,2,0,5,0,4,7,0},
				{9,0,0,0,0,0,0,0,6},
				{0,7,5,0,4,0,3,0,1},
				{0,0,8,0,0,0,0,0,7},
				{1,0,0,8,0,0,2,3,0},
				{7,3,0,1,9,0,0,5,8}};

		int[][] INVALID = new int[][]{	
				{5,6,0,0,2,9,0,1,3},
				{0,8,9,0,0,1,0,0,2},
				{4,0,0,0,0,0,9,0,0},
				{6,0,2,0,5,0,4,7,0},
				{9,0,0,0,0,0,0,0,6},
				{0,7,5,0,4,0,3,0,1},
				{0,0,8,0,0,0,0,0,7},
				{1,0,0,8,1,0,2,3,0},
				{7,3,0,1,9,0,0,5,8}};


		int[][] intBoardMED = new int[][]{	
				{6,0,1,3,0,7,0,0,0},
				{2,0,7,0,4,0,0,3,9},
				{0,3,0,6,0,0,0,0,0},
				{7,0,0,0,8,0,0,0,0},
				{5,2,6,0,0,0,7,1,8},
				{0,0,0,0,7,0,0,0,3},
				{0,0,0,0,0,4,0,9,0},
				{1,7,0,0,6,0,3,0,4},
				{0,0,0,2,0,5,8,0,6}};
		int[][] blank = new int[][]{	
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0}};
		int[][] HARD = new int[][]{	
				{5,1,0,0,0,0,0,8,0},
				{0,7,0,1,0,0,0,0,0},
				{0,0,0,0,9,4,0,6,0},
				{0,0,2,0,0,3,0,0,8},
				{0,0,1,0,7,0,9,0,0},
				{7,0,0,8,0,0,6,0,0},
				{0,2,0,6,4,0,0,0,0},
				{0,0,0,0,0,1,0,3,0},
				{0,4,0,0,0,0,0,2,1}};


		MySolver solve = new MySolver();
		//solve.isSolvable(intBoardMED);
		solve.isSolvable(HARD);

	}
}
