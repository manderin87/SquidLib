package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.FakeLanguageGen;
import squidpony.panel.IColoredString;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.SpatialMap;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.MixedGenerator;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class EverythingDemo extends ApplicationAdapter {
    private enum Phase {WAIT, PLAYER_ANIM, MONSTER_ANIM}
    private static class Monster {
        public AnimatedEntity entity;
        public int state;
        public Monster(Actor actor, int x, int y, int state)
        {
            entity = new AnimatedEntity(actor, x, y);
            this.state = state;
        }
        public Monster(AnimatedEntity ae, int state)
        {
            entity = ae;
            this.state = state;
        }
        public Monster change(int state)
        {
            this.state = state;
            return this;
        }
        public Monster change(AnimatedEntity ae)
        {
            entity = ae;
            return this;
        }
        public Monster move(int x, int y)
        {
            entity.gridX = x;
            entity.gridY = y;
            return this;
        }
    }
    SpriteBatch batch;

    private Phase phase = Phase.WAIT;
    private StatefulRNG rng;
    private SquidLayers display;
    private SquidMessageBox messages;
    /** Non-{@code null} iff '?' was pressed before */
    private /*Nullable*/ Actor help;
    private DungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] lights;
    private Color[][] colors, bgColors;
    private double[][] fovmap, pathMap;
    private AnimatedEntity player;
    private FOV fov;
    /** In number of cells */
    private int width;
    /** In number of cells */
    private int height;
    /** The pixel width of a cell */
    private int cellWidth;
    /** The pixel height of a cell */
    private int cellHeight;
    private SquidInput input;
    private double counter;
    private boolean[][] seen;
    private int health = 7;
    private SquidColorCenter fgCenter, bgCenter;
    private Color bgColor;
    private SpatialMap<Integer, Monster> monsters;
    private DijkstraMap getToPlayer, playerToCursor;
    private Stage stage;
    private int framesWithoutAnimation = 0;
    private Coord cursor;
    private ArrayList<Coord> toCursor;
    private ArrayList<Coord> awaitedMoves;
    private String lang;
    private SquidColorCenter[] colorCenters;
    private int currentCenter;
    private boolean changingColors = false;
    @Override
    public void create () {
        // gotta have a random number generator. We seed a LightRNG with any long we want, then pass that to an RNG.
        rng = new StatefulRNG(0xBADBEEFB0BBL);

        // for demo purposes, we allow changing the SquidColorCenter and the filter effect associated with it.
        // next, we populate the colorCenters array with the SquidColorCenters that will modify any colors we request
        // of them using the filter we specify. Only one SquidColorCenter will be used at any time for foreground, and
        // sometimes another will be used for background.
        colorCenters = new SquidColorCenter[18];
        // MultiLerpFilter here is given two colors to tint everything toward one of; this is meant to reproduce the
        // "Hollywood action movie poster" style of using primarily light orange (explosions) and gray-blue (metal).

        colorCenters[0] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.GAMBOGE_DYE, SColor.COLUMBIA_BLUE},
                new float[]{0.25f, 0.2f}
        ));
        colorCenters[1] = colorCenters[0];

        // MultiLerpFilter here is given three colors to tint everything toward one of; this is meant to look bolder.

        colorCenters[2] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.RED_PIGMENT, SColor.MEDIUM_BLUE, SColor.LIME_GREEN},
                new float[]{0.2f, 0.25f, 0.25f}
        ));
        colorCenters[3] = colorCenters[2];

        // ColorizeFilter here is given a slightly-grayish dark brown to imitate a sepia tone.

        colorCenters[4] = new SquidColorCenter(new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.7f, -0.05f));
        colorCenters[5] = new SquidColorCenter(new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.65f, 0.07f));

        // HallucinateFilter makes all the colors very saturated and move even when you aren't doing anything.

        colorCenters[6] = new SquidColorCenter(new Filters.HallucinateFilter());
        colorCenters[7] = colorCenters[6];

        // SaturationFilter here is used to over-saturate the colors slightly. Background is less saturated.

        colorCenters[8] = new SquidColorCenter(new Filters.SaturationFilter(1.35f));
        colorCenters[9] = new SquidColorCenter(new Filters.SaturationFilter(1.15f));

        // SaturationFilter here is used to de-saturate the colors slightly. Background is less saturated.

        colorCenters[10] = new SquidColorCenter(new Filters.SaturationFilter(0.7f));
        colorCenters[11] = new SquidColorCenter(new Filters.SaturationFilter(0.5f));

        // WiggleFilter here is used to randomize the colors slightly.

        colorCenters[12] = new SquidColorCenter(new Filters.WiggleFilter());
        colorCenters[13] = colorCenters[12];

        // SaturationFilter here is used to de-saturate the colors slightly. Background is less saturated.

        colorCenters[14] = new SquidColorCenter(new Filters.PaletteFilter(SColor.BLUE_GREEN_SERIES));
        colorCenters[15] = new SquidColorCenter(new Filters.PaletteFilter(SColor.ACHROMATIC_SERIES));

        colorCenters[16] = DefaultResources.getSCC();
        colorCenters[17] = colorCenters[16];

        fgCenter = colorCenters[16];
        bgCenter = colorCenters[17];
        currentCenter = 8;
        batch = new SpriteBatch();
        width = 80;
        height = 30;
        cellWidth = 11;
        cellHeight = 22;
        // the font will try to load Inconsolata-LGC as a bitmap font from resources.
        // this font is covered under the SIL Open Font License (fully free), so there's no reason it can't be used.
        display = new SquidLayers(width, height, cellWidth, cellHeight,
                new TextCellFactory().defaultNarrowDistanceFieldFont(), bgCenter, fgCenter);
        display.setAnimationDuration(0.03f);
        messages = new SquidMessageBox(width, 4, display.getTextFactory());
        stage = new Stage(new ScreenViewport(), batch);

        //These need to have their positions set before adding any entities if there is an offset involved.
        messages.setPosition(0, 0);
        display.setPosition(0, messages.getHeight());
        messages.appendWrappingMessage("Use numpad or vi-keys (hjklyubn) to move. Use ? for help, f to change colors, q to quit." +
                " Click the top or bottom border of this box to scroll.");
        counter = 0;

        dungeonGen = new DungeonGenerator(width, height, rng);
        dungeonGen.addWater(8, 6);
        dungeonGen.addGrass(5);
        dungeonGen.addBoulders(10);
        dungeonGen.addDoors(18, false);
        MixedGenerator mix = new MixedGenerator(width, height, rng);
        mix.putCaveCarvers(1);
        mix.putBoxRoomCarvers(1);
        mix.putRoundRoomCarvers(2);
        char[][] mg = mix.generate();
        decoDungeon = dungeonGen.generate(mg);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        //bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = dungeonGen.getBareDungeon();
        lineDungeon = DungeonUtility.hashesToLines(dungeonGen.getDungeon(), true);
        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        short[] placement = CoordPacker.pack(bareDungeon, '.');
        Coord pl = dungeonGen.utility.randomCell(placement);
        placement = CoordPacker.removePacked(placement, pl.x, pl.y);
        int numMonsters = 25;
        monsters = new SpatialMap<Integer, Monster>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = dungeonGen.utility.randomCell(placement);
            placement = CoordPacker.removePacked(placement, monPos.x, monPos.y);
            monsters.put(monPos, i, new Monster(display.animateActor(monPos.x, monPos.y, 'Я',
                    fgCenter.filter(display.getPalette().get(11))), 0));
        }
        // your choice of FOV matters here.
        fov = new FOV(FOV.RIPPLE_TIGHT);
        getToPlayer = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.CHEBYSHEV);
        getToPlayer.rng = rng;
        getToPlayer.setGoal(pl);
        pathMap = getToPlayer.scan(null);
        res = DungeonUtility.generateResistances(decoDungeon);
        fovmap = fov.calculateFOV(res, pl.x, pl.y, 8, Radius.SQUARE);

        player = display.animateActor(pl.x, pl.y, Character.forDigit(health, 10),
                fgCenter.filter(display.getPalette().get(30)));
        cursor = Coord.get(-1, -1);
        toCursor = new ArrayList<Coord>(10);
        awaitedMoves = new ArrayList<Coord>(10);
        playerToCursor = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.EUCLIDEAN);
        final int[][] initialColors = DungeonUtility.generatePaletteIndices(decoDungeon),
                initialBGColors = DungeonUtility.generateBGPaletteIndices(decoDungeon);
        colors = new Color[width][height];
        bgColors = new Color[width][height];
        ArrayList<Color> palette = display.getPalette();
        bgColor = SColor.DARK_SLATE_GRAY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                colors[i][j] = palette.get(initialColors[i][j]);
                bgColors[i][j] = palette.get(initialBGColors[i][j]);
            }
        }
        lights = DungeonUtility.generateLightnessModifiers(decoDungeon, counter);
        seen = new boolean[width][height];
        lang = FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 4, 6, new String[]{",", ",", ",", " -"},
                new String[]{"..."}, 0.25);
        // this is a big one.
        // SquidInput can be constructed with a KeyHandler (which just processes specific keypresses), a SquidMouse
        // (which is given an InputProcessor implementation and can handle multiple kinds of mouse move), or both.
        // keyHandler is meant to be able to handle complex, modified key input, typically for games that distinguish
        // between, say, 'q' and 'Q' for 'quaff' and 'Quip' or whatever obtuse combination you choose. The
        // implementation here handles hjklyubn keys for 8-way movement, numpad for 8-way movement, arrow keys for
        // 4-way movement, and wasd for 4-way movement. Shifted letter keys produce capitalized chars when passed to
        // KeyHandler.handle(), but we don't care about that so we just use two case statements with the same body,
        // one for the lower case letter and one for the upper case letter.
        // You can also set up a series of future moves by clicking within FOV range, using mouseMoved to determine the
        // path to the mouse position with a DijkstraMap (called playerToCursor), and using touchUp to actually trigger
        // the event when someone clicks.
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W':
                    {
                        move(0, -1);
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S':
                    {
                        move(0, 1);
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A':
                    {
                        move(-1, 0);
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D':
                    {
                        move(1, 0);
                        break;
                    }

                    case SquidInput.UP_LEFT_ARROW:
                    case 'y':
                    case 'Y':
                    {
                        move(-1, -1);
                        break;
                    }
                    case SquidInput.UP_RIGHT_ARROW:
                    case 'u':
                    case 'U':
                    {
                        move(1, -1);
                        break;
                    }
                    case SquidInput.DOWN_RIGHT_ARROW:
                    case 'n':
                    case 'N':
                    {
                        move(1, 1);
                        break;
                    }
                    case SquidInput.DOWN_LEFT_ARROW:
                    case 'b':
                    case 'B':
                    {
                        move(-1, 1);
                        break;
                    }
                    case '?': {
                    	toggleHelp();
                        break;
                    }
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                        break;
                    }
                    case 'f':
                    case 'F':
                    {
                        currentCenter = (currentCenter + 1) % 9;
                        // idx is 3 when we use the HallucinateFilter, which needs special work
                        changingColors = currentCenter == 3;
                        fgCenter = colorCenters[currentCenter * 2];
                        bgCenter = colorCenters[currentCenter * 2 + 1];
                        display.setFGColorCenter(fgCenter);
                        display.setBGColorCenter(bgCenter);
                        break;
                    }
                }
            }
        }, new SquidMouse(cellWidth, cellHeight, width, height, 0, 0, new InputAdapter() {

            // if the user clicks within FOV range and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(fovmap[screenX][screenY] > 0.0 && awaitedMoves.isEmpty()) {
                    if (toCursor.isEmpty()) {
                        cursor = Coord.get(screenX, screenY);
                        //Uses DijkstraMap to get a path. from the player's position to the cursor
                        toCursor = playerToCursor.findPath(30, null, null, Coord.get(player.gridX, player.gridY), cursor);
                    }
                    awaitedMoves = new ArrayList<>(toCursor);
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return mouseMoved(screenX, screenY);
            }

            // causes the path to the mouse position to become highlighted (toCursor contains a list of points that
            // receive highlighting). Uses DijkstraMap.findPath() to find the path, which is surprisingly fast.
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if(!awaitedMoves.isEmpty())
                    return false;
                if(cursor.x == screenX && cursor.y == screenY)
                {
                    return false;
                }
                if(fovmap[screenX][screenY] > 0.0) {
                    cursor = Coord.get(screenX, screenY);
                    //Uses DijkstraMap to get a path. from the player's position to the cursor
                    toCursor = playerToCursor.findPath(30, null, null, Coord.get(player.gridX, player.gridY), cursor);
                }
                return false;
            }
        }));
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        // and then add display and messages, our two visual components, to the list of things that act in Stage.
        stage.addActor(display);
        stage.addActor(messages);

    }
    /**
     * Move the player or open closed doors, remove any monsters the player bumped, then update the DijkstraMap and
     * have the monsters that can see the player try to approach.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod
     * @param ymod
     */
    private void move(int xmod, int ymod) {
    	clearHelp();

        if(health <= 0) return;

        int newX = player.gridX + xmod, newY = player.gridY + ymod;
        if (newX >= 0 && newY >= 0 && newX < width && newY < height
                && bareDungeon[newX][newY] != '#')
        {
            // '+' is a door.
            if (lineDungeon[newX][newY] == '+') {
                decoDungeon[newX][newY] = '/';
                lineDungeon[newX][newY] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(decoDungeon);
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, player.gridX, player.gridY, 8, Radius.SQUARE);

            } else {
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, newX, newY, 8, Radius.SQUARE);
                display.slide(player, newX, newY);
                monsters.remove(Coord.get(newX, newY));
            }

            phase = Phase.PLAYER_ANIM;
        }
    }

    // check if a monster's movement would overlap with another monster.
    private boolean checkOverlap(Monster mon, int x, int y, ArrayList<Coord> futureOccupied)
    {
        if(monsters.containsPosition(Coord.get(x, y)) && !mon.equals(monsters.get(Coord.get(x, y))))
            return true;
        for(Coord p : futureOccupied)
        {
            if(x == p.x && y == p.y)
                return true;
        }
        return false;
    }

    private void postMove()
    {

        phase = Phase.MONSTER_ANIM;
        // The next two lines are important to avoid monsters treating cells the player WAS in as goals.
        getToPlayer.clearGoals();
        getToPlayer.resetMap();
        // now that goals are cleared, we can mark the current player position as a goal.
        getToPlayer.setGoal(player.gridX, player.gridY);
        // this is an important piece of DijkstraMap usage; the argument is a Set of Points for squares that
        // temporarily cannot be moved through (not walls, which are automatically known because the map char[][]
        // was passed to the DijkstraMap constructor, but things like moving creatures and objects).
        LinkedHashSet<Coord> monplaces = monsters.positions();

        pathMap = getToPlayer.scan(monplaces);

        // recalculate FOV, store it in fovmap for the render to use.
        fovmap = fov.calculateFOV(res, player.gridX, player.gridY, 8, Radius.SQUARE);
        // handle monster turns
        ArrayList<Coord> nextMovePositions = new ArrayList<Coord>(25);
        for(Coord pos : monsters.positions())
        {
            Monster mon = monsters.get(pos);
            // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
            if(mon.state > 0 || fovmap[pos.x][pos.y] > 0.1)
            {
                if(mon.state == 0)
                {
                    messages.appendMessage("The AЯMED GUAЯD shouts at you, \"" +
                            FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 1, 3,
                            new String[]{",", ",", ",", " -"}, new String[]{"!"}, 0.25) + "\"");
                }
                // this block is used to ensure that the monster picks the best path, or a random choice if there
                // is more than one equally good best option.
                Direction choice = null;
                double best = 9999.0;
                for(Direction d : rng.shuffle(Direction.OUTWARDS))
                {
                    Coord tmp = pos.translate(d);
                    if(pathMap[tmp.x][tmp.y] < best &&
                            !checkOverlap(mon, tmp.x, tmp.y, nextMovePositions))
                    {
                        // pathMap is a 2D array of doubles where 0 is the goal (the player).
                        // we use best to store which option is closest to the goal.
                        best = pathMap[tmp.x][tmp.y];
                        choice = d;
                    }
                }
                if(choice != null) {
                    Coord tmp = pos.translate(choice);
                    // if we would move into the player, instead damage the player and give newMons the current
                    // position of this monster.
                    if (tmp.x == player.gridX && tmp.y == player.gridY) {
                        display.wiggle(player);
                        health--;
                        player.setText("" + health);
                        monsters.positionalModify(pos, mon.change(1));
                    }
                    // otherwise store the new position in newMons.
                    else {
                        /*if (fovmap[mon.getKey().x][mon.getKey().y] > 0.0) {
                            display.put(mon.getKey().x, mon.getKey().y, 'M', 11);
                        }*/
                        nextMovePositions.add(tmp);
                        monsters.positionalModify(pos, mon.change(1));
                        monsters.move(pos, tmp);
                        display.slide(mon.entity, tmp.x, tmp.y);

                    }
                }
                else
                {
                    monsters.positionalModify(pos, mon.change(1));
                }
            }
        }

    }

    private void toggleHelp() {
        if(help != null)
        {
            clearHelp();
            return;
        }
        final int nbMonsters = monsters.size();

		/* Prepare the String to display */
		final IColoredString<Color> cs = new IColoredString.Impl<Color>();
		cs.append("Still ", null);
        final Color nbColor;
        if (nbMonsters <= 1)
            /* Green */
            nbColor = Color.GREEN;
        else if (nbMonsters <= 5)
            /* Orange */
            nbColor = Color.ORANGE;
        else
            /* Red */
            nbColor = Color.RED;
        cs.appendInt(nbMonsters, nbColor);
        cs.append(String.format(" monster%s to kill", nbMonsters == 1 ? "" : "s"), null);

        IColoredString<Color> helping1 = new IColoredString.Impl<Color>("Use numpad or vi-keys (hjklyubn) to move.", Color.WHITE);
        IColoredString<Color> helping2 = new IColoredString.Impl<Color>("Use ? for help, f to change colors, q to quit.", Color.WHITE);
        IColoredString<Color> helping3 = new IColoredString.Impl<Color>("Click the top or bottom border of the lower message box to scroll.", Color.WHITE);

		/* The panel's width */
        final int w = Math.max(helping3.length(), cs.length());
		/* The panel's height. */
        final int h = 5;
        final SquidPanel bg = new SquidPanel(w, h, display.getTextFactory());
        final SquidPanel fg = new SquidPanel(w, h, display.getTextFactory());
        final GroupCombinedPanel<Color> gcp = new GroupCombinedPanel<Color>();
		/*
		 * We're setting them late just for the demo, as it avoids giving 'w'
		 * and 'h' at construction time.
		 */
		gcp.setPanels(bg, fg);

		/*
		 * Set the position (the center), using libgdx's 'setPosition'
		 * method, that takes the bottom left corner as input.
		 */
		gcp.setPosition(((width / 2) - (w / 2)) * cellWidth, (height / 2) * cellHeight);

		/* Fill the background with some grey */
		gcp.fillBG(new Color(0.3f, 0.3f, 0.3f, 0.9f));

		/* Now, to set the text we have to follow SquidPanel's convention */
		/* First 0: align left, second 0: first line */
        gcp.putFG(0, 0, cs);

		/* 0: align left, 2: third line */
        gcp.putFG(0, 2, helping1);
        gcp.putFG(0, 3, helping2);
        gcp.putFG(0, 4, helping3);

        help = gcp;

		stage.addActor(gcp);
	}

	private void clearHelp() {
		if (help == null)
			/* Nothing to do */
			return;
		help.clear();
		stage.getActors().removeValue(help, true);
        help = null;
	}

    public void putMap()
    {
        boolean overlapping = false;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                overlapping = monsters.containsPosition(Coord.get(i,j)) || (player.gridX == i && player.gridY == j);
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +215 lightness and 0.0 being rather dark at -105.
                if (fovmap[i][j] > 0.0) {
                    seen[i][j] = true;
                    display.put(i, j, (overlapping) ? ' ' : lineDungeon[i][j], fgCenter.filter(colors[i][j]), bgCenter.filter(bgColors[i][j]),
                            lights[i][j] + (int) (-105 + 320 * fovmap[i][j]));
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                } else if (seen[i][j]) {
                    display.put(i, j, lineDungeon[i][j], fgCenter.filter(colors[i][j]), bgCenter.filter(bgColors[i][j]), -140);
                }
            }
        }
        for (Coord pt : toCursor)
        {
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.highlight(pt.x, pt.y, lights[pt.x][pt.y] + (int) (170 * fovmap[pt.x][pt.y]));
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);

        // used as the z-axis when generating Simplex noise to make water seem to "move"
        counter += Gdx.graphics.getDeltaTime() * 15;
        // this does the standard lighting for walls, floors, etc. but also uses counter to do the Simplex noise thing.
        lights = DungeonUtility.generateLightnessModifiers(decoDungeon, counter);

        // you done bad. you done real bad.
        if (health <= 0) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(width / 2 - 18, height / 2 - 10, "   THE TSAR WILL HAVE YOUR HEAD!    ");
            display.putBoxedString(width / 2 - 18, height / 2 - 5,  "      AS THE OLD SAYING GOES,       ");
            display.putBoxedString(width / 2 - lang.length() / 2, height / 2, lang);
            display.putBoxedString(width / 2 - 18, height / 2 + 5,  "             q to quit.             ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if(input.hasNext())
                input.next();
            return;
        }
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            if(!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 3) {
                    framesWithoutAnimation = 0;
                    switch (phase) {
                        case WAIT:
                        case MONSTER_ANIM:
                            Coord m = awaitedMoves.remove(0);
                            toCursor.remove(0);
                            move(m.x - player.gridX, m.y - player.gridY);
                            break;
                        case PLAYER_ANIM:
                            postMove();
                            break;
                    }
                }
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if(input.hasNext() && !display.hasActiveAnimations() && phase == Phase.WAIT) {
            input.next();
        }
        // if the previous blocks didn't happen, and there are no active animations, then either change the phase
        // (because with no animations running the last phase must have ended), or start a new animation soon.
        else if(!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 3) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case WAIT:
                        break;
                    case MONSTER_ANIM: {
                        phase = Phase.WAIT;
                    }
                    break;
                    case PLAYER_ANIM: {
                        postMove();

                    }
                }
            }
        }
        // if we do have an animation running, then how many frames have passed with no animation needs resetting
        else
        {
            framesWithoutAnimation = 0;
        }

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();
        if(help == null) {
            // disolay does not draw all AnimatedEntities by default, since FOV often changes how they need to be drawn.
            batch.begin();
            // the player needs to get drawn every frame, of course.
            display.drawActor(batch, 1.0f, player);
            for (Monster mon : monsters) {
                // monsters are only drawn if within FOV.
                if (fovmap[mon.entity.gridX][mon.entity.gridY] > 0.0) {
                    display.drawActor(batch, 1.0f, mon.entity);
                }
            }
            // batch must end if it began.
            batch.end();
        }
        // if using a filter that changes each frame, clear the known relationship between requested and actual colors
        if(changingColors) {
            fgCenter.clearCache();
            bgCenter.clearCache();
        }
    }

    @Override
	public void resize(int width, int height) {
		super.resize(width, height);
		input.getMouse().reinitialize((float) width / this.width, (height - messages.getHeight()) / this.height, this.width, this.height, 0, 0);
	}
}
