package thiagotgm.blakebot;

/**
 * Interface for a class that gets notified when the bot changes connection
 * status (connects or disconnects).
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-30
 */
public interface ConnectionStatusListener {
    
    /**
     * Event triggered when the bot changes connection status.
     * 
     * @param isConnected if true, the bot just connected.
     *                    if false, the bot just disconnected.
     */
    void connectionChange( boolean isConnected );

}
