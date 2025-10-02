import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Random;

public class GenerarImagen {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java GenerarImagen <ancho> <alto>");
            return;
        }
        int ancho = 0;
        int alto = 0;
        try {
            ancho = Integer.parseInt(args[0]);
            alto = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Los argumentos deben ser números enteros.");
            return;
        }
        if (ancho <= 0 || alto <= 0) {
            System.out.println("El ancho y el alto deben ser mayores que cero.");
            return;
        }
        
        // Usar el nuevo método que incluye la línea blanca
        int[][][] imagen = crearImagenConLineaBlanca(alto, ancho);
        guardarImagen(imagen, "imagen_rgb.png");
        System.out.println("Imagen generada con línea blanca y guardada como imagen_rgb.png");
    }

    // Nuevo método que añade una línea blanca horizontal de 1 píxel en la mitad
    public static int[][][] crearImagenConLineaBlanca(int filas, int columnas) {
        int[][][] imagen = new int[filas][columnas][3];
        Random random = new Random();
        
        // Calcular la fila donde estará la línea blanca (exactamente en la mitad)
        int filaLinea = filas / 2;
        
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (i == filaLinea) {
                    // Línea blanca horizontal de 1 píxel en la mitad
                    imagen[i][j][0] = 255; // R = 255 (blanco)
                    imagen[i][j][1] = 255; // G = 255 (blanco)
                    imagen[i][j][2] = 255; // B = 255 (blanco)
                } else {
                    // Colores aleatorios para el resto
                    imagen[i][j][0] = random.nextInt(256); // R
                    imagen[i][j][1] = random.nextInt(256); // G
                    imagen[i][j][2] = random.nextInt(256); // B
                }
            }
        }
        return imagen;
    }

    // Método original (mantener para compatibilidad)
    public static int[][][] crearImagenRGB(int filas, int columnas) {
        int[][][] imagen = new int[filas][columnas][3];
        Random random = new Random();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                imagen[i][j][0] = random.nextInt(256); // R
                imagen[i][j][1] = random.nextInt(256); // G
                imagen[i][j][2] = random.nextInt(256); // B
            }
        }
        return imagen;
    }

    public static void guardarImagen(int[][][] imagen, String nombreArchivo) {
        int filas = imagen.length;
        int columnas = imagen[0].length;
        BufferedImage bufferedImage = new BufferedImage(columnas, filas, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int r = imagen[i][j][0];
                int g = imagen[i][j][1];
                int b = imagen[i][j][2];
                int rgb = (r << 16) | (g << 8) | b;
                bufferedImage.setRGB(j, i, rgb);
            }
        }
        try {
            ImageIO.write(bufferedImage, "png", new File(nombreArchivo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[][][] crearImagenPatron(int filas, int columnas) {
        int[][][] imagen = new int[filas][columnas][3];

        // Fondo negro
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                imagen[i][j][0] = 0; // R = 0
                imagen[i][j][1] = 0; // G = 0
                imagen[i][j][2] = 0; // B = 0
            }
        }

        // Cuadrado blanco en el centro
        int lado = Math.min(filas, columnas) / 3;
        int inicioY = (filas - lado) / 2;
        int inicioX = (columnas - lado) / 2;
        for (int i = inicioY; i < inicioY + lado; i++) {
            for (int j = inicioX; j < inicioX + lado; j++) {
                imagen[i][j][0] = 255; // R = 255
                imagen[i][j][1] = 255; // G = 255
                imagen[i][j][2] = 255; // B = 255
            }
        }
        return imagen;
    }
}
