package it.duepassicalzature.interfaccia.converter;

import it.duepassicalzature.interfaccia.model.IProductsWeb;
import it.duepassicalzature.interfaccia.model.ProductsWeb;

import java.util.ArrayList;
import java.util.List;

public class IPW2PW {

    public static List<ProductsWeb> conv2Mod(List<IProductsWeb> Iprod){

        List<ProductsWeb> listaProdotti = new ArrayList<>();

        for (IProductsWeb ip : Iprod){

            ProductsWeb p = new ProductsWeb(
                   ip.getId(),
                   ip.getCode(),
                   ip.getName(),
                   ip.getPricesell(),
                   ip.getCategory(),
                   ip.getCategory_name(),
                   ip.getCodart(),
                   ip.getCodcol(),
                   ip.getDesccol(),
                   ip.getTaglia(),
                   ip.getTarget(),
                   ip.getBrand(),
                   ip.getDescbreve(),
                   ip.getTitolo(),
                   ip.getPercorsodesc(),
                   ip.getListinoweb1(),
                   ip.getLastupdate(),
                   ip.getLastaction(),
                   ip.getGiacenza(),
                   ip.getCat_json(),
                   ip.getStagione(),
                   ip.getPercentuale(),
                   ip.getEanprod()
            );
            listaProdotti.add(p);
        }

        return listaProdotti;
    }
}
