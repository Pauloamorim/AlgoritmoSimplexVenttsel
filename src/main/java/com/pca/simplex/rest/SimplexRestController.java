package com.pca.simplex.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pca.simplex.pojo.DadoFuncaoObjetivo;
import com.pca.simplex.service.SimplexService;


@RestController
public class SimplexRestController {

	@Autowired
	SimplexService service;
	
	@RequestMapping(method=RequestMethod.POST,value="/simplex")
	public String calcularSimplex(@RequestBody DadoFuncaoObjetivo dadoFuncaoObjetivo){
		service.calcularSimplex(dadoFuncaoObjetivo);
		return "chamou";
	}
	
}
