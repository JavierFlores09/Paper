package org.bukkit.craftbukkit.inventory.view;

import com.google.common.base.Preconditions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.view.MerchantView;
import org.jetbrains.annotations.NotNull;

public class CraftMerchantView extends CraftInventoryView<MerchantMenu, MerchantInventory> implements MerchantView {

    private final net.minecraft.world.item.trading.Merchant trader;

    public CraftMerchantView(final HumanEntity player, final MerchantInventory viewing, final MerchantMenu container, final net.minecraft.world.item.trading.Merchant trader) {
        super(player, viewing, container);
        this.trader = trader;
        this.container.setMerchantLevel(trader instanceof Villager villager ? villager.getVillagerData().level() : 1);
        this.container.setShowProgressBar(trader.showProgressBar());
        this.container.setCanRestock(trader.canRestock());
    }

    @NotNull
    @Override
    public Merchant getMerchant() {
        return this.trader.getCraftMerchant();
    }

    @Override
    public int getMerchantLevel() {
        return this.container.getTraderLevel();
    }

    @Override
    public void setMerchantLevel(final int level) {
        Preconditions.checkArgument(1 <= level && level <= 5, "Merchant level must be between 1 and 5, was %s", level);
        this.container.setMerchantLevel(level);
        this.updateMerchantOffers();
    }

    @Override
    public int getMerchantExperience() {
        return this.container.getTraderXp();
    }

    @Override
    public void setMerchantExperience(final int experience) {
        Preconditions.checkArgument(experience >= 0, "Merchant experience must not be negative, was %s", experience);
        if (this.trader instanceof Villager villager) {
            villager.setVillagerXp(experience);
        } else {
            this.container.setXp(experience);
        }
        this.updateMerchantOffers();
    }

    @Override
    public boolean isMerchantExperienceBarVisible() {
        return this.container.showProgressBar();
    }

    @Override
    public void setMerchantExperienceBarVisible(final boolean visible) {
        this.container.setShowProgressBar(visible);
        this.updateMerchantOffers();
    }

    @Override
    public boolean canRestock() {
        return this.container.canRestock();
    }

    @Override
    public void setRestock(final boolean canRestock) {
        this.container.setCanRestock(canRestock);
        this.updateMerchantOffers();
    }

    public void sendMerchantOffers() {
        final ServerPlayer player = ((CraftPlayer) this.getPlayer()).getHandle();
        if (!this.trader.getOffers().isEmpty()) {
            player.sendMerchantOffers(this.container.containerId, this.trader.getOffers(), this.getMerchantLevel(), this.getMerchantExperience(), this.isMerchantExperienceBarVisible(), this.canRestock());
        }
    }

    private void updateMerchantOffers() {
        final ServerPlayer player = ((CraftPlayer) this.getPlayer()).getHandle();
        if (player.containerMenu == this.container) {
            this.sendMerchantOffers();
        }
    }
}
