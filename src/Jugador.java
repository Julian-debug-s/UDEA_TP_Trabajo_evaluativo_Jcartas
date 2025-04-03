import java.util.Random;

import javax.swing.JPanel;

public class Jugador {

    private int TOTAL_CARTAS = 10;
    private int MARGEN = 10;
    private int DISTANCIA = 40;

    private Carta[] cartas = new Carta[TOTAL_CARTAS];
    private Random r = new Random(); // la suerte del jugador

    public void repartir() {
        for (int i = 0; i < TOTAL_CARTAS; i++) {
            cartas[i] = new Carta(r);
        }
    }

    public void mostrar(JPanel pnl) {
        pnl.removeAll();
        int posicion = MARGEN + (TOTAL_CARTAS - 1) * DISTANCIA;
        for (Carta carta : cartas) {
            carta.mostrar(pnl, posicion, MARGEN);
            posicion -= DISTANCIA;
        }
        pnl.repaint();
    }

    public String getGrupos() {
        String mensaje = "No se encontraron figuras";
        int[] contadores = new int[NombreCarta.values().length];
        for (Carta c : cartas) {
            contadores[c.getNombre().ordinal()]++;
        }

        boolean hayGrupos = false;
        for (int contador : contadores) {
            if (contador > 1) {
                hayGrupos = true;
                break;
            }
        }

        if (hayGrupos) {
            mensaje = "Se encontraron los siguientes grupos:\n";
            int fila = 0;
            for (int contador : contadores) {
                if (contador > 1) {
                    mensaje += Grupo.values()[contador] + " de " + NombreCarta.values()[fila] + "\n";
                }
                fila++;
            }
        }

        // SI HAY GRUPOS ESCALONADOS
        int n_filas = Pinta.values().length;
        int n_columnas = NombreCarta.values().length;
        int[][] matriz = new int[n_filas][n_columnas];

        // LLENO LA MATRIZ
        for (Carta carta : cartas) {
            matriz[carta.getPinta().ordinal()][carta.getNombre().ordinal()]++;
        }

        // Recorre las filas
        for (int i = 0; i < n_filas; i++) {
            int cartasJuntas = 0;
            for (int j = 0; j < n_columnas; j++) {
                if (matriz[i][j] > 0) {
                    cartasJuntas++;
                } else if (cartasJuntas > 0) {
                    if (cartasJuntas > 1) {
                        mensaje += Grupo.values()[cartasJuntas] + " escalonado de " + Pinta.values()[i] + " desde " + NombreCarta.values()[j - cartasJuntas] + " hasta " + NombreCarta.values()[j - 1] + "\n";
                    }
                    cartasJuntas = 0;
                }
            }
            if (cartasJuntas > 1) {
                mensaje += Grupo.values()[cartasJuntas] + " escalonado de " + Pinta.values()[i] + " desde " + NombreCarta.values()[n_columnas - cartasJuntas] + " hasta " + NombreCarta.values()[n_columnas - 1] + "\n";
            }
        }

        // Calcular el puntaje de las cartas que no están en los grupos
        int puntaje = calcularPuntajeCartasNoGrupo();

        // Agregar el puntaje al mensaje
        mensaje += "\nPuntaje de cartas no agrupadas: " + puntaje;

        return mensaje;
    }

    // Método para calcular el puntaje de las cartas que no están en los grupos
    private int calcularPuntajeCartasNoGrupo() {
        int puntaje = 0;
        // Crear una lista de cartas no agrupadas
        Carta[] cartasNoGrupo = obtenerCartasNoGrupo();

        // Calcular el puntaje
        for (Carta carta : cartasNoGrupo) {
            if (carta.getNombre() == NombreCarta.ACE || carta.getNombre() == NombreCarta.JACK ||
                carta.getNombre() == NombreCarta.QUEEN || carta.getNombre() == NombreCarta.KING) {
                puntaje += 10; // Ace, Jack, Queen, King valen 10
            } else {
                puntaje += carta.getNombre().ordinal() + 1; // Las cartas numéricas valen su número
            }
        }

        return puntaje;
    }

    // Método para obtener las cartas que no forman parte de un grupo
    private Carta[] obtenerCartasNoGrupo() {
        int contador = 0;
        Carta[] cartasNoGrupo = new Carta[TOTAL_CARTAS];
        
        // Para identificar cartas en grupos por nombre
        int[] contadoresNombre = new int[NombreCarta.values().length];
        for (Carta carta : cartas) {
            contadoresNombre[carta.getNombre().ordinal()]++;
        }
        
        // Para identificar cartas en grupos escalonados
        boolean[][] enGrupoEscalonado = new boolean[Pinta.values().length][NombreCarta.values().length];
        
        // Llenar la matriz para identificar grupos escalonados
        int[][] matriz = new int[Pinta.values().length][NombreCarta.values().length];
        for (Carta carta : cartas) {
            matriz[carta.getPinta().ordinal()][carta.getNombre().ordinal()]++;
        }
        
        // Marcar las cartas que forman parte de grupos escalonados
        for (int i = 0; i < Pinta.values().length; i++) {
            int cartasJuntas = 0;
            int inicioGrupo = -1;
            
            for (int j = 0; j < NombreCarta.values().length; j++) {
                if (matriz[i][j] > 0) {
                    if (cartasJuntas == 0) {
                        inicioGrupo = j;
                    }
                    cartasJuntas++;
                } else if (cartasJuntas > 0) {
                    if (cartasJuntas > 1) {
                        // Marcar todas las cartas del grupo escalonado
                        for (int k = inicioGrupo; k < inicioGrupo + cartasJuntas; k++) {
                            enGrupoEscalonado[i][k] = true;
                        }
                    }
                    cartasJuntas = 0;
                }
            }
            
            // Verificar si hay un grupo al final de la columna
            if (cartasJuntas > 1) {
                // Marcar todas las cartas del grupo escalonado
                for (int k = inicioGrupo; k < inicioGrupo + cartasJuntas; k++) {
                    enGrupoEscalonado[i][k] = true;
                }
            }
        }
        
        // Agregamos las cartas que no forman parte de ningún grupo (ni por nombre ni escalonado)
        for (Carta carta : cartas) {
            int nombreOrdinal = carta.getNombre().ordinal();
            int pintaOrdinal = carta.getPinta().ordinal();
            
            // Solo agregamos cartas que no forman parte de grupos por nombre ni escalonados
            if (contadoresNombre[nombreOrdinal] == 1 && !enGrupoEscalonado[pintaOrdinal][nombreOrdinal]) {
                cartasNoGrupo[contador++] = carta;
            }
        }

        // Reducimos el tamaño del array
        Carta[] cartasFinales = new Carta[contador];
        System.arraycopy(cartasNoGrupo, 0, cartasFinales, 0, contador);
        return cartasFinales;
    }
}