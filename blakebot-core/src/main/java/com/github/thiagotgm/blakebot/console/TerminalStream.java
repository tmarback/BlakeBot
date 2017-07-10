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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OutputStream that outputs to a GUI terminal in the form of a JTextPane,
 * using a given text color.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-28
 */
class TerminalStream extends OutputStream {

    private static final Logger log = LoggerFactory.getLogger( TerminalStream.class );
    
    private final Color textColor;
    private final JTextPane output;
    private final Style outStyle;
    
    /**
     * Initializes a new instance that outputs to a given TextPane using a
     * given text color.
     * 
     * @param output JTextPane that the stream outputs to.
     * @param textColor Color used for the output text.
     */
    public TerminalStream( JTextPane output, Color textColor ) {
        
        this.textColor = textColor;
        this.output = output;
        outStyle = output.addStyle( null, null );
        
    }
    
    /**
     * Appends a string to the output panel with the defined color.
     * 
     * @param str String to be appended.
     */
    private void appendString( String str ) {
        
        StyledDocument document = (StyledDocument) output.getDocument();
        StyleConstants.setForeground( outStyle, textColor );
        try {
            document.insertString( document.getLength(), str, outStyle );
        } catch ( BadLocationException e ) {
            log.error( "Could not write to text area." );
        }
                                                     
    }
    
    @Override
    public void write( final int b ) throws IOException {
        
        appendString( String.valueOf( (char) b ) );
        
    }
        
    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
        
        appendString( new String( b, off, len ) );
        
    }

    @Override
    public void write(byte[] b) throws IOException {
        
        write( b, 0, b.length );
        
    }

}
