/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Test;

import sun.misc.Unsafe;

import com.ibm.layout.Array1D;
import com.ibm.layout.Array2D;
import com.ibm.layout.BooleanArray1D;
import com.ibm.layout.BooleanArray2D;
import com.ibm.layout.ByteArray1D;
import com.ibm.layout.ByteArray2D;
import com.ibm.layout.CharArray1D;
import com.ibm.layout.CharArray2D;
import com.ibm.layout.DoubleArray1D;
import com.ibm.layout.DoubleArray2D;
import com.ibm.layout.FloatArray1D;
import com.ibm.layout.FloatArray2D;
import com.ibm.layout.IntArray1D;
import com.ibm.layout.IntArray2D;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutHelper;
import com.ibm.layout.LayoutType;
import com.ibm.layout.Location;
import com.ibm.layout.LongArray1D;
import com.ibm.layout.LongArray2D;
import com.ibm.layout.ShortArray1D;
import com.ibm.layout.ShortArray2D;
import com.user.types.ArrayElement;
import com.user.types.AllPoints;
import com.user.types.ArrayCases;
import com.user.types.Boolean;
import com.user.types.Byte;
import com.user.types.Char;
import com.user.types.Double;
import com.user.types.Float;
import com.user.types.Int;
import com.user.types.IntM3;
import com.user.types.Line;
import com.user.types.Long;
import com.user.types.M3;
import com.user.types.MyBooleanArray1DCharSeq;
import com.user.types.MyByteArray1DCharSeq;
import com.user.types.MyCharArray1DCharSeq;
import com.user.types.MyDoubleArray1DCharSeq;
import com.user.types.MyFloatArray1DCharSeq;
import com.user.types.MyIntArray1DCharSeq;
import com.user.types.MyLine;
import com.user.types.MyLine_Ext;
import com.user.types.MyLongArray1DCharSeq;
import com.user.types.MyPrimBooleanArray1DCharSeq;
import com.user.types.MyPrimByteArray1DCharSeq;
import com.user.types.MyPrimCharArray1DCharSeq;
import com.user.types.MyPrimDoubleArray1DCharSeq;
import com.user.types.MyPrimFloatArray1DCharSeq;
import com.user.types.MyPrimIntArray1DCharSeq;
import com.user.types.MyPrimLongArray1DCharSeq;
import com.user.types.MyPrimShortArray1DCharSeq;
import com.user.types.MyShortArray1DCharSeq;
import com.user.types.Point;
import com.user.types.Point3D;
import com.user.types.Point4D;
import com.user.types.Point5D;
import com.user.types.Point5D_DoubleExt;
import com.user.types.Point5D_Ext;
import com.user.types.Short;
import com.user.types.VLS1;
import com.user.types.VLS3;
import com.user.types.VLSLong3;
import com.user.types.VariableLengthArrayWithByteRepeatCount;
import com.user.types.VariableLengthArrayWithCharRepeatCount;
import com.user.types.VariableLengthArrayWithIntRepeatCount;
import com.user.types.VariableLengthArrayWithLongRepeatCount;
import com.user.types.VariableLengthArrayWithShortRepeatCount;


public class TestLayout {
	static LayoutHelper f = LayoutHelper.getFactory();
	static Unsafe unsafe = null;
	static final int SIZE_OF_VLA1 = 10;
	static final int SIZE_OF_VLA2 = 5;
			
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPrimByteArray() {
		ByteArray1D bb = LayoutType.getPrimArray1D(byte.class, 10);
		ByteArray2D bb2 = LayoutType.getPrimArray2D(byte.class, 2, 5);
		Location loc = new Location(new byte[(int)(bb.getLength())]);
		//switching to map APIla
		bb.bindLocation(loc);
		bb2.bindLocation(loc);
		for (long i = 0; i < bb.getLength(); i++) {
			bb.put(i, (byte)(i + 3));
		}

		for (long i = 0; i < bb.getLength(); i++) {
			assertTrue(bb.at(i) == (byte)(i + 3));
		}
		
		for (long i = 0; i < bb2.dim1(); i++) {
			for (long j = 0; j < bb2.dim2(); j++) {
				assertTrue(bb2.at(i, j) == (byte)(j + (i * bb2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(bb.containsVLA());
		assertFalse(bb2.containsVLA());
	}
	
	@Test
	public void testPrimCharArray() {
		CharArray1D cc = LayoutType.getPrimArray1D(char.class, 10);
		CharArray2D cc2 = LayoutType.getPrimArray2D(char.class, 2, 5);
		Location loc = new Location(new byte[(int)(cc.getLength() * 2)]);
		//switching to map APIla
		cc.bindLocation(loc);
		cc2.bindLocation(loc);
		for (long i = 0; i < cc.getLength(); i++) {
			cc.put(i, (char)(i + 3));
		}

		for (long i = 0; i < cc.getLength(); i++) {
			assertTrue(cc.at(i) == (char)(i + 3));
		}
		
		for (long i = 0; i < cc2.dim1(); i++) {
			for (long j = 0; j < cc2.dim2(); j++) {
				assertTrue(cc2.at(i, j) == (char)(j + (i * cc2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(cc.containsVLA());
		assertFalse(cc2.containsVLA());
	}
	
	@Test
	public void testPrimIntArray() {
		IntArray1D ii = LayoutType.getPrimArray1D(int.class, 10);
		IntArray2D ii2 = LayoutType.getPrimArray2D(int.class, 2, 5);
		Location loc = new Location(new byte[(int)(ii.getLength() * 4)]);
		//switching to map APIla
		ii.bindLocation(loc);
		ii2.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (int)(i + 3));
		}

		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.at(i) == (int)(i + 3));
		}
		
		for (long i = 0; i < ii2.dim1(); i++) {
			for (long j = 0; j < ii2.dim2(); j++) {
				assertTrue(ii2.at(i, j) == (int)(j + (i * ii2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(ii.containsVLA());
		assertFalse(ii2.containsVLA());
	}
	
	@Test
	public void testPrimShortArray() {
		ShortArray1D ss = LayoutType.getPrimArray1D(short.class, 10);
		ShortArray2D ss2 = LayoutType.getPrimArray2D(short.class, 2, 5);
		Location loc = new Location(new byte[(short)(ss.getLength() * 2)]);
		//switching to map APIla
		ss.bindLocation(loc);
		ss2.bindLocation(loc);
		for (long i = 0; i < ss.getLength(); i++) {
			ss.put(i, (short)(i + 3));
		}

		for (long i = 0; i < ss.getLength(); i++) {
			assertTrue(ss.at(i) == (short)(i + 3));
		}
		
		for (long i = 0; i < ss2.dim1(); i++) {
			for (long j = 0; j < ss2.dim2(); j++) {
				assertTrue(ss2.at(i, j) == (short)(j + (i * ss2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(ss.containsVLA());
		assertFalse(ss2.containsVLA());
	}
	
	@Test
	public void testPrimLongArray() {
		LongArray1D ll = LayoutType.getPrimArray1D(long.class, 10);
		LongArray2D ll2 = LayoutType.getPrimArray2D(long.class, 2, 5);
		Location loc = new Location(new byte[(int)(ll.getLength() * 8)]);
		//switching to map APIla
		ll.bindLocation(loc);
		ll2.bindLocation(loc);
		for (long i = 0; i < ll.getLength(); i++) {
			ll.put(i, (long)(i + 3));
		}

		for (long i = 0; i < ll.getLength(); i++) {
			assertTrue(ll.at(i) == (long)(i + 3));
		}
		
		for (long i = 0; i < ll2.dim1(); i++) {
			for (long j = 0; j < ll2.dim2(); j++) {
				assertTrue(ll2.at(i, j) == (long)(j + (i * ll2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(ll.containsVLA());
		assertFalse(ll2.containsVLA());
	}
	
	@Test
	public void testPrimFloatArray() {
		FloatArray1D ff = LayoutType.getPrimArray1D(float.class, 10);
		FloatArray2D ff2 = LayoutType.getPrimArray2D(float.class, 2, 5);
		Location loc = new Location(new byte[(int)(ff.getLength() * 4)]);
		//switching to map APIla
		ff.bindLocation(loc);
		ff2.bindLocation(loc);
		for (long i = 0; i < ff.getLength(); i++) {
			ff.put(i, (float)(i + 3));
		}

		for (long i = 0; i < ff.getLength(); i++) {
			assertTrue(ff.at(i) == (float)(i + 3));
		}
		
		for (long i = 0; i < ff2.dim1(); i++) {
			for (long j = 0; j < ff2.dim2(); j++) {
				assertTrue(ff2.at(i, j) == (float)(j + (i * ff2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(ff.containsVLA());
		assertFalse(ff2.containsVLA());
	}
	
	@Test
	public void testPrimDoubleArray() {
		DoubleArray1D dd = LayoutType.getPrimArray1D(double.class, 10);
		DoubleArray2D dd2 = LayoutType.getPrimArray2D(double.class, 2, 5);
		Location loc = new Location(new byte[(int)(dd.getLength() * 8)]);
		//switching to map APIla
		dd.bindLocation(loc);
		dd2.bindLocation(loc);
		for (long i = 0; i < dd.getLength(); i++) {
			dd.put(i, (double)(i + 3));
		}

		for (long i = 0; i < dd.getLength(); i++) {
			assertTrue(dd.at(i) == (double)(i + 3));
		}
		
		for (long i = 0; i < dd2.dim1(); i++) {
			for (long j = 0; j < dd2.dim2(); j++) {
				assertTrue(dd2.at(i, j) == (double)(j + (i * dd2.dim2()) + 3));
			}
		}
		
		//test containsVLA
		assertFalse(dd.containsVLA());
		assertFalse(dd2.containsVLA());
	}
	
	@Test
	public void testPrimBooleanArray() {
		BooleanArray1D zz = LayoutType.getPrimArray1D(boolean.class, 10);
		BooleanArray2D zz2 = LayoutType.getPrimArray2D(boolean.class, 2, 5);
		Location loc = new Location(new byte[(int)(zz.getLength())]);
		//switching to map APIla
		zz.bindLocation(loc);
		zz2.bindLocation(loc);
		for (long i = 0; i < zz.getLength(); i++) {
			zz.put(i, true);
		}

		for (long i = 0; i < zz.getLength(); i++) {
			assertTrue(zz.at(i) == true);
		}
		
		for (long i = 0; i < zz2.dim1(); i++) {
			for (long j = 0; j < zz2.dim2(); j++) {
				assertTrue(zz2.at(i, j) == true);
			}
		}
		
		//test containsVLA
		assertFalse(zz.containsVLA());
		assertFalse(zz2.containsVLA());
	}
	
	@Test
	public void testOnHeap() {

		Point c = Layout.getLayout(Point.class);
		Location loc = new Location(new byte[(int) (c.sizeof()*8)]);
		c.bindLocation(loc);
		c.x(10);
		c.y(20);
		System.out.println("c: " + c);

		Array1D<Point> ca = Array1D.getArray1D(Point.class, 10);
		loc = new Location(new byte[(int) (ca.getLength() * 8)]);
		ca.bindLocation(loc);
		
		System.out.println("ca.getLength() = " + ca.getLength());
		for (long i = 0; i < ca.getLength(); i++) {
			ca.at(i).x((int)i);
			ca.at(i).y((int)-i);
		}
		for (long i = 0; i < ca.getLength(); i++) {
			System.out.println("ca[" + i + "]: " + ca.at(i));
			assertTrue(ca.at(i).x() == i);
			assertTrue(ca.at(i).y() == -i);
		}

		Array1D<Point> ca2 = ca.range(1, ca.getLength() - 2);
		System.out.println("ca2.getLength() = " + ca2.getLength());
		for (long i = 0; i < ca2.getLength(); i++) {
			System.out.println("ca2[" + i + "]: " + ca2.at(i));
			assertTrue(ca2.at(i).x() == ca.at(i + 1).x());
			assertTrue(ca2.at(i).y() == ca.at(i + 1).y());
		}
	}

	@Test
	public void testOffHeap() {
		long addr = 0;

		addr = unsafe.allocateMemory(8);
		Point c = Layout.getLayout(Point.class);
		Location loc = new Location(addr);
		c.bindLocation(loc);
		c.x(11);
		c.y(32);
		System.out.println("off heap c: " + c);
		unsafe.freeMemory(addr);

		addr = unsafe.allocateMemory(8 * 10);

		Array1D<Point> ca = Array1D.getArray1D(Point.class, 10);
		Location loc1 = new Location(addr);
		ca.bindLocation(loc1);
		System.out.println("ca.getLength() = " + ca.getLength());
		for (long i = 0; i < ca.getLength(); i++) {
			ca.at(i).x((int)i);
			ca.at(i).y((int)-i);
		}
		for (long i = 0; i < ca.getLength(); i++) {
			System.out.println("ca[" + i + "]: " + ca.at(i));
			assertTrue(ca.at(i).x() == i);
			assertTrue(ca.at(i).y() == -i);
		}

		Array1D<Point> ca2 = ca.range(1, ca.getLength() - 2);
		System.out.println("ca2.getLength() = " + ca2.getLength());
		for (long i = 0; i < ca2.getLength(); i++) {
			System.out.println("ca2[" + i + "]: " + ca2.at(i));
			assertTrue(ca2.at(i).x() == ca.at(i + 1).x());
			assertTrue(ca2.at(i).y() == ca.at(i + 1).y());
		}

		unsafe.freeMemory(addr);
	}

	@Test
	public void testLine() {
		System.out.println("=== testLine ===");
		long addr = 0;

		addr = unsafe.allocateMemory(16);
		Line c = Layout.getLayout(Line.class);
		Location loc = new Location(addr);
		c.bindLocation(loc);
		c.st().x(11);
		c.st().y(32);
		c.en().x(11);
		c.en().y(32);
		System.out.println("off heap c: " + c);
		assertTrue(c.st().x() == 11);
		assertTrue(c.st().y() == 32);
		assertTrue(c.en().x() == 11);
		assertTrue(c.en().y() == 32);
		unsafe.freeMemory(addr);

		addr = unsafe.allocateMemory(16 * 10);
		Array1D<Line> ca = Array1D.getArray1D(Line.class, 10);
		Location loc1 = new Location(addr);
		ca.bindLocation(loc1);
		System.out.println("ca.getLength() = " + ca.getLength());
		for (long i = 0; i < ca.getLength(); i++) {
			ca.at(i).en().x((int)i);
			ca.at(i).en().y((int)-i);
		}
		for (long i = 0; i < ca.getLength(); i++) {
			System.out.println("ca[" + i + "]: " + ca.at(i));
			assertTrue(ca.at(i).en().x() == i);
			assertTrue(ca.at(i).en().y() == -i);
		}

		Array1D<Line> ca2 = ca.range(1, ca.getLength() - 2);
		System.out.println("ca2.getLength() = " + ca2.getLength());
		for (long i = 0; i < ca2.getLength(); i++) {
			System.out.println("ca2[" + i + "]: " + ca2.at(i));
			assertTrue(ca2.at(i).en().x() == ca.at(i + 1).en().x());
			assertTrue(ca2.at(i).en().y() == ca.at(i + 1).en().y());
		}

		unsafe.freeMemory(addr);
	}

	@Test
	public void testByteArray() {
		System.out.println("=== testByteArray ===");

		//		Byte b = f.getLayout(Byte.class);
		//		System.out.println("b: " + b.value());
		//		b.value((byte)3);
		//		System.out.println("b: " + b.value());

		Array1D<Byte> ba = Array1D.getArray1D(Byte.class, 3);
		Location loc = new Location(new byte[(int)(ba.getLength() * 1)]);
		ba.bindLocation(loc);
		System.out.println("size = " + ba.sizeof());

		for (long i = 0; i < ba.getLength(); i++) {
			System.out.println("ba[" + i + "]: " + ba.at(i).value());
		}

		for (long i = 0; i < ba.getLength(); i++) {
			ba.at(i).value((byte)(10 - i));
		}
		for (long i = 0; i < ba.getLength(); i++) {
			System.out.println("ba[" + i + "]: " + ba.at(i));
		}

		byte bb[] = new byte[3];
		ba.writeToByteArray(0, bb, 0, bb.length);
		System.out.println("bb: " + Arrays.toString(bb));

		long addr = unsafe.allocateMemory(ba.sizeof());
		Array1D<Byte> ba2 = Array1D.getArray1D(Byte.class, ba.getLength());
		Location loc1 = new Location(new byte[(int)(ba2.getLength() * 1)]);
		ba2.bindLocation(loc1);
		ba2.readFromByteArray(0, bb, 0, bb.length);
		for (long i = 0; i < ba2.getLength(); i++) {
			System.out.println("ba2[" + i + "]: " + ba2.at(i));
		}
		unsafe.freeMemory(addr);
	}

	@Test
	public void testUnsafeCast() {
		System.out.println("== testUnsafeCast ==");
		Array1D<Byte> ba = Array1D.getArray1D(Byte.class, 8);
		Location loc = new Location(new byte[(int)(ba.getLength() * 1)]);
		ba.bindLocation(loc);
		
		for (long i = 0; i < ba.getLength(); i++) {
			ba.at(i).value((byte)(10 - i));
		}

		Point p = ba.unsafeCast(Point.class);
		System.out.println("p: " + p);

		byte[] bb = new byte[(int)p.sizeof()];
		p.writeToByteArray(0, bb, 0, bb.length);
		System.out.println("bb: " + Arrays.toString(bb));
		for (int i = 0; i < bb.length; i++) {
			assertTrue(bb[i] == (byte)(10 - i));
		}
	}

	@Test
	public void testCtor() {
		Point p = Layout.getLayout(Point.class);
		try {
			Point p2 = p.getClass().newInstance();
			fail("should have thrown IllegalAccessException");
			assertTrue(p2 == null);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (InstantiationException e) {
			System.out.println(e);
		}
	}

	@Test
	public void testArray1D() {
		// allocate off-heap 1D array
		long elementCount = 10;
		long addr = unsafe.allocateMemory(8 * elementCount);

		Array1D<Point> pp = Array1D.getArray1D(Point.class, elementCount);
		Location loc = new Location(addr);
		pp.bindLocation(loc);
		Point p = pp.at(1);
		p.x(10);

		unsafe.freeMemory(addr);
	}

	@Test
	public void testArray2D() {
		// allocate off-heap 2D array
		long rows = 3;
		long cols = 4;
		long addr = unsafe.allocateMemory(8 * rows * cols);

		Array2D<Point> pp = Array2D.getArray2D(Point.class, rows, cols);
		Location loc = new Location(addr);
		pp.bindLocation(loc);
		// synthesizes PointImpl, Point2DImpl, allocates Point2DImpl and returns.
		pp.at(0, 0).x(10);
		pp.at(0, 0).y(-5);
		pp.at(1, 0).x(20);
		pp.at(1, 0).y(-25);
		pp.put(0, 1, pp.at(1, 0));

		System.out.println("pp[0,1]: " + pp.at(0, 1));
		assertTrue(pp.at(0, 1).x() == 20);
		assertTrue(pp.at(0, 1).y() == -25);

		unsafe.freeMemory(addr);

		Array2D<Int> ii = Array2D.getArray2D(Int.class, rows, cols);
		Location loc1 = new Location(new byte[(int)(rows * cols * 4)]);
		ii.bindLocation(loc1);
		ii.at(0, 0).value(10);
		ii.at(1, 0).value(20);
		ii.put(0, 1, ii.at(1, 0));

		System.out.println("ii[0,1]: " + ii.at(0, 1));
		assertTrue(ii.at(0, 0).value() == 10);
		assertTrue(ii.at(1, 0).value() == 20);
		assertTrue(ii.at(0, 1).value() == 20);
	}

	/* array of primitives */
	@Test
	public void testIntM3() {
		IntArray2D q = LayoutType.getPrimArray2D(int.class, 1, 1);
		Location loc = new Location(new byte[(int) (q.dim1() * q.dim2() * 4)]);
		q.bindLocation(loc);
		System.out.println("q: " + q);

		IntM3 m3 = Layout.getLayout(IntM3.class);
		loc = new Location(new byte[(int)(48 * m3.sizeof())]);
		m3.bindLocation(loc);
		
		IntArray2D m = m3.m();
		System.out.println("m3 dim1 = " + m.dim1());
		for (long i = 0; i < m3.m().dim1(); i++) {
			m3.m().put(i, i, (int)i + 10);
		}
		System.out.println("m: " + m3.m());
		System.out.println("n: " + m3.n());
		System.out.println("p: " + m3.p());
	}

	@Test
	public void testM3() {
		System.out.println("== testM3 ==");

		Array2D<Int> q = Array2D.getArray2D(Int.class, 2,5);
		Location loc = new Location(new byte[(int)(q.dim1() * q.dim2() * 4)]);
		q.bindLocation(loc);
		for (long i = 0; i < q.dim1(); i++) {
			for (long j = 0; j < q.dim2(); j++) {
				q.at(i, j).value((int)(i + j));
				assertEquals("q.at(" + i + "," + j + ")", (i + j), q.at(i, j).value());
			}
		}
		System.out.println("q: " + q);
		for (long i = 0; i < q.dim1(); i++) {
			for (long j = 0; j < q.dim2(); j++) {
				assertEquals("q.at(" + i + "," + j + ")", (i + j), q.at(i, j).value());
			}
		}

		M3 m3 = Layout.getLayout(M3.class);
		Location loc1 = new Location(new byte[(int) (m3.sizeof() * 48)]);
		m3.bindLocation(loc1);
		System.out.println("m3 dim1 = " + m3.m().dim1());
		for (long i = 0; i < m3.m().dim1(); i++) {
			m3.m().at(i, i).value((int)i + 10);
		}
		System.out.println("m: " + m3.m());
		System.out.println("n: " + m3.n());
		System.out.println("p: " + m3.p());
	}

	@Test
	public void testArrayCases() {
		System.out.println("== testArrayCases ==");
		ArrayCases ac = Layout.getLayout(ArrayCases.class);
		Location loc = new Location(new byte[(int) (ac.sizeof() * 140)]);
		ac.bindLocation(loc);
		
		for (long i = 0; i < ac.array1().getLength(); i++) {
			ac.array1().at(i).value((byte)i);
		}
		for (long i = 0; i < ac.array1().getLength(); i++) {
			assertEquals((byte)i, ac.array1().at(i).value());
			System.out.print(ac.array1().at(i) + " ");
		}
		System.out.println();

		for (long i = 0; i < ac.array2().dim1(); i++) {
			for (long j = 0; j < ac.array2().dim2(); j++) {
				ac.array2().at(i, j).value((int)(i + j));
			}
		}
		for (long i = 0; i < ac.array2().dim1(); i++) {
			for (long j = 0; j < ac.array2().dim2(); j++) {
				assertEquals((int)(i + j), ac.array2().at(i, j).value());
				System.out.print(ac.array2().at(i, j).value() + " ");
			}
		}
		System.out.println();

		for (long i = 0; i < ac.array3().getLength(); i++) {
			ac.array3().at(i).value((double)i);
		}
		for (long i = 0; i < ac.array3().getLength(); i++) {
			assertEquals((double)i, ac.array3().at(i).value(), 0.1);
			System.out.print(ac.array3().at(i).value() + " ");
		}
		System.out.println();

		for (long i = 0; i < ac.array4().dim1(); i++) {
			for (long j = 0; j < ac.array4().dim2(); j++) {
				ac.array4().at(i, j).value((byte)(i + j));
			}
		}
		for (long i = 0; i < ac.array4().dim1(); i++) {
			for (long j = 0; j < ac.array4().dim2(); j++) {
				assertEquals((byte)(i + j), ac.array2().at(i, j).value());
				System.out.print(ac.array2().at(i, j).value() + " ");
			}
		}
		System.out.println();
	}

	@Test
	public void testMyLine() {
		System.out.println("== testMyLine ==");
		MyLine line = Layout.getLayout(MyLine.class);
		Location loc = new Location(new byte[(int) (line.sizeof() * 16)]);
		line.bindLocation(loc);
		System.out.println("line: " + line);
		System.out.println(" length: " + line.length());
		line.st(10, 11);
		line.en(20, 21);
		System.out.println("line: " + line);
		System.out.println(" length: " + line.length());
	}

	@Test
	public void testMyLine_Ext() {
		System.out.println("== testMyLine_Ext ==");
		MyLine_Ext line = Layout.getLayout(MyLine_Ext.class);
		Location loc = new Location(new byte[(int) (line.sizeof() * 16)]);
		line.bindLocation(loc);
		System.out.println("line: " + line);
		System.out.println(" length: " + line.length());
		line.st(0, 0);
		line.en(5, 5);
		System.out.println("line: " + line);
		System.out.println(" length: " + line.length());
	}

	@Test
	public void test1DArrayMyLine() {
		System.out.println("== test1DArrayMyLine ==");

		Array1D<MyLine> ml = Array1D.getArray1D(MyLine.class, 6);
		Location loc = new Location(new byte[(int)(ml.getLength() * 16)]);
		ml.bindLocation(loc);
		for (int i = 0; i < ml.getLength(); i++) {
			ml.at(i).st(i, -i);
			ml.at(i).en(-i, i);
		}
		for (int i = 0; i < ml.getLength(); i++) {
			assertTrue(ml.at(i).st().x() == i);
			assertTrue(ml.at(i).st().y() == -i);
			assertTrue(ml.at(i).en().x() == -i);
			assertTrue(ml.at(i).en().y() == i);
			System.out.println(ml.at(i));
		}
	}

	@Test
	public void test2DArrayMyLine() {
		System.out.println("== test2DArrayMyLine ==");

		Array2D<MyLine> ml = Array2D.getArray2D(MyLine.class, 4, 4);
		Location loc = new Location(new byte[(int)(ml.dim1() * ml.dim2() * 16)]);
		ml.bindLocation(loc);
		for (int i = 0; i < ml.dim1(); i++) {
			for (int j = 0; j < ml.dim2(); j++) {
				ml.at(i, j).st(i, j);
				ml.at(i, j).en(j, i);
			}
		}
		for (int i = 0; i < ml.dim1(); i++) {
			for (int j = 0; j < ml.dim2(); j++) {
				assertEquals(i, ml.at(i, j).st().x());
				assertEquals(j, ml.at(i, j).st().y());
				assertEquals(j, ml.at(i, j).en().x());
				assertEquals(i, ml.at(i, j).en().y());
				System.out.print(ml.at(i, j) + " ");
			}
			System.out.println();
		}
	}

	@Test
	public void test1DArrayMyLine_Ext() {
		System.out.println("== test1DArrayMyLine_Ext ==");

		Array1D<MyLine_Ext> ml = Array1D.getArray1D(MyLine_Ext.class, 5);
		Location loc = new Location(new byte[(int)(ml.getLength() * 16)]);
		ml.bindLocation(loc);
		for (int i = 0; i < ml.getLength(); i++) {
			ml.at(i).st(i, -i);
			ml.at(i).en(-i, i);
		}
		for (int i = 0; i < ml.getLength(); i++) {
			assertTrue(ml.at(i).st().x() == i);
			assertTrue(ml.at(i).st().y() == -i);
			assertTrue(ml.at(i).en().x() == -i);
			assertTrue(ml.at(i).en().y() == i);
			System.out.println(ml.at(i));
		}
	}

	@Test
	public void test2DArrayMyLine_Ext() {
		System.out.println("== test2DArrayMyLine_Ext ==");

		Array2D<MyLine_Ext> ml = Array2D.getArray2D(MyLine_Ext.class, 3, 3);
		Location loc = new Location(new byte[(int)(ml.dim1() * ml.dim2() * 16)]);
		ml.bindLocation(loc);
		for (int i = 0; i < ml.dim1(); i++) {
			for (int j = 0; j < ml.dim2(); j++) {
				ml.at(i, j).st(i, j);
				ml.at(i, j).en(j, i);
			}
		}
		for (int i = 0; i < ml.dim1(); i++) {
			for (int j = 0; j < ml.dim2(); j++) {
				assertEquals(i, ml.at(i, j).st().x());
				assertEquals(j, ml.at(i, j).st().y());
				assertEquals(j, ml.at(i, j).en().x());
				assertEquals(i, ml.at(i, j).en().y());
				System.out.print(ml.at(i, j) + " ");
			}
			System.out.println();
		}
	}

	@Test
	public void testPoint3D() {
		System.out.println("== testPoint3D ==");
		Point3D p3 = Layout.getLayout(Point3D.class);
		Location loc = new Location(new byte[(int) (4 * p3.sizeof())]);
		p3.bindLocation(loc);
		p3.x(1);
		p3.y(2);
		p3.z(3);
		assertEquals(1, p3.x());
		assertEquals(2, p3.y());
		assertEquals(3, p3.z());
		System.out.print("x:" + p3.x() + "  ");
		System.out.print("y:" + p3.y() + "  ");
		System.out.println("z:" + p3.z());
	}

	@Test
	public void test1DArrayPoint3D() {
		System.out.println("== test1DArrayPoint3D ==");
		Array1D<Point3D> p3 = Array1D.getArray1D(Point3D.class, 5);
		Location loc = new Location(new byte[(int)(p3.getLength() * 12)]);
		p3.bindLocation(loc);
		for (int i = 0; i < p3.getLength(); i++) {
			p3.at(i).x(i);
			p3.at(i).y(i + 1);
			p3.at(i).z(i + 2);
		}
		for (int i = 0; i < p3.getLength(); i++) {
			assertEquals(i, p3.at(i).x());
			assertEquals(i + 1, p3.at(i).y());
			assertEquals(i + 2, p3.at(i).z());
		}
		for (int i = 0; i < p3.getLength(); i++) {
			System.out.print("x:" + p3.at(i).x() + "  ");
			System.out.print("y:" + p3.at(i).y() + "  ");
			System.out.println("z:" + p3.at(i).z());
		}
	}

	@Test
	public void test2DArrayPoint3D() {
		System.out.println("== test2DArrayPoint3D ==");
		Array2D<Point3D> p3 = Array2D.getArray2D(Point3D.class, 5, 5);
		Location loc = new Location(new byte[(int)(p3.dim1() * p3.dim2() * 12)]);
		p3.bindLocation(loc);
		for (int i = 0; i < p3.dim1(); i++) {
			for (int j = 0; j < p3.dim2(); j++) {
				p3.at(i, j).x(i + j);
				p3.at(i, j).y(i + j + 1);
				p3.at(i, j).z(i + j + 2);
			}
		}
		for (int i = 0; i < p3.dim1(); i++) {
			for (int j = 0; j < p3.dim2(); j++) {
				assertEquals(i + j, p3.at(i, j).x());
				assertEquals(i + j + 1, p3.at(i, j).y());
				assertEquals(i + j + 2, p3.at(i, j).z());
				System.out.print(p3.at(i, j) + " ");
			}
			System.out.println();
		}
	}

	@Test
	public void testPoint4D() {
		System.out.println("== testPoint4D ==");
		Point4D p4 = Layout.getLayout(Point4D.class);
		Location loc = new Location(new byte[(int)(p4.sizeof() * 4)]);
		p4.bindLocation(loc);
		p4.x(1);
		p4.y(2);
		p4.z(3);
		p4.o(4);
		assertEquals(1, p4.x());
		assertEquals(2, p4.y());
		assertEquals(3, p4.z());
		assertEquals(4, p4.o());
		System.out.print("x:" + p4.x() + "  ");
		System.out.print("y:" + p4.y() + "  ");
		System.out.print("z:" + p4.z() + "  ");
		System.out.print("o:" + p4.o() + "  ");
		System.out.println();
	}

	@Test
	public void test1DArrayPoint4D() {
		System.out.println("== test1DArrayPoint4D ==");
		Array1D<Point4D> p4 = Array1D.getArray1D(Point4D.class, 3);
		Location loc = new Location(new byte[(int)(p4.getLength() * 16)]);
		p4.bindLocation(loc);
		for (int i = 0; i < p4.getLength(); i++) {
			p4.at(i).x(i);
			p4.at(i).y(i + 1);
			p4.at(i).z(i + 2);
			p4.at(i).o(i + 3);
		}
		for (int i = 0; i < p4.getLength(); i++) {
			assertEquals(i, p4.at(i).x());
			assertEquals(i + 1, p4.at(i).y());
			assertEquals(i + 2, p4.at(i).z());
			assertEquals(i + 3, p4.at(i).o());
		}
		for (int i = 0; i < p4.getLength(); i++) {
			System.out.print("x:" + p4.at(i).x() + "  ");
			System.out.print("y:" + p4.at(i).y() + "  ");
			System.out.print("z:" + p4.at(i).z() + "  ");
			System.out.println("o:" + p4.at(i).o());
		}
	}

	@Test
	public void test2DArrayPoint4D() {
		System.out.println("== test2DArrayPoint4D ==");
		Array2D<Point4D> p4 = Array2D.getArray2D(Point4D.class, 4, 4);
		Location loc = new Location(new byte[(int)(p4.dim1() * p4.dim2() * 16)]);
		p4.bindLocation(loc);
		for (int i = 0; i < p4.dim1(); i++) {
			for (int j = 0; j < p4.dim2(); j++) {
				p4.at(i, j).x(i + j);
				p4.at(i, j).y(i + j + 1);
				p4.at(i, j).z(i + j + 2);
				p4.at(i, j).o(i + j + 3);
			}
		}
		for (int i = 0; i < p4.dim1(); i++) {
			for (int j = 0; j < p4.dim2(); j++) {
				assertEquals(i + j, p4.at(i, j).x());
				assertEquals(i + j + 1, p4.at(i, j).y());
				assertEquals(i + j + 2, p4.at(i, j).z());
				assertEquals(i + j + 3, p4.at(i, j).o());
				System.out.print(p4.at(i, j) + " ");
			}
			System.out.println();
		}
	}

	@Test
	public void testPoint5D() {
		System.out.println("== testPoint5D ==");
		Point5D p5 = Layout.getLayout(Point5D.class);
		Location loc = new Location(new byte[(int) (4 * p5.sizeof())]);
		p5.bindLocation(loc);
		p5.x(1);
		p5.y(2);
		p5.z(3);
		p5.o(4);
		p5.p(5);
		assertEquals(1, p5.x());
		assertEquals(2, p5.y());
		assertEquals(3, p5.z());
		assertEquals(4, p5.o());
		assertEquals(5, p5.p());
		System.out.print("x:" + p5.x() + "  ");
		System.out.print("y:" + p5.y() + "  ");
		System.out.print("z:" + p5.z() + "  ");
		System.out.print("o:" + p5.o() + "  ");
		System.out.println("p:" + p5.p() + "  ");
	}

	@Test
	public void testPoint5D_Ext() {
		System.out.println("== testPoint5D_Ext ==");
		Point5D_Ext p5e =  Layout.getLayout(Point5D_Ext.class);
		Location loc = new Location(new byte[(int) (4 * p5e.sizeof())]);
		p5e.bindLocation(loc);
		p5e.set(1, 2, 3, 4, 5);
		assertEquals(1, p5e.x());
		assertEquals(2, p5e.y());
		assertEquals(3, p5e.z());
		assertEquals(4, p5e.o());
		assertEquals(5, p5e.p());
		System.out.println(p5e.sum());
	}

	@Test
	public void testPoint5D_DoubleExt() {
		System.out.println("== testPoint5D_DoubleExt ==");
		Point5D_DoubleExt p5de = Layout.getLayout(Point5D_DoubleExt.class);
		Location loc = new Location(new byte[(int) (4 * p5de.sizeof())]);
		p5de.bindLocation(loc);
		p5de.set(1, 2, 3, 4, 5);
		assertEquals(1, p5de.x());
		assertEquals(2, p5de.y());
		assertEquals(3, p5de.z());
		assertEquals(4, p5de.o());
		assertEquals(5, p5de.p());
		System.out.println(p5de.product());
	}

	@Test
	public void testAllPoints() {
		System.out.println("== testAllPoints ==");
		AllPoints ap = Layout.getLayout(AllPoints.class);
		Location loc = new Location(new byte[(int) (ap.sizeof() * 56)]);
		ap.bindLocation(loc);
		ap.a().x(1);
		ap.b().y(2);
		ap.c().z(3);
		ap.d().o(4);
		ap.d().p(5);
		System.out.println(ap);
		assertEquals(1, ap.a().x());
		assertEquals(2, ap.b().y());
		assertEquals(3, ap.c().z());
		assertEquals(4, ap.d().o());
		assertEquals(5, ap.d().p());
	}

	@Test
	public void testGetDoubleArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for double array 1D
		System.out.println("== testGetDoubleArray1DCharSeq ==");
		MyDoubleArray1DCharSeq dd = Array1D.getUserArray1D(MyDoubleArray1DCharSeq.class, Double.class, 2);
		Location loc = new Location(new byte[(int) (dd.length() * 8)]);
		dd.bindLocation(loc);
		for (long i = 0; i < dd.getLength(); i++) {
			dd.at(i).value((double)(i + 65 + 20));
		}
		for (long i = 0; i < dd.getLength(); i++) {
			assertTrue(dd.at(i).value() == (double)(i + 65 + 20));
		}
		System.out.println(dd);
		//test getLength(),put(),at()
		for (long i = 0; i < dd.getLength(); i++) {
			assertTrue(dd.charAt((int)i) == (char)(i + 65 + 20));
			System.out.print(dd.charAt((int)i));
		}
		System.out.println();
		assertTrue(dd.length() == dd.getLength());
		//test SubSequence
		dd = (MyDoubleArray1DCharSeq)dd.subSequence(1, 2);
		//test charAt()
		for (long i = 0; i < dd.getLength(); i++) {
			System.out.print(dd.charAt((int)i));
		}
		System.out.println();
		assertTrue(dd.length() == dd.getLength());
	}
	
	@Test
	public void testGetByteArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for byte array 1D
		System.out.println("== testGetByteArray1DCharSeq ==");
		MyByteArray1DCharSeq bb = Array1D.getUserArray1D(MyByteArray1DCharSeq.class, Byte.class, 10);
		Location loc = new Location(new byte[(int) (bb.length() * 1)]);
		bb.bindLocation(loc);
		for (long i = 0; i < bb.getLength(); i++) {
			bb.at(i).value((byte)(i + 65));
		}
		for (long i = 0; i < bb.getLength(); i++) {
			assertTrue(bb.at(i).value() == (byte)(i + 65));
		}
		System.out.println(bb);
		//test getLength(),put(),at()
		for (long i = 0; i < bb.getLength(); i++) {
			assertTrue(bb.charAt((int)i) == (char)(i + 65));
			System.out.print(bb.charAt((int)i));
		}
		System.out.println();
		assertTrue(bb.length() == bb.getLength());
		//test SubSequence
		bb = (MyByteArray1DCharSeq)bb.subSequence(0, 5);
		//test charAt()
		for (long i = 0; i < bb.getLength(); i++) {
			System.out.print(bb.charAt((int)i));
		}
		System.out.println();
		assertTrue(bb.length() == bb.getLength());
	}
	
	@Test
	public void testGetBooleanArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for boolean array 1D
		System.out.println("== testGetBooleanArray1DCharSeq ==");
		MyBooleanArray1DCharSeq bb = Array1D.getUserArray1D(MyBooleanArray1DCharSeq.class, Boolean.class, 8);
		Location loc = new Location(new byte[(int) (bb.length())]);
		bb.bindLocation(loc);
		for (long i = 0; i < bb.getLength(); i++) {
			if (i % 2 == 0) {
				bb.at(i).value(true);
			} else {
				bb.at(i).value(false);
			}
		}
		for (long i = 0; i < bb.getLength(); i++) {
			if (i % 2 == 0) {
				assertTrue(bb.at(i).value() == true);
			} else {
				assertTrue(bb.at(i).value() == false);
			}
		}
		System.out.println(bb);
		//test getLength(),put(),at()
		for (long i = 0; i < bb.getLength(); i++) {
			if (i % 2 == 0) {
				assertTrue(bb.charAt((int)i) == 'T');
			} else {
				assertTrue(bb.charAt((int)i) == 'F');
			}
			System.out.print(bb.charAt((int)i));
		}
		System.out.println();
		assertTrue(bb.length() == bb.getLength());
		//test SubSequence
		bb = (MyBooleanArray1DCharSeq)bb.subSequence(0, 3);
		//test charAt()
		for (long i = 0; i < bb.getLength(); i++) {
			System.out.print(bb.charAt((int)i));
		}
		System.out.println();
		assertTrue(bb.length() == bb.getLength());
	}
	
	@Test
	public void testGetIntArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for int array 1D
		System.out.println("== testGetIntArray1DCharSeq ==");
		MyIntArray1DCharSeq ii = Array1D.getUserArray1D(MyIntArray1DCharSeq.class, Int.class, 5);
		Location loc = new Location(new byte[(int) (ii.length() * 4)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.at(i).value((byte)(i + 65));
		}
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.at(i).value() == (byte)(i + 65));
		}
		System.out.println(ii);
		//test getLength(),put(),at()
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(i + 65));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
		assertTrue(ii.length() == ii.getLength());
		//test SubSequence
		ii = (MyIntArray1DCharSeq)ii.subSequence(0, 2);
		//test charAt()
		for (long i = 0; i < ii.getLength(); i++) {
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
		assertTrue(ii.length() == ii.getLength());
	}

	@Test
	public void testGetLongArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for long array 1D
		System.out.println("== testGetLongArray1DCharSeq ==");
		MyLongArray1DCharSeq ll = Array1D.getUserArray1D(MyLongArray1DCharSeq.class, Long.class, 5);
		Location loc = new Location(new byte[(int) (ll.length() * 8)]);
		ll.bindLocation(loc);
		for (long i = 0; i < ll.getLength(); i++) {
			ll.at(i).value((long)(i + 120));
		}
		for (long i = 0; i < ll.getLength(); i++) {
			assertTrue(ll.at(i).value() == (long)(i + 120));
		}
		System.out.println(ll);
		//test getLength(),put(),at()
		for (long i = 0; i < ll.getLength(); i++) {
			assertTrue(ll.charAt((int)i) == (char)(i + 120));
			System.out.print(ll.charAt((int)i));
		}
		System.out.println();
		assertTrue(ll.length() == ll.getLength());
		//test SubSequence
		ll = (MyLongArray1DCharSeq)ll.subSequence(0, 3);
		//test charAt()
		for (long i = 0; i < ll.getLength(); i++) {
			System.out.print(ll.charAt((int)i));
		}
		System.out.println();
		assertTrue(ll.length() == ll.getLength());
	}

	@Test
	public void testGetShortArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for short array 1D
		System.out.println("== testGetShortArray1DCharSeq ==");
		MyShortArray1DCharSeq ss = Array1D.getUserArray1D(MyShortArray1DCharSeq.class, Short.class, 7);
		Location loc = new Location(new byte[(int) (ss.length() * 2)]);
		ss.bindLocation(loc);
		for (long i = 0; i < ss.getLength(); i++) {
			ss.at(i).value((short)(i + 65));
		}
		for (long i = 0; i < ss.getLength(); i++) {
			assertTrue(ss.at(i).value() == (short)(i + 65));
		}
		System.out.println(ss);
		//test getLength(),put(),at()
		for (long i = 0; i < ss.getLength(); i++) {
			assertTrue(ss.charAt((int)i) == (char)(i + 65));
			System.out.print(ss.charAt((int)i));
		}
		System.out.println();
		assertTrue(ss.length() == ss.getLength());
		//test SubSequence
		ss = (MyShortArray1DCharSeq)ss.subSequence(3, 5);
		//test charAt()
		for (long i = 0; i < ss.getLength(); i++) {
			System.out.print(ss.charAt((int)i));
		}
		System.out.println();
		assertTrue(ss.length() == ss.getLength());
	}
	
	@Test
	public void testGetFloatArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for float array 1D
		System.out.println("== testGetFloatArray1DCharSeq ==");
		MyFloatArray1DCharSeq ff = Array1D.getUserArray1D(MyFloatArray1DCharSeq.class, Float.class, 10);
		Location loc = new Location(new byte[(int) (ff.length() * 4)]);
		ff.bindLocation(loc);
		for (long i = 0; i < ff.getLength(); i++) {
			ff.at(i).value((float)(i + 65 + 10));
		}
		for (long i = 0; i < ff.getLength(); i++) {
			assertTrue(ff.at(i).value() == (float)(i + 65 + 10));
		}
		System.out.println(ff);
		//test getLength(),put(),at()
		for (long i = 0; i < ff.getLength(); i++) {
			assertTrue(ff.charAt((int)i) == (char)(i + 65 + 10));
			System.out.print(ff.charAt((int)i));
		}
		System.out.println();
		assertTrue(ff.length() == ff.getLength());
		//test SubSequence
		ff = (MyFloatArray1DCharSeq)ff.subSequence(0, 5);
		//test charAt()
		for (long i = 0; i < ff.getLength(); i++) {
			System.out.print(ff.charAt((int)i));
		}
		System.out.println();
		assertTrue(ff.length() == ff.getLength());
	}

	@Test
	public void testGetCharArray1DCharSeq() {
		//test getPrimArray1DCharSeq() in Layout Factory for char array 1D
		System.out.println("== testGetCharArray1DCharSeq ==");
		MyCharArray1DCharSeq cc = Array1D.getUserArray1D(MyCharArray1DCharSeq.class, Char.class, 5);
		Location loc = new Location(new byte[(int) (cc.length() * 2)]);
		cc.bindLocation(loc);
		for (long i = 0; i < cc.getLength(); i++) {
			cc.at(i).value((char)(i + 65));
		}
		for (long i = 0; i < cc.getLength(); i++) {
			assertTrue(cc.at(i).value() == (char)(i + 65));
		}
		System.out.println(cc);
		//test getLength(),put(),at()
		for (long i = 0; i < cc.getLength(); i++) {
			assertTrue(cc.charAt((int)i) == (char)(i + 65));
			System.out.print(cc.charAt((int)i));
		}
		System.out.println();
		assertTrue(cc.length() == cc.getLength());
		//test SubSequence
		cc = (MyCharArray1DCharSeq)cc.subSequence(0, 3);
		//test charAt()
		for (long i = 0; i < cc.getLength(); i++) {
			System.out.print(cc.charAt((int)i));
		}
		System.out.println();
		assertTrue(cc.length() == cc.getLength());
	}
	
	@Test
	public void testGetUserDefinedByteArray1D() {
		System.out.println("== testGetUserDefinedByteArray1D ==");
		MyPrimByteArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimByteArray1DCharSeq.class, byte.class, 10);
		Location loc = new Location(new byte[(int) (ii.length())]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (byte)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimByteArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (byte)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedDoubleArray1D() {
		System.out.println("== testGetUserDefinedDoubleArray1D ==");
		MyPrimDoubleArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimDoubleArray1DCharSeq.class, double.class, 10);
		Location loc = new Location(new byte[(int) (ii.length() * 8)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (double)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimDoubleArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (double)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedBooleanArray1D() {
		System.out.println("== testGetUserDefinedLongArray1D ==");
		MyPrimBooleanArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimBooleanArray1DCharSeq.class, boolean.class, 10);
		Location loc = new Location(new byte[(int) (ii.length())]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			if (i % 2 == 0) {
				ii.put(i, true);
			} else {
				ii.put(i, false);
			}
		}
		for (long i = 0; i < ii.getLength(); i++) {
			if (i % 2 == 0) {
				assertTrue(ii.at(i) == true);
			} else {
				assertTrue(ii.at(i) == false);
			}
		}
		System.out.println(ii);
		//test getLength(),put(),at()
		for (long i = 0; i < ii.getLength(); i++) {
			if (i % 2 == 0) {
				assertTrue(ii.charAt((int)i) == 'T');
			} else {
				assertTrue(ii.charAt((int)i) == 'F');
			}
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
		assertTrue(ii.length() == ii.getLength());
		//test SubSequence
		ii = (MyPrimBooleanArray1DCharSeq)ii.subSequence(0, 3);
		//test charAt()
		for (long i = 0; i < ii.getLength(); i++) {
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedShortArray1D() {
		System.out.println("== testGetUserDefinedShortArray1D ==");
		MyPrimShortArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimShortArray1DCharSeq.class, short.class, 10);
		Location loc = new Location(new byte[(int) (ii.length() * 2)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (short)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimShortArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (short)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedIntArray1D() {
		System.out.println("== testGetUserDefinedIntArray1D ==");
		MyPrimIntArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimIntArray1DCharSeq.class, int.class, 10);
		Location loc = new Location(new byte[(int) ( ii.length() * 4)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (int)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimIntArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (int)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedCharArray1D() {
		System.out.println("== testGetUserDefinedCharArray1D ==");
		MyPrimCharArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimCharArray1DCharSeq.class, char.class, 10);
		Location loc = new Location(new byte[(int) (ii.length() * 2)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (char)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimCharArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (char)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedFloatArray1D() {
		System.out.println("== testGetUserDefinedFloatArray1D ==");
		MyPrimFloatArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimFloatArray1DCharSeq.class, float.class, 10);
		Location loc = new Location(new byte[(int) (ii.length() * 4)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (float)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimFloatArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (float)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testGetUserDefinedLongArray1D() {
		System.out.println("== testGetUserDefinedLongArray1D ==");
		MyPrimLongArray1DCharSeq ii = LayoutType.getPrimUserArray1D(MyPrimLongArray1DCharSeq.class, long.class, 10);
		Location loc = new Location(new byte[(int) (ii.length() * 8)]);
		ii.bindLocation(loc);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (long)(10 - i));
			assertTrue(ii.at(i) == (10 - i));
		}
		System.out.println(ii);
		ii = (MyPrimLongArray1DCharSeq)ii.subSequence(0, 1);
		for (long i = 0; i < ii.getLength(); i++) {
			ii.put(i, (long)(i + 65));
		}
		System.out.println(ii);
		for (long i = 0; i < ii.getLength(); i++) {
			assertTrue(ii.charAt((int)i) == (char)(ii.at(i)));
			System.out.print(ii.charAt((int)i));
		}
		System.out.println();
	}
	
	@Test
	public void testPrimArray1DCharSeq() {
		System.out.println("== testBytePrimUserArray1DCharSeq ==");
		long elementCount = 10;
		long addr = unsafe.allocateMemory(elementCount);
		MyPrimByteArray1DCharSeq bb = LayoutType.getPrimUserArray1D(MyPrimByteArray1DCharSeq.class, byte.class, elementCount);
		Location loc = new Location(addr);
		bb.bindLocation(loc);
		for (long i = 0; i < bb.getLength(); i++) {
			bb.put(i, (byte)(i + 65 + 32));
		}
		for (long i = 0; i < bb.getLength(); i++) {
			assertTrue(bb.at(i) == (byte)(i + 65 + 32));
		}
		System.out.println(bb);
		//test getLength(),put(),at()
		for (long i = 0; i < bb.getLength(); i++) {
			assertTrue(bb.charAt((int)i) == (char)(i + 65 + 32));
			System.out.print(bb.charAt((int)i));
		}
		System.out.println();
		assertTrue(bb.length() == bb.getLength());
		//test SubSequence
		bb = (MyPrimByteArray1DCharSeq)bb.subSequence(0, 2);
		//test charAt()
		for (long i = 0; i < bb.getLength(); i++) {
			System.out.print(bb.charAt((int)i));
		}
		System.out.println();
		assertTrue(bb.length() == bb.getLength());
	}
	
	@Test
	public void testVLAWithByteRepeatCount() {
		System.out.println("== testVLAWithByteRepeatCount ==");
		
		//create variable length array with repeatCountInitializer test
		VariableLengthArrayWithByteRepeatCount vla = Layout.getLayout(VariableLengthArrayWithByteRepeatCount.class);
		//sizeof(jbyte) + sizeof(ArrayElement)*SIZE_OF_VLA1
		ArrayElement element = Layout.getLayout(ArrayElement.class);
		//long ptr = unsafe.allocateMemory(1 + element.sizeof() * SIZE_OF_VLA1);
		vla.bindLocation(new Location(new byte[(int) (1 + element.sizeof() * SIZE_OF_VLA1)]), (byte) SIZE_OF_VLA1);

		assertEquals((byte) SIZE_OF_VLA1, vla.lengthOfArray());
		assertEquals((long) SIZE_OF_VLA1, vla.elements().getVLALength());

		//overlay array on existing memory test
		VariableLengthArrayWithByteRepeatCount vla2 = Layout.getLayout(VariableLengthArrayWithByteRepeatCount.class);
		//sizeof(jbyte) + sizeof(ArrayElement)*SIZE_OF_VLA2
		long ptr2 = unsafe.allocateMemory(4 + element.sizeof() * SIZE_OF_VLA2);
		Location loc = new Location(ptr2);
		//set lengthOfArray to 5
		Byte lengthOfArray = Layout.getLayout(Byte.class);
		lengthOfArray.bindLocation(loc);
		lengthOfArray.value((byte) SIZE_OF_VLA2);
		//overlay on memory
		vla2.bindLocation(loc);
		
		assertEquals((byte) SIZE_OF_VLA2, vla2.lengthOfArray());
		assertEquals((long)SIZE_OF_VLA2, vla2.elements().getVLALength());
		
		//Set array element
		long ptr3 = unsafe.allocateMemory(element.sizeof());
		element.bindLocation(new Location(ptr3));
		element.item1(10);
		element.item2(20);
		vla.elements().put(0, element);
		vla2.elements().put(0, element);
		
		assertEquals(element.item1(), vla.elements().at(0).item1());
		assertEquals(element.item1(), vla2.elements().at(0).item1());
		assertEquals(element.item2(), vla.elements().at(0).item2());
		assertEquals(element.item2(), vla2.elements().at(0).item2());
		
		//test containsVLA
		assertTrue(vla.containsVLA());
		assertTrue(vla2.containsVLA());
		assertFalse(vla.elements().containsVLA());
		assertFalse(vla2.elements().containsVLA());
		assertFalse(vla.elements().at(0).containsVLA());
		assertFalse(vla2.elements().at(0).containsVLA());
	}
	
	@Test
	public void testVLAWithShortRepeatCount() {
		System.out.println("== testVLAWithShortRepeatCount ==");
		
		//create variable length array with repeatCountInitializer test
		VariableLengthArrayWithShortRepeatCount vla = Layout.getLayout(VariableLengthArrayWithShortRepeatCount.class);
		//sizeof(jshort) + sizeof(ArrayElement)*SIZE_OF_VLA1
		ArrayElement element = Layout.getLayout(ArrayElement.class);
		long ptr = unsafe.allocateMemory(1 + element.sizeof() * SIZE_OF_VLA1);
		vla.bindLocation(new Location(ptr), (short) SIZE_OF_VLA1);

		assertEquals((char) SIZE_OF_VLA1, vla.lengthOfArray());
		assertEquals((long) SIZE_OF_VLA1, vla.elements().getVLALength());

		//overlay array on existing memory test
		VariableLengthArrayWithShortRepeatCount vla2 = Layout.getLayout(VariableLengthArrayWithShortRepeatCount.class);
		//sizeof(jshort) + sizeof(ArrayElement)*SIZE_OF_VLA2
		long ptr2 = unsafe.allocateMemory(4 + element.sizeof() * SIZE_OF_VLA2);
		Location loc = new Location(ptr2);
		//set lengthOfArray to 5
		Short lengthOfArray = Layout.getLayout(Short.class);
		lengthOfArray.bindLocation(loc);
		lengthOfArray.value((short) SIZE_OF_VLA2);
		//overlay on existing memory
		vla2.bindLocation(loc);
		
		assertEquals((short) SIZE_OF_VLA2, vla2.lengthOfArray());
		assertEquals((long)SIZE_OF_VLA2, vla2.elements().getVLALength());
		
		//Set array element test
		long ptr3 = unsafe.allocateMemory(element.sizeof());
		element.bindLocation(new Location(ptr3));
		element.item1(10);
		element.item2(20);
		vla.elements().put(0, element);
		vla2.elements().put(0, element);
		
		assertEquals(element.item1(), vla.elements().at(0).item1());
		assertEquals(element.item1(), vla2.elements().at(0).item1());
		assertEquals(element.item2(), vla.elements().at(0).item2());
		assertEquals(element.item2(), vla2.elements().at(0).item2());
		
		//test containsVLA
		assertTrue(vla.containsVLA());
		assertTrue(vla2.containsVLA());
		assertFalse(vla.elements().containsVLA());
		assertFalse(vla2.elements().containsVLA());
		assertFalse(vla.elements().at(0).containsVLA());
		assertFalse(vla2.elements().at(0).containsVLA());
	}
	
	@Test
	public void testVLAWithIntRepeatCount() {
		System.out.println("== testVLAWithIntRepeatCount ==");
		
		//create variable length array with repeatCountInitializer test
		VariableLengthArrayWithIntRepeatCount vla = Layout.getLayout(VariableLengthArrayWithIntRepeatCount.class);
		//sizeof(jint) + sizeof(ArrayElement)*SIZE_OF_VLA1
		ArrayElement element = Layout.getLayout(ArrayElement.class);
		long ptr = unsafe.allocateMemory(1 + element.sizeof() * SIZE_OF_VLA1);
		vla.bindLocation(new Location(ptr), (int) SIZE_OF_VLA1);

		assertEquals((int) SIZE_OF_VLA1, vla.lengthOfArray());
		assertEquals((long) SIZE_OF_VLA1, vla.elements().getVLALength());

		//overlay array on existing memory test
		VariableLengthArrayWithIntRepeatCount vla2 = Layout.getLayout(VariableLengthArrayWithIntRepeatCount.class);
		//sizeof(jint) + sizeof(ArrayElement)*SIZE_OF_VLA2
		long ptr2 = unsafe.allocateMemory(4 + element.sizeof() * SIZE_OF_VLA2);
		Location loc = new Location(ptr2);
		//set lengthOfArray to 5
		Int lengthOfArray = Layout.getLayout(Int.class);
		lengthOfArray.bindLocation(loc);
		lengthOfArray.value((int) SIZE_OF_VLA2);
		//overlay array on existing memory
		vla2.bindLocation(loc);
		
		assertEquals((int) SIZE_OF_VLA2, vla2.lengthOfArray());
		assertEquals((long)SIZE_OF_VLA2, vla2.elements().getVLALength());
		
		//Set array element test
		long ptr3 = unsafe.allocateMemory(element.sizeof());
		element.bindLocation(new Location(ptr3));
		element.item1(10);
		element.item2(20);
		vla.elements().put(0, element);
		vla2.elements().put(0, element);
		
		assertEquals(element.item1(), vla.elements().at(0).item1());
		assertEquals(element.item1(), vla2.elements().at(0).item1());
		assertEquals(element.item2(), vla.elements().at(0).item2());
		assertEquals(element.item2(), vla2.elements().at(0).item2());
		
		//test containsVLA
		assertTrue(vla.containsVLA());
		assertTrue(vla2.containsVLA());
		assertFalse(vla.elements().containsVLA());
		assertFalse(vla2.elements().containsVLA());
		assertFalse(vla.elements().at(0).containsVLA());
		assertFalse(vla2.elements().at(0).containsVLA());
	}
	
	@Test
	public void testVLAWithLongRepeatCount() {
		System.out.println("== testVLAWithLongRepeatCount ==");
		
		//create variable length array with repeatCountInitializer test
		VariableLengthArrayWithLongRepeatCount vla = Layout.getLayout(VariableLengthArrayWithLongRepeatCount.class);
		//sizeof(jlong) + sizeof(ArrayElement)*SIZE_OF_VLA1
		ArrayElement element = Layout.getLayout(ArrayElement.class);
		long ptr = unsafe.allocateMemory(1 + element.sizeof() * SIZE_OF_VLA1);
		vla.bindLocation(new Location(ptr), (long) SIZE_OF_VLA1);

		assertEquals((long) SIZE_OF_VLA1, vla.lengthOfArray());
		assertEquals((long) SIZE_OF_VLA1, vla.elements().getVLALength());

		//overlay array on existing memory test
		VariableLengthArrayWithLongRepeatCount vla2 = Layout.getLayout(VariableLengthArrayWithLongRepeatCount.class);
		//sizeof(jlong) + sizeof(ArrayElement)*SIZE_OF_VLA2
		long ptr2 = unsafe.allocateMemory(4 + element.sizeof() * SIZE_OF_VLA2);
		Location loc = new Location(ptr2);
		//set lengthOfArray to 5
		Long lengthOfArray = Layout.getLayout(Long.class);
		lengthOfArray.bindLocation(loc);
		lengthOfArray.value((long) SIZE_OF_VLA2);
		//overlay array on existing memory
		vla2.bindLocation(loc);
		
		assertEquals((long) SIZE_OF_VLA2, vla2.lengthOfArray());
		assertEquals((long)SIZE_OF_VLA2, vla2.elements().getVLALength());
		
		//Set array element test
		long ptr3 = unsafe.allocateMemory(element.sizeof());
		element.bindLocation(new Location(ptr3));
		element.item1(10);
		element.item2(20);
		vla.elements().put(0, element);
		vla2.elements().put(0, element);
		
		assertEquals(element.item1(), vla.elements().at(0).item1());
		assertEquals(element.item1(), vla2.elements().at(0).item1());
		assertEquals(element.item2(), vla.elements().at(0).item2());
		assertEquals(element.item2(), vla2.elements().at(0).item2());
		
		//test containsVLA
		assertTrue(vla.containsVLA());
		assertTrue(vla2.containsVLA());
		assertFalse(vla.elements().containsVLA());
		assertFalse(vla2.elements().containsVLA());
		assertFalse(vla.elements().at(0).containsVLA());
		assertFalse(vla2.elements().at(0).containsVLA());
	}
	
	@Test
	public void testVLAWithCharRepeatCount() {
		System.out.println("== testVLAWithCharRepeatCount ==");
		
		//create variable length array with repeatCountInitializer test
		VariableLengthArrayWithCharRepeatCount vla = Layout.getLayout(VariableLengthArrayWithCharRepeatCount.class);
		//sizeof(jchar) + sizeof(ArrayElement)*SIZE_OF_VLA1
		ArrayElement element = Layout.getLayout(ArrayElement.class);
		long ptr = unsafe.allocateMemory(1 + element.sizeof() * SIZE_OF_VLA1);
		vla.bindLocation(new Location(ptr), (char) SIZE_OF_VLA1);

		assertEquals((char) SIZE_OF_VLA1, vla.lengthOfArray());
		assertEquals((long) SIZE_OF_VLA1, vla.elements().getVLALength());

		//overlay array on existing memory test
		VariableLengthArrayWithCharRepeatCount vla2 = Layout.getLayout(VariableLengthArrayWithCharRepeatCount.class);
		//sizeof(jchar) + sizeof(ArrayElement)*SIZE_OF_VLA2
		long ptr2 = unsafe.allocateMemory(4 + element.sizeof() * SIZE_OF_VLA2);
		Location loc = new Location(ptr2);
		//set lengthOfArray to 5
		Char lengthOfArray = Layout.getLayout(Char.class);
		lengthOfArray.bindLocation(loc);
		lengthOfArray.value((char) SIZE_OF_VLA2);
		//overlay array on existing memory 
		vla2.bindLocation(loc);
		
		assertEquals((char) SIZE_OF_VLA2, vla2.lengthOfArray());
		assertEquals((long)SIZE_OF_VLA2, vla2.elements().getVLALength());
		
		//Set array element test
		long ptr3 = unsafe.allocateMemory(element.sizeof());
		element.bindLocation(new Location(ptr3));
		element.item1(10);
		element.item2(20);
		vla.elements().put(0, element);
		vla2.elements().put(0, element);
		
		assertEquals(element.item1(), vla.elements().at(0).item1());
		assertEquals(element.item1(), vla2.elements().at(0).item1());
		assertEquals(element.item2(), vla.elements().at(0).item2());
		assertEquals(element.item2(), vla2.elements().at(0).item2());
		
		//test containsVLA
		assertTrue(vla.containsVLA());
		assertTrue(vla2.containsVLA());
		assertFalse(vla.elements().containsVLA());
		assertFalse(vla2.elements().containsVLA());
		assertFalse(vla.elements().at(0).containsVLA());
		assertFalse(vla2.elements().at(0).containsVLA());
	}
	
	@Test
	public void testMultipleVLAInLayout() {
		System.out.println("== testMultipleVLAInLayout ==");
		VLS1 layout = Layout.getLayout(VLS1.class);
		byte[] data = {3,1,2,3,5,1,2,3,4,5,2,1,2};
		layout.bindLocation(new Location(data));
		
		/* test repeatCout */
		assertEquals(layout.b().getVLALength(), layout.a());
		assertEquals(layout.d().getVLALength(), layout.c());
		assertEquals(layout.f().getVLALength(), layout.e());
		
		assertEquals(3, layout.b().getVLALength());
		assertEquals(5, layout.d().getVLALength());
		assertEquals(2, layout.f().getVLALength());
		
		/* Test reads */
		assertEquals(3, layout.b().at(2).value());
		assertEquals(5, layout.d().at(4).value());
		assertEquals(2, layout.f().at(1).value());
		
		/* Test writes */
		Byte writeData = Layout.getLayout(Byte.class);
		writeData.bindLocation(new Location(new byte []{-1}));
		
		layout.b().put(2, writeData);
		layout.d().put(4, writeData);
		layout.f().put(1, writeData);
		
		assertEquals(-1, layout.b().at(2).value());
		assertEquals(-1, layout.d().at(4).value());
		assertEquals(-1, layout.f().at(1).value());
	}
	
	@Test
	public void testMultipleVLAArraysInLayout() {
		System.out.println("== testMultipleVLAArraysInLayout ==");
		VLS3 layout = Layout.getLayout(VLS3.class);
		byte[] data = {3,3,1,2,3,5,1,2,3,4,5,2,1,2,6};
		layout.bindLocation(new Location(data));
		
		/* test repeatCout */
		assertEquals(3, layout.d().getVLALength());
		
		assertEquals(layout.d().at(0).a(), layout.d().at(0).b().getVLALength());
		assertEquals(layout.d().at(1).a(), layout.d().at(1).b().getVLALength());
		assertEquals(layout.d().at(2).a(), layout.d().at(2).b().getVLALength());
		
		assertEquals(3, layout.d().at(0).b().getVLALength());
		assertEquals(5, layout.d().at(1).b().getVLALength());
		assertEquals(2, layout.d().at(2).b().getVLALength());
		
		/* Test reads */
		assertEquals(3, layout.c());
		assertEquals(6, layout.e());
		
		/* Test writes */
		Byte writeData = Layout.getLayout(Byte.class);
		writeData.bindLocation(new Location(new byte []{0,0,0,0,0,0,0,0}));
		writeData.value((byte)-1);
		
		layout.d().at(0).b().put(2, writeData);
		layout.d().at(1).b().put(4, writeData);
		layout.d().at(2).b().put(1, writeData);
		
		assertEquals(-1, layout.d().at(0).b().at(2).value());
		assertEquals(-1, layout.d().at(1).b().at(4).value());
		assertEquals(-1, layout.d().at(2).b().at(1).value());
	}
	
	private byte[] longArrayToByteArray(long[] longArr) {
		byte[] arr = new byte[longArr.length * 8];
		LongArray1D longLayoutArr = LayoutType.getPrimArray1D(long.class, longArr.length);
		longLayoutArr.bindLocation(new Location(arr));

		for(int i = 0; i < longArr.length; i++) {
			longLayoutArr.put(i, longArr[i]);
		}
		
		return arr;
	}
	
	private byte[] intArrayToByteArray(int[] intArr) {
		byte[] arr = new byte[intArr.length * 4];
		IntArray1D intLayoutArr = LayoutType.getPrimArray1D(int.class, intArr.length);
		intLayoutArr.bindLocation(new Location(arr));

		for(int i = 0; i < intArr.length; i++) {
			intLayoutArr.put(i, intArr[i]);
		}
		
		return arr;
	}
	
	@Test
	public void testMultipleLongVLAArraysInLayout() {
		System.out.println("== testMultipleLongVLAArraysInLayout ==");
		VLSLong3 layout = Layout.getLayout(VLSLong3.class);
		long[] data = {3,3,1,2,3,5,1,2,3,4,5,2,1,2,6};
		layout.bindLocation(new Location(longArrayToByteArray(data)));
		
		/* test repeatCout */
		assertEquals(3, layout.d().getVLALength());
		
		assertEquals(layout.d().at(0).a(), layout.d().at(0).b().getVLALength());
		assertEquals(layout.d().at(1).a(), layout.d().at(1).b().getVLALength());
		assertEquals(layout.d().at(2).a(), layout.d().at(2).b().getVLALength());
		
		assertEquals(3, layout.d().at(0).b().getVLALength());
		assertEquals(5, layout.d().at(1).b().getVLALength());
		assertEquals(2, layout.d().at(2).b().getVLALength());
		
		/* Test reads */
		assertEquals(3, layout.c());
		assertEquals(6, layout.e());
		
		/* Test writes */
		Long writeData = Layout.getLayout(Long.class);
		writeData.bindLocation(new Location(new byte [8]));
		writeData.value(-1);
		
		layout.d().at(0).b().put(2, writeData);
		layout.d().at(1).b().put(4, writeData);
		layout.d().at(2).b().put(1, writeData);
		
		assertEquals(-1, layout.d().at(0).b().at(2).value());
		assertEquals(-1, layout.d().at(1).b().at(4).value());
		assertEquals(-1, layout.d().at(2).b().at(1).value());
	}
	
	@Test
	public void testEAInLayout() {
		System.out.println("== testEAInLayout ==");
		VLS1 layout = Layout.getLayout(VLS1.class);
		byte[] data = {3,1,2,3,5,1,2,3,4,5,2,1,2};
		layout.bindLocation(new Location(data));
				
		assertEquals(layout.EA().a().lValue(), layout.a());
		assertEquals(layout.EA().b().lValue().value(), layout.b().at(0).value());
		assertEquals(layout.EA().c().lValue(), layout.c());
		assertEquals(layout.EA().d().lValue().value(), layout.d().at(0).value());
		assertEquals(layout.EA().e().lValue(), layout.e());
		assertEquals(layout.EA().f().lValue().value(), layout.f().at(0).value());
		
		AllPoints ap = Layout.getLayout(AllPoints.class);
		Location loc = new Location(intArrayToByteArray(new int [] {1,2,1,2,3,1,2,3,4,1,2,3,4,5}));
		ap.bindLocation(loc);
		
		assertEquals(ap.a().x(), ap.a().EA().x().lValue());
		assertEquals(ap.a().y(), ap.a().EA().y().lValue());
		assertEquals(ap.b().x(), ap.b().EA().x().lValue());
		assertEquals(ap.b().y(), ap.b().EA().y().lValue());
		assertEquals(ap.b().z(), ap.b().EA().z().lValue());
		assertEquals(ap.c().x(), ap.c().EA().x().lValue());
		assertEquals(ap.c().y(), ap.c().EA().y().lValue());
		assertEquals(ap.c().z(), ap.c().EA().z().lValue());
		assertEquals(ap.c().o(), ap.c().EA().o().lValue());
		assertEquals(ap.d().x(), ap.d().EA().x().lValue());
		assertEquals(ap.d().y(), ap.d().EA().y().lValue());
		assertEquals(ap.d().z(), ap.d().EA().z().lValue());
		assertEquals(ap.d().o(), ap.d().EA().o().lValue());
		assertEquals(ap.d().p(), ap.d().EA().p().lValue());
	}
}
