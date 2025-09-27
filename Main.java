public class Main {
    public static void main(String[] args) {
        mostrarElementosEstructurantes();
        if (args.length < 3) {
            System.out.println("Uso: java Main <imagen> <operacion> <ee> [numHilos]");
            System.out.println("Ejemplo: java Main imagen_rgb.png dilatacion Estructura1 4");
            return;
        }

        String imagePath = args[0];
        String operacion = args[1].toLowerCase();
        String eeNombre = args[2];
        int numHilos = args.length > 3 ? Integer.parseInt(args[3]) : 4;

        // Verificar memoria disponible
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
        System.out.println("Memoria máxima disponible: " + maxMemory + " MB");

        ElementoEstructurante ee;
        switch (eeNombre) {
            case "Estructura1": ee = ElementoEstructurante.Estructura1; break;
            case "Estructura2": ee = ElementoEstructurante.Estructura2; break;
            case "Estructura3": ee = ElementoEstructurante.Estructura3; break;
            case "Estructura4": ee = ElementoEstructurante.Estructura4; break;
            case "Estructura5": ee = ElementoEstructurante.Estructura5; break;
            default:
                System.out.println("Elemento estructurante no válido.");
                return;
        }

        System.out.println("Procesando con " + numHilos + " hilos...");

        // Cargar imagen una sola vez para obtener dimensiones
        int[][][] matriz = TrabajarMatrizRGB.ImagenToMatriz(imagePath);
        if (matriz == null) {
            System.out.println("Error al cargar la imagen.");
            return;
        }
        
        int alto = matriz.length;
        int ancho = matriz[0].length;
        System.out.println("Dimensiones de la imagen: " + alto + "x" + ancho);
        
        // Calcular memoria aproximada requerida por canal
        long memoriaRequerida = ((long) alto * ancho * 4 * 3) / (1024 * 1024); // 3 matrices por canal
        System.out.println("Memoria aproximada requerida por canal: " + memoriaRequerida + " MB");

        String salidaSecuencial = "imagen_" + operacion + "_secuencial.png";
        String salidaParalela = "imagen_" + operacion + "_paralela.png";

        // PROCESAMIENTO SECUENCIAL
        System.out.println("\n=== PROCESAMIENTO SECUENCIAL ===");
        long inicioSec = System.nanoTime();
        
        procesarImagenCompleta(imagePath, operacion, ee, salidaSecuencial, false, 0);
        
        long finSec = System.nanoTime();
        double tiempoSecuencial = (finSec - inicioSec) / 1_000_000_000.0;

        // Limpiar memoria antes del procesamiento paralelo
        matriz = null;
        System.gc();
        Thread.yield();

        // PROCESAMIENTO PARALELO
        System.out.println("\n=== PROCESAMIENTO PARALELO ===");
        long inicioPar = System.nanoTime();
        
        procesarImagenCompleta(imagePath, operacion, ee, salidaParalela, true, numHilos);
        
        long finPar = System.nanoTime();
        double tiempoParalelo = (finPar - inicioPar) / 1_000_000_000.0;

        // Mostrar resultados
        System.out.println("\n=== RESULTADOS ===");
        System.out.printf("Tiempo secuencial: %.4f segundos%n", tiempoSecuencial);
        System.out.printf("Tiempo paralelo: %.4f segundos%n", tiempoParalelo);
        if (tiempoParalelo > 0) {
            System.out.printf("Speedup: %.2fx%n", tiempoSecuencial / tiempoParalelo);
            System.out.printf("Eficiencia: %.2f%%%n", (tiempoSecuencial / tiempoParalelo) / numHilos * 100);
        }
        System.out.println("\nImágenes guardadas:");
        System.out.println("- " + salidaSecuencial);
        System.out.println("- " + salidaParalela);

        // Comparar imágenes si es necesario (opcional para imágenes muy grandes)
        System.out.println("\nNota: Para imágenes de 10000x10000, la comparación pixel a pixel");
        System.out.println("puede ser muy lenta. Las imágenes han sido guardadas para verificación manual.");

        // Mostrar uso de memoria final
        System.gc();
        long memoriaUsada = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.println("Memoria utilizada al final: " + memoriaUsada + " MB");
    }

    private static void procesarImagenCompleta(String imagePath, String operacion, 
                                             ElementoEstructurante ee, String salidaPath,
                                             boolean usarParalelo, int numHilos) {
        try {
            // Crear BufferedImage de salida
            java.awt.image.BufferedImage imagenOriginal = javax.imageio.ImageIO.read(new java.io.File(imagePath));
            int alto = imagenOriginal.getHeight();
            int ancho = imagenOriginal.getWidth();
            
            java.awt.image.BufferedImage imagenSalida = new java.awt.image.BufferedImage(
                ancho, alto, java.awt.image.BufferedImage.TYPE_INT_RGB);

            // Procesar cada canal por separado
            for (int canal = 0; canal < 3; canal++) {
                System.out.println("Procesando canal " + (canal + 1) + "/3 " + 
                                 (usarParalelo ? "(paralelo)" : "(secuencial)") + "...");
                
                // Extraer canal
                int[][] canalData = extraerCanal(imagenOriginal, canal);
                
                // Procesar canal
                int[][] canalProcesado;
                if (usarParalelo) {
                    if (operacion.equals("dilatacion")) {
                        canalProcesado = Paralelo.dilatacion(canalData, ee, numHilos);
                    } else {
                        canalProcesado = Paralelo.erosion(canalData, ee, numHilos);
                    }
                } else {
                    if (operacion.equals("dilatacion")) {
                        canalProcesado = Secuencial.dilatacion(canalData, ee);
                    } else {
                        canalProcesado = Secuencial.erosion(canalData, ee);
                    }
                }
                
                // Escribir canal procesado a la imagen de salida
                escribirCanal(imagenSalida, canalProcesado, canal);
                
                // Liberar memoria inmediatamente
                canalData = null;
                canalProcesado = null;
                System.gc();
                
                // Mostrar progreso de memoria
                Runtime runtime = Runtime.getRuntime();
                long memoriaUsada = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
                System.out.println("  Memoria utilizada: " + memoriaUsada + " MB");
            }
            
            // Guardar imagen final
            System.out.println("Guardando imagen final...");
            javax.imageio.ImageIO.write(imagenSalida, "png", new java.io.File(salidaPath));
            
            // Limpiar
            imagenOriginal = null;
            imagenSalida = null;
            System.gc();
            
        } catch (Exception e) {
            System.err.println("Error procesando imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int[][] extraerCanal(java.awt.image.BufferedImage imagen, int canal) {
        int alto = imagen.getHeight();
        int ancho = imagen.getWidth();
        int[][] canalData = new int[alto][ancho];
        
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int rgb = imagen.getRGB(x, y);
                switch (canal) {
                    case 0: canalData[y][x] = (rgb >> 16) & 0xFF; break; // R
                    case 1: canalData[y][x] = (rgb >> 8) & 0xFF; break;  // G
                    case 2: canalData[y][x] = rgb & 0xFF; break;         // B
                }
            }
            
            // Limpiar memoria cada 1000 filas
            if (y % 1000 == 0 && y > 0) {
                System.gc();
            }
        }
        
        return canalData;
    }

    private static void escribirCanal(java.awt.image.BufferedImage imagen, int[][] canalData, int canal) {
        int alto = imagen.getHeight();
        int ancho = imagen.getWidth();
        
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int rgb = imagen.getRGB(x, y);
                int valor = Math.max(0, Math.min(255, canalData[y][x]));
                
                switch (canal) {
                    case 0: // R
                        rgb = (valor << 16) | (rgb & 0x00FFFF);
                        break;
                    case 1: // G
                        rgb = (rgb & 0xFF00FF) | (valor << 8);
                        break;
                    case 2: // B
                        rgb = (rgb & 0xFFFF00) | valor;
                        break;
                }
                
                imagen.setRGB(x, y, rgb);
            }
            
            // Limpiar memoria cada 1000 filas
            if (y % 1000 == 0 && y > 0) {
                System.gc();
            }
        }
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