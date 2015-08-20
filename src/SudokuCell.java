import java.util.ArrayList;
import java.util.List;

//A class for storing information for a cell on the board
public class SudokuCell {
	private int value;
	private List<Integer> possibleValues;

	public SudokuCell(int value)
	{//Store cell's value and give original list of possible values;
		this.value = value;
	}

	public boolean isEmpty()
	{//simple check if value is zero, a placeholder value
		return value == 0;
	}
	
	//sets the value back to zero, a placeholder, and decrement count on number of cells with values 
	public void resetValue()
	{
		this.value = 0;
	}
	
	//return the arraylist of possible values this cell can have
	public List<Integer> getPossibleValues()
	{
		return possibleValues;
	}

	//returns value of this cell
	public int getValue()
	{
		return this.value;
	}
	
	//change the value of this cell
	public void setValue(int i)
	{
		this.value = i;
	}
	
	/*sets the list of possible values and does a few check
	* if list size is zero, then no possible values means the board is invalid
	*otherwise if list is a size of 1, then there is only one possible value for the cell, and assumes that value */
	public boolean setValList(List<Integer> list)
	{
		this.possibleValues = new ArrayList<Integer>(list);
		if(list.size() == 0)
			return false;
		else if(list.size() == 1)
			this.value = list.get(0);
		return true;	

	}




}
