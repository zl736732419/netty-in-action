package com.zheng.nettyinaction.codecstudy.objstream;

import com.zheng.nettyinaction.codecstudy.ICodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 基于jdk obj stream编解码
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class ObjectStreamCodec implements ICodec {

    @Override
    public <T> byte[] encode(T t) {
        if (!(t instanceof Serializable)) {
            return null;
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objOutput = null;
        try {
            objOutput = new ObjectOutputStream(output);
            objOutput.writeObject(t);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                objOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output.toByteArray();
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ObjectInputStream objInput = null;
        T t = null;
        try {
            objInput = new ObjectInputStream(input);
            t = (T) objInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                objInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return t;
    }
}
