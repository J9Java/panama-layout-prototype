package com.user.examples.javaclassfile;

import com.ibm.layout.Layout;
import com.ibm.layout.Pointer;
import com.ibm.layout.PointerType;
import com.ibm.layout.PriviledgedVLArray;

public interface ConstantPool<T extends Layout> extends PriviledgedVLArray<T> {
	
	default long arrayLength(PointerType ptr) {
		@SuppressWarnings("unchecked")
		Pointer<Short> cpCountPtr = (Pointer<Short>) ptr.castTo(Short.class);
		short cpCount = (short) (cpCountPtr.lValue().val() - 1);
		@SuppressWarnings("unchecked")
		Pointer<CPEntry> entries = (Pointer<CPEntry>) cpCountPtr.atOffset(1).castTo(CPEntry.class);
		
		for (int i = 0; i < cpCount; i++) {
			byte tag = entries.lValue().tag();
			if (5 == tag || 6 == tag) {
				cpCount--;
			}
			
			entries = (Pointer<CPEntry>) entries.atOffset(1);
		}
		return cpCount;
	}

}
