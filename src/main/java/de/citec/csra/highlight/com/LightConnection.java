/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.com;

import de.citec.csra.highlight.cfg.Configurable.Stage;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.DimmableLightRemote;
import org.openbase.bco.dal.remote.unit.LightRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.InitializeException;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.state.PowerStateType.PowerState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.vision.HSBColorType.HSBColor;

import java.util.concurrent.TimeUnit;

import static de.citec.csra.rst.util.StringRepresentation.shortString;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static rst.domotic.state.PowerStateType.PowerState.State.OFF;
import static rst.domotic.state.PowerStateType.PowerState.State.ON;

/**
 * @author pholthau
 */
public class LightConnection implements RemoteConnection<Stage> {

    private final static Logger LOGGER = LoggerFactory.getLogger(LightConnection.class);

    private final long timeout;
    private UnitConfig unitConfig;
    private State originalState;
    private HSBColor originalColor;

    private final HSBColor A_COLOR = HSBColor.newBuilder().setHue(210).setSaturation(100).setBrightness(100).build();
    private final HSBColor B_COLOR = HSBColor.newBuilder().setHue(180).setSaturation(100).setBrightness(100).build();

    public LightConnection(final UnitConfig unitConfig, long timeout) {
        this.timeout = timeout;
        this.unitConfig = unitConfig;
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

        switch (unitConfig.getType()) {
            case COLORABLE_LIGHT:
                ColorableLightRemote colorableLight = Units.getFutureUnit(unitConfig, true, Units.COLORABLE_LIGHT).get(timeout, MILLISECONDS);
                switch (argument) {
                    case INIT:
                        break;
                    case PREPARE:
                        originalState = colorableLight.getPowerState().getValue();
                        LOGGER.info("Storing light power ''{}'' as ''{}''.", unitConfig.getLabel(), shortString(originalState));
                        originalColor = colorableLight.getHSBColor();
                        LOGGER.info("Storing light color ''{}'' as ''{}''.", colorableLight.getLabel(), shortString(originalColor));
                        LOGGER.info("Set light color ''{}'' to ''{}''.", colorableLight.getLabel(), shortString(A_COLOR));
                        colorableLight.setColor(B_COLOR).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                    case EXEC:
                        LOGGER.info("Set light color ''{}'' to ''{}''.", colorableLight.getLabel(), shortString(A_COLOR));
                        colorableLight.setColor(A_COLOR).get(timeout, TimeUnit.MILLISECONDS);
                        //implies power state on
                        break;
                    case RESET:
                        LOGGER.info("Reset light color ''{}'' to ''{}''.", colorableLight.getLabel(), shortString(originalColor));
                        colorableLight.setColor(originalColor).get(timeout, TimeUnit.MILLISECONDS);
                        LOGGER.info("Reset light power ''{}'' to ''{}''.", colorableLight.getLabel(), shortString(originalState));
                        colorableLight.setPowerState(PowerState.newBuilder().setValue(originalState).build()).get(timeout, TimeUnit.MILLISECONDS);
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
                        originalState = light.getPowerState().getValue();
                        LOGGER.info("Storing dimmer power ''{}'' as ''{}''.", light.getLabel(), shortString(originalState));
                        LOGGER.info("Set dimmer power ''{}'' to ''{}''.", light.getLabel(), shortString(OFF));
                        light.setPowerState(PowerState.newBuilder().setValue(OFF).build());
                        break;
                    case EXEC:
                        LOGGER.info("Set dimmer power ''{}'' to ''{}''.", light.getLabel(), shortString(ON));
                        light.setPowerState(PowerState.newBuilder().setValue(ON).build());
                        break;
                    case RESET:
                        LOGGER.info("Reset dimmer power ''{}'' to ''{}''.", light.getLabel(), shortString(originalState));
                        light.setPowerState(PowerState.newBuilder().setValue(originalState).build()).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                }
                break;
            default:
                throw new InvalidStateException("Registerd unit is not a light!");
        }
    }

    @Override
    public String getAddress() {
        try {
            return ScopeGenerator.generateStringRep(unitConfig.getScope());
        } catch (CouldNotPerformException ex) {
            LOGGER.warn("Could not infer scope of unit ''{0}'' ({1}), returning null.", unitConfig.getLabel(), ex);
            return null;
        }
    }
}
