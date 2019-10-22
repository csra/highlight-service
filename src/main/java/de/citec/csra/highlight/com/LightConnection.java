/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.com;

import de.citec.csra.highlight.cfg.Configurable.Stage;
import org.openbase.bco.dal.remote.action.RemoteAction;
import org.openbase.bco.dal.remote.layer.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.layer.unit.LightRemote;
import org.openbase.bco.dal.remote.layer.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.type.domotic.action.ActionDescriptionType.ActionDescription;
import org.openbase.type.domotic.action.ActionEmphasisType.ActionEmphasis.Category;
import org.openbase.type.domotic.action.ActionInitiatorType.ActionInitiator.InitiatorType;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter.Builder;
import org.openbase.type.domotic.action.ActionPriorityType.ActionPriority.Priority;
import org.openbase.type.vision.HSBColorType.HSBColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.state.PowerStateType.PowerState.State;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

import java.util.concurrent.TimeUnit;

import static de.citec.csra.rst.util.StringRepresentation.shortString;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openbase.type.domotic.state.PowerStateType.PowerState.State.OFF;
import static org.openbase.type.domotic.state.PowerStateType.PowerState.State.ON;

/**
 * @author pholthau
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>s
 */
public class LightConnection implements RemoteConnection<Stage> {

    private final static Logger LOGGER = LoggerFactory.getLogger(LightConnection.class);

    private final static long ANIMATION_RATE = 600;

    private final long timeout;
    private final HSBColor BLUE_COLOR = HSBColor.newBuilder().setHue(247d).setSaturation(1d).setBrightness(1d).build();
    private UnitConfig unitConfig;
    private State originalPowerState;
    private HSBColor originalColor;

    private static final ActionParameter ACTION_PARAMETER;

    /**
     * These are parameters used for any highlighting actions.
     * The priority is high because highlighting always refers to an ongoing system - human interaction.
     */
    static {
        final Builder actionParameterBuilder = ActionParameter.newBuilder();
        actionParameterBuilder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.SYSTEM);
        actionParameterBuilder.setSchedulable(false);
        actionParameterBuilder.setSchedulable(false);
        actionParameterBuilder.addCategory(Category.COMFORT);
        actionParameterBuilder.setPriority(Priority.HIGH);
        actionParameterBuilder.setExecutionTimePeriod(SECONDS.toMicros(5));
        ACTION_PARAMETER = actionParameterBuilder.build();
    }

    public LightConnection(final UnitConfig unitConfig, long timeout) throws InstantiationException {
        try {
            this.timeout = timeout;
            this.unitConfig = unitConfig;

            // validate
            switch (unitConfig.getUnitType()) {
                case COLORABLE_LIGHT:
                case DIMMABLE_LIGHT:
                case LIGHT:
                    break;
                default:
                    throw new InvalidStateException("Registered Unit[" + unitConfig.getLabel() + "] is not a light!");
            }
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }


    public LightConnection(String alias, long timeout) throws InstantiationException, InterruptedException {
        this.timeout = timeout;
        try {
            // lookup unit config
            Registries.getUnitRegistry().waitForData(timeout, MILLISECONDS);
            this.unitConfig = Registries.getUnitRegistry().getUnitConfigByAlias(alias);
        } catch (CouldNotPerformException | IllegalArgumentException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    public void send(Stage argument) throws Exception {

        switch (unitConfig.getUnitType()) {
            case COLORABLE_LIGHT:
                ColorableLightRemote colorableLight = Units.getFutureUnit(unitConfig, true, Units.COLORABLE_LIGHT).get(timeout, MILLISECONDS);
                switch (argument) {
                    case INIT:
                        break;
                    case PREPARE:
                        break;
                    case EXEC:
                        LOGGER.info("Execute light ''{}'' on / off animation.", colorableLight.getLabel());
                        colorableLight.setColor(BLUE_COLOR, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setColor(BLUE_COLOR, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setColor(BLUE_COLOR, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setColor(BLUE_COLOR, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        colorableLight.setColor(BLUE_COLOR, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        break;
                    case RESET:
                        colorableLight.setPowerState(OFF, ACTION_PARAMETER).get(timeout, MILLISECONDS);
                        break;
                }
                break;
            case DIMMABLE_LIGHT:
            case LIGHT:
                LightRemote light = Units.getFutureUnit(unitConfig, true, Units.LIGHT).get(timeout, MILLISECONDS);
                switch (argument) {
                    case INIT:
                        break;
                    case PREPARE:
                        break;
                    case EXEC:
                        LOGGER.info("Execute light ''{}'' on / off animation.", light.getLabel());
                        light.setPowerState(ON, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        light.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        light.setPowerState(ON, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        light.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        light.setPowerState(ON, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        light.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        Thread.sleep(ANIMATION_RATE);
                        light.setPowerState(ON, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                    case RESET:
                        light.setPowerState(OFF, ACTION_PARAMETER).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                }
                break;
            default:
                throw new InvalidStateException("Registered unit is not a light!");
        }
    }

    @Override
    public String getAddress() {
        try {
            return ScopeProcessor.generateStringRep(unitConfig.getScope());
        } catch (CouldNotPerformException ex) {
            LOGGER.warn("Could not infer scope of unit ''{0}'' ({1}), returning null.", unitConfig.getLabel(), ex);
            return null;
        }
    }
}
