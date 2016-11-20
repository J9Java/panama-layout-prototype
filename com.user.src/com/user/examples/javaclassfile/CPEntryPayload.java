package com.user.examples.javaclassfile;

import com.ibm.layout.Layout;
import com.ibm.layout.Pointer;
import com.ibm.layout.PointerType;
import com.ibm.layout.PriviledgedVLArray;

public interface CPEntryPayload<T extends Layout> extends PriviledgedVLArray<T> {
	default long arrayLength(PointerType ptr) {
		@SuppressWarnings("unchecked")
		Pointer<CPEntry> cpEntryPtr = (Pointer<CPEntry>) ptr.castTo(CPEntry.class);
		
		switch (cpEntryPtr.lValue().tag()) {
		case 1:
			@SuppressWarnings("unchecked")
			Pointer<CONSTANTUtf8Info> utf8Ptr = (Pointer<CONSTANTUtf8Info>) ptr.castTo(CONSTANTUtf8Info.class);
			return utf8Ptr.lValue().length() + 2;
		case 3:
			//CONSTANT_Integer 	3
		case 4:
			//CONSTANT_Float 	4
			return 4;
		case 5:
			//CONSTANT_Long 	5
		case 6:
			//CONSTANT_Double 	6
			return 8;
		case 7:
			//CONSTANT_Class 	7
			return 2;
		case 8:
			//CONSTANT_String 	8
			return 2;
		case 9:
			//CONSTANT_Fieldref 	9
		case 10:
			//CONSTANT_Methodref 	10
		case 11:
			//CONSTANT_InterfaceMethodref 	11
			return 4;
		case 12:
			//CONSTANT_NameAndType 	12
			return 4;
		case 15:
			//CONSTANT_MethodHandle 	15
			return 3;
		case 16:
			//CONSTANT_MethodType 	16
			return 2;
		case 18:
			//CONSTANT_InvokeDynamic 	18
			return 4;
		}
		return 0;
	}
}
