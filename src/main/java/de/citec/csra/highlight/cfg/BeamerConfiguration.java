/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.MethodCallConnection;
import rsb.RSBException;
import rst.geometry.SphericalDirectionFloatType.SphericalDirectionFloat;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class BeamerConfiguration extends HighlightTarget {

	private static final long DELAY = 100;
	private static MethodCallConnection<SphericalDirectionFloat> panTilt;
	private static MethodCallConnection<Boolean> shutterLamp;
	private static MethodCallConnection<Boolean> shutter;

	public BeamerConfiguration(SphericalDirectionFloat argument) throws RSBException {
		super.setExecution(getPanTilt(), argument);
		super.setPrepare(getShutterAndLamp(), true, DELAY);
		super.setReset(getPanTilt(), SphericalDirectionFloat.newBuilder().setAzimuth(180).setElevation(40).build(), DELAY);
		super.setShutdown(getShutter(), false, DELAY);
	}

	private MethodCallConnection<SphericalDirectionFloat> getPanTilt() throws RSBException {
		if (panTilt == null) {
			panTilt = new MethodCallConnection<>("/home/living/movinghead", "setPanTilt");
		}
		return panTilt;
	}

	private MethodCallConnection<Boolean> getShutterAndLamp() throws RSBException {
		if (shutterLamp == null) {
			shutterLamp = new MethodCallConnection<>("/home/living/movinghead", "setShutterAndLampState");
		}
		return shutterLamp;
	}

	private MethodCallConnection<Boolean> getShutter() throws RSBException {
		if (shutter == null) {
			shutter = new MethodCallConnection<>("/home/living/movinghead", "setShutterState");
		}
		return shutter;
	}
}
