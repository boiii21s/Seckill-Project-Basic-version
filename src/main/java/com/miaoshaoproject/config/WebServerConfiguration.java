package com.miaoshaoproject.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

//如果Spring容器中没有TomcatEmbededServletContainerFactory这个bean的时候，就会把这个WebServerConfiguration加入到容器中
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        //使用对应工厂类提供的接口，定制化tomcat connector
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

                //设置最大的Keepalivetimeout的时间,30秒内客户端没有请求就会断开keepalive链接
                protocol.setKeepAliveTimeout(30);
                //当客户端发送超过10000个请求，keepalive就会断开
                protocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
