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
 * OutputStream that outputs to a GUI terminal in the form of a JTextPane, using
 * a given text color. A backup stream may be specified to also receive the
 * write requests sent to this stream.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-28
 */
class TerminalStream extends OutputStream {

    private static final Logger log = LoggerFactory.getLogger( TerminalStream.class );

    private final OutputStream backupOut;
    private final JTextPane output;
    private final Style outStyle;

    private boolean closed;

    /**
     * Initializes a new instance that outputs to a given TextPane using a given
     * text color, and uses the given backup stream.
     * <p>
     * Output will be sent to the backup stream <b>before</b> outputting to the
     * pane. If the backup stream throws an exception, this stream will bubble it
     * up.
     * 
     * @param output
     *            JTextPane that the stream outputs to.
     * @param textColor
     *            Color used for the output text.
     * @param backupOut
     *            Backup stream to where output should also be directed to along
     *            with printing to the pane. Normally, the output will appear in
     *            both the pane and the stream. If <tt>null</tt>, no redirection is
     *            made.
     */
    public TerminalStream( JTextPane output, Color textColor, OutputStream backupOut ) {

        this.backupOut = backupOut;
        this.output = output;
        outStyle = output.addStyle( null, null );
        StyleConstants.setForeground( outStyle, textColor );
        this.closed = false;

    }

    /**
     * Initializes a new instance that outputs to a given TextPane using a given
     * text color.
     * 
     * @param output
     *            JTextPane that the stream outputs to.
     * @param textColor
     *            Color used for the output text.
     */
    public TerminalStream( JTextPane output, Color textColor ) {

        this( output, textColor, null );

    }

    /**
     * Appends a string to the output panel with the defined color.
     * 
     * @param str
     *            String to be appended.
     */
    private void appendString( String str ) {

        StyledDocument document = (StyledDocument) output.getDocument();
        try {
            document.insertString( document.getLength(), str, outStyle );
        } catch ( BadLocationException e ) {
            log.error( "Could not write to text area." );
        }

    }

    /**
     * Checks if the stream is already closed. If it is, throws an exception.
     *
     * @throws IOException
     *             if the stream is already closed.
     */
    private void checkState() throws IOException {

        if ( closed ) {
            throw new IOException( "Stream is already closed." );
        }

    }

    @Override
    public void write( final int b ) throws IOException {

        checkState();
        if ( backupOut != null ) {
            backupOut.write( b );
        }
        appendString( String.valueOf( (char) b ) );

    }

    @Override
    public void write( byte[] b, int off, int len ) throws IOException {

        checkState();
        if ( backupOut != null ) {
            backupOut.write( b, off, len );
        }
        appendString( new String( b, off, len ) );

    }

    @Override
    public void write( byte[] b ) throws IOException {

        write( b, 0, b.length );

    }

    @Override
    public void close() throws IOException {

        closed = true;
        if ( backupOut != null ) {
            backupOut.close();
        }

    }

    @Override
    public void flush() throws IOException {

        if ( backupOut != null ) {
            backupOut.flush();
        }

    }

}
