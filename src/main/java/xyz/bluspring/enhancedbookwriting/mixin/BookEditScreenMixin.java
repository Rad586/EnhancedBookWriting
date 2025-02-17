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

    @Unique
    private static final ResourceLocation FLIP_TO_FIRST = new ResourceLocation("enhancedbookwriting", "textures/gui/flip_to_first.png");

    @Unique
    private static final ResourceLocation FLIP_TO_LAST = new ResourceLocation("enhancedbookwriting", "textures/gui/flip_to_last.png");

    @Unique
    private static final ResourceLocation PREPEND_PAGE = new ResourceLocation("enhancedbookwriting", "textures/gui/prepend_page.png");
    @Unique
    private static final ResourceLocation APPEND_PAGE = new ResourceLocation("enhancedbookwriting", "textures/gui/append_page.png");

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
    @Unique private Button flipToFirstPage;
    @Unique private Button flipToLastPage;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "updateButtonVisibility", at = @At("TAIL"))
    private void ebw$updateButtons(CallbackInfo ci) {
        this.appendPage.visible = this.prependPage.visible = this.deletePage.visible = this.flipToFirstPage.visible = this.flipToLastPage.visible = !this.isSigning;

        this.appendPage.active = this.prependPage.active = this.pages.size() < 100;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;updateButtonVisibility()V"))
    private void ebw$initButtons(CallbackInfo ci) {
        // TODO: Image Buttons don't really have the ability to be selected with keyboard-only.
        //       can we fix that somehow?

        this.prependPage = addRenderableWidget(new ImageButton(this.width / 2 - 185 + 50 + 25, 160, 16, 16, 0, 0, 0, PREPEND_PAGE, 16, 16, (button) -> {
            if (this.getNumPages() < 100) {
                this.pages.add(this.currentPage, "");
                this.isModified = true;

                this.updateButtonVisibility();
                this.clearDisplayCacheAfterPageChange();
            }
        },
                (button, poseStack, x, y) -> this.renderTooltip(poseStack, Component.literal("Prepend Page"), x, y),
                Component.translatable("enhancedbookwriting.prepend")
        ));

        this.appendPage = addRenderableWidget(new ImageButton(this.width / 2 + 70 + 15, 160, 16, 16, 0, 0, 0, APPEND_PAGE, 16, 16, (button) -> {
            if (this.getNumPages() < 100) {
                this.pages.add(this.currentPage + 1, "");
                this.isModified = true;
                ++this.currentPage;

                this.updateButtonVisibility();
                this.clearDisplayCacheAfterPageChange();
            }
        },
                (button, poseStack, x, y) -> this.renderTooltip(poseStack, Component.literal("Append Page"), x, y),
                Component.translatable("enhancedbookwriting.append")
        ));

        this.deletePage = addRenderableWidget(new ImageButton(this.width - 25, this.height - 25, 16, 16, 0, 0, 0, DELETE_PAGE, 16, 16, (button) -> {
            this.pages.remove(this.currentPage);
            if (this.pages.isEmpty())
                this.pages.add("");

            if (this.currentPage != 0)
                this.currentPage--;

            this.isModified = true;

            this.updateButtonVisibility();
            this.clearDisplayCacheAfterPageChange();
        },
                (button, poseStack, x, y) -> this.renderTooltip(poseStack, Component.literal("Delete Page"), x, y),
                Component.translatable("enhancedbookwriting.delete")
        ));

        this.flipToFirstPage = addRenderableWidget(new ImageButton(this.width / 2 - 185 + 50 + 25, 140, 16, 16, 0, 0, 0, FLIP_TO_FIRST, 16, 16, (button) -> {
            this.currentPage = 0;

            this.updateButtonVisibility();
            this.clearDisplayCacheAfterPageChange();
        },
                (button, poseStack, x, y) -> this.renderTooltip(poseStack, Component.literal("Flip to First Page"), x, y),
                Component.translatable("enhancedbookwriting.flip_to_first")
        ));

        this.flipToLastPage = addRenderableWidget(new ImageButton(this.width / 2 + 70 + 15, 140, 16, 16, 0, 0, 0, FLIP_TO_LAST, 16, 16, (button) -> {
            this.currentPage = this.pages.size() - 1;

            this.updateButtonVisibility();
            this.clearDisplayCacheAfterPageChange();
        },
                (button, poseStack, x, y) -> this.renderTooltip(poseStack, Component.literal("Flip to Last Page"), x, y),
                Component.translatable("enhancedbookwriting.flip_to_last")
        ));
    }
}
