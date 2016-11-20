/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import com.ibm.layout.ImplHelper.TypeInfo;

import sun.misc.Unsafe;

/**
 * LayoutFactory Implemented by LayoutHelper
 * 
 * <p>
 * Types of layouts are:<br>
 * - singleton<br>
 * - (1|2)-dim primitive array<br>
 * - (1|2)-dim layout array<br>
 * </p>
 * n-dim array layouts and their corresponding factory methods might be generated in the future.
 */
public final class LayoutHelper {
	static private final LayoutHelper inst = new LayoutHelper();
	static private final Unsafe unsafe = UnsafeHelper.getUnsafe();

	private LayoutHelper() {
	}

	/**
	 * Access the layout factory.
	 * @return a layout factory implemented by LayoutHelper
	 */
	public static LayoutHelper getFactory() {
		return inst;
	}

	class ImplClassLoader extends ClassLoader {
		ImplClassLoader() {
			super(ClassLoader.getSystemClassLoader());
			try {
				loadPrimArrayClass("com.ibm.layout.gen.UnsafeImplHelper");
			} catch (Exception e) {
				throw new RuntimeException("could not load UnsafeImplHelper");
			}
		}

		@SuppressWarnings("unchecked")
		Class<?> loadLayoutClass(Class<? extends Layout> interfaceClass) throws Exception {
			Class<?> implClass = findLoadedClass(getImplClassName(interfaceClass));
			if (null != implClass) {
				return implClass;
			}

			TypeInfo[] requiredClasses = ImplHelper.getRequiredClasses(interfaceClass);
			for (TypeInfo type : requiredClasses) {
				Type t = type.type;
				if (t instanceof ParameterizedType) {
					Type actualType = ((ParameterizedType)t).getActualTypeArguments()[0];
					if (LayoutType.class.isAssignableFrom((Class<?>) actualType)) {
						if (((ParameterizedType)t).getRawType().toString().contains("1D")) {
							inst.genArray1DImpl((Class<? extends Layout>)actualType);
						} else if (((ParameterizedType)t).getRawType().toString().contains("2D")) {
							inst.genArray2DImpl((Class<? extends Layout>)actualType);
						} else if (((ParameterizedType)t).getRawType().toString().contains("VLA")) {
							inst.genVLAImpl((Class<? extends Layout>)actualType, (Class<? extends Layout>)interfaceClass);
						} else if (ComplexVLArray.class.isAssignableFrom((Class<?>) ((ParameterizedType)t).getRawType())) {
							inst.genVLAWithUserClassImpl((Class<? extends Layout>)actualType, (Class<? extends Layout>)interfaceClass, t, type);
						} else if (PriviledgedVLArray.class.isAssignableFrom((Class<?>) ((ParameterizedType)t).getRawType())) {
							inst.genVLAWithUserClassImpl((Class<? extends Layout>)actualType, (Class<? extends Layout>)interfaceClass, t, type);
						}
					}
				} else {
					if (LayoutType.class.isAssignableFrom((Class<?>) t)) {
						if (((Class<? extends Layout>)t).getSimpleName().contains("1D") | ((Class<? extends Layout>)t).getSimpleName().contains("2D")) {
							inst.genPrimArrayImpl("com.ibm.layout.gen." + ((Class<? extends Layout>)t).getSimpleName() + "Impl");
						} else {
							inst.genLayoutImpl((Class<? extends Layout>)t);
						}
					}
				} 
			}
			
			GenLayout loader = new GenLayout(interfaceClass);
			byte[] bytes = loader.genBytecode();
			implClass = defineClass(null, bytes, 0, bytes.length);
			bytes = loader.genEABytecode();
			if (null != bytes) {
				defineClass(null, bytes, 0, bytes.length);
			}
			return implClass;
		}

		@SuppressWarnings("unchecked")
		Class<? extends LayoutType> loadPrimArrayClass(String className) throws Exception {
			if (className.contains("com.ibm.layout.gen.")) {
				Class<?> implClass = findLoadedClass(className);
				if (null != implClass) {
					return (Class<? extends LayoutType>) implClass;
				}
				
				String clsFile = ClassLoader.getSystemResource(className.replace('.', '/') + ".class").getFile();

				File file = new File(clsFile);
				int size = (int)file.length();
				byte classBytes[] = new byte[size];
				FileInputStream fis = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(fis);
				dis.readFully(classBytes);
				dis.close();
				implClass = defineClass(null, classBytes, 0, classBytes.length);
				return (Class<? extends LayoutType>) implClass;

			} else {
				return (Class<? extends LayoutType>) super.loadClass(className);
			}
		}

		@SuppressWarnings("unchecked")
		<E extends Layout, AE extends Array1D<E>> 
		Class<AE> load1DClass(Class<E> elementInterfaceClass, Class<AE> userDefinedArrayClass) throws Exception
		{
			String arrayInterfaceClassName;
			
			//Load element class
			@SuppressWarnings("unused")
			Class<E> elementCls = genLayoutImpl(elementInterfaceClass);
			
			if (null == userDefinedArrayClass) {
				/* append "1DImpl" to the element class name */
				arrayInterfaceClassName = get1DImplClassName(elementInterfaceClass);
			} else {
				/* append "Impl" to the user-defined class name */
				arrayInterfaceClassName = getImplClassName(userDefinedArrayClass);
			}
			
			Class<AE> implClass = (Class<AE>)findLoadedClass(arrayInterfaceClassName);
			if (null == implClass) {
				GenArray1D generator = new GenArray1D(elementInterfaceClass, userDefinedArrayClass);
				byte[] bytes = generator.genBytecode();
				implClass = (Class<AE>)defineClass(null, bytes, 0, bytes.length);
			}
			return implClass;
		}
		
		@SuppressWarnings("unchecked")
		<E extends Layout, AE extends Pointer<E>> 
		Class<AE> loadPointerClass(Class<E> elementInterfaceClass, Class<AE> userDefinedPointerClass) throws Exception
		{
			String arrayInterfaceClassName;
						
			if (null == userDefinedPointerClass) {
				/* append "1DImpl" to the element class name */
				arrayInterfaceClassName = getPointerImplClassName(elementInterfaceClass);
			} else {
				/* append "Impl" to the user-defined class name */
				arrayInterfaceClassName = getPointerImplClassName(userDefinedPointerClass);
			}
			
			Class<AE> implClass = (Class<AE>)findLoadedClass(arrayInterfaceClassName);
			if (null == implClass) {
				GenPointer generator = new GenPointer(elementInterfaceClass, userDefinedPointerClass);
				byte[] bytes = generator.genBytecode();
				implClass = (Class<AE>)defineClass(null, bytes, 0, bytes.length);
			}
			return implClass;
		}
		

		@SuppressWarnings("unchecked")
		<E extends Layout, AE extends VLArray<E>, EE extends Layout> 
		Class<AE> loadVLAClass(Class<E> elementInterfaceClass, Class<AE> userDefinedArrayClass, Class<EE> enclosingClass, TypeInfo typeInfo) throws Exception
		{
			String arrayInterfaceClassName;
			//Load element class
			@SuppressWarnings("unused")
			Class<E> elementCls = genLayoutImpl(elementInterfaceClass);
			
			if (null == userDefinedArrayClass) {
				arrayInterfaceClassName = getVLAImplClassName(elementInterfaceClass);
			} else {
				arrayInterfaceClassName = getVLAImplClassName(userDefinedArrayClass, typeInfo.repeatCountMember, enclosingClass);
			}
			
			Class<AE> implClass = (Class<AE>)findLoadedClass(arrayInterfaceClassName);
			if (null == implClass) {
				GenVLArray generator = new GenVLArray(elementInterfaceClass, userDefinedArrayClass, typeInfo);
				byte[] bytes = generator.genBytecode();
				implClass = (Class<AE>)defineClass(null, bytes, 0, bytes.length);
			}
			return implClass;
		}
		
		@SuppressWarnings("unchecked")
		<AE extends LayoutType> 
		Class<AE> loadPrim1DClass(Class<?> elementInterfaceClass, Class<AE> userDefinedArrayClass) throws Exception
		{
			String arrayInterfaceClassName;
			
			/* append "Impl" to the user-defined class name */
			arrayInterfaceClassName = getImplClassName(userDefinedArrayClass);
			
			Class<AE> implClass = (Class<AE>)findLoadedClass(arrayInterfaceClassName);
			if (null == implClass) {
				GenPrimArray1D generator = new GenPrimArray1D(elementInterfaceClass, userDefinedArrayClass);
				byte[]	bytes = generator.genBytecode();
				implClass = (Class<AE>)defineClass(null, bytes, 0, bytes.length);
			}
			
			return implClass;
		}
		
		Class<?> load2DClass(Class<? extends Layout> elementInterfaceClass) throws Exception {
			Class<?> implClass = findLoadedClass(get2DImplClassName(elementInterfaceClass));
			if (null == implClass) {
				GenArray2D generator = new GenArray2D(elementInterfaceClass);
				byte[] bytes = generator.genBytecode();
				implClass = defineClass(null, bytes, 0, bytes.length);
			}
			return implClass;
		}
	}

	private ImplClassLoader implClassloader = new ImplClassLoader();

	static String getVLAImplClassName(Class<? extends LayoutType> cls, String repeatCountName, Class<? extends Layout> enclosingClass) {
		return getVLAImplClassName(cls.getSimpleName(), repeatCountName, enclosingClass.getSimpleName());
	}
	
	static String getVLAImplClassName(Class<? extends LayoutType> cls) {
		return "com.ibm.layout.gen." + cls.getSimpleName() + "VLArrayImpl";
	}
	
	static String getVLAImplClassName(String cls, String repeatCountName, String enclosingClass) {
		return "com.ibm.layout.gen." + cls + repeatCountName + enclosingClass + "VLArrayImpl";
	}
	
	static String getImplClassName(Class<? extends LayoutType> cls) {
		return "com.ibm.layout.gen." + cls.getSimpleName() + "Impl";
	}

	static String get1DImplClassName(Class<? extends LayoutType> cls) {
		return "com.ibm.layout.gen." + cls.getSimpleName() + "1DImpl";
	}
	
	static String getPointerImplClassName(Class<? extends LayoutType> cls) {
		return "com.ibm.layout.gen.Layout" + cls.getSimpleName() + "PointerImpl";
	}

	static String get2DImplClassName(Class<? extends LayoutType> cls) {
		return "com.ibm.layout.gen." + cls.getSimpleName() + "2DImpl";
	}

	static String getPrimArray1DName(Class<?> primCls) {
		String implClsName = "com.ibm.layout.gen.";

		if (primCls == byte.class) {
			implClsName += "ByteArray1DImpl";
		} else if (primCls == boolean.class) {
			implClsName += "BooleanArray1DImpl";
		} else if (primCls == short.class) {
			implClsName += "ShortArray1DImpl";
		} else if (primCls == char.class) {
			implClsName += "CharArray1DImpl";
		} else if (primCls == int.class) {
			implClsName += "IntArray1DImpl";
		} else if (primCls == long.class) {
			implClsName += "LongArray1DImpl";
		} else if (primCls == float.class) {
			implClsName += "FloatArray1DImpl";
		} else if (primCls == double.class) {
			implClsName += "DoubleArray1DImpl";
		} else {
			implClsName = null;
		}
		return implClsName;
	}

	static String getPrimArray2DName(Class<?> primCls) {
		String implClsName = "com.ibm.layout.gen.";

		if (primCls == byte.class) {
			implClsName += "ByteArray2DImpl";
		} else if (primCls == boolean.class) {
			implClsName += "BooleanArray2DImpl";
		} else if (primCls == short.class) {
			implClsName += "ShortArray2DImpl";
		} else if (primCls == char.class) {
			implClsName += "CharArray2DImpl";
		} else if (primCls == int.class) {
			implClsName += "IntArray2DImpl";
		} else if (primCls == long.class) {
			implClsName += "LongArray2DImpl";
		} else if (primCls == float.class) {
			implClsName += "FloatArray2DImpl";
		} else if (primCls == double.class) {
			implClsName += "DoubleArray2DImpl";
		} else {
			implClsName = null;
		}
		return implClsName;
	}
	
	static String getPrimPointerName(Class<?> primCls) {
		String implClsName = "com.ibm.layout.gen.";

		if (primCls == byte.class) {
			implClsName += "BytePointerImpl";
		} else if (primCls == boolean.class) {
			implClsName += "BooleanPointerImpl";
		} else if (primCls == short.class) {
			implClsName += "ShortPointerImpl";
		} else if (primCls == char.class) {
			implClsName += "CharPointerImpl";
		} else if (primCls == int.class) {
			implClsName += "IntPointerImpl";
		} else if (primCls == long.class) {
			implClsName += "LongPointerImpl";
		} else if (primCls == float.class) {
			implClsName += "FloatPointerImpl";
		} else if (primCls == double.class) {
			implClsName += "DoublePointerImpl";
		} else {
			implClsName = null;
		}
		return implClsName;
	}
	
	/**
	 * Create a singleton layout class
	 * @param interfaceCls The layout class
	 * @param <T> subclass of Layout
	 * @return a layout instance
	 */
	public <T extends Layout> Class<T> genLayoutImpl(final Class<T> interfaceCls) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> implCls = (Class<T>)implClassloader.loadLayoutClass(interfaceCls);
			
			unsafe.ensureClassInitialized(implCls);
			
			Field f = implCls.getDeclaredField("unsafe");
			unsafe.putObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), unsafe);
			
			return implCls;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create an array class of java primitive type 
	 * 
	 * @param <T> subclass of Layout
	 * @param className, the name of the built-in java primitive layout array
	 * @return class of a primitive array
	 */
	public <T extends LayoutType> Class<T> genPrimArrayImpl(final String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> implCls = (Class<T>)implClassloader.loadPrimArrayClass(className);
			unsafe.ensureClassInitialized(implCls);
			
			return implCls;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create a 1D array class
	 * 
	 * @param <AE> subclass of Layout
	 * @param <E> element type
	 * @param elementInterfaceClass, the element type of the array
	 * @return 1D array class
	 */
	public <E extends Layout, AE extends Array1D<E>> Class<AE> genArray1DImpl(final Class<E> elementInterfaceClass) {
		return genArray1DImpl(elementInterfaceClass, null);
	}

	/**
	 * Create a 1D array class from user defined Layout
	 * 
	 * @param <AE> subclass of Layout
	 * @param <E> element type
	 * @param elementInterfaceClass, the element type of the array
	 * @param userDefinedArrayClass, user defined class
	 * @return class
	 */
	public <E extends Layout, AE extends Array1D<E>> Class<AE> genArray1DImpl(final Class<E> elementInterfaceClass,
		final Class<AE> userDefinedArrayClass)
	{
		try {
			Class<AE> implCls = implClassloader.load1DClass(elementInterfaceClass, userDefinedArrayClass);
			unsafe.ensureClassInitialized(implCls);
			
			Field f = implCls.getDeclaredField("unsafe");
			unsafe.putObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), unsafe);
			
			return implCls;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create a Pointer class
	 * 
	 * @param elementInterfaceClass, the lvalue type of the Pointer
	 * @return Pointer class
	 */
	public <E extends Layout, AE extends Pointer<E>> Class<AE> genPointer(final Class<E> elementInterfaceClass) {
		return genPointer(elementInterfaceClass, null);
	}

	/**
	 * Create a Pointer class
	 * @param userDefinedArrayClass, user defined class
	 * @param elementInterfaceClass, the lvalue type of the Pointer
	 * @return Pointer class
	 */
	public <E extends Layout, AE extends Pointer<E>> Class<AE> genPointer(final Class<E> elementInterfaceClass,
		final Class<AE> userDefinedArrayClass)
	{
		try {
			Class<AE> implCls = implClassloader.loadPointerClass(elementInterfaceClass, userDefinedArrayClass);
			unsafe.ensureClassInitialized(implCls);
			
			return implCls;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create a VLA class from user defined Layout
	 * 
	 * @param <AE> subclass of Layout
	 * @param <E> element type
	 * @param elementInterfaceClass, the element type of the array
	 * @param userDefinedArrayClass, user defined class
	 * @return class
	 */
	public <E extends Layout, AE extends VLArray<E>, EE extends Layout> Class<AE> genVLAImpl(final Class<E> elementInterfaceClass,
		final Class<AE> userDefinedArrayClass, final Class<EE> enclosingClass, final TypeInfo typeInfo)
	{
		try {
			Class<AE> implCls = implClassloader.loadVLAClass(elementInterfaceClass, userDefinedArrayClass, enclosingClass, typeInfo);
			unsafe.ensureClassInitialized(implCls);
			
			Field f = implCls.getDeclaredField("unsafe");
			unsafe.putObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), unsafe);
			
			return implCls;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create a VLA class
	 * 
	 * @param <AE> subclass of Layout
	 * @param <E> element type
	 * @param elementInterfaceClass, the element type of the array
	 * @param enclosingClass, the layout class that contains the VLA
	 * @return variable sized array class
	 */
	public <E extends Layout, AE extends VLArray<E>, EE extends Layout> Class<AE> genVLAImpl(final Class<E> elementInterfaceClass, final Class<EE> enclosingClass) {
		return genVLAImpl(elementInterfaceClass, null, enclosingClass, null);
	}

	/**
	 * Create a VLA class
	 * 
	 * @param <AE> subclass of Layout
	 * @param <E> element type
	 * @param elementInterfaceClass, the element type of the array
	 * @param enclosingClass, the layout class that contains the VLA
	 * @param returnType, return type for the VLA
	 * @return variable sized array class
	 */
	public <E extends Layout, AE extends VLArray<E>, EE extends Layout> Class<AE> genVLAWithUserClassImpl(final Class<E> elementInterfaceClass, final Class<EE> enclosingClass, Type returnType, TypeInfo typeInfo) {
		@SuppressWarnings("unchecked")
		Class<AE> userClass = (Class<AE>) ((ParameterizedType) returnType).getRawType();
		
		return genVLAImpl(elementInterfaceClass, userClass, enclosingClass, typeInfo);
	}
	
	/**
	 * Create an array class of java primitive type from user defined layout
	 * 
	 * @param <AE> subclass of LayoutType
	 * @param elementInterfaceClass, the element type of the array
	 * @param userDefinedArrayClass, the user defined layout
	 * @return class of a primitive array
	 */
	public <AE extends LayoutType> Class<AE> genPrimUserArray1DImpl(final Class<?> elementInterfaceClass,
			final Class<AE> userDefinedArrayClass)
		{
			try {				
				Class<AE> implCls = implClassloader.loadPrim1DClass(elementInterfaceClass, userDefinedArrayClass);
				unsafe.ensureClassInitialized(implCls);
				
				Field f = implCls.getDeclaredField("unsafe");
				unsafe.putObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), unsafe);
				
				return implCls;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	
	/**
	 * Create a 2D array class
	 * 
	 * @param <T> subclass of Layout
	 * @param elementInterfaceClass, the element type of the array
	 * @return 2D array class
	 */
	public <T extends Layout> Class<Array2D<T>> genArray2DImpl(final Class<T> elementInterfaceClass) {
		try {
			@SuppressWarnings("unused")
			Class<Array1D<T>> array1Dcls = genArray1DImpl(elementInterfaceClass);
			
			@SuppressWarnings("unchecked")
			Class<Array2D<T>> implCls = (Class<Array2D<T>>)implClassloader.load2DClass(elementInterfaceClass);
			unsafe.ensureClassInitialized(implCls);
			Field f = implCls.getDeclaredField("unsafe");
			unsafe.putObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), unsafe);

			return implCls;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a layout to access a direct byte buffer.
	 * @param buffer a direct byte buffer
	 * @return a ByteArray1D layout
	 */
	public ByteArray1D fromByteBuffer(ByteBuffer buffer) {
		if (!buffer.isDirect()) {
			throw new UnsupportedOperationException("not direct");
		}

		ByteArray1D b = (ByteArray1D) LayoutType.getPrimArray1D(byte.class, UnsafeHelper.getDirectByteBufferLength(buffer));
		Location loc = new Location(UnsafeHelper.getDirectByteBufferAddress(buffer));
		b.bindLocation(loc);
		return b;
	}
}
