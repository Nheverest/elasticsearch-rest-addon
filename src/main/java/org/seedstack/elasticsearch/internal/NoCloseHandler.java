package org.seedstack.elasticsearch.internal;

import javassist.util.proxy.MethodHandler;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.CheckedConsumer;
import org.seedstack.seed.SeedException;
import org.seedstack.shed.reflect.ReflectUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class NoCloseHandler implements MethodHandler {

    private CheckedConsumer<RestClient, IOException> doClose = this::close;

    private void close(RestClient restClient) {
        throw SeedException.createNew(ElasticSearchErrorCode.FORBIDDEN_CLIENT_CLOSE);
    }

    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
        Field modifiersField = RestHighLevelClient.class.getDeclaredField("doClose");
        modifiersField.setAccessible(true);
        ReflectUtils.setValue(modifiersField, self, doClose);
        return proceed.invoke(self, args);
    }

}
