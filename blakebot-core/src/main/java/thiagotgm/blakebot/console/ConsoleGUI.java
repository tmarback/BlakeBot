package thiagotgm.blakebot.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
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
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        log.info( "Console started." );

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

        JPanel buttons = new JPanel();
        buttons.add( exitButton );
        getContentPane().add( buttons, BorderLayout.SOUTH );

        // Displays the console.
        setSize( 1000, 800 );
        setVisible( true );

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
