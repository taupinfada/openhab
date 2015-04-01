/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient.internal;

import org.openhab.core.items.Item;
import org.openhab.core.types.Type;

/**
 * Represents all valid commands which could be processed by this binding
 * 
 * @author Félix CARDON
 * @since 0.1.0
 */
public class VclientCommandType {

	String commandGetter;
	String commandSetter;
	Class<? extends Item> itemClass;
	Class<? extends Type> typeClass;
	String unit;

	public String getCommandGetter() {
		return commandGetter;
	}

	public String getCommandSetter() {
		return commandSetter;
	}

	public Class<? extends Item> getItemClass() {
		return itemClass;
	}

	public Class<? extends Type> getTypeClass() {
		return typeClass;
	}

	public String getPattern() {
		return unit;
	}
}
