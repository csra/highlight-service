/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.MethodCallConnection;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import rsb.RSBException;
import rst.geometry.SphericalDirectionFloatType.SphericalDirectionFloat;
import rst.geometry.TranslationType.Translation;

/**
 * @author Patrick Holthaus(<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProjectorConfiguration extends HighlightTarget {

    private static final long DELAY = 100;
    private static final SphericalDirectionFloat PARKING_POSITION = SphericalDirectionFloat.newBuilder().setAzimuth(180).setElevation(40).build();

    private static MethodCallConnection<Translation> lookAt;
    private static MethodCallConnection<SphericalDirectionFloat> panTilt;
    private static MethodCallConnection<Boolean> shutterLamp;
    private static MethodCallConnection<Boolean> shutter;
    private static MethodCallConnection<Boolean> parking;

    public ProjectorConfiguration(final Translation translation) throws InstantiationException {
        this();
        try {
            super.setExecution(getLookAt(), translation);
        } catch (RSBException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public ProjectorConfiguration(final SphericalDirectionFloat panTilt) throws InstantiationException {
        this();
        try {
            super.setExecution(getPanTilt(), panTilt);
        } catch (RSBException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    private ProjectorConfiguration() throws InstantiationException {
        try {
            super.setPrepare(getShutterAndLamp(), true, DELAY);
            super.setReset(getPanTilt(), PARKING_POSITION, DELAY);
            super.setShutdown(getShutter(), false, DELAY);
        } catch (RSBException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    private MethodCallConnection<Translation> getLookAt() throws RSBException {
        if (lookAt == null) {
            lookAt = new MethodCallConnection<>("/home/living/movinghead", "lookAt");
        }
        return lookAt;
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

    private MethodCallConnection<Boolean> setParkingPosition() throws RSBException {
        if (parking == null) {
            parking = new MethodCallConnection<>("/home/living/movinghead", "setParkingPosition");
        }
        return parking;
    }
}
