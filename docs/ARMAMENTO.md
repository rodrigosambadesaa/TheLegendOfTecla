# Armamento

El arsenal distingue `MELE`, `ARROJADIZA`, `ARCO`, `BALLESTA` y `FUEGO`.
Cada familia define alcance y compatibilidad de proyectil:

| Familia | Recurso | Alcance base |
|---|---|---:|
| Espadas y cuchillos de melé | ninguno | 1 |
| Cuchillos arrojadizos | `CUCHILLO_ARROJADIZO` | 4 |
| Arcos | `FLECHA` | 6 |
| Ballestas | `VIROTE` | 7 |
| Armas de fuego | pistola, rifle, pesada, cohete o energía | 8 |

`Armeria` ofrece fábricas seguras para las variantes habituales. Las armas
históricas conservan su constructor y comportamiento para no romper escenarios.

Las granadas (`FRAGMENTACION`, `INCENDIARIA`, `ATURDIDORA`) son explosivos
arrojadizos accesibles a cualquier clase. Los explosivos de demolición continúan
siendo exclusivos del Zapador.

Competencias:

- Marine: melé, arrojadizas y fuego, incluidas armas pesadas.
- Francotirador: arrojadizas, arco, ballesta y fuego común de precisión.
- Zapador: melé, arrojadizas, fuego pesado, granadas y demolición.
- Aliados: todas las familias comunes, pero no munición pesada/cohetes ni demolición.
- Enemigos: carga finita específica de rol. Berserker usa espada; Medic pistola;
  Sniper rifle; Scout cuchillos; Pyro energía; Commander rifle; CommanderPrime y
  Heavy Floater usan armamento pesado, y PyroOverlord combina energía y granadas.

Al morir, el enemigo deja tanto el arma equipada como su reserva. El perfil del
personaje que la recoge determina si sabe equiparla; el botín nunca se clona.

Ejemplo JSON de arco:

```json
{
  "tipo": "arma",
  "nombre": "arco compuesto",
  "categoriaArma": "ARCO",
  "tipoMunicion": "FLECHA",
  "capacidadCargador": 1,
  "municionActual": 1,
  "valor": 14,
  "peso": 1.8,
  "fila": 1,
  "columna": 2
}
```
