package com.github.thiagotgm.blakebot.module.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that keeps record of the blacklist for all servers.<br>
 * Uses a Singleton pattern.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class Blacklist {
    
    private static final String FILENAME = "Blacklist.xml";
    private static final String FILEPATH = Paths.get( "data" ).toString();
    private static final String PATH = Paths.get( FILEPATH, FILENAME ).toString();
    private static final String ROOT_TAG = "blacklist";
    
    private static final Logger log = LoggerFactory.getLogger( Blacklist.class );
    
    private final Document document;
    private final Element root;
    
    private static Blacklist instance;
    
    /**
     * Creates a new instance using data loaded from the blacklist file.<br>
     * If the file doesn't exist, starts a new document.
     */
    private Blacklist() {
        
        Document document = loadDocument();
        this.document = ( document == null ) ? newDocument() : document;
        this.root = this.document.getRootElement(); 
        
    }
    
    /**
     * Returns the current instance. If there isn't one, creates it.
     *
     * @return The Blacklist instance.
     */
    public static Blacklist getInstance() {
        
        if ( instance == null ) {
            instance = new Blacklist();
        }
        return instance;
        
    }
    
    /**
     * Creates a new document to represent the blacklist.
     *
     * @return The newly created document.
     */
    private Document newDocument() {
        
        Document document = DocumentHelper.createDocument();
        document.addElement( ROOT_TAG );
        return document;
        
    }
    
    /**
     * Loads an existing blacklist document.
     *
     * @return The loaded document.
     */
    private Document loadDocument() {
        
        File inputFile = new File( PATH );
        if ( !inputFile.exists() ) {
            return null;
        }
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read( inputFile );
        } catch ( DocumentException e ) {
            log.error( "Failed to read blacklist document.", e );
        }
        return document;
        
    }
    
    /**
     * Writes blacklist document to file.
     */
    public void saveDocument() {
        
        try {
            FileOutputStream output = new FileOutputStream( new File( PATH ) );
        } catch ( FileNotFoundException e ) {
            log.error( "Could not create or open blacklist file.", e );
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = null;
        try {
            writer = new XMLWriter( System.out, format );
        } catch ( UnsupportedEncodingException e ) {
            log.error( "Could not create XML writer.", e );
        }
        try {
            writer.write( document );
        } catch ( IOException e ) {
            log.error( "Could not write to blacklist file.", e );
        }
        
    }

}
