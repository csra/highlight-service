/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight;

import de.citec.csra.allocation.cli.ExecutableResource;
import de.citec.csra.highlight.cfg.Defaults;
import de.citec.csra.highlight.cfg.HighlightConfigGenerator;
import de.citec.csra.highlight.cfg.Highlightable;
import de.citec.csra.highlight.cfg.TargetObject;
import de.citec.csra.rst.parse.HighlightTargetParser;
import de.citec.csra.task.srv.AbstractTaskHandler;
import de.citec.csra.task.srv.ExecutableResourceTask;
import de.citec.csra.task.srv.LocalTask;

import java.util.HashSet;
import java.util.Set;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.hri.HighlightTargetType.HighlightTarget;
import rst.hri.HighlightTargetType.HighlightTarget.Modality;

/**
 * @author pholthau
 * @author mpohling
 */
public class HighlightTaskHandler extends AbstractTaskHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(HighlightTaskHandler.class);
    private final HighlightConfigGenerator highlightConfigGenerator;


    public HighlightTaskHandler() throws InterruptedException {
        this.highlightConfigGenerator = new HighlightConfigGenerator();
    }

    @Override
    public LocalTask newLocalTask(final Object description) throws IllegalArgumentException {
        try {
            HighlightTarget highlightTarget;
            if (description instanceof HighlightTarget) {
                highlightTarget = (HighlightTarget) description;
            } else if (description instanceof String) {
                highlightTarget = new HighlightTargetParser().getValue((String) description);
            } else {
                throw new IllegalArgumentException("unreadable description: " + description);
            }
            return new ExecutableResourceTask(getActions(highlightTarget), true);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    private Set<ExecutableResource> getActions(final HighlightTarget highlightTarget) throws InterruptedException {
        final Set<ExecutableResource> actions = new HashSet<>();

        // generate actions
        for (final Modality modality : highlightTarget.getModalityList()) {
            try {
                Highlightable highlightConfig;

                // generate / load highlight config
                try {
                    highlightConfig = highlightConfigGenerator.generate(highlightTarget.getTargetId(), modality);
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory("Could not generate highlight config via registry and use default configuration instead!", ex, LOGGER, LogLevel.WARN);

                    // load default config as fallback
                    TargetObject targetObject = TargetObject.valueOf(highlightTarget.getTargetId().toUpperCase());
                    highlightConfig = Defaults.get(targetObject, modality);
                }

                actions.add(new HighlightExecutable(highlightConfig, highlightTarget.getDuration().getTime() / 1000l));
            } catch (Exception ex) {
                ExceptionPrinter.printHistory("Could not generate action for Target[" + highlightTarget + "]!", ex, LOGGER);
            }
        }
        return actions;
    }
}
