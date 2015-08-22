package EnumValues;


public enum Invert_Box implements EnumValues<Integer> {
	INVERT_BOX0(0),
	INVERT_BOX1(1),
	INVERT_BOX2(2),
	INVERT_BOX3(3),
	INVERT_BOX4(4),
	INVERT_BOX5(5),
	INVERT_BOX6(6),
	INVERT_BOX7(7),
	INVERT_BOX8(8);
	
	private final Integer id;
	private static final EnumValuesMap<Invert_Box, Integer> resolver = new EnumValuesMap<>(Invert_Box.class); 

	private Invert_Box(Integer id) { 
		this.id = id; 
	}
	
	public Integer getValue() { 
		return id; 
	}

	public static Invert_Box getEnum(Integer value) {
		return resolver.get(value);
	}
}
