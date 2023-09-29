package xyz.bluspring.enhancedbookwriting.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    @Unique
    private static final ResourceLocation DELETE_PAGE = new ResourceLocation("enhancedbookwriting", "textures/gui/remove_page.png");

    @Shadow private boolean isSigning;

    @Shadow protected abstract int getNumPages();

    @Shadow @Final private List<String> pages;
    @Shadow private int currentPage;
    @Shadow private boolean isModified;

    @Shadow protected abstract void updateButtonVisibility();

    @Shadow protected abstract void clearDisplayCacheAfterPageChange();

    @Unique private Button appendPage;
    @Unique private Button prependPage;
    @Unique private Button deletePage;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "updateButtonVisibility", at = @At("TAIL"))
    private void ebw$updateButtons(CallbackInfo ci) {
        this.appendPage.visible = this.prependPage.visible = this.deletePage.visible = !this.isSigning;

        this.appendPage.active = this.prependPage.active = this.pages.size() < 100;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;updateButtonVisibility()V"))
    private void ebw$initButtons(CallbackInfo ci) {
        this.appendPage = addRenderableWidget(new Button(this.width / 2 + 70 + 15, 159, 98, 20, Component.literal("Append Page"), (button) -> {
            if (this.getNumPages() < 100) {
                this.pages.add(this.currentPage + 1, "");
                this.isModified = true;
                ++this.currentPage;

                this.updateButtonVisibility();
                this.clearDisplayCacheAfterPageChange();
            }
        }));

        this.prependPage = addRenderableWidget(new Button(this.width / 2 - 185, 159, 98, 20, Component.literal("Prepend Page"), (button) -> {
            if (this.getNumPages() < 100) {
                this.pages.add(this.currentPage, "");
                this.isModified = true;

                this.updateButtonVisibility();
                this.clearDisplayCacheAfterPageChange();
            }
        }));

        this.deletePage = addRenderableWidget(new ImageButton(this.width - 25, this.height - 25, 20, 20, 0, 0, 0, DELETE_PAGE, 16, 16, (button) -> {
            this.pages.remove(this.currentPage);
            if (this.pages.size() == 0)
                this.pages.add("");

            this.isModified = true;

            this.updateButtonVisibility();
            this.clearDisplayCacheAfterPageChange();
        }, Component.literal("Delete Page")));
    }
}
