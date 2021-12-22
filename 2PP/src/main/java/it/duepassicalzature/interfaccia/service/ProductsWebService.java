package it.duepassicalzature.interfaccia.service;

import it.duepassicalzature.interfaccia.DTO.*;
import it.duepassicalzature.interfaccia.converter.IPW2PW;
import it.duepassicalzature.interfaccia.model.Attributes;
import it.duepassicalzature.interfaccia.model.IUpdateProducts;
import it.duepassicalzature.interfaccia.model.IVariationWeb;
import it.duepassicalzature.interfaccia.model.ProductsWeb;
import it.duepassicalzature.interfaccia.repository.*;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductsWebService {

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

	Logger log = LoggerFactory.getLogger(ProductsWebService.class);
	final String uriPostProduct = "https://duepassicalzature.it/wp-json/wc/v3/products/";

	public void aggiungiProdottiDaDBaWC() throws IOException {

		List<ProductsWeb> listaProdotti = IPW2PW.conv2Mod(dataAggiornamentoRepository.getAllProductsByCodart());
		List<String> allUrlImages = ImagesService.getAllImagesUrlFromFTP();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		log.info("Ci sono " + listaProdotti.size() + " prodotti da inserire! Inserimento in corso...");

		for (ProductsWeb prodotto : listaProdotti) {

			MappaturaProdIdSku prodottoGiaMappato = mappaturaRepository.cercaSeHaUnPadre(prodotto.getCodart());
			if (prodottoGiaMappato == null) {

				log.info("Inserimento prodotto con ID " + prodotto.getId() + " e codart " + prodotto.getCodart());

				List<IVariationWeb> attributi = attributiRepository.getTaglieColByCodArt(prodotto.getCodart());
				List<Attributes> attributesProdottos = new ArrayList<>();
				List<String> opzioniTaglie = new ArrayList<>();
				List<String> opzioniColore = new ArrayList<>();
				List<String> opzioniBrand = new ArrayList<>();
				List<String> opzioniStagione = new ArrayList<>();

				JSONObject productJsonObject = new JSONObject();
				productJsonObject.put("name", prodotto.getTitolo());
				productJsonObject.put("type", "variable");
				productJsonObject.put("sale_price", prodotto.getListinoweb1().toString());
				productJsonObject.put("regular_price", prodotto.getPricesell().toString());
				try {
					productJsonObject.put("description", descriptionService.getLongDescription(prodotto.getPercorsodesc()));
					productJsonObject.put("short_description", descriptionService.getDescription(prodotto.getDescbreve()));
				} catch (JSONException e) {
					log.error("Descrizione prodotto non trovata! Ignoramento in corso...");
					e.printStackTrace();
				}
				productJsonObject.put("sku", prodotto.getCodart());
				productJsonObject.put("slug", prodotto.getTitolo());
				productJsonObject.put("manage_stock", false);
				// Le giacenze dei prodotti padre devono avere quantità 0. Aggiornamento del 9 Marzo 2021
				// productJsonObject.put("stock_quantity", prodotto.getGiacenza());

				JSONArray categoriesJsonObject = new JSONArray(prodotto.getCatJson());
				productJsonObject.put("categories", categoriesJsonObject);

				List<String> urlImagesCodart = imagesService.getUrlImagesByCodart(prodotto.getCodart(), allUrlImages);
				JSONArray arrayImages = new JSONArray();

				for (String url : urlImagesCodart) {
					JSONObject urlJson = new JSONObject();
					urlJson.put("src", url);
					arrayImages.put(urlJson);
				}

				productJsonObject.put("images", arrayImages);

				if (arrayImages.length() == 0) {
					productJsonObject.put("status", "draft");
				} else {
					productJsonObject.put("status", "publish");
				}

				for (IVariationWeb attributo : attributi) {
					opzioniTaglie.add(attributo.getTaglia());
					opzioniColore.add(attributo.getDesccol());
					opzioniBrand.add(attributo.getBrand());
				}

				opzioniStagione.add(prodotto.getStagione().toUpperCase());

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

				/*
				 * 19-06-21, non inviare più metadata guida alle taglie if (prodotto.getCategory().substring(0,
				 * 6).equalsIgnoreCase("9998-1") && prodotto.getTarget().equalsIgnoreCase("UOMO")) { metadata = new
				 * JSONArray(descriptionService.leggiMetaData("C:/note/metadata_scarpe_uomo.txt")); }
				 * 
				 * if (prodotto.getCategory().substring(0, 6).equalsIgnoreCase("9999-1") &&
				 * prodotto.getTarget().equalsIgnoreCase("DONNA")) { metadata = new
				 * JSONArray(descriptionService.leggiMetaData("C:/note/metadata_scarpe_donna.txt")); }
				 */

				eanprod.put("key", "_woosea_gtin");
				eanprod.put("value", prodotto.getEanprod());
				metadata.put(eanprod);

				/*
				 * 19-06-21, spostare sconto applicato in TAGS, spostare stagione in attributi stagione.put("key",
				 * "stagione"); stagione.put("value", prodotto.getStagione()); metadata.put(stagione);
				 * 
				 * percentuale.put("key", "sconto_applicato"); percentuale.put("value", prodotto.getPercentuale());
				 * metadata.put(percentuale);
				 */

				JSONObject percentuale = new JSONObject();
				percentuale.put("id", getTagIdScontoApplicatoDaPercentuale(prodotto.getPercentuale()));

				JSONArray arrayTag = new JSONArray();
				arrayTag.put(percentuale);

				productJsonObject.put("meta_data", metadata);
				productJsonObject.put("tags", arrayTag);

				HttpEntity<String> request = new HttpEntity<String>(productJsonObject.toString(), headers);

				String productReturn = "";
				/***************** INSERIMENTO PRODOTTO ****************************************/
				try {
					productReturn = restTemplate.postForObject(uriPostProduct, request, String.class);
				} catch (RestClientException e) {
					log.error("Errore nel caricamento dell'articolo: " + prodotto.getCode());
					e.printStackTrace();
					continue;
				}

				System.out.println(productReturn);

				JSONObject prodottoPadre = new JSONObject(productReturn);

				Integer idProdPadre = prodottoPadre.getInt("id");
				log.info("ID prodotto padre:" + idProdPadre + ". Creazione variazioni taglia/colore...");

				MappaturaProdIdSku mapProdSku = new MappaturaProdIdSku();
				mapProdSku.setIdWooCommerce(idProdPadre);
				mapProdSku.setSku(prodotto.getCodart());
				mappaturaRepository.save(mapProdSku);

				List<MappaturaImagesWC> immaginiDaMappare = new ArrayList<>();

				for (String urlCompleta : urlImagesCodart) {
					MappaturaImagesWC m = new MappaturaImagesWC();
					m.setIdWooCommerce(idProdPadre);
					m.setSku(prodotto.getCodart());
					m.setId(urlCompleta.replace("/wp-content/uploads/foto-prodotto/", ""));
					m.setLastUpdate(new Date());
					immaginiDaMappare.add(m);
				}

				imagesRepository.saveAll(immaginiDaMappare);

				String uriPostVariazione = uriPostProduct + idProdPadre + "/variations";

				for (IVariationWeb attributo : attributi) {

					JSONObject varProductJsonObject = new JSONObject();

					varProductJsonObject.put("type", "variation");
					varProductJsonObject.put("sale_price", prodotto.getListinoweb1().toString());
					varProductJsonObject.put("regular_price", prodotto.getPricesell().toString());
					varProductJsonObject.put("sku", attributo.getCode());
					varProductJsonObject.put("manage_stock", true);
					varProductJsonObject.put("stock_quantity", attributo.getGiacenza());

					JSONObject vartagliejson = new JSONObject();
					vartagliejson.put("id", 7);
					vartagliejson.put("name", "Taglia");
					vartagliejson.put("option", attributo.getTaglia());
					JSONObject varcolorejson = new JSONObject();
					varcolorejson.put("id", 9);
					varcolorejson.put("name", "Colore");
					varcolorejson.put("option", attributo.getDesccol());
					JSONObject varbrandjson = new JSONObject();
					varbrandjson.put("id", 8);
					varbrandjson.put("name", "Brand");
					varbrandjson.put("option", attributo.getBrand());
					JSONObject varstagionijson = new JSONObject();
					varbrandjson.put("id", 10);
					varbrandjson.put("name", "Stagione");
					varbrandjson.put("option", prodotto.getStagione().toUpperCase());

					JSONArray arraytagcol = new JSONArray();
					arraytagcol.put(varcolorejson);
					arraytagcol.put(vartagliejson);
					arraytagcol.put(varbrandjson);
					arraytagcol.put(varstagionijson);

					varProductJsonObject.put("attributes", arraytagcol);

					// CODICE FORNITORE
					try {
						JSONArray metaData = new JSONArray();
						JSONObject metaDataGitEan = new JSONObject();
						metaDataGitEan.put("key", "_woosea_gtin");
						metaDataGitEan.put("value", attributo.getEanprod());
						metaData.put(metaDataGitEan);
						varProductJsonObject.put("meta_data", metaData);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// FINE CODICE FORNITORE
					/***************** INSERIMENTO VARIAZIONI ****************************************/
					HttpEntity<String> requestvar = new HttpEntity<String>(varProductJsonObject.toString(), headers);

					try {
						String varproductReturn = restTemplate.postForObject(uriPostVariazione, requestvar, String.class);

						JSONObject variazioneJson = new JSONObject(varproductReturn);

						Integer idVariazioneWC = variazioneJson.getInt("id");

						MappaturaProdIdSku mapVarSku = new MappaturaProdIdSku();
						mapVarSku.setIdWooCommerce(idVariazioneWC);
						mapVarSku.setSku(attributo.getCode());
						mapVarSku.setParentIdWC(idProdPadre);

						mappaturaRepository.save(mapVarSku);
					} catch (RestClientException e) {
						log.error("Errore nella comunicazione con WC!");
						e.printStackTrace();
					} catch (JSONException e) {
						log.error("Errore nel parsing JSON!");
						e.printStackTrace();
					}
					log.info("Prodotto inserito con successo!");
				}
			}

			if (prodottoGiaMappato != null) {

				String urlVariazione = "https://duepassicalzature.it/wp-json/wc/v3/products/" + prodottoGiaMappato.getIdWooCommerce() + "/variations";

				List<IVariationWeb> attributi = attributiRepository.getTaglieColByCodArtToInsert(prodotto.getCodart());

				for (IVariationWeb attributo : attributi) {

					log.info("Inserimento nuova variazione del prodotto " + attributo.getCode() + " e codart " + attributo.getCodart());

					JSONObject varProductJsonObject = new JSONObject();

					varProductJsonObject.put("type", "variation");
					varProductJsonObject.put("sale_price", prodotto.getListinoweb1().toString());
					varProductJsonObject.put("regular_price", prodotto.getPricesell().toString());
					varProductJsonObject.put("sku", attributo.getCode());
					varProductJsonObject.put("manage_stock", true);
					varProductJsonObject.put("stock_quantity", attributo.getGiacenza());

					JSONObject vartagliejson = new JSONObject();
					vartagliejson.put("id", 7);
					vartagliejson.put("name", "Taglia");
					vartagliejson.put("option", attributo.getTaglia());
					JSONObject varcolorejson = new JSONObject();
					varcolorejson.put("id", 9);
					varcolorejson.put("name", "Colore");
					varcolorejson.put("option", attributo.getDesccol());
					JSONObject varbrandjson = new JSONObject();
					varbrandjson.put("id", 8);
					varbrandjson.put("name", "Brand");
					varbrandjson.put("option", attributo.getBrand());
					JSONObject varstagionijson = new JSONObject();
					varbrandjson.put("id", 10);
					varbrandjson.put("name", "Stagione");
					varbrandjson.put("option", prodotto.getStagione().toUpperCase());

					JSONArray arraytagcol = new JSONArray();
					arraytagcol.put(varcolorejson);
					arraytagcol.put(vartagliejson);
					arraytagcol.put(varbrandjson);
					arraytagcol.put(varstagionijson);

					varProductJsonObject.put("attributes", arraytagcol);

					// CODICE FORNITORE
					try {
						JSONArray metaData = new JSONArray();
						JSONObject metaDataGitEan = new JSONObject();
						metaDataGitEan.put("key", "_woosea_gtin");
						metaDataGitEan.put("value", attributo.getEanprod());
						metaData.put(metaDataGitEan);
						varProductJsonObject.put("meta_data", metaData);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// FINE CODICE FORNITORE

					HttpEntity<String> requestvar = new HttpEntity<String>(varProductJsonObject.toString(), headers);

					try {
						String varproductReturn = restTemplate.postForObject(urlVariazione, requestvar, String.class);

						JSONObject variazioneJson = new JSONObject(varproductReturn);

						Integer idVariazioneWC = variazioneJson.getInt("id");

						MappaturaProdIdSku mapVarSku = new MappaturaProdIdSku();
						mapVarSku.setIdWooCommerce(idVariazioneWC);
						mapVarSku.setSku(attributo.getCode());
						mapVarSku.setParentIdWC(prodottoGiaMappato.getIdWooCommerce());

						mappaturaRepository.save(mapVarSku);
					} catch (RestClientException e) {
						log.error("Errore nella comunicazione con WC!");
						e.printStackTrace();
					} catch (JSONException e) {
						log.error("Errore nel parsing JSON!");
						e.printStackTrace();
					}
					log.info("Prodotto inserito con successo!");
				}
			}

		}

		log.info("Inserimento nuovi prodotti completato!");

	}

	public void aggiornaProdotti() {

		List<IUpdateProducts> prodottiDaAggiornare = mappaturaRepository.aggiornamentoAnagraficaByCodart();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		log.info("Sono stati trovati " + prodottiDaAggiornare.size() + " prodotti padre a cui aggiornare le anagrafiche!");

		for (IUpdateProducts p : prodottiDaAggiornare) {

			String updateProduct = "https://duepassicalzature.it/wp-json/wc/v3/products/" + p.getId_woo_commerce();

			// "/variations";

			log.info("Aggiornamento anagrafica prodotto con ID WooCommerce " + p.getId_woo_commerce() + " e codart " + p.getCodart());

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

			/*
			 * 19-06-21, non inviare più metadata guida alle taglie if (p.getCategory().substring(0,
			 * 6).equalsIgnoreCase("9998-1") && p.getTarget().equalsIgnoreCase("UOMO")) { metadata = new
			 * JSONArray(descriptionService.leggiMetaData("C:/note/metadata_scarpe_uomo.txt")); }
			 * 
			 * if (p.getCategory().substring(0, 6).equalsIgnoreCase("9999-1") &&
			 * p.getTarget().equalsIgnoreCase("DONNA")) { metadata = new
			 * JSONArray(descriptionService.leggiMetaData("C:/note/metadata_scarpe_donna.txt")); }
			 */

			eanprod.put("key", "_woosea_gtin");
			eanprod.put("value", p.getEanprod());
			metadata.put(eanprod);

			/*
			 * 19-06-21, spostare sconto applicato in TAGS, spostare stagione in attributi stagione.put("key",
			 * "stagione"); stagione.put("value", p.getStagione()); metadata.put(stagione);
			 * 
			 * percentuale.put("key", "sconto_applicato"); percentuale.put("value", p.getPercentuale());
			 * metadata.put(percentuale);
			 */

			JSONObject percentuale = new JSONObject();
			percentuale.put("id", getTagIdScontoApplicatoDaPercentuale(p.getPercentuale()));

			JSONArray arrayTag = new JSONArray();
			arrayTag.put(percentuale);

			productJsonObject.put("tags", arrayTag);
			productJsonObject.put("meta_data", metadata);

			List<IUpdateProducts> variazioniDaAggiornare = mappaturaRepository.aggiornamentoVariazioniByParent(p.getId_woo_commerce());
			JSONArray arraytagcol = new JSONArray();
			for (IUpdateProducts variazione : variazioniDaAggiornare) {

				String urlVariazione = "https://duepassicalzature.it/wp-json/wc/v3/products/" + p.getId_woo_commerce() + "/variations/" + variazione.getId_woo_commerce();
				log.info("Inserimento nuova variazione del prodotto " + variazione.getCode() + " e codart " + variazione.getCodart());

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
					log.info("Aggiornamento riuscito!");
				} catch (RestClientException e) {
					log.error("Errore nell'aggiornamento dell'articolo con ID WC " + variazione.getId_woo_commerce());
					e.printStackTrace();
					continue;
				}
			}

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

	public void aggiornaPrezzi() {

		List<IUpdateProducts> prodottiDaAggiornare = mappaturaRepository.aggiornamentoPrezzi();
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

}
