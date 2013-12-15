package org.teiath.webservices.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.teiath.data.domain.User;
import org.teiath.data.domain.trg.*;
import org.teiath.data.paging.SearchResults;
import org.teiath.data.search.ListingSearchCriteria;
import org.teiath.service.exceptions.AuthenticationException;
import org.teiath.service.exceptions.ServiceException;
import org.teiath.service.trg.*;
import org.teiath.service.user.UserLoginService;
import org.teiath.service.values.*;
import org.teiath.webservices.authentication.Utils;
import org.teiath.webservices.model.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Controller
public class ProductsController {

	@Autowired
	private SearchListingsService searchListingsService;
	@Autowired
	private CreateListingService createListingService;
	@Autowired
	private EditListingService editListingService;
	@Autowired
	private ListListingsService listListingsService;
	@Autowired
	private ViewListingSellerService viewListingSellerService;
	@Autowired
	private EditTransactionTypeService editTransactionTypeService;
	@Autowired
	private EditProductCategoryService editProductCategoryService;
	@Autowired
	private EditProductStatusService editProductStatusService;
	@Autowired
	private CreateCurrencyService createCurrencyService;
	@Autowired
	private EditCurrencyService editCurrencyService;
	@Autowired
	private UserLoginService userLoginService;

	@RequestMapping(value = "/services/products", method = RequestMethod.GET)
	public ServiceProductList searchProducts(String transactionType, String category, String status, String pDateFrom,
	                                         String pDateTo, Double price, String keyword, String orderBy, String orderDir, Integer pn, Integer ps) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		ServiceProductList serviceProductList = new ServiceProductList();
		serviceProductList.setServiceProducts(new ArrayList<ServiceProduct>());
		ServiceProduct serviceProduct;

		ListingSearchCriteria criteria = new ListingSearchCriteria();
		try {
			criteria.setTransactionTypeName(transactionType);
			criteria.setProductCategoryName(category);
			criteria.setProductStatusName(status);
			criteria.setDateFrom(pDateFrom != null ? sdf.parse(pDateFrom) : null);
			criteria.setDateTo(pDateTo != null ? sdf.parse(pDateTo) : null);
			criteria.setMaxAmount(price != null ? new BigDecimal(price) : null);
			criteria.setListingKeyword(keyword);

			// Paging and sorting criteria
			criteria.setPageNumber(pn == null? 0: pn);
			criteria.setPageSize(ps == null? Integer.MAX_VALUE: ps);
			criteria.setOrderField(orderBy);
			if (orderDir != null) {
				if (orderDir.equals("asc")) {
					criteria.setOrderDirection("ascending");
				} else if (orderDir.equals("desc")) {
					criteria.setOrderDirection("descending");
				}
			}

			SearchResults<Listing> results = searchListingsService.searchListings(criteria);

			for (Listing listing : results.getData()) {
				serviceProduct = new ServiceProduct();
				serviceProduct.setTransactionTypeName(listing.getTransactionType().getName());
				serviceProduct.setProductName(listing.getProductName());
				serviceProduct.setDescription(listing.getProductDescription());
				serviceProduct.setProductCategoryName(listing.getProductCategory().getName());
				serviceProduct.setProductStatusName(listing.getProductStatus().getName());
				serviceProduct.setPurchaseDate(listing.getPurchaseDate());
				serviceProduct.setPrice(listing.getPrice());
				serviceProduct.setListingCreationDate(listing.getListingCreationDate());
				serviceProduct.setCode(listing.getCode());

				serviceProductList.getServiceProducts().add(serviceProduct);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return serviceProductList;
	}

	@RequestMapping(value = "/services/product", method = RequestMethod.GET)
	public ServiceProduct searchProductByCode(String code) {

		ServiceProduct serviceProduct = new ServiceProduct();
		try {

			Listing listing = viewListingSellerService.getListingByCode(code);
			serviceProduct.setTransactionTypeName(listing.getTransactionType().getName());
			serviceProduct.setProductName(listing.getProductName());
			serviceProduct.setDescription(listing.getProductDescription());
			serviceProduct.setProductCategoryName(listing.getProductCategory().getName());
			serviceProduct.setProductStatusName(listing.getProductStatus().getName());
			serviceProduct.setPurchaseDate(listing.getPurchaseDate());
			serviceProduct.setPrice(listing.getPrice());
			serviceProduct.setListingCreationDate(listing.getListingCreationDate());
			serviceProduct.setCode(code);

		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return serviceProduct;
	}

	@RequestMapping(value = "/services/product", method = RequestMethod.POST, headers="Accept=application/xml, application/json")
	public @ResponseBody ResponseMessages saveProduct(@RequestBody ServiceProduct product, HttpServletRequest request) {
		ResponseMessages responseMessages = new ResponseMessages();

		String authorization = request.getHeader("Authorization");
		if (authorization == null) {
			responseMessages.addMessage("This method needs authorization");
			return responseMessages;
		} else {
			try {
				String[] credentials = Utils.decodeHeader(authorization);
				User user = userLoginService.wsLogin(credentials[0], credentials[1]);

				if (user == null) {
					throw new AuthenticationException("Invalid Username/Password");
				}

				TransactionType transactionType = null;
				ProductCategory productCategory = null;
				ProductStatus productStatus = null;
				Currency currency = null;

				if (product.getTransactionType() == null) {
					responseMessages.addMessage("transactionType is mandatory");
				} else {
					try {
						transactionType = editTransactionTypeService.getTransactionTypeById(product.getTransactionType());
					} catch (ServiceException e) {
						responseMessages.addMessage("transactionType is invalid");
					}
				}
				if (product.getProductCategory() == null) {
					responseMessages.addMessage("productCategory is mandatory");
				} else {
					try {
						productCategory = editProductCategoryService.getProductCategoryById(product.getProductCategory());
					} catch (ServiceException e) {
						responseMessages.addMessage("productCategory is invalid");
					}
				}
				if (product.getProductName() == null) {
					responseMessages.addMessage("ProductName is mandatory");
				}
				if (product.getDescription() == null) {
					responseMessages.addMessage("description is mandatory");
				}
				if (product.getProductStatus() == null) {
					responseMessages.addMessage("productStatus is mandatory");
				} else {
					try {
						productStatus = editProductStatusService.getProductStatusById(product.getProductStatus());
					} catch (ServiceException e) {
						responseMessages.addMessage("productStatus is invalid");
					}
				}
				if (product.getPurchaseDate() == null) {
					responseMessages.addMessage("purchaseDate is mandatory");
				}
				if (product.getCurrency() != null) {
					try {
						currency = editCurrencyService.getCurrencyById(product.getCurrency());
					} catch (ServiceException e) {
						responseMessages.addMessage("currency is invalid");
					}
				}

				if (responseMessages.getMessage() != null) {
					return responseMessages;
				} else {
					Listing listing = new Listing();
					listing.setListingCreationDate(new Date());
					listing.setProductName(product.getProductName());
					listing.setProductDescription(product.getDescription());
					listing.setPurchaseDate(product.getPurchaseDate());
					listing.setActive(true);
					if (product.getProductBrand() != null) {
						listing.setProductBrand(product.getProductBrand());
					}
					if (product.getPrice() != null) {
						listing.setPrice(product.getPrice());
					} else {
						listing.setPrice(new BigDecimal(0.0));
					}
					if (currency != null) {
						listing.setCurrency(currency);
					} else {
						listing.setCurrency(createCurrencyService.getDefaultCurrency());
					}
					if (product.getSendHome() != null) {
						listing.setSendHome(product.getSendHome());
					} else {
						listing.setSendHome(false);
					}
					if (product.getEnabled() != null) {
						listing.setEnabled(product.getEnabled());
					} else {
						listing.setEnabled(true);
					}
					if (product.getComments() != null) {
						listing.setComments(product.getComments());
					}
					if (transactionType != null) {
						listing.setTransactionType(transactionType);
					}
					if (productCategory != null) {
						listing.setProductCategory(productCategory);
					}
					if (productStatus != null) {
						listing.setProductStatus(productStatus);
					}

					Transaction transaction = new Transaction();
					transaction.setListing(listing);
					transaction.setCompleted(false);
					transaction.setSeller(user);
					transaction.setTransactionDate(new Date());
					listing.setTransaction(transaction);

					listing.setUser(user);

					createListingService.saveListing(listing);

					responseMessages.addMessage("Product is successfully created");
				}

			} catch (ServiceException e) {
				responseMessages.addMessage("Product persist has failed");
			} catch (AuthenticationException e) {
				responseMessages.addMessage(e.getMessage());
			}
		}

		return responseMessages;
	}

	@RequestMapping(value = "/services/product", method = RequestMethod.PUT, headers="Accept=application/xml, application/json")
	public @ResponseBody ResponseMessages updateProduct(String code, @RequestBody ServiceProduct product, HttpServletRequest request) {
		ResponseMessages responseMessages = new ResponseMessages();

		String authorization = request.getHeader("Authorization");
		if (authorization == null) {
			responseMessages.addMessage("This method needs authorization");
			return responseMessages;
		} else {
			try {
				String[] credentials = Utils.decodeHeader(authorization);
				User user = userLoginService.wsLogin(credentials[0], credentials[1]);

				if (user == null) {
					throw new AuthenticationException("Invalid Username/Password");
				}

				TransactionType transactionType = null;
				ProductCategory productCategory = null;
				ProductStatus productStatus = null;
				Currency currency = null;

				if (code == null) {
					responseMessages.addMessage("Parameter 'code' is missing");
					return responseMessages;
				}

				if (product.getTransactionType() != null) {
					try {
						transactionType = editTransactionTypeService.getTransactionTypeById(product.getTransactionType());
					} catch (ServiceException e) {
						responseMessages.addMessage("transactionType is invalid");
					}
				}
				if (product.getProductCategory() != null) {
					try {
						productCategory = editProductCategoryService.getProductCategoryById(product.getProductCategory());
					} catch (ServiceException e) {
						responseMessages.addMessage("productCategory is invalid");
					}
				}
				if (product.getProductStatus() != null) {
					try {
						productStatus = editProductStatusService.getProductStatusById(product.getProductStatus());
					} catch (ServiceException e) {
						responseMessages.addMessage("productStatus is invalid");
					}
				}
				if (product.getCurrency() != null) {
					try {
						currency = editCurrencyService.getCurrencyById(product.getCurrency());
					} catch (ServiceException e) {
						responseMessages.addMessage("currency is invalid");
					}
				}

				if (responseMessages.getMessage() != null) {
					return responseMessages;
				} else {
					Listing listing = editListingService.getListingByCode(code);

					if (listing.getUser().getId().intValue() != user.getId().intValue()) {
						responseMessages.addMessage("Permission Denied. You don't own the selected Product");
						return responseMessages;
					}

					if (product.getProductName() != null) {
						listing.setProductName(product.getProductName());
					}
					if (product.getDescription() != null) {
						listing.setProductDescription(product.getDescription());
					}
					if (product.getPurchaseDate() != null) {
						listing.setPurchaseDate(product.getPurchaseDate());
					}
					if (product.getProductBrand() != null) {
						listing.setProductBrand(product.getProductBrand());
					}
					if (product.getPrice() != null) {
						listing.setPrice(product.getPrice());
					}
					if (currency != null) {
						listing.setCurrency(currency);
					}
					if (product.getSendHome() != null) {
						listing.setSendHome(product.getSendHome());
					}
					if (product.getEnabled() != null) {
						listing.setEnabled(product.getEnabled());
					}
					if (product.getComments() != null) {
						listing.setComments(product.getComments());
					}
					if (transactionType != null) {
						listing.setTransactionType(transactionType);
					}
					if (productCategory != null) {
						listing.setProductCategory(productCategory);
					}
					if (productStatus != null) {
						listing.setProductStatus(productStatus);
					}

					editListingService.saveListing(listing);

					responseMessages.addMessage("Product is successfully updated");
				}

			} catch (AuthenticationException e) {
				responseMessages.addMessage(e.getMessage());
			} catch (ServiceException e) {
				responseMessages.addMessage("Product update has failed");
				responseMessages.addMessage(e.getMessage());
				e.printStackTrace();

			}
		}

		return responseMessages;
	}

	@RequestMapping(value = "/services/product", method = RequestMethod.DELETE)
	public @ResponseBody ResponseMessages deleteProduct(String code, HttpServletRequest request) {
		ResponseMessages responseMessages = new ResponseMessages();

		String authorization = request.getHeader("Authorization");
		if (authorization == null) {
			responseMessages.addMessage("This method needs authorization");
			return responseMessages;
		} else {
			try {
				String[] credentials = Utils.decodeHeader(authorization);
				User user = userLoginService.wsLogin(credentials[0], credentials[1]);

				if (user == null) {
					throw new AuthenticationException("Invalid Username/Password");
				}

				if (code == null) {
					responseMessages.addMessage("Parameter 'code' is missing");
					return responseMessages;
				}

				Listing listing = editListingService.getListingByCode(code);

				if (listing.getUser().getId().intValue() != user.getId().intValue()) {
					responseMessages.addMessage("Permission Denied. You don't own the selected Product");
					return responseMessages;
				}

				listListingsService.deleteListing(listing);

				responseMessages.addMessage("Product is successfully deleted");
			} catch (AuthenticationException e) {
				responseMessages.addMessage(e.getMessage());
			} catch (ServiceException e) {
				responseMessages.addMessage("Product delete has failed");
				e.printStackTrace();
			}
		}

		return responseMessages;
	}

	@RequestMapping(value = "/services/currencies", method = RequestMethod.GET)
	public ServiceCurrencyList getCurrencies() {
		ServiceCurrencyList serviceCurrencyList = new ServiceCurrencyList();
		serviceCurrencyList.setServiceCurrencies(new ArrayList<ServiceCurrency>());
		ServiceCurrency serviceCurrency;

		try {

			Collection<Currency> results = createListingService.getCurrencies();

			for (Currency currency : results) {
				serviceCurrency = new ServiceCurrency();
				serviceCurrency.setId(currency.getId());
				serviceCurrency.setTitle(currency.getName());

				serviceCurrencyList.getServiceCurrencies().add(serviceCurrency);
			}

		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return serviceCurrencyList;
	}

	@RequestMapping(value = "/services/transaction_types", method = RequestMethod.GET)
	public ServiceTransactionTypeList getTransactionTypes() {
		ServiceTransactionTypeList serviceTransactionTypeList = new ServiceTransactionTypeList();
		serviceTransactionTypeList.setServiceTransactionTypes(new ArrayList<ServiceTransactionType>());
		ServiceTransactionType serviceTransactionType;

		try {

			Collection<TransactionType> results = createListingService.getTransactionTypes();

			for (TransactionType transactionType : results) {
				serviceTransactionType = new ServiceTransactionType();
				serviceTransactionType.setId(transactionType.getId());
				serviceTransactionType.setTitle(transactionType.getName());

				serviceTransactionTypeList.getServiceTransactionTypes().add(serviceTransactionType);
			}

		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return serviceTransactionTypeList;
	}

	@RequestMapping(value = "/services/product_categories", method = RequestMethod.GET)
	public ServiceProductCategoryList getProductCategories() {
		ServiceProductCategoryList serviceProductCategoryList = new ServiceProductCategoryList();
		serviceProductCategoryList.setServiceProductCategories(new ArrayList<ServiceProductCategory>());
		ServiceProductCategory serviceProductCategory;

		try {

			Collection<ProductCategory> results = createListingService.getProductCategories();

			for (ProductCategory productCategory : results) {
				serviceProductCategory = new ServiceProductCategory();
				serviceProductCategory.setId(productCategory.getId());
				serviceProductCategory.setName(productCategory.getName());
				serviceProductCategory.setParentId(productCategory.getParentCategoryId());

				serviceProductCategoryList.getServiceProductCategories().add(serviceProductCategory);
			}

		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return serviceProductCategoryList;
	}

	@RequestMapping(value = "/services/product_statuses", method = RequestMethod.GET)
	public ServiceProductStatusList getProductStatuses() {
		ServiceProductStatusList serviceProductStatusList = new ServiceProductStatusList();
		serviceProductStatusList.setServiceProductStatuses(new ArrayList<ServiceProductStatus>());
		ServiceProductStatus serviceProductStatus;

		try {

			Collection<ProductStatus> results = createListingService.getProductStatuses();

			for (ProductStatus productStatus : results) {
				serviceProductStatus = new ServiceProductStatus();
				serviceProductStatus.setId(productStatus.getId());
				serviceProductStatus.setName(productStatus.getName());

				serviceProductStatusList.getServiceProductStatuses().add(serviceProductStatus);
			}

		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return serviceProductStatusList;
	}


}
