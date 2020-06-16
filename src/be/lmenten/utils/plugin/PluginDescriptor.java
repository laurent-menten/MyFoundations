package be.lmenten.utils.plugin;

import java.lang.Runtime.Version;

public interface PluginDescriptor
{
	public String getName();
	public String getSimpleName();
	public Version getVersion();
	public String getDescription();

	public Plugin getInstance();
}
