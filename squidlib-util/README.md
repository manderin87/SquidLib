SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
--  

SquidLib is used for Wyrm, Epigon, Attack the Geth, Assault Fish, [Dungeon Mercenary](http://www.schplaf.org/hgames/), and other projects.

[![Join the slow-paced chat at https://gitter.im/SquidPony/SquidLib](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/SquidPony/SquidLib)

Documentation:
---
Jars of javadocs are distributed with each release via Maven Central, and with the current latest via JitPack. You can
get the docs and source of the latest version, 3.0.0-b8, in two parts for each; squidlib-util (the core of the library,
and also the largest part) has its
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0-b8/squidlib-util-3.0.0-b8-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0-b8/squidlib-util-3.0.0-b8-sources.jar),
while squidlib (the display part of the library, named the way it is because depending on squidlib should also pull in
squidlib-util to make it a "one-stop shop" dependency) has its
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0-b8/squidlib-3.0.0-b8-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0-b8/squidlib-3.0.0-b8-sources.jar).
The completely-optional squidlib-extra module (primarily used for serialization; relies on libGDX but doesn't use it for display) has its
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.0-b8/squidlib-extra-3.0.0-b8-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.0-b8/squidlib-extra-3.0.0-b8-sources.jar).

You can browse the javadocs of a recent commit (possibly newer than 3.0.0-b8, but no older)
[here for squidlib-util](http://squidpony.github.io/SquidLib/squidlib-util/index.html),
[here for squidlib](http://squidpony.github.io/SquidLib/squidlib/index.html), and
[here for squidlib-extra](http://squidpony.github.io/SquidLib/squidlib-extra/index.html), though the docs here are
updated whenever the project is rebuilt fully, which only coincides with releases occasionally.

Current Features:
--
###Ease Of Use
-   SquidLib is separated into multiple modules, so you can use the code for logic with or without the text-based display code
  -   There's squidlib-util for game logic that doesn't specify a display mode, squidlib for text-based display and input, and squidlib-extra for saving/loading data
-   Emphasis on cross-platform compatibility
  -   This means not just Windows, MacOS (formerly OS X), and Linux, but Android and HTML targets are important as well
  -   HTML targeting uses Google Web Toolkit (GWT) 2.6.x and is expected to update to GWT 2.8.0 when libGDX has a new release
    -   All the code in SquidLib is written with GWT compatibility in mind, and things we know are incompatible are marked as GwtIncompatible with an annotation
  -   Android targeting is known to work, but the UI conventions tend to be rather different (e.g. an 80x24 character
      console-style screen would be hard to navigate by touching a char on a phone with a small screen)
    -   We provide a class for simple on-screen controls in the display module, which can be dismissed (if you want) when a keyboard is available
  -   iOS is possibly a valid target, but no one has tried yet (likely due to the requirements of owning a computer
      running MacOS, owning an iPhone or iPad, and the not-insignificant fee to release a product into the App Store)
    -   The way to go is probably [mobidevelop's RoboVM fork](https://github.com/MobiDevelop/robovm) if you meet those requirements
-   The game logic code in squidlib-util includes a really wide variety of functionality, and emphasizes providing pre-made solutions to hard problems
-   The text display code in squidlib is meant to make prototyping easier, but also to make traditional roguelikes look nice
-   Standard GUI notation of (x,y) locations within the grid, used reliably throughout the library
  -   This is reinforced by the Coord class from squidlib-util, a simple 2D point that can be used throughout the library without producing data that needs garbage collection
-   Standard usage of common code, with inheritance and interfaces used where appropriate
  -   The best example of this is RNG, a class or generating random numbers, which can use a wide variety of algorithms by taking a RandomnessSource implementation as a constructor argument.
  -   RNG is used throughout SquidLib as a standard tool for randomness, and tends to be better for this than java.util.Random.

###Display features
-   Uses the scene2d.ui classes from libGDX to display text/images and handle input
-   Images may be used alongside characters in same panel
  -  Characters can be used as a drop-in fallback mechanism!
-   Several fonts provided as resources, some narrow, some square, for unicode line drawing to work out-of-the-box
  -   Multiple fonts use a distance field effect, allowing them to be scaled and stretched smoothly
  -   The distance field fonts are difficult to make yourself, so lots are provided in the assets folder or zip file.
-   Multiple grids of different configurations can be used simultaneously in the same display
-   Multiple grids of different configurations can be overlaid allowing for transparency effects
  -   A convenience class, SquidLayers provides foreground and background setting with this
  -   SquidLayers also allows background brightness changes (such as from torchlight) with just an int argument
-   Robust libGDX animation support, from simple sliding to "burst" effects that create rotating chars spreading out of a cell
-   Starting with 3.0.0 beta 5, there is support for variable-width fonts in various widgets, which can be much more legible
-   Can create multiple overlapping layers
  -   Basic foreground & background color differences per cell
    -  Using a partially-transparent, full-cell tile in an overlay can change the colors below it
  -   Multiple layers can be used to have multiple characters in a single cell
  -   Multiple different sized layers can be used for sub-cell effects
  -   Overlays can be used to show animation effects without disturbing the display
  -   Overlays can be used to show potential Areas of Effect or Ranges
  -   A tint animation can be used to highlight an area or cell without using overlays
-   Over 2000 icons from http://game-icons.net are in the optional assets, with a distance field effect applied so they can be used along with "stretchable" fonts, and all in a texture atlas for efficient lookup

####Lots of Color
-   SColor class extends libGDX's Color and can be used as a drop-in replacement for any usage of that class
-   Over 500 named colors
-   Each named color has a sample of its appearance in the Javadoc against multiple backgrounds
-   HTML browsing of the API shows these samples
-   Pop-up javadoc in NetBeans, IntelliJ, and Eclipse show these samples
-   Can get a list of colors that are a gradient between two colors
-   Can perform LIBTCOD style "dark", "light", and "desaturate" commands on any color
-   Can get an arbitrary amount of blend between two colors
-   Starting with 3.0.0 beta 2, you can alter colors automatically using Filters
-   Starting with 3.0.0 beta 5, lots of options are available for generating gradients, including with Filters, and gradients that wrap around like a rainbow
-   In recent betas (7 onward), there's a filter that adjusts the brightness of red and green to help players with common forms of red-green colorblindness distinguish game objects that would otherwise be too similar
-   SquidColorCenter (and potentially other IColorCenter implementations) allow finding and adjusting the hue, saturation, and value of colors

###Roguelike-Intended Toolkit
-   Robust Field of View and Line of Sight system
  -  Includes multiple options to fit the desired level of permissiveness for FOV and LOS
  -  Can handle directional FOV by simply specifying an angle and a span in degrees to cover with the FOV
-   Sound propagation system that can be used like Line of Sight, but for sounds that echo and pass through walls
-   Spill class implements randomized flood-fill, useful for spreading gases and other fluids
-   Splash is an easy-to-use implementation of randomized flood-fill for when you don't need all of Spill's features or bloat; it's new in 3.0.0 beta 3.
-   MultiSpill produces, as you might expect, multiple randomized flood-fills that do not overlap, and can fill at different rates
  -   This in turn is used by SpillWorldMap to generate non-realistic but interesting-for-gameplay world maps, where land masses
      are flood-filled at the same time as water and their interaction produces nice shapes
-   The various AOE implementations help NPCs figure out how to position area-of-effect attacks when there are multiple characters they want to affect
-   ZOI is a class for determining zones of influence, using multiple influencing cells and finding the areas that are closer
    to one cell than than are to others (it also can detect where two or more influences are equally close)

###Dungeon Generation Toolkit
-   Full-featured Herringbone Wang Tile dungeon generator
  -   Herringbone Wang Tiles can produce less predictable, more varied dungeon layouts than BSP or other methods
  -   Add water, doors, and traps to a dungeon by specifying the percentage of valid cells to affect
  -   Many different styles of dungeon, from simple rectangular rooms and hallways to sinuous circular caverns
-   Alternate dungeon generators available, such as the one used by the original Rogue
-   Convert dungeons that use `#` for walls to use box drawing characters; this can also be used for graphical walls
-   Convenience functions/constructors let you use the `char[][]` dungeons to easily build other grid things
  -   DijkstraMap will have walls automatically placed in as obstacles if passed a `char[][]` when it's constucted
  -   FOV resistance maps can be generated automatically by DungeonUtility given a `char[][]`
- MixedGenerator can produce maps that combine cave areas with artificial areas, starting in 3.0.0 beta 2.
  - A winding, snake-like path can be produced by SerpentMapGenerator, and has been adapted to generate multi-level dungeons with SerpentDeepMapGenerator. Both of these use MixedGenerator, and so can mix natural rock with worked stone.
-   Big improvements in 3.0.0 beta 5, including...
  -   SectionDungeonGenerator, which can put different dungeon features in different areas (grass or moss may grow in caves but not rooms, for instance)
  -   More maze generators and improvements to existing ones, including an arcade-reminiscent PacMazeGenerator
  -   ModularMapGenerator can produce repeated sections of map, like one common style of room, for military or sci-fi themed areas
  -   DenseRoomMapGenerator produces a cramped, over-filled section of purely rectangular rooms, with periodic breaks for doors.
  -   OrganicMapGenerator makes nice caves using properties of Perlin noise to produce open cave areas and the WobblyLine line drawing class to connect them.
-   SectionDungeonGenerator works with an existing char[][] and an int[][] that stores environment information to place certain features, like grass, only in certain areas, like caves
  -   It also has features for generating large lake-like areas (which could be pits or pools of lava as well, and are configurable), and placing mazes in some areas
-   Placement offers the ability to analyze a map to find cells with certain properties that might be needed for some objects to fit
-   MimicFill is a port of [ConvChain](https://github.com/mxgmn/ConvChain) and allows a square boolean 2D array to be used as a "stylistic basis" for filling an unlimited region with an imitation of the original square's style
-   DetailedMimic is like MimicFill, and is a port of [SynTex](https://github.com/mxgmn/SynTex) but can handle color or
    char information, with the former useful for texture generation and the latter for map style imitation.

###SquidAI Pathfinding code
-   Dijkstra Maps and A* can be used for pathfinding and other purposes.
  -   DijkstraMap provides support for getting to a target, avoiding paths that would make you stop in an invalid cell.
  -   DijkstraMap supports fleeing monsters, optionally sharing one "flee map" for all monsters fleeing the hero.
  -   DijkstraMap can be given a Technique that contains a minimum and maximum range, and an Area of Effect, and it will pathfind to a relatively good place to use that technique.
    -   There are many kinds of Area of Effect (AOE) provided, and given the right information, they can calculate the best place to position that AOE to hit as many targets as possible (not an easy task, but it gets calculated quickly).
  -   DijkstraMap can partially scan an area, stopping once it reaches a given distance.
    -   Since beta 7, DijkstraMap finally incorporates the straightforward optimization of immediately stopping distance
        calculation once an ideal path has been found; the case of scanning the whole map intentionally is unaffected, though
        other optimizations made that faster too
    - It would seem like partial scanning is normally ideal, but there are cases where you can avoid multiple partial scans
      by doing one full scan and using `DijkstraMap.findPathPreScanned(Coord)` until the circumstances change; this approach
      is used in BasicDemo (in the examples of the display module) to handle the highlighted path from the player to the mouse cursor
  -   Several classes support multi-cell creatures, including DijkstraMap
  -   DijkstraMap is currently recommended for pathfinding because it has been optimized more heavily than AStarSearch
    -   Make that, much more heavily, since beta 7; DijkstraMap can, depending on heuristic, beat even top-notch pathfinding algorithms like gdx-ai's indexed A* on long/winding enough paths.
    -   AStarSearch has gotten some optimization, but is still much slower than DijkstraMap in betas 7 and 8
  -   CustomDijkstraMap allows pathfinding on unusual map types, such as ones where you can rotate in your cell but it takes time, or where there are thin walls
    -   See ThinWallDemo and RotationDemo in the display module's tests folder for examples; this is an advanced feature, though

###Fully Documented API
-   Demos of all functionality included
-   EverythingDemo shows off lots of features and is fully documented; a good place to start
-   SquidAIDemo has two AI teams fight each other with large area-of-effect attacks
-   Several other demos are smaller and meant to test individual features, like RotationDemo to test DijkstraMap's pathfinding with facing when changing direction costs a turn
-   SquidSetup produces a sample project with a heavily-documented basic example to get started

###Math Toolkit
-   Custom extension(s) of Random allows drop-in replacement with added features
  -   XorRNG, which is a XorShift128+ RNG
  -   XoRoRNG, which is a xoroshiro128+ RNG using a very recently-published algorithm
    -   XoRoRNG is very fast and has good properties for heavy usage
  -   LightRNG, which is a SplitMix64 RNG
    -   LightRNG can skip ahead or behind in its generated sequence, and it's one of the fastest of the RNGs here, in a virtual tie with XoRoRNG, just behind ThunderRNG
  -   LongPeriodRNG, which is a XorShift1024* RNG (it preferred to replace MersenneTwister for applications like shuffling large sequences, and is much faster)
  -   PermutedRNG is fairly fast (not quite as fast as LightRNG), but has potential statistical advantages
    -   The variant on PermutedRNG called PintRNG is optimized for environments with poor 64-bit math, like GWT
  -   ThunderRNG has somewhat poor statistical properties and period, but is the fastest RNG we have here
  -   IsaacRNG implements the ISAAC cipher as an RNG, which is pretty much the closest thing we have to a secure random number generator, but is much faster than the JDK's SecureRandom
  -   DharmaRNG can be used to make more or less "lucky" RNGs that skew towards high or low results
  -   So can EditRNG, but EditRNG also allows tweaking the "centrality" of the numbers it generates, and has an easier-to-understand expected average (recommended for luck alteration in RNGs)
  -   DeckRNG should be less random during any particular span of random numbers, since it "shuffles" 16 numbers, from low to high, and draws them in a random order.
  -   SobolQRNG produces deterministic results that may seem random, but tend to be more evenly distributed
  -   VanDerCorputQRNG is similar to SobolQRNG, but has many different sequences, and also provides static methods to get random but separated Coords
-   Able to find Bresenham Lines for 2D and 3D coordinates.
  -   Also can use Wu or Elias Lines (antialiased Bresenham Lines)
  -   Also several other line drawing algorithms, including one that only makes orthogonal movements, another with options to make wider lines, and another that wiggles in random directions and makes a randomized line toward a goal.
-   Perlin noise implementation
  -   Used to make Brogue-style "moving" water that works by altering the background lightness
  -   Also used for a world map generator in MetsaMapFactory
  -   The variant called WhirlingNoise is slightly faster and has a little better avoidance of patterns
  -   MerlinNoise is a very aesthetically different kind of noise that tends to look harsh, and is also fairly fast
-   Lots of code for dealing with oddly-shaped or non-contiguous regions in CoordPacker, GreasedRegion, and some other classes
  -   CoordPacker is optimized for memory usage, while GreasedRegion is optimized for speed; both support similar operations but GreasedRegion is usually preferred
  -   These operations start with finding the parts of a 2D array that match some property and storing them as a region...
    -   Go from there to randomly or iteratively sampling points from that region...
    -   From there to inserting or removing points in that region...
    -   To merging, subtracting, or finding the intersection of two regions...
    -   To expanding the area of a region, retracting the area (similar to expanding the parts that are not included in the region)..
    -   To getting the fringe (area that would be included in an expansion, but no the original area) or surface (the part of the original region that would be removed by a retraction)...
    -   To the always-useful flood fill operation, which can be handy for finding reachable areas
  -   CoordPacker is built around a mix of simple and complicated encoding/compression tricks to obtain incredibly small memory usage for many regions
    -   RegionMap can use CoordPacker's regions as keys in a Map-like data structure, and find all regions that overlap with a point
  -   GreasedRegion is a little "fatty" in its memory usage, in the sense of greasy food, but also like "greased lightning" in its speed
    -   It is the recommended way to modify regions by shrinking, expanding, or other related bulk operations
    -   GreasedRegion is heavily used internally, and though it shouldn't need to be used in most code that uses SquidLib, lots of code can benefit from representing regions as objects
  -   Using these classes can be an excellent tool for making interesting maps, and they can be combined with other strategies

###Data Structures
-   SquidLib needed some unusual features in the course of its development, and we have more than a few unusual data structures to fill those needs
  -   OrderedSet (no relation to the libGDX class that shares the name) is an insertion-ordered Set that also allows lookup of keys by their index in the ordering (like a List)
    -   The variant called Arrangement is bidirectional, allowing you to find the index of a key as well as the key at an index
  -   OrderedMap (also no relation to libGDX's class) is a Map that shares OrderedSet's ability to look up keys and values by index.
  -   OrderedSet implements SortedSet, while OrderedMap and Arrangement implement SortedMap, and Arrangement also implements Iterable for its keys
  -   K2 is like OrderedSet but has two key types (A and B) stored in side-by-side collections, but sharing indexes as pairs and allowing the corresponding A to be looked up given a B and vice versa
  -   K2V1 is like K2 but also stores values that don't need to be unique like keys do, but can't be looked up in the same way either (you can use an index, an A key, or a B key to look up a value, though)
  -   All of these mentioned so far can be reordered, possibly with a random ordering produced by RNG
  -   Using indexes instead of always using keys can be useful, but it also means that getting random values from these collections will behave as expected across platforms
  -   Most of these classes allow you to specify how keys are hashed and compared for equality, which permits using arrays as keys (if they aren't modified) or even allows the behavior of IdentityHashMap to be used with insertion ordering (which does allow keys to be modified, but has special requirements for accessing them)
    -   The class CrossHash includes multiple hashing algorithms and predefined IHasher values for various usages, which can be passed to many OrderedMap/Set constructors
  -   SquidLib releases before beta 7 used LinkedHashMap and LinkedHashSet in place of OrderedMap and OrderedSet, and converting from beta 6 to beta 7 or later may entail some changes
-   Maker, a small class in squidlib-util, helps construct these data structures more conveniently
-   The serialization code in squidlib-extra is aware of these data structures and can serialize them efficiently and with less restrictions that normally apply to JSON (you can serialize non-String keys to our dialect of JSON with OrderedMap, for example)
  -   This code also provides ways to compress the JSON output using lz-string encoding, which performs very well here; it uses the tiny library [BlazingChain](https://github.com/tommyettinger/BlazingChain) to do this

###Text generation
-   The FakeLanguageGen class can help imitate a linguistic style or mix multiple languages, then produce text in an imaginary language
  -   There are lots of predefined languages in FakeLanguageGen, some of them based on real-world languages, some based on fictional characters
    -   For imitations of real languages, we have Arabic, English, French, Greek (in Latin or Greek alphabets), Hindi, Inuktitut, Japanese (in Latin alphabet), Nahuatl, Norse, Russian (in Latin and Cyrillic alphabets), Somali, Swahili
    -   For fictional languages, we have "Fantasy" (a mix of several languages), "Fancy Fantasy" (Fantasy with extraneous accents added), "Elf", "Goblin", "Demonic", "Infernal", and the ability to randomly generate whole language styles
    -   You can mix languages and add modifiers to them, like a lisp changing "s" to "th", or the simplifying modifier that can be applied to Norse to make it easier to read for English speakers (changing ð and þ to th, for example)
-   The NaturalLanguageCipher class allows you to take a FakeLanguageGen and use it as a cipher for English text while recognizing prefixes and suffixes, like recognizing that "taker" is composed of "take" and "-er", and using the same suffix for "-er" throughout the cipher.
  -   This is reversible by building a dictionary between English words and their ciphered equivalents.
  -   LanguageCipher is similar, and does a bit less work but doesn't identify prefixes or suffixes, so it will produce completely different words for "take" and "taker"
-   WeightedLetterNamegen is meant to generate names that are similar to ones from a list, with several lists provided
  -   It is good for last names in particular, but also has pre-made lists of first names and some other groups (such as the style of silly-sounding sci-fi names common in the Star Wars universe)
  -   It uses DamerauLevenshteinAlgorithm, a standard way of comparing text for similarity, to evaluate how close words or names are to each other
-   Thesaurus provides a simple enough way to add variety to procedural names/titles/descriptions of various things, by cycling through a shuffled list of synonyms and replacing a word (or category) with a synonym when it encounters one it knows
  -   It is also useful for generating procedural names of factions, like "The Magnanimous La Kingdom" or "Reimui-Shwop Emirate"

###Actively Developed
- Started in 2011 by SquidPony (Eben Howard), SquidLib has since picked up contributions from a number of developers around the world
- Development has accelerated recently as more people started adding code, with Tommy Ettinger working on things that aren't included in most other roguelike libraries, smelc and David Becker each contributing quite a few pull requests that help stability, performance, and code clarity, and still more developers helping by reporting and commenting on issues
- SquidLib 2.9.1 is pretty good
- SquidLib 3.0.0 will be better!
  -  Seriously, 2.x and 3.x are pretty much completely different codebases. The switch to libGDX is significant!
- But, 3.0.0's final release will be major, and so should be expected to *break* API backwards compatibility
  - Any minor releases after 3.0.0 and before 4.0.0 should be expected to *keep* API backwards compatibility, unless a feature is broken or unusable
  - The most significant change in 3.0.0 is the removal of the Swing-based rendering and full transition to the similar, but much faster and more responsive, libGDX renderer
  - 3.0.0-b1 is the last release to contain Swing. If you're porting code that used an earlier version of SquidLib and need Swing for some reason, you may want to stay with the largely-compatible 2.9.1 instead of the very-different 3.0.0-b1.
    - This should also enable SquidLib to be used for rendering on Android/iOS and not only the desktop platforms Swing is limited to
  - There is now a tool that sets up a project for people who want an easy way to handle the dependencies of SquidLib and/or libGDX
    - We now have SquidSetup to automatically handle the setup of a new project that uses SquidLib 3.0.0-b6, including fetching
    dependencies automatically and setting up a project that potentially targets both desktop and Android, potentially
    HTML, and ~~possibly iOS as well~~ (iOS is currently in a bit of a chaotic state with RoboVM's changes, and isn't
    recommended yet; you can use [mobidevelop's RoboVM fork](https://github.com/MobiDevelop/robovm) if you're feeling adventurous).
    - If you already use Maven, Gradle, SBT, Leiningen, or some other dependency manager, upgrading should be easier to the 3.0.0 series
    - If you don't, you should since it can really ease updating, and SquidSetup should handle the hard parts for you.
  - If you use SquidLib's latest version as of April 12, the assets have been moved out of the `squidlib` jar, making the download size smaller, but a freshly-updated SquidSetup has all the latest assets
    and will put them in the correct assets folder (as before) without duplicating them. You can delete any assets you don't use, or move them out of the distributed part of your code.
    - If you don't use SquidSetup, you can download any assets you need from the assets/ folder of this GitHub repo, or
      get all the assets in a .zip file from SquidLib's main folder, [assets.zip](https://github.com/SquidPony/SquidLib/raw/master/assets.zip).
  - [This commit on June 19, 2016](https://github.com/SquidPony/SquidLib/commit/22c770b37b3635c6beacadda6ef71e07c9a55a8e)
    changed usages of LinkedHashMap and LinkedHashSet to OrderedMap and OrderedSet in the squidpony.squidmath package.
    If you use SquidLib versions before that commit (such as beta 6), then update to after that commit (which was some
    time after beta 6), you should expect some find/replace needed throughout your code. The APIs for LinkedHashSet and
    OrderedSet are identical except for the additions OrderedSet makes, and almost the same is true for LinkedHashMap
    (no possible access-ordering like in LinkedHashMap; only insertion-ordering or user-specified-ordering are possible
    in OrderedMap, though the last isn't possible in LinkedHashMap).

Download
--

Download JARs for older versions from the Releases tab, use Maven Central to download the latest version with your
choice of features (display or none, with or without squidlib-extra), or simply use SquidSetup to make a new project
configured the way libGDX prefers to work, and copy in any code you might already have.

Ideally, if you're just starting out you should use SquidSetup. Beta 6 has been added to SquidSetup.
This is [the most recent, beta 6, release of the setup tool](https://github.com/SquidPony/SquidLib/releases/tag/v3.0.0-b6);
beta 8 support is being added to the next build.
This is [the new snapshot setup tool](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.0-LATEST), which is
good if you already understand Gradle, but especially if you want to test new features/fixes as they come in. It
currently isn't named in the most accurate way; it actually downloads a fixed version from JitPack.io, but you can
change that version to the latest commit if you want to use more recent code between releases.

SquidLib is supported as a third-party extension for [gdx-setup by czyzby](https://github.com/czyzby/gdx-setup),
an alternative to the current official libGDX setup that aims to have more features and update more readily. This will
replace the current SquidSetup for stable releases, and it is likely that the next significant change to SquidSetup will
be to make it a simple fork of czyzby's setup tool that includes the assets and also gets the most recent version from
JitPack if possible. The gdx-setup project also allows demo code to be more easily supplied, and some demos are in
progress for SquidLib (one is already present in gdx-setup, but it is made for an earlier version of SquidLib; this is
being remedied and gdx-setup should be ready to use beta 8 very soon).

More information is available on the wiki here on Github, at the page on [Project Setup](https://github.com/SquidPony/SquidLib/wiki/Project-Setup).
It may be somewhat out of date for now.

GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/ (possibly down permanently; server mishaps)

Created by Eben Howard - howard@squidpony.com  
Currently developed by Tommy Ettinger - tommy.ettinger@gmail.com

Additional work has been greatly appreciated by a team of contributors. smelC and David Becker have each done excellent work in improving and modernizing SquidLib in all sorts of ways.
In particular, David Becker needs thanks for handling some very tough work with Maven configuration and encouraging more unit tests (which have caught quite a few bugs),
and smelC has found all sorts of ways to give back to SquidLib as he has worked on [Dungeon Mercenary](http://www.schplaf.org/hgames/), including doing most of the work for the HTML target,
cleaning up and improving the handling of colors, emphasizing more flexible ways to work with display (such as zooming the screen on mobile), and supporting non-monospaced fonts in the display.
Don't be shy about posting issues! Many of SquidLib's biggest and best changes have been motivated by issues posted by users, including the port to Android!
