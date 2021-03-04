package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class StudentPlayerBestFit extends PylosPlayer {
    PylosLocation lastPlaced;

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
        // Moves will have a certain priority
        //      Try to make a square of our own
        //      Try to block a square from the opponent
        //      Try to move a sphere to a higher layer, if that doesn't open up a square for the opponent
        //      Place a reserve sphere on the board

        // Get all usable locations
        PylosLocation[] allLocations = board.getLocations();
        ArrayList<PylosLocation> usableLocations = new ArrayList<>();
        for (PylosLocation loc : allLocations) {
            if (loc.isUsable()) usableLocations.add(loc);
        }

        //first check if there are any squares that can be made
        ArrayList<PylosLocation> squareLocations = searchSquares(usableLocations, this);
        if (squareLocations.size() > 0) {
            Collections.shuffle(squareLocations);
            PylosLocation moveLocation = squareLocations.get(0);

            //Check if any sphere can be promoted to form a square, if not, take a reserve sphere
            PylosSphere[] spheres = board.getSpheres(this);
            PylosSphere moveSphere = null;
            for (PylosSphere sphere : spheres) {
                if (sphere.canMoveTo(moveLocation) && !sphere.isReserve()) {
                    moveSphere = sphere;
                }
            }
            if (moveSphere == null) {
                moveSphere = board.getReserve(this);
            }

            //do the move if it doesn't result in a draw
            if (!game.moveSphereIsDraw(moveSphere, moveLocation)) {
                game.moveSphere(moveSphere, moveLocation);
                lastPlaced = moveLocation;
                return;
            }
        }

        //then check if there are any squares the opponent can make
        ArrayList<PylosLocation> opponentSquareLocations = searchSquares(usableLocations, this.OTHER);
        if (opponentSquareLocations.size() > 0) {
            Collections.shuffle(opponentSquareLocations);
            PylosLocation moveLocation = opponentSquareLocations.get(0);

            //Check if any sphere can be promoted to block the square, if not, take a reserve sphere
            PylosSphere[] spheres = board.getSpheres(this);
            PylosSphere moveSphere = null;
            for (PylosSphere sphere : spheres) {
                if (sphere.canMoveTo(moveLocation) && !sphere.isReserve()) {
                    moveSphere = sphere;
                }
            }
            if (moveSphere == null) {
                moveSphere = board.getReserve(this);
            }

            //do the move if it doesn't result in a draw
            if (!game.moveSphereIsDraw(moveSphere, moveLocation)) {
                game.moveSphere(moveSphere, moveLocation);
                lastPlaced = moveLocation;
                return;
            }

        }
        //check for any promotions that can be made, first check which spheres should stay in place in order to prevent opponent squares
        ArrayList<PylosLocation> stayLocations = new ArrayList<>();
        for (PylosSquare ps : board.getAllSquares()) {
            if (ps.getInSquare(this.OTHER) == 3) {
                stayLocations.addAll(Arrays.asList(ps.getLocations()));
            }
        }
        for (PylosSphere sphere : board.getSpheres(this)) {
            if (!sphere.isReserve() && !stayLocations.contains(sphere.getLocation())) {
                for (PylosLocation loc : usableLocations) {
                    if (sphere.canMoveTo(loc)) {
                        game.moveSphere(sphere, loc);
                        lastPlaced = loc;
                        return;
                    }
                }
            }
        }

        //place a reserve sphere, avoid any spots that will lead to opponent promoting
        ArrayList<PylosLocation> goodLocations = new ArrayList<>();
        ArrayList<PylosLocation> worseLocations = new ArrayList<>();
        PylosLocation moveLocation;

        for (PylosLocation loc : usableLocations) {
            for (PylosSquare ps : loc.getSquares()) {
                if (ps.getInSquare() < 3) goodLocations.add(loc);
                else worseLocations.add(loc);
            }
        }
        if (goodLocations.size() > 0) {
            Collections.shuffle(goodLocations);
            moveLocation = goodLocations.get(0);
        } else {
            Collections.shuffle(worseLocations);
            moveLocation = worseLocations.get(0);
        }
        PylosSphere moveSphere = board.getReserve(this);
        if(game.moveSphereIsDraw(moveSphere,moveLocation)){
            doMove(game,board);
        }else {
            game.moveSphere(moveSphere, moveLocation);
            lastPlaced = moveLocation;
        }
    }

    //method to get all squares with 3 spheres of a certain color
    private ArrayList<PylosLocation> searchSquares(ArrayList<PylosLocation> usableLocations, PylosPlayer player) {
        ArrayList<PylosLocation> squareLocations = new ArrayList<>();
        for (PylosLocation loc : usableLocations) {
            for (PylosSquare sq : loc.getSquares()) {
                if (sq.getInSquare(player) == 3) {
                    squareLocations.add(loc);
                    break;
                }
            }
        }
        return squareLocations;
    }

    @Override
    public void doRemove(PylosGameIF game, PylosBoard board) {
        //Removes a sphere in the square that was just created
        //Avoid removing the sphere that was just placed in order to avoid draws
        ArrayList<PylosSphere> removableSpheres = new ArrayList<>();
        for (PylosSphere ps : board.getSpheres(this)) {
            if (!ps.isReserve() && !ps.getLocation().hasAbove()) {
                removableSpheres.add(ps);
            }
        }
        PylosSphere sphereToRemove = null;
        if (removableSpheres.size() == 1) {
            sphereToRemove = removableSpheres.get(0);
        } else {
            for (PylosSquare ps : board.getAllSquares()) {
                if (ps.isSquare(this) && Arrays.asList(ps.getLocations()).contains(lastPlaced)) {
                    for (PylosLocation loc : ps.getLocations()) {
                        if (!loc.equals(lastPlaced) && loc.getSphere().canMove()) sphereToRemove = loc.getSphere();
                    }
                    if (sphereToRemove == null) sphereToRemove = lastPlaced.getSphere();
                }
            }
        }
        game.removeSphere(sphereToRemove);
    }

    @Override
    public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
        //remove a second sphere if it doesn't open up a square opportunity for the opponent
        //if no sphere can be safely removed, pass
        ArrayList<PylosSphere> removableSpheres = new ArrayList<>();
        for (PylosSphere ps : board.getSpheres(this)) {
            if (!ps.isReserve() && !ps.getLocation().hasAbove()) {
                removableSpheres.add(ps);
            }
        }
        ArrayList<PylosLocation> stayLocations = new ArrayList<>();
        for (PylosSquare ps : board.getAllSquares()) {
            if(ps.getInSquare(this)==3||ps.getInSquare(this.OTHER)==3){
                stayLocations.addAll(Arrays.asList(ps.getLocations()));
            }
        }

        for(PylosSphere sphere : removableSpheres) {
            if(!stayLocations.contains(sphere.getLocation())){
                game.removeSphere(sphere);
                return;
            }
        }
        game.pass();
    }
}
