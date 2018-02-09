/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.HighlightService;
import de.citec.csra.highlight.com.InformerConnection;
import de.citec.csra.highlight.com.MethodCallConnection;

import java.util.EnumMap;
import java.util.Map;

import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.RSBException;
import rst.geometry.SphericalDirectionFloatType.SphericalDirectionFloat;
import rst.hri.HighlightTargetType.HighlightTarget.Modality;

/**
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class Defaults {

    private final static Logger LOGGER = LoggerFactory.getLogger(HighlightService.class);

    private static final Map<TargetObject, Map<Modality, Highlightable>> CONFIGS = new EnumMap<>(TargetObject.class);

    public static void register(TargetObject targetObject, Modality modality, Highlightable highlightable) {
        if (!CONFIGS.containsKey(targetObject)) {
            CONFIGS.put(targetObject, new EnumMap<>(Modality.class));
        }
        CONFIGS.get(targetObject).put(modality, highlightable);
    }

    public static Highlightable get(TargetObject targetObject, Modality modality) {
        if (!CONFIGS.containsKey(targetObject)) {
            CONFIGS.put(targetObject, new EnumMap<>(Modality.class));
        }
        return CONFIGS.get(targetObject).get(modality);
    }

    private static SphericalDirectionFloat direction(long x, long y) {
        return SphericalDirectionFloat.newBuilder().setAzimuth(x).setElevation(y).build();
    }

    public static void loadDefaults() {

        InformerConnection mekaConnection = null;

        try {
            mekaConnection = new InformerConnection<>("/meka/posture_execution/");
        } catch (RSBException ex) {
            ExceptionPrinter.printHistory("Could not load meka connection!", ex, LOGGER);
        }

        MethodCallConnection audioSystemConnection = null;

        try {
            audioSystemConnection = new MethodCallConnection("/home/audio/control/radio/", "play");
        } catch (RSBException ex) {
            ExceptionPrinter.printHistory("Could not load audio system connection!", ex, LOGGER);
        }

        for (Modality modality : Modality.values()) {
            for (TargetObject targetObject : TargetObject.values()) {
                try {
                    String label = "";
                    String gst = "";
                    String snd = "";
                    SphericalDirectionFloat gaze = SphericalDirectionFloat.getDefaultInstance();
                    SphericalDirectionFloat spot = SphericalDirectionFloat.getDefaultInstance();
                    switch (targetObject) {
                        case ENTRANCE:
                            label = "ColorableLight-38";
                            gaze = direction(70, 1);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(84, 33);
                            break;
                        case SURFACE:
                            label = "ColorableLight-11";
                            gaze = direction(-55, -15);
                            gst = "all pointing_screen";
                            snd = "Waikiki.ogg";
                            spot = direction(220, 90);
                            break;
                        case ZEN:
                            label = "ColorableLight-20";
                            gaze = direction(-40, -5);
                            gst = "all pointing_screen";
                            snd = "Waikiki.ogg";
                            spot = direction(280, 63);
                            break;
                        case PLANT:
                            label = "ColorableLight-20";
                            gaze = direction(-35, -10);
                            gst = "all pointing_screen";
                            snd = "Waikiki.ogg";
                            spot = direction(294, 74);
                            break;
                        case FLOBI:
                            label = "ColorableLight-38";
                            gaze = direction(70, 1);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(84, 33);
                            break;
                        case MEKA:
                            label = "ColorableLight-9";
                            gaze = null;
                            gst = "all welcoming";
                            snd = "Waikiki.ogg";
                            spot = direction(140, 60);
                            break;
                        case TV:
                            label = "ColorableLight-27";
                            gaze = direction(-20, 1);
                            gst = "all pointing_screen";
                            snd = "Waikiki.ogg";
                            spot = direction(335, 70);
                            break;
                        case WATER:
                            label = "ColorableLight-24";
                            gaze = direction(70, -10);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(118, 42);
                            break;
                        case CUPBOARD1:
                            label = "ColorableLight-23";
                            gaze = direction(70, 1);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(117, 31);
                            break;
                        case CUPBOARD2:
                            label = "ColorableLight-28";
                            gaze = direction(70, 1);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(109, 28);
                            break;
                        case DRAWER1:
                            label = "ColorableLight-22";
                            gaze = direction(70, -20);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(114, 45);
                            break;
                        case DRAWER2:
                            label = "ColorableLight-2";
                            gaze = direction(70, -20);
                            gst = "all pointing_kitchen";
                            snd = "Waikiki.ogg";
                            spot = direction(114, 45);
                            break;
                        default:

                            break;
                    }

                    switch (modality) {
                        case AMBIENT_LIGHT:
                            register(targetObject, modality, new LightConfiguration(label));
                            break;
                        case GAZE:
                            register(targetObject, modality, new MekaGazeConfiguration(gaze));
                            break;
                        case GESTURE:
                            if (mekaConnection != null) {
                                register(targetObject, modality, new HighlightTarget().setExecution(mekaConnection, gst));
                            }
                            break;
                        case SOUND:
                            if (audioSystemConnection != null) {
                                register(targetObject, modality, new HighlightTarget().setExecution(audioSystemConnection, snd));
                            }
                            break;
                        case SPOT_LIGHT:
                            register(targetObject, modality, new ProjectorConfiguration(spot));
                            break;
                    }
                } catch (final Exception ex) {
                    ExceptionPrinter.printHistory("Could not load default configuration for Target[" + targetObject + "] with Modality[" + modality.name() + "]!", ex, LOGGER);
                }
            }
        }
    }
}
