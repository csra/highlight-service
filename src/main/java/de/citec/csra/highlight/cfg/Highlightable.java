/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.cfg;

import java.util.Collection;

/**
 * @author pholthau
 */
public interface Highlightable {

    void highlight(long duration) throws Exception;

    Collection<String> getInterfaces();
}
