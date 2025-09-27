public class Secuencial {

    public static int[][] dilatacion(int[][] canal, ElementoEstructurante ee) {
        int alto = canal.length;
        int ancho = canal[0].length;
        int[][] resultado = new int[alto][ancho];
        int[][] estructura = ee.getEstructura();
        int anclaY = ee.getAnclaY();
        int anclaX = ee.getAnclaX();

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int max = 0;
                for (int i = 0; i < estructura.length; i++) {
                    for (int j = 0; j < estructura[0].length; j++) {
                        if (estructura[i][j] == 1) {
                            int yy = y + i - anclaY;
                            int xx = x + j - anclaX;
                            if (yy >= 0 && yy < alto && xx >= 0 && xx < ancho) {
                                max = Math.max(max, canal[yy][xx]);
                            }
                        }
                    }
                }
                resultado[y][x] = max;
            }
        }
        return resultado;
    }

    public static int[][] erosion(int[][] canal, ElementoEstructurante ee) {
        int alto = canal.length;
        int ancho = canal[0].length;
        int[][] resultado = new int[alto][ancho];
        int[][] estructura = ee.getEstructura();
        int anclaY = ee.getAnclaY();
        int anclaX = ee.getAnclaX();

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int min = 255;
                for (int i = 0; i < estructura.length; i++) {
                    for (int j = 0; j < estructura[0].length; j++) {
                        if (estructura[i][j] == 1) {
                            int yy = y + i - anclaY;
                            int xx = x + j - anclaX;
                            if (yy >= 0 && yy < alto && xx >= 0 && xx < ancho) {
                                min = Math.min(min, canal[yy][xx]);
                            }
                        }
                    }
                }
                resultado[y][x] = min;
            }
        }
        return resultado;
    }
}
