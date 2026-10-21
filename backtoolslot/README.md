# Back Tool Slot (Fabric 1.21.1)

Um slot do **Trinkets** (`chest/back_tool`) que carrega **uma ferramenta nas costas**: picareta, machado, pá, enxada,
espada, tesoura ou vara de pesca. Funciona **ao lado** do BackSlot (a espada na cintura e o slot de costas dele
continuam como são) e não depende dele.

A posição da ferramenta muda sozinha conforme o que você está usando: peitoral, elytra (em pé, voando com as asas
abertas ou planando com as asas fechadas) e **mochila do Backpacked**. Com mochila, a ferramenta vai para o lado oposto
ao do escudo do Back Shield (x negativo), pendurada na lateral da mochila, para os dois não se sobreporem.

Precisa do Trinkets e da Fabric API. O Backpacked e o Back Shield são opcionais.

## Quais itens entram
A tag `#backtoolslot:tools` (espadas, picaretas, machados, pás, enxadas, tesoura e vara de pesca). Qualquer item que
estenda `ToolItem` também é aceito no slot, mas só é **desenhado** se estiver na tag. Para adicionar itens (de outros
mods, por exemplo), use um datapack com `data/backtoolslot/tags/item/tools.json`:

```json
{
  "replace": false,
  "values": [ { "id": "outromod:martelo", "required": false } ]
}
```

## Posição (cliente)
Arquivo `config/backtoolslot-client.json`, ajustável ao vivo:

```
/backtool status                          (perfil ativo e o que foi detectado)
/backtool show [perfil]
/backtool set <perfil> <campo> <valor>    ex.: /backtool set normal rotZ 45
/backtool reset [perfil]                  (restaura os padrões do mod)
/backtool reload
```
Perfis: `normal`, `chest`, `backpack`, `chest_backpack`, `elytra`, `elytra_chest`, `elytra_backpack`,
`elytra_chest_backpack`, `flying`, `flying_backpack`, `gliding`, `gliding_backpack`. Cada perfil tem `x`, `y`, `z`,
`rotX`, `rotY`, `rotZ`, `scale`, `visible` (0/1) e `mirror` (0/1). Os valores iniciais são um ponto de partida: a
ferramenta é um sprite diagonal, então a rotação (`rotZ`) e o `mirror` costumam precisar de ajuste.

## Compilar
Java 21. Na raiz do repositório: `./gradlew :backtoolslot:build`; o jar sai em
`backtoolslot/build/libs/backtoolslot-1.0.0.jar` (use o jar sem `-sources`).

## Licença
GPL-3.0-only (veja `LICENSE`).
