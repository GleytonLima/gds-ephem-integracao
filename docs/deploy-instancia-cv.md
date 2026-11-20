# Deploy da instância de Cabo Verde (ePHEM API v2)

Cada integrador atende **um** ePHEM. A instância de Cabo Verde usa a **mesma imagem** da UnB
(`gleytonlima/gds-ephem-integracao`) e muda só a configuração: outro `.env`, outro database e
outro host público. O GDS não sabe qual protocolo é usado: ele só aponta o contexto de Cabo Verde
para a URL dessa instância.

```
GDS, contexto UnB         ──> integracaoapp     (vbeapi.online/)    ──> ePHEM UnB    (XML-RPC)
GDS, contexto Cabo Verde  ──> integracaoapp-cv  (vbeapi.online/cv/) ──> cv.pheoc.com (API v2 + XML-RPC)
```

Na instância CV, os signals são criados pela API v2. Mensagens e consulta de status continuam
pelo XML-RPC, porque a v2 ainda não cobre essas operações.

## Onde está cada parte

| Parte | Onde |
|---|---|
| Compose, `.env` e banco do servidor | Repositório [GleytonLima/gds-vbe-api](https://github.com/GleytonLima/gds-vbe-api): arquivos `docker-compose.cv.yml`, `app/.env.cv.exemplo`, `db/initdb.sh` e a seção "Instância de Cabo Verde" do README |
| Publicação da imagem e reinício das instâncias | [.github/workflows/build_and_publish.yaml](../.github/workflows/build_and_publish.yaml): tags `latest` e SHA; reinicia `integracaoapp-cv` quando o serviço existe no compose do servidor |
| Configuração da aplicação (abaixo) | API do integrador (`/configuracoes`, `/templates`) e tela de integração do GDS |

## Passo a passo

1. **Subir a instância no servidor**: siga a seção "Instância de Cabo Verde" do README do gds-vbe-api.
   O script `scripts/ativar-instancia-cv.sh` de lá cobre o database `integracao_cv`, o `app/.env.cv`,
   o `COMPOSE_FILE` e a subida. A instância fica no caminho `/cv/` do mesmo host, sem DNS novo; isso
   exige nginx-proxy 1.1 ou mais novo. As tabelas são
   criadas pelo Flyway na primeira subida, já com o template 1 e a linha 1 de `configuracao_sistema`.

2. **`configuracao_sistema`**: a linha criada pela migration tem ids de usuários da UnB. Ajuste para
   os ids do ePHEM de Cabo Verde:
   - `ephemGdsUserId`: o *partner* do usuário da integração (no cv: 2095, Isaac);
   - `ephemAdminUserId`: o *partner* de quem recebe as mensagens.

   São ids de `res.partner`, usados em `author_id` e `partner_ids` das mensagens, e não de `res.users`.
   Sem isso, o envio e a listagem de mensagens apontam para as pessoas erradas.
   ```sh
   curl -X PATCH https://vbeapi.online/cv/api-integracao/v1/configuracoes/1 \
     -H 'Content-Type: application/json' \
     -d '{"ephemGdsUserId": <id>, "ephemAdminUserId": <id>}'
   ```
   Atenção: hoje o integrador não exige autenticação, porque o `WebSecurityDisabledConfiguration` libera
   tudo. Por isso esse endpoint, que devolve inclusive a `ephemAdminApiKey`, fica aberto na internet.
   `communityLeadersSourceId` só vale no caminho XML-RPC. Na v2, a fonte é o API source "GDS"
   cadastrado no ePHEM.

3. **Template**: o template 1, criado pela migration, já lê o lugar de `evento_estado_ocorrencia`
   (ilha) e `evento_municipio_ocorrencia` (concelho), que são as chaves que o GDS envia. O schema e o
   `inputEphemMap` do template só são usados no caminho XML-RPC. Se o formulário de Cabo Verde usar
   outras chaves, ajuste o `locationMap` com `PATCH /templates/1`.

4. **GDS**: na tela "Configuração da integração" do contexto Cabo Verde, preencha:
   - URL de homologação com `https://vbeapi.online/cv` (produção: `https://prod.vbeapi.online/cv`);
   - o mesmo token e o mesmo mapeamento do contexto UnB.

5. **Teste de fumaça**: envie um relato de teste pelo GDS e confira:
   - o log com `lugares de CV carregados` e `publicado na ePHEM API v2`;
   - no ePHEM, o signal com o `external_id` do prefixo configurado.

## Deploys seguintes

Nada muda: o push na `main` publica a imagem e o passo de SSH reinicia `integracaoapp` e, se ele
existir no compose do servidor, `integracaoapp-cv`. Para voltar só a instância CV a uma versão
anterior, veja o README do gds-vbe-api.

## Homologação e produção

| Host | Instância CV aponta para | Prefixo |
|---|---|---|
| `vbeapi.online` (homologação) | `cv.pheoc.com` (dev de Cabo Verde) | `GDS-HOMOL-` |
| `prod.vbeapi.online` (produção) | ePHEM de produção de Cabo Verde (**endereço e key a definir com o admin de lá**) | `GDS-` |

O `external_id` é o prefixo mais o id do evento, e o POST é idempotente por ele. Por isso duas
instâncias que publicam no mesmo ePHEM precisam de prefixos diferentes.

## Recursos do servidor

São duas JVMs e o Postgres no mesmo host. Limite a memória de cada JVM com `JVM_OPTIONS`
(o exemplo usa `-Xmx384m`) e confira a memória do droplet antes de subir.
