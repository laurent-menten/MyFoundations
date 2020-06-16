package be.lmenten.utils.jdbc;

import be.lmenten.utils.plugin.PluginDescriptor;

public interface DataManagerDescriptor
	extends PluginDescriptor
{
	public DataManager<? extends Data> getInstance();

	public Class<? extends Data> getManagedClass();
}
