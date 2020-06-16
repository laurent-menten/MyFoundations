package be.lmenten.utils.swing.logging;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JLogHandlerFrame
	extends JFrame
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------

	private JTable logTable;
	private JScrollPane scrollPane;

	private LogRecordTableModel model;

	// ========================================================================
	// ===
	// ========================================================================

	public JLogHandlerFrame()
	{
		setTitle( "Logging" );
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent e )
			{
				String message
					=	"<html>"
					+		"<b>Yes</b>: close application<br />"
					+		"<b>No</b>: close only window<br />"
					+		"<b>Cancel</b>: do nothing<br />"
					+	"</html>"
					;

				 int rc = JOptionPane.showConfirmDialog( null,
						 message, "Alert",
						 JOptionPane.YES_NO_CANCEL_OPTION );

				 if( rc == JOptionPane.YES_OPTION )
				 {
System.out.println( "exit" );
					 System.exit( 0 );
				 }
				 else if( rc == JOptionPane.NO_OPTION )
				 {
System.out.println( "hide" );
					 setVisible( false );
				 }
				 else
				 {
System.out.println( "do nothing" );
					 //do nothing
				 }
			}
		} );
	
		model = new DefaultLogRecordTableModel();

		logTable = new JTable( model );
		logTable.setFillsViewportHeight( true );
		logTable.setDefaultRenderer( Level.class, new LevelRenderer() );

		logTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		for( int i = 0 ; i < model.getColumnCount() ; i++ )
		{
			int width = model.getColumnWidth( i );
			logTable.getColumnModel().getColumn( i ).setPreferredWidth( width );
		}

		scrollPane = new JScrollPane( logTable );

		getContentPane().add( scrollPane, BorderLayout.CENTER );
		pack();
	}

	 // ========================================================================
	// ===
	// ========================================================================

	public Handler getHandler()
	{
		return logHandler;
	}

	public void installHandler()
	{
		Logger.getLogger( "" ).addHandler( logHandler );		
	}

	// ------------------------------------------------------------------------

	private final Handler logHandler = new Handler()
	{
		@Override
		public void publish( LogRecord record )
		{
			if( this.isLoggable( record ) )
			{
				JLogHandlerFrame.this.model.addRecord( record );
			}
		}
		
		@Override
		public void flush()
		{
		}
		
		@Override
		public void close()
			throws SecurityException
		{
		}
	};
}
