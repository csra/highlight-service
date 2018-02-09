/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import de.citec.csra.highlight.com.RemoteConnection;

/**
 * @author pholthau
 */
public interface Configurable {

    enum Stage {
        INIT,
        PREPARE,
        EXEC,
        RESET,
        SHUTDOWN
    }

    <I> Configurable setInit(RemoteConnection<I> ri, I argument, long delay);

    <P> Configurable setPrepare(RemoteConnection<P> ri, P argument, long delay);

    <T> Configurable setExecution(RemoteConnection<T> ri, T argument);

    <R> Configurable setReset(RemoteConnection<R> ri, R argument, long delay);

    <S> Configurable setShutdown(RemoteConnection<S> ri, S argument, long delay);
}
