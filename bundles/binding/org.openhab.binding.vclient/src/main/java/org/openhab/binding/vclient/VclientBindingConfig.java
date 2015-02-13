/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient;

import org.openhab.binding.vclient.internal.VclientCommandType;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;

/**
 * This is a class that stores vclient binding configuration elements : - the
 * type of command to operate on vcontrol - an interface to related openHAB
 * item
 * 
 * @author Félix CARDON
 * @since 0.1.0
 * 
 */

public class VclientBindingConfig implements BindingConfig {
	public VclientCommandType commandType;
	public Item item;

	public VclientBindingConfig(VclientCommandType commandType, Item item) {
		this.commandType = commandType;
		this.item = item;
	}
}
