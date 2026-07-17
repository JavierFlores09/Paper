package org.bukkit.inventory.view;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * An instance of {@link InventoryView} which provides extra methods related to
 * merchant view data.
 */
public interface MerchantView extends InventoryView {

    @NotNull
    @Override
    MerchantInventory getTopInventory();

    /**
     * Gets the merchant that this view is for.
     *
     * @return The merchant that this view uses
     */
    @NotNull
    Merchant getMerchant();

    /**
     * Gets the level displayed in this merchant view.
     *
     * @return the displayed merchant level
     */
    @Range(from = 1, to = 5)
    int getMerchantLevel();

    /**
     * Sets the level displayed in this merchant view.
     *
     * @param level the merchant level, from 1 to 5
     * @throws IllegalArgumentException if the level is outside the range 1 to 5
     */
    void setMerchantLevel(@Range(from = 1, to = 5) int level);

    /**
     * Gets the experience displayed in this merchant view.
     *
     * @return the displayed merchant experience
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    int getMerchantExperience();

    /**
     * Sets the experience displayed in this merchant view.
     *
     * @param experience the merchant experience
     * @throws IllegalArgumentException if the experience is negative
     */
    void setMerchantExperience(@Range(from = 0, to = Integer.MAX_VALUE) int experience);

    /**
     * Gets whether the merchant experience bar is visible in this view.
     *
     * @return whether the experience bar is visible
     */
    boolean isMerchantExperienceBarVisible();

    /**
     * Sets whether the merchant experience bar is visible in this view.
     *
     * @param visible whether the experience bar is visible
     */
    void setMerchantExperienceBarVisible(boolean visible);

    /**
     * Gets whether this merchant view indicates that its offers can be
     * restocked.
     * <p>
     * This only controls whether the client displays the restock information
     * for out-of-stock offers. It does not cause the merchant to restock.
     *
     * @return whether the merchant is shown as able to restock
     */
    boolean canRestock();

    /**
     * Sets whether this merchant view indicates that its offers can be
     * restocked.
     * <p>
     * This only controls whether the client displays the restock information
     * for out-of-stock offers. It does not cause the merchant to restock.
     *
     * @param canRestock whether the merchant is shown as able to restock
     */
    void setRestock(boolean canRestock);
}
