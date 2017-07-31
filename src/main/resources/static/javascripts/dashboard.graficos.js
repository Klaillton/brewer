var Brewer = Brewer || {};

Brewer.GraficoVendaPorMes = (function() {
	
	function GraficoVendaPorMes(){
		this.ctx = $('#graficoVendasPorMes')[0].getContext('2d');		
	}
	
	GraficoVendaPorMes.prototype.iniciar = function() {
		$.ajax({
			url: 'vendas/totalPorMes',
			method: 'GET',
			success: onDadosRecebidos.bind(this),
			
		});
	}
	
	function onDadosRecebidos(vendasMes) {
		var meses = [];
		var valores = [];
		vendasMes.forEach(function(obj){
			meses.unshift(obj.mes);
			valores.unshift(obj.total);
		});
		
		var graficoVendasPorMes = new Chart(this.ctx, {
			type: 'line',
			data: {
				labels: meses,
				datasets: [{
					label: 'Vendas por mês',
					backgroundColor: 'rgba(26,179,148,0.5)',
					pointBorderColor: 'rgba(26,179,148,1)',
					pointBackgroundColor: '#fff',
					data: valores
				}]
			},
		});
	}
	
	return GraficoVendaPorMes;
	
}());

$(function() {
	var graficoVendaPorMes = new Brewer.GraficoVendaPorMes();
	graficoVendaPorMes.iniciar();
});