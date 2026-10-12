package net.swofty.type.skyblockgeneric.bank;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBanker;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBankerDeposit;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class PersonalBankItem {
    public static final String HANDLER_ID = "PERSONAL_BANK_INTERACT";

    private PersonalBankItem() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER_ID, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> use(player, p -> new GUIBanker().open(p)))
                .leftClickHandler((player, item) -> use(player, p -> new GUIBankerDeposit().open(p)))
                .build());
    }

    public static @Nullable String problem(boolean unlocked, long remainingCooldownMillis) {
        if (!unlocked || remainingCooldownMillis > 0) return "Your Personal Bank is locked or still on cooldown!";
        return null;
    }

    private static void use(SkyBlockPlayer player, Consumer<SkyBlockPlayer> open) {
        String problem = problem(PersonalBankService.isUnlocked(player), PersonalBankService.remaining(player));
        if (problem != null) {
            player.sendMessage("<c>" + problem);
            return;
        }
        open.accept(player);
        DatapointBankData.BankData data = PersonalBankService.data(player);
        data.setLastRemoteBankUse(System.currentTimeMillis());
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).setValue(data);
    }
}
