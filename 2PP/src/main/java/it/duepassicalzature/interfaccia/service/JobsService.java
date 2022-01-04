package it.duepassicalzature.interfaccia.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.duepassicalzature.interfaccia.model.DataUltimaGiacenza;
import it.duepassicalzature.interfaccia.model.DataUltimoAggiornamento;
import it.duepassicalzature.interfaccia.model.IGiacenzaWeb;
import it.duepassicalzature.interfaccia.repository.DataAggiornamentoRepository;
import it.duepassicalzature.interfaccia.repository.DataGiacenzaRepository;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;

@Service
public class JobsService {

	@Autowired
	ProductsWebService productsService;
	@Autowired
	OrdiniService ordiniService;
	@Autowired
	DescriptionService descriptionService;
	@Autowired
	AttributesService attributesService;
	@Autowired
	CategoriesService categoriesService;
	@Autowired
	ImagesService imagesService;
	@Autowired
	DataAggiornamentoRepository dataAggiornamentoRepository;
	@Autowired
	DataGiacenzaRepository giacenzaRepository;

	Logger log = LoggerFactory.getLogger(JobsService.class);

	//@Scheduled(cron = "${aggiornamentoAnagraficaProdotti}", zone = "Europe/Paris")
	public void productsJob() throws IOException {

		// Inserimento delle nuove categorie
		log.info("Aggiornamento delle nuove categorie...");
		categoriesService.aggiornaCategorie();

		// Inserimento dei nuovi brand, taglie e colori
		log.info("Aggiornamento dei nuovi attributi...");
		attributesService.aggiornaAttributi();

		// Inserimento anagrafiche nuovi prodotti
		log.info("Inserimento nuovi prodotti...");
		productsService.aggiungiProdottiDaDBaWC();

		// Aggiornamento anagrafiche prodotti
		log.info("Aggiornamento anagrafiche prodotti...");
		productsService.aggiornaProdotti();

		// Aggiornamento prezzi prodotti
		log.info("Aggiornamento prezzi prodotti...");
		productsService.aggiornaPrezzi();

		DataUltimoAggiornamento dataAttuale = new DataUltimoAggiornamento();
		dataAttuale.setId(1);
		dataAttuale.setData_ultimo_aggiornamento(new Date());
		log.info("Salvataggio data di ultimo aggiornamento in data: " + dataAttuale.getData_ultimo_aggiornamento().toString());
		dataAggiornamentoRepository.save(dataAttuale);

	}

	//@Scheduled(cron = "${aggiornamentoImmaginiProdotti}", zone = "Europe/Paris")
	public void aggiornaImmagini() throws IOException {

		// Aggiornamento nuove immagini
		log.info("Inserimento nuove immagini...");
		imagesService.aggiornaImmagini();

	}

	//@Scheduled(fixedRateString = "${aggiornamentoGiacenzaProdotti}")
	public void aggiornaGiacenze() {

		DataUltimaGiacenza dataUltimaGiacenza = new DataUltimaGiacenza();
		dataUltimaGiacenza.setId(1);
		dataUltimaGiacenza.setDataAggiornamentoGiacenza(new Date());

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Object> response = null;
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String urlGiacenzaPadri = "https://duepassicalzature.it/wp-json/wc/v3/products/";
		String urlGiacenzaFigli = "https://duepassicalzature.it/wp-json/wc/v3/products/";

		List<IGiacenzaWeb> listaGiacenzeFigli = giacenzaRepository.listaGiacenzeVariazioni();

		log.info("Giacenze variazioni da aggiornare: " + listaGiacenzeFigli.size());

		for (IGiacenzaWeb g : listaGiacenzeFigli) {

			try {
				Integer idWc = g.getId_woo_commerce();
				Integer giacenza = g.getGiacenza();
				Integer parentId = g.getParent_idwc();

				log.info("Modifica giacenza variazione con ID: " + idWc + " e ID prodotto Padre: " + parentId + " in corso...");
				response = restTemplate.exchange(urlGiacenzaFigli + parentId + "/variations/" + idWc + "?stock_quantity=" + giacenza, HttpMethod.PUT, new HttpEntity<Object>(headers), Object.class);
				log.info("Modifica effettuata con successo! Giacenza aggiornata a: " + giacenza);
			} catch (RestClientException e) {
				log.error("Errore nell'aggiornamento della giacenza con id: " + g.getId_woo_commerce());
				e.printStackTrace();
			}

		}

		List<IGiacenzaWeb> listaGiacenzePadri = giacenzaRepository.listaGiacenzePadri();

		log.info("Giacenze prodotti padre da aggiornare: " + listaGiacenzePadri.size());

		for (IGiacenzaWeb g : listaGiacenzePadri) {

			try {
				Integer idWc = g.getId_woo_commerce();
				Integer giacenza = g.getGiacenza();

				log.info("Modifica giacenza prodotto Padre con ID: " + idWc + " in corso...");
				response = restTemplate.exchange(urlGiacenzaPadri + idWc + "?stock_quantity=" + giacenza, HttpMethod.PUT, new HttpEntity<Object>(headers), Object.class);
				log.info("Modifica effettuata con successo! Giacenza aggiornata a: " + giacenza);
			} catch (RestClientException e) {
				log.error("Errore nell'aggiornamento della giacenza con ID: " + g.getId_woo_commerce());
				e.printStackTrace();
			}

		}

		giacenzaRepository.save(dataUltimaGiacenza);

	}

	public void modificaCategorieProdotti() {

		String urlmodificaProdotti = "https://duepassicalzature.it/wp-json/wc/v3/products/";

		List<Integer> scarpeUomo = dataAggiornamentoRepository.getScarpeDonna();

		String metadataArray = descriptionService.leggiMetaData("C:/note/metadata_scarpe_donna.txt");

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject productJson = new JSONObject();
		productJson.put("meta_data", new JSONArray(metadataArray));

		HttpEntity<String> request = new HttpEntity<String>(productJson.toString(), headers);

		for (Integer i : scarpeUomo) {

			try {
				log.info("modifica meta data id:" + i);
				restTemplate.put("assasa" + i, request);
				log.info("modifica successfull");
			} catch (RestClientException e) {
				log.error("errore nella modifica");
				e.printStackTrace();
			}

		}

	}

	// @Scheduled(fixedRateString = "${secondiaggiornamentoanagraficaProdotti}")
	/*
	 * public void IMMASGINJIIIadiUNNNNProdotti() throws IOException {
	 * 
	 * String urlmodificaProdotti = "https://duepassicalzature.it/wp-json/wc/v3/products/"; //26745
	 * 
	 * List<IProductsWeb> listaProdotti = dataAggiornamentoRepository.getAllProductsGrouped(); String urlImagesFolder =
	 * "https://duepassicalzature.it/wp-content/uploads/foto-prodotto/"; List<String> urlImages = new ArrayList<>();
	 * 
	 * RestTemplate restTemplate = new RestTemplate(); HttpHeaders headers = WooCommerceSecurity.createHeaders();
	 * headers.setContentType(MediaType.APPLICATION_JSON);
	 * 
	 * Document doc = Jsoup.connect(urlImagesFolder).get(); Elements links = doc.select("a[href]");
	 * 
	 * for (Element e : links) { if (e.attr("href").length() > 25) { if
	 * (e.attr("href").startsWith("/wp-content/uploads/foto-prodotto/")) {
	 * urlImages.add(e.attr("href").replace("/wp-content/uploads/foto-prodotto/", "")); } } }
	 * 
	 * JSONObject productJson = new JSONObject();
	 * 
	 * for (IProductsWeb p : listaProdotti) {
	 * 
	 * String codArt = p.getCodart(); Integer idWC = dataAggiornamentoRepository.getWCidByCodart(codArt); JSONArray
	 * imagesJson = new JSONArray(); List<MappaturaImagesWC> listaFotoToInsert = new ArrayList<>();
	 * 
	 * for (String url : urlImages){
	 * 
	 * String urlcodart = url.substring(0, url.length()-6);
	 * 
	 * if (urlcodart.equalsIgnoreCase(codArt)){ System.out.println("2. PK url db: " + url); JSONObject jsonUrlImg = new
	 * JSONObject(); jsonUrlImg.put("src", urlImagesFolder + url); imagesJson.put(jsonUrlImg);
	 * 
	 * MappaturaImagesWC mapImg = new MappaturaImagesWC(); mapImg.setId(url); mapImg.setIdWooCommerce(idWC);
	 * mapImg.setSku(codArt); mapImg.setLastUpdate(new Date()); listaFotoToInsert.add(mapImg); }
	 * 
	 * }
	 * 
	 * productJson.put("images", imagesJson);
	 * 
	 * HttpEntity<String> request = new HttpEntity<String>(productJson.toString(), headers);
	 * 
	 * try { log.info("Inserimento immagini del prodotto con idWC: " + idWC); restTemplate.put(urlmodificaProdotti +
	 * idWC, request); log.info("Inserimento successfull");
	 * 
	 * imagesRepository.saveAll(listaFotoToInsert);
	 * 
	 * log.info("salvataggio nel db effettuato"); } catch (RestClientException e) { log.error("errore nella modifica");
	 * e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public void modificaCategoriadiUNNNNProdotti(){
	 * 
	 * String urlmodificaProdotti = "https://duepassicalzature.it/wp-json/wc/v3/products/"; //26745
	 * 
	 * List<IProductsWeb> listaProdotti = dataAggiornamentoRepository.getAllProductsGrouped();
	 * 
	 * RestTemplate restTemplate = new RestTemplate(); HttpHeaders headers = WooCommerceSecurity.createHeaders();
	 * headers.setContentType(MediaType.APPLICATION_JSON);
	 * 
	 * JSONObject productJson = new JSONObject();
	 * 
	 * for (IProductsWeb p : listaProdotti) {
	 * 
	 * String codArt = p.getCodart();
	 * 
	 * Integer idWC = dataAggiornamentoRepository.getWCidByCodart(codArt);
	 * 
	 * productJson.put("categories", new JSONArray(p.getCat_json()));
	 * 
	 * HttpEntity<String> request = new HttpEntity<String>(productJson.toString(), headers);
	 * 
	 * try { log.info("modifica categorie del prodotto con id:" + codArt); restTemplate.put(urlmodificaProdotti + idWC,
	 * request); log.info("modifica successfull"); MappaturaProdIdSku map = new MappaturaProdIdSku();
	 * map.setIdWooCommerce(idWC); map.setSku(codArt); map.setLastUpdate(new Date()); mappaturaRepository.save(map);
	 * log.info("salvataggio nel db effettuato"); } catch (RestClientException e) { log.error("errore nella modifica");
	 * e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public void aggiornaCategorieSchifoso() throws JsonProcessingException {
	 * 
	 * String uriCategorie = "https://duepassicalzature.it/wp-json/wc/v3/products/categories"; List<MappaturaCatIdDbId>
	 * listaCategorieDB = categorieRepository.findAll(); HttpHeaders headers = WooCommerceSecurity.createHeaders();
	 * headers.setContentType(MediaType.APPLICATION_JSON); RestTemplate restTemplate = new RestTemplate();
	 * ResponseEntity<Object> response = null; ObjectMapper mapper = new ObjectMapper();
	 * 
	 * try { response = restTemplate.exchange (uriCategorie, HttpMethod.GET, new
	 * HttpEntity<Object>(WooCommerceSecurity.createHeaders()), Object.class);
	 * log.info("GET categorie avvenuta con successo!"); } catch (Exception e) {
	 * log.error("Errore durante la connessione all'API categories"); e.printStackTrace(); // TROW exception }
	 * 
	 * String categoriesJSON = mapper.writeValueAsString(response.getBody()); JSONArray categoriesArray = new
	 * JSONArray(categoriesJSON); List<Integer> idCatWC = new ArrayList<>();
	 * 
	 * for (int i = 0; i < categoriesArray.length(); i++){ Integer categoriesId =
	 * categoriesArray.getJSONObject(i).getInt("id"); idCatWC.add(categoriesId); }
	 * 
	 * for (MappaturaCatIdDbId m : listaCategorieDB){
	 * 
	 * if (!idCatWC.contains(m.getIdWooCommerce())){ //FAI POST con id WC
	 * 
	 * } }
	 * 
	 * }
	 */

	//@Scheduled(fixedRateString = "${prova}")
	public void prova() throws Exception {

		// Aggiornamento anagrafiche prodotti
		// log.info("Aggiornamento anagrafiche prodotti...");
		// attributesService.aggiornaAttributi();
	    productsService.aggiungiProdottiDaDBaWC();
		//productsService.aggiornaProdotti();

		// Aggiornamento prezzi prodotti
		// log.info("Aggiornamento prezzi prodotti...");
		// productsService.aggiornaPrezzi();
	}

	// @Scheduled(fixedRateString = "${secondiaggiornamentoOrdiniDaAPIaDB}")
	public void pezza() {

		String urlModificametadata = "https://duepassicalzature.it/wp-json/wc/v3/products/";

		List<Integer> scarpeUomo = dataAggiornamentoRepository.getScarpeDonna();

		String metadataArray = descriptionService.leggiMetaData("C:/note/metadata_scarpe_donna.txt");

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = WooCommerceSecurity.createHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject productJson = new JSONObject();
		productJson.put("meta_data", new JSONArray(metadataArray));

		HttpEntity<String> request = new HttpEntity<String>(productJson.toString(), headers);

		for (Integer i : scarpeUomo) {

			try {
				log.info("modifica meta data id:" + i);
				restTemplate.put(urlModificametadata + i, request);
				log.info("modifica successfull");
			} catch (RestClientException e) {
				log.error("errore nella modifica");
				e.printStackTrace();
			}

		}

	}

	// @Scheduled(fixedRateString = "${secondiaggiornamentoOrdiniDaAPIaDB}")
	// @Scheduled(fixedRateString = "${aggiornamentoGiacenzaProdotti}")
	public void getOrdersJob() throws Exception {
		ordiniService.getOrdersFromAPIandSaveToDB();
	}

}
