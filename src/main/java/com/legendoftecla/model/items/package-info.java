/**
 * Objetos recogibles, utilizables y equipables.
 *
 * <p>{@link com.legendoftecla.model.items.Objeto} define identidad, descripcion
 * y peso. {@link com.legendoftecla.model.items.Botiquin} recupera salud,
 * {@link com.legendoftecla.model.items.ToritoRojo} energia y
 * {@link com.legendoftecla.model.items.Binocular} amplia temporalmente la
 * vision. {@link com.legendoftecla.model.items.Arma} y
 * {@link com.legendoftecla.model.items.Armadura} alteran combate y estadisticas;
 * {@link com.legendoftecla.model.items.Explosivo} se consume a distancia.</p>
 *
 * <p>Los valores se validan al construir y modificar el objeto. Las reglas que
 * dependen del turno o de un objetivo concreto se coordinan desde el motor.</p>
 */
package com.legendoftecla.model.items;
