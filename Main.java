public class Main {
    public static void main(String[] args) {
        mostrarElementosEstructurantes();
        if (args.length < 3) {
            System.out.println("Uso: java Main <imagen> <operacion> <ee>");
            System.out.println("Ejemplo: java Main imagen_rgb.png dilatacion Estructura1");
            return;
        }

        String imagePath = args[0];
        String operacion = args[1].toLowerCase();
        String eeNombre = args[2];

        int[][][] matriz = TrabajarMatrizRGB.ImagenToMatriz(imagePath);
        if (matriz == null) {
            System.out.println("Error al cargar la imagen.");
            return;
        }

        int[][][] canales = TrabajarMatrizRGB.separarCanales(matriz);

        ElementoEstructurante ee;
        switch (eeNombre) {
            case "Estructura1":
                ee = ElementoEstructurante.Estructura1;
                break;
            case "Estructura2":
                ee = ElementoEstructurante.Estructura2;
                break;
            case "Estructura3":
                ee = ElementoEstructurante.Estructura3;
                break;
            case "Estructura4":
                ee = ElementoEstructurante.Estructura4;
                break;
            case "Estructura5":
                ee = ElementoEstructurante.Estructura5;
                break;
            default:
                System.out.println("Elemento estructurante no válido.");
                return;
        }

        int[][] canalR, canalG, canalB;
        String salida;

        long inicio = System.nanoTime();

        if (operacion.equals("dilatacion")) {
            canalR = Secuencial.dilatacion(canales[0], ee);
            canalG = Secuencial.dilatacion(canales[1], ee);
            canalB = Secuencial.dilatacion(canales[2], ee);
            salida = "imagen_dilatada.png";
        } else if (operacion.equals("erosion")) {
            canalR = Secuencial.erosion(canales[0], ee);
            canalG = Secuencial.erosion(canales[1], ee);
            canalB = Secuencial.erosion(canales[2], ee);
            salida = "imagen_erosionada.png";
        } else {
            System.out.println("Operación no válida. Usa 'dilatacion' o 'erosion'.");
            return;
        }

        long fin = System.nanoTime(); 
        double tiempoSegundos = (fin - inicio) / 1_000_000_000.0;

        int[][][] matrizFinal = TrabajarMatrizRGB.combinarCanales(canalR, canalG, canalB);
        TrabajarMatrizRGB.MatrizToImagen(matrizFinal, salida);
        System.out.println("Imagen procesada y guardada como " + salida);
        System.out.printf("Tiempo de ejecucion secuencial: %.4f segundos%n", tiempoSegundos);
    }

    public static void mostrarElementosEstructurantes() {
        System.out.println("Elementos estructurantes disponibles:");
        mostrarEE("Estructura1", ElementoEstructurante.Estructura1);
        mostrarEE("Estructura2", ElementoEstructurante.Estructura2);
        mostrarEE("Estructura3", ElementoEstructurante.Estructura3);
        mostrarEE("Estructura4", ElementoEstructurante.Estructura4);
        mostrarEE("Estructura5", ElementoEstructurante.Estructura5);
    }

    public static void mostrarEE(String nombre, ElementoEstructurante ee) {
        System.out.println(nombre + ":");
        int[][] estructura = ee.getEstructura();
        for (int i = 0; i < estructura.length; i++) {
            for (int j = 0; j < estructura[0].length; j++) {
                System.out.print(estructura[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Ancla: (" + ee.getAnclaY() + ", " + ee.getAnclaX() + ")");
        System.out.println();
    }
}