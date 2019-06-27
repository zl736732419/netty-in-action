package com.zheng.nettyinaction.codecstudy;

import org.junit.Before;
import org.junit.Test;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public abstract class AbstractCodecTest {
    private int count = 1000;
    private Person person;
    
    @Before
    public void init() {
        person = new Person(1L, "zhangsan", "大学本科");
    }
    
    @Test
    public void test() {
       codec(true, getClazz());
    }

    protected Class<?> getClazz() {
        return Person.class;
    }

    private <T> void codec(boolean log, Class<T> clazz) {
        ICodec codec = getCodec();
        byte[] bytes = codec.encode(getPerson());
        if (log) {
            System.out.println("encode bytes size: " + bytes.length);
        }
        T t = codec.decode(bytes, clazz);
        if (log) {
            System.out.println(t);
        }
    }

    protected Object getPerson() {
        return person;
    }

    @Test
    public void performance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ICodec codec = getCodec();
            codec.encode(getPerson());
        }
        long end = System.currentTimeMillis();
        System.out.println("codec toke " + (end - start) + "ms.");
    }
    
    protected abstract ICodec getCodec();
}
