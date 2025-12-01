export interface Cerveja {
  codigo?: number;
  sku: string;
  nome: string;
  descricao?: string;
  valor: number;
  teorAlcoolico: number;
  comissao: number;
  quantidadeEstoque: number;
  origem: 'NACIONAL' | 'INTERNACIONAL';
  sabor: 'ADOCICADA' | 'AMARGA' | 'FORTE' | 'FRUTADA' | 'SUAVE';
  estilo?: Estilo;
  foto?: string;
}

export interface Estilo {
  codigo?: number;
  nome: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
