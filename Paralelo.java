import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.List;
import java.util.ArrayList;

public class Paralelo {

    public static int[][] dilatacion(int[][] canal, ElementoEstructurante ee, int numHilos) {
        return procesarParalelo(canal, ee, numHilos, true);
    }

    public static int[][] erosion(int[][] canal, ElementoEstructurante ee, int numHilos) {
        return procesarParalelo(canal, ee, numHilos, false);
    }

    private static int[][] procesarParalelo(int[][] canal, ElementoEstructurante ee, int numHilos, boolean esDilatacion) {
        int alto = canal.length;
        int ancho = canal[0].length;
        int[][] resultado = new int[alto][ancho];
        
        // Calcular radio del elemento estructurante
        int radio = Math.max(ee.getEstructura().length, ee.getEstructura()[0].length) / 2;
        
        // Calcular tamaño mínimo de bloque para que sea eficiente
        int minTamBloque = 100; // Mínimo 100x100 píxeles por bloque
        int maxBloquesEficientes = Math.max(1, (alto * ancho) / (minTamBloque * minTamBloque));
        
        // Ajustar número de hilos a lo realmente necesario
        int hilosOptimos = Math.min(numHilos, maxBloquesEficientes);
        
        // Si la imagen es muy pequeña, usar procesamiento secuencial
        if (alto < minTamBloque && ancho < minTamBloque) {
            System.out.println("=== IMAGEN PEQUEÑA - PROCESAMIENTO SECUENCIAL ===");
            System.out.println("Dimensiones " + alto + "x" + ancho + " < " + minTamBloque + "x" + minTamBloque);
            System.out.println("Usando procesamiento secuencial en lugar de paralelo");
            
            if (esDilatacion) {
                return Secuencial.dilatacion(canal, ee);
            } else {
                return Secuencial.erosion(canal, ee);
            }
        }
        
        // Calcular dimensiones de bloques optimizadas
        int tempBloquesY = (int) Math.sqrt(hilosOptimos);
        int tempBloquesX = hilosOptimos / tempBloquesY;
        
        // Ajustar si los bloques resultarían muy pequeños
        while (tempBloquesY > 1 && (alto / tempBloquesY) < minTamBloque) {
            tempBloquesY--;
            tempBloquesX = hilosOptimos / tempBloquesY;
        }
        while (tempBloquesX > 1 && (ancho / tempBloquesX) < minTamBloque) {
            tempBloquesX--;
            hilosOptimos = tempBloquesY * tempBloquesX;
        }
        
        // Declarar variables finales para usar en lambdas
        final int bloquesY = tempBloquesY;
        final int bloquesX = tempBloquesX;
        final int totalBloques = bloquesY * bloquesX;
        final int tamBloque = alto / bloquesY;
        final int tamBloqueX = ancho / bloquesX;
        
        System.out.println("=== INFORMACIÓN DE HILOS ===");
        System.out.println("Hilos solicitados: " + numHilos);
        System.out.println("Hilos óptimos calculados: " + hilosOptimos);
        System.out.println("Bloques a procesar: " + totalBloques + " (" + bloquesY + "x" + bloquesX + ")");
        System.out.println("Tamaño de bloque: " + tamBloque + "x" + tamBloqueX);
        System.out.println("Radio del halo: " + radio);
        
        if (hilosOptimos < numHilos) {
            System.out.println("NOTA: Reduciendo hilos de " + numHilos + " a " + hilosOptimos + 
                             " para evitar bloques muy pequeños");
        }
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(hilosOptimos);
        List<Future<BloqueResultado>> futures = new ArrayList<>();
        
        // Crear tareas para cada bloque
        for (int by = 0; by < bloquesY; by++) {
            for (int bx = 0; bx < bloquesX; bx++) {
                final int bloqueY = by;
                final int bloqueX = bx;
                final int bloqueId = by * bloquesX + bx + 1;
                
                Future<BloqueResultado> future = executor.submit(() -> {
                    // Mostrar información del hilo actual
                    String nombreHilo = Thread.currentThread().getName();
                    System.out.println("Hilo " + nombreHilo + " procesando bloque " + bloqueId + "/" + totalBloques);
                    
                    // Calcular región del bloque sin halo
                    int inicioY = bloqueY * tamBloque;
                    int finY = (bloqueY == bloquesY - 1) ? alto : (bloqueY + 1) * tamBloque;
                    int inicioX = bloqueX * tamBloqueX;
                    int finX = (bloqueX == bloquesX - 1) ? ancho : (bloqueX + 1) * tamBloqueX;
                    
                    // Calcular región con halo
                    int inicioYHalo = Math.max(0, inicioY - radio);
                    int finYHalo = Math.min(alto, finY + radio);
                    int inicioXHalo = Math.max(0, inicioX - radio);
                    int finXHalo = Math.min(ancho, finX + radio);
                    
                    // Extraer región con halo
                    int altoHalo = finYHalo - inicioYHalo;
                    int anchoHalo = finXHalo - inicioXHalo;
                    int[][] regionHalo = new int[altoHalo][anchoHalo];
                    
                    for (int y = 0; y < altoHalo; y++) {
                        for (int x = 0; x < anchoHalo; x++) {
                            regionHalo[y][x] = canal[inicioYHalo + y][inicioXHalo + x];
                        }
                    }
                    
                    // Procesar región con halo
                    int[][] regionProcesada;
                    if (esDilatacion) {
                        regionProcesada = Secuencial.dilatacion(regionHalo, ee);
                    } else {
                        regionProcesada = Secuencial.erosion(regionHalo, ee);
                    }
                    
                    // Extraer solo la parte original (sin halo)
                    int offsetY = inicioY - inicioYHalo;
                    int offsetX = inicioX - inicioXHalo;
                    int altoOriginal = finY - inicioY;
                    int anchoOriginal = finX - inicioX;
                    
                    int[][] bloqueResultado = new int[altoOriginal][anchoOriginal];
                    for (int y = 0; y < altoOriginal; y++) {
                        for (int x = 0; x < anchoOriginal; x++) {
                            bloqueResultado[y][x] = regionProcesada[offsetY + y][offsetX + x];
                        }
                    }
                    
                    System.out.println("Hilo " + nombreHilo + " terminó bloque " + bloqueId + "/" + totalBloques);
                    return new BloqueResultado(bloqueResultado, inicioY, inicioX);
                });
                
                futures.add(future);
            }
        }
             
        // Ensamblar resultados
        try {
            int bloquesCompletados = 0;
            for (Future<BloqueResultado> future : futures) {
                BloqueResultado br = future.get();
                int[][] bloque = br.datos;
                int inicioY = br.inicioY;
                int inicioX = br.inicioX;
                
                for (int y = 0; y < bloque.length; y++) {
                    for (int x = 0; x < bloque[0].length; x++) {
                        resultado[inicioY + y][inicioX + x] = bloque[y][x];
                    }
                }
                
                bloquesCompletados++;
                double progreso = (bloquesCompletados * 100.0) / totalBloques;
                System.out.println("Progreso: " + bloquesCompletados + "/" + totalBloques + 
                                 " (" + String.format("%.1f", progreso) + "%)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
        
        // Estado final
        System.out.println("=== PROCESAMIENTO COMPLETADO ===");
        System.out.println("Bloques procesados: " + totalBloques);
        System.out.println("Hilos realmente utilizados: " + hilosOptimos + "/" + numHilos);
        
        return resultado;
    }
    
    private static class BloqueResultado {
        int[][] datos;
        int inicioY;
        int inicioX;
        
        BloqueResultado(int[][] datos, int inicioY, int inicioX) {
            this.datos = datos;
            this.inicioY = inicioY;
            this.inicioX = inicioX;
        }
    }
}
