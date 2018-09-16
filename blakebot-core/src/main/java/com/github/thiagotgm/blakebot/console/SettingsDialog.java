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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.Bot;
import com.github.thiagotgm.bot_utils.Settings;
import com.github.thiagotgm.bot_utils.storage.DatabaseManager;
import com.github.thiagotgm.bot_utils.storage.DatabaseManager.DatabaseType;
import com.github.thiagotgm.bot_utils.storage.impl.AbstractDatabase;

/**
 * Panel that provides a way to change bot settings.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-03
 */
public class SettingsDialog extends JDialog {
	
	/**
	 * UID that represents this class.
	 */
	private static final long serialVersionUID = -7466923438781284439L;

	private static final Logger LOG = LoggerFactory.getLogger( SettingsDialog.class );
	
	private static final int BORDER_SIZE = 20;
	private static final int SETTING_SPACING = 30;
	private static final int FIELD_SPACING = 10;
	private static final int BUTTON_SPACING = 30;
	
	private boolean save = false;
	
	private final JTextField tokenField;
	private final JLabel tokenMissing;
	private final JTextField cacheField;
	private final JLabel cacheMissing;
	private final JLabel cacheInvalid;
	
	/**
	 * Instantiates a dialog owned by the given frame.
	 * 
	 * @param owner The frame that owns this dialog.
	 */
	public SettingsDialog( Frame owner ) {
		
		super( owner, "Settings", true );
		
		LOG.debug( "Opening settings menu." );
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		contentPane.setBorder( BorderFactory.createEmptyBorder( BORDER_SIZE, BORDER_SIZE,
				                                                BORDER_SIZE, BORDER_SIZE ) );
		getContentPane().add( contentPane, BorderLayout.CENTER );
		
		JLabel warning = new JLabel( "NOTE: changes are not applied until the next time "
				+ "the program is started.", JLabel.CENTER );
		warning.setForeground( Color.RED );
		warning.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( warning );
		
		/* Create setting inputs */
		
		// Bot token.
		contentPane.add( Box.createVerticalStrut( SETTING_SPACING ) );
		
		JLabel tokenLabel = new JLabel( "Bot token" );
		tokenLabel.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( tokenLabel );
		contentPane.add( Box.createVerticalStrut( FIELD_SPACING ) );
		tokenField = new JTextField( Settings.getStringSetting( Bot.LOGIN_TOKEN_SETTING ) );
		tokenField.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( tokenField );
		
		tokenMissing = new JLabel( "Please enter a token." );
		tokenMissing.setForeground( Color.RED );
		tokenMissing.setAlignmentX( LEFT_ALIGNMENT );
		tokenMissing.setVisible( false );
		contentPane.add( tokenMissing );
		
		contentPane.add( Box.createVerticalStrut( SETTING_SPACING ) );
		
		// Cache size.
		JLabel cacheLabel = new JLabel( "Database cache size" );
		cacheLabel.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( cacheLabel );
		contentPane.add( Box.createVerticalStrut( FIELD_SPACING ) );
		cacheField = new JTextField( Settings.getStringSetting( AbstractDatabase.CACHE_SETTING ) );
		cacheField.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( cacheField );
		
		cacheMissing = new JLabel( "Please enter a cache size." );
		cacheMissing.setForeground( Color.RED );
		cacheMissing.setAlignmentX( LEFT_ALIGNMENT );
		cacheMissing.setVisible( false );
		contentPane.add( cacheMissing );
		cacheInvalid = new JLabel( "Please enter a valid size." );
		cacheInvalid.setForeground( Color.RED );
		cacheInvalid.setAlignmentX( LEFT_ALIGNMENT );
		cacheInvalid.setVisible( false );
		contentPane.add( cacheInvalid );
		
		// Database change.
		contentPane.add( Box.createVerticalStrut( SETTING_SPACING ) );
		
		DatabaseType changeType = DatabaseManager.getDatabaseChangeRequestType();
		JLabel databaseLabel = new JLabel( changeType == null ? "No database change pending.":
																"Database changing to: " + changeType );
		contentPane.add( databaseLabel );
		contentPane.add( Box.createVerticalStrut( FIELD_SPACING ) );
		JButton databaseChange = new JButton( "Change" );
		databaseChange.addActionListener( ( e ) -> {
			
			new DatabaseChangeDialog( this );
			DatabaseType type = DatabaseManager.getDatabaseChangeRequestType();
			databaseLabel.setText( type == null ? "No database change pending.":
				                                  "Database changing to: " + type );
			
		});
		JButton databaseCancel = new JButton( "Cancel" );
		databaseCancel.addActionListener( ( e ) -> {
			
			if ( DatabaseManager.cancelDatabaseChange() ) {
				databaseLabel.setText( "No database change pending." );
			}
			
		});
		JPanel databasePanel = new JPanel();
		databasePanel.setAlignmentX( LEFT_ALIGNMENT );
		databasePanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		databasePanel.add( databaseChange );
		databasePanel.add( databaseCancel );
		contentPane.add( databasePanel );
		
		/* Create exit buttons */
		
		contentPane.add( Box.createVerticalStrut( SETTING_SPACING ) );
		
		JButton saveButton = new JButton( "Save" );
		saveButton.addActionListener( ( e ) -> {
			
			SettingsDialog.this.save = true; // Need to save settings.
			SettingsDialog.this.dispatchEvent( new WindowEvent( // Close dialog.
					SettingsDialog.this, WindowEvent.WINDOW_CLOSING ) );
			
		});
		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( ( e ) -> {
			
			SettingsDialog.this.dispatchEvent( new WindowEvent( // Close dialog.
					SettingsDialog.this, WindowEvent.WINDOW_CLOSING ) );
			
		});
		
		JPanel exitPane = new JPanel();
		exitPane.setLayout( new BoxLayout( exitPane, BoxLayout.X_AXIS ) );
		exitPane.add( Box.createHorizontalGlue() );
		exitPane.add( saveButton );
		exitPane.add( Box.createHorizontalStrut( BUTTON_SPACING ) );
		exitPane.add( cancelButton );
		exitPane.add( Box.createHorizontalGlue() );
		exitPane.setAlignmentX( LEFT_ALIGNMENT );
		
		contentPane.add( exitPane );
		
		contentPane.add( Box.createVerticalStrut( SETTING_SPACING ) );
		
		JLabel cancelWarning = new JLabel( "Pressing 'Cancel' (or closing "
				+ "this window) will abort any setting changes, EXCEPT for "
				+ "any placed Database change requests.", JLabel.CENTER );
		cancelWarning.setForeground( Color.RED );
		cancelWarning.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( cancelWarning );
		
		contentPane.add( Box.createVerticalGlue() );
		
		/* Add closing handler */
		
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		
		addWindowListener( new WindowAdapter() {

            @Override
            public void windowClosing( WindowEvent arg0 ) {

                LOG.debug( "Closing settings menu." );
                if ( save ) {
                	LOG.info( "Saving new settings." );
                	boolean error = false;
                	
                	String token = tokenField.getText();
                	if ( ( token == null ) || token.isEmpty() ) {
                		tokenMissing.setVisible( true );
                		error = true;
                	} else {
                		tokenMissing.setVisible( false );
                	}
                	
                	String cacheSizeStr = cacheField.getText();
                	int cacheSize = 0;
                	if ( ( cacheSizeStr == null ) || cacheSizeStr.isEmpty() ) {
                		cacheMissing.setVisible( true );
                		cacheInvalid.setVisible( false );
                		error = true;
                	} else {
                		cacheMissing.setVisible( false );
                		try {
                			cacheSize = Integer.parseInt( cacheSizeStr );
                			cacheInvalid.setVisible( false );
                		} catch ( NumberFormatException e )  {
                			cacheInvalid.setVisible( true );
                			error = true;
                		}
                	}
                	
                	if ( error ) { // Found errors, abort closing.
                		LOG.error( "Error in inputted settings. Aborting close." );
                		pack();
                		save = false;
                		return; // Try again.
                	} else { // Save settings.
                		Settings.setSetting( Bot.LOGIN_TOKEN_SETTING, token );
                		Settings.setSetting( AbstractDatabase.CACHE_SETTING, cacheSize );
                		LOG.debug( "Saved settings." );
                	}
                } else {
                	LOG.debug( "Aborting changes." );
                }
                SettingsDialog.this.setVisible( false );
                SettingsDialog.this.dispose();
                
            }
            
        });
		
		pack();
		setVisible( true );
		
	}

}
