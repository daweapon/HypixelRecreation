package dev.daweapon.type.hub.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyEvent;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleClaims;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCSimon extends HypixelNPC {

    public NPCSimon() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<f>Simon", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "qiMy1FIMyajFuYHGimHJ4sbe0iStC8HB6as9hvKaTMpuVyTrW+bD9WFTAsCdD7J4mC6Wo3pLMconk6jhNM1dKSGRZNJf3WOSVZF6qcf1cjAIPsM1M6iC4f+TqIfWDIKQd0MnosuaKeosfS0M3/9CZ+03BJrED2v4FS53QRzjaDRngIj1amWhCE9rezqnilSz406Vt0lcwLlP+8BOkh0gI8rh5JMffrChcIpQKP/anXxnRV7Lbwv4MfT1o/vfh+eHgOIfslvm0tSWsTbKU6AD7P2LX/h4P+Io/axKYoHAwEv20rpoOQD7A1hyVzSoHJJj2cfySFxWuOwEEdNhvh14Pidy+dX7GAjyRTbIszwFkor+kbcZXJPdgAmSDT88chkyZpNgPjiJMMB0FsUYcZx3Hiwl65mOKsbhkf0J8dI3v7RSO6EtNQf5JlRplY4MvAFk88C9RtXtBA+trhWgenkDg8yKNVXoTRoyblq7PzB0XNbLc+Xnb+/Lwk9fOnaTsLQIusgt8lws0rcfIwbXoXdWegPNB6m5DKGZ+rFLjSmDkZ807NM4LUDeoLNxIp9nE3Chswt+G86jK0NAL4xLw/jaf78sLHc/uLGplDav2qQQDmx6M/7DNqcFu6zSAnGq/0RUG2DFuR5JGJrN9EOFyGZHsj85ojhE0zeByOHtf72NnB0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc5Mzc4NDg0NDc4MywKICAicHJvZmlsZUlkIiA6ICJmN2M3N2Q5OTlmMTU0YTY2YTg3ZGM0YTUxZWYzMGQxOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJoeXBpeGVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI0N2MwNTg4NDVmM2I2ZDAzNDhhYWU1NmI2YWExYTYxYzQ2OWQ4NGNiYWJhMDQ4MTY0MDUwYTFkMzM1ZDZlYTAiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3OTEyNzkwZmYxNjRiOTMxOTZmMDhiYTcxZDBlNjIxMjkzMDQ3NzZkMGYzNDczMzRmOGE2ZWFlNTA5ZjhhNTYiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-7.5, 76, 3.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return CenturyEvent.latest() != null;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!CenturyEvent.isActive()) {
            setDialogue(player, CenturyEvent.latest() == null ? "upcoming" : "over");
            return;
        }
        RaffleClaims.claimSimonCake(player, () -> setDialogue(player, "cake"), () -> setDialogue(player, "during"));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("upcoming").lines(
                        "Hey, are you excited about the upcoming <d>Century Celebration <f>event?",
                        "Once it starts, make sure to come talk to me.",
                        "I'll give you some <d>cake<f>!").build(),
                DialogueSet.builder().key("cake").lines(
                        "To celebrate the <d>turn of the century<f>, EVERYONE gets a cake!",
                        "Put it on your island and invite your friends over to share a slice!").build(),
                DialogueSet.builder().key("during").lines(
                        "We hope you enjoy the festivities this year!",
                        "That <6>Sirius <f>fellow helped supply a lot of the rewards this year. What a guy!",
                        "Make sure to talk to <a>Rezzus <f>to learn more about the event!").build(),
                DialogueSet.builder().key("over").lines(
                        "The <d>Century Celebration<f> event is over.",
                        "We hope you enjoyed it!").build()
        };
    }
}
