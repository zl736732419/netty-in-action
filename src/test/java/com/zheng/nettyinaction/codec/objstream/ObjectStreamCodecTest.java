package com.zheng.nettyinaction.codec.objstream;

import com.zheng.nettyinaction.codec.ICodec;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class ObjectStreamCodecTest extends AbstractCodecTest{

    /**
     * encode bytes size: 231
     */
    @Override
    public void test() {
        super.test();
    }

    /**
     * codec toke 62ms.
     */
    @Override
    public void performance() {
        super.performance();
    }

    @Override
    protected ICodec getCodec() {
        return new ObjectStreamCodec();
    }
}
