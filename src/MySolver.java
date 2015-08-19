import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/*Author: Peter Chow
 * 
 * Code is based on the backtracking code provided by Andrew Davison
 * Andrew's site : http://fivedots.coe.psu.ac.th/~ad/jg
 * 
 * Code creates a copy from the passed in int array and 
 * attempts to find all possible solutions for a given cell using isPossibleToSolve().
 * Code then attempt backtracking in the solve() method
 * using only possible solutions per cell found earlier,
 * reducing the number of islegal() checks overall.
 * */

public class MySolver {


	private static final int BOARD_DIMENSION = 9;
	private static final int BOX_DIMENSION = 3;
	private static final ArrayList<Integer> VALUE_LIST = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

	//Core public method of this class
	public static boolean getSolution(int[][] board)
	{	
		long startTime = System.currentTimeMillis();
		
		CellObject[][] myBoard = new CellObject[BOARD_DIMENSION][BOARD_DIMENSION];
		for (int i=0; i < BOARD_DIMENSION; i++)
			for(int j=0; j < BOARD_DIMENSION; j++)
				myBoard[i][j] = new CellObject(board[i][j]);
		
		
		if(!isPossibleToSolve(myBoard))
		{	//checks if each cell has any possible values and duplicates
			System.out.println("INVALID BOARD: Duplicate found");
			return false;
		}

		if(!solve(0, 0, myBoard))
		{	//Try brute force using backtracking
			System.out.println("INVALID BOARD: No Possible Solution ");
			return false;
		}

		System.out.println("Found a solution in " + (System.currentTimeMillis() - startTime) + "ms");

		for (int i=0; i < BOARD_DIMENSION; i++)    // copy result back into original array
			for(int j=0; j < BOARD_DIMENSION; j++)
				board[i][j] = myBoard[i][j].getValue();

		return true;
	}

	private static boolean isPossibleToSolve(CellObject[][] myBoard)
	{	//Creates a reference of all Rows, Columns, and Boxes which we do an intersection based on the cell's index
		List<Set<Integer>> rowRef = new ArrayList<Set<Integer>>(9);
		if(!getRowReference(myBoard, rowRef))
			return false;

		List<Set<Integer>> colRef = new ArrayList<Set<Integer>>(9);
		if(!getColReference(myBoard, colRef))
			return false;

		List<Set<Integer>> boxRef = new ArrayList<Set<Integer>>(9);
		if(!getBoxReference(myBoard, boxRef))
			return false;

		//for each cell index, we do the intersection with the references and save that to the cellObject's possible value list.
		for (int i=0; i < BOARD_DIMENSION; i++)
			for(int j=0; j < BOARD_DIMENSION; j++)
				if(myBoard[i][j].isEmpty())
					if(!myBoard[i][j].setValList(intersection(rowRef.get(i), colRef.get(j), boxRef.get(((i/3)*3)+(j/3)))))
						return false; //If no possible values are found for no value cell, board is invalid					
		return true;
	}

	private static boolean solve(int row, int col, CellObject[][] board)
	{	//Based on Andrew Davison sudoku backTracking code
		if (row == BOARD_DIMENSION) {
			row = 0;  col++;    // move to top of next column
			if (col == BOARD_DIMENSION)
				return true;
		}
		if (!board[row][col].isEmpty())   // skip cells that is filled
			return solve(row+1, col, board);

		List<Integer> copyValues = board[row][col].getPossibleValues();
		for (Integer value: copyValues) {     // try all data from the intersected reference list
			if(isLegal(board, row, col, value))
			{
				board[row][col].setValue(value);
				if (solve(row+1, col, board))
					return true;
			}
		}
		board[row][col].resetValue(); // reset value when backtracking
		return false;
	} 

	private static boolean isLegal(CellObject[][] board, int rowIndex, int colIndex, Integer val)
	{	//Legal checks if the given value is possible to be used at the given index within the board
		//check row for duplicate elements
		for (int col = 0; col < BOARD_DIMENSION; col++)  
			if (val == board[rowIndex][col].getValue())
				return false;

		//check columns for duplicate elements
		for (int row = 0; row < BOARD_DIMENSION; row++) 
			if (val == board[row][colIndex].getValue())
				return false;
		
		//Check box for duplicate elements
		int boxRowOffset = (rowIndex/3)*3;
		int boxColOffset = (colIndex/3)*3;
		for (int x = boxColOffset; x < boxColOffset+3; x++)  
			for (int y = boxRowOffset; y < boxRowOffset+3; y++) 
				if (val == board[y][x].getValue())
					return false;

		return true; // no violations, so it's allowed
	}

	private static boolean getRowReference(CellObject[][] board, List<Set<Integer>> rowRef)
	{	//get all values for each row on the board, list is row index and set is value of each row
		for(int row = 0; row < BOARD_DIMENSION; row++)
		{
			Set<Integer> currentRowRef = new HashSet<Integer>(VALUE_LIST); 
			for(int col = 0; col < BOARD_DIMENSION; col++)
			{
				int value = board[row][col].getValue();
				if(value != 0 && !currentRowRef.remove(Integer.valueOf(value)))
					return false;
			}
			rowRef.add(currentRowRef);
		}
		return true;
	}

	private static boolean getColReference(CellObject[][] board, List<Set<Integer>> colRef)
	{	//get all values for each column on the board, list is col index and set is value of each column
		for(int col = 0; col < BOARD_DIMENSION; col++)
		{
			Set<Integer> currentColRef = new HashSet<Integer>(VALUE_LIST); 
			for(int row = 0; row < BOARD_DIMENSION; row++)
			{
				int value = board[row][col].getValue();
				if(value != 0 && !currentColRef.remove(Integer.valueOf(value)))
					return false;
			}
			colRef.add(currentColRef);
		}
		return true;
	}

	private static boolean getBoxReference(CellObject[][] board, List<Set<Integer>> boxRef)
	{	//get all value for each box on the board, list is index of each box and set is value of each box
		for(int row = 0; row < BOARD_DIMENSION; row+=3)
			for(int col = 0; col < BOARD_DIMENSION; col+=3)
			{
				Set<Integer> currentBoxRef = new HashSet<Integer>(VALUE_LIST); 
				for(int i = 0; i < BOX_DIMENSION; i++)
					for(int j = 0; j < BOX_DIMENSION; j++)
					{
						int value = board[i + row][j + col].getValue();
						if(value != 0 && !currentBoxRef.remove(Integer.valueOf(value)))
							return false;
					}

				boxRef.add(currentBoxRef);
			}
		return true;
	}

	private static List<Integer> intersection(Set<Integer> rowRef, Set<Integer> colRef, Set<Integer> boxRef){
		List<Integer> iter = new ArrayList<Integer>(boxRef);
		List<Integer> result = new ArrayList<Integer>();

		for (int n: iter) 
			if(colRef.contains(n) && rowRef.contains(n)) 
				result.add(n);

		if(result.size() == 1)
		{
			Integer value = result.get(0);
			rowRef.remove(value);
			colRef.remove(value);
			boxRef.remove(value);
		}

		return result;
	}



	//Test cases provided for use as a simple Sudoku solver 
	@SuppressWarnings("unused")
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


		MySolver.getSolution(HARD);
		ImgHelper.printBoard(HARD);

	}
}
