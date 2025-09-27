public class Main {
    public static void main(String[] args) {
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

        int[][][] matrizFinal = TrabajarMatrizRGB.combinarCanales(canalR, canalG, canalB);
        TrabajarMatrizRGB.MatrizToImagen(matrizFinal, salida);
        System.out.println("Imagen procesada y guardada como " + salida);
    }
}