package EnumValues;

import java.util.HashMap;
import java.util.Map;

//Code from:
//https://virgo47.wordpress.com/2014/08/02/converting-java-enums-to-values-and-back/
//Modular map to get Enum Constants based on their values
public class EnumValuesMap<T extends EnumValues<Y>, Y> {

	private final Map<Y, T> values = new HashMap<>();

	public EnumValuesMap(Class<T> enumClass) {
		for (T t : enumClass.getEnumConstants()) 
		{
			values.put(t.getValue(), t);
		}
	}

	public T get(Y value) {
		T enumValue = values.get(value);
		return enumValue;
	}
}
