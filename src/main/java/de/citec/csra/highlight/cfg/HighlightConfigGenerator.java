package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.InformerConnection;
import de.citec.csra.highlight.com.MethodCallConnection;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.EnumNotSupportedException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.openbase.jul.extension.rst.processing.MetaConfigPool;
import org.openbase.jul.extension.rst.processing.MetaConfigVariableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.RSBException;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.hri.HighlightTargetType.HighlightTarget.Modality;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HighlightConfigGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(HighlightConfigGenerator.class);

    private static final long TIMEOUT = TimeUnit.SECONDS.toMicros(2);


    private static final String DEFAULT_NOTIFICATION_SOUND_FILE = "Waikiki.ogg";
    public static final double LOOKUP_RADIUS = 5;

    private static final String META_CONFIG_KEY_HIGHLIGHT_SOUND_FILE = "HIGHLIGHT_SOUND_FILE";
    private static final String META_CONFIG_KEY_HIGHLIGHT_GESTURE = "HIGHLIGHT_GESTURE";

    private InformerConnection mekaGestureConnection = null;
    private MethodCallConnection audioSystemConnection = null;

    public HighlightConfigGenerator() throws InterruptedException {
        try {
            Registries.getUnitRegistry(false).getDataFuture().get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (CouldNotPerformException | TimeoutException | ExecutionException ex) {
            ExceptionPrinter.printHistory("Could not preload registry cache!", ex, LOGGER);
        }

        try {
            mekaGestureConnection = new InformerConnection<>("/meka/posture_execution/");
        } catch (RSBException ex) {
            ExceptionPrinter.printHistory("Could not establish meka gesture connection!", ex, LOGGER);
        }

        try {
            audioSystemConnection = new MethodCallConnection("/home/audio/control/radio/", "play");
        } catch (RSBException ex) {
            ExceptionPrinter.printHistory("Could not establish audio connection!", ex, LOGGER);
        }
    }

    public Highlightable generate(final String targetObject, final Modality modality) throws CouldNotPerformException, InterruptedException {
        try {
            UnitConfig targetUnitConfig = null;

            if (targetObject == null || targetObject.isEmpty()) {
                throw new NotAvailableException("TargetObject");
            }

            // resolve unit from target
            try {
                Registries.getUnitRegistry(false).getUnitConfigById(targetObject);
            } catch (NotAvailableException ex) {
                try {
                    targetUnitConfig = Registries.getUnitRegistry(false).getUnitConfigByAlias(targetObject);
                } catch (NotAvailableException exx) {
                    try {
                        targetUnitConfig = Registries.getUnitRegistry(false).getUnitConfigByScope(ScopeGenerator.generateScope(targetObject));
                    } catch (NotAvailableException exxx) {
                        try {
                            targetUnitConfig = Registries.getUnitRegistry(false).getUnitConfigById(targetObject);
                        } catch (NotAvailableException exxxx) {
                            List<UnitConfig> unitConfigs = Registries.getUnitRegistry(false).getUnitConfigsByLabel(targetObject);

                            if (unitConfigs.isEmpty()) {
                                throw new NotAvailableException("Target[" + targetObject + "] could not be resolved as id, alias, scope or label via the unit registry.");
                            }

                            if (unitConfigs.size() > 1) {
                                LOGGER.warn("More than one unit detected which is matching the object target label! We use the first result and skip the other ones.");
                            }
                            targetUnitConfig = unitConfigs.get(0);
                        }
                    }
                }
            }

            // validate target is available
            if (targetUnitConfig == null) {
                throw new InvalidStateException("Could not detect target unit!");
            }

            // setup variable pool
            MetaConfigPool variableProvider = new MetaConfigPool();
            variableProvider.register(new MetaConfigVariableProvider("TargetUnitConfig", targetUnitConfig.getMetaConfig()));
            variableProvider.register(new MetaConfigVariableProvider("TargetUnitType", Registries.getUnitRegistry().getUnitTemplateByType(targetUnitConfig.getType()).getMetaConfig()));

            // generate highlight config related to the given modality
            switch (modality) {
                case AMBIENT_LIGHT:
                    try {
                        // lookup next light to target

                        // 5m lookup radius
                        List<UnitConfig> closeUnitList = Registries.getUnitRegistry().getUnitConfigsByCoordinate(Registries.getUnitRegistry().getUnitPositionGlobalVec3DDouble(targetUnitConfig), LOOKUP_RADIUS, UnitType.LIGHT);
                        if (closeUnitList.isEmpty()) {
                            throw new CouldNotPerformException("Could not find any light close to the given target!");
                        }
                        return new LightConfiguration(closeUnitList.get(0));
                    } catch (CouldNotPerformException ex) {
                        throw new CouldNotPerformException("Could highlight next light!", ex);
                    }
                case GAZE:
                    throw new EnumNotSupportedException(modality, this);
                case GESTURE:
                    if (mekaGestureConnection == null) {
                        throw new NotAvailableException("Meka gesture connection");
                    }

                    // load custom gesture
                    try {
                        return new HighlightTarget().setExecution(mekaGestureConnection, variableProvider.getValue(META_CONFIG_KEY_HIGHLIGHT_GESTURE));
                    } catch (final NotAvailableException ex) {
                        throw new CouldNotPerformException("Could not generate gesture config!", ex);
                    }
                case SOUND:
                    if (audioSystemConnection != null) {
                        throw new NotAvailableException("Audio system connection");
                    }

                    String soundFile = DEFAULT_NOTIFICATION_SOUND_FILE;

                    // load custom unit sound
                    try {
                        soundFile = variableProvider.getValue(META_CONFIG_KEY_HIGHLIGHT_SOUND_FILE);
                    } catch (final NotAvailableException ex) {
                        // use default instead.
                    }

                    return new HighlightTarget().setExecution(audioSystemConnection, soundFile);
                case SPOT_LIGHT:
                default:
                    throw new EnumNotSupportedException(modality, this);
//                    try {
//                        return new ProjectorConfiguration(spot);
//                    } catch (RSBException ex) {
//                        throw new InvalidStateException("Projector connection error", ex);
//                    }
            }
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate highlight configuration!", ex);
        }
    }
}
