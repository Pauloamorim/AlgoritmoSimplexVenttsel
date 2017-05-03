package com.pca.simplex.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pca.simplex.pojo.DadoFuncaoObjetivo;
import com.pca.simplex.pojo.DadoRestricao;
import com.pca.simplex.pojo.ValorSentenca;
import com.pca.simplex.pojo.estatico.SinalOperacao;
import com.pca.simplex.pojo.estatico.TipoFuncaoObjetivo;

@Service
public final class SimplexService {

	private static final BigDecimal IDENTIFICADOR_COLUNA_MEMBRO_LIVRE = new BigDecimal("0.1");
	private static final BigDecimal IDENTIFICADOR_LINHA_FUNCAO_OBJETIVA = new BigDecimal("0.2");
	private  final String POSITIVO = "positivo";
	private  final String NEGATIVO = "negativo";
	private  final BigDecimal UM_NEGATIVO = new BigDecimal("-1");
	private  BigDecimal[][] tabelaSubCelulaSuperior;
	private  BigDecimal[][] tabelaSubCelulaInferior;
	private  List<BigDecimal> listaVariaveisDecisaoRetornaraoValor = new ArrayList<BigDecimal>();

	public  void  calcularSimplex(DadoFuncaoObjetivo dadoFuncaoObjetiva) throws RuntimeException{
		listaVariaveisDecisaoRetornaraoValor = new ArrayList<BigDecimal>(0);
		System.out.println("-----------------------INICIO CALCULO SIMPLEX---------------");
		
		
		/**
		 * A matriz foi montada com o tamanho da linha + 2, para contemplar além das retrições, 
		 *  adicionar a linha de identificação(aonde ficaram x1,x2,membroLivre, FO) e
		 *  a linha da função Objetiva FO
		 */
		tabelaSubCelulaSuperior = new BigDecimal[dadoFuncaoObjetiva.getListaDadosRestricoes().size()+2][dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size()+2];
		tabelaSubCelulaInferior = new BigDecimal[dadoFuncaoObjetiva.getListaDadosRestricoes().size()+2][dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size()+2];
		
		alterarTipoFuncaoObjetiva(dadoFuncaoObjetiva);
		adicionaVariaveisAuxiliares(dadoFuncaoObjetiva);
		
		printarRestricoes(dadoFuncaoObjetiva);
		
		nomearLinhasColunasIdentificadoras(dadoFuncaoObjetiva);
		
		preencheFuncaoObjetivaTabelaSimplex(dadoFuncaoObjetiva);
		preencheRestricoesTabelaSimplex(dadoFuncaoObjetiva);
		
		imprimirTabela();
		
		procurarVariavelBasicaMembroLivreNegativo();
		
	}

	/**
	 * Se a função for de maximização, multiplica seus elementos por -1 e troca o enum para MIN
	 * @param dadoFuncaoObjetiva
	 */
	private  void alterarTipoFuncaoObjetiva(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		//se for problema de maximização, inverte para um problema de minimização
		if(TipoFuncaoObjetivo.MAX.equals(dadoFuncaoObjetiva.getTipoFuncaoObjetivo())){
			//multiplica valores da FO por -1
			for ( ValorSentenca valorSentenca : dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva()) {
				valorSentenca.setValor(valorSentenca.getValor().multiply(UM_NEGATIVO));
			}
			//altera a FO para um problema do tipo MINIMIZAÇÃO
			dadoFuncaoObjetiva.setTipoFuncaoObjetivo(TipoFuncaoObjetivo.MIN);
		}
	}
	/**
	 * Adiciona variaveis auxiliares em todas as restrições.
	 * Se for >= a variavel auxiliar será negativa
	 * Se for <= a variavel auxiliar será positiva
	 * @param dadoFuncaoObjetiva
	 */
	private  void adicionaVariaveisAuxiliares(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		//adiciona as variáveis auxiliares
		Integer proximoNumeroVariavel = obterProximoNumeroVariavel(dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva());
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			
			BigDecimal valor;
			if(SinalOperacao.MAIOR_IGUAL.equals(dadoRestricao.getSinalOperacao()) || SinalOperacao.MAIOR.equals(dadoRestricao.getSinalOperacao()))
				valor = UM_NEGATIVO;
			else
				valor = BigDecimal.ONE;
			
			//adiciona a variavel auxiliar com o valor 1 e o nome x + o próximo valor
			dadoRestricao.getListaValoresRestricoes().add(new ValorSentenca(valor,"x".concat(proximoNumeroVariavel.toString())));
			//a operação passa a ser uma igualdade
			dadoRestricao.setSinalOperacao(SinalOperacao.IGUAL);
			
			proximoNumeroVariavel++;
		}
	}
	/**
	 * Este método irá preencher as colunas que definem as variáveis, exemplo:
	 * 
	 * 			Membro Livre	| 		x1		| 		X2
	 * F(x)
	 * x3
	 * x4
	 * x5
	 * 
	 * 
	 * Como a matriz é do tipo {@link BigDecimal}, a letra "x" é omitida.
	 * Para o Membro Livre, foi definido o número 0,1
	 * Para a F(x), foi definido o número 0,2
	 * @param dadoFuncaoObjetiva
	 */
	private void nomearLinhasColunasIdentificadoras(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		
		//preenche as colunas
		int controlaLista = 0;
		for (int i = 0; i <=  dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size()+1; i++) {
			if(i==0){
				tabelaSubCelulaSuperior[0][i] = BigDecimal.ZERO;
				tabelaSubCelulaInferior[0][i] = BigDecimal.ZERO;
			}
			//coluna que identifica membro livre
			else if(i == 1){
				tabelaSubCelulaSuperior[0][i] = IDENTIFICADOR_COLUNA_MEMBRO_LIVRE;
				tabelaSubCelulaInferior[0][i] = IDENTIFICADOR_COLUNA_MEMBRO_LIVRE;
			}
			else{
				BigDecimal identificadorVariavel = new BigDecimal(dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().get(controlaLista).getNome().substring(1));
				tabelaSubCelulaSuperior[0][i] = identificadorVariavel;
				tabelaSubCelulaInferior[0][i] = identificadorVariavel;
				listaVariaveisDecisaoRetornaraoValor.add(identificadorVariavel);
				controlaLista++;
			}
		}
		

		//preenche as linhas
		
		int indiceLinha = 2; //começa da linha 2, pois as linhas 0 e 1 já foram preenchidas 
		tabelaSubCelulaSuperior[1][0] = IDENTIFICADOR_LINHA_FUNCAO_OBJETIVA;
		tabelaSubCelulaInferior[1][0] = IDENTIFICADOR_LINHA_FUNCAO_OBJETIVA;
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			BigDecimal identificadorVariavel = new BigDecimal(dadoRestricao.getListaValoresRestricoes().get(dadoRestricao.getListaValoresRestricoes().size()-1).getNome().substring(1));
			tabelaSubCelulaSuperior[indiceLinha][0] = identificadorVariavel;
			tabelaSubCelulaInferior[indiceLinha][0] = identificadorVariavel;
			indiceLinha++;
		}
	}
	/**
	 * Obtem o próximo nome para a variável auxiliar, exemplo:
	 * 		80x1 + 60x2 -> O método retornara 3 que será a próxima variável auxiliar
	 * 
	 * @param listaSentencasFuncaoObjetiva
	 * @return {@link Integer}
	 */
	private  Integer obterProximoNumeroVariavel(List<ValorSentenca> listaSentencasFuncaoObjetiva) {
		return Integer.valueOf(listaSentencasFuncaoObjetiva.get(listaSentencasFuncaoObjetiva.size()-1).getNome().substring(1)) + 1;
	}
	/**
	 * Preenche a linha da função objetiva na tabela Simplex
	 * @param dadoFuncaoObjetiva
	 * @param tabelaSubCelulaSuperior
	 */
	private  void preencheFuncaoObjetivaTabelaSimplex(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		//preenchendo a tabela, parte função objetiva
		int controlaLista = 0;
		//começa do indice 1, pois o zero é só o identificador das variaveis
		for (int i = 1; i <=  dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size()+1; i++) {
			if(i==1)
				tabelaSubCelulaSuperior[1][i] = BigDecimal.ZERO;
			else{
				tabelaSubCelulaSuperior[1][i] = dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().get(controlaLista).getValor().multiply(UM_NEGATIVO);	
				controlaLista++;
			}
		}
	}
	
	/**
	 * preenche as linhas e colunas das restrições na tabela SIMPLEX
	 * @param dadoFuncaoObjetiva
	 * @param tabelaSubCelulaSuperior
	 */
	private  void preencheRestricoesTabelaSimplex(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		int linha = 2;
		int controlaListaRestricao = 0;
		BigDecimal valorVariavelAuxiliar = BigDecimal.ZERO;
		
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			controlaListaRestricao = 0;
			
			//retorna se a variavel auxiliar é positiva ou negativa
			valorVariavelAuxiliar = dadoRestricao.getListaValoresRestricoes().get(dadoRestricao.getListaValoresRestricoes().size()-1).getValor();
			
			for (int j = 1; j <=  dadoRestricao.getListaValoresRestricoes().size(); j++) {
				//preenche o valor do membro livre
				if(j == 1)
					tabelaSubCelulaSuperior[linha][j] = dadoRestricao.getResultado().multiply(valorVariavelAuxiliar).setScale(3,RoundingMode.HALF_EVEN);
				else{
					//preenche as demais variáveis
					tabelaSubCelulaSuperior[linha][j] = dadoRestricao.getListaValoresRestricoes().get(controlaListaRestricao).getValor().multiply(valorVariavelAuxiliar).setScale(3,RoundingMode.HALF_EVEN);
					controlaListaRestricao++;
				}
			}
			linha++;
		}
	}
	
	/**
	 * Método auxiliar para imprimir a função objetiva e as restrições
	 * @param dadoFuncaoObjetiva
	 */
	private  void printarRestricoes(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		for (ValorSentenca val : dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva()) {
			System.out.print(" ".concat((val.getValor().compareTo(BigDecimal.ZERO) == 1 ? "+" : "")
					.concat(val.getValor().toString()).concat(val.getNome())));
		}
		System.out.println();
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			for (ValorSentenca valorSentenca : dadoRestricao.getListaValoresRestricoes()) {
				System.out.print(" ".concat((valorSentenca.getValor().compareTo(BigDecimal.ZERO) == 1 ? "+" : "")
						.concat(valorSentenca.getValor().toString()).concat(valorSentenca.getNome())));
			}
			System.out.print(" ".concat(dadoRestricao.getSinalOperacao().toString()).concat(" ").concat(dadoRestricao.getResultado().toString()));
			System.out.println();
		}
	}
	/**
	 * 1ª Fase - Passo 1
	 * Procura uma variável básica com membro livre negativo<br>
	 * 	- se encontrar: chama a operação 2 do algoritmo
	 * 	- se não encontrar: chama a segunda etapa da solução do problema
	 * @param tabelaSubCelulaSuperior
	 * @throws RuntimeException 
	 */
	private  void procurarVariavelBasicaMembroLivreNegativo() throws RuntimeException {
		//1ª Fase, passo 1
		//procura uma variável básica com membro livro negativo;
		boolean encontrou = false;
		for (int i = 2; i < tabelaSubCelulaSuperior.length; i++) {
			if(tabelaSubCelulaSuperior[i][1].compareTo(BigDecimal.ZERO) == -1){
				// passo 1.1
				encontrou = true;
				System.out.println("Variável básica negativa -> "+tabelaSubCelulaSuperior[i][1].toString());
				System.out.println("Indice da linha da variável básica negativa -> "+i);
				//encontrou o elemento negativo e vai para o 2º passo
				procurarElementoNegativoNaLinha(i);
			}
		}
		//não encontrou o elemento negativo, vai para a Fase 2 do algoritmo
		if(!encontrou)
			executaSegundaFaseAlgoritmoSimplex();
	}

	/**
	 * 1ª Fase - passo 2<br>
	 * Procura um elemento negativo na linha em que foi encontrado o membro livre negativo
	 * 	- se encontrar: A coluna onde o elemento foi encontrado é considerada permissível
	 * 	- se não encontrar: A Solução permissível não existe
	 * 
	 * @param indiceLinhaPossuiMembroLivreNegativo
	 * @param tabelaSubCelulaSuperior
	 * @throws RuntimeException 
	 */
	private  void procurarElementoNegativoNaLinha(int indiceLinhaPossuiMembroLivreNegativo) throws RuntimeException {
		BigDecimal elementoPermitido = null;
		Integer indiceColunaPermitida = null;
		for (int i = 2; i < tabelaSubCelulaSuperior[indiceLinhaPossuiMembroLivreNegativo].length; i++) {
		
			//verifica se é negativo o valor
			if(tabelaSubCelulaSuperior[indiceLinhaPossuiMembroLivreNegativo][i].compareTo(BigDecimal.ZERO) == -1){
				//encontrou o elemento negativo
				elementoPermitido = tabelaSubCelulaSuperior[indiceLinhaPossuiMembroLivreNegativo][i];
				indiceColunaPermitida = i;
				break;
			}
		}
		//elemento negativo não existe, Solução permissível inexistente
		if(elementoPermitido == null && indiceColunaPermitida == null){
			throw new RuntimeException("Solução Permissível Não Existe");
		}else{
			//encontrou o elemento negativo, irá para o passo 3 procurar a linha permitida
			System.out.println("Elemento negativo na linha -> "+elementoPermitido.toString());
			System.out.println("Indice da coluna do elemento permitido -> "+indiceColunaPermitida);
			procurarLinhaPermitida(indiceColunaPermitida);
		}
	}

	/**
	 * 1ª Fase - passo 3
	 * 	Busca a linha permitida, a partir do menor resultado da divisão do membro livre pelos elementos da coluna permitida<br>
	 * 	-** Só é quociente valido se o numerador e o denomidador possuirem o mesmo sinal e o denominador for diferente que zero
	 * @param indiceColunaPermitida
	 * @throws RuntimeException 
	 */
	private  void procurarLinhaPermitida(Integer indiceColunaPermitida) throws RuntimeException {
		Integer indiceLinhaPermitida = null;
		BigDecimal resultadoDivisao = new BigDecimal(Integer.MAX_VALUE); //Integer.MAX_VALUE para garantir que o primeiro valor seja sempre atribuido
		for (int i = 2; i < tabelaSubCelulaSuperior.length; i++) {
			BigDecimal membroLivre = tabelaSubCelulaSuperior[i][1];
			BigDecimal elemento = tabelaSubCelulaSuperior[i][indiceColunaPermitida];
			
			//Só é quociente valido se o numerador e o denomidador possuirem o mesmo sinal e o denominador for diferente de zero
			if(verificarSinalElemento(elemento).equals(verificarSinalElemento(membroLivre)) && elemento.compareTo(BigDecimal.ZERO) != 0){
				//executa a divisão do membro livre pelo elemento
				BigDecimal resultadoAux = membroLivre.divide(elemento,3,RoundingMode.HALF_UP);
				
				//se o resultado da divisão atual for menor que o que está gravado na variável resultadoDivisao, atribui novamente
				if(resultadoAux.compareTo(resultadoDivisao) == -1){
					resultadoDivisao = resultadoAux;
					indiceLinhaPermitida = i;
				}
			}
			
		}
		System.out.println("Indice da linha permitida ->"+indiceLinhaPermitida);
		//executa o passo 4 que é a chamada do algoritmo de troca
		executarAlgoritmoTroca(indiceLinhaPermitida,indiceColunaPermitida);
	}
	private  String verificarSinalElemento(BigDecimal elementoPermitido) {
		return elementoPermitido.compareTo(BigDecimal.ZERO) == -1 ? NEGATIVO : POSITIVO;
	}

	//----------------------------------------------------------------------------------------------------------
	//---------------------------------INÍCIO ALGORITMO DE TROCA------------------------------------------------
	//----------------------------------------------------------------------------------------------------------
	
	private  void executarAlgoritmoTroca(Integer indiceLinhaPermitida, Integer indiceColunaPermitida) throws RuntimeException {
		//PASSO 1 - calcula o inverso do elemento permitido
		BigDecimal elementoPermitidoInvertido = calculaInversoElementoPermitido(indiceLinhaPermitida,indiceColunaPermitida);
		System.out.println("Elemento permitido invertido -> "+elementoPermitidoInvertido.toString());
		
		
		boolean encontrouVariavelBasicaMembroLivreNegativo = false; 
		
		//preenche o elemento da tabelaSubCelulainferior com o elemento invertido
		tabelaSubCelulaInferior[indiceLinhaPermitida][indiceColunaPermitida] = elementoPermitidoInvertido;
		
		multiplicarLinhaPermitidaPorElementoPermitidoInvertido(elementoPermitidoInvertido,indiceLinhaPermitida,indiceColunaPermitida);
		multiplicarColunaPermitidaPorElementoPermitidoInvertido(elementoPermitidoInvertido,indiceLinhaPermitida,indiceColunaPermitida);
		
		preencherTodasCelulasTabelaSubCelulaInferiorVazias(indiceLinhaPermitida,indiceColunaPermitida);
		
		inverterLinhaPermitidaComColunaPermitida(indiceLinhaPermitida,indiceColunaPermitida);
		
		imprimirTabela();
		
		for (int i = 2; i < tabelaSubCelulaSuperior.length; i++) {
			if(tabelaSubCelulaSuperior[i][1].compareTo(BigDecimal.ZERO) == -1){
				encontrouVariavelBasicaMembroLivreNegativo = true;
				procurarVariavelBasicaMembroLivreNegativo();
			}
		}
		
		if(!encontrouVariavelBasicaMembroLivreNegativo){
			executaSegundaFaseAlgoritmoSimplex();
		}
		
	}

	private  BigDecimal calculaInversoElementoPermitido(Integer indiceLinhaPermitida, Integer indiceColunaPermitida) {
		return BigDecimal.ONE.divide(tabelaSubCelulaSuperior[indiceLinhaPermitida][indiceColunaPermitida],3,RoundingMode.HALF_UP);
	}

	/**
	 * Algoritmo de Troca - Passo 2
	 * 	Multiplica toda a linha permitida pelo inverso do elemento permitido
	 * @param elementoPermitidoInvertido
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida 
	 */
	private  void multiplicarLinhaPermitidaPorElementoPermitidoInvertido(BigDecimal elementoPermitidoInvertido,
			Integer indiceLinhaPermitida, Integer indiceColunaPermitida) {
		for (int i = 1; i < tabelaSubCelulaSuperior[indiceLinhaPermitida].length; i++) {
			//verificação para não alterar o valor do elemento permitido que já foi calculado
			if(i != indiceColunaPermitida)
				tabelaSubCelulaInferior[indiceLinhaPermitida][i] = tabelaSubCelulaSuperior[indiceLinhaPermitida][i].multiply(elementoPermitidoInvertido).setScale(3,RoundingMode.HALF_EVEN);
		}
	}
	/**
	 * Algoritmo de Troca - Passo 3
	 * 	Multiplica toda a coluna permitida pelo  elemento permitido inverso multiplicado por -1
	 * @param elementoPermitidoInvertido
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida 
	 */
	private  void multiplicarColunaPermitidaPorElementoPermitidoInvertido(BigDecimal elementoPermitidoInvertido,
			Integer indiceLinhaPermitida, Integer indiceColunaPermitida) {
		BigDecimal elementoPermitidoMultiplicadoUmNegativo = elementoPermitidoInvertido.multiply(UM_NEGATIVO);
		for (int i = 1; i < tabelaSubCelulaSuperior.length; i++) {
			
			//verificação para não alterar o valor do elemento permitido que já foi calculado
			if(i != indiceLinhaPermitida) {
				tabelaSubCelulaInferior[i][indiceColunaPermitida] = tabelaSubCelulaSuperior[i][indiceColunaPermitida].multiply(elementoPermitidoMultiplicadoUmNegativo).setScale(3,RoundingMode.HALF_EVEN);
			}
		}
		
	}
	/**
	 * Preenche todas as células da tabela inferior multiplicando da seguinte forma:
	 * 	Elemento marcado da respectiva linha da tabela inferior X elemento marcado da respectiva coluna da tabela superior
	 * 
	 * ** Elemento marcado entende-se por célula da coluna/linha permitida que já possui valor
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida
	 */
	private  void preencherTodasCelulasTabelaSubCelulaInferiorVazias(Integer indiceLinhaPermitida,
			Integer indiceColunaPermitida) {
		for (int i = 1; i < tabelaSubCelulaInferior.length; i++) {
			
			//busca o elemento que já possui valor na coluna permitida, na tabela tabelaSubCelulaInferior
			BigDecimal elementoMarcadoLinha = tabelaSubCelulaInferior[i][indiceColunaPermitida];
			for (int j = 1; j < tabelaSubCelulaInferior[i].length; j++) {
				
				//busca o elemento que já possui valor na linha permitida, na tabela tabelaSubCelulaSuperior
				BigDecimal elementoMarcadoColuna = tabelaSubCelulaSuperior[indiceLinhaPermitida][j];
				if(tabelaSubCelulaInferior[i][j] == null){
					tabelaSubCelulaInferior[i][j] = elementoMarcadoLinha.multiply(elementoMarcadoColuna).setScale(3,RoundingMode.HALF_EVEN);
				}
			}
		}
	}
	
	/**
	 * Inverte o identificador da linha permitida com o identificador da coluna permitida
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida
	 */
	private  void inverterLinhaPermitidaComColunaPermitida(Integer indiceLinhaPermitida,
			Integer indiceColunaPermitida) {
		
		//obtem os valores identificadores da linha e coluna permitidas
		BigDecimal colunaInvertida = tabelaSubCelulaSuperior[0][indiceColunaPermitida];
		BigDecimal linhaInvertida = tabelaSubCelulaSuperior[indiceLinhaPermitida][0];
		
		//inverte a coluna na tabela superior e inferior
		tabelaSubCelulaSuperior[0][indiceColunaPermitida] = linhaInvertida;
		tabelaSubCelulaInferior[0][indiceColunaPermitida] = linhaInvertida;
		
		//inverte a linha na tabela superior e inferior
		tabelaSubCelulaSuperior[indiceLinhaPermitida][0] = colunaInvertida ;
		tabelaSubCelulaInferior[indiceLinhaPermitida][0] = colunaInvertida;
		
		// monta a nova tabela após executar o algoritmo de troca
		for (int i = 1; i < tabelaSubCelulaSuperior.length; i++) {
			for (int j = 1; j < tabelaSubCelulaSuperior[i].length; j++) {
				
				//se pertencer a linha ou a coluna permitida, somente transporta o valor da tabela inferior para a tabela superior
				if(i == indiceLinhaPermitida || j == indiceColunaPermitida){
					tabelaSubCelulaSuperior[i][j] = tabelaSubCelulaInferior[i][j];
				}else{
					//soma o valor da tabela inferior com a tabela superior e atribui a nova tabela superior
					tabelaSubCelulaSuperior[i][j] = tabelaSubCelulaInferior[i][j].add(tabelaSubCelulaSuperior[i][j]);
				}
				tabelaSubCelulaInferior[i][j] = null;
			}
		}
	}
	
	
	
	//------------------------------------------------------------------
	//-----------------------2ª Fase Algoritmo Simplex
	//------------------------------------------------------------------
	
	/**
	 * Executa a segunda Fase do algoritmo Simplex
	 * @throws RuntimeException 
	 * 
	 */
	private  void executaSegundaFaseAlgoritmoSimplex() throws RuntimeException {
		System.out.println("\n\n -------------INICIOU SEGUNDA FASE ALGORITMO SIMPLEX---------------");
		procurarElementoPositivoLinhaFuncaoObjetivo();
		
	}
	
	/**
	 * Passo 1 - 2ª Fase algoritmo Simplex
	 * Procura por um elemento positivo na linha da função objetiva
	 * 	- se existe, passa para a 2ª fase da 2ª etapa do método simplex
	 *  - se não existe, ENCONTROU A SOLUÇÃO ÓTIMA
	 * @throws RuntimeException 
	 */
	private  void procurarElementoPositivoLinhaFuncaoObjetivo() throws RuntimeException {
		//não considera o membro livre
		for (int i = 2; i < tabelaSubCelulaSuperior[1].length; i++) {
			if(tabelaSubCelulaSuperior[1][i].compareTo(BigDecimal.ZERO) == 1 ){
				//encontrou elemento positivo na linha da função objetiva, irá procurar um elemento positivo na coluna agora
				procuraElementoPositivoColunaPermitida(i);
			}
		}
		//não existe elemento positivo, logo chegou a solução ótima
		System.out.println("\n\n\nChegou na solução ótima");
		String valoresVariaveisFuncaoObjetiva = encontrarValoresFinaisVariaveisFuncaoObjetiva();
		imprimirTabela();
		throw new RuntimeException("Solução ótima encontrada -> "+valoresVariaveisFuncaoObjetiva);
	}

	/**
	 * Função executada quando a solução ótima é encontrada para retornar os valores no JSON
	 * @return
	 */
	private String encontrarValoresFinaisVariaveisFuncaoObjetiva() {
		String retorno = "";
		for (BigDecimal valorVariavelDecisao : listaVariaveisDecisaoRetornaraoValor) {
			for (int i = 2; i < tabelaSubCelulaSuperior.length; i++) {
					if(tabelaSubCelulaSuperior[i][0] == valorVariavelDecisao){
						retorno = retorno.concat("X").concat(valorVariavelDecisao.toString()).concat(" = ").
								concat(tabelaSubCelulaSuperior[i][1].toString()).concat(" ");
						break;
					}
			}
			if(retorno == ""){
				retorno = retorno.concat("X").concat(valorVariavelDecisao.toString()).concat(" = ").
						concat("0").concat(" ");
			}
		}
		return retorno;
	}

	/**
	 * Passo 2 - 2ª Fase algoritmo Simplex
	 * Na coluna permitida do elemento positivo encontrado na linha da função objetivo, procura um elemento positivo
	 * 	- caso encontre, irá chamar o método para buscar a linha permitida
	 * 	-caso não encontre, a solução ótima é ilimitada
	 * @param indiceColuna
	 * @throws RuntimeException 
	 */
	private  void procuraElementoPositivoColunaPermitida(int indiceColuna) throws RuntimeException {
		 for (int i = 2; i < tabelaSubCelulaSuperior.length; i++) {
			if(tabelaSubCelulaSuperior[i][indiceColuna].compareTo(BigDecimal.ZERO) == 1){
				//encontrou o elemento positivo na linha, irá procurar qual é a a linha permitida
				procurarLinhaPermitida(indiceColuna);
			}
		}
		 //não encontrou elemento positivo na linha, logo a solução é ilimitada
		 throw new RuntimeException("Solução Ótima Ilimitada");
	}

	/**
	 * Método auxiliar para o desenvolvedor para imprmir a tabela com os valores
	 */
	private  void imprimirTabela() {
		System.out.println();
		System.out.println("------------------TABELA COM VALORES ------------------");
		for (int i = 0; i < tabelaSubCelulaSuperior.length; i++) {
			for (int j = 0; j < tabelaSubCelulaSuperior[i].length; j++) {
				System.out.print(tabelaSubCelulaSuperior[i][j]+"/"+tabelaSubCelulaInferior[i][j]+"                     ");
			}
			System.out.println();
		}
	}
}

