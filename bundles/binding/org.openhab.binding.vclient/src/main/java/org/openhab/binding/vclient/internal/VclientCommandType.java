/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient.internal;

import java.util.regex.Pattern;

import org.openhab.core.items.Item;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Type;

/**
 * Represents all valid commands which could be processed by this binding
 * 
 * @author Félix CARDON
 * @since 0.1.0
 */
public enum VclientCommandType {

	// System info
	// getDevice: identifiant de la régulation
	DEVICE {
		{
			commandGetter = "getDevice";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^(\\.*)$");
		}
	},
	// getSystemTime: Date system de la chaudière
	TIME {
		{
			commandGetter = "getSystemTime";
			itemClass = DateTimeItem.class;
			typeClass = DateTimeType.class;
			pattern = Pattern.compile("^(\\.*)$");
		}
	},

	// Temperature status
	// getTempExt: Température extérieure en degrés Celsius
	TEMP_EXT {
		{
			commandGetter = "getTempExt";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempExtMoy: Température extérieure en degrés Celsius
	TEMP_EXT_MOY {
		{
			commandGetter = "getTemptExtMoy";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempDepCC2: Température de départ du circuit de chauffage 2
	TEMP_DEP_CC2 {
		{
			commandGetter = "getTempDepCC2";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempIntCC2: Température intérieure pour le circuit de chauffage 2
	TEMP_INT_CC2 {
		{
			commandGetter = "getTempIntCC2";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempECS: Température eau chaude sanitaire
	TEMP_ECS {
		{
			commandGetter = "getTempECS";
			commandSetter = "setTempECS";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempDechargeECS: Température de décharge de l'eau chaude sanitaire
	TEMP_DEC_ECS {
		{
			commandGetter = "getTempDechargeECS";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempFume: Température des gaz d'échappement en degrés Celsius
	TEMP_SMOCKE {
		{
			commandGetter = "getTempFume";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempChaudiere: Température de la chaudière en degrès Celsius
	TEMP_BOILER {
		{
			commandGetter = "getTempChaudiere";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},

	// Status
	// getPompeStatutCC2: Statut de la pompe de circulation du circuit de
	STATE_PUMP_CC2 {
		{
			commandGetter = "getPompeStatutCC2";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^(\\w*)$");
		}
	},
	// getModeCC2: Mode de fonctionnement du circuit de chauffage 2
	STATE_MODE_CC2 {
		{
			commandGetter = "getModeCC2";
			commandSetter = "setModeCC2";
			itemClass = SwitchItem.class;
			typeClass = OnOffType.class;
			pattern = Pattern.compile("^(\\w*)$");
		}
	},
	// getEcoModeCC2: Statut du mode économique du circuit de chauffage 2
	STATE_MODE_ECO_CC2 {
		{
			commandGetter = "getEcoModeCC2";
			commandSetter = "setEcoModeCC2";
			itemClass = SwitchItem.class;
			typeClass = OnOffType.class;
			pattern = Pattern.compile("^(\\w*)$");
		}
	},
	// getRecModeCC2: Statut du mode réception du circuit de chauffage 2
	STATE_MODE_REC_CC2 {
		{
			commandGetter = "getRecModeCC2";
			commandGetter = "setRecModeCC2";
			itemClass = SwitchItem.class;
			typeClass = OnOffType.class;
			pattern = Pattern.compile("^(0|1)$");
		}
	},
	// getStatutPompeECS: Statut de la pompe d'alimentation de l'eau chaude
	// sanitaire
	STATE_PUMP_ECS {
		{
			commandGetter = "getStatutPompeECS";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^(\\w*)$");
		}
	},
	// getStatutCircuECS: Statut de la pompe de circulation de l'eau chaude
	// sanitaire
	STATE_PUMP_CIR {
		{
			commandGetter = "getStatutCircuECS";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^(\\w*)$");
		}
	},
	// getActChaudiere: Temps d'activation de la chaudière
	TIME_ON {
		{
			commandGetter = "getActChaudiere";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^(\\w*)$");
		}
	},
	// getDemarrageChaudiere: Démarrage de la chaudière
	STATE_ON {
		{
			commandGetter = "getDemarrageChaudiere";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\d*\\.\\d*)$");
		}
	},
	// getBruleur1Heure: Temps d'activation du bruleur 1
	TIME_BURNER_1 {
		{
			commandGetter = "getBruleur1Heure";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Heures$");
		}
	},
	// getBruleur2Heure: Temps d'activation du bruleur 2
	TIME_BURNER_2 {
		{
			commandGetter = "getBruleur2Heure";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^(\\-?\\d*\\.\\d*) Heures$");
		}
	};

	/**
	 * Represents the heatpump command as it will be used in *.items
	 * configuration
	 */
	String commandGetter;
	String commandSetter;
	Class<? extends Item> itemClass;
	Class<? extends Type> typeClass;
	Pattern pattern;

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

	public Pattern getPattern() {
		return pattern;
	}
}
