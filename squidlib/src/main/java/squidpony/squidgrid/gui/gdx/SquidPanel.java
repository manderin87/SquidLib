package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import squidpony.ArrayTools;
import squidpony.IColorCenter;
import squidpony.panel.IColoredString;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Displays text and images in a grid pattern. Supports basic animations.
 * 
 * Grid width and height settings are in terms of number of cells. Cell width and height
 * are in terms of number of pixels.
 *
 * When text is placed, the background color is set separately from the foreground character. When moved, only the
 * foreground character is moved.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SquidPanel extends Group implements ISquidPanel<Color> {

    public float DEFAULT_ANIMATION_DURATION = 0.12F;
    protected int animationCount = 0;
    protected Color defaultForeground = Color.WHITE;
    protected IColorCenter<Color> scc;
    protected final int cellWidth, cellHeight;
    protected int gridWidth, gridHeight, gridOffsetX = 0, gridOffsetY = 0;
    protected final String[][] contents;
    protected final float[][] colors;
    protected Color lightingColor = SColor.WHITE, tmpColor = new Color();
    protected final TextCellFactory textFactory;
    protected float xOffset, yOffset;
    public final OrderedSet<AnimatedEntity> animatedEntities;
    /**
     * For thin-wall maps, where only cells where x and y are both even numbers have backgrounds displayed.
     * Should be false when using this SquidPanel for anything that isn't specifically a background of a map
     * that uses the thin-wall method from ThinDungeonGenerator or something similar. Even the foregrounds of
     * thin-wall maps should have this false, since ThinDungeonGenerator (in conjunction with DungeonUtility's
     * hashesToLines method) makes thin lines for walls that should be displayed as between the boundaries of
     * other cells. The overlap behavior needed for some "thin enough" cells to be displayed between the cells
     * can be accomplished by using {@link #setTextSize(int, int)} to double the previously-given cell width
     * and height.
     */
    public boolean onlyRenderEven = false;

    /**
     * Creates a bare-bones panel with all default values for text rendering.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     */
    public SquidPanel(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont());
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default square font.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public SquidPanel(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont().width(cellWidth).height(cellHeight).initBySize());
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory) {
        this(gridWidth, gridHeight, factory, DefaultResources.getSCC());
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center
     * 			The color center to use. Can be {@code null}, but then must be set later on with
     *          {@link #setColorCenter(IColorCenter)}.
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center) {
        this(gridWidth, gridHeight, factory, center, 0f, 0f);
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center
     * 			The color center to use. Can be {@code null}, but then must be set later on with
     *          {@link #setColorCenter(IColorCenter)}.
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center,
                      float xOffset, float yOffset) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        if(center == null)
            scc = DefaultResources.getSCC();
        else
            scc = center;

        if (factory == null) {
            textFactory = new TextCellFactory();
        }
        else
            textFactory = factory;
        if (!textFactory.initialized()) {
            textFactory.initByFont();
        }

        cellWidth = MathUtils.round(textFactory.actualCellWidth);
        cellHeight = MathUtils.round(textFactory.actualCellHeight);

        contents = new String[gridWidth][gridHeight];
        colors = ArrayTools.fill(scc.filter(Color.CLEAR).toFloatBits(), gridWidth, gridHeight);

        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        setSize(w, h);
        animatedEntities = new OrderedSet<>();
    }

    /**
     * Places the given characters into the grid starting at 0,0.
     *
     * @param chars
     */
    public void put(char[][] chars) {
        put(0, 0, chars);
    }

	@Override
	public void put(/* @Nullable */char[][] chars, Color[][] foregrounds) {
		if (chars == null) {
			/* Only colors to put */
			final int width = foregrounds.length;
			final int height = width == 0 ? 0 : foregrounds[0].length;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++)
					put(x, y, foregrounds[x][y]);
			}
		} else
			put(0, 0, chars, foregrounds);
	}

    public void put(char[][] chars, int[][] indices, ArrayList<Color> palette) {
        put(0, 0, chars, indices, palette);
    }

    public void put(int xOffset, int yOffset, char[][] chars) {
        put(xOffset, yOffset, chars, defaultForeground);
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, int[][] indices, ArrayList<Color> palette) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + foregrounds.length; x++) {
            for (int y = yOffset; y < yOffset + foregrounds[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, '\0', foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, int[][] indices, ArrayList<Color> palette) {
        for (int x = xOffset; x < xOffset + indices.length; x++) {
            for (int y = yOffset; y < yOffset + indices[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, '\0', palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color foreground) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foreground);
                }
            }
        }
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * Will use the default color for this component to draw the characters.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     */
    public void put(int xOffset, int yOffset, String string) {
        put(xOffset, yOffset, string, defaultForeground);
    }

	@Override
	public void put(int xOffset, int yOffset, IColoredString<? extends Color> cs) {
		int x = xOffset;
		for (IColoredString.Bucket<? extends Color> fragment : cs) {
			final String s = fragment.getText();
			final Color color = fragment.getColor();
			put(x, yOffset, s, color == null ? getDefaultForegroundColor() : scc.filter(color));
			x += s.length();
		}
	}

    @Override
    public void put(int xOffset, int yOffset, String string, Color foreground) {
        if(string == null || string.isEmpty())
            return;
        if (string.length() == 1) {
            put(xOffset, yOffset, string.charAt(0), foreground);
        }
        else
        {
            float enc = scc.filter(foreground).toFloatBits();
            for (int i = 0; i < string.length(); i++) {
                put(xOffset + i, yOffset, string.charAt(i), enc);
            }
        }
    }
    public void put(int xOffset, int yOffset, String string, Color foreground, float colorMultiplier) {
        if (string.length() == 1) {
            put(xOffset, yOffset, string.charAt(0), foreground, colorMultiplier);
        }
        else
        {
            for (int i = 0; i < string.length(); i++) {
                put(xOffset + i, yOffset, string.charAt(i), foreground, colorMultiplier);
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color foreground, float colorMultiplier) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foreground, colorMultiplier);
                }
            }
        }
    }

    /**
     * Puts the given string horizontally or optionally vertically, with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * Will use the default color for this component to draw the characters.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void put(int xOffset, int yOffset, String string, boolean vertical) {
        put(xOffset, yOffset, string, defaultForeground, vertical);
    }

    /**
     * Puts the given string horizontally or optionally vertically, with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param foreground the color to draw the characters
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void put(int xOffset, int yOffset, String string, Color foreground, boolean vertical) {
        if (vertical) {
            for (int i = 0; i < string.length(); i++) {
                put(xOffset, yOffset + i, string.charAt(i), foreground);
            }
        } else {
            put(xOffset, yOffset, string, foreground);
        }
    }

    /**
     * Puts the given string in the chosen direction, with the first character shown (not necessarily the first in the
     * string) at the given offset. If you use {@link Direction#LEFT}, then this effectively reverses the String and
     * prints it with the last character of the String at the minimum-x position, which is the same position that the
     * first character would be if you printed normally or if you gave this RIGHT (x is xOffset, y is yOffset). Giving
     * UP acts similarly to LEFT, but has the last character at the minimum-y position and has the first character below
     * it. The diagonals act as you would expect, combining the behavior of one of UP or DOWN with one of LEFT or RIGHT.
     * <br>
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param foreground the color to draw the characters
     * @param direction the direction the text should be written in, such as {@link Direction#RIGHT} for normal layout
     */
    public void put(int xOffset, int yOffset, String string, Color foreground, Direction direction) {
        float enc = scc.filter(foreground).toFloatBits();
        switch (direction)
        {
            case DOWN:
                for (int i = 0; i < string.length(); i++) {
                    put(xOffset, yOffset + i, string.charAt(i), enc);
                }
                break;
            case UP:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset, yOffset + p, string.charAt(i), enc);
                }
                break;
            case LEFT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + p, yOffset, string.charAt(i), enc);
                }
                break;
            case DOWN_RIGHT:
                for (int i = 0; i < string.length(); i++) {
                    put(xOffset + i, yOffset + i, string.charAt(i), enc);
                }
                break;
            case UP_RIGHT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + i, yOffset + p, string.charAt(i), enc);
                }
                break;
            case UP_LEFT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + p, yOffset + p, string.charAt(i), enc);
                }
                break;
            case DOWN_LEFT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + p, yOffset + i, string.charAt(i), enc);
                }
                break;
            default:
                for (int i = 0; i < string.length(); i++) {
                    put(xOffset + i, yOffset, string.charAt(i), enc);
                }
        }
    }

    /**
     * Erases the entire panel, leaving only a transparent space.
     */
    public void erase() {
        for (int i = 0; i < contents.length; i++) {
            Arrays.fill(contents[i], "\0");
            Arrays.fill(colors[i], 0f);
            /*
            for (int j = 0; j < contents[i].length; j++) {
                contents[i][j] = "\0";
                colors[i][j] = Color.CLEAR;
            }
            */
        }
    }

    @Override
	public void clear(int x, int y) {
        put(x, y, 0f);
    }

    @Override
    public void put(int x, int y, Color color) {
        put(x, y, '\0', color);
    }

    public void put(int x, int y, float encodedColor) {
        put(x, y, '\0', encodedColor);
    }

    public void put(int x, int y, Color color, float colorMultiplier) {
        put(x, y, '\0', color, colorMultiplier);
    }

    @Override
	public void put(int x, int y, char c) {
        put(x, y, c, defaultForeground);
    }

    /**
     * Takes a unicode codepoint for input.
     *
     * @param x
     * @param y
     * @param code
     */
    public void put(int x, int y, int code) {
        put(x, y, code, defaultForeground);
    }

    public void put(int x, int y, int c, Color color) {
        put(x, y, String.valueOf(Character.toChars(c)), color);
    }

    public void put(int x, int y, int index, ArrayList<Color> palette) {
        put(x, y, palette.get(index));
    }

    public void put(int x, int y, char c, int index, ArrayList<Color> palette) {
        put(x, y, c, palette.get(index));
    }

    /**
     * Takes a unicode char for input.
     *
     * @param x
     * @param y
     * @param c
     * @param color
     */
    @Override
    public void put(int x, int y, char c, Color color) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return;//skip if out of bounds
        }
        contents[x][y] = String.valueOf(c);
        colors[x][y] = scc.filter(color).toFloatBits();
    }
    /**
     * Takes a unicode char for input.
     *
     * @param x
     * @param y
     * @param c
     * @param encodedColor
     */
    public void put(int x, int y, char c, float encodedColor) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return;//skip if out of bounds
        }
        contents[x][y] = String.valueOf(c);
        colors[x][y] = encodedColor;
    }

	/**
     * Puts the given character at position x, y, with its color determined by the given color interpolated with
     * this SquidPanel's lightingColor (default is white light) by the amount specified by colorMultiplier (0.0
     * causes no change to the given color, 1.0 uses the lightingColor only, and anything between 0 and 1 will
     * produce some tint to color, and probably cache the produced color in the IColorCenter this uses).
	 */
	public void put(int x, int y, char c, Color color, float colorMultiplier) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return;//skip if out of bounds
        }
        contents[x][y] = String.valueOf(c);
		colors[x][y] = scc.lerp(color, lightingColor, colorMultiplier).toFloatBits();
	}

    @Override
	public int cellWidth() {
        return cellWidth;
    }

    @Override
	public int cellHeight() {
        return cellHeight;
    }

    @Override
	public int gridHeight() {
        return gridHeight;
    }

    @Override
	public int gridWidth() {
        return gridWidth;
    }

	/**
	 * @return The {@link TextCellFactory} backing {@code this}.
	 */
	public TextCellFactory getTextCellFactory() {
		return textFactory;
	}

    /**
     * Sets the size of the text in this SquidPanel (but not the size of the cells) to the given width and height in
     * pixels (which may be stretched by viewports later on, if your program uses them).
     * @param wide the width of a glyph in pixels
     * @param high the height of a glyph in pixels
     * @return this for chaining
     */
    public SquidPanel setTextSize(int wide, int high)
    {
        textFactory.tweakHeight(high).tweakWidth(wide).initBySize();
        //textFactory.setSmoothingMultiplier((3f + Math.max(cellWidth * 1f / wide, cellHeight * 1f / high)) / 4f);
        return this;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        textFactory.configureShader(batch);
        int inc = onlyRenderEven ? 2 : 1;
        for (int x = gridOffsetX; x < gridWidth; x += inc) {
            for (int y = gridOffsetY; y < gridHeight; y += inc) {
                textFactory.draw(batch, contents[x][y],
                        colors[x][y],
                        xOffset + /*- getX() + 1f * */ x * cellWidth,
                        yOffset + /*- getY() + 1f * */ (gridHeight - y) * cellHeight + 1f);
            }
        }
        super.draw(batch, parentAlpha);
        for(AnimatedEntity ae : animatedEntities)
        {
            ae.actor.act(Gdx.graphics.getDeltaTime());
        }
    }

    /**
     * Draws one AnimatedEntity, specifically the Actor it contains. Batch must be between start() and end()
     * @param batch Must have start() called already but not stop() yet during this frame.
     * @param parentAlpha This can be assumed to be 1.0f if you don't know it
     * @param ae The AnimatedEntity to draw; the position to draw ae is stored inside it.
     */
    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae)
    {
            ae.actor.draw(batch, parentAlpha);
    }

    @Override
	public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

	@Override
	public Color getDefaultForegroundColor() {
		return defaultForeground;
	}

    public AnimatedEntity getAnimatedEntityByCell(int x, int y) {
        for(AnimatedEntity ae : animatedEntities)
        {
            if(ae.gridX == x && ae.gridY == y)
                return ae;
        }
        return  null;
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given color.
     * @param x
     * @param y
     * @param c
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, char c, Color color)
    {
        return animateActor(x, y, false, String.valueOf(c), color);
        /*
        Actor a = textFactory.makeActor("" + c, color);
        a.setName("" + c);
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given color. If doubleWidth is true, treats
     * the char c as the left char to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param c
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, char c, Color color)
    {
        return animateActor(x, y, doubleWidth, String.valueOf(c), color);
        /*
        Actor a = textFactory.makeActor("" + c, color);
        a.setName("" + c);
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color.
     * @param x
     * @param y
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, Color color)
    {
        return animateActor(x, y, false, s, color);
        /*
        Actor a = textFactory.makeActor(s, color);
        a.setName(s);
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Color color)
    {
        Actor a = textFactory.makeActor(s, color);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Collection<Color> colors)
    {
        Actor a = textFactory.makeActor(s, colors);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Collection<Color> colors, float loopTime)
    {
        Actor a = textFactory.makeActor(s, colors, loopTime, doubleWidth);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using '^' as its contents, but as an image so it can be rotated.
     * Uses the given colors in a looping pattern, that doesn't count as an animation. If doubleWidth is true, treats
     * the '^' as starting in the middle of a 2-char cell.
     * @param x
     * @param y
     * @param doubleWidth
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity directionMarker(int x, int y, boolean doubleWidth, Collection<Color> colors, float loopTime)
    {
        Actor a = textFactory.makeDirectionMarker(colors, loopTime, doubleWidth);
        a.setName("^");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    public AnimatedEntity directionMarker(int x, int y, boolean doubleWidth, Color color)
    {
        Actor a = textFactory.makeDirectionMarker(color);
        a.setName("^");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the char c with a color looked up by index in palette.
     * @param x
     * @param y
     * @param c
     * @param index
     * @param palette
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, char c, int index, ArrayList<Color> palette)
    {
        return animateActor(x, y, c, palette.get(index));
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s with a color looked up by index in palette.
     * @param x
     * @param y
     * @param s
     * @param index
     * @param palette
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, int index, ArrayList<Color> palette)
    {
        return animateActor(x, y, s, palette.get(index));
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which will be
     * stretched to fit one cell.
     * @param x
     * @param y
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture)
    {
        return animateActor(x, y, false, texture, Color.WHITE);
        /*
        Actor a = textFactory.makeActor(texture, Color.WHITE);
        a.setName("");
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell.
     * @param x
     * @param y
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture, Color color)
    {
        return animateActor(x, y, false, texture, color);
        /*
        Actor a = textFactory.makeActor(texture, color);
        a.setName("");
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture)
    {
        return animateActor(x, y, doubleWidth, texture, Color.WHITE);
        /*
        Actor a = textFactory.makeActor(texture, Color.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);

        a.setName("");
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Color color) {
        Actor a = textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which, if and only
     * if stretch is true, will be stretched to fit one cell, or two cells if doubleWidth is true. If stretch is false,
     * this will preserve the existing size of texture.
     * @param x
     * @param y
     * @param doubleWidth
     * @param stretch
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture)
    {
        Actor a = (stretch)
                ? textFactory.makeActor(texture, Color.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, Color.WHITE, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which, if and only
     * if stretch is true, will be stretched to fit one cell, or two cells if doubleWidth is true. If stretch is false,
     * this will preserve the existing size of texture.
     * @param x
     * @param y
     * @param doubleWidth
     * @param stretch
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture, Color color) {

        Actor a = (stretch)
                ? textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, color, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
            */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Created an Actor from the contents of the given x,y position on the grid.
     * @param x
     * @param y
     * @return
     */
    public Actor cellToActor(int x, int y)
    {
        return cellToActor(x, y, false);
    }

    /**
     * Created an Actor from the contents of the given x,y position on the grid; deleting
     * the grid's String content at this cell.
     * 
     * @param x
     * @param y
     * @param doubleWidth
     * @return A fresh {@link Actor}, that has just been added to {@code this}.
     */
    public Actor cellToActor(int x, int y, boolean doubleWidth)
    {
    	return createActor(x, y, contents[x][y], colors[x][y], doubleWidth);
    }

    protected /* @Nullable */ Actor createActor(int x, int y, String name, Color color, boolean doubleWidth) {
        if (name == null || name.isEmpty())
            return null;
        else {
            final Actor a = textFactory.makeActor(name, scc.filter(color));
            a.setName(name);
            a.setPosition(adjustX(x, doubleWidth) - getX() * 2, adjustY(y) - getY() * 2);
            addActor(a);
            return a;
        }
    }

    protected /* @Nullable */ Actor createActor(int x, int y, String name, float encodedColor, boolean doubleWidth) {
        return createActor(x, y, name, Filters.Utility.colorFromFloat(tmpColor, encodedColor), doubleWidth);
    }

    public float adjustX(float x, boolean doubleWidth)
    {
        if(doubleWidth)
            return x * 2 * cellWidth + getX();
        else
            return x * cellWidth + getX();
    }

    public float adjustY(float y)
    {
        return (gridHeight - y - 1) * cellHeight + getY() + 1 + cellHeight - textFactory.actualCellHeight; // - textFactory.lineHeight //textFactory.lineTweak * 3f
        //return (gridHeight - y - 1) * cellHeight + textFactory.getDescent() * 3 / 2f + getY();
    }

    /*
    public void startAnimation(Actor a, int oldX, int oldY)
    {
        Coord tmp = Coord.get(oldX, oldY);

        tmp.x = Math.round(a.getX() / cellWidth);
        tmp.y = gridHeight - Math.round(a.getY() / cellHeight) - 1;
        if(tmp.x >= 0 && tmp.x < gridWidth && tmp.y > 0 && tmp.y < gridHeight)
        {
        }
    }
    */
    public void recallActor(Actor a, boolean restoreSym)
    {
        animationCount--;
        int x = Math.round((a.getX() - getX()) / cellWidth),
                y = gridHeight - (int)(a.getY() / cellHeight) - 1;
        if(onlyRenderEven)
        {
            // this just sets the least significant bit to 0, making any odd numbers even (decrementing)
            x &= -2;
            y &= -2;
        }
        if(x < 0 || y < 0 || x >= contents.length || y >= contents[x].length)
            return;
        if (restoreSym)
        	contents[x][y] = a.getName();
        removeActor(a);
    }
    public void recallActor(AnimatedEntity ae)
    {
        if(ae.doubleWidth)
            ae.gridX = Math.round((ae.actor.getX() - getX()) / (2 * cellWidth));
        else
            ae.gridX = Math.round((ae.actor.getX() - getX()) / cellWidth);
        ae.gridY = gridHeight - (int)((ae.actor.getY() - getY()) / cellHeight) - 1;
        if(onlyRenderEven)
        {
            // this just sets the least significant bit to 0, making any odd numbers even (decrementing)
            ae.gridX &= -2;
            ae.gridY &= -2;
        }
        ae.animating = false;
        animationCount--;
    }

    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param ae an AnimatedEntity returned by animateActor()
     * @param direction
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(final AnimatedEntity ae, Direction direction, float duration)
    {
        final Actor a = ae.actor;
        final float x = adjustX(ae.gridX, ae.doubleWidth),
                y = adjustY(ae.gridY);
        if(a == null || ae.animating) return;
        duration = clampDuration(duration);
        animationCount++;
        ae.animating = true;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(x + (direction.deltaX / 3F) * ((ae.doubleWidth) ? 2F : 1F), y + direction.deltaY / 3F,
                        Align.center, duration * 0.35F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
                    }
                }))));

    }
    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param x
     * @param y
     * @param direction
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(int x, int y, Direction direction, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        duration = clampDuration(duration);
        animationCount++;
        float nextX = adjustX(x, false), nextY = adjustY(y);
        /*
        x *= cellWidth;
        y = (gridHeight - y - 1);
        y *= cellHeight;
        y -= 1;
        x +=  getX();
        y +=  getY();
        */
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX + direction.deltaX / 3F, nextY + direction.deltaY / 3F,
                        Align.center, duration * 0.35F),
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a, true);
                    }
                }))));

    }

    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param x
     * @param y
     * @param direction
     */
    public void bump(int x, int y, Direction direction) {
        bump(x, y, direction, DEFAULT_ANIMATION_DURATION);
    }
    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param location
     * @param direction
     */
    public void bump(Coord location, Direction direction) {
        bump(location.x, location.y, direction, DEFAULT_ANIMATION_DURATION);
    }
    /**
     * Start a movement animation for the object at the grid location x, y and moves it to newX, newY over a number of
     * seconds given by duration (often 0.12f or somewhere around there).
     * @param ae an AnimatedEntity returned by animateActor()
     * @param newX
     * @param newY
     * @param duration
     */
    public void slide(final AnimatedEntity ae, int newX, int newY, float duration)
    {
        final Actor a = ae.actor;
        final float nextX = adjustX(newX, ae.doubleWidth), nextY = adjustY(newY);
        if(a == null || ae.animating) return;
        duration = clampDuration(duration);
        animationCount++;
        ae.animating = true;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
                    }
                }))));
    }

    /**
     * Start a movement animation for the object at the grid location x, y and moves it to newX, newY over a number of
     * seconds given by duration (often 0.12f or somewhere around there).
     * @param x
     * @param y
     * @param newX
     * @param newY
     * @param duration
     */
    public void slide(int x, int y, int newX, int newY, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        duration = clampDuration(duration);
        animationCount++;
        float nextX = adjustX(newX, false), nextY = adjustY(newY);

        /*
        newX *= cellWidth;
        newY = (gridHeight - newY - 1);
        newY *= cellHeight;
        newY -= 1;
        x += getX();
        y += getY();
        */
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a, true);
                    }
                }))));
    }

	/**
	 * Slides {@code name} from {@code (x,y)} to {@code (newx, newy)}. If
	 * {@code name} or {@code
	 * color} is {@code null}, it is picked from this panel (hereby removing the
	 * current name, if any).
	 * 
	 * @param x
	 *            Where to start the slide, horizontally.
	 * @param y
	 *            Where to start the slide, vertically.
	 * @param name
	 *            The name to slide, or {@code null} to pick it from this
	 *            panel's {@code (x,y)} cell.
	 * @param color
	 *            The color to use, or {@code null} to pick it from this panel's
	 *            {@code (x,y)} cell.
	 * @param newX
	 *            Where to end the slide, horizontally.
	 * @param newY
	 *            Where to end the slide, vertically.
	 * @param duration
	 *            The animation's duration.
	 */
	public void slide(int x, int y, final /* @Nullable */ String name, /* @Nullable */ Color color, int newX,
			int newY, float duration) {
	    slide(x, y, name, color, newX, newY, duration, null);
	}

    /**
     * Slides {@code name} from {@code (x,y)} to {@code (newx, newy)}. If
     * {@code name} or {@code color} is {@code null}, it is picked from this
     * panel (thereby removing the current name, if any). This also allows
     * a Runnable to be given as {@code postRunnable} to be run after the
     * slide completes.
     *
     * @param x
     *            Where to start the slide, horizontally.
     * @param y
     *            Where to start the slide, vertically.
     * @param name
     *            The name to slide, or {@code null} to pick it from this
     *            panel's {@code (x,y)} cell.
     * @param color
     *            The color to use, or {@code null} to pick it from this panel's
     *            {@code (x,y)} cell.
     * @param newX
     *            Where to end the slide, horizontally.
     * @param newY
     *            Where to end the slide, vertically.
     * @param duration
     *            The animation's duration.
     * @param postRunnable a Runnable to execute after the slide completes; may be null to do nothing.
     */
    public void slide(int x, int y, final /* @Nullable */ String name, /* @Nullable */ Color color, int newX,
                      int newY, float duration, /* @Nullable */ Runnable postRunnable) {
        final Actor a = createActor(x, y, name == null ? contents[x][y] : name,
                color == null ? Filters.Utility.colorFromFloat(tmpColor, colors[x][y]) : color, false);
        if (a == null)
            return;

        duration = clampDuration(duration);
        animationCount++;

        final int nbActions = 2 + (postRunnable == null ? 0 : 1);

        int index = 0;
        final Action[] sequence = new Action[nbActions];
        final float nextX = adjustX(newX, false);
        final float nextY = adjustY(newY);
        sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                recallActor(a, name == null);
            }
        }));

        a.addAction(Actions.sequence(sequence));
    }


    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     *
     * @param start Coord to pick up a tile from and slide
     * @param end Coord to end the slide on
     */
    public void slide(Coord start, Coord end) {
        slide(start.x, start.y, end.x, end.y, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed for one grid square in
     * the direction provided.
     *
     * @param start Coord to pick up a tile from and slide
     * @param direction Direction enum that indicates which way the slide should go
     */
    public void slide(Coord start, Direction direction) {
        slide(start.x, start.y, start.x + direction.deltaX, start.y + direction.deltaY, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided speed. The duration is
     * how many seconds should pass for the entire animation.
     *
     * @param start
     * @param end
     * @param duration
     */
    public void slide(Coord start, Coord end, float duration) {
        slide(start.x, start.y, end.x, end.y, duration);
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param ae an AnimatedEntity returned by animateActor()
     * @param duration
     */
    public void wiggle(final AnimatedEntity ae, float duration) {

        final Actor a = ae.actor;
        final float x = adjustX(ae.gridX, ae.doubleWidth), y = adjustY(ae.gridY);
        //final int x = ae.gridX * cellWidth * ((ae.doubleWidth) ? 2 : 1) + (int)getX(), y = (gridHeight - ae.gridY - 1) * cellHeight - 1 + (int)getY();
        if(a == null || ae.animating)
            return;
        duration = clampDuration(duration);
        ae.animating = true;
        animationCount++;
        StatefulRNG gRandom = DefaultResources.getGuiRandom();
        a.addAction(Actions.sequence(
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.2F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
                    }
                }))));
    }
    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param x
     * @param y
     * @param duration
     */
    public void wiggle(int x, int y, float duration) {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        duration = clampDuration(duration);
        animationCount++;
        float nextX = adjustX(x, false), nextY = adjustY(y);
        /*
        x *= cellWidth;
        y = (gridHeight - y - 1);
        y *= cellHeight;
        y -= 1;
        x +=  getX();
        y +=  getY();
        */
        StatefulRNG gRandom = DefaultResources.getGuiRandom();
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration * 0.2F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a, true);
                    }
                }))));
    }

    /**
     * Starts a tint animation for {@code ae} for the given {@code duration} in seconds.
     *
     * @param ae an AnimatedEntity returned by animateActor()
     * @param color what to transition ae's color towards, and then transition back from
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final AnimatedEntity ae, Color color, float duration) {
        final Actor a = ae.actor;
        if(a == null)
            return;
        duration = clampDuration(duration);
        ae.animating = true;
        animationCount++;
        Color ac = scc.filter(a.getColor());
        a.addAction(Actions.sequence(
                Actions.color(color, duration * 0.3f),
                Actions.color(ac, duration * 0.7f),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
                    }
                }))));
    }

    /**
	 * Like {@link #tint(int, int, Color, float)}, but waits for {@code delay}
	 * (in seconds) before performing it.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color what to transition ae's color towards, and then transition back from
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(float delay, int x, int y, Color color, float duration) {
        tint(delay, x, y, color, duration, null);
    }

    /**
     * Like {@link #tint(int, int, Color, float)}, but waits for {@code delay}
     * (in seconds) before performing it. Additionally, enqueue {@code postRunnable}
     * for running after the created action ends.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color what to transition ae's color towards, and then transition back from
     * @param duration how long the total "round-trip" transition should take in seconds
     * @param postRunnable a Runnable to execute after the tint completes; may be null to do nothing.
     */

    public void tint(float delay, int x, int y, Color color, float duration, Runnable postRunnable) {
        final Actor a = cellToActor(x, y);
        if(a == null)
            return;
        duration = clampDuration(duration);
        animationCount++;

        Color ac = scc.filter(a.getColor());

        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = Actions.color(color, duration * 0.3f);
        sequence[index++] = Actions.color(ac, duration * 0.7f);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
        /* Do this one last, so that hasActiveAnimations() returns true during 'postRunnable' */
        sequence[index] = Actions.run(new Runnable() {
            @Override
            public void run() {
                recallActor(a, true);
            }
        });

        a.addAction(Actions.sequence(sequence));
    }

    /**
	 * Starts a tint animation for the object at {@code (x,y)} for the given
	 * {@code duration} (in seconds).
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color
     * @param duration
     */
    public void tint(int x, int y, Color color, float duration) {
    	tint(0f, x, y, color, duration);
    }

	/**
	 * Fade the cell at {@code (x,y)} to {@code color}. Contrary to
	 * {@link #tint(int, int, Color, float)}, this action does not restore the
	 * cell's color at the end of its execution. This is for example useful to
	 * fade the game screen when the rogue dies.
	 *
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
	 * @param color
	 *            The color at the end of the fadeout.
	 * @param duration
	 *            The fadeout's duration.
	 */
	public void fade(int x, int y, Color color, float duration) {
		final Actor a = cellToActor(x, y);
		if (a == null)
			return;
        duration = clampDuration(duration);
		animationCount++;
		final Color c = scc.filter(color);
		a.addAction(Actions.sequence(Actions.color(c, duration), Actions.run(new Runnable() {
			@Override
			public void run() {
				recallActor(a, true);
			}
		})));
	}

    /**
     * Create a new Actor at (x, y) that looks like glyph but can rotate, and immediately starts changing color from
     * startColor to endColor and changing rotation from startRotation to endRotation, taking duration seconds to
     * complete before removing the Actor.
     * @param x the x position in cells; doesn't change
     * @param y the y position in cells; doesn't change
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int x, int y, char glyph, Color startColor, Color endColor,
                       float startRotation, float endRotation, float duration)
    {
        summon(x, y, x, y, glyph, startColor, endColor, false, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, sets its color, and immediately
     * starts changing position so it ends on the cell (endX, endY) and changing rotation from startRotation to
     * endRotation, taking duration seconds to complete before removing the Actor.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param color the Color of the glyph throughout the effect
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color color,
                       float startRotation, float endRotation, float duration)
    {
        summon(startX, startY, endX, endY, glyph, color, color, false, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but has the given rotation, and immediately starts
     * changing color from startColor to endColor, and changing position so it ends on the cell (endX, endY), taking
     * duration seconds to complete before removing the Actor.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param rotation the amount of rotation, in degrees, the glyph should have throughout the effect
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color startColor, Color endColor,
                       float rotation, float duration)
    {
        summon(startX, startY, endX, endY, glyph, startColor, endColor, false, rotation, rotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, and immediately starts changing
     * color from startColor to endColor, changing position so it ends on the cell (endX, endY), and changing rotation
     * from startRotation to endRotation, taking duration seconds to complete before removing the Actor.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color startColor, Color endColor,
                       float startRotation, float endRotation, float duration)
    {
        summon(startX, startY, endX, endY, glyph, startColor, endColor, false, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, and immediately starts changing
     * color from startColor to endColor, changing position so it ends on the cell (endX, endY), and changing rotation
     * from startRotation to endRotation, taking duration seconds to complete before removing the Actor. Allows
     * setting doubleWidth, which centers the created Actor in the space between the two glyphs in a cell.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param doubleWidth true if this uses double-width cells, false in most cases
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color startColor, Color endColor, boolean doubleWidth,
                       float startRotation, float endRotation, float duration)
    {
        summon(0f, startX, startY, endX, endY, glyph, startColor, endColor, doubleWidth, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, and immediately starts changing
     * color from startColor to endColor, changing position so it ends on the cell (endX, endY), and changing rotation
     * from startRotation to endRotation, taking duration seconds to complete before removing the Actor. Allows
     * setting doubleWidth, which centers the created Actor in the space between the two glyphs in a cell.
     * @param delay amount of time, in seconds, to wait before starting the effect
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param doubleWidth true if this uses double-width cells, false in most cases
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(float delay, int startX, int startY, int endX, int endY, char glyph,
                       Color startColor, Color endColor, boolean doubleWidth,
                       float startRotation, float endRotation, float duration)

    {
        duration = clampDuration(duration);
        animationCount++;
        final ColorChangeImage
                gi = textFactory.makeGlyphImage(glyph, scc.gradient(startColor, endColor, (int) (duration * 40)), duration * 1.1f, doubleWidth);
        gi.setPosition(adjustX(startX, doubleWidth) - getX() * 2, adjustY(startY) - getY() * 2);
        gi.setRotation(startRotation);
        addActor(gi);
        final int nbActions = 2 + (0 < delay ? 1 : 0);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = Actions.parallel(
                Actions.moveTo(adjustX(endX, doubleWidth) - getX() * 2, adjustY(endY) - getY() * 2, duration),
                Actions.rotateTo(endRotation, duration));
        sequence[index] = Actions.run(new Runnable() {
            @Override
            public void run() {
                recallActor(gi, false);
            }
        });

        gi.addAction(Actions.sequence(sequence));
    }
    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default, uses a "normal" amount of rotation for
     * for all of the actors (a value of 1f if you used another overload), and always uses an end color that is a
     * modified copy of startColor with 0 alpha (making the Actors all fade to transparent). The parameter
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param duration how long, in seconds, the effect should last
     */

    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor,
                      float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, startColor.cpy().sub(0,0,0,1), false, 1f, duration);
    }


    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default, and uses a "normal" amount of rotation
     * for all of the actors (a value of 1f if you used another overload). The parameter
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor, Color endColor,
                      float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, endColor, false, 1f, duration);
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default. Some parameters need explanation:
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor, Color endColor,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, endColor, false, rotationStrength, duration);
    }


    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * Some parameters need explanation: distance is how many cells away to move the created Actors away from (x,y);
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, boolean eightWay, char glyph,
                      Color startColor, Color endColor,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, distance, eightWay, glyph, startColor, endColor, false, rotationStrength, duration);
    }


    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default. Some parameters need explanation:
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param doubleWidth true if this should use the double-width-cell technique, false in most cases
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor, Color endColor, boolean doubleWidth,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, endColor, doubleWidth, rotationStrength, duration);
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * Some parameters need explanation: distance is how many cells away to move the created Actors away from (x,y);
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param doubleWidth true if this should use the double-width-cell technique, false in most cases
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, boolean eightWay, char glyph,
                      Color startColor, Color endColor, boolean doubleWidth,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, distance, eightWay, glyph, startColor, endColor, doubleWidth, rotationStrength, duration);
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * Some parameters need explanation: distance is how many cells away to move the created Actors away from (x,y);
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param delay amount of time, in seconds, to wait before starting the effect
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param doubleWidth true if this should use the double-width-cell technique, false in most cases
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(float delay, int x, int y, int distance, boolean eightWay, char glyph,
                      Color startColor, Color endColor, boolean doubleWidth,
                      float rotationStrength, float duration)
    {
        Direction d;
        if(eightWay)
        {
            for (int i = 0; i < 8; i++) {
                d = Direction.CLOCKWISE[i];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        glyph, startColor, endColor, doubleWidth,
                        45f * i, 45f * (i - rotationStrength),
                        duration);
            }
        }
        else
        {
            for (int i = 0; i < 4; i++) {
                d = Direction.CARDINALS_CLOCKWISE[i];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        glyph, startColor, endColor, doubleWidth,
                        90f * i, 90f * (i - rotationStrength),
                        duration);
            }

        }
    }

	@Override
    public boolean hasActiveAnimations() {
        //return animationCount != 0;
        if (0 < animationCount)
            return true;
        else
            return 0 < getActions().size;
    }

    public OrderedSet<AnimatedEntity> getAnimatedEntities() {
        return animatedEntities;
    }

    public void removeAnimatedEntity(AnimatedEntity ae)
    {
        animatedEntities.remove(ae);
    }

	@Override
	public ISquidPanel<Color> getBacker() {
		return this;
	}

	/**
	 * @return The current color center. Never {@code null}.
	 */
	public IColorCenter<Color> getColorCenter() {
		return scc;
	}

	/**
	 * Use this method if you use your own {@link IColorCenter} and want this
	 * panel not to allocate its own colors (or fill
	 * {@link DefaultResources#getSCC()} but instead to the provided center.
	 * 
	 * @param scc
	 *            The color center to use. Should not be {@code null}.
	 * @return {@code this}
	 * @throws NullPointerException
	 *             If {@code scc} is {@code null}.
	 */
	@Override
	public SquidPanel setColorCenter(IColorCenter<Color> scc) {
		if (scc == null)
			/* Better fail now than later */
			throw new NullPointerException(
					"The color center should not be null in " + getClass().getSimpleName());
		this.scc = scc;
		return this;
	}

    public String getAt(int x, int y)
    {
        if(contents[x][y] == null)
            return "";
        return contents[x][y];
    }
    public Color getColorAt(int x, int y)
    {
        return Filters.Utility.colorFromFloat(tmpColor, colors[x][y]);
    }

    public Color getLightingColor() {
        return lightingColor;
    }

    public void setLightingColor(Color lightingColor) {
        this.lightingColor = lightingColor;
    }

    protected float clampDuration(float duration) {
    	if (duration < 0.02f)
    		return 0.02f;
    	else
    		return duration;
    }

    /**
     * The X offset that the whole panel's internals will be rendered at.
     * @return the current offset in cells along the x axis
     */
    public int getGridOffsetX() {
        return gridOffsetX;
    }

    /**
     * Sets the X offset that the whole panel's internals will be rendered at.
     * @param gridOffsetX the requested offset in cells; should be less than gridWidth
     */
    public void setGridOffsetX(int gridOffsetX) {
        if(gridOffsetX < gridWidth)
            this.gridOffsetX = gridOffsetX;
    }

    /**
     * The Y offset that the whole panel's internals will be rendered at.
     * @return the current offset in cells along the y axis
     */
    public int getGridOffsetY() {
        return gridOffsetY;
    }

    /**
     * Sets the Y offset that the whole panel's internals will be rendered at.
     * @param gridOffsetY the requested offset in cells; should be less than gridHeight
     */
    public void setGridOffsetY(int gridOffsetY) {
        if(gridOffsetY < gridHeight)
            this.gridOffsetY = gridOffsetY;

    }

    /**
     * The number of cells along the x-axis that will be rendered of this panel.
     * @return the number of cells along the x-axis that will be rendered of this panel
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Sets the number of cells along the x-axis that will be rendered of this panel to gridWidth.
     * @param gridWidth the requested width in cells
     */
    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    /**
     * The number of cells along the y-axis that will be rendered of this panel
     * @return the number of cells along the y-axis that will be rendered of this panel
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * Sets the number of cells along the y-axis that will be rendered of this panel to gridHeight.
     * @param gridHeight the requested height in cells
     */
    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    /**
     * Sets the position of the actor's bottom left corner.
     *
     * @param x
     * @param y
     */
    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        setBounds(x, y, getWidth(), getHeight());
    }

    public float getxOffset() {
        return xOffset;
    }

    public void setOffsetX(float xOffset) {
        this.xOffset = xOffset;
    }

    public float getyOffset() {
        return yOffset;
    }

    public void setOffsetY(float yOffset) {
        this.yOffset = yOffset;
    }

    public void setOffsets(float x, float y) {
        xOffset = x;
        yOffset = y;
    }

    /**
     * Gets the status of a boolean flag used for rendering thin maps; it will almost always be false unless it
     * was set to true with {@link #setOnlyRenderEven(boolean)}.
     * <br>
     * This is meant for thin-wall maps, where only cells where x and y are both even numbers have backgrounds
     * displayed. Should be false when using this SquidPanel for anything that isn't specifically a background
     * of a map that uses the thin-wall method from ThinDungeonGenerator or something similar. Even the
     * foregrounds of thin-wall maps should have this false, since ThinDungeonGenerator (in conjunction with
     * DungeonUtility's hashesToLines() method) makes thin lines for walls that should be displayed as between
     * the boundaries of other cells. The overlap behavior needed for some "thin enough" cells to be displayed
     * between the cells can be accomplished by using {@link #setTextSize(int, int)} to double the
     * previously-given cell width and height.
     *
     * @return the current status of the onlyRenderEven flag, which defaults to false
     */
    public boolean getOnlyRenderEven() {
        return onlyRenderEven;
    }
    /**
     * Sets the status of a boolean flag used for rendering thin maps; it should almost always be the default,
     * which is false, unless you are using a thin-wall map, and then this should be true only if this
     * SquidPanel is used for the background layer.
     * <br>
     * This is meant for thin-wall maps, where only cells where x and y are both even numbers have backgrounds
     * displayed. Should be false when using this SquidPanel for anything that isn't specifically a background
     * of a map that uses the thin-wall method from ThinDungeonGenerator or something similar. Even the
     * foregrounds of thin-wall maps should have this false, since ThinDungeonGenerator (in conjunction with
     * DungeonUtility's hashesToLines() method) makes thin lines for walls that should be displayed as between
     * the boundaries of other cells. The overlap behavior needed for some "thin enough" cells to be displayed
     * between the cells can be accomplished by using {@link #setTextSize(int, int)} to double the
     * previously-given cell width and height.
     *
     * @param onlyRenderEven generally, should only be true if this SquidPanel is a background of a thin map
     */

    public void setOnlyRenderEven(boolean onlyRenderEven) {
        this.onlyRenderEven = onlyRenderEven;
    }
}
