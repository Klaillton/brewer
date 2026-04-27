-- Bootstrap manual de um administrador local (uso em desenvolvimento).
-- 1) Gere um hash BCrypt e substitua o valor em @admin_hash.
-- 2) Ajuste nome/email se desejar.
-- 3) Execute no banco alvo.

SET @admin_nome  = 'brewer';
SET @admin_email = 'brewer@brewer.com';
SET @admin_hash  = '$2a$10$ZjtZblwv5OsmfDTIWO9ZiuuYDVv8vqNKirhhkj94U8Vz.zgBj3soK';

INSERT INTO usuario (nome, email, senha, ativo)
SELECT @admin_nome, @admin_email, @admin_hash, 1
WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE email = @admin_email
);

INSERT INTO usuario_grupo (codigo_usuario, codigo_grupo)
SELECT u.codigo, 1
FROM usuario u
WHERE u.email = @admin_email
  AND NOT EXISTS (
    SELECT 1
    FROM usuario_grupo ug
    WHERE ug.codigo_usuario = u.codigo
      AND ug.codigo_grupo = 1
  );
