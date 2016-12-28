package thiagotgm.blake_bot.console;

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


class TerminalStream extends OutputStream {

    private static final String STYLE_NAME = "Output style";
    private static final Logger log = LoggerFactory.getLogger( TerminalStream.class );
    
    private final Color textColor;
    private final JTextPane output;
    private final Style outStyle;
    
    public TerminalStream( JTextPane output, Color textColor ) {
        
        this.textColor = textColor;
        this.output = output;
        outStyle = output.addStyle( STYLE_NAME, null );
        
    }
    
    private void appendString( String str, Color color ) {
        
        StyledDocument document = (StyledDocument) output.getDocument();
        StyleConstants.setForeground( outStyle, color );
        try {
            document.insertString( document.getLength(), str, outStyle );
        } catch ( BadLocationException e ) {
            log.error( "Could not write to text area." );
        }
                                                     
    }
    
    @Override
    public void write( final int b ) throws IOException {
        
        appendString( String.valueOf( (char) b ), textColor );
        
    }
        
    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
        
        appendString( new String( b, off, len ), textColor );
        
    }

    @Override
    public void write(byte[] b) throws IOException {
        
        write( b, 0, b.length );
        
    }

}
