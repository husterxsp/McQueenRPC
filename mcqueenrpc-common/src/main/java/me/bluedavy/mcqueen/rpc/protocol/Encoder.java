/**
 * nfs-rpc
 * Apache License
 * <p>
 * http://code.google.com/p/nfs-rpc (c) 2011
 */
package me.bluedavy.mcqueen.rpc.protocol;

/**
 * Encoder Interface
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public interface Encoder {

    /**
     * Encode Object to byte[]
     */
    byte[] encode(Object object) throws Exception;

}
