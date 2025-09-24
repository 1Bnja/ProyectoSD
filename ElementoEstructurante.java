public class ElementoEstructurante {    
    
    private int[][] estructura;
    private int anclaY;
    private int anclaX;

    public ElementoEstructurante(int[][] estructura, int anclaY, int anclaX) {
        this.estructura = estructura;
        this.anclaY = anclaY;
        this.anclaX = anclaX;
    }

    public static ElementoEstructurante Estructura1 = new ElementoEstructurante(
        new int[][] {
            {0, 0, 0},
            {1, 1, 0},
            {0, 1, 0}
        }, 1, 1  
    );
        
    public static ElementoEstructurante Estructura2 = new ElementoEstructurante(
        new int[][] {
            {0, 1, 0},
            {1, 1, 0},
            {0, 0, 0}
        }, 1, 1  
    );

    public static ElementoEstructurante Estructura3 = new ElementoEstructurante(
        new int[][] {
            {0, 0, 0},
            {1, 1, 1},
            {0, 0, 0}
        }, 1, 1  
    );

    public static ElementoEstructurante Estructura4= new ElementoEstructurante(
        new int[][] {
            {0, 0, 0},
            {0, 1, 0},
            {0, 1, 0}
        }, 1, 1  
    );

    public static ElementoEstructurante Estructura5 = new ElementoEstructurante(
        new int[][] {
            {1, 0, 1},
            {0, 1, 0},
            {1, 0, 1}
        }, 1, 1  
    );

    public int[][] getEstructura() {
        return estructura;
    }

    public int getAnclaY() {
        return anclaY;
    }

    public int getAnclaX() {
        return anclaX;
    }
}