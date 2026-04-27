-- Endurece o admin padrão legado sem quebrar FKs (ex.: vendas históricas).
-- Estratégia: remove vínculo com grupo administrador e desativa a conta.
DELETE FROM usuario_grupo
WHERE codigo_grupo = 1
  AND codigo_usuario IN (
    SELECT codigo FROM usuario WHERE email = 'admin@brewer.com'
  );

UPDATE usuario
SET ativo = 0,
    email = CONCAT('admin_desativado_', codigo, '@invalid.local')
WHERE email = 'admin@brewer.com';
