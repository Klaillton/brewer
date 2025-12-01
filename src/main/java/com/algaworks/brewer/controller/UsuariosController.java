package com.algaworks.brewer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.algaworks.brewer.Service.CadastroUsuarioService;
import com.algaworks.brewer.Service.StatusUsuario;
import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.Service.Exception.SenhaObrigatoriaUsuarioException;
import com.algaworks.brewer.Service.Exception.UsuarioJaCadastradoException;
import com.algaworks.brewer.controller.page.PageWrapper;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Grupos;
import com.algaworks.brewer.repository.Usuarios;
import com.algaworks.brewer.repository.filter.UsuarioFilter;

@Controller
@RequestMapping("/usuarios")
public class UsuariosController {
	
	@Autowired
	private CadastroUsuarioService cadastroUsuarioService;
	
	@Autowired
	private Grupos grupos;
	
	@Autowired
	private Usuarios usuarios;
	
	
	@RequestMapping("/novo")
	public ModelAndView novo(Usuario usuario) {
		ModelAndView mv = new ModelAndView("usuario/CadastroUsuario");
		mv.addObject("grupos", grupos.findAll());
		
		return mv;
	}
	
	@PostMapping({"/novo", "{\\d+}"})/*As regras ficam na camada de servico, nenhuma regra e usada no controller*/
	public ModelAndView salvar(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes){
		if(result.hasErrors()){
			return novo(usuario);
		}
		
		try {
			cadastroUsuarioService.salvar(usuario);
		} catch (UsuarioJaCadastradoException e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novo(usuario);
		} catch (SenhaObrigatoriaUsuarioException e){
			result.rejectValue("senha", e.getMessage(), e.getMessage());
			return novo(usuario);
		}
		
		attributes.addFlashAttribute("mensagem", "Usuario salvo com sucesso!");
		
		return new ModelAndView("redirect:/usuarios/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(UsuarioFilter usuarioFilter
			, @PageableDefault(size=3) Pageable pageable, HttpServletRequest httpServletRequest){
		ModelAndView mv = new ModelAndView("/usuario/PesquisaUsuarios");
		mv.addObject("usuarios", usuarios.filtrar(usuarioFilter, pageable));
		mv.addObject("grupos", grupos.findAll());
		
		PageWrapper<Usuario> paginaWrapper = new PageWrapper<>(usuarios.filtrar(usuarioFilter, pageable), 
				httpServletRequest);
 		
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@PutMapping("/status")
	@ResponseStatus(HttpStatus.OK)
	public void atualizarStatus(@RequestParam("codigos[]") Long[] codigos, @RequestParam("status") StatusUsuario statusUsuario) {
		cadastroUsuarioService.alterarStatus(codigos, statusUsuario);
	}
	
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		Usuario usuario = usuarios.buscarComGrupos(codigo);
		ModelAndView mv = novo(usuario);
		mv.addObject(usuario);
		
		return mv;
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("codigo") Usuario usuario) {
		try {
			cadastroUsuarioService.excluir(usuario);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

}









