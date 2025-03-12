package com.ytgld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.BiFunction;


@Environment(EnvType.CLIENT)
public class MRender extends RenderLayer {
    public MRender(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }
    public static ShaderProgramKey register(String id, VertexFormat format) {
        return register(id, format, Defines.EMPTY);
    }
    public static ShaderProgramKey register(String is, VertexFormat format, Defines defines) {
        ShaderProgramKey shaderProgramKey = new ShaderProgramKey(Identifier.of(YTGLDBetterBeacon.MODID,"core/" + is), format, defines);
        ShaderProgramKeys.getAll().add(shaderProgramKey);
        return shaderProgramKey;
    }
    protected static final Target O = new Target("set", () -> {
        if (MinecraftClient.getInstance().worldRenderer instanceof MFramebuffer framebuffer) {
            Framebuffer target = framebuffer.defaultFramebufferSets();

            if (target != null) {
                target.copyDepthFrom(MinecraftClient.getInstance().getFramebuffer());
                target.beginWrite(false);
            }
        }
    }, () -> {
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    });
    private static final BiFunction<Identifier, Boolean, RenderLayer> BEACON_BEAM;
    public static RenderLayer getBeaconBeamM(Identifier texture, boolean translucent) {
        return BEACON_BEAM.apply(texture, translucent);
    }

    static {
        BEACON_BEAM = Util.memoize((texture, affectsOutline) -> {
            MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(
                    BEACON_BEAM_PROGRAM).texture(new RenderPhase.Texture(texture, TriState.FALSE,
                    false)).transparency(affectsOutline ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                    .target(O)
                    .writeMaskState(affectsOutline ? COLOR_MASK : ALL_MASK).build(false);


            return of("beacon_beam", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS, 1536,
                    false, true, multiPhaseParameters);
        });
    }

}
