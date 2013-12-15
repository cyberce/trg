package org.teiath.webservices.model;

import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "currencies")
@JsonRootName(value = "currencies")
public class ServiceCurrencyList {
	private List<ServiceCurrency> serviceCurrencies;

	public ServiceCurrencyList() {
	}

	public ServiceCurrencyList(List<ServiceCurrency> serviceCurrencies) {
		this.serviceCurrencies = serviceCurrencies;
	}

	@XmlElement(name = "currency")
	public List<ServiceCurrency> getServiceCurrencies() {
		return serviceCurrencies;
	}

	public void setServiceCurrencies(List<ServiceCurrency> serviceCurrencies) {
		this.serviceCurrencies = serviceCurrencies;
	}
}
