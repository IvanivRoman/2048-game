package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    int score;
    int maxTile;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
        this.score = 0;
        this.maxTile = 0;
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++){
            for (int j = 0; j < FIELD_WIDTH; j++){
                if(gameTiles[i][j].value == 0)
                    emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    private void addTile(){
        List<Tile> list = getEmptyTiles();
        if (list != null && list.size() != 0) {
            list.get((int) (list.size() * Math.random())).setValue(Math.random() < 0.9 ? 2 : 4);
        }
    }

    void resetGameTiles(){
        for (int i = 0; i < FIELD_WIDTH; i++){
            for (int j = 0; j < FIELD_WIDTH; j++){
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean changed = false;

        for (int j = 0; j < 3; j++) {
            if (tiles[j].value != 0 && tiles[j].value == tiles[j + 1].value) {
                tiles[j].setValue(tiles[j].value * 2);
                tiles[j + 1].setValue(0);
                if (tiles[j].value > maxTile) maxTile = tiles[j].value;
                score += tiles[j].value;
                changed = true;
            }
        }

        if(changed)
            compressTiles(tiles);

        return changed;
    }

    private boolean compressTiles(Tile[] tiles){
        boolean changed = false;
        Tile tile;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(tiles[j].value == 0 && tiles[j + 1].value != 0){
                    tile = tiles[j];
                    tiles[j] = tiles[j + 1];
                    tiles[j + 1] = tile;
                    changed = true;
                }
            }
        }
        return changed;
    }

    public void left(){
        if(isSaveNeeded)
            saveState(this.gameTiles);

        boolean changed = false;
        for(int i = 0; i < FIELD_WIDTH; i++){
            if(compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i]))
                changed = true;
        }

        if(changed){
            addTile();
            isSaveNeeded = true;
        }
    }

    public void up(){
        saveState(gameTiles);
        reverse90();
        left();
        reverse90();
        reverse90();
        reverse90();
    }

    public void right(){
        saveState(this.gameTiles);
        reverse90();
        reverse90();
        left();
        reverse90();
        reverse90();
    }

    public void down(){
        saveState(this.gameTiles);
        reverse90();
        reverse90();
        reverse90();
        left();
        reverse90();
    }

    private void reverse90(){
        for (int i = 0; i < 2; i++) {
            for (int j = i; j < 3 - i; j++) {
                Tile tmp = gameTiles[i][j];
                gameTiles[i][j] = gameTiles[j][3 - i];
                gameTiles[j][3 - i] = gameTiles[3 - i][3 - j];
                gameTiles[3 - i][3 - j] = gameTiles[3 - j][i];
                gameTiles[3 - j][i] = tmp;
            }
        }
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove(){
        if (!getEmptyTiles().isEmpty())
            return true;

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 1; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == gameTiles[i][j - 1].value || gameTiles[j][i].value == gameTiles[j - 1][i].value)
                    return true;
            }
        }

        return false;
    }

    private void saveState(Tile[][] tiles){
        Tile[][] destination = new Tile[tiles.length][tiles[0].length];

        for(int i=0; i<tiles.length; i++)
            for(int j=0; j<tiles[i].length; j++)
                destination[i][j]=new Tile(tiles[i][j].value);

        previousStates.push(destination);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if(!previousScores.isEmpty() && !previousStates.isEmpty()){
            this.gameTiles = previousStates.pop();
            this.score = previousScores.pop();
        }
    }

    public void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0:
                left();
                break;
            case 1:
                up();
                break;
            case 2:
                right();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged(){
        int gameTilesSum = 0;
        int previousStatesSum = 0;
        if(!previousStates.isEmpty()){
            Tile[][] previous = previousStates.peek();
            for(int i = 0; i < FIELD_WIDTH; i++){
                for(int j = 0; j < FIELD_WIDTH; j++){
                    gameTilesSum += gameTiles[i][j].value;
                    previousStatesSum += previous[i][j].value;
                }
            }
        }
        return gameTilesSum != previousStatesSum;
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency;
        move.move();

        if(hasBoardChanged())
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        else
            moveEfficiency = new MoveEfficiency(-1, 0, move);

        rollback();

        return moveEfficiency;
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> effectiveMove = new PriorityQueue<>(4, Collections.reverseOrder());
        effectiveMove.add(getMoveEfficiency(this::left));
        effectiveMove.add(getMoveEfficiency(this::up));
        effectiveMove.add(getMoveEfficiency(this::right));
        effectiveMove.add(getMoveEfficiency(this::down));
        effectiveMove.peek().getMove().move();
    }
}
