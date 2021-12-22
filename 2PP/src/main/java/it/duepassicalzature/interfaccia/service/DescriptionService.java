package it.duepassicalzature.interfaccia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Service
public class DescriptionService {

    Logger log = LoggerFactory.getLogger(DescriptionService.class);

    public String getDescription(String url) {

        List<String> lines = Collections.emptyList();
        String description = "";

        try {
            lines = Files.readAllLines(Paths.get(url), StandardCharsets.ISO_8859_1);
            Iterator<String> itr = lines.iterator();

            while (itr.hasNext()){
                description += itr.next() + " ";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return description;
    }

    public String getLongDescription(String url) {

        List<String> lines = Collections.emptyList();
        String description = "";

        try {
            lines = Files.readAllLines(Paths.get(url), StandardCharsets.ISO_8859_1);
            Iterator<String> itr = lines.iterator();

            while (itr.hasNext()){
                description += itr.next() + " ";
            }

        } catch (IOException e) {
            log.error("Descrizione non trovata per questo prodotto!");
            e.printStackTrace();
        }

        return description;
    }

    public String leggiMetaData(String url) {

        List<String> lines = Collections.emptyList();
        String description = "";

        try {
            lines = Files.readAllLines(Paths.get(url), StandardCharsets.ISO_8859_1);
            Iterator<String> itr = lines.iterator();

            while (itr.hasNext()){
                description += itr.next() + " ";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return description;
    }


}
