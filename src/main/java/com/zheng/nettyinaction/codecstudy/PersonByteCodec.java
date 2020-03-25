package com.zheng.nettyinaction.codecstudy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * <pre>
 *
 *  File:
 *
 *  Copyright (c) 2016, globalegrow.com All Rights Reserved.
 *
 *  Description:
 *  TODO
 *
 *  Revision History
 *  Date,                   Who,                    What;
 *  2020年03月25日            xiaolian             Initial.
 *
 * </pre>
 */
public class PersonByteCodec implements ICodec<Person> {


    @Override
    public byte[] encode(Person person) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putLong(person.getUserId());
        buffer.put(person.getName().getBytes(StandardCharsets.UTF_8));
        buffer.put(",".getBytes(StandardCharsets.UTF_8));
        buffer.put(person.getMaster().getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    @Override
    public Person decode(byte[] bytes, Class<Person> clazz) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long userId = buffer.getLong();
        byte[] remaining = new byte[buffer.remaining()];
        buffer.get(remaining);
        String string = new String(remaining);
        String[] arr = string.split(",");
        Person person = new Person();
        person.setUserId(userId);
        person.setName(arr[0]);
        person.setMaster(arr[1]);
        return person;
    }

    public static void main(String[] args) {
        Person person = new Person(1L, "xiaolian", "master");
        PersonByteCodec codec = new PersonByteCodec();
        byte[] bytes = codec.encode(person);
        System.out.println(bytes.length);
        Person result = codec.decode(bytes, Person.class);
        System.out.println(result);
    }
}
