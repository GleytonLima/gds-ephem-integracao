# Collection Bruno — ePHEM API v2

Testes da API REST v2 do ePHEM contra o ambiente de dev de Cabo Verde (`cv.pheoc.com`).

| Pasta | O que faz | Precisa de |
|---|---|---|
| `01-descoberta` | descoberta, OpenAPI, key invalida (401) | nada |
| `02-lugares` | paises, estados, municipios | papel `ephem_signals_api_reader` |
| `03-signals-leitura` | listar, filtro invalido (422), 404 | papel `ephem_signals_api_reader` |
| `04-signals-escrita` | **cria e altera** signals `BRUNO-TEST-*`: idempotencia, PATCH com If-Match, 409, 422 | papel `ephem_signals_api_writer` + API source |

A API key e lida da variavel de ambiente `EPHEM_NOVO_API_KEY` (ou de um `.env` nesta pasta, ver `.env.example`).

## Rodar

Pelo app Bruno: abra esta pasta como collection e escolha o ambiente `cv-dev`.

Pelo CLI (sem instalar globalmente):

```powershell
cd docs/bruno/ephem-api-v2
npx -y @usebruno/cli@latest run 01-descoberta 02-lugares 03-signals-leitura --env cv-dev
```

Escrita (so em dev/treinamento):

```powershell
npx -y @usebruno/cli@latest run 04-signals-escrita --env cv-dev
```
