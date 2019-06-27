package com.zheng.nettyinaction.codec.objstream;

import com.zheng.nettyinaction.codec.ICodec;
import com.zheng.nettyinaction.codec.msgpack.MessagePackCodec;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class MessagePackCodecTest extends AbstractCodecTest {
    /**
     * encode bytes size: 24
     */
    @Override
    public void test() {
        super.test();
    }

    /**
     * codec toke 14612ms.
     */
    @Override
    public void performance() {
        super.performance();
    }

    @Override
    protected ICodec getCodec() {
        return new MessagePackCodec();
    }
}
