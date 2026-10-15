package dev.daweapon.type.skyblockgeneric.museum;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class MuseumDisplay {
    public abstract MuseumDisplayEntityInformation display(SkyBlockPlayer player,
                                                           MuseumDisplays display,
                                                           boolean empty,
                                                           int position
    );
}