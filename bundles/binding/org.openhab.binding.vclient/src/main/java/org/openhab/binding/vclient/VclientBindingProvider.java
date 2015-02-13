/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient;

import org.openhab.core.binding.BindingProvider;

/**
 * @author Felix CARDON
 * @since 0.1
 */
public interface VclientBindingProvider extends BindingProvider {

	/**
	 * @return the binding config to the given <code>itemName</code>
	 */
	public VclientBindingConfig getConfig(String itemName);
}
