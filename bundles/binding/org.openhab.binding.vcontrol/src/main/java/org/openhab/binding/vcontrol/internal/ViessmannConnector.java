package org.openhab.binding.vcontrol.internal;

import java.util.List;

public interface ViessmannConnector {
	public boolean connect(String serverIp, int serverPort);

	public String getValue(String command);

	public List<String> getCommands();
	
	public boolean isAvailableCommand(String command);

	public boolean submit(String command);

	public boolean disconnect();
}
