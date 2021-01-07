/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;

/**
 * @author
 *
 * Use action adapter to invoke methods of specified object.
 */
@SuppressWarnings("serial")
public class ActionAdapter extends AbstractAction {
	
	/**
	 * Object to notify.
	 */
	private Object notifyObject;
	
	/**
	 * Method to invoke.
	 */
	private String methodName;
	
	/**
	 * Method parameters.
	 */
	private Class<?>[] parameters;
	
	/**
	 * Constructor.
	 * @param NotifyObject
	 * @param MethodName
	 */
	ActionAdapter(Object NotifyObject, String MethodName, Class<?>[] Parameters) {
		
		notifyObject = NotifyObject;
		methodName = MethodName;
		parameters = Parameters;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		run();
	}
	
	/**
	 * Run the method.
	 * @return
	 */
	public Object run(Object ... args) {
				
		// Find object's method and invoke it.
		try {
			Method method = notifyObject.getClass().getDeclaredMethod(methodName, parameters);
			method.setAccessible(true);
			return method.invoke(notifyObject, args);
		}
		catch (InvocationTargetException e1) {
				e1.printStackTrace();
				return null;
		}
		catch (Exception e2) {
		}
		
		// Find object's method and invoke it.
		try {
			Method method = notifyObject.getClass().getMethod(methodName, parameters);
			return method.invoke(notifyObject);
			
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (InvocationTargetException e3) {
			e3.printStackTrace();
		} catch (NoSuchMethodException e4) {
			e4.printStackTrace();
		}
		
		return null;
	}
}
