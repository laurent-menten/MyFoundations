// = ======================================================================== =
// = ================= Copyright (c) 2020+ = Laurent Menten ================= =
// = ======================================================================== =
// = = This program is free software: you can redistribute it and/or modify = =
// = = it under the terms of the GNU General Public License as published by = =
// = = the Free Software Foundation, either version 3 of the License, or    = =
// = = (at your option) any later version.                                  = =
// = =                                                                      = =
// = = This program is distributed in the hope that it will be useful, but  = =
// = = WITHOUT ANY WARRANTY; without even the implied warranty of           = =
// = = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    = =
// = = General Public License for more details.                             = =
// = =                                                                      = =
// = = You should have received a copy of the GNU General Public License    = =
// = = along with this program. If not, see                                 = =
// = = <https://www.gnu.org/licenses/>.                                     = =
// = ======================================================================== =

package be.lmenten.utils.app;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.lmenten.utils.logging.AnsiLogFormatter;
import be.lmenten.utils.logging.LogFormatter;
import be.lmenten.utils.logging.LogRegExPackageFilter;
import be.lmenten.utils.swing.logging.JLogHandlerFrame;

/**
 * 
 * @param <T>
 *
 * @author Laurent Menten
 * @version 1.0, (5 Jun 2020)
 * @since 1.0
 */
public abstract class Application<T extends Application<T>>
{
	private static final String HELP_TEXT
		=	"  -noAnsiLog         Disable ANSI log output\n"
		+	"  -dontFilterLogs    Do not filter out foreign packages logs\n"
		+	"  -keepLogFile       Do not remove log file on successful exit\n"
		+	"  -createLogWindow   Create a window with application log\n"
		+	"  -showLogWindow     Show a window with application log"
		+									" (emplied -createLowWindow)\n"
		+	"  -keepLogWindowOpen Do not close log window at exit\n"
		+	"  -help              Show this help page\n"
		;

	private boolean finished;

	// -------------------------------------------------------------------------

	protected JLogHandlerFrame logWindow;

	// =========================================================================
	// ===
	// =========================================================================

	public Application()
	{
	}

	// =========================================================================
	// ===
	// =========================================================================

	public static void launch( Class<? extends Application<?>> type, String [] args )
	{
		// ----------------------------------------------------------------------
		// - Runtime configuration ----------------------------------------------
		// ----------------------------------------------------------------------

		String datePattern = "yyyyMMdd-HHmmss";
		SimpleDateFormat dateFormat = new SimpleDateFormat( datePattern );
		String dateString = dateFormat.format( new Date() );

		String logfileName = "session-" + dateString + ".log";

		// ----------------------------------------------------------------------
		// - Command line arguments ---------------------------------------------
		// ----------------------------------------------------------------------

		boolean noAnsiLog = false;
		boolean filterLogs = true;

		boolean keepLogFile = true;

		boolean createLogWindow = true;
		boolean showLogWindow = false;
		boolean keepLogWindowOpen = true;

		// ----------------------------------------------------------------------

		for( String arg : args )
		{
			switch( arg )
			{
				case "-noAnsiLog":			noAnsiLog = true ;			break ;
				case "-dontFilterLogs":		filterLogs = false ;			break ;
				case "-keepLogFile":			keepLogFile = true ;			break ;
				case "-createLogWindow":	createLogWindow = true ;	break ;
				case "-showLogWindow":		showLogWindow = true ;		break ;
				case "-keepLogWindowOpen":	keepLogWindowOpen = true;	break;

				// ----------------------------------------------------------------

				case "-h":
				case "-help":
				case "--help":
				{
					System.out.println( "Syntax: java "
							+ type.getName() + "<options>" );
					System.out.println( HELP_TEXT );

					System.exit( 0 );
				}

				default:
				{
					System.err.println( "Unknown argument '"
							+ arg + "'" );
					System.err.println( "Please use -h for help." );							

					System.exit( -1 );
				}
			}
		}

		// ----------------------------------------------------------------------
		// - Custom log output & formatter --------------------------------------
		// ----------------------------------------------------------------------

		if( ! noAnsiLog )
		{
			AnsiLogFormatter.install();
		}
		else
		{
			LogFormatter.install();
		}

		// ----------------------------------------------------------------------

		try
		{
			FileHandler fileLogHandler = new FileHandler( logfileName );
			LogFormatter fileLogFormatter = new LogFormatter();
			fileLogHandler.setFormatter( fileLogFormatter );

			Logger.getLogger( "" ).addHandler( fileLogHandler );
		}

		catch( IOException e )
		{
			log.log( Level.WARNING, "Failed to install logfile handler", e );
		}

		// ----------------------------------------------------------------------
		// - Filter out foreign packages logs -----------------------------------
		// ----------------------------------------------------------------------

		if( filterLogs )
		{
			LogRegExPackageFilter.install( "be\\.lmenten\\..*", Level.ALL );
		}

		// ----------------------------------------------------------------------
		// - Show UI log window -------------------------------------------------
		// ----------------------------------------------------------------------

		final JLogHandlerFrame logWindow;

		if( createLogWindow || showLogWindow )
		{
			logWindow = new JLogHandlerFrame();
			logWindow.setSize( 1024, 600 );
			logWindow.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( WindowEvent e )
				{
					e.getWindow().dispose();
				}	
			} );

			Handler handler = logWindow.getHandler();
			handler.setFilter( Logger.getLogger( "" ).getFilter() );

			logWindow.installHandler();

			if( showLogWindow )
			{
				logWindow.setVisible( true );
			}
		}
		else
		{
			logWindow = null;
		}

		// ----------------------------------------------------------------------
		// - Application run ----------------------------------------------------
		// ----------------------------------------------------------------------

		final Application<? extends Application<?>> app;

		try
		{
			Constructor<? extends Application<?>> constructor
				= type.getConstructor();

			app = (Application<? extends Application<?>>) constructor.newInstance();
			app.logWindow = logWindow;

			// -------------------------------------------------------------------

			log.info( "Initializing application ..." );

			app.initialize();

			log.info( "Starting application ..." );

			Thread appThread = new Thread( () -> app.run(), "master" );
			appThread.start();

			synchronized( app )
			{
				while( ! app.finished() )
				{
					app.wait();

					if( ! app.finished() )
					{
						log.warning( "Application notified but not finished." );
					}
				}
			}

			app.cleanup();

			log.info( "Application finished." );
		}

		// ----------------------------------------------------------------------
		// - Last chance exception handling -------------------------------------
		// ----------------------------------------------------------------------

		catch( Exception e )
		{
			keepLogFile = true;

			log.log( Level.SEVERE, "Unexpected exception caught", e );
		}

		// ----------------------------------------------------------------------
		// - Close resources ----------------------------------------------------
		// ----------------------------------------------------------------------

		finally
		{
			if( ! keepLogFile )
			{
				Path logfilePath = Paths.get( logfileName );

				try
				{
					Files.deleteIfExists( logfilePath );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			if( (logWindow != null) && ! keepLogWindowOpen )
			{
				logWindow.dispose();
			}
		}
	}

	// =========================================================================
	// === TERMINATION =========================================================
	// =========================================================================

	/**
	 * 
	 */
	public final void finish()
	{
		this.finished = true;

		synchronized( this )
		{
			this.notify();			
		}
	}

	/**
	 * 
	 * @return
	 */
	public final boolean finished()
	{
		return finished;
	}

	// =========================================================================
	// === APPLICATION LIFECYCLE ===============================================
	// =========================================================================

	protected abstract void initialize();

	protected abstract void run();

	protected abstract void cleanup();

	// =========================================================================
	// === LOGGING =============================================================
	// =========================================================================

	private static final Logger log
		= Logger.getLogger( Application.class.getName() );
}
