package it.duepassicalzature.interfaccia.controller;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.duepassicalzature.interfaccia.DTO.MappaturaProdIdSku;
import it.duepassicalzature.interfaccia.model.Attributes;
import it.duepassicalzature.interfaccia.model.IProductWebNew;
import it.duepassicalzature.interfaccia.model.IUpdateProducts;
import it.duepassicalzature.interfaccia.model.IVariationWeb;
import it.duepassicalzature.interfaccia.repository.DataAggiornamentoRepository;
import it.duepassicalzature.interfaccia.repository.MappaturaAttributiRepository;
import it.duepassicalzature.interfaccia.repository.MappaturaImagesRepository;
import it.duepassicalzature.interfaccia.repository.MappaturaRepository;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;
import it.duepassicalzature.interfaccia.service.DescriptionService;
import it.duepassicalzature.interfaccia.service.ImagesService;
import it.duepassicalzature.interfaccia.service.ProductsWebService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/retail/")
public class ProductController {

	Logger log = LoggerFactory.getLogger(ProductsWebService.class);
	@Autowired
	MappaturaRepository mappaturaRepository;

	@Autowired
	MappaturaAttributiRepository attributiRepository;
	@Autowired
	DataAggiornamentoRepository dataAggiornamentoRepository;
	@Autowired
	MappaturaImagesRepository imagesRepository;

	@Autowired
	ImagesService imagesService;
	@Autowired
	DescriptionService descriptionService;
	// @GetMapping("getProductsWebParent")
	// public ResponseEntity<List<IProductWebNew>> getProductsWebParent() {
	// List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent("",null,"","");
	// return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
	// }
	//

	@GetMapping("getProductsWebParent")
	public ResponseEntity<List<IProductWebNew>> getProductsWebParent(@RequestParam String codart, @RequestParam Integer id_woo, @RequestParam String stagione, @RequestParam String brand) {
		List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent(codart, id_woo, stagione, brand);
		return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
	}

	@PostMapping("deleteproducts")
	@ResponseBody
	@Transactional
	public ResponseEntity<List<IProductWebNew>> deleteproducts(@RequestBody String params) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String[] listIdWooComm = params.split(",");

		for (String idWooComm : listIdWooComm) {
			try {
				mappaturaRepository.deleteProdFiglio(new Integer(idWooComm));
				log.info("Cancellato prodotto figlio dalla tabella Mappatura con idWooCommerce " + idWooComm);
				mappaturaRepository.deleteProdPadre(new Integer(idWooComm));
				log.info("Cancellato prodotto Padre dalla tabella Mappatura con idWooCommerce " + idWooComm);
			} catch (Exception e) {
				log.error("Errore cancellazione da DB");
			}
			try {
				String urlDelProduct = "https://duepassicalzature.it/wp-json/wc/v3/products/" + idWooComm + "?force=true";
				HttpEntity<?> request = new HttpEntity<Object>(headers);
				restTemplate.exchange(urlDelProduct, HttpMethod.DELETE, request, String.class);
				log.info("Cancellato prodotto con ID WooCommerce " + idWooComm);
				//restTemplate.delete(urlDelProduct, headers);
			} catch (Exception e) {
				log.error("Errore cancellazione da Woocommerce");
			}
		}

		List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent("", null, "", "");
		return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
	}

	@PostMapping("modifyproducts")
	@ResponseBody
	@Transactional
	public ResponseEntity<List<IProductWebNew>> modifyproducts(@RequestBody String params) {
		
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String[] listIdWooComm = params.split(",");
		log.info("Sono stati trovati " + listIdWooComm.length + " prodotti padre a cui aggiornare le anagrafiche!");
		for (String idWooComm : listIdWooComm) {
			
			List<IUpdateProducts> prodottiDaAggiornare = mappaturaRepository.getListaProdDaAggiornareByIdWoo(new Integer(idWooComm));
			IUpdateProducts p = prodottiDaAggiornare.get(0);
			aggiornaAnagProdotto(p, headers, idWooComm);
			//
			aggiornaPrezziProd(idWooComm);
			
		}
		List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent("", null, "", "");
		return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
	}

	@GetMapping("getStagioni")
	public ResponseEntity<List<String>> getStagioni() {
		List<String> listaStagioni = mappaturaRepository.getStagioni();
		return new ResponseEntity<List<String>>(listaStagioni, HttpStatus.OK);
	}

	@GetMapping("getBrands")
	public ResponseEntity<List<String>> getBrands() {
		List<String> listaBrand = mappaturaRepository.getBrands();
		return new ResponseEntity<List<String>>(listaBrand, HttpStatus.OK);
	}

	public static Integer getTagIdScontoApplicatoDaPercentuale(String percentuale) {

		int tagId = 1113;
		switch (percentuale) {
		case "20%":
			tagId = 1115;
			break;
		case "50%":
			tagId = 1114;
			break;
		case "70%":
			tagId = 1112;
			break;
		}
		return tagId;
	}
	
	private void aggiornaAnagProdotto(IUpdateProducts p, HttpHeaders headers, String idWooComm) {
		RestTemplate restTemplate = new RestTemplate();
		log.info("Aggiornamento anagrafica prodotto con ID WooCommerce " + p.getId_woo_commerce() + " e codart " + p.getCodart());
		//
		List<IVariationWeb> attributi = attributiRepository.getTaglieColByCodArt(p.getCodart());
		List<Attributes> attributesProdottos = new ArrayList<>();
		List<String> opzioniTaglie = new ArrayList<>();
		List<String> opzioniColore = new ArrayList<>();
		List<String> opzioniBrand = new ArrayList<>();
		List<String> opzioniStagione = new ArrayList<>();
		JSONObject productJsonObject = new JSONObject();
		productJsonObject.put("name", p.getTitolo());
		productJsonObject.put("sale_price", p.getListinoweb1().toString());
		productJsonObject.put("regular_price", p.getPricesell().toString());
		productJsonObject.put("description", descriptionService.getLongDescription(p.getPercorsodesc()));
		productJsonObject.put("short_description", descriptionService.getDescription(p.getDescbreve()));
		productJsonObject.put("slug", p.getTitolo());
		productJsonObject.put("manage_stock", false);
		// Le giacenze dei prodotti padre devono avere quantità 0. Aggiornamento del 9 Marzo 2021
		// productJsonObject.put("stock_quantity", prodotto.getGiacenza());
		JSONArray categoriesJsonObject = new JSONArray(p.getCat_json());
		productJsonObject.put("categories", categoriesJsonObject);
		for (IVariationWeb attributo : attributi) {
			opzioniTaglie.add(attributo.getTaglia());
			opzioniColore.add(attributo.getDesccol());
			opzioniBrand.add(attributo.getBrand());
		}
		opzioniStagione.add(p.getStagione().toUpperCase());

		Attributes taglie = new Attributes(7, "Taglia", 3, true, true, opzioniTaglie);
		Attributes colori = new Attributes(9, "Colore", 2, true, true, opzioniColore);
		Attributes brands = new Attributes(8, "Brand", 1, true, false, opzioniBrand);
		Attributes stagioni = new Attributes(10, "Stagione", 4, true, false, opzioniStagione);
		attributesProdottos.add(taglie);
		attributesProdottos.add(colori);
		attributesProdottos.add(brands);
		attributesProdottos.add(stagioni);

		productJsonObject.put("attributes", new JSONArray(attributesProdottos));

		JSONArray metadata = new JSONArray();
		JSONObject eanprod = new JSONObject();

		eanprod.put("key", "_woosea_gtin");
		eanprod.put("value", p.getEanprod());
		metadata.put(eanprod);

		JSONObject percentuale = new JSONObject();
		percentuale.put("id", getTagIdScontoApplicatoDaPercentuale(p.getPercentuale()));

		JSONArray arrayTag = new JSONArray();
		arrayTag.put(percentuale);

		productJsonObject.put("tags", arrayTag);
		productJsonObject.put("meta_data", metadata);
		//PREZZI
		productJsonObject.put("sale_price", p.getListinoweb1().toString());
		productJsonObject.put("regular_price", p.getPricesell().toString());
		
		List<IUpdateProducts> variazioniDaAggiornare = mappaturaRepository.aggiornamentoVariazioniByParent(p.getId_woo_commerce());
		JSONArray arraytagcol = new JSONArray();
		for (IUpdateProducts variazione : variazioniDaAggiornare) {

			String urlVariazione = "https://duepassicalzature.it/wp-json/wc/v3/products/" + p.getId_woo_commerce() + "/variations/" + variazione.getId_woo_commerce();

			JSONObject varJsonObject = new JSONObject();
			varJsonObject.put("type", "variation");

			JSONObject vartagliejson = new JSONObject();
			vartagliejson.put("id", 7);
			vartagliejson.put("name", "Taglia");
			vartagliejson.put("option", variazione.getTaglia());

			JSONObject varcolorejson = new JSONObject();
			varcolorejson.put("id", 9);
			varcolorejson.put("name", "Colore");
			varcolorejson.put("option", variazione.getDesccol());

			arraytagcol.put(varcolorejson);
			arraytagcol.put(vartagliejson);

			varJsonObject.put("attributes", arraytagcol);

			HttpEntity<String> requestVAR = new HttpEntity<String>(varJsonObject.toString(), headers);
			try {
				
				restTemplate.put(urlVariazione, requestVAR);
				log.info("Aggiornamento Variazione riuscito: " + varJsonObject.toString());
			} catch (RestClientException e) {
				log.error("Errore Aggiornamento Variazione: " + varJsonObject.toString());
				e.printStackTrace();
				continue;
			}
		}
		HttpEntity<String> request = new HttpEntity<String>(productJsonObject.toString(), headers);
		try {
			String updateProduct = "https://duepassicalzature.it/wp-json/wc/v3/products/" + idWooComm;
			restTemplate.put(updateProduct, request);
			log.info("Aggiornamento Articolo riuscito: " + p.getId_woo_commerce());
		} catch (RestClientException e) {
			log.error("Errore nell'aggiornamento dell'articolo con ID WC " + p.getId_woo_commerce());
			e.printStackTrace();
		}
	}
	
	private void aggiornaPrezziProd(String idWooComm) {
		
		List<IUpdateProducts> prodottiDaAggiornare = mappaturaRepository.aggiornamentoPrezziByIdWoo(new Integer(idWooComm));
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		log.info("Sono stati trovati " + prodottiDaAggiornare.size() + " prodotti a cui aggiornare i prezzi!");

		for (IUpdateProducts p : prodottiDaAggiornare) {

			String updateProduct = "https://duepassicalzature.it/wp-json/wc/v3/products/" + p.getParent_idwc() + "/variations/" + p.getId_woo_commerce();
			log.info("Aggiornamento prezzi prodotto con ID WooCommerce " + p.getId_woo_commerce() + " e sku " + p.getCode());

			JSONObject productJsonObject = new JSONObject();
			productJsonObject.put("sale_price", p.getListinoweb1().toString());
			productJsonObject.put("regular_price", p.getPricesell().toString());

			// CODICE FORNITORE
			try {
				JSONArray metaData = new JSONArray();
				JSONObject metaDataGitEan = new JSONObject();
				metaDataGitEan.put("key", "_woosea_gtin");
				metaDataGitEan.put("value", p.getEanprod());
				metaData.put(metaDataGitEan);
				productJsonObject.put("meta_data", metaData);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// FINE CODICE FORNITORE

			HttpEntity<String> request = new HttpEntity<String>(productJsonObject.toString(), headers);

			try {
				restTemplate.put(updateProduct, request);
				log.info("Aggiornamento riuscito!");
			} catch (RestClientException e) {
				log.error("Errore nell'aggiornamento dell'articolo con ID WC " + p.getId_woo_commerce());
				e.printStackTrace();
				continue;
			}

		}
	}

}
