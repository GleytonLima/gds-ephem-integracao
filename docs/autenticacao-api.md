# Autenticação da API do integrador

Até esta versão, a API do integrador aceitava qualquer requisição. Isso permitia, por exemplo, criar eventos (que viram
signals no ePHEM), alterar `/configuracoes` e `/templates`, e ler a `ephemAdminApiKey`.

Agora a API pode exigir um **token por consumidor**. A verificação vem **desligada por padrão**, para o deploy não
derrubar ninguém. Ela só deve ser ligada depois que todos os consumidores estiverem enviando o token.

## Configuração (variáveis de ambiente do integrador)

| Variável | Padrão | Descrição |
|---|---|---|
| `INTEGRACAO_AUTH_ENABLED` | `false` | `true` passa a exigir token em toda a API, exceto na documentação (swagger) |
| `INTEGRACAO_AUTH_TOKENS` | vazio | `nome:token` separados por vírgula, ex.: `gds:<token>,etl:<token>`. O nome só aparece em log. Cada token precisa ter **24 caracteres ou mais** |

Com `INTEGRACAO_AUTH_ENABLED=true` e nenhum token configurado, o integrador **não sobe**, de propósito.

Gere um token por consumidor (e por instância: UnB e Cabo Verde devem ter tokens diferentes):

```sh
openssl rand -hex 32
```

## Como o consumidor envia

Header `Authorization`, em qualquer um dos formatos:

```
Authorization: Bearer <token>
Authorization: <token>
```

Sem token ou com token desconhecido, a resposta é **401**, e o token inválido fica registrado no log com a origem
da requisição. A documentação (`/v2/api-docs`, `/swagger-ui`) continua pública.

## Consumidores

| Consumidor | Como passa a enviar o token |
|---|---|
| Backend do GDS (`gds-admin`) | Já envia o header `Authorization` com o valor do campo **"Token de autenticação"** da tela "Configuração da integração" de cada contexto. Basta preencher o token nas URLs de produção e de homologação |
| ETL (`gds-ephem-etl`) | Precisa enviar o header. Ver a mudança no próprio repositório do ETL |
| Frontend antigo (`gds-ephem-integracao-frontend`) | Não envia nada. Um token num frontend de navegador ficaria visível a qualquer pessoa, então ele **para de funcionar** quando a autenticação for ligada. Ele está sem commits desde 2024 |

## A `ephemAdminApiKey` não sai mais na API

Em `/configuracoes`, o campo `ephemAdminApiKey` pode ser gravado (`PATCH`), mas **não aparece mais nas respostas**.
Isso vale mesmo com a autenticação desligada.

**A key que já foi exposta continua válida até ser revogada no ePHEM.** Rotacione-a no ePHEM da UnB.

## Ordem segura de ativação (por instância)

1. **Deploy desta versão** com a autenticação desligada (padrão). Nada muda para os consumidores; a key some de
   `/configuracoes`.
2. **Gerar os tokens** e colocá-los no `.env` do integrador (`INTEGRACAO_AUTH_TOKENS`), ainda com
   `INTEGRACAO_AUTH_ENABLED=false`.
3. **Configurar os consumidores**:
   - GDS: preencher o campo "Token de autenticação" dos contextos que apontam para esta instância;
   - ETL: configurar o token dele.

   Como a verificação ainda está desligada, enviar o token não muda nada.
4. **Ligar** `INTEGRACAO_AUTH_ENABLED=true` e recriar o container (`docker-compose up -d <serviço>`).
5. **Conferir**:
   - `curl -s -o /dev/null -w '%{http_code}' https://<host>/api-integracao/v1/configuracoes` deve dar **401**;
   - um relato de teste pelo GDS deve passar;
   - o log não deve mostrar `token invalido` vindo dos consumidores.
6. Se algo quebrar, voltar `INTEGRACAO_AUTH_ENABLED=false` e recriar o container. Não há mudança de banco.

Faça primeiro na homologação (`vbeapi.online`, instâncias UnB e CV) e depois na produção.
