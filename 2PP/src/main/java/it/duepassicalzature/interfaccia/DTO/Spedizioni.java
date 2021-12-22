package it.duepassicalzature.interfaccia.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Spedizioni {

    @JsonProperty("country")
    private String country;
    @JsonProperty("state")
    private String state;
    @JsonProperty("postcode")
    private String postcode;
    @JsonProperty("city")
    private String city;
    @JsonProperty("address_1")
    private String address_1;
    @JsonProperty("address_2")
    private String address_2;

   }

