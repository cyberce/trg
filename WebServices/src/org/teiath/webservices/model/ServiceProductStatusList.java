package org.teiath.webservices.model;

import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "productStatuses")
@JsonRootName(value = "productStatuses")
public class ServiceProductStatusList {
	List<ServiceProductStatus> serviceProductStatuses;

	public ServiceProductStatusList() {
	}

	public ServiceProductStatusList(List<ServiceProductStatus> serviceProductStatuses) {
		this.serviceProductStatuses = serviceProductStatuses;
	}

	@XmlElement(name = "productStatus")
	public List<ServiceProductStatus> getServiceProductStatuses() {
		return serviceProductStatuses;
	}

	public void setServiceProductStatuses(List<ServiceProductStatus> serviceProductStatuses) {
		this.serviceProductStatuses = serviceProductStatuses;
	}
}
