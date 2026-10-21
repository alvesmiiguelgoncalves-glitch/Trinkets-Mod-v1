# Totem Slot (Fabric 1.21.1)

Um slot do **Trinkets** na parte das **pernas** (`legs/totem`) para o Totem da Imortalidade. O totem fica
**pendurado na cintura** e, se você ia morrer, ele é gasto e te salva com o mesmo efeito do totem vanilla na mão:
vida 1, efeitos removidos, Regeneração II (45 s), Absorção II (5 s), Resistência ao Fogo (40 s), animação e som do
totem, estatística de uso e gatilho de conquista.

- Totem **na mão** continua funcionando como no vanilla e tem prioridade (nunca gasta dois).
- Como no vanilla, o totem **não** salva de `/kill` nem de cair no vazio.
- O mod usa o evento `ALLOW_DEATH` da Fabric API; não há mixins.
- Precisa do Trinkets e da Fabric API (no cliente e no servidor).

## Totens de outros mods
O slot só aceita itens da tag `#totemslot:totems` (o totem vanilla já está nela). Para aceitar o totem de outro mod,
crie um datapack com `data/totemslot/tags/item/totems.json`:

```json
{
  "replace": false,
  "values": [
    { "id": "outromod:totem_especial", "required": false }
  ]
}
```
Esses totens são tratados como o totem vanilla (mesmos efeitos). Totens de outros mods com efeito próprio não
disparam a lógica própria deles a partir do slot.

## Posição do totem (cliente)
Arquivo `config/totemslot-client.json`, com **perfis independentes** (mexer em um nunca altera outro). Ajustável ao vivo:

```
/totemslot status                          (perfil ativo e o que foi detectado)
/totemslot show [perfil]
/totemslot set <perfil> <campo> <valor>    ex.: /totemslot set lantern rotX -10
/totemslot reset [perfil]                  (restaura os padrões do mod)
/totemslot reload
```

| Perfil | Quando vale |
|---|---|
| `normal` | nada de especial |
| `chest` | peitoral (armadura) |
| `lantern` / `chest_lantern` | lanterna num slot do Trinkets (de outro mod). **Só vale sem mochila.** Sem / com peitoral |
| `backpack` / `chest_backpack` | mochila do Backpacked, sem / com peitoral |
| `elytra` / `elytra_chest` | elytra equipada, sem mochila |
| `elytra_backpack` / `elytra_chest_backpack` | elytra + mochila |
| `flying` / `flying_backpack` | voando com elytra, asas abertas |
| `gliding` / `gliding_backpack` | voando com elytra, asas fechadas |

Prioridade: voando > com mochila > com lanterna (sem mochila) > elytra > peitoral > normal.
A lanterna é detectada em qualquer slot do Trinkets (item `LanternBlock` ou com "lantern" no id).

Campos: `x`, `y`, `z` (ponto onde o totem está preso, no referencial do torso: `y` para baixo a partir do pescoço,
0.70 = cintura; `z` negativo = frente do corpo), `rotX`, `rotY`, `rotZ` (graus; `rotY` 90 = de lado), `scale`,
`swing` (balanço ao andar), `idle` (balanço parado), `visible` (0/1), `mirror` (0/1, troca de lado) e **`leg`**.

**`leg` (0/1):** com `leg` ligado o totem fica preso à **perna** (a direita se `x` < 0, senão a esquerda) e acompanha o
movimento dela, em vez de a perna passar por cima. Use quando o totem está na frente do corpo. Já vem ligado nos perfis
de lanterna. Cada vez que a perna balança, o totem recebe uma contra-rotação parcial para continuar "pendurado".

Os perfis `normal`, `chest`, `lantern`, `backpack`, `chest_backpack` e `elytra_backpack` vêm com os valores que o autor
ajustou; os demais são cópias deles (veja o comentário em `TotemConfig.java`) até você ajustar.
Um arquivo antigo (sem perfis) é copiado para `totemslot-client.old.json`.

## Compilar
Java 21. Na raiz do repositório: `./gradlew :totemslot:build`; o jar sai em `totemslot/build/libs/totemslot-1.1.0.jar` (o `-sources` é só o código-fonte).
O workflow da raiz compila todos os mods no GitHub Actions.

## Licença
GPL-3.0-only (veja `LICENSE`).
