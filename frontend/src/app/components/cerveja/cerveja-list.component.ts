import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CervejaService } from '../../services/cerveja.service';
import { Cerveja, Page } from '../../models/cerveja.model';

@Component({
  selector: 'app-cerveja-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h2>Lista de Cervejas</h2>
      
      <table *ngIf="cervejas.length > 0">
        <thead>
          <tr>
            <th>SKU</th>
            <th>Nome</th>
            <th>Estilo</th>
            <th>Valor</th>
            <th>Estoque</th>
            <th>Origem</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let cerveja of cervejas">
            <td>{{ cerveja.sku }}</td>
            <td>{{ cerveja.nome }}</td>
            <td>{{ cerveja.estilo?.nome || '-' }}</td>
            <td>{{ cerveja.valor | currency:'BRL' }}</td>
            <td>{{ cerveja.quantidadeEstoque }}</td>
            <td>{{ cerveja.origem }}</td>
            <td>
              <button class="btn btn-danger" (click)="excluir(cerveja.codigo!)">
                Excluir
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <p *ngIf="cervejas.length === 0 && !loading">
        Nenhuma cerveja cadastrada.
      </p>

      <p *ngIf="loading">Carregando...</p>

      <div class="pagination" *ngIf="totalPages > 1">
        <button class="btn btn-primary" 
                [disabled]="currentPage === 0" 
                (click)="loadPage(currentPage - 1)">
          Anterior
        </button>
        <span>Página {{ currentPage + 1 }} de {{ totalPages }}</span>
        <button class="btn btn-primary" 
                [disabled]="currentPage >= totalPages - 1" 
                (click)="loadPage(currentPage + 1)">
          Próxima
        </button>
      </div>
    </div>
  `,
  styles: [`
    .pagination {
      display: flex;
      gap: 15px;
      align-items: center;
      justify-content: center;
      margin-top: 20px;
    }
    h2 {
      margin-bottom: 20px;
      color: #333;
    }
  `]
})
export class CervejaListComponent implements OnInit {
  cervejas: Cerveja[] = [];
  loading = true;
  currentPage = 0;
  totalPages = 0;
  pageSize = 10;

  constructor(private cervejaService: CervejaService) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading = true;
    this.currentPage = page;
    this.cervejaService.listar(page, this.pageSize).subscribe({
      next: (data: Page<Cerveja>) => {
        this.cervejas = data.content;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar cervejas:', error);
        this.loading = false;
      }
    });
  }

  excluir(codigo: number): void {
    if (confirm('Tem certeza que deseja excluir esta cerveja?')) {
      this.cervejaService.excluir(codigo).subscribe({
        next: () => {
          this.loadPage(this.currentPage);
        },
        error: (error) => {
          console.error('Erro ao excluir cerveja:', error);
          alert('Erro ao excluir cerveja. Verifique se ela não está vinculada a uma venda.');
        }
      });
    }
  }
}
