package com.ibm.layout.gen;

import java.lang.reflect.Field;
import java.nio.ByteOrder;

import sun.misc.Unsafe;

class UnsafeImplHelper {
	static Unsafe unsafe = getUnsafe();
	final static boolean isLE = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	
	/** 
	 * get theUnsafe field from sun/misc/Unsafe
	 * 
	 * @return unsafe
	 */
	static public Unsafe getUnsafe() {
		if (null == unsafe) {
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);
				unsafe = (Unsafe)field.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return unsafe;
	}
	
	public final static boolean loadBEBoolean(Object obj, long off) {
		return unsafe.getBoolean(obj, off);
	}
	
	public final static boolean loadNativeBoolean(Object obj, long off) {
		return unsafe.getBoolean(obj, off);
	}
	
	public final static boolean loadLEBoolean(Object obj, long off) {
		return unsafe.getBoolean(obj, off);
	}
	
	public final static byte loadBEByte(Object obj, long off) {
		return unsafe.getByte(obj, off);
	}
	
	public final static byte loadNativeByte(Object obj, long off) {
		return unsafe.getByte(obj, off);
	}
	
	public final static byte loadLEByte(Object obj, long off) {
		return unsafe.getByte(obj, off);
	}
	
	public final static short loadBEShort(Object obj, long off) {
		short val = unsafe.getShort(obj, off);
		if (isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static short loadNativeShort(Object obj, long off) {
		return unsafe.getShort(obj, off);
	}
	
	public final static short loadLEShort(Object obj, long off) {
		short val = unsafe.getShort(obj, off);
		if (!isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static int loadBEInt(Object obj, long off) {
		int val = unsafe.getInt(obj, off);
		if (isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static int loadNativeInt(Object obj, long off) {
		return unsafe.getInt(obj, off);
	}
	
	public final static int loadLEInt(Object obj, long off) {
		int val = unsafe.getInt(obj, off);
		if (!isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static long loadBELong(Object obj, long off) {
		long val = unsafe.getLong(obj, off);
		if (isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static long loadNativeLong(Object obj, long off) {
		return unsafe.getLong(obj, off);
	}
	
	public final static long loadLELong(Object obj, long off) {
		long val = unsafe.getLong(obj, off);
		if (!isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static char loadBEChar(Object obj, long off) {
		short val = unsafe.getShort(obj, off);
		if (isLE) {
			return (char) switchEndian(val);
		}
		return (char) val;
	}
	
	public final static char loadNativeChar(Object obj, long off) {
		return unsafe.getChar(obj, off);
	}
	
	public final static char loadLEChar(Object obj, long off) {
		short val = unsafe.getShort(obj, off);
		if (!isLE) {
			return (char) switchEndian(val);
		}
		return (char) val;
	}
	
	public final static float loadBEFloat(Object obj, long off) {
		int val = unsafe.getInt(obj, off);
		if (isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static float loadNativeFloat(Object obj, long off) {
		return unsafe.getFloat(obj, off);
	}
	
	public final static float loadLEFloat(Object obj, long off) {
		int val = unsafe.getInt(obj, off);
		if (!isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static double loadBEDouble(Object obj, long off) {
		long val = unsafe.getLong(obj, off);
		if (isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	public final static double loadNativeDouble(Object obj, long off) {
		return unsafe.getDouble(obj, off);
	}
	
	public final static double loadLEDouble(Object obj, long off) {
		long val = unsafe.getLong(obj, off);
		if (!isLE) {
			return switchEndian(val);
		}
		return val;
	}
	
	private final static short switchEndian(short val) {
		short reversed = (short) ((val >> 8) & 0x00FF);
		reversed |= (short) ((val << 8) & 0xFF00);
		return reversed;
	}
	
	private final static int switchEndian(int val) {
		int reversed = (int) ((val >> 24) & 0x000000FF);
		reversed |= (int) ((val >> 8) & 0x0000FF00);
		reversed |= (int) ((val << 8) & 0x00FF0000);
		reversed |= (int) ((val << 24) & 0xFF000000);
		return reversed;
	}
	
	private final static long switchEndian(long val) {
		long reversed = (long) ((val >> 56) & 0x00000000000000FFL);
		reversed |= (long) ((val >> 40) & 0x000000000000FF00L);
		reversed |= (long) ((val >> 24) & 0x0000000000FF0000L);
		reversed |= (long) ((val >> 8) & 0x00000000FF000000L);
		reversed |= (long) ((val << 8) & 0x000000FF00000000L);
		reversed |= (long) ((val << 24) & 0x0000FF0000000000L);
		reversed |= (long) ((val << 40) & 0x00FF000000000000L);
		reversed |= (long) ((val << 56 ) & 0xFF00000000000000L);
		return reversed;
	}
	
	public final static void storeBEBoolean(Object obj, long off, boolean val) {
		unsafe.putBoolean(obj, off, val);
	}
	
	public final static void storeNativeBoolean(Object obj, long off, boolean val) {
		unsafe.putBoolean(obj, off, val);
	}
	
	public final static void storeLEBoolean(Object obj, long off, boolean val) {
		unsafe.putBoolean(obj, off, val);
	}
	
	public final static void storeBEByte(Object obj, long off, byte val) {
		unsafe.putByte(obj, off, val);
	}
	
	public final static void storeNativeByte(Object obj, long off, byte val) {
		unsafe.putByte(obj, off, val);
	}
	
	public final static void storeLEByte(Object obj, long off, byte val) {
		unsafe.putByte(obj, off, val);
	}
	
	public final static void storeBEShort(Object obj, long off, short val) {
		if (isLE) {
			val = switchEndian(val);
		}
		unsafe.putShort(obj, off, val);
	}
	
	public final static void storeNativeShort(Object obj, long off, short val) {
		unsafe.putShort(obj, off, val);
	}
	
	public final static void storeLEShort(Object obj, long off, short val) {
		if (!isLE) {
			val = switchEndian(val);
		}
		unsafe.putShort(obj, off, val);
	}
	
	public final static void storeBEInt(Object obj, long off, int val) {
		if (isLE) {
			val = switchEndian(val);
		}
		unsafe.putInt(obj, off, val);
	}
	
	public final static void storeNativeInt(Object obj, long off, int val) {
		unsafe.putInt(obj, off, val);
	}
	
	public final static void storeLEInt(Object obj, long off, int val) {
		if (!isLE) {
			val = switchEndian(val);
		}
		unsafe.putInt(obj, off, val);
	}
	
	public final static void storeBELong(Object obj, long off, long val) {
		if (isLE) {
			val = switchEndian(val);
		}
		unsafe.putLong(obj, off, val);
	}
	
	public final static void storeNativeLong(Object obj, long off, long val) {
		unsafe.putLong(obj, off, val);
	}
	
	public final static void storeLELong(Object obj, long off, long val) {
		if (!isLE) {
			val = switchEndian(val);
		}
		unsafe.putLong(obj, off, val);
	}
	
	public final static void storeBEChar(Object obj, long off, char val) {
		if (isLE) {
			val = (char) switchEndian(val);
		}
		unsafe.putChar(obj, off, val);
	}
	
	public final static void storeNativeChar(Object obj, long off, char val) {
		unsafe.putChar(obj, off, val);
	}
	
	public final static void storeLEChar(Object obj, long off, char val) {
		if (!isLE) {
			val = (char) switchEndian(val);
		}
		unsafe.putChar(obj, off, val);
	}
	
	public final static void storeBEFloat(Object obj, long off, float val) {
		if (isLE) {
			val = (float) switchEndian((int) val);
		}
		unsafe.putFloat(obj, off, val);
	}
	
	public final static void storeNativeFloat(Object obj, long off, float val) {
		unsafe.putFloat(obj, off, val);
	}
	
	public final static void storeLEFloat(Object obj, long off, float val) {
		if (!isLE) {
			val = (float) switchEndian((int) val);
		}
		unsafe.putFloat(obj, off, val);
	}
	
	public final static void storeBEDouble(Object obj, long off, double val) {
		if (isLE) {
			val = (double) switchEndian((long)val);
		}
		unsafe.putDouble(obj, off, val);
	}
	
	public final static void storeNativeDouble(Object obj, long off, double val) {
		unsafe.putDouble(obj, off, val);
	}
	
	public final static void storeLEDouble(Object obj, long off, double val) {
		if (!isLE) {
			val = (double) switchEndian((long)val);
		}
		unsafe.putDouble(obj, off, val);
	}
	
}
