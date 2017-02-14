package com.leap12.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Reflex {

	/**
	 * Get all declared methods for the given type "startType" and its supertypes until "stopType" is reached.<br>
	 * This will find methods even if they are private.
	 * 
	 * @param startType inclusive
	 * @param stopType exclusive
	 * @return
	 * @throws Exception
	 */
	public static List<Method> getAllMethods( Class<?> startType, Class<?> stopType ) throws Exception {
		Queue<Class<?>> queue = new LinkedList<>();
		queue.add( startType );
		List<Method> allMethods = new ArrayList<>();

		Class<?> node;
		while ( null != ( node = queue.poll() ) ) {
			allMethods.addAll( Arrays.asList( node.getDeclaredMethods() ) );
			Class<?> superclass = node.getSuperclass();
			if ( !superclass.equals( stopType ) ) {
				queue.add( node.getSuperclass() );
			}
		}

		return allMethods;
	}


	/**
	 * Get all declared fields for the given type "startType" and its supertypes until "stopType" is reached.<br>
	 * This will find fields even if they are private.
	 * 
	 * @param startType inclusive
	 * @param stopType exclusive
	 * @return
	 * @throws Exception
	 */
	public static List<Field> getAllFields( Class<?> startType, Class<?> stopType ) throws Exception {
		Queue<Class<?>> queue = new LinkedList<>();
		queue.add( startType );

		List<Field> allFields = new ArrayList<>();

		Class<?> node;
		while ( null != ( node = queue.poll() ) ) {
			allFields.addAll( Arrays.asList( node.getDeclaredFields() ) );
			Class<?> superclass = node.getSuperclass();
			if ( !superclass.equals( stopType ) ) {
				queue.add( node.getSuperclass() );
			}
		}

		return allFields;
	}

}
