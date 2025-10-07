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
        
        // Limpiar memoria y esperar estabilización
        System.gc();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.gc();
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // Medir memoria baseline
        long memoriaLibreInicial = runtime.freeMemory();
        long memoriaTotalInicial = runtime.totalMemory();
        long memoriaUsadaInicial = memoriaTotalInicial - memoriaLibreInicial;
        
        long inicioSec = System.nanoTime();
        RecursosMedidos recursosSeq = procesarImagenCompletaConMedicion(imagePath, operacion, ee, salidaSecuencial, false, 0);
        long finSec = System.nanoTime();
        
        double tiempoSecuencial = (finSec - inicioSec) / 1_000_000_000.0;

        // Limpiar memoria antes del procesamiento paralelo
        matriz = null;
        System.gc();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.gc();
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        // PROCESAMIENTO PARALELO
        System.out.println("\n=== PROCESAMIENTO PARALELO ===");
        
        // Medir memoria baseline para paralelo
        long memoriaLibreInicialPar = runtime.freeMemory();
        long memoriaTotalInicialPar = runtime.totalMemory();
        long memoriaUsadaInicialPar = memoriaTotalInicialPar - memoriaLibreInicialPar;
        
        long inicioPar = System.nanoTime();
        RecursosMedidos recursosPar = procesarImagenCompletaConMedicion(imagePath, operacion, ee, salidaParalela, true, numHilos);
        long finPar = System.nanoTime();
        
        double tiempoParalelo = (finPar - inicioPar) / 1_000_000_000.0;

        // MOSTRAR RECURSOS UTILIZADOS DE FORMA CLARA Y DETALLADA
        System.out.println("\n=== RECURSOS UTILIZADOS DETALLADOS ===");
        System.out.println("PROCESAMIENTO SECUENCIAL:");
        System.out.println("  • Número de hilos: 1");
        System.out.printf("  • Memoria pico utilizada: %d MB%n", recursosSeq.memoriaPico / (1024 * 1024));
        System.out.printf("  • Memoria promedio utilizada: %d MB%n", recursosSeq.memoriaPromedio / (1024 * 1024));
        System.out.printf("  • Memoria final utilizada: %d MB%n", recursosSeq.memoriaFinal / (1024 * 1024));
        System.out.printf("  • Tiempo de ejecución: %.4f segundos%n", tiempoSecuencial);
        System.out.printf("  • Memoria heap total durante procesamiento: %d MB%n", recursosSeq.heapTotal / (1024 * 1024));
        
        System.out.println("\nPROCESAMIENTO PARALELO:");
        System.out.printf("  • Número de hilos: %d%n", numHilos);
        System.out.printf("  • Memoria pico utilizada: %d MB%n", recursosPar.memoriaPico / (1024 * 1024));
        System.out.printf("  • Memoria promedio utilizada: %d MB%n", recursosPar.memoriaPromedio / (1024 * 1024));
        System.out.printf("  • Memoria final utilizada: %d MB%n", recursosPar.memoriaFinal / (1024 * 1024));
        System.out.printf("  • Tiempo de ejecución: %.4f segundos%n", tiempoParalelo);
        System.out.printf("  • Memoria heap total durante procesamiento: %d MB%n", recursosPar.heapTotal / (1024 * 1024));
        
        // Comparación más detallada
        System.out.println("\n=== COMPARACIÓN DETALLADA DE RECURSOS ===");
        long diferenciaPico = (recursosPar.memoriaPico - recursosSeq.memoriaPico) / (1024 * 1024);
        long diferenciaPromedio = (recursosPar.memoriaPromedio - recursosSeq.memoriaPromedio) / (1024 * 1024);
        
        System.out.printf("  • Diferencia en memoria pico: %+d MB (%s usa más en pico)%n", 
            diferenciaPico, diferenciaPico > 0 ? "Paralelo" : "Secuencial");
        System.out.printf("  • Diferencia en memoria promedio: %+d MB (%s usa más en promedio)%n", 
            diferenciaPromedio, diferenciaPromedio > 0 ? "Paralelo" : "Secuencial");
        
        if (recursosSeq.memoriaPico > 0) {
            System.out.printf("  • Relación memoria pico paralelo/secuencial: %.2fx%n", 
                (double)recursosPar.memoriaPico / recursosSeq.memoriaPico);
        }
        if (recursosSeq.memoriaPromedio > 0) {
            System.out.printf("  • Relación memoria promedio paralelo/secuencial: %.2fx%n", 
                (double)recursosPar.memoriaPromedio / recursosSeq.memoriaPromedio);
        }

        // Mostrar resultados de rendimiento
        System.out.println("\n=== RESULTADOS DE RENDIMIENTO ===");
        System.out.printf("Tiempo secuencial: %.4f segundos%n", tiempoSecuencial);
        System.out.printf("Tiempo paralelo: %.4f segundos%n", tiempoParalelo);
        if (tiempoParalelo > 0) {
            System.out.printf("Speedup: %.2fx%n", tiempoSecuencial / tiempoParalelo);
            System.out.printf("Eficiencia: %.2f%%%n", (tiempoSecuencial / tiempoParalelo) / numHilos * 100);
        }
        System.out.println("\nImágenes guardadas:");
        System.out.println("- " + salidaSecuencial);
        System.out.println("- " + salidaParalela);

        // COMPARAR RESULTADOS
        System.out.println("\n=== COMPARACIÓN DE RESULTADOS ===");
        compararImagenes(salidaSecuencial, salidaParalela);

        // Mostrar uso de memoria final
        System.gc();
        long memoriaFinal = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.println("\n=== ESTADO FINAL DEL SISTEMA ===");
        System.out.println("Memoria utilizada al final: " + memoriaFinal + " MB");
        System.out.println("Memoria máxima disponible: " + maxMemory + " MB");
        System.out.printf("Porcentaje de memoria utilizada: %.2f%%%n", (memoriaFinal * 100.0) / maxMemory);
    }

    // Nueva clase para medir recursos de forma más precisa
    private static class RecursosMedidos {
        long memoriaPico = 0;
        long memoriaPromedio = 0;
        long memoriaFinal = 0;
        long heapTotal = 0;
        int mediciones = 0;
        long sumaMemoria = 0;
        
        void registrarMedicion(long memoriaUsada, long heapTotal) {
            this.memoriaPico = Math.max(this.memoriaPico, memoriaUsada);
            this.sumaMemoria += memoriaUsada;
            this.mediciones++;
            this.memoriaPromedio = this.sumaMemoria / this.mediciones;
            this.memoriaFinal = memoriaUsada;
            this.heapTotal = Math.max(this.heapTotal, heapTotal);
        }
    }

    private static RecursosMedidos procesarImagenCompletaConMedicion(String imagePath, String operacion, 
                                             ElementoEstructurante ee, String salidaPath,
                                             boolean usarParalelo, int numHilos) {
        RecursosMedidos recursos = new RecursosMedidos();
        Runtime runtime = Runtime.getRuntime();
        
        try {
            // Crear BufferedImage de salida
            java.awt.image.BufferedImage imagenOriginal = javax.imageio.ImageIO.read(new java.io.File(imagePath));
            int alto = imagenOriginal.getHeight();
            int ancho = imagenOriginal.getWidth();
            
            // Medición inicial
            long memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
            recursos.registrarMedicion(memoriaUsada, runtime.totalMemory());
            
            java.awt.image.BufferedImage imagenSalida = new java.awt.image.BufferedImage(
                ancho, alto, java.awt.image.BufferedImage.TYPE_INT_RGB);

            // Medición después de crear imágenes
            memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
            recursos.registrarMedicion(memoriaUsada, runtime.totalMemory());

            // Procesar cada canal por separado
            for (int canal = 0; canal < 3; canal++) {
                System.out.println("Procesando canal " + (canal + 1) + "/3 " + 
                                 (usarParalelo ? "(paralelo)" : "(secuencial)") + "...");
                
                // Extraer canal
                int[][] canalData = extraerCanal(imagenOriginal, canal);
                
                // Medición después de extraer canal
                memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
                recursos.registrarMedicion(memoriaUsada, runtime.totalMemory());
                
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
                
                // Medición después de procesar canal
                memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
                recursos.registrarMedicion(memoriaUsada, runtime.totalMemory());
                
                // Escribir canal procesado a la imagen de salida
                escribirCanal(imagenSalida, canalProcesado, canal);
                
                // Medición después de escribir canal
                memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
                recursos.registrarMedicion(memoriaUsada, runtime.totalMemory());
                
                // Mostrar progreso de memoria durante el procesamiento
                long memoriaUsadaMB = memoriaUsada / (1024 * 1024);
                long memoriaPicoMB = recursos.memoriaPico / (1024 * 1024);
                System.out.println("  Memoria actual: " + memoriaUsadaMB + " MB, Pico: " + memoriaPicoMB + " MB");
                
                // Liberar memoria inmediatamente pero NO hacer GC agresivo
                canalData = null;
                canalProcesado = null;
            }
            
            // Guardar imagen final
            System.out.println("Guardando imagen final...");
            javax.imageio.ImageIO.write(imagenSalida, "png", new java.io.File(salidaPath));
            
            // Medición final
            memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
            recursos.registrarMedicion(memoriaUsada, runtime.totalMemory());
            
            // Limpiar
            imagenOriginal = null;
            imagenSalida = null;
            
        } catch (Exception e) {
            System.err.println("Error procesando imagen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recursos;
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

    /**
     * Compara dos imágenes píxel por píxel para verificar si son idénticas
     * @param imagenPath1 Ruta de la primera imagen (secuencial)
     * @param imagenPath2 Ruta de la segunda imagen (paralela)
     */
    public static void compararImagenes(String imagenPath1, String imagenPath2) {
        try {
            System.out.println("Comparando imágenes...");
            long inicioComparacion = System.nanoTime();
            
            // Cargar ambas imágenes
            java.awt.image.BufferedImage imagen1 = javax.imageio.ImageIO.read(new java.io.File(imagenPath1));
            java.awt.image.BufferedImage imagen2 = javax.imageio.ImageIO.read(new java.io.File(imagenPath2));
            
            // Verificar dimensiones
            if (imagen1.getWidth() != imagen2.getWidth() || imagen1.getHeight() != imagen2.getHeight()) {
                System.out.println(" Las imágenes tienen dimensiones diferentes:");
                System.out.println("  Imagen 1: " + imagen1.getWidth() + "x" + imagen1.getHeight());
                System.out.println("  Imagen 2: " + imagen2.getWidth() + "x" + imagen2.getHeight());
                return;
            }
            
            int alto = imagen1.getHeight();
            int ancho = imagen1.getWidth();
            int pixelesDiferentes = 0;
            int pixelesTotales = alto * ancho;
            int primerasDiferencias = 0;
            final int MAX_DIFERENCIAS_MOSTRAR = 10;
            
            System.out.println("Dimensiones: " + alto + "x" + ancho + " (" + pixelesTotales + " píxeles)");
            
            // Variables para mostrar progreso
            int progresoAnterior = 0;
            
            // Comparar píxel por píxel
            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    int rgb1 = imagen1.getRGB(x, y);
                    int rgb2 = imagen2.getRGB(x, y);
                    
                    if (rgb1 != rgb2) {
                        pixelesDiferentes++;
                        
                        // Mostrar las primeras diferencias encontradas
                        if (primerasDiferencias < MAX_DIFERENCIAS_MOSTRAR) {
                            int r1 = (rgb1 >> 16) & 0xFF;
                            int g1 = (rgb1 >> 8) & 0xFF;
                            int b1 = rgb1 & 0xFF;
                            
                            int r2 = (rgb2 >> 16) & 0xFF;
                            int g2 = (rgb2 >> 8) & 0xFF;
                            int b2 = rgb2 & 0xFF;
                            
                            System.out.println("  Diferencia en píxel (" + x + "," + y + "):");
                            System.out.println("    Secuencial: RGB(" + r1 + "," + g1 + "," + b1 + ")");
                            System.out.println("    Paralela:   RGB(" + r2 + "," + g2 + "," + b2 + ")");
                            
                            primerasDiferencias++;
                        }
                    }
                }
                
                // Mostrar progreso cada 10% para imágenes grandes
                if (alto > 1000) {
                    int progresoActual = (y * 100) / alto;
                    if (progresoActual >= progresoAnterior + 10) {
                        System.out.println("  Progreso comparación: " + progresoActual + "% - " +
                                         "Diferencias hasta ahora: " + pixelesDiferentes);
                        progresoAnterior = progresoActual;
                    }
                }
            }
            
            long finComparacion = System.nanoTime();
            double tiempoComparacion = (finComparacion - inicioComparacion) / 1_000_000_000.0;
            
            // Mostrar resultados de la comparación
            System.out.println("\n=== RESULTADO DE LA COMPARACIÓN ===");
            System.out.printf("Tiempo de comparación: %.4f segundos%n", tiempoComparacion);
            System.out.println("Píxeles totales: " + pixelesTotales);
            System.out.println("Píxeles diferentes: " + pixelesDiferentes);
            
            if (pixelesDiferentes == 0) {
                System.out.println(" RESULTADOS IDÉNTICOS");
                System.out.println("Las imágenes secuencial y paralela son exactamente iguales.");
            } else {
                double porcentajeDiferencia = (pixelesDiferentes * 100.0) / pixelesTotales;
                System.out.println(" RESULTADOS DIFERENTES");
                System.out.printf("Porcentaje de píxeles diferentes: %.6f%%%n", porcentajeDiferencia);
                
                if (primerasDiferencias >= MAX_DIFERENCIAS_MOSTRAR) {
                    System.out.println("... y " + (pixelesDiferentes - MAX_DIFERENCIAS_MOSTRAR) + " diferencias más.");
                }
                
                // Determinar si las diferencias son significativas
                if (porcentajeDiferencia < 0.001) {
                    System.out.println("⚠️  Las diferencias son mínimas (< 0.001%), posiblemente debido a:");
                    System.out.println("   - Precisión de punto flotante");
                    System.out.println("   - Orden de operaciones en el procesamiento paralelo");
                    System.out.println("   - Condiciones de borde en la división por bloques");
                } else {
                    System.out.println("⚠️  Las diferencias son significativas. Verificar:");
                    System.out.println("   - Implementación del algoritmo paralelo");
                    System.out.println("   - Manejo de halos en los bloques");
                    System.out.println("   - Sincronización de hilos");
                }
            }
            
            // Liberar memoria
            imagen1 = null;
            imagen2 = null;
            System.gc();
            
        } catch (Exception e) {
            System.err.println(" Error al comparar imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}