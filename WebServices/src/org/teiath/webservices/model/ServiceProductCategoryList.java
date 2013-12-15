package org.teiath.webservices.model;

import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "productCategories")
@JsonRootName(value = "productCategories")
public class ServiceProductCategoryList {
	private List<ServiceProductCategory> serviceProductCategories;

	public ServiceProductCategoryList() {
	}

	public ServiceProductCategoryList(List<ServiceProductCategory> serviceProductCategories) {
		this.serviceProductCategories = serviceProductCategories;
	}

	@XmlElement(name = "productCategory")
	public List<ServiceProductCategory> getServiceProductCategories() {
		return serviceProductCategories;
	}

	public void setServiceProductCategories(List<ServiceProductCategory> serviceProductCategories) {
		this.serviceProductCategories = serviceProductCategories;
	}
}
