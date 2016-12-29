package thiagotgm.blakebot.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thiagotgm.blakebot.Bot;

/**
 * GUI used for server-side management of the bot.
 * 
 * @author ThiagoTGM
 * @version 0.1
 * @since 2016-12-28
 */
public class ConsoleGUI extends JFrame {

    private static final long serialVersionUID = 7890114311672131502L;
    private static final Logger log = LoggerFactory.getLogger( ConsoleGUI.class );

    private final Bot bot;

    /**
     * Creates a GUI that manages a given bot instance.
     * 
     * @param bot Bot to be managed by the GUI instance.
     */
    public ConsoleGUI( Bot bot ) {

        // Initializes the console.
        super( "BlakeBot Console" );
        this.bot = bot;
        setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowListener() {

            @Override
            public void windowActivated( WindowEvent arg0 ) {

                // Do nothing
                
            }

            @Override
            public void windowClosed( WindowEvent arg0 ) {

                log.info( "Console closed. Exiting." );
                System.exit( 0 );
                
            }

            @Override
            public void windowClosing( WindowEvent arg0 ) {

                log.debug( "Closing console." );
                if ( bot.isConnected() ) {
                    bot.terminate();
                    if ( bot.isConnected() ) {
                        // Failed to disconnect.
                        return;
                    }
                }
                ConsoleGUI.this.setVisible( false );
                ConsoleGUI.this.dispose();
                
            }

            @Override
            public void windowDeactivated( WindowEvent arg0 ) {

                // Do nothing
                
            }

            @Override
            public void windowDeiconified( WindowEvent arg0 ) {

                // Do nothing
                
            }

            @Override
            public void windowIconified( WindowEvent arg0 ) {

                // Do nothing
                
            }

            @Override
            public void windowOpened( WindowEvent arg0 ) {

                // Do nothing
                
            }
            
        });
        log.debug( "Console created." );

        // Creates the output terminal.
        JTextPane output = new JTextPane();
        DefaultCaret caret = (DefaultCaret) output.getCaret();
        caret.setUpdatePolicy( DefaultCaret.ALWAYS_UPDATE );
        JScrollPane scrollPane = new JScrollPane( output );
        getContentPane().add( scrollPane, BorderLayout.CENTER );
        redirectOutStream( output );
        redirectErrStream( output );

        // Creates the command buttons.
        JButton exitButton = new JButton( "Terminate" );
        exitButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                // Shuts down the bot.
                if ( bot.isConnected() ) {
                    bot.terminate();
                }

            }

        } );
        JButton nameButton = new JButton( "Change username" );
        nameButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                String newName = (String) JOptionPane.showInputDialog( 
                        ConsoleGUI.this,
                        "Please input a new username.",
                        "New Username",
                        JOptionPane.QUESTION_MESSAGE );
                while ( bot.getUsername().equals( newName ) ) {
                    
                    newName = (String) JOptionPane.showInputDialog( 
                            ConsoleGUI.this,
                            "That is the current name!\n" +
                            "Please input a new username.",
                            "New Username",
                            JOptionPane.QUESTION_MESSAGE );
                    
                }
                if ( ( newName != null ) && ( newName.length() > 0 ) ) {
                    bot.setUsername( newName );
                } else {
                    log.debug( "Name change cancelled." );
                }
                
            }
            
        });

        JPanel buttons = new JPanel();
        buttons.add( exitButton );
        buttons.add( nameButton );
        getContentPane().add( buttons, BorderLayout.SOUTH );

        // Displays the console.
        setSize( 1000, 800 );
        setVisible( true );
        log.info( "Console started." );

    } 

    /**
     * Redirects stdout to a given JTextPane, using black for the text
     * color.
     * 
     * @param output Pane where stdout should be redirected to.
     */
    private void redirectOutStream( JTextPane output ) {
        
        OutputStream out = new TerminalStream( output, Color.BLACK );
        System.setOut( new PrintStream( out, true ) );
        
    }
    
    /**
     * Redirects stderr to a given JTextPane, using red for the text
     * color.
     * 
     * @param output Pane where stderr should be redirected to.
     */
    private void redirectErrStream( JTextPane output ) {
        
        OutputStream out = new TerminalStream( output, Color.RED );
        System.setErr( new PrintStream( out, true ) );
        
    }

}
