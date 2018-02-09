/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.RemoteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.citec.csra.rst.util.StringRepresentation.shortString;

import java.util.HashSet;
import java.util.Set;

/**
 * @author pholthau
 */
public class HighlightTarget implements Highlightable, Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HighlightTarget.class);

    private RemoteConnection exec;
    private Object execArg;

    private RemoteConnection init;
    private Object initArg;
    private long initDelay;

    private RemoteConnection prepare;
    private Object prepareArg;
    private long prepareDelay;

    private RemoteConnection reset;
    private Object resetArg;
    private long resetDelay;

    private RemoteConnection shutdown;
    private Object shutdownArg;
    private long shutdownDelay;

    @Override
    public <I> HighlightTarget setInit(RemoteConnection<I> ri, I argument, long delay) {
        this.init = ri;
        this.initArg = argument;
        this.initDelay = delay;
        return this;
    }

    @Override
    public <P> HighlightTarget setPrepare(RemoteConnection<P> ri, P argument, long delay) {
        this.prepare = ri;
        this.prepareArg = argument;
        this.prepareDelay = delay;
        return this;
    }

    @Override
    public <T> HighlightTarget setExecution(RemoteConnection<T> ri, T argument) {
        this.exec = ri;
        this.execArg = argument;
        return this;
    }

    @Override
    public <R> HighlightTarget setReset(RemoteConnection<R> ri, R argument, long delay) {
        this.reset = ri;
        this.resetArg = argument;
        this.resetDelay = delay;
        return this;
    }

    @Override
    public <S> HighlightTarget setShutdown(RemoteConnection<S> ri, S argument, long delay) {
        this.shutdown = ri;
        this.shutdownArg = argument;
        this.shutdownDelay = delay;
        return this;
    }

    @Override
    public void highlight(long duration) throws Exception {
        init();
        try {
            prepare();
            execute(duration - (this.initDelay + this.prepareDelay + this.resetDelay + this.shutdownDelay));
        } catch (InterruptedException ex) {
            LOGGER.info("Execution interrupted, shutting down immediately.");
            this.resetDelay = 0;
            this.shutdownDelay = 0;
        } finally {
            reset();
            shutdown();
        }
    }

    private void init() throws Exception {
        if (this.init != null) {
            LOGGER.info("Sending to ''{0}'' with argument ''{1}'' for initialization ({2}ms).", init.getAddress(), shortString(initArg), initDelay);
            this.init.send(this.initArg);
            Thread.sleep(this.initDelay);
        }
    }

    private void prepare() throws Exception {
        if (this.prepare != null) {
            LOGGER.info("Sending to ''{0}'' with argument ''{1}'' for preparation ({2}ms).", prepare.getAddress(), shortString(prepareArg), prepareDelay);
            this.prepare.send(this.prepareArg);
            Thread.sleep(this.prepareDelay);
        }
    }

    private void execute(long duration) throws Exception {
        if (this.exec != null) {
            LOGGER.info("Sending to ''{0}'' with argument ''{1}'' as a target ({2}ms).", exec.getAddress(), shortString(execArg), duration);
            this.exec.send(this.execArg);
            Thread.sleep(duration);
        }
    }

    private void reset() throws Exception {
        if (this.reset != null) {
            LOGGER.info("Sending to ''{0}'' with argument ''{1}'' for reset ({2}ms).", reset.getAddress(), shortString(resetArg), resetDelay);
            this.reset.send(this.resetArg);
            Thread.sleep(this.resetDelay);
        }
    }

    private void shutdown() throws Exception {
        if (this.shutdown != null) {
            LOGGER.info("Sending to ''{0}'' with argument ''{1}'' for shutdown ({2}ms).", shutdown.getAddress(), shortString(shutdownArg), shutdownDelay);
            this.shutdown.send(this.shutdownArg);
            Thread.sleep(this.shutdownDelay);
        }
    }

    @Override
    public Set<String> getInterfaces() {
        Set<String> ifs = new HashSet<>();
        if (this.exec != null) {
            ifs.add(this.exec.getAddress());
        }
        if (this.prepare != null) {
            ifs.add(this.prepare.getAddress());
        }
        if (this.reset != null) {
            ifs.add(this.reset.getAddress());
        }
        if (this.shutdown != null) {
            ifs.add(this.shutdown.getAddress());
        }
        return ifs;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + shortString(exec.getAddress()) + "]";
    }
}
