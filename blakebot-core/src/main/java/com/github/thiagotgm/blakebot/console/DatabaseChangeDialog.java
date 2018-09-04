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
import java.awt.Dialog;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.Database.Parameter;
import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;
import com.github.thiagotgm.blakebot.common.storage.DatabaseManager.DatabaseType;

/**
 * Panel that provides a way to request a database change.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-03
 */
public class DatabaseChangeDialog extends JDialog {
	
	/**
	 * UID that represents this class.
	 */
	private static final long serialVersionUID = -7110306896626785435L;

	private static final Logger LOG = LoggerFactory.getLogger( DatabaseChangeDialog.class );
	
	private static final int BORDER_SIZE = 20;
	private static final int SETTING_SPACING = 30;
	private static final int FIELD_SPACING = 10;
	private static final int BUTTON_SPACING = 30;
	
	private static final DatabaseType[] TYPES = DatabaseType.values();
	private static final String[] TYPE_NAMES = new String[TYPES.length];
	
	static { // Obtain type names.
		
		for ( int i = 0; i < TYPES.length; i++ ) {
			
			TYPE_NAMES[i] = TYPES[i].getName();
			
		}
		
	}
	
	private boolean request = false;
	
	private final JComboBox<String> typePicker;
	private final JLabel errorMessage;
	private final JPanel infoPanel;
	
	private DatabaseType type;
	private List<Parameter> params;
	private List<JLabel> paramNames;
	private List<JComponent> arguments;
	
	private final ItemListener nameUpdater = ( e ) -> {
		
		setParameterNames(); // Update parameter names.
		
	};
	
	/**
	 * Instantiates a dialog owned by the given dialog.
	 * 
	 * @param owner The dialog that owns this dialog.
	 */
	public DatabaseChangeDialog( Dialog owner ) {
		
		super( owner, "Database Change Request", true );
		
		LOG.debug( "Opening database change request menu." );
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		contentPane.setBorder( BorderFactory.createEmptyBorder( BORDER_SIZE, BORDER_SIZE,
				                                                BORDER_SIZE, BORDER_SIZE ) );
		getContentPane().add( contentPane, BorderLayout.CENTER );
		
		typePicker = new JComboBox<>( TYPE_NAMES );
		typePicker.setEditable( false );
		typePicker.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( typePicker );
		
		// Listener to update the panel when the type changes.
		typePicker.addItemListener( ( e ) -> {
			
			setInfoPanel(); // Update panel.
			
		});
		
		/* Make input error message */
		
		errorMessage = new JLabel( "Could not load database with the given parameters.",
				JLabel.CENTER );
		errorMessage.setForeground( Color.RED );
		errorMessage.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( errorMessage );
		
		/* Create type picker */
		
		/* Create panel for taking input */
		
		infoPanel = new JPanel();
		infoPanel.setLayout( new BoxLayout( infoPanel, BoxLayout.Y_AXIS ) );
		infoPanel.setAlignmentX( LEFT_ALIGNMENT );
		setInfoPanel();
		contentPane.add( infoPanel );
		
		/* Create exit buttons */
		
		contentPane.add( Box.createVerticalStrut( SETTING_SPACING ) );
		
		JButton saveButton = new JButton( "Save" );
		saveButton.addActionListener( ( e ) -> {
			
			request = true;
			DatabaseChangeDialog.this.dispatchEvent( new WindowEvent( // Close dialog.
					DatabaseChangeDialog.this, WindowEvent.WINDOW_CLOSING ) );
			
		});
		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( ( e ) -> {
			
			DatabaseChangeDialog.this.dispatchEvent( new WindowEvent( // Close dialog.
					DatabaseChangeDialog.this, WindowEvent.WINDOW_CLOSING ) );
			
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
		
		JLabel cancelWarning = new JLabel( "The database will be migrated when "
				+ "the program is closed.", JLabel.CENTER );
		cancelWarning.setForeground( Color.RED );
		cancelWarning.setAlignmentX( LEFT_ALIGNMENT );
		contentPane.add( cancelWarning );
		
		contentPane.add( Box.createVerticalGlue() );
		
		/* Add closing handler */
		
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		
		addWindowListener( new WindowAdapter() {

            @Override
            public void windowClosing( WindowEvent arg0 ) {

            	if ( request ) {
            		List<String> args = new ArrayList<>( params.size() );
            		for ( JComponent input : arguments ) {
            			
            			if ( input instanceof JComboBox ) {
            				args.add( (String) ( (JComboBox<?>) input ).getSelectedItem() );
            			} else {
            				String arg = ( (JTextField) input ).getText();
            				args.add( arg == null ? "" : arg ); // Count null as blank.
            			}
            			
            		}
            		
            		// Attempt to request database change.
            		if ( !DatabaseManager.requestDatabaseChange( type, args ) ) {
            			// Request failed.
            			errorMessage.setVisible( true ); // Show error message.
            			request = false;
            			return; // Abort closing.
            		}
            	}
            	
            	setVisible( false );
            	dispose();
            	
            }
            
        });
		
		pack();
		setVisible( true );
		
	}
	
	/**
	 * Sets the info panel to have the necessary fields to receive the arguments
	 * for the currently selected database type.
	 */
	private void setInfoPanel() {
		
		infoPanel.removeAll(); // Remove previous entry fields.
		errorMessage.setVisible( false ); // Hide error message.
		
		type = TYPES[typePicker.getSelectedIndex()];
		LOG.trace( "{} database selected.", type.getName() );
		
		params = type.getLoadParams();
		paramNames = new ArrayList<>( params.size() );
		arguments = new ArrayList<>( params.size() );
		for ( Parameter param : params ) {
			
			infoPanel.add( Box.createVerticalStrut( SETTING_SPACING ) );
			
			JLabel label = new JLabel( param.getName( 0 ) );
			label.setAlignmentX( LEFT_ALIGNMENT );
			paramNames.add( label );
			infoPanel.add( label );
			
			infoPanel.add( Box.createVerticalStrut( FIELD_SPACING ) );
			
			JComponent input;
			if ( param.getChoices() == null ) { // Text input.
				input = new JTextField();
			} else { // Choice between set values.
				JComboBox<String> argPicker = new JComboBox<>(
						param.getChoices().toArray( new String[0] ) );
				argPicker.addItemListener( nameUpdater ); // Set to update names on change.
				input = argPicker;
			}
			input.setAlignmentX( LEFT_ALIGNMENT );
			arguments.add( input );
			infoPanel.add( input );
			
		}
		
		pack(); // Recalculate window.
		
	}
	
	/**
	 * Updates the parameter names according to the current argument selections.
	 */
	private void setParameterNames() {
		
		int lastChoice = 0;
		Iterator<Parameter> paramIter = params.iterator();
		Iterator<JLabel> nameIter = paramNames.iterator();
		Iterator<JComponent> argIter = arguments.iterator();
		while ( paramIter.hasNext() ) {
			
			nameIter.next().setText( paramIter.next().getName( lastChoice ) );
			JComponent curArg = argIter.next();
			if ( curArg instanceof JComboBox ) { // Cur arg is a choice.
				lastChoice = ( (JComboBox<?>) curArg ).getSelectedIndex();
			}
			
		}
		
		pack(); // Recalculate window.
		
	}
	
}
