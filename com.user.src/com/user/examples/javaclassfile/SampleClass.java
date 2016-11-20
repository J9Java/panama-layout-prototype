package com.user.examples.javaclassfile;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SampleClass {
	int field1 = 123123;
	double feilds = 1324123.41234123F;
	long field2 = 123411L;
	float field3 = 1234.1234123F;
	static MethodHandle mh = null;
	
	void meth() {
		System.out.println("string1");
	}
	
	public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
		System.out.println("blah");
		
		mh = MethodHandles.lookup().findStatic(SampleClass.class, "main", MethodType.methodType(void.class, String[].class));
	}
}
