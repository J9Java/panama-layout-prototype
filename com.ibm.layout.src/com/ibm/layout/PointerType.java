package com.ibm.layout;

public interface PointerType extends LayoutType {
	public Location getLocation();
	
	public long sizeof();

	public PointerType castTo(Class<?> clazz);
	
	@Override
	public String toString();
}
