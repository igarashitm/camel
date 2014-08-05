package org.apache.camel.example.mqtt;

import org.junit.Test;

public class CamelMQTTExampleTest {
    @Test
    public void testProduce() throws Exception {
        CamelMQTTExample instance = new CamelMQTTExample();
        instance.before();
        instance.testProduce();
        instance.after();
    }
}
