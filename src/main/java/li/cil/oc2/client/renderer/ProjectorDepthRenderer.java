package li.cil.oc2.client.renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import li.cil.oc2.common.block.ProjectorBlock;
import li.cil.oc2.common.blockentity.ProjectorBlockEntity;
import li.cil.oc2.common.bus.device.vm.block.ProjectorDevice;
import li.cil.oc2.jcodec.common.model.Picture;
import li.cil.oc2.jcodec.scale.Yuv420jToRgb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class ProjectorDepthRenderer {
    private static final List<ProjectorBlockEntity> VISIBLE_PROJECTORS = new ArrayList<>();

    private static final Cache<ProjectorBlockEntity, RenderInfo> RENDER_INFO = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(5))
            .removalListener(ProjectorDepthRenderer::handleProjectorRemoved)
            .build();

    private static void handleProjectorRemoved(RemovalNotification<ProjectorBlockEntity, RenderInfo> notification) {
        final RenderInfo info = notification.getValue();
        if (info != null) info.close();
    }

    public static void addProjector(final ProjectorBlockEntity projector) {
        VISIBLE_PROJECTORS.add(projector);
    }

    public static boolean isIsRenderingProjectorDepth() {
        return false;
    }

    public static boolean willRenderProjectorDepth() {
        return false;
    }

    public static void captureMainCameraDepth() {
        // no-op
    }

    @Nullable
    private static DynamicTexture getProjectedTexture(final ProjectorBlockEntity projector) {
        try {
            return RENDER_INFO.get(projector, () -> {
                final DynamicTexture texture = new DynamicTexture(ProjectorDevice.WIDTH, ProjectorDevice.HEIGHT, false);
                texture.upload();
                final RenderInfo info = new RenderInfo(texture);
                projector.setFrameConsumer(info);
                return info;
            }).texture();
        } catch (ExecutionException e) {
            return null;
        }
    }

    @SubscribeEvent
    public static void renderProjectors(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (VISIBLE_PROJECTORS.isEmpty()) return;

        final Minecraft mc = Minecraft.getInstance();
        final Vec3 camPos = mc.getEntityRenderDispatcher().camera.getPosition();

        final var prevShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        PoseStack poseStack = event.getPoseStack();

        for (ProjectorBlockEntity projector : VISIBLE_PROJECTORS) {
            DynamicTexture texture = getProjectedTexture(projector);
            if (texture == null) continue;

            projector.onRendering();

            Direction facing = projector.getBlockState().getValue(ProjectorBlock.FACING);
            Vec3 basePos = Vec3.atCenterOf(projector.getBlockPos());

            poseStack.pushPose();
            poseStack.translate(basePos.x - camPos.x, basePos.y - camPos.y, basePos.z - camPos.z);
            poseStack.mulPose(facing.getRotation());

            final float ratio = ProjectorDevice.HEIGHT / (float) ProjectorDevice.WIDTH;
            final float ratioComplement = 1.0f - ratio;

            final float distance = 5.0f;
            final float scale = distance;

            poseStack.translate(0.5, 0.5 + distance - 0.01, 0.5 + (distance - 1) * 0.2);
            poseStack.translate(0.5 * (scale - 1), 0, 0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
            poseStack.scale(-1.0f * scale, -ratio * scale, 1.0f * scale);

            RenderSystem.setShaderTexture(0, texture.getId());
            Matrix4f matrix = poseStack.last().pose();
            drawQuad(matrix);

            poseStack.popPose();
        }

        RenderSystem.setShader(() -> prevShader);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        VISIBLE_PROJECTORS.clear();
    }

    private static void drawQuad(Matrix4f pose) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(pose, 0, 0, 0).uv(0, 1).color(255, 255, 255, 255).endVertex();
        buffer.vertex(pose, 0, 1, 0).uv(0, 0).color(255, 255, 255, 255).endVertex();
        buffer.vertex(pose, 1, 1, 0).uv(1, 0).color(255, 255, 255, 255).endVertex();
        buffer.vertex(pose, 1, 0, 0).uv(1, 1).color(255, 255, 255, 255).endVertex();
        tesselator.end();
    }

    private record RenderInfo(DynamicTexture texture) implements ProjectorBlockEntity.FrameConsumer {
        private static final ThreadLocal<byte[]> RGB = ThreadLocal.withInitial(() -> new byte[3]);

        public synchronized void close() {
            texture.close();
        }

        @Override
        public synchronized void processFrame(final Picture picture) {
            final NativeImage image = texture.getPixels();
            if (image == null) return;

            final byte[] y = picture.getPlaneData(0);
            final byte[] u = picture.getPlaneData(1);
            final byte[] v = picture.getPlaneData(2);

            int lumaIndex = 0, chromaIndex = 0;
            for (int halfRow = 0; halfRow < ProjectorDevice.HEIGHT / 2; halfRow++, lumaIndex += ProjectorDevice.WIDTH * 2) {
                final int row = halfRow * 2;
                for (int halfCol = 0; halfCol < ProjectorDevice.WIDTH / 2; halfCol++, chromaIndex++) {
                    final int col = halfCol * 2;
                    final int yIndex = lumaIndex + col;
                    final byte cb = u[chromaIndex];
                    final byte cr = v[chromaIndex];
                    setFromYUV420(image, col, row, y[yIndex], cb, cr);
                    setFromYUV420(image, col + 1, row, y[yIndex + 1], cb, cr);
                    setFromYUV420(image, col, row + 1, y[yIndex + ProjectorDevice.WIDTH], cb, cr);
                    setFromYUV420(image, col + 1, row + 1, y[yIndex + ProjectorDevice.WIDTH + 1], cb, cr);
                }
            }

            texture.upload();
        }

        private static void setFromYUV420(final NativeImage image, final int col, final int row, final byte y, final byte cb, final byte cr) {
            final byte[] bytes = RGB.get();
            Yuv420jToRgb.YUVJtoRGB(y, cb, cr, bytes, 0);
            final int r = bytes[0] + 128;
            final int g = bytes[1] + 128;
            final int b = bytes[2] + 128;
            image.setPixelRGBA(col, row, r | (g << 8) | (b << 16) | (0xFF << 24));
        }
    }
}
