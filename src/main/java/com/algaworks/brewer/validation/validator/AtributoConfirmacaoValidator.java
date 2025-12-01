package com.algaworks.brewer.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.apache.commons.beanutils.BeanUtils;

import com.algaworks.brewer.validation.AtributosConfirmacao;

public class AtributoConfirmacaoValidator implements ConstraintValidator<AtributosConfirmacao, Object>{

	private String atributo;
	private String atributoConfirmacao;
	
	@Override
	public void initialize(AtributosConfirmacao constraintAnnotation) {
		this.atributo = constraintAnnotation.atributo();
		this.atributoConfirmacao = constraintAnnotation.atributoConfirmacao();
		
	}

	@Override
	public boolean isValid(Object object, ConstraintValidatorContext context) {
		boolean valido = false;
		
		try {
			Object valorAtributo = BeanUtils.getProperty(object, this.atributo);
			Object valorAtributoConfirmacao = BeanUtils.getProperty(object, this.atributoConfirmacao);
			
			valido = ambosSaoNull(valorAtributo, valorAtributoConfirmacao) || ambosSaoIguais(valorAtributo, valorAtributoConfirmacao);
			
		} catch (Exception e) {
			throw new RuntimeException("Erro recuperando valores dos atributos", e);
		}
		
		if(!valido){
			context.disableDefaultConstraintViolation();
			String mensagem = context.getDefaultConstraintMessageTemplate();
			ConstraintViolationBuilder violationBuilder = context.buildConstraintViolationWithTemplate(mensagem);
			violationBuilder.addPropertyNode(atributoConfirmacao).addConstraintViolation();
			
		}
		
		return valido;
	}

	private boolean ambosSaoIguais(Object valorAtributo, Object valorAtributoConfirmacao) {
		return valorAtributo != null && valorAtributo.equals(valorAtributoConfirmacao);
	}

	private boolean ambosSaoNull(Object valorAtributo, Object valorAtributoConfirmacao) {		
		return valorAtributo == null && valorAtributoConfirmacao == null;
	}

}
