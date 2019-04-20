/**
 * nfs-rpc
 * Apache License
 * <p>
 * http://code.google.com/p/nfs-rpc (c) 2011
 */
package me.bluedavy.mcqueen.rpc.protocol;

/**
 * Protocol Interface,for custom network protocol
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public interface Protocol {

    /**
     * encode Message to byte & write to network framework
     *
     * @param message
     * @param bytebufferWrapper
     * @return
     */
    public ByteBufferWrapper encode(Object message, ByteBufferWrapper bytebufferWrapper) throws Exception;

    /**
     * decode stream to object
     *
     * @param wrapper
     * @param errorObject stream not enough,then return this object
     * @param originPos
     * @return Object
     */
    public Object decode(ByteBufferWrapper wrapper, Object errorObject, int... originPos) throws Exception;

}