package br.unb.sds.gds2ephem.configs;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class XmlRpcClientBeans {
    @Value("${odoo.url}")
    private String url;

    @Bean(name = "xmlRpcClientObject")
    public XmlRpcClient gerarClientObject() throws MalformedURLException {
        final var xmlRpcClientConfig = new XmlRpcClientConfigImpl();
        xmlRpcClientConfig.setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
        xmlRpcClientConfig.setEnabledForExtensions(true);
        final var models = new XmlRpcClient();
        models.setConfig(xmlRpcClientConfig);
        return models;
    }

    @Bean(name = "xmlRpcClientCommon")
    public XmlRpcClient gerarClientCommon() throws MalformedURLException {
        final var xmlRpcClientConfig = new XmlRpcClientConfigImpl();
        xmlRpcClientConfig.setEnabledForExtensions(true);
        xmlRpcClientConfig.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        final var client = new XmlRpcClient();
        client.setConfig(xmlRpcClientConfig);
        return client;
    }
}
