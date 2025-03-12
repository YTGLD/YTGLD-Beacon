package com.ytgld.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ytgld.DefaultFramebufferSets;
import com.ytgld.MFramebuffer;
import com.ytgld.YTGLDBetterBeacon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements MFramebuffer{
    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void scheduleTerrainUpdate();

    @Shadow @Final private DefaultFramebufferSet framebufferSet;
    @Unique
    private  Framebuffer entityOutlineFramebuffer;
    @Unique
    private final DefaultFramebufferSets defaultFramebufferSets  = new DefaultFramebufferSets();
    @Inject(method = "close", at = @At(value = "RETURN"))
    private void close(CallbackInfo ci) {
        if (this.entityOutlineFramebuffer != null) {
            this.entityOutlineFramebuffer.delete();
        }
        if (entityOutlineFramebuffer != null) {
            entityOutlineFramebuffer.delete();
        }
    }
    @Inject(method = "loadEntityOutlinePostProcessor", at = @At(value = "RETURN"))
    private void loadEntityOutlinePostProcessor(CallbackInfo ci) {
        if (this.entityOutlineFramebuffer != null) {
            this.entityOutlineFramebuffer.delete();
        }
        this.entityOutlineFramebuffer = new SimpleFramebuffer(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), true);
        this.entityOutlineFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    }
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At(value = "RETURN"))
    private void drawEntityOutlinesFramebuffer(CallbackInfo ci) {
        if (!this.client.gameRenderer.isRenderingPanorama()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE
            );
            this.entityOutlineFramebuffer.drawInternal(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    @Inject(method = "onResized", at = @At(value = "RETURN"))
    private void onResized(int width, int height, CallbackInfo ci) {
        this.scheduleTerrainUpdate();
        if (this.entityOutlineFramebuffer != null) {
            this.entityOutlineFramebuffer.resize(width, height);
        }
    }
    @Inject(method = "renderMain", at = @At(value = "RETURN"))
    private void renderMain(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Fog fog, boolean renderBlockOutline, boolean renderEntityOutlines, RenderTickCounter renderTickCounter, Profiler profiler, CallbackInfo ci) {
        if (this.entityOutlineFramebuffer != null) {
            this.defaultFramebufferSets.entityOutlineFramebuffer = frameGraphBuilder.createObjectNode("entity_outline", this.entityOutlineFramebuffer);
        }
    }

    @Inject(method = "renderMain", at = @At(value = "RETURN"))
    private void renderMain2INVOKE_ASSIGN(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Fog fog, boolean renderBlockOutline, boolean renderEntityOutlines, RenderTickCounter renderTickCounter, Profiler profiler, CallbackInfo ci) {
        RenderPass renderPass = frameGraphBuilder.createPass("c_blur");
        this.defaultFramebufferSets.entityOutlineFramebuffer = renderPass.transfer(this.defaultFramebufferSets.entityOutlineFramebuffer);

        Handle<Framebuffer> handle5 = this.defaultFramebufferSets.entityOutlineFramebuffer;

        handle5.get().setClearColor(0,0,0,0);
        handle5.get().clear();
        this.framebufferSet.mainFramebuffer.get().beginWrite(false);
    }
    @Inject(method = "renderMain", at = @At(value = "RETURN"))
    private void renderMains(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Fog fog, boolean renderBlockOutline, boolean renderEntityOutlines, RenderTickCounter renderTickCounter, Profiler profiler, CallbackInfo ci) {

        int i = this.client.getFramebuffer().textureWidth;
        int j = this.client.getFramebuffer().textureHeight;

        PostEffectProcessor postEffectProcessor2 = this.client.getShaderLoader().loadPostEffect(YTGLDBetterBeacon.POST, Set.of(DefaultFramebufferSets.MAIN,DefaultFramebufferSets.ENTITY_OUTLINE));
        if (postEffectProcessor2 != null) {
            postEffectProcessor2.render(frameGraphBuilder, i, j, this.defaultFramebufferSets);
        }

    }
    @Override
    public Framebuffer defaultFramebufferSets() {
        return entityOutlineFramebuffer;
    }
}
