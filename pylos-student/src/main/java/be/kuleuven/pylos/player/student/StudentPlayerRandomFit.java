package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.player.PylosPlayer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ine on 5/05/2015.
 */
public class StudentPlayerRandomFit extends PylosPlayer{

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
        /* add a reserve sphere to a feasible random location */
        PylosLocation[] allLocations = board.getLocations();
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
