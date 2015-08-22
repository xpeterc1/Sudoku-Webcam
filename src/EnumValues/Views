package EnumValues;

public enum Views implements EnumValues<Integer>{
	SCREEN_VIEW(0),
	CROP_VIEW(1),
	BOARD_OUTLINE_VIEW(2),
	BOX_OUTLINE_VIEW(3),
	MACHINE_VIEW(4);

	private final Integer id;
	private static final EnumValuesMap<Views, Integer> resolver = new EnumValuesMap<>(Views.class); 

	private Views(Integer id) { 
		this.id = id; 
	}
	
	public Integer getValue() { 
		return id; 
	}

	public static Views getEnum(Integer value) {
		return resolver.get(value);
	}
	

}
