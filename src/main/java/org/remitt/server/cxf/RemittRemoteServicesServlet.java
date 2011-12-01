package org.remitt.server.cxf;

import javax.servlet.ServletConfig;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.remitt.server.impl.ServiceImpl;

public class RemittRemoteServicesServlet extends CXFNonSpringServlet {

	private static final long serialVersionUID = 5150445863173665764L;

	@Override
	public void loadBus(ServletConfig servletConfig) {
		super.loadBus(servletConfig);

		Bus bus = getBus();
		BusFactory.setDefaultBus(bus);
		ServerFactoryBean factory = new ServerFactoryBean();
		factory.setBus(bus);
		factory.getInInterceptors().add(new BasicAuthAuthorizationInterceptor());
		factory.setServiceClass(ServiceImpl.class);
		factory.setAddress("/interface");
		factory.create();
	}

}
