package com.hc9.web.main.service.smsmail.mwsms.impl;

import com.hc9.web.main.service.smsmail.mwsms.ISmsSoap;
import com.hc9.web.main.service.smsmail.mwsms.ISmsSoapService;

/**
 * 配置soap请求的实体类
 * @author Administrator
 *
 */
public class SmsLocator extends org.apache.axis.client.Service implements ISmsSoapService {

	private String ip;
	private String port;
	
    // 配置soap访问地址
	//TODO 配置
    private String SmsSoap_address = "http://"+ip+":"+port+"/MWGate/wmgw.asmx?wsdl";
    
    public SmsLocator() {
    
    }


    public SmsLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SmsLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }



    public java.lang.String getSmsSoapAddress() {
        return SmsSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SmsSoapWSDDServiceName = "SmsSoap";

    public java.lang.String getSmsSoapWSDDServiceName() {
        return SmsSoapWSDDServiceName;
    }

    public void setSmsSoapWSDDServiceName(java.lang.String name) {
        SmsSoapWSDDServiceName = name;
    }

    public ISmsSoap getSmsSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SmsSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSmsSoap(endpoint);
    }

    public ISmsSoap getSmsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            SmsSoapStub _stub = new SmsSoapStub(portAddress, this);
            _stub.setPortName(getSmsSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSmsSoapEndpointAddress(java.lang.String address) {
        SmsSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (ISmsSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                SmsSoapStub _stub = new SmsSoapStub(new java.net.URL(SmsSoap_address), this);
                _stub.setPortName(getSmsSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SmsSoap".equals(inputPortName)) {
            return getSmsSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "Sms");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "SmsSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
		if ("SmsSoap".equals(portName)) {
		            setSmsSoapEndpointAddress(address);
		}
		else 
		{ // Unknown Port Name
		            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }



}
