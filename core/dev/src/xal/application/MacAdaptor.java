//
// MacAdaptor.java: Source file for 'MacAdaptor'
// Project xal
//
// Created by t6p on 3/23/2010
//

package xal.application;

import java.lang.reflect.*;


/** MacAdaptor provides Mac OS X specific support using reflection so it only gets loaded for Mac OS X */
public class MacAdaptor {
	/** Constructor */
    private MacAdaptor() {}
	
	
	/** perform Mac initialization */
	static void initialize() {
		// display the menu bar at the top of the screen consistent with the Mac look and feel
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		
		try {
			// dynamically get the Mac specific extensions
			final Class macApplicationClass = Class.forName( "com.apple.eawt.Application" );
			final Class macEventListenerClass = Class.forName( "com.apple.eawt.ApplicationListener" );
			
			// get the Mac application instance for our application
			final Method appMethod = macApplicationClass.getMethod( "getApplication" );
			final Object macApplication = appMethod.invoke( null );
			
			// register our handler to handle Mac events from com.apple.eawt.ApplicationListener
			final Object proxy = Proxy.newProxyInstance( MacAdaptor.class.getClassLoader(), new Class[] { macEventListenerClass }, new MacEventHandler() );
			final Method registrationMethod = macApplicationClass.getMethod( "addApplicationListener", new Class[] { macEventListenerClass } );
			registrationMethod.invoke( macApplication, proxy );
		}
		catch ( ClassNotFoundException exception ) {
			exception.printStackTrace();
		}
		catch ( NoSuchMethodException exception ) {
			exception.printStackTrace();
		}
		catch ( IllegalAccessException exception ) {
			exception.printStackTrace();
		}
		catch ( IllegalArgumentException exception ) {
			exception.printStackTrace();
		}
		catch ( InvocationTargetException exception ) {
			exception.printStackTrace();
		}
	}
	
	
	
	/** handle Mac Events (quit and show about box) ignoring other events */
	static class MacEventHandler implements InvocationHandler {
		public Object invoke( final Object proxy, final Method method, final Object[] args ) {
			try {
				final String methodName = method.getName();
				final Object event = args[0];
				final Method markMethod = event.getClass().getMethod( "setHandled", new Class[] { Boolean.TYPE } );		// method to indicate whether we handled the event
				
				// get the XAL application
				final xal.application.Application xalApp = xal.application.Application.getApp();
				
				if ( methodName.equals( "handleQuit" ) ) {	// attempt to quit the application using the default XAL behavior
					xalApp.quit();
					markMethod.invoke( event, false );		// if we get to this point then we haven't quit the application
				}
				else if ( methodName.equals( "handleAbout" ) ) {	// display the about box
					xalApp.showAboutBox();
					markMethod.invoke( event, true );
				}
				else {
					markMethod.invoke( event, false );		// no other events are handled
				}
			}
			catch ( NoSuchMethodException exception ) {
				exception.printStackTrace();
			}
			catch ( IllegalAccessException exception ) {
				exception.printStackTrace();
			}
			catch ( IllegalArgumentException exception ) {
				exception.printStackTrace();
			}
			catch ( InvocationTargetException exception ) {
				exception.printStackTrace();
			}
			finally {
				return null;
			}
		}
	}
}

