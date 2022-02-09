package it.duepassicalzature.interfaccia.service;

import java.util.ArrayList;
import java.util.List;

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

import it.duepassicalzature.interfaccia.DTO.MappaturaCatIdDbId;
import it.duepassicalzature.interfaccia.model.ICategoriesWeb;
import it.duepassicalzature.interfaccia.repository.DataAggiornamentoRepository;
import it.duepassicalzature.interfaccia.repository.MappaturaCategorieRepository;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;

@Service
public class CategoriesService {

    Logger log = LoggerFactory.getLogger(CategoriesService.class);
    public String urlPostCategories = "https://duepassicalzature.it/wp-json/wc/v3/products/categories";

    @Autowired
    DataAggiornamentoRepository dataAggiornamentoRepository;
    @Autowired
    MappaturaCategorieRepository mappaturaCategorieRepository;

    public void inserisciCategorie() {

        HttpHeaders headers = WooCommerceSecurity.createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<ICategoriesWeb> listaCategoriePadri = dataAggiornamentoRepository.getAllParentCategories();
        List<MappaturaCatIdDbId> mappaturaCategorieWC = new ArrayList<>();

        for (ICategoriesWeb catP : listaCategoriePadri){

            JSONObject categoryObj = new JSONObject();
            categoryObj.put("name", catP.getName());
            categoryObj.put("slug", catP.getSlug());
            log.info("Inserimento categoria padre: " + catP.getName());
            HttpEntity<String> request = new HttpEntity<String>(categoryObj.toString(), headers);
            RestTemplate restCat = new RestTemplate();

            try {
                String catReturn = restCat.postForObject(urlPostCategories, request, String.class);
                JSONObject catPadre = new JSONObject(catReturn);
                Integer idCatPadre = catPadre.getInt("id");
                MappaturaCatIdDbId mapPadre = new MappaturaCatIdDbId();
                mapPadre.setIdDatabase(catP.getId());
                mapPadre.setIdWooCommerce(idCatPadre);
                mappaturaCategorieWC.add(mapPadre);

                log.info("Id categoria padre:" + idCatPadre + " e nome: " + catP.getName() + ". Creazione sottocategorie...");

                List<ICategoriesWeb> sottoCategorie = dataAggiornamentoRepository.getAllChildCategoriesByParent(catP.getId());

                    for (ICategoriesWeb sc : sottoCategorie){

                            String nomeSottoCat = sc.getName();
                            JSONObject subCat = new JSONObject();
                            subCat.put("name", nomeSottoCat);
                            subCat.put("parent", idCatPadre);
                            subCat.put("slug", sc.getSlug());
                            HttpEntity<String> subCatReq = new HttpEntity<String>(subCat.toString(), headers);
                            RestTemplate restSubCat = new RestTemplate();
                            log.info("Inserimento sottocategoria " + nomeSottoCat);
                            try {
                                String subCatReturn = restSubCat.postForObject(urlPostCategories, subCatReq, String.class);
                                log.info("Inserita con successo");
                                JSONObject subCateg = new JSONObject(subCatReturn);
                                Integer idSubCat = subCateg.getInt("id");
                                MappaturaCatIdDbId mapFiglio = new MappaturaCatIdDbId();
                                mapFiglio.setIdDatabase(sc.getId());
                                mapFiglio.setIdWooCommerce(idSubCat);
                                mappaturaCategorieWC.add(mapFiglio);

                                List<ICategoriesWeb> nipoti = dataAggiornamentoRepository.getAllChildCategoriesByParent(sc.getId());

                                    for (ICategoriesWeb nipote : nipoti) {

                                        String nomeNipote = nipote.getName();
                                        JSONObject nipoteCat = new JSONObject();
                                        nipoteCat.put("name", nomeNipote);
                                        nipoteCat.put("parent", idSubCat);
                                        nipoteCat.put("slug", nipote.getSlug());
                                        HttpEntity<String> nipReq = new HttpEntity<String>(nipoteCat.toString(), headers);
                                        RestTemplate restNip = new RestTemplate();
                                        log.info("Inserimento sottocategoria " + nomeNipote);

                                        try {
                                            String nipoteResp = restNip.postForObject(urlPostCategories, nipReq, String.class);
                                            log.info("Terzo livello inserito con successo");
                                            JSONObject nipJson = new JSONObject(nipoteResp);
                                            Integer idNipote = nipJson.getInt("id");
                                            MappaturaCatIdDbId mapNipote = new MappaturaCatIdDbId();
                                            mapNipote.setIdDatabase(nipote.getId());
                                            mapNipote.setIdWooCommerce(idNipote);
                                            mappaturaCategorieWC.add(mapNipote);

                                            List<ICategoriesWeb> grannipoti = dataAggiornamentoRepository.getAllChildCategoriesByParent(nipote.getId());

                                                for (ICategoriesWeb grannipote : grannipoti) {

                                                    String nomeGranNipote = grannipote.getName();
                                                    JSONObject granNipoteCat = new JSONObject();
                                                    granNipoteCat.put("name", nomeGranNipote);
                                                    granNipoteCat.put("parent", idNipote);
                                                    granNipoteCat.put("slug", grannipote.getSlug());
                                                    HttpEntity<String> granNipReq = new HttpEntity<String>(granNipoteCat.toString(), headers);
                                                    RestTemplate granRestNip = new RestTemplate();
                                                    log.info("Inserimento sottocategoria quarto livello " + nomeGranNipote);

                                                        try {
                                                            String granNipoteResp = granRestNip.postForObject(urlPostCategories, granNipReq, String.class);
                                                            log.info("Quarto livello inserito con successo");
                                                            JSONObject grannipJson = new JSONObject(granNipoteResp);
                                                            Integer idGranNipote = grannipJson.getInt("id");
                                                            MappaturaCatIdDbId mapGranNip = new MappaturaCatIdDbId();
                                                            mapGranNip.setIdDatabase(grannipote.getId());
                                                            mapGranNip.setIdWooCommerce(idGranNipote);
                                                            mappaturaCategorieWC.add(mapGranNip);

                                                        } catch (RestClientException e) {
                                                            log.error("Errore nel quarto livello");
                                                            e.printStackTrace();
                                                        } catch (JSONException e) {
                                                            log.error("Errore nel quarto livello");
                                                            e.printStackTrace();
                                                    }
                                                }

                                                } catch (RestClientException e) {
                                            log.error("Errore nel terzo livello");
                                            e.printStackTrace();
                                        }
                                    }
                            } catch (RestClientException e) {
                                log.error("Errore nel secondo livello");
                                e.printStackTrace();
                            }
                    }
            }
            catch (RestClientException e) {
                log.error("Errore nella categoria principale");
                e.printStackTrace();
            }

        }

        mappaturaCategorieRepository.saveAll(mappaturaCategorieWC);

    }

    public void aggiornaCategorie(){

        List<ICategoriesWeb> listaCategorie = mappaturaCategorieRepository.categorieDaAggiungere();
        HttpHeaders headers = WooCommerceSecurity.createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();

        log.info("Trovate " + listaCategorie.size() + " da aggiungere...");
        for (ICategoriesWeb c : listaCategorie){

            if (c.getParentid() != null){
                //Inserisci categoria sotto padre categoria
                MappaturaCatIdDbId m = mappaturaCategorieRepository.getParentWC(c.getParentid());
                Integer idPadre = m.getIdWooCommerce();

                JSONObject categoryObj = new JSONObject();
                categoryObj.put("name", c.getName());
                categoryObj.put("parent", idPadre);
                log.info("Inserimento categoria figlia: " + c.getId());
                HttpEntity<String> request = new HttpEntity<String>(categoryObj.toString(), headers);

                try {
                    String catReturn = restTemplate.postForObject(urlPostCategories, request, String.class);
                    JSONObject catPadre = new JSONObject(catReturn);
                    Integer idCatPadre = catPadre.getInt("id");
                    MappaturaCatIdDbId mapPadre = new MappaturaCatIdDbId();
                    mapPadre.setIdDatabase(c.getId());
                    mapPadre.setIdWooCommerce(idCatPadre);
                    mappaturaCategorieRepository.save(mapPadre);

                    log.info("Id categoria padre:" + idCatPadre + " mappata e salvata nel DB!");
                } catch (RestClientException e) {
                    log.error("Errore nella connessione a WC!");
                    e.printStackTrace();
                } catch (JSONException e) {
                    log.error("Errore nel parsing JSON!");
                    e.printStackTrace();
                }

            }
            else {
                //Inserimento categoria padre
                JSONObject categoryObj = new JSONObject();
                categoryObj.put("name", c.getName());
                log.info("Inserimento categoria padre: " + c.getName());
                HttpEntity<String> request = new HttpEntity<String>(categoryObj.toString(), headers);

                try {
                    String catReturn = restTemplate.postForObject(urlPostCategories, request, String.class);
                    JSONObject catPadre = new JSONObject(catReturn);
                    Integer idCatPadre = catPadre.getInt("id");
                    MappaturaCatIdDbId mapPadre = new MappaturaCatIdDbId();
                    mapPadre.setIdDatabase(c.getId());
                    mapPadre.setIdWooCommerce(idCatPadre);
                    mappaturaCategorieRepository.save(mapPadre);

                    log.info("Id categoria padre:" + idCatPadre + " mappata e salvata nel DB!");
                } catch (RestClientException e) {
                    log.error("Errore nella connessione a WC!");
                    e.printStackTrace();
                } catch (JSONException e) {
                    log.error("Errore nel parsing JSON!");
                    e.printStackTrace();
                }

            }

        }


    }

}
