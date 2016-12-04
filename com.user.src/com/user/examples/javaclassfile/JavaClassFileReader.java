package com.user.examples.javaclassfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import com.ibm.layout.Layout;
import com.ibm.layout.Location;
import com.ibm.layout.Pointer;
import com.ibm.layout.VLArray;

public class JavaClassFileReader {
	private String classFileString = "";
	private JavaClassFileFormat jcf = null;
	private short longs = 0;
	private ArrayList<java.lang.Short> longIndices = new ArrayList<java.lang.Short>();
	
	short realIndex(short index) {
		for (java.lang.Short s : longIndices) {
			if (index > s) {
				index--;
			}
		}
		index--;
		return index;
	}
	
	short realIndexUp(short index) {
		index++;
		for (java.lang.Short s : longIndices) {
			if (index >= s) {
				index++;
			}
		}
		return index;
	}
	
	public JavaClassFileReader(String classFileName) {
		File file = new File(classFileName);
		RandomAccessFile rFile = null;
		FileChannel channel = null;
		try {
			rFile = new RandomAccessFile(file, "r");
			channel = rFile.getChannel();
			long fileSize = file.length();
			
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
			byte fileContents[] = new byte[(int) fileSize];
			buf.get(fileContents, 0, (int)fileSize);
			
			jcf = Layout.getLayout(JavaClassFileFormat.class);
			jcf.bindLocation(new Location(fileContents));
			channel.close();
			rFile.close();
		} catch (IOException e ) {
			System.out.println("could not open file: " + classFileName);
		} 
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("enter in the path to a class file");
			return;
		}
		JavaClassFileReader reader = new JavaClassFileReader(args[0]);
		System.out.println("printing " + args[0]);
		System.out.println(reader.classFileToString());
	}
	
	public String classFileToString() {
		if (classFileString.equals("")) {
			addString("Magic: " + Integer.toHexString(jcf.magic()));
			addString("Minor: " + jcf.minor());
			addString("Major: " + jcf.major());
			addString("ConstantPoolCount: " + jcf.cpCount());
			newLine();
			addString("ConstantPool:"); 
			addConstantPool();
			newLine();
			addString("Fields:"); 
			addFields();
			newLine();
			addString("Methods:"); 
			addMethods();
			
		}
		return classFileString;
	}
	
	private void addFields() {
		short fieldCount = jcf.fieldsCount();
		VLArray<Fields> fields = jcf.fieldInfo();
		
		for (int i = 0; i < fieldCount; i++) {
			Fields f = fields.at(i);
			addStringNNL(getUTF8String(jcf.cp().at(realIndex(f.descriptorIndex()))));
			addStringNNL(" ");
			addString(getUTF8String(jcf.cp().at(realIndex(f.nameIndex()))));
			addString(" accessFlags: " + f.accessFlags());
		}
		
	}

	private void addMethods() {
		short methodCount = jcf.methodsCount();
		VLArray<Methods> methods = jcf.methodInfo();
		
		for (int i = 0; i < methodCount; i++) {
			Methods m = methods.at(i);
			addStringNNL(getUTF8String(jcf.cp().at(realIndex(m.descriptorIndex()))));
			addStringNNL(" ");
			addString(getUTF8String(jcf.cp().at(realIndex(m.nameIndex()))));
			addString(" accessFlags: " + m.accessFlags());
			newLine();
		}
	}
	
	private void addString(String str) {
		classFileString += str + "\n";
	}
	
	private void addStringNNL(String str) {
		classFileString += str;
	}
	
	private void newLine() {
		classFileString += "\n";
	}
	
	private String getUTF8String(CPEntry utf8) {
		@SuppressWarnings("unchecked")
		Pointer<CONSTANTUtf8Info> utf8Ptr = (Pointer<CONSTANTUtf8Info>) utf8.EA().tag().castTo(CONSTANTUtf8Info.class);
		return constantUtf8InfoToString(utf8Ptr.lValue());
	}
	
	private String constantUtf8InfoToString(CONSTANTUtf8Info utf8) {
		int size = utf8.length();
		byte bytes[] = new byte[size];
		
		VLArray<Byte> utfBytes = utf8.bytes();
		for(int i = 0; i < size; i++) {
			bytes[i] = utfBytes.at(i).val();
		}
		
		return new String(bytes);
	}
	
	private String getClassString(short index) {
		return getUTF8String(jcf.cp().at(realIndex(payloadToShort(jcf.cp().at(index).info()))));
	}
	
	private String getNameAndType(short index) {
		CPEntry entry = jcf.cp().at(index);
		short index1 = payloadToShort(entry.info());
		short index2 = payloadToShort(entry.info(), 2);
		return "\"" + getUTF8String(jcf.cp().at(realIndex(index1))) + "\":" +  getUTF8String(jcf.cp().at(realIndex(index2)));
	}
	
	@SuppressWarnings("unchecked")
	private short payloadToShort(CPEntryPayload<Byte> data) {
		return ((Pointer<Short>) data.at(0).EA().val().castTo(Short.class)).lValue().val();
	}
	
	@SuppressWarnings("unchecked")
	private short payloadToShort(CPEntryPayload<Byte> data, int index) {
		return ((Pointer<Short>) data.at(index).EA().val().castTo(Short.class)).lValue().val();
	}
	
	@SuppressWarnings("unchecked")
	private int payloadToInt(CPEntryPayload<Byte> data) {
		return ((Pointer<Int>) data.at(0).EA().val().castTo(Int.class)).lValue().val();
	}
	
	@SuppressWarnings("unchecked")
	private float payloadToFloat(CPEntryPayload<Byte> data) {
		return ((Pointer<Float>) data.at(0).EA().val().castTo(Float.class)).lValue().val();
	}
	
	@SuppressWarnings("unchecked")
	private long payloadToLong(CPEntryPayload<Byte> data) {
		return ((Pointer<Long>) data.at(0).EA().val().castTo(Long.class)).lValue().val();
	}
	
	@SuppressWarnings("unchecked")
	private double payloadToDouble(CPEntryPayload<Byte> data) {
		return ((Pointer<Double>) data.at(0).EA().val().castTo(Double.class)).lValue().val();
	}
	
	private void addConstantPool() {
		ConstantPool<CPEntry> constantPool = jcf.cp();
		long size = constantPool.getVLALength();
		
		//find all the longs
		for (int i = 0; i < size; i++) {
			CPEntry entry = constantPool.at(i);
			switch (entry.tag()) {
			case 5:
				longs++;
				longIndices.add((short) (i+1+longs));
				continue;
			case 6:
				longs++;
				longIndices.add((short) (i+1+longs));
				continue;
			}
		}
		
		for (short i = 0; i < size; i++) {
			short index1 = 0;
			short index2 = 0;
			CPEntry entry = constantPool.at(i);
			switch (entry.tag()) {
			case 1:
				addString("#" + realIndexUp(i) + " = Utf8 \t\t" + getUTF8String(entry));
				continue;
			case 3:
				addString("#" + realIndexUp(i) + " = Integer \t\t" + payloadToInt(entry.info()));
				continue;
			case 4:
				addString("#" + realIndexUp(i) + " = Float \t\t" + payloadToFloat(entry.info()));
				continue;
			case 5:
				addString("#" + realIndexUp(i) + " = Long \t\t" + payloadToLong(entry.info()));
				continue;
			case 6:
				addString("#" + realIndexUp(i) + " = Double \t\t" + payloadToDouble(entry.info()));
				continue;
			case 7:
				index1 = payloadToShort(entry.info());
				addString("#" + realIndexUp(i) + " = Class \t\t #" + index1 + "\t\t\t\t\t//" + getUTF8String(constantPool.at(realIndex(index1))));
				continue;
			case 8:
				index1 = payloadToShort(entry.info());
				addString("#" + realIndexUp(i) + " = String \t\t #" + index1 + "\t\t\t\t\t//" + getUTF8String(constantPool.at(realIndex(index1))));
				continue;
			case 9:
				index1 = payloadToShort(entry.info());
				index2 = payloadToShort(entry.info(), 2);
				addString("#" + realIndexUp(i) + " = FieldRef \t\t #" + index1+ ",#" + index2 + "\t\t\t\t\t//" + getClassString(realIndex(index1)) + ", " + getNameAndType(realIndex(index2)));
				continue;
			case 10:
				index1 = payloadToShort(entry.info());
				index2 = payloadToShort(entry.info(), 2);
				addString("#" + realIndexUp(i) + " = MethodRef \t #" + index1+ ",#" + index2 + "\t\t\t\t//" + getClassString(realIndex(index1)) + ", " + getNameAndType(realIndex(index2)));
				continue;
			case 11:
				index1 = payloadToShort(entry.info());
				index2 = payloadToShort(entry.info(), 2);
				addString("#" + realIndexUp(i) + " = InterfaceMethodRef \t #" + index1+ ",#" + index2 + "\t\t\t//" + getClassString(realIndex(index1)) + ", " + getNameAndType(realIndex(index2)));
				continue;
			case 12:
				index1 = payloadToShort(entry.info());
				index2 = payloadToShort(entry.info(), 2);
				addString("#" + realIndexUp(i) + " = NameAndType \t #" + index1+ ",#" + index2 + "\t\t\t\t\t//" + getUTF8String(constantPool.at(realIndex(index1))) + ", " + getUTF8String(constantPool.at(realIndex(index2))));
				continue;
			case 15:
				index1 = entry.info().at(0).val();
				index2 = payloadToShort(entry.info(), 1);
				addString("#" + realIndexUp(i) + " = MethodHandle \t #" + index1+ ",#" + index2);
				continue;
			case 16:
				index1 = payloadToShort(entry.info());
				addString("#" + realIndexUp(i) + " = MethodType \t #" + index1);
				continue;
			case 18:
				index1 = payloadToShort(entry.info());
				index2 = payloadToShort(entry.info(), 2);
				addString("#" + realIndexUp(i) + " = InvokeDynamic \t #" + index1+ ",#" + index2);
				continue;
			default:
				throw new RuntimeException("bad cpEntry");
			}
		}
	}
	
	
	
	

	
	
}
