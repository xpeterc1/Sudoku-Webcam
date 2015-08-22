package EnumValues;

public enum Box implements EnumValues<Integer> {
	BOX0(0),
	BOX1(1),
	BOX2(2),
	BOX3(3),
	BOX4(4),
	BOX5(5),
	BOX6(6),
	BOX7(7),
	BOX8(8);
	
	private final Integer id;
	private static final EnumValuesMap<Box, Integer> resolver = new EnumValuesMap<>(Box.class);

	private Box(Integer id) { 
		this.id = id; 
	}
	
	public Integer getValue() { 
		return id; 
	}

	public static Box getEnum(Integer value) {
	    return resolver.get(value);
	}
}
