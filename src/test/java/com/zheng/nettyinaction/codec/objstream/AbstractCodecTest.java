package com.zheng.nettyinaction.codec.objstream;

import com.zheng.nettyinaction.codec.ICodec;
import com.zheng.nettyinaction.codec.Person;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public abstract class AbstractCodecTest {
    protected Person person;
    
    private int count = 1000;
    
    @Before
    public void init() {
        person = new Person(1L, "zhangsan", "大学本科");
    }
    
    @Test
    public void test() {
       codec(true);
    }
    
    private void codec(boolean log) {
        ICodec codec = getCodec();
        byte[] bytes = codec.encode(person);
        if (log) {
            System.out.println("encode bytes size: " + bytes.length);
        }
        Person person1 = codec.decode(bytes, Person.class);
        if (log) {
            System.out.println(person1);
        }
    }

    @Test
    public void performance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ICodec codec = getCodec();
            codec.encode(person);
        }
        long end = System.currentTimeMillis();
        System.out.println("codec toke " + (end - start) + "ms.");
    }
    
    protected abstract ICodec getCodec();
}
