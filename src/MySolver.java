import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//Custom Sudoku Solver
/*Author: Peter Chow
 * 
 * Code is based on the backtracking code by Andrew Davison and Bob Carpenter
 * 
 * Code creates a copy from the passed in int array and 
 * attempts to find all possible solutions for a given cell using LogicSolve().
 * Code attempt backtracking in the backtrackSolve() method
 * using only possible solutions per cell found earlier,
 * reducing the number of islegal() checks required overall.
 * 
 * 
 * 
 * */

public class MySolver {


	private static final int BOARD_DIMENSIONS = 9;
	private static final int BOX_DIMENSIONS = 3;
	private static final ArrayList<Integer> VALUE_LIST = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

	//Core method of this class
	public static boolean isSolvable(int[][] board){	
		//long startTime = System.nanoTime();
		long startTime = System.currentTimeMillis();

		int[][] boardCopy = new int[9][9];
		CellObject[][] myBoard = copyFromArray(board, boardCopy);
		//printTable(myBoard);
		//find possible values per each cell.
		if(!logicSolve(myBoard, boardCopy)){
			System.out.println("INVALID TABLE: Duplicate found");
			return false;
		}
		//Try brute force using backtracking
		if(!backtrackSolve(0, 0, myBoard)){
			System.out.println("INVALID TABLE: No Possible Solution ");
			return false;
		}

		printTable(myBoard);
		System.out.println("Found a solution for MYSOLVER in " + (System.currentTimeMillis() - startTime) + "ms, count: ");

		for (int i=0; i < BOARD_DIMENSIONS; i++)    // copy result back into original array
			for(int j=0; j < BOARD_DIMENSIONS; j++){
				board[i][j] = myBoard[i][j].getValue();
			}
		return true;
	}

	//Copy int array and save to a copy using CellObjects
	private static CellObject[][] copyFromArray(int[][] board, int[][] boardCopy){
		CellObject[][] myBoard = new CellObject[BOARD_DIMENSIONS][BOARD_DIMENSIONS];
		for (int i=0; i < BOARD_DIMENSIONS; i++){
			for(int j=0; j < BOARD_DIMENSIONS; j++){
				myBoard[i][j] = new CellObject(board[i][j]);
				boardCopy[i][j] = board[i][j];
			}
		}
		return myBoard;
	}

	//Creates a reference of all Rows, Columns, and Boxes which we do an intersection based on the cell's location for that cell's possible value list. 
	private static boolean logicSolve(CellObject[][] myBoard, int[][] boardCopy){
		ArrayList<HashSet<Integer>> rowRef = new ArrayList<HashSet<Integer>>(9);
		if(!getRowReference(boardCopy, rowRef))
			return false;
		ArrayList<HashSet<Integer>> colRef = new ArrayList<HashSet<Integer>>(9);
		if(!getColReference(boardCopy, colRef))
			return false;

		ArrayList<HashSet<Integer>> boxRef = new ArrayList<HashSet<Integer>>(9);
		if(!getBoxReference(boardCopy, boxRef))
			return false;

		//for each cell index, we do the intersection with the references and save that to the cellObject's possible value list.
		for (int i=0; i < BOARD_DIMENSIONS; i++)
			for(int j=0; j < BOARD_DIMENSIONS; j++)
				if(myBoard[i][j].isEmpty())
					if(!myBoard[i][j].setValList(intersection(rowRef.get(i), colRef.get(j), boxRef.get(((i/3)*3)+(j/3)))))
						return false; //If no possible values are found for no value cell, board is invalid					
		return true;
	}

	//Code based on Andrew Davison and Bob Carpenter code
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
		for (Integer value: copyValues) {     // try all data from the intersected reference list
			if(isLegal(board, row, col, value)){
				board[row][col].setValue(value);
				if (backtrackSolve(row+1, col, board))
					return true;
			}
		}
		board[row][col].resetValue(); // reset value when backtracking
		return false;
	} 
	
	//Legal checks if the given value is possible to be used at the given index of i (row), j (column) within the board 
	private static boolean isLegal(CellObject[][] board, int i, int j, Integer val)
	{
		for (int row = 0; row < i; row++)  // check elements in jth column
			if (val == board[row][j].getValue())
				return false;

		for (int col = 0; col < j; col++)  // check elements in ith row
			if (val == board[i][col].getValue())
				return false;

		// check box containing (i,j) element
		int boxRowOffset = (i/3)*3;
		int boxColOffset = (j/3)*3;
		for (int x = 0; x < BOX_DIMENSIONS; x++)    // check elements in the box
			for (int y = 0; y < BOX_DIMENSIONS; y++) 
				if (val == board[boxRowOffset + y][boxColOffset + x].getValue())
					return false;

		return true; // no violations, so it's allowed
	}

	public static boolean getRowReference(int[][] board, ArrayList<HashSet<Integer>> rowRef){
		for(int row = 0; row < BOARD_DIMENSIONS; row++){
			HashSet<Integer> currentRowRef = new HashSet<Integer>(VALUE_LIST); 
			for(int col = 0; col < BOARD_DIMENSIONS; col++){
				int value = board[row][col];
				if(value != 0 && !currentRowRef.remove(Integer.valueOf(value)))
					return false;
			}
			rowRef.add(currentRowRef);
		}
		return true;
	}
	public static boolean getColReference(int[][] board, ArrayList<HashSet<Integer>> colRef){
		for(int col = 0; col < BOARD_DIMENSIONS; col++){
			HashSet<Integer> currentColRef = new HashSet<Integer>(VALUE_LIST); 
			for(int row = 0; row < BOARD_DIMENSIONS; row++){
				int value = board[row][col];
				if(value != 0 && !currentColRef.remove(Integer.valueOf(value)))
					return false;
			}
			colRef.add(currentColRef);
		}
		return true;
	}

	public static boolean getBoxReference(int[][] board, ArrayList<HashSet<Integer>> boxRef){
		for(int row = 0; row < BOARD_DIMENSIONS; row+=3){
			for(int col = 0; col < BOARD_DIMENSIONS; col+=3){
				HashSet<Integer> currentBoxRef = new HashSet<Integer>(VALUE_LIST); 

				for(int i = 0; i < BOX_DIMENSIONS; i++){
					for(int j = 0; j < BOX_DIMENSIONS; j++){
						int value = board[i + row][j + col];
						if(value != 0 && !currentBoxRef.remove(Integer.valueOf(value)))
							return false;
					}
				}
				boxRef.add(currentBoxRef);
			}
		}
		return true;
	}

	public static ArrayList<Integer> intersection(Set<Integer> a, Set<Integer> b, Set<Integer> c){
		List<Integer> iter = new ArrayList<Integer>(a);
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int n: iter) {
			if(b.contains(n) && c.contains(n)) {
				result.add(n);
			}
		}
		
		if(result.size() == 1){
			Integer value = result.get(0);
			a.remove(value);
			b.remove(value);
			c.remove(value);
		}

		return result;
	}

	//Print board to console for Debugging for CellObjects
	public static void printTable(CellObject[][] table)
	{
		for (int i = 0; i < BOARD_DIMENSIONS; i++) {
			if (i%BOX_DIMENSIONS == 0)
				System.out.println(" -----------------------");
			for (int j = 0; j < BOARD_DIMENSIONS; j++) {
				if (j%BOX_DIMENSIONS == 0)
					System.out.print("| ");
				System.out.print( (table[i][j].getValue() == 0) ? " " :
					Integer.toString(table[i][j].getValue()));
				System.out.print(' ');
			}
			System.out.println("|");
		}
		System.out.println(" -----------------------");
		System.out.println();
	}
}
