package org.teiath.webservices.model;

import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "transactionTypes")
@JsonRootName(value = "transactionTypes")
public class ServiceTransactionTypeList {
	private List<ServiceTransactionType> serviceTransactionTypes;

	public ServiceTransactionTypeList() {
	}

	public ServiceTransactionTypeList(List<ServiceTransactionType> serviceTransactionTypes) {
		this.serviceTransactionTypes = serviceTransactionTypes;
	}

	@XmlElement(name = "transactionType")
	public List<ServiceTransactionType> getServiceTransactionTypes() {
		return serviceTransactionTypes;
	}

	public void setServiceTransactionTypes(List<ServiceTransactionType> serviceTransactionTypes) {
		this.serviceTransactionTypes = serviceTransactionTypes;
	}
}
