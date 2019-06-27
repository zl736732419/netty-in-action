package com.zheng.nettyinaction.codecstudy;

import com.zheng.nettyinaction.codecstudy.objstream.ObjectStreamCodec;

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
     * codecstudy toke 62ms.
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
