package com.ytgld;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.Handle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DefaultFramebufferSets
        implements PostEffectProcessor.FramebufferSet {

    public  Handle<Framebuffer> entityOutlineFramebuffer;
    public  Handle<Framebuffer> mainFramebuffer = Handle.empty();
    public static final Identifier MAIN =Identifier.of(YTGLDBetterBeacon.MODID,"main");
    public static final Identifier ENTITY_OUTLINE = Identifier.of(YTGLDBetterBeacon.MODID,"entity_outline");
    @Override
    public Handle<Framebuffer> getOrThrow(Identifier id) {
        if (id .equals(ENTITY_OUTLINE) ) {
            return entityOutlineFramebuffer;
        }else if (id.equals(MAIN)){
            return mainFramebuffer;
        }
        return null;
    }

    @Override
    public void set(Identifier id, Handle<Framebuffer> framebuffer) {
        if (id.equals(ENTITY_OUTLINE) ) {
            entityOutlineFramebuffer = framebuffer;
        }else if (id.equals(MAIN)){
            mainFramebuffer = framebuffer;
        }else {
            System.out.println(id);
        }
    }

    @Nullable
    @Override
    public Handle<Framebuffer> get(Identifier id) {
        if (id .equals(ENTITY_OUTLINE) ) {
            return entityOutlineFramebuffer;
        }else if (id.equals(MAIN)){
            return mainFramebuffer;
        }
        return null;
    }
    public void clear() {
        this.mainFramebuffer = Handle.empty();
        this.entityOutlineFramebuffer = null;
    }
}
