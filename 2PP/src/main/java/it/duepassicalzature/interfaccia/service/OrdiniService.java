package it.duepassicalzature.interfaccia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.duepassicalzature.interfaccia.DTO.OrdersDTO;
import it.duepassicalzature.interfaccia.converter.OrdersModelToDTO;
import it.duepassicalzature.interfaccia.model.Orders;
import it.duepassicalzature.interfaccia.repository.OrdersDTORepository;
import it.duepassicalzature.interfaccia.security.WooCommerceSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrdiniService {

    @Autowired
    OrdersDTORepository ordersDTORepository;

    Logger log = LoggerFactory.getLogger(OrdiniService.class);
    final String uriGetOrders = "https://duepassicalzature.it/wp-json/wc/v3/orders?per_page=100";

    public void getOrdersFromAPIandSaveToDB() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> response = null;
        List<Orders> listaOrdini = new ArrayList<>();
        String ordineJSON = "";
        log.info("Avvio GET ordini da API WooCommerce...");

        try {
            response = restTemplate.exchange
            (uriGetOrders, HttpMethod.GET, new HttpEntity<Object>(WooCommerceSecurity.createHeaders()), Object.class);
            log.info("GET ordini effettuata con successo");
        } catch (Exception e) {
            log.error("Errore nella comunicazione con l'API!");
            e.printStackTrace();
        }

        try {
            log.info("Parsing ordini in JSON...");
            ordineJSON = mapper.writeValueAsString(response.getBody());
            log.info("Parsing ordini avvenuto con successo, conversione in oggetto Orders...");

            //log.info(ordineJSON.toString());

            listaOrdini = mapper.readValue(ordineJSON, new TypeReference<List<Orders>>(){});
            log.info("Conversione in oggetto Orders, effettuata con successo! La lista contiene " + listaOrdini.size() + " ordini");
        } catch (Exception e) {
            log.error("Errore durante il parsing o nella conversione in oggetto Orders!");
            e.printStackTrace();
        }

        try {
            log.info("Conversione in DTO e salvataggio nel DB...");

            List<OrdersDTO> test = (OrdersModelToDTO.convertModelToDTO(listaOrdini));
            for(OrdersDTO o : test) {
                log.info(o.getNumber()+" "+o.getDataCreated() + " "+o.getShipping_method_id());
            }

            ordersDTORepository.saveAll(OrdersModelToDTO.convertModelToDTO(listaOrdini));
            log.info("Ordini aggiornati con successo! FINE PROCESSO");
        } catch (Exception e) {
            log.error("Errore nel salvataggio della lista Orders nel DB!");
            e.printStackTrace();
        }
    }



}
