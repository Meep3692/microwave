package ca.awoo.microwave.hell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import ca.awoo.microwave.Game;

public class PieceSprite {
    public static enum Direction {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST
    }
    public static enum Type {
        BISHOP(4, 0, 0),
        KNIGHT(4, 0, 128),
        KING(2, 0, 128*2),
        QUEEN(1, 128*2, 128*2),
        ROOK(1, 128*3, 128*2),
        PAWN(1, 0, 128*3);
        public final int dirs;
        public final int rootX;
        public final int rootY;
        private Type(int dirs, int rootX, int rootY){
            this.dirs = dirs;
            this.rootX = rootX;
            this.rootY = rootY;
        }
    }
    public static enum Team{
        BLACK,
        WHITE
    }
    public static enum Variant{
        MARBLE,
        PLASTIC,
        RUST,
        WOOD
    }
    private final Type type;
    private final Team team;
    private final Variant variant;
    private final Image spriteSheet;
    private final Image spriteSheetDiag;
    public int layer;
    public boolean visible = true;
    public PieceSprite(Game game, Type type, Team team, Variant variant) {
        this.type = type;
        this.team = team;
        this.variant = variant;
        Color mask = ((team == Team.BLACK) ? new Color(0, 128, 128) : Color.BLACK);
        String teamName = ((team == Team.BLACK) ? "Black" : "White");
        String variantName;
        switch(variant){
            case MARBLE:
                variantName = "Marble";
                break;
            case PLASTIC:
                variantName = "Plastic";
                break;
            case RUST:
                variantName = "Rust";
                break;
            case WOOD:
                variantName = "Wood";
                break;
            default:
                throw new IllegalArgumentException("Unknown variant: " + variant);
        }
        String spritePath = "/com/screamingbrainstudio/chess/Top Down/Pieces/" + teamName + "/" + teamName + " - " + variantName + " 1 128x128.png";
        String spritePathDiag = "/com/screamingbrainstudio/chess/Isometric/Pieces/" + teamName + "/" + teamName + " - " + variantName + " 1.png";
        spriteSheet = game.getImageMasked(spritePath, mask);
        if(team == Team.BLACK && variant == Variant.WOOD){
            spriteSheetDiag = game.getImageMasked(spritePathDiag, Color.BLACK);
        }else{
            spriteSheetDiag = game.getImageMasked(spritePathDiag, mask);
        }
    }

    public void draw(Graphics g, Direction dir, int x, int y, double scale){
        Image sheet = spriteSheet;
        int dirIndex = 0;
        if(type.dirs == 4){
            switch(dir){
                case EAST:
                    dirIndex = 0;
                    sheet = spriteSheet;
                    break;
                case NORTH:
                    dirIndex = 1;
                    sheet = spriteSheet;
                    break;
                case SOUTH:
                    dirIndex = 2;
                    sheet = spriteSheet;
                    break;
                case WEST:
                    dirIndex = 3;
                    sheet = spriteSheet;
                    break;
                case NORTH_EAST:
                    dirIndex = 0;
                    sheet = spriteSheetDiag;
                    break;
                case NORTH_WEST:
                    dirIndex = 1;
                    sheet = spriteSheetDiag;
                    break;
                case SOUTH_EAST:
                    dirIndex = 2;
                    sheet = spriteSheetDiag;
                    break;
                case SOUTH_WEST:
                    dirIndex = 3;
                    sheet = spriteSheetDiag;
                    break;
            }
        }else if(type.dirs == 2){
            switch(dir){
                case NORTH:
                case SOUTH:
                    dirIndex = 0;
                    sheet = spriteSheet;
                    break;
                case EAST:
                case WEST:
                    dirIndex = 1;
                    sheet = spriteSheet;
                    break;
                case NORTH_EAST:
                case SOUTH_WEST:
                    dirIndex = 0;
                    sheet = spriteSheetDiag;
                    break;
                case NORTH_WEST:
                case SOUTH_EAST:
                    dirIndex = 1;
                    sheet = spriteSheetDiag;
                    break;
            }
        }else{
            dirIndex = 0;
            sheet = spriteSheet;
        }
        int sourcex = type.rootX + dirIndex*128;
        int sourcey = type.rootY;
        int xd2 = (int) (x+128*scale);
        int yd2 = (int) (y+128*scale);
        g.drawImage(sheet, x, y, xd2, yd2, sourcex, sourcey, sourcex+128, sourcey+128, null);
    }

    public Type getType() {
        return type;
    }

    public Team getTeam() {
        return team;
    }

    public Variant getVariant() {
        return variant;
    }
    
}
