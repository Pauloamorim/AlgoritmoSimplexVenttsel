package com.pca.simplex.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.pca.simplex.pojo.DetalhesResposta;



@ControllerAdvice
public class ResourceExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<DetalhesResposta> handleLivroNaoEncontradoException
							(Exception  e, HttpServletRequest request) {
		
		DetalhesResposta resposta = new DetalhesResposta();
		resposta.setMensagem(e.getMessage());
		
		return ResponseEntity.status(HttpStatus.OK).body(resposta);
	}
	
}