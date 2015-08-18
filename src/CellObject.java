import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
//A class for storing information for a cell

public class CellObject {
	private static ArrayList<Integer> ORGINAL_LIST = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));
	private static int FIRST_ELEMENT = 0;
	private static int solvedCount = 0;
	private Integer value;
	private ArrayList<Integer> possibleValues;
	private boolean orginalNumber = false;


	//Store cell's value and give original list of possible values;
	public CellObject(Integer value){
		this.value = value;
		if(value != 0){
			this.orginalNumber = true;
			this.solvedCount++;
		}else{
			this.possibleValues = new ArrayList<Integer>(ORGINAL_LIST);
		}
	}

	//returns a count, this is used to see if any new values were added
	public int getSolvedCount(){
		return solvedCount;
	}
	
	//simple check if value is zero, a placeholder value
	public boolean hasValue(){
		return value != 0;
	}
	
	//sets the value back to zero, a placeholder, and decrement count on number of cells with values 
	public void resetValue(){
		this.value = 0;
		solvedCount--;
	}
	
	//return the arraylist of possible values this cell can have
	public ArrayList<Integer> getPossiblIntegers(){
		return possibleValues;
	}

	//later use for image to bold numbers from original values scanned in from image
	public boolean isOrginal(){
		return orginalNumber;
	}

	//returns value of this cell
	public Integer getValue(){
		return this.value;
	}
	
	//change the value of this cell
	public void setValue(Integer i){
		this.value = i;
		solvedCount++;
	}
	
	//sets the list of possible values and does a few check
	//if list size is zero, then no possible values means the board is invalid
	//otherwise if list is a size of 1, then there is only one possible value for the cell, and assumes that value
	public boolean setValList(ArrayList<Integer> list){
		this.possibleValues = new ArrayList<Integer>(list);
		if(list.size() == 0)
			return false;
		else if(list.size() == 1)
			this.value = list.get(FIRST_ELEMENT);
		return true;	

	}




}
