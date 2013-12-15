package org.teiath.webservices.model;

import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "currency")
@JsonRootName(value = "currency")
public class ServiceCurrency {
	private Integer id;
	private String title;

	public ServiceCurrency() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
