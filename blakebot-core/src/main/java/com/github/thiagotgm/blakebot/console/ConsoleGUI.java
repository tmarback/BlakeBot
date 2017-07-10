/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlakeBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.Bot;
import com.github.thiagotgm.blakebot.ConnectionStatusListener;
import com.github.thiagotgm.blakebot.PropertyNames;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * GUI used for server-side management of the bot.
 * Uses a Singleton pattern (only a single instance can exist).
 * Instance can only be started if the bot is ready to be started.
 * @see com.github.thiagotgm.blakebot.Bot Bot
 * 
 * @author ThiagoTGM
 * @version 2.0.0
 * @since 2016-12-28
 */
public class ConsoleGUI extends JFrame implements ConnectionStatusListener {

    private static final long serialVersionUID = 7890114311672131502L;
    private static final Logger log = LoggerFactory.getLogger( ConsoleGUI.class );

    private final JButton connectionButton;
    private final JButton nameButton;
    private final JButton statusButton;
    private final JButton presenceButton;
    private final JButton imageButton;
    
    private static ConsoleGUI instance = null;

    /**
     * Creates a GUI that manages the bot instance.
     * 
     * @throws IllegalStateException if the bot is not ready to be initialized.
     */
    private ConsoleGUI() throws IllegalStateException {
        
        // Initializes the console.
        super( "BlakeBot Console" );
        final Bot bot = Bot.getInstance();
        setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {

            @Override
            public void windowClosed( WindowEvent arg0 ) {

                log.info( "Console closed. Exiting." );
                bot.saveProperties();
                System.exit( 0 );
                
            }

            @Override
            public void windowClosing( WindowEvent arg0 ) {

                log.debug( "Closing console." );
                if ( bot.isConnected() ) {
                    try {
                        bot.terminate();
                    } catch ( DiscordException e ) {
                        // Failed to disconnect.
                        return;
                    }
                }
                ConsoleGUI.this.setVisible( false );
                ConsoleGUI.this.dispose();
                
            }
            
        });
        log.debug( "Console created." );

        // Creates the output terminal.
        JTextPane output = new JTextPane();
        JScrollPane scrollPane = new JScrollPane( output );
        getContentPane().add( scrollPane, BorderLayout.CENTER );
        redirectOutStream( output );
        redirectErrStream( output );

        // Creates the command buttons.
        connectionButton = new JButton( "Connect" );
        connectionButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                connectionButton.setEnabled( false );
                if ( bot.isConnected() ) {
                    // Disconnects the bot.
                    try {
                        bot.terminate();
                    } catch ( DiscordException e ) {
                        connectionButton.setEnabled( true );
                    }
                } else {
                    // Connects the bot.
                    try {
                        bot.login();
                    } catch ( DiscordException | RateLimitException e ) {
                        connectionButton.setEnabled( true );
                    }
                }

            }

        } );
        nameButton = new JButton( "Change username" );
        nameButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                // Changes name of the bot.
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
        statusButton = new JButton( "Change status" );
        statusButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                // Changes status of the bot.
                String newStatus = (String) JOptionPane.showInputDialog( 
                        ConsoleGUI.this,
                        "Please input a new status.",
                        "New Status",
                        JOptionPane.QUESTION_MESSAGE );
                while ( ( bot.getStatus() != null ) && ( bot.getStatus().equals( newStatus ) ) ) {
                    
                    newStatus = (String) JOptionPane.showInputDialog( 
                            ConsoleGUI.this,
                            "That is the current status!\n" +
                            "Please input a new status.",
                            "New Status",
                            JOptionPane.QUESTION_MESSAGE );
                    
                }
                if ( ( newStatus != null ) && ( newStatus.length() > 0 ) ) {
                    bot.setStatus( newStatus );
                } else {
                    log.debug( "Status change cancelled." );
                }
                
            }
            
        });
        presenceButton = new JButton( "Change presence" );
        presenceButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {

                // Changes presence of the bot.
                String[] options = { "Online", "Idle" };
                int choice = JOptionPane.showOptionDialog(
                        ConsoleGUI.this,
                        "Please choose a presence.",
                        "Presence Picker",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0] );
                if ( choice == JOptionPane.YES_OPTION ) {
                    bot.setIdle( false );
                } else if ( choice == JOptionPane.NO_OPTION ) {
                    bot.setIdle( true );
                } else {
                    log.debug( "Presence change cancelled." );
                }
                
            }
            
        });
        imageButton = new JButton( "Change profile image" );
        imageButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent ev ) {

                // Changes image of the bot.
                String[] options = { "Local File", "URL" };
                int choice = JOptionPane.showOptionDialog(
                        ConsoleGUI.this,
                        "Please choose where the image should be taken from.",
                        "Origin Picker",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0] );
                if ( choice == JOptionPane.YES_OPTION ) {
                    // Image from local file.
                    String path = (String) JOptionPane.showInputDialog( 
                            ConsoleGUI.this,
                            "Please input a file path.",
                            "File Input",
                            JOptionPane.QUESTION_MESSAGE );
                    if ( ( path != null ) && ( path.length() > 0 ) ) {
                        bot.setImage( new File( path ) );
                        return;
                    }
                } else if ( choice == JOptionPane.NO_OPTION ) {
                    // Image from URL.
                    String url = (String) JOptionPane.showInputDialog( 
                            ConsoleGUI.this,
                            "Please input a URL.",
                            "URL Input",
                            JOptionPane.QUESTION_MESSAGE );
                    if ( ( url != null ) && ( url.length() > 0 ) ) {
                        try {
                            bot.setImage( url );
                        } catch ( IllegalArgumentException e ) {
                            // Do nothing.
                        }
                        return;
                    }
                }
                log.debug( "Image change cancelled." );
                
            }
            
        });

        // Organizes the buttons in a panel.
        JPanel buttons = new JPanel();
        buttons.add( connectionButton );
        buttons.add( nameButton );
        buttons.add( statusButton );
        buttons.add( presenceButton );
        buttons.add( imageButton );
        getContentPane().add( buttons, BorderLayout.SOUTH );
        setButtonsEnabled( false ); // Needs to connect before using command buttons.

        // Displays the console.
        int width = Integer.valueOf( Bot.getProperties().getProperty( PropertyNames.CONSOLE_WIDTH ) );
        int height = Integer.valueOf( Bot.getProperties().getProperty( PropertyNames.CONSOLE_HEIGHT ) );
        setSize( width, height );
        addComponentListener( new ComponentAdapter() {

            @Override
            public void componentResized( ComponentEvent ev ) {

                Bot.getProperties().setProperty( PropertyNames.CONSOLE_WIDTH,
                        String.valueOf( ConsoleGUI.this.getWidth() ) );
                Bot.getProperties().setProperty( PropertyNames.CONSOLE_HEIGHT,
                        String.valueOf( ConsoleGUI.this.getHeight() ) );
                
            }

        });
        setVisible( true );
        log.info( "Console started." );

    }
    
    /**
     * Gets the running instance of the console. If one is not currently running,
     * creates a new one.
     * The bot must be ready for initialization before the console can be
     * started. See {@link com.github.thiagotgm.blakebot.Bot#getInstance() Bot.getInstance}
     * for details on requirements for bot startup.
     * 
     * @return The running instance of the bot.
     * @throws IllegalStateException if the bot is not ready to be initialized.
     */
    public static ConsoleGUI getInstance() throws IllegalStateException {
       
        if ( instance == null ) {
            instance = new ConsoleGUI();
        }
        return instance;
        
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
    
    /**
     * Enables or disables all buttons other than the connect/disconnect button.
     * 
     * @param enabled If true, all buttons become enabled. Otherwise, they
     *                all become disabled.
     */
    private void setButtonsEnabled( boolean enabled ) {
        
        nameButton.setEnabled( enabled );
        statusButton.setEnabled( enabled );
        presenceButton.setEnabled( enabled );
        imageButton.setEnabled( enabled );
        
    }
    
    /**
     * If the bot becomes connected or disconnected, changes the button panel
     * accordingly.
     * 
     * @param isConnected If true,bot became connected. All control buttons
     *                    are enabled and connect/disconnect button shows
     *                    "Disconnect" option.
     *                    Otherwise, bot became disconnected. 
     *                    Only connect/disconnect button is enabled,
     *                    and it shows "Connect" option.
     */
    @Override
    public void connectionChange( boolean isConnected ) {
        
        setButtonsEnabled( isConnected );
        if ( isConnected ) {
            connectionButton.setText( "Disconnect" );
        } else {
            connectionButton.setText( "Connect" );
        }
        connectionButton.setEnabled( true );
        
    }

}
