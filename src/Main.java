import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// CLASE MODELO: SEDE (NODO)
class Sede {
    private final int id;
    private final String nombre;

    public Sede(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre + " [Sede " + id + "]";
    }
}

// CLASE MODELO: CONEXIÓN VIAL (ARISTA)
class Conexion {
    private final int destinoId;
    private final int distanciaKm;

    public Conexion(int destinoId, int distanciaKm) {
        this.destinoId = destinoId;
        this.distanciaKm = distanciaKm;
    }

    public int getDestinoId() {
        return destinoId;
    }

    public int getDistanciaKm() {
        return distanciaKm;
    }
}

// ESTRUCTURA DE DATOS Y CORE ALGORÍTMICO (GRAFO + DIJKSTRA)
class GrafoLogistico {
    private final Map<Integer, Sede> sedes = new HashMap<>();
    private final Map<Integer, List<Conexion>> adyacencia = new HashMap<>();

    public void agregarSede(int id, String nombre) {
        Sede sede = new Sede(id, nombre);
        sedes.put(id, sede);
        adyacencia.putIfAbsent(id, new ArrayList<>());
    }

    public void agregarRutaBidireccional(int origenId, int destinoId, int distanciaKm) {
        adyacencia.get(origenId).add(new Conexion(destinoId, distanciaKm));
        adyacencia.get(destinoId).add(new Conexion(origenId, distanciaKm));
    }

    public Sede getSede(int id) {
        return sedes.get(id);
    }

// Clase Auxiliar para el estado de busqueda en Dijkstra
    private record NodoDijkstra(int idSede, int distanciaAcumulada) {}

// Clase Resultado con el reporte de la busqueda
    public static class ResultadoRuta {
        private final List<Sede> camino;
        private final List<Integer> distanciasTramos;
        private final int distanciaTotal;

        public ResultadoRuta(List<Sede> camino, List<Integer> distanciasTramos, int distanciaTotal) {
            this.camino = camino;
            this.distanciasTramos = distanciasTramos;
            this.distanciaTotal = distanciaTotal;
        }

        public List<Sede> getCamino() { return camino; }
        public List<Integer> getDistanciasTramos() { return distanciasTramos; }
        public int getDistanciaTotal() { return distanciaTotal; }
    }
// Implementacion del algoritmo de Dijkstra para encontrar la ruta minima.

    public ResultadoRuta calcularRutaOptima(int origenId, int destinoId) {
        Map<Integer, Integer> distancias = new HashMap<>();
        Map<Integer, Integer> predecesores = new HashMap<>();
        PriorityQueue<NodoDijkstra> colaPrioridad = new PriorityQueue<>(Comparator.comparingInt(NodoDijkstra::distanciaAcumulada));

// Inicializacion
        for (int id : sedes.keySet()) {
            distancias.put(id, Integer.MAX_VALUE);
        }
        distancias.put(origenId, 0);
        colaPrioridad.add(new NodoDijkstra(origenId, 0));

        while (!colaPrioridad.isEmpty()) {
            NodoDijkstra actual = colaPrioridad.poll();
            int u = actual.idSede();

            if (u == destinoId) break; // Se alcanzó el destino

            if (actual.distanciaAcumulada() > distancias.get(u)) continue;

            for (Conexion conexion : adyacencia.get(u)) {
                int v = conexion.getDestinoId();
                int peso = conexion.getDistanciaKm();

                if (distancias.get(u) + peso < distancias.get(v)) {
                    distancias.put(v, distancias.get(u) + peso);
                    predecesores.put(v, u);
                    colaPrioridad.add(new NodoDijkstra(v, distancias.get(v)));
                }
            }
        }

// Reconstruccion de la ruta hacia atras
        List<Sede> camino = new ArrayList<>();
        Integer paso = destinoId;

        if (distancias.get(destinoId) == Integer.MAX_VALUE) {
            return new ResultadoRuta(Collections.emptyList(), Collections.emptyList(), -1); // Sin ruta
        }

        while (paso != null) {
            camino.add(sedes.get(paso));
            paso = predecesores.get(paso);
        }
        Collections.reverse(camino);

// Obtenemos las distancias individuales de cada tramo en la ruta optima
        List<Integer> tramos = new ArrayList<>();
        for (int i = 0; i < camino.size() - 1; i++) {
            int actualId = camino.get(i).getId();
            int siguienteId = camino.get(i + 1).getId();
            for (Conexion con : adyacencia.get(actualId)) {
                if (con.getDestinoId() == siguienteId) {
                    tramos.add(con.getDistanciaKm());
                    break;
                }
            }
        }

        return new ResultadoRuta(camino, tramos, distancias.get(destinoId));
    }
}

// CLASE PRINCIPAL SALIDA CLI

public class Main {

    public static void main(String[] args) {
        // Inicializar la red logística
        GrafoLogistico redLogistica = new GrafoLogistico();

        // Cargar Centros Logísticos
        redLogistica.agregarSede(0, "Quito");
        redLogistica.agregarSede(1, "Manta");
        redLogistica.agregarSede(2, "Guayaquil");
        redLogistica.agregarSede(3, "Ambato");
        redLogistica.agregarSede(4, "Cuenca");

        // Cargar Red Vial (Distancias)
        redLogistica.agregarRutaBidireccional(0, 3, 150); // Quito ↔ Ambato
        redLogistica.agregarRutaBidireccional(0, 2, 420); // Quito ↔ Guayaquil
        redLogistica.agregarRutaBidireccional(3, 4, 220); // Ambato ↔ Cuenca
        redLogistica.agregarRutaBidireccional(2, 4, 195); // Guayaquil ↔ Cuenca
        redLogistica.agregarRutaBidireccional(2, 1, 190); // Guayaquil ↔ Manta
        redLogistica.agregarRutaBidireccional(3, 1, 310); // Ambato ↔ Manta

        // Definir consulta: Quito (0) a Cuenca (4)
        int origenId = 0;
        int destinoId = 4;

        GrafoLogistico.ResultadoRuta resultado = redLogistica.calcularRutaOptima(origenId, destinoId);

        // Generar salida formateada en consola
        imprimirReporte(redLogistica.getSede(origenId), redLogistica.getSede(destinoId), resultado);
    }

    private static void imprimirReporte(Sede origen, Sede destino, GrafoLogistico.ResultadoRuta resultado) {
        System.out.println("===============================================================================");
        System.out.println("LOGIPACK ECUADOR - SISTEMA DE OPTIMIZACIÓN");
        System.out.println("Estudiante: ANDY BURGA");
        System.out.println("===============================================================================");

        System.out.println("[Ruta seleccionada]:  " + origen.getNombre().toUpperCase() + " (" + origen.getId() +
                ") ──> " + destino.getNombre().toUpperCase() + " (" + destino.getId() + ")");

        // Formatear gráfica de la ruta óptima
        System.out.println("[Grafica de la ruta óptima]:");
        StringBuilder grafica = new StringBuilder();
        List<Sede> camino = resultado.getCamino();
        List<Integer> tramos = resultado.getDistanciasTramos();

        for (int i = 0; i < camino.size(); i++) {
            Sede s = camino.get(i);
            grafica.append("[").append(s.getId()).append("] ").append(s.getNombre().toUpperCase());
            if (i < tramos.size()) {
                grafica.append(" ──(").append(tramos.get(i)).append(" km)──> ");
            }
        }
        System.out.println(grafica);

        // Formatear detalle del despacho
        System.out.println("\n[Detalle del Despacho]:");
        System.out.printf("• Origen:          %-20s%n", origen);
        System.out.printf("• Destino:         %-20s%n", destino);

        StringBuilder secuencia = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            secuencia.append(camino.get(i).getNombre());
            if (i < camino.size() - 1) {
                secuencia.append(" -> ");
            }
        }
        System.out.println("• Secuencia Óptima: " + secuencia);
        System.out.println("• Distancia Total: " + resultado.getDistanciaTotal() + " km");
    }
}