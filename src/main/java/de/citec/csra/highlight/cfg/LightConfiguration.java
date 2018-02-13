/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import static de.citec.csra.highlight.cfg.Configurable.Stage.EXEC;
import static de.citec.csra.highlight.cfg.Configurable.Stage.INIT;
import static de.citec.csra.highlight.cfg.Configurable.Stage.PREPARE;
import static de.citec.csra.highlight.cfg.Configurable.Stage.RESET;
import de.citec.csra.highlight.com.LightConnection;
import org.openbase.jul.exception.InstantiationException;
import rsb.InitializeException;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author pholthau
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class LightConfiguration extends HighlightTarget {

	public LightConfiguration(final UnitConfig unitConfig) throws InstantiationException {
		LightConnection li = new LightConnection(unitConfig, 1000);
		super.setInit(li, INIT, 50);
		super.setPrepare(li, PREPARE, 50);
		super.setExecution(li, EXEC);
		super.setReset(li, RESET, 500);
	}

	public LightConfiguration(String alias) throws InterruptedException, InstantiationException {
		LightConnection li = new LightConnection(alias, 1000);
		super.setInit(li, INIT, 50);
		super.setPrepare(li, PREPARE, 500);
		super.setExecution(li, EXEC);
		super.setReset(li, RESET, 50);
	}
}
