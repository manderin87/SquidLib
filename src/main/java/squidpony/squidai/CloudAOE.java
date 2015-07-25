package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidgrid.Spill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An AOE type that has a center and a volume, and will randomly expand in all directions until it reaches volume or
 * cannot expand further. Specify the RadiusType as Radius.DIAMOND for Manhattan distance (and the best results),
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean. You can specify a seed for the RNG and a fresh RNG will
 * be used for all random expansion; the RNG will reset to the specified seed after each generation so the same
 * CloudAOE can be used in different places by just changing the center. You can cause the CloudAOE to not reset after
 * generating each time by using setExpanding(true) and cause it to reset after the next generation by setting it back
 * to the default of false. If expanding is true, then multiple calls to getArea with the same center and larger volumes
 * will produce more coherent clumps of affected area with fewer gaps, and can be spaced out over multiple calls.
 *
 * This will produce doubles for its getArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidgrid.Spill to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class CloudAOE implements AOE {
    private Spill spill;
    private Point center;
    private int volume;
    private Spill.Measurement measurement;
    private long seed;
    private boolean expanding;
    private Radius rt;
    private char[][] dungeon;
    public CloudAOE(Point center, int volume, Radius radiusType)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }
    public CloudAOE(Point center, int volume, Radius radiusType, long rngSeed)
    {
        this.seed = rngSeed;
        this.spill = new Spill(new RNG(new LightRNG(rngSeed)));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }
    private CloudAOE()
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = new Point(1, 1);
        this.volume = 1;
        this.measurement = Spill.Measurement.MANHATTAN;
        rt = Radius.DIAMOND;
        this.expanding = false;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public Radius getRadiusType() {
        return rt;
    }

    public void setRadiusType(Radius radiusType) {
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }

    @Override
    public void shift(Point aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Point> targets) {
        for (Point p : targets)
        {
            if(rt.radius(center.x, center.y, p.x, p.y) <= Math.sqrt(volume) * 1.5)
                return true;
        }
        return false;
    }

    @Override
    public ArrayList<Point> idealLocations(Set<Point> targets, Set<Point> requiredExclusions) {
        if(targets == null)
            return new ArrayList<Point>();
        if(requiredExclusions == null) requiredExclusions = new LinkedHashSet<Point>();

        int totalTargets = targets.size();
        ArrayList<Point> bestPoints = new ArrayList<Point>(totalTargets * 8);

        if(totalTargets == 0 || volume <= 0)
            return bestPoints;

        if(volume == 1)
        {
            bestPoints.addAll(targets);
            return bestPoints;
        }
        Point[] ts = targets.toArray(new Point[targets.size()]);
        Point[] exs = targets.toArray(new Point[requiredExclusions.size()]);
        Point t = exs[0];

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];

        Spill sp;

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }

        for (int i = 0; i < exs.length; ++i, t = exs[i]) {
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    dungeonCopy[x][y] = (sp.spillMap[x][y]) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = ts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(spill.measurement == Spill.Measurement.CHEBYSHEV) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(spill.measurement == Spill.Measurement.EUCLIDEAN) dmm = DijkstraMap.Measurement.EUCLIDEAN;
        DijkstraMap dm = new DijkstraMap(dungeon, dmm);

        for (int i = 0; i < ts.length; ++i, t = ts[i]) {
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (sp.spillMap[x][y]) ? dm.physicalMap[x][y] : DijkstraMap.WALL;
                }
            }
            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] != '!') ? dm.gradientMap[x][y] : 99999.0;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }
        double bestQuality = 99999 * ts.length;
        double[][] qualityMap = new double[dungeon.length][dungeon[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0.0;
                for (int i = 0; i < ts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    bestQuality = qualityMap[x][y];
                    bestPoints.clear();
                    bestPoints.add(new Point(x, y));
                }
                else if(qualityMap[x][y] == bestQuality)
                {
                    bestPoints.add(new Point(x, y));
                }
            }
        }

        return bestPoints;
    }

/*
    @Override
    public ArrayList<ArrayList<Point>> idealLocations(Set<Point> targets, Set<Point> requiredExclusions) {
        int totalTargets = targets.size() + 1;
        int radius = Math.max(1, (int) (Math.sqrt(volume) * 1.5));
        ArrayList<ArrayList<Point>> locs = new ArrayList<ArrayList<Point>>(totalTargets);

        for(int i = 0; i < totalTargets; i++)
        {
            locs.add(new ArrayList<Point>(volume));
        }
        if(totalTargets == 1)
            return locs;
        double ctr = 0;
        if(radius < 1)
        {
            locs.get(totalTargets - 2).addAll(targets);
            return locs;
        }
        double tempRad;
        boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
        for (int x = 1; x < dungeon.length - 1; x += radius) {
            BY_POINT:
            for (int y = 1; y < dungeon[x].length - 1; y += radius) {
                for(Point ex : requiredExclusions)
                {
                    if(rt.radius(x, y, ex.x, ex.y) <= radius * 0.75)
                        continue BY_POINT;
                }
                ctr = 0;
                for(Point tgt : targets)
                {
                    tempRad = rt.radius(x, y, tgt.x, tgt.y);
                    if(tempRad < radius)
                        ctr += 1.0 - (tempRad / radius) * 0.5;
                }
                if(ctr >= 1)
                    locs.get((int)(totalTargets - ctr)).add(new Point(x, y));
            }
        }
        Point it;
        for(int t = 0; t < totalTargets - 1; t++)
        {
            if(locs.get(t).size() > 0) {
                int numPoints = locs.get(t).size();
                for (int i = 0; i < numPoints; i++) {
                    it = locs.get(t).get(i);
                    for (int x = Math.max(1, it.x - radius / 2); x < it.x + (radius + 1) / 2 && x < dungeon.length - 1; x++) {
                        BY_POINT:
                        for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
                        {
                            if(tested[x][y])
                                continue;
                            tested[x][y] = true;

                            for(Point ex : requiredExclusions)
                            {
                                if(rt.radius(x, y, ex.x, ex.y) <= radius * 0.75)
                                    continue BY_POINT;
                            }

                            ctr = 0;
                            for(Point tgt : targets)
                            {
                                tempRad = rt.radius(x, y, tgt.x, tgt.y);
                                if(tempRad < radius)
                                    ctr += 1.0 - (tempRad / radius) * 0.5;
                            }
                            if(ctr >= 1)
                                locs.get((int)(totalTargets - ctr)).add(new Point(x, y));
                        }
                    }
                }
            }
        }
        return locs;
    }
*/

    @Override
    public void setMap(char[][] map) {
        spill.initialize(map);
        this.dungeon = map;
    }

    @Override
    public HashMap<Point, Double> findArea() {
        spill.start(center, volume, null);
        HashMap<Point, Double> r = AreaUtils.arrayToHashMap(spill.spillMap);
        if(!expanding)
        {
            spill.reset();
            spill.lrng.setState(this.seed);
        }
        return r;
    }

    public boolean isExpanding() {
        return expanding;
    }

    public void setExpanding(boolean expanding) {
        this.expanding = expanding;
    }
}
