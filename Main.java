public class Main {
    public static void main(String[] args) {
        String imagePath = "imagen_rgb.png";
        int[][][] matriz = TrabajarMatrizRGB.ImagenToMatriz(imagePath);

        if (matriz != null) {
            System.out.println("Imagen convertida a matriz correctamente.");
        } else {
            System.out.println("Error al convertir la imagen.");
        }
    }
}