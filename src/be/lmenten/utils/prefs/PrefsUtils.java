package be.lmenten.utils.prefs;

import java.util.prefs.Preferences;

public class PrefsUtils
{
	public static Preferences getNode( Class<?> clazz )
	{
		Preferences prefs = Preferences.userRoot();

		String nodeName = "." + clazz.getName();
		nodeName = nodeName.replaceAll( "\\.", "/" );

		return prefs.node( nodeName );
	}
}
