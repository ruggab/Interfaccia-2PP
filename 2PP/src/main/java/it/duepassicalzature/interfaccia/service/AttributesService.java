package it.duepassicalzature.interfaccia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.duepassicalzature.interfaccia.DTO.MappaturaAttributi;
import it.duepassicalzature.interfaccia.converter.OrdersModelToDTO;
import it.duepassicalzature.interfaccia.model.Orders;
import it.duepassicalzature.interfaccia.repository.DataAggiornamentoRepository;
import it.duepassicalzature.interfaccia.repository.MappaturaAttributiRepository;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttributesService {

    @Autowired
    DataAggiornamentoRepository dataAggiornamentoRepository;
    @Autowired
    MappaturaAttributiRepository attributiRepository;

    Logger log = LoggerFactory.getLogger(AttributesService.class);
    public String urlAttributi = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/";
    //public List<String> listaColori = dataAggiornamentoRepository.getAllColors();
    //public List<String> listaTaglie = dataAggiornamentoRepository.getAllSizes();
    //public List<String> listaBrand = dataAggiornamentoRepository.getAllBrands();
    public String codCol = "9";
    public String codSize = "7";
    public String codBrand = "8";
    public String codStagione = "10";

    public void inserisciAttributo(String codAttributo, List<String> listaAttributi){

        String postAttributes = urlAttributi+codAttributo+"/terms";
        HttpHeaders headers = WooCommerceSecurity.createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();

        for (String a : listaAttributi) {

            JSONObject attrJson = new JSONObject();
            attrJson.put("name", a);

            HttpEntity<String> request = new HttpEntity<String>(attrJson.toString(), headers);

            try {
                log.info("Inserimento attributo: " + a);
                restTemplate.postForObject(postAttributes, request, String.class);
            }
            catch (RestClientException e) {
                log.error("Errore nella POST dell'attributo!");
                e.printStackTrace();
            }
        }

        log.info("Inserimento attributi completato");

    }

    public void mappaAttributi() {

        String urlAttributi7 = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/7/terms?per_page=100";
        String urlAttributi8 = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/8/terms?per_page=100";
        String urlAttributi9 = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/9/terms?per_page=100";

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = WooCommerceSecurity.createHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MappaturaAttributi> listaAttributi = new ArrayList<>();
        log.info("Avvio GET attibuti da API WooCommerce...");

        ResponseEntity<String> response
                = restTemplate.exchange(urlAttributi8, HttpMethod.GET, new HttpEntity<String>(WooCommerceSecurity.createHeaders()), String.class);

        JSONArray arrayAtt = new JSONArray(response.getBody());

        for (int i=0; i < arrayAtt.length(); i++){
            MappaturaAttributi m = new MappaturaAttributi();
            JSONObject jsonObject = arrayAtt.getJSONObject(i);
            m.setIdTerms(jsonObject.getInt("id"));
            m.setName(jsonObject.getString("name"));
            m.setParent_idWC(8);
            System.out.println(m.toString());
            listaAttributi.add(m);
        }

        attributiRepository.saveAll(listaAttributi);
        log.info("Attributi salvati!");

    }

    public void aggiornaAttributi(){

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = WooCommerceSecurity.createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String urlTaglie = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/7/terms";
        String urlBrand = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/8/terms";
        String urlColori = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/9/terms";
        String urlStagioni = "https://duepassicalzature.it/wp-json/wc/v3/products/attributes/10/terms";

        List<String> listaNuoveTaglie = attributiRepository.getAllNewSizes();
        log.info("Ci sono " + listaNuoveTaglie.size()+ " nuove taglie da inserire! Inserimento in corso...");

        for (String nuovaTaglia : listaNuoveTaglie){

            JSONObject tagliaJson = new JSONObject();
            tagliaJson.put("name", nuovaTaglia);

            HttpEntity<String> request = new HttpEntity<String>(tagliaJson.toString(), headers);

            try {
                String attributoResponse = restTemplate.postForObject(urlTaglie, request, String.class);
                JSONObject attributoJson = new JSONObject(attributoResponse);
                MappaturaAttributi m = new MappaturaAttributi();
                m.setIdTerms(attributoJson.getInt("id"));
                m.setName(nuovaTaglia);
                m.setParent_idWC(7);
            } catch (RestClientException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> listaNuoviBrand = attributiRepository.getAllNewBrands();
        log.info("Ci sono " + listaNuoviBrand.size()+ " nuovi brand da inserire! Inserimento in corso...");

        for (String nuovoBrand : listaNuoviBrand){

            JSONObject brandJson = new JSONObject();
            brandJson.put("name", nuovoBrand);

            HttpEntity<String> request = new HttpEntity<String>(brandJson.toString(), headers);

            try {
                String attributoResponse = restTemplate.postForObject(urlBrand, request, String.class);
                JSONObject attributoJson = new JSONObject(attributoResponse);
                MappaturaAttributi m = new MappaturaAttributi();
                m.setIdTerms(attributoJson.getInt("id"));
                m.setName(nuovoBrand);
                m.setParent_idWC(8);
            } catch (RestClientException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> listaNuoviColori = attributiRepository.getAllNewColors();
        log.info("Ci sono " + listaNuoviColori.size()+ " nuovi colori da inserire! Inserimento in corso...");

        for (String nuovoColore : listaNuoviColori){

            JSONObject coloreJson = new JSONObject();
            coloreJson.put("name", nuovoColore);

            HttpEntity<String> request = new HttpEntity<String>(coloreJson.toString(), headers);

            try {
                String attributoResponse = restTemplate.postForObject(urlColori, request, String.class);
                JSONObject attributoJson = new JSONObject(attributoResponse);
                MappaturaAttributi m = new MappaturaAttributi();
                m.setIdTerms(attributoJson.getInt("id"));
                m.setName(nuovoColore);
                m.setParent_idWC(9);
            } catch (RestClientException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> listaNuoveStagioni = attributiRepository.getAllNewSeasons();
        log.info("Ci sono " + listaNuoveStagioni.size()+ " nuove stagioni da inserire! Inserimento in corso...");

        for (String nuovaStagione : listaNuoveStagioni){

            JSONObject stagioneJson = new JSONObject();
            stagioneJson.put("name", nuovaStagione.toUpperCase());
            log.info("Inserimento stagione: " + nuovaStagione);

            HttpEntity<String> request = new HttpEntity<String>(stagioneJson.toString(), headers);

            try {
                String attributoResponse = restTemplate.postForObject(urlStagioni, request, String.class);
                JSONObject attributoJson = new JSONObject(attributoResponse);
                MappaturaAttributi m = new MappaturaAttributi();
                m.setIdTerms(attributoJson.getInt("id"));
                m.setName(nuovaStagione);
                m.setParent_idWC(10);
                attributiRepository.save(m);
            } catch (RestClientException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            };
        }

        log.info("Aggiornamento colori, brand, taglie e stagioni completato!");

    }

}
