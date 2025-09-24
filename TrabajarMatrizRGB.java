import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TrabajarMatrizRGB {

    public static int[][][] ImagenToMatriz(String path) {
        try {
            BufferedImage imagen = ImageIO.read(new File(path));
            int alto = imagen.getHeight();
            int ancho = imagen.getWidth();
            int[][][] matriz = new int[alto][ancho][3];

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    int rgb = imagen.getRGB(x, y);
                    matriz[y][x][0] = (rgb >> 16) & 0xFF; 
                    matriz[y][x][1] = (rgb >> 8) & 0xFF;  
                    matriz[y][x][2] = rgb & 0xFF;         
                }
            }
            return matriz;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void MatrizToImagen(int[][][] matriz, String path) {
        if (matriz == null) return;
        int alto = matriz.length;
        int ancho = matriz[0].length;
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int r = matriz[y][x][0];
                int g = matriz[y][x][1];
                int b = matriz[y][x][2];
                int rgb = (r << 16) | (g << 8) | b;
                imagen.setRGB(x, y, rgb);
            }
        }
        try {
            ImageIO.write(imagen, "png", new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[][][] separarCanales(int[][][] matrizRGB) {
        int alto = matrizRGB.length;
        int ancho = matrizRGB[0].length;
        int[][] canalR = new int[alto][ancho];
        int[][] canalG = new int[alto][ancho];
        int[][] canalB = new int[alto][ancho];

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                canalR[y][x] = matrizRGB[y][x][0];
                canalG[y][x] = matrizRGB[y][x][1];
                canalB[y][x] = matrizRGB[y][x][2];
            }
        }
        return new int[][][] { canalR, canalG, canalB };
    }

    public static int[][][] combinarCanales(int[][] canalR, int[][] canalG, int[][] canalB) {
        int alto = canalR.length;
        int ancho = canalR[0].length;
        int[][][] matrizRGB = new int[alto][ancho][3];

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                matrizRGB[y][x][0] = canalR[y][x];
                matrizRGB[y][x][1] = canalG[y][x];
                matrizRGB[y][x][2] = canalB[y][x];
            }
        }
        return matrizRGB;
    }
}