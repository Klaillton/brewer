import { test, expect } from '@playwright/test';

test.describe('Brewer API Tests', () => {
  
  test.describe('Cervejas API', () => {
    
    test('should return cervejas list', async ({ request }) => {
      const response = await request.get('/api/cervejas');
      expect(response.status()).toBe(200);
      
      const data = await response.json();
      expect(data).toHaveProperty('content');
      expect(Array.isArray(data.content)).toBe(true);
    });

    test('should return 404 for non-existent cerveja', async ({ request }) => {
      const response = await request.get('/api/cervejas/999999');
      expect(response.status()).toBe(404);
    });
  });

  test.describe('Estilos API', () => {
    
    test('should return estilos list', async ({ request }) => {
      const response = await request.get('/api/estilos/all');
      expect(response.status()).toBe(200);
      
      const data = await response.json();
      expect(Array.isArray(data)).toBe(true);
    });
  });

  test.describe('Estados API', () => {
    
    test('should return estados list', async ({ request }) => {
      const response = await request.get('/api/estados');
      expect(response.status()).toBe(200);
      
      const data = await response.json();
      expect(Array.isArray(data)).toBe(true);
    });
  });

  test.describe('Vendas API', () => {
    
    test('should return vendas list', async ({ request }) => {
      const response = await request.get('/api/vendas');
      expect(response.status()).toBe(200);
      
      const data = await response.json();
      expect(data).toHaveProperty('content');
    });

    test('should return sales statistics', async ({ request }) => {
      const valorAnoResponse = await request.get('/api/vendas/stats/valor-ano');
      expect(valorAnoResponse.status()).toBe(200);

      const valorMesResponse = await request.get('/api/vendas/stats/valor-mes');
      expect(valorMesResponse.status()).toBe(200);

      const ticketMedioResponse = await request.get('/api/vendas/stats/ticket-medio-ano');
      expect(ticketMedioResponse.status()).toBe(200);
    });
  });

});
