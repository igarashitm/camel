package org.apache.camel.example.mqtt;

import org.apache.activemq.command.Command;
import org.apache.camel.Exchange;
import org.apache.camel.component.mqtt.MQTTConsumer;
import org.apache.camel.component.mqtt.MQTTEndpoint;
import org.apache.log4j.Logger;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.codec.MQTTFrame;
import org.fusesource.mqtt.codec.PINGRESP;
import org.fusesource.mqtt.codec.PUBACK;
import org.fusesource.mqtt.codec.PUBCOMP;
import org.fusesource.mqtt.codec.PUBLISH;
import org.fusesource.mqtt.codec.PUBREC;
import org.fusesource.mqtt.codec.PUBREL;
import org.fusesource.mqtt.codec.SUBACK;
import org.fusesource.mqtt.codec.UNSUBACK;

public class BytemanHelper {
    private Logger _logger = Logger.getLogger(BytemanHelper.class);

    public void traceClientSend(MQTTFrame frame, UTF8Buffer clientId) {
        if (PUBLISH.TYPE == frame.messageType()) {
            //printStackTrace();
        }
        StringBuilder str = new StringBuilder("@>>>>> client-send: clientId=").append(clientId)
                                .append(", ").append(decode(frame));
        _logger.trace(str.toString());
    }

    public void traceClientReceive(MQTTFrame frame, UTF8Buffer clientId) {
        StringBuilder str = new StringBuilder("@<<<<< client-recv: clientId=").append(clientId)
                                .append(", ").append(decode(frame));
        _logger.trace(str.toString());
    }

    public void traceServerReceive(MQTTFrame frame, String clientId) {
        if (PUBLISH.TYPE == frame.messageType()) {
            //printStackTrace();
        }
        StringBuilder str = new StringBuilder("#<<<<< server-recv: clientId=").append(clientId)
                                .append(", ").append(decode(frame));
        _logger.trace(str.toString());
    }

    public void traceServerSend(Command command, String clientId) {
        StringBuilder str = new StringBuilder("#>>>>> server-send: clientId=").append(clientId)
                                .append(", ").append(decode(command));
        _logger.trace(str.toString());
    }

    public void traceCamelProducer(Exchange exchange, MQTTEndpoint endpoint) {
        try {
            String topic = endpoint.getConfiguration().getPublishTopicName();
            StringBuilder str = new StringBuilder("$>>>>> camel-produ: topic=").append(topic)
                                        .append(", body=").append(exchange.getIn());
            _logger.trace(str.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void traceCamelConsumer(Exchange exchange) {
        try {
            String topic = (String)exchange.getProperty("MQTTTopicPropertyName");
            StringBuilder str = new StringBuilder("$<<<<< camel-consu: topic=").append(topic)
                                        .append(", body=").append(exchange.getIn());
            _logger.trace(str.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private String decode(MQTTFrame frame) {
        try {
            switch(frame.messageType()) {
            case PUBLISH.TYPE: {
                return new PUBLISH().decode(frame).toString();
            }
            case PUBREL.TYPE:{
                return new PUBREL().decode(frame).toString();
            }
            case PUBACK.TYPE:{
                return new PUBACK().decode(frame).toString();
            }
            case PUBREC.TYPE:{
                return new PUBREC().decode(frame).toString();
            }
            case PUBCOMP.TYPE:{
                return new PUBCOMP().decode(frame).toString();
            }
            case SUBACK.TYPE: {
                return new SUBACK().decode(frame).toString();
            }
            case UNSUBACK.TYPE: {
                return new UNSUBACK().decode(frame).toString();
            }
            case PINGRESP.TYPE: {
                return frame.toString();
            }
            }
        } catch (Exception e) {
            _logger.error("", e);
        }
        return frame.toString();
    }

    private String decode(Command command) {
        return command.toString();
    }

    private void printStackTrace() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println("\t" + ste.toString());
        }
    }
}
