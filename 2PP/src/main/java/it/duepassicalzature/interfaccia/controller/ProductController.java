package it.duepassicalzature.interfaccia.controller;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import it.duepassicalzature.interfaccia.DTO.MappaturaProdIdSku;
import it.duepassicalzature.interfaccia.model.IProductWebNew;
import it.duepassicalzature.interfaccia.repository.MappaturaRepository;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/retail/")
public class ProductController {

	@Autowired
	MappaturaRepository mappaturaRepository;

//	@GetMapping("getProductsWebParent")
//	public ResponseEntity<List<IProductWebNew>> getProductsWebParent() {
//		List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent("",null,"","");
//		return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
//	}
//	
	
	@GetMapping("getProductsWebParent")
	public ResponseEntity<List<IProductWebNew>> getProductsWebParent(@RequestParam String  codart,
			@RequestParam Integer id_woo, @RequestParam String stagione, @RequestParam String  brand) {
		List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent(codart, id_woo, stagione, brand);
		return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
	}
	
	@PostMapping("deleteproducts")
    @ResponseBody
    @Transactional
	public ResponseEntity<List<IProductWebNew>> deleteproducts(@RequestBody String params) {
		System.out.println(params);
		String[] listIdWooComm = params.split(",");
		
		for (String idWooComm : listIdWooComm) {
			mappaturaRepository.deleteProdFiglio(new Integer(idWooComm));
			mappaturaRepository.deleteProdPadre(new Integer(idWooComm));
		}
		
		List<IProductWebNew> listaMappProdPadre = mappaturaRepository.getProductsWebParent("",null,"","");
		return new ResponseEntity<List<IProductWebNew>>(listaMappProdPadre, HttpStatus.OK);
	}
	
	

    @GetMapping("getStagioni")
    public ResponseEntity<List<String>> getStagioni(){
        List<String> listaStagioni = mappaturaRepository.getStagioni();
        return new ResponseEntity<List<String>>(listaStagioni, HttpStatus.OK);
    }
    
    @GetMapping("getBrands")
    public ResponseEntity<List<String>> getBrands(){
        List<String> listaBrand = mappaturaRepository.getBrands();
        return new ResponseEntity<List<String>>(listaBrand, HttpStatus.OK);
    }
	
	

}
