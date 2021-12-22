package it.duepassicalzature.interfaccia.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.duepassicalzature.interfaccia.DTO.OrdersDTO;
import it.duepassicalzature.interfaccia.DTO.OrdersItem;
import it.duepassicalzature.interfaccia.DTO.Spedizioni;
import it.duepassicalzature.interfaccia.model.*;
import it.duepassicalzature.interfaccia.service.OrdiniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersModelToDTO {

    public static List<OrdersDTO> convertModelToDTO(List<Orders> ordersList) throws JsonProcessingException {

        List<OrdersDTO> ordersDTOList = new ArrayList<>();

        Logger log = LoggerFactory.getLogger(OrdiniService.class);

        for (Orders o : ordersList){
            OrdersDTO dto = new OrdersDTO();

            dto.setId(o.getId());
            dto.setNumber(o.getNumber());
            dto.setStatus(o.getStatus());
            dto.setCurrency(o.getCurrency());
            dto.setDataCreated(o.getDate_created());
            dto.setTotal(o.getTotal());

            dto.setBilling_first_name(o.getBilling().getFirst_name());
            dto.setBilling_last_name(o.getBilling().getLast_name());
            dto.setBilling_company(o.getBilling().getCompany());
            dto.setBilling_address_1(o.getBilling().getAddress_1());
            dto.setBilling_address_2(o.getBilling().getAddress_2());
            dto.setBilling_city(o.getBilling().getCity());
            dto.setBilling_state(o.getBilling().getState());
            dto.setBilling_postcode(o.getBilling().getPostcode());
            dto.setBilling_country(o.getBilling().getCountry());
            dto.setBilling_email(o.getBilling().getEmail());
            dto.setBilling_phone(o.getBilling().getPhone());

            dto.setShipping_first_name(o.getShipping().getFirst_name());
            dto.setShipping_last_name(o.getShipping().getLast_name());
            dto.setShipping_company(o.getShipping().getCompany());
            dto.setShipping_address_1(o.getShipping().getAddress_1());
            dto.setShipping_address_2(o.getShipping().getAddress_2());
            dto.setShipping_city(o.getShipping().getCity());
            dto.setShipping_state(o.getShipping().getState());
            dto.setShipping_postcode(o.getShipping().getPostcode());
            dto.setShipping_country(o.getShipping().getCountry());
            dto.setShipping_total(o.getShipping_total());

            dto.setPayment_method(o.getPayment_method());
            dto.setPayment_method_title(o.getPayment_method_title());
            dto.setDatePaid(o.getDate_paid());


            //PRELEVO ANCHE I DATI DEL METODO DI SPEDIZIONE
            for (ShippingLine l : o.getShipping_lines()){
                //Aggiorno i dati di spedizioni nella tabella
                dto.setShipping_method_title(l.getMethod_title());
                dto.setShipping_method_id(l.getMethod_id());
                //Se il metodo di spedizione è ritiro in sede devo prelevare
                // i dati della spediazione dal metadata
                if (l.getMethod_id().equals("local_pickup_plus")){
                    // Procedura per recuperare i dati
                    // log.info("DENTRO IF LOCAL_PICKUP_PLUS");
                    for (MetaData x : l.getMeta_data())
                    {
                        if(x.getDisplay_key().equals("_pickup_location_address"))
                        {
                            //log.info("Dentro _pickup_location_address");
                            ObjectMapper mapper = new ObjectMapper();
                            //Conversione Object to JSONString
                            String jsonString = mapper.writeValueAsString(x.getDisplay_value());
                            // Mappo sulla classe Spedizioni
                            Spedizioni Shipping = mapper.readValue(jsonString, Spedizioni.class);
                            // Scrivo sulla classe sottoinsieme
                            dto.setShipping_address_1(Shipping.getAddress_1());
                            //Aggiorno indirizzo due prendendolo dall'altro metadata in
                            // modo da avere il nome del centro commerciale
                            dto.setShipping_city(Shipping.getCity());
                            dto.setShipping_state(Shipping.getState());
                            dto.setShipping_postcode(Shipping.getPostcode());
                            dto.setShipping_country(Shipping.getCountry());
                        }
                        //inserisco il nome del centro commerciale nell'indirizzo 2
                        if(x.getDisplay_key().equals("_pickup_location_name"))
                        {
                            dto.setShipping_address_2(x.getDisplay_value().toString());
                        }
                    }
                }
            }


            List<OrdersItem> listaProdotti = new ArrayList<>();

            for (LineItem l : o.getLine_items()){
                OrdersItem prodotto = new OrdersItem();
                prodotto.setId(l.getId());
                prodotto.setName(l.getName());
                prodotto.setProduct_id(l.getProduct_id());
                prodotto.setVariation_id(l.getVariation_id());
                prodotto.setQuantity(l.getQuantity());
                prodotto.setTotal(l.getTotal());
                prodotto.setSku(l.getSku());
                prodotto.setPrice(l.getPrice());

                listaProdotti.add(prodotto);
            }

            dto.setProductsOrder(listaProdotti);
            ordersDTOList.add(dto);
        }
        return ordersDTOList;
    }













}
