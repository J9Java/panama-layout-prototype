/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
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
import com.ibm.layout.ByteArray1D;
import com.ibm.layout.IntArray2D;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutHelper;
import com.ibm.layout.LayoutType;
import com.ibm.layout.Location;
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
import com.user.types.Ptr;
import com.user.types.Ptr2;
import com.user.types.Short;


public class TestLayout {
	static LayoutHelper f = LayoutHelper.getFactory();
	static Unsafe unsafe = null;

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
	public void testPrimArray() {
		ByteArray1D bb = LayoutType.getPrimArray1D(byte.class, 10);
		Location loc = new Location(new byte[(int)(bb.getLength())]);
		//switching to map APIla
		bb.bindLocation(loc);
		for (long i = 0; i < bb.getLength(); i++) {
			bb.put(i, (byte)(i + 3));
		}

		for (long i = 0; i < bb.getLength(); i++) {
			assertTrue(bb.at(i) == (byte)(i + 3));
		}

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
		// @todo-alin IllegalAccessError because the Impl class is loaded in different classloader
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
	public void testPtr() {
		Ptr ptr = Layout.getLayout(Ptr.class);
		assertNotNull(ptr);
	}

	@Test
	public void test1DArrayPtr() {
		Array1D<Ptr> ptr = Array1D.getArray1D(Ptr.class, 5);
		Location loc = new Location(new byte[(int)(ptr.getLength() * 0)]);
		ptr.bindLocation(loc);
	}

	@Test
	public void test2DArrayPtr() {
		Array2D<Ptr> ptr = Array2D.getArray2D(Ptr.class, 2, 2);
		Location loc = new Location(new byte[(int)(ptr.dim1() * ptr.dim2() * 0)]);
		ptr.bindLocation(loc);
	}

	@Test
	public void testPtr2() {
		System.out.println("== testPtr2 ==");
		Ptr2 ptr2 = Layout.getLayout(Ptr2.class);
		Location loc = new Location(new byte[(int) (4 * 4)]);
		ptr2.bindLocation(loc);
		ptr2.z(5);
		ptr2.z();
		System.out.println(ptr2);
	}

	@Test
	public void test1DArrayPtr2() {
		System.out.println("== test1DArrayPtr2 ==");
		Array1D<Ptr2> ptr2 = Array1D.getArray1D(Ptr2.class, 5);
		Location loc = new Location(new byte[(int)(ptr2.getLength() * 4)]);
		ptr2.bindLocation(loc);
		for (int i = 0; i < ptr2.getLength(); i++) {
			ptr2.at(i).z(i);
		}
		for (int i = 0; i < ptr2.getLength(); i++) {
			assertEquals(i, ptr2.at(i).z());
		}
		System.out.println(ptr2);
	}

	@Test
	public void test2DArrayPtr2() {
		System.out.println("== test2DArrayPtr2 ==");
		Array2D<Ptr2> ptr2 = Array2D.getArray2D(Ptr2.class, 3, 3);
		Location loc = new Location(new byte[(int)(ptr2.dim1() * ptr2.dim2() * 4)]);
		ptr2.bindLocation(loc);
		for (int i = 0; i < ptr2.dim1(); i++) {
			for (int j = 0; j < ptr2.dim2(); j++) {
				ptr2.at(i, j).z(i + j);
			}
		}
		for (int i = 0; i < ptr2.dim1(); i++) {
			for (int j = 0; j < ptr2.dim2(); j++) {
				assertEquals(i + j, ptr2.at(i, j).z());
				System.out.print(ptr2.at(i, j) + " ");
			}
			System.out.println();
		}
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

}
