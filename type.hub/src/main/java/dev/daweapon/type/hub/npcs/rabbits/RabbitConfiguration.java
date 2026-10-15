package dev.daweapon.type.hub.npcs.rabbits;

import lombok.AllArgsConstructor;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@AllArgsConstructor
public class RabbitConfiguration extends HumanConfiguration {

    private final DatapointChocolateFactory.EmployeeType type;
    private final String texture;
    private final String signature;
    private final Pos pos;

    @Override
    public String texture(HypixelPlayer player) {
        return texture;
    }

    @Override
    public String signature(HypixelPlayer player) {
        return signature;
    }

    @Override
    public boolean visible(HypixelPlayer player) {
        if (player instanceof SkyBlockPlayer skyBlockPlayer) {
            DatapointChocolateFactory.ChocolateFactoryData data = skyBlockPlayer.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class).getValue();
            return data.getEmployees().containsKey(type);
        }
        return false;
    }

    @Override
    public String[] holograms(HypixelPlayer player) {
        if (player instanceof SkyBlockPlayer skyBlockPlayer) {
            DatapointChocolateFactory.ChocolateFactoryData data = skyBlockPlayer.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class).getValue();
            DatapointChocolateFactory.EmployeeData employee = data.getEmployees().get(type);
            if (employee != null) {
                ChocolateFactoryRank rank = ChocolateFactoryRank.fromLevel(employee.getLevel());
                return new String[]{
                        rank.getHologramLine(employee.getLevel()).serialize(),
                        Text.of("<color:{}>{}", rank.getColor(), type.getName()).serialize(),
                        "<e><l>CLICK"
                };
            }
        }
        return new String[]{
                Text.of("<b>{}", type.getName()).serialize(),
                "<e><l>CLICK"
        };
    }

    @Override
    public String chatName(HypixelPlayer player) {
        if (player instanceof SkyBlockPlayer skyBlockPlayer) {
            DatapointChocolateFactory.ChocolateFactoryData data = skyBlockPlayer.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class).getValue();
            DatapointChocolateFactory.EmployeeData employee = data.getEmployees().get(type);
            if (employee != null) {
                ChocolateFactoryRank rank = ChocolateFactoryRank.fromLevel(employee.getLevel());
                return Text.of("<color:{}>{}", rank.getColor(), type.getName()).serialize();
            }
        }
        return Text.of("{}", type.getName()).serialize();
    }

    @Override
    public Pos position(HypixelPlayer player) {
        return pos;
    }

    @Override
    public boolean looking(HypixelPlayer player) {
        return true;
    }
}
