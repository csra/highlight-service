/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.InformerConnection;

import java.util.Set;

import rsb.RSBException;
import rst.geometry.SphericalDirectionFloatType.SphericalDirectionFloat;

/**
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class MekaGazeConfiguration extends HighlightTarget {

    private static InformerConnection<SphericalDirectionFloat> gaze;
    private static InformerConnection<Boolean> pause;
    private final SphericalDirectionFloat ZERO = SphericalDirectionFloat.newBuilder().setAzimuth(0).setElevation(0).build();
    private final static long DELAY = 100;
    private final static long RESET = 1500;
    private final int instan;

    public MekaGazeConfiguration(SphericalDirectionFloat argument) throws RSBException {
        super.setExecution(getGaze(), argument);
        super.setPrepare(getPause(), true, DELAY);
        super.setReset(getGaze(), ZERO, RESET);
        super.setShutdown(getPause(), false, DELAY);
        instan = i++;
    }

    private static InformerConnection<SphericalDirectionFloat> getGaze() throws RSBException {
        if (gaze == null) {
            gaze = new InformerConnection<>("/meka/robotgaze/set/gaze");
        }
        return gaze;
    }

    private static InformerConnection<Boolean> getPause() throws RSBException {
        if (pause == null) {
            pause = new InformerConnection<>("/meka/robotgaze/set/pause");
        }
        return pause;
    }

    private static int i = 0;

    @Override
    public Set<String> getInterfaces() {
        Set<String> ifaces = super.getInterfaces();
        ifaces.add(instan + "");
        return ifaces;
    }
}
