# Trinkets Mods (Fabric 1.21.1)

Repositório com mods **independentes**, cada um com o próprio jar. Todos precisam do **Trinkets** e da **Fabric API**.

| Pasta | Mod | O que faz |
|---|---|---|
| [`backshield/`](backshield) | Back Shield | Escudo nas costas em um slot do Trinkets, 16 perfis de posição, tecla G/R, integração com Backpacked, BackSlot e elytra |
| [`totemslot/`](totemslot) | Totem Slot | Totem da imortalidade pendurado na cintura (slot nas pernas) que salva de morrer |
| [`backtoolslot/`](backtoolslot) | Back Tool Slot | Slot do Trinkets que carrega uma ferramenta (picareta, machado, pá...) nas costas, com perfis para peitoral, elytra e mochila |

Cada pasta tem o próprio README com instruções e opções.

## Compilar
Java 21. Na raiz:

```
./gradlew build          # todos os mods
./gradlew :backshield:build
./gradlew :totemslot:build
./gradlew :backtoolslot:build
```
Os jars saem em `<mod>/build/libs/<mod>-<versao>.jar`. Use o jar **sem** `-sources` no nome. O workflow
`.github/workflows/build.yml` compila tudo no GitHub Actions e publica os jars no artefato `mods-jar`.

## Adicionar outro mod
Crie a pasta, um `build.gradle` (copie o de `totemslot/` e ajuste `version`, `group` e `archivesName`) e inclua a
pasta em `settings.gradle`. Cada mod precisa de um `id` próprio no `fabric.mod.json`.

## Licença
GPL-3.0-only (veja `LICENSE` na raiz e em cada pasta).
