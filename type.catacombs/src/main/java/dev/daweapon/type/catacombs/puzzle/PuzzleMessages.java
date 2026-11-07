package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Hypixel's puzzle chat. The solved lines are captured ("PUZZLE SOLVED! daweapon tied Tic Tac Toe! Good job!" and
 * "... wasn't fooled by Hugo! Good job!"); the fail lines follow the same shape and are not captured.
 */
public final class PuzzleMessages {
    private static final String GOOD_JOB = "<4>G<c>o<6>o<e>d<a> <2>j<b>o<3>b<5>!";
    private static final String YIKES = "<4>Y<c>i<6>k<e>e<a>s<2>!";

    private PuzzleMessages() {
    }

    public static String solved(SkyBlockPlayer player, String what) {
        return "<a><l>PUZZLE SOLVED!</l> <6>" + player.getUsername() + " <e>" + what + " " + GOOD_JOB;
    }

    public static String failed(SkyBlockPlayer player, String what) {
        return "<c><l>PUZZLE FAIL!</l> <6>" + player.getUsername() + " <e>" + what + " " + YIKES;
    }

    public static String npc(String name, String line) {
        return "<e>[NPC] <c>" + name + "<f>: " + line;
    }

    public static String npcName(String name) {
        return "<c>" + name + "<f>";
    }
}
