/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight;

import de.citec.csra.highlight.cfg.Defaults;
import de.citec.csra.task.srv.TaskServer;
import org.openbase.bco.registry.remote.login.BCOLogin;
import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.communication.controller.jp.JPScope;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.communicationpatterns.TaskStateType.TaskState;
import rst.geometry.SphericalDirectionFloatType.SphericalDirectionFloat;
import rst.geometry.TranslationType.Translation;
import rst.hri.HighlightTargetType.HighlightTarget;
import org.openbase.type.spatial.PanTiltAngleType.PanTiltAngle;

/**
 * @author Patrick Holthaus (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class HighlightService {

    private final static Logger LOGGER = LoggerFactory.getLogger(HighlightService.class);
    private final static String SCOPEVAR = "SCOPE_HIGHLIGHT";

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(TaskState.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(HighlightTarget.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SphericalDirectionFloat.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(PanTiltAngle.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(Translation.getDefaultInstance()));
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            JPService.setApplicationName(HighlightService.class);
            if (System.getenv().containsKey(SCOPEVAR)) {
                JPService.registerProperty(JPScope.class, ScopeProcessor.generateScope(System.getenv(SCOPEVAR)));
            } else {
                JPService.registerProperty(JPScope.class, ScopeProcessor.generateScope("/citec/csra/home/highlight"));
            }
            JPService.registerProperty(JPDebugMode.class);
            JPService.parseAndExitOnError(args);

            BCOLogin.getSession().loginBCOUser();

            // preload configurations
            Defaults.loadDefaults();

            // execute highlight task handler
            new TaskServer(ScopeProcessor.generateStringRep(JPService.getProperty(JPScope.class).getValue()), new HighlightTaskHandler()).execute();
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not launch " + JPService.getApplicationName(), ex), LOGGER);
        }
    }
}
