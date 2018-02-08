/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.com;

import de.citec.csra.highlight.cfg.Configurable.Stage;

import static de.citec.csra.rst.util.StringRepresentation.shortString;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.DimmableLightRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import rsb.InitializeException;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.state.PowerStateType.PowerState.State;

import static rst.domotic.state.PowerStateType.PowerState.State.OFF;
import static rst.domotic.state.PowerStateType.PowerState.State.ON;

import rst.domotic.unit.UnitConfigType.UnitConfig;

import static rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType.DIMMABLE_LIGHT;

import rst.vision.HSBColorType.HSBColor;

/**
 * @author pholthau
 */
public class LightConnection implements RemoteConnection<Stage> {

    private final static Logger LOG = Logger.getLogger(LightConnection.class.getName());

    private final long timeout;
    private UnitConfig unit;
    private State originalState;
    private HSBColor originalColor;

    private final HSBColor A_COLOR = HSBColor.newBuilder().setHue(210).setSaturation(100).setBrightness(100).build();
    private final HSBColor B_COLOR = HSBColor.newBuilder().setHue(180).setSaturation(100).setBrightness(100).build();

    public LightConnection(final UnitConfig unitConfig, long timeout) {
        this.timeout = timeout;
        this.unit = unitConfig;
    }


    public LightConnection(String label, long timeout) throws InitializeException {
        this.timeout = timeout;
        try {
            Registries.getUnitRegistry().waitForData(timeout, MILLISECONDS);
            List<UnitConfig> units = Registries.getUnitRegistry().getUnitConfigsByLabel(label);
            loop:
            for (UnitConfig u : units) {
                switch (u.getType()) {
                    case COLORABLE_LIGHT:
                    case DIMMABLE_LIGHT:
                        this.unit = u;
                        break loop;
                    default:
                        break;
                }
            }
            if (this.unit == null) {
                throw new IllegalArgumentException("no light with id '" + label + "' available");
            }
        } catch (InterruptedException | CouldNotPerformException | IllegalArgumentException ex) {
            throw new InitializeException(ex);
        }
    }

    @Override
    public void send(Stage argument) throws Exception {

        switch (unit.getType()) {
            case COLORABLE_LIGHT:
                ColorableLightRemote light = Units.getFutureUnit(unit, true, ColorableLightRemote.class).get(timeout, MILLISECONDS);
                switch (argument) {
                    case INIT:
                        break;
                    case PREPARE:
                        originalState = light.getPowerState().getValue();
                        LOG.log(Level.INFO, "Storing light power ''{0}'' as ''{1}''.", new Object[]{unit.getLabel(), shortString(originalState)});
                        originalColor = light.getHSBColor();
                        LOG.log(Level.INFO, "Storing light color ''{0}'' as ''{1}''.", new Object[]{unit.getLabel(), shortString(originalColor)});
                        LOG.log(Level.INFO, "Set light color ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(A_COLOR)});
                        light.setColor(B_COLOR).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                    case EXEC:
                        LOG.log(Level.INFO, "Set light color ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(A_COLOR)});
                        light.setColor(A_COLOR).get(timeout, TimeUnit.MILLISECONDS);
                        //implies power state on
                        break;
                    case RESET:
                        LOG.log(Level.INFO, "Reset light color ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(originalColor)});
                        light.setColor(originalColor).get(timeout, TimeUnit.MILLISECONDS);
                        LOG.log(Level.INFO, "Reset light power ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(originalState)});
                        light.setPowerState(PowerState.newBuilder().setValue(originalState).build()).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                }
                break;
            case DIMMABLE_LIGHT:
                DimmableLightRemote dimmer = Units.getFutureUnit(unit, true, DimmableLightRemote.class).get(timeout, MILLISECONDS);
                switch (argument) {
                    case INIT:
                        break;
                    case PREPARE:
                        originalState = dimmer.getPowerState().getValue();
                        LOG.log(Level.INFO, "Storing dimmer power ''{0}'' as ''{1}''.", new Object[]{unit.getLabel(), shortString(originalState)});
                        LOG.log(Level.INFO, "Set dimmer power ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(OFF)});
                        dimmer.setPowerState(PowerState.newBuilder().setValue(OFF).build());
                        break;
                    case EXEC:
                        LOG.log(Level.INFO, "Set dimmer power ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(ON)});
                        dimmer.setPowerState(PowerState.newBuilder().setValue(ON).build());
                        break;
                    case RESET:
                        LOG.log(Level.INFO, "Reset dimmer power ''{0}'' to ''{1}''.", new Object[]{unit.getLabel(), shortString(originalState)});
                        dimmer.setPowerState(PowerState.newBuilder().setValue(originalState).build()).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String getAddress() {
        try {
            return ScopeGenerator.generateStringRep(unit.getScope());
        } catch (CouldNotPerformException ex) {
            LOG.log(Level.WARNING, "Could not infer scope of unit ''{0}'' ({1}), returning null.", new Object[]{unit.getLabel(), ex});
            return null;
        }
    }
}
