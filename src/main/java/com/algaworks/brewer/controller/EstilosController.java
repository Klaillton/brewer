package com.algaworks.brewer.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.algaworks.brewer.controller.page.PageWrapper;
import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.Estilos;
import com.algaworks.brewer.repository.filter.EstiloFilter;
import com.algaworks.brewer.service.CadastroEstiloService;
import com.algaworks.brewer.service.exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.service.exception.NomeEstiloJaCadastradoException;

@Controller
@RequestMapping("/estilos")
public class EstilosController {
	
	@Autowired
	private Estilos estilos;
	
	@Autowired
	private CadastroEstiloService cadastroEstiloService;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Estilo estilo){
		ModelAndView mv = new ModelAndView("estilo/CadastroEstilo");
		return mv;
	}
	
	@PostMapping(value = {"/novo", "{\\d+}"})
	public ModelAndView salvar(@Valid Estilo estilo, BindingResult result, Model model,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return novo(estilo);
		}

		// Salvar no banco de dados
		try {
			cadastroEstiloService.salvar(estilo);
		} catch (NomeEstiloJaCadastradoException e) {
			result.rejectValue("nome", e.getMessage(), e.getMessage());
			return novo(estilo);
		} 
		/* Tratamento dessa exceção do try-catch()
		 *  este metodo não pode ser tratado com um handler pois ele retorna 
		 * toda uma view; este metodo faz um foward (redirect) limpando a view
		 * para usar no handler tem que fazer todo o tratamento como está no
		 * catch o que acaba misturando os propositos do codigo */

		attributes.addFlashAttribute("mensagem", "Estilo salvo com sucesso!");

		return new ModelAndView("redirect:/estilos/novo");

	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ResponseEntity<?> salvar(@RequestBody @Valid Estilo estilo, BindingResult result){
		if(result.hasErrors()){/*ResponseEntity deixa controlar os status na página diferente de ModelAndView que retorna a view completa  
		// badRequest é um erro 400 (todo erro 4 vem do usuário)*/
			return ResponseEntity.badRequest().body(result.getFieldError("nome").getDefaultMessage());
		}
		
		estilo = cadastroEstiloService.salvar(estilo);
		/* esta exceção foi tratada em controller.handler */
		
		return ResponseEntity.ok(estilo);

	}
	
	@GetMapping
	public ModelAndView pesquisar(EstiloFilter estiloFilter, BindingResult result, 
			@PageableDefault(size=4) Pageable pageable, HttpServletRequest httpServletRequest){
		ModelAndView mv = new ModelAndView("estilo/PesquisaEstilos");

		
		PageWrapper<Estilo> paginaWrapper = new PageWrapper<>(estilos.filtrar(estiloFilter, pageable), 
				httpServletRequest);
 		
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("codigo") Estilo estilo) {
		try {
			cadastroEstiloService.excluir(estilo);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		Estilo estilo = estilos.findOne(codigo);
		ModelAndView mv = novo(estilo);
		mv.addObject(estilo);
		
		return mv;
	}

}




















