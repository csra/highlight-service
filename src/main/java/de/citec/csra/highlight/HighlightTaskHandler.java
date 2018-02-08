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
import de.citec.csra.rst.parse.EnumParser;
import de.citec.csra.rst.parse.HighlightTargetParser;
import de.citec.csra.task.srv.AbstractTaskHandler;
import de.citec.csra.task.srv.ExecutableResourceTask;
import de.citec.csra.task.srv.LocalTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;
import rsb.RSBException;
import rst.hri.HighlightTargetType.HighlightTarget;
import rst.hri.HighlightTargetType.HighlightTarget.Modality;

/**
 * @author pholthau
 */
public class HighlightTaskHandler extends AbstractTaskHandler {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HighlightTaskHandler.class);

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

    private Set<ExecutableResource> getActions(final HighlightTarget highlightTarget) {
        final Set<ExecutableResource> actions = new HashSet<>();

        // generate actions
        highlightTarget.getModalityList().forEach((modality) -> {
            Highlightable highlightConfig;

            // generate / load highlight config
            try {
                highlightConfig = HighlightConfigGenerator.generate(highlightTarget.getTargetId(), modality);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not generate highlight config and use default implementation instead!", ex, LOGGER);

                // load default config
//				TargetObject targetObject = new EnumParser<>(TargetObject.class).getValue(highlightTarget.getTargetId().toUpperCase());
                TargetObject targetObject = TargetObject.valueOf(highlightTarget.getTargetId().toUpperCase());
                highlightConfig = Defaults.get(targetObject, modality);
            }

            try {
                actions.add(new HighlightExecutable(highlightConfig, highlightTarget.getDuration().getTime() / 1000l));
            } catch (RSBException ex) {
                Logger.getLogger(HighlightTaskHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return actions;
    }
}
