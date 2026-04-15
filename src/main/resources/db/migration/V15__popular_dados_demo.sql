-- =============================================================
-- Vendedores (grupo 2 = Vendedor)
-- Senha: mesma do admin
-- =============================================================
INSERT INTO usuario (nome, email, senha, ativo) VALUES
  ('Carlos Oliveira', 'carlos@brewer.com', '$2a$10$x3dW.vGNa.OsxIBZ7qi36uScizK1I1UspCXjasBlnZ31k5yiw.KCa', 1),
  ('Ana Santos',      'ana@brewer.com',    '$2a$10$x3dW.vGNa.OsxIBZ7qi36uScizK1I1UspCXjasBlnZ31k5yiw.KCa', 1);

INSERT INTO usuario_grupo (codigo_usuario, codigo_grupo) VALUES
  ((SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'), 2),
  ((SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'),    2);

-- =============================================================
-- Clientes (2 pessoa física, 1 pessoa jurídica)
-- =============================================================
INSERT INTO cliente (nome, tipo_pessoa, cpf_cnpj, telefone, email) VALUES
  ('João Pereira',  'FISICA',    '111.444.777-35',      '(11) 91234-5678', 'joao.pereira@email.com'),
  ('Maria Lima',    'FISICA',    '987.654.321-00',      '(21) 98765-4321', 'maria.lima@email.com'),
  ('Bares e Cia',   'JURIDICA',  '24.527.248/0001-08',  '(31) 3456-7890',  'contato@barecia.com');

-- =============================================================
-- Estilos extras (IDs 5, 6, 7 — IDs 1-4 criados em V01)
-- =============================================================
INSERT INTO estilo VALUES (0, 'IPA');
INSERT INTO estilo VALUES (0, 'Stout');
INSERT INTO estilo VALUES (0, 'Weizen');

-- =============================================================
-- Cervejas (10)
-- =============================================================
INSERT INTO cerveja (sku, nome, descricao, valor, teor_alcoolico, comissao, sabor, origem, codigo_estilo, quantidade_estoque) VALUES
  ('SKU001', 'Amber Dream',        'Lager âmbar encorpada com aroma de caramelo.',              18.90, 4.8, 3.00, 'ADOCICADA', 'NACIONAL',      1, 100),
  ('SKU002', 'Dark Night',         'Lager escura com notas de chocolate amargo e café.',         22.50, 5.2, 4.00, 'AMARGA',    'NACIONAL',      2,  80),
  ('SKU003', 'Pale Rider',         'Pale lager leve e refrescante, ideal para dias quentes.',    16.00, 4.0, 2.50, 'SUAVE',     'NACIONAL',      3, 120),
  ('SKU004', 'Prague Gold',        'Pilsner com lúpulo aromático e baixo amargor.',              19.90, 4.4, 3.50, 'SUAVE',     'INTERNACIONAL', 4,  90),
  ('SKU005', 'Hops Valley IPA',    'IPA com explosão de lúpulo cítrico e tropical.',             28.00, 6.8, 5.00, 'AMARGA',    'INTERNACIONAL', 5,  60),
  ('SKU006', 'Midnight Stout',     'Stout robusto com sabor de toffee e baunilha.',              32.00, 7.5, 5.50, 'FORTE',     'INTERNACIONAL', 6,  50),
  ('SKU007', 'Bavarian Wheat',     'Weizen bávara tradicional com notas de banana e cravo.',     24.00, 5.0, 4.00, 'FRUTADA',   'INTERNACIONAL', 7,  70),
  ('SKU008', 'Sertão Lager',       'Pilsner artesanal brasileira com milho nativo.',             15.50, 4.2, 2.00, 'SUAVE',     'NACIONAL',      3, 150),
  ('SKU009', 'Caramel Ale',        'Pale lager com malte caramelo e finalização doce.',          20.00, 4.6, 3.00, 'ADOCICADA', 'NACIONAL',      3, 110),
  ('SKU010', 'Tropical Storm IPA', 'IPA com maracujá, manga e abacaxi.',                         30.00, 7.0, 5.00, 'FRUTADA',   'NACIONAL',      5,  40);

-- =============================================================
-- Vendas — 4 por cliente × 3 clientes × 2 vendedores = 24
-- Padrão por grupo de 4: EMITIDA, EMITIDA, ORCAMENTO, CANCELADA
-- =============================================================

-- ---- Carlos × João ------------------------------------------
INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-10 09:00:00', 85.80, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (2, 18.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU001'), LAST_INSERT_ID()),
  (3, 16.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU003'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-18 14:00:00', 50.90, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (1, 19.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU004'), LAST_INSERT_ID()),
  (2, 15.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU008'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-05 10:30:00', 90.00, 'ORCAMENTO',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (4, 22.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU002'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-20 16:00:00', 88.00, 'CANCELADA',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (1, 32.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU006'), LAST_INSERT_ID()),
  (2, 28.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU005'), LAST_INSERT_ID());

-- ---- Carlos × Maria -----------------------------------------
INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-12 11:00:00', 112.00, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (3, 24.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU007'), LAST_INSERT_ID()),
  (2, 20.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU009'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-25 09:30:00', 78.00, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (1, 30.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU010'), LAST_INSERT_ID()),
  (3, 16.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU003'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-08 15:00:00', 37.80, 'ORCAMENTO',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (2, 18.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU001'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-03-01 10:00:00', 77.50, 'CANCELADA',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (5, 15.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU008'), LAST_INSERT_ID());

-- ---- Carlos × Bares e Cia -----------------------------------
INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-15 08:00:00', 237.50, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (10, 16.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU003'), LAST_INSERT_ID()),
  ( 5, 15.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU008'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-28 13:00:00', 147.10, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (3, 22.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU002'), LAST_INSERT_ID()),
  (4, 19.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU004'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-14 11:00:00', 113.40, 'ORCAMENTO',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (6, 18.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU001'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-03-05 09:00:00', 148.00, 'CANCELADA',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'carlos@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (2, 32.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU006'), LAST_INSERT_ID()),
  (3, 28.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU005'), LAST_INSERT_ID());

-- ---- Ana × João ---------------------------------------------
INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-11 10:00:00', 120.00, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (3, 20.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU009'), LAST_INSERT_ID()),
  (2, 30.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU010'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-22 14:30:00', 96.00, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (4, 24.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU007'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-10 09:00:00', 120.00, 'ORCAMENTO',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (2, 32.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU006'), LAST_INSERT_ID()),
  (2, 28.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU005'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-25 16:00:00', 99.50, 'CANCELADA',
   (SELECT codigo FROM cliente WHERE email = 'joao.pereira@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (5, 19.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU004'), LAST_INSERT_ID());

-- ---- Ana × Maria --------------------------------------------
INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-14 11:00:00', 101.70, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (2, 22.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU002'), LAST_INSERT_ID()),
  (3, 18.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU001'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-30 10:00:00', 94.00, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (4, 15.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU008'), LAST_INSERT_ID()),
  (2, 16.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU003'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-12 15:30:00', 60.00, 'ORCAMENTO',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (3, 20.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU009'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-03-03 09:30:00', 80.00, 'CANCELADA',
   (SELECT codigo FROM cliente WHERE email = 'maria.lima@email.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (1, 32.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU006'), LAST_INSERT_ID()),
  (2, 24.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU007'), LAST_INSERT_ID());

-- ---- Ana × Bares e Cia --------------------------------------
INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-01-16 08:30:00', 221.00, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (8, 16.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU003'), LAST_INSERT_ID()),
  (6, 15.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU008'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-01 13:00:00', 156.20, 'EMITIDA',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (5, 19.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU004'), LAST_INSERT_ID()),
  (3, 18.90, (SELECT codigo FROM cerveja WHERE sku = 'SKU001'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-02-18 10:00:00', 120.00, 'ORCAMENTO',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (4, 30.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU010'), LAST_INSERT_ID());

INSERT INTO venda (data_criacao, valor_total, status, codigo_cliente, codigo_usuario) VALUES
  ('2026-03-10 11:00:00', 129.00, 'CANCELADA',
   (SELECT codigo FROM cliente WHERE email = 'contato@barecia.com'),
   (SELECT codigo FROM usuario WHERE email = 'ana@brewer.com'));
INSERT INTO item_venda (quantidade, valor_unitario, codigo_cerveja, codigo_venda) VALUES
  (3, 28.00, (SELECT codigo FROM cerveja WHERE sku = 'SKU005'), LAST_INSERT_ID()),
  (2, 22.50, (SELECT codigo FROM cerveja WHERE sku = 'SKU002'), LAST_INSERT_ID());
