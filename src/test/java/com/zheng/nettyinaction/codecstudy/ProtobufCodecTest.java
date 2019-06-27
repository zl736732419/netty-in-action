package com.zheng.nettyinaction.codecstudy;

import com.zheng.nettyinaction.codecstudy.protobuf.PersonModule;
import com.zheng.nettyinaction.codecstudy.protobuf.PersonProtobufCodec;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class ProtobufCodecTest extends AbstractCodecTest {
    private PersonModule.Person person;

    @Override
    public void init() {
        person = PersonModule.Person.newBuilder()
                .setUserId(1L)
                .setName("zhangsan")
                .setMaster("大学本科")
                .build();
    }

    @Override
    protected Object getPerson() {
        return person;
    }

    @Override
    protected Class<?> getClazz() {
        return PersonModule.Person.class;
    }

    /**
     * encode bytes size: 26
     */
    @Override
    public void test() {
        super.test();
    }

    /**
     * codecstudy toke 35ms.
     */
    @Override
    public void performance() {
        super.performance();
    }

    @Override
    protected ICodec getCodec() {
        return new PersonProtobufCodec();
    }
}
