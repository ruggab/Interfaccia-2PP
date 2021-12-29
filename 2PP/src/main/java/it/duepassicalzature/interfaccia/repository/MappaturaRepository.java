package it.duepassicalzature.interfaccia.repository;

import it.duepassicalzature.interfaccia.DTO.MappaturaProdIdSku;
import it.duepassicalzature.interfaccia.model.IProductWebNew;
import it.duepassicalzature.interfaccia.model.IProductsWeb;
import it.duepassicalzature.interfaccia.model.IUpdateProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MappaturaRepository extends JpaRepository<MappaturaProdIdSku, Integer> {

    @Query(value = "SELECT id_woo_commerce, sku, parent_idwc, last_update FROM public.mappatura_prod_id_sku where parent_idwc is null " , nativeQuery = true)
    List<MappaturaProdIdSku> getProdottiPadre();
  
    
    @Query(value = "SELECT MIN(code) as code, MAX(pricesell) as pricesell, TRIM(codart) as codart, MAX(desccol) as desccol, MIN(taglia) as taglia, MAX(brand) as brand, MAX(descbreve) as descbreve, \n" +
            "MAX(titolo) as titolo, MAX(percorsodesc) as percorsodesc, MAX(listinoweb1) as listinoweb1, MAX(cat_json) as cat_json, \n" +
            "MAX(id_woo_commerce) as id_woo_commerce, MAX(parent_idwc) as parent_idwc, MAX(percentuale) as percentuale, MAX(stagione) as stagione, MAX(eanprod) as eanprod, MAX(category) as category, MAX(target) as target\n" +
            "FROM public.vproducts_web AS p\n" +
            "JOIN mappatura_prod_id_sku AS m ON m.sku = p.codart\n" +
            "WHERE p.lastaction = 'U' AND lastupdate > (SELECT MAX(data_ultimo_aggiornamento)\n" +
            " FROM public.data_ultimo_aggiornamento) \n" +
            "GROUP BY TRIM(codart) ; " , nativeQuery = true)
    List<IUpdateProducts> aggiornamentoAnagraficaByCodart();

    @Query(value = "SELECT code, pricesell, codart, desccol, taglia, family, brand, descbreve, titolo, percorsodesc, listinoweb1, cat_json, id_woo_commerce, parent_idwc, eanprod\n" +
            "FROM public.vproducts_web AS p\n" +
            "JOIN mappatura_prod_id_sku AS m ON m.sku = p.code\n" +
            "WHERE parent_idwc is not null AND lastaction = 'U' AND lastupdate > (SELECT MAX(data_ultimo_aggiornamento)\n" +
            "FROM public.data_ultimo_aggiornamento) ; \n" , nativeQuery = true)
    List<IUpdateProducts> aggiornamentoPrezzi();

    @Query(value = "SELECT * FROM public.mappatura_prod_id_sku where sku = ?1" , nativeQuery = true)
    MappaturaProdIdSku cercaSeHaUnPadre(String codart);
    
    
    @Query(value = "SELECT code, pricesell, TRIM(codart) as codart, desccol, taglia, brand, descbreve,"
    		+ "  titolo, percorsodesc, listinoweb1, cat_json,"
    		+ "  id_woo_commerce, parent_idwc, percentuale, stagione, eanprod, category, target"
    		+ " FROM public.vproducts_web AS p"
    		+ " JOIN mappatura_prod_id_sku AS m ON m.sku = p.code"
    		+ " WHERE p.lastaction = 'U' AND lastupdate > (SELECT MAX(data_ultimo_aggiornamento) FROM public.data_ultimo_aggiornamento) and parent_idwc = ?1 " , nativeQuery = true)
    List<IUpdateProducts> aggiornamentoVariazioniByParent(Integer parentIdWc);
    
    
      
    @Query(value = "select distinct m.id_woo_commerce, p.titolo, p.category_name, p.codart, p.tipovariante progressivo, p.codcol,p.stagione,"
    		+ " p.target, p.brand, p.pricesell*1.22 prezzo, p.percentuale, p.listinoweb1, p.qtaweb, p.lastupdate"
    		+ " from vproducts_web_new p join mappatura_prod_id_sku m on p.codart = m.sku "
    		+ " where (?1 = '' or p.codart = ?1) and (?2 is null or m.id_woo_commerce = (CAST (CAST(?2 AS character varying) AS integer))) "
    		+ " and (?3 = '' or p.stagione = ?3) and (?4 = '' or p.brand = ?4) order by m.id_woo_commerce" , nativeQuery = true)
    List<IProductWebNew> getProductsWebParent(String codart, Integer id_woo, String stagione, String brand);
    
    
    @Query(value = "delete from mappatura_prod_id_sku where id_woo_commerce = ?1 " , nativeQuery = true)
    @Modifying
    void deleteProdFiglio(Integer idWooComm);
    
    @Query(value = "delete from mappatura_prod_id_sku where parent_idwc = ?1 " , nativeQuery = true)
    @Modifying
    void deleteProdPadre(Integer idWooComm);
    
    
    @Query(value = "select distinct stagione from vproducts_web_new " , nativeQuery = true)
    List<String> getStagioni();
    
    @Query(value = "select distinct brand from vproducts_web_new " , nativeQuery = true)
    List<String> getBrands();
  

}
