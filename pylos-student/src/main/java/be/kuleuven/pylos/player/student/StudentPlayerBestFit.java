package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.Collections;

public class StudentPlayerBestFit extends PylosPlayer{

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
        /* add a reserve sphere to a feasible random location */
        PylosLocation[] allLocations = board.getLocations();
        ArrayList<PylosLocation> usableLocations = new ArrayList<>();
        for(PylosLocation loc:allLocations){
            if(loc.isUsable())usableLocations.add(loc);
        }

        //first check if there are any squares that can be made
        ArrayList<PylosLocation> squareLocations = searchSquares(usableLocations, this);
        if(squareLocations.size()>0){
            Collections.shuffle(squareLocations);
            PylosLocation moveLocation = squareLocations.get(0);
            PylosSphere[] spheres = board.getSpheres(this);
            PylosSphere moveSphere=null;
            for(PylosSphere sphere : spheres){
                if(sphere.canMoveTo(moveLocation) && !sphere.isReserve()){
                    moveSphere = sphere;
                }
            }
            if (moveSphere==null){
                moveSphere= board.getReserve(this);
            }

            game.moveSphere(moveSphere, moveLocation);
            return;
        }

        //then check if there are any squares the opponent can make
        ArrayList<PylosLocation> opponentSquareLocations = searchSquares(usableLocations, this.OTHER);
        if(opponentSquareLocations.size()>0){
            Collections.shuffle(opponentSquareLocations);
            PylosLocation moveLocation = opponentSquareLocations.get(0);
            PylosSphere[] spheres = board.getSpheres(this);
            PylosSphere moveSphere=null;
            for(PylosSphere sphere : spheres){
                if(sphere.canMoveTo(moveLocation) && !sphere.isReserve()){
                    moveSphere = sphere;
                }
            }
            if (moveSphere==null){
                moveSphere= board.getReserve(this);
            }

            game.moveSphere(moveSphere, moveLocation);
            return;
        }

        //check for any promotions that can be made

        //avoid any spots that will lead to opponent promoting


        allLocations = board.getLocations();
        PylosSphere reserve = null;

        int loc = -1;
        boolean okLocation = false;
        reserve = board.getReserve(this);
        while(!okLocation){

            loc = getRandom().nextInt(30);
            okLocation= allLocations[loc].isUsable();


        }
        game.moveSphere(reserve, allLocations[loc]);
    }

    private ArrayList<PylosLocation> searchSquares(ArrayList<PylosLocation> usableLocations,PylosPlayer player) {
        ArrayList<PylosLocation> squareLocations = new ArrayList<>();
        for(PylosLocation loc: usableLocations){
            for(PylosSquare sq : loc.getSquares()){
                if(sq.getInSquare(player)==3){
                    squareLocations.add(loc);
                    break;
                }
            }
        }
        return squareLocations;
    }

    @Override
    public void doRemove(PylosGameIF game, PylosBoard board) {
        /* removeSphere a random sphere */
        ArrayList<PylosSphere> removableSpheres = new ArrayList<>();
        for (PylosSphere ps : board.getSpheres(this)) {
            if (!ps.isReserve() && !ps.getLocation().hasAbove()) {
                removableSpheres.add(ps);
            }
        }
        PylosSphere sphereToRemove;
        if (removableSpheres.size() == 1) {
            sphereToRemove = removableSpheres.get(0);
        } else {
            sphereToRemove = removableSpheres.get(getRandom().nextInt(removableSpheres.size() - 1));
        }
        game.removeSphere(sphereToRemove);
    }

    @Override
    public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
        /* always pass */
        game.pass();
    }
}
