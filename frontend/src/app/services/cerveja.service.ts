import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { Cerveja, Page } from '../models/cerveja.model';

@Injectable({
  providedIn: 'root'
})
export class CervejaService {
  private apiUrl = `${environment.apiUrl}/cervejas`;

  constructor(private http: HttpClient) {}

  listar(page: number = 0, size: number = 10): Observable<Page<Cerveja>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<Cerveja>>(this.apiUrl, { params });
  }

  buscar(codigo: number): Observable<Cerveja> {
    return this.http.get<Cerveja>(`${this.apiUrl}/${codigo}`);
  }

  criar(cerveja: Cerveja): Observable<Cerveja> {
    return this.http.post<Cerveja>(this.apiUrl, cerveja);
  }

  atualizar(codigo: number, cerveja: Cerveja): Observable<Cerveja> {
    return this.http.put<Cerveja>(`${this.apiUrl}/${codigo}`, cerveja);
  }

  excluir(codigo: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${codigo}`);
  }
}
