/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.InformerConnection;
import de.citec.csra.highlight.com.MethodCallConnection;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import rsb.InitializeException;
import rsb.RSBException;
import rst.geometry.SphericalDirectionFloatType.SphericalDirectionFloat;
import rst.hri.HighlightTargetType.HighlightTarget.Modality;

/**
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class Defaults {

    private static final Map<TargetObject, Map<Modality, Highlightable>> CONFIGS = new EnumMap<>(TargetObject.class);

    public static void register(TargetObject tgt, Modality modality, Highlightable conf) {
        if (!CONFIGS.containsKey(tgt)) {
            CONFIGS.put(tgt, new EnumMap<>(Modality.class));
        }
        CONFIGS.get(tgt).put(modality, conf);
    }

    public static Highlightable get(TargetObject tgt, Modality m) {
        if (!CONFIGS.containsKey(tgt)) {
            CONFIGS.put(tgt, new EnumMap<>(Modality.class));
        }
        return CONFIGS.get(tgt).get(m);
    }

    private static SphericalDirectionFloat direction(long x, long y) {
        return SphericalDirectionFloat.newBuilder().setAzimuth(x).setElevation(y).build();
    }

	public static void loadDefaults() {

        InformerConnection gesture = null;
        try {
            gesture = new InformerConnection<>("/meka/posture_execution/");
        } catch (RSBException ex) {
            Logger.getLogger(Defaults.class.getName()).log(Level.SEVERE, null, ex);
        }
        MethodCallConnection sound = null;
        try {
            sound = new MethodCallConnection("/home/audio/control/radio/", "play");
        } catch (RSBException ex) {
            Logger.getLogger(Defaults.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Modality modality : Modality.values()) {
            for (TargetObject targetObject : TargetObject.values()) {
                String label = "";
                String gst = "";
                String snd = "";
                SphericalDirectionFloat gaze = SphericalDirectionFloat.getDefaultInstance();
                SphericalDirectionFloat spot = SphericalDirectionFloat.getDefaultInstance();
                switch (targetObject) {
                    case ENTRANCE:
                        label = "Hallway_0";
                        gaze = direction(70, 1);
                        gst = "all pointing_kitchen";
                        snd = "Waikiki.ogg";
                        spot = direction(84, 33);
                        break;
                    case SURFACE:
                        label = "CeilingWindowLamp";
                        gaze = direction(-55, -15);
                        gst = "all pointing_screen";
                        snd = "Waikiki.ogg";
                        spot = direction(220, 90);
                        break;
                    case ZEN:
                        label = "SLampRight2";
                        gaze = direction(-40, -5);
                        gst = "all pointing_screen";
                        snd = "Waikiki.ogg";
                        spot = direction(280, 63);
                        break;
                    case PLANT:
                        label = "SLampRight2";
                        gaze = direction(-35, -10);
                        gst = "all pointing_screen";
                        snd = "Waikiki.ogg";
                        spot = direction(294, 74);
                        break;
                    case FLOBI:
                        label = "Hallway_0";
                        gaze = direction(70, 1);
                        gst = "all pointing_kitchen";
                        snd = "Waikiki.ogg";
                        spot = direction(84, 33);
                        break;
                    case MEKA:
                        label = "Table_0";
                        gaze = null;
                        gst = "all welcoming";
                        snd = "Waikiki.ogg";
                        spot = direction(140, 60);
                        break;
                    case TV:
                        label = "LLamp6";
                        gaze = direction(-20, 1);
                        gst = "all pointing_screen";
                        snd = "Waikiki.ogg";
                        spot = direction(335, 70);
                        break;
                    case WATER:
                        label = "CeilingLamp 1";
                        gaze = direction(70, -10);
                        gst = "all pointing_kitchen";
                        snd = "Waikiki.ogg";
                        spot = direction(118, 42);
                        break;
                    case CUPBOARD1:
                        label = "503";
                        gaze = direction(70, 1);
                        gst = "all pointing_kitchen";
                        snd = "Waikiki.ogg";
                        spot = direction(117, 31);
                        break;
                    case CUPBOARD2:
                        label = "505";
                        gaze = direction(70, 1);
                        gst = "all pointing_kitchen";
                        snd = "Waikiki.ogg";
                        spot = direction(109, 28);
                        break;
                    case DRAWER1:
                        label = "502";
                        gaze = direction(70, -20);
                        gst = "all pointing_kitchen";
                        snd = "Waikiki.ogg";
                        spot = direction(114, 45);
                        break;
                    case DRAWER2:
                        label = "507";
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
                        try {
                            register(targetObject, modality, new ColorableLightConfiguration(label));
                        } catch (InitializeException ex) {
                            Logger.getLogger(Defaults.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case GAZE:
                        try {
                            register(targetObject, modality, new MekaGazeConfiguration(gaze));
                        } catch (RSBException ex) {
                            Logger.getLogger(Defaults.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case GESTURE:
                        if (gesture != null) {
                            register(targetObject, modality, new HighlightTarget().setExecution(gesture, gst));
                        }
                        break;
                    case SOUND:
                        if (sound != null) {
                            register(targetObject, modality, new HighlightTarget().setExecution(sound, snd));
                        }
                        break;
                    case SPOT_LIGHT:
                        try {
                            register(targetObject, modality, new ProjectorConfiguration(spot));
                        } catch (RSBException ex) {
                            Logger.getLogger(Defaults.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                }
            }
        }
    }
}
