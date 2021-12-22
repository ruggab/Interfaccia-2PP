package it.duepassicalzature.interfaccia.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.duepassicalzature.interfaccia.DTO.MappaturaImagesWC;
import it.duepassicalzature.interfaccia.DTO.MappaturaProdIdSku;
import it.duepassicalzature.interfaccia.repository.MappaturaImagesRepository;
import it.duepassicalzature.interfaccia.repository.MappaturaRepository;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;

@Service
public class ImagesService {

    @Autowired
    MappaturaImagesRepository imagesRepository;
    @Autowired
    MappaturaRepository mappaturaRepository;

    Logger log = LoggerFactory.getLogger(ImagesService.class);


    public static List<String> getUrlImagesByCodart(String codart, List<String> urlImages) {
        String urlImagesFolder = "https://duepassicalzature.it/archivio-foto-prodotto/";
        List<String> urlProduct = new ArrayList<>();
        for (String url : urlImages){
            String str = url;
            String result = null;
            if ((str != null) && (str.length() > 4)) {
                result = str.substring(0, str.length() - 6);
                if (result.equals(codart.trim())){
                    urlProduct.add(urlImagesFolder + str);
                }
            }
        }
        System.out.println(urlProduct.toString());
        return urlProduct;
    }

    public static List<String> getAllImagesUrlFromFTP() throws IOException {

        String urlImagesFolder = "https://duepassicalzature.it/archivio-foto-prodotto/";
        List<String> urlImages = new ArrayList<>();

        Document doc = Jsoup.connect(urlImagesFolder).get();
        Elements links = doc.select("a[href]");

        for (Element e : links){
            if (e.attr("href").length() > 30){
                if (e.attr("href").startsWith("/archivio-foto-prodotto/")){
                    urlImages.add(e.attr("href").replace("/archivio-foto-prodotto/", ""));
                }
            }
        }

       
        return urlImages;
    }

    public void aggiornaImmagini() throws IOException {

        String urlmodificaProdotti = "https://duepassicalzature.it/wp-json/wc/v3/products/";
        String urlImagesFolder = "https://duepassicalzature.it/archivio-foto-prodotto/";
        List<MappaturaImagesWC> immaginiDB = imagesRepository.findAll();
        List<String> urlImages = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = WooCommerceSecurity.createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Document doc = Jsoup.connect(urlImagesFolder).get();
        Elements links = doc.select("a[href]");

        Integer lunghezzaUrl = urlImagesFolder.length();

        for (Element e : links) {
            if (e.attr("href").length() > 30) {
                if (e.attr("href").startsWith("/archivio-foto-prodotto/")) {
                    urlImages.add(e.attr("href").replace("/archivio-foto-prodotto/", ""));
                }
            }
        }

        for (MappaturaImagesWC m : immaginiDB){

            if (urlImages.contains(m.getId())){
                urlImages.remove(m.getId());
            }

        }

        System.out.println(urlImages.toString());
        log.info("La lista contiene " + urlImages.size() + " nuove immagini da caricare.");

        List<MappaturaProdIdSku> listaProdotti = mappaturaRepository.getProdottiPadre();

        for (MappaturaProdIdSku p : listaProdotti) {


                try {
                    log.info("Analizzo se il prodotto con ID " + p.getIdWooCommerce() + " non ha foto caricate...");
                    ResponseEntity<String> response
                            = restTemplate.exchange(urlmodificaProdotti + p.getIdWooCommerce(), HttpMethod.GET, new HttpEntity<String>(WooCommerceSecurity.createHeaders()), String.class);

                    JSONObject prodottoJson = new JSONObject(response.getBody());

                    JSONArray arrayJsonImg = prodottoJson.getJSONArray("images");

                    if (arrayJsonImg.length() == 0) {

                        List<String> immaginiDaAggiungere = new ArrayList<>();

                        for (String url : urlImages) {

                            String codart = url.substring(0, url.length() - 6);

                            if (codart.equals(p.getSku())) {
                                immaginiDaAggiungere.add(url);
                            }

                        }

                        if (immaginiDaAggiungere.size() > 0) {

                            JSONObject productJson = new JSONObject();
                            productJson.put("status", "publish");
                            JSONArray immaginiJSON = new JSONArray();

                            for (String immagine : immaginiDaAggiungere) {

                                JSONObject imgJson = new JSONObject();
                                String urlCompleta = urlImagesFolder + immagine;
                                imgJson.put("src", urlCompleta);
                                immaginiJSON.put(imgJson);
                            }

                            productJson.put("images", immaginiJSON);

                            HttpEntity<String> request = new HttpEntity<String>(productJson.toString(), headers);

                            try {
                                log.info("Inserimento immagini al prodotto con codart: " + p.getSku() + " e ID WC: " + p.getIdWooCommerce());
                                restTemplate.put(urlmodificaProdotti + p.getIdWooCommerce(), request);
                                log.info("Inserimento effettuato!");

                                List<MappaturaImagesWC> listaImmaginiDaSalvare = new ArrayList<>();

                                for (String immagine : immaginiDaAggiungere) {

                                    MappaturaImagesWC map = new MappaturaImagesWC();
                                    map.setId(immagine);
                                    map.setSku(p.getSku());
                                    map.setLastUpdate(new Date());
                                    map.setIdWooCommerce(p.getIdWooCommerce());
                                    listaImmaginiDaSalvare.add(map);
                                }

                                imagesRepository.saveAll(listaImmaginiDaSalvare);


                            } catch (RestClientException e) {
                                log.error("Errore nell'inserimento immagini");
                                e.printStackTrace();
                            }

                        }
                    }
                } catch (RestClientException e) {
                    log.error("Nessun dato da WC per il prodotto con ID: " + p.getIdWooCommerce());
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }

        log.info("Aggiornamento immagini completato!");

    }


}
