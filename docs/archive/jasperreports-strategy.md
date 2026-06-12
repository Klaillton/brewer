# JasperReports Strategy

## Estado atual

- O projeto usa JasperReports apenas em um fluxo: emissao do relatorio de vendas.
- O uso da biblioteca esta centralizado em src/main/java/com/algaworks/brewer/Service/RelatorioService.java.
- O controlador correspondente esta em src/main/java/com/algaworks/brewer/controller/RelatoriosController.java.
- No repositorio existe apenas o artefato compilado src/main/resources/relatorios/relatorio_vendas_emitidas.jasper.
- O arquivo fonte .jrxml nao esta versionado.

## Risco do upgrade direto

- A vulnerabilidade aberta no GitHub exige JasperReports 7.0.4 ou superior.
- A linha 7 do JasperReports quebrou compatibilidade com arquivos compilados .jasper produzidos em versoes anteriores.
- Sem o .jrxml fonte, o upgrade direto da dependencia tem alto risco de falha em runtime.
- Mesmo mantendo a API Java parecida, o gargalo real nao e o codigo Java, e sim a compatibilidade do template binario existente.

## Conclusao pratica

- Nao vale fazer upgrade cego de 6.21.3 para 7.x neste projeto.
- Tambem nao vale migrar para DynamicReports: ele depende de JasperReports e aparenta estar defasado para um projeto novo.

## Opcao recomendada

Substituir JasperReports por geracao de PDF via HTML + Thymeleaf + OpenHTMLtoPDF.

### Motivos

- O projeto ja usa Thymeleaf extensivamente.
- Existe apenas um relatorio conhecido, o que reduz o custo de substituicao.
- O fluxo atual parece simples: filtro por periodo e retorno direto de PDF.
- A substituicao elimina dependencia de arquivo binario .jasper e de ferramenta externa para editar template.
- A manutencao futura fica mais simples porque o layout passa a ser HTML versionado no repositorio.

## Quando ainda vale manter JasperReports

Manter JasperReports so faz sentido se pelo menos uma destas condicoes for verdadeira:

- o .jrxml original puder ser recuperado;
- o layout atual tiver alta complexidade de paginacao, subreports, bandas ou componentes especificos do Jasper;
- houver expectativa de ampliar o uso de relatorios visuais desenhados em Jaspersoft Studio.

## Proximo experimento sugerido

1. Recriar o relatorio de vendas emitidas como template HTML/Thymeleaf dedicado a PDF.
2. Gerar o PDF com OpenHTMLtoPDF em um servico paralelo ao RelatorioService atual.
3. Comparar saida visual e comportamento com o PDF atual.
4. Se o resultado for aceitavel, remover JasperReports e fechar o alerta de seguranca pela substituicao.

## Plano alternativo se a equipe quiser insistir no JasperReports

1. Recuperar ou reconstruir o .jrxml original.
2. Testar conversao/atualizacao do design com Jaspersoft Studio 7.
3. Subir a dependencia para 7.0.6 em branch isolada.
4. Recompilar o template e validar a geracao do PDF antes de qualquer merge.