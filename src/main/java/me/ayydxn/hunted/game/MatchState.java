package me.ayydxn.hunted.game;

/**
 * Represents the different states in which a match can be in at any given time.
 */
public enum MatchState
{
    /**
     * The match is starting and any necessary initialization is being done.
     */
    STARTING,

    /**
     * The match has started and normal gameplay is currently underway.
     */
    ACTIVE,

    /*
    * The match is ending and any necessary cleanup is being done.
    */
    ENDING,

    /**
     * The match is ended and nothing is happening.
     */
    ENDED
}
