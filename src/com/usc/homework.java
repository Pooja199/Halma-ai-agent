package com.usc;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class homework {

    public static void main(String[] args) throws IOException {
        int flag = 0;
        File file = new File("input.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String gameType = sc.next();
        char pawnColor = sc.next().charAt(0);
        double totalTime = sc.nextDouble();
        char[][] grid = new char[16][16];
        List<State> whiteList = new ArrayList<>();
        List<State> blackList = new ArrayList<>();
        State white = null;
        State black = null;
        for (int i = 0; i < 16; i++) {
            String line = sc.next();
            for (int j = 0; j < 16; j++) {
                grid[i][j] = line.charAt(j);
                if ('W' == grid[i][j]) {
                    white = new State(i, j);
                    whiteList.add(white);
                } else if ('B' == grid[i][j]) {
                    black = new State(i, j);
                    blackList.add(black);
                }
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        Board board = new Board(whiteList, blackList);
        switch (gameType) {
            case "SINGLE":
                FileWriter fileWriter = new FileWriter("output.txt");
                PrintWriter printWriter = new PrintWriter(fileWriter);
                Instant start = Instant.now();
                List<State> sortedList = getGoalStateList(pawnColor);
                State nextMove = minimax(3, pawnColor, board, true, pawnColor, -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, grid, sortedList).getState();
                Stack<State> stack = new Stack<>();
                if (nextMove.getMoveType() == 'E') {
                    System.out.println(nextMove.getMoveType() + " " + nextMove.parent.y + "," + nextMove.parent.x + " " + nextMove.y + "," + nextMove.x);
                    printWriter.println(nextMove.getMoveType() + " " + nextMove.parent.y + "," + nextMove.parent.x + " " + nextMove.y + "," + nextMove.x);
                } else {
                    stack.add(nextMove);
                    while (nextMove.parent != null) {
                        nextMove = nextMove.parent;
                        stack.add(nextMove);
                    }
                    stack.pop();
                    while (!stack.isEmpty()) {
                        State state = stack.pop();
                        System.out.println(state.getMoveType() + " " + state.parent.y + "," + state.parent.x + " " + state.y + "," + state.x);
                        printWriter.println(state.getMoveType() + " " + state.parent.y + "," + state.parent.x + " " + state.y + "," + state.x);
                    }
                }
                Instant end = Instant.now();
                System.out.println(Duration.between(start, end).toMillis());
                printWriter.close();
                break;
            case "GAME":
                int count = 0;
                Instant starte = Instant.now();
                //todo remove
                while (!isBoardInWinningPosition(board, pawnColor)) {
//                for(int x =0; x<5; x++){
                    FileWriter fileWriterGame = new FileWriter("output.txt");
                    PrintWriter printWriterGame = new PrintWriter(fileWriterGame);
                    printWriterGame.flush();
                    List<State> sortedListGame = null;
                    System.out.println("PAWNCOLOR = " + pawnColor);
                    StateValuePair minimaxPair;
                    if(pawnColor=='W') {
                        minimaxPair = minimax(1, pawnColor, board, true, pawnColor, -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, grid, sortedListGame);
                    }
                    else {
                        minimaxPair = minimax(3, pawnColor, board, true, pawnColor, -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, grid, sortedListGame);

                    }
                    System.out.println(minimaxPair.getValue());
                    State nextMoveGame = minimaxPair.getState();
                    if (nextMoveGame == null) {
                        break;
                    }
                    char[][] pc = board.makeMove(pawnColor, nextMoveGame, grid);
                    for (int i = 0; i < 16; i++) {
                        for (int j = 0; j < 16; j++) {
                            System.out.print(pc[i][j]);
                        }
                        System.out.println();
                    }
                    count++;
                    System.out.println(count);
                    Stack<State> stackGame = new Stack<>();
                    if (nextMoveGame.getMoveType() == 'E') {
                        System.out.println(nextMoveGame.getMoveType() + " " + nextMoveGame.parent.y + "," + nextMoveGame.parent.x + " " + nextMoveGame.y + "," + nextMoveGame.x);
                        printWriterGame.println(nextMoveGame.getMoveType() + " " + nextMoveGame.parent.y + "," + nextMoveGame.parent.x + " " + nextMoveGame.y + "," + nextMoveGame.x);
                    } else {
                        stackGame.add(nextMoveGame);
                        while (nextMoveGame.parent != null) {
                            nextMoveGame = nextMoveGame.parent;
                            stackGame.add(nextMoveGame);
                        }
                        stackGame.pop();
                        while (!stackGame.isEmpty()) {
                            State state = stackGame.pop();
                            System.out.println(state.getMoveType() + " " + state.parent.y + "," + state.parent.x + " " + state.y + "," + state.x);
                            printWriterGame.println(state.getMoveType() + " " + state.parent.y + "," + state.parent.x + " " + state.y + "," + state.x);
                        }
                    }
                    Instant ende = Instant.now();
                    System.out.println(Duration.between(starte, ende).toMillis());
                    pawnColor = pawnColor == 'W' ? 'B' : 'W';
                    printWriterGame.close();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + gameType);
        }
    }


    private static StateValuePair minimax(int depth, char pawnColor, Board board, boolean maximizingPlayer, char maxPlayerColor, double alpha, double beta, char[][] grid, List<State> sortedGoalStateList) {
        long value;
        State finalState = null;
        if (isBoardInWinningPosition(board, maxPlayerColor)) {
            return new StateValuePair(null, 1000000L*(depth+1));
        }
        if (depth == 0) {
            StateValuePair stateValuePair =  calculateHeuristic(board, maxPlayerColor);
            if(stateValuePair.getValue()==999957){
                System.out.println("TEST");
            }
            return stateValuePair;
        }

        if (maximizingPlayer) {
            List<State> states = board.findAllValidMoves(pawnColor, board);
            value = -10000000000L;
            for (State state : states) {
                board.makeMove(pawnColor, state, grid);
                StateValuePair tempMoveVal = minimax(depth - 1, pawnColor == 'W' ? 'B' : 'W', board, !maximizingPlayer, maxPlayerColor, alpha, beta, grid, sortedGoalStateList);
                board.unMakeMove(pawnColor, state, grid);
                if (tempMoveVal.getValue() > value) {
                    finalState = state;
                    value = tempMoveVal.getValue();
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta)
                    break;
            }
        } else {
            List<State> states = board.findAllValidMoves(pawnColor, board);
            value = 10000000000L;
            for (State state : states) {
                board.makeMove(pawnColor, state, grid);
                StateValuePair tempMoveVal = minimax(depth - 1, pawnColor == 'W' ? 'B' : 'W', board, !maximizingPlayer, maxPlayerColor, alpha, beta, grid, sortedGoalStateList);
                board.unMakeMove(pawnColor, state, grid);
                if (tempMoveVal.getValue() < value) {
                    finalState = state;
                    value = tempMoveVal.getValue();
                }
                beta = Math.min(beta, value);
                if (alpha >= beta)
                    break;
            }
        }
        StateValuePair stateLongPair = new StateValuePair(finalState, value);
        return stateLongPair;
    }

    private static StateValuePair calculateHeuristic(Board board, char maxPlayerColor) {
        StateValuePair stateValuePair = null;
        long multiplier = 100;
        if (maxPlayerColor == 'W') {
            long maxDistance = 0;
            for (State state : board.whiteStates) {
                long distance = (state.x + state.y);
                maxDistance -= distance * multiplier;
                stateValuePair = new StateValuePair(state, maxDistance);
            }
            if (maxDistance > -150 * multiplier) {
                List<State> unoccupiedGoals = new ArrayList<>();
                List<State> unMatchedSates = new ArrayList<>();
                List<State> goalStates = getGoalStateList(maxPlayerColor);
                for(State state: board.whiteStates){
                    if(!goalStates.contains(state)){
                        unMatchedSates.add(state);
                    }
                }

                for(State goal: goalStates){
                    if (!Arrays.asList(board.whiteStates).contains(goal)){
                        unoccupiedGoals.add(goal);
                    }
                }
                return calculateHeuristicSecond(unoccupiedGoals,unMatchedSates);
            }
        } else {
            long maxDistance = 0;
            for (State state : board.blackStates) {
                if (state.parent != null) {
                    long distance = Math.abs(state.x - 15) + Math.abs(state.y - 15);
                    maxDistance -= distance * multiplier;
                    stateValuePair = new StateValuePair(state, maxDistance);
                }
            }
            if (maxDistance > -150 * multiplier) {
                List<State> unoccupiedGoalsBlack = new ArrayList<>();
                List<State> unMatchedSatesBlack = new ArrayList<>();
                List<State> goalStates = getGoalStateList(maxPlayerColor);
                for(State state: board.blackStates){
                    if(!goalStates.contains(state)){
                        unMatchedSatesBlack.add(state);
                    }
                }

                for(State goal: goalStates){
                    if (!Arrays.asList(board.blackStates).contains(goal)){
                        unoccupiedGoalsBlack.add(goal);
                    }
                }
                return calculateHeuristicSecond( unoccupiedGoalsBlack,unMatchedSatesBlack);
            }
        }
        return stateValuePair;
    }

    private static StateValuePair calculateHeuristicSecond( List<State> goalStateList, List<State> boardList) {
        boardList = sortList(boardList);
        goalStateList=sortList(goalStateList);
        long distance;
        long totalDistance = 0;
        for (int i = 0; i < goalStateList.size(); i++) {
            distance = -Math.abs(boardList.get(i).x - goalStateList.get(i).x) - Math.abs(boardList.get(i).y - goalStateList.get(i).y);
            totalDistance += distance;
        }
        return new StateValuePair(null, totalDistance);
    }

    private static boolean isBoardInWinningPosition(Board board, char pawnColor) {
        if (pawnColor == 'W') {
            int countWhite = 0;
            int countBlack = 0;
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    if ((i == 0 || i == 1) && j <= 4 || (i == 2 && j <= 3) || (i == 3 && j <= 2) || (i == 4 && j <= 1)) {
                        State state = new State(i, j);
                        for (State whites : board.whiteStates) {
                            if (whites.equals(state)) {
                                countWhite++;
                            }
                        }
                        for (State blacks : board.blackStates) {
                            if (blacks.equals(state)) {
                                countBlack++;
                            }
                        }
                    }
                }
            }
//            if ((countBlack + countWhite) == 19) {
//                System.out.println(19);
//            }
            return countWhite > 0 && (countBlack + countWhite) == 19;
        } else if (pawnColor == 'B') {
            int countWhite = 0;
            int countBlack = 0;
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    if (((i == 15 || i == 14) && j >= 11) || (i == 13 && j >= 12) || (i == 12 && j >= 13) || (i == 11 && j >= 14)) {
                        State state = new State(i, j);
                        for (State whites : board.whiteStates) {
                            if (whites.equals(state)) {
                                countWhite++;
                            }
                        }
                        for (State blacks : board.blackStates) {
                            if (blacks.equals(state)) {
                                countBlack++;
                            }
                        }
                    }
                }
            }
            return countBlack > 0 && (countBlack + countWhite) == 19;
        }
        return false;
    }

    private static List<State> getGoalStateList(char pawnColor) {
        List<State> result = new ArrayList<>();
        if (pawnColor == 'W') {
            for (int i = 0; i <= 4; i++) {
                if (i == 0) {
                    for (int j = 0; j <= 4; j++)
                        result.add(new State(i, j));
                }
                if (i == 1) {
                    for (int j = 0; j <= 4; j++)
                        result.add(new State(i, j));
                }
                if (i == 2) {
                    for (int j = 0; j <= 3; j++)
                        result.add(new State(i, j));
                }
                if (i == 3) {
                    for (int j = 0; j <= 2; j++)
                        result.add(new State(i, j));
                }
                if (i == 4) {
                    for (int j = 0; j <= 1; j++)
                        result.add(new State(i, j));
                }
            }
        } else {
            for (int i = 15; i >= 11; i--) {
                if (i == 15) {
                    for (int j = 15; j >= 11; j--)
                        result.add(new State(i, j));
                }
                if (i == 14) {
                    for (int j = 15; j >= 11; j--)
                        result.add(new State(i, j));
                }
                if (i == 13) {
                    for (int j = 15; j >= 12; j--)
                        result.add(new State(i, j));
                }
                if (i == 12) {
                    for (int j = 15; j >= 13; j--)
                        result.add(new State(i, j));
                }
                if (i == 11) {
                    for (int j = 15; j >= 14; j--)
                        result.add(new State(i, j));
                }
            }
        }
        return result;
    }

    private static List<State> sortList(List<State> unsortedList){
        Collections.sort(unsortedList, new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                int result = Double.compare(o1.x, o2.x);
                if (result == 0) {
                    // both X are equal -> compare Y too
                    result = Double.compare(o1.y, o2.y);
                }
                return result;
            }
        });
        return unsortedList;
    }
}

class StateValuePair {

    private State state;
    private Long value;

    public StateValuePair(State state, Long value) {
        this.state = state;
        this.value = value;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}


class Board {
    State[] blackStates = new State[19];
    State[] whiteStates = new State[19];


    Board(List<State> whiteList, List<State> blackList) {
        int i = 0, j = 0;
        for (State state : whiteList) {
            whiteStates[i] = state;
            i++;
        }
        for (State state : blackList) {
            blackStates[j] = state;
            j++;
        }
    }

    private static boolean isStateInsideOwnCamp(State state, char pawnColor) {
        if (pawnColor == 'B') {
            if ((state.y == 0 || state.y == 1) && (state.x >= 0 && state.x <= 4)) {
                return true;
            } else if (state.y == 2 && state.x >= 0 && state.x <= 3) {
                return true;
            } else if (state.y == 3 && state.x >= 0 && state.x <= 2) {
                return true;
            } else return state.y == 4 && (state.x == 0 || state.x == 1);
        } else if (pawnColor == 'W') {
            if ((state.y == 15 || state.y == 14) && state.x >= 11 && state.x <= 15)
                return true;
            else if (state.y == 13 && state.x >= 12 && state.x <= 15)
                return true;
            else if (state.y == 12 && state.x >= 13 && state.x <= 15)
                return true;
            else return state.y == 11 && state.x == 14 || state.x == 15;
        }
        return false;
    }

    List<State> findAllValidMoves(char pawnColor, Board board) {
        if (pawnColor == 'W') {
            List<State> pawnInsideCamp = new ArrayList<>();
            List<State> pawnOutsideCamp = new ArrayList<>();
            for (State st : whiteStates) {
                if (isStateInsideOwnCamp(st, pawnColor)) {
                    pawnInsideCamp.add(st);
                }
                if (!isStateInsideOwnCamp(st, pawnColor)) {
                    pawnOutsideCamp.add(st);
                }
            }
            List<State> insideToNext = new ArrayList<>();
            List<State> outsideToNext = new ArrayList<>();
            List<State> resultMoveInsideToOutside = new ArrayList<>();
            List<State> resultMoveWithinOwnCamp = new ArrayList<>();
            List<State> result = new ArrayList<>();
            if (!pawnInsideCamp.isEmpty()) {
                for (State inside : pawnInsideCamp) {
                    insideToNext.addAll(inside.getNextMoves(pawnColor, board));
                }
            }
            if (!pawnOutsideCamp.isEmpty()) {
                for (State outside : pawnOutsideCamp) {
                    outsideToNext.addAll(outside.getNextMoves(pawnColor, board));
                }
            }
            for (State next : insideToNext) {
                if (!isStateInsideOwnCamp(next, pawnColor)) {
                    resultMoveInsideToOutside.add(next);
                } else {
                    if(moveAwayFromCorner(pawnColor,next.parent,next))
                        resultMoveWithinOwnCamp.add(next);
                }
            }
            if (!resultMoveInsideToOutside.isEmpty()) {
                result = resultMoveInsideToOutside;
            } else if (resultMoveInsideToOutside.isEmpty() && !resultMoveWithinOwnCamp.isEmpty()) {
                result = resultMoveWithinOwnCamp;
            } else if (resultMoveInsideToOutside.isEmpty() && resultMoveWithinOwnCamp.isEmpty()) {
                result = outsideToNext;
            }
            Collections.shuffle(result);
            return result;
        } else {
            List<State> pawnInsideCamp = new ArrayList<>();
            List<State> pawnOutsideCamp = new ArrayList<>();
            for (State st : blackStates) {
                if (isStateInsideOwnCamp(st, pawnColor)) {
                    pawnInsideCamp.add(st);
                }
                if (!isStateInsideOwnCamp(st, pawnColor)) {
                    pawnOutsideCamp.add(st);
                }
            }
            List<State> insideToNext = new ArrayList<>();
            List<State> outsideToNext = new ArrayList<>();
            List<State> resultMoveInsideToOutside = new ArrayList<>();
            List<State> resultMoveWithinOwnCamp = new ArrayList<>();
            List<State> result = new ArrayList<>();
            if (!pawnInsideCamp.isEmpty()) {
                for (State inside : pawnInsideCamp) {
                    insideToNext.addAll(inside.getNextMoves(pawnColor, board));
                }
            }
            if (!pawnOutsideCamp.isEmpty()) {
                for (State outside : pawnOutsideCamp) {
                    outsideToNext.addAll(outside.getNextMoves(pawnColor, board));
                }
            }
            for (State next : insideToNext) {
                if (!isStateInsideOwnCamp(next, pawnColor)) {
                    resultMoveInsideToOutside.add(next);
                } else {
                    if(moveAwayFromCorner(pawnColor,next.parent,next))
                        resultMoveWithinOwnCamp.add(next);
                }
            }
            if (!resultMoveInsideToOutside.isEmpty()) {
                result = resultMoveInsideToOutside;
            } else if (resultMoveInsideToOutside.isEmpty() && !resultMoveWithinOwnCamp.isEmpty()) {
                result = resultMoveWithinOwnCamp;
            } else if (resultMoveInsideToOutside.isEmpty() && resultMoveWithinOwnCamp.isEmpty()) {
                result = outsideToNext;
            }
            Collections.shuffle(result);
            return result;
        }
    }

    private int findPiece(State state, char pawnColor) {
        if (pawnColor == 'W') {
            for (int i = 0; i < whiteStates.length; i++) {
                if (state.equals(whiteStates[i])) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < blackStates.length; i++) {
                if (state.equals(blackStates[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean moveAwayFromCorner(char pawnColor, State oldState, State newState){
        State corner = null;
        if(pawnColor=='W'){
            corner = new State(15,15);
        }else{
            corner = new State(0,0);
        }
        long oldist = Math.abs(corner.x-oldState.x)+Math.abs(corner.y-oldState.y);
        long newDist = Math.abs(corner.x-newState.x)+Math.abs(corner.y-newState.y);
        return oldist<newDist;
    }

    char[][] makeMove(char pawnColor, State state, char[][] grid) {
        //todo change state
        State finalState = state;
        if (state != null) {
            while (state.parent != null) {
                state = state.parent;
            }
            if (pawnColor == 'W') {
                int index = findPiece(state, pawnColor);
                grid[state.x][state.y] = '.';
                grid[finalState.x][finalState.y] = 'W';
                whiteStates[index] = finalState;
            } else if (pawnColor == 'B') {
                int index = findPiece(state, pawnColor);
                grid[state.x][state.y] = '.';
                grid[finalState.x][finalState.y] = 'B';
                blackStates[index] = finalState;
            }
        }
        return grid;
    }

    void unMakeMove(char pawnColor, State state, char[][] grid) {
        State finalState = state;
        while (state.parent != null) {
            state = state.parent;
        }
        if (pawnColor == 'W') {
            int index = findPiece(finalState, pawnColor);
            grid[finalState.x][finalState.y] = '.';
            grid[state.x][state.y] = 'W';
            whiteStates[index] = state;
        } else if (pawnColor == 'B') {
            int index = findPiece(finalState, pawnColor);
            grid[finalState.x][finalState.y] = '.';
            grid[state.x][state.y] = 'B';
            blackStates[index] = state;
        }
    }
}


class State {

    final int y;
    int x;
    State parent;
    private char moveType;

    State(int x, int y) {
        this.x = x;
        this.y = y;
    }

    char getMoveType() {
        return moveType;
    }

    public State getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return y == state.y &&
                x == state.x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(y, x);
    }

    List<State> getNextMoves(char pawnColor, Board board) {
        List<State> moves = new LinkedList<>();
        State state = new State(x, y);
        if (pawnColor == 'W') {
            int[] xMove = {-1, -1, 0, -1, 1};
            int[] yMove = {-1, 0, -1, 1, -1};
            for (int i = 0; i < 5; i++) {
                State tempState = new State(x + xMove[i], y + yMove[i]);
                if (isValidRowCol(tempState.x, tempState.y) && !isStateInBoard(board, tempState)) {
                    tempState.moveType = 'E';
                    tempState.parent = state;
                    moves.add(tempState);
                } else {
                    //JUMPS
                    getJumpMoves(board, tempState, xMove[i], yMove[i], moves, pawnColor, state);
                }
            }
        } else if (pawnColor == 'B') {
            //todo change
            int[] xMove = {1, 0, 1, 1, -1};
            int[] yMove = {0, 1, 1, -1, 1};
            for (int i = 0; i < 5; i++) {
                State tempState = new State(x + xMove[i], y + yMove[i]);
                if (isValidRowCol(tempState.x, tempState.y) && !isStateInBoard(board, tempState)) {
                    tempState.moveType = 'E';
                    tempState.parent = state;
                    moves.add(tempState);
                } else {
                    //JUMPS
                    getJumpMoves(board, tempState, xMove[i], yMove[i], moves, pawnColor, state);
                }
            }
        }
        return moves;
    }

    private void getJumpMoves(Board board, State tempState, int xMove, int yMove, List<State> moves, char pawnColor, State parent) {
        if (isValidRowCol(tempState.x, tempState.y) && isStateInBoard(board, tempState)) {
            State nextJumpState = new State(tempState.x + xMove, tempState.y + yMove);
            if (isValidRowCol(nextJumpState.x, nextJumpState.y) && !isStateInBoard(board, nextJumpState)) {
                nextJumpState.moveType = 'J';
                nextJumpState.parent = parent;
                moves.add(nextJumpState);
                if (pawnColor == 'W') {
                    for (int j = 0; j < 3; j++) {
                        int[] xMoveNew = {-1, 0, -1};
                        int[] yMoveNew = {0, -1, -1};
                        if (isValidRowCol(nextJumpState.x + xMoveNew[j], nextJumpState.y + yMoveNew[j])) {
                            tempState = new State(nextJumpState.x + xMoveNew[j], nextJumpState.y + yMoveNew[j]);
                            getJumpMoves(board, tempState, xMoveNew[j], yMoveNew[j], moves, pawnColor, nextJumpState);
                        }
                    }
                } else if (pawnColor == 'B') {
                    for (int j = 0; j < 3; j++) {
                        int[] xMoveNew = {1, 0, 1};
                        int[] yMoveNew = {0, 1, 1};
                        if (isValidRowCol(nextJumpState.x + xMoveNew[j], nextJumpState.y + yMoveNew[j])) {
                            tempState = new State(nextJumpState.x + xMoveNew[j], nextJumpState.y + yMoveNew[j]);
                            getJumpMoves(board, tempState, xMoveNew[j], yMoveNew[j], moves, pawnColor, nextJumpState);
                        }
                    }
                }
            }
        }
    }

    private boolean isStateInBoard(Board board, State state) {
        for (int i = 0; i < board.whiteStates.length; i++) {
            if (board.whiteStates[i].equals(state) || board.blackStates[i].equals(state)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidRowCol(int x, int y) {
        return (x >= 0) && (x < 16) &&
                (y >= 0) && (y < 16);
    }
}
