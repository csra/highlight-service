/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight;

import de.citec.csra.allocation.cli.ExecutableResource;
import static de.citec.csra.allocation.cli.ExecutableResource.Completion.RETAIN;
import de.citec.csra.highlight.cfg.Highlightable;
import java.util.concurrent.ExecutionException;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.logging.Logger;
import rsb.RSBException;
import static rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Initiator.HUMAN;
import static rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy.MAXIMUM;
import static rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority.NORMAL;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class HighlightExecutable extends ExecutableResource {

	private final static long OVERHEAD = 100;
	private final static Logger LOG = Logger.getLogger(HighlightExecutable.class.getName());
	private final Highlightable cfg;
	private final long duration;

	public HighlightExecutable(Highlightable cfg, long duration) {
		super("exec[" + cfg.toString() + "]",
				MAXIMUM,
				NORMAL,
				HUMAN,
				0,
				duration + OVERHEAD,
				MILLISECONDS,
				RETAIN,
				cfg.getInterfaces().stream().map(r -> "highlight:" + r).toArray(s -> new String[s]));
		this.cfg = cfg;
		this.duration = duration;
	}

	@Override
	public Object execute() throws ExecutionException {
		try {
			this.cfg.highlight(getRemote().getRemainingTime() - OVERHEAD);
			return null;
		} catch (Exception ex) {
			throw new ExecutionException(ex);
		}
	}

	@Override
	public String toString() {
		return "exec[" + cfg.toString() + ":" + duration + "]";
	}

}
