package org.seedstack.elasticsearch.internal;

import javassist.util.proxy.MethodHandler;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.CheckedConsumer;
import org.seedstack.shed.reflect.ReflectUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DefaultHandler implements MethodHandler {

    private CheckedConsumer<RestClient, IOException> doClose = RestClient::close;

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        Field modifiersField = RestHighLevelClient.class.getDeclaredField("doClose");
        modifiersField.setAccessible(true);
        ReflectUtils.setValue(modifiersField, self, doClose);
        return proceed.invoke(self, args);
    }
}
