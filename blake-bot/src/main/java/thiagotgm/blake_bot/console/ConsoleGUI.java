package thiagotgm.blake_bot.console;

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

import thiagotgm.blake_bot.Bot;

/**
 * GUI for managing the bot.
 * 
 * @author ThiagoTGM
 * @version 0.1
 * @since 2016-12-28
 */
public class ConsoleGUI extends JFrame {

    private static final long serialVersionUID = 7890114311672131502L;
    private static final Logger log = LoggerFactory.getLogger( ConsoleGUI.class );

    private final Bot bot;

    public ConsoleGUI( Bot bot ) {

        super( "BlakeBot Console" );
        this.bot = bot;
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        log.info( "Console started." );

        JTextPane output = new JTextPane();
        DefaultCaret caret = (DefaultCaret) output.getCaret();
        caret.setUpdatePolicy( DefaultCaret.ALWAYS_UPDATE );
        JScrollPane scrollPane = new JScrollPane( output );
        getContentPane().add( scrollPane, BorderLayout.CENTER );
        redirectOutStream( output );
        redirectErrStream( output );

        JButton exitButton = new JButton( "Terminate" );
        exitButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                if ( bot.isConnected() ) {
                    bot.terminate();
                    log.info( "Bot terminated." );
                }

            }

        } );

        JPanel buttons = new JPanel();
        buttons.add( exitButton );
        getContentPane().add( buttons, BorderLayout.SOUTH );

        setSize( 1000, 800 );
        setVisible( true );

    } 

    private void redirectOutStream( JTextPane output ) {
        
        OutputStream out = new TerminalStream( output, Color.BLACK );
        System.setOut( new PrintStream( out, true ) );
        
    }
    
    private void redirectErrStream( JTextPane output ) {
        
        OutputStream out = new TerminalStream( output, Color.RED );
        System.setErr( new PrintStream( out, true ) );
        
    }

}
