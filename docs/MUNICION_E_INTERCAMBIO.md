# Municion e intercambio

Las armas historicas creadas sin datos de cargador conservan municion infinita por
compatibilidad. Los escenarios nuevos pueden declarar armas finitas con
`tipoMunicion`, `capacidadCargador` y `municionActual`. Los paquetes tienen peso y
una cantidad que se consume sin duplicarse.

Comandos:

- `recargar` o `recargar <arma>`
- `estado arma`
- `dar <objeto> <aliado>`
- `pedir <objeto> <aliado>`
- `intercambiar <objeto1> <objeto2> <aliado>`

Los intercambios requieren distancia Manhattan maxima 1, personajes vivos y
ausencia de combate visible. Respetan peso, huecos, restricciones de clase y no
permiten transferir equipo que no este guardado en la mochila. La cooperacion
automatica de aliados puede desactivarse mediante
`MotorPartida.setCooperacionInventarioActiva(false)`.

Ejemplo JSON:

```json
{
  "tipo": "arma",
  "nombre": "rifle",
  "descripcion": "Rifle tactico",
  "peso": 3.5,
  "valor": 14,
  "tipoMunicion": "RIFLE",
  "capacidadCargador": 8,
  "municionActual": 4,
  "fila": 2,
  "columna": 3
}
```

```json
{
  "tipo": "municion",
  "nombre": "cargador rifle",
  "descripcion": "Reserva",
  "peso": 0.8,
  "tipoMunicion": "RIFLE",
  "cantidad": 8,
  "fila": 2,
  "columna": 4
}
```
