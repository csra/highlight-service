/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.highlight.com;

/**
 *
 * @author pholthau
 */
public interface RemoteConnection<T> {
	
	void send(T argument) throws Exception;
	
	String getAddress();
	
}
