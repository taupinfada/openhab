package org.openhab.binding.vcontrol.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ViessmannControlAvailableCommand {

	static final Logger logger = LoggerFactory
			.getLogger(VControlConnector.class);

	private static ViessmannControlAvailableCommand singleton = null;
	private String host;
	private int port;
	private List<String> availableCommand;

	private ViessmannControlAvailableCommand(String host, int port) {
		this.host = host;
		this.port = port;
		ViessmannConnector vcc = ViessmannConnectorFactory
				.getConnector(ViessmannConnectorFactory.VCONTROL_CONNECTOR);
		while (!vcc.connect(host, port)) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				logger.error("Unable to wait for retry connection: "
						+ e.getMessage());
			}
		}
		this.availableCommand = vcc.getCommands();
		vcc.disconnect();
	}

	public static ViessmannControlAvailableCommand getViessmannControlAvailableCommand(
			String host, int port) {
		// If we change host, then we reload the available commands
		if (singleton != null
				&& (singleton.host != host || singleton.port != port)) {
			singleton = null;
		}
		if (singleton == null) {
			singleton = new ViessmannControlAvailableCommand(host, port);
		}
		return singleton;
	}

	public static ViessmannControlAvailableCommand getViessmannControlAvailableCommand() {
		return singleton;
	}

	public boolean isAvailable(String command) {
		if (availableCommand != null) {
			return availableCommand.contains(command);
		} else {
			return false;
		}
	}
}