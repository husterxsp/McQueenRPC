package me.bluedavy.mcqueen.rpc.protocol;

import com.esotericsoftware.kryo.io.Output;
import me.bluedavy.mcqueen.rpc.benchmark.KryoUtils;

/**
 * Kryo Encoder
 *
 * @author <a href="mailto:jlusdy@gmail.com">jlusdy</a>
 */
public class KryoEncoder implements Encoder {
    /**
     * @param object
     * @return
     */
    @Override
    public byte[] encode(Object object) throws Exception {
        Output output = new Output(256);
        KryoUtils.getKryo().writeClassAndObject(output, object);
        return output.toBytes();
    }

}
